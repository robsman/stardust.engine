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
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.IllegalStateChangeException;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * Adapter class which converts IWorkItems to IActivityInstances. Not all methods
 * are supported and will throw UnsupportedOperationException.
 *
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class WorkItemAdapter extends AttributedIdentifiablePersistentBean implements IActivityInstance
{
   /**
    *
    */
   private static final long serialVersionUID = 7050026372389262860L;
   private IWorkItem workItem;
   private PersistenceController persistenceController;

   public WorkItemAdapter(IWorkItem workItem)
   {
      super();
      this.workItem = workItem;
   }

   public void accept(Map data)
   {
      throw new UnsupportedOperationException();
   }

   public void activate() throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public void bind(IEventHandler handler, EventHandlerBinding aspect)
   {
      throw new UnsupportedOperationException();
   }

   public void complete() throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public void delegateToDefaultPerformer() throws AccessForbiddenException
   {
      throw new UnsupportedOperationException();
   }

   public void delegateToParticipant(IModelParticipant participant)
         throws AccessForbiddenException
   {
      throw new UnsupportedOperationException();
   }

   public void delegateToParticipant(IModelParticipant participant, IDepartment department, IDepartment lastDepartment)
         throws AccessForbiddenException
   {
      throw new UnsupportedOperationException();
   }

   public void delegateToUser(IUser user) throws AccessForbiddenException
   {
      throw new UnsupportedOperationException();
   }

   public void delegateToUserGroup(IUserGroup userGroup) throws AccessForbiddenException
   {
      throw new UnsupportedOperationException();
   }

   public void doCompleteActivity() throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public void doStartActivity(IActivity activity) throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public IActivity getActivity()
   {
      return workItem.getActivity();
   }

   public IParticipant getCurrentPerformer()
   {
      return workItem.getCurrentPerformer();
   }

   public long getCurrentPerformerOID()
   {
      return workItem.getCurrentPerformerOID();
   }

   public IUser getCurrentUserPerformer()
   {
      return workItem.getCurrentUserPerformer();
   }

   public long getCurrentUserPerformerOID()
   {
      return workItem.getCurrentUserPerformerOID();
   }

   public Map getIntrinsicOutAccessPointValues()
   {
      throw new UnsupportedOperationException();
   }

   public Date getLastModificationTime()
   {
      return workItem.getLastModificationTime();
   }

   public IUser getPerformedBy()
   {
      return null;
   }

   public IProcessInstance getProcessInstance()
   {
      return ProcessInstanceBean.findByOID(workItem.getProcessInstanceOID());
   }

   public long getProcessInstanceOID()
   {
      return workItem.getProcessInstanceOID();
   }

   public Date getStartTime()
   {
      return workItem.getStartTime();
   }

   public ActivityInstanceState getState()
   {
      return workItem.getState();
   }

   public void hibernate()
   {
      throw new UnsupportedOperationException();
   }

   public void interrupt() throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public boolean isAborting()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isCompleted()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isHibernated()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isHalted()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isHalting()
   {
      throw new UnsupportedOperationException();
   }

   public boolean isTerminated()
   {
      ActivityInstanceState state = workItem.getState();
      return (ActivityInstanceState.Completed == state)
            || (ActivityInstanceState.Aborted == state);
   }

   public void processException(Throwable interruptionState) throws Throwable
   {
      throw new UnsupportedOperationException();
   }

   public void processOutDataMappings(Map outAccessPointValues)
   {
      throw new UnsupportedOperationException();
   }

   public void removeFromWorklists()
   {
      throw new UnsupportedOperationException();
   }

   public void start()
   {
      throw new UnsupportedOperationException();
   }

   public void suspend() throws IllegalStateChangeException
   {
      throw new UnsupportedOperationException();
   }

   public void unbind(IEventHandler handler, EventHandlerBinding aspect)
   {
      throw new UnsupportedOperationException();
   }

   public void addPropertyValues(Map map)
   {
      throw new UnsupportedOperationException();
   }

   public AbstractProperty createProperty(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }

   public Map getAllProperties()
   {
      return super.getAllProperties();
   }

   public Map getAllPropertyValues()
   {
      throw new UnsupportedOperationException();
   }

   public Serializable getPropertyValue(String name)
   {
      return super.getPropertyValue(name);
   }

   public void removeProperty(String name)
   {
      throw new UnsupportedOperationException();
   }

   public void removeProperty(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }

   public void setPropertyValue(String name, Serializable value, boolean force)
   {
      throw new UnsupportedOperationException();
   }

   public void setPropertyValue(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }

   public long getOID()
   {
      return workItem.getActivityInstanceOID();
   }

   public void lock() throws ConcurrencyException
   {
      throw new UnsupportedOperationException();
   }

   public void setOID(long oid)
   {
      throw new UnsupportedOperationException();
   }

   public void delete()
   {
      throw new UnsupportedOperationException();
   }

   public void delete(boolean writeThrough)
   {
      throw new UnsupportedOperationException();
   }

   public void disconnectPersistenceController()
   {
      throw new UnsupportedOperationException();
   }

   public void fetch()
   {
      throw new UnsupportedOperationException();
   }

   public PersistenceController getPersistenceController()
   {
      if(persistenceController == null)
      {
         Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         DmlManager manager = session.getDMLManager(getPropertyImplementationClass());
         persistenceController = manager.createPersistenceController(session, this);
      }

      return persistenceController;
   }

   public void markCreated()
   {
      throw new UnsupportedOperationException();
   }

   public void markModified()
   {
      throw new UnsupportedOperationException();
   }

   public void markModified(String fieldName)
   {
      throw new UnsupportedOperationException();
   }

   public void setPersistenceController(PersistenceController persistenceController)
   {
      this.persistenceController = persistenceController;
   }

   public IDepartment getCurrentDepartment()
   {
      return workItem.getDepartment();
   }

   public long getCurrentDepartmentOid()
   {
      return workItem.getDepartmentOid();
   }

   public double getCriticality()
   {
      return workItem.getCriticality();
   }

   public int getBenchmarkValue()
   {
      return workItem.getBenchmarkValue();
   }

   public boolean isDefaultCaseActivityInstance()
   {
      return PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID.equals(getActivity().getId())
         && getProcessInstance().isCaseProcessInstance();
   }

   public QualityAssuranceState getQualityAssuranceState()
   {
      String value = (String) getPropertyValue(QualityAssuranceState.PROPERTY_KEY);
      if (StringUtils.isNotEmpty(value))
      {
         return QualityAssuranceState.valueOf(value);
      }

      return QualityAssuranceState.NO_QUALITY_ASSURANCE;
   }

   public void setQualityAssuranceState(QualityAssuranceState s)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Class getPropertyImplementationClass()
   {
      return ActivityInstanceProperty.class;
   }
}