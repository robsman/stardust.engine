/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;

/**
 * <p>
 * This class facilitates the task of
 * <ul>
 *    <li>reading a model from the file system, and</li>
 *    <li>deploying the read model to the Stardust Engine Runtime.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Furthermore, it caches read models so that a particular model will only
 * be read once from the file system.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ModelDeployer
{
   private static final String MODEL_FOLDER = "models";
   
   private static final Map<String, DeploymentElement> DEPLOYMENT_ELEMENTS = newHashMap();

   /**
    * <p>
    * Deploys the model with the given name to the Stardust Engine Runtime.
    * </p>
    * 
    * <p>
    * Models to be deployed with this method must reside in the folder
    * <code>models</code> on the classpath.
    * </p>
    * 
    * <p>
    * This method ensures that either all models or none at all will be deployed.
    * </p>
    * 
    * @param adminService an administration service of a user authorized to deploy models
    * @param modelNames the names of the models without extension (which will be assumed to be <code>xpdl</code>)
    * 
    * @throws ModelIOException if an exception occurs while reading the model from the file system
    * @throws DeploymentException if an exception occurs during model deployment to the runtime
    */
   public static void deploy(final AdministrationService adminService, final String ... modelNames) throws ModelIOException, DeploymentException
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      if (modelNames == null)
      {
         throw new NullPointerException("Name of Models must not be null.");
      }
      if (modelNames.length == 0)
      {
         throw new IllegalArgumentException("Name of Models must not be empty.");
      }

      final List<DeploymentElement> deploymentElements = newArrayList();
      for (final String m : modelNames)
      {
         final DeploymentElement deploymentElement = getDeploymentElement(m);
         deploymentElements.add(deploymentElement);
      }
      
      adminService.deployModel(deploymentElements, DeploymentOptions.DEFAULT);
   }
   
   private static DeploymentElement getDeploymentElement(final String modelName)
   {
      if (modelName == null)
      {
         throw new NullPointerException("Name of Model must not be null.");
      }
      if (modelName.isEmpty())
      {
         throw new IllegalArgumentException("Name of Model must not be empty.");
      }
      
      DeploymentElement result = DEPLOYMENT_ELEMENTS.get(modelName);
      
      if (result == null)
      {
         try
         {
            result = createNewDeploymentElement(modelName);
         }
         catch (final IOException e)
         {
            throw new ModelIOException("Unable to read model file.", e);
         }
         
         DEPLOYMENT_ELEMENTS.put(modelName, result);
      }
      
      return result;
   }
   
   private static DeploymentElement createNewDeploymentElement(final String modelName) throws IOException
   {
      final String fqModelName = MODEL_FOLDER + "/" + modelName + "." + XpdlUtils.EXT_XPDL;
      final InputStream is = ModelDeployer.class.getClassLoader().getResourceAsStream(fqModelName);
      final Reader reader = new InputStreamReader(is);
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      int character;
      
      final byte[] model;
      try
      {
         while ((character = reader.read()) != -1)
         {
            outStream.write(character);
         }
         model = outStream.toByteArray();
      }
      finally
      {
         outStream.close();
      }
      
      final DeploymentElement result = new DeploymentElement(model);
      return result;
   }
   
   private static final class ModelIOException extends RuntimeException
   {
      private static final long serialVersionUID = 482439818461160624L;

      public ModelIOException(final String errorMsg, final IOException e)
      {
         super(errorMsg, e);
      }
   }
   
   private ModelDeployer()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
