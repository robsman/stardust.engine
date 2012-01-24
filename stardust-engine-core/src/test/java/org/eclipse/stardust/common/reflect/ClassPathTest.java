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
package org.eclipse.stardust.common.reflect;

import java.io.File;

import org.eclipse.stardust.common.reflect.ClassPath;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class ClassPathTest extends TestCase
{
   /**
    *
    */
   public ClassPathTest(String name)
   {
      super(name);
   }

   /**
    *
    */
   protected void setUp()
   {
   }

   /**
    *
    */
   protected void tearDown()
   {
   }

   /**
    *
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite(ClassPathTest.class);

      return suite;
   }

   /**
    *
    */
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   /**
    *
    */
   public void testPackageIteration()
   {
      java.util.Iterator iterator = ClassPath.instance().getAllPackages();
      boolean _found = false;

      while (iterator.hasNext())
      {
         if (iterator.next().toString().equals("javax"))
         {
            _found = true;

            break;
         }
      }

      assertTrue("Test load of standard packages",
            _found);
   }

   /**
    *
    */
   public void testLoadPackageClasses()
   {
      ClassPath.instance().addEntryFromURL(new File("./javax"));

      java.util.Iterator iterator = ClassPath.instance().getAllPackages();
      boolean _found = false;

      while (iterator.hasNext())
      {
         if (iterator.next().toString().equals("javax"))
         {
            _found = true;

            break;
         }
      }

      assertTrue("Test load of local package",
            _found);
   }
}
