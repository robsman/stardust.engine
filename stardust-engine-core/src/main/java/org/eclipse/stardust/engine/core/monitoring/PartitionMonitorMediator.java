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
package org.eclipse.stardust.engine.core.monitoring;

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;

/**
 * @author sauer
 * @version $Revision: $
 */
public class PartitionMonitorMediator implements IPartitionMonitor
{
   private static final String FAILED_BROADCASTING_PARTITION_MONITOR_EVENT = "Failed broadcasting partition monitor event.";

   private static final Logger trace = LogManager.getLogger(PartitionMonitorMediator.class);

   private final List<IPartitionMonitor> monitors;
   
   public PartitionMonitorMediator(List<IPartitionMonitor> monitors)
   {
      this.monitors = monitors;
   }

   public void userRealmCreated(IUserRealm realm)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.userRealmCreated(realm);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }
   
   public void userRealmDropped(IUserRealm realm)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.userRealmDropped(realm);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }
   
   public void userCreated(IUser user)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.userCreated(user);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }

   public void userEnabled(IUser user)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.userEnabled(user);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }
   
   public void userDisabled(IUser user)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.userDisabled(user);
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }

   public void modelDeployed(IModel model, boolean isOverwrite) throws DeploymentException
   {
      // Left empty by intent as this method is marked as deprecated. As long as this
      // is still supported it will be called #beforeModelDeployment(Collection<IModel>, boolean)
   }

   @Override
   public void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.beforeModelDeployment(models, isOverwrite);

            // additionally call deprecated method as long as it is supported
            for (IModel model : models)
            {
               monitor.modelDeployed(model, isOverwrite);
            }
         }
         catch (DeploymentException e)
         {
            trace.error(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
            throw e;
         }         
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.afterModelDeployment(models, isOverwrite);
         }
         catch (DeploymentException e)
         {
            trace.error(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
            TransactionUtils.getCurrentTxStatus().setRollbackOnly();
            throw e;
         }
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }

   public void modelDeleted(IModel model) throws DeploymentException
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.modelDeleted(model);
         }
         catch (DeploymentException e)
         {
            trace.error(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
            throw (DeploymentException) e;            
         }         
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }

   @Override
   public void modelLoaded(IModel model)
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.modelLoaded(model);
         }
         catch (DeploymentException e)
         {
            trace.error(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
            throw (DeploymentException) e;            
         }         
         catch (Exception e)
         {
            trace.warn(FAILED_BROADCASTING_PARTITION_MONITOR_EVENT, e);
         }
      }
   }  
}