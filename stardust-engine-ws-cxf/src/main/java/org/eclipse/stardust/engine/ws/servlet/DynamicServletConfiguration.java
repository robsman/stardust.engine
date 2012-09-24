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
package org.eclipse.stardust.engine.ws.servlet;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.eclipse.stardust.engine.ws.processinterface.AuthMode;



public class DynamicServletConfiguration
{
   private static final boolean PROCESS_SERVICE_HTTP_BASIC_AUTH_ENABLED = true;

   private static final String PROCESS_SERVICE_HTTP_BASIC_AUTH = "ProcessServiceHttpBasicAuth";

   private static final boolean PROCESS_SERVICE_HTTP_BASIC_AUTH_SSL_ENABLED = true;

   private static final String PROCESS_SERVICE_HTTP_BASIC_AUTH_SSL = "ProcessServiceHttpBasicAuthSsl";

   private static final boolean PROCESS_SERVICE_WSS_USERNAME_TOKEN_ENABLED = true;

   private static final String PROCESS_SERVICE_WSS_USERNAME_TOKEN = "ProcessServiceWssUsernameToken";

   /**
    * Preference Store moduleId for preferences concerning web services
    */
   public static final String MODULE_ID_WEB_SERVICE = "web-service";

   /**
    * Preference Store preferencesId for process-interface specific preferences.
    */
   public static final String PREFERENCES_ID_PROCESS_INTERFACE = "process-interface";

   private static final ThreadLocal<DynamicServletConfiguration> INSTANCE = new ThreadLocal<DynamicServletConfiguration>();

   private String currentPartitionId;

   private Map<String, Serializable> mergedPreferenceMap;

   /**
    * ThreadLocal
    */
   private DynamicServletConfiguration()
   {
      currentPartitionId = null;
   }

   /**
    * ThreadLocal
    * 
    * @return the current instance held in a ThreadLocal.
    */
   public static DynamicServletConfiguration getCurrentInstance()
   {
      DynamicServletConfiguration instance = INSTANCE.get();
      if (instance == null)
      {
         instance = new DynamicServletConfiguration();
         INSTANCE.set(instance);
      }
      return instance;
   }

   public String getEndpointName(String partitionId, AuthMode authMode)
   {
      String endpointName = null;
      Map<String, Serializable> preferences = getPreferenceMap(partitionId);

      switch (authMode)
      {
      case HttpBasicAuth:
         endpointName = (String) preferences.get("DynamicEndpoint.HttpBasicAuth.Name");
         if (StringUtils.isEmpty(endpointName))
         {
            endpointName = PROCESS_SERVICE_HTTP_BASIC_AUTH;
         }
         break;
      case HttpBasicAuthSsl:
         endpointName = (String) preferences.get("DynamicEndpoint.HttpBasicAuthSsl.Name");
         if (StringUtils.isEmpty(endpointName))
         {
            endpointName = PROCESS_SERVICE_HTTP_BASIC_AUTH_SSL;
         }
         break;
      case WssUsernameToken:
         endpointName = (String) preferences.get("DynamicEndpoint.WssUsernameToken.Name");
         if (StringUtils.isEmpty(endpointName))
         {
            endpointName = PROCESS_SERVICE_WSS_USERNAME_TOKEN;
         }
         break;
      }
      return endpointName;
   }

   public boolean isEndpointEnabled(String partitionId, AuthMode authMode)
   {
      boolean enabled = false;
      Map<String, Serializable> preferences = getPreferenceMap(partitionId);

      switch (authMode)
      {
      case HttpBasicAuth:
         Boolean en = (Boolean) preferences.get("DynamicEndpoint.HttpBasicAuth.Enable");
         if (en != null)
         {
            enabled = en;
         }
         else
         {
            enabled = PROCESS_SERVICE_HTTP_BASIC_AUTH_ENABLED;
         }
         break;
      case HttpBasicAuthSsl:
         Boolean en2 = (Boolean) preferences.get("DynamicEndpoint.HttpBasicAuthSsl.Enable");
         if (en2 != null)
         {
            enabled = en2;
         }
         else
         {
            enabled = PROCESS_SERVICE_HTTP_BASIC_AUTH_SSL_ENABLED;
         }
         break;
      case WssUsernameToken:
         Boolean en3 = (Boolean) preferences.get("DynamicEndpoint.WssUsernameToken.Enable");
         if (en3 != null)
         {
            enabled = en3;
         }
         else
         {
            enabled = PROCESS_SERVICE_WSS_USERNAME_TOKEN_ENABLED;
         }
         break;
      }
      return enabled;
   }

   public String getDefaultModelId(String partitionId)
   {
      Map<String, Serializable> preferenceMap = getPreferenceMap(partitionId);

      return (String) preferenceMap.get("DynamicEndpoint.DefaultModelId");
   }

   private Map<String, Serializable> getPreferenceMap(String partitionId)
   {
      Preferences prefs = null;
      Preferences defaultPrefs = null;
      if (partitionId != null && !partitionId.equals(currentPartitionId))
      {
         // Precondition: Credentials and session properties are set.
         try
         {

            ServiceFactory serviceFactory = WebServiceEnv.currentWebServiceEnvironment()
                  .getServiceFactory();
            prefs = serviceFactory.getQueryService().getPreferences(
                  PreferenceScope.PARTITION, MODULE_ID_WEB_SERVICE,
                  PREFERENCES_ID_PROCESS_INTERFACE);
            defaultPrefs = serviceFactory.getQueryService().getPreferences(
                  PreferenceScope.DEFAULT, MODULE_ID_WEB_SERVICE,
                  PREFERENCES_ID_PROCESS_INTERFACE);
         }
         catch (RuntimeException e)
         {
            WebServiceEnv.removeCurrent();
            throw e;
         }
         this.currentPartitionId = partitionId;

         this.mergedPreferenceMap = mergePreferences(defaultPrefs, prefs);
      }
      return this.mergedPreferenceMap;
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

}