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
package org.eclipse.stardust.engine.core.extensions.data;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataLoader;


/**
 * Default behavior for most of data types
 */
public class DefaultDataTypeLoader implements DataLoader
{

   public void loadData(IData data)
   {
      // nothing should be done here
   }

   public void deployData(IRuntimeOidRegistry rtOidRegistry, IData data, long rtOid, long modelOID, RootElement model)
   {
      // nothing should be done here
   }

}
