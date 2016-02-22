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

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.spring.SpringConstants;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
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
 */
public class SpringAppContext
{
   private static final Log LOG = LogFactory.getLog(SpringAppContext.class);

   private static final String APP_CTX_DELIMITER = ",";

   private static final String APP_CTX_NAME_PREFIX = "stardust-local-";
   private static final String APP_CTX_NAME_SUFFIX = ".app-ctx.xml";

   private static final String APP_CTX_NAME_DEFAULT = "stardust-local" + APP_CTX_NAME_SUFFIX;
   private static final String APP_CTX_NAME_JMS_FORKING = APP_CTX_NAME_PREFIX + "forking-jms-activemq" + APP_CTX_NAME_SUFFIX;
   private static final String APP_CTX_NAME_NATIVE_THREADING = APP_CTX_NAME_PREFIX + "forking-native-threading" + APP_CTX_NAME_SUFFIX;

   public void bootstrap(final ForkingServiceMode forkingServiceMode, final Class<?> testClass) throws TestRtEnvException
   {
      try
      {
         final String appCtxNames = determineAppCtxNames(forkingServiceMode, testClass);
         final GlobalParameters params = GlobalParameters.globals();
         params.set(SpringConstants.PRP_APPLICATION_CONTEXT_FILE, appCtxNames);

         /* causes the Spring Application Context to be initialized */
         appCtx();
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
         SpringUtils.reset();
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
      return (ConfigurableApplicationContext) SpringUtils.getApplicationContext();
   }

   private String determineAppCtxNames(final ForkingServiceMode forkingServiceMode, final Class<?> testClass)
   {
      final StringBuilder sb = new StringBuilder();

      final Set<ApplicationContextConfiguration> appCtxConfigs = newHashSet();
      Class<?> classIterator = testClass;
      do
      {
         final ApplicationContextConfiguration appCtxConfig = classIterator.getAnnotation(ApplicationContextConfiguration.class);
         if (appCtxConfig != null)
         {
            appCtxConfigs.add(appCtxConfig);
         }
         classIterator = classIterator.getSuperclass();
      } while (classIterator != null);

      for (ApplicationContextConfiguration appCtxConfig : appCtxConfigs)
      {
         for (final String l : appCtxConfig.locations())
         {
            sb.append(l).append(APP_CTX_DELIMITER);
         }
      }

      sb.append(APP_CTX_NAME_DEFAULT);
      sb.append(APP_CTX_DELIMITER);
      if (forkingServiceMode == ForkingServiceMode.NATIVE_THREADING)
      {
         sb.append(APP_CTX_NAME_NATIVE_THREADING);
      }
      else if (forkingServiceMode == ForkingServiceMode.JMS)
      {
         sb.append(APP_CTX_NAME_JMS_FORKING);
      }
      else
      {
         throw new IllegalArgumentException("Unknown forking service mode '" + forkingServiceMode + "'.");
      }

      return sb.toString();
   }
}
