/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.error.InternalException;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class Base64Test extends TestCase
{
   private static final String plain = "Un text lung care contine si litere si cifre " +
      "si chiar si diacritice 1234567890!§$%&/()=?üÜäÄöÖß";

   public Base64Test(String name)
   {
      super(name);
   }

   /**
    * Put all code to set up your test fixture (environment) here
    *
    */
   protected void setUp()
   {
   }

   /**
    * here goes code to tear down your test fixture
    * (e. g. release ressources, close streams or files, etc.)
    */
   protected void tearDown()
   {
   }

   /**
    *
    *
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite(Base64Test.class);
      return suite;
   }

   public void testEncodeDecode()
   {
      byte[] bytes = Base64.encode(plain.getBytes());
      String encripted = new String(bytes);
      assertTrue("String was not encripted", !plain.equals(encripted));
      assertTrue("Encripted string does not end with '='", encripted.endsWith("="));
      String decripted = new String(Base64.decode(encripted.getBytes()));
      assertEquals(plain, decripted);
      assertNotSame(plain, decripted);
   }

   public void testCorruptedEncoding()
   {
      byte[] bytes = Base64.encode(plain.getBytes());
      String encripted = new String(bytes);
      encripted = encripted.substring(0, encripted.length() - 1);
      try
      {
         Base64.decode(encripted.getBytes());
         fail("Decoded byte array with invalid ending.");
      }
      catch (InternalException ex)
      {
      }
   }

   public void testNull()
   {
      assertNull("Encoded null is not null", Base64.encode(null));
      assertNull("Decoded null is not null", Base64.decode(null));
   }

   public void testInvalidLength()
   {
      byte[] bytes = Base64.encode(plain.getBytes());
      byte[] copy = new byte[bytes.length - 2];
      System.arraycopy(bytes, 0, copy, 0, bytes.length - 2);
      try
      {
         Base64.decode(copy);
         fail("Decoded byte array with invalid length.");
      }
      catch (InternalException ex)
      {
      }
   }
}
