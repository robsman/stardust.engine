/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.QueryService;
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

   private WebServiceEnv wsEnv;

   /**
    * Returns a service factory that has been created based on the parameters given with
    * the HTTP request.
    *
    * @return the service factory
    */
   protected final WebServiceEnv environment()
   {
      if (wsEnv == null)
      {
         WebServiceEnv.removeCurrent();
         final String[] userPwd = usernamePassword();
         WebServiceEnv.setCurrentCredentials(userPwd[0], userPwd[1]);
         WebServiceEnv.setCurrentSessionProperties(properties());
         wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      }
      return wsEnv;
   }

   /**
    * @return the modelId as passed in query parameter 'stardust-bpm-model'
    */
   protected String getModelId()
   {
      return StringUtils.isEmpty(modelId) ? getDefaultModelId(getPartitionId()) : modelId;
   }

   protected String getPartitionId()
   {
      return StringUtils.isEmpty(partition) ? PredefinedConstants.DEFAULT_PARTITION_ID : partition;
   }

   private String getDefaultModelId(String partitionId)
   {
      Map<String, Serializable> preferenceMap = getPreferenceMap(partitionId);
      return (String) preferenceMap.get("DynamicEndpoint.DefaultModelId");
   }

   protected Model getModel()
   {
      String modelId = getModelId();
      Model model = environment().getActiveModel(modelId);
      if (null == model)
      {
         String errorMsg = "No active model was found for modelId '" + modelId + "'.";
         ResponseBuilder responseBuilder = Response.status(Status.NOT_FOUND).entity(errorMsg);
         throw new WebApplicationException(responseBuilder.build());
      }
      return model;
   }

   private Map<String, Serializable> getPreferenceMap(String partitionId)
   {
      Preferences prefs = null;
      Preferences defaultPrefs = null;
      if (partitionId != null)
      {
         QueryService qs = environment().getServiceFactory().getQueryService();
         prefs = qs.getPreferences(
               PreferenceScope.PARTITION, MODULE_ID_WEB_SERVICE,
               PREFERENCES_ID_PROCESS_INTERFACE);
         defaultPrefs = qs.getPreferences(
               PreferenceScope.DEFAULT, MODULE_ID_WEB_SERVICE,
               PREFERENCES_ID_PROCESS_INTERFACE);
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
      int idx = decodedUsernamePwd.indexOf(':');
      final String[] usernamePwd = {"", ""};
      if (idx == -1) 
      {
         usernamePwd[0] = decodedUsernamePwd;
      } 
      else 
      {
         usernamePwd[0] = decodedUsernamePwd.substring(0, idx);
         if (idx < (decodedUsernamePwd.length() - 1)) 
         {
            usernamePwd[1] = decodedUsernamePwd.substring(idx + 1);
         }
      }
      if (StringUtils.isEmpty(usernamePwd[0]))
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
