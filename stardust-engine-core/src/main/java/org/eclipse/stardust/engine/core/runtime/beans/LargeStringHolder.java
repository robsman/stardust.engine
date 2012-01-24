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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.DefaultPersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * This class helps to store large string data. It is a persistent class that
 * represents a record in the STRING_DATA table. Each records is a part of a
 * larger string that was splitted into smaller parts.
 *
 * <p>Large string are stored by splitting them into portions of 4000 characters
 * and save each portion in a record of the STRING_DATA table. When the string
 * have to be retrieved from the database all string portions are retrieved in
 * the correct order (the same as at save time) and concatenated together.
 *
 * <p>This is done by static utility methods in this class. So client have no
 * need to deal with splitting and concatnating the string.
 *
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class LargeStringHolder extends IdentifiablePersistentBean implements Comparable
{
   public static final String END_MARKER = "\\";
   
   /**
    * Database meta information like table name, name of the PK, and name of
    * the PK sequence
    */
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__OBJECTID = "objectid";
   public static final String FIELD__DATA_TYPE = "data_type";
   public static final String FIELD__DATA = "data";

   public static final FieldRef FR__OID = new FieldRef(LargeStringHolder.class, FIELD__OID);
   public static final FieldRef FR__OBJECTID = new FieldRef(LargeStringHolder.class, FIELD__OBJECTID);
   public static final FieldRef FR__DATA_TYPE = new FieldRef(LargeStringHolder.class, FIELD__DATA_TYPE);
   public static final FieldRef FR__DATA = new FieldRef(LargeStringHolder.class, FIELD__DATA);

   public static final String TABLE_NAME = "STRING_DATA";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "STRING_DATA_SEQ";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] str_dt_i1_INDEX =
         new String[]{FIELD__OBJECTID, FIELD__DATA_TYPE};
   public static final String[] str_dt_i2_UNIQUE_INDEX = new String[]{FIELD__OID};

   /**
    * The columns
    */
   private long objectid;
   private static final int data_type_COLUMN_LENGTH = 32;
   private String data_type;
   private static final int data_COLUMN_LENGTH = 4000;
   private String data;
   
   transient private boolean useEndMarker = false;

   /**
    * Constant defining the maximum size of a string portion that can be
    * stored in a VARCHAR field
    */
   private static int ATOM_SIZE = 1000;
   
   /**
    * Finds all string portions for an OID and persistent class in the
    * database and returns an iterator on them.
    *
    * @param oid the oid of the object this string portion is assigned to
    * @param persistent the class of the object the oid is related to.
    * @return an iterator containing all string portion for the passed OID.
    */
   public static ClosableIterator findAllByOID(long oid, Class persistent,
         boolean considerDisk)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      TypeDescriptor foreignType = TypeDescriptor.get(persistent);
      final String tableName = (null != foreignType)
            ? foreignType.getTableName()
            : TypeDescriptor.getTableName(persistent);

      SortedMap result = null;
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         // if we find any large strings in the session cache, it is safe to assume we
         // have all as the engine always loads all or nothing
         Collection cachedParts = ((org.eclipse.stardust.engine.core.persistence.jdbc.Session) session).getCache(LargeStringHolder.class);
         if ((null != cachedParts) && !cachedParts.isEmpty())
         {
            for (Iterator i = cachedParts.iterator(); i.hasNext();)
            {
               PersistenceController pc = (PersistenceController) i.next();
               
               if ( !((DefaultPersistenceController) pc).isDeleted())
               {
                  LargeStringHolder cachedPart = (LargeStringHolder) pc.getPersistent();
                  
                  if ((cachedPart.getObjectID() == oid)
                        && tableName.equals(cachedPart.getDataType()))
                  {
                     if (null == result)
                     {
                        result = CollectionUtils.newSortedMap();
                     }
                     
                     result.put(new Long(cachedPart.getOID()), cachedPart);
                  }
               }
            }
         }
      }
      
      if (considerDisk)
      {
         ClosableIterator recordsFromDisk = session.getIterator(LargeStringHolder.class,
               QueryExtension
               .where(Predicates.andTerm(
                     Predicates.isEqual(FR__OBJECTID, oid),
                     Predicates.isEqual(FR__DATA_TYPE, tableName)))
                     .addOrderBy(FR__OID));
         
         if ((null == result) || result.isEmpty())
         {
            return recordsFromDisk;
         }
         else
         {
            try
            {
               while (recordsFromDisk.hasNext())
               {
                  LargeStringHolder part = (LargeStringHolder) recordsFromDisk.next();
                  
                  result.put(new Long(part.getOID()), part);
               }
            }
            finally
            {
               recordsFromDisk.close();
            }
         }
      }

      return ClosableIteratorAdapter.newIteratorAdapter((null != result)
            ? result.values().iterator()
            : Collections.EMPTY_LIST.iterator());
   }

   /**
    * Deletes all string portions (the whole large string) from the database
    * that are related to an OID and the class of the object represented by the
    * OID.
    *
    * @param oid the oid of the object
    * @param persistent the class of the object represented by OID.
    */
   public static void deleteAllForOID(long oid, Class persistent)
   {
      deleteAllForOID(oid, persistent, true);
   }

   /**
    * Deletes all string portions (the whole large string) from the database
    * that are related to an OID and the class of the object represented by the
    * OID.
    *
    * @param oid the oid of the object
    * @param persistent the class of the object represented by OID.
    */
   public static void deleteAllForOID(long oid, Class persistent, boolean considerDisk)
   {
      ClosableIterator i = findAllByOID(oid, persistent, considerDisk);
      try
      {
         while (i.hasNext())
         {
            ((LargeStringHolder) i.next()).delete(true);
         }
      }
      finally
      {
         i.close();
      }
   }

   /**
    * Deletes all string portions (the whole large string) from the database
    * that are related to an OID and the class of the object represented by the
    * OID.
    *
    * @param persistent the class of the object represented by OID.
    */
   public static void deleteAllForDataType(Class persistent)
   {
      TypeDescriptor foreignType = TypeDescriptor.get(persistent);
      final String tableName = (null != foreignType)
            ? foreignType.getTableName()
            : TypeDescriptor.getTableName(persistent);

      ClosableIterator iterator =
            SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
                  LargeStringHolder.class,
                  QueryExtension.where(
                        Predicates.isEqual(FR__DATA_TYPE, tableName)));

      try
      {
         while (iterator.hasNext())
         {
            ((LargeStringHolder) iterator.next()).delete(true);
         }

      }
      finally
      {
         iterator.close();
      }
   }

   /**
    * Retrieves a large string from the database, by first getting an iterator
    * on all string portions and then concatnate them. This result is returned.
    *
    * @param oid the OID of an object.
    * @param persistent the class of the object represented by OID
    * @return the large string
    */
   public static String getLargeString(long oid, Class persistent)
   {
      return getLargeString(oid, persistent, true);
   }

   /**
    * Retrieves a large string from the database, by first getting an iterator
    * on all string portions and then concatnate them. This result is returned.
    *
    * @param oid the OID of an object.
    * @param persistent the class of the object represented by OID
    * @return the large string
    */
   public static String getLargeString(long oid, Class persistent, boolean considerDisk)
   {
      ClosableIterator parts = findAllByOID(oid, persistent, considerDisk);

      try
      {
         if (parts.hasNext())
         {
            StringBuffer joinBuffer = new StringBuffer();

            while (parts.hasNext())
            {
               LargeStringHolder part = (LargeStringHolder) parts.next();
               
               if (null != part.getData())
               {
                  joinBuffer.append(part.getData());
               }
            }
            
            return joinBuffer.toString();
         }
      }
      finally
      {
         parts.close();
      }

      return null;
   }
   
   public static void setLargeString(long oid, Class persistent, String value)
   {
      setLargeString(oid, persistent, value, true);
   }
   
   /**
    * Stores a large string in the database. The passed string is splitted into
    * portions of 4000 characters and then stored in the database using
    * instances of this class.
    *
    * @param oid the oid of the object
    * @param persistent the class of the object represented by OID
    * @param value the large string to store.
    * @param considerDisk if there might be an old value in the DB
    */
   public static void setLargeString(long oid, Class persistent, String value,
         boolean considerDisk)
   {
      deleteAllForOID(oid, persistent, considerDisk);

      if (value == null)
      {
         return;
      }
      
      int loopCount = ((value.length() - 1) / ATOM_SIZE) + 1;

      for (int i = 0; i < loopCount; i++)
      {
         String part;

         if (i == (loopCount - 1))
         {
            part = value.substring(ATOM_SIZE * i);
         }
         else
         {
            part = value.substring(ATOM_SIZE * i, ATOM_SIZE * (i + 1));
         }

         LargeStringHolder holder = new LargeStringHolder(oid, persistent, part);
         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(holder);
      }
   }

   /**
    *
    */
   public static byte[] getLargeBinary(long oid, Class persistent)
   {
      String base64String = getLargeString(oid, persistent);

      if ((base64String == null) || (base64String.length() == 0))
      {
         return null;
      }

      return Base64.decode(base64String.getBytes());
   }

   /**
    *
    */
   public static void setLargeBinary(long oid, Class persistent, byte[] value)
   {
      if (value == null)
      {
         deleteAllForOID(oid, persistent);
         return;
      }

      String base64String = new String(Base64.encode(value));

      setLargeString(oid, persistent, base64String);
   }

   /**
    * The default constructor
    */
   public LargeStringHolder()
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
         useEndMarker = jdbcSession.getDBDescriptor().isTrimmingTrailingBlanks();
      }
   }

   /**
    * This constructor is used when portions of string have to be stored into the
    * database.
    *
    * @param objectid the oid of the object
    * @param persistent the class of the object represented by oid
    * @param data the string portion
    */
   protected LargeStringHolder(long objectid, Class persistent, String data)
   {
      this();
      
      this.objectid = objectid;
      this.data = data;

      if (useEndMarker)
      {
         this.data += END_MARKER;
      }

      TypeDescriptor foreignType = TypeDescriptor.get(persistent);
      this.data_type = (null != foreignType)
            ? foreignType.getTableName()
            : TypeDescriptor.getTableName(persistent);
   }

   /**
    * Getter for the data type of this string portion.
    *
    * @return the data type for that string portion
    */
   public String getDataType()
   {
      fetch();
      return data_type;
   }

   /**
    * Getter for the ObjectID for the whole large string object
    *
    * @return the Object ID of the whole large string object
    */
   public long getObjectID()
   {
      fetch();
      return objectid;
   }

   /**
    * Getter for the portion of the large string
    *
    * @return the string portion represented by an instance of this class
    */
   public String getData()
   {
      fetch();
      String returnData = data;
      if(useEndMarker)
      {
         returnData = data.substring(0, data.length() - 1);
      }
      
      return returnData;
   }

   /**
    * Setter for the string portion
    *
    * @param data the string portion.
    */
   public void setData(String data)
   {
      fetch();
      String useData = data + (useEndMarker ? END_MARKER : "");  
      if ( !CompareHelper.areEqual(this.data, useData))
      {
         markModified(FIELD__DATA);
         this.data = useData;
      }
   }

   public int compareTo(Object rhs)
   {
      LargeStringHolder rhsHolder = (LargeStringHolder) rhs;

      if (getOID() == rhsHolder.getOID())
      {
         return 0;
      }
      else
      {
         return getOID() < rhsHolder.getOID() ? -1 : 1;
      }
   }
}
