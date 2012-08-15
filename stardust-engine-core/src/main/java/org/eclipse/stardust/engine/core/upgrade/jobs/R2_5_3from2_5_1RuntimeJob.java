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

import java.sql.SQLException;
import java.util.Iterator;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.TableInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class R2_5_3from2_5_1RuntimeJob extends OracleAwareRuntimeUpgradeJob
{
   public static final Logger trace = LogManager.getLogger(R2_5_3from2_5_1RuntimeJob.class);

   private static final TableInfo[] NEW_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo("MESSAGE_STORE", "oid NUMBER, source VARCHAR2(255), "
                  + "arrivalTime number, rCount number"),
         };

   private static final TableInfo[] DROP_TABLE_LIST =
         new TableInfo[]
         {
            new TableInfo("MESSAGE", null),
         };

   protected void upgradeSchema(boolean recover) throws UpgradeException
   {
      System.out.println("Creating additional tables ...");

      for (int i = 0; i < NEW_TABLE_LIST.length; i++)
      {
         TableInfo tableInfo = NEW_TABLE_LIST[i];
         try
         {
            tableInfo.create(item);
         }
         catch (SQLException e)
         {
            String message = "Failed creating table " + tableInfo.getTableName() + ": "
                  + e.getMessage();
            System.out.println(message);
            trace.warn(message, e);
         }
      }

      System.out.println("Dropping obsolete tables ...");

      for (int i = 0; i < DROP_TABLE_LIST.length; i++)
      {
         TableInfo tableInfo = DROP_TABLE_LIST[i];
         try
         {
            tableInfo.drop(item);
         }
         catch (SQLException e)
         {
            String message = "Failed dropping table " + tableInfo.getTableName() + ": "
                  + e.getMessage();
            System.out.println(message);
            trace.warn(message, e);
         }
      }

      trace.info("Updating indexes.");

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE UNIQUE INDEX MESSAGE_STORE_IDX1 "
               + "ON MESSAGE_STORE(oid)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index MESSAGE_STORE_IDX1 on attribute "
               + "oid of table MESSAGE_STORE. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX MESSAGE_STORE_IDX2 "
               + "ON MESSAGE_STORE(source)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index MESSAGE_STORE_IDX1 on attribute "
               + "source of table MESSAGE_STORE. Message: " + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }

      try
      {
         DatabaseHelper.executeDdlStatement(item, "CREATE INDEX MESSAGE_STORE_IDX3 "
               + "ON MESSAGE_STORE(arrivalTime, rCount)");
      }
      catch (SQLException e)
      {
         String message = "Couldn't create index MESSAGE_STORE_IDX3 on attributes "
               + "arrivalTime and rCount of table MESSAGE_STORE. Message: "
               + e.getMessage();
         System.out.println(message);
         trace.warn("", e);
      }
   }

   protected void migrateData(boolean recover) throws UpgradeException
   {
      // fill the data table
      Iterator oidItr = getPre30ModelOIDs();
      while (oidItr.hasNext())
      {
         long modelOID = ((Long) oidItr.next()).longValue();
         ModelItem model = retrieveModelFromAuditTrail(modelOID);
         if (model != null)
         {
            try
            {
               DatabaseHelper.executeUpdate(item, "DELETE DATA WHERE model=" + modelOID);
            }
            catch (SQLException e)
            {
               trace.warn(e);
               throw new UpgradeException("Unable to prepare DATA table for upgrade: "
                     + e.getMessage());
            }
            NodeList modelChildren = model.getModelElement().getChildNodes();
            for (int i = 0; i < modelChildren.getLength(); i++)
            {
               Node el = modelChildren.item(i);
               if (el.getNodeType() == Node.ELEMENT_NODE && el.getNodeName().equals("DATA"))
               {
                  Element element = (Element) el;

                  String oid = element.getAttribute("oid");
                  String id = element.getAttribute("id");
                  String name = element.getAttribute("name");
                  String description = "";
                  NodeList dataChildren = element.getChildNodes();
                  for (int j = 0; j < dataChildren.getLength(); j++)
                  {
                     Node eel = dataChildren.item(j);
                     if (eel.getNodeType() == Node.ELEMENT_NODE && eel.getNodeName()
                           .equals("DESCRIPTION"))
                     {
                        Node textel = eel.getFirstChild();
                        if (textel instanceof Text)
                        {
                           description = ((Text) textel).getData();
                        }
                     }
                  }
                  try
                  {
                     DatabaseHelper.executeUpdate(item,
                           "INSERT INTO DATA (oid, id, model, name, description) VALUES ("
                           + oid + ", "
                           + "'" + org.eclipse.stardust.common.StringUtils.cutString(id, 50) + "', "
                           + modelOID + ", "
                           + "'" + org.eclipse.stardust.common.StringUtils.cutString(name,100) + "', "
                           + "'" + org.eclipse.stardust.common.StringUtils.cutString(description, 4000) + "')");
                  }
                  catch (SQLException e)
                  {
                     String message = "Error inserting column into DATA table with " +
                           "oid = " + oid + ". Message: " + e.getMessage();
                     System.out.println(message);
                     trace.warn(message, e);
                  }
               }
            }
         }
      }
   }

   protected void finalizeSchema(boolean recover) throws UpgradeException
   {
   }

   public Version getVersion()
   {
      return new Version(2, 5, 3);
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
