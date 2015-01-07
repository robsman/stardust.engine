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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Function;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.web.dms.DmsContentServlet.ExecutionServiceProvider;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailPartitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.QueryServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
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

   /**
    * ThreadLocal
    */
   private DynamicServletConfiguration()
   {
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

   private Map<String, Serializable> getPreferenceMap(final String partitionId)
   {
      @SuppressWarnings("unchecked")
      Map<String, Serializable> mergedPreferenceMap = (Map<String, Serializable>) getForkingService().isolate(
            new Function<Map<String, Serializable>>()
            {
               @Override
               protected Map<String, Serializable> invoke()
               {
                  // bind thread to given partition
                  ParametersFacade.pushLayer(Collections.singletonMap(
                        SecurityProperties.CURRENT_PARTITION,
                        AuditTrailPartitionBean.findById(partitionId)));

                  try
                  {
                     Preferences prefs = null;
                     Preferences defaultPrefs = null;
                     // Precondition: Credentials and session properties are set.
                     try
                     {

                        QueryService qs = new QueryServiceImpl();

                        prefs = qs.getPreferences(PreferenceScope.PARTITION,
                              MODULE_ID_WEB_SERVICE, PREFERENCES_ID_PROCESS_INTERFACE);
                        defaultPrefs = qs.getPreferences(PreferenceScope.DEFAULT,
                              MODULE_ID_WEB_SERVICE, PREFERENCES_ID_PROCESS_INTERFACE);
                     }
                     catch (RuntimeException e)
                     {
                        throw e;
                     }

                     return mergePreferences(defaultPrefs, prefs);
                  }
                  finally
                  {
                     ParametersFacade.popLayer();
                  }
               }
            });
      
      return mergedPreferenceMap;
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

   private ForkingService getForkingService()
   {
      ForkingServiceFactory factory = null;
      ForkingService forkingService = null;
      factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory == null)
      {
         List<ExecutionServiceProvider> exProviderList = ExtensionProviderUtils.getExtensionProviders(ExecutionServiceProvider.class);
         if (exProviderList.size() == 1)
         {
            ExecutionServiceProvider executionServiceProvider = exProviderList.get(0);

            forkingService = executionServiceProvider.getExecutionService("ejb");
         }
         if (forkingService == null)
         {
            for (ExecutionServiceProvider executionServiceProvider : exProviderList)
            {
               forkingService = executionServiceProvider.getExecutionService(DynamicCXFServlet.getClientContext());
               if (forkingService != null)
               {
                  break;
               }
            }
         }
      }
      else
      {
         forkingService = factory.get();
      }
      return forkingService;
   }

}