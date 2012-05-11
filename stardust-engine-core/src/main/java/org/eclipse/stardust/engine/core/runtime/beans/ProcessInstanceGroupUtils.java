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
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.BaseErrorCase;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.pojo.data.PrimitiveXmlUtils;



public class ProcessInstanceGroupUtils
{
   private static final String QUALIFIED_CASE_PERFORMER_ID = new QName(
         PredefinedConstants.PREDEFINED_MODEL_ID, PredefinedConstants.CASE_PERFORMER_ID).toString();

   private ProcessInstanceGroupUtils()
   {
      // Utility class
   }

   public static void setPrimitiveDescriptors(IProcessInstance processInstance,
         Map<String, ? > values)
   {
      Map<String, Object> descriptorsMap = getGroupDescriptorsMap(processInstance);

      Set< ? > entrySet = values.entrySet();
      for (Object object : entrySet)
      {
         Entry<String, Object> entry = (Entry<String, Object>) object;
         String key = entry.getKey();
         Object value = entry.getValue();

         setPrimitiveDescriptorValue(processInstance, key, value, descriptorsMap);
      }

      setGroupDescriptorsMap(processInstance, descriptorsMap);
   }

   public static void setPrimitiveDescriptor(IProcessInstance processInstance,
         String key, Object value)
   {
      Map<String, Object> descriptorsMap = getGroupDescriptorsMap(processInstance);

      setPrimitiveDescriptorValue(processInstance, key, value, descriptorsMap);

      setGroupDescriptorsMap(processInstance, descriptorsMap);
   }

   public static Object getPrimitiveDescriptor(IProcessInstance processInstance,
         String descrId)
   {
      List<Map<String, String>> groupDescriptorsList = getGroupDescriptorsList(processInstance);
      for (Map<String, String> descriptorEntry : groupDescriptorsList)
      {
         String encodedValue = descriptorEntry.get("value");
         QName valueAsQName = QName.valueOf(encodedValue);
         String id = valueAsQName.getNamespaceURI();
         if (descrId != null && descrId.equals(id))
         {
            String type = descriptorEntry.get("type");
            String value = valueAsQName.getLocalPart();
            return unmarshalPrimitiveDescriptorValue(type, value);
         }
      }
      return null;
   }

   public static Map<String, Object> getPrimitiveDescriptors(IProcessInstance processInstance, Set<String> ids)
   {
      Map<String, Object> descriptors = CollectionUtils.newHashMap();

      List<Map<String, String>> groupDescriptorsList = getGroupDescriptorsList(processInstance);
      for (Map<String, String> descriptorEntry : groupDescriptorsList)
      {
         String encodedValue = descriptorEntry.get("value");
         QName valueAsQName = QName.valueOf(encodedValue);
         String id = valueAsQName.getNamespaceURI();
         String type = descriptorEntry.get("type");
         String value = valueAsQName.getLocalPart();

         if (CollectionUtils.isEmpty(ids) || ids.contains(id))
         {
            descriptors.put(id, unmarshalPrimitiveDescriptorValue(type, value));
         }
      }

      return descriptors;
   }

   public static List<DataPath> getDescriptorDefinitions(final IProcessInstance processInstance)
   {
      List<DataPath> descriptors = CollectionUtils.newList();

      List<Map<String, String>> caseDescriptorList = getGroupDescriptorsList(processInstance);
      for (int i = 0; i < caseDescriptorList.size(); i++)
      {
         Map<String, String> data = caseDescriptorList.get(i);
         String encodedValue = data.get("value");
         QName valueAsQName = QName.valueOf(encodedValue);
         String id = valueAsQName.getNamespaceURI();
         Class mappedType = PrimitiveXmlUtils.getMappedType(QName.valueOf(data.get("type")));
         descriptors.add(DetailsFactory.create(new CaseDescriptorRef(
               processInstance.getProcessDefinition().getModel().getModelOID(),
               processInstance.getOID(),
               i, id, mappedType.getName())));
      }

      return descriptors;
   }

   public static void assertNotCasePerformer(ParticipantInfo participant)
   {
      if (participant != null)
      {
         assertNotCasePerformer(participant.getQualifiedId());
      }
   }

   public static void assertNotCasePerformer(String qualifiedParticipantId)
   {
      if (isCasePerformer(qualifiedParticipantId))
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PARTICIPANT_ID.raise(qualifiedParticipantId));
      }
   }

   public static boolean isCasePerformer(String qualifiedParticipantId)
   {
      if (qualifiedParticipantId != null
            && QUALIFIED_CASE_PERFORMER_ID.equals(qualifiedParticipantId))
      {
         return true;
      }
      return false;
   }

   private static void setPrimitiveDescriptorValue(IProcessInstance processInstance,
         String key, Object value, Map<String, Object> descriptorsMap)
   {
      if (value != null)
      {
         descriptorsMap.put(key, marshalPrimitiveDescriptorValue(key, value));
      }
      else
      {
         descriptorsMap.remove(key);
      }
   }

   private static Object marshalPrimitiveDescriptorValue(String key, Object value)
   {
      Map<String, String> newMap = CollectionUtils.newMap();

      QName type = PrimitiveXmlUtils.marshalSimpleTypeXsdType(value.getClass());
      String stringValue = PrimitiveXmlUtils.marshalPrimitiveValue((Serializable) value);

      if (type != null)
      {
         //newMap.put("id", key);
         newMap.put("type", type.toString());
         newMap.put("value", '{' + key + '}' + stringValue);
      }
      else
      {
         throw new InvalidValueException(
               BaseErrorCase.BPMRT_GENERAL_INCOMPATIBLE_TYPE.raise(value.getClass(),
                     String.class));
      }

      return newMap;
   }

   private static Object unmarshalPrimitiveDescriptorValue(String type, String value)
   {
      return PrimitiveXmlUtils.unmarshalPrimitiveValue(QName.valueOf(type), value);
   }

   private static IData getGroupInfoData(IProcessInstance pi)
   {
      IModel model = (IModel) pi.getProcessDefinition().getModel();

      return model.findData(PredefinedConstants.CASE_DATA_ID);
   }

   private static Map<String, Object> getGroupDescriptorsMap(IProcessInstance pi)
   {
      Map<String, Object> descriptorsMap = CollectionUtils.newHashMap();

      List<Map<String, String>> descriptorsList = getGroupDescriptorsList(pi);

      if (descriptorsList != null)
      {
         for (Map<String, String> descriptorEntry : descriptorsList)
         {
            String encodedValue = descriptorEntry.get("value");
            QName valueAsQName = QName.valueOf(encodedValue);
            String id = valueAsQName.getNamespaceURI();
            descriptorsMap.put(id, descriptorEntry);
         }
      }

      return descriptorsMap;
   }

   private static List<Map<String, String>> getGroupDescriptorsList(IProcessInstance pi)
   {
      List<Map<String, String>> inDataValue = (List<Map<String, String>>) pi.getInDataValue(getGroupInfoData(pi),
            PredefinedConstants.CASE_DESCRIPTORS_ELEMENT);
      return inDataValue != null ? inDataValue : Collections.EMPTY_LIST;
   }

   private static void setGroupDescriptorsMap(IProcessInstance processInstance,
         Map<String, Object> descriptorsMap)
   {
      processInstance.setOutDataValue(getGroupInfoData(processInstance),
            PredefinedConstants.CASE_DESCRIPTORS_ELEMENT, new ArrayList(
                  descriptorsMap.values()));
   }

}
