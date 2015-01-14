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

import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * <p>
 * This class aims at facilitating reoccurring operations when processing
 * transient processes.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class UniqueRootPiTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   private final Long rootPiOid;

   private final Set<PersistentKey> persistentKeysToBeInserted = newHashSet();

   private final Set<PersistentKey> persistentKeysToBeDeleted = newHashSet();

   private final boolean pisAreTransientExecutionCandidates;

   private boolean deferredPersist = false;

   private boolean transientSession = false;

   private boolean allPisAreCompleted = false;

   private boolean cancelTransientExecution = false;

   /**
    * <p>
    * The constructor initializing an object of this class with its initial state.
    * </p>
    */
   /* package-private */ UniqueRootPiTransientProcessInstanceSupport(final Long rootPiOid, final boolean pisAreTransientExecutionCandidates, final boolean cancelTransientExecution, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      this.rootPiOid = rootPiOid;
      this.pisAreTransientExecutionCandidates = pisAreTransientExecutionCandidates;
      this.cancelTransientExecution = cancelTransientExecution;

      if (cancelTransientExecution)
      {
         return;
      }

      determineWhetherCurrentSessionIsTransient(pis, (ais != null) ? ais : Collections.<Object, PersistenceController>emptyMap());
      if ( !isCurrentSessionTransient())
      {
         resetTransientPiProperty(pis);
         this.cancelTransientExecution = true;
         return;
      }

      determineWhetherItsDeferredPersist(pis);
      determineWhetherAllPIsAreCompleted(pis);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeInserted(java.util.List)
    */
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      collectPersistentKeys(persistentsToBeInserted, persistentKeysToBeInserted);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeDeleted(org.eclipse.stardust.engine.core.persistence.Persistent)
    */
   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      collectPersistentKeys(Collections.singletonList(persistentToBeDeleted), persistentKeysToBeDeleted);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToBlob(java.util.List, org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor)
    */
   @Override
   public void writeToBlob(final List<Persistent> persistentsToBeInserted, final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      ProcessBlobWriter.writeInstances(blobBuilder, typeDesc, persistentsToBeInserted);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#cleanUpInMemStorage()
    */
   @Override
   public void cleanUpInMemStorage()
   {
      final boolean purgePiGraph;
      final Set<PersistentKey> keysToBeDeleted = new HashSet<PersistentKey>(persistentKeysToBeDeleted);
      if (!isCurrentSessionTransient() || areAllPisCompleted())
      {
         purgePiGraph = true;
         keysToBeDeleted.addAll(persistentKeysToBeInserted);
      }
      else
      {
         purgePiGraph = false;
      }

      if ( !keysToBeDeleted.isEmpty())
      {
         TransientProcessInstanceStorage.instance().delete(keysToBeDeleted, purgePiGraph, Collections.singleton(rootPiOid));
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToInMemStorage(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder)
    */
   @Override
   public void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      if ( !(blobBuilder instanceof ByteArrayBlobBuilder))
      {
         throw new IllegalArgumentException("Blob builder must be of type '" + ByteArrayBlobBuilder.class + "'.");
      }

      final byte[] blob = ((ByteArrayBlobBuilder) blobBuilder).getBlob();
      final ProcessInstanceGraphBlob piBlob = new ProcessInstanceGraphBlob(blob);

      TransientProcessInstanceStorage.instance().insertOrUpdate(piBlob, rootPiOid, persistentKeysToBeInserted);
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
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isDeferredPersist()
    */
   @Override
   public boolean isDeferredPersist()
   {
      return deferredPersist;
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
      final boolean deferredPersist = isDeferredPersist();
      final boolean cancelledTransientExecution = isTransientExecutionCancelled();

      return transientExecutionIntermediateState || deferredPersist || cancelledTransientExecution;
   }

   private void determineWhetherCurrentSessionIsTransient(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      transientSession = true;

      for (final PersistenceController pc : ais.values())
      {
         final IActivityInstance ai = (IActivityInstance) pc.getPersistent();
         if (isSuspendedSubprocessActivityInstance(ai))
         {
            continue;
         }

         transientSession &= ai.isCompleted();
         if ( !isCurrentSessionTransient())
         {
            return;
         }
      }

      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();

         final boolean piDoesNotExistInDB = pc.isCreated();
         final boolean piIsNotInterrupted = pi.getState() != ProcessInstanceState.Interrupted;
         final boolean piIsNotAborted = pi.getState() != ProcessInstanceState.Aborted;
         final boolean piIsNotAborting = pi.getState() != ProcessInstanceState.Aborting;

         transientSession &= piDoesNotExistInDB && piIsNotInterrupted && piIsNotAborted && piIsNotAborting;
         if ( !isCurrentSessionTransient())
         {
            return;
         }
      }
   }

   private boolean isSuspendedSubprocessActivityInstance(final IActivityInstance ai)
   {
      final boolean isSuspended = ai.getState() == ActivityInstanceState.Suspended;
      final boolean isSubprocessAi = ai.getActivity().getImplementationType() == ImplementationType.SubProcess;
      return isSuspended && isSubprocessAi;
   }

   /**
    *  we can just take an arbitrary pi since it's guaranteed that all point to the same root pi:
    *  if it was not the case, {@link #transientSession} would be <code>false</code> and this
    *  method would not have been invoked
    */
   private void determineWhetherItsDeferredPersist(final Map<Object, PersistenceController> pis)
   {
      final IProcessInstance pi = (IProcessInstance) pis.values().iterator().next().getPersistent();
      final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
      deferredPersist = rootPi.getAuditTrailPersistence() == AuditTrailPersistence.DEFERRED;
   }

   private void determineWhetherAllPIsAreCompleted(final Map<Object, PersistenceController> pis)
   {
      allPisAreCompleted = true;

      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         allPisAreCompleted &= pi.getState() == ProcessInstanceState.Completed;

         if ( !areAllPisCompleted())
         {
            return;
         }
      }
   }
}
