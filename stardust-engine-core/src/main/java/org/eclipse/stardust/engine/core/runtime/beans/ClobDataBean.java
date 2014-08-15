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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.LazilyEvaluated;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * Holds huge strings in CLOB-columns
 */
public class ClobDataBean extends IdentifiablePersistentBean
      implements Comparable, LazilyEvaluated
{
   /**
    * Database meta information like table name, name of the PK, and name of
    * the PK sequence
    */
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__OWNER_ID = "ownerId";
   public static final String FIELD__OWNER_TYPE = "ownerType";
   public static final String FIELD__STRING_VALUE = "stringValue";
   public static final String FIELD__STRING_KEY = "stringKey";

   public static final FieldRef FR__OID = new FieldRef(ClobDataBean.class, FIELD__OID);
   public static final FieldRef FR__OWNER_ID = new FieldRef(ClobDataBean.class, FIELD__OWNER_ID);
   public static final FieldRef FR__OWNER_TYPE = new FieldRef(ClobDataBean.class, FIELD__OWNER_TYPE);
   public static final FieldRef FR__STRING_VALUE = new FieldRef(ClobDataBean.class, FIELD__STRING_VALUE);
   public static final FieldRef FR__STRING_KEY = new FieldRef(ClobDataBean.class, FIELD__STRING_KEY);

   public static final String TABLE_NAME = "clob_data";
   public static final String DEFAULT_ALIAS = "clb";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "clob_data_seq";
   public static final String[] clob_dt_i1_INDEX =
      new String[]{FIELD__OWNER_ID, FIELD__OWNER_TYPE};
   public static final String[] clob_dt_i2_UNIQUE_INDEX = new String[]{FIELD__OID};
   public static final String[] clob_dt_i3_INDEX =
         new String[]{FIELD__OWNER_ID, FIELD__STRING_KEY};

   public static final boolean TRY_DEFERRED_INSERT = true;

   /**
    * The columns
    */
   private static final int ownerType_COLUMN_LENGTH = 32;
   private long ownerId;

   private String ownerType;

   private static final int stringValue_COLUMN_LENGTH = Integer.MAX_VALUE;
   private String stringValue;

   private static final int stringKey_COLUMN_LENGTH = 255;
   private String stringKey;

   private transient StringValueProvider stringValueProvider;

   public static ClobDataBean find(long oid, Class owner)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      return (ClobDataBean) session.findFirst(ClobDataBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OWNER_ID, oid), Predicates.isEqual(
                        FR__OWNER_TYPE, TypeDescriptor.getTableName(owner)))));
   }

   public static ClobDataBean find(Class<?> owner, String stringKey)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      return (ClobDataBean) session.findFirst(ClobDataBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(
                        FR__OWNER_TYPE, TypeDescriptor.getTableName(owner)),
                        Predicates.isEqual(FR__STRING_KEY, stringKey))));
   }

   /**
    * The default constructor
    */
   public ClobDataBean()
   {
   }

   public ClobDataBean(long ownerId, Class owner, String stringValue)
   {
      this();

      this.ownerId = ownerId;
      this.ownerType = TypeDescriptor.getTableName(owner);

      this.stringValue = stringValue;
   }

   public ClobDataBean(long ownerId, Class owner, StringValueProvider stringValueProvider)
   {
      this();

      this.ownerId = ownerId;
      this.ownerType = TypeDescriptor.getTableName(owner);

      this.stringValueProvider = stringValueProvider;
   }

   public ClobDataBean(long ownerId, Class owner, StringValueProvider stringValueProvider, String stringKey)
   {
      this();

      this.ownerId = ownerId;
      this.ownerType = TypeDescriptor.getTableName(owner);

      this.stringValueProvider = stringValueProvider;
      this.stringKey = stringKey;
   }

   /**
    * Getter for the ObjectID for the whole large string object
    *
    * @return the Object ID of the whole large string object
    */
   public long getOwnerID()
   {
      fetch();
      return this.ownerId;
   }

   /**
    * Getter for the data type of this string portion.
    *
    * @return the data type for that string portion
    */
   public String getOwnerType()
   {
      fetch();
      return this.ownerType;
   }

   /**
    * Getter for the whole huge string
    *
    * @return the whole huge string
    */
   public String getStringValue()
   {
      fetch();

      // TODO perform lazy evaluation first?

      return this.stringValue;
   }

   /**
    * Gets the optional string key for the large string object
    *
    * @return an optional string key identifying the large string object.
    */
   public String getStringKey()
   {
      fetch();
      return this.stringKey;
   }

   /**
    * Setter for the whole huge string
    *
    * @param stringValue the whole huge string
    */
   public void setStringValue(String stringValue)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.stringValue, stringValue))
      {
         markModified(FIELD__STRING_VALUE);
         this.stringValue = stringValue;
      }
   }

   public void performLazyEvaluation()
   {
      if (null != stringValueProvider)
      {
         setStringValue(stringValueProvider.getStringValue());
      }
   }

   public StringValueProvider getStringValueProvider()
   {
      return stringValueProvider;
   }

   public void setStringValueProvider(StringValueProvider stringValueProvider, boolean markDirty)
   {
      fetch();

      if (markDirty)
      {
         markModified(FIELD__STRING_VALUE);
      }
      this.stringValueProvider = stringValueProvider;
   }

   public int compareTo(Object rhs)
   {
      ClobDataBean rhsHolder = (ClobDataBean) rhs;

      if (getOID() == rhsHolder.getOID())
      {
         return 0;
      }
      else
      {
         return getOID() < rhsHolder.getOID() ? -1 : 1;
      }
   }

   public static interface StringValueProvider
   {
      String getStringValue();
   }

}
