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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.TransitionBean;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils.ProcessBlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobReader;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerBean.ModelManagerPartition;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.thirdparty.encoding.Text;

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
   
   public static void archive(List<IProcessInstance> pis)
   {
      if (CollectionUtils.isNotEmpty(pis))
      {
         List<Long> oids = new ArrayList<Long>();
         for (IProcessInstance pi : pis)
         {
            oids.add(pi.getOID());
         }
         ArchiveFilter filter = new ArchiveFilter(null, null, oids, null, null, null, null);
         ExportResult exportResult = (ExportResult) new WorkflowServiceImpl()
               .execute(new ExportProcessesCommand(
                     ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, 
                     null));
         ExportQueueSender sender = new ExportQueueSender();
         sender.sendMessage(exportResult);
      }
      else
      {
         LOGGER.warn("Received empty list of processes to archive.");
      }
   }
   
   /**
    * for archiving deferred/writebehind processes
    * @param blobBuilder
    * @param session
    */
   public static void archive(Set<Persistent> persistents, Session session)
   {
      if (persistents == null)
      {
         LOGGER.warn("Received null persistents to archive.");
         return;
      }
      if (session == null)
      {
         LOGGER.warn("Received null session to archive.");
         return;
      }
      
      ExportResult exportResult = new ExportResult(null);
      exportResult.close(persistents, session);
     
      ExportQueueSender sender = new ExportQueueSender();
      sender.sendMessage(exportResult);
   }

   public static String getUUID(IProcessInstance processInstance)
   {
      setModelUUID((IModel)processInstance.getProcessDefinition().getModel());
      String modelUUID = (String)processInstance.getProcessDefinition().getModel().getRuntimeAttribute(PredefinedConstants.MODEL_UUID);
      return getUUID(processInstance.getOID(), modelUUID);
   }
   
   private static String getUUID(Long oid, String modelUUID)
   {
      if (StringUtils.isEmpty(modelUUID))
      {
         throw new IllegalArgumentException("modelUUID can not be empty");
      }
      String uuid = ArchiveManagerFactory.getCurrentId() + "_" + oid + "_"
            + modelUUID;
      return uuid;
   }

   public static Gson getGson()
   {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.setPrettyPrinting();
      gsonBuilder.excludeFieldsWithoutExposeAnnotation();
      Gson gson = gsonBuilder.create();
      return gson;
   }

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
         List<T> batchList = new ArrayList<T>();
         // sublist is not serializable, so create an arraylist
         batchList.addAll(list.subList(i, end));
         result.add(batchList);
      }
      return result;
   }
   
   public static byte[] addAll(final byte[] array1, final byte[] array2)
   {
      final byte[] joinedArray = new byte[array1.length + array2.length];
      System.arraycopy(array1, 0, joinedArray, 0, array1.length);
      System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
      return joinedArray;
   }

   /**
    * Partitions the exportMetaData. Firstly partitions it by date. So that each
    * ExportMetaData returned is at most for 1 index Date. If there are more than size
    * processes for a specific date, there will be more than one ExportMetaData returned
    * for the date, with each containing at most size processes
    * 
    * @param exportMetaData
    * @param size
    * @return
    */
   public static List<ExportMetaData> partition(ExportMetaData exportMetaData, int size)
   {
      List<ExportMetaData> result = new ArrayList<ExportMetaData>();

      Set<Date> indexDates = exportMetaData.getIndexDates();
      for (Date date : indexDates)
      {
         List<Long> processesForDate = exportMetaData
               .getRootProcessesForDate(date);
         if (processesForDate.size() > size)
         {
            List<List<Long>> batches = partition(processesForDate, size);
            for (List<Long> rootOids : batches)
            {
               HashMap<Long, ArrayList<Long>> processesToSubprocesses = new HashMap<Long, ArrayList<Long>>();
               Map<Long, String> oidsToUuids = new HashMap<Long, String>();
               for (Long rootOid : rootOids)
               {
                  processesToSubprocesses.put(rootOid, exportMetaData
                        .getRootToSubProcesses().get(rootOid));
                  oidsToUuids.put(rootOid, exportMetaData.getOidsToUuids().get(rootOid));
                  for (Long subOid : processesToSubprocesses.get(rootOid))
                  {
                     oidsToUuids.put(subOid, exportMetaData.getOidsToUuids().get(subOid));
                  }
               }
               Map<Date, List<Long>> dateToRootPiOids = new HashMap<Date, List<Long>>();
               dateToRootPiOids.put(date, rootOids);
               result.add(new ExportMetaData(exportMetaData.getModelOids(), processesToSubprocesses,
                     dateToRootPiOids, oidsToUuids));
            }
         }
         else
         {
            HashMap<Long, ArrayList<Long>> processesToSubprocesses = new HashMap<Long, ArrayList<Long>>();
            Map<Long, String> oidsToUuids = new HashMap<Long, String>();
            for (Long rootOid : processesForDate)
            {
               processesToSubprocesses.put(rootOid, exportMetaData
                     .getRootToSubProcesses().get(rootOid));
               oidsToUuids.put(rootOid, exportMetaData.getOidsToUuids().get(rootOid));
               for (Long subOid : processesToSubprocesses.get(rootOid))
               {
                  oidsToUuids.put(subOid, exportMetaData.getOidsToUuids().get(subOid));
               }
            }
            Map<Date, List<Long>> dateToRootPiOids = new HashMap<Date, List<Long>>();
            dateToRootPiOids.put(date, processesForDate);
            result.add(new ExportMetaData(exportMetaData.getModelOids(), processesToSubprocesses,
                  dateToRootPiOids, oidsToUuids));
         }
      }

      return result;
   }
      
   /**
    * Formats a date with the given dateFormat, if dateFormat is empty
    * ArchiveManagerFactory.getDateFormat() is used
    * @param date
    * @param dateFormat
    * @return
    */
   public static String formatDate(Date date, String dateFormat)
   {
      try
      {
         if (StringUtils.isEmpty(dateFormat))
         {
            dateFormat = ArchiveManagerFactory.getDateFormat();  
         }
         DateFormat df = new SimpleDateFormat(dateFormat);
         if (date != null)
         {
            return df.format((Date) date);
         }
         return "";
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Failed to format date " + date + " with date pattern: " + dateFormat);
      }
   }
   
   public static ExportResult merge(List<ExportResult> exportResults, Map<Date,ExportModel> exportModelByDate)
   {
      ExportResult exportResult;
      if (CollectionUtils.isNotEmpty(exportResults))
      {
         Set<Long> purgeProcessIds = new HashSet<Long>();
         Set<Date> uniqueDates = new HashSet<Date>();
         for (ExportResult result : exportResults)
         {
            if (result == null)
            {
               LOGGER.warn("Received a null exportResult, continueing to the next");
               continue;
            }
            uniqueDates.addAll(result.getDates());
            purgeProcessIds.addAll(result.getPurgeProcessIds());
         }
         HashMap<Date, byte[]> resultsByDate = new HashMap<Date, byte[]>();
         HashMap<Date, ExportIndex> indexByDate = new HashMap<Date, ExportIndex>();
         HashMap<Date, List<Long>> processInstanceOidsByDate = new HashMap<Date, List<Long>>();
         HashMap<Date, List<Integer>> processLengthsByDate = new HashMap<Date, List<Integer>>();
         if (exportModelByDate == null)
         {
            exportModelByDate = new HashMap<Date, ExportModel>();
         }
         String archiveManagerId = null;
         String dateFormat = null;
         String dumpLocation = null;
         dateloop: for (Date date : uniqueDates)
         {
            for (ExportResult export : exportResults)
            {
               ExportIndex exportIndex = export.getExportIndex(date);
               if (exportIndex != null)
               {
                  archiveManagerId = exportIndex.getArchiveManagerId();
                  dateFormat = exportIndex.getDateFormat();
                  dumpLocation = exportIndex.getDumpLocation();
                  break dateloop;
               }
            }
         }
         for (Date date : uniqueDates)
         {
            byte[] allData = resultsByDate.get(date);
            ExportIndex index = indexByDate.get(date);
            if (allData == null)
            {
               allData = new byte[] {};
               index = new ExportIndex(archiveManagerId, dateFormat, dumpLocation);
               indexByDate.put(date, index);
            }
            for (ExportResult result : exportResults)
            {
               if (result == null)
               {
                  continue;
               }
               byte[] data = result.getResults(date);
               // does this specific exportResult have data for this date
               if (data != null)
               {
                  allData = addAll(allData, data);
                  // new reference every time, re-put everytime
                  resultsByDate.put(date, allData);

                  List<Long> processInstanceOids = processInstanceOidsByDate.get(date);
                  List<Integer> processLengths = processLengthsByDate.get(date);
                  if (processInstanceOids == null)
                  {
                     processInstanceOids = new ArrayList<Long>();
                     processLengths = new ArrayList<Integer>();
                     processInstanceOidsByDate.put(date, processInstanceOids);
                     processLengthsByDate.put(date, processLengths);
                  }

                  processInstanceOids.addAll(result.getProcessInstanceOids(date));
                  processLengths.addAll(result.getProcessLengths(date));
                  index.getRootProcessToSubProcesses().putAll(
                        result.getExportIndex(date).getRootProcessToSubProcesses());
                  
                  mergeFields(index, result.getExportIndex(date));
                  index.getOidsToUuids().putAll(result.getExportIndex(date).getOidsToUuids());
                  ExportModel fromModel = result.getExportModel(date);
                  if (fromModel != null)
                  {
                     ExportModel exportModel = exportModelByDate.get(date);
                     if (exportModel == null)
                     {
                        exportModelByDate.put(date, fromModel);
                     }
                     else
                     {
                        exportModel.getFqIdToRtOid().putAll(fromModel.getFqIdToRtOid());
                        exportModel.getModelOidToUuid().putAll(fromModel.getModelOidToUuid());
                        exportModel.getUuiIdToXpdl().putAll(fromModel.getUuiIdToXpdl());
                     }
                  }
               }
            }
         }
         exportResult = new ExportResult(exportModelByDate, resultsByDate, indexByDate,
               processInstanceOidsByDate, processLengthsByDate, dumpLocation, purgeProcessIds);
      }
      else
      {
         exportResult = null;
      }
      return exportResult;
   }

   private static void mergeFields(ExportIndex index, ExportIndex batchResultIndex)
   {
      for (String field : batchResultIndex.getFields().keySet())
      {
         if (index.getFields().get(field) != null)
         {
            for (String value : batchResultIndex.getFields().get(field).keySet())
            {
               if (index.getFields().get(field).get(value) == null)
               {
                  index.getFields().get(field).put(value, batchResultIndex.getFields().get(field).get(value));
               }
               else
               {
                  index.getFields().get(field).get(value).addAll(batchResultIndex.getFields().get(field).get(value));
               }
            }
         }
         else
         {
            index.getFields().put(field, batchResultIndex.getFields().get(field));
         }
      }
   }
   /**
    * This rounds a date off to the hour (floor)
    * @param date
    * @return
    */
   public static Date getIndexDateTime(Date date)
   {
      if (date == null)
      {
         return null;
      }
      Calendar c = Calendar.getInstance();
      c.setTime(date);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c.getTime();
   }

   private static boolean isPathToInclude(IDataPath path, Set<String> ids)
   {
      if (path.getData().getType().getId().equals(PredefinedConstants.PRIMITIVE_DATA))
      {
         return path.isDescriptor()
               && (Direction.IN.equals(path.getDirection()) || Direction.IN_OUT
                     .equals(path.getDirection()))
               && ((null == ids) || ids.contains(path.getId()));
      }
      return false;
   }

   public static Map<String, String> getFormattedDescriptors(
         IProcessInstance processInstance, Set<String> ids)
   {
      Map<String, Object> descriptorObjects = getDescriptors(processInstance, ids);
      Map<String, String> descriptors = new HashMap<String, String>();
      DateFormat df = new SimpleDateFormat(ArchiveManagerFactory.getDateFormat());
      for (String key : descriptorObjects.keySet())
      {
         Object value = descriptorObjects.get(key);
         String stringValue;
         if (value instanceof Date)
         {
            stringValue = df.format((Date) value);
         }
         else
         {
            stringValue = value.toString();
         }
         descriptors.put(key, stringValue);
      }
      return descriptors;
   }

   public static Map<String, Object> getDescriptors(IProcessInstance processInstance,
         Set<String> ids) throws ObjectNotFoundException
   {
      if (processInstance == null)
      {
         throw new IllegalArgumentException("Provide a processInstance");
      }
      try
      {
         IProcessDefinition processDefinition = processInstance.getProcessDefinition();
         return getDescriptors(processInstance, processDefinition, ids);
      }
      catch (ObjectNotFoundException e)
      {
         LOGGER.warn("Process Definition not found for pi OID: " + processInstance.getOID());
         return new HashMap<String, Object>();
      }
   }

   public static Map<String, Object> getDescriptors(IProcessInstance processInstance,
         IProcessDefinition processDefinition, Set<String> ids)
         throws ObjectNotFoundException
   {
      if (processInstance == null)
      {
         throw new IllegalArgumentException("Provide a processInstance");
      }
      if (processDefinition == null)
      {
         throw new IllegalArgumentException("Provide a processDefinition");
      }
      if (processInstance.isCaseProcessInstance())
      {
         HashMap primitiveDescriptors = new HashMap(
               ProcessInstanceGroupUtils.getPrimitiveDescriptors(processInstance, ids));

         ModelElementList allDataPaths = processDefinition.getDataPaths();
         for (int i = 0; i < allDataPaths.size(); i++)
         {
            final IDataPath path = (IDataPath) allDataPaths.get(i);
            if (isPathToInclude(path, ids))
            {
               primitiveDescriptors.put(path.getId(),
                     getInDataPath(processInstance, path));
            }
         }
         return primitiveDescriptors;
      }
      else
      {

         List<IDataPath> requestedDataPaths = new ArrayList<IDataPath>();
         ModelElementList allDataPaths = processDefinition.getDataPaths();
         for (int i = 0; i < allDataPaths.size(); i++)
         {
            final IDataPath path = (IDataPath) allDataPaths.get(i);
            if (isPathToInclude(path, ids))
            {
               requestedDataPaths.add(path);
            }
         }

         // prefetch data values in batch to improve performance
         List<IData> dataItems = new ArrayList<IData>(requestedDataPaths.size());
         for (IDataPath path : requestedDataPaths)
         {
            dataItems.add(path.getData());
         }
         processInstance.preloadDataValues(dataItems);

         Map<String, Object> values = new HashMap<String, Object>(
               requestedDataPaths.size());
         for (IDataPath path : requestedDataPaths)
         {
            values.put(path.getId(), getInDataPath(processInstance, path));
         }

         return values;
      }
   }

   private static Object getInDataPath(IProcessInstance processInstance, IDataPath path)
   {
      IData data = path.getData();
      if (data == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_DANGLING_IN_DATA_PATH.raise(path));
      }
      Object value = processInstance.getInDataValue(data, path.getAccessPath());
      Type type = (Type) data.getAttribute(PredefinedConstants.TYPE_ATT);
      if (type.equals(Type.Timestamp))
      {
         return value;
      }
      else
      {
         return value.toString();
      }
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
         final Session session, ImportOidResolver oidResolver)
   {
      int count;
      if (rawData == null)
      {
         count = 0;
      }
      else
      {
         final ProcessBlobReader reader = new ProcessBlobReader(session,
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
            final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
            rtEnv.setOperationMode(BpmRuntimeEnvironment.OperationMode.PROCESS_IMPORT);
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
    * Validates the model being imported. Populates keyToRuntimeOidMap.
    * 
    * @param rawData
    * @param classToRuntimeOidMap
    *           Map with element class as Key to Map of imported runtimeOid to current
    *           environment's runtimeOid
    */
   public static void validateModel(QueryService queryService, ExportModel exportModel, ImportMetaData importMetaData)
   {
      if (importMetaData == null)
      {
         throw new IllegalArgumentException("Null importMetaData provided");
      }
      

      if (!SecurityProperties.getPartition().getId().equals(exportModel.getPartition()))
      {
         throw new IllegalStateException(
               "Invalid environment to import into. Export partition "
                     + exportModel.getPartition() + " does not match current partition "
                     + SecurityProperties.getPartition().getId());
      }
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      List<IModel> allModels = modelManager.getModels();
      Map<String, Long> modelUuidToOid = new HashMap<String, Long>();
      Map<String, IdentifiableElement> allFqIds = new HashMap<String, IdentifiableElement>();
      if (CollectionUtils.isEmpty(allModels))
      {
         throw new IllegalStateException(
               "Invalid environment to import into. Current environment does not have a model.");
      }
      else
      {
         Map<String, Integer> uuidToModel = new HashMap<String, Integer>();
         for (IModel model : allModels)
         {
            setModelUUID(model);
            String uuid = (String)model.getRuntimeAttribute(PredefinedConstants.MODEL_UUID);
            if (uuidToModel.containsKey(uuid))
            {
               uuidToModel.put(uuid, findLastCompatibleModelOid(queryService, model.getId(), uuid));
            }
            else
            {
               uuidToModel.put(uuid, model.getModelOID());
            }
            modelUuidToOid.put(uuid, Long.valueOf(uuidToModel.get(uuid)));
            allFqIds.putAll(ModelManagerBean.getAllFqIds(modelManager, model));
         }
      }

      // there are IdentifiableElements that do not have an fqId, we handle them here

      // start transitions do not have a fqId, and always have a runtimeOid of -1
      // we need to add this special case to support this, so that this is not an
      // inconsistency when importing such transitions
      importMetaData.addMappingForClass(TransitionBean.class,
            TransitionTokenBean.START_TRANSITION_RT_OID,
            TransitionTokenBean.START_TRANSITION_RT_OID);

      // model doesnt have an fqId so we compare uuid
      // we have two start markers: 1 at start of id printing, and another before fqIds
      for (Integer exportModelOid : exportModel.getModelOidToUuid().keySet())
      {
        
         String exportUuid = exportModel.getModelOidToUuid().get(exportModelOid);
         Long importModelId = modelUuidToOid.get(exportUuid);
         if (importModelId != null)
         {
            importMetaData.addMappingForClass(ModelBean.class, new Long(exportModelOid),
                  importModelId);
         }
         else
         {
            throw new IllegalStateException(
                  "Invalid environment to import into. Current environment does "
                        + "not have a model with uuid:" + exportUuid);
         }
      }

      // transition token table has model 0 when transition is -1
      importMetaData.addMappingForClass(ModelBean.class,
            TransitionTokenBean.START_TRANSITION_MODEL_OID,
            TransitionTokenBean.START_TRANSITION_MODEL_OID);

      for (String key : exportModel.getFqIdToRtOid().keySet())
      {
         Long oldId = exportModel.getFqIdToRtOid().get(key);

         IdentifiableElement identifiableElement = allFqIds.get(key);
         if (identifiableElement == null)
         {
            throw new IllegalStateException(
                  "Invalid model being imported. IdentifiableElement " + key
                        + " not found in current model");
         }
         importMetaData.addMappingForClass(identifiableElement.getClass(), oldId,
               modelManager.getRuntimeOid(identifiableElement));
      }
   }

   private static Integer findLastCompatibleModelOid(QueryService queryService, String id,
         String uuid)
   {
      int result = 0;
      Models models = queryService.getModels(DeployedModelQuery.findForId(id));
      if (models != null)
      {
         Date lastDate = null;
         for (DeployedModelDescription model : models)
         {
            Date deploymentTime = model.getDeploymentTime();
            if (lastDate == null || lastDate.before(deploymentTime))
            {
               if (getModelUUID((long)model.getModelOID()).equals(uuid))
               {
                  lastDate = deploymentTime;
                  result = model.getModelOID();
               }
            }
         }
      }
      return result;
   }

   public static Map<String, List<byte[]>> getDataByTable(byte[] rawData)
   {
      ByteArrayBlobReader reader = null;
      Map<String, List<byte[]>> result;
      try
      {
         reader = new ByteArrayBlobReader(rawData);
         reader.nextBlob();
         result = splitArrayByTables(reader, null);
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
         else if (p instanceof TransitionInstanceBean)
         {
            TransitionInstanceBean transitionInstance = (TransitionInstanceBean) p;
            transitionInstance.prepareForImportFromArchive();

         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Loaded " + count + " ProcessInstanceBeans");
      }
      return count;
   }

   public static ExportModel exportModels(Set<Integer> modelOids)
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();
      List<IModel> allModels = new ArrayList<IModel>();
      for (Integer modelOid : modelOids)
      {
         IModel model = modelManager.findModel(modelOid);

         List<IModel> usedModels = ModelRefBean.getUsedModels(model);
         if (CollectionUtils.isNotEmpty(usedModels))
         {
            allModels.addAll(usedModels);
         }

         allModels.add(model);
      }
      return exportModels(modelManager, allModels);
   }


   public static List<Integer> findProcessDefinitionOids(
         Collection<String> processDefinitionIds)
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      List<IModel> activeModels = modelManager.findActiveModels();
      List<Integer> result = new ArrayList<Integer>();
      Set<String> searchItems = new HashSet<String>();
      searchItems.addAll(processDefinitionIds);
      Set<String> found = new HashSet<String>();
      for (IModel model : activeModels)
      {
         for (String proc : processDefinitionIds)
         {
            IProcessDefinition procDef = model.findProcessDefinition(proc);
            if (procDef != null)
            {
               found.add(proc);
               result.add((int)modelManager.getRuntimeOid(procDef));
            }
         }
         searchItems.removeAll(found);
         found.clear();
         if (searchItems.isEmpty())
         {
            break;
         }
      }
      return result;
   }

   public static List<Integer> getPartitionModelOids()
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();

      List<IModel> allModels = modelManager.getModels();
      List<Integer> result = new ArrayList<Integer>();
      for (IModel model : allModels)
      {
         result.add(model.getModelOID());
      }
      return result;
   }
   
   public static List<Integer> findModelOids(QueryService queryService,
         Collection<String> modelIds)
   {
      List<Integer> result = new ArrayList<Integer>();
      for (String id : modelIds)
      {
         Models models = queryService.getModels(DeployedModelQuery.findForId(id));
         
         if (models != null)
         {
            for (DeployedModelDescription model : models)
            {
               result.add((int)model.getModelOID());
            }
         }
      }
      return result;
   }

   public static String getXpdl(int modelOId)
   {
      String content = LargeStringHolder.getLargeString(modelOId, ModelPersistorBean.class);
      String xpdl = XpdlUtils.convertCarnot2Xpdl(content);
      return xpdl;
   }

   public static String getModelUUID(Long modelOId)
   {
      ModelManagerPartition modelManager = (ModelManagerPartition) ModelManagerFactory
            .getCurrent();
      IModel model = modelManager.findModel(modelOId);
      setModelUUID(model);
      return  model.getRuntimeAttribute(PredefinedConstants.MODEL_UUID);
   }
      
   private static void setModelUUID(IModel model)
   {
      if (StringUtils.isEmpty(model.getRuntimeAttribute(PredefinedConstants.MODEL_UUID)))
      {
         String content = LargeStringHolder.getLargeString(model.getModelOID(), ModelPersistorBean.class);
         String xpdl = XpdlUtils.convertCarnot2Xpdl(content);
         model.setRuntimeAttribute(PredefinedConstants.MODEL_UUID, Text.md5(xpdl));
      }
   }

   private static ExportModel exportModels(ModelManagerPartition modelManager,
         List<IModel> models)
   {
      
      String partition = SecurityProperties.getPartition().getId();
      Map<Integer, String> modelOidToUuid = new HashMap<Integer, String>();
      Map<String, String> uuidToXpdl = new HashMap<String, String>();
      // models doesn't have fqIds so we explicitly write model ids here
      // need to write all modelids, we don't know in which version models processes were
      // started
      for (IModel model : models)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            setModelUUID(model);
            modelOidToUuid.put(model.getModelOID(), (String) model.getRuntimeAttribute(PredefinedConstants.MODEL_UUID));
            uuidToXpdl.put((String) model.getRuntimeAttribute(PredefinedConstants.MODEL_UUID), getXpdl(model.getModelOID()));
         }
      }

      Map<String, Long> fqIdToRtOid = new HashMap<String, Long>();
      for (IModel model : models)
      {
         Map<String, IdentifiableElement> allFqIds = ModelManagerBean.getAllFqIds(
               modelManager, model);
         for (String key : allFqIds.keySet())
         {
            fqIdToRtOid.put(key, modelManager.getRuntimeOid(allFqIds.get(key)));
         }
      }
      return new ExportModel(fqIdToRtOid, modelOidToUuid, uuidToXpdl, partition);
   }

   private static Map<String, List<byte[]>> splitArrayByTables(
         ByteArrayBlobReader reader, Map<String, List<byte[]>> dataByTables)
   {
      if (dataByTables == null)
      {
         dataByTables = new HashMap<String, List<byte[]>>();
      }
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
      if (reader.getCurrentIndex() < reader.getBlob().length)
      {
         return splitArrayByTables(reader, dataByTables);
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
