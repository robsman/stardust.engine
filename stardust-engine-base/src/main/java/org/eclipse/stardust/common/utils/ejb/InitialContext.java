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
package org.eclipse.stardust.common.utils.ejb;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * Fake JNDI context for EJB simulation.
 */
public class InitialContext implements Context
{
   private static final Logger trace = LogManager.getLogger(InitialContext.class);

   /**
    * Adds a new environment property to the environment of this context.
    */
   public java.lang.Object addToEnvironment(java.lang.String propName,
         java.lang.Object propVal)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'name' to the object 'obj'.
    */
   public void bind(Name name, java.lang.Object obj)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'name' to the object 'obj' using its string name.
    */
   public void bind(java.lang.String name, java.lang.Object obj)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Closes this context.
    */
   public void close()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Composes the name of this context with a name relative to this context.
    */
   public Name composeName(Name name, Name prefix)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Composes the string name of this context with a string name relative to
    * this context.
    */
   public java.lang.String composeName(java.lang.String name, java.lang.String prefix)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Creates and binds a new context.
    */
   public Context createSubcontext(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Creates and binds a new context using a string name.
    */
   public Context createSubcontext(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Destroys the named context and removes it from the namespace.
    */
   public void destroySubcontext(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Destroys the (string-) named context and removes it from the
    * namespace.
    */
   public void destroySubcontext(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Retrieves the environment in effect for this context.
    */
   public java.util.Hashtable getEnvironment()
   {
      throw new UnsupportedOperationException();
   }

   /**
    *
    */
   public String getNameInNamespace()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Retrieves the parser associated with the named context.
    */
   public NameParser getNameParser(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Retrieves the parser associated with the (string-) named context.
    */
   public NameParser getNameParser(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Enumerates the names and the class names of their bound objects in the
    * named context.
    */
   public NamingEnumeration list(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Enumerates the names and the class names of their bound objects in the
    * (string-) named context.
    */
   public NamingEnumeration list(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Enumerates the names and their bound objects in the named context.
    */
   public NamingEnumeration listBindings(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Enumerates the names and their bound objects in the (string-) named
    * context.
    */
   public NamingEnumeration listBindings(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Retrieves the named object.
    */
   public java.lang.Object lookup(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Looks up a (singleton) instance of a bean home implementation.
    *
    * If a path
    * <p>
    * <pre>de.acme.support.SupportCase</pre>
    * <p>
    * is provided, the method tries to find an instance of
    * <code>de.acme.support.SupportCaseHome</code>.
    * <p>
    * It first checks, wether a class <code>de.acme.support.beans.SupportCaseHomeImpl</code>
    * exists. If so, it returns an instance of it.
    * <p>
    * If not, it checks, wether a class <code>de.acme.support.SupportCaseHome</code>
    * exists. If so, it returns an instance of it.
    */
   public Object lookup(String path)
   {
      // Try <package>.<bean>HomeImpl

      String _path = path.substring(0, path.lastIndexOf('.')) +
            path.substring(path.lastIndexOf('.')) + "HomeImpl";

      trace.debug( "Try to lookup " + _path);

      try
      {
         Class type = Reflect.getClassFromClassName(_path, false);
         if (null != type)
         {
            trace.debug( "Type retrieved: " + type);

            Object object = type.newInstance();

            trace.debug( "Object created: " + object);

            return object;
         }
      }
      catch (Exception x)
      {
         // Continue
      }

      // Try <package>.beans.<bean>HomeImpl

      _path = path.substring(0, path.lastIndexOf('.')) + ".beans" +
            path.substring(path.lastIndexOf('.')) + "HomeImpl";

      trace.debug( "Try to lookup " + _path);

      try
      {
         Class type = Reflect.getClassFromClassName(_path, false);
         if (null != type)
         {
            trace.debug( "Type retrieved: " + type);

            Object object = type.newInstance();

            trace.debug( "Object created: " + object);

            return object;
         }
      }
      catch (Exception x)
      {
         // Continue
      }

      _path = path + "Home";

      trace.debug( "Try to lookup " + _path);

      // Try to load <package>.<bean>Home as a class

      try
      {
         Class type = Reflect.getClassFromClassName(_path, true);

         trace.debug( "Type retrieved: " + type);

         Object object = type.newInstance();

         trace.debug( "Object created: " + object);

         return object;
      }
      catch (Exception x)
      {
         throw new PublicException("Failed to create or retrieve instance of \"fake\" home class \""
               + _path + "\". Reason: " + x.getMessage());
      }
   }

   /**
    * Retrieves the named object, following links except for the terminal
    * atomic component of name.
    */
   public java.lang.Object lookupLink(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Retrieves the (string-) named object, following links except for the
    * terminal atomic component of name.
    */
   public java.lang.Object lookupLink(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'name' to the object 'obj', overwriting any existing binding.
    */
   public void rebind(Name name, java.lang.Object obj)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'name' to the object 'obj' using its string name, overwriting any
    * existing binding.
    */
   public void rebind(java.lang.String name, java.lang.Object obj)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Removes an environment property from the environment of this context.
    */
   public java.lang.Object removeFromEnvironment(java.lang.String propName)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'newName' to the object bound to 'oldName', and unbinds
    * 'oldName'.
    */
   public void rename(Name oldName, Name newName)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Binds 'newName' to the object bound to 'oldName', and unbinds
    * 'oldName' using string names.
    */
   public void rename(java.lang.String oldName, java.lang.String newName)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Unbinds the named object from the namespace using its string name.
    */
   public void unbind(Name name)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Unbinds the named object from the namespace using its string name.
    */
   public void unbind(java.lang.String name)
   {
      throw new UnsupportedOperationException();
   }
}