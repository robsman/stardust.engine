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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils;
import org.eclipse.stardust.engine.core.runtime.utils.PerformerUtils.EncodedPerformer;

/**
 *
 */
public class ActivityInstanceHistoryBean extends PersistentBean
      implements IActivityInstanceHistory
{
   public static final String FIELD__PROCESS_INSTANCE = ActivityInstanceBean.FIELD__PROCESS_INSTANCE;
   public static final String FIELD__ACTIVITY_INSTANCE = WorkItemBean.FIELD__ACTIVITY_INSTANCE;
   public static final String FIELD__STATE = ActivityInstanceBean.FIELD__STATE;
   public static final String FIELD__FROM = "fromTimestamp";
   public static final String FIELD__UNTIL = "untilTimestamp";
   public static final String FIELD__DOMAIN = WorkItemBean.FIELD__DOMAIN;
   public static final String FIELD__PERFORMER_KIND = WorkItemBean.FIELD__PERFORMER_KIND;
   public static final String FIELD__PERFORMER = WorkItemBean.FIELD__PERFORMER;
   public static final String FIELD__DEPARTMENT = WorkItemBean.FIELD__DEPARTMENT;
   public static final String FIELD__ON_BEHALF_OF_KIND = "onBehalfOfKind";
   public static final String FIELD__ON_BEHALF_OF = "onBehalfOf";
   public static final String FIELD__ON_BEHALF_OF_DEPARTMENT = "onBehalfOfDepartment";
   public static final String FIELD__ON_BEHALF_OF_USER = "onBehalfOfUser";
   public static final String FIELD__USER = "workflowUser";

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__ACTIVITY_INSTANCE = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__ACTIVITY_INSTANCE);
   public static final FieldRef FR__STATE = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__STATE);
   public static final FieldRef FR__FROM = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__FROM);
   public static final FieldRef FR__UNTIL = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__UNTIL);
   public static final FieldRef FR__DOMAIN = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__DOMAIN);
   public static final FieldRef FR__PERFORMER_KIND = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__PERFORMER_KIND);
   public static final FieldRef FR__PERFORMER = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__PERFORMER);
   public static final FieldRef FR__DEPARTMENT = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__DEPARTMENT);
   public static final FieldRef FR__ON_BEHALF_OF_KIND = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__ON_BEHALF_OF_KIND);
   public static final FieldRef FR__ON_BEHALF_OF = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__ON_BEHALF_OF);
   public static final FieldRef FR__ON_BEHALF_OF_DEPARTMENT = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__ON_BEHALF_OF_DEPARTMENT);
   public static final FieldRef FR__ON_BEHALF_OF_USER = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__ON_BEHALF_OF_USER);
   public static final FieldRef FR__USER = new FieldRef(ActivityInstanceHistoryBean.class, FIELD__USER);

   public static final String TABLE_NAME = "act_inst_history";
   public static final String DEFAULT_ALIAS = "aih";
   public static final String[] PK_FIELD = new String[] {FIELD__ACTIVITY_INSTANCE, FIELD__FROM};
   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] act_inst_hist_idx1_UNIQUE_INDEX =
         new String[]{FIELD__ACTIVITY_INSTANCE, FIELD__FROM};
   public static final String[] act_inst_hist_idx2_INDEX =
      new String[]{FIELD__PROCESS_INSTANCE};

   static final boolean state_USE_LITERALS = true;
   static final boolean performerKind_USE_LITERALS = true;
   static final boolean onBehalfOfKind_USE_LITERALS = true;

   private long processInstance;

   private long activityInstance;

   private int state;

   private long fromTimestamp;

   private long untilTimestamp;

   // TODO domain OID
   @SuppressWarnings("unused")
   private long domain;

   /**
    * Contains the performer type according to  {@link PerformerType}.
    */
   private int performerKind;

   /**
    * Contains the OID of the associated performer. The performer type is encoded in field
    * {@link #performerKind}.
    */
   private long performer;

   /**
    * Contains the OID of the associated department.
    */
   private long department;

   /**
    * Contains the performer type according to {@link PerformerType}, but excluding
    * {@link PerformerType#User}.
    */
   private int onBehalfOfKind;

   /**
    * Contains the OID of the performer this activity was executed on behalf of. The
    * on-behalf-of performer type is encoded in field {@link #onBehalfOfKind}.
    */
   private long onBehalfOf;

   /**
    * Contains the OID of the department this activity was executed on behalf of.
    */
   private long onBehalfOfDepartment;

   /**
    * Contains the OID of the user this activity was executed on behalf of.
    */
   private long onBehalfOfUser;


   private long workflowUser;

   /**
    * Gets all historic states instantiated on behalf of the activity instance
    * <tt>activityInstance</tt> in ascending order with respect to field {@link #FR__FROM} .
    */
   public static Iterator getAllForActivityInstance(IActivityInstance activityInstance)
   {
      return getAllForActivityInstance(activityInstance, true);
   }

   /**
    * Gets last historic state instantiated on behalf of the activity instance
    * <tt>activityInstance</tt> in given order with respect to field {@link #FR__FROM} .
    */
   public static Pair<ActivityInstanceHistoryBean, IUser> getLastForActivityInstance(
         IActivityInstance activityInstance)
   {
      return getLast(activityInstance, null);
   }

   /**
    * Gets last historic state that contain a user performer instantiated on behalf of the
    * activity instance <tt>activityInstance</tt> in given order with respect to field
    * {@link #FR__FROM} .
    */
   public static Pair<ActivityInstanceHistoryBean, IUser> getLastUserPerformerForActivityInstance(
         IActivityInstance activityInstance)
   {
      ComparisonTerm statePredicate = Predicates.isEqual(FR__STATE,
            ActivityInstanceState.APPLICATION);
      return getLast(activityInstance, statePredicate);
   }

   private static Pair<ActivityInstanceHistoryBean, IUser> getLast(
         IActivityInstance activityInstance, ComparisonTerm filter)
   {
      ComparisonTerm oidPredicate = Predicates.isEqual(FR__ACTIVITY_INSTANCE,
            activityInstance.getOID());
      QueryExtension qe = QueryExtension.where(filter == null
            ? oidPredicate
            : Predicates.andTerm(oidPredicate, filter));
      qe.getOrderCriteria().add(ActivityInstanceHistoryBean.FR__FROM, false);
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if ( !activityInstance.isTerminated())
      {
         return new Pair(session.findFirst(ActivityInstanceHistoryBean.class, qe), null);
      }
      else
      {
         ResultIterator<ActivityInstanceHistoryBean> i = session.getIterator(
               ActivityInstanceHistoryBean.class, qe, 0, 2);
         try
         {
            IUser onBehalfOf = null;
            ActivityInstanceHistoryBean candidate = null;
            if (i.hasNext())
            {
               candidate = i.next();
               if (candidate.isTerminated())
               {
                  onBehalfOf = candidate.getOnBehalfOfUser();
                  if (i.hasNext())
                  {
                     candidate = i.next();
                  }
                  else
                  {
                     candidate = null;
                  }
               }
            }
            return new Pair(candidate, onBehalfOf);
         }
         finally
         {
            i.close();
         }
      }
   }

      private boolean isTerminated()
      {
         ActivityInstanceState state = getState();
         return (ActivityInstanceState.Completed == state)
               || (ActivityInstanceState.Aborted == state);
      }

   public static Iterator<ActivityInstanceHistoryBean> getAllForActivityInstance(
         IActivityInstance activityInstance, boolean ascending)
   {
      QueryExtension qe = QueryExtension.where(Predicates.isEqual(FR__ACTIVITY_INSTANCE,
            activityInstance.getOID()));
      qe.getOrderCriteria().add(ActivityInstanceHistoryBean.FR__FROM, ascending);
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .getVector(ActivityInstanceHistoryBean.class, qe)
            .iterator();
   }

   public static boolean existsForDepartment(long departmentOid)
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm departmentPredicate = Predicates.isEqual(FR__DEPARTMENT, departmentOid);
      ComparisonTerm onBehalfOfDepartmentPredicate = Predicates.isEqual(FR__ON_BEHALF_OF_DEPARTMENT, departmentOid);
      QueryExtension queryExtension = QueryExtension.where(new OrTerm(
            new PredicateTerm[] {departmentPredicate, onBehalfOfDepartmentPredicate}));
      return session.exists(ActivityInstanceHistoryBean.class, queryExtension);
   }

   public ActivityInstanceHistoryBean()
   {
   }

   public ActivityInstanceHistoryBean(IActivityInstance activityInstance, Date from,
         Date until, ActivityInstanceState state, IParticipant performer,
         IDepartment department, EncodedPerformer encodedOnBehalfOf, long onBehalfOfUser,
         long workflowUser)
   {
      this.processInstance = activityInstance.getProcessInstanceOID();
      this.activityInstance = activityInstance.getOID();

      this.state = state.getValue();

      this.fromTimestamp = (null != from) ? from.getTime() : 0l;
      this.untilTimestamp = (null != until) ? until.getTime() : 0l;

      // encode worklist
      PerformerUtils.EncodedPerformer encodedPerformer = PerformerUtils
            .encodeParticipant(performer, department);
      this.performerKind = encodedPerformer.kind.getValue();
      this.performer = encodedPerformer.oid;
      this.department = encodedPerformer.departmentOid;

      if (null == encodedOnBehalfOf)
      {
         encodedOnBehalfOf = PerformerUtils.encodeParticipant(null);
      }

      this.onBehalfOfKind = encodedOnBehalfOf.kind.getValue();
      this.onBehalfOf = encodedOnBehalfOf.oid;
      this.onBehalfOfDepartment = encodedOnBehalfOf.departmentOid;
      this.onBehalfOfUser = onBehalfOfUser;

      this.workflowUser = workflowUser;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public long getProcessInstanceOid()
   {
      fetch();

      return processInstance;
   }

   public IProcessInstance getProcessInstance()
   {
      fetch();

      return ProcessInstanceBean.findByOID(processInstance);
   }

   /*
    * Retrieves the OID of the activity instances for which the log has been
    * written.
    */
   public long getActivityInstanceOid()
   {
      fetch();

      return activityInstance;
   }

   /*
    * Retrieves the activity instances for which the log has been
    * written.
    */
   public IActivityInstance getActivityInstance()
   {
      fetch();

      return ActivityInstanceBean.findByOID(activityInstance);
   }

   public IActivity getActivity()
   {
      fetch();

      // try to leverage if a previous query has retrieved the associated work item
      org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) PropertyLayerProviderInterceptor
            .getCurrent().getAuditTrailSession();
      Collection<PersistenceController> wiCache = jdbcSession.getCache(WorkItemBean.class);
      if (!wiCache.isEmpty())
      {
         for (PersistenceController wiPc : wiCache)
         {
            WorkItemBean wi = (WorkItemBean) wiPc.getPersistent();
            if (wi.getActivityInstanceOID() == activityInstance)
            {
               return wi.getActivity();
            }
         }
      }

      // otherwise load AI and resolve activity
      return getActivityInstance().getActivity();
   }

   /*
    * Retrieves the type of the log.
    */
   public ActivityInstanceState getState()
   {
      fetch();

      return ActivityInstanceState.getState(state);
   }

   public Date getFrom()
   {
      fetch();

      return new Date(fromTimestamp);
   }

   public Date getUntil()
   {
      fetch();

      return new Date(untilTimestamp);
   }

   public void setUntil(Date until)
   {
      markModified(FIELD__UNTIL);
      this.untilTimestamp = (null != until) ? until.getTime() : 0;
   }

   /**
    * Retrieves the participant, the activity instance was assigned to.
    */
   public IParticipant getPerformer()
   {
      fetch();

      return PerformerUtils.decodePerformer(PerformerType.get(performerKind), performer,
            (IModel) getActivity().getModel());
   }

   /**
    * @return returns the Oid of the user performer. If the performer is not a {@link IUser} or there is no performer <code>0</code> is returned.
    */
   public long getUserPerformerOid()
   {
      fetch();
      if (PerformerType.User.equals(PerformerType.get(performerKind)))
      {
         return performer;
      }
      return 0;
   }

   /**
    * Retrieves the department, the activity instance was assigned to.
    */
   public IDepartment getDepartment()
   {
      fetch();
      if(0 != department)
      {
         return DepartmentBean.findByOID(department);
      }

      return null;
   }

   /**
    * Retrieves the participant the activity instance was executed on behalf of.
    */
   public IParticipant getOnBehalfOf()
   {
      fetch();

      return PerformerUtils.decodePerformer(PerformerType.get(onBehalfOfKind), onBehalfOf,
            (IModel) getActivity().getModel());
   }

   /**
    * Retrieves the department the activity instance was executed on behalf of.
    */
   public IDepartment getOnBehalfOfDepartment()
   {
      fetch();
      if(0 != onBehalfOfDepartment)
      {
         return DepartmentBean.findByOID(onBehalfOfDepartment);
      }

      return null;
   }

   public PerformerUtils.EncodedPerformer getEncodedOnBehalfOf()
   {
      fetch();

      return new PerformerUtils.EncodedPerformer(PerformerType.get(onBehalfOfKind),
            onBehalfOf, onBehalfOfDepartment);
   }

   public long getOnBehalfOfUserOid()
   {
      fetch();
      return onBehalfOfUser;
   }

   public IUser getOnBehalfOfUser()
   {
      fetch();
      return onBehalfOfUser == 0 ? null : UserBean.findByOid(onBehalfOfUser);
   }

   /*
    * public void setOnBehalfOf(IParticipant onBehalfOf) { PerformerUtils.EncodedPerformer
    * encodedOnBehalfOf = PerformerUtils.encodeParticipant(onBehalfOf);
    * this.setEncodedOnBehalfOf(encodedOnBehalfOf); }
    *
    * public void setOnBehalfOf(IParticipant onBehalfOf, IDepartment onBehalfOfDepartment)
    * { PerformerUtils.EncodedPerformer encodedOnBehalfOf = PerformerUtils
    * .encodeParticipant(onBehalfOf, onBehalfOfDepartment);
    * this.setEncodedOnBehalfOf(encodedOnBehalfOf); }
    *
    * public void setEncodedOnBehalfOf(PerformerUtils.EncodedPerformer encodedOnBehalfOf)
    * { if (null == encodedOnBehalfOf) { encodedOnBehalfOf =
    * PerformerUtils.encodeParticipant(null); }
    *
    * int newOnBehalfOfKind = encodedOnBehalfOf.kind.getValue(); long newOnBehalfOf =
    * encodedOnBehalfOf.oid; long newOnBehalfOfDepartment =
    * encodedOnBehalfOf.departmentOid;
    *
    * if (newOnBehalfOfKind != this.onBehalfOfKind) {
    * markModified(FIELD__ON_BEHALF_OF_KIND); this.onBehalfOfKind = newOnBehalfOfKind; }
    *
    * if (newOnBehalfOf != this.onBehalfOf) { markModified(FIELD__ON_BEHALF_OF);
    * this.onBehalfOf = newOnBehalfOf; }
    *
    * if (newOnBehalfOfDepartment != this.onBehalfOfDepartment) {
    * markModified(FIELD__ON_BEHALF_OF_DEPARTMENT); this.onBehalfOfDepartment =
    * newOnBehalfOfDepartment; } }
    */

   /*
    * Retrieves the user of the activity instance log context.
    */
   public IUser getUser()
   {
      fetch();

      return (0 != workflowUser) ? UserBean.findByOid(workflowUser) : null;
   }

   public long getUserOid()
   {
      fetch();

      return workflowUser;
   }
}
