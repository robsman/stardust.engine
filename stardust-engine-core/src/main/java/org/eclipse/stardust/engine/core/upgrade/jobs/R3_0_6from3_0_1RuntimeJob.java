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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.upgrade.framework.CreateTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R3_0_6from3_0_1RuntimeJob extends OracleDB2AwareRuntimeUpgradeJob
{
   private static final Version VERSION = new Version(3, 0, 6);
   
   private static final String FIELD_OID = "oid";
   private static final String FIELD_ID = "id";

   private static final String TABLE_NAME_USERGROUP = "usergroup";
   private static final String FIELD_USERGROUP__OID = "oid";
   private static final String FIELD_USERGROUP__ID = "id";
   private static final String FIELD_USERGROUP__NAME = "name";
   private static final String FIELD_USERGROUP__VALID_FROM = "validFrom";
   private static final String FIELD_USERGROUP__VALID_TO = "validTo";
   private static final String FIELD_USERGROUP__DESCRIPTION = "description";

   private static final String TABLE_NAME_USERGROUP_PRP = "usergroup_property";
   private static final String FIELD_USERGROUP_PRP__OID = "oid";
   private static final String FIELD_USERGROUP_PRP__OBJECT_OID = "objectOID";
   private static final String FIELD_USERGROUP_PRP__NAME = "name";
   private static final String FIELD_USERGROUP_PRP__TYPE_KEY = "type_key";
   private static final String FIELD_USERGROUP_PRP__NUMBER_VALUE = "number_value";
   private static final String FIELD_USERGROUP_PRP__STRING_VALUE = "string_value";

   private static final String TABLE_NAME_USER_USERGROUP = "user_usergroup";
   private static final String FIELD_USER_USERGROUP__OID = "oid";
   private static final String FIELD_USER_USERGROUP__WORKFLOW_USER = "workflowUser";
   private static final String FIELD_USER_USERGROUP__USER_GROUP = "userGroup";

   public Version getVersion()
   {
      return VERSION;
   }
   
   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_USERGROUP)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_USERGROUP__OID, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_USERGROUP__ID, String.class, 50);
         private final FieldInfo NAME = new FieldInfo(FIELD_USERGROUP__NAME,
               String.class, 150);
         private final FieldInfo VALID_FROM = new FieldInfo(FIELD_USERGROUP__VALID_FROM,
               Long.TYPE);
         private final FieldInfo VALID_TO = new FieldInfo(FIELD_USERGROUP__VALID_TO,
               Long.TYPE);
         private final FieldInfo DESCRIPTION = new FieldInfo(FIELD_USERGROUP__DESCRIPTION,
               String.class);
         
         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, ID, NAME, VALID_FROM, VALID_TO, DESCRIPTION};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo(TABLE_NAME_USERGROUP + "_idx1", true,
                        new FieldInfo[] {OID}),
                  new IndexInfo(TABLE_NAME_USERGROUP + "_idx2", true,
                        new FieldInfo[] {ID})};
         }

         public String getSequenceName()
         {
            return !item.isArchiveAuditTrail() ? TABLE_NAME_USERGROUP + "_seq" : null;
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_USERGROUP_PRP)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_USERGROUP_PRP__OID, Long.TYPE);
         private final FieldInfo OBJECT_OID = new FieldInfo(
               FIELD_USERGROUP_PRP__OBJECT_OID, Long.TYPE);
         private final FieldInfo NAME = new FieldInfo(FIELD_USERGROUP_PRP__NAME,
               String.class);
         private final FieldInfo TYPE_KEY = new FieldInfo(FIELD_USERGROUP_PRP__TYPE_KEY,
               Integer.TYPE);
         private final FieldInfo NUMBER_VALUE = new FieldInfo(
               FIELD_USERGROUP_PRP__NUMBER_VALUE, Long.TYPE);
         private final FieldInfo STRING_VALUE = new FieldInfo(
               FIELD_USERGROUP_PRP__STRING_VALUE, String.class, 128);

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, OBJECT_OID, NAME, TYPE_KEY, NUMBER_VALUE, STRING_VALUE};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("usergrp_prp_idx1", new FieldInfo[] {OBJECT_OID}),
                  new IndexInfo("usergrp_prp_idx2", new FieldInfo[] {
                        TYPE_KEY, NUMBER_VALUE}),
                  new IndexInfo("usergrp_prp_idx3", new FieldInfo[] {
                        TYPE_KEY, STRING_VALUE}),
                  new IndexInfo("usergrp_prp_idx4", true, new FieldInfo[] {OID})};
         }

         public String getSequenceName()
         {
            return !item.isArchiveAuditTrail() ? TABLE_NAME_USERGROUP_PRP + "_seq" : null;
         }
      }, this);
      
      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_USER_USERGROUP)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_USER_USERGROUP__OID, Long.TYPE);
         private final FieldInfo WORKFLOW_USER = new FieldInfo(
               FIELD_USER_USERGROUP__WORKFLOW_USER, Long.TYPE);
         private final FieldInfo USER_GROUP = new FieldInfo(
               FIELD_USER_USERGROUP__USER_GROUP, Long.TYPE);

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, WORKFLOW_USER, USER_GROUP};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("user_usergrp_idx1", true, new FieldInfo[] {OID}),
                  new IndexInfo("user_usergrp_idx2", new FieldInfo[] {WORKFLOW_USER}),
                  new IndexInfo("user_usergrp_idx3", new FieldInfo[] {USER_GROUP})};
         }

         public String getSequenceName()
         {
            return !item.isArchiveAuditTrail() ? TABLE_NAME_USER_USERGROUP + "_seq" : null;
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      // no data migration needed
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      // no schema finalization needed
   }

   @Override
   protected void printUpgradeSchemaInfo()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void printMigrateDataInfo()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   protected void printFinalizeSchemaInfo()
   {
      // TODO Auto-generated method stub
      
   }
}
