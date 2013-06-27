/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
   
   protected void parse(String xml)
   {
      List<DataCluster> parsedClusters = new ArrayList();

      try
      {
         trace.debug("Data cluster configuration bootstrapped from audit trail.");

         DocumentBuilder domBuilder = new RuntimeSetupDocumentBuilder();
         Document setup = domBuilder.parse(new InputSource(new StringReader(xml)));

         NodeList rsNodes = setup.getElementsByTagName(RUNTIME_SETUP);
         if (0 < rsNodes.getLength())
         {
            if (1 < rsNodes.getLength())
            {
               trace.warn("Ignoring all but first <" + RUNTIME_SETUP + "> elements");
            }
            final NodeList atNodes = ((Element) rsNodes.item(0))
                  .getElementsByTagName(AUDIT_TRAIL);
            if (0 < atNodes.getLength())
            {
               if (1 < atNodes.getLength())
               {
                  trace.warn("Ignoring all but first <" + AUDIT_TRAIL + "> elements");
               }
               final NodeList dccNodes = ((Element) atNodes.item(0))
                     .getElementsByTagName(DATA_CLUSTERS);
               if (0 < dccNodes.getLength())
               {
                  if (1 < dccNodes.getLength())
                  {
                     trace.warn("Ignoring all but first <" + DATA_CLUSTERS + "> elements");
                  }
                  final NodeList dcNodes = ((Element) dccNodes.item(0))
                        .getElementsByTagName(DATA_CLUSTER);
                  for (int dcIdx = 0; dcIdx < dcNodes.getLength(); ++dcIdx)
                  {
                     final Element dcNode = (Element) dcNodes.item(dcIdx);

                     String dcTableName = dcNode.getAttribute(DATA_CLUSTER_TABNAME_ATT);

                     List slots = new ArrayList();
                     List indexes = new ArrayList();

                     NodeList dscNodes = dcNode.getElementsByTagName(DATA_SLOTS);
                     if (0 < dscNodes.getLength())
                     {
                        if (1 < dscNodes.getLength())
                        {
                           trace.warn("Ignoring all but first <" + DATA_SLOTS
                                 + "> elements");
                        }
                        NodeList dsNodes = ((Element) dscNodes.item(0))
                              .getElementsByTagName(DATA_SLOT);
                        for (int dsIdx = 0; dsIdx < dsNodes.getLength(); ++dsIdx)
                        {
                           Element dsNode = (Element) dsNodes.item(dsIdx);

                           String modelId = dsNode.getAttribute(DATA_SLOT_MODELID_ATT);
                           String dataId = dsNode.getAttribute(DATA_SLOT_DATAID_ATT);
                           String attribute = dsNode
                                 .getAttribute(DATA_SLOT_ATTRIBUTENAME_ATT);

                           if (StringUtils.isNotEmpty(modelId)
                                 && StringUtils.isNotEmpty(dataId))
                           {
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
                                       "A single data-slot must not contain both storage types sValueColumn and nValueColumn.");
                              }
                              if (StringUtils.isNotEmpty(nValueColumn)
                                    && StringUtils.isNotEmpty(dValueColumn))
                              {
                                 throw new PublicException(
                                       "A numeric data-slot must not contain both storage types nValueColumn and dValueColumn.");
                              }
                              if (StringUtils.isEmpty(sValueColumn)
                                    && StringUtils.isNotEmpty(dValueColumn))
                              {
                                 throw new PublicException(
                                       "A data-slot must not contain storage type dValueColumn without storage type sValueColumn.");
                              }
                              if (StringUtils.isNotEmpty(sValueColumn)
                                    && StringUtils.isEmpty(dValueColumn))
                              {
                                 trace.info(MessageFormat
                                       .format(
                                             "Data slot for modelId: {0}, dataId: {1}, attribute: {2} "
                                                   + "does define a string value column but no double value column. "
                                                   + "Sorting for these data might be performed lexically even if it contains numeric values",
                                             new Object[] {modelId, dataId, attribute}));
                              }
                              slots.add(new DataSlot(modelId, dataId, attribute, dsNode
                                    .getAttribute(DATA_SLOT_OIDCOLUMN_ATT), dsNode
                                    .getAttribute(DATA_SLOT_TYPECOLUMN_ATT),
                                    nValueColumn, sValueColumn, dValueColumn,
                                    ignorePreparedStatements));
                           }
                           else
                           {
                              trace.warn("Will ignore data slot for cluster table: "
                                    + dcTableName
                                    + ". ModelId or dataId is not set: modelId: "
                                    + modelId + ", dataId: " + dataId + ", attribute: "
                                    + attribute);
                           }
                        }
                     }

                     NodeList dcicNodes = dcNode
                           .getElementsByTagName(DATA_CLUSTER_INDEXES);
                     if (0 < dcicNodes.getLength())
                     {
                        if (1 < dcicNodes.getLength())
                        {
                           trace.warn("Ignoring all but first <" + DATA_CLUSTER_INDEXES
                                 + "> elements");
                        }
                        NodeList dciNodes = ((Element) dcicNodes.item(0))
                              .getElementsByTagName(DATA_CLUSTER_INDEX);
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

                     Parameters params = Parameters.instance();
                     String schemaName = params.getString(Session.KEY_AUDIT_TRAIL_SCHEMA);
                     
                     parsedClusters.add(new DataCluster(schemaName, dcTableName, dcNode
                           .getAttribute(DATA_CLUSTER_PICOLUMN_ATT), (DataSlot[]) slots
                           .toArray(new DataSlot[0]), (DataClusterIndex[]) indexes
                           .toArray(new DataClusterIndex[0]), getEnableStates(dcNode)));
                  }
               }
            }
         }

         this.clusters = (DataCluster[]) parsedClusters.toArray(EMPTY_DATA_CLUSTER_ARRAY);
      }
      catch (SAXException e)
      {
         throw new PublicException("Invalid runtime setup configuration.", e);
      }
      catch (IOException e)
      {
         throw new InternalException(
               "Cannot read runtime setup configuration from String.", e);
      }
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
}
