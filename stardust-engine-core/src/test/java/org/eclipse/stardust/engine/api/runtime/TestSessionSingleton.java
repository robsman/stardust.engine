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

/**
 *
 */
public class TestSessionSingleton
{

   protected boolean willFail = true;
   protected static TestSessionSingleton instance;

   protected TestSessionSingleton()
   {
      super();
   }

   public static TestSessionSingleton getInstance()
   {
      if (instance == null)
      {
         instance = new TestSessionSingleton();
      }
      return instance;
   }

   public boolean isWillFail()
   {
      return willFail;
   }

   public void setWillFail(boolean failMode)
   {
      willFail = failMode;
   }

   public String throwRuntimeException()
   {
      if (willFail)
         throw new RuntimeException("Exception thrown in Class " + this.getClass().getName());

      return ("Did not fail, because Fail Mode was " + willFail);
   }

   public String throwException() throws Exception
   {
      if (willFail)
         throw new Exception("Exception thrown in Class " + this.getClass().getName());
      return ("Did not fail, because Fail Mode was " + willFail);
   }

   public String throwError()
   {
      if (willFail)
         System.out.println(InitializerFails.getString());
      return ("Did not fail, because Fail Mode was " + willFail);
   }

   public void complete()
   {
   }
}
