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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;



/**
 * Fake instead of the UserHome interface as long as this class is not real
 * EJB.
 */
public class UserHome
{
   private static final String USER_BEAN_NAME = "org.eclipse.stardust.engine.core.runtime.beans.UserBean";

   /**
    *
    */
   public IUser create(String account, String firstName,
         String lastName)
   {
      try
      {
         Class _type = Reflect.getClassFromClassName(USER_BEAN_NAME);
         IUser _user = (IUser) _type.newInstance();
         Method _createMethod = _type.getMethod("ejbCreate", new Class[]{String.class, String.class,
                                                                         String.class});

         _createMethod.invoke(_user, new Object[]{account, firstName, lastName});

         return _user;
      }
      catch (InvocationTargetException x)
      {
         throw new PublicException(
               BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(x
                     .getTargetException().getMessage()));
      }
      catch (Exception x)
      {
         throw new InternalException(x);
      }
   }

   /**
    *
    */
   public IUser findByPrimaryKey(UserPK pk)
   {
      try
      {
         Class _type = Reflect.getClassFromClassName(USER_BEAN_NAME);
         IUser _user = (IUser) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
               .findByOID(_type, pk.oid);

         // perform bean specific inits
         Method _findMethod = _type.getMethod("ejbFindByPrimaryKey", new Class[]{UserPK.class});
         _findMethod.invoke(_user, new Object[]{pk});

         return _user;
      }
      catch (InvocationTargetException x)
      {
         throw new PublicException(
               BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(x
                     .getTargetException().getMessage()));
      }
      catch (Exception x)
      {
         throw new InternalException(x);
      }
   }

}
