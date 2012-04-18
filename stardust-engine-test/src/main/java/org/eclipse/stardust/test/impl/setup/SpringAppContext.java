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
package org.eclipse.stardust.test.impl.setup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
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
 * @author Nicolas.Werlein
 * @version $Revision: 55261 $
 */
public class SpringAppContext
{
   private static final Log LOG = LogFactory.getLog(SpringAppContext.class);
   
   public void bootstrap() throws TestRtEnvException
   {
      try
      {
         /* causes the Spring Application Context to be initialized */
         ((ConfigurableApplicationContext) SpringUtils.getApplicationContext()).refresh();
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
         ((ConfigurableApplicationContext) SpringUtils.getApplicationContext()).close();
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to close Spring Application Context.";
         LOG.error(errorMsg, e);
         throw new TestRtEnvException(errorMsg, e, TestRtEnvAction.APP_CTX_TEARDOWN);         
      }
   }
}
