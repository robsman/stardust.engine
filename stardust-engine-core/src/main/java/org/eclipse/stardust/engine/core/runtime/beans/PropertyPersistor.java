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

import java.util.Locale;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * Stores a property information alternatively to the regular property
 * mechanism of Java.
 */
public class PropertyPersistor extends IdentifiablePersistentBean
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__NAME = "name";
   public static final String FIELD__VALUE = "value";
   public static final String FIELD__LOCALE = "locale";
   public static final String FIELD__FLAGS = "flags";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(PropertyPersistor.class, FIELD__OID);
   public static final FieldRef FR__NAME = new FieldRef(PropertyPersistor.class, FIELD__NAME);
   public static final FieldRef FR__VALUE = new FieldRef(PropertyPersistor.class, FIELD__VALUE);
   public static final FieldRef FR__LOCALE = new FieldRef(PropertyPersistor.class, FIELD__LOCALE);
   public static final FieldRef FR__FLAGS = new FieldRef(PropertyPersistor.class, FIELD__FLAGS);
   public static final FieldRef FR__PARTITION = new FieldRef(PropertyPersistor.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "property";
   public static final String DEFAULT_ALIAS = "prp";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "property_seq";
   public static final String[] property_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};
   
   public static final String LOCALE_DEFAULT = "DEFAULT";

   private String name;
   private static final int value_COLUMN_LENGTH = 500;
   private String value;
   private String locale;
   private int flags;
   
   private long partition;

   public static ResultIterator findAll(String locale)
   {
      return findAll(SessionFactory.getSession(SessionFactory.AUDIT_TRAIL), locale);
   }
   
   public static ResultIterator findAll(Session session, String locale)
   {
      return findAll(session, locale, -1);
   }
   
   public static ResultIterator findAll(Session session, String locale, long partitionOid)
   {
      String localeString;

      if (locale == null)
      {
         localeString = LOCALE_DEFAULT;
      }
      else
      {
         localeString = locale.toString();
      }

      return session.getIterator( //
            PropertyPersistor.class, // 
            QueryExtension.where(Predicates.andTerm( //
                  Predicates.isEqual(FR__LOCALE, localeString), //
                  Predicates.isEqual(FR__PARTITION, partitionOid))));
   }
   
   public static String findValueByName(String name)
   {
      return findValueByName(name, -1);
   }
   
   public static String findValueByName(String name, long partitionOid)
   {
      PropertyPersistor property = findByName(name);
      if (property != null)
      {
         return property.getValue();
      }
      return null;
   }

   public static String findValueByName(String name, long partitionOid, boolean huge)
   {
      PropertyPersistor property = findByName(name);
      if (property != null)
      {
         return property.getValue();
      }
      return null;
   }

   /**
    *
    */
   public static PropertyPersistor findByName(String name)
   {
      return findByName(name, null, -1);
   }

   public static PropertyPersistor findByName(String name, long partitionOid)
   {
      return findByName(name, null, partitionOid);
   }

   /**
    * Reserved for future use.
    */
   public static PropertyPersistor findByName(String name, String locale, long partitionOid)
   {
      String localeString;

      if (locale == null)
      {
         localeString = LOCALE_DEFAULT;
      }
      else
      {
         localeString = locale.toString();
      }

      PropertyPersistor propertyPersistor =
            (PropertyPersistor) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findFirst(
                  PropertyPersistor.class,
                  QueryExtension.where(
                        Predicates.andTerm(
                              Predicates.isEqual(FR__NAME, name),
                              Predicates.isEqual(FR__LOCALE, localeString),
                              Predicates.isEqual(FR__PARTITION, partitionOid))));

      if ((null == propertyPersistor) && locale != null)
      {
         propertyPersistor =
               (PropertyPersistor) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findFirst(
                     PropertyPersistor.class,
                     QueryExtension.where(
                           Predicates.andTerm(
                                 Predicates.isEqual(FR__NAME, name),
                                 Predicates.isEqual(FR__LOCALE, LOCALE_DEFAULT),
                                 Predicates.isEqual(FR__PARTITION, partitionOid))));
      }
      return propertyPersistor;
   }

   /**
    * Default constructor for persistence management.
    */
   public PropertyPersistor()
   {
   }

   /**
    * Creates a global property with DEFAULT local.
    * 
    * @param name the name of the property.
    * @param value of the property.
    * 
    */
   public PropertyPersistor(String name, String value)
   {
      this(name, value, null, null);
   }
   
   /**
    * Creates a property with DEFAULT local for a given partition.
    * 
    * @param name the name of the property.
    * @param value of the property.
    * @param partition
    * 
    */
   public PropertyPersistor(String name, String value, IAuditTrailPartition partition)
   {
      this(name, value, null, partition);
   }
   
   /**
    * Reserved for future use.
    */
   public PropertyPersistor(String name, String value, Locale locale)
   {
      this(name, value, locale, null);
   }

   /**
    * Reserved for future use.
    */
   public PropertyPersistor(String name, String value, Locale locale, IAuditTrailPartition partition)
   {
      this.name = name;
      this.value = value;

      this.partition = partition != null ? partition.getOID() : -1;

      if (locale != null)
      {
         this.locale = locale.toString();
      }
      else
      {
         this.locale = LOCALE_DEFAULT;
      }

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /**
    * Returns the value of the property.
    */
   public String getName()
   {
      fetch();
      return name;
   }

   /**
    * Returns the value of the property.
    */
   public String getValue()
   {
      fetch();
      return value;
   }

   /**
    * Sets the value of the property.
    */
   public void setValue(String value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.value, value))
      {
         markModified(FIELD__VALUE);
         this.value = value;
      }
   }
   
   public long getPartition()
   {
      fetch();
      return partition;
   }
}
