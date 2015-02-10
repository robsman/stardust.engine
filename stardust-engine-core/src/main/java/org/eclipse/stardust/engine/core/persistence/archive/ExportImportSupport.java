/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils.ProcessBlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobReader;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerBean.ModelManagerPartition;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * <p>
 * This class aims at facilitating import and export of process archived process instances
 * </p>
 *
 * @author jsaayman
 * @version $Revision$
 */
public class ExportImportSupport
{
   private static final Logger LOGGER = LogManager.getLogger(ExportImportSupport.class);

   private static <T> List<List<T>> partition(List<T> list, int size)
   {
      if (list == null || size < 1)
      {
         throw new IllegalArgumentException();
      }
      List<List<T>> result = new ArrayList<List<T>>();
      for (int i = 0; i < list.size(); i += size)
      {
         int end = i + size;
         if (end > list.size())
         {
            end = list.size();
         }
         result.add(list.subList(i, end));
      }
      return result;
   }

   public static List<ExportMetaData> partition(ExportMetaData exportMetaData, int size)
   {
      List<ExportMetaData> result = new ArrayList<ExportMetaData>();
      
      List<List<Long>> batches = partition(new ArrayList<Long>(exportMetaData.getRootProcessInstanceOids()), size);
      for (List<Long> batch : batches)
      {
         HashMap<Long, ArrayList<Long>> part = new HashMap<Long, ArrayList<Long>>();
         for (Long key : batch)
         {
            part.put(key, exportMetaData.getMappedProcessInstances().get(key));
         }
         result.add(new ExportMetaData(exportMetaData.getModelOids(), part));
      }
      return result;
   }

   public static Date getStartOfDay(Date date)
   {
      if (date == null)
      {
         return null;
      }
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
   }

   public static Date getEndOfDay(Date date)
   {
      if (date == null)
      {
         return null;
      }
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.set(Calendar.HOUR_OF_DAY, 23);
      c.set(Calendar.MINUTE, 59);
      c.set(Calendar.SECOND, 59);
      c.set(Calendar.MILLISECOND, 999);
      return c.getTime();
   }

   /**
    * <p>
    * Loads the process instance graph contained in the raw data and attaches all included
    * {@link Persistent}s to the given {@link Session}'s cache.
    * </p>
    *
    * @param rawData
    *           the raw byte array that needs to be deserialized
    * @param session
    *           the session the {@link Persistent}s should be populated to
    * @param filter
    *           Filter to use when importing processes. Null filter will import all
    *           processes.
    * @param oidResolver
    * 
    */
   public static int importProcessInstances(Map<String, List<byte[]>> rawData,
         final Session session, ImportFilter filter, ImportOidResolver oidResolver)
   {
      int count;
      if (rawData == null)
      {
         count = 0;
      }
      else
      {
         final ProcessBlobReader reader = new ProcessBlobReader(session, filter,
               oidResolver);
         final Set<Persistent> persistents = new HashSet<Persistent>();

         // these two tables needs to be read first for filtering and avoiding duplicate
         // imports
         List<byte[]> allTableData = rawData.get(ProcessInstanceBean.TABLE_NAME);
         for (byte[] tableData : allTableData)
         {
            persistents.addAll(reader.readProcessBlob(tableData));
         }
         rawData.remove(ProcessInstanceBean.TABLE_NAME);
         if (rawData.get(DataValueBean.TABLE_NAME) != null)
         {
            allTableData = rawData.get(DataValueBean.TABLE_NAME);
            for (byte[] tableData : allTableData)
            {
               persistents.addAll(reader.readProcessBlob(tableData));
            }
            rawData.remove(DataValueBean.TABLE_NAME);
         }

         for (String table : rawData.keySet())
         {
            allTableData = rawData.get(table);
            for (byte[] tableData : allTableData)
            {
               persistents.addAll(reader.readProcessBlob(tableData));
            }
         }
         TransientProcessInstanceUtils.processPersistents(session, null, persistents);

         if (CollectionUtils.isNotEmpty(persistents))
         {
            count = prepareObjectsForImport(persistents, session);
         }
         else
         {
            count = 0;
         }
      }
      return count;
   }

   /**
    * Validates the model being imported. Populates keyToRuntimeOidMap. Returns the data
    * to import less the model.
    * 
    * @param rawData
    * @param classToRuntimeOidMap
    *           Map with element class as Key to Map of imported runtimeOid to current
    *           environment's runtimeOid
    * @return 
    * @return Map of tableNames to import and their corresponding byte[]
    */
   public static Map<String, List<byte[]>> validateModel(byte[] rawData,
         ImportMetaData importMetaData)
   {
      if (importMetaData == null)
      {
         throw new IllegalArgumentException("Null importMetaData provided");
      }
      ByteArrayBlobReader reader = null;
      try
      {
         reader = new ByteArrayBlobReader(rawData);
         reader.nextBlob();

         byte modelMarker = reader.readByte();
         if (modelMarker != BlobBuilder.MODEL_MARKER_START)
         {
            throw new IllegalStateException("No model provided in import.");
         }
         String exportPartition = reader.readString();
         if (!SecurityProperties.getPartition().getId().equals(exportPartition))
         {
            throw new IllegalStateException(
                  "Invalid environment to import into. Export partition "
                        + exportPartition + " does not match current partition "
                        + SecurityProperties.getPartition().getId());
         }
         ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
               .getCurrent();

         List<IModel> activeModels = modelManager.findActiveModels();
         Map<String, Long> activeModelMap = new HashMap<String, Long>();
         Map<String, IdentifiableElement> allFqIds = new HashMap<String, IdentifiableElement>();
         if (CollectionUtils.isEmpty(activeModels))
         {
            throw new IllegalStateException(
                  "Invalid environment to import into. Current environment does not have an active model.");
         }
         else
         {
            for (IModel model : activeModels)
            {
               activeModelMap.put(model.getId(), Long.valueOf(model.getModelOID()));
               allFqIds.putAll(ModelManagerBean.getAllFqIds(modelManager, model));
            }
         }

         // there are IdentifiableElements that do not have an fqId, we handle them here

         // start transitions do not have a fqId, and always have a runtimeOid of -1
         // we need to add this special case to support this, so that this is not an
         // inconsistency when importing such transitions
         importMetaData.addMappingForClass(TransitionBean.class, TransitionTokenBean.START_TRANSITION_RT_OID, TransitionTokenBean.START_TRANSITION_RT_OID);

         // model doesnt have an fqId so we explicitly write model id here
         // we have two start markers: 1 at start of id printing, and another before fqIds
         while ((modelMarker = reader.readByte()) != BlobBuilder.MODEL_MARKER_START)
         {
            if (modelMarker != BlobBuilder.MODEL_MARKER_ELEMENT)
            {
               throw new IllegalStateException("Unknown model marker '" + modelMarker
                     + "'.");
            }
            Long exportModelId = reader.readLong();
            String modelId = reader.readString();
            Long importModelId = activeModelMap.get(modelId);
            if (importModelId != null)
            {
               importMetaData.addMappingForClass(ModelBean.class, exportModelId, importModelId);
            }
            else
            {
               throw new IllegalStateException(
                     "Invalid environment to import into. Current environment does "
                           + "not have an active model with id:" + modelId);
            }
         }

         // transition token table has model 0 when transition is -1
         importMetaData.addMappingForClass(ModelBean.class, TransitionTokenBean.START_TRANSITION_MODEL_OID, TransitionTokenBean.START_TRANSITION_MODEL_OID);
         
         while ((modelMarker = reader.readByte()) != BlobBuilder.MODEL_MARKER_END)
         {
            if (modelMarker != BlobBuilder.MODEL_MARKER_ELEMENT)
            {
               throw new IllegalStateException("Unknown model marker '" + modelMarker
                     + "'.");
            }
            String key = reader.readString();
            Long oldId = reader.readLong();

            IdentifiableElement identifiableElement = allFqIds.get(key);
            if (identifiableElement == null)
            {
               throw new IllegalStateException(
                     "Invalid model being imported. IdentifiableElement " + key
                           + " not found in current model");
            }
            importMetaData.addMappingForClass(identifiableElement.getClass(), oldId, modelManager.getRuntimeOid(identifiableElement));
         }
         reader.readByte();
         if (reader.getCurrentIndex() < rawData.length)
         {
            return splitArrayByTables(reader);
         } 
         else
         {
            LOGGER.info("No Process Data in this file");
            return new HashMap<String,List<byte[]>>();
         }
      }
      finally
      {
         if (reader != null)
         {
            reader.close();
         }
      }
   }

   public static Map<String, List<byte[]>> getDataByTable(byte[] rawData)
   {
      ByteArrayBlobReader reader = null;
      Map<String, List<byte[]>> result;
      try
      {
         reader = new ByteArrayBlobReader(rawData);
         reader.nextBlob();
         result = splitArrayByTables(reader);
         return result;
      }
      finally
      {
         if (reader != null)
         {
            reader.close();
         }
      }
   }

   private static int prepareObjectsForImport(final Set<Persistent> persistents,
         final Session session)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Loaded " + persistents.size() + " persistents:");
      }
      int count = 0;
      for (final Persistent p : persistents)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Persistent loaded: " + p.getClass().getSimpleName() + ". "
                  + p.toString());
         }
         if (p instanceof ProcessInstanceBean)
         {
            ProcessInstanceBean processInstance = (ProcessInstanceBean) p;
            processInstance.prepareForImportFromArchive();
            count++;
         }
         else if (p instanceof ActivityInstanceBean)
         {
            ActivityInstanceBean activity = (ActivityInstanceBean) p;
            // initialized the initial performer attribute which is necessary upon
            // session flushing
            activity.prepareForImportFromArchive();
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Loaded " + count + " ProcessInstanceBeans");
      }
      return count;
   }


   public static byte[] exportModels(List<Integer> modelOids)
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();
      List<IModel> allModels = new ArrayList<IModel>();
      for (Integer modelOid : modelOids)
      {
         IModel model = modelManager.findModel(modelOid);
         allModels.add(model);
      }
      return exportModels(modelManager, allModels);
   }
   
   public static byte[] exportModels()
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      List<IModel> activeModels = modelManager.findActiveModels();
      return exportModels(modelManager, activeModels);
   }


   public static Collection<Integer> getActiveModelOids()
   {
      List<Integer> results = new ArrayList<Integer>();
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      List<IModel> activeModels = modelManager.findActiveModels();
      for (IModel model : activeModels)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            results.add(model.getModelOID());
         }
      }
      return results;
   }
   
   private static byte[] exportModels(ModelManagerPartition modelManager, List<IModel> models)
   {
      ByteArrayBlobBuilder blobBuilder = new ByteArrayBlobBuilder();
      blobBuilder.init(null);

      blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_START);
      blobBuilder.writeString(SecurityProperties.getPartition().getId());

      // models doesn't have fqIds so we explicitly write model ids here
      // need to write all modelids, we don't know in which version models processes were
      // started
      for (IModel model : models)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_ELEMENT);
            blobBuilder.writeLong(model.getModelOID());
            blobBuilder.writeString(model.getId());
         }
      }

      blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_START);
      for (IModel model : models)
      {
         Map<String, IdentifiableElement> allFqIds = ModelManagerBean.getAllFqIds(
               modelManager, model);
         for (String key : allFqIds.keySet())
         {
            blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_ELEMENT);
            blobBuilder.writeString(key);
            blobBuilder.writeLong(modelManager.getRuntimeOid(allFqIds.get(key)));
         }
      }
      blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_END);
      blobBuilder.persistAndClose();
      return blobBuilder.getBlob();
   }
   
   private static Map<String, List<byte[]>> splitArrayByTables(ByteArrayBlobReader reader)
   {
      final Map<String, List<byte[]>> dataByTables = new HashMap<String, List<byte[]>>();
      byte sectionMarker;
      while ((sectionMarker = reader.readByte()) != BlobBuilder.SECTION_MARKER_EOF)
      {
         if (sectionMarker != BlobBuilder.SECTION_MARKER_INSTANCES)
         {
            throw new IllegalStateException("Unknown section marker '" + sectionMarker
                  + "'.");
         }
         readSection(reader, dataByTables);
      }
      return dataByTables;
   }

   private static void readSection(final ByteArrayBlobReader reader,
         final Map<String, List<byte[]>> dataByTables)
   {
      int startIndex = reader.getCurrentIndex() - 1;
      final String tableName = reader.readString();
      final int instanceCount = reader.readInt();

      final TypeDescriptor typeDesc = TypeDescriptorRegistry.current()
            .getDescriptorForTable(tableName);

      final List<FieldDescriptor> fieldDescs = typeDesc.getPersistentFields();
      final List<LinkDescriptor> linkDescs = typeDesc.getLinks();

      for (int i = 0; i < instanceCount; i++)
      {
         readPersistent(reader, typeDesc, fieldDescs);
         readLinkBuffer(reader, linkDescs);
      }
      // data in rawdata is batched up by batch size used in ProcessElementsVisitor
      List<byte[]> allTableData = dataByTables.get(tableName);
      if (allTableData == null)
      {
         allTableData = new ArrayList<byte[]>();
         dataByTables.put(tableName, allTableData);
      }
      int endIndex = reader.getCurrentIndex() + 1;
      byte[] data = new byte[endIndex - startIndex];
      System.arraycopy(reader.getBlob(), startIndex, data, 0, data.length - 1);
      data[data.length - 1] = BlobBuilder.SECTION_MARKER_EOF;
      allTableData.add(data);
   }

   private static void readPersistent(final ByteArrayBlobReader reader,
         final TypeDescriptor typeDesc, final List<FieldDescriptor> fieldDescs)
   {
      for (final FieldDescriptor fd : fieldDescs)
      {
         final Field field = fd.getField();
         final Class< ? > fieldType = field.getType();
         reader.readFieldValue(fieldType);
      }
   }

   private static void readLinkBuffer(final ByteArrayBlobReader reader,
         final List<LinkDescriptor> linkDescs)
   {
      for (int i = 0; i < linkDescs.size(); i++)
      {
         final LinkDescriptor linkDesc = linkDescs.get(i);
         final Field fkField = linkDesc.getFkField();
         final Class< ? > fkFieldType = fkField.getType();
         reader.readFieldValue(fkFieldType);
      }
   }

}
