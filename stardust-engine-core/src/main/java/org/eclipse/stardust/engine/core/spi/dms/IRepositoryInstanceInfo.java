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

public interface IRepositoryInstanceInfo extends IRepositoryCapabilities
{
   public String getProviderId();

   public String getRepositoryId();

   public String getRepositoryName();

   public String getRepositoryVersion();

   public String getRepositoryType();
}
