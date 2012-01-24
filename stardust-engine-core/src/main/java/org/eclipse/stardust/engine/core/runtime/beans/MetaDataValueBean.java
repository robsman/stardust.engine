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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;


/**
 * Describes workflow data values being created and modified during process
 * execution.
 */
public class MetaDataValueBean implements IDataValue
{
   private static final Logger trace = LogManager.getLogger(MetaDataValueBean.class);

   public long data;
   public ProcessInstanceBean processInstance;
   public Object value;

   public MetaDataValueBean()
   {
   }

   /**
    * Creates an instance of the workflow data. This object manages data created
    * or retrieved during workflow processing.
    * <p/>
    * If the type of the data object is a literal, a literal
    * PersistenceController is created.
    * <p/>
    * If the type is an entity bean reference, a serializable
    * PersistenceController is created
    * to hold the primary key of the entity bean.
    */
   public MetaDataValueBean(IProcessInstance processInstance, IData data)
   {
      this.processInstance = (ProcessInstanceBean) processInstance
            .getScopeProcessInstance();
      this.data = data.getOID();

      if (trace.isDebugEnabled())
      {
         trace.debug("Data value created for '" + data + "' and '" + processInstance
               + "'.");
      }

      setValue(DataValueUtils.createNewValueInstance(data, processInstance), true);
   }

   /**
    * Returns the data, this data value is instantiated from.
    */
   public IData getData()
   {
      return (IData) ModelManagerFactory.getCurrent()
            .lookupObjectByOID(data);
   }

   /**
    * Retrieves the value of the data value.
    * 
    * @return If the type of the data value's data is a literal, the java
    *         wrapper object (<code>Integer</code>, <code>Long</code> etc.) is returned.
    *         If the type is an (entity bean) reference, the entity bean is returned.
    */
   public Object getValue()
   {
      return value;
   }

   // @todo (france, ub): how the forceRefresh semantics precisely works
   /**
    * Sets the PersistenceController of this data value either to the literal
    * provided as a wrapping object in <code>value</code> or the primary key
    * of the entity bean referenced by <code>value</code>.
    */
   public void setValue(Object value, boolean forceRefresh)
   {
      this.value = value;
   }

   // @todo (france, ub): investigate usage of this method in the context of plethora
   /**
    * Retrieves the serialized value of the data value.
    * 
    * @return If the type of the data value's data is a literal, the java
    *         wrapper object (<code>Integer</code>, <code>Long</code> etc.) is returned.
    *         If the type is an (entity bean) reference, the pk of the entity bean is
    *         returned.
    */
   public Serializable getSerializedValue()
   {
      return (Serializable) value;
   }

   public IProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void refresh()
   {
   }
}
