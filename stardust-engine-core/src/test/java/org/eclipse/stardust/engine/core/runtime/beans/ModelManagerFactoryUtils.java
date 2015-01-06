package org.eclipse.stardust.engine.core.runtime.beans;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

/**
 * <p>
 * Provides utility methods concerning {@link ModelManagerFactory}.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class ModelManagerFactoryUtils
{
   /**
    * <p>
    * Initializes the current property layer with a given model manager.
    * </p>
    * 
    * @param modelManager the model manager to set
    */
   public static void initRtEnvWithModelManager(final ModelManager modelManager)
   {
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      if (rtEnv == null || rtEnv.getModelManager() == null)
      {
         final PropertyLayer propertyLayer = PropertyLayerProviderInterceptor.BPM_RT_ENV_LAYER_FACTORY_NOPREDECESSOR.createPropertyLayer(null);
         
         assertThat(propertyLayer, instanceOf(BpmRuntimeEnvironment.class));
         PropertyLayerProviderInterceptor.setCurrent((BpmRuntimeEnvironment) propertyLayer);
         
         rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      }
      rtEnv.setModelManager(modelManager);
   }
}
