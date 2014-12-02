package org.eclipse.stardust.test.api.util;

import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;

/**
 * <p>
 * This class allows to specify {@link DeploymentOptions} in addition to the <i>Model(s)</i> to be deployed.
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
    * @param modelNames the <i>Model(s)</i> to be deployed, identified by <i>Model Name</i>, must not be {@code null} or empty
    */
   public TestModels(final DeploymentOptions deploymentOptions, final String ... modelNames)
   {
      this.deploymentOptions = deploymentOptions;
      if (modelNames == null || modelNames.length == 0)
      {
         throw new IllegalArgumentException("At least one model must be specified.");
      }

      this.modelNames = modelNames;
   }

   /**
    * <p>
    * Initializes a new instance of {@link TestModels} with the parameters given.
    * </p>
    *
    * @param modelNames the <i>Model(s)</i> to be deployed, identified by <i>Model Name</i>, must not be {@code null} or empty
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
    * @return the <i>Model Name(s)</i> associated with this object
    */
   public String[] modelNames() {
      return modelNames;
   }
}
