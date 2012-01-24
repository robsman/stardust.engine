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

import java.util.Date;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.TimestampProviderUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * @author mgille
 * @version $Revision$
 */
public abstract class AbstractProperty extends IdentifiablePersistentBean
      implements Attribute, BigData
{
   private static final long serialVersionUID = 2L;

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__OBJECT_OID = "objectOID";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__TYPE_KEY = "type_key";
   public static final String FIELD__NUMBER_VALUE = "number_value";
   public static final String FIELD__STRING_VALUE = "string_value";
   public static final String FIELD__LAST_MODIFICATION_TIME = "lastModificationTime";

   // hard coded default column length
   private static final int string_value_COLUMN_LENGTH = 128;
   // during runtime once initialized concrete column length
   private static transient Integer stringValueColumnLength = new Integer(-1);

   private long objectOID;
   private String name;
   private int type_key;
   static final boolean type_key_USE_LITERALS = true;
   private long number_value;
   private String string_value;
   private long lastModificationTime;
   

   private transient BigDataHandler dataHandler;

   /**
    *
    */
   public AbstractProperty()
   {
      this.dataHandler = new LargeStringHolderBigDataHandler(this);
      this.lastModificationTime = TimestampProviderUtils.getTimeStamp().getTime();
   }

   /**
    *
    */
   public AbstractProperty(long objectOID, String name, Object value)
   {
      this();
      this.name = name;
      this.objectOID = objectOID;
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
      setValue(value);
   }

   public long getObjectOID()
   {
      fetch();
      return objectOID;
   }

   /*
    * Returns the name of the property.
    */
   public String getName()
   {
      fetch();
      return name;
   }

   /*
    * Returns the value of the property.
    */
   public Object getValue()
   {
      fetch();
      return dataHandler.read();
   }

   /*
    * Sets the value of the property.
    */
   public void setValue(Object inputValue)
   {
      callOnChange();
      dataHandler.write(inputValue, false);
   }

   /*
    *
    */
   public String getStringifiedValue()
   {
      return getValue().toString();
   }

   public void setShortStringValue(String value)
   {
      fetch();
      if ( !CompareHelper.areEqual(value, string_value))
      {
         callOnChange();
         markModified(FIELD__STRING_VALUE);
         this.string_value = value;
      }
   }

   public void setLongValue(long value)
   {
      fetch();
      if (this.number_value != value)
      {
         callOnChange();
         markModified(FIELD__NUMBER_VALUE);
         this.number_value = value;
      }
   }

   public int getType()
   {
      fetch();
      return this.type_key;
   }

   public long getLongValue()
   {
      fetch();
      return this.number_value;
   }

   public void setType(int type)
   {
      fetch();
      if (this.type_key != type)
      {
         callOnChange();
         markModified(FIELD__TYPE_KEY);
         this.type_key = type;
      }
   }

   public String getShortStringValue()
   {
      fetch();
      return this.string_value;
   }

   public int getShortStringColumnLength()
   {
      if ( -1 == stringValueColumnLength.intValue())
      {
         synchronized (stringValueColumnLength)
         {
            if ( -1 == stringValueColumnLength.intValue())
            {
               TypeDescriptor typeDescriptor = TypeDescriptor.get(getClass());
               stringValueColumnLength = new Integer(typeDescriptor.getPersistentField(
                     FIELD__STRING_VALUE).getLength());
            }
         }
      }
      
      return stringValueColumnLength.intValue();
   }
   
   public Date getLastModificationTime()
   {
      fetch();
      if (Unknown.LONG == lastModificationTime)
      {
         return null;
      }
      
      return new Date(lastModificationTime);
   }
   
   protected void callOnChange()
   {
      setLastModificationTime(TimestampProviderUtils.getTimeStamp().getTime());
   }
   
   private void setLastModificationTime(long lastModificationTime)
   {
      fetch();
      if (this.lastModificationTime != lastModificationTime)
      {
         markModified(FIELD__LAST_MODIFICATION_TIME);
         this.lastModificationTime = lastModificationTime;
      }
   }
}
