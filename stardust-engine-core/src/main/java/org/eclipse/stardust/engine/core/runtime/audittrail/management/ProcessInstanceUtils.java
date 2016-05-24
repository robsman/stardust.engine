/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import static org.eclipse.stardust.common.CollectionUtils.newTreeSet;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.NoteDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.persistence.DeleteDescriptor;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * @author rsauer
 * @version $Revision: 28247 $
 */
public class ProcessInstanceUtils
{
   private static final Logger trace = LogManager.getLogger(ProcessInstanceUtils.class);

   private static final String STMT_BATCH_SIZE = KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE;
   private static final int DEFAULT_STATEMENT_BATCH_SIZE = 100;

   private static final int PK_OID = 0;

   public static void cleanupProcessInstance(IProcessInstance pi)
   {
      // hierarchy is deleted only for root process instances
      if (pi.getOID() == pi.getRootProcessInstanceOID() && Parameters.instance().getBoolean(
            KernelTweakingProperties.AUTOMATIC_HIERARCHY_CLEANUP, false))
      {
         ProcessInstanceHierarchyBean.delete(pi);
      }

      // tokens are deleted only for completed process instances
      if (pi.isCompleted() && Parameters.instance().getBoolean(
            KernelTweakingProperties.AUTOMATIC_TOKEN_CLEANUP, false))
      {
         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).delete(
               TransitionTokenBean.class,
               Predicates.isEqual(TransitionTokenBean.FR__PROCESS_INSTANCE,
                     pi.getOID()), true);
      }
   }

   public static boolean isLoadNotesEnabled()
   {
      Parameters parameters = Parameters.instance();
      ProcessInstanceDetailsLevel detailsLevel = parameters.getObject(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, ProcessInstanceDetailsLevel.Default);
      EnumSet<ProcessInstanceDetailsOptions> detailsOptions = parameters.getObject(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS, EnumSet.noneOf(ProcessInstanceDetailsOptions.class));


      return ProcessInstanceDetailsLevel.Full == detailsLevel
            || ProcessInstanceDetailsLevel.WithProperties == detailsLevel
            || ProcessInstanceDetailsLevel.WithResolvedProperties == detailsLevel
            || detailsOptions.contains(ProcessInstanceDetailsOptions.WITH_NOTES);
   }

   public static boolean hasNotes(IProcessInstance pi)
   {
      return pi.isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_NOTE);
   }

   public static List<Note> getNotes(IProcessInstance pi, ActivityInstance contextObject)
   {
      List<Note> activityInstanceNotes = new ArrayList<Note>();

      ProcessInstance processInstanceContext = contextObject.getProcessInstance().getScopeProcessInstance();
      List<Note> allNotes = getNotes(pi, processInstanceContext);
      for(Note n: allNotes)
      {
         if(n.getContextKind() == ContextKind.ActivityInstance
               && n.getContextOid() == contextObject.getOID())
         {
            activityInstanceNotes.add(n);
         }
      }

      return activityInstanceNotes;
   }

   public static List<Note> getNotes(IProcessInstance pi, ProcessInstance contextObject)
   {
      List<AbstractPropertyWithUser> notesAsProperties = pi.getNotes();
      Collections.sort(notesAsProperties, new Comparator<AbstractPropertyWithUser>()
            {
               @Override
               public int compare(AbstractPropertyWithUser o1, AbstractPropertyWithUser o2)
               {
                  return o1.getLastModificationTime().compareTo(
                        o2.getLastModificationTime());
               }
            });
      List<Note> notes = new ArrayList<Note>(notesAsProperties.size());

      for (Iterator<AbstractPropertyWithUser> iterator = notesAsProperties.iterator(); iterator.hasNext();)
      {
         AbstractPropertyWithUser noteAttribute = iterator.next();
         String rawText = (String) noteAttribute.getValue();

         int contextKind;
         long oid;
         String noteText;

         try
         {
            Object[] noteParts;
            noteParts = new java.text.MessageFormat(
                  ProcessInstanceBean.PI_NOTE_CONTEXT_PREFIX_PATTERN + "{2}")
                  .parse(rawText);

            contextKind = Integer.valueOf((String) noteParts[0]).intValue();
            oid = Long.valueOf((String) noteParts[1]).longValue();
            noteText = (String) noteParts[2];
         }
         catch (ParseException e)
         {
            contextKind = ContextKind.PROCESS_INSTANCE;
            oid = pi != null ? pi.getOID() : 0;
            noteText = rawText;
         }

         IUser user = noteAttribute.getUser();
         User userDetails = null;
         PropertyLayer layer = null;
         try
         {
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
            userDetails = (User) DetailsFactory.create(user, IUser.class,
                  UserDetails.class);
         }
         finally
         {
            if (null != layer)
            {
               ParametersFacade.popLayer();
            }
         }

         Note note = new NoteDetails(noteText, ContextKind.get(contextKind), oid,
               null, noteAttribute.getLastModificationTime(),
               userDetails == null ? null : userDetails);

         notes.add(note);
      }

      resolveContextObjects(notes, contextObject);
      return notes;
   }

   private static void resolveContextObjects(List<Note> notes, ProcessInstance contextObject)
   {
      if ( !notes.isEmpty() && contextObject != null)
      {
         final ProcessInstanceDetailsLevel detailsLevel = contextObject.getDetailsLevel();
         if (ProcessInstanceDetailsLevel.WithResolvedProperties == detailsLevel
               || ProcessInstanceDetailsLevel.Full == detailsLevel)
         {
            Map<Long, ActivityInstance> aiContextObjects = new HashMap<Long, ActivityInstance>();

            // collect oids for AI context objects.
            for (Note note : notes)
            {
               final ContextKind contextKind = note.getContextKind();
               if (ContextKind.ActivityInstance.equals(contextKind))
               {
                  aiContextObjects.put(note.getContextOid(), null);
               }
            }

            // fetch AI context objects
            if ( !aiContextObjects.isEmpty())
            {
               org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
               Iterator result = session.getIterator(ActivityInstanceBean.class,
                     QueryExtension.where(Predicates.inList(ActivityInstanceBean.FR__OID,
                           aiContextObjects.keySet().iterator())));

               // map the AI details to their oids
               while (result.hasNext())
               {
                  IActivityInstance ai = (IActivityInstance) result.next();
                  aiContextObjects.put(ai.getOID(),
                        (ActivityInstance) DetailsFactory.create(ai,
                              IActivityInstance.class, ActivityInstanceDetails.class));
               }
            }

            // set resolved context objects
            for (Note note : notes)
            {
               NoteDetails noteDetails = (NoteDetails) note;
               final ContextKind contextKind = note.getContextKind();
               if (ContextKind.ProcessInstance.equals(contextKind))
               {
                  noteDetails.setContextObject(contextObject);
               }
               else if (ContextKind.ActivityInstance.equals(contextKind))
               {
                  noteDetails.setContextObject((RuntimeObject) aiContextObjects.get(
                        note.getContextOid()));
               }
            }
         }
      }
   }

   /**
    * Checks if the process tree for the given processInstance. If the process instance
    * itself or any of its parents is in {@link ProcessInstanceState#ABORTED} or
    * {@link ProcessInstanceState#ABORTING} state, true is returned,
    *
    * @param processInstance
    *           - the process instance to check
    * @return true if the process instance or its parent is is in
    *         {@link ProcessInstanceState#ABORTED} or
    *         {@link ProcessInstanceState#ABORTING} state, false otherwise
    */
   public static boolean isInAbortingPiHierarchy(IProcessInstance processInstance)
   {
      boolean result = false;
      final Long piOid = Long.valueOf(processInstance.getOID());

      if (piOid.longValue() == processInstance.getRootProcessInstanceOID())
      {
         return isAbortedStateSafe(processInstance);
      }
      else
      {
         IProcessInstance rootPi = processInstance.getRootProcessInstance();

         if (isAbortedStateSafe(rootPi))
         {
            result = true;
         }
         else
         {
            if ( !rootPi.getPersistenceController().isLocked())
            {
               try
               {
                  rootPi.getPersistenceController().reloadAttribute(
                        ProcessInstanceBean.FIELD__PROPERTIES_AVAILABLE);
               }
               catch (PhantomException e)
               {
                  throw new InternalException(e);
               }
            }
            if (rootPi.isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_PI_ABORTING))
            {
               List abortingOids = new ArrayList();
               for (Iterator iter = rootPi.getAbortingPiOids().iterator(); iter.hasNext();)
               {
                  Attribute attribute = (Attribute) iter.next();
                  abortingOids.add(attribute.getValue());
               }

               IProcessInstance currentPi = processInstance;
               while (null != currentPi)
               {
                  if (abortingOids.contains(Long.valueOf(currentPi.getOID())))
                  {
                     result = true;
                     break;
                  }

                  // get the parent process instance, if any.
                  IActivityInstance startingActivityInstance = currentPi.getStartingActivityInstance();
                  if (null == startingActivityInstance)
                  {
                     currentPi = null;
                  }
                  else
                  {
                     currentPi = startingActivityInstance.getProcessInstance();
                  }
               }
            }
         }
      }

      return result;
   }

   /**
    * Checks if the process tree for the given processInstance. If the process instance
    * itself or any of its parents is in {@link ProcessInstanceState#HALTED} or
    * {@link ProcessInstanceState#HALTING} state, true is returned,
    */
   public static boolean isInHaltingPiHierarchy(IProcessInstance processInstance)
   {
      boolean result = false;

      if (processInstance.getOID() == processInstance.getRootProcessInstanceOID())
      {
         if (processInstance instanceof ProcessInstanceBean)
         {
            ProcessInstanceState piState = processInstance.getState();
            try
            {
               if (!processInstance.getPersistenceController().isLocked())
               {
                  ((ProcessInstanceBean) processInstance).reloadAttribute(ProcessInstanceBean.FIELD__STATE);
               }
               return processInstance.isHalting() || processInstance.isHalted();
            }
            catch (PhantomException e)
            {
               // (fh) don't know what else to do
               throw new InternalException(e);
            }
            finally
            {
               if (piState != processInstance.getState())
               {
                  trace.debug(
                        "Using current process instance state '" + processInstance.getState()
                        + "' instead of the cached state '" + piState + "'.");
                  ((ProcessInstanceBean) processInstance).restoreState(piState);
               }
            }
         }
         return processInstance.isHalting() || processInstance.isHalted();
      }
      else
      {
         IProcessInstance rootPi = processInstance.getRootProcessInstance();
         ProcessInstanceState piState = processInstance.getState();
         try
         {
            if (!rootPi.getPersistenceController().isLocked())
            {
               ((ProcessInstanceBean) rootPi).reloadAttribute(ProcessInstanceBean.FIELD__STATE);
            }
            if (rootPi.isHalting() || rootPi.isHalted())
            {
               result = true;
            }
            else
            {
               if (!rootPi.getPersistenceController().isLocked())
               {
                  try
                  {
                     rootPi.getPersistenceController().reloadAttribute(
                           ProcessInstanceBean.FIELD__PROPERTIES_AVAILABLE);
                  }
                  catch (PhantomException e)
                  {
                     throw new InternalException(e);
                  }
               }
               if (rootPi.isPropertyAvailable(ProcessInstanceBean.PI_PROPERTY_FLAG_PI_HALTING))
               {
                  List haltingOids = new ArrayList();
                  for (Iterator iter = rootPi.getHaltingPiOids().iterator(); iter.hasNext();)
                  {
                     Object next = iter.next();
                     if (next instanceof Long)
                     {
                        haltingOids.add(next);
                     }
                     else
                     {
                        Attribute attribute = (Attribute) iter.next();
                        haltingOids.add(attribute.getValue());
                     }
                  }

                  IProcessInstance currentPi = processInstance;
                  while (null != currentPi)
                  {
                     if (haltingOids.contains(Long.valueOf(currentPi.getOID())))
                     {
                        result = true;
                        break;
                     }

                     // get the parent process instance, if any.
                     IActivityInstance startingActivityInstance = currentPi.getStartingActivityInstance();
                     if (null == startingActivityInstance)
                     {
                        currentPi = null;
                     }
                     else
                     {
                        currentPi = startingActivityInstance.getProcessInstance();
                     }
                  }
               }
            }
         }
         catch (PhantomException e)
         {
            // (fh) don't know what else to do
            throw new InternalException(e);
         }
         finally
         {
            if (piState != rootPi.getState())
            {
               trace.debug(
                     "Using current root process instance state '" + rootPi.getState()
                     + "' instead of the cached state '" + piState + "'.");
               ((ProcessInstanceBean) rootPi).restoreState(piState);
            }
         }
      }

      return result;
   }

   /**
    * Tests if the given process instance has a persisted state of ABORTING or ABORTED.
    * After that call the state is recovered.
    *
    * @param pi
    *           the process instance
    * @return true if the persisted state is ABORTING or ABORT
    */
   private static boolean isAbortedStateSafe(IProcessInstance pi)
   {
      ProcessInstanceState stateBackup = pi.getState();
      if (ProcessInstanceState.Aborting.equals(stateBackup)
            || ProcessInstanceState.Aborted.equals(stateBackup))
      {
         return true;
      }
      try
      {
         ((PersistentBean) pi).reloadAttribute(ProcessInstanceBean.FIELD__STATE);
         return pi.isAborting() || pi.isAborted();
      }
      catch (PhantomException x)
      {
         throw new InternalException(x);
      }
      finally
      {
         if ( !stateBackup.equals(pi.getState()))
         {
            try
            {
               Reflect.getField(ProcessInstanceBean.class,
                     ProcessInstanceBean.FIELD__STATE).setInt(pi, stateBackup.getValue());
            }
            catch (Exception e)
            {
               // should never happen
               throw new InternalException(e);
            }
         }
      }
   }

   public static int deleteProcessInstances(List<Long> piOids, Session session)
   {
      if (piOids.isEmpty())
      {
         return 0;
      }

      deletePiParts(piOids, TransitionTokenBean.class,
            TransitionTokenBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, TransitionInstanceBean.class,
            TransitionInstanceBean.FR__PROCESS_INSTANCE, session);

      deleteAiParts(piOids, LogEntryBean.class, LogEntryBean.FR__ACTIVITY_INSTANCE, session);

      deleteAiParts(piOids, ActivityInstanceHistoryBean.class,
            ActivityInstanceHistoryBean.FR__ACTIVITY_INSTANCE, session);

      deleteAiParts(piOids, EventBindingBean.class, EventBindingBean.FR__OBJECT_OID,
            Predicates.isEqual(
                  EventBindingBean.FR__TYPE, Event.ACTIVITY_INSTANCE),
            session);

      deleteAiParts(piOids, ActivityInstanceProperty.class,
            ActivityInstanceProperty.FR__OBJECT_OID, session);

      deletePiParts(piOids, ActivityInstanceBean.class,
            ActivityInstanceBean.FR__PROCESS_INSTANCE, session);

      // TODO (ab) SPI
      List<Long> structuredDataOids = findAllStructuredDataOids(SecurityProperties.getPartitionOid(), session);
      if (structuredDataOids.size() != 0)
      {
         delete2ndLevelPiParts(piOids, LargeStringHolder.class,
               LargeStringHolder.FR__OBJECTID, StructuredDataValueBean.class,
               StructuredDataValueBean.FR__PROCESS_INSTANCE, Predicates.isEqual(
                     LargeStringHolder.FR__DATA_TYPE,
                     TypeDescriptor.getTableName(StructuredDataValueBean.class)), session);

         deletePiParts(piOids, StructuredDataValueBean.class, StructuredDataValueBean.FR__PROCESS_INSTANCE, session);

         delete2ndLevelPiParts(piOids, ClobDataBean.class,
               ClobDataBean.FR__OID, DataValueBean.class,
               DataValueBean.FIELD__NUMBER_VALUE,
               DataValueBean.FR__PROCESS_INSTANCE, Predicates.inList(
                     DataValueBean.FR__DATA, structuredDataOids), session);
      }

      deleteDvParts(piOids, LargeStringHolder.class, LargeStringHolder.FR__OBJECTID,
            Predicates.isEqual(
                  LargeStringHolder.FR__DATA_TYPE,
                  TypeDescriptor.getTableName(DataValueBean.class)),
            session);

      deletePiParts(piOids, DataValueBean.class, DataValueBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, LogEntryBean.class, LogEntryBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, EventBindingBean.class, EventBindingBean.FR__OBJECT_OID,
            Predicates.isEqual(
                  EventBindingBean.FR__TYPE, Event.PROCESS_INSTANCE),
            session);

      delete2ndLevelPiParts(piOids, LargeStringHolder.class, LargeStringHolder.FR__OBJECTID, //
            ProcessInstanceProperty.class, ProcessInstanceProperty.FR__OBJECT_OID, //
            isEqual(LargeStringHolder.FR__DATA_TYPE, ProcessInstanceProperty.TABLE_NAME),
            session);
      deletePiParts(piOids, ProcessInstanceProperty.class,
            ProcessInstanceProperty.FR__OBJECT_OID, session);

      deletePiParts(piOids, ProcessInstanceLinkBean.class,
            ProcessInstanceLinkBean.FR__LINKED_PROCESS_INSTANCE, session);
      deletePiParts(piOids, ProcessInstanceLinkBean.class,
            ProcessInstanceLinkBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, ProcessInstanceHierarchyBean.class,
            ProcessInstanceHierarchyBean.FR__SUB_PROCESS_INSTANCE, session);
      deletePiParts(piOids, ProcessInstanceHierarchyBean.class,
            ProcessInstanceHierarchyBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, ProcessInstanceScopeBean.class,
            ProcessInstanceScopeBean.FR__PROCESS_INSTANCE, session);

      deletePiParts(piOids, WorkItemBean.class,
            WorkItemBean.FR__PROCESS_INSTANCE, session);

      deleteDataClusterValues(piOids, session);

      return deletePiParts(piOids, ProcessInstanceBean.class,
            ProcessInstanceBean.FR__OID, null, session);
   }

   public static void deleteDataClusterValues(List piOids, Session session)
   {
      if (piOids.isEmpty())
      {
         // if no PI oids are specified then deletion of data cluster values will be skipped.
         return;
      }

      final int batchSize = getStatementBatchSize();

      // finally deleting rows from data clusters
      final DataCluster[] dClusters = RuntimeSetup.instance().getDataClusterSetup();

      for (int idx = 0; idx < dClusters.length; ++idx)
      {
         final DataCluster dCluster = dClusters[idx];

         for (Iterator<List<Long>> iterator = getChunkIterator(piOids, batchSize); iterator.hasNext();)
         {
            List piOidsBatch = iterator.next();

            Statement stmt = null;
            try
            {
               stmt = session.getConnection().createStatement();
               StringBuffer buffer = new StringBuffer(100 + piOidsBatch.size() * 10);
               buffer.append("DELETE FROM ").append(dCluster.getQualifiedTableName())
                     .append(" WHERE ").append(dCluster.getProcessInstanceColumn())
                     .append(" IN (").append(StringUtils.join(piOidsBatch.iterator(), ", ")).append(")");
               if (trace.isDebugEnabled())
               {
                  trace.debug(buffer);
               }
               stmt.executeUpdate(buffer.toString());
            }
            catch (SQLException e)
            {
               throw new PublicException(
                     BpmRuntimeError.JDBC_FAILED_DELETING_ENRIES_FROM_DATA_CLUSTER_TABLE
                           .raise(dCluster.getTableName()), e);
            }
            finally
            {
               QueryUtils.closeStatement(stmt);
            }
         }
      }
   }

   public static void abortProcessInstance(long piOid) throws ConcurrencyException
   {
      ProcessInstanceBean pi = ProcessInstanceBean.findByOID(piOid);
      abortProcessInstance(pi);
   }

   public static void abortProcessInstance(IProcessInstance pi)
         throws ConcurrencyException
   {
      stopProcessInstance(pi, StopMode.ABORT);
   }

   public static void stopProcessInstance(IProcessInstance pi, StopMode stopMode)
         throws ConcurrencyException
   {
      ProcessInstanceBean processInstance = (ProcessInstanceBean) pi;

      processInstance.lock();
      ProcessInstanceBean rootProcessInstance = (ProcessInstanceBean) processInstance.getRootProcessInstance();
      rootProcessInstance.lock();

      try
      {
         rootProcessInstance.reloadAttribute(
            ProcessInstanceBean.FIELD__PROPERTIES_AVAILABLE);
      }
      catch (PhantomException e)
      {
         throw new InternalException(e);
      }

      if (processInstance.getPersistenceController().isCreated()
            && (processInstance.getPersistenceController().getSession() instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session))
      {
         // can potentially abort synchronously as there can be no concurrent activity
         // thread (the PI was not yet INSERTed into the Audit Trail DB)

         stopProcessInstanceFromSessionCache(
               (org.eclipse.stardust.engine.core.persistence.jdbc.Session) pi.getPersistenceController().getSession(),
               processInstance, stopMode);
      }
      else
      {
         final long piOid = processInstance.getOID();
         final long userOid = SecurityProperties.getUserOID();

         HierarchyStateChangeJanitorCarrier carrier = null;
         if (StopMode.ABORT.equals(stopMode))
         {
            // Mark this PI at its root PI as aborting
            rootProcessInstance.addAbortingPiOid(piOid);
            // Mark this PI itself as aborting.
            processInstance.setState(ProcessInstanceState.ABORTING);
            carrier = new AbortionJanitorCarrier(piOid, userOid);
         }
         else if (StopMode.HALT.equals(stopMode))
         {
            // Mark this PI at its root PI as halting.
            rootProcessInstance.addHaltingPiOid(piOid);
            // Mark this PI itself as halting.
            processInstance.setState(ProcessInstanceState.HALTING);
            carrier = new ProcessHaltJanitor.Carrier(piOid, userOid);
         }

         BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         if (rtEnv.getExecutionPlan() != null)
         {
            carrier.createAction().execute();
         }
         else
         {
            // abort/halt the complete subprocess hierarchy asynchronously.
            ProcessHierarchyStateChangeJanitor.scheduleJanitor(carrier);
         }
      }
   }

   private static void stopProcessInstanceFromSessionCache(Session session,
         IProcessInstance pi, StopMode stopMode)
   {
      // handle potential stop scenarios
      Set<Long> stoppingPiOids = newTreeSet();
      stoppingPiOids.add(pi.getOID());

      // phase one: collect full PI hierarchy to be stopped.
      Collection<PersistenceController> cachedPiHierarchy = session.getCache(ProcessInstanceHierarchyBean.class);
      for (PersistenceController pcPiHier : cachedPiHierarchy)
      {
         ProcessInstanceHierarchyBean piHier = (ProcessInstanceHierarchyBean) pcPiHier.getPersistent();
         if (pi == piHier.getProcessInstance()) // is parent PI
         {
            stoppingPiOids.add(piHier.getSubProcessInstance().getOID());
         }
      }

      // phase two: stop full PI hierarchy starting from PI to be stopped.
      Collection<PersistenceController> cachedPis = session.getCache(ProcessInstanceBean.class);
      for (PersistenceController pcPi : cachedPis)
      {
         ProcessInstanceBean piToStop = (ProcessInstanceBean) pcPi.getPersistent();
         if (stoppingPiOids.contains(piToStop.getOID())) // is abort candidate
         {
            if ( !piToStop.isTerminated())
            {
               if (StopMode.ABORT.equals(stopMode))
               {
                  if (piToStop.isAborting())
                  {
                     piToStop.getRootProcessInstance()
                           .removeAbortingPiOid(piToStop.getOID());
                  }
                  piToStop.setState(ProcessInstanceState.ABORTED);
                  EventUtils.detachAll(piToStop);
                  ProcessInstanceUtils.cleanupProcessInstance(piToStop);
               }
               else if (StopMode.HALT.equals(stopMode))
               {
                  if (piToStop.isHalting())
                  {
                     piToStop.getRootProcessInstance()
                           .removeHaltingPiOid(piToStop.getOID());
                  }
                  piToStop.setState(ProcessInstanceState.HALTED);
               }
            }
         }
      }

      // phase three: stop all non-terminated AIs for stopped PIs
      for (PersistenceController pcAi : (Collection<PersistenceController>) session.getCache(ActivityInstanceBean.class))
      {
         ActivityInstanceBean aiToStop = (ActivityInstanceBean) pcAi.getPersistent();
         if (stoppingPiOids.contains(aiToStop.getProcessInstanceOID())
               && !aiToStop.isTerminated())
         {
            if (StopMode.ABORT.equals(stopMode))
            {
               aiToStop.setState(ActivityInstanceState.ABORTED);
               aiToStop.removeFromWorklists();
               EventUtils.detachAll(aiToStop);
            }
            else if (StopMode.HALT.equals(stopMode))
            {
               aiToStop.setState(ActivityInstanceState.HALTED);
            }
         }
      }
   }


   private static List<Long> findAllStructuredDataOids(short partitionOid, Session session)
   {
      QueryDescriptor structDataQuery = QueryDescriptor.from(StructuredDataBean.class) //
            .select(StructuredDataBean.FR__DATA) //
            .groupBy(StructuredDataBean.FR__DATA) //
            .where(Predicates.isEqual(ModelPersistorBean.FR__PARTITION, partitionOid));

      structDataQuery.innerJoin(ModelPersistorBean.class) //
            .on(StructuredDataBean.FR__MODEL, ModelPersistorBean.FIELD__OID);

      List<Long> dataOids = CollectionUtils.newArrayList();

      ResultSet dataRtOids = session.executeQuery(structDataQuery);
      try
      {
         while (dataRtOids.next())
         {
            dataOids.add(dataRtOids.getLong(1));
         }
      }
      catch (SQLException sqle)
      {
          throw new PublicException(
                  BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED
                        .raise(), sqle);
      }
      finally
      {
          closeResultSet(dataRtOids);
      }

      return dataOids;
   }

   private static int deletePiParts(List<Long> piOids, Class partType, FieldRef fkPiField,
         Session session)
   {
      return deletePiParts(piOids, partType, fkPiField, null, session);
   }

   private static int deletePiParts(List<Long> piOids, Class partType, FieldRef fkPiField,
         PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator<List<Long>> iterator = getChunkIterator(piOids, batchSize); iterator.hasNext();)
      {
         List piOidsBatch = (List) iterator.next();

         PredicateTerm piPredicate = Predicates.inList(fkPiField, piOidsBatch);

         PredicateTerm predicate = (null != restriction)
            ? Predicates.andTerm(piPredicate, restriction)
            : piPredicate;

         // delete lock rows

         TypeDescriptor tdType = TypeDescriptor.get(partType);
         if (session.isUsingLockTables() && tdType.isDistinctLockTableName())
         {
            Assert.condition(1 == tdType.getPkFields().length,
                  "Lock-tables are not supported for types with compound PKs.");

            DeleteDescriptor delete = DeleteDescriptor
                  .fromLockTable(partType);

            String partOid = tdType.getPkFields()[PK_OID].getName();
            PredicateTerm lockRowsPredicate = Predicates
                  .inList(delete.fieldRef(partOid), QueryDescriptor
                        .from(partType)
                        .select(partOid)
                        .where(predicate));

            session.executeDelete(delete
                  .where(lockRowsPredicate));
         }

         // delete data rows

         DeleteDescriptor delete = DeleteDescriptor
               .from(partType)
               .where(predicate);

         processedItems += session.executeDelete(delete);
      }

      return processedItems;
   }

   private static void deleteAiParts(List<Long> piOids, Class partType, FieldRef fkAiField, Session session)
   {
      deleteAiParts(piOids, partType, fkAiField, null, session);
   }

   private static void deleteAiParts(List<Long> piOids, Class partType, FieldRef fkAiField,
         PredicateTerm restriction, Session session)
   {
      delete2ndLevelPiParts(piOids, partType, fkAiField, ActivityInstanceBean.class,
            ActivityInstanceBean.FR__PROCESS_INSTANCE, restriction, session);
   }

   private static void deleteDvParts(List<Long> piOids, Class partType, FieldRef fkDvField,
         PredicateTerm restriction, Session session)
   {
      delete2ndLevelPiParts(piOids, partType, fkDvField, DataValueBean.class,
            DataValueBean.FR__PROCESS_INSTANCE, restriction, session);
   }


   private static int delete2ndLevelPiParts(List<Long> piOids, Class partType, FieldRef fkPiPartField,
         Class piPartType, FieldRef piOidField, PredicateTerm restriction, Session session)
   {
      TypeDescriptor tdPiPart = TypeDescriptor.get(piPartType);
      return delete2ndLevelPiParts(piOids, partType, fkPiPartField, piPartType, tdPiPart.getPkFields()[0].getName(),  piOidField, restriction, session);
   }

   private static int delete2ndLevelPiParts(List<Long> piOids, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, FieldRef piOidField, PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator<List<Long>> iterator = getChunkIterator(piOids, batchSize); iterator.hasNext();)
      {
         List<Long> piOidsBatch = iterator.next();

         PredicateTerm predicate = Predicates
               .andTerm(
                     Predicates.inList(piOidField, piOidsBatch),
                     (null != restriction) ? restriction : Predicates.TRUE);

         // delete lock rows
         TypeDescriptor tdType = TypeDescriptor.get(partType);
         if (session.isUsingLockTables() && tdType.isDistinctLockTableName())
         {
            Assert.condition(1 == tdType.getPkFields().length,
                  "Lock-tables are not supported for types with compound PKs.");

            String partOid = tdType.getPkFields()[PK_OID].getName();

            QueryDescriptor lckSubselect = QueryDescriptor
                  .from(partType)
                  .select(partOid);

            lckSubselect.innerJoin(piPartType)
                  .on(fkPiPartField, piPartPkName);

            DeleteDescriptor delete = DeleteDescriptor.fromLockTable(partType);
            delete.where(Predicates.inList(delete.fieldRef(partOid), lckSubselect.where(predicate)));

            session.executeDelete(delete);
         }

         // delete data rows

         DeleteDescriptor delete = DeleteDescriptor.from(partType);

         delete.innerJoin(piPartType)
               .on(fkPiPartField, piPartPkName);

         processedItems += session.executeDelete(delete.where(predicate));
      }

      return processedItems;
   }

   /**
    * @return
    */
   private static int getStatementBatchSize()
   {
      return Parameters.instance().getInteger(STMT_BATCH_SIZE,
            DEFAULT_STATEMENT_BATCH_SIZE);
   }

   private static <E> Iterator<List<E>> getChunkIterator(List<E> list, int chunkSize)
   {
      return new ListChunkIterator<E>(list, chunkSize);
   }

   private static final class ListChunkIterator<E> implements Iterator<List<E>>
   {
      private final int chunkSize;
      private final ArrayList<E> list;

      private int offset = 0;

      public ListChunkIterator(List<E> list, int chunkSize)
      {
         super();

         if(chunkSize <= 0)
         {
            throw new IllegalArgumentException("Argument chunkSize must be greater than 0.");
         }

         if(null == list)
         {
            throw new IllegalArgumentException("Argument list must not be null.");
         }

         this.chunkSize = chunkSize;
         this.list = new ArrayList<E>(list);
      }

      public boolean hasNext()
      {
         return offset < list.size();
      }

      public List<E> next()
      {
         ArrayList<E> nextListChunk = new ArrayList<E>(chunkSize);

         int upperLimit = Math.min(list.size() - offset, chunkSize);
         for (int idx = 0; idx < upperLimit; ++idx)
         {
            nextListChunk.add(list.get(offset + idx));
         }

         offset += upperLimit;

         return nextListChunk;
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }
   }

   /*public static void main(String[] args)
   {
      Integer[] intArray = new Integer[] { new Integer(1), new Integer(2),
            new Integer(3), new Integer(4), new Integer(5), new Integer(6),
            new Integer(7), new Integer(8), new Integer(9), new Integer(10) };

      List inList = Arrays.asList(intArray);

      for (Iterator iterator = getChunkIterator(inList, 3); iterator.hasNext();)
      {
         List chunkList = (List) iterator.next();
         System.out.println(chunkList);

         int x = 0;
   }

   }*/

   private ProcessInstanceUtils()
   {
   }

   public static void checkGroupTermination(IProcessInstance process, StopMode stopMode)
   {
      if (process.getStartingActivityInstance() == null)
      {
         IProcessInstance root = process.getRootProcessInstance();
         if (!root.isTerminated() && !root.isAborting() && root.isCaseProcessInstance())
         {
            List<IProcessInstance> members = ProcessInstanceHierarchyBean.findChildren(root);
            if (members.isEmpty())
            {
               stopProcessInstance(root, stopMode);
            }
            else
            {
               for (IProcessInstance member : members)
               {
                  if (!member.isTerminated())
                  {
                     return;
                  }
               }

               Iterator<IActivityInstance> ais = ActivityInstanceBean.getAllForProcessInstance(root);
               while (ais.hasNext())
               {
                  IActivityInstance ai = ais.next();
                  if (!ai.isTerminated())
                  {
                     ai.activate();
                     ActivityThread.schedule(null, null, ai, true, null, Collections.EMPTY_MAP, false);
                  }
               }
            }
         }
      }
   }

   public static IProcessInstance getActualRootPI(IProcessInstance processInstance)
   {
      IProcessInstance rootProcessInstance = processInstance.getRootProcessInstance();
      if (rootProcessInstance.isCaseProcessInstance())
      {
         // find top most parent excluding the case itself
         IProcessInstance parent = ProcessInstanceHierarchyBean.findParentForSubProcessInstanceOid(processInstance.getOID());
         while (parent != null && parent != rootProcessInstance)
         {
            processInstance = parent;
            parent = ProcessInstanceHierarchyBean.findParentForSubProcessInstanceOid(processInstance.getOID());
         }
      }
      else
      {
         processInstance = rootProcessInstance;
      }
      return processInstance;
   }

   /**
    * @return whether the Infinity property to enable support for transient processes is set
    */
   public static boolean isTransientPiSupportEnabled()
   {
      final Parameters params = Parameters.instance();
      final String transientPiSupport = params.getString(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);

      final boolean isOn = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ON.equals(transientPiSupport);
      final boolean isAlwaysTransient = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT.equals(transientPiSupport);
      final boolean isAlwaysDeferred = KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED.equals(transientPiSupport);
      return isOn | isAlwaysTransient | isAlwaysDeferred;
   }

   /**
    * @return whether the activity instances of the given process instance should not be executed in parallel, but serially
    */
   public static boolean isSerialExecutionScenario(final IProcessInstance pi)
   {
      return isTransientExecutionScenario(pi);
   }

   /**
    * @return whether the given process instance is executed transiently
    */
   public static boolean isTransientExecutionScenario(final IProcessInstance pi)
   {
      if ( !isTransientPiSupportEnabled())
      {
         return false;
      }

      if (pi != null)
      {
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
         return AuditTrailPersistence.isTransientExecution(rootPi.getAuditTrailPersistence());
      }
      return false;
   }

   /**
    * <p>
    * Schedules a worker thread processing the queued {@link ActivityThread}s for the given
    * {@link IProcessInstance}, if and only if there are any {@link ActivityThread}s to be processed
    * for the given {@link IProcessInstance}. This worker thread guarantees a serial execution of all
    * {@link ActivityThread}s for the given {@link IProcessInstance}.
    * </p>
    *
    * @param pi the process instance for which a worker thread should be scheduled
    */
   public static void scheduleSerialActivityThreadWorkerIfNecessary(final IProcessInstance pi)
   {
      final boolean piCompleted = pi.getState() == ProcessInstanceState.Completed;
      if (piCompleted)
      {
         return;
      }

      final Map<Long, Queue<SerialActivityThreadData>> map = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SerialActivityThreadWorkerCarrier.SERIAL_ACTIVITY_THREAD_MAP_ID);
      final boolean isActivityThreadAvailable = map.containsKey(pi.getRootProcessInstanceOID());
      if ( !isActivityThreadAvailable)
      {
         return;
      }

      final SerialActivityThreadWorkerCarrier carrier = new SerialActivityThreadWorkerCarrier();
      carrier.setRootProcessInstanceOid(pi.getRootProcessInstanceOID());

      final ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
      ForkingService service = null;
      try
      {
         service = factory.get();
         service.fork(carrier, true);
      }
      finally
      {
         factory.release(service);
      }
   }
}
