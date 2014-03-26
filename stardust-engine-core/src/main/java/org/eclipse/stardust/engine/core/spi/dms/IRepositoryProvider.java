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

import java.util.List;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

@SPI(status = Status.Experimental, useRestriction = UseRestriction.Public)
public interface IRepositoryProvider
{

   public interface Factory
   {
      IRepositoryProvider getInstance();
   }
   
   public String getProviderId();
   
   public List<IRepositoryConfiguration> getDefaultConfigurations();
   
   public IRepositoryService createService(IRepositoryConfiguration configuration, String partitionId);

   public void destroyService(IRepositoryService service);
   
   public IRepositoryProviderInfo getProviderInfo();


}
