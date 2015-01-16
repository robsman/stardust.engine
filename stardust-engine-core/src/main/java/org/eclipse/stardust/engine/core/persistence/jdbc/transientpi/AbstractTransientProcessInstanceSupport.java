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

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Nicolas.Werlein
 */
public abstract class AbstractTransientProcessInstanceSupport
{
   private static final Logger LOGGER = LogManager.getLogger(AbstractTransientProcessInstanceSupport.class);

   public static AbstractTransientProcessInstanceSupport newInstance(final boolean supportsSequences, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      if ((pis == null) || pis.isEmpty())
      {
         return new NoOpTransientProcessInstanceSupport();
      }

      if (ProcessInstanceUtils.isTransientPiSupportEnabled() && !supportsSequences)
      {
         LOGGER.warn("Transient Process instance support cannot be enabled due to lack of support for DB sequences.");
         return new NoOpTransientProcessInstanceSupport();
      }

      final Set<Long> rootPis = determineRootProcessInstances(pis.values());
      if (rootPis.size() == 1)
      {
         return new UniqueRootPiTransientProcessInstanceSupport(rootPis.iterator().next(), pis, ais);
      }
      else
      {
         return new MultipleRootPisTransientProcessInstanceSupport(rootPis, pis, ais);
      }
   }

   /**
    * <p>
    * Allows for collecting all {@link Persistent}s that the currently running {@link Session}
    * has marked for database insertion.
    * </p>
    *
    * @param persistentsToBeInserted {@link Persistent}s marked for database insertion
    */
   public abstract void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted);

   /**
    * <p>
    * Allows for collecting all {@link Persistent}s that the currently running {@link Session}
    * has marked for database deletion.
    * </p>
    *
    * @param persistentToBeDeleted the {@link Persistent} marked for database deletion
    */
   public abstract void addPersistentToBeDeleted(final Persistent persistentToBeDeleted);

   /**
    * <p>
    * Writes the {@link Persistent}s given by means of the last invocation of {@link #addPersistentToBeInserted(List)} to the process instance blob.
    * </p>
    *
    * @param blobBuilder the blob builder responsible for writing to the blob
    * @param typeDesc the {@link TypeDescriptor} of the {@link Persistent} to be written
    */
   public abstract void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc);

   /**
    * <p>
    * Cleans up the in-memory storage, i.e. purges no longer needed process instance blobs and/or auxiliary information.
    * </p>
    */
   public abstract void cleanUpInMemStorage();

   /**
    * <p>
    * Writes the built process instance blob to the in-memory storage.
    * </p>
    *
    * @param blobBuilder the blob builder encapsulating the built process instance blob
    */
   public abstract void writeToInMemStorage(final BlobBuilder blobBuilder);

   /**
    * @return whether the currently processed process instance is a candidate for transient execution
    */
   public abstract boolean arePisTransientExecutionCandidates();

   /**
    * @return whether the process instance graph persist operation will be deferred
    */
   public abstract boolean isDeferredPersist();

   /**
    * @return whether the current session is executed transiently
    */
   public abstract boolean isCurrentSessionTransient();

   /**
    * @return whether all process instances of the currently processed process instance graph are completed
    */
   public abstract boolean areAllPisCompleted();

   /**
    * @return whether transient process instance execution has been cancelled
    */
   public abstract boolean isTransientExecutionCancelled();

   /**
    * @return whether all {@link Persistent}s associated with the currently processed process instance graph need to be written to the process instance blob
    */
   public abstract boolean persistentsNeedToBeWrittenToBlob();

   /**
    * @return an appropriate {@link BlobBuilder} for the particular {@link AbstractTransientProcessInstanceSupport} implementation
    */
   public abstract BlobBuilder newBlobBuilder();

   protected final void resetTransientPiProperty(final Map<Object, PersistenceController> pis)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
         rootPi.setAuditTrailPersistence(AuditTrailPersistence.IMMEDIATE);
      }
   }

   protected final void collectPersistentKeys(final List<Persistent> persistentsToCollect, final Set<PersistentKey> persistentKeys)
   {
      for (final Persistent p : persistentsToCollect)
      {
         if (p instanceof IdentifiablePersistent)
         {
            final long oid = ((IdentifiablePersistent) p).getOID();
            final Class<? extends Persistent> clazz = p.getClass();
            final PersistentKey persistentKey = new PersistentKey(oid, clazz);

            persistentKeys.add(persistentKey);
         }
      }
   }

   private static Set<Long> determineRootProcessInstances(final Collection<PersistenceController> pis)
   {
      final Set<Long> result = newHashSet();

      for (final PersistenceController pc : pis)
      {
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI((IProcessInstance) pc.getPersistent());
         result.add(Long.valueOf(rootPi.getOID()));
      }

      if (result.isEmpty())
      {
         throw new IllegalStateException("Root process instance could not be determined.");
      }

      if (result.size() > 1)
      {
         LOGGER.warn("Root process instance is not unique (OIDs: " + result + ").");
      }

      return result;
   }
}
