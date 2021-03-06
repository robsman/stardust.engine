/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.config.Parameters;
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
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

/**
 * <p>
 * An {@link AbstractTransientProcessInstanceSupport} implementation if there's only one root process instance
 * (common case) for the {@link Persistent}s being flushed.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class UniqueRootPiTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   private final Long rootPiOid;

   private final Set<PersistentKey> allPersistentKeysToBeInserted = newHashSet();
   private final Set<PersistentKey> allPersistentKeysToBeDeleted = newHashSet();

   private List<Persistent> chunkOfPersistentsToBeInserted;

   private final boolean pisAreTransientExecutionCandidates;

   private final boolean cancelTransientExecution;

   private final boolean transientSession;

   private final boolean deferredPersist;

   private final boolean allPisAreCompleted;


   /**
    * <p>
    * The constructor initializing an object of this class with its initial state.
    * </p>
    */
   /* package-private */ UniqueRootPiTransientProcessInstanceSupport(final Long rootPiOid, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      this.rootPiOid = rootPiOid;

      pisAreTransientExecutionCandidates = determineWhetherPisAreTransientExecutionCandidates(pis);
      if ( !pisAreTransientExecutionCandidates)
      {
         cancelTransientExecution = isSwitchFromTransientOrDeferredToImmediate();
         transientSession = false;
         deferredPersist = false;
         allPisAreCompleted = false;
         return;
      }

      transientSession = determineWhetherCurrentSessionIsTransient(pis, (ais != null) ? ais : Collections.<Object, PersistenceController>emptyMap());
      if ( !transientSession)
      {
         resetTransientPiProperty(pis, rootPiOid);
         cancelTransientExecution = true;
         deferredPersist = false;
         allPisAreCompleted = false;
         return;
      }

      cancelTransientExecution = false;
      deferredPersist = determineWhetherItsDeferredPersist(pis);
      allPisAreCompleted = determineWhetherAllPIsAreCompleted(pis);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeInserted(java.util.List)
    */
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      collectPersistentKeys(persistentsToBeInserted, allPersistentKeysToBeInserted);
      this.chunkOfPersistentsToBeInserted = persistentsToBeInserted;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeDeleted(org.eclipse.stardust.engine.core.persistence.Persistent)
    */
   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      collectPersistentKeys(Collections.singletonList(persistentToBeDeleted), allPersistentKeysToBeDeleted);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor)
    */
   @Override
   public void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      ProcessBlobWriter.writeInstances(blobBuilder, typeDesc, chunkOfPersistentsToBeInserted);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#cleanUpInMemStorage()
    */
   @Override
   public void cleanUpInMemStorage()
   {
      final Set<PersistentKey> keysToBeDeleted = new HashSet<PersistentKey>(allPersistentKeysToBeDeleted);
      final boolean purgePiGraph;
      if (!isCurrentSessionTransient() || areAllPisCompleted())
      {
         purgePiGraph = true;
         keysToBeDeleted.addAll(allPersistentKeysToBeInserted);
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.Session, org.eclipse.stardust.common.config.Parameters)
    */
   @Override
   public void storeBlob(final BlobBuilder blobBuilder, final Session session, final Parameters parameters)
   {
      if (isCurrentSessionTransient() && !areAllPisCompleted())
      {
         writeToInMemStorage(blobBuilder);
      }
      else
      {
         writeToAuditTrail(blobBuilder, session, parameters);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#arePisTransientExecutionCandidates()
    */
   @Override
   public boolean arePisTransientExecutionCandidates()
   {
      return pisAreTransientExecutionCandidates;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isCurrentSessionTransient()
    */
   @Override
   public boolean isCurrentSessionTransient()
   {
      return transientSession;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#areAllPisCompleted()
    */
   @Override
   public boolean areAllPisCompleted()
   {
      return allPisAreCompleted;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isTransientExecutionCancelled()
    */
   @Override
   public boolean isTransientExecutionCancelled()
   {
      return cancelTransientExecution;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#persistentsNeedToBeWrittenToBlob()
    */
   @Override
   public boolean persistentsNeedToBeWrittenToBlob()
   {
      final boolean transientExecutionIntermediateState = isCurrentSessionTransient() && !areAllPisCompleted();
      final boolean cancelledTransientExecution = isTransientExecutionCancelled();

      return transientExecutionIntermediateState || deferredPersist || cancelledTransientExecution;
   }

   @Override
   public BlobBuilder newBlobBuilder()
   {
      return new ByteArrayBlobBuilder();
   }

   private boolean determineWhetherPisAreTransientExecutionCandidates(final Map<Object, PersistenceController> pis)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         if ( !ProcessInstanceUtils.isTransientExecutionScenario(pi))
         {
            return false;
         }
      }

      return true;
   }

   private boolean isSwitchFromTransientOrDeferredToImmediate()
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

   private boolean determineWhetherCurrentSessionIsTransient(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      for (final PersistenceController pc : ais.values())
      {
         final IActivityInstance ai = (IActivityInstance) pc.getPersistent();
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

   /**
    *  we can just take an arbitrary pi since it's guaranteed that all point to the same root pi
    */
   private boolean determineWhetherItsDeferredPersist(final Map<Object, PersistenceController> pis)
   {
      final IProcessInstance pi = (IProcessInstance) pis.values().iterator().next().getPersistent();
      final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
      return rootPi.getAuditTrailPersistence() == AuditTrailPersistence.DEFERRED;
   }

   private boolean determineWhetherAllPIsAreCompleted(final Map<Object, PersistenceController> pis)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         if ( !(pi.getState() == ProcessInstanceState.Completed))
         {
            return false;
         }
      }

      return true;
   }

   private void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      final byte[] blob = castToByteArrayBlobBuilder(blobBuilder).getBlob();
      final ProcessInstanceGraphBlob piBlob = new ProcessInstanceGraphBlob(blob);

      TransientProcessInstanceStorage.instance().insertOrUpdate(piBlob, rootPiOid, allPersistentKeysToBeInserted);
   }

   private void writeToAuditTrail(final BlobBuilder blobBuilder, final Session session, final Parameters parameters)
   {
      final ByteArrayBlobBuilder bb = castToByteArrayBlobBuilder(blobBuilder);
      writeOneBlobToAuditTrail(bb, session, parameters);
   }
}
