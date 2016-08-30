/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.PropertyPersistor;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster.DataClusterEnableState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class RuntimeSetup implements XMLConstants
{
   private static final Logger trace = LogManager.getLogger(RuntimeSetup.class);

   private static final DataCluster[] EMPTY_DATA_CLUSTER_ARRAY = new DataCluster[0];

   private DataCluster[] clusters = EMPTY_DATA_CLUSTER_ARRAY;

   public static final String RUNTIME_SETUP_PROPERTY = "org.eclipse.stardust.engine.core.runtime.setup";

   public static final String PRE_STARDUST_RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION = "ag.carnot.workflow.runtime.setup_definition";
   public static final String RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION = "org.eclipse.stardust.engine.core.runtime.setup_definition";
   public static final String ENABLED_FOR_PI_STATE = "";


   public static RuntimeSetup instance()
   {
      RuntimeSetup setup = (RuntimeSetup) Parameters.instance().get(
            RUNTIME_SETUP_PROPERTY);

      if (null == setup)
      {
         setup = new RuntimeSetup();
         setup.loadFromDataBase();
         Parameters.instance().set(RUNTIME_SETUP_PROPERTY, setup);
      }

      return setup;
   }

   public boolean hasDataClusterSetup()
   {
      return clusters != null && clusters.length > 0;
   }

   public DataCluster[] getDataClusterSetup()
   {
      return clusters;
   }

   private void loadFromDataBase()
   {
      PropertyPersistor prop = PropertyPersistor
         .findByName(RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
      //try to load old style definition as fallback
      if(prop == null)
      {
         prop = PropertyPersistor.findByName(PRE_STARDUST_RUNTIME_SETUP_PROPERTY_CLUSTER_DEFINITION);
      }

      if (null != prop)
      {
         String xml = LargeStringHolder.getLargeString(prop.getOID(),
               PropertyPersistor.class);
         trace.debug("Data cluster configuration bootstrapped from audit trail.");
         parse(xml);
      }
   }

   private static Element getFirstElement(Document doc, String tagName)
   {
      NodeList nodeList = doc.getElementsByTagName(tagName);
      if (0 < nodeList.getLength())
      {
         if (1 < nodeList.getLength())
         {
            trace.warn("Ignoring all but first <" + tagName + "> elements");
         }
         return (Element) nodeList.item(0);
      }

      return null;
   }

   private static Element getFirstElement(Element element, String tagName)
   {
      NodeList nodeList = element.getElementsByTagName(tagName);
      if (0 < nodeList.getLength())
      {
         if (1 < nodeList.getLength())
         {
            trace.warn("Ignoring all but first <" + tagName + "> elements");
         }
         return (Element) nodeList.item(0);
      }

      return null;
   }

   protected void parse(String xml)
   {
      if(StringUtils.isEmpty(xml))
      {
         return;
      }

      List<DataCluster> parsedClusters = new ArrayList();

      try
      {
         trace.debug("Data cluster configuration bootstrapped from audit trail.");

         DocumentBuilder domBuilder = new RuntimeSetupDocumentBuilder();
         Document setup = domBuilder.parse(new InputSource(new StringReader(xml)));

         Element runtimeSetup = getFirstElement(setup, RUNTIME_SETUP);
         if(runtimeSetup != null)
         {
            Element auditTrail = getFirstElement(runtimeSetup, AUDIT_TRAIL);
            if(auditTrail != null)
            {
               Element dataClusters = getFirstElement(auditTrail, DATA_CLUSTERS);
               if(dataClusters != null)
               {
                  final NodeList dcNodes = dataClusters.getElementsByTagName(DATA_CLUSTER);
                  for (int dcIdx = 0; dcIdx < dcNodes.getLength(); ++dcIdx)
                  {
                     final Element dcNode = (Element) dcNodes.item(dcIdx);

                     String dcTableName = dcNode.getAttribute(DATA_CLUSTER_TABNAME_ATT);

                     List<DataSlot> dataSlots = getDataSlots(dcNode, dcTableName);
                     List<DescriptorSlot> descriptorSlots = getDescriptorSlots(dcNode, dcTableName);
                     List<DataClusterIndex> indexes = getClusterIndexes(dcNode, dcTableName);

                     Parameters params = Parameters.instance();
                     String schemaName = params.getString(Session.KEY_AUDIT_TRAIL_SCHEMA);

                     parsedClusters.add(new DataCluster(schemaName, dcTableName,
                           dcNode.getAttribute(DATA_CLUSTER_PICOLUMN_ATT),
                           dataSlots.toArray(new DataSlot[0]),
                           descriptorSlots.toArray(new DescriptorSlot[0]),
                           indexes.toArray(new DataClusterIndex[0]),
                           getEnableStates(dcNode)));
                  }
               }
            }
         }

         this.clusters = (DataCluster[]) parsedClusters.toArray(EMPTY_DATA_CLUSTER_ARRAY);
      }
      catch (SAXException e)
      {
         throw new PublicException(
               BpmRuntimeError.BPMRT_INVALID_RUNTIME_SETUP_CONFIGURATION.raise(), e);
      }
      catch (IOException e)
      {
         throw new InternalException(
               "Cannot read runtime setup configuration from String.", e);
      }
   }

   private List<DataClusterIndex> getClusterIndexes(final Element dcNode, String dcTableName)
   {
      List<DataClusterIndex> indexes = CollectionUtils.newArrayList();

      Element dataClusterIndexes = getFirstElement(dcNode, DATA_CLUSTER_INDEXES);
      if(dataClusterIndexes != null)
      {
         NodeList dciNodes = dataClusterIndexes.getElementsByTagName(DATA_CLUSTER_INDEX);
         for (int dciIdx = 0; dciIdx < dciNodes.getLength(); ++dciIdx)
         {
            Element dciNode = (Element) dciNodes.item(dciIdx);

            NodeList cnNodes = dciNode
                  .getElementsByTagName(DATA_CLUSTER_INDEX_COLUMN);
            List<String> columnNames = new ArrayList(cnNodes.getLength());
            for (int cnIdx = 0; cnIdx < cnNodes.getLength(); ++cnIdx)
            {
               Element cnNode = (Element) cnNodes.item(cnIdx);
               columnNames.add(cnNode.getAttribute(INDEX_COLUMN_NAME_ATT));
            }

            indexes.add(new DataClusterIndex(dcTableName, dciNode
                  .getAttribute(INDEX_NAME_ATT), StringUtils.getBoolean(
                  dciNode.getAttribute(INDEX_UNIQUE_ATT), false),
                  columnNames));
         }
      }

      return indexes;
   }

   private List<DataSlot> getDataSlots(final Element dcNode, String dcTableName)
   {
      List<DataSlot> dataSlots = CollectionUtils.newArrayList();

      Element dataSlotsElemet = getFirstElement(dcNode, DATA_SLOTS);
      if(dataSlotsElemet != null)
      {
         NodeList dsNodes = dataSlotsElemet.getElementsByTagName(DATA_SLOT);
         for (int dsIdx = 0; dsIdx < dsNodes.getLength(); ++dsIdx)
         {
            Element dsNode = (Element) dsNodes.item(dsIdx);

            try
            {
               ClusterSlotData csd = getClusterSlotData(dsNode);

               String ignorePreparedStatementsAttr = dsNode
                     .getAttribute(DATA_SLOT_IGNORE_PREPARED_STATEMENTS_ATT);
               boolean ignorePreparedStatements = Boolean
                     .valueOf(ignorePreparedStatementsAttr);
               String nValueColumn = dsNode
                     .getAttribute(DATA_SLOT_NVALCOLUMN_ATT);
               String sValueColumn = dsNode
                     .getAttribute(DATA_SLOT_SVALCOLUMN_ATT);
               String dValueColumn = dsNode
                     .getAttribute(DATA_SLOT_DVALCOLUMN_ATT);
               if (StringUtils.isNotEmpty(nValueColumn)
                     && StringUtils.isNotEmpty(sValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_SINGLE_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_SVALUECOLUMN_AND_NVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isNotEmpty(nValueColumn)
                     && StringUtils.isNotEmpty(dValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_NUMERIC_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_NVALUECOLUMN_AND_DVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isEmpty(sValueColumn)
                     && StringUtils.isNotEmpty(dValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_DATA_SLOT_MUST_NOT_CONTAIN_STORAGE_TYPE_DVALUECOLUMN_WITHOUT_STORAGE_TYPE_SVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isNotEmpty(sValueColumn)
                     && StringUtils.isEmpty(dValueColumn))
               {
                  trace.info(MessageFormat
                        .format(
                              "Data slot for modelId: {0}, dataId: {1}, attribute: {2} "
                                    + "does define a string value column but no double value column. "
                                    + "Sorting for these data might be performed lexically even if it contains numeric values",
                              new Object[] {csd.getModelId(), csd.getDataId(), csd.getAttributeName()}));
               }
               dataSlots.add(new DataSlot(csd, dsNode
                     .getAttribute(DATA_SLOT_OIDCOLUMN_ATT), dsNode
                     .getAttribute(DATA_SLOT_TYPECOLUMN_ATT),
                     nValueColumn, sValueColumn, dValueColumn,
                     ignorePreparedStatements));

            }
            catch (InvalidClusterSlotDataException x)
            {
               trace.warn("Will ignore data slot for cluster table: "
                     + dcTableName + ". " + x.getMessage());

            }
         }
      }

      return dataSlots;
   }

   private List<DescriptorSlot> getDescriptorSlots(final Element dcNode, String dcTableName)
   {
      List<DescriptorSlot> descriptorSlots = CollectionUtils.newArrayList();

      Element descrSlotsElement = getFirstElement(dcNode, DESCRIPTOR_SLOTS);
      if(descrSlotsElement != null)
      {
         NodeList dsNodes = descrSlotsElement.getElementsByTagName(DESCRIPTOR_SLOT);
         for (int dsIdx = 0; dsIdx < dsNodes.getLength(); ++dsIdx)
         {
            Element dsNode = (Element) dsNodes.item(dsIdx);

            try
            {
               Set<ClusterSlotData> csd = getClusterSlotDatas(dsNode);

               String descriptorId = dsNode
                     .getAttribute(DESCRIPTOR_SLOT_DESCR_ID_ATT);
               String ignorePreparedStatementsAttr = dsNode
                     .getAttribute(DATA_SLOT_IGNORE_PREPARED_STATEMENTS_ATT);
               boolean ignorePreparedStatements = Boolean
                     .valueOf(ignorePreparedStatementsAttr);
               String nValueColumn = dsNode
                     .getAttribute(DATA_SLOT_NVALCOLUMN_ATT);
               String sValueColumn = dsNode
                     .getAttribute(DATA_SLOT_SVALCOLUMN_ATT);
               String dValueColumn = dsNode
                     .getAttribute(DATA_SLOT_DVALCOLUMN_ATT);
               if (StringUtils.isNotEmpty(nValueColumn)
                     && StringUtils.isNotEmpty(sValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_SINGLE_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_SVALUECOLUMN_AND_NVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isNotEmpty(nValueColumn)
                     && StringUtils.isNotEmpty(dValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_NUMERIC_DATA_SLOT_MUST_NOT_CONTAIN_BOTH_STORAGES_TYPES_NVALUECOLUMN_AND_DVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isEmpty(sValueColumn)
                     && StringUtils.isNotEmpty(dValueColumn))
               {
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_A_DATA_SLOT_MUST_NOT_CONTAIN_STORAGE_TYPE_DVALUECOLUMN_WITHOUT_STORAGE_TYPE_SVALUECOLUMN
                              .raise());
               }
               if (StringUtils.isNotEmpty(sValueColumn)
                     && StringUtils.isEmpty(dValueColumn))
               {
                  trace.info(MessageFormat
                        .format(
                              "Descriptor slot with descriptorId: {0} "
                                    + "does define a string value column but no double value column. "
                                    + "Sorting for these data might be performed lexically even if it contains numeric values",
                              new Object[] {descriptorId}));
               }
               descriptorSlots.add(new DescriptorSlot(descriptorId, csd, dsNode
                     .getAttribute(DATA_SLOT_OIDCOLUMN_ATT), dsNode
                     .getAttribute(DATA_SLOT_TYPECOLUMN_ATT),
                     nValueColumn, sValueColumn, dValueColumn,
                     ignorePreparedStatements));

            }
            catch (InvalidClusterSlotDataException x)
            {
               trace.warn("Will ignore data slot for cluster table: "
                     + dcTableName + ". " + x.getMessage());

            }
         }
      }

      return descriptorSlots;
   }

   /**
    * @param dataNode a node of <data-slot> or <data>
    * @return
    */
   private static ClusterSlotData getClusterSlotData(Element dataNode)
   {
      String modelId = dataNode.getAttribute(DATA_SLOT_MODELID_ATT);
      String dataId = dataNode.getAttribute(DATA_SLOT_DATAID_ATT);
      String attributeName = dataNode.getAttribute(DATA_SLOT_ATTRIBUTENAME_ATT);

      if (StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(dataId))
      {
         return new ClusterSlotData(modelId, dataId, attributeName);
      }
      else
      {
         throw new InvalidClusterSlotDataException(modelId, dataId, attributeName);
      }
   }

   /**
    * @param dataNode a node of <descriptor-slot>
    * @return
    */
   private static Set<ClusterSlotData> getClusterSlotDatas(Element descrSlotNode)
   {
      Set<ClusterSlotData> csds = CollectionUtils.newSet();

      Element datasElement = getFirstElement(descrSlotNode, DATAS);
      if(datasElement != null)
      {
         NodeList dataNodes = datasElement.getElementsByTagName(DATA);
         for (int dsIdx = 0; dsIdx < dataNodes.getLength(); ++dsIdx)
         {
            Element dataNode = (Element) dataNodes.item(dsIdx);
            ClusterSlotData csd = getClusterSlotData(dataNode);
            csds.add(csd);
         }
      }

      return csds;
   }

   private Set<DataClusterEnableState> getEnableStates(Element dcNode)
   {
      Set<DataClusterEnableState> enableStates = new HashSet<DataClusterEnableState>();
      try
      {
         String attribute = dcNode.getAttribute(DATA_CLUSTER_ENABLED_PI_STATE);
         StringTokenizer st = new StringTokenizer(attribute, ",");
         while(st.hasMoreTokens())
         {
            String token = st.nextToken();
            DataClusterEnableState enableState = getEnableState(token);
            if(enableState != null)
            {
               enableStates.add(enableState);
            }
         }
      }
      catch(Exception ignored)
      {}

      if(enableStates.isEmpty())
      {
         enableStates.add(DataClusterEnableState.ALL);
      }
      return enableStates;
   }

   private DataClusterEnableState getEnableState(String s)
   {
      try
      {
         String enableString = s.trim().replace(" ", "");
         enableString = enableString.toUpperCase();
         return DataClusterEnableState.valueOf(enableString);
      }
      catch(Exception ignored)
      {}

      return null;
   }

   private static class InvalidClusterSlotDataException extends InternalException
   {
      private static final long serialVersionUID = 1L;

      public InvalidClusterSlotDataException(String modelId, String dataId,
            String attributeName)
      {
         super("ModelId or dataId is not set: modelId: " + modelId + ", dataId: " + dataId
               + ", attributeName: " + attributeName);
      }
   }
}
