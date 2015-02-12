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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * Holds huge strings in CLOB-columns
 */
public class ClobDataBean extends IdentifiablePersistentBean
      implements Comparable, LazilyEvaluated, IProcessInstanceAware
{
   /**
    * Database meta information like table name, name of the PK, and name of
    * the PK sequence
    */
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__OWNER_ID = "ownerId";
   public static final String FIELD__OWNER_TYPE = "ownerType";
   public static final String FIELD__STRING_VALUE = "stringValue";

   public static final FieldRef FR__OID = new FieldRef(ClobDataBean.class, FIELD__OID);
   public static final FieldRef FR__OWNER_ID = new FieldRef(ClobDataBean.class, FIELD__OWNER_ID);
   public static final FieldRef FR__OWNER_TYPE = new FieldRef(ClobDataBean.class, FIELD__OWNER_TYPE);
   public static final FieldRef FR__STRING_VALUE = new FieldRef(ClobDataBean.class, FIELD__STRING_VALUE);

   public static final String TABLE_NAME = "clob_data";
   public static final String DEFAULT_ALIAS = "clb";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "clob_data_seq";
   public static final String[] clob_dt_i1_INDEX =
      new String[]{FIELD__OWNER_ID, FIELD__OWNER_TYPE};
   public static final String[] clob_dt_i2_UNIQUE_INDEX = new String[]{FIELD__OID};

   public static final boolean TRY_DEFERRED_INSERT = true;

   /**
    * The columns
    */
   private static final int ownerType_COLUMN_LENGTH = 32;
   private long ownerId;

   private String ownerType;

   private static final int stringValue_COLUMN_LENGTH = Integer.MAX_VALUE;
   private String stringValue;

   private transient StringValueProvider stringValueProvider;

   public static ClobDataBean find(long oid, Class owner)
   {
      final Session session = (Session)SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      return (ClobDataBean) session.findFirst(ClobDataBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OWNER_ID, oid),
                  Predicates.isEqual(FR__OWNER_TYPE, TypeDescriptor.getTableName(owner)))));
   }

   public static ClobDataBean find(long ownerId, Class<?> owner, String stringValueLike)
   {
      final Session session = (Session)SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      return (ClobDataBean) session.findFirst(ClobDataBean.class,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OWNER_ID, ownerId),
                  Predicates.isEqual(FR__OWNER_TYPE, TypeDescriptor.getTableName(owner)),
                  Predicates.isLike(FR__STRING_VALUE, stringValueLike))));
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

   @Override
   public IProcessInstance getProcessInstance()
   {
      String ownerType = getOwnerType();
      if (DataValueBean.TABLE_NAME.equals(ownerType))
      {
         return getDataValueProcessInstance();
      }
      else if (StructuredDataValueBean.TABLE_NAME.equals(ownerType))
      {
         return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findByOID(StructuredDataValueBean.class, getOwnerID()).getProcessInstance();
      }

      // TODO are all owner types covered?

      throw new UnsupportedOperationException("Cannot determine the process instance due to an unknown owner type: '" + ownerType + "'");
   }

   private IProcessInstance getDataValueProcessInstance()
   {
      long dvOid;
      QueryDescriptor clobQuery = QueryDescriptor.from(DataValueBean.class)
            .select(DataValueBean.FIELD__OID)
            .where(Predicates.isEqual(DataValueBean.FR__NUMBER_VALUE, getOID()));

      ResultSet rs = ((Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL))
            .executeQuery(clobQuery);
      try
      {
         if (rs.next())
         {
            dvOid = rs.getBigDecimal(DataValueBean.FIELD__OID).longValue();
         }
         else
         {
            dvOid = -1;
         }
      }
      catch (SQLException e)
      {
         throw new IllegalStateException(
               "Can't determine related process instance for clob with id: " + getOID(),
               e);
      }
      if (dvOid != -1)
      {
         return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
               .findByOID(DataValueBean.class, dvOid).getProcessInstance();
      }
      else
      {
         throw new IllegalStateException(
               "Can't determine related process instance for clob with id: " + getOID());
      }
   }

   public static interface StringValueProvider
   {
      String getStringValue();
   }

}
