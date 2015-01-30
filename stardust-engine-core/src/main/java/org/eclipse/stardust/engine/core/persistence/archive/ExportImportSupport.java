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
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils.ProcessBlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobReader;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerBean.ModelManagerPartition;

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
   public static int importProcessInstances(Map<String, byte[]> rawData,
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
         persistents.addAll(reader.readProcessBlob(rawData
               .get(ProcessInstanceBean.TABLE_NAME)));
         rawData.remove(ProcessInstanceBean.TABLE_NAME);
         if (rawData.get(DataValueBean.TABLE_NAME) != null)
         {
            persistents.addAll(reader.readProcessBlob(rawData
                  .get(DataValueBean.TABLE_NAME)));
            rawData.remove(DataValueBean.TABLE_NAME);
         }

         for (String table : rawData.keySet())
         {
            persistents.addAll(reader.readProcessBlob(rawData.get(table)));
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
    * @return Map of tableNames to import and their corresponding byte[]
    */
   public static Map<String, byte[]> validateModel(byte[] rawData,
         Map<Class, Map<Long, Long>> classToRuntimeOidMap)
   {
      if (classToRuntimeOidMap == null)
      {
         throw new IllegalArgumentException("Null classToRuntimeOidMap provided");
      }
      final ByteArrayBlobReader reader = new ByteArrayBlobReader(rawData);
      reader.nextBlob();

      byte modelMarker = reader.readByte();
      if (modelMarker != BlobBuilder.MODEL_MARKER_START)
      {
         throw new IllegalStateException("No model provided in import.");
      }
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      IModel activeModel = modelManager.findActiveModel();
      if (activeModel == null)
      {
         throw new IllegalStateException(
               "Invalid environment to import into. Current environment does not have an active model.");
      }

      // there are IdentifiableElements that do not have an fqId, we handle them here

      // start transitions do not have a fqId, and always have a runtimeOid of -1
      // we need to add this special case to support this, so that this is not an
      // inconsistency when importing such transitions
      Map<Long, Long> idMap = new HashMap<Long, Long>();
      idMap.put(TransitionTokenBean.START_TRANSITION_RT_OID,
            TransitionTokenBean.START_TRANSITION_RT_OID);
      classToRuntimeOidMap.put(TransitionBean.class, idMap);

      // model doesnt have an fqId so we explicitly write model id here
      Long exportModelId = reader.readLong();
      idMap = new HashMap<Long, Long>();
      idMap.put(exportModelId, Long.valueOf(activeModel.getModelOID()));
      // transition token table has model 0 when transition is -1
      idMap.put(TransitionTokenBean.START_TRANSITION_MODEL_OID,
            TransitionTokenBean.START_TRANSITION_MODEL_OID);
      classToRuntimeOidMap.put(ModelBean.class, idMap);

      Map<String, IdentifiableElement> allFqIds = ModelManagerBean.getAllFqIds(
            modelManager, activeModel);

      while ((modelMarker = reader.readByte()) != BlobBuilder.MODEL_MARKER_END)
      {
         if (modelMarker != BlobBuilder.MODEL_MARKER_ELEMENT)
         {
            throw new IllegalStateException("Unknown model marker '" + modelMarker + "'.");
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
         idMap = classToRuntimeOidMap.get(identifiableElement.getClass());
         if (idMap == null)
         {
            idMap = new HashMap<Long, Long>();
            classToRuntimeOidMap.put(identifiableElement.getClass(), idMap);
         }
         idMap.put(oldId, modelManager.getRuntimeOid(identifiableElement));
      }

      return splitArrayByTables(reader);
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

   public static byte[] exportModel()
   {
      ByteArrayBlobBuilder blobBuilder = new ByteArrayBlobBuilder();
      blobBuilder.init(null);
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();
      IModel model = modelManager.findActiveModel();
      Map<String, IdentifiableElement> allFqIds = ModelManagerBean.getAllFqIds(
            modelManager, model);
      blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_START);

      // model doesn't have an fqId so we explicitly write model id here
      blobBuilder.writeLong(model.getModelOID());

      for (String key : allFqIds.keySet())
      {
         blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_ELEMENT);
         blobBuilder.writeString(key);
         blobBuilder.writeLong(modelManager.getRuntimeOid(allFqIds.get(key)));
      }

      blobBuilder.writeByte(ByteArrayBlobBuilder.MODEL_MARKER_END);
      blobBuilder.persistAndClose();
      return blobBuilder.getBlob();
   }

   private static Map<String, byte[]> splitArrayByTables(ByteArrayBlobReader reader)
   {
      final Map<String, byte[]> dataByTables = new HashMap<String, byte[]>();

      byte sectionMarker;
      reader.readByte();
      while ((sectionMarker = reader.readByte()) != BlobBuilder.SECTION_MARKER_EOF)
      {
         if (sectionMarker != BlobBuilder.SECTION_MARKER_INSTANCES)
         {
            throw new IllegalStateException("Unknown section marker '" + sectionMarker
                  + "'.");
         }
         readSection(reader, dataByTables);
      }

      reader.close();
      return dataByTables;
   }

   private static void readSection(final ByteArrayBlobReader reader,
         final Map<String, byte[]> dataByTables)
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
      int endIndex = reader.getCurrentIndex() + 1;
      byte[] rest = new byte[endIndex - startIndex];
      System.arraycopy(reader.getBlob(), startIndex, rest, 0, rest.length - 1);
      rest[rest.length - 1] = BlobBuilder.SECTION_MARKER_EOF;
      dataByTables.put(tableName, rest);
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
