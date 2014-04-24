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
package org.eclipse.stardust.engine.extensions.ejb.ejb2.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.extensions.ejb.data.IEntityPKEvaluator;


/**
 * @author ubirkemeyer
 * @version $Revision: 52518 $
 */
public class Entity20PKEvaluator implements IEntityPKEvaluator
{
   private static final Logger trace = LogManager.getLogger(Entity20PKEvaluator.class);

   /**
    * Obtains the PK from an entity bean access point. The access point may either be
    * the remote or local interface, the PK itself or for legacy support the reference to
    * a locally bound entity bean.
    *
    * @param attributes The access point definition's attributes.
    * @param value The base value to be used for PK inspection.
    * @return The obtained PK, or <code>null</code> if no strategy was successful.
    */
   public Object getEntityBeanPK(Map attributes, Object value)
   {
      Object pk = null;
      if (null != value)
      {
         if (value instanceof EJBObject)
         {
            EJBObject bean = (EJBObject) value;
            try
            {
               pk = bean.getPrimaryKey();
            }
            catch (RemoteException e)
            {
               throw new PublicException(
                     BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_PK.raise(),
                     e.detail);
            }
         }
         else if (value instanceof EJBLocalObject)
         {
            EJBLocalObject bean = (EJBLocalObject) value;
            try
            {
               pk = bean.getPrimaryKey();
            }
            catch (EJBException e)
            {
               throw new PublicException(
                     BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_PK.raise(), e);

            }
         }
         else if (value instanceof Handle)
         {
            Handle handle = (Handle) value;

            try
            {
               pk = handle.getEJBObject().getPrimaryKey();
            }
            catch (RemoteException e)
            {
               throw new PublicException(
                     BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_PK.raise(),
                     e.detail);
            }
         }
         else
         {
            final Class entityInterfaceClass = getEntityInterfaceClass(attributes);
            if ((null != entityInterfaceClass)
                  && entityInterfaceClass.isAssignableFrom(value.getClass()))
            {
               // special handling to support legacy locally bound entity beans

               try
               {
                  Method getPKMethod = value.getClass().getMethod("getPrimaryKey");
                  pk = getPKMethod.invoke(value);
               }
               catch (NoSuchMethodException e)
               {
                  throw new PublicException(
                        BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_NO_GETPK_METHOD
                              .raise(value.getClass()), e);
               }
               catch (IllegalAccessException e)
               {
                  throw new PublicException(
                        BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_PK.raise(), e);
               }
               catch (InvocationTargetException e)
               {
                  throw new PublicException(
                        BpmRuntimeError.EJB_FAILED_OBTAINING_ENTITY_BEAN_PK.raise(), e);
               }
            }
            else
            {
               // assume the value is the PK itself

               // todo check actual value type against PK type from attributes?

               pk = value;
            }
         }
      }

      return pk;
   }

   public Object findEntityByPK(Map attributes, Object pk)
   {
      if (null == pk)
      {
         return null;
      }

      boolean isLocal = Boolean.TRUE.equals(attributes.get(PredefinedConstants
            .IS_LOCAL_ATT));

      String jndiPath = (String) attributes.get(PredefinedConstants
            .JNDI_PATH_ATT);

      Object home = EJBUtils.getHomeObject(jndiPath, getEntityHomeClass(attributes),
            isLocal);

      Assert.isNotNull(home, "The entity bean home object may not be null.");

      Class interfaceClass = getEntityInterfaceClass(attributes);

      try
      {
         Method findByPrimaryKeyMethod = home.getClass().getMethod("findByPrimaryKey",
               new Class[]{getEntityPKClass(attributes)});

         if (trace.isDebugEnabled())
         {
            trace.debug("Found findByPrimaryKey method: "
                  + findByPrimaryKeyMethod);
         }

         Object result = findByPrimaryKeyMethod.invoke(home, new Object[]{pk});

         if (!isLocal)
         {
            result = PortableRemoteObject.narrow(result, interfaceClass);
         }

         return result;
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_FAILED_LOOKING_UP_ENTITY_BEAN_VIA_PK.raise(),
               e.getTargetException());
      }
      catch (Exception e)
      {
         throw new PublicException(
               BpmRuntimeError.EJB_FAILED_LOOKING_UP_ENTITY_BEAN_VIA_PK.raise(), e);
      }
   }

   private static Class getEntityInterfaceClass(Map attributes)
   {
      String className = (String) attributes.get(PredefinedConstants
            .REMOTE_INTERFACE_ATT);

      Class interfaceClass = null;
      if (!StringUtils.isEmpty(className))
      {
         interfaceClass = Reflect.getClassFromClassName(className);
      }
      return interfaceClass;
   }

   private static Class getEntityPKClass(Map attributes)
   {
      Class pkClass = null;

      String pkClassName = (String) attributes.get(PredefinedConstants.PRIMARY_KEY_ATT);
      if (!StringUtils.isEmpty(pkClassName))
      {
         pkClass = Reflect.getClassFromClassName(pkClassName);
      }
      else
      {
         // guess PK class from interface class as of usual conventions

         String interfaceClass = (String) attributes.get(PredefinedConstants
               .REMOTE_INTERFACE_ATT);
         if (!StringUtils.isEmpty(interfaceClass))
         {
            pkClass = Reflect.getClassFromClassName(interfaceClass + "PK");
         }
      }
      return pkClass;
   }

   public static Class getEntityHomeClass(Map attributes)
   {
      Class homeClass = null;

      String homeClassName = (String) attributes.get(PredefinedConstants
            .HOME_INTERFACE_ATT);
      if (!StringUtils.isEmpty(homeClassName))
      {
         homeClass = Reflect.getClassFromClassName(homeClassName);
      }
      else
      {
         // guess home class from interface class as of usual conventions

         String interfaceClass = (String) attributes.get(PredefinedConstants
               .REMOTE_INTERFACE_ATT);
         if (!StringUtils.isEmpty(interfaceClass))
         {
            homeClass = Reflect.getClassFromClassName(interfaceClass + "Home");
         }
      }
      return homeClass;
   }
}
