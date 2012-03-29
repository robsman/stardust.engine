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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.junit.rules.ExternalResource;

/**
 * <p>
 * This class represents a runtime configurer which 
 * <ul>
 *   <li>deploys a model <b>before</b> the test case execution, and</li>
 *   <li>cleans up the runtime and deletes all models <b>after</b> the test case execution.</li>
 * </ul>
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class RuntimeConfigurer extends ExternalResource
{
   private static final Log LOG = LogFactory.getLog(RuntimeConfigurer.class);
   
   private final String[] modelNames;
   private final ServiceFactory sf;
   
   /**
    * <p>
    * Sets up a runtime configurer with the specified model using the given service factory.
    * </p>
    * 
    * @param sf the service factory to use for model deployment and runtime cleanup
    * @param modelNames the names of the models to deploy
    */
   public RuntimeConfigurer(final ServiceFactory sf, final String ... modelNames)
   {
      if (modelNames == null)
      {
         throw new NullPointerException("Model Names must not be null.");
      }
      if (modelNames.length == 0)
      {
         throw new IllegalArgumentException("Model Names must not be empty.");
      }
      if (sf == null)
      {
         throw new NullPointerException("Service Factory must not be null.");
      }
      
      this.modelNames = modelNames;
      this.sf = sf;
   }
   
   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#before()
    */
   @Override
   protected void before()
   {
      LOG.debug("Trying to deploy model(s) '" + Arrays.asList(modelNames) + "'.");
      ModelDeployer.deploy(sf.getAdministrationService(), modelNames);
   }
   
   /* (non-Javadoc)
    * @see org.junit.rules.ExternalResource#after()
    */
   @Override
   protected void after()
   {
      LOG.debug("Trying to clean up runtime and models.");
      sf.getAdministrationService().cleanupRuntimeAndModels();
   }
}
