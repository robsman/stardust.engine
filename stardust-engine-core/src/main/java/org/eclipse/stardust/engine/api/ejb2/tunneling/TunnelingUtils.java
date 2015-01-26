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
package org.eclipse.stardust.engine.api.ejb2.tunneling;

import java.rmi.RemoteException;
import java.util.Map;

import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;

/**
 * @author sauer
 * @version $Revision: $
 */
public class TunnelingUtils
{
   private static final Logger trace = LogManager.getLogger(TunnelingUtils.class);

   public static Class getTunnelingHomeClass(Class homeClass)
   {
      String homeClassName = homeClass.getName();
      int insertIdx = homeClassName.lastIndexOf(".");

      String tunnelingHomeClassName = homeClassName.substring(0, insertIdx + 1)
            + "tunneling.Tunneling" + homeClassName.substring(insertIdx + 1);

      return Reflect.getClassFromClassName(tunnelingHomeClassName);
   }

   public static Pair/* <Class, Object> */castToTunnelingRemoteServiceHome(
         Object rawHome, Class homeClass)
   {
      // try to cast to tunneling variant of interface
      Class tunnelingHomeClass = getTunnelingHomeClass(homeClass);
      try
      {
         Object home = PortableRemoteObject.narrow(rawHome, tunnelingHomeClass);

         // narrow succeeded, so tunneling will be used
         if (trace.isDebugEnabled())
         {
            trace.debug("Switching to tunneling mode for service with home class "
                  + homeClass);
         }

         return new Pair(tunnelingHomeClass, home);
      }
      catch (ClassCastException cce)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Failed switching to tunneling mode for service with home class "
                  + homeClass, cce);
         }
      }

      return null;
   }

   public static Pair/* <Class, Object> */castToTunnelingLocalServiceHome(Object rawHome,
         Class homeClass)
   {
      Class tunnelingHomeClass = getTunnelingHomeClass(homeClass);
      if (tunnelingHomeClass.isInstance(rawHome))
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Switching to tunneling mode service with home class "
                  + homeClass);
         }

         return new Pair(tunnelingHomeClass, rawHome);
      }

      return null;
   }

   public static TunneledContext performTunnelingLogin(TunnelingService endpoint, String userId,
         String password, Map properties) throws WorkflowException, RemoteException
   {
      InvokerPrincipal loginResult = null;
      if (endpoint instanceof TunnelingRemoteService)
      {
         loginResult = ((TunnelingRemoteService) endpoint).login(userId, password,
               properties);
      }
      else if (endpoint instanceof TunnelingLocalService)
      {
         loginResult = ((TunnelingLocalService) endpoint).login(userId, password,
               properties);
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.EJB_INVALID_TUNNELING_SERVICE_ENDPOINT
                     .raise());
      }

      Assert.condition(null != loginResult, "Tunneling mode login must return an invoker principal.");

      return new TunneledContext(loginResult);
   }

}
