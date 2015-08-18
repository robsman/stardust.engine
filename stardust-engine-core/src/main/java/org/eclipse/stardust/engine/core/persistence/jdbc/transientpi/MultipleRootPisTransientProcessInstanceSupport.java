/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstanceAware;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstanceAware;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

/**
 * <p>
 * An {@link AbstractTransientProcessInstanceSupport} implementation if there's more than one root process instance
 * for the {@link Persistent}s being flushed.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class MultipleRootPisTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   private final Set<Long> rootPiOids;

   private final Map<Long, Set<PersistentKey>> allPersistentKeysToBeInserted = newHashMap();
   private final Map<Long, Set<PersistentKey>> allPersistentKeysToBeDeleted = newHashMap();

   private final Map<Long, List<Persistent>> chunkOfPersistentsToBeInserted = newHashMap();

   private final Map<Long, Boolean> pisAreTransientExecutionCandidatesByRootPiOid = newHashMap();
   private final Map<Long, Boolean> cancelTransientExecutionByRootPiOid = newHashMap();
   private final Map<Long, Boolean> transientSessionByRootPiOid = newHashMap();
   private final Map<Long, Boolean> deferredPersistByRootPiOid = newHashMap();
   private final Map<Long, Boolean> allPisAreCompletedByRootPiOid = newHashMap();

   private final boolean pisAreTransientExecutionCandidatesCumulated;
   private final boolean cancelTransientExecutionCumulated;
   private final boolean transientSessionCumulated;

   /* package-private */ MultipleRootPisTransientProcessInstanceSupport(final Set<Long> rootPiOids, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      this.rootPiOids = rootPiOids;

      initCollections(pis, ais);

      pisAreTransientExecutionCandidatesCumulated = cumulate(pisAreTransientExecutionCandidatesByRootPiOid);
      cancelTransientExecutionCumulated = cumulate(cancelTransientExecutionByRootPiOid);
      transientSessionCumulated = cumulate(transientSessionByRootPiOid);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeInserted(java.util.List)
    */
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      chunkOfPersistentsToBeInserted.clear();
      for (final Persistent p : persistentsToBeInserted)
      {
         final Long rootPiOid = Long.valueOf(determineRootPiOidFor(p));
         if (needBlobFor(rootPiOid))
         {
            collectPersistentKeys(Collections.singletonList(p), allPersistentKeysToBeInserted.get(rootPiOid));

            List<Persistent> list = chunkOfPersistentsToBeInserted.get(rootPiOid);
            if (list == null)
            {
               list = new ArrayList<Persistent>();
               chunkOfPersistentsToBeInserted.put(rootPiOid, list);
            }
            list.add(p);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeDeleted(org.eclipse.stardust.engine.core.persistence.Persistent)
    */
   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      final Long rootPiOid = Long.valueOf(determineRootPiOidFor(persistentToBeDeleted));
      if (pisAreTransientExecutionCandidatesByRootPiOid.get(rootPiOid))
      {
         collectPersistentKeys(Collections.singletonList(persistentToBeDeleted), allPersistentKeysToBeDeleted.get(rootPiOid));
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor)
    */
   @Override
   public void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      final ByteArrayBlobBuilderMediator bb = castToByteArrayBlobBuilderMediator(blobBuilder);
      for (final Entry<Long, List<Persistent>> e : chunkOfPersistentsToBeInserted.entrySet())
      {
         final Long rootPiOid = e.getKey();
         if (needBlobFor(rootPiOid))
         {
            ProcessBlobWriter.writeInstances(bb.blobBuilders().get(rootPiOid), typeDesc, chunkOfPersistentsToBeInserted.get(rootPiOid));
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#cleanUpInMemStorage()
    */
   @Override
   public void cleanUpInMemStorage()
   {
      for (final Entry<Long, Set<PersistentKey>> p : allPersistentKeysToBeDeleted.entrySet())
      {
         final Long rootPiOid = p.getKey();
         if (pisAreTransientExecutionCandidatesByRootPiOid.get(rootPiOid) || cancelTransientExecutionByRootPiOid.get(rootPiOid))
         {
            final Set<PersistentKey> keysToBeDeleted = CollectionUtils.newHashSet(p.getValue());
            final boolean purgePiGraph;
            if ( !transientSessionByRootPiOid.get(rootPiOid) || allPisAreCompletedByRootPiOid.get(rootPiOid))
            {
               purgePiGraph = true;
               keysToBeDeleted.addAll(allPersistentKeysToBeInserted.get(rootPiOid));
            }
            else
            {
               purgePiGraph = false;
            }

            if ( !keysToBeDeleted.isEmpty())
            {
               TransientProcessInstanceStorage.instance().delete(keysToBeDeleted, purgePiGraph, rootPiOid);
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.Session, org.eclipse.stardust.common.config.Parameters)
    */
   @Override
   public void storeBlob(final BlobBuilder blobBuilder, final Session session, final Parameters parameters)
   {
      writeToInMemStorage(blobBuilder);
      writeToAuditTrail(blobBuilder, session, parameters);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#arePisTransientExecutionCandidates()
    */
   @Override
   public boolean arePisTransientExecutionCandidates()
   {
      return pisAreTransientExecutionCandidatesCumulated;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isCurrentSessionTransient()
    */
   @Override
   public boolean isCurrentSessionTransient()
   {
      return transientSessionCumulated;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#areAllPisCompleted()
    */
   @Override
   public boolean areAllPisCompleted()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isTransientExecutionCancelled()
    */
   @Override
   public boolean isTransientExecutionCancelled()
   {
      return cancelTransientExecutionCumulated;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#persistentsNeedToBeWrittenToBlob()
    */
   @Override
   public boolean persistentsNeedToBeWrittenToBlob()
   {
      /* we always do have at least one async subprocess stub which needs to be written either to the in-mem storage */
      /* (if it's transient or deferred) or to the audit trail db (if it's immediate), i.e. in both cases it needs   */
      /* to be written to the blob first                                                                             */
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#newBlobBuilder()
    */
   @Override
   public BlobBuilder newBlobBuilder()
   {
      return new ByteArrayBlobBuilderMediator(rootPiOids);
   }

   private void initCollections(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      for (final Long rootPiOid : rootPiOids)
      {
         allPersistentKeysToBeInserted.put(rootPiOid, new HashSet<PersistentKey>());
         allPersistentKeysToBeDeleted.put(rootPiOid, new HashSet<PersistentKey>());
         chunkOfPersistentsToBeInserted.put(rootPiOid, new ArrayList<Persistent>());

         final boolean pisAreTransientExecutionCandidates = determineWhetherPisAreTransientExecutionCandidates(pis, rootPiOid);
         final boolean cancelTransientExecution;

         if ( !pisAreTransientExecutionCandidates)
         {
            cancelTransientExecution = isSwitchFromTransientOrDeferredToImmediate(rootPiOid);
            pisAreTransientExecutionCandidatesByRootPiOid.put(rootPiOid, Boolean.valueOf(pisAreTransientExecutionCandidates));
            cancelTransientExecutionByRootPiOid.put(rootPiOid, Boolean.valueOf(cancelTransientExecution));
            transientSessionByRootPiOid.put(rootPiOid, Boolean.FALSE);
            deferredPersistByRootPiOid.put(rootPiOid, Boolean.FALSE);
            allPisAreCompletedByRootPiOid.put(rootPiOid, Boolean.FALSE);
            continue;
         }

         final boolean transientSession = determineWhetherCurrentSessionIsTransient(pis, ais, rootPiOid);
         if ( !transientSession)
         {
            resetTransientPiProperty(pis, rootPiOid);
            cancelTransientExecution = true;
            pisAreTransientExecutionCandidatesByRootPiOid.put(rootPiOid, Boolean.valueOf(pisAreTransientExecutionCandidates));
            cancelTransientExecutionByRootPiOid.put(rootPiOid, Boolean.valueOf(cancelTransientExecution));
            transientSessionByRootPiOid.put(rootPiOid, Boolean.valueOf(transientSession));
            deferredPersistByRootPiOid.put(rootPiOid, Boolean.FALSE);
            allPisAreCompletedByRootPiOid.put(rootPiOid, Boolean.FALSE);
            continue;
         }

         cancelTransientExecution = false;
         final boolean deferredPersist = determineWhetherItsDeferredPersist(pis, rootPiOid);
         final boolean allPisAreCompleted = determineWhetherAllPIsAreCompleted(pis, rootPiOid);
         pisAreTransientExecutionCandidatesByRootPiOid.put(rootPiOid, Boolean.valueOf(pisAreTransientExecutionCandidates));
         cancelTransientExecutionByRootPiOid.put(rootPiOid, Boolean.valueOf(cancelTransientExecution));
         transientSessionByRootPiOid.put(rootPiOid, Boolean.valueOf(transientSession));
         deferredPersistByRootPiOid.put(rootPiOid, Boolean.valueOf(deferredPersist));
         allPisAreCompletedByRootPiOid.put(rootPiOid, Boolean.valueOf(allPisAreCompleted));
      }
   }

   private boolean determineWhetherPisAreTransientExecutionCandidates(final Map<Object, PersistenceController> pis, final Long rootPiOid)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();

         if (ProcessInstanceUtils.getActualRootPI(pi).getOID() != rootPiOid.longValue())
         {
            continue;
         }

         if ( !ProcessInstanceUtils.isTransientExecutionScenario(pi))
         {
            return false;
         }
      }

      return true;
   }

   private boolean isSwitchFromTransientOrDeferredToImmediate(final Long rootPiOid)
   {
      final IProcessInstance rootPi = ProcessInstanceBean.findByOID(rootPiOid.longValue());
      final boolean isImmediateNow = rootPi.getAuditTrailPersistence() == AuditTrailPersistence.IMMEDIATE;
      final boolean wasTransient = rootPi.getPreviousAuditTrailPersistence() == AuditTrailPersistence.TRANSIENT;
      final boolean wasDeferred = rootPi.getPreviousAuditTrailPersistence() == AuditTrailPersistence.DEFERRED;
      if (isImmediateNow && (wasTransient || wasDeferred))
      {
         return true;
      }

      return false;
   }

   private boolean determineWhetherCurrentSessionIsTransient(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais, final Long rootPiOid)
   {
      for (final PersistenceController pc : ais.values())
      {
         final IActivityInstance ai = (IActivityInstance) pc.getPersistent();

         if (ProcessInstanceUtils.getActualRootPI(ai.getProcessInstance()).getOID() != rootPiOid)
         {
            continue;
         }

         if (isSuspendedSubprocessActivityInstance(ai))
         {
            continue;
         }

         if ( !ai.isCompleted())
         {
            return false;
         }
      }

      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();

         if (ProcessInstanceUtils.getActualRootPI(pi).getOID() != rootPiOid)
         {
            continue;
         }

         final boolean piDoesNotExistInDB = pc.isCreated();
         final boolean piIsNotInterrupted = pi.getState() != ProcessInstanceState.Interrupted;
         final boolean piIsNotAborted = pi.getState() != ProcessInstanceState.Aborted;
         final boolean piIsNotAborting = pi.getState() != ProcessInstanceState.Aborting;

         if ( !(piDoesNotExistInDB && piIsNotInterrupted && piIsNotAborted && piIsNotAborting))
         {
            return false;
         }
      }

      return true;
   }

   private boolean determineWhetherAllPIsAreCompleted(final Map<Object, PersistenceController> pis, final Long rootPiOid)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();

         if (ProcessInstanceUtils.getActualRootPI(pi).getOID() != rootPiOid)
         {
            continue;
         }

         if ( !(pi.getState() == ProcessInstanceState.Completed))
         {
            return false;
         }
      }

      return true;
   }

   private boolean determineWhetherItsDeferredPersist(final Map<Object, PersistenceController> pis, final Long rootPiOid)
   {
      final IProcessInstance pi = ProcessInstanceBean.findByOID(rootPiOid);
      final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
      return rootPi.getAuditTrailPersistence() == AuditTrailPersistence.DEFERRED;
   }

   private boolean needBlobFor(final Long rootPiOid)
   {
      return transientSessionByRootPiOid.get(rootPiOid) || !allPisAreCompletedByRootPiOid.get(rootPiOid);
   }

   private boolean cumulate(final Map<Long, Boolean> map)
   {
      boolean result = false;

      for (final Boolean b : map.values())
      {
         result |= b.booleanValue();
      }

      return result;
   }

   private ByteArrayBlobBuilderMediator castToByteArrayBlobBuilderMediator(final BlobBuilder blobBuilder)
   {
      if ( !(blobBuilder instanceof ByteArrayBlobBuilderMediator))
      {
         throw new IllegalArgumentException("Blob builder must be of type '" + ByteArrayBlobBuilderMediator.class + "'.");
      }

      return (ByteArrayBlobBuilderMediator) blobBuilder;
   }

   private long determineRootPiOidFor(final Persistent p)
   {
      final IProcessInstance pi;
      if (p instanceof IProcessInstanceAware)
      {
         pi = ((IProcessInstanceAware) p).getProcessInstance();
      }
      else if (p instanceof IActivityInstanceAware)
      {
         pi = (((IActivityInstanceAware) p).getActivityInstance().getProcessInstance());
      }
      else
      {
         throw new UnsupportedOperationException("Cannot determine the root process instance OID of '" + p.getClass() + "'");
      }

      return ProcessInstanceUtils.getActualRootPI(pi).getOID();
   }

   private void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      final ByteArrayBlobBuilderMediator bb = castToByteArrayBlobBuilderMediator(blobBuilder);
      for (final Entry<Long, ByteArrayBlobBuilder> e : bb.blobBuilders().entrySet())
      {
         final Long rootPiOid = e.getKey();
         if (transientSessionByRootPiOid.get(rootPiOid) && !allPisAreCompletedByRootPiOid.get(rootPiOid))
         {
            final ProcessInstanceGraphBlob piBlob = new ProcessInstanceGraphBlob(e.getValue().getBlob());
            TransientProcessInstanceStorage.instance().insertOrUpdate(piBlob, rootPiOid, allPersistentKeysToBeInserted.get(rootPiOid));
         }
      }
   }

   private void writeToAuditTrail(final BlobBuilder blobBuilder, final Session session, final Parameters parameters)
   {
      final Map<Long, ByteArrayBlobBuilder> blobBuilders = ((ByteArrayBlobBuilderMediator) blobBuilder).blobBuilders();
      for (final Entry<Long, ByteArrayBlobBuilder> e : blobBuilders.entrySet())
      {
         final Long rootPiOid = e.getKey();
         if ( !transientSessionByRootPiOid.get(rootPiOid) || (deferredPersistByRootPiOid.get(rootPiOid) && allPisAreCompletedByRootPiOid.get(rootPiOid)))
         {
            writeOneBlobToAuditTrail(e.getValue(), session, parameters);
         }
      }
   }

   /**
    * <p>
    * A {@link BlobBuilder} implementation coping with more than one {@link BlobBuilder},
    * {@link ByteArrayBlobBuilder} in particular.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   public static final class ByteArrayBlobBuilderMediator implements BlobBuilder
   {
      private final Map<Long, ByteArrayBlobBuilder> blobBuilders = CollectionUtils.newHashMap();

      public ByteArrayBlobBuilderMediator(final Set<Long> rootPiOids)
      {
         for (final Long rootPiOid : rootPiOids)
         {
            blobBuilders.put(rootPiOid, new ByteArrayBlobBuilder());
         }
      }

      @Override
      public void init(final Parameters params) throws PublicException
      {
         for (final ByteArrayBlobBuilder b : blobBuilders.values())
         {
            b.init(params);
         }
      }

      @Override
      public void persistAndClose() throws PublicException
      {
         for (final ByteArrayBlobBuilder b : blobBuilders.values())
         {
            b.persistAndClose();
         }
      }

      @Override
      public void startInstancesSection(final String tableName, final int nInstances) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeBoolean(final boolean value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeChar(final char value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeByte(final byte value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeShort(final short value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeInt(final int value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeLong(final long value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeFloat(final float value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeDouble(final double value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void writeString(final String value) throws InternalException
      {
         throw new UnsupportedOperationException();
      }

      /* package-private */ Map<Long, ByteArrayBlobBuilder> blobBuilders()
      {
         return blobBuilders;
      }
   }
}
