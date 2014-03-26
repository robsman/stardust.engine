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

import java.io.Serializable;

public abstract interface IRepositoryCapabilities extends Serializable
{
   public boolean isFullTextSearchSupported();

   public boolean isMetaDataSearchSupported();
   
   public boolean isMetaDataStorageSupported();

   public boolean isVersioningSupported();

   public boolean isTransactionSupported();

   public boolean isStreamingIOSupported();

   public boolean isAccessControlPolicySupported();
   
   public boolean isWriteSupported();
}
