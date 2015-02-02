package org.eclipse.stardust.test.monitoring;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DeploymentInfoDetails;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
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
public class PartitionMonitorSpiTest implements IPartitionMonitor
{

   @Override
   public void userRealmCreated(IUserRealm realm)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("userRealmCreated",
            "Callback after UserRealm <" + realm.getId() + "> has been created");
   }

   @Override
   public void userRealmDropped(IUserRealm realm)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("userRealmDropped",
            "Callback after UserRealm <" + realm.getId() + "> has been dropped");
   }

   @Override
   public void userCreated(IUser user)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("userCreated",
            "Callback after user <" + user.getId() + "> has been created");
   }

   @Override
   public void userEnabled(IUser user)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("userEnabled",
            "Callback after user <" + user.getId() + "> has been enabled");

   }

   @Override
   public void userDisabled(IUser user)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("userDisabled",
            "Callback after user <" + user.getId() + "> has been disabled");

   }

   @Override
   public void modelDeployed(IModel model, boolean isOverwrite)
         throws DeploymentException
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("modelDeployed",
            "Callback method is DEPRECATED");

   }

   @Override
   public void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("beforeModelDeployment",
            "Callback BEFORE deployment has been executed");
   }

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("afterModelDeployment",
            "Callback AFTER deployment has been executed");

   }

   @Override
   public void modelDeleted(IModel model) throws DeploymentException
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("modelDeleted",
            "Callback AFTER model <" + model.getId() + "> has been deleted");
   }

   @Override
   public void modelLoaded(IModel model)
   {
      PartitionMonitorSpiTestLog.getInstance().addLogEntry("modelLoaded",
            "Callback AFTER model <" + model.getId() + "> has been loaded");

   }

}
