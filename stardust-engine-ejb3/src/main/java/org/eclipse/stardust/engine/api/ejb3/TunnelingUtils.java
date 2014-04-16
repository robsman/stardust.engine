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
package org.eclipse.stardust.engine.api.ejb3;

import java.rmi.RemoteException;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.ejb3.beans.AbstractEjb3ServiceBean;
import org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
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
            +  homeClassName.substring(insertIdx + 1) + "Impl";
      
      return Reflect.getClassFromClassName(tunnelingHomeClassName);
   }
   
   public static Pair/* <Class, Object> */castToTunnelingRemoteServiceHome(
         Object rawHome, Class homeClass)
   {
      // try to cast to tunneling variant of interface
      Class tunnelingHomeClass = getTunnelingHomeClass(homeClass);
      try
      {
    	 InitialContext context = new InitialContext();
    	 
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
      catch (NamingException nex)
      {
    	  
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

   public static TunneledContext performTunnelingLogin(Ejb3Service endpoint, String userId,
         String password, Map properties) throws WorkflowException, RemoteException
   {
      LoggedInUser loginResult = null;
      
      loginResult = endpoint.login(userId, password, properties);
      
      Assert.condition(null != loginResult, "Tunneling mode login must return an invoker principal.");
         
      return new TunneledContext(new InvokerPrincipal(loginResult.getUserId(), loginResult.getProperties()));
   }
   
}
