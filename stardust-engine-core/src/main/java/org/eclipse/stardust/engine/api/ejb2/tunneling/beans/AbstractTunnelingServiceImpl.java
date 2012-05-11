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
package org.eclipse.stardust.engine.api.ejb2.tunneling.beans;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.ejb2.WorkflowException;
import org.eclipse.stardust.engine.api.ejb2.beans.AbstractEjbServiceImpl;
import org.eclipse.stardust.engine.api.ejb2.tunneling.TunneledContext;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;



/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractTunnelingServiceImpl extends AbstractEjbServiceImpl
{
   private static final Logger trace = LogManager.getLogger(AbstractTunnelingServiceImpl.class);

   private static final long serialVersionUID = 1L;

   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
      super.setSessionContext(ctx);

      if (null != ctx)
      {
         try
         {
            boolean isStateless = ctx.getEJBHome().getEJBMetaData().isStatelessSession();
            if ( !isStateless)
            {
               trace.warn("Tunneling EJB facade of class " + getClass().getName()
                     + " should be deployed as stateless session bean.");
            }
         }
         catch (IllegalStateException ise)
         {
            trace.warn("Failed to determine if EJB is stateful or stateless, assuming stateless.", ise);
         }
         catch (RemoteException re)
         {
            trace.warn("Failed to determine if EJB is stateful or stateless, assuming stateless.", re);
         }
      }
   }

   public InvokerPrincipal login(String username, String password, Map properties)
         throws WorkflowException
   {
      try
      {
         LoggedInUser user = ((ManagedService) service).login(username, password,
               properties);

         return (null != user) //
               ? new InvokerPrincipal(user.getUserId(), user.getProperties())
               : null;
      }
      catch (LoginFailedException e)
      {
         throw new WorkflowException(e);
      }
      catch (PublicException e)
      {
         throw new WorkflowException(e);
      }
   }

   protected Map initInvocationContext(TunneledContext tunneledContext)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal principalBackup = InvokerPrincipalUtils.setCurrent(tunneledContext.getInvokerPrincipal());
            
            if (null != principalBackup)
            {
               return Collections.singletonMap(InvokerPrincipal.class.getName(), principalBackup);
            }
         }
      }
      
      return null;
   }

   protected void clearInvocationContext(TunneledContext tunneledContext, Map contextBackup)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal backup = (null != contextBackup)
            ? (InvokerPrincipal) contextBackup.get(InvokerPrincipal.class.getName())
                  : null;
            
            if (null != backup)
            {
               InvokerPrincipalUtils.setCurrent(backup);
            }
            else
            {
               InvokerPrincipalUtils.removeCurrent();
            }
         }
      }
   }

}
