/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.compatibility.spi.security;

import java.security.Principal;
import java.util.HashMap;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.core.compatibility.spi.security.PrincipalNameProvider;
import org.eclipse.stardust.engine.extensions.ejb.utils.J2EEUtils;
import org.junit.*;

/**
 * TODO javadoc
 * 
 * @author Stephan.Born
 * @version $Revision$
 */
public class PrincipalNameProviderTest
{
   @BeforeClass
   public static void setUpClass()
   {
   }

   @AfterClass
   public static void tearDownClass()
   {

   }

   @Before
   public void setUp()
   {
   }

   @After
   public void tearDown()
   {
      // flushes cached extension provider instances used by ExtensionProviderUtils
      Parameters.instance().flush();
   }

   @Test
   public void testExceptionOnNullPrincipal()
   {
      try
      {
         J2EEUtils.getPrincipalName(null);
      }
      catch (IllegalArgumentException x)
      {
         // this is expected
         return;
      }
      Assert.lineNeverReached();
   }

   @Test
   public void testDefaultPrincipalProvider()
   {
      final String expectedName = "expectedName";
      Principal principal = new Principal()
      {

         @Override
         public String getName()
         {
            return expectedName;
         }
      };

      String principalName = J2EEUtils.getPrincipalName(principal);

      Assert.condition(expectedName.equals(principalName), "Expected " + expectedName
            + " but got " + principalName);
   }

   @Test
   public void testCustomPrincipalProvider()
   {

      // Prioncipa itself will provide not expected name
      final String notExpectedName = "notExpectedName";
      Principal principal = new Principal()
      {

         @Override
         public String getName()
         {
            return notExpectedName;
         }
      };

      final String expectedName = TestPrincipalNameProvider.getExpectedName();

      // This will set name provider whoch will overwrite default behavior and return expected name
      HashMap<String, Object> newHashMap = CollectionUtils.newHashMap();
      newHashMap.put(PrincipalNameProvider.class.getName() + ".Providers",
            TestPrincipalNameProvider.class.getName());
      ParametersFacade.pushLayer(newHashMap);

      String principalName = J2EEUtils.getPrincipalName(principal);

      Assert.condition(expectedName.equals(principalName), "Expected " + expectedName + " but got "
            + principalName);

      ParametersFacade.popLayer();
   }

   public static class TestPrincipalNameProvider implements PrincipalNameProvider
   {
      private static final String expectedName = "customExpectedName";

      @Override
      public String getName(Principal principal)
      {
         return expectedName;
      }

      public static String getExpectedName()
      {
         return expectedName;
      }
   }
}
