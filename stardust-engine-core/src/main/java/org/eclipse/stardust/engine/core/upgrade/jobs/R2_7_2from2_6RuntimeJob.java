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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class R2_7_2from2_6RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   private Map modelElements = new HashMap();
   private int batchSize = 100;
   private boolean testMode;

   public R2_7_2from2_6RuntimeJob()
   {
      String bs = Parameters.instance().getString(RuntimeUpgrader.UPGRADE_BATCH_SIZE);
      if (bs != null)
      {
         batchSize = Integer.parseInt(bs);
      }
      String tm = System.getProperty("carnot.upgrade.testmode");
      if (tm != null)
      {
         testMode = Boolean.getBoolean(tm);
      }
   }

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX activity_inst_idx7 ON "
               + "activity_instance (state)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index activity_inst_idx7 on table "
               + "ACTIVITY_INSTANCE.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX TRANS_TOKEN_IDX3 "
               + "ON TRANS_TOKEN (processinstance, transition, isConsumed)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index TRANS_TOKEN_IDX3 for  table "
               + "TRANS_TOKEN.";
         warn(message, e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE UNIQUE INDEX DATA_VALUES_INDEX6 "
               + "ON DATA_VALUE (data, processInstance)");
         item.getConnection().commit();
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index DATA_VALUES_INDEX6 for table "
               + "DATA_VALUE.";
         warn(message, e);
      }

   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      readModels();

      upgradeTable("ACTIVITY_INSTANCE", new String[]{"ACTIVITY", "CURRENTPERFORMER"}, "OID", true);
      upgradeTable("ACTIVITY_INST_LOG", new String[]{"PARTICIPANT"}, "OID", true);
      upgradeTable("TRANS_INST", new String[]{"TRANSITION"}, "OID", true);
      upgradeTable("TRANS_TOKEN", new String[]{"TRANSITION"}, "OID", true);
      upgradeTable("DATA_VALUE", new String[]{"DATA"}, "OID", true);
      upgradeTable("TIMER_LOG", new String[]{"TRIGGEROID"}, "OID", true);
      upgradeTable("USER_ORGANISATION", new String[]{"ORGANISATIONOID"}, "ID", true);
      upgradeTable("USER_ROLE", new String[]{"ROLEOID"}, "ID", true);
      upgradeTable("PROCESS_INSTANCE", new String[]{"PROCESSDEFINITION"}, "OID", true);
      upgradeTable("ACTIVITY", new String[]{"OID", "PROCESS_DEFINITION"}, "OID", false);
      upgradeTable("DATA", new String[]{"OID"}, "OID", false);
      upgradeTable("PROCESS_DEFINITION", new String[]{"OID"}, "OID", false);
      upgradeTable("TRANSITION", new String[]{"OID", "SOURCE_ACTIVITY", "TARGET_ACTIVITY"}, "OID", false);
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   public Version getVersion()
   {
      return Version.createFixedVersion(2, 7, 2);
   }

   protected void upgradeModel(boolean recover) throws UpgradeException
   {
   }

   private void upgradeTable(String name, String[] columns, String pkColumn, boolean cut)
   {
      info("Upgrading table '" + name + "'....");

      StringBuffer selectString = new StringBuffer("SELECT ").append(pkColumn).append(" AS PKVAL, ");
      StringBuffer updateString = new StringBuffer("UPDATE " + name + " SET ");
      for (int i = 0; i < columns.length; i++)
      {
         String column = columns[i];
         selectString.append(column);
         updateString.append(column).append("=?");
         if (i < columns.length - 1)
         {
            selectString.append(", ");
            updateString.append(", ");
         }
      }
      selectString.append(" FROM " + name);
      selectString.append(" WHERE ").append(pkColumn).append(">?");
      if (cut)
      {
         selectString.append(" AND rownum <").append(batchSize);
      }
      selectString.append(" ORDER BY ").append(pkColumn);

      updateString.append(" WHERE ").append(pkColumn).append("=?");

      try
      {
         Connection connection = item.getConnection();
         PreparedStatement statement = connection.prepareStatement(selectString.toString());
         PreparedStatement upgradeStmt = connection.prepareStatement(updateString.toString());

         boolean eating = true;
         long oid = 0;
         while (eating)
         {
            eating = false;
            statement.setLong(1, oid);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
               if (cut)
               {
                  eating = true;
               }
               try
               {
                  oid = rs.getLong(1);
                  long[] values = new long[columns.length];
                  for (int i = 0; i < values.length; i++)
                  {

                     long oldElementOID = rs.getLong(i + 2);
                     if (((int) oldElementOID) > (1L << 17))
                     {
                        info("oid " + oldElementOID + " already transformed for table '"
                              + name + "', column '" + columns[i] + "'.");
                        values[i] = oldElementOID;
                     }
                     else
                     {
                        if (oldElementOID <= 0)
                        {
                           values[i] = oldElementOID;
                        }
                        else
                        {
                           Long modelOID = (Long) modelElements.get(new Long(oldElementOID));
                           if (modelOID == null)
                           {
                              warn("The oid " + oldElementOID + " for table '"
                                    + name + "', column '" + columns[i]
                                    + "' couldn't be found in any model.", null);
                              values[i] = oldElementOID;
                           }
                           else if ((0 == modelOID.intValue())
                                 && (oldElementOID >> 32) == modelOID.longValue() >> 32)
                           {
                              // keep original representation, as it fits the 2.7.2++ pattern
                              values[i] = oldElementOID;
                           }
                           else
                           {
                              values[i] = modelOID.longValue()
                                    + ((oldElementOID - (int) oldElementOID) >> 15)
                                    + ((int) oldElementOID);
                           }
                        }
                     }
                     upgradeStmt.setLong(i + 1, values[i]);
                  }
                  if (testMode)
                  {
                     continue;
                  }
                  upgradeStmt.setLong(values.length + 1, oid);
                  upgradeStmt.addBatch();
               }
               catch (SQLException e)
               {
                  warn(e.getMessage(), null);
               }
            }
            if (cut && !eating)
            {
               break;
            }
            upgradeStmt.executeBatch();
            connection.commit();
            info("Commit point for table '" + name + "', oid = " + oid);
         }
         info("Table '" + name + "' upgraded.");
      }
      catch (SQLException e)
      {
         error("", e);
      }
   }

   private void readModels()
   {
      Iterator oidItr = getPre30ModelOIDs();
      while (oidItr.hasNext())
      {
         long oid = ((Long) oidItr.next()).longValue();
         String model = retrieveModelFromAuditTrail(oid).getModel();
         parseModel(model);
      }
   }

   private void parseModel(String model)
   {
      try
      {
         DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();

         domBuilderFactory.setValidating(false);

         DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
         domBuilder.setErrorHandler(new DefaultHandler());

         InputSource inputSource = new InputSource(new StringReader(model));
         URL dtd = RuntimeUpgradeJob.class.getResource("WorkflowModel.dtd");
         inputSource.setSystemId(dtd.toString());
         Document document = domBuilder.parse(inputSource);
         Element modelElement = document.getDocumentElement();
         fetchOIDValues(modelElement);
      }
      catch (SAXException e)
      {
         error("", e);
      }
      catch (IOException e)
      {
         error("", e);
      }
      catch (ParserConfigurationException e)
      {
         fatal("", e);
      }
   }

   private void fetchOIDValues(Element model)
   {
      Long modelOID = new Long(model.getAttribute("oid"));
      NodeList elements = model.getElementsByTagName("*");
      for (int i = 0; i < elements.getLength(); i++)
      {
         Element element = (Element) elements.item(i);
         if (element.hasAttribute("oid"))
         {
            try
            {
               Long oid = new Long(element.getAttribute("oid"));
               if (oid.longValue() != 0)
               {
                  if (modelElements.containsKey(oid))
                  {
                     error("The oid " + oid + " from model " + modelOID
                           + "is already in use for model " + modelElements.get(oid), null);
                  }
                  else
                  {
                     modelElements.put(oid, modelOID);
                  }
               }
            }
            catch (Exception e)
            {
               warn("", e);
            }
         }
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
