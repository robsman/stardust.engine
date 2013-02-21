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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;

import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.engine.api.model.IData;


/**
 * Describes workflow data values being created and modified during process
 * execution.
 */
public interface IDataValue extends ValueProvider<Object>
{
   /**
    *
    */
   public IData getData();

   /**
    *
    */
   public void setValue(Object value, boolean forceRefresh);

   /**
    * Sets the serialized value of the data value directly.
    */
   public Serializable getSerializedValue();

   void refresh();

   IProcessInstance getProcessInstance();
}
