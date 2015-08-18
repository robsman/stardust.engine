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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.ActivityExecutionUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.IActivityExecutionStrategy;



/**
 * @author mgille
 */
public class WorkItemBean extends PersistentBean implements IWorkItem, IProcessInstanceAware
{
   public static final String FIELD__ACTIVITY_INSTANCE = "activityInstance";
   public static final String FIELD__PROCESS_INSTANCE = ActivityInstanceBean.FIELD__PROCESS_INSTANCE;
   public static final String FIELD__ROOT_PROCESS_INSTANCE = ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE;
   public static final String FIELD__SCOPE_PROCESS_INSTANCE = ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE;
   public static final String FIELD__MODEL = ActivityInstanceBean.FIELD__MODEL;
   public static final String FIELD__ACTIVITY = ActivityInstanceBean.FIELD__ACTIVITY;
   public static final String FIELD__STATE = ActivityInstanceBean.FIELD__STATE;
   public static final String FIELD__START_TIME = ActivityInstanceBean.FIELD__START_TIME;
   public static final String FIELD__LAST_MODIFICATION_TIME = ActivityInstanceBean.FIELD__LAST_MODIFICATION_TIME;
   public static final String FIELD__DOMAIN = "domain";
   public static final String FIELD__PERFORMER_KIND = "performerKind";
   public static final String FIELD__PERFORMER = "performer";
   public static final String FIELD__DEPARTMENT = "department";
   public static final String FIELD__CRITICALITY = "criticality";

   public static final FieldRef FR__ACTIVITY_INSTANCE = new FieldRef(WorkItemBean.class, FIELD__ACTIVITY_INSTANCE);
   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(WorkItemBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__ROOT_PROCESS_INSTANCE = new FieldRef(WorkItemBean.class, FIELD__ROOT_PROCESS_INSTANCE);
   public static final FieldRef FR__SCOPE_PROCESS_INSTANCE = new FieldRef(WorkItemBean.class, FIELD__SCOPE_PROCESS_INSTANCE);
   public static final FieldRef FR__MODEL = new FieldRef(WorkItemBean.class, FIELD__MODEL);
   public static final FieldRef FR__ACTIVITY = new FieldRef(WorkItemBean.class, FIELD__ACTIVITY);
   public static final FieldRef FR__STATE = new FieldRef(WorkItemBean.class, FIELD__STATE);
   public static final FieldRef FR__START_TIME = new FieldRef(WorkItemBean.class, FIELD__START_TIME);
   public static final FieldRef FR__LAST_MODIFICATION_TIME = new FieldRef(WorkItemBean.class, FIELD__LAST_MODIFICATION_TIME);
   public static final FieldRef FR__DOMAIN = new FieldRef(WorkItemBean.class, FIELD__DOMAIN);
   public static final FieldRef FR__PERFORMER_KIND = new FieldRef(WorkItemBean.class, FIELD__PERFORMER_KIND);
   public static final FieldRef FR__PERFORMER = new FieldRef(WorkItemBean.class, FIELD__PERFORMER);
   public static final FieldRef FR__DEPARTMENT = new FieldRef(WorkItemBean.class, FIELD__DEPARTMENT);
   public static final FieldRef FR__CRITICALITY = new FieldRef(WorkItemBean.class, FIELD__CRITICALITY);

   public static final String TABLE_NAME = "workitem";
   public static final String DEFAULT_ALIAS = "wi";
   public static final String PK_FIELD = FIELD__ACTIVITY_INSTANCE;
   public static final String[] workitem_idx1_UNIQUE_INDEX =
      new String[]{FIELD__ACTIVITY_INSTANCE};
   // TODO: add index for department
   public static final String[] workitem_idx2_INDEX =
         new String[]{FIELD__PERFORMER, FIELD__DEPARTMENT, FIELD__PERFORMER_KIND, FIELD__STATE};
   public static final String[] workitem_idx3_INDEX =
      new String[]{FIELD__STATE};
   public static final String[] workitem_idx4_INDEX =
         new String[]{FIELD__PROCESS_INSTANCE};
   public static final String[] workitem_idx5_INDEX =
      new String[]{FIELD__SCOPE_PROCESS_INSTANCE};

   static final boolean state_USE_LITERALS = true;

   private long activityInstance;

   private long processInstance;

   private long scopeProcessInstance;

   private long rootProcessInstance;

   /**
    * Contains the OID of the activity.
    */
   protected long model;

   protected long activity;

   private int state;

   private long startTime;

   private long lastModificationTime;

   // TODO domain OID
   private long domain;

   private double criticality;


   /**
    * Contains the performer type according to  {@link PerformerType}.
    */
   private int performerKind;

   /**
    * Contains the OID of the associated performer. The performer type is encoded in field
    * {@link #performerKind}.
    */
   private long performer;

   private long department;

   /**
    * Returns all activity instances, whose current performer equals
    * <tt>currentPerformer</tt>.
    */
   public static WorkItemBean findByOID(long aiOid)
      throws ObjectNotFoundException
   {
      if (0 == aiOid)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_WORK_ITEM_OID.raise(0), 0);
      }
      WorkItemBean result = (WorkItemBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(WorkItemBean.class, aiOid);
      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_WORK_ITEM_OID.raise(aiOid), aiOid);
      }
      return result;
   }

   public static List<WorkItemBean> findByProcessInstanceRootOid(long processInstanceRootOid)
   {
      Vector vector = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
      .getVector(WorkItemBean.class,
            QueryExtension.where(Predicates.isEqual(FR__ROOT_PROCESS_INSTANCE, processInstanceRootOid)));

      return vector != null ? new ArrayList<WorkItemBean>(vector) : Collections.EMPTY_LIST;
   }

   /**
    * Constructor for persistence framework.
    */
   public WorkItemBean()
   {
   }

   public WorkItemBean(IActivityInstance activityInstance, IProcessInstance processInstance)
   {
      this.activityInstance = activityInstance.getOID();
      this.processInstance = processInstance.getOID();
      this.scopeProcessInstance = processInstance.getScopeProcessInstanceOID();
      this.rootProcessInstance = processInstance.getRootProcessInstanceOID();

      this.model = activityInstance.getActivity().getModel().getModelOID();
      this.activity = ModelManagerFactory.getCurrent().getRuntimeOid(
            activityInstance.getActivity());

      this.startTime = activityInstance.getStartTime().getTime();

      this.criticality = activityInstance.getCriticality();

      update(activityInstance);

      // TODO performers

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public void update(IActivityInstance activityInstance)
   {
      // support polymorphism wrt. activity definition
      IActivityExecutionStrategy aeStrategy = ActivityExecutionUtils.getExecutionStrategy(getActivity());
      if (null != aeStrategy)
      {
         aeStrategy.updateWorkItem(activityInstance, this);
      }
      else
      {
         doUpdate(activityInstance);
      }
   }

   public void doUpdate(IActivityInstance activityInstance)
   {
      Assert.condition(
            ActivityInstanceState.Suspended.equals(activityInstance.getState())
                  || ActivityInstanceState.Application.equals(activityInstance.getState()),
            MessageFormat.format(
                  "AI must be either supended or active, but not in state {0}.",
                  new Object[] {activityInstance.getState()}));
      if (state != activityInstance.getState().getValue())
      {
         markModified(FIELD__STATE);
         this.state = activityInstance.getState().getValue();
      }

      if (lastModificationTime != activityInstance.getLastModificationTime().getTime())
      {
         markModified(FIELD__LAST_MODIFICATION_TIME);
         this.lastModificationTime = activityInstance.getLastModificationTime().getTime();
      }

      if (0 != activityInstance.getCurrentUserPerformerOID())
      {
         if ((PerformerType.USER != performerKind)
               || (performer != activityInstance.getCurrentUserPerformerOID()))
         {
            markModifiedPerformerFields();
            this.performerKind = PerformerType.USER;
            this.performer = activityInstance.getCurrentUserPerformerOID();
            this.department = 0;
         }
      }
      else if (0 < activityInstance.getCurrentPerformerOID())
      {
         if ((PerformerType.MODEL_PARTICIPANT != performerKind)
               || (performer != activityInstance.getCurrentPerformerOID())
               || (department != activityInstance.getCurrentDepartmentOid()))
         {
            markModifiedPerformerFields();
            this.performerKind = PerformerType.MODEL_PARTICIPANT;
            this.performer = activityInstance.getCurrentPerformerOID();

            // TODO: department as oid or bean.
            IDepartment aiDepartment = activityInstance.getCurrentDepartment();
            this.department = aiDepartment == null ? 0 : aiDepartment.getOID();
         }
      }
      else if (0 > activityInstance.getCurrentPerformerOID())
      {
         if ((PerformerType.USER_GROUP != performerKind)
               || (performer != activityInstance.getCurrentPerformerOID()))
         {
            markModifiedPerformerFields();
            this.performerKind = PerformerType.USER_GROUP;
            this.performer = -activityInstance.getCurrentPerformerOID();
            this.department = 0;
         }
      }
      else
      {
         if ((PerformerType.NONE != performerKind)
               || (performer != 0))
         {
            markModifiedPerformerFields();
            this.performerKind = PerformerType.NONE;
            this.performer = 0;
            this.department = 0;
         }
      }
      if (this.criticality != activityInstance.getCriticality())
      {
         markModified(FIELD__CRITICALITY);
         this.criticality = activityInstance.getCriticality();
      }
   }

   private void markModifiedPerformerFields()
   {
      markModified(FIELD__PERFORMER_KIND);
      markModified(FIELD__PERFORMER);
      markModified(FIELD__DEPARTMENT);
   }

   /**
    * @return The stringified representation of this activity.
    */
   public String toString()
   {
      // TODO
      IActivity activity = getActivity();
      Assert.isNotNull(activity);
      return "Activity instance '" + activity.getId() + "',  oid: "
            + getActivityInstanceOID() + " (process instance = "
            + getProcessInstanceOID() + ")";
   }

   public long getActivityInstanceOID()
   {
      fetch();
      return activityInstance;
   }

   public long getProcessInstanceOID()
   {
      fetch();
      return processInstance;
   }

   public long getScopeProcessInstanceOID()
   {
      fetch();
      return scopeProcessInstance;
   }

   public long getRootProcessInstanceOID()
   {
      fetch();
      return rootProcessInstance;
   }

   public void setRootProcessInstance(long rootProcessInstance)
   {
      fetch();

      markModified(FIELD__ROOT_PROCESS_INSTANCE);
      this.rootProcessInstance = rootProcessInstance;
   }

   public IActivity getActivity()
   {
      fetch();

      IActivity result = ModelManagerFactory.getCurrent().findActivity(model, activity);
      if (null == result)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_ACTIVITY_FOR_WORK_ITEM.raise(activity,
                     getActivityInstanceOID()), activity);
      }
      return result;
   }

   /**
    * @return The state of the associated activity instance.
    */
   public ActivityInstanceState getState()
   {
      fetch();
      return (ActivityInstanceState) IntKey.getKey(ActivityInstanceState.class, state);
   }

   public Date getStartTime()
   {
      fetch();
      return new Date(startTime);
   }

   public Date getLastModificationTime()
   {
      fetch();
      return new Date(lastModificationTime);
   }

   /**
    * Retrieves the user, the activity instance is assigned to, if
    * the activity is assigned to a user.
    * Returns <tt>null</tt> otherwise.
    */
   public IUser getCurrentUserPerformer()
   {
      fetch();

      if ((PerformerType.USER == performerKind) && (0 != performer))
      {
         return UserBean.findByOid(performer);
      }
      else
      {
         return null;
      }
   }

   /**
    * Retrieves the participant, the activity instance is currently assigned to,
    * if the activity is assigned to a participant.
    * Returns <tt>null</tt> otherwise.
    */
   public IParticipant getCurrentPerformer()
   {
      fetch();

      if ((PerformerType.MODEL_PARTICIPANT == performerKind) && (0 < performer))
      {
         return ModelManagerFactory.getCurrent().findModelParticipant(
               getActivity().getModel().getModelOID(), performer);
      }
      else if ((PerformerType.USER_GROUP == performerKind) && (0 != performer))
      {
         return UserGroupBean.findByOid(performer);
      }
      else
      {
         return null;
      }
   }

   public IDepartment getDepartment()
   {
      fetch();

      if (0 != department)
      {
         return DepartmentBean.findByOID(department);
      }
      else
      {
         return null;
      }
   }

   public long getDepartmentOid()
   {
      fetch();
      return department;
   }

   public long getCurrentPerformerOID()
   {
      fetch();
      switch (performerKind)
      {
      case PerformerType.MODEL_PARTICIPANT:
         return performer;
      case PerformerType.USER_GROUP:
         return -performer;
      default: return 0;
      }
   }

   public long getCurrentUserPerformerOID()
   {
      fetch();
      if (PerformerType.USER == performerKind)
      {
         return performer;
      }
      return 0;
   }

   public double getCriticality()
   {
      fetch();
      return criticality;
   }

   @Override
   public IProcessInstance getProcessInstance()
   {
      return ProcessInstanceBean.findByOID(getProcessInstanceOID());
   }
}

