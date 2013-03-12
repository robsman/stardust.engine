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
package org.eclipse.stardust.common.log;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class LogUtils
{
   private static final String CAT_ROOT = LogUtils.class.getPackage().getName();

   private static final Logger bootstrapTrace = LogManager.getLogger(CAT_ROOT + ".Bootstrap");
   
   private static final Logger trace = LogManager.getLogger(LogUtils.class);

   /**
    * Returns a pretty printed label for the given instance. Basically the same as
    * {@link Object#toString}, but with stripped packege.
    * 
    * @param instance
    *           The instance to pretty print.
    * @return The label in format "<code>ClassName@12345</code>".
    */
   public static final String instanceInfo(Object instance)
   {
      if (null == instance)
      {
         return null;
      }
      else
      {
         return new StringBuffer().append(
               Reflect.getHumanReadableClassName(instance.getClass()))
               .append("@").append(Integer.toHexString(instance.hashCode()))
               .toString();
      }
   }

   public static void traceObject(Object o, boolean showCallStack)
   {
      if (bootstrapTrace.isDebugEnabled())
      {
         String message;
         if (o == null)
         {
            message = "Object: null";
         }
         else
         {
            message = "Object: " + o + ", Class: " + o.getClass().getName()
                  + ", Classloader: " + o.getClass().getClassLoader();
         }

         if (showCallStack)
         {
            bootstrapTrace.debug(message);
            Iterator<String> stackTrace = getStackTrace(new Exception()).iterator();
            if (stackTrace.hasNext())
            {
               bootstrapTrace.debug("Call stack (no error):");
               stackTrace.next();
               while (stackTrace.hasNext())
               {
                  bootstrapTrace.debug(stackTrace.next());
               }
            }
            else
            {
               bootstrapTrace.debug("No call stack available.");
            }
         }
         else
         {
            bootstrapTrace.debug(message);
         }
      }
   }

   public static void listContext(String prefix, Context context)
   {
      try
      {
         for (NamingEnumeration<Binding> bindings = context.listBindings(""); bindings.hasMore();)
         {
            Binding binding = bindings.next();
            try
            {
               Object value = binding.getObject();
               if (value instanceof Context)
               {
                  trace.info(prefix + binding.getName() + ":");
                  listContext(prefix + "  ", (Context) value);
               }
               else
               {
                  trace.info(prefix + binding.getName() + " = " + value);
               }
            }
            catch (Exception e)
            {
               trace.info("Failed listing context " + prefix + binding.getName(), e);
            }
         }
      }
      catch (Exception e)
      {
         trace.info(prefix + "Cannot list context: " + e.getMessage(), e);
      }
   }

   public static List<String> getStackTrace(Throwable t)
   {
      VectorWriter writer = new VectorWriter();
      t.printStackTrace(writer);
      return writer.getVector();
   }

   public static void traceException(Throwable e, boolean rethrowCanonicalized)
   {
      if (e instanceof InvocationTargetException)
      {
         traceException(((InvocationTargetException)e).getTargetException(), rethrowCanonicalized);
      }
      else if (e instanceof ApplicationException)
      {
         ApplicationException applicationException 
            = (ApplicationException) e;
         if (!applicationException.isLogged())
         {
            if (applicationException instanceof PublicException)
            {
               trace.warn(e.getMessage());
               trace.debug("", e);
            }
            else
            {
               trace.warn("", e);
            }
            applicationException.setLogged(true);
         }
         if (rethrowCanonicalized)
         {
            throw (ApplicationException) e;
         }
         else
         {
            return;
         }
      }
      // @todo (france, ub): in production mode don't log the fact of an UndeclaredThrowable
      else if (e instanceof  UndeclaredThrowableException)
      {
         Throwable wrapped = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
         trace.warn("Undeclared throwable: ", wrapped);
         if (rethrowCanonicalized)
         {
            throw new InternalException("Undeclared throwable: " + wrapped.getClass().getName());
         }
      }
      else
      {
         trace.warn("", e);
         if (rethrowCanonicalized)
         {
            throw new InternalException(e.getMessage());
         }
      }
   }
   
   static class VectorWriter extends PrintWriter
   {
      private List<String> vector = CollectionUtils.newList();

      VectorWriter()
      {
         super(new NullWriter());
      }

      public void println(Object o)
      {
         vector.add(o.toString());
      }

      public void println(char s[])
      {
         vector.add(new String(s));
      }

      public void println(String s)
      {
         vector.add(s);
      }

      public List<String> getVector()
      {
         return vector;
      }
   }

   static class NullWriter extends Writer
   {
      NullWriter()
      {
      }

      public void close()
      {
      }

      public void flush()
      {
      }

      public void write(char ac[], int i, int j)
      {
      }
   }
}