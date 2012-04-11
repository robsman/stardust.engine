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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.DataCopyOptions;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.vfs.VfsUtils;

public class DataCopyUtils
{
   private static final Logger trace = LogManager.getLogger(DataCopyUtils.class);

   private DataCopyUtils()
   {
   }

   protected static void copyDataUsingDocumentCopyHeuristics(
         IProcessInstance sourceProcessInstance, IProcessInstance targetProcessInstance,
         Set<String> ignoreDataIds)
   {
      Map<String, DataCopyMappingRule> mappingRules = DataCopyUtils.createCopyDataHeuristicRules(
            sourceProcessInstance, targetProcessInstance);

      copyData(sourceProcessInstance, targetProcessInstance, ignoreDataIds, mappingRules);
   }

   protected static void copyDataUsingNoOverrideHeuristics(
         IProcessInstance sourceProcessInstance, IProcessInstance targetProcessInstance,
         Set<String> ignoreDataIds)
   {
      Map<String, DataCopyMappingRule> mappingRules = new HashMap<String, DataCopyMappingRule>();

      Set<String> ignoreDataIds2 = new HashSet<String>();

      if (ignoreDataIds != null)
      {
         ignoreDataIds2.addAll(ignoreDataIds);
      }

      for (Iterator iterator = sourceProcessInstance.getAllDataValues(); iterator.hasNext();)
      {
         IDataValue srcValue = (IDataValue) iterator.next();
         IData srcData = srcValue.getData();
         String srcDataId = srcData.getId();
         // only if target data value is not initialized and only for document data
         if ( !ignoreDataIds2.contains(srcDataId) && isDmsDocument(srcData))
         {
            IData targetData = getDataFromProcessInstance(targetProcessInstance,
                  srcDataId);
            if (targetData != null && hasSameModelId(srcData, targetData)
                  && hasOutDataMapping(srcValue, targetProcessInstance)
                  && !isInitializedInTarget(srcValue, targetProcessInstance)
                  && hasSameDocumentTypeId(srcData, targetData))
            {
               if (docTypeSchemaEquals(srcData, targetData))
               {
                  // Will be copied 1:1.
               }
               else
               {
                  mappingRules.put(srcDataId, new DataCopyMappingRule(srcData,
                        targetData, true, false));
               }
            }
            else if (supportsProcessAttachments(targetProcessInstance))
            {
               addCopyToProcessAttachmentsRule(srcData, targetProcessInstance,
                     mappingRules);
            }
            else
            {
               // No out data mapping exists, target supports no process attachments: no
               // copy.
               ignoreDataIds2.add(srcDataId);
            }
         }
         else if (DmsConstants.DATA_ID_ATTACHMENTS.equals(srcDataId)
               && supportsProcessAttachments(targetProcessInstance))
         {
            IData targetData = getDataFromProcessInstance(targetProcessInstance,
                  DmsConstants.DATA_ID_ATTACHMENTS);
            if (targetData != null)
            {
               mappingRules.put(srcDataId, new DataCopyMappingRule(srcData, targetData,
                     false, true));
            }
            else
            {
               ignoreDataIds2.add(srcDataId);
            }
         }
         else
         {
            ignoreDataIds2.add(srcDataId);
         }

      }

      copyData(sourceProcessInstance, targetProcessInstance, ignoreDataIds2, mappingRules);
   }

   private static boolean hasSameModelId(IData srcData, IData targetData)
   {
      IModel srcModel = (IModel) srcData.getModel();
      IModel trgModel = (IModel) targetData.getModel();

      return srcModel.getId().equals(trgModel.getId());
   }

   private static boolean docTypeSchemaEquals(IData srcData, IData targetData)
   {
      String srcStructTypeDefId = getStructTypeDefId(srcData);
      String trgStructTypeDefId = getStructTypeDefId(targetData);

      if (StringUtils.isEmpty(srcStructTypeDefId) && StringUtils.isEmpty(trgStructTypeDefId))
      {
         // both have default document meta data schema.
         return true;
      }

      IModel srcModel = (IModel) srcData.getModel();
      IModel trgModel = (IModel) targetData.getModel();


      ITypeDeclaration srcTypeDeclaration = srcModel.findTypeDeclaration(srcStructTypeDefId);
      ITypeDeclaration trgTypeDeclaration = trgModel.findTypeDeclaration(trgStructTypeDefId);

      Set<TypedXPath> srcXPaths = StructuredTypeRtUtils.getAllXPaths(srcModel, srcTypeDeclaration);
      Set<TypedXPath> trgXPaths = StructuredTypeRtUtils.getAllXPaths(trgModel, trgTypeDeclaration);

      if (srcXPaths != null)
      {
         return srcXPaths.equals(trgXPaths);
      }
      return false;
   }

   private static boolean supportsProcessAttachments(IProcessInstance processInstance)
   {
      return null != getDataFromProcessInstance(processInstance,
            DmsConstants.DATA_ID_ATTACHMENTS);
   }

   private static boolean hasOutDataMapping(IDataValue srcValue,
         IProcessInstance processInstance)
   {
      String srcDataId = srcValue.getData().getId();
      Iterator allOutDataPaths = processInstance.getProcessDefinition()
            .getAllOutDataPaths();
      while (allOutDataPaths.hasNext())
      {
         IDataPath dataPath = (IDataPath) allOutDataPaths.next();
         if (dataPath.getDirection().isCompatibleWith(Direction.OUT)
               && dataPath.getData().getId().equals(srcDataId)
               && StringUtils.isEmpty(dataPath.getAccessPath()))
         {
            return true;
         }
      }
      return false;
   }

   private static boolean isInitializedInTarget(IDataValue srcValue,
         IProcessInstance processInstance)
   {
      String srcDataId = srcValue.getData().getId();

      IData targetData = getDataFromProcessInstance(processInstance, srcDataId);

      IDataValue dataValue = processInstance.getDataValue(targetData);

      return null != dataValue.getValue();
   }

   private static void addCopyToProcessAttachmentsRule(IData srcData,
         IProcessInstance targetProcessInstance,
         Map<String, DataCopyMappingRule> mappingRules)
   {
      if (isDmsDocument(srcData) || isDmsDocumentList(srcData))
      {
         IData targetProcessAttachmentsData = getDataFromProcessInstance(
               targetProcessInstance, DmsConstants.DATA_ID_ATTACHMENTS);

         if (null != targetProcessAttachmentsData)
         {
            mappingRules.put(srcData.getId(), new DataCopyMappingRule(srcData,
                  targetProcessAttachmentsData, false, true));
         }
      }
   }

   private static IData getDataFromProcessInstance(IProcessInstance processInstance,
         String dataId)
   {
      IModel model = (IModel) processInstance.getProcessDefinition().getModel();
      return model.findData(dataId);
   }

   private static void copyData(IProcessInstance sourceProcessInstance,
         IProcessInstance targetProcessInstance, Set<String> ignoreDataIds,
         Map<String, DataCopyMappingRule> mappingRules)
   {
      // copy all data
      for (Iterator iterator = sourceProcessInstance.getAllDataValues(); iterator.hasNext();)
      {
         IDataValue srcValue = (IDataValue) iterator.next();

         if (trace.isDebugEnabled())
         {
            trace.debug("Data value '" + srcValue.getData().getId() + "' retrieved.");
         }

         String dataId = srcValue.getData().getId();
         // do not copy data already initialized by the data map.
         if ( !ignoreDataIds.contains(dataId))
         {
            IData targetData = null;
            Object targetValue = null;
            if ( !mappingRules.containsKey(dataId))
            {
               targetData = getDataFromProcessInstance(targetProcessInstance, dataId);
               if ( !ignoreDataIds.contains(targetData.getId()))
               {
                  targetValue = srcValue.getSerializedValue();
                  targetProcessInstance.setOutDataValue(targetData, "", targetValue);
               }
            }
            else
            {
               DataCopyMappingRule dataCopyMappingRule = mappingRules.get(dataId);
               targetData = dataCopyMappingRule.getTargetData();

               if ( !ignoreDataIds.contains(targetData.getId()))
               {
                  Object modifiedValue = processMappingRule(srcValue,
                        dataCopyMappingRule, targetProcessInstance);
                  if (modifiedValue != null)
                  {
                     targetValue = modifiedValue;
                  }
                  else
                  {
                     targetValue = srcValue.getSerializedValue();
                  }
                  targetProcessInstance.setOutDataValue(targetData, "", targetValue);
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Data copy Heuristic in effect: Mapping data '" + dataId
                           + "' to '" + targetData.getId() + "'");
                  }
               }
            }
         }
      }
   }

   private static Object processMappingRule(IDataValue srcValue,
         DataCopyMappingRule dataCopyMappingRule, IProcessInstance targetProcessInstance)
   {
      Object modifiedDataValueObject = null;
      if (dataCopyMappingRule.isRemoveMetaData())
      {
         EmbeddedServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();
         DocumentManagementService dms = sf.getDocumentManagementService();

         if (isDmsDocument(dataCopyMappingRule.getSourceData()))
         {
            Object value = srcValue.getProcessInstance().getInDataValue(
                  dataCopyMappingRule.getSourceData(), "");
            if (value instanceof Document)
            {
               Document document = (Document) value;

               document.setProperties(Collections.EMPTY_MAP);
               document.setDocumentType(null);

               if ( !isVersioned(document))
               {
                  dms.versionDocument(document.getId(), null);
               }

               modifiedDataValueObject = dms.updateDocument(document, true, null, false);
            }
         }
         if (isDmsDocumentList(dataCopyMappingRule.getSourceData()))
         {
            Object value = srcValue.getProcessInstance().getInDataValue(
                  dataCopyMappingRule.getSourceData(), "");
            if (value instanceof List)
            {
               List<Document> docList = (List<Document>) value;
               List<Document> updatedDocList = new ArrayList<Document>();
               for (Document document : docList)
               {
                  document.setProperties(Collections.EMPTY_MAP);
                  document.setDocumentType(null);

                  if ( !isVersioned(document))
                  {
                     dms.versionDocument(document.getId(), null);
                  }

                  updatedDocList.add(dms.updateDocument(document, true, null, false));
               }

               modifiedDataValueObject = updatedDocList;
            }
         }
      }
      else if (dataCopyMappingRule.isMergeListTypeData())
      {
         if (isDmsDocumentList(dataCopyMappingRule.getTargetData()))
         {
            if (isDmsDocumentList(dataCopyMappingRule.getSourceData()))
            {
               Object srcValueObj = srcValue.getProcessInstance().getInDataValue(
                     dataCopyMappingRule.getSourceData(), "");
               List<Document> srcDocList = null;
               if (srcValueObj instanceof List)
               {
                  srcDocList = (List<Document>) srcValueObj;
               }

               Object targetValueObj = targetProcessInstance.getInDataValue(
                     dataCopyMappingRule.getTargetData(), "");
               List<Document> targetDocList = null;
               if (targetValueObj instanceof List)
               {
                  targetDocList = (List<Document>) targetValueObj;
               }
               else if (null == targetValueObj)
               {
                  targetDocList = new ArrayList<Document>();
               }

               if (srcDocList != null)
               {
                  // Merge both lists
                  List<Document> updatedDocList = new ArrayList<Document>(targetDocList);
                  for (Document srcDocument : srcDocList)
                  {
                     if ( !existsInTarget(srcDocument.getId(), targetDocList))
                     {
                        updatedDocList.add(srcDocument);
                     }
                  }
                  modifiedDataValueObject = updatedDocList;
               }
               else
               {
                  // nothing added, but targetDocList needs to be returned so the default
                  // source value does not overwrite it.
                  modifiedDataValueObject = targetDocList;
               }
            }
            else if (isDmsDocument(dataCopyMappingRule.getSourceData()))
            {
               Object srcValueObj = srcValue.getProcessInstance().getInDataValue(
                     dataCopyMappingRule.getSourceData(), "");
               Document srcDoc = null;
               if (srcValueObj instanceof Document)
               {
                  srcDoc = (Document) srcValueObj;
               }

               Object targetValueObj = targetProcessInstance.getInDataValue(
                     dataCopyMappingRule.getTargetData(), "");
               List<Document> targetDocList = null;
               if (targetValueObj instanceof List)
               {
                  targetDocList = (List<Document>) targetValueObj;
               }
               else if (targetValueObj == null)
               {
                  targetDocList = new ArrayList<Document>();
               }

               if (srcDoc != null)
               {
                  // Merge both lists
                  List<Document> updatedDocList = new ArrayList<Document>(targetDocList);

                  if ( !existsInTarget(srcDoc.getId(), targetDocList))
                  {
                     updatedDocList.add(srcDoc);
                  }

                  modifiedDataValueObject = updatedDocList;
               }
               else
               {
                  // nothing added, but targetDocList needs to be returned so the default
                  // source value does not overwrite it.
                  modifiedDataValueObject = targetDocList;
               }
            }
         }
      }

      return modifiedDataValueObject;
   }

   private static boolean existsInTarget(String documentId, List<Document> targetDocList)
   {
      boolean exists = false;
      for (Document targetDocument : targetDocList)
      {
         if (documentId != null && documentId.equals(targetDocument.getId()))
         {
            exists = true;
         }
      }
      return exists;
   }

   private static boolean isVersioned(Document doc)
   {
      boolean result = true;
      if (VfsUtils.VERSION_UNVERSIONED.equals(doc.getRevisionId()))
      {
         result = false;
      }
      return result;
   }

   private static Map<String, DataCopyMappingRule> createCopyDataHeuristicRules(
         IProcessInstance parentProcessInstance, IProcessInstance processInstance)
   {
      final List<IDataValue> allDataValues = CollectionUtils.newListFromIterator(parentProcessInstance.getAllDataValues());
      final List<IData> targetUsedData = getDataUsedInProcess(processInstance);

      Map<String, DataCopyMappingRule> mappingRules = new HashMap<String, DataCopyMappingRule>();
      Map<String, Integer> numDocTypePerStructDefIdDoc = getNumDocTypePerStructDefIdForDoc(allDataValues);
      Map<String, Integer> numDocTypePerStructDefIdDocList = getNumDocTypePerStructDefIdForDocList(allDataValues);

      if (allDataValues != null)
      {
         for (IDataValue value : allDataValues)
         {
            IData data = value.getData();
            // 0.) implicit: if the data is used in the target, copy it into same data.
            DataCopyMappingRule targetRule = evaluateSameIdRule(data, targetUsedData);
            // 1.) same-type-rule
            if (targetRule == null)
            {
               targetRule = evaluateSameTypeRule(data, numDocTypePerStructDefIdDoc,
                     numDocTypePerStructDefIdDocList, targetUsedData);
            }
            // 2.) one-document-rule
            if (targetRule == null)
            {
               targetRule = evaluateOneDocumentRule(data, allDataValues, targetUsedData);
            }
            // 3.) else merge to process attachments
            if (targetRule == null)
            {
               addCopyToProcessAttachmentsRule(data, processInstance, mappingRules);
            }
            if (null != targetRule)
            {
               mappingRules.put(data.getId(), targetRule);
            }

         }
      }

      return mappingRules;
   }

   private static Map<String, Integer> getNumDocTypePerStructDefIdForDoc(
         List<IDataValue> allSourceData)
   {
      Map<String, Integer> map = new HashMap<String, Integer>();

      for (IDataValue dataValue : allSourceData)
      {
         IData data = dataValue.getData();
         if (isDmsDocument(data))
         {
            String structTypeDefId = getStructTypeDefId(data);
            if ( !StringUtils.isEmpty(structTypeDefId))
            {
               Integer integer = map.get(structTypeDefId);
               if (integer == null)
               {
                  integer = Integer.valueOf(0);
               }
               integer++ ;
               map.put(structTypeDefId, integer);
            }
         }
      }

      return map;
   }

   private static Map<String, Integer> getNumDocTypePerStructDefIdForDocList(
         List<IDataValue> allSourceData)
   {
      Map<String, Integer> map = new HashMap<String, Integer>();

      for (IDataValue dataValue : allSourceData)
      {
         IData data = dataValue.getData();
         if (isDmsDocumentList(data))
         {
            String structTypeDefId = getStructTypeDefId(data);
            if ( !StringUtils.isEmpty(structTypeDefId))
            {
               Integer integer = map.get(structTypeDefId);
               if (integer == null)
               {
                  integer = Integer.valueOf(0);
               }
               integer++ ;
               map.put(structTypeDefId, integer);
            }
         }
      }

      return map;
   }

   private static List<IData> getDataUsedInProcess(IProcessInstance processInstance)
   {
      List<IData> usedData = new ArrayList<IData>();
      IModel model = (IModel) processInstance.getProcessDefinition().getModel();

      Set<String> usedDataIds = DataUtils.getDataForProcess(
            processInstance.getProcessDefinition().getId(), model);

      for (String dataId : usedDataIds)
      {
         IData data = model.findData(dataId);
         usedData.add(data);
      }

      return usedData;
   }

   private static DataCopyMappingRule evaluateSameIdRule(IData data,
         List<IData> targetUsedData)
   {
      for (IData iData : targetUsedData)
      {
         if (data.getId().equals(iData.getId())
               && (isDmsDocument(iData) || isDmsDocumentList(iData))
               && hasSameDocumentTypeId(data, iData))
         {
            return new DataCopyMappingRule(data, iData, false, true);
         }
      }
      return null;
   }

   private static DataCopyMappingRule evaluateSameTypeRule(IData data,
         final Map<String, Integer> numDocTypePerStructDefIdForDoc,
         final Map<String, Integer> numDocTypePerStructDefIdForDocList,
         final List<IData> targetUsedData)
   {
      IData targetData = null;

      // same-type-rule
      int found = 0;
      if (isDmsDocument(data))
      {
         String structTypeDefId = getStructTypeDefId(data);
         if ( !StringUtils.isEmpty(structTypeDefId))
         {
            // if more than one document data using the same doc type exist the rule does
            // not apply.
            Integer numSameDocTypeUsed = numDocTypePerStructDefIdForDoc.get(structTypeDefId);
            if (numSameDocTypeUsed != 1)
            {
               return null;
            }
         }
         for (IData iData : targetUsedData)
         {
            if (isDmsDocument(iData) && hasSameDocumentTypeId(data, iData))
            {
               targetData = iData;
               found++ ;
            }
         }
      }
      if (found == 0)
      {
         if (isDmsDocumentList(data))
         {
            String structTypeDefId = getStructTypeDefId(data);
            if ( !StringUtils.isEmpty(structTypeDefId))
            {
               // if more than one documentList data using the same doc type exist the
               // rule does not apply.
               Integer numSameDocTypeUsed = numDocTypePerStructDefIdForDocList.get(structTypeDefId);
               if (numSameDocTypeUsed != 1)
               {
                  return null;
               }
            }
            for (IData iData : targetUsedData)
            {
               if (isDmsDocumentList(iData) && hasSameDocumentTypeId(data, iData))
               {
                  targetData = iData;
                  found++ ;
               }
            }
         }
      }
      return found == 1 ? new DataCopyMappingRule(data, targetData, false, false) : null;

   }

   private static DataCopyMappingRule evaluateOneDocumentRule(IData data,
         final List<IDataValue> allDataValues, final List<IData> targetUsedData)
   {
      if (isDmsDocument(data) || isDmsDocumentList(data))
      {
         IData targetData = null;
         int docSrc = 0;
         int docListSrc = 0;
         for (IDataValue iDataValue : allDataValues)
         {
            if (isDmsDocument(iDataValue.getData()))
            {
               docSrc++ ;
            }
            if (isDmsDocumentList(iDataValue.getData()))
            {
               docListSrc++ ;
            }
         }
         int docTarget = 0;
         int docListTarget = 0;
         for (IData iData : targetUsedData)
         {
            if (isDmsDocument(iData) && isDmsDocument(data))
            {
               targetData = iData;
               docTarget++ ;
            }
            if (isDmsDocumentList(iData) && isDmsDocumentList(data))
            {
               targetData = iData;
               docListTarget++ ;
            }
         }

         // exactly one document in source and target
         if (isDmsDocument(data) && docSrc == 1 && docTarget == 1)
         {
            return new DataCopyMappingRule(data, targetData, true, false);
         }
         // exactly one document list in source and target
         if (isDmsDocumentList(data) && docListSrc == 1 && docListTarget == 1)
         {
            return new DataCopyMappingRule(data, targetData, true, false);
         }
      }
      return null;
   }

   private static boolean hasSameDocumentTypeId(IData data, IData iData)
   {
      String structTypeDefId = getStructTypeDefId(data);
      String structTypeDefId2 = getStructTypeDefId(iData);

      if (StringUtils.isEmpty(structTypeDefId) && StringUtils.isEmpty(structTypeDefId2))
      {
         return true;
      }
      else if (structTypeDefId != null)
      {
         return structTypeDefId.equals(structTypeDefId2);
      }
      return false;
   }

   private static String getStructTypeDefId(IData data)
   {
      String structTypeDefId = null;
      if (data != null)
      {
         structTypeDefId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
      }
      return structTypeDefId;
   }

   private static boolean isDmsDocument(IData data)

   {
      String dataTypeId = data.getType().getId();
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
      {
         return true;
      }
      return false;
   }

   private static boolean isDmsDocumentList(IData data)

   {
      String dataTypeId = data.getType().getId();
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
      {
         return true;
      }
      return false;
   }

   public static Map<String, ? extends Serializable> copyData(IProcessInstance pi, IModel target, DataCopyOptions dco)
   {
      DataCopyResult dcr = new DataCopyResult(target);
      addExplicitValues(dcr, dco.getReplacementTable());
      Set<String> translated = addSpecifiedValues(dcr, pi, dco.getDataTranslationTable());
      if (dco.copyAllData() && !dco.useHeuristics())
      {
         addExistingValues(dcr, pi, translated);
      }
      return dcr.result;
   }

   private static void addExplicitValues(DataCopyResult dcr, Map<String, ? extends Serializable> values)
   {
      if (values != null)
      {
         for (Map.Entry<String, ? extends Serializable> entry : values.entrySet())
         {
            dcr.addValue(null, entry.getKey(), entry.getValue());
         }
      }
   }

   private static Set<String> addSpecifiedValues(DataCopyResult dcr, IProcessInstance pi,
         Map<String, String> translation)
   {
      if (translation != null)
      {
         Set<String> translated = CollectionUtils.newSet();
         IProcessDefinition pd = pi.getProcessDefinition();
         IModel model = (IModel) pd.getModel();
         
         for (Map.Entry<String, String> entry : translation.entrySet())
         {
            String newDataId = entry.getKey();
            if (!dcr.result.containsKey(newDataId))
            {
               String dataId = entry.getValue();
               if (dataId == null)
               {
                  dataId = newDataId;
               }
               translated.add(dataId);
               IData data = model.findData(dataId);
               Object value = pi.getInDataValue(data, "");
               if (value instanceof Serializable)
               {
                  dcr.addValue(data, newDataId, (Serializable) value);
               }
            }
         }
         return translated;
      }
      return Collections.emptySet();
   }

   private static void addExistingValues(DataCopyResult dcr,
         IProcessInstance pi, Set<String> translated)
   {
      Iterator<IDataValue> dataValues = pi.getAllDataValues();
      while (dataValues.hasNext())
      {
         IDataValue dv = dataValues.next();
         IData data = dv.getData();
         String dataId = data.getId();
         if (!data.isPredefined() && !dcr.result.containsKey(dataId) && !translated.contains(dataId))
         {
            Object value = pi.getInDataValue(data, "");
            if (value instanceof Serializable)
            {
               dcr.addValue(data, dataId, (Serializable) value);
            }
         }
      }
   }

   private static class DataCopyResult
   {
      private Map<String, Serializable> result = CollectionUtils.newMap();
      
      private final IModel target;

      private DataCopyResult(IModel target)
      {
         this.target = target;
      }

      private void addValue(IData source, String dataId, Serializable value)
      {
         IData data = target.findData(dataId);
         if (data != null)
         {
            if (source != null)
            {
               if (!CompareHelper.areEqual(source.getType().getId(), data.getType().getId()))
               {
                  // data type changed.
                  return;
               }
               boolean isStruct = PredefinedConstants.STRUCTURED_DATA.equals(data.getType().getId());
               if (!isStruct && !data.getAllAttributes().equals(source.getAllAttributes()))
               {
                  // data definition changed.
                  return;
               }
               if (isStruct)
               {
                  // traverse and fix
                  IXPathMap srcMap = StructuredTypeRtUtils.getXPathMap(source);
                  TypedXPath srcPath = srcMap.getRootXPath();
                  IXPathMap tgtMap = StructuredTypeRtUtils.getXPathMap(data);
                  TypedXPath tgtPath = tgtMap.getRootXPath();
                  Map<String, Serializable> map = CollectionUtils.newMap();
                  map.put(srcPath.getId(), value);
                  repair(tgtPath, srcPath, map);
                  value = map.get(tgtPath.getId());
                  if (value == null)
                  {
                     // incompatible.
                     return;
                  }
               }
            }
            result.put(dataId, value);
         }
      }

      private void repair(TypedXPath tgtPath, TypedXPath srcPath, Map<String, ?> map)
      {
         if (compatible(tgtPath, srcPath))
         {
            Object o = map.get(tgtPath.getId());
            if (tgtPath.getType() == -1)
            {
               if (tgtPath.isList())
               {
                  // TODO: lists of lists
                  for (Object item : (List) o)
                  {
                     repairChildren(tgtPath, srcPath, item);
                  }
               }
               else
               {
                  repairChildren(tgtPath, srcPath, o);
               }
            }
         }
         else
         {
            map.remove(srcPath.getId());
         }
      }

      private void repairChildren(TypedXPath tgtPath, TypedXPath srcPath, Object o)
      {
         if (o instanceof Map)
         {
            Map<String, ?> m = (Map) o;
            Set<String> keys = CollectionUtils.newSet(); 
            keys.addAll(m.keySet());
            for (String key : keys)
            {
               TypedXPath tgtChild = tgtPath.getChildXPath(key);
               if (tgtChild == null)
               {
                  m.remove(key);
               }
               else
               {
                  TypedXPath srcChild = srcPath.getChildXPath(key);
                  repair(tgtChild, srcChild, m);
               }
            }
         }
      }

      private boolean compatible(TypedXPath tgtPath, TypedXPath srcPath)
      {
         if (!CompareHelper.areEqual(tgtPath.getId(), srcPath.getId()))
         {
            return false;
         }
         if (tgtPath.getType() != srcPath.getType())
         {
            return false;
         }
         return tgtPath.isList() == srcPath.isList();
      }
   }
}
