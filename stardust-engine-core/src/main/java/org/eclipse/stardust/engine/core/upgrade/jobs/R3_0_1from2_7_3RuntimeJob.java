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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author rsauer
 * @version $Revision$
 */
public class R3_0_1from2_7_3RuntimeJob extends OracleDB2AwareRuntimeUpgradeJob
{
   private static final Version VERSION = new Version(3, 0, 1);

   private static final String V273_WORKFLOW_TAG = "WORKFLOW";
   private static final String V273_ACTIVITY_TAG = "ACTIVITY";
   private static final String V273_NOTIFICATION_TAG = "NOTIFICATION";
   private static final String V273_DESCRIPTION_TAG = "DESCRIPTION";
   private static final String V273_PARTICIPANTS_TAG = "PARTICIPANTS";
   private static final String V273_ORGANISATION_TAG = "ORGANISATION";
   private static final String V273_ROLE_TAG = "ROLE";

   private static final String V273_OID_ATTR = "oid";
   private static final String V273_MODEL_VALID_FROM_ATTR = "valid_from_time_stamp";
   private static final String V273_MODEL_VALID_TO_ATTR = "valid_to_time_stamp";
   private static final String V273_NOTIFICATION_TIMEOUT_ATTR = "timeout_in_sec";
   private static final String V273_NOTIFICATION_YEAR_TIMEOUT_ATTR = "year_timeout";
   private static final String V273_NOTIFICATION_MONTH_TIMEOUT_ATTR = "month_timeout";

   private static final int LOG_ENTRY_ACTIVITY_INSTANCE_TIMEOUT = 1;
   private static final int LOG_ENTRY_PROCESS_INSTANCE_TIMEOUT = 0;

   private static final int EVENT_BINDING_TARGET_ACTIVITY = 1;
   private static final int EVENT_BINDING_TARGET_PROCESS = 2;

   private static final String TABLE_NAME_ACTIVITY_INSTANCE = "activity_instance";
   private static final String FIELD_ACTIVITY_INSTANCE__ACTIVITY = "activity";

   private static final String TABLE_NAME_PROCESS_INSTANCE = "process_instance";
   private static final String FIELD_PROCESS_INSTANCE__PROCESS_DEFINITION = "processDefinition";

   private static final NotificationMigrationProfile MIGRATION_PROFILE_PROCESS_NOTIFICATION = new NotificationMigrationProfile(
         EVENT_BINDING_TARGET_PROCESS, LOG_ENTRY_PROCESS_INSTANCE_TIMEOUT,
         TABLE_NAME_PROCESS_INSTANCE, FIELD_PROCESS_INSTANCE__PROCESS_DEFINITION,
         "NOT IN (1, 2)");

   private static final NotificationMigrationProfile MIGRATION_PROFILE_ACTIVITY_NOTIFICATION = new NotificationMigrationProfile(
         EVENT_BINDING_TARGET_ACTIVITY, LOG_ENTRY_ACTIVITY_INSTANCE_TIMEOUT,
         TABLE_NAME_ACTIVITY_INSTANCE, FIELD_ACTIVITY_INSTANCE__ACTIVITY, "NOT IN (2, 6)");

   private final int batchSize;

   public R3_0_1from2_7_3RuntimeJob()
   {
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      batchSize = (null != bs) ? Integer.parseInt(bs) : 500;
   }

   public Version getVersion()
   {
      return VERSION;
   }

   /**
    * Match only if we have a runtime < 3.0.0
    */
   public boolean matches(Version version)
   {
      return version.compareTo(new Version(3, 0, 0)) < 0;
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      DatabaseHelper.createTable(item, new PropertyTableInfo("ACT_INST", item.isArchiveAuditTrail()), this);

      DatabaseHelper.createTable(item, new EventBindingTableInfo(item.isArchiveAuditTrail()), this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("workflowuser")
      {
         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {new IndexInfo("WORKFLOWUSER_IDX2", true,
                  new FieldInfo[] {new FieldInfo("account", Long.TYPE)})};
         }
      }, this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("USER_PROPERTY")
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {new FieldInfo("objectOID", Long.TYPE)};
         }

         public IndexInfo[] getDroppedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("USER_PROPERTY_IDX2", NO_FIELDS),
                  new IndexInfo("USER_PROPERTY_IDX3", NO_FIELDS),
                  new IndexInfo("USER_PROPERTY_IDX4", NO_FIELDS),
                  new IndexInfo("USER_PROPERTY_IDX5", NO_FIELDS)};
         }

         public IndexInfo[] getAddedIndexes()
         {
            return new IndexInfo[] {
                  new IndexInfo("USER_PROP_IDX1",
                        new FieldInfo[] {PropertyTableInfo.OBJECT_OID}),
                  new IndexInfo("USER_PROP_IDX2", new FieldInfo[] {
                        PropertyTableInfo.TYPE_KEY, PropertyTableInfo.NUMBER_VALUE}),
                  new IndexInfo("USER_PROP_IDX3", new FieldInfo[] {
                        PropertyTableInfo.TYPE_KEY, PropertyTableInfo.STRING_VALUE}),
                  new IndexInfo("USER_PROP_IDX4", true,
                        new FieldInfo[] {PropertyTableInfo.OID})};
         }
      }, this);

      DatabaseHelper.createTable(item, new ParticipantTableInfo(item.isArchiveAuditTrail()), this);

      DatabaseHelper.createTable(item, new UserParticipantTableInfo(item.isArchiveAuditTrail()), this);

      DatabaseHelper.createTable(item, new PropertyTableInfo("PROC_INST", item.isArchiveAuditTrail()), this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("MODEL")
      {
         public FieldInfo[] getAddedFields()
         {
            return new FieldInfo[] {
                  new FieldInfo("deploymentComment", String.class),
                  new FieldInfo("deploymentStamp", Long.TYPE),
                  new FieldInfo("version", String.class),
                  new FieldInfo("revision", Long.TYPE),
                  new FieldInfo("disabled", Long.TYPE)};
         }
      }, this);
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      final Set modelRecords = loadSortedModelRecords();

      try
      {
         for (Iterator i = modelRecords.iterator(); i.hasNext();)
         {
            ModelRecord modelRecord = (ModelRecord) i.next();

            // fetch full XML representation
            ModelItem modelItem = retrieveModelFromAuditTrail(modelRecord.oid << 32);
            Element modelDOM = modelItem.getModelElement();

            // migrate participants
            Map participants = findRuntimeParticipants(modelRecord.oid, modelDOM);
            populateParticipantTable(participants, recover);
            populateParticipantLinkTable(modelRecord.oid, participants, recover);

            if ( !item.isArchiveAuditTrail())
            {
               // migrate notifications
               migrateNotifications(modelRecord, modelDOM);
            }

            item.commit();
         }
      }
      catch (Exception e)
      {
         final String message = "Failed migrating models table.";

         warn(message, e);
         rollback();
         throw new UpgradeException(message);
      }

      // finalize model table
      try
      {
         long maxModelOID = 0;
         ModelRecord predecessor = null;
         for (Iterator i = modelRecords.iterator(); i.hasNext();)
         {
            ModelRecord modelRecord = (ModelRecord) i.next();
            // explicitly maintain successor/predecessor relationship
            if (null != predecessor)
            {
               modelRecord.predecessorOID = predecessor.oid;
            }

            // remember greatest model OID for later sequence tweaking
            maxModelOID = Math.max(maxModelOID, modelRecord.oid);

            writeUpdatedModelRecord(modelRecord);

            predecessor = modelRecord;
         }

         // sequence model_seq will be adjusted during schema finalization

         item.commit();
      }
      catch (Exception e)
      {
         final String message = "Failed migrating models table.";

         warn(message, e);
         rollback();
         throw new UpgradeException(message);
      }

      try
      {
         migrateUserProperties();

         item.commit();
      }
      catch (Exception e)
      {
         final String message = "Failed migrating user properties.";

         warn(message, e);
         rollback();
         throw new UpgradeException(message);
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
      if (!item.isArchiveAuditTrail())
      {
         // alter model_seq beyond current greatest model OID
         ResultSet rs = null;
         try
         {
            rs = DatabaseHelper.executeQuery(item, "SELECT max(modelOid) FROM model");
            if (rs.next())
            {
               final String upgradeLevel = item.getProperty(UPGRADE_LEVEL);

               long maxOid = rs.getLong(1);
               if (rs.wasNull())
               {
                  maxOid = 0;
               }
               else if (maxOid >= (1l << 32))
               {
                  maxOid = (maxOid >> 32);
               }
               // TODO schema name
               DatabaseHelper.executeDdlStatement(item, item.getDbDescriptor()
                     .getDropPKSequenceStatementString(null, "model_seq"));
               DatabaseHelper.executeDdlStatement(item, item.getDbDescriptor()
                     .getCreatePKSequenceStatementString(null,
                           "model_seq", String.valueOf(1 + maxOid)));
            }
            else
            {
               error("Failed adjusting model table sequence. Unable to determine current "
                     + "maximum model OID value.", null);
            }
         }
         catch (SQLException e)
         {
            error("Failed adjusting model table sequence.", e);
         }
      }

      DatabaseHelper.alterTable(item, new AlterTableInfo("PROCESS_INSTANCE")
      {
         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {
                  new FieldInfo("startingDomain", Long.TYPE),
                  new FieldInfo("currentDomain", Long.TYPE)};
         }
      }, this);

      DatabaseHelper.dropTable(item, new DropTableInfo("USER_ORGANISATION",
            !item.isArchiveAuditTrail() ? "USER_ORGANISATION_SEQ" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("USER_ROLE",
            !item.isArchiveAuditTrail() ? "USER_ROLE_SEQ" : null), this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("USER_PROPERTY")
      {
         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {new FieldInfo("workflowUser", Long.TYPE)};
         }
      }, this);

      DatabaseHelper.dropTable(item, new DropTableInfo("dom_organisation",
            !item.isArchiveAuditTrail() ? "domain_organisation_seq" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("subdom_superdom",
            !item.isArchiveAuditTrail() ? "subdomain_superdomain_seq" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("domain_domain",
            !item.isArchiveAuditTrail() ? "domain_domain_seq" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("domain_property",
            !item.isArchiveAuditTrail() ? "domain_property_seq" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("worlflowdomain",
            !item.isArchiveAuditTrail() ? "workflowdomain_seq" : null), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("daemon_log",
            !item.isArchiveAuditTrail() ? "daemon_log_seq" : null), this);

      DatabaseHelper.createTable(item, new DaemonLogTableInfo(item.isArchiveAuditTrail()), this);

      DatabaseHelper.dropTable(item, new DropTableInfo("notification_log",
            !item.isArchiveAuditTrail() ? "notification_log_seq" : null), this);

      DatabaseHelper.alterTable(item, new AlterTableInfo("MODEL")
      {
         public FieldInfo[] getDroppedFields()
         {
            return new FieldInfo[] {
                  new FieldInfo("modelOID", Long.TYPE),
                  new FieldInfo("versionID", String.class)};
         }
      }, this);
   }

   private Set loadSortedModelRecords()
   {
      Set models = new TreeSet(new Comparator()
            {
               public int compare(Object lhs, Object rhs)
               {
                  // sorting strategy copied over from 2.7.x code, sorting with descending
                  // valid-from date and modelOID
                  ModelRecord lhsModel = (ModelRecord) lhs;
                  ModelRecord rhsModel = (ModelRecord) rhs;
                  Calendar lhsValidFrom = Calendar.getInstance();
                  lhsValidFrom.setTime(new Date(lhsModel.validFrom));
                  Calendar rhsValidFrom = Calendar.getInstance();
                  rhsValidFrom.setTime(new Date(rhsModel.validFrom));

                  int result;
                  if (lhsValidFrom.equals(rhsValidFrom))
                  {
                     result = new Long(rhsModel.oldOID).compareTo(new Long(lhsModel.oldOID));
                  }
                  else
                  {
                     result = lhsValidFrom.after(rhsValidFrom) ? -1 : 1;
                  }

                  return result;
               }
            });

      for (Iterator i = getPre30ModelOIDs(); i.hasNext();)
      {
         final long modelOID = ((Long) i.next()).longValue();

         Statement stmt = null;
         try
         {
            stmt = item.getConnection().createStatement();
            ResultSet rs = null;
            try
            {
               rs = stmt.executeQuery("SELECT oid, id, name, versionID, "
                     + "validFrom, validTo FROM model"
                     + " WHERE modelOID=" + modelOID);

               if (rs.next())
               {
                  // preserve modelOID, as it is used in the high 32 bit of full model
                  // element oids
                  ModelRecord modelRecord = new ModelRecord(rs.getLong(1),
                        modelOID >> 32, rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getLong(5), rs.getLong(6));

                  models.add(modelRecord);
               }
               else
               {
                  throw new UpgradeException("Failed loading model (oid=" + modelOID
                        + ") for data migration");
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rs);
            }
         }
         catch (SQLException e)
         {
            String message = "Failed loading model records";
            warn(message, e);
            throw new UpgradeException(message);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
      return models;
   }

   private Map findRuntimeParticipants(long modelOID, Element modelDOM)
   {
      Map participants = new TreeMap();

      try
      {
         XPathExpression participantsXPath = XPathFactory.newInstance().newXPath().compile(V273_PARTICIPANTS_TAG + "/*");
         NodeList participantNodes = (NodeList) participantsXPath.evaluate(modelDOM, XPathConstants.NODESET);
         for (int i = 0, nNodes = participantNodes.getLength(); i < nNodes; i++)
         {
            Node participantNode = (Node) participantNodes.item(i);
            if ((Node.ELEMENT_NODE == participantNode.getNodeType())
                  && (V273_ORGANISATION_TAG.equals(participantNode.getNodeName())
                        || V273_ROLE_TAG.equals(participantNode.getNodeName())))
            {
               final NamedNodeMap attrs = participantNode.getAttributes();

               final long elementOID = Long.parseLong(attrs.getNamedItem("oid")
                     .getNodeValue());

               if ((elementOID >> 32) == modelOID)
               {
                  info("Skipping OID conversion of " + elementOID);

                  participants.put(new Long(elementOID), participantNode);
               }
               else
               {
                  Assert.condition(elementOID < 0x000000100000000l, "Invalid element OID: "
                        + elementOID);

                  participants.put(new Long((modelOID << 32) + elementOID), participantNode);
               }
            }
         }
      }
      catch (XPathException e)
      {
         String message = "Failed trying to extract participants from model "
               + modelOID;

         fatal(message, e);
         throw new UpgradeException(message);
      }

      return participants;
   }

   private void populateParticipantTable(Map participants, boolean recover)
         throws SQLException
   {
      for (Iterator i = participants.entrySet().iterator(); i.hasNext();)
      {
         final Entry entry = (Entry) i.next();

         final long oid = ((Long) (entry).getKey()).longValue();
         final long modelOID = oid >> 32;

         Element participantNode = (Element) entry.getValue();
         final long elementOID = Long.parseLong(participantNode.getAttribute("oid"));

         Assert.condition(((oid == elementOID) && (modelOID == (elementOID >> 32)))
               || (oid == ((modelOID << 32) + elementOID)),
               "Invalid participant runtime OID :" + oid);

         final NodeList descriptionNodes = participantNode
               .getElementsByTagName(V273_DESCRIPTION_TAG);
         final String description = (0 < descriptionNodes.getLength()) ? descriptionNodes
               .item(0).getNodeValue() : "";

         Statement stmt = null;
         try
         {
            stmt = item.getConnection().createStatement();

            ResultSet rs = null;
            try
            {
               rs = stmt.executeQuery("SELECT * FROM " + ParticipantTableInfo.TABLE_NAME
                     + " WHERE " + ParticipantTableInfo.OID.name + "=" + oid);

               if (recover && rs.next())
               {
                  stmt.executeUpdate("UPDATE "
                        + ParticipantTableInfo.TABLE_NAME
                        + " SET "
                        + ParticipantTableInfo.ID.name + "='"
                        + StringUtils.cutString(participantNode.getAttribute("id"), 50)
                        + "', " + ParticipantTableInfo.MODEL.name + "=" + modelOID
                        + ", " + ParticipantTableInfo.NAME.name + "='" + StringUtils
                              .cutString(participantNode.getAttribute("name"), 100)
                        + "', " + ParticipantTableInfo.DESCRIPTION.name + "='"
                        + StringUtils.cutString(description, 4000) + "', "
                        + ParticipantTableInfo.TYPE.name + "=0"
                        + " WHERE " + ParticipantTableInfo.OID.name + "=" + oid);
               }
               else
               {
                  stmt.executeUpdate("INSERT INTO "
                        + ParticipantTableInfo.TABLE_NAME
                        + "(" + ParticipantTableInfo.OID.name
                        + ", " + ParticipantTableInfo.ID.name
                        + ", " + ParticipantTableInfo.MODEL.name
                        + ", " + ParticipantTableInfo.NAME.name
                        + ", " + ParticipantTableInfo.DESCRIPTION.name
                        + ", " + ParticipantTableInfo.TYPE.name
                        + ") VALUES (" + oid
                        + ", '" + StringUtils
                              .cutString(participantNode.getAttribute("id"), 50)
                        + "', " + modelOID
                        + ", '" + StringUtils
                              .cutString(participantNode.getAttribute("name"), 100)
                        + "', '" + StringUtils.cutString(description, 4000) + "', 0)");
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rs);
            }
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   private void populateParticipantLinkTable(long modelOID, Map participants,
         boolean recover) throws SQLException
   {
      // migrate USER_ORGANISATION/USER_ROLE data to USER_PARTICIPANT table

      Statement selectStmt = null;
      try
      {
         selectStmt = item.getConnection().createStatement();

         ResultSet orgLinks = null;
         try
         {
            orgLinks = selectStmt.executeQuery("SELECT workflowUser, organisationOID"
                  + "  FROM USER_ORGANISATION"
                  + " WHERE organisationOID BETWEEN " + (modelOID << 32)
                  + " AND " + (((modelOID + 1) << 32) - 1));

            createParticipantLinks(participants, orgLinks, V273_ORGANISATION_TAG);
         }
         finally
         {
            QueryUtils.closeResultSet(orgLinks);
         }

         ResultSet roleLinks = null;
         try
         {
            roleLinks = selectStmt.executeQuery("SELECT workflowUser, roleOID"
                  + " FROM USER_ROLE"
                  + " WHERE roleOID BETWEEN " + (modelOID << 32)
                  + " AND " + (((modelOID + 1) << 32) - 1));

            createParticipantLinks(participants, roleLinks, V273_ROLE_TAG);
         }
         finally
         {
            QueryUtils.closeResultSet(roleLinks);
         }
      }
      finally
      {
         QueryUtils.closeStatement(selectStmt);
      }
   }

   private void createParticipantLinks(Map participants, ResultSet links,
         String participantTag) throws SQLException
   {
      while (links.next())
      {
         Statement stmt = null;
         try
         {
            final long userOID = links.getLong(1);
            final long participantOID = links.getLong(2);

            Element participantNode = (Element) participants.get(new Long(
                  participantOID));

            if (null != participantNode)
            {
               Assert.condition(participantTag.equals(participantNode.getNodeName()),
                     "Invalid participant link between user " + userOID
                           + " and participant " + participantOID + ", expected link to "
                           + "participant of type " + participantTag);

               stmt = item.getConnection().createStatement();

               ResultSet rs = null;
               try
               {
                  rs = stmt.executeQuery("SELECT *"
                        + "  FROM " + UserParticipantTableInfo.TABLE_NAME
                        + " WHERE " + UserParticipantTableInfo.WORKFLOW_USER.name
                        + "=" + userOID
                        + "   AND " + UserParticipantTableInfo.PARTICIPANT.name
                        + "=" + participantOID);

                  if (!rs.next())
                  {
                     stmt.executeUpdate("INSERT INTO "
                           + UserParticipantTableInfo.TABLE_NAME
                           + " ("
                           + UserParticipantTableInfo.OID.name
                           + ", "
                           + UserParticipantTableInfo.WORKFLOW_USER.name
                           + ", "
                           + UserParticipantTableInfo.PARTICIPANT.name
                           + ") VALUES ("
                           + item.getSequenceValue(UserParticipantTableInfo.SEQUENCE_NAME,
                                 UserParticipantTableInfo.TABLE_NAME,
                                 UserParticipantTableInfo.OID.name)
                           + ", " + userOID + ", " + participantOID + ")");
                  }
               }
               finally
               {
                  QueryUtils.closeResultSet(rs);
               }
            }
            else
            {
               warn("Invalid participant link between user " + userOID
                     + " and unknown participant " + participantOID, null);
/*
               throw new UpgradeException("Invalid participant link between user "
                     + userOID + " and unknown participant " + participantOID);
*/
            }
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
         }
      }
   }

   private void migrateNotifications(ModelRecord modelRecord, Element modelDOM) throws SQLException
   {
      try
      {
         XPathExpression processNotificationsXPath = XPathFactory.newInstance().newXPath().compile(V273_WORKFLOW_TAG + "/" + V273_NOTIFICATION_TAG);
         NodeList processNotifications = (NodeList) processNotificationsXPath.evaluate(modelDOM, XPathConstants.NODESET);
         for (int j = 0, nNodes = processNotifications.getLength(); j < nNodes; j++)
         {
            Element notification = (Element) processNotifications.item(j);
            Element process = (Element) notification.getParentNode();

            createTimerEventBindings(notification, modelRecord.oid, process,
                  MIGRATION_PROFILE_PROCESS_NOTIFICATION);
         }

         XPathExpression activityNotificationsXPath = XPathFactory.newInstance().newXPath().compile(V273_WORKFLOW_TAG + "/" + V273_ACTIVITY_TAG + "/" + V273_NOTIFICATION_TAG);
         NodeList activityNotifications = (NodeList) activityNotificationsXPath.evaluate(modelDOM, XPathConstants.NODESET);
         for (int j = 0, nNodes = activityNotifications.getLength(); j < nNodes; j++)
         {
            Element notification = (Element) activityNotifications.item(j);
            Element activity = (Element) notification.getParentNode();

            createTimerEventBindings(notification, modelRecord.oid, activity,
                  MIGRATION_PROFILE_ACTIVITY_NOTIFICATION);
         }
      }
      catch (XPathException e)
      {
         warn("Failed extracting notifications for migration", e);
      }
   }

   private void writeUpdatedModelRecord(ModelRecord modelRecord) throws SQLException
   {
      Statement stmt = null;
      try
      {
         stmt = item.getConnection().createStatement();
         stmt.executeUpdate("UPDATE model SET oid=" + modelRecord.oid
               + ", modelOID=" + modelRecord.oid
               + ", version='" + modelRecord.version + "'"
               + ", predecessor=" + modelRecord.predecessorOID
               + ", revision=1, deploymentStamp=" + new Date().getTime()
               + ", deploymentComment='', disabled=0 WHERE oid=" + modelRecord.oldOID);

         stmt.executeUpdate("UPDATE string_data SET objectID=" + modelRecord.oid
               + " WHERE data_type='model' AND objectID=" + (modelRecord.oid << 32));

         stmt.executeUpdate("UPDATE data SET model=" + modelRecord.oid
               + " WHERE model=" + (modelRecord.oid << 32));

         stmt.executeUpdate("UPDATE process_definition SET model=" + modelRecord.oid
               + " WHERE model=" + (modelRecord.oid << 32));
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
   }

   private void createTimerEventBindings(Element source, long modelOID,
         Element target, NotificationMigrationProfile migrationProfile) throws SQLException
   {
      final long handlerElementOID = Long.parseLong(source.getAttribute(V273_OID_ATTR));
      final long handlerOID = (modelOID << 32) + handlerElementOID;

      final long milliSecondsOffset = !StringUtils.isEmpty(source.getAttribute(V273_NOTIFICATION_TIMEOUT_ATTR))
            ? Long.parseLong(source.getAttribute(V273_NOTIFICATION_TIMEOUT_ATTR))
            : 0;
      // special care needed for 'year_timeout' and 'month_timeout' as of the resulting
      // variable absolute offset
      final int yearOffset = (!StringUtils.isEmpty(source.getAttribute(V273_NOTIFICATION_YEAR_TIMEOUT_ATTR)))
            ? Integer.parseInt(source.getAttribute(V273_NOTIFICATION_YEAR_TIMEOUT_ATTR))
            : 0;
      final int monthOffset = (!StringUtils.isEmpty(source.getAttribute(V273_NOTIFICATION_MONTH_TIMEOUT_ATTR)))
            ? Integer.parseInt(source.getAttribute(V273_NOTIFICATION_MONTH_TIMEOUT_ATTR))
            : 0;

      final long targetElementOID = Long.parseLong(target.getAttribute(V273_OID_ATTR));
      final long targetOID = (modelOID << 32) + targetElementOID;

      Statement stmt = null;
      try
      {
         stmt = item.getConnection().createStatement();

         if ((0 == monthOffset) && (0 == yearOffset))
         {
            // batch-insert timeouts consisting of fixed seconds amount
            stmt.executeUpdate("INSERT INTO " + EventBindingTableInfo.TABLE_NAME
                  + " (" + EventBindingTableInfo.OID.name
                  + ", " + EventBindingTableInfo.OBJECT_OID.name
                  + ", " + EventBindingTableInfo.TYPE.name
                  + ", " + EventBindingTableInfo.HANDLER_OID.name
                  + ", " + EventBindingTableInfo.BIND_STAMP.name
                  + ", " + EventBindingTableInfo.TARGET_STAMP.name + ") "
                  // TODO schema name
                  + "SELECT " + item.getDbDescriptor().getNextValForSeqString(
                        null, EventBindingTableInfo.SEQUENCE_NAME)
                  + ", t.oid, " + migrationProfile.eventTargetType + ", " + handlerOID
                  + ", t.startTime, t.startTime+" + milliSecondsOffset
                  + "  FROM " + migrationProfile.targetTable + " t"
                  + " WHERE t.state " + migrationProfile.activeTargetStateFilter
                  + "   AND t." + migrationProfile.targetModelElement + "=" + targetOID
                  + "   AND t.oid NOT IN ("
                  + "    SELECT nl.runtimeObject FROM notification_log nl"
                  + "     WHERE nl.type=" + migrationProfile.notificationLogType
                  + "       AND nl.notification=" + handlerOID
                  + "    )"
                  + "   AND t.oid NOT IN ("
                  + "    SELECT " + EventBindingTableInfo.OBJECT_OID.name
                  + "      FROM " + EventBindingTableInfo.TABLE_NAME
                  + "     WHERE " + EventBindingTableInfo.OBJECT_OID.name + "=t.oid"
                  + "       AND " + EventBindingTableInfo.HANDLER_OID.name + "=" + handlerOID
                  + "    )");
         }
         else
         {
            // manually compute timouts involving months or years
            ResultSet rs = null;
            try
            {
               rs = stmt.executeQuery("SELECT oid, startTime"
                     + "  FROM " + migrationProfile.targetTable + " t"
                     + " WHERE t.state " + migrationProfile.activeTargetStateFilter
                     + "   AND t." + migrationProfile.targetModelElement + "=" + targetOID
                     + "   AND t.oid NOT IN ("
                     + "    SELECT nl.runtimeObject FROM notification_log nl"
                     + "     WHERE nl.type=" + migrationProfile.notificationLogType
                     + "       AND nl.notification=" + handlerOID
                     + "    )"
                     + "   AND t.oid NOT IN ("
                     + "    SELECT " + EventBindingTableInfo.OBJECT_OID.name
                     + "      FROM " + EventBindingTableInfo.TABLE_NAME
                     + "     WHERE " + EventBindingTableInfo.OBJECT_OID.name + "=t.oid"
                     + "       AND " + EventBindingTableInfo.HANDLER_OID.name + "=" + handlerOID
                     + "    )");

               PreparedStatement insertStmt = null;
               try
               {
                  insertStmt = item.getConnection().prepareStatement(
                        "INSERT INTO " + EventBindingTableInfo.TABLE_NAME
                        + " (" + EventBindingTableInfo.OID.name
                        + ", " + EventBindingTableInfo.OBJECT_OID.name
                        + ", " + EventBindingTableInfo.TYPE.name
                        + ", " + EventBindingTableInfo.HANDLER_OID.name
                        + ", " + EventBindingTableInfo.BIND_STAMP.name
                        + ", " + EventBindingTableInfo.TARGET_STAMP.name + ") "
                        + "VALUES (?"
                        + ", ?"
                        + ", " + migrationProfile.eventTargetType
                        + ", " + handlerOID
                        + ", ?, ?)");

                  int batchSizeCounter = 0;
                  Calendar cal = Calendar.getInstance();
                  while (rs.next())
                  {
                     final long oid = rs.getLong(1);
                     final long startTime = rs.getLong(2);

                     cal.setTime(new Date(startTime + milliSecondsOffset));
                     cal.add(Calendar.YEAR, yearOffset);
                     cal.add(Calendar.MONTH, monthOffset);

                     insertStmt.setLong(1, Long.parseLong(item.getSequenceValue(
                           EventBindingTableInfo.SEQUENCE_NAME,
                           EventBindingTableInfo.TABLE_NAME,
                           EventBindingTableInfo.OID.name)));
                     insertStmt.setLong(2, oid);
                     insertStmt.setLong(3, startTime);
                     insertStmt.setLong(4, cal.getTime().getTime());

                     if (batchSize > batchSizeCounter)
                     {
                        insertStmt.addBatch();
                        ++batchSizeCounter;
                     }
                     else
                     {
                        insertStmt.executeBatch();
                        insertStmt.clearBatch();
                        batchSizeCounter = 0;
                     }
                  }

                  if (0 < batchSizeCounter)
                  {
                     insertStmt.executeBatch();
                  }
               }
               finally
               {
                  QueryUtils.closeStatement(insertStmt);
               }
            }
            finally
            {
               QueryUtils.closeResultSet(rs);
            }
         }
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
   }

   private void migrateUserProperties() throws SQLException
   {
      Statement stmt = null;
      try
      {
         stmt = item.getConnection().createStatement();
         stmt.executeUpdate("UPDATE USER_PROPERTY SET "
               + PropertyTableInfo.OBJECT_OID.name + " = workflowUser");
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
   }

   private static class DaemonLogTableInfo extends CreateTableInfo
   {
      public static final FieldInfo OID = new FieldInfo("oid", Long.TYPE);
      public static final FieldInfo TYPE = new FieldInfo("type", String.class, 100);
      public static final FieldInfo CODE = new FieldInfo("code", Integer.TYPE);
      public static final FieldInfo STAMP = new FieldInfo("stamp", Long.TYPE);
      public static final FieldInfo STATE = new FieldInfo("state", Integer.TYPE);

      public static final IndexInfo IDX1 = new IndexInfo("DAEMON_LOG_IDX1", true,
            new FieldInfo[] {OID});
      private boolean archive;

      public DaemonLogTableInfo(boolean archive)
      {
         super("DAEMON_LOG", false);
         this.archive = archive;
      }

      public FieldInfo[] getFields()
      {
         return new FieldInfo[] {OID, TYPE, CODE, STAMP, STATE};
      }

      public IndexInfo[] getIndexes()
      {
         return new IndexInfo[] {IDX1};
      }

      public String getSequenceName()
      {
         return !archive ? "DAEMON_LOG_SEQ" : null;
      }
   }

   private static class PropertyTableInfo extends CreateTableInfo
   {
      public static final FieldInfo OID = new FieldInfo("oid", Long.TYPE);
      public static final FieldInfo OBJECT_OID = new FieldInfo("objectOID", Long.TYPE);
      public static final FieldInfo NAME = new FieldInfo("name", String.class);
      public static final FieldInfo TYPE_KEY = new FieldInfo("type_key", Integer.TYPE);
      public static final FieldInfo NUMBER_VALUE = new FieldInfo("number_value",
            Long.TYPE);
      public static final FieldInfo STRING_VALUE = new FieldInfo("string_value",
            String.class, 128);

      private final String baseName;
      private boolean archive;

      public PropertyTableInfo(String baseName, boolean archive)
      {
         super(baseName + "_PROPERTY", false);
         this.baseName = baseName;
         this.archive = archive;
      }

      public FieldInfo[] getFields()
      {
         return new FieldInfo[] {OID, OBJECT_OID, NAME, TYPE_KEY, NUMBER_VALUE,
               STRING_VALUE};
      }

      public IndexInfo[] getIndexes()
      {
         return new IndexInfo[] {
               new IndexInfo(baseName + "_PRP_IDX1", new FieldInfo[] {OBJECT_OID}),
               new IndexInfo(baseName + "_PRP_IDX2", new FieldInfo[] {
                     TYPE_KEY, NUMBER_VALUE}),
               new IndexInfo(baseName + "_PRP_IDX3", new FieldInfo[] {
                     TYPE_KEY, STRING_VALUE}),
               new IndexInfo(baseName + "_PRP_IDX4", true, new FieldInfo[] {OID})};
      }

      public String getSequenceName()
      {
         return !archive ? baseName + "_PROPERTY_SEQ" : null;
      }
   }

   private static class EventBindingTableInfo extends CreateTableInfo
   {
      public static final String TABLE_NAME = "event_binding";
      public static final String SEQUENCE_NAME = "event_binding_seq";

      public static final FieldInfo OID = new FieldInfo("oid", Long.TYPE);
      public static final FieldInfo OBJECT_OID = new FieldInfo("objectOID", Long.TYPE);
      public static final FieldInfo TYPE = new FieldInfo("type", Integer.TYPE);
      public static final FieldInfo HANDLER_OID = new FieldInfo("handlerOID", Long.TYPE);
      public static final FieldInfo BIND_STAMP = new FieldInfo("bindStamp", Long.TYPE);
      public static final FieldInfo TARGET_STAMP = new FieldInfo("targetStamp", Long.TYPE);
      private boolean archive;

      public EventBindingTableInfo(boolean archive)
      {
         super(TABLE_NAME, false);
         this.archive = archive;
      }

      public FieldInfo[] getFields()
      {
         return new FieldInfo[] {OID, OBJECT_OID, TYPE, HANDLER_OID, BIND_STAMP, TARGET_STAMP};
      }

      public IndexInfo[] getIndexes()
      {
         return new IndexInfo[] {
               new IndexInfo("event_binding_idx1", true, new FieldInfo[] {OID}),
               new IndexInfo("event_binding_idx2", new FieldInfo[] {
                     OBJECT_OID, TYPE, HANDLER_OID}),
               new IndexInfo("event_binding_idx3", new FieldInfo[] {TARGET_STAMP})};
      }

      public String getSequenceName()
      {
         return !archive ? SEQUENCE_NAME : null;
      }
   }

   private static class UserParticipantTableInfo extends CreateTableInfo
   {
      public static final String TABLE_NAME = "USER_PARTICIPANT";
      public static final String SEQUENCE_NAME = "user_participant_seq";

      public static final FieldInfo OID = new FieldInfo("oid", Long.TYPE);
      public static final FieldInfo WORKFLOW_USER = new FieldInfo("workflowUser",
            Long.TYPE);
      public static final FieldInfo PARTICIPANT = new FieldInfo("participant", Long.TYPE);

      public static final IndexInfo IDX1 = new IndexInfo("user_particip_idx1",
            new FieldInfo[] {WORKFLOW_USER});
      public static final IndexInfo IDX2 = new IndexInfo("user_particip_idx2",
            new FieldInfo[] {PARTICIPANT});
      public static final IndexInfo IDX3 = new IndexInfo("user_particip_idx3", true,
            new FieldInfo[] {OID});
      private boolean archive;

      public UserParticipantTableInfo(boolean archive)
      {
         super(TABLE_NAME, false);
         this.archive = archive;
      }

      public FieldInfo[] getFields()
      {
         return new FieldInfo[] {OID, WORKFLOW_USER, PARTICIPANT};
      }

      public IndexInfo[] getIndexes()
      {
         return new IndexInfo[] {IDX1, IDX2, IDX3};
      }

      public String getSequenceName()
      {
         return !archive ? SEQUENCE_NAME : null;
      }
   }

   private static class ParticipantTableInfo extends CreateTableInfo
   {
      public static final String TABLE_NAME = "PARTICIPANT";

      public static final FieldInfo OID = new FieldInfo("oid", Long.TYPE);
      public static final FieldInfo ID = new FieldInfo("id", String.class, 50);
      public static final FieldInfo MODEL = new FieldInfo("model", Long.TYPE);
      public static final FieldInfo NAME = new FieldInfo("name", String.class, 100);
      public static final FieldInfo DESCRIPTION = new FieldInfo("description",
            String.class, 4000);
      public static final FieldInfo TYPE = new FieldInfo("type", Integer.TYPE);

      public static final IndexInfo IDX1 = new IndexInfo("participant_idx1", true,
            new FieldInfo[] {OID});
      public static final IndexInfo IDX2 = new IndexInfo("participant_idx2",
            new FieldInfo[] {ID});
      private boolean archive;

      public ParticipantTableInfo(boolean archive)
      {
         super(TABLE_NAME, false);
         this.archive = archive;
      }

      public FieldInfo[] getFields()
      {
         return new FieldInfo[] {OID, ID, MODEL, NAME, DESCRIPTION, TYPE};
      }

      public IndexInfo[] getIndexes()
      {
         return new IndexInfo[] {IDX1, IDX2};
      }

      public String getSequenceName()
      {
         return null;
      }
   }

   private static class ModelRecord
   {
      private final long oldOID;
      private final long oid;

      private final String id;
      private final String name;
      private final String version;
      private final long validFrom;
      private final long validTo;

      private long predecessorOID;

      // deploymentComment will remain empty
      // deploymentStamp will be set to new Date()
      // revision will be set to 1
      // disabled will be set to false

      public ModelRecord(long oldOID, long oid, String id, String name, String version,
            long validFrom, long validTo)
      {
         this.oldOID = oldOID;

         this.oid = oid;
         this.id = id;
         this.name = name;
         this.version = version;
         this.validFrom = validFrom;
         this.validTo = validTo;
      }
   }

   private static class NotificationMigrationProfile
   {
      public final int eventTargetType;
      public final int notificationLogType;
      public final String targetTable;
      public final String targetModelElement;
      public final String activeTargetStateFilter;

      private NotificationMigrationProfile(int eventTargetType, int notificationLogType,
            String targetTable, String targetModelElement, String activeTargetStateFilter)
      {
         this.eventTargetType = eventTargetType;
         this.notificationLogType = notificationLogType;
         this.targetTable = targetTable;
         this.targetModelElement = targetModelElement;
         this.activeTargetStateFilter = activeTargetStateFilter;
      }
   }
}
