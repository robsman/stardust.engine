package org.eclipse.stardust.test.api.util;

import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;

/**
 * <p>
 * This class allows to specify {@link DeploymentOptions} in addition to the model(s) to be deployed.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestModels
{
   private final DeploymentOptions deploymentOptions;
   private final String[] modelNames;

   /**
    * <p>
    * Initializes a new instance of {@link TestModels} with the parameters given.
    * </p>
    *
    * @param deploymentOptions the {@link DeploymentOptions} to use when deploying the models specified, may be {@code null}
    * @param modelNames the model(s)to be deployed, identified by model name, must not be {@code null} or empty
    */
   public TestModels(final DeploymentOptions deploymentOptions, final String ... modelNames)
   {
      /* deployment options may be null */
      if (modelNames == null || modelNames.length == 0)
      {
         throw new IllegalArgumentException("At least one model must be specified.");
      }

      this.deploymentOptions = deploymentOptions;
      this.modelNames = modelNames;
   }

   /**
    * <p>
    * Initializes a new instance of {@link TestModels} with the parameters given.
    * </p>
    *
    * @param modelNames the model(s)to be deployed, identified by model name, must not be {@code null} or empty
    */
   public TestModels(final String ... modelNames)
   {
      this(null, modelNames);
   }

   /**
    * @return the {@link DeploymentOptions} associated with this object
    */
   public DeploymentOptions deploymentOptions()
   {
      return deploymentOptions;
   }

   /**
    * @return the model name(s) associated with this object
    */
   public String[] modelNames() {
      return modelNames;
   }
}
