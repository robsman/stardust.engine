package org.eclipse.stardust.engine.core.upgrade.utils.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeItem;

public class ModelXmlLoader
{
   private final String MODEL_CONTENT_TABLE = "string_data";

   private RuntimeItem runtimeItem;
   private static ModelXmlLoader instance;
   private Map<Long, List<String>> modelXmlCache = new HashMap<Long, List<String>>();

   public static ModelXmlLoader getInstance(RuntimeItem runtimeItem)
   {
      if(instance == null)
      {
         instance = new ModelXmlLoader(runtimeItem);
      }

      return instance;
   }


   private ModelXmlLoader(RuntimeItem runtimeItem)
   {
      this.runtimeItem = runtimeItem;
      try
      {
         loadModelIntoCache();
      }
      catch(SQLException e)
      {
         throw new RuntimeException("Exception occured during trying to load model content from auditrail.", e);
      }


   }

   private void loadModelIntoCache() throws SQLException
   {
      StringBuffer modelContentStatement = new StringBuffer();
      modelContentStatement.append("Select objectid, data from ");
      modelContentStatement.append(DatabaseHelper.getQualifiedName(MODEL_CONTENT_TABLE));
      modelContentStatement.append(" where data_type = 'model'");
      modelContentStatement.append(" order by oid asc");

      ResultSet rs = DatabaseHelper.executeQuery(runtimeItem, modelContentStatement.toString());
      while(rs.next())
      {
         Long modelOid = rs.getLong(1);
         String modelPart = rs.getString(2);

         List<String> modelParts = modelXmlCache.get(modelOid);
         if(modelParts == null)
         {
            modelParts = new ArrayList<String>();
            modelXmlCache.put(modelOid, modelParts);
         }

         modelParts.add(modelPart);
      }
   }

   public Set<Long> getModelOids()
   {
      return modelXmlCache.keySet();
   }

   public String getModelXml(long modelOid)
   {
      List<String> modelPartsList = modelXmlCache.get(modelOid);
      StringBuffer modelXml = new StringBuffer();
      for(String s: modelPartsList)
      {
         modelXml.append(s);
      }

      return modelXml.toString();
   }
}
