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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * Holds Preferences
 */
public class PreferencesBean extends PersistentBean implements IPreferences
{
   /**
    * Database meta information like table name, name of the PK, and name of the PK
    * sequence
    */
   public static final String FIELD__OWNER_ID = "ownerId";

   public static final String FIELD__OWNER_TYPE = "ownerType";

   public static final String FIELD__MODULE_ID = "moduleId";

   public static final String FIELD__PREFERENCES_ID = "preferencesId";
   
   public static final String FIELD__PARTITION = "partition";

   public static final String FIELD__STRING_VALUE = "stringValue";

   public static final FieldRef FR__OWNER_ID = new FieldRef(PreferencesBean.class,
         FIELD__OWNER_ID);

   public static final FieldRef FR__OWNER_TYPE = new FieldRef(PreferencesBean.class,
         FIELD__OWNER_TYPE);

   public static final FieldRef FR__MODULE_ID = new FieldRef(PreferencesBean.class,
         FIELD__MODULE_ID);
   
   public static final FieldRef FR__PREFERENCES_ID = new FieldRef(PreferencesBean.class,
         FIELD__PREFERENCES_ID);

   public static final FieldRef FR__PARTITION = new FieldRef(PreferencesBean.class,
         FIELD__PARTITION);
   
   public static final FieldRef FR__STRING_VALUE = new FieldRef(PreferencesBean.class,
         FIELD__STRING_VALUE);

   public static final String TABLE_NAME = "preferences";

   public static final String DEFAULT_ALIAS = "prf";

   public static final String[] PK_FIELD = new String[] {
         FIELD__OWNER_ID, FIELD__OWNER_TYPE, FIELD__MODULE_ID, FIELD__PREFERENCES_ID};

   public static final String[] preferences_idx1_UNIQUE_INDEX = new String[] {
         FIELD__OWNER_ID, FIELD__OWNER_TYPE, FIELD__MODULE_ID, FIELD__PREFERENCES_ID, FIELD__PARTITION};

   /**
    * The columns
    */
   private long ownerId;

   private static final int ownerType_COLUMN_LENGTH = 32;

   private String ownerType;

   private static final int moduleId_COLUMN_LENGTH = 255;

   private String moduleId;

   private static final int preferencesId_COLUMN_LENGTH = 255;

   private String preferencesId;
   
   private long partition;

   private static final int stringValue_COLUMN_LENGTH = Integer.MAX_VALUE;

   private String stringValue;

   public static PreferencesBean find(long ownerId, String scopeId,
         String moduleId, String preferencesId)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      return (PreferencesBean) session.findFirst(PreferencesBean.class,
            QueryExtension.where(Predicates.andTerm(//
                  Predicates.isEqual(FR__OWNER_ID, ownerId),//
                  Predicates.isEqual(FR__OWNER_TYPE, scopeId),//
                  Predicates.isEqual(FR__MODULE_ID, moduleId),//
                  Predicates.isEqual(FR__PREFERENCES_ID, preferencesId))));
   }

   /**
    * The default constructor
    */
   public PreferencesBean()
   {
   }

   public PreferencesBean(long ownerId, String ownerType, String moduleId,
         String preferencesId, short partitionOid, String stringValue)
   {
      this();

      this.ownerId = ownerId;
      this.ownerType = ownerType;
      this.moduleId = StringUtils.cutString(moduleId, moduleId_COLUMN_LENGTH);
      this.preferencesId = StringUtils.cutString(preferencesId,
            preferencesId_COLUMN_LENGTH);

      this.partition=partitionOid;
      this.stringValue = stringValue;
      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /**
    * Getter for the owner scope ID of the preferences object
    * 
    * @return the owner scope ID of the preferences object
    */
   public long getOwnerId()
   {
      fetch();

      return this.ownerId;
   }

   /**
    * Getter for the preference scope type of the preferences object
    * 
    * @return the the the preference scope type of the preferences object
    */
   public String getOwnerType()
   {
      fetch();

      return this.ownerType;
   }

   public String getModuleId()
   {
      fetch();

      return moduleId;
   }

   public String getPreferencesId()
   {
      fetch();

      return preferencesId;
   }
   
   public short getPartitionOid()
   {
      fetch();

      return (short) partition;
   }

   /**
    * Getter for the preferences string
    * 
    * @return the preferences string
    */
   public String getStringValue()
   {
      fetch();

      return this.stringValue;
   }

   /**
    * Setter for the preferences string
    * 
    * @param stringValue
    *           the preferences string
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

}
