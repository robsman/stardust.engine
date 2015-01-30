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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.archive.ImportFilter;
import org.eclipse.stardust.engine.core.persistence.archive.ImportOidResolver;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobReader;

/**
 * <p>
 * Contains utility methods for common tasks wrt. transient process execution.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TransientProcessInstanceUtils
{
   /**
    * <p>
    * Loads the process instance graph identified by the given root process instance OID,
    * i.e. reads the corresponding process instance blob from the in-memory storage and
    * attaches all included {@link Persistent}s to the given {@link Session}'s cache. If
    * no matching process instance blob can be found in the in-memory storage, this method
    * silently returns without modifying the {@link Session}.
    * </p>
    *
    * @param rootPiOid
    *           the root process instance OID whose process instance graph should be
    *           loaded
    * @param session
    *           the session the {@link Persistent}s should be populated to
    */
   public static void loadProcessInstanceGraphIfExistent(final long rootPiOid,
         final Session session)
   {
      final ProcessInstanceGraphBlob blob = TransientProcessInstanceStorage.instance()
            .selectForRootPiOid(rootPiOid);
      if (blob == null)
      {
         return;
      }

      loadProcessInstanceGraph(blob, session, null);
   }

   /**
    * <p>
    * Loads the process instance graph identified by the given {@link PersistentKey}, i.e.
    * reads the corresponding process instance blob from the in-memory storage and
    * attaches all included {@link Persistent}s to the given {@link Session}'s cache. If
    * no matching process instance blob can be found in the in-memory storage, this method
    * silently returns without modifying the {@link Session}.
    * </p>
    *
    * @param pk
    *           the {@link PersistentKey} identifying a {@link Persistent} whose process
    *           instance graph should be loaded
    * @param session
    *           the session the {@link Persistent}s should be populated to
    * @return the resolved {@link Persistent} matching the given {@link PersistentKey}, or
    *         <code>null</code> if no matching process instance blob can be found
    */
   public static Persistent loadProcessInstanceGraphIfExistent(final PersistentKey pk,
         final Session session)
   {
      final ProcessInstanceGraphBlob blob = TransientProcessInstanceStorage.instance()
            .select(pk);
      if (blob == null)
      {
         return null;
      }

      Set<Persistent> results = loadProcessInstanceGraph(blob, session, pk);
      if (CollectionUtils.isNotEmpty(results))
      {
         return results.iterator().next();
      }
      else
      {
         return null;
      }
   }

   /**
    * 
    * @param blob
    * @param session
    * @param pk
    * @param filter
    *           Filter to use when importing processes. Null filter will import all
    *           processes.
    * @param oidResolver
    * @return
    */
   private static Set<Persistent> loadProcessInstanceGraph(
         final ProcessInstanceGraphBlob blob, final Session session,
         final PersistentKey pk)
   {
      final ProcessBlobReader reader = new ProcessBlobReader(session);
      final Set<Persistent> persistents = reader.readProcessBlob(blob.blob);

      return processPersistents(session, pk, persistents);
   }

   public static Set<Persistent> processPersistents(final Session session,
         final PersistentKey pk, final Set<Persistent> persistents)
   {
      final Set<Persistent> deferredPersistents = newHashSet();
      Persistent result = null;
      for (final Persistent p : persistents)
      {
         if (isPkNotSet(p, session) || hasParents(p, session))
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
         throw new IllegalStateException(
               "Persistent could not be found in the corresponding process instance graph.");
      }

      for (final Persistent p : deferredPersistents)
      {
         ensurePkLinksAreFetched(p, session);
         loadPersistent(p, session);
      }

      if (pk != null)
      {
         Set<Persistent> results = new HashSet<Persistent>();
         results.add(result);
         return results;
      }
      else
      {
         return persistents;
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

   private static boolean hasParents(final Persistent persistent, final Session session)
   {
      final TypeDescriptor typeDesc = session.getTypeDescriptor(persistent.getClass());
      final List<LinkDescriptor> parents = typeDesc.getParents();
      return !parents.isEmpty();
   }

   private static void ensurePkLinksAreFetched(final Persistent persistent,
         final Session session)
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

   private static Persistent identifyLookedUpPersistent(final Persistent p,
         final PersistentKey pk)
   {
      if (!(p instanceof IdentifiablePersistent))
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
    * The class responsible for reading a process instance blob in order to (re-) create
    * all {@link Persistent}s encoded in it.
    * </p>
    */
   public static final class ProcessBlobReader
   {
      private final Session session;

      private final TypeDescriptorRegistry typeDescRegistry;

      private final ImportFilter filter;

      private final ImportOidResolver oidResolver;

      /**
       * <p>
       * Initializes an object of this class.
       * </p>
       *
       * @param session
       *           the session used to create {@link Persistent}s from the information
       *           encoded in the blob
       */
      public ProcessBlobReader(final Session session)
      {
         if (session == null)
         {
            throw new NullPointerException("Session must not be null.");
         }

         this.session = session;
         this.typeDescRegistry = TypeDescriptorRegistry.current();
         this.filter = null;
         this.oidResolver = null;
      }

      public ProcessBlobReader(final Session session, final ImportFilter filter,
            final ImportOidResolver oidResolver)
      {
         if (session == null)
         {
            throw new NullPointerException("Session must not be null.");
         }

         this.session = session;
         this.typeDescRegistry = TypeDescriptorRegistry.current();
         this.filter = filter;
         this.oidResolver = oidResolver;
      }

      /**
       * <p>
       * Reads the given process instance blob in order to (re-) create all
       * {@link Persistent}s encoded in it.
       * </p>
       *
       * @param blob
       *           the blob to be read
       * @param filter
       * @param oidResolver
       * @return a set of all {@link Persistent}s encoded in the blob
       */
      public Set<Persistent> readProcessBlob(final byte[] blob)
      {
         final Set<Persistent> persistents = new HashSet<Persistent>();

         final ByteArrayBlobReader reader = new ByteArrayBlobReader(blob);
         reader.nextBlob();

         byte sectionMarker;
         while ((sectionMarker = reader.readByte()) != BlobBuilder.SECTION_MARKER_EOF)
         {
            if (sectionMarker != BlobBuilder.SECTION_MARKER_INSTANCES)
            {
               throw new IllegalStateException("Unknown section marker '" + sectionMarker
                     + "'.");
            }

            readSection(reader, persistents);
         }

         return persistents;
      }

      private void readSection(final ByteArrayBlobReader reader,
            final Set<Persistent> persistents)
      {
         final String tableName = reader.readString();
         final int instanceCount = reader.readInt();

         final TypeDescriptor typeDesc = typeDescRegistry
               .getDescriptorForTable(tableName);

         final List<FieldDescriptor> fieldDescs = typeDesc.getPersistentFields();
         final List<LinkDescriptor> linkDescs = typeDesc.getLinks();

         for (int i = 0; i < instanceCount; i++)
         {
            final Persistent persistent = recreatePersistent(reader, typeDesc, fieldDescs);

            final Object[] linkBuffer = recreateLinkBuffer(reader, linkDescs);

            if (this.filter == null)
            {
               final DefaultPersistenceController pc = new DefaultPersistenceController(
                     session, typeDesc, persistent, linkBuffer);
               pc.markCreated();
               persistents.add(persistent);
            }
            else
            {
               boolean isInFilter = filter.isInFilter(persistent, linkBuffer);
               if (isInFilter)
               {
                  final DefaultPersistenceController pc = new DefaultPersistenceController(
                        session, typeDesc, persistent, linkBuffer);
                  persistents.add(persistent);
                  pc.markCreated();
               }
            }
         }
      }

      private Persistent recreatePersistent(final ByteArrayBlobReader reader,
            final TypeDescriptor typeDesc, final List<FieldDescriptor> fieldDescs)
      {
         final Persistent p = (Persistent) Reflect.createInstance(typeDesc.getType(),
               null, null);
         for (final FieldDescriptor fd : fieldDescs)
         {
            final Field field = fd.getField();
            final Class< ? > fieldType = field.getType();
            Object fieldValue = reader.readFieldValue(fieldType);
            if (this.oidResolver != null && fieldValue.getClass() == Long.class)
            {
               fieldValue = oidResolver.resolve(field, (Long) fieldValue);
            }
            Reflect.setFieldValue(p, field, fieldValue);
         }

         return p;
      }

      private Object[] recreateLinkBuffer(final ByteArrayBlobReader reader,
            final List<LinkDescriptor> linkDescs)
      {
         if (linkDescs.isEmpty())
         {
            return DefaultPersistenceController.NO_LINK_BUFFER;
         }

         final Object[] result = new Object[linkDescs.size()];
         for (int i = 0; i < linkDescs.size(); i++)
         {
            final LinkDescriptor linkDesc = linkDescs.get(i);
            final Field fkField = linkDesc.getFkField();
            final Class< ? > fkFieldType = fkField.getType();
            final Object fkFieldValue = reader.readFieldValue(fkFieldType);
            result[i] = fkFieldValue;
         }

         return result;
      }

   }
}
