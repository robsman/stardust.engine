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
package org.eclipse.stardust.common.error;

/**
 * This class is used to wrap {@link PublicException} and {@link ResourceException}
 * instances when engine services are called in EJB context.
 */
public class WorkflowException extends Exception
{
   private static final long serialVersionUID = 1L;

   private Throwable inner;

   public WorkflowException(Throwable x)
   {
      super(x.getMessage());
      this.inner = x;
   }

   /**
    * @deprecated use {@link #getCause()} instead
    *
    * This method returns the inner throwable which caused this workflow exception.
    * In case that the inner exception is no instance of {@link PublicException} the
    * returned inner exception will be wrapped in a {@link PublicException} instance.
    *
    * @return <code>null</code> or <code>{@link PublicException}</code>
    */
   public PublicException getRootCause()
   {
      PublicException x = null;
      if (null != inner)
      {
         x = inner instanceof PublicException ? (PublicException) inner : new PublicException(inner);
      }
      return x;
   }

   /**
    * This method returns the inner throwable which caused this workflow exception.
    *
    * @return <code>null</code> or <code>{@link Throwable}</code>
    */
   public Throwable getCause()
   {
      return inner;
   }

   public void printStackTrace(java.io.PrintStream ps)
   {
      if (inner == null)
      {
         super.printStackTrace(ps);
      }
      else
      {
         synchronized (ps)
         {
            ps.println(this);
            inner.printStackTrace(ps);
         }
      }
   }

   public void printStackTrace()
   {
      printStackTrace(System.err);
   }

   public void printStackTrace(java.io.PrintWriter pw)
   {
      if (inner == null)
      {
         super.printStackTrace(pw);
      }
      else
      {
         synchronized (pw)
         {
            pw.println(this);
            inner.printStackTrace(pw);
         }
      }
   }
}