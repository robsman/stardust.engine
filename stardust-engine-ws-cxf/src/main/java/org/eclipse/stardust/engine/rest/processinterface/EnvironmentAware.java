/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.rest.processinterface;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.web.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.ws.WebServiceEnv;



/**
 * <p>
 * This class is responsible for extracting information about the environment
 * from the HTTP request and providing it in a convenient way. This
 * information includes
 * <ul>
 *   <li>the Username/Password pair from the HTTP authorization header,</li>
 *   <li>the EPM partition,</li>
 *   <li>the EPM realm, and</li>
 *   <li>the EPM domain.</li>
 * </ul>
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class EnvironmentAware
{
   private static final String BASIC_LITERAL = "Basic";

   /**
    * Preference Store moduleId for preferences concerning web services
    */
   private static final String MODULE_ID_WEB_SERVICE = "web-service";

   /**
    * Preference Store preferencesId for process-interface specific preferences.
    */
   private static final String PREFERENCES_ID_PROCESS_INTERFACE = "process-interface";

   @HeaderParam("Authorization")
   private String authHeader;

   @QueryParam("stardust-bpm-partition")
   private String partition;

   @QueryParam("stardust-bpm-realm")
   private String realm;

   @QueryParam("stardust-bpm-domain")
   private String domain;

   @QueryParam("stardust-bpm-model")
   private String modelId;

   private ServiceFactory serviceFactory;

   /**
    * Returns a service factory that has been created based on the parameters given with
    * the HTTP request.
    *
    * @return the service factory
    */
   protected final ServiceFactory serviceFactory()
   {
      if (serviceFactory == null)
      {
         final String[] userPwd = usernamePassword();
         serviceFactory = ServiceFactoryLocator.get(userPwd[0], userPwd[1], properties());
      }
      return serviceFactory;
   }

   /**
    * @return the modelId as passed in query parameter 'stardust-bpm-model'
    */
   protected String getModelId()
   {
      if (StringUtils.isEmpty(modelId))
      {
         return getDefaultModelId(getPartitionId());
      }

      return modelId;
   }

   protected String getPartitionId()
   {
      String partitionId = partition;
      if (StringUtils.isEmpty(partitionId))
      {
         partitionId = PredefinedConstants.DEFAULT_PARTITION_ID;
      }
      return partitionId;
   }

   private String getDefaultModelId(String partitionId)
   {
      Map<String, Serializable> preferenceMap = getPreferenceMap(partitionId);

      return (String) preferenceMap.get("DynamicEndpoint.DefaultModelId");
   }

   private Map<String, Serializable> getPreferenceMap(String partitionId)
   {
      Preferences prefs = null;
      Preferences defaultPrefs = null;
      if (partitionId != null)
      {

         try
         {
            prefs = serviceFactory().getQueryService().getPreferences(
                  PreferenceScope.PARTITION, MODULE_ID_WEB_SERVICE,
                  PREFERENCES_ID_PROCESS_INTERFACE);
            defaultPrefs = serviceFactory().getQueryService().getPreferences(
                  PreferenceScope.DEFAULT, MODULE_ID_WEB_SERVICE,
                  PREFERENCES_ID_PROCESS_INTERFACE);
         }
         catch (RuntimeException e)
         {
            WebServiceEnv.removeCurrent();
            throw e;
         }

      }
      return mergePreferences(defaultPrefs, prefs);
   }

   private Map<String, Serializable> mergePreferences(Preferences defaultPrefs,
         Preferences prefs)
   {
      Map<String, Serializable> ret = CollectionUtils.newMap();

      if (defaultPrefs != null)
      {
         ret.putAll(defaultPrefs.getPreferences());
      }
      if (prefs != null)
      {
         ret.putAll(prefs.getPreferences());
      }
      return ret;
   }

   private Map<String, ? extends Serializable> properties()
   {
      final Map<String, Serializable> properties = new HashMap<String, Serializable>();
      if ( !StringUtils.isEmpty(partition))
      {
         properties.put(SecurityProperties.PARTITION, partition);
      }
      if ( !StringUtils.isEmpty(realm))
      {
         properties.put(SecurityProperties.REALM, realm);
      }
      if ( !StringUtils.isEmpty(domain))
      {
         properties.put(SecurityProperties.DOMAIN, domain);
      }

      return properties;
   }

   private String[] usernamePassword()
   {
      final String decodedUsernamePwd = decodeHeader();
      final String[] usernamePwd = decodedUsernamePwd.split(":", -1);

      if (StringUtils.isEmpty(usernamePwd[0]) || usernamePwd.length < 2)
      {
         throw new UnauthorizedException();
      }

      return usernamePwd;
   }

   private String decodeHeader()
   {
      if (authHeader == null)
      {
         throw new UnauthorizedException();
      }
      if ( !authHeader.startsWith(BASIC_LITERAL))
      {
         final String errorMsg = "Only HTTP Basic Authentication supported";
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
      }

      final String encodedUsernamePwd = authHeader.replaceFirst(BASIC_LITERAL, "").trim();
      final byte[] decodedUsernamePwd = Base64.decode(encodedUsernamePwd.getBytes());
      return new String(decodedUsernamePwd);
   }

   /**
    * This exception will be thrown if the HTTP authorization header does not
    * contain a username/password pair.
    *
    * @author Nicolas.Werlein
    */
   public static final class UnauthorizedException extends WebApplicationException
   {
      private static final long serialVersionUID = -3034845631125374732L;

      public UnauthorizedException()
      {
         super(Response.status(Status.UNAUTHORIZED)
               .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Eclipse Process Manager\"")
               .build());
      }
   }
}
