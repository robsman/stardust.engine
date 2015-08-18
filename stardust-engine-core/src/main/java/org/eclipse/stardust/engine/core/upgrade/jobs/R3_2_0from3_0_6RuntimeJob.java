/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R3_2_0from3_0_6RuntimeJob extends DbmsAwareRuntimeUpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R3_2_0from3_0_6RuntimeJob.class);
   private static final Version VERSION = Version.createFixedVersion(3, 2, 0);

   private static final String PROP_3_2_0_MODEL_TABLE_MIGRATION = "carnot.upgrade.3.2.0.modelTableMigration";

   private static final String DATA_DEF_TAG = "data";
   private static final String ROLE_DEF_TAG = "role";
   private static final String ORGANIZATION_DEF_TAG = "organization";
   private static final String PROCESS_DEF_TAG = "processDefinition";
   private static final String TRIGGER_DEF_TAG = "trigger";
   private static final String ACTIVITY_DEF_TAG = "activity";
   private static final String TRANSITION_DEF_TAG = "transition";
   private static final String EVENT_HANDLER_DEF_TAG = "eventHandler";

   private static final String OID_ATTR = "oid";
   private static final String ID_ATTR = "id";
   private static final String NAME_ATTR = "name";

   private static final String FIELD_OID = "oid";
   private static final String FIELD_ID = "id";
   private static final String FIELD_NAME = "name";
   private static final String FIELD_MODEL = "model";
   private static final String FIELD_PROC_DEF = "processDefinition";
   private static final String FIELD_PROC_INST = "processInstance";
   private static final String FIELD_DATA = "data";
   private static final String FIELD_ACT_DEF = "activity";
   private static final String FIELD_SRC_ACT = "sourceActivity";
   private static final String FIELD_TGT_ACT = "targetActivity";
   private static final String FIELD_TRANS_DEF = "transition";
   private static final String FIELD_PARTICIPANT = "participant";

   private static final String TABLE_NAME_ACT_DEF = "activity";
   private static final String OLD_FIELD_ACT_DEF_PROC_DEF = "process_definition";

   private static final String TABLE_NAME_ACT_INST = "activity_instance";

   private static final String TABLE_NAME_ACT_INST_LOG = "activity_inst_log";

   private static final String TABLE_NAME_DATA_DEF = "data";

   private static final String TABLE_NAME_DATA_VALUE = "data_value";

   private static final String TABLE_NAME_EVENT_BND = "event_binding";
   private static final String FIELD_EVT_BND__HANDLER_OID = "handlerOid";
   private static final String FIELD_EVT_BND__OBJECT_OID = "objectOid";
   private static final String FIELD_EVT_BND__TYPE = "type";

   private static final String TABLE_NAME_EVT_HANDLER = "event_handler";

   private static final String TABLE_NAME_PARTICIPANT_DEF = "participant";

   private static final String TABLE_NAME_PROC_DEF = "process_definition";

   private static final String TABLE_NAME_PROC_INST = "process_instance";

   private static final String TABLE_NAME_PROC_TRIGGER = "process_trigger";

   private static final String TABLE_NAME_TIMER_LOG = "timer_log";
   private static final String FIELD_TIMER_LOG__TRIGGER_OID = "triggerOid";

   private static final String TABLE_NAME_TRANS_DEF = "transition";
   private static final String OLD_FIELD_TRANS_DEF_SRC_ACT = "source_activity";
   private static final String OLD_FIELD_TRANS_DEF_TGT_ACT = "target_activity";

   private static final String TABLE_NAME_TRANS_INST = "trans_inst";

   private static final String TABLE_NAME_TRANS_TOKEN = "trans_token";
   private static final String FIELD_TRANS_TOKEN__IS_CONSUMED = "isConsumed";

   private static final String TABLE_NAME_USR_PARTICIPANT = "user_participant";
   private static final String FIELD_USR_PARTICIPANT__PARTICIPANT = "participant";

   private static final String TABLE_NAME_TMP_OID_MAPPING = "TMP_CARNOT_320_OIDS";
   private static final String FIELD_TMP_OIDS_TYPE = "type";
   private static final String FIELD_TMP_OIDS_OLD_OID = "oldOid";
   private static final String FIELD_TMP_OIDS_MODEL = "model";
   private static final String FIELD_TMP_OIDS_RTOID = "rtOid";

   private static final String MSG_ERROR_INCONSISTENCIES = "Unable to migrate data due "
         + "to {0,choice,1#one reported inconsistency|1<{0,number} reported "
         + "inconsistencies}. Please contact CARNOT support.";

   R3_2_0from3_0_6RuntimeJob()
   {
      super(new DBMSKey[] {
            DBMSKey.ORACLE, DBMSKey.ORACLE9i, DBMSKey.DB2_UDB, DBMSKey.MYSQL,
            DBMSKey.DERBY});
   }

   public Version getVersion()
   {
      return VERSION;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_ACT_DEF)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);
         private final FieldInfo PROC_DEF = new FieldInfo(FIELD_PROC_DEF, Long.TYPE);

         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_ACT_DEF + "_idx1",
               true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_ACT_DEF + "_idx2",
               new FieldInfo[] {ID, OID, MODEL});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL, PROC_DEF};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_ACT_INST)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_ACT_INST_LOG)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_DATA_DEF)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);

         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_DATA_DEF + "_idx1",
               true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_DATA_DEF + "_idx2",
               new FieldInfo[] {ID, OID, MODEL});

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_DATA_VALUE)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_EVENT_BND)
      {
         private final FieldInfo HANDLER_OID = new FieldInfo(FIELD_EVT_BND__HANDLER_OID,
               Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo OBJECT_OID = new FieldInfo(FIELD_EVT_BND__OBJECT_OID,
               Long.TYPE);
         private final FieldInfo TYPE = new FieldInfo(FIELD_EVT_BND__TYPE, Integer.TYPE);

         private final IndexInfo IDX2 = new IndexInfo(TABLE_NAME_EVENT_BND + "_idx2",
               new FieldInfo[] {OBJECT_OID, TYPE, HANDLER_OID, MODEL});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {IDX2};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_EVT_HANDLER)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);
         private final FieldInfo NAME = new FieldInfo(FIELD_NAME, String.class, 100);
         private final FieldInfo PROC_DEF = new FieldInfo(FIELD_PROC_DEF, Long.TYPE);
         private final FieldInfo ACT_DEF = new FieldInfo(FIELD_ACT_DEF, Long.TYPE);

         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_EVT_HANDLER + "_idx1",
               true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_EVT_HANDLER + "_idx2",
               new FieldInfo[] {ID, OID, MODEL});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[]{OID, MODEL, ID, NAME, PROC_DEF, ACT_DEF};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_PARTICIPANT_DEF)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);

         private final IndexInfo PK_IDX = new IndexInfo(TABLE_NAME_PARTICIPANT_DEF
               + "_idx1", true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(TABLE_NAME_PARTICIPANT_DEF
               + "_idx2", new FieldInfo[] {ID, OID, MODEL});

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_PROC_DEF)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);

         private final IndexInfo PK_IDX = new IndexInfo("proc_def_idx1", true,
               new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo("proc_def_idx2", new FieldInfo[] {
               ID, OID, MODEL});

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_PROC_INST)
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {new FieldInfo(FIELD_MODEL, Long.TYPE)};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_PROC_TRIGGER)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);
         private final FieldInfo NAME = new FieldInfo(FIELD_NAME, String.class, 100);
         private final FieldInfo PROC_DEF = new FieldInfo(FIELD_PROC_DEF, Long.TYPE);

         private final IndexInfo PK_IDX = new IndexInfo(
               "proc_trigger_idx1", true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(
               "proc_trigger_idx2", new FieldInfo[] {ID, OID, MODEL});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, MODEL, ID, NAME, PROC_DEF};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_TIMER_LOG)
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {new FieldInfo(FIELD_MODEL, Long.TYPE)};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_TRANS_DEF)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_OID, Long.TYPE);
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo ID = new FieldInfo(FIELD_ID, String.class, 50);
         private final FieldInfo PROC_DEF = new FieldInfo(FIELD_PROC_DEF, Long.TYPE);
         private final FieldInfo SRC_ACT = new FieldInfo(FIELD_SRC_ACT, Long.TYPE);
         private final FieldInfo TGT_ACT = new FieldInfo(FIELD_TGT_ACT, Long.TYPE);

         private final IndexInfo PK_IDX = new IndexInfo(
               "trans_idx1", true, new FieldInfo[] {OID, MODEL});
         private final IndexInfo ID_IDX = new IndexInfo(
               "trans_idx2", new FieldInfo[] { ID, OID, MODEL});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL, PROC_DEF, SRC_ACT, TGT_ACT};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {PK_IDX, ID_IDX};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_TRANS_INST)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_TRANS_TOKEN)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo TRANS_DEF = new FieldInfo(FIELD_TRANS_DEF, Long.TYPE);
         private final FieldInfo PROC_INST = new FieldInfo(FIELD_PROC_INST, Long.TYPE);
         private final FieldInfo IS_CONSUMED = new FieldInfo(
               FIELD_TRANS_TOKEN__IS_CONSUMED, Integer.TYPE);

         private final IndexInfo IDX3 = new IndexInfo(TABLE_NAME_TRANS_TOKEN + "_idx3",
               new FieldInfo[] {PROC_INST, TRANS_DEF, MODEL, IS_CONSUMED});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {IDX3};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_USR_PARTICIPANT)
      {
         private final FieldInfo MODEL = new FieldInfo(FIELD_MODEL, Long.TYPE);
         private final FieldInfo PARTICIPANT = new FieldInfo(
               FIELD_USR_PARTICIPANT__PARTICIPANT, Long.TYPE);

         private final IndexInfo IDX2 = new IndexInfo("user_particip_idx2",
               new FieldInfo[] {PARTICIPANT, MODEL});

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {MODEL};
         }

         public IndexInfo[] getAlteredIndexes()
         {
            return new IndexInfo[] {IDX2};
         }
      }, this);

      DatabaseHelper.createTable(item, new CreateTableInfo(TABLE_NAME_TMP_OID_MAPPING)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_TMP_OIDS_OLD_OID, Long.TYPE);
         private final FieldInfo TYPE = new FieldInfo(FIELD_TMP_OIDS_TYPE, String.class,
               20);
         private final FieldInfo MODEL = new FieldInfo(FIELD_TMP_OIDS_MODEL, Long.TYPE);
         private final FieldInfo RT_OID = new FieldInfo(FIELD_TMP_OIDS_RTOID, Long.TYPE);

         private final IndexInfo PK_IDX = new IndexInfo("tmp_oids_idx1", true,
               new FieldInfo[] {OID, TYPE});

         public FieldInfo[] getFields()
         {
            return new FieldInfo[] {OID, TYPE, MODEL, RT_OID};
         }

         public IndexInfo[] getIndexes()
         {
            return new IndexInfo[] {PK_IDX};
         }

         public String getSequenceName()
         {
            return null;
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      try
      {
         String modelTableMigration = item.getProperty(PROP_3_2_0_MODEL_TABLE_MIGRATION);

         if (StringUtils.isEmpty(modelTableMigration))
         {
            info("Migrating model element tables ...");

            // perform model table migration in one TX
            item.getConnection().setAutoCommit(false);

            // evaluate RT OID mappings
            Map fqIdRegistries = new HashMap();
            fqIdRegistries.put(TABLE_NAME_DATA_DEF, new HashMap());
            fqIdRegistries.put(TABLE_NAME_PARTICIPANT_DEF, new HashMap());
            fqIdRegistries.put(TABLE_NAME_PROC_DEF, new HashMap());
            fqIdRegistries.put(TABLE_NAME_PROC_TRIGGER, new HashMap());
            fqIdRegistries.put(TABLE_NAME_ACT_DEF, new HashMap());
            fqIdRegistries.put(TABLE_NAME_TRANS_DEF, new HashMap());
            fqIdRegistries.put(TABLE_NAME_EVT_HANDLER, new HashMap());

            int nInconsistencies = 0;

            if (item.isArchiveAuditTrail())
            {
               // give runtime OIDs from production audit trail precedence
               String productionSchema = item.getProductionAtSchemaName();
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_DATA_DEF, fqIdRegistries, null, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_PARTICIPANT_DEF, fqIdRegistries, null, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_PROC_DEF, fqIdRegistries, null, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_PROC_TRIGGER, fqIdRegistries, FIELD_PROC_DEF, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_ACT_DEF, fqIdRegistries, FIELD_PROC_DEF, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_TRANS_DEF, fqIdRegistries, FIELD_PROC_DEF, null);
               nInconsistencies += loadMappingFromModelElementTable(productionSchema,
                     TABLE_NAME_EVT_HANDLER, fqIdRegistries, FIELD_PROC_DEF,
                     FIELD_ACT_DEF);

               if (0 != nInconsistencies)
               {
                  item.rollback();
                  error(MessageFormat.format(MSG_ERROR_INCONSISTENCIES,
                        new Object[] {new Integer(nInconsistencies)}), null);
               }
            }

            List triggerDefs = new ArrayList();
            List handlerDefs = new ArrayList();

            for (Iterator modelOidItr = getPost30ModelOIDs(); modelOidItr.hasNext();)
            {
               // TODO order model OIDs ascending to guarantee order of evaluation
               long modelOid = ((Long) modelOidItr.next()).longValue();
               ModelItem modelItem = retrieveModelFromAuditTrail(modelOid);
               Element modelDom = modelItem.getModelElement();

               findGlobalRtOids(modelOid, modelDom, DATA_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_DATA_DEF));
               findGlobalRtOids(modelOid, modelDom, ROLE_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_PARTICIPANT_DEF));
               findGlobalRtOids(modelOid, modelDom, ORGANIZATION_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_PARTICIPANT_DEF));
               findGlobalRtOids(modelOid, modelDom, PROCESS_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_PROC_DEF));

               findProcessPartRtOids(modelOid, modelDom, EVENT_HANDLER_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_EVT_HANDLER), handlerDefs);
               findProcessPartRtOids(modelOid, modelDom, TRIGGER_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_PROC_TRIGGER), triggerDefs);
               findProcessPartRtOids(modelOid, modelDom, ACTIVITY_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_ACT_DEF), null);
               findProcessPartRtOids(modelOid, modelDom, TRANSITION_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_TRANS_DEF), null);

               findActivityPartRtOids(modelOid, modelDom, EVENT_HANDLER_DEF_TAG,
                     (Map) fqIdRegistries.get(TABLE_NAME_EVT_HANDLER), handlerDefs);
            }

            final Map oidRtOidMappings = new HashMap();
            oidRtOidMappings.put(TABLE_NAME_DATA_DEF,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_DATA_DEF)));
            oidRtOidMappings.put(TABLE_NAME_PARTICIPANT_DEF,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_PARTICIPANT_DEF)));
            oidRtOidMappings.put(TABLE_NAME_PROC_DEF,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_PROC_DEF)));
            oidRtOidMappings.put(TABLE_NAME_PROC_TRIGGER,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_PROC_TRIGGER)));
            oidRtOidMappings.put(TABLE_NAME_ACT_DEF,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_ACT_DEF)));
            oidRtOidMappings.put(TABLE_NAME_TRANS_DEF,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_TRANS_DEF)));
            oidRtOidMappings.put(TABLE_NAME_EVT_HANDLER,
                  getOidRtOidMapping((Map) fqIdRegistries.get(TABLE_NAME_EVT_HANDLER)));

            // find audit trail records not contained in RT OID mapping, either check if ID
            // was truncated (error) or element was deleted (add mapping, info)

            nInconsistencies += verifyModelElementTable(TABLE_NAME_DATA_DEF,
                  fqIdRegistries, oidRtOidMappings, null);
            nInconsistencies += verifyModelElementTable(TABLE_NAME_PARTICIPANT_DEF,
                  fqIdRegistries, oidRtOidMappings, null);
            nInconsistencies += verifyModelElementTable(TABLE_NAME_PROC_DEF,
                  fqIdRegistries, oidRtOidMappings, null);

            nInconsistencies += verifyModelElementTable(TABLE_NAME_ACT_DEF,
                  fqIdRegistries, oidRtOidMappings, new ActivityFqRetriever());
            nInconsistencies += verifyModelElementTable(TABLE_NAME_TRANS_DEF,
                  fqIdRegistries, oidRtOidMappings, new TransitionFqRetriever());

            // create special mapping for start transition

            Map transOidRtOidMapping = (Map) oidRtOidMappings.get(TABLE_NAME_TRANS_DEF);
            RtOidMapping mapping = (RtOidMapping) transOidRtOidMapping.get(new Long(
                  -1));
            if (null == mapping)
            {
               mapping = new RtOidMapping((String) null, -1);
               mapping.registerElement(0, -1);
               transOidRtOidMapping.put(new Long(-1), mapping);
            }
            else
            {
               ++nInconsistencies;
               warn("Inconsistency: The transition OID -1 is reserved for special use, "
                     + "but seems to be assigned to regular transitions with OIDs "
                     + mapping.oids + "'.", null);
            }

            if (0 == nInconsistencies)
            {
               // populate mapping table

               populateMappingTable(TABLE_NAME_ACT_DEF, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_DATA_DEF, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_EVT_HANDLER, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_PARTICIPANT_DEF, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_PROC_DEF, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_PROC_TRIGGER, oidRtOidMappings);
               populateMappingTable(TABLE_NAME_TRANS_DEF, oidRtOidMappings);

               // update top-level model elements (data, participant, process definition)
               nInconsistencies += migrateGlobalElementTable(TABLE_NAME_DATA_DEF);
               nInconsistencies += migrateGlobalElementTable(TABLE_NAME_PARTICIPANT_DEF);
               nInconsistencies += migrateGlobalElementTable(TABLE_NAME_PROC_DEF);

               // update 2nd-level model elements (activity, transition)

               nInconsistencies += migrateActivityElementTable();
               nInconsistencies += migrateTransitionElementTable();

               // create process trigger entries

               nInconsistencies += populateTriggerTable(triggerDefs,
                     (Map) fqIdRegistries.get(TABLE_NAME_PROC_DEF));

               // create event handler entries (for process definitions and activities)

               nInconsistencies += populateEventHandlerTable(handlerDefs,
                     (Map) fqIdRegistries.get(TABLE_NAME_PROC_DEF),
                     (Map) fqIdRegistries.get(TABLE_NAME_ACT_DEF));

               if (0 == nInconsistencies)
               {
                  item.createProperty(PROP_3_2_0_MODEL_TABLE_MIGRATION,
                        Boolean.TRUE.toString(), false);

                  item.commit();
                  info("Model element table migration done.");
               }
               else
               {
                  item.rollback();
                  error(MessageFormat.format(MSG_ERROR_INCONSISTENCIES,
                        new Object[] {new Integer(nInconsistencies)}), null);
               }
            }
            else
            {
               item.rollback();
               error(MessageFormat.format(MSG_ERROR_INCONSISTENCIES, new Object[] {new Integer(
                     nInconsistencies)}), null);
            }
         }
         else if (!Boolean.TRUE.toString().equals(modelTableMigration))
         {
            item.rollback();
            error("Unvalid value for property " + PROP_3_2_0_MODEL_TABLE_MIGRATION
                  + ". Please contact CARNOT support.", null);
         }
         else
         {
            info("Skipping (previously performed) model element table migration.");
         }
      }
      catch (SQLException e)
      {
         try
         {
            item.rollback();
         }
         catch (SQLException e1)
         {
            warn("Failed rolling back transaction.", e1);
         }
         error("Failed migrating model element tables.", e);
      }

      try
      {

         // update runtime items (activity inst, activity inst log, data value, event
         // binding, proc inst, timer log, trans inst, trans token, user participant)

         info("Migrating runtime item tables ...");

         int nErrors = 0;

         nErrors += migrateActInstTable();

         nErrors += migrateRuntimeItemTable(TABLE_NAME_ACT_INST_LOG, FIELD_PARTICIPANT,
               TABLE_NAME_PARTICIPANT_DEF);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_DATA_VALUE, FIELD_DATA,
               TABLE_NAME_DATA_DEF);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_EVENT_BND, FIELD_EVT_BND__HANDLER_OID,
               TABLE_NAME_EVT_HANDLER);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_PROC_INST, FIELD_PROC_DEF,
               TABLE_NAME_PROC_DEF);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_TIMER_LOG, FIELD_TIMER_LOG__TRIGGER_OID,
               TABLE_NAME_PROC_TRIGGER);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_TRANS_INST, FIELD_TRANS_DEF,
               TABLE_NAME_TRANS_DEF);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_TRANS_TOKEN, FIELD_TRANS_DEF,
               TABLE_NAME_TRANS_DEF);

         nErrors += migrateRuntimeItemTable(TABLE_NAME_USR_PARTICIPANT, FIELD_PARTICIPANT,
               TABLE_NAME_PARTICIPANT_DEF);

         if (0 == nErrors)
         {
            info("Runtime item table migration done.");
         }
         else
         {
            error(MessageFormat.format(MSG_ERROR_INCONSISTENCIES,
                  new Object[] {new Integer(nErrors)}), null);
         }
      }
      catch (SQLException e)
      {
         try
         {
            item.rollback();
         }
         catch (SQLException e1)
         {
            warn("Failed rolling back transaction.", e1);
         }
         error("Failed migrating runtime item tables.", e);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.dropTable(item, new DropTableInfo(TABLE_NAME_TMP_OID_MAPPING, null),
            this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_ACT_DEF)
      {
         private final FieldInfo OLD_PROC_DEF = new FieldInfo(OLD_FIELD_ACT_DEF_PROC_DEF,
               Long.TYPE);

         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {OLD_PROC_DEF};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_TRANS_DEF)
      {
         private final FieldInfo OLD_SRC_ACT = new FieldInfo(OLD_FIELD_TRANS_DEF_SRC_ACT,
               Long.TYPE);
         private final FieldInfo OLD_TGT_ACT = new FieldInfo(OLD_FIELD_TRANS_DEF_TGT_ACT,
               Long.TYPE);

         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {OLD_SRC_ACT, OLD_TGT_ACT};
         }
      }, this);
   }

   private int loadMappingFromModelElementTable(String schemaName, String tableName,
         Map fqIdRtOidMappings, String procOidColumn, String actOidColumn)
         throws SQLException
   {
      final Map fqIdRtOidMapping = (Map) fqIdRtOidMappings.get(tableName);

      int nInconsistencies = 0;

      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = item.getConnection().createStatement();

         StringBuffer buffer = new StringBuffer(200);
         buffer.append("SELECT ").append(FIELD_OID).append(", ").append(FIELD_ID);
         if (!StringUtils.isEmpty(procOidColumn))
         {
            buffer.append(", ").append(procOidColumn);
         }
         if (!StringUtils.isEmpty(actOidColumn))
         {
            buffer.append(", ").append(actOidColumn);
         }
         buffer.append("  FROM ");
         if (!StringUtils.isEmpty(schemaName))
         {
            buffer.append(schemaName).append(".");
         }
         buffer.append(tableName);

         rs = stmt.executeQuery(buffer.toString());

         while (rs.next())
         {
            long rtOid = rs.getLong(1);
            String id = rs.getString(2);

            // handling elements orphaned during model overwrite operations

            String[] fqId;
            if ( !StringUtils.isEmpty(procOidColumn))
            {
               final long procOid = rs.getLong(3);
               String procId = null;
               Map procFqIdRegistry = (Map) fqIdRtOidMappings.get(TABLE_NAME_PROC_DEF);
               for (Iterator i = procFqIdRegistry.entrySet().iterator(); i.hasNext();)
               {
                  Map.Entry entry = (Map.Entry) i.next();
                  if (((RtOidMapping) entry.getValue()).rtOid == procOid)
                  {
                     procId = ((RtOidMapping) entry.getValue()).fqId[0];
                     break;
                  }
               }

               if (StringUtils.isEmpty(procId))
               {
                  ++nInconsistencies;
                  warn("Inconsistency: The process definition runtime OID " + rtOid
                        + " can not be resolved.", null);
               }

               if ( !StringUtils.isEmpty(actOidColumn))
               {
                  final long actOid = rs.getLong(4);
                  if (0 != actOid)
                  {
                     String actId = null;
                     Map actFqIdRegistry = (Map) fqIdRtOidMappings.get(TABLE_NAME_ACT_DEF);
                     for (Iterator i = actFqIdRegistry.entrySet().iterator(); i.hasNext();)
                     {
                        Map.Entry entry = (Map.Entry) i.next();
                        final RtOidMapping actRtOidMapping = ((RtOidMapping) entry.getValue());
                        if (actRtOidMapping.rtOid == actOid)
                        {
                           if ( !procId.equals(actRtOidMapping.fqId[0]))
                           {
                              ++nInconsistencies;
                              warn("Inconsistency: The associated process definition for "
                                    + "the activity with runtime OID " + rtOid
                                    + " is invalid. Found '" + actRtOidMapping.fqId[0]
                                    + "', expected '" + procId + "'.", null);
                           }
                           actId = actRtOidMapping.fqId[1];
                           break;
                        }
                     }

                     if (StringUtils.isEmpty(actId))
                     {
                        ++nInconsistencies;
                        warn("Inconsistency: The activity definition runtime OID "
                              + rtOid + " can not be resolved.", null);
                     }
                     fqId = new String[] {procId, actId, id};
                  }
                  else
                  {
                     fqId = new String[] {procId, id};
                  }
               }
               else
               {
                  fqId = new String[] {procId, id};
               }
            }
            else
            {
               fqId = new String[] {id};
            }

            RtOidMapping mapping = (RtOidMapping) fqIdRtOidMapping.get(RtOidMapping.getFqIdKey(fqId));
            if (null != mapping)
            {
               // check for consistency
               if (mapping.rtOid != rtOid)
               {
                  ++nInconsistencies;
                  warn("Inconsistency: The fully qualified ID " + fqId
                        + " for a model element from table" + tableName
                        + " is not consistently mapped to exactly one runtime OID. "
                        + "Found multiple values " + rtOid + " vs. " + mapping.rtOid
                        + ".", null);
               }
            }
            else
            {
               fqIdRtOidMapping.put(RtOidMapping.getFqIdKey(fqId), new RtOidMapping(fqId,
                     rtOid));
            }
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }

      return nInconsistencies;
   }

   private void findGlobalRtOids(long modelOid, Element modelDom, String elementTag,
         Map rtOidRegistry)
   {
      try
      {
         XPathExpression modelElementsXPath = XPathFactory.newInstance().newXPath().compile(elementTag);
         NodeList modelElements = (NodeList) modelElementsXPath.evaluate(modelDom, XPathConstants.NODESET);
         for (int i = 0, nNodes = modelElements.getLength(); i < nNodes; i++)
         {
            Element modelElement = (Element) modelElements.item(i);
            String id = modelElement.getAttribute(ID_ATTR);

            String fqIdKey = RtOidMapping.getFqIdKey(new String[] {id});
            RtOidMapping mapping = (RtOidMapping) rtOidRegistry.get(fqIdKey);
            if (null == mapping)
            {
               mapping = new RtOidMapping(id, findAvailableRtOid(rtOidRegistry));
               rtOidRegistry.put(fqIdKey, mapping);
            }
            mapping.registerElement(modelOid,
                  Long.parseLong(modelElement.getAttribute(OID_ATTR)));
         }
      }
      catch (XPathException e)
      {
         warn("Failed evaluating runtime OIDs for model element: " + elementTag, e);
      }
   }

   private long findAvailableRtOid(Map rtOidRegistry)
   {
      long maxRtOid = 0;
      for (Iterator mappingItr = rtOidRegistry.entrySet().iterator(); mappingItr.hasNext();)
      {
         Map.Entry entry = (Map.Entry) mappingItr.next();
         maxRtOid = Math.max(maxRtOid, ((RtOidMapping) entry.getValue()).rtOid);
      }
      return 1 + maxRtOid;
   }

   private void findProcessPartRtOids(long modelOid, Element modelDom, String elementTag,
         Map rtOidRegistry, List definitionCache)
   {
      try
      {
         XPathExpression processPartsXPath = XPathFactory.newInstance().newXPath().compile(PROCESS_DEF_TAG + "/" + elementTag);
         NodeList processParts = (NodeList) processPartsXPath.evaluate(modelDom, XPathConstants.NODESET);
         for (int i = 0, nNodes = processParts.getLength(); i < nNodes; i++)
         {
            Element processPart = (Element) processParts.item(i);
            String partId = processPart.getAttribute(ID_ATTR);

            Element process = (Element) processPart.getParentNode();
            String processId = process.getAttribute(ID_ATTR);

            String fqIdKey = RtOidMapping.getFqIdKey(new String[] {processId, partId});
            RtOidMapping mapping = (RtOidMapping) rtOidRegistry.get(fqIdKey);
            if (null == mapping)
            {
               mapping = new RtOidMapping(processId, partId,
                     findAvailableRtOid(rtOidRegistry));
               rtOidRegistry.put(fqIdKey, mapping);
            }
            mapping.registerElement(modelOid,
                  Long.parseLong(processPart.getAttribute(OID_ATTR)));

            if (null != definitionCache)
            {
               String partName = processPart.getAttribute(NAME_ATTR);
               definitionCache.add(new ElementDef(mapping.rtOid, modelOid, partId,
                     partName, processId));
            }
         }
      }
      catch (XPathException e)
      {
         warn("Failed evaluating runtime OIDs for model element: " + elementTag, e);
      }
   }

   private void findActivityPartRtOids(long modelOid, Element modelDom,
         String elementTag, Map rtOidRegistry, List definitionCache)
   {
      try
      {
         XPathExpression activityPartsXPath = XPathFactory.newInstance().newXPath().compile(PROCESS_DEF_TAG + "/"
               + ACTIVITY_DEF_TAG + "/" + elementTag);
         NodeList activityParts = (NodeList) activityPartsXPath.evaluate(modelDom, XPathConstants.NODESET);
         for (int i = 0, nNodes = activityParts.getLength(); i < nNodes; i++)
         {
            Element activityPart = (Element) activityParts.item(i);
            String partId = activityPart.getAttribute(ID_ATTR);

            Element activity = (Element) activityPart.getParentNode();
            String activityId = activity.getAttribute(ID_ATTR);

            Element process = (Element) activity.getParentNode();
            String processId = process.getAttribute(ID_ATTR);

            String fqIdKey = RtOidMapping.getFqIdKey(new String[] {
                  processId, activityId, partId});
            RtOidMapping mapping = (RtOidMapping) rtOidRegistry.get(fqIdKey);
            if (null == mapping)
            {
               mapping = new RtOidMapping(processId, activityId, partId,
                     findAvailableRtOid(rtOidRegistry));
               rtOidRegistry.put(fqIdKey, mapping);
            }
            mapping.registerElement(modelOid,
                  Long.parseLong(activityPart.getAttribute(OID_ATTR)));

            if (null != definitionCache)
            {
               String partName = activityPart.getAttribute(NAME_ATTR);
               definitionCache.add(new ElementDef(mapping.rtOid, modelOid, partId,
                     partName, processId, activityId));
            }
         }
      }
      catch (XPathException e)
      {
         warn("Failed evaluating runtime OIDs for model element: " + elementTag, e);
      }
   }

   private Map getOidRtOidMapping(Map rtOidRegistry)
   {
      Map oidRtOidMapping = new HashMap(rtOidRegistry.size());
      for (Iterator mappingItr = rtOidRegistry.values().iterator(); mappingItr.hasNext();)
      {
         RtOidMapping mapping = (RtOidMapping) mappingItr.next();
         for (Iterator oidItr = mapping.oids.iterator(); oidItr.hasNext();)
         {
            Long oid = (Long) oidItr.next();
            oidRtOidMapping.put(oid, mapping);
         }
      }
      return oidRtOidMapping;
   }

   private int verifyModelElementTable(String tableName, Map fqIdRegistries,
         Map oidRtOidMappings, FqRetriever fqRetriever) throws SQLException
   {
      final Map oidRtOidMapping = (Map) oidRtOidMappings.get(tableName);
      final Map fqIdRegistry = (Map) fqIdRegistries.get(tableName);

      int nInconsistencies = 0;

      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = item.getConnection().createStatement();
         rs = stmt.executeQuery("SELECT " + FIELD_OID + ", " + FIELD_ID
               + " FROM " + tableName);

         Map orphanMapping = new HashMap();
         while (rs.next())
         {
            long oid = rs.getLong(1);
            String id = rs.getString(2);

            RtOidMapping mapping = (RtOidMapping) oidRtOidMapping.get(new Long(oid));
            if (null == mapping)
            {
               // handling elements orphaned during model overwrite operations

               String[] fqId = (null != fqRetriever)
                     ? fqRetriever.getFq(oid)
                     : new String[] {id};

               mapping = (RtOidMapping) fqIdRegistry.get(RtOidMapping.getFqIdKey(fqId));
               if (null != mapping)
               {
                  // orphan might have successor element in same model version
                  boolean superceededInModel = false;
                  for (Iterator i = mapping.oids.iterator(); i.hasNext();)
                  {
                     long otherOid = ((Long) i.next()).longValue();
                     if ((oid >> 32) == (otherOid >> 32))
                     {
                        superceededInModel = true;
                        ++nInconsistencies;
                        warn("Inconsistency: The orphaned " + tableName + " record with "
                              + "OID=" + oid + " was probably superceeded by another "
                              + "record with OID=" + otherOid + " (ID is '" + id + "').",
                              null);
                     }
                  }

                  if (!superceededInModel)
                  {
                     // no successor in the element's model version
                     orphanMapping.put(new Long(oid), fqId);
                  }
               }
               else
               {
                  // no sucessor in any model version
                  orphanMapping.put(new Long(oid), fqId);
               }
            }
            else
            {
               if (!CompareHelper.areEqual(id, mapping.getElementId()))
               {
                  ++nInconsistencies;
                  if (mapping.getElementId().startsWith(id))
                  {
                     warn("Inconsistency: The " + tableName + " record with OID=" + oid
                           + " was stored with a truncated ID ('" + id
                           + "'). The full ID stored in the model file is '"
                           + mapping.getElementId() + "'.", null);
                  }
                  else
                  {
                     warn("Inconsistency: The " + tableName + " record with OID=" + oid
                           + " was stored with an invalid ID ('" + id
                           + "'). The associated model element seems to be have "
                           + "the ID '" + mapping.getElementId() + "'.", null);
                  }
               }
            }
         }

         // process orphan mappings
         for (Iterator orphanItr = orphanMapping.entrySet().iterator(); orphanItr.hasNext();)
         {
            Map.Entry entry = (Map.Entry) orphanItr.next();
            long oid = ((Long) entry.getKey()).longValue();
            String[] fqId = (String[]) entry.getValue();
            String fqIdKey = RtOidMapping.getFqIdKey(fqId);
            for (Iterator i = orphanMapping.entrySet().iterator(); i.hasNext();)
            {
               Map.Entry otherEntry = (Map.Entry) i.next();
               String[] otherFqId = (String[]) otherEntry.getValue();
               String otherFqIdKey = RtOidMapping.getFqIdKey(otherFqId);
               if (CompareHelper.areEqual(fqIdKey, otherFqIdKey))
               {
                  long otherOid = ((Long) otherEntry.getKey()).longValue();
                  if ((oid >> 32) == (otherOid >> 32) && (oid != otherOid))
                  {
                     ++nInconsistencies;
                     warn("Inconsistency: The orphaned " + tableName + " record with "
                           + "OID=" + oid + " collides with another orphaned record "
                           + "with OID=" + otherOid + " (common ID is '"
                           + fqId[fqId.length - 1] + "').", null);
                  }
               }
            }

            RtOidMapping mapping = (RtOidMapping) fqIdRegistry.get(fqIdKey);
            if (null == mapping)
            {
               mapping = new RtOidMapping(fqId, findAvailableRtOid(fqIdRegistry));
               fqIdRegistry.put(fqIdKey, mapping);
            }
            mapping.registerElement((oid >> 32), (int) (oid % 0x100000000l));
            oidRtOidMapping.put(new Long(oid), mapping);
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }

      return nInconsistencies;
   }

   private void populateMappingTable(String type, Map oidRtOidMappings) throws SQLException
   {
      final Map oidRtOidMapping = (Map) oidRtOidMappings.get(type);

      PreparedStatement delStmt = null;
      PreparedStatement stmt = null;
      try
      {
         delStmt = item.getConnection().prepareStatement(
               "DELETE FROM " + TABLE_NAME_TMP_OID_MAPPING
               + " WHERE " + FIELD_TMP_OIDS_TYPE + " = ?");
         delStmt.setString(1, type);
         delStmt.executeUpdate();

         stmt = item.getConnection().prepareStatement(
               "INSERT INTO " + TABLE_NAME_TMP_OID_MAPPING + "("
               + FIELD_TMP_OIDS_OLD_OID + ", "
               + FIELD_TMP_OIDS_TYPE + ", "
               + FIELD_TMP_OIDS_MODEL + ", "
               + FIELD_TMP_OIDS_RTOID + ")"
               + " VALUES (?, ?, ?, ?)");

         for (Iterator i = oidRtOidMapping.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            long oid = ((Long) entry.getKey()).longValue();
            RtOidMapping mapping = (RtOidMapping) entry.getValue();

            stmt.setLong(1, oid);
            stmt.setString(2, type);
            stmt.setLong(3, ( -1 != oid) ? (oid >> 32) : 0);
            stmt.setLong(4, mapping.rtOid);
            stmt.executeUpdate();
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
   }

   private int migrateGlobalElementTable(String tableName) throws SQLException
   {
      int nErrors = 0;

      PreparedStatement stmtMtm = null;
      try
      {
         long nExistingRecords = countRecords(tableName);

         stmtMtm = item.getConnection().prepareStatement("UPDATE " + tableName
               + " SET oid = (SELECT rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING
                     + " WHERE oldOid = oid"
                     + "   AND type = ?)"
               + " WHERE EXISTS (SELECT rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING
                     + " WHERE oldOid = oid"
                     + "   AND type = ?)");
         stmtMtm.setString(1, tableName);
         stmtMtm.setString(2, tableName);
         int nUpdatedRecords = stmtMtm.executeUpdate();
         if (nUpdatedRecords != nExistingRecords)
         {
            warn("Inconsistency: Model table migration only updated " + nUpdatedRecords
                  + " of total " + nExistingRecords + " records of table " + tableName
                  + ".", null);
            ++nErrors;
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmtMtm);
      }
      return nErrors;
   }

   private int migrateActivityElementTable() throws SQLException
   {
      int nErrors = 0;

      PreparedStatement stmtMtm = null;
      PreparedStatement stmtProcUpdate = null;
      try
      {
         long nExistingRecords = countRecords(TABLE_NAME_ACT_DEF);

         stmtMtm = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_ACT_DEF
               + " SET model = (SELECT m.model"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?),"
               + " oid = (SELECT m.rtOid"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?)"
               + " WHERE EXISTS (SELECT m.rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?)");
         stmtMtm.setString(1, TABLE_NAME_ACT_DEF);
         stmtMtm.setString(2, TABLE_NAME_ACT_DEF);
         stmtMtm.setString(3, TABLE_NAME_ACT_DEF);
         int nUpdatedRecords = stmtMtm.executeUpdate();
         if (nUpdatedRecords != nExistingRecords)
         {
            warn("Inconsistency: Model table migration only updated " + nUpdatedRecords
                  + " of total " + nExistingRecords + " records of table "
                  + TABLE_NAME_ACT_DEF + ".", null);
            ++nErrors;
         }

         stmtProcUpdate = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_ACT_DEF
               + "   SET processDefinition = (SELECT m.rtOid"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = process_definition"
                     + "   AND m.type = ?)"
               + " WHERE EXISTS (SELECT m.rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = process_definition"
                     + "   AND m.type = ?)");
         stmtProcUpdate.setString(1, TABLE_NAME_PROC_DEF);
         stmtProcUpdate.setString(2, TABLE_NAME_PROC_DEF);
         nUpdatedRecords = stmtProcUpdate.executeUpdate();
         if (nUpdatedRecords != nExistingRecords)
         {
            warn("Inconsistency: Migrating the process definition field on table "
                  + TABLE_NAME_ACT_DEF + " only updated " + nUpdatedRecords
                  + " of total " + nExistingRecords + " records.", null);
            ++nErrors;
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmtMtm);
      }
      return nErrors;
   }

   private int migrateTransitionElementTable() throws SQLException
   {
      int nErrors = 0;

      PreparedStatement stmtMtm = null;
      PreparedStatement stmtSrcActUpdate = null;
      PreparedStatement stmtTgtActUpdate = null;
      PreparedStatement stmtProcessUpdate = null;
      try
      {
         long nExistingRecords = countRecords(TABLE_NAME_TRANS_DEF);

         stmtMtm = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_TRANS_DEF
               + " SET model = (SELECT m.model"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?),"
               + " oid = (SELECT m.rtOid"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?)"
               + " WHERE EXISTS (SELECT m.rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = oid"
                     + "   AND m.type = ?)");
         stmtMtm.setString(1, TABLE_NAME_TRANS_DEF);
         stmtMtm.setString(2, TABLE_NAME_TRANS_DEF);
         stmtMtm.setString(3, TABLE_NAME_TRANS_DEF);
         int nUpdatedRecords = stmtMtm.executeUpdate();
         if (nUpdatedRecords != nExistingRecords)
         {
            warn("Inconsistency: Model table migration only updated " + nUpdatedRecords
                  + " of total " + nExistingRecords + " records of table "
                  + TABLE_NAME_ACT_DEF + ".", null);
            ++nErrors;
         }

         long nExistingSrcRecords = countRecords(TABLE_NAME_TRANS_DEF, "source_activity <> 0");
         stmtSrcActUpdate = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_TRANS_DEF
               + "   SET sourceActivity = (SELECT m.rtOid"
                     + " FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = source_activity"
                     + "   AND m.type = ?)"
               + " WHERE EXISTS (SELECT m.rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = source_activity"
                     + "   AND m.type = ?)");
         stmtSrcActUpdate.setString(1, TABLE_NAME_ACT_DEF);
         stmtSrcActUpdate.setString(2, TABLE_NAME_ACT_DEF);
         nUpdatedRecords = stmtSrcActUpdate.executeUpdate();
         if (nUpdatedRecords != nExistingSrcRecords)
         {
            warn("Inconsistency: Setting the source activity field on table "
                  + TABLE_NAME_TRANS_DEF + " only updated " + nUpdatedRecords
                  + " of total " + nExistingSrcRecords + " records.", null);
            ++nErrors;
         }

         long nExistingTgtRecords = countRecords(TABLE_NAME_TRANS_DEF, "target_activity <> 0");
         stmtTgtActUpdate = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_TRANS_DEF
               + "   SET targetActivity = (SELECT m.rtOid"
                     + " FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = target_activity"
                     + "   AND m.type = ?)"
               + " WHERE EXISTS (SELECT m.rtOid FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                     + " WHERE m.oldOid = target_activity"
                     + "   AND m.type = ?)");
         stmtTgtActUpdate.setString(1, TABLE_NAME_ACT_DEF);
         stmtTgtActUpdate.setString(2, TABLE_NAME_ACT_DEF);
         nUpdatedRecords = stmtTgtActUpdate.executeUpdate();
         if (nUpdatedRecords != nExistingTgtRecords)
         {
            warn("Inconsistency: Setting the target activity field on table "
                  + TABLE_NAME_TRANS_DEF + " only updated " + nUpdatedRecords
                  + " of total " + nExistingSrcRecords + " records.", null);
            ++nErrors;
         }

         stmtProcessUpdate = item.getConnection().prepareStatement("UPDATE " + TABLE_NAME_TRANS_DEF
               + "   SET processDefinition = (SELECT p.oid"
                     + " FROM " + TABLE_NAME_PROC_DEF + " p"
                     + " WHERE ("
                           + "p.oid IN (SELECT a.processDefinition"
                                 + "  FROM " + TABLE_NAME_ACT_DEF + " a"
                                 + " WHERE a.oid = sourceActivity"
                                 + "   AND a.model = " + TABLE_NAME_TRANS_DEF + ".model)"
                           + " OR "
                           + "p.oid IN (SELECT a.processDefinition"
                                 + "  FROM " + TABLE_NAME_ACT_DEF + " a"
                                 + " WHERE a.oid = targetActivity"
                                 + " AND a.model = " + TABLE_NAME_TRANS_DEF + ".model)"
                           + ") AND p.model = " + TABLE_NAME_TRANS_DEF + ".model"
                     + ")"
               + " WHERE EXISTS (SELECT p.oid FROM " + TABLE_NAME_PROC_DEF + " p"
                     + " WHERE ("
                           + "p.oid IN (SELECT a.processDefinition"
                                 + "  FROM " + TABLE_NAME_ACT_DEF + " a"
                                 + " WHERE a.oid = sourceActivity"
                                 + " AND a.model = " + TABLE_NAME_TRANS_DEF + ".model)"
                           + " OR "
                           + "p.oid IN (SELECT a.processDefinition"
                                 + "  FROM " + TABLE_NAME_ACT_DEF + " a"
                                 + " WHERE a.oid = targetActivity"
                                 + " AND a.model = " + TABLE_NAME_TRANS_DEF + ".model)"
                           + ") AND p.model = " + TABLE_NAME_TRANS_DEF + ".model"
                     + ")");
         nUpdatedRecords = stmtProcessUpdate.executeUpdate();
         if (nUpdatedRecords != nExistingRecords)
         {
            warn("Inconsistency: Setting the process definition field on table "
                  + TABLE_NAME_TRANS_DEF + " only updated " + nUpdatedRecords
                  + " of total " + nExistingRecords + " records.", null);
            ++nErrors;
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmtMtm);
      }
      return nErrors;
   }

   private int populateTriggerTable(List triggerDefs, Map procFqIdRegistry)
         throws SQLException
   {
      int nErrors = 0;

      PreparedStatement stmt = null;
      try
      {
         stmt = item.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME_PROC_TRIGGER
               + "(oid, model, id, name, processDefinition)"
               + " VALUES (?, ?, ?, ?, ?)");
         for (Iterator i = triggerDefs.iterator(); i.hasNext();)
         {
            ElementDef triggerDef = (ElementDef) i.next();

            RtOidMapping procMapping = (RtOidMapping) procFqIdRegistry.get(
                  RtOidMapping.getFqIdKey(new String[] {
                        triggerDef.processId}));

            String triggerId = StringUtils.cutString(triggerDef.id, 50);
            if (!CompareHelper.areEqual(triggerId, triggerDef.id))
            {
               ++nErrors;
               warn("The ID of trigger '" + triggerDef.id + "' must be truncated to be "
                     + "stored in the audit trail.", null);
            }

            if ((null != procMapping)
                  && CompareHelper.areEqual(procMapping.getElementId(),
                        triggerDef.processId))
            {
               stmt.setLong(1, triggerDef.rtOid);
               stmt.setLong(2, triggerDef.modelOid);
               stmt.setString(3, triggerId);
               stmt.setString(4, triggerDef.name);
               stmt.setLong(5, procMapping.rtOid);
               stmt.executeUpdate();
            }
            else
            {
               ++nErrors;
               warn("Inconsistency: Failed writing record for trigger with ID "
                     + triggerId + " as the associated process (ID is "
                     + triggerDef.processId + ") is unknown.", null);
            }
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }

      return nErrors;
   }

   private int populateEventHandlerTable(List handlerDefs, Map procFqIdRegistry,
         Map actFqIdRegistry) throws SQLException
   {
      int nErrors = 0;

      PreparedStatement stmt = null;
      try
      {
         stmt = item.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME_EVT_HANDLER
               + "(oid, model, id, name, processDefinition, activity)"
               + "VALUES (?, ?, ?, ?, ?, ?)");
         for (Iterator i = handlerDefs.iterator(); i.hasNext();)
         {
            ElementDef handlerDef = (ElementDef) i.next();

            RtOidMapping actMapping;
            if (!StringUtils.isEmpty(handlerDef.activityId))
            {
               actMapping = (RtOidMapping) actFqIdRegistry.get(
                     RtOidMapping.getFqIdKey(new String[] {
                           handlerDef.processId, handlerDef.activityId}));
               if ((null == actMapping)
                     || !CompareHelper.areEqual(actMapping.getElementId(),
                           handlerDef.activityId))
               {
                  ++nErrors;
                  warn("Inconsistency: Failed writing record for event handler with ID "
                        + handlerDef.id + " as the associated activity (ID is "
                        + handlerDef.activityId + ") is unknown.", null);
               }
            }
            else
            {
               actMapping = null;
            }

            RtOidMapping procMapping = (RtOidMapping) procFqIdRegistry.get(
                  RtOidMapping.getFqIdKey(new String[] {
                        handlerDef.processId}));

            String handlerId = StringUtils.cutString(handlerDef.id, 50);
            if (!CompareHelper.areEqual(handlerId, handlerDef.id))
            {
               ++nErrors;
               warn("The ID of event handler '" + handlerDef.id + "' must be truncated "
                     + "to be stored in the audit trail.", null);
            }

            if ((null != procMapping)
                  && CompareHelper.areEqual(procMapping.getElementId(),
                        handlerDef.processId))
            {
               stmt.setLong(1, handlerDef.rtOid);
               stmt.setLong(2, handlerDef.modelOid);
               stmt.setString(3, handlerId);
               stmt.setString(4, handlerDef.name);
               stmt.setLong(5, procMapping.rtOid);
               stmt.setLong(6, (null != actMapping) ? actMapping.rtOid : 0);
               stmt.executeUpdate();
            }
            else
            {
               ++nErrors;
               warn("Inconsistency: Failed writing record for event handler with ID "
                     + handlerDef.id + " as the associated process (ID is "
                     + handlerDef.processId + ") is unknown.", null);
            }
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }

      return nErrors;
   }

   private long countRecords(String tableName) throws SQLException
   {
      return countRecords(tableName, null);
   }

   private long countRecords(String tableName, String predicate) throws SQLException
   {
      long count;

      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = item.getConnection().prepareStatement("SELECT COUNT(*)"
               + " FROM " + tableName
               + (StringUtils.isEmpty(predicate)
                     ? ""
                     : (" WHERE " + predicate)));
         rs = stmt.executeQuery();
         if (rs.next())
         {
            count = rs.getLong(1);
         }
         else
         {
            warn("Failed counting records for table " + tableName + ".", null);
            count = 0;
         }
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(stmt, rs);
      }
      return count;
   }

   private int migrateActInstTable() throws SQLException
   {
      return migrateRuntimeItemTable(TABLE_NAME_ACT_INST, FIELD_ACT_DEF,
            TABLE_NAME_ACT_DEF, new RtTableMigrationExtension()
            {
               private int nErrors = 0;

               private long nPendingWorklistAis;
               private long nUpdatedWorklistAis;

               public void beforeMigration(RuntimeItem item) throws SQLException
               {
                  this.nPendingWorklistAis = countRecords(TABLE_NAME_ACT_INST,
                        "currentPerformer > 0 AND model IS NULL");
               }

               public void doMigration(RuntimeItem item, RtOidMappingTuple mapping)
                     throws SQLException
               {
                  final long nPendingSliceWorklistAis = countRecords(TABLE_NAME_ACT_INST,
                        FIELD_ACT_DEF + " = " + mapping.getOldOid()
                        + " AND currentPerformer > 0"
                        + " AND model IS NULL");

                  PreparedStatement stmt = null;
                  try
                  {
                     stmt = item.getConnection().prepareStatement(
                           "UPDATE " + TABLE_NAME_ACT_INST
                           + " SET currentPerformer = (SELECT m.rtOid"
                                 + "  FROM " + TABLE_NAME_TMP_OID_MAPPING + " m"
                                 + " WHERE m.oldOid = currentPerformer"
                                 + "   AND m.type = ?"
                           + ")"
                           + " WHERE " + FIELD_ACT_DEF + " = ?"
                           + "   AND currentPerformer > 0"
                           + "   AND model IS NULL");
                     stmt.setString(1, TABLE_NAME_PARTICIPANT_DEF);
                     stmt.setLong(2, mapping.getOldOid());
                     int nUpdatedSliceWorklistAis = stmt.executeUpdate();

                     if (nUpdatedSliceWorklistAis < nPendingSliceWorklistAis)
                     {
                        ++nErrors;
                        throw new UpgradeException("Could only migrate worklist "
                              + "association of " + nUpdatedSliceWorklistAis + " of "
                              + nPendingSliceWorklistAis + " pending records of table "
                              + TABLE_NAME_ACT_INST
                              + " for instances of activity with oid "
                              + mapping.getOldOid() + ".");
                     }

                     this.nUpdatedWorklistAis += nUpdatedSliceWorklistAis;
                  }
                  finally
                  {
                     QueryUtils.closeStatement(stmt);
                  }
               }

               public int afterMigration(RuntimeItem item) throws SQLException
               {
                  if (nUpdatedWorklistAis < nPendingWorklistAis)
                  {
                     ++nErrors;
                     warn("Could only migrate worklist association of " + nUpdatedWorklistAis
                           + " of " + nPendingWorklistAis + " pending records of table "
                           + TABLE_NAME_ACT_INST + ".", null);
                  }
                  return nErrors;
               }
            });
   }

   private int migrateRuntimeItemTable(String tableName, String fkFieldName,
         String fkType) throws SQLException
   {
      return migrateRuntimeItemTable(tableName, fkFieldName, fkType, null);
   }

   private int migrateRuntimeItemTable(String tableName, String fkFieldName,
         String fkType, RtTableMigrationExtension extension) throws SQLException
   {
      int nErrors = 0;

      final long nPendingRecords = countRecords(tableName, "model IS NULL");

      if (0 != nPendingRecords)
      {
         System.out.print("Migrating table " + tableName + " ");
         try
         {
            List rtOidMappings = new ArrayList();

            boolean translateSpecialMinusOneOid = TABLE_NAME_TRANS_DEF.equals(fkType);

            PreparedStatement stmtMappingReader = null;
            ResultSet rsMappings = null;
            try
            {
               stmtMappingReader = item.getConnection().prepareStatement(
                     "SELECT oldOid, model, rtOid"
                     + "  FROM " + TABLE_NAME_TMP_OID_MAPPING
                     + " WHERE type = ?"
                     + " ORDER BY oldOid");
               stmtMappingReader.setString(1, fkType);
               rsMappings = stmtMappingReader.executeQuery();
               while (rsMappings.next())
               {
                  RtOidMappingTuple mapping = new RtOidMappingTuple(
                        rsMappings.getLong(1), rsMappings.getLong(2),
                        rsMappings.getLong(3));
                  if (translateSpecialMinusOneOid && (-1 == mapping.getOldOid()))
                  {
                     translateSpecialMinusOneOid = false;
                  }
                  rtOidMappings.add(mapping);
               }
               if (translateSpecialMinusOneOid)
               {
                  rtOidMappings.add(new RtOidMappingTuple(-1, 0, -1));
               }
            }
            finally
            {
               QueryUtils.closeStatementAndResultSet(stmtMappingReader, rsMappings);
            }

            PreparedStatement stmt = null;
            try
            {
               long nUpdatedRecords = 0;
               if (null != extension)
               {
                  extension.beforeMigration(item);
               }

               stmt = item.getConnection().prepareStatement(
                     "UPDATE " + tableName
                     + " SET   model = ?, " + fkFieldName + " = ?"
                     + " WHERE " + fkFieldName + " = ?"
                     + "   AND model IS NULL");

               for (Iterator i = rtOidMappings.iterator(); i.hasNext();)
               {
                  RtOidMappingTuple mapping = (RtOidMappingTuple) i.next();

                  if (null != extension)
                  {
                     extension.doMigration(item, mapping);
                  }

                  stmt.setLong(1, mapping.getModel());
                  stmt.setLong(2, mapping.getRtOid());
                  stmt.setLong(3, mapping.getOldOid());

                  nUpdatedRecords += stmt.executeUpdate();

                  item.commit();
                  System.out.print(".");
               }

               if (null != extension)
               {
                  nErrors += extension.afterMigration(item);
               }
               if (nUpdatedRecords < nPendingRecords)
               {
                  ++nErrors;
                  warn("Could only migrate " + nUpdatedRecords + " of " + nPendingRecords
                        + " pending records of table " + tableName + ".", null);
               }
            }
            finally
            {
               QueryUtils.closeStatement(stmt);
            }
         }
         finally
         {
            System.out.println();
         }
      }
      else
      {
         System.out.println("Nothing to be migrated for table " + tableName + ".");
      }
      return nErrors;
   }

   private interface FqRetriever
   {
      String[] getFq(long oid) throws SQLException;
   }

   private class ActivityFqRetriever implements FqRetriever
   {
      public String[] getFq(long oid) throws SQLException
      {
         String[] fqId = new String[2];

         Statement stmt = null;
         ResultSet rs = null;
         try
         {
            stmt = item.getConnection().createStatement();
            rs = stmt.executeQuery("SELECT a.id, p.id"
                  + "  FROM activity a, process_definition p"
                  + " WHERE a.process_definition = p.oid"
                  + "   AND a.oid = " + oid);
            if (rs.next())
            {
               fqId[0] = rs.getString(2);
               fqId[1] = rs.getString(1);
            }
            else
            {
               error("Unable to find process definition for activity with OID " + oid
                     + ".", null);
            }
         }
         finally
         {
            QueryUtils.closeStatementAndResultSet(stmt, rs);
         }

         return fqId;
      }
   }

   private class TransitionFqRetriever implements FqRetriever
   {
      public String[] getFq(long oid) throws SQLException
      {
         String[] fqId = new String[2];

         Statement stmt = null;
         ResultSet rs = null;
         try
         {
            stmt = item.getConnection().createStatement();
            rs = stmt.executeQuery("SELECT t.id, p.id"
                  + "  FROM transition t, activity a, process_definition p"
                  + " WHERE (t.source_activity = a.oid OR t.target_activity = a.oid)"
                  + "   AND a.process_definition = p.oid"
                  + "   AND t.oid = " + oid);
            if (rs.next())
            {
               fqId[0] = rs.getString(2);
               fqId[1] = rs.getString(1);
            }
            else
            {
               error("Unable to find process definition for activity with OID " + oid
                     + ".", null);
            }
         }
         finally
         {
            QueryUtils.closeStatementAndResultSet(stmt, rs);
         }

         return fqId;
      }
   }

   private static class RtOidMapping
   {
      final String[] fqId;
      final long rtOid;

      final Set oids = new TreeSet();

      RtOidMapping(String[] fqId, long rtOid)
      {
         this.fqId = fqId;
         this.rtOid = rtOid;
      }

      RtOidMapping(String id, long rtOid)
      {
         this(new String[] {id}, rtOid);
      }

      RtOidMapping(String processId, String id, long rtOid)
      {
         this(new String[] {processId, id}, rtOid);
      }

      RtOidMapping(String processId, String activityId, String id, long rtOid)
      {
         this(new String[] {processId, activityId, id}, rtOid);
      }

      public void registerElement(long modelOid, long elementOid)
      {
         Assert.condition(modelOid <= Integer.MAX_VALUE, "Model OID exceeds 32 bits.");

         if (modelOid == (elementOid >> 32))
         {
            System.out.println("Not padding elemnt OID " + elementOid);

            oids.add(new Long(elementOid));
         }
         else
         {
            oids.add(new Long((modelOid << 32) + elementOid));
         }
      }

      public String getElementId()
      {
         return fqId[fqId.length - 1];
      }

      public String getFqIdKey()
      {
         return getFqIdKey(fqId);
      }

      public static String getFqIdKey(String[] fqId)
      {
         StringBuffer buffer = new StringBuffer(100);
         for (int i = 0; i < fqId.length; i++ )
         {
            buffer.append("::[").append(fqId[i]).append("]");
         }
         return buffer.toString();
      }
   }

   private static class ElementDef
   {
      final long rtOid;
      final long modelOid;
      final String id;
      final String name;
      final String processId;
      final String activityId;

      ElementDef(long rtOid, long modelOid, String id, String name, String processId)
      {
         this(rtOid, modelOid, id, name, processId, null);
      }

      ElementDef(long rtOid, long modelOid, String id, String name, String processId, String activityId)
      {
         this.rtOid = rtOid;
         this.modelOid = modelOid;
         // TODO prevent ID truncation
         this.id = id;
         this.name = name;
         this.processId = processId;
         this.activityId = activityId;
      }
   }

   private static class RtOidMappingTuple
   {
      private final long oldOid;
      private final long model;
      private final long rtOid;

      RtOidMappingTuple(long oldOid, long model, long rtOid)
      {
         this.oldOid = oldOid;
         this.model = model;
         this.rtOid = rtOid;
      }

      public long getOldOid()
      {
         return oldOid;
      }

      public long getModel()
      {
         return model;
      }

      public long getRtOid()
      {
         return rtOid;
      }
   }

   private interface RtTableMigrationExtension
   {
      void beforeMigration(RuntimeItem item) throws SQLException;

      void doMigration(RuntimeItem item, RtOidMappingTuple mapping) throws SQLException;

      int afterMigration(RuntimeItem item) throws SQLException;
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

   @Override
   protected Logger getLogger()
   {
      return trace;
   }
}
