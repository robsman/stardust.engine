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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;



/**
 * @author stephan.born
 * @version $Revision: $
 */
public class ActivityInstanceUtils
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceUtils.class);

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

      final ImplementationType implementationType = activityInstance.getActivity()
            .getImplementationType();
      boolean abortAiDuringSubProcessAbortion = false;
      if (ImplementationType.SubProcess.equals(implementationType))
      {
         IProcessInstance subProcess = ProcessInstanceBean
               .findForStartingActivityInstance(activityInstance.getOID());

         if (subProcess != null)
         {
            ProcessInstanceUtils.abortProcessInstance(subProcess);
            abortAiDuringSubProcessAbortion = true;
         }
      }

      activityInstance.removeFromWorklists();
      activityInstance.setState(ActivityInstanceState.ABORTING);
      EventUtils.detachAll(activityInstance);
      
      if (!abortAiDuringSubProcessAbortion)
      {
         scheduleNewActivityThread(activityInstance);
      }
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

      if (AbortScope.RootHierarchy == abortScope)
      {
         IProcessInstance processInstance = ai.getProcessInstance();
         ProcessInstanceUtils.abortProcessInstance(ProcessInstanceUtils.getActualRootPI(processInstance));

      }
      else if (AbortScope.SubHierarchy == abortScope)
      {
         ActivityInstanceUtils.abortActivityInstance(ai);

      }
      else
      {
         throw new InternalError(MessageFormat.format("AbortScope {0} is not expected.",
               new Object[] { abortScope }));
      }
   }
   
   public static void scheduleNewActivityThread(IActivityInstance activityInstance)
         throws ConcurrencyException
   {
      String threadMode = Parameters.instance().getString(
            EngineProperties.PROCESS_TERMINATION_THREAD_MODE, EngineProperties.THREAD_MODE_ASYNCHRONOUS);
      boolean synchronously = threadMode.equals(EngineProperties.THREAD_MODE_SYNCHRONOUS);

      ActivityThread.schedule(null, null, activityInstance, synchronously, null,
            Collections.EMPTY_MAP, false);
   }
   
   public static void assertNotActivatedByOther(IActivityInstance ai)
         throws AccessForbiddenException
   {
      if (ai.getActivity().isInteractive())
      {
         long current = ai.getCurrentUserPerformerOID();
         if (current != SecurityProperties.getUserOID()
               && ai.getState() == ActivityInstanceState.Application)
         {
            throw new AccessForbiddenException(
                  BpmRuntimeError.BPMRT_AI_CURRENTLY_ACTIVATED_BY_OTHER.raise(new Long(ai
                        .getOID()), new Long(current)));
         }
      }
   }
   
   public static void assertNotTerminated(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      if (activityInstance.isTerminated())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_IS_ALREADY_TERMINATED.raise(activityInstance
                     .getOID()));
      }
   }
   
   public static void assertNotInAbortingProcess(IActivityInstance activityInstance)
         throws AccessForbiddenException
   {
      if (activityInstance.isAborting())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.BPMRT_AI_IS_IN_ABORTING_PROCESS.raise(activityInstance
                     .getOID()));
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
               BpmRuntimeError.BPMRT_AI_IS_NOT_GRANTED_TO_USER.raise(new Long(
                     activityInstance.getOID()), user));
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
               BpmRuntimeError.BPMRT_AI_MUST_NOT_BE_SUBPROCESS_INVOCATION
                     .raise(activityInstance.getOID()));
      }
   }

   public static void assertNotOnOtherUserWorklist(IActivityInstance activityInstance,
         boolean allowAdmin) throws AccessForbiddenException
   {
      IUser currentUser = SecurityProperties.getUser();
      if (isTransientSystemUser(currentUser))
      {
         return;
      }

      long currentUserPerformerOid = activityInstance.getCurrentUserPerformerOID();

      if (0 != currentUserPerformerOid && currentUserPerformerOid != currentUser.getOID())
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
                        new Long(activityInstance.getOID()), SecurityProperties.getUser()
                              .getRealmQualifiedAccount()));
         }
      }
   }

   public static void assertNotActivated(IActivityInstance activityInstance)
         throws ConcurrencyException
   {
      if (activityInstance.getState() == ActivityInstanceState.Application)
      {
         throw new ConcurrencyException(
               BpmRuntimeError.BPMRT_AI_CURRENTLY_ACTIVATED_BY_SELF.raise(new Long(
                     activityInstance.getOID())));
      }
   }

   /**
    * Test if the given user is a transient system user created with
    * {@link UserBean#createTransientUser(String, String, String, UserRealmBean)}.
    * 
    * @param user
    * @return true if it the transient user, otherwise false.
    */
   private static boolean isTransientSystemUser(IUser user)
   {
      return 0 == user.getOID() 
            && PredefinedConstants.SYSTEM.equals(user.getAccount())
            && PredefinedConstants.SYSTEM_REALM.equals(user.getRealm().getId());
   }

   private ActivityInstanceUtils()
   {
   }

   public static void assertNotDefaultCaseInstance(IActivityInstance activityInstance)
   {
      if (activityInstance.isDefaultCaseActivityInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_USER_IS_NOT_AUTHORIZED_TO_PERFORM_AI.raise(
               SecurityProperties.getUserOID(), activityInstance.getOID()));
      }
   }
}
