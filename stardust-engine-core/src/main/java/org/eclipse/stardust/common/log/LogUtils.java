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

import javax.naming.Context;
import javax.naming.NameClassPair;
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
   private static final Logger bootstrapTrace = LogManager.getLogger("ag.carnot.bootstrap");
   
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
            Iterator stackTrace = getStackTrace(new Exception()).iterator();
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
         for (NamingEnumeration names = context.list(""); names.hasMore();)
         {
            NameClassPair pair = (NameClassPair) names.next();
            try
            {
               Object value = context.lookup(pair.getName());
               if (value instanceof Context)
               {
                  trace.info(prefix + pair.getName() + ":");
                  listContext(prefix + "  ", (Context) value);
               }
               else
               {
                  trace.info(prefix + pair.getName() + " = " + value);
               }
            }
            catch (Exception e)
            {
               trace.info("Failed listing context " + prefix + pair.getName(), e);
            }
         }
      }
      catch (Exception e)
      {
         trace.info(prefix + "Cannot list context: " + e.getMessage(), e);
      }
   }

   public static List getStackTrace(Throwable t)
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
         if (!((ApplicationException) e).isLogged())
         {
            if (e instanceof PublicException)
            {
               trace.warn(e.getMessage());
               if (!((PublicException)e).isS())
               {
                  trace.debug("", e);
               }
            }
            else
            {
               trace.warn("", e);
            }
            ((ApplicationException) e).setLogged(true);
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
      private List vector = CollectionUtils.newList();

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

      public List getVector()
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