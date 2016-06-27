/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.monitoring;

import java.util.Collection;

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

   @Deprecated
   void modelDeployed(IModel model, boolean isOverwrite) throws DeploymentException;
 
   /**
    * Client can check if arguments are valid and otherwise throw an DeploymentException.
    * This is called before deployment/overwrite of all models given models is performed.
    * 
    * @param models
    * @param isOverwrite
    * @throws DeploymentException
    */
   void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite) throws DeploymentException;

   /**
    * Client can check if arguments are valid and otherwise throw an DeploymentException.
    * This is called after deployment/overwrite of all models given models is performed. Still
    *
    *
    * @param models
    * @param isOverwrite
    * @throws DeploymentException
    */
   void afterModelDeployment(Collection<IModel> models, boolean isOverwrite) throws DeploymentException;

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