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
package org.eclipse.stardust.test.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * <p>
 * This class simply wraps the Spring Application Context
 * maintained by the class <code>SpringUtils</code> and
 * provides convenient methods to explicitly
 * <ul>
 *   <li>bootstrap, and</li>
 *   <li>close</li>
 * </ul>
 * the same.
 * </p>
 * 
 * <p>
 * Furthermore, it allows for retrieving the same.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: 55261 $
 */
public class SpringAppContext
{
   private static final Log LOG = LogFactory.getLog(SpringAppContext.class);
   
   private static final String FORKING_SERVICE_MODE_PROPERTY_KEY = "forking.service.mode";
   
   private static final String FORKING_SERVICE_MODE_PROPERTY_VALUE_JMS = "jms-activemq";
   private static final String FORKING_SERVICE_MODE_PROPERTY_VALUE_NATIVE_THREADING = "native-threading";
   
   public void bootstrap(final ForkingServiceMode forkingServiceMode) throws TestRtEnvException
   {
      try
      {
         final String forkingServiceModeValue = (forkingServiceMode == ForkingServiceMode.JMS) 
                                                   ? FORKING_SERVICE_MODE_PROPERTY_VALUE_JMS
                                                   : FORKING_SERVICE_MODE_PROPERTY_VALUE_NATIVE_THREADING;
         System.setProperty(FORKING_SERVICE_MODE_PROPERTY_KEY, forkingServiceModeValue);
         
         /* causes the Spring Application Context to be initialized */
         appCtx().refresh();
         
         System.clearProperty(FORKING_SERVICE_MODE_PROPERTY_KEY);
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to bootstrap Spring Application Context.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, e, TestRtEnvAction.APP_CTX_SETUP);
      }
   }
   
   public void close() throws TestRtEnvException
   {
      try
      {
         appCtx().close();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to close Spring Application Context.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, e, TestRtEnvAction.APP_CTX_TEARDOWN);         
      }
   }
   
   public ConfigurableApplicationContext appCtx()
   {
      return ((ConfigurableApplicationContext) SpringUtils.getApplicationContext());
   }
}
