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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.AlterTableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R3_0_1from3_0_0RuntimeJob extends OracleDB2AwareRuntimeUpgradeJob
{
   private static final Version VERSION = Version.createFixedVersion(3, 0, 1);

   private static final String FIELD_OID = "oid";
   private static final String FIELD_ID = "id";

   private static final String TABLE_NAME_EVENT_BINDING = "event_binding";
   private static final String FIELD_EVENT_BINDING__OID = "oid";
   private static final String FIELD_EVENT_BINDING__OBJECT_OID = "objectOID";
   private static final String FIELD_EVENT_BINDING__TYPE = "type";
   private static final String FIELD_EVENT_BINDING__HANDLER = "handler";
   private static final String FIELD_EVENT_BINDING__HANDLER_OID = "handlerOID";
   private static final String FIELD_EVENT_BINDING__TARGET_STAMP = "targetStamp";

   private static final String TABLE_NAME_PI = "process_instance";
   private static final String FIELD_PI__PROCESS = "processDefinition";

   private static final String TABLE_NAME_AI = "activity_instance";
   private static final String FIELD_AI__ACTIVITY = "activity";

   private static final String PROCESS_DEFINITION_TAG = "processDefinition";
   private static final String ACTIVITY_TAG = "activity";
   private static final String EVENT_HANDLER_TAG = "eventHandler";

   private static final int EVENT_BINDING_TARGET_AI = 1;
   private static final int EVENT_BINDING_TARGET_PI = 2;

   public Version getVersion()
   {
      return VERSION;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      // renaming indexes to be aligned along a common naming scheme for property tables

      final FieldInfo OBJECT_OID = new FieldInfo("objectOID", Long.TYPE);
      final FieldInfo TYPE_KEY = new FieldInfo("type_key", Integer.TYPE);
      final FieldInfo NUMBER_VALUE = new FieldInfo("number_value", Long.TYPE);
      final FieldInfo STRING_VALUE = new FieldInfo("string_value", String.class,
            128);
      final FieldInfo OID = new FieldInfo("oid", Long.TYPE);

      DatabaseHelper.alterTable(item, new AlterTableInfo("ACT_INST_PROPERTY")
      {
         public IndexInfo[] getDroppedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("ACT_INST_PROP_IDX", NO_FIELDS),
                  new IndexInfo("ACT_INST_PROP_IDX2", NO_FIELDS),
                  new IndexInfo("ACT_INST_PROP_IDX3", NO_FIELDS),
                  new IndexInfo("ACT_INST_PROP_IDX4", NO_FIELDS)};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("ACT_INST_PRP_IDX1", new FieldInfo[] {OBJECT_OID}),
                  new IndexInfo("ACT_INST_PRP_IDX2", new FieldInfo[] {
                        TYPE_KEY, NUMBER_VALUE}),
                  new IndexInfo("ACT_INST_PRP_IDX3", new FieldInfo[] {
                        TYPE_KEY, STRING_VALUE}),
                  new IndexInfo("ACT_INST_PRP_IDX4", true, new FieldInfo[] {OID})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("PROC_INST_PROPERTY")
      {
         public IndexInfo[] getDroppedIndexes()
         {
            return new IndexInfo[] {new IndexInfo("PROC_INST_PRP_IDX", NO_FIELDS)};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {new IndexInfo("PROC_INST_PRP_IDX1",
                  new FieldInfo[] {OBJECT_OID})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("workflowuser")
            {
               public IndexInfo[] getAddedIndxes()
               {
                  return new IndexInfo[] {new IndexInfo("WORKFLOWUSER_IDX2", true,
                        new FieldInfo[] {new FieldInfo("account", Long.TYPE)})};
               }
            }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("USER_PROPERTY")
      {
         public IndexInfo[] getDroppedIndexes()
         {
            return new IndexInfo[] {new IndexInfo("USER_PROP_IDX", NO_FIELDS),
                  new IndexInfo("USER_PROP_IDX2", NO_FIELDS),
                  new IndexInfo("USER_PROP_IDX3", NO_FIELDS),
                  new IndexInfo("USER_PROP_IDX4", NO_FIELDS)};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("USER_PRP_IDX1", new FieldInfo[] {OBJECT_OID}),
                  new IndexInfo("USER_PRP_IDX2", new FieldInfo[] {
                        TYPE_KEY, NUMBER_VALUE}),
                  new IndexInfo("USER_PROP_IDX3", new FieldInfo[] {
                        TYPE_KEY, STRING_VALUE}),
                  new IndexInfo("USER_PRP_IDX4", true, new FieldInfo[] {OID})};
         }
      }, this);

      // modifying event binding to OID based references, increasing alignment with other
      // tables
      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_EVENT_BINDING)
      {
         private final FieldInfo OID = new FieldInfo(FIELD_EVENT_BINDING__OID, Long.TYPE);

         private final FieldInfo OBJECT_OID = new FieldInfo(
               FIELD_EVENT_BINDING__OBJECT_OID, Long.TYPE);

         private final FieldInfo TYPE = new FieldInfo(FIELD_EVENT_BINDING__TYPE,
               Integer.TYPE);

         private final FieldInfo HANDLER_OID = new FieldInfo(
               FIELD_EVENT_BINDING__HANDLER_OID, Long.TYPE);

         private final FieldInfo TARGET_STAMP = new FieldInfo(
               FIELD_EVENT_BINDING__TARGET_STAMP, Long.TYPE);

         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {HANDLER_OID};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("event_binding_idx1", true, new FieldInfo[] {OID}),
                  new IndexInfo("event_binding_idx2", new FieldInfo[] {
                        OBJECT_OID, TYPE, HANDLER_OID}),
                  new IndexInfo("event_binding_idx3", new FieldInfo[] {TARGET_STAMP})};
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      // migrate bindings based on handlerID to handlerOID

      for (Iterator i = getPost30ModelOIDs(); i.hasNext();)
      {
         Long modelOID = (Long) i.next();
         ModelItem modelItem = retrieveModelFromAuditTrail(modelOID.longValue());
         Element modelDOM = modelItem.getModelElement();

         try
         {
            XPathExpression processNotificationsXPath = XPathFactory.newInstance().newXPath().compile(PROCESS_DEFINITION_TAG + "/" + EVENT_HANDLER_TAG);
            NodeList processNotifications = (NodeList) processNotificationsXPath.evaluate(modelDOM, XPathConstants.NODESET);
            for (int j = 0, nNodes = processNotifications.getLength(); j < nNodes; j++)
            {
               Element eventHandler = (Element) processNotifications.item(j);
               Element context = (Element) eventHandler.getParentNode();

               migrateEventHandlerBindings(eventHandler, modelOID.longValue(),
                     EVENT_BINDING_TARGET_PI, context, TABLE_NAME_PI, FIELD_PI__PROCESS);

               item.commit();
            }

            XPathExpression activityNotificationsXPath = XPathFactory.newInstance().newXPath().compile(PROCESS_DEFINITION_TAG + "/" + ACTIVITY_TAG + "/" + EVENT_HANDLER_TAG);
            NodeList activityNotifications = (NodeList) activityNotificationsXPath.evaluate(modelDOM, XPathConstants.NODESET);
            for (int j = 0, nNodes = activityNotifications.getLength(); j < nNodes; j++)
            {
               Element eventHandler = (Element) activityNotifications.item(j);
               Element context = (Element) eventHandler.getParentNode();

               migrateEventHandlerBindings(eventHandler, modelOID.longValue(),
                     EVENT_BINDING_TARGET_AI, context, TABLE_NAME_AI, FIELD_AI__ACTIVITY);

               item.commit();
            }
         }
         catch (XPathException e)
         {
            warn("Failed extracting notifications for migration", e);
         }
         catch (SQLException e)
         {
            String message = "Failed migrating event bindings";
            fatal(message, e);
            rollback();

            throw new UpgradeException(message);
         }
      }

      // restore foreign key semantics for model table dependent objects

      try
      {
         final long shift = (1l << 32);

         String pdSql = "UPDATE process_definition o SET o.model = o.model/" + shift
               + " WHERE o.model >= (" + shift + ")"
               + "   AND EXISTS (SELECT * FROM model m WHERE m.oid=(o.model/" + shift + "))";

         String dataSql = "UPDATE data o SET o.model = o.model/" + shift
               + " WHERE o.model >= (" + shift + ")"
               + "   AND EXISTS (SELECT * FROM model m WHERE m.oid=(o.model/" + shift + "))";

         String participantSql = "UPDATE participant o SET o.model = o.model/" + shift
            + " WHERE o.model >= (" + shift + ")"
            + "   AND EXISTS (SELECT * FROM model m WHERE m.oid=(o.model/" + shift + "))";

         Statement stmt = null;
         try
         {
            stmt = item.getConnection().createStatement();
            stmt.execute(pdSql);
            stmt.execute(dataSql);
            stmt.execute(participantSql);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }

         item.commit();
      }
      catch (SQLException e)
      {
         String message = "Failed migrating event bindings";
         fatal(message, e);
         rollback();

         throw new UpgradeException(message);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.alterTable(item, new AlterTableInfo(TABLE_NAME_EVENT_BINDING)
      {
         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {new FieldInfo(FIELD_EVENT_BINDING__HANDLER,
                  String.class)};
         }
      }, this);
   }

   private void migrateEventHandlerBindings(Element eventHandler, long modelOID,
         int contextKind, Element context, String contextTableName,
         String contextTypeField) throws SQLException
   {
      final long eventHandlerOID = (modelOID << 32)
            + Long.parseLong(eventHandler.getAttribute(FIELD_OID));
      final String eventHandlerID = eventHandler.getAttribute(FIELD_ID);

      final long contextTypeOID = (modelOID << 32)
            + Long.parseLong(context.getAttribute(FIELD_OID));

      Statement stmt = null;
      try
      {
         stmt = item.getConnection().createStatement();
         stmt.executeUpdate("UPDATE " + TABLE_NAME_EVENT_BINDING
               + "   SET " + FIELD_EVENT_BINDING__HANDLER_OID + "=" + eventHandlerOID
               + " WHERE " + FIELD_EVENT_BINDING__TYPE + "=" + contextKind
               + "   AND " + FIELD_EVENT_BINDING__HANDLER + "='" + eventHandlerID + "'"
               + "   AND " + FIELD_EVENT_BINDING__OBJECT_OID + " IN ("
               + "    SELECT " + FIELD_OID + " FROM " + contextTableName
               + "     WHERE " + contextTypeField + "=" + contextTypeOID
               + "    )");
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
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
