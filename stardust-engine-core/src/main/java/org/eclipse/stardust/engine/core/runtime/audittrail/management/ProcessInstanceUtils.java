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

import static org.eclipse.stardust.common.CollectionUtils.newTreeSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.NoteDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.monitoring.MonitoringUtils;
import org.eclipse.stardust.engine.core.persistence.DeleteDescriptor;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.AbortionJanitorCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.AbstractPropertyWithUser;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceLogBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.LogEntryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessAbortionJanitor;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceProperty;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
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
   
   
   public static int deleteProcessInstances(List piOids, Session session)
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

      deleteAiParts(piOids, ActivityInstanceLogBean.class,
            ActivityInstanceLogBean.FR__ACTIVITY_INSTANCE, session);

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
      List<Long> structuredDataOids = findAllStructuredDataOids();
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

      deletePiParts(piOids, ProcessInstanceProperty.class,
            ProcessInstanceProperty.FR__OBJECT_OID, session);

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

      // finally deleting rows from data clusters
      final DataCluster[] dClusters = RuntimeSetup.instance().getDataClusterSetup();

      for (int idx = 0; idx < dClusters.length; ++idx)
      {
         final DataCluster dCluster = dClusters[idx];

         Statement stmt = null;
         try
         {
            stmt = session.getConnection().createStatement();
            StringBuffer buffer = new StringBuffer(100 + piOids.size() * 10);
            buffer.append("DELETE FROM ").append(dCluster.getQualifiedTableName())
                  .append(" WHERE ").append(dCluster.getProcessInstanceColumn())
                  .append(" IN (").append(StringUtils.join(piOids.iterator(), ", ")).append(")");
            if (trace.isDebugEnabled())
            {
               trace.debug(buffer);
            }
            stmt.executeUpdate(buffer.toString());
         }
         catch (SQLException e)
         {
            throw new PublicException(MessageFormat.format(
                  "Failed deleting entries from data cluster table ''{0}''. Reason: {1}.",
                  new Object[] {dCluster.getTableName(), e.getMessage()}), e);
         }
         finally
         {
            QueryUtils.closeStatement(stmt);
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

         abortProcessInstanceFromSessionCache(
               (org.eclipse.stardust.engine.core.persistence.jdbc.Session) pi.getPersistenceController().getSession(),
               processInstance);
      }
      else
      {
         // Mark this PI itself as aborting
         processInstance.setState(ProcessInstanceState.ABORTING);

         // Mark this PI at its root PI as aborting
         final long piOid = processInstance.getOID();
         rootProcessInstance.addAbortingPiOid(piOid);

         // abort the complete subprocess hierarchy asynchronously.
         ProcessAbortionJanitor.scheduleJanitor(new AbortionJanitorCarrier(piOid));
      }
   }

   private static void abortProcessInstanceFromSessionCache(Session session,
         IProcessInstance pi)
   {
      // handle potential abort scenarios
      Set<Long> abortingPiOids = newTreeSet();
      abortingPiOids.add(pi.getOID());

      // phase one: collect full PI hierarchy to be aborted
      Collection<PersistenceController> cachedPiHierarchy = session.getCache(ProcessInstanceHierarchyBean.class);
      for (PersistenceController pcPiHier : cachedPiHierarchy)
      {
         ProcessInstanceHierarchyBean piHier = (ProcessInstanceHierarchyBean) pcPiHier.getPersistent();
         if (pi == piHier.getProcessInstance()) // is parent PI
         {
            abortingPiOids.add(piHier.getSubProcessInstance().getOID());
         }
      }

      // phase two: abort full PI hierarchy starting from PI to be aborted
      Collection<PersistenceController> cachedPis = session.getCache(ProcessInstanceBean.class);
      for (PersistenceController pcPi : cachedPis)
      {
         ProcessInstanceBean piToAbort = (ProcessInstanceBean) pcPi.getPersistent();
         if (abortingPiOids.contains(piToAbort.getOID())) // is abort candidate
         {
            if ( !piToAbort.isTerminated())
            {
               if (piToAbort.isAborting())
               {
                  piToAbort.getRootProcessInstance().removeAbortingPiOid(
                        piToAbort.getOID());
               }
               piToAbort.setState(ProcessInstanceState.ABORTED);
               EventUtils.detachAll(piToAbort);
               MonitoringUtils.processExecutionMonitors().processAborted(piToAbort);
            }
         }
      }

      // phase three: abort all non-terminated AIs for aborted PIs
      for (PersistenceController pcAi : (Collection<PersistenceController>) session.getCache(ActivityInstanceBean.class))
      {
         ActivityInstanceBean aiToAbort = (ActivityInstanceBean) pcAi.getPersistent();
         if (abortingPiOids.contains(aiToAbort.getProcessInstanceOID())
               && !aiToAbort.isTerminated())
         {
            aiToAbort.setState(ActivityInstanceState.ABORTED);
            aiToAbort.removeFromWorklists();
            EventUtils.detachAll(aiToAbort);
         }
      }
   }


   private static List<Long> findAllStructuredDataOids()
   {
      List<Long> dataOids = new LinkedList<Long>();
      for (Iterator modelItr = ModelManagerFactory.getCurrent().getAllModels(); modelItr.hasNext(); )
      {
         IModel model = (IModel)modelItr.next();
         for (IData data : model.getData())
         {
            if (StructuredTypeRtUtils.isStructuredType(data.getType().getId()) ||
                  StructuredTypeRtUtils.isDmsType(data.getType().getId()))
            {
               dataOids.add(Long.valueOf(ModelManagerFactory.getCurrent().getRuntimeOid(data)));
            }
         }
      }
      return dataOids;
   }

   private static int deletePiParts(List piOids, Class partType, FieldRef fkPiField,
         Session session)
   {
      return deletePiParts(piOids, partType, fkPiField, null, session);
   }

   private static int deletePiParts(List piOids, Class partType, FieldRef fkPiField,
         PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator iterator = getChunkIterator(piOids, batchSize); iterator.hasNext();)
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

   private static void deleteAiParts(List piOids, Class partType, FieldRef fkAiField, Session session)
   {
      deleteAiParts(piOids, partType, fkAiField, null, session);
   }

   private static void deleteAiParts(List piOids, Class partType, FieldRef fkAiField,
         PredicateTerm restriction, Session session)
   {
      delete2ndLevelPiParts(piOids, partType, fkAiField, ActivityInstanceBean.class,
            ActivityInstanceBean.FR__PROCESS_INSTANCE, restriction, session);
   }

   private static void deleteDvParts(List piOids, Class partType, FieldRef fkDvField,
         PredicateTerm restriction, Session session)
   {
      delete2ndLevelPiParts(piOids, partType, fkDvField, DataValueBean.class,
            DataValueBean.FR__PROCESS_INSTANCE, restriction, session);
   }


   private static int delete2ndLevelPiParts(List piOids, Class partType, FieldRef fkPiPartField,
         Class piPartType, FieldRef piOidField, PredicateTerm restriction, Session session)
   {
      TypeDescriptor tdPiPart = TypeDescriptor.get(piPartType);
      return delete2ndLevelPiParts(piOids, partType, fkPiPartField, piPartType, tdPiPart.getPkFields()[0].getName(),  piOidField, restriction, session);
   }

   private static int delete2ndLevelPiParts(List piOids, Class partType, FieldRef fkPiPartField,
         Class piPartType, String piPartPkName, FieldRef piOidField, PredicateTerm restriction, Session session)
   {
      int processedItems = 0;
      int batchSize = getStatementBatchSize();

      for (Iterator iterator = getChunkIterator(piOids, batchSize); iterator.hasNext();)
      {
         List piOidsBatch = (List) iterator.next();

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

   private static Iterator/*<List>*/ getChunkIterator(List list, int chunkSize)
   {
      return new ListChunkIterator(list, chunkSize);
   }

   private static final class ListChunkIterator implements Iterator
   {
      private final int chunkSize;
      private final ArrayList list;

      private int offset = 0;

      public ListChunkIterator(List list, int chunkSize)
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
         this.list = new ArrayList(list);
      }

      public boolean hasNext()
      {
         return offset < list.size();
      }

      public Object next()
      {
         ArrayList nextListChunk = new ArrayList(chunkSize);

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

   public static void checkGroupTermination(IProcessInstance process)
   {
      if (process.getStartingActivityInstance() == null)
      {
         IProcessInstance root = process.getRootProcessInstance();
         if (!root.isTerminated() && !root.isAborting() && root.isCaseProcessInstance())
         {
            List<IProcessInstance> members = ProcessInstanceHierarchyBean.findChildren(root);
            if (members.isEmpty())
            {
               abortProcessInstance(root);
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
}
