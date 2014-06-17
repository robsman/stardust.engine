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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;

/**
 * @author sauer
 * @version $Revision: $
 */
public class TunnelingUtils
{
   public static TunneledContext performTunnelingLogin(ManagedService service, String userId,
         String password, Map<?, ?> properties) throws WorkflowException, RemoteException
   {
      LoggedInUser loginResult = service.login(userId, password, properties);

      Object signedPrincipal = loginResult.getProperties().get(InvokerPrincipal.PRP_SIGNED_PRINCIPAL);
      Assert.condition(null != loginResult, "Tunneling mode login must return an invoker principal.");
      if (signedPrincipal instanceof InvokerPrincipal)
      {
         return new TunneledContext((InvokerPrincipal) signedPrincipal);
      }
      else
      {
         return new TunneledContext(new InvokerPrincipal(loginResult.getUserId(), loginResult.getProperties()));
      }
   }
}
