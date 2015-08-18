/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct.beans;

import java.io.Serializable;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstanceAware;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolderBigDataHandler;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;


/**
 * Describes workflow data values of structured data being created and modified during
 * process execution.
 */
public class StructuredDataValueBean extends IdentifiablePersistentBean
      implements IStructuredDataValue, BigData, IProcessInstanceAware
{

   private static final long serialVersionUID = 722289133134906543L;

   private static final Logger trace = LogManager.getLogger(StructuredDataValueBean.class);

   /**
    * Providing this instance will result in default initialization.
    */
   public static final Object USE_DEFAULT_INITIAL_VALUE = new Object();

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__PARENT = "parent";
   public static final String FIELD__ENTRY_KEY = "entryKey";
   public static final String FIELD__XPATH = "xpath";
   public static final String FIELD__TYPE_KEY = "type_key";
   public static final String FIELD__STRING_VALUE = "string_value";
   public static final String FIELD__NUMBER_VALUE = "number_value";
   public static final String FIELD__DOUBLE_VALUE = "double_value";

   public static final FieldRef FR__OID = new FieldRef(StructuredDataValueBean.class,
         FIELD__OID);

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(
         StructuredDataValueBean.class, FIELD__PROCESS_INSTANCE);

   public static final FieldRef FR__PARENT = new FieldRef(
         StructuredDataValueBean.class, FIELD__PARENT);

   public static final FieldRef FR__ENTRY_KEY = new FieldRef(
         StructuredDataValueBean.class, FIELD__ENTRY_KEY);

   public static final FieldRef FR__XPATH = new FieldRef(
         StructuredDataValueBean.class, FIELD__XPATH);

   public static final FieldRef FR__TYPE_KEY = new FieldRef(
         StructuredDataValueBean.class, FIELD__TYPE_KEY);

   public static final FieldRef FR__STRING_VALUE = new FieldRef(
         StructuredDataValueBean.class, FIELD__STRING_VALUE);

   public static final FieldRef FR__NUMBER_VALUE = new FieldRef(
         StructuredDataValueBean.class, FIELD__NUMBER_VALUE);

   public static final FieldRef FR__DOUBLE_VALUE = new FieldRef(
         StructuredDataValueBean.class, FIELD__DOUBLE_VALUE);

   public static final String TABLE_NAME = "structured_data_value";
   public static final String DEFAULT_ALIAS = "sdv";
   public static final String LOCK_TABLE_NAME = "structured_data_value_lck";
   public static final String LOCK_INDEX_NAME = "struct_dv_lck_idx";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "structured_data_value_seq";

   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] struct_dv_index1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] struct_dv_index2_INDEX = new String[] {FIELD__PARENT};
   public static final String[] struct_dv_index3_INDEX = new String[] {FIELD__XPATH};
   public static final String[] struct_dv_index4_INDEX = new String[] {FIELD__TYPE_KEY};
   public static final String[] struct_dv_index5_INDEX = new String[] {FIELD__NUMBER_VALUE};
   public static final String[] struct_dv_index6_INDEX = new String[] {FIELD__STRING_VALUE};
   public static final String[] struct_dv_index7_INDEX = new String[] {FIELD__PROCESS_INSTANCE};

   // @todo in the end, COLUMN_LENGTH should be the metadata and not COLUMN_TYPE
   public static final int string_value_COLUMN_LENGTH = 128;
   public static final int entryKey_COLUMN_LENGTH = 50;

   protected static final Class LOADER = StructuredDataValueLoader.class;

   public ProcessInstanceBean processInstance;
   public long parent;
   public String entryKey;
   public long xpath;
   public int type_key = BigData.NULL;
   public String string_value;
   public long number_value;
   public double double_value;

   static final boolean type_key_USE_LITERALS = true;

   private transient LargeStringHolderBigDataHandler dataHandler;

   public static final String processInstance_REGISTRAR = "addStructuredDataValue";

   /**
    *
    */
   public StructuredDataValueBean()
   {
      dataHandler = new LargeStringHolderBigDataHandler(this);
   }

   /**
    * Creates an instance of the workflow data. This object manages data created or
    * retrieved during workflow processing. <p/> If the type of the data object is a
    * literal, a literal PersistenceController is created. <p/> If the type is an entity
    * bean reference, a serializable PersistenceController is created to hold the primary
    * key of the entity bean.
    *
    * @param parent
    * @param entry_key
    * @param xpath
    */
   public StructuredDataValueBean(IProcessInstance processInstance, long parent, long xpath,
         Object initialValue, String entry_key, int type_key)
   {
      this.processInstance = (ProcessInstanceBean) processInstance.getScopeProcessInstance();
      this.parent = parent;
      this.entryKey = entry_key;
      this.xpath = xpath;
      this.type_key = type_key;

      dataHandler = new LargeStringHolderBigDataHandler(this);
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      // explicitly asking for an atomic data value, as we need to know for the actual
      // value of the data, considered a blob at worst (canonicalizeDataValue treats pairs
      // an collections special as they might be used for BETWEEN or IN predicates)
      if (LargeStringHolderBigDataHandler.canonicalizeAtomicDataValue(
            string_value_COLUMN_LENGTH, initialValue).isLarge())
      {
         session.cluster(this);
         setValue(initialValue, true, false);

         // Update of data cluster will be done in {@link Session#flush()}
      }
      else
      {
         setValue(initialValue, false, false);
         session.cluster(this);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Structured data value entry created for parent_oid '" + parent
               + "' entry_key '" + entry_key + "' xpath_oid '" + xpath + "'.");
      }

      // cache this sdv
      (new StructuredDataValueLoader()).load(this);
   }

   /**
    * Retrieves the value of the data value.
    *
    * @return If the type of the data value's data is a literal, the java wrapper object (<code>Integer</code>,
    *         <code>Long</code> etc.) is returned. If the type is an (entity bean)
    *         reference, the entity bean is returned.
    */
   public Object getValue()
   {
      return dataHandler.read();
   }

   // @todo (france, ub): how the forceRefresh semantics precisely works
   /**
    * Sets the PersistenceController of this data value either to the literal provided as
    * a wrapping object in <code>value</code> or the primary key of the entity bean
    * referenced by <code>value</code>.
    */
   public void setValue(Object value, boolean forceRefresh, boolean applyLock)
   {
      if (applyLock)
      {
         lock();
      }

      dataHandler.write(value, forceRefresh);
   }

   // @todo (france, ub): investigate usage of this method in the context of plethora
   /**
    * Retrieves the serialized value of the data value.
    *
    * @return If the type of the data value's data is a literal, the java wrapper object (<code>Integer</code>,
    *         <code>Long</code> etc.) is returned. If the type is an (entity bean)
    *         reference, the pk of the entity bean is returned.
    */
   public Serializable getSerializedValue()
   {
      return (Serializable) dataHandler.read();
   }

   // BigData interface implementation

   public String getShortStringValue()
   {
      fetch();
      return string_value == null && type_key == BigData.STRING ? "" : string_value;
   }

   public void setShortStringValue(String value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.string_value, value))
      {
         markModified(FIELD__STRING_VALUE);
         string_value = value;
      }
   }

   public long getLongValue()
   {
      fetch();
      return number_value;
   }

   public void setLongValue(long value)
   {
      fetch();
      if (this.number_value != value)
      {
         markModified(FIELD__NUMBER_VALUE);
         number_value = value;
      }
   }

   public int getType()
   {
      fetch();
      return type_key;
   }

   public void setType(int type)
   {
      fetch();
      if (this.type_key != type)
      {
         markModified(FIELD__TYPE_KEY);
         type_key = type;
      }
   }

   public int getShortStringColumnLength()
   {
      return string_value_COLUMN_LENGTH;
   }

   public void refresh()
   {
      dataHandler.refresh();
   }

   public String getEntryKey()
   {
      return this.entryKey;
   }

   public long getParentOID()
   {
      return this.parent;
   }

   public long getXPathOID()
   {
      return this.xpath;
   }

   public boolean isRootEntry()
   {
      if (this.parent == NO_PARENT)
      {
         return true;
      }
      return false;
   }

   public boolean isAttribute()
   {
      if (this.entryKey == null)
      {
         return true;
      }
      return false;
   }

   public boolean isElement()
   {
      if (this.isAttribute() == true)
      {
         return false;
      }
      return true;
   }

   public ProcessInstanceBean getProcessInstance()
   {
      return this.processInstance;
   }

   public String toString()
   {
      return "StructuredDataValueBean: processInstance=<" + this.processInstance + "> parentOid=<"
            + this.parent + "> xPathOid=<" + this.xpath + "> value=<"
            + this.getValue() + "> key=<" + this.entryKey + "> type=<" + this.type_key + ">";
   }

   public double getDoubleValue()
   {
      fetch();
      return double_value;
   }

   @Override
   public void setDoubleValue(double value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.double_value, value))
      {
         markModified(FIELD__DOUBLE_VALUE);
         double_value = value;
      }
   }

}
