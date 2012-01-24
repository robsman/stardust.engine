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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


public class DataTypeBean extends IdentifiableElementBean implements IDataType
{
   public DataTypeBean()
   {
   }

   public DataTypeBean(String id, String name, boolean predefined)
   {
      super(id, name);
      setPredefined(predefined);
   }

   public String toString()
   {
      return "Data Type: " + getName();
   }
}
