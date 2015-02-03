/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.spi.monitoring;

import java.util.Collection;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;

/**
 * <p>
 * Test implementation of a PartitionMonitor
 * </p>
 *
 * @author Thomas.Wolfram
 *
 */
public class TestPartitionMonitor implements IPartitionMonitor
{

   @Override
   public void userRealmCreated(IUserRealm realm)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("userRealmCreated",
            "Callback after UserRealm <" + realm.getId() + "> has been created");
   }

   @Override
   public void userRealmDropped(IUserRealm realm)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("userRealmDropped",
            "Callback after UserRealm <" + realm.getId() + "> has been dropped");
   }

   @Override
   public void userCreated(IUser user)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("userCreated",
            "Callback after user <" + user.getId() + "> has been created");
   }

   @Override
   public void userEnabled(IUser user)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("userEnabled",
            "Callback after user <" + user.getId() + "> has been enabled");

   }

   @Override
   public void userDisabled(IUser user)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("userDisabled",
            "Callback after user <" + user.getId() + "> has been disabled");

   }

   @Override
   public void modelDeployed(IModel model, boolean isOverwrite)
         throws DeploymentException
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("modelDeployed",
            "Callback method is DEPRECATED");

   }

   @Override
   public void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("beforeModelDeployment",
            "Callback BEFORE deployment has been executed");
   }

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("afterModelDeployment",
            "Callback AFTER deployment has been executed");

   }

   @Override
   public void modelDeleted(IModel model) throws DeploymentException
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("modelDeleted",
            "Callback AFTER model <" + model.getId() + "> has been deleted");
   }

   @Override
   public void modelLoaded(IModel model)
   {
      TestPartitionMonitorLog.getInstance().addLogEntry("modelLoaded",
            "Callback AFTER model <" + model.getId() + "> has been loaded");

   }

}
