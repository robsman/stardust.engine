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
package org.eclipse.stardust.engine.api.runtime;

import java.io.File;

/**
 *
 */
public class TestSessionBean implements TestSession
{
   private TestSerializable serializable;

   public int complete(String string, TestSerializable testSerializable)
   {
      System.out.println("Completing test session with parameter 1 = " + string
                         + " parameter 2 = " + testSerializable + ".");

      return 42;
   }

   /**
    *
    */
   public String throwRuntimeException()
   {
      System.out.println("TestErrorBean: before throwing RuntimeException");

      return (TestSessionSingleton.getInstance().throwRuntimeException());
   }

   /**
    *
    */
   public String throwException() throws Exception
   {
      System.out.println("TestErrorBean: before throwing Exception");

      return (TestSessionSingleton.getInstance().throwException());
   }

   /**
    *
    */
   public String throwError()
   {
      System.out.println("TestErrorBean: before throwing Error");

      return (TestSessionSingleton.getInstance().throwError());
   }

   /**
    * Execution crashes if mark file does not exist otherwise method
    * is idempotent. Allows recovery testing.
    */
   public void alternatingExecution() throws Exception
   {
      File _markFile = new File("test_session.mark");

      if (!_markFile.exists())
      {
         try
         {
            _markFile.createNewFile();
         }
         catch (Exception x)
         {
         }

         System.out.println("TestErrorBean: before throwing Exception");

         TestSessionSingleton.getInstance().throwException();
      }
      else
      {
         _markFile.delete();
      }
   }

   /**
    *
    */
   public void complete()
   {
      System.out.println("Completing test session.");
   }

   /**
    *
    */
   public boolean isWillFail()
   {
      return TestSessionSingleton.getInstance().isWillFail();
   }

   /**
    *
    */
   public void setWillFail(boolean failMode)
   {
      TestSessionSingleton.getInstance().setWillFail(failMode);
   }

   /**
    * Retrieves the serializable from this session.
    */
   public TestSerializable getSerializable()
   {
      if (serializable == null)
      {
         serializable = new TestSerializable(14, "Willi");
      }

      return serializable;
   }

   /**
    * Sets the serializable for this session.
    */
   public void setSerializable(TestSerializable serializable)
   {
      this.serializable = serializable;
   }
}
