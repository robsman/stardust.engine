/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.preferences;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.dto.ModelReconfigurationInfoDetails;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.ReconfigurationInfo;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.ValidationConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


public class PreferenceStorageFactory
{
   public static IPreferenceStorageManager getCurrent()
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      
      if (rtEnv == null)
      {
         rtEnv = new BpmRuntimeEnvironment(null);
         PropertyLayerProviderInterceptor.setCurrent(rtEnv);         
      }
      
      IPreferenceStorageManager store = rtEnv.getPreferenceStore();
      if (store == null)
      {
         store = new PreferenceStorageManager(createHandlers());
         rtEnv.setPreferenceStore(store);
      }

      return store;
   }

   private static List<PreferenceChangeHandler> createHandlers()
   {
      PreferenceChangeHandler configurationVariableHandler = new PreferenceChangeHandler(
            ConfigurationVariableUtils.CONFIGURATION_VARIABLES);

      configurationVariableHandler.addListener(new IPreferenceChangeListener()
      {

         public List<ReconfigurationInfo> onPreferenceChange(PreferenceChangeEvent event)
               throws PreferenceChangeException
         {
            Map<String, Serializable> preferences = event.getChangedPreferences()
                  .getPreferences();
            String modelId = event.getChangedPreferences().getPreferencesId();

            List<ReconfigurationInfo> infos = CollectionUtils.newArrayList();
            if (preferences != null)
            {
               ModelManager modelManager = ModelManagerFactory.getCurrent();
               List<IModel> modelsForId = modelManager.getModelsForId(modelId);
                              
               for (IModel model : modelsForId)
               {
                  ModelReconfigurationInfoDetails details = new ModelReconfigurationInfoDetails(model);
                  details.setSuccess(true);
                  
                  String xml = LargeStringHolder.getLargeString(model.getModelOID(),
                        ModelPersistorBean.class);

                  IModel newModel = new DefaultXMLReader(true,
                        new ValidationConfigurationVariablesProvider(preferences))
                        .importFromXML(new StringReader(xml));

                  // Add property to property layer to hold information that model is revalidated
                  PropertyLayer layer = null;
                  try {
                     Map<String, Object> props = new HashMap<String, Object>();
                     props.put(ModelElementBean.PRP_REVALIDATE_ELEMENTS, true);
                     layer = ParametersFacade.pushLayer(props);
                     
                     List<Inconsistency> inconsistencies = newModel.checkConsistency();
                     details.addInconsistencies(inconsistencies);
                  }                  
                  finally {
                     if(layer != null)
                     {
                        ParametersFacade.popLayer();
                     }
                  }


                  if (( !event.isForceEnabled() && details.hasWarnings())
                        || details.hasErrors())
                  {
                     details.setSuccess(false);
                  }
                  
                  infos.add(details);
               }
            }
            return infos;
         }
      });

      return Collections.singletonList(configurationVariableHandler);
   }
}
