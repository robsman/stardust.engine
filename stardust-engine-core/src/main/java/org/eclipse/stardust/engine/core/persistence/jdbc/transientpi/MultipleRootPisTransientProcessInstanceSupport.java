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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class MultipleRootPisTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   private final Set<Long> rootPiOids;

   private final Set<PersistentKey> persistentKeysToBeInserted = newHashSet();

   private final Set<PersistentKey> persistentKeysToBeDeleted = newHashSet();

   private final boolean pisAreTransientExecutionCandidates;

   private final boolean cancelTransientExecution;

   /* package-private */ MultipleRootPisTransientProcessInstanceSupport(final Set<Long> rootPiOids, final boolean pisAreTransientExecutionCandidates, final boolean cancelTransientExecution, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      this.rootPiOids = rootPiOids;
      this.pisAreTransientExecutionCandidates = pisAreTransientExecutionCandidates;

      // TODO [CRNT-26302] add transient PI support for more than one root PI
      this.cancelTransientExecution = true;
      resetTransientPiProperty(pis);
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
      if ( !persistentKeysToBeDeleted.isEmpty())
      {
         TransientProcessInstanceStorage.instance().delete(persistentKeysToBeDeleted, true, rootPiOids);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToInMemStorage(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder)
    */
   @Override
   public void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      // TODO [CRNT-26302] add transient PI support for more than one root PI
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
      // TODO [CRNT-26302] add transient PI support for more than one root PI
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isCurrentSessionTransient()
    */
   @Override
   public boolean isCurrentSessionTransient()
   {
      // TODO [CRNT-26302] add transient PI support for more than one root PI
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#areAllPisCompleted()
    */
   @Override
   public boolean areAllPisCompleted()
   {
      // TODO [CRNT-26302] add transient PI support for more than one root PI
      return false;
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
      // TODO [CRNT-26302] add transient PI support for more than one root PI
      return true;
   }
}
