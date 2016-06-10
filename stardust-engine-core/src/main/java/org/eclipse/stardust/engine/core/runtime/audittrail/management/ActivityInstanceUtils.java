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
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;

/**
 * @author stephan.born
 * @version $Revision: $
 */
public class ActivityInstanceUtils
{

   private static final Logger trace = LogManager.getLogger(ActivityThread.class);

   /**
    * Lock activity to guarantee, that no other session can touch this activity
    */
   public static ActivityInstanceBean lock(long activityInstanceOID)
         throws ObjectNotFoundException
   {
      ActivityInstanceBean activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
      if (!activityInstance.isTerminated())
      {
         activityInstance.lockAndCheck();
      }
      return activityInstance;
   }

   public static void abortActivityInstance(long aiOid)
   {
      ActivityInstanceBean ai = ActivityInstanceBean.findByOID(aiOid);
      abortActivityInstance(ai);
   }

   public static void abortActivityInstance(IActivityInstance ai)
         throws ConcurrencyException
   {
      ai.lock();

      ActivityInstanceBean activityInstance = (ActivityInstanceBean) ai;

      activityInstance.removeFromWorklists();
      activityInstance.setState(ActivityInstanceState.ABORTING);
      EventUtils.detachAll(activityInstance);

      ImplementationType implementationType = activityInstance.getActivity().getImplementationType();
      if (ImplementationType.SubProcess.equals(implementationType))
      {
         IProcessInstance subProcess = ProcessInstanceBean.findForStartingActivityInstance(ai.getOID());
         if (subProcess != null)
         {
            ProcessInstanceUtils.abortProcessInstance(subProcess);
            return;
         }
      }
      scheduleNewActivityThread(ai);
   }

   public static void abortActivityInstance(long aiOid, AbortScope abortScope)
   {
      ActivityInstanceBean ai = ActivityInstanceBean.findByOID(aiOid);
      abortActivityInstance(ai, abortScope);
   }

   public static void abortActivityInstance(IActivityInstance ai, AbortScope abortScope)
   {
      assertNotTerminated(ai);
      assertNotInAbortingProcess(ai);
      assertNotActivatedByOther(ai);

      IProcessInstance pi = ai.getProcessInstance();

      if (AbortScope.RootHierarchy == abortScope)
      {
         IProcessInstance processInstance = ai.getProcessInstance();
         ProcessInstanceUtils.abortProcessInstance(ProcessInstanceUtils.getActualRootPI(processInstance));

      }
      else if (AbortScope.SubHierarchy == abortScope)
      {
         // in case that process instance is already aborted but not its activity
         if (isProcessInstanceAborted(ai, pi))
         {
            ((ActivityInstanceBean) ai).setState(ActivityInstanceState.ABORTED);
         }
         else
         {
            ActivityInstanceUtils.abortActivityInstance(ai);
         }
      }
      else
      {
         throw new InternalError(MessageFormat.format("AbortScope {0} is not expected.",
               new Object[] { abortScope }));
      }

      // in case that process instance is already aborted but not its activity
      if (isProcessInstanceAborted(ai, pi))
      {
         ((ActivityInstanceBean) ai).setState(ActivityInstanceState.ABORTED);
      }

   }

   private static boolean isProcessInstanceAborted(IActivityInstance ai,
         IProcessInstance pi)
   {
      return ProcessInstanceUtils.isInAbortingPiHierarchy(pi)
            && pi.getState().equals(ProcessInstanceState.Aborted) && !ai.isAborting()
            && !ai.isTerminated();
   }

   public static void scheduleNewActivityThread(IActivityInstance activityInstance)
         throws ConcurrencyException
   {
      String threadMode = Parameters.instance().getString(
            EngineProperties.PROCESS_TERMINATION_THREAD_MODE, EngineProperties.THREAD_MODE_ASYNCHRONOUS);

      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      boolean synchronously = rtEnv.getExecutionPlan() != null || threadMode.equals(EngineProperties.THREAD_MODE_SYNCHRONOUS);

      ActivityThread.schedule(null, null, activityInstance, synchronously, null,
            Collections.EMPTY_MAP, false);
   }

   public static void assertNotActivatedByOther(IActivityInstance ai)
         throws AccessForbiddenException
   {
      assertNotActivatedByOther(ai, false);
   }

   public static void assertNotActivatedByOther(IActivityInstance ai,
         boolean ignoreDeputies) throws AccessForbiddenException
   {

      if (ai.getActivity().isInteractive())
      {
         long current = ai.getCurrentUserPerformerOID();
         if (current != SecurityProperties.getUserOID()
               && ai.getState() == ActivityInstanceState.Application
               && (ignoreDeputies || !UserUtils.isDeputyOf(SecurityProperties.getUser(), current)))
         {
            String account = null;
            try
            {
               account = UserBean.findByOid(current).getAccount();
            }
            catch (ObjectNotFoundException ex)
            {
               account = "<not found>";
            }
            IActivity a = ai.getActivity();
            IProcessDefinition p = a.getProcessDefinition();
            throw new AccessForbiddenException(
                  BpmRuntimeError.BPMRT_AI_CURRENTLY_ACTIVATED_BY_OTHER.raise(
                        ai.getOID(), current, account, a.getId(), p.getId(), p.getModel()
                              .getId()));
         }
      }
   }

   public static void assertNotTerminated(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      if (activityInstance.isTerminated())
      {
         throw new AccessForbiddenException(BpmRuntimeError.BPMRT_AI_IS_ALREADY_TERMINATED
               .raise(activityInstance.getOID(), activityInstance.getActivity().getId()));
      }
   }

   public static void assertNotInAbortingProcess(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      if (activityInstance.isAborting())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_IS_IN_ABORTING_PROCESS.raise(
                     activityInstance.getOID(), activityInstance.getActivity().getId()));
      }
   }

   public static void assertGrantedAccess(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      IUser user = SecurityProperties.getUser();
      IActivity activity = activityInstance.getActivity();
      if (null != user
            && 0 != user.getOID()
            && null != activity.getPerformer()
            && !activity.getPerformer().isAuthorized(user))
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_IS_NOT_GRANTED_TO_USER.raise(
                     new Long(activityInstance.getOID()),
                     activityInstance.getActivity().getId(), user.getId(), user));
      }
   }

   public static boolean isAdmin(IActivityInstance activityInstance)
   {
      IUser user = SecurityProperties.getUser();
      if (user != null && user.getOID() != 0)
      {
         IActivity activity = activityInstance.getActivity();
         IModel model = (IModel) activity.getModel();
         IModelParticipant admin = model.findParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);
         if (admin != null)
         {
            return admin.isAuthorized(user);
         }
      }
      return false;
   }

   public static void assertNoSubprocess(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      if (ImplementationType.SubProcess.equals(activityInstance.getActivity()
            .getImplementationType()))
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_MUST_NOT_BE_SUBPROCESS_INVOCATION.raise(
                     activityInstance.getOID(), activityInstance.getActivity().getId()));
      }
   }

   public static void assertNotInUserWorklist(IActivityInstance activityInstance,
         ParticipantInfo participant) throws AccessForbiddenException
   {
      IParticipant currentPerformer = activityInstance.getCurrentPerformer();
      if(currentPerformer instanceof IUserGroup)
      {
         if(participant instanceof UserInfo)
         {
            long oid = ((UserInfo) participant).getOID();
            for (Iterator i = ((IUserGroup) currentPerformer).findAllUsers(); i.hasNext();)
            {
               IUser user = (IUser) i.next();
               long memberOid = user.getOID();
               if(oid == memberOid)
               {
                  return;
               }
            }
         }

         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_CAN_NOT_BE_DELEGATED_TO_NON_USERGROUP_MEMBER
                     .raise(new Long(activityInstance.getOID()),
                           activityInstance.getActivity().getId()));
      }
   }

   public static void assertNotOnOtherUserWorklist(IActivityInstance activityInstance,
         boolean allowAdmin) throws AccessForbiddenException
   {
      IUser currentUser = SecurityProperties.getUser();
      if (isSystemUser(currentUser))
      {
         return;
      }

      long currentUserPerformerOid = activityInstance.getCurrentUserPerformerOID();

      if (0 != currentUserPerformerOid && currentUserPerformerOid != currentUser.getOID()
            && !UserUtils.isDeputyOf(currentUser, currentUserPerformerOid))
      {
         boolean isAdmin = false;
         if (allowAdmin)
         {
            isAdmin = currentUser.hasRole(PredefinedConstants.ADMINISTRATOR_ROLE);
         }

         if ( !isAdmin)
         {
            throw new AccessForbiddenException(
                  BpmRuntimeError.BPMRT_AI_MUST_NOT_BE_ON_OTHER_USER_WORKLIST.raise(
                        new Long(activityInstance.getOID()),
                        activityInstance.getActivity().getId(),
                        SecurityProperties.getUser().getRealmQualifiedAccount()));
         }
      }
   }

   public static void assertNotActivated(IActivityInstance activityInstance)
         throws ConcurrencyException
   {
      if (activityInstance.getState() == ActivityInstanceState.Application)
      {
         throw new ConcurrencyException(
               BpmRuntimeError.BPMRT_AI_CURRENTLY_ACTIVATED_BY_SELF.raise(
                     new Long(activityInstance.getOID()),
                     activityInstance.getActivity().getId()));
      }
   }

   /**
    * Test if the given user is the system user.
    *
    * @param user
    * @return true if it the transient user, otherwise false.
    */
   private static boolean isSystemUser(IUser user)
   {
      return user == UserBean.getSystemUser(SecurityProperties.getPartition());
   }

   private ActivityInstanceUtils()
   {
   }

   public static void assertNotDefaultCaseInstance(IActivityInstance activityInstance)
   {
      if (activityInstance.isDefaultCaseActivityInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_USER_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
               SecurityProperties.getUser().getId(), SecurityProperties.getUserOID(), activityInstance.getOID(), activityInstance.getActivity().getId()));
      }
   }


   public static void setOutDataValues(String context, Map<String, ?> values,
         IActivityInstance activityInstance, boolean ignoreMappingIfQaInstance) throws ObjectNotFoundException, InvalidValueException
   {
      boolean performDataMapping = true;
      if (QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         performDataMapping = QualityAssuranceUtils.canDataMappingsBePerformed(
               activityInstance, values, ignoreMappingIfQaInstance);
         if (!performDataMapping)
         {
            StringBuffer errorMessage = new StringBuffer();
            errorMessage.append("Datamapping will not be executed for qa instance with oid");
            errorMessage.append("'");
            errorMessage.append(activityInstance.getOID());
            errorMessage.append("'");
            trace.warn(errorMessage);

            return;
         }
      }

      if (null == context)
      {
         context = PredefinedConstants.DEFAULT_CONTEXT;
      }
      if (values != null && !values.isEmpty())
      {
         IActivity activity = activityInstance.getActivity();
         IProcessInstance processInstance = ProcessInstanceBean.findByOID(activityInstance.getProcessInstanceOID());
         for (Map.Entry<String, ?> entry : values.entrySet())
         {
            IDataMapping dm = activity.findDataMappingById(entry.getKey(), Direction.OUT, context);
            if (dm == null)
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_UNKNOWN_OUT_DATA_MAPPING.raise(entry.getKey(),
                           context, activity.getId(), activityInstance.getOID()));
            }
            ((ProcessInstanceBean)processInstance).setOutDataValue(dm.getData(), dm.getDataPath(), entry.getValue(), new DataMappingContext(activityInstance));
         }
      }
   }

   public static void setOutDataValues(String context, Map<String, ?> values,
         IActivityInstance activityInstance) throws ObjectNotFoundException, InvalidValueException
   {
      setOutDataValues(context, values, activityInstance, false);
   }

   public static void complete(IActivityInstance activityInstance, String context,
         Map<String, ? > outData, boolean synchronously)
   {
      if (QualityAssuranceUtils.isQualityAssuranceInstance(activityInstance))
      {
         QualityAssuranceUtils.assertCompletingIsAllowed(activityInstance, outData);
      }

      setOutDataValues(context, outData, activityInstance);
      ActivityThread.schedule(null, null, activityInstance, synchronously, null,
            Collections.EMPTY_MAP, false);
   }

   public static boolean isTransientExecutionScenario(final IActivityInstance ai)
   {
      if ( !ProcessInstanceUtils.isTransientPiSupportEnabled())
      {
         return false;
      }

      if (ai != null)
      {
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(ai.getProcessInstance());
         return AuditTrailPersistence.isTransientExecution(rootPi.getAuditTrailPersistence());
      }
      return false;
   }

   public static boolean isActiveState(IActivityInstance activityInstance)
   {
      return !activityInstance.isTerminated() && !activityInstance.isAborting()
            && !activityInstance.isHalted();
   }

   public static boolean isHaltable(IActivityInstance activityInstance)
   {
      return activityInstance != null
            // terminated activities are terminated !!!
            && (!activityInstance.isTerminated()
            // already halted, nothing more to do here.
            && !activityInstance.isHalted()
            // is in process of aborting, do not halt.
            && !activityInstance.isAborting()
            || ActivityInstanceState.Application.equals(activityInstance.getState())
               && activityInstance.getActivity().isInteractive());
   }

   public static void assertPerformAllowedIfHalted(IActivityInstance ai)
   {
      if (ai.isHalted() && !wasPreviouslyActivated(ai))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_IS_HALTED.raise(ai.getProcessInstanceOID()));
      }
   }

   private static boolean wasPreviouslyActivated(IActivityInstance activityInstance)
   {
      // check last states must be Halted and Application in that order
      Iterator<ActivityInstanceHistoryBean> historicStates = ActivityInstanceHistoryBean.getAllForActivityInstance(activityInstance, false);
      if (historicStates != null && historicStates.hasNext()
            && ActivityInstanceState.Halted == historicStates.next().getState())
      {
         if (historicStates.hasNext()
               && ActivityInstanceState.Application == historicStates.next().getState())
         {
            return true;
         }
      }
      return false;
   }

   public static void assertNotHalted(IActivityInstance ai)
   {
      if (ai.isHalted())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_AI_IS_HALTED.raise(ai.getOID()));
      }

      if (isHaltable(ai)
            && ProcessInstanceUtils.isInHaltingPiHierarchy(ai.getProcessInstance()))
      {
         // found an AI which is in halted hierarchy but not in halted state.

         // reschedule halt janitor.
         ProcessHaltJanitor.scheduleSeparate(ai.getProcessInstanceOID(), 0);

         // cancel current service call.
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_PI_IS_HALTED.raise(ai.getProcessInstanceOID()));
      }
   }

   public static boolean isMandatoryDatamapping(IDataMapping dataMapping, long activityInstanceOID)
   {
      IData data = dataMapping.getData();
      if(data == null)
      {
         return false;
      }

      IDataType dataType = (IDataType) data.getType();
      if(PredefinedConstants.PRIMITIVE_DATA.equals(dataType.getId()))
      {
         Type type = data.getAttribute(PredefinedConstants.TYPE_ATT);
         if (Type.Boolean == type
               || Type.String == type
               || Type.Enumeration == type)
         {
            return false;
         }
      }
      else
      {
         return false;
      }

      IActivityInstance ai = ActivityInstanceBean.findByOID(activityInstanceOID);
      IActivity activity = ai.getActivity();

      IDataMapping mandatoryDataMapping = null;
      ModelElementList outDataMappings = activity.getOutDataMappings();
      for (int i = 0; i < outDataMappings.size(); ++i)
      {
         IDataMapping outDataMapping = (IDataMapping) outDataMappings.get(i);
         if(outDataMapping.getId().equals(dataMapping.getId())
               && outDataMapping.getData().equals(dataMapping.getData())
               && outDataMapping.getContext().equals(dataMapping.getContext()))
         {
            mandatoryDataMapping = outDataMapping;
            break;
         }
      }

      if(mandatoryDataMapping != null && mandatoryDataMapping.getBooleanAttribute(PredefinedConstants.MANDATORY_DATA_MAPPING))
      {
         if(data.getAttribute(PredefinedConstants.DEFAULT_VALUE_ATT) == null)
         {
            int cnt = 0;
            Iterator<ActivityInstanceHistoryBean> history = ActivityInstanceHistoryBean.getAllForActivityInstance(
            ActivityInstanceBean.findByOID(activityInstanceOID));
            while (history.hasNext())
            {
               ActivityInstanceHistoryBean aih = history.next();
               if (ActivityInstanceState.Application == aih.getState())
               {
                  cnt++;
               }
            }
            if(cnt == 1)
            {
               return true;
            }
         }
      }

      return false;
   }
}