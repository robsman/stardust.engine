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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.Function;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeOidRegistry;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeInfo.ModelType;
import org.xml.sax.SAXException;

/**
 * The base class for all model upgrade jobs.
 *
 * TODO: (fh)
 *  - calls to addRole and addPrimitiveData from sub classes should only record the change request.
 *  - actual action should be performed as a block in the run method.
 *  - updating of the model string should be performed as transformation: string -> sax source with (custom xml reader) -> identity transformer -> string
 *  - the custom xml reader should inject/filter the changes recorded at step 1.
 *  - updating of the runtime should read (once) the relevant content from the tables and fill in the runtime oid registry, then inject the new model elements.
 *
 * @see UpgradeJob
 * @author Florin.Herinean
 * @version $Revision$
 */
public abstract class ModelUpgradeJob extends UpgradeJob
{
   private static final String FIELD__OID = "oid";
   private static final String FIELD__MODEL = "model";
   private static final String FIELD__ID = "id";
   private static final String FIELD__NAME = "name";
   private static final String FIELD__DESCRIPTION = "description";
   private static final String FIELD__PARTITION = "partition";

   private static final String MODEL_TABLE_NAME = "model";
   private static final String DATA_TABLE_NAME = "data";
   private static final String PARTICIPANT_TABLE_NAME = "participant";

   @Override
   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      if (item instanceof ModelItem)
      {
         try
         {
            updateVersionInModel((ModelItem) item);
         }
         catch (Exception e)
         {
            throw new UpgradeException(e);
         }
      }
      item.setVersion(getVersion());
      return item;
   }

   private void updateVersionInModel(ModelItem item) throws JAXBException, SAXException
   {
      int ix = -1;
      String model = item.getModel();
      switch (item.getUpgradeInfo().getType())
      {
      case cwm:
         ix = model.indexOf("carnotVersion=\"");
         break;
      case xpdl:
         ix = model.indexOf("CarnotVersion=\"");
      }
      if (ix >= 0)
      {
         int start = ix + 15;
         int end = model.indexOf('"', start + 1);
         model = model.substring(0, start) + getVersion().toString() + model.substring(end);
         item.setModel(model);
      }
   }

   protected void addRole(ModelUpgradeInfo info, ModelItem modelItem, String id, String name) throws SQLException, JAXBException, SAXException
   {
      insertRoleInModel(info, modelItem, id, name);
      insertInRuntime(modelItem, id, name, PARTICIPANT_TABLE_NAME, new Pair<String, Object>("type", 0));
   }

   protected void addPrimitiveData(ModelUpgradeInfo info, ModelItem modelItem, String id, String name, Type type) throws SQLException, JAXBException, SAXException
   {
      insertPrimitiveDataInModel(info, modelItem, id, name, type);
      insertInRuntime(modelItem, id, name, DATA_TABLE_NAME);
   }

   protected void changePrimitiveDataType(ModelUpgradeInfo info, ModelItem modelItem, String id, Type type)
   {
      if (info.hasData(id))
      {
         FileModel modifier = FileModel.create(modelItem.getModel());
         modifier.setPrimitiveDataType(id, type);
         modelItem.setModel(modifier.toString());
      }
   }

   private void insertRoleInModel(ModelUpgradeInfo info, ModelItem modelItem, String id,
         String name)
   {
      if (!info.hasParticipant(id))
      {
         String model = modelItem.getModel();
         int ix = findInsertionPoint(model, getInsertRoleAfterTag(info.getType()));
         String def = getRoleDefinition(info.getType())
               .replace("$oid", Long.toString(info.getNextOid()))
               .replace("$id", id)
               .replace("$name", name)
               .replace("$desc", name + '.');
         model = new StringBuilder(model).insert(ix, def).toString();
         modelItem.setModel(model);
      }
   }

   protected void insertInRuntime(ModelItem modelItem, String id, String name, String tableName, Pair<String, Object>... extraColumns)
         throws SQLException, JAXBException, SAXException
   {
      RuntimeItem runtimeItem = modelItem.getRuntimeItem();
      long modelOid = modelItem.getOid();

      if (runtimeItem != null && modelOid != Long.MIN_VALUE
            && !Parameters.instance().getBoolean(Upgrader.UPGRADE_DRYRUN, false))
      {
         Statement statement = null;
         try
         {
            statement = runtimeItem.getConnection().createStatement();

            HashMap<Long, Long> oids = getExistingRuntimeOids(id, tableName, statement);
            if (!oids.containsKey(modelOid))
            {
               long nextOid = 0;
               short partitionOid = getPartition(modelOid, statement);
               String modelId = modelItem.getUpgradeInfo().getId();
               Set<Long> modelHierarchy = getModelHierarchy(partitionOid, modelId, statement);
               nextOid = findExistingRuntimeOidInHierarchy(oids, modelHierarchy, partitionOid);
               if (nextOid == 0)
               {
                  nextOid = getNextRuntimeOidInHierarchy(partitionOid, modelId, tableName, statement);
                  if (nextOid == 0)
                  {
                     nextOid = RuntimeOidRegistry.firstOidInPartition(partitionOid);
                  }
               }
               String extraNames = "";
               String extraValues = "";
               if (extraColumns != null)
               {
                  StringBuilder n = new StringBuilder();
                  StringBuilder v = new StringBuilder();
                  for (Pair<String, Object> column : extraColumns)
                  {
                     n.append(", ").append(column.getFirst());
                     Object value = column.getSecond();
                     if (value instanceof String)
                     {
                        v.append(", '").append(value).append("'");
                     }
                     else
                     {
                        v.append(", ").append(value);
                     }
                  }
                  extraNames = n.toString();
                  extraValues = v.toString();
               }

               statement.executeUpdate("INSERT INTO " + DatabaseHelper.getQualifiedName(tableName)
                     + " (" + FIELD__OID + ", " + FIELD__MODEL + ", " + FIELD__ID + ", " + FIELD__NAME + ", " + FIELD__DESCRIPTION + extraNames + ")"
                     + " VALUES (" + nextOid + ", " + modelOid + ", '" + id + "', '" + name + "', '" + name + ".'" + extraValues + ")");
            }
         }
         finally
         {
            QueryUtils.closeStatement(statement);
         }
      }
   }

   private Set<Long> getModelHierarchy(short partitionOid, String id, Statement statement) throws SQLException
   {
      ResultSet rs = null;
      HashSet<Long> oids = new HashSet<Long>();
      try
      {
         rs = statement.executeQuery("SELECT " + FIELD__OID
            + " FROM " + DatabaseHelper.getQualifiedName(MODEL_TABLE_NAME)
            + " WHERE " + FIELD__PARTITION + "=" + partitionOid + " AND " + FIELD__ID + "='" + id + "'");
         while (rs.next())
         {
            oids.add(rs.getLong(1));
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      return oids;
   }

   protected long findExistingRuntimeOidInHierarchy(HashMap<Long, Long> oids, Set<Long> modelHierarchy, short partitionOid)
   {
      for (Long modelOid : modelHierarchy)
      {
         Long runtimeOid = oids.get(modelOid);
         if (runtimeOid != null)
         {
            System.err.println("Found existing oid: " + runtimeOid);
            return runtimeOid;
         }
      }
      return 0;
   }

   protected long getNextRuntimeOidInHierarchy(short partitionOid, String modelId, String tableName, Statement statement)
         throws SQLException
   {
      ResultSet rs = null;
      try
      {
         rs = statement.executeQuery("SELECT " + Function.MAX + "(" + FIELD__OID + ")"
               + " FROM " + DatabaseHelper.getQualifiedName(tableName)
               + " WHERE " + FIELD__MODEL + " IN ( "
                     + "SELECT " + FIELD__OID
                     + " FROM " + DatabaseHelper.getQualifiedName(MODEL_TABLE_NAME)
                     + " WHERE " + FIELD__PARTITION + "=" + partitionOid + ")");
         if (rs.next())
         {
            return rs.getLong(1) + 1;
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      return 0;
   }

   protected short getPartition(long modelOid, Statement statement) throws SQLException
   {
      ResultSet rs = null;
      try
      {
         rs = statement.executeQuery("SELECT " + FIELD__PARTITION
               + " FROM " + DatabaseHelper.getQualifiedName(MODEL_TABLE_NAME)
               + " WHERE " + FIELD__OID + "=" + modelOid);
         if (rs.next())
         {
            return rs.getShort(1);
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      throw new UpgradeException("Unable to find partition for model with oid: " + modelOid);
   }

   private HashMap<Long, Long> getExistingRuntimeOids(String id, String tableName,
         Statement statement) throws SQLException
   {
      ResultSet rs = null;
      HashMap<Long, Long> oids = new HashMap<Long, Long>();
      try
      {
         rs = statement.executeQuery("SELECT " + FIELD__OID + "," + FIELD__MODEL
            + " FROM " + DatabaseHelper.getQualifiedName(tableName)
            + " WHERE " + FIELD__ID + "='" + id + "'");
         while (rs.next())
         {
            oids.put(rs.getLong(2), rs.getLong(1));
         }
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      return oids;
   }

   private void insertPrimitiveDataInModel(ModelUpgradeInfo info, ModelItem modelItem,
         String id, String name, Type type)
   {
      if (!info.hasData(id))
      {
         String model = modelItem.getModel();
         int ix = findInsertionPoint(model, getInsertDataAfterTag(info.getType()));
         String def = getPrimitiveDataDefinition(info.getType(), type)
               .replace("$oid", Long.toString(info.getNextOid()))
               .replace("$id", id)
               .replace("$name", name)
               .replace("$desc", name + '.');
         model = new StringBuilder(model).insert(ix, def).toString();
         modelItem.setModel(model);
      }
   }

   private String getXpdlBasicType(Type type)
   {
      if (Type.Timestamp == type)
      {
         return "DATETIME";
      }
      else if (Type.Calendar == type)
      {
         return "DATETIME";
      }
      // TODO: (fh) support other conversions
      return "";
   }

   private String getRoleDefinition(ModelType modelType)
   {
      switch (modelType)
      {
      case cwm: return cwmRoleDefinition;
      case xpdl: return xpdlRoleDefinition;
      }
      return null;
   }

   private String getInsertRoleAfterTag(ModelType modelType)
   {
      switch (modelType)
      {
      case cwm: return "role";
      case xpdl: return "Participant";
      }
      return null;
   }

   private String getPrimitiveDataDefinition(ModelType modelType, Type type)
   {
      switch (modelType)
      {
      case cwm: return cwmDataDefinition.replace("$type", type.toString());
      case xpdl: return xpdlDataDefinition.replace("$type", type.toString()).replace("$basicType", getXpdlBasicType(type));
      }
      return null;
   }

   private String getInsertDataAfterTag(ModelType modelType)
   {
      switch (modelType)
      {
      case cwm: return "data";
      case xpdl: return "DataField";
      }
      return null;
   }

   private int findInsertionPoint(String model, String tag)
   {
      int result = -1;
      int ix = findEndTag(model, tag, 0);
      while (ix >= 0)
      {
         result = model.indexOf('>', ix + 1) + 1;
         ix = findEndTag(model, tag, result);
      }
      return result;
   }

   private int findEndTag(String model, String tag, int from)
   {
      while (true)
      {
         int ix = model.indexOf(tag, from);
         if (ix < 0)
         {
            return ix;
         }
         int end = model.indexOf('>', ix + tag.length());
         if (end < 0)
         {
            // nothing found
            return end;
         }
         from = end + 1;
         if (end >= 0)
         {
            int start = model.lastIndexOf('<', ix - 1);
            if (start >= 0)
            {
               String tagContent = model.substring(start + 1, end).trim();
               if (tagContent.startsWith("/") || tagContent.endsWith("/"))
               {
                  char nextChar = model.charAt(ix + tag.length());
                  if (Character.isWhitespace(nextChar) || nextChar == '>' || nextChar == '/')
                  {
                     if (start + (tagContent.startsWith("/") ? 2 : 1) == ix || model.charAt(ix - 1) == ':')
                     {
                        return start;
                     }
                  }
               }
            }
         }
      }
   }

   private static final String xpdlRoleDefinition =
         "<Participant Id=\"$id\" Name=\"$name\">"
       +    "<ParticipantType Type=\"ROLE\"/>/>"
       +    "<Description>$desc</Description>"
       +    "<ExtendedAttributes>"
       +        "<ExtendedAttribute Name=\"CarnotExt\">"
       +            "<carnot:Role Oid=\"$oid\"/>"
       +        "</ExtendedAttribute>"
       +    "</ExtendedAttributes>"
       + "</Participant>";

   private static final String cwmRoleDefinition =
         "<role oid=\"$oid\" id=\"$id\" name=\"$name\">"
       +    "<description>$desc</description>"
       + "</role>";

   private static final String xpdlDataDefinition =
         "<DataField Id=\"$id\" Name=\"$name\" IsArray=\"FALSE\">"
       +    "<DataType>"
       +        "<BasicType Type=\"$basicType\"/>"
       +    "</DataType>"
       +    "<Description>$desc</Description>"
       +    "<ExtendedAttributes>"
       +        "<ExtendedAttribute Name=\"CarnotExt\">"
       +            "<carnot:DataField Oid=\"$oid\" Type=\"primitive\" IsPredefined=\"true\">"
       +                "<carnot:Attributes>"
       +                    "<carnot:Attribute Name=\"carnot:engine:type\" Value=\"$type\" Type=\"org.eclipse.stardust.engine.core.pojo.data.Type\"/>"
       +                "</carnot:Attributes>"
       +            "</carnot:DataField>"
       +        "</ExtendedAttribute>"
       +    "</ExtendedAttributes>"
       + "</DataField>";

   private static final String cwmDataDefinition =
         "<data oid=\"$oid\" id=\"$id\" name=\"$name\" predefined=\"true\" type=\"primitive\">"
       +    "<attribute name=\"carnot:engine:type\" type=\"org.eclipse.stardust.engine.core.pojo.data.Type\" value=\"$type\"/>"
       +    "<description>$desc</description>"
       + "</data>";
}
