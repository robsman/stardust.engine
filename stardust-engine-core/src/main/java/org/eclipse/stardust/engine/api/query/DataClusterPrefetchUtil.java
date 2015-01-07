/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.engine.api.query;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataSlot;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;

public class DataClusterPrefetchUtil
{
   private static final Logger trace = LogManager.getLogger(DataClusterPrefetchUtil.class);

   
   public static List<StructuredDataPrefetchInfo> getPrefetchInfo(IData structuredData, String xPathExpression)
   {
      if(!canPrefetchStructuredDataFromDataCluster(structuredData, xPathExpression))
      {
         return new ArrayList();
      }
      
      
      String xPath = xPathExpression;
      //workaround for bug in the modeler when descriptor path is left empty / cleared
      if(xPath == null)
      {
         xPath = "";
      }
      
      DataCluster[] clusters = RuntimeSetup.instance().getDataClusterSetup();
      IXPathMap xPathMap = DataXPathMap.getXPathMap(structuredData);
      TypedXPath typedXPath = xPathMap.getXPath(xPath);
      
      List<StructuredDataPrefetchInfo> prefetchInfo = new ArrayList<StructuredDataPrefetchInfo>();
      collectPrefetchInfo(clusters, prefetchInfo, structuredData, xPathMap, typedXPath);
      
      return prefetchInfo;
   }
      
   public static boolean canPrefetchStructuredDataFromDataCluster(IData structuredData, String xPathExpression)
   {
      String xPath = xPathExpression;
      //workaround for bug in the modeler when descriptor path is left empty / cleared
      if(xPath == null)
      {
         xPath = "";
      }
      
      boolean useDataClusters = Parameters.instance().getBoolean(
            KernelTweakingProperties.DESCRIPTOR_PREFETCH_USE_DATACLUSTER, false);
      DataCluster[] clusters = RuntimeSetup.instance().getDataClusterSetup();
      if (useDataClusters && clusters.length > 0)
      {
         IXPathMap xPathMap = DataXPathMap.getXPathMap(structuredData);
         TypedXPath typedXPath = xPathMap.getXPath(xPath);
      
         if(typedXPath != null)
         {
            try {
               assertIsNotListType(typedXPath);
               checkParentXPath(typedXPath);
               checkChildXPath(clusters, structuredData, typedXPath);
                   
               //primitive type: check if mapped in cluster
               if(StructuredDataXPathUtils.isPrimitiveType(typedXPath))
               {
                  assertIsMapped(clusters, structuredData, typedXPath);
               }
               
               return true;
            }
            catch(IllegalArgumentException e)
            {
               if(trace.isInfoEnabled())
               {
                  trace.info(e);
               }
            }
         }
      }
      
      return false;
   }
   
   private static void collectPrefetchInfo(DataCluster[] clusters, List<StructuredDataPrefetchInfo> collection, IData structuredData, IXPathMap xPathMap, TypedXPath typedXPath)
   {
      if(StructuredDataXPathUtils.isPrimitiveType(typedXPath))
      {
         Pair<DataCluster, DataSlot> clusterAndSlot = getClusterAndSlot(clusters, structuredData, typedXPath);
         long xPathOid = xPathMap.getXPathOID(typedXPath.getXPath());
         
         StructuredDataPrefetchInfo prefetchInfo 
            = new StructuredDataPrefetchInfo(xPathOid, clusterAndSlot.getFirst(), clusterAndSlot.getSecond());
         collection.add(prefetchInfo);
      }  
      
      List<TypedXPath> childs = typedXPath.getChildXPaths();
      for(TypedXPath child: childs)
      {
         collectPrefetchInfo(clusters, collection, structuredData, xPathMap, child);
      } 
   }
  
   private static void checkChildXPath(DataCluster[] clusters, IData structuredData, TypedXPath typedXPath)
   {
      List<TypedXPath> childs = typedXPath.getChildXPaths();
      for(TypedXPath child: childs)
      {
         assertIsNotListType(child);
         //primitive types must be mapped in cluster
         if(StructuredDataXPathUtils.isPrimitiveType(child))
         {
            assertIsMapped(clusters, structuredData, child); 
         }
         checkChildXPath(clusters, structuredData, child);
      }
   }
   
   private static void checkParentXPath(TypedXPath typedXPath)
   {
      TypedXPath parent = typedXPath.getParentXPath();
      if(parent != null)
      {
         assertIsNotListType(parent);
         checkParentXPath(parent);
      }
   }
   
   private static void assertIsNotListType(TypedXPath typedXPath) throws IllegalArgumentException
   {
      StringBuffer errorMsg = new StringBuffer();
      errorMsg.append("Invalid XPath '");
      errorMsg.append(typedXPath.getXPath());
      errorMsg.append("' detected");
      
      if(typedXPath.isList() || typedXPath.isEnumeration())
      {
         errorMsg.append(" List typed is not supported");
         throw new IllegalArgumentException(errorMsg.toString());
      }
   }
   
   private static void assertIsMapped(DataCluster[] clusters, IData structuredData, TypedXPath typedXPath) throws IllegalArgumentException
   {
      DataSlot ds = getDataSlot(clusters, structuredData, typedXPath);
      if(ds == null)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Invalid XPath '");
         errorMsg.append(typedXPath.getXPath());
         errorMsg.append("' detected");
         errorMsg.append(" xpath is not mapped in the datacluster");
         throw new IllegalArgumentException(errorMsg.toString());
      }
   }
   
   private static DataSlot getDataSlot(DataCluster[] clusters, IData structuredData, TypedXPath typedXPath)
   {
      Pair<DataCluster, DataSlot> clusterAndSlot = getClusterAndSlot(clusters, structuredData, typedXPath);
      if(clusterAndSlot != null)
      {
         return clusterAndSlot.getSecond();
      }
      
      return null;
   }
   
   private static Pair<DataCluster, DataSlot> getClusterAndSlot(DataCluster[] clusters, IData structuredData, TypedXPath typedXPath)
   {
      String fullDataId = getFullQualifiedId(structuredData);
      for(DataCluster dc: clusters)
      {
         DataSlot ds = dc.getSlot(fullDataId, typedXPath.getXPath());
         if(ds != null)
         {
            Pair<DataCluster, DataSlot> pair = new Pair<DataCluster, DataSlot>(dc, ds);
            return pair;
         }
      }
      
      return null;
   }
   
   private static String getFullQualifiedId(IData structuredData)
   {
      String id = structuredData.getId();
      String modelId = structuredData.getModel().getId();
      
      //check if already qualified
      QName fullQualifiedId = QName.valueOf(id);
      if(!modelId.equals(fullQualifiedId.getNamespaceURI()))
      {
         fullQualifiedId = new QName(modelId, id);
      }
      
      return fullQualifiedId.toString();
   }
   
   public static class StructuredDataPrefetchInfo
   {
      private final DataSlot dataslot;
      private final DataCluster cluster;
      private final long xpathOid;

      public StructuredDataPrefetchInfo(long xpathOid, DataCluster cluster, DataSlot dataslot)
      {
         this.xpathOid = xpathOid;
         this.cluster = cluster;
         this.dataslot = dataslot;
      }

      public DataSlot getDataslot()
      {
         return dataslot;
      }

      public DataCluster getCluster()
      {
         return cluster;
      }

      public long getXpathOid()
      {
         return xpathOid;
      }
   }
   
   public static class StructuredDataEvaluaterInfo
   {
      public static final String EXTENDED_EVALUATOR_INFO = "EXTENDED_EVALUATOR_INFO";
      
      private final String xpath;
      private final IData data;
      private final Class< ? extends ExtendedAccessPathEvaluator> evaluatorClass;

      public StructuredDataEvaluaterInfo(IData data, String xpath, Class<? extends ExtendedAccessPathEvaluator> evaluatorClass)
      {
         this.data = data;
         this.xpath = xpath;
         this.evaluatorClass = evaluatorClass;   
      }

      public String getXpath()
      {
         return xpath;
      }

      public IData getData()
      {
         return data;
      }

      public Class< ? extends ExtendedAccessPathEvaluator> getEvaluatorClass()
      {
         return evaluatorClass;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((data == null) ? 0 : data.hashCode());
         result = prime * result
               + ((evaluatorClass == null) ? 0 : evaluatorClass.hashCode());
         result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         StructuredDataEvaluaterInfo other = (StructuredDataEvaluaterInfo) obj;
         if (data == null)
         {
            if (other.data != null)
               return false;
         }
         else if (!data.equals(other.data))
            return false;
         if (evaluatorClass == null)
         {
            if (other.evaluatorClass != null)
               return false;
         }
         else if (!evaluatorClass.equals(other.evaluatorClass))
            return false;
         if (xpath == null)
         {
            if (other.xpath != null)
               return false;
         }
         else if (!xpath.equals(other.xpath))
            return false;
         return true;
      }
   }
}
