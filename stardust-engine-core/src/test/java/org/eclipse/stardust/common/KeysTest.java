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

import java.io.IOException;
import java.util.Collection;

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.StringKey;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author ubirkemeyer
 * @version $Revision: 43703 $
 */
public class KeysTest extends TestCase
{
   public static AStringKey Fred = new AStringKey("Fred");
   public static AnIntKey Pluesch = new AnIntKey(1, "Pluesch");

   public static Test suite()
   {
      return new TestSuite(KeysTest.class);
   }

   public void testGetKeysOnStringKey()
   {
      Collection c = StringKey.getKeys(AStringKey.class);
      assertEquals(2, c.size());
   }

   public void testGetKeysOnIntKey()
   {
      Collection d = IntKey.getKeys(AnIntKey.class);
      assertEquals(2, d.size());
   }

   public void testStringKeyRetrieval()
   {
      assertNotNull(StringKey.getKey(AStringKey.class, "Fred"));
      assertNotNull(StringKey.getKey(AStringKey.class, "Barney"));
   }


   public void testIntKeyRetrieval()
   {
      assertNotNull(IntKey.getKey(AnIntKey.class, 1));
      assertNotNull(IntKey.getKey(AnIntKey.class, 17));
   }

   public static class AStringKey extends StringKey
   {
      private static final long serialVersionUID = 1L;

      public static AStringKey Barney = new AStringKey("Barney");
      public AStringKey(String id)
      {
         super(id, id);
      }
   }
   
   public void testSerialization()
   {
      try
      {
         Serialization.declareCritical(AStringKey.class);
         byte[] serializedKey = Serialization.serializeObject(Fred);
         AStringKey fred2 = (AStringKey) Serialization.deserializeObject(serializedKey);
         assertEquals(Fred, fred2);
      }
      catch (IOException e) 
      {
         fail("error occurred during (de-)serialization");
      }
      catch (ClassNotFoundException e)
      {
         fail("error occurred during deserialization");
      }
   }

   public static class AnIntKey extends IntKey
   {
      private static final long serialVersionUID = 1L;

      public static AnIntKey Plum = new AnIntKey(17, "Plum");

      public AnIntKey(int id, String defaultName)
      {
         super(id, defaultName);
      }
   }
}
