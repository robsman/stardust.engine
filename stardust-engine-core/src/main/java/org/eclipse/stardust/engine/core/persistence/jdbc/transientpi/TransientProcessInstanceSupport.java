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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.lang.reflect.Field;
import java.util.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.BlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobReader;
import org.eclipse.stardust.engine.core.persistence.jms.ProcessBlobWriter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;

/**
 * <p>
 * TODO (nw) javadoc
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceSupport
{
   private static final Logger LOGGER = LogManager.getLogger(TransientProcessInstanceSupport.class);
   
   private final boolean enabled;
   
   private boolean pisAreTransientExecutionCandidates = false;
   
   private boolean deferredPersist = false;
   
   private boolean transientSession = false;
   
   private boolean allPisAreCompleted = false;
   
   private boolean cancelTransientExecution = false;
   
   private final Set<Long> rootPiOids = newHashSet();
   
   private final Set<PersistentKey> persistentKeysToBeInserted;
   
   private final Set<PersistentKey> persistentKeysToBeDeleted;
   
   public TransientProcessInstanceSupport(final boolean supportsSequences)
   {
      if (ProcessInstanceUtils.isTransientPiSupportEnabled() && supportsSequences)
      {
         this.persistentKeysToBeInserted = newHashSet();
         this.persistentKeysToBeDeleted = newHashSet();
         this.enabled = true;
      }
      else
      {
         if (ProcessInstanceUtils.isTransientPiSupportEnabled() && !supportsSequences)
         {
            LOGGER.warn("Transient Process instance support cannot be enabled due to lack of support for DB sequences.");
         }
         
         this.persistentKeysToBeInserted = Collections.emptySet();
         this.persistentKeysToBeDeleted = Collections.emptySet();
         this.enabled = false;
      }
   }
   
   public void init(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      assertEnabled();
      
      determineWhetherPisAreTransientExecutionCandidates(pis);
      if ( !arePisTransientExecutionCandidates())
      {
         return;
      }
      
      determineWhetherCurrentSessionIsTransient(pis, (ais != null) ? ais : Collections.<Object, PersistenceController>emptyMap());
      if ( !isCurrentSessionTransient())
      {
         resetTransientPiProperty(pis);
         cancelTransientExecution = true;
         return;
      }
      
      determineWhetherItsDeferredPersist(pis);
      determineWhetherAllPIsAreCompleted(pis);
   }
   
   public void collectPersistentKeysToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      collectPersistentKeys(persistentsToBeInserted, persistentKeysToBeInserted);
   }
   
   public void collectPersistentKeyToBeDeleted(final Persistent persistentToBeDeleted)
   {
      collectPersistentKeys(Collections.singletonList(persistentToBeDeleted), persistentKeysToBeDeleted);
   }
   
   private void collectPersistentKeys(final List<Persistent> persistentsToCollect, final Set<PersistentKey> persistentKeys)
   {
      assertEnabled();
      
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
   
   public void writeToBlob(final List<Persistent> persistentsToBeInserted, final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      assertEnabled();
      
      /* write persistents to blob */
      ProcessBlobWriter.writeInstances(blobBuilder, typeDesc, persistentsToBeInserted);
   }
   
   public void cleanUpInMemStorage()
   {
      assertEnabled();
      
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
         TransientProcessInstanceStorage.instance().delete(keysToBeDeleted, purgePiGraph, rootPiOids);
      }
   }
   
   public void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      assertEnabled();
      
      if ( !(blobBuilder instanceof ByteArrayBlobBuilder))
      {
         throw new IllegalArgumentException("Blob builder must be of type '" + ByteArrayBlobBuilder.class + "'.");
      }
      
      if (rootPiOids.isEmpty())
      {
         throw new IllegalStateException("Root Process Instance OID has not been initialized.");
      }
      
      final byte[] blob = ((ByteArrayBlobBuilder) blobBuilder).getBlob();
      final ProcessInstanceGraphBlob piBlob = new ProcessInstanceGraphBlob(blob);
      
      /* it's safe to assume that for a write operation (i.e. the session is still transient) */
      /* the root PI OID is unique, i.e. the set only contains one element                    */
      final long rootPiOid = rootPiOids.iterator().next().longValue();
      
      TransientProcessInstanceStorage.instance().insertOrUpdate(persistentKeysToBeInserted, piBlob, rootPiOid);
   }
   
   public boolean arePisTransientExecutionCandidates()
   {
      return pisAreTransientExecutionCandidates;
   }
   
   public boolean isDeferredPersist()
   {
      return deferredPersist;
   }
   
   public boolean isCurrentSessionTransient()
   {
      return transientSession;
   }
   
   public boolean areAllPisCompleted()
   {
      assertEnabled();
      
      return allPisAreCompleted;
   }
   
   public boolean isTransientExecutionCancelled()
   {
      return cancelTransientExecution;
   }
   
   public boolean persistentsNeedToBeWrittenToBlob()
   {
      final boolean transientExecutionIntermediateState = isCurrentSessionTransient() && !areAllPisCompleted();
      final boolean deferredPersist = isDeferredPersist();
      final boolean cancelledTransientExecution = isTransientExecutionCancelled();
      
      return transientExecutionIntermediateState || deferredPersist || cancelledTransientExecution;
   }
   
   public static void loadProcessInstanceGraphIfExistent(final long rootPiOid, final Session session)
   {
      final ProcessInstanceGraphBlob blob = TransientProcessInstanceStorage.instance().selectForRootPiOid(rootPiOid);
      if (blob == null)
      {
         return;
      }
      
      loadProcessInstanceGraph(blob, session, null);
   }
   
   public static Persistent loadProcessInstanceGraphIfExistent(final PersistentKey pk, final Session session)
   {
      final ProcessInstanceGraphBlob blob = TransientProcessInstanceStorage.instance().select(pk);
      if (blob == null)
      {
         return null;
      }      
      
      return loadProcessInstanceGraph(blob, session, pk);
   }
   
   private static Persistent loadProcessInstanceGraph(final ProcessInstanceGraphBlob blob, final Session session, final PersistentKey pk)
   {
      final ProcessBlobReader reader = new ProcessBlobReader(session);
      final Set<Persistent> persistents = reader.readProcessBlob(blob);
      
      final Set<Persistent> deferredPersistents = newHashSet();
      Persistent result = null;
      for (final Persistent p : persistents)
      {
         if (isPkNotSet(p, session))
         {
            deferredPersistents.add(p);
            continue;
         }
         loadPersistent(p, session);
         if (result == null && pk != null)
         {
            result = identifyLookedUpPersistent(p, pk);
         }
      }
      
      if (result == null && pk != null)
      {
         throw new IllegalStateException("Persistent could not be found in the corresponding process instance graph.");
      }
      
      for (final Persistent p : deferredPersistents)
      {
         ensurePkLinksAreFetched(p, session);
         loadPersistent(p, session);
      }
      
      return result;
   }
   
   private void determineWhetherPisAreTransientExecutionCandidates(final Map<Object, PersistenceController> pis)
   {
      pisAreTransientExecutionCandidates = (pis != null) && !pis.isEmpty();
      
      if ( !arePisTransientExecutionCandidates())
      {
         return;
      }
      
      for (final PersistenceController pc : pis.values())
      {
         if ( !pc.isCreated())
         {
            pisAreTransientExecutionCandidates = false;
            break;
         }
         
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         pisAreTransientExecutionCandidates &= ProcessInstanceUtils.isTransientExecutionScenario(pi);
      }
   }
      
   private void determineWhetherCurrentSessionIsTransient(final Map<Object, PersistenceController> pis, final Map<Object, PersistenceController> ais)
   {
      transientSession = determineWhetherRootProcessInstanceIsUnique(pis.values());
      if ( !isCurrentSessionTransient())
      {
         return;
      }
      
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
   
   private boolean determineWhetherRootProcessInstanceIsUnique(final Collection<PersistenceController> pis)
   {
      boolean result = true;
      
      for (final PersistenceController pc : pis)
      {
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI((IProcessInstance) pc.getPersistent());
         rootPiOids.add(Long.valueOf(rootPi.getOID()));
      }
      
      if (rootPiOids.isEmpty())
      {
         throw new IllegalStateException("Root process instance could not be determined.");
      }

      if (rootPiOids.size() > 1)
      {
         result = false;
         LOGGER.warn("Root process instance is not unique (OIDs: " + rootPiOids + ").");
      }
      
      return result;
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
   
   private void resetTransientPiProperty(final Map<Object, PersistenceController> pis)
   {
      for (final PersistenceController pc : pis.values())
      {
         final IProcessInstance pi = (IProcessInstance) pc.getPersistent();
         final IProcessInstance rootPi = ProcessInstanceUtils.getActualRootPI(pi);
         rootPi.setAuditTrailPersistence(AuditTrailPersistence.IMMEDIATE);
      }
   }
   
   private void assertEnabled()
   {
      if ( !enabled)
      {
         throw new IllegalStateException("Transient process instance support is disabled.");
      }
   }
   
   private static boolean isPkNotSet(final Persistent persistent, final Session session)
   {
      final TypeDescriptor typeDesc = session.getTypeDescriptor(persistent.getClass());
      final Field[] pkFields = typeDesc.getPkFields();
      for (final Field f : pkFields)
      {
         if (Reflect.getFieldValue(persistent, f) == null)
         {
            return true;
         }
      }
      
      return false;
   }
   
   private static void ensurePkLinksAreFetched(final Persistent persistent, final Session session)
   {
      final PersistenceController pc = persistent.getPersistenceController();
      
      final TypeDescriptor typeDesc = session.getTypeDescriptor(persistent.getClass());
      final Field[] pkFields = typeDesc.getPkFields();
      for (final Field f : pkFields)
      {
         final String linkName = f.getName();
         if (typeDesc.getLink(linkName) != null)
         {
            pc.fetchLink(linkName);
         }
      }
   }
   
   private static void loadPersistent(final Persistent p, final Session session)
   {
      final TypeDescriptor typeDesc = session.getTypeDescriptor(p.getClass());
      final Object identityKey = typeDesc.getIdentityKey(p);
      session.addToPersistenceControllers(identityKey, p.getPersistenceController());
   }

   private static Persistent identifyLookedUpPersistent(final Persistent p, final PersistentKey pk)
   {
      if ( !(p instanceof IdentifiablePersistent))
      {
         return null;
      }
      
      final IdentifiablePersistent ip = (IdentifiablePersistent) p;
      if (pk.clazz().equals(ip.getClass()) && pk.oid() == ip.getOID())
      {
         return p;
      }
      else
      {
         return null;
      }
   }
   
   /**
    * <p>
    * TODO (nw) javadoc
    * </p>
    */
   private static final class ProcessBlobReader
   {
      private final Session session;
      
      private final TypeDescriptorRegistry typeDescRegistry;

      private final Map<Class<?>, ReadOp> readOps;
      
      public ProcessBlobReader(final Session session)
      {
         if (session == null)
         {
            throw new NullPointerException("Session must not be null.");
         }
         
         this.session = session;
         this.typeDescRegistry = TypeDescriptorRegistry.current();
         this.readOps = initReadOpMap();
      }
      
      public Set<Persistent> readProcessBlob(final ProcessInstanceGraphBlob blob)
      {
         final Set<Persistent> persistents = new HashSet<Persistent>();
         
         final ByteArrayBlobReader reader = new ByteArrayBlobReader(blob.blob);
         reader.nextBlob();
         
         byte sectionMarker;
         while ((sectionMarker = reader.readByte()) != BlobBuilder.SECTION_MARKER_EOF)
         {
            if (sectionMarker != BlobBuilder.SECTION_MARKER_INSTANCES)
            {
               throw new IllegalStateException("Unknown section marker '" + sectionMarker + "'.");
            }
            
            readSection(reader, persistents);
         }
         
         return persistents;
      }
      
      private void readSection(final ByteArrayBlobReader reader, final Set<Persistent> persistents)
      {
         final String tableName = reader.readString();
         final int instanceCount = reader.readInt();
         
         final TypeDescriptor typeDesc = typeDescRegistry.getDescriptorForTable(tableName);
         
         final List<FieldDescriptor> fieldDescs = typeDesc.getPersistentFields();
         final List<LinkDescriptor> linkDescs = typeDesc.getLinks();
         
         for (int i=0; i<instanceCount; i++)
         {
            final Persistent persistent = recreatePersistent(reader, typeDesc, fieldDescs);
            
            final Object[] linkBuffer = recreateLinkBuffer(reader, linkDescs);
            final DefaultPersistenceController pc = new DefaultPersistenceController(session, typeDesc, persistent, linkBuffer);
            pc.markCreated();
            
            persistents.add(persistent);
         }
      }
      
      private Persistent recreatePersistent(final BlobReader reader, final TypeDescriptor typeDesc, final List<FieldDescriptor> fieldDescs)
      {
         final Persistent p = (Persistent) Reflect.createInstance(typeDesc.getType(), null, null);
         for (final FieldDescriptor fd : fieldDescs)
         {
            final Field field = fd.getField();
            final Class<?> fieldType = field.getType();
            final Object fieldValue = readFieldValue(reader, fieldType);
            Reflect.setFieldValue(p, field, fieldValue);
         }
         
         return p;
      }
      
      private Object[] recreateLinkBuffer(final BlobReader reader, final List<LinkDescriptor> linkDescs)
      {
         if (linkDescs.isEmpty())
         {
            return DefaultPersistenceController.NO_LINK_BUFFER;
         }
         
         final Object[] result = new Object[linkDescs.size()];
         for (int i=0; i<linkDescs.size(); i++)
         {
            final LinkDescriptor linkDesc = linkDescs.get(i);
            final Field fkField = linkDesc.getFkField();
            final Class<?> fkFieldType = fkField.getType();
            final Object fkFieldValue = readFieldValue(reader, fkFieldType);
            result[i] = fkFieldValue;
         }

         return result;
      }
      
      private Object readFieldValue(final BlobReader reader, final Class<?> fieldType)
      {
         final ReadOp readOp = readOps.get(fieldType);
         if (readOp == null)
         {
            throw new IllegalArgumentException("Unsupported field type '" + fieldType + "'.");
         }
         
         return readOp.read(reader);
      }
      
      private Map<Class<?>, ReadOp> initReadOpMap()
      {
         final Map<Class<?>, ReadOp> result = newHashMap();
         
         result.put(Boolean.TYPE, new BooleanReadOp());
         result.put(Boolean.class, new BooleanReadOp());
         
         result.put(Byte.TYPE, new ByteReadOp());
         result.put(Byte.class, new ByteReadOp());
         
         result.put(Character.TYPE, new CharacterReadOp());
         result.put(Character.class, new CharacterReadOp());
         
         result.put(Short.TYPE, new ShortReadOp());
         result.put(Short.class, new ShortReadOp());
         
         result.put(Integer.TYPE, new IntegerReadOp());
         result.put(Integer.class, new IntegerReadOp());
         
         result.put(Long.TYPE, new LongReadOp());
         result.put(Long.class, new LongReadOp());
         
         result.put(Float.TYPE, new FloatReadOp());
         result.put(Float.class, new FloatReadOp());
         
         result.put(Double.TYPE, new DoubleReadOp());
         result.put(Double.class, new DoubleReadOp());
         
         result.put(String.class, new StringReadOp());
         
         result.put(Date.class, new DateReadOp());
         
         return result;
      }
      
      private static interface ReadOp
      {
         Object read(final BlobReader reader);
      }

      private static class BooleanReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readBoolean();
         }
      }
      
      private static class ByteReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readByte();
         }
      }
      
      private static class CharacterReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readChar();
         }
      }
      
      private static class ShortReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readShort();
         }
      }

      private static class IntegerReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readInt();
         }
      }
      
      private static class LongReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readLong();
         }
      }
      
      private static class FloatReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readFloat();
         }
      }
      
      private static class DoubleReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readDouble();
         }
      }
      
      private static class StringReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return reader.readString();
         }
      }
      
      private static class DateReadOp implements ReadOp
      {
         @Override
         public Object read(final BlobReader reader)
         {
            return new Date(reader.readLong());
         }
      }
   }
}
