/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.monitoring;

import java.util.Collection;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.spi.monitoring.IPartitionMonitor;

public class AbstractPartitionMonitor implements IPartitionMonitor
{
   @Override
   public void userRealmCreated(IUserRealm realm) {}

   @Override
   public void userRealmDropped(IUserRealm realm) {}

   @Override
   public void userCreated(IUser user) {}

   @Override
   public void userEnabled(IUser user) {}

   @Override
   public void userDisabled(IUser user) {}

   @Override
   public void modelDeployed(IModel model, boolean isOverwrite) throws DeploymentException {}

   @Override
   public void beforeModelDeployment(Collection<IModel> models, boolean isOverwrite) throws DeploymentException {}

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite) throws DeploymentException {}

   @Override
   public void modelDeleted(IModel model) throws DeploymentException {}

   @Override
   public void modelLoaded(IModel model) {}
}
