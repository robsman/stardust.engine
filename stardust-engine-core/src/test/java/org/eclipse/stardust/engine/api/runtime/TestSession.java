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
public interface TestSession
{
   /**
    *
    */
   public String throwRuntimeException();

   /**
    *
    */
   public String throwException() throws Exception;

   /**
    *
    */
   public String throwError();

   /**
    *
    */
   public boolean isWillFail();

   /**
    *
    */
   public void setWillFail(boolean failMode);

   /**
    *
    */
   public void complete();

   /**
    *
    */
   public int complete(String string, TestSerializable testSerializable);

   /**
    * Execution crashes if mark file does not exist otherwise method
    * is idempotent. Allows recovery testing.
    */
   public void alternatingExecution() throws Exception;

   /**
    * Retrieves the serializable from this session.
    */
   public TestSerializable getSerializable();

   /**
    * Sets the serializable for this session.
    */
   public void setSerializable(TestSerializable serializable);
}
