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
package org.eclipse.stardust.engine.api.ejb2.beans;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.ejb2.WorkflowException;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RemoteServiceImpl extends AbstractEjbServiceImpl
{
   private static final Logger trace = LogManager.getLogger(RemoteServiceImpl.class);

   public void login(String username, String password) throws WorkflowException
   {
      login(username, password, Collections.EMPTY_MAP);
   }

   public void login(String username, String password, Map properties)
         throws WorkflowException
   {
      try
      {
         ((ManagedService) service).login(username, password, properties);
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

   public void logout()
   {
      ((ManagedService) service).logout();
   }
   
}
