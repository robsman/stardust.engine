/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.setup;

import org.junit.AfterClass;

/**
 * <p>
 * This is the base class all <i>Camel IPP Integration</i> test cases should inherit from since it
 * loads the mandatory <i>Default Camel Context</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@ApplicationContextConfiguration(locations = { "classpath:stardust-default-camel-context.app-ctx.xml", "classpath:app-ctxs/camel-common-test.app-ctx.xml" })
public abstract class AbstractCamelIntegrationTest
{
   private static final String HAZELCAST_LOGGING_TYPE_KEY = "hazelcast.logging.type";
   private static final String HAZELCAST_LOGGING_TYPE_VALUE = "log4j";

   static
   {
      System.setProperty(HAZELCAST_LOGGING_TYPE_KEY, HAZELCAST_LOGGING_TYPE_VALUE);
   }

   @AfterClass
   public static void tearDownOnce()
   {
      System.clearProperty(HAZELCAST_LOGGING_TYPE_KEY);
   }
}
