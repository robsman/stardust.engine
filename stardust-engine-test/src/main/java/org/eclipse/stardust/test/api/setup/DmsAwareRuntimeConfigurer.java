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
package org.eclipse.stardust.test.api.setup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;

/**
 * <p>
 * This class represents a runtime configurer which &ndash; in addition to the actions
 * performed by {@link RuntimeConfigurer} &ndash; cleans up the DMS repository after a
 * test has been executed.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DmsAwareRuntimeConfigurer extends RuntimeConfigurer
{
   private static final Log LOG = LogFactory.getLog(DmsAwareRuntimeConfigurer.class);
   
   private static final String DMS_REPO_ROOT_FOLDER = "/";
   
   /**
    * <p>
    * Sets up a DMS aware runtime configurer with the specified model using the given service factory.
    * </p>
    * 
    * @param sf the service factory to use for model deployment and runtime cleanup; must not be null
    * @param modelNames the names of the models to deploy; may be null or empty
    */
   public DmsAwareRuntimeConfigurer(final ServiceFactory sf, final String ... modelNames)
   {
      super(sf, modelNames);
   }
   
   /**
    * <p>
    * Cleans up the DMS repository.
    * </p>
    * 
    * @throws TestRtEnvException if the DMS repository could not be cleaned up
    */
   public void cleanUpDmsRepository() throws TestRtEnvException
   {
      try
      {
         serviceFactory().getDocumentManagementService().removeFolder(DMS_REPO_ROOT_FOLDER, true);
      }
      catch (final Exception e)
      {
         throw new TestRtEnvException("Could not clean up DMS repository.", e, TestRtEnvAction.CLEAN_UP_DMS_REPOSITORY);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.setup.RuntimeConfigurer#after()
    */
   @Override
   protected void after()
   {
      super.after();
      
      LOG.debug("Trying to clean up the DMS repository.");
      cleanUpDmsRepository();
   }
}
