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

import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkResult;


/** */
public interface IActivityInstance extends AttributedIdentifiablePersistent
{
   /**
    * Sets the state an activity instance is in - regarding quality control.
    * See also {@link QualityAssuranceUtils.QualityAssuranceState}
    * @param s - the state to set
    */
   public void setQualityAssuranceState(QualityAssuranceState s);

   /**
    * Get the state an activity instance is in - regarding quality control.
    * See also {@link QualityAssuranceUtils.QualityAssuranceState}
    * @return the state an activity instance is in - regarding quality control
    */
   public QualityAssuranceState getQualityAssuranceState();

   /**
    * @return The state of the activity instance.
    */
   public ActivityInstanceState getState();

   /**
    *
    */
   public Date getStartTime();

   /**
    *
    */
   public Date getLastModificationTime();

   /**
    *
    */
   public IActivity getActivity();

   /**
    *
    */
   public IProcessInstance getProcessInstance();

   /**
    * Retrieves the participant, the activity instance is assigned to, if
    * the activity instance is assigned to a participant.
    * Returns <tt>null</tt> otherwise.
    */
   public IParticipant getCurrentPerformer();

   /**
    * Retrieves the user, the activity instance is assigned to, if
    * the activity is assigned to a user.
    * Returns <tt>null</tt> otherwise.
    */
   public IUser getCurrentUserPerformer();

   /**
    * Retrieves the OID of the participant, the activity instance is assigned to, if
    * the activity is assigned to a participant. An OID &gt; 0 designates a model
    * participant, while an OID &lt; 0 designates a user group.
    * Returns <tt>0</tt> otherwise.
    */
   public long getCurrentPerformerOID();

   /**
    * Retrieves the OID of the user, the activity instance is assigned to, if
    * the activity is assigned to a user.
    * Returns <tt>0</tt> otherwise.
    */
   public long getCurrentUserPerformerOID();

   /**
    * Retrieves the user, the activity instance is performed by.
    */
   public IUser getPerformedBy();

   /**
    * Retrieves the department the activity instance is assigned to. This will
    * only be the case if the current performer is a scoped model participant
    * or the activity instance has been completed.
    *
    * @return the assigned department, otherwise null.
    */
   public IDepartment getCurrentDepartment();

   public double getCriticality();

   public int getBenchmarkValue();

   public long getCurrentDepartmentOid();

   public boolean isCompleted();

   public boolean isTerminated();

   public boolean isHibernated();

   public boolean isAborting();

   boolean isDefaultCaseActivityInstance();

   long getProcessInstanceOID();

   void removeFromWorklists();

   Map getIntrinsicOutAccessPointValues();

   void start();

   void processException(Throwable interruptionState) throws Throwable;

   void accept(Map data);

   void complete() throws IllegalStateChangeException;

   void activate() throws IllegalStateChangeException;

   void suspend() throws IllegalStateChangeException;

   void interrupt() throws IllegalStateChangeException;

   void delegateToDefaultPerformer() throws AccessForbiddenException;

   void processOutDataMappings(Map outAccessPointValues);

   void hibernate();

   void delegateToUser(IUser user) throws AccessForbiddenException;

   void delegateToUserGroup(IUserGroup userGroup) throws AccessForbiddenException;

   void delegateToParticipant(IModelParticipant participant) throws AccessForbiddenException;

   void delegateToParticipant(IModelParticipant participant, IDepartment department, IDepartment lastDepartment) throws AccessForbiddenException;

   void bind(IEventHandler handler, EventHandlerBinding aspect);

   void unbind(IEventHandler handler, EventHandlerBinding aspect);

   void doStartActivity(IActivity activity) throws IllegalStateChangeException;

   void doCompleteActivity() throws IllegalStateChangeException;

   boolean isHalted();

   boolean isHalting();

}
