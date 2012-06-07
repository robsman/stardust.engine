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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributesImpl;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfoImpl;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IQualityAssuranceCode;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.core.javascript.QualityAssuranceFormulaEvaluater;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStorageFactory;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;



/**
 * Utility class for Quality Assurance
 * 
 * @author barry.grotjahn
 * @version $Revision: 47927 $
 */
public class QualityAssuranceUtils
{
   
   
   /**
    * key under which the user probability will be stored in the user properties
    */
   public static final String QUALITY_ASSURANCE_USER_PROBABILITY = "QualityAssuranceUserProbability";  
   
   /**
    * key under which the user default probability will be stored in the preference store
    */
   public static final String QUALITY_ASSURANCE_USER_DEFAULT_PROBABILITY = "QualityAssuranceUserDefaultProbability";
   
   /**
    * Describes the state an activity instance is in - regarding quality assurance
    * 
    * @author barry.grotjahn
    * @version $Revision: $
    */
   public static enum QualityAssuranceState {
      /** The current instance is no quality assurance instance 
       * and is not part of the quality assurance cycle */
      NO_QUALITY_ASSURANCE,
      /** The current instance is a quality assurance instance
       *  and is part of the quality assurance cycle */
      IS_QUALITY_ASSURANCE,
      /** The current instance triggered a quality assurance instance
       *  and is part of the quality assurance cycle */
      QUALITY_ASSURANCE_TRIGGERED,
      /** The current instance is a workflow instance which is reworked and is part of the quality assurance cycle. 
       *  The previous quality assurance instances was marked with "Fail"
       */
      IS_REVISED;

      /** the key under which the state will be stored in the activity instance properties */
      public static final String PROPERTY_KEY = "QualityControlUtils.State";
   }

   public static boolean isQualityAssuranceInstance(IActivityInstance activityInstance)
   {
      return isQualityAssuranceInstance(activityInstance.getActivity(), 
            activityInstance.getQualityAssuranceState());
   }
   
   public static boolean isQualityAssuranceInstance(IActivity activity, QualityAssuranceState state)
   {
      if(activity.isQualityAssuranceEnabled() && state == QualityAssuranceState.IS_QUALITY_ASSURANCE)
      {
         return true;
      }
         
      return false;
   }
      
   public static QualityAssuranceResult.ResultState getResultState(IActivityInstance activityInstance)
   {
      if(QualityAssuranceUtils.isQualityAssuranceEnabled(activityInstance))
      {   
         ActivityInstanceAttributes attributes 
            = QualityAssuranceUtils.getActivityInstanceAttributes(activityInstance);         
         if(attributes != null)
         {
            QualityAssuranceResult.ResultState resultState 
               = attributes.getQualityAssuranceResult().getQualityAssuranceState();
            return resultState;
         }
      }
      
      return null;
   }
   
   public static IUser getMonitoredUser(IActivityInstance activityInstance)
   {
      IUser monitoredUser = null;
      if(activityInstance.getQualityAssuranceState() == QualityAssuranceState.IS_QUALITY_ASSURANCE)
      {
         //get the instance that created this qa instance
         Long monitoredInstanceOid 
            = (Long) activityInstance.getPropertyValue(QualityAssuranceInfo.MONITORED_INSTANCE_OID);
         IActivityInstance monitoredInstance 
            =   ActivityInstanceBean.findByOID(monitoredInstanceOid);
         monitoredUser = monitoredInstance.getPerformedBy(); 
      }
      
      return monitoredUser;
   }
      
   public static boolean isQualityAssuranceEnabled(IActivityInstance instance)
   {
      return instance.getActivity().isQualityAssuranceEnabled();
   }
      
   public static ActivityInstanceAttributes getActivityInstanceAttributes(IActivityInstance instance)
   {
      return ActivityInstanceAttributesMapper.getAsObject(instance.getAllProperties());
   }
   
   public static ActivityInstanceAttributes prepareForSave(ActivityInstanceAttributes attributes)
   {
      ActivityInstanceAttributesImpl preparedInstance 
         = new ActivityInstanceAttributesImpl(attributes);
      return preparedInstance;
   }
   
   public static void setActivityInstanceAttributes(ActivityInstanceAttributes attributes, IActivityInstance instance)
   {
      Map<String, Serializable> properties 
         = ActivityInstanceAttributesMapper.getAsProperties(attributes);
      for(String key: properties.keySet())
      {
         Serializable value = properties.get(key);
         instance.setPropertyValue(key, value);
      }
   }
   
   private static int getQualityAssuranceProbability(IActivityInstance activityInstance)
   {
      //try to get probability from user
      IUser aiPerformer = activityInstance.getPerformedBy();
      if(aiPerformer.getQualityAssuranceProbability() != null)
      {
         return aiPerformer.getQualityAssuranceProbability();
      }
      
      //try to get probability from user default
      IPreferenceStorageManager prefStore = PreferenceStorageFactory.getCurrent();
      Preferences preferences = prefStore.getPreferences(PreferenceScope.PARTITION, 
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS, 
            PreferencesConstants.PREFERENCE_ID_QUALITY_CONTROL);
      if(preferences != null)
      {
         Map<String, Serializable> preferenceValues = preferences.getPreferences();
         if(preferenceValues != null)
         {
            Integer userDefaultProbability = (Integer) preferenceValues.get(QUALITY_ASSURANCE_USER_DEFAULT_PROBABILITY);
            if(userDefaultProbability != null)
            {
               return userDefaultProbability;
            }
         } 
      }
      
      //try to get for participant / department combination
      IModelParticipant aiPerformerParticipant = activityInstance.getActivity().getPerformer();
      IDepartment participantDepartment 
         = DepartmentUtils.getDepartment(aiPerformerParticipant, activityInstance.getProcessInstance());
      IModel model = (IModel) activityInstance.getActivity().getModel();
      String participantProbabilityKey 
         = getParticipantProbabiltyKey(model.getId(), activityInstance.getActivity(), participantDepartment);
      
      if(preferences != null)
      {
         Map<String, Serializable> preferenceValues = preferences.getPreferences();
         if(preferenceValues != null)
         {
            Integer participantProbability = (Integer) preferenceValues.get(participantProbabilityKey);
            if(participantProbability != null)
            {
               return participantProbability;
            }
         } 
      }
      
      //return the probability modeled in the activity
      return activityInstance.getActivity().getQualityAssuranceProbability();
   }
   
   /**
    * Performs an Algorithm to decide if an activity instance should go under quality
    * assurance
    * 
    * @param probability - the probability in percentage (0-100), borders inclusive
    * @return if the activity instance should go under quality
    */
   public static boolean shouldQualityAssuranceBePerformed(IActivityInstance activityInstance)
   {   
      int probability = getQualityAssuranceProbability(activityInstance);

      //if quality control instance failed - state will be QualityAssuranceState.IS_REVISED
      //in that case - a new quality control needs to be performed
      if(activityInstance.getQualityAssuranceState() == QualityAssuranceState.IS_REVISED)
      {
         return true;
      }
      
      boolean isProbabilityCheckWinner = false;
      if (probability < 0 || probability > 100)
      {
         BpmRuntimeError errorCase =  BpmRuntimeError.BPMRT_INVALID_PROBABILIY.raise(probability);
         throw new InvalidArgumentException(errorCase);
      }

      if (probability == 0)
      {
         isProbabilityCheckWinner = false;
      }
      else if (probability == 100)
      {
         isProbabilityCheckWinner = true;
      }
      else
      {
         // get random number in the interval 0-100 (interval borders inclusive)
         Random numberGenerator = new Random();
         int randomNumber = numberGenerator.nextInt(101);

         if (randomNumber <= probability)
         {
            isProbabilityCheckWinner = true;
         }
      }
      
      //take qa formula into consideration
      boolean isFomulaEvaluationSuccessful = QualityAssuranceFormulaEvaluater.evaluate(activityInstance);
      
      return (isProbabilityCheckWinner && isFomulaEvaluationSuccessful);
   }
   
   public static QualityAssuranceInfo getQualityAssuranceInfo(IActivityInstance activityInstance)
   {
      ActivityInstance lastQcInstance = null;
      ActivityInstance monitoredInstance = null;
      
      Long failedQaInstanceOid 
         = (Long) activityInstance.getPropertyValue(QualityAssuranceInfo.FAILED_QUALITY_CONTROL_INSTANCE_OID);
      Long monitoredInstanceOid 
         = (Long) activityInstance.getPropertyValue(QualityAssuranceInfo.MONITORED_INSTANCE_OID);
          
      if(failedQaInstanceOid != null)
      {
         if(failedQaInstanceOid == activityInstance.getOID())
         {
            throw new InternalException(QualityAssuranceInfo.FAILED_QUALITY_CONTROL_INSTANCE_OID+": " +
                 "Infinite loop detected for activity instance oid: "+failedQaInstanceOid);
         }
         
         ActivityInstanceBean lastQcInstanceBean 
            = ActivityInstanceBean.findByOID(failedQaInstanceOid);
         lastQcInstance = new ActivityInstanceDetails(lastQcInstanceBean);
      }
      
      if(monitoredInstanceOid != null)
      {
         if(monitoredInstanceOid == activityInstance.getOID())
         {
            throw new InternalException(QualityAssuranceInfo.MONITORED_INSTANCE_OID+": " +
                 "Infinite loop detected for activity instance oid: "+monitoredInstanceOid);
         }
         
         ActivityInstanceBean monitoredInstanceBean
            = ActivityInstanceBean.findByOID(monitoredInstanceOid);
         monitoredInstance = new ActivityInstanceDetails(monitoredInstanceBean);
      }
         
      return new QualityAssuranceInfoImpl(lastQcInstance, monitoredInstance);
   }
   
   private static final class ActivityInstanceAttributesMapper
   {
      public static final String QC_ATTRIBUTES_KEY = "ActivityInstanceAttributes";
      
      public static Map<String, Serializable> getAsProperties(
            ActivityInstanceAttributes attributes)
      {
         Map<String, Serializable> properties = new HashMap<String, Serializable>();
         if(attributes != null)
         {
            properties.put(QC_ATTRIBUTES_KEY, attributes);
         }
         return properties;
      }

      public static ActivityInstanceAttributes getAsObject(
            Map<String, Serializable> properties)
      {
         ActivityInstanceAttributes attributes = null;
         try
         {
            ActivityInstanceProperty p = (ActivityInstanceProperty) properties.get(QC_ATTRIBUTES_KEY);
            if(p != null)
            {
               attributes = (ActivityInstanceAttributes) p.getValue();
            }
         }
         catch (Exception e)
         {
            throw new InternalException("Error during building ActivityInstanceAttributes", e);
         }

         return attributes;
      }
   }    
   
   public static String getParticipantProbabiltyKey(String modelId, Activity a, DepartmentInfo department)
   {
      // <Model ID>::<Process ID>::<Activity ID>::<Default Performer>{[<Department ID>]}
      String probabilityKey = "";
      probabilityKey = modelId + "::";
      probabilityKey += a.getProcessDefinitionId() + "::";
      probabilityKey += a.getId() + "::";      
      probabilityKey += a.getDefaultPerformer().getId();
      if(department != null)
      {
         probabilityKey += "{" + department.getId() + "}";
      }
      return probabilityKey;
   }
   
   public static String getParticipantProbabiltyKey(String modelId, IActivity a, IDepartment department)
   {
      // <Model ID>::<Process ID>::<Activity ID>::<Default Performer>{[<Department ID>]}
      String probabilityKey = "";
      probabilityKey = modelId + "::";
      probabilityKey += a.getProcessDefinition().getId() + "::";
      probabilityKey += a.getId() + "::";      
      probabilityKey += a.getPerformer().getId();
      if(department != null)
      {
         probabilityKey += "{" + department.getId() + "}";
      }
      return probabilityKey;
   }
   
   public static boolean isActivationAllowed(IActivityInstance activityInstance)
   {
      try 
      {
         assertActivationIsAllowed(activityInstance);
      }
      catch(IllegalOperationException ignored)
      {
         return false;
      }
      
      return true;
   }
   
   public static void assertCompletingIsAllowed(IActivityInstance activityInstance,  Map<String, ?> outData)
   {
      if(QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         // on complete the activity instance attributes must be set before
         ActivityInstanceAttributes attributes = getActivityInstanceAttributes(activityInstance);
         if(attributes == null)
         {
            BpmRuntimeError error 
               = BpmRuntimeError.BPMRT_COMPLETE_QA_NO_ATTRIBUTES_SET.raise(activityInstance.getOID());
            throw new IllegalOperationException(error);
         }
      }
   }
   
   public static boolean canDataMappingsBePerformed(IActivityInstance activityInstance,  Map<String, ?> outData, boolean ignoreMappingIfQaInstance)
   {
      if(QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         if(ignoreMappingIfQaInstance)
         {
            return false;
         }
         
         // on complete the activity instance attributes must be set before
         ActivityInstanceAttributes attributes = getActivityInstanceAttributes(activityInstance);
         // modifying entered data on qc instances is only allowed if in correction mode
         if (outData != null && !outData.isEmpty())
         {
            QualityAssuranceResult.ResultState resultState = attributes
               .getQualityAssuranceResult().getQualityAssuranceState();
            if (QualityAssuranceResult.ResultState.PASS_WITH_CORRECTION != resultState)
            {
               return false;
            }
         }
      }
      
      return true;
   }
   
   public static void assertDelegationIsAllowed(IActivityInstance activityInstance, IUser delegate)
   {
      //delegation of an qa instance to the user who is monitored 
      //(worked on the previous instance) is not allowed, even when the user would have the permission
      if(QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         IUser monitoredUser = QualityAssuranceUtils.getMonitoredUser(activityInstance);
         if(delegate.getOID() == monitoredUser.getOID())
         {
            BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_DELEGATE_QA_INSTANCE_NOT_ALLOWED.raise(
                  activityInstance.getOID(), monitoredUser.getOID());
            throw new IllegalOperationException(errorCase);
         }
      }
   }

   public static void assertActivationIsAllowed(IActivityInstance activityInstance)
   {
      if(QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         //get the instance that created this qa instance         
         IUser monitoredUser = QualityAssuranceUtils.getMonitoredUser(activityInstance);
         IUser currentUser = SecurityProperties.getUser();
         
         // the user who is monitored is not allowed to activate
         // qa instances (the instances that are monitoring)
         if (monitoredUser.getOID() == currentUser.getOID())
         {
            BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_USER_NOT_ALLOWED_ACTIVATE_QA_INSTANCE.raise(
                  monitoredUser.getOID(), activityInstance.getOID());
            throw new IllegalOperationException(errorCase);
         }
      }
   }
   
   public static void assertAttributesNotNull(ActivityInstanceAttributes attributes)
   {
      if(attributes == null)
      {
         BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("attributes");
         throw new InvalidArgumentException(errorCase);
      }
   }
   
   public static void validateActivityInstanceAttributes(
         ActivityInstanceAttributes attributes, IActivityInstance ai)
   {      
      if(isQualityAssuranceInstance(ai))
      {
         QualityAssuranceResult qaResult = attributes.getQualityAssuranceResult();
         if(qaResult == null)
         {
            BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_NULL_ATTRIBUTE.raise("qaResult");
            throw new InvalidArgumentException(errorCase);
         }
         
         if(qaResult.getQualityAssuranceState() == null)
         {
            BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_NULL_ATTRIBUTE.raise("resultState");
            throw new InvalidArgumentException(errorCase);
         }
         
         validateQaCodes(qaResult, ai.getActivity());
      }
   }
   

   private static void validateQaCodes(QualityAssuranceResult qaResult, IActivity activity)
   {
      //if passed with correction or failed, 
      //and error codes are available on the activity - at least one has to be specified
      boolean errorCodesRequired = false;
      ResultState resultState = qaResult.getQualityAssuranceState();
      if(resultState == ResultState.PASS_WITH_CORRECTION || resultState == ResultState.FAILED)
      {
         Set<IQualityAssuranceCode> errorCodesDefined = activity.getQualityAssuranceCodes();
         if(errorCodesDefined != null && !errorCodesDefined.isEmpty())
         {
            errorCodesRequired = true;
         }
      }
      
      if(errorCodesRequired)
      {
         Set<QualityAssuranceCode> qaCodesDefined 
            = qaResult.getQualityAssuranceCodes();
         
         if(qaCodesDefined == null || qaCodesDefined.isEmpty())
         {
            BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_NO_ERROR_CODE_SET.raise();
            throw new InvalidArgumentException(errorCase);
         }
         
         for(QualityAssuranceCode code: qaCodesDefined)
         {
            if(code == null)
            {
               BpmRuntimeError errorCase = BpmRuntimeError.BPMRT_NULL_ELEMENT_IN_COLLECTION.raise("qualityAssuranceCodes");
               throw new InvalidArgumentException(errorCase);
            }
         }
      }
   }
}