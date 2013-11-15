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
package org.eclipse.stardust.engine.core.monitoring;

import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.warn("Failed broadcasting partition monitor event.", e);
         }
      }
   }

   public void modelDeployed(IModel model, boolean isOverwrite) throws DeploymentException
   {
      for (int i = 0; i < monitors.size(); ++i)
      {
         IPartitionMonitor monitor = monitors.get(i);
         try
         {
            monitor.modelDeployed(model, isOverwrite);
         }
         catch (DeploymentException e)
         {
            trace.error("Failed broadcasting partition monitor event.", e);
            throw (DeploymentException) e;            
         }         
         catch (Exception e)
         {
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.error("Failed broadcasting partition monitor event.", e);
            throw (DeploymentException) e;            
         }         
         catch (Exception e)
         {
            trace.warn("Failed broadcasting partition monitor event.", e);
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
            trace.error("Failed broadcasting partition monitor event.", e);
            throw (DeploymentException) e;            
         }         
         catch (Exception e)
         {
            trace.warn("Failed broadcasting partition monitor event.", e);
         }
      }
   }  
}