/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.examples;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.context.ApplicationContext;

import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.test.api.setup.ApplicationContextConfiguration;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This is an example how a custom {@link ApplicationContext} may be integrated with
 * <i>Stardust Engine Test</i> by using {@code ApplicationContextConfiguration}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@ApplicationContextConfiguration(locations = "classpath:app-ctxs/my-app-ctx-test.app-ctx.xml")
public class MyAppCtxTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory serviceFactory = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(serviceFactory);

   @Test
   public void testBeanResolval()
   {
      final FirstSpringBean a = SpringUtils.getApplicationContext().getBean("a", FirstSpringBean.class);
      final SecondSpringBean b = SpringUtils.getApplicationContext().getBean("b", SecondSpringBean.class);

      assertThat(a.toString(), equalTo(FirstSpringBean.class.getSimpleName()));
      assertThat(b.toString(), equalTo(SecondSpringBean.class.getSimpleName()));
   }

   /**
    * <p>
    * This is a dummy <i>Spring</i> bean used in the test.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class FirstSpringBean
   {
      @Override
      public String toString()
      {
         return getClass().getSimpleName();
      }
   }

   /**
    * <p>
    * This is a dummy <i>Spring</i> bean used in the test.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class SecondSpringBean
   {
      @Override
      public String toString()
      {
         return getClass().getSimpleName();
      }
   }
}
