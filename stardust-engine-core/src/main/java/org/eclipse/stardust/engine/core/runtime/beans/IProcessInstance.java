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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;


/**
 *
 */
public interface IProcessInstance extends SymbolTable, AttributedIdentifiablePersistent
{

   /**
    * Returns the start time of the process instance
    */
   public Date getStartTime();

   /**
    * Returns the termination time of the process instance
    */
   public Date getTerminationTime();

   /**
    * Returns the state of the process instance.
    */
   public ProcessInstanceState getState();

   /**
    * Checks, wether the process is - normally or abnormally - terminated.
    */
   public boolean isTerminated();

   /**
    *
    */
   public IProcessDefinition getProcessDefinition();

   /**
    * Sets the user who has started the process, if the process is
    * started with a manual trigger.
    */
   public void setStartingUser(IUser startingUser);

   /**
    * Returns the user who has started the process. If the process is not
    * started with a manual trigger, <tt>null</tt> is returned.
    */
   public IUser getStartingUser();

   /**
    *
    */
   public IDataValue getDataValue(IData data);

   /**
    * Returns all existing data values of the scope process instance
    */
   public Iterator getAllDataValues();

   /**
    *
    */
   public IDataValue getDataValue(IData data,
         AbstractInitialDataValueProvider dataValueProvider);

   /**
    *
    */
   public void setDataValue(String name, Object value);

   /**
    * Returns the OID of the root process instance hosting a subprocess instance, or the
    * OID of this process instance itself in case of a top-level process instance.
    *
    * @return The OID of the root process instance.
    * @see #getRootProcessInstance()
    */
   public long getRootProcessInstanceOID();

   /**
    * Returns the root process instance hosting the subprocess instance, or the process
    * instance itself in case of a top-level process instance.
    *
    * @return The root process instance.
    * @see #getRootProcessInstanceOID()
    */
   public IProcessInstance getRootProcessInstance();

   /**
    * Returns the OID of the scope process instance the data values of this
    * process instance are bound to.
    *
    * @return The OID of the scope process instance.
    */
   public long getScopeProcessInstanceOID();

   /**
    * Returns the scope process instance the data values of this
    * process instance are bound to.
    *
    * @return The root process instance.
    * @see #getScopeProcessInstanceOID()
    */
   public IProcessInstance getScopeProcessInstance();

   /**
    * Checks if this process instance is a case instance.
    *
    * @return true if this is a case instance.
    */
   boolean isCaseProcessInstance();

   /**
    * Gets the priority of the process instance.
    *
    * @return The priority of the process instance or -1 if no priority set.
    */
   int getPriority();

   /**
    * Sets the priority of the process instance.
    *
    * param priority The new priority of the process instance.
    */
   void setPriority(int priority);

   /**
    * Returns the OID of the calling activity instance if this process is started as a
    * subprocess on behalf of this activity instance.
    *
    * @return The OID of the calling activity instance.
    */
   public long getStartingActivityInstanceOID();

   /**
    * Returns the calling activity instance if this process is started as a
    * subprocess on behalf of this activity instance.
    */
   public IActivityInstance getStartingActivityInstance();

   /**
    * Returns all activity instances already performed on behalf of this process.
    * This is the audit trail of the process.
    */
   public Iterator getAllPerformedActivityInstances();

   /**
    * Interrupts the process due to exception in non-interactive application.
    */
   public void interrupt();

   /**
    * Resets the interrupted flag after recovery.
    */
   public void resetInterrupted();

   /**
    * Evaluates a condition against the data instances of
    * this process instance.
    */
   public boolean validateLoopCondition(String condition);

   boolean isCompleted();

   void setOutDataValue(IData data, String path, Object value)
         throws InvalidValueException;

   Object getInDataValue(IData data, String path);

   Map getExistingDataValues(boolean includePredefined);

   boolean isAborted();

   boolean isAborting();

   void bind(IEventHandler handler, EventHandlerBinding aspect);

   void unbind(IEventHandler handler, EventHandlerBinding aspect);

   void preloadDataValues(List dataItems);

   boolean isPropertyAvailable();

   boolean isPropertyAvailable(int pattern);

   void addNote(String note);

   void addNote(String note, ContextKind contextKind, long contextOid);

   List/**/ getNotes();

   void addAbortingPiOid(long oid);

   void removeAbortingPiOid(long oid);

   List/**/ getAbortingPiOids();

   public long getReferenceDeployment();

   public AuditTrailPersistence getAuditTrailPersistence();

   public void setAuditTrailPersistence(final AuditTrailPersistence auditTrailPersistence);

   public AuditTrailPersistence getPreviousAuditTrailPersistence();

   public void addExistingNote(ProcessInstanceProperty srcNote);
   
   void setBenchmark(long benchmarkOid);
   
   long getBenchmark();
   
   int getBenchmarkValue();
}
