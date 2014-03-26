/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.User;


public interface IRepositoryInstance
{

   public String getRepositoryId();

   public String getProviderId();
   
   public String getPartitionId();

   public IRepositoryInstanceInfo getRepositoryInstanceInfo();

   public void initialize(Parameters parameters, User user);

   public void cleanup(User user);

   public void close(User user);

}
