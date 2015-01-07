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
package org.eclipse.stardust.engine.core.spi.monitoring;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;

/**
 * SPI based monitoring for
 * user/realm management
 * model deployment
 * 
 * @author sauer
 * @version $Revision: $
 */
public interface IPartitionMonitor
{
   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param realm
    */
   void userRealmCreated(IUserRealm realm);
   
   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param realm
    */
   void userRealmDropped(IUserRealm realm);
   
   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param user
    */
   void userCreated(IUser user);
   
   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param user
    */
   void userEnabled(IUser user);
   
   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param user
    */
   void userDisabled(IUser user);

   /**
    * Client can check if arguments are valid and otherwise throw an DeploymentException.
    * 
    * @param model
    * @param isOverwrite
    * @throws DeploymentException
    */
   void modelDeployed(IModel model, boolean isOverwrite) throws DeploymentException;
 
   /**
    * Client can check if arguments are valid and otherwise throw an DeploymentException.
    * 
    * @param model
    * @throws DeploymentException
    */
   void modelDeleted(IModel model) throws DeploymentException;

   /**
    * Client can analyze and monitor argument and method call.
    * 
    * @param model
    */
   void modelLoaded(IModel model);   
}