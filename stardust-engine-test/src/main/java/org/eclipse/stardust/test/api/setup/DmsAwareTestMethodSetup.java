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

import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class deals with test method setup, i.e. it &ndash; in addition to the actions
 * performed by {@link TestMethodSetup} &ndash; cleans up the DMS repository after a
 * test has been executed.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DmsAwareTestMethodSetup extends TestMethodSetup
{
   /**
    * <p>
    * Sets up a DMS aware runtime configurer with the username password pair to use for runtime configuration.
    * </p>
    * 
    * @param userPwdPair the credentials of the user used for runtime configuration; must not be null
    * @param testClassSetup the corresponding test class setup object; must not be null
    */
   public DmsAwareTestMethodSetup(final UsernamePasswordPair userPwdPair, final TestClassSetup testClassSetup)
   {
      super(userPwdPair, testClassSetup);
   }
   
   /**
    * <p>
    * Cleans up the DMS repository and delegates to the corresponding superclass method afterwards.
    * </p>
    */
   @Override
   protected void after()
   {
      RtEnvHome.cleanUpDmsRepository(serviceFactory().getDocumentManagementService());
      
      super.after();
   }
}
