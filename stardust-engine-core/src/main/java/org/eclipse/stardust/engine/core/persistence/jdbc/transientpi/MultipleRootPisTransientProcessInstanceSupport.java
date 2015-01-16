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
import java.util.Collection;
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
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstanceAware;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstanceAware;

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

   private final Map<Long, Set<PersistentKey>> allPersistentKeysToBeInserted = newHashMap();
   private final Map<Long, Set<PersistentKey>> allPersistentKeysToBeDeleted = newHashMap();

   private final Map<Long, List<Persistent>> chunkOfPersistentsToBeInserted = newHashMap();

   private final boolean pisAreTransientExecutionCandidates;

   private final boolean cancelTransientExecution;

   /* package-private */ MultipleRootPisTransientProcessInstanceSupport(final Set<Long> rootPiOids, final boolean pisAreTransientExecutionCandidates, final boolean cancelTransientExecution, final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      this.rootPiOids = rootPiOids;
      this.pisAreTransientExecutionCandidates = pisAreTransientExecutionCandidates;

      // TODO [CRNT-26302] add transient PI support for more than one root PI
      this.cancelTransientExecution = true;
      resetTransientPiProperty(pis);

      for (final Long rootPiOid : rootPiOids)
      {
         allPersistentKeysToBeInserted.put(rootPiOid, new HashSet<PersistentKey>());
         allPersistentKeysToBeDeleted.put(rootPiOid, new HashSet<PersistentKey>());

         chunkOfPersistentsToBeInserted.put(rootPiOid, new ArrayList<Persistent>());
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeInserted(java.util.List)
    */
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      // TODO [CRNT-26302] only process those persistents belonging to a pi graph applicable to this operation

      chunkOfPersistentsToBeInserted.clear();
      for (final Persistent p : persistentsToBeInserted)
      {
         final Long rootPiOid = Long.valueOf(determineRootPiOidFor(p));
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeDeleted(org.eclipse.stardust.engine.core.persistence.Persistent)
    */
   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      // TODO [CRNT-26302] only process those persistents belonging to a pi graph applicable to this operation

      final Long rootPiOid = Long.valueOf(determineRootPiOidFor(persistentToBeDeleted));
      collectPersistentKeys(Collections.singletonList(persistentToBeDeleted), allPersistentKeysToBeDeleted.get(rootPiOid));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor)
    */
   @Override
   public void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      // TODO [CRNT-26302] only process those persistents belonging to a pi graph applicable to this operation

      final ByteArrayBlobBuilderMediator bb = castToByteArrayBlobBuilderMediator(blobBuilder);
      for (final Entry<Long, List<Persistent>> e : chunkOfPersistentsToBeInserted.entrySet())
      {
         final Long rootPiOid = e.getKey();
         ProcessBlobWriter.writeInstances(bb.blobBuilders().get(rootPiOid), typeDesc, chunkOfPersistentsToBeInserted.get(rootPiOid));
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#cleanUpInMemStorage()
    */
   @Override
   public void cleanUpInMemStorage()
   {
      // TODO [CRNT-26302] only process those persistents belonging to a pi graph applicable to this operation

      for (final Entry<Long, Set<PersistentKey>> p : allPersistentKeysToBeDeleted.entrySet())
      {
         if ( !p.getValue().isEmpty())
         {
            TransientProcessInstanceStorage.instance().delete(p.getValue(), true, Collections.singleton(p.getKey()));
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToInMemStorage(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder)
    */
   @Override
   public void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      // TODO [CRNT-26302] only process those persistents belonging to a pi graph applicable to this operation

      final ByteArrayBlobBuilderMediator bb = castToByteArrayBlobBuilderMediator(blobBuilder);
      for (final Entry<Long, ByteArrayBlobBuilder> e : bb.blobBuilders().entrySet())
      {
         final Long rootPiOid = e.getKey();
         final ProcessInstanceGraphBlob piBlob = new ProcessInstanceGraphBlob(e.getValue().getBlob());
         TransientProcessInstanceStorage.instance().insertOrUpdate(piBlob, rootPiOid, allPersistentKeysToBeInserted.get(rootPiOid));
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#newBlobBuilder()
    */
   @Override
   public BlobBuilder newBlobBuilder()
   {
      return new ByteArrayBlobBuilderMediator(rootPiOids);
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

      public Collection<ByteArrayBlobBuilder> getBlobBuilders()
      {
         return blobBuilders.values();
      }

      /* package-private */ Map<Long, ByteArrayBlobBuilder> blobBuilders()
      {
         return blobBuilders;
      }
   }
}
