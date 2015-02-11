/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.log;

import java.lang.reflect.Field;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * This class focuses on testing logging operations.
 * </p>
 *
 * @author Roland.Stamm
 */
public class LogTest
{

   @Test
   public void testGetCustomLogger()
   {

      Assert.assertNotEquals(CustomLogger.class.getName(),
            LogManager.getLogger(LogTest.class).getClass().getName());

      Properties properties = System.getProperties();

      try
      {
         // Set custom logger
         properties.put("carnot.log.type", "CUSTOM," + CustomLogger.class.getName());
         resetLogManager();

         Logger customLogger = LogManager.getLogger(LogTest.class);
         Assert.assertEquals(CustomLogger.class.getName(), customLogger.getClass()
               .getName());
         customLogger.info("Test Info!");

      }
      finally
      {
         // Remove custom logger
         properties.remove("carnot.log.type");
         resetLogManager();
      }

      Assert.assertNotEquals(CustomLogger.class.getName(),
            LogManager.getLogger(LogTest.class).getClass().getName());
   }

   private void resetLogManager()
   {
      try
      {
         // Reset LogManager is only possible via reflection.
         Field field = LogManager.class.getDeclaredField("bootstrapped");
         field.setAccessible(true);
         field.setBoolean(LogManager.class, false);

         Field field2 = LogManager.class.getDeclaredField("logType");
         field2.setAccessible(true);
         field2.set(LogManager.class, null);
      }
      catch (Exception e)
      {
         Assert.fail("private fields changed? " + e.getMessage());
      }
   }
}
