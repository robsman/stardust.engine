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
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE_DEFAULT_VALUE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

/**
 * <p>
 * This class represents the <i>Transient Process Instance In-Memory Storage</i> used to
 * hold process instance graphs (encoded as {@link ProcessInstanceGraphBlob}) in memory
 * during transient execution. This allows the transient execution of process instances
 * requiring more than one transaction to complete.
 * </p>
 *
 * <p>
 * The following operations for {@link ProcessInstanceGraphBlob}s on this in-memory storage are provided:
 * <ul>
 *    <li><i>Select</i></li>
 *    <li><i>Insert</i></li>
 *    <li><i>Update</i></li>
 *    <li><i>Delete</i></li>
 * </ul>
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TransientProcessInstanceStorage
{
   private static final String PERSISTENT_TO_ROOT_PI_MAP_ID = "stardust::persistentToRootProcessInstanceMap";

   private static final String ROOT_PI_TO_PI_BLOB_MAP_ID = "stardust::rootProcessInstanceToProcessInstanceBlobMap";

   private final ProcessInstanceBlobsHolder piBlobsHolder;

   /**
    * @return the one and only instance of this class
    */
   public static TransientProcessInstanceStorage instance()
   {
      return TransientProcessInstanceStorageHolder.instance;
   }

   /**
    * <p>
    * Inserts the given {@link ProcessInstanceGraphBlob} into the <i>Transient Process Instance In-Memory Storage</i>. If it
    * has already been inserted, the {@link ProcessInstanceGraphBlob} will be updated such that the old entry will be removed
    * and the new one inserted. Afterwards, in both cases the given {@link ProcessInstanceGraphBlob} is associated with the
    * given root process instance OID, meaning from now on this root process instance OID can be used to retrieve the
    * {@link ProcessInstanceGraphBlob} from the <i>Transient Process Instance In-Memory Storage</i>.
    * </p>
    *
    * <p>
    * If {@link KernelTweakingProperties#TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE} is set to <code>true</code>, in addition to
    * the root process instance OID, the given {@link PersistentKey}s can be used to retrieve the {@link ProcessInstanceGraphBlob}
    * from the <i>Transient Process Instance In-Memory Storage</i>.
    * </p>
    *
    * @param blob the {@link ProcessInstanceGraphBlob} to insert or update
    * @param rootPiOid the root process instance OID to associate with the given {@link ProcessInstanceGraphBlob}
    * @param persistentKeys the keys that should be associated with the given {@link ProcessInstanceGraphBlob}
    */
   public void insertOrUpdate(final ProcessInstanceGraphBlob blob, final long rootPiOid, final Set<PersistentKey> persistentKeys)
   {
      if (blob == null)
      {
         throw new NullPointerException("Blob must not be null.");
      }

      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }

      final InsertOrUpdateOperation insertOrUpdateOp = new InsertOrUpdateOperation(blob, rootPiOid, persistentKeys);
      piBlobsHolder.accessPiBlobs(insertOrUpdateOp);
   }

   /**
    * <p>
    * If {@link KernelTweakingProperties#TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE} is set to <code>true</code>, the
    * {@link ProcessInstanceGraphBlob} associations for all given <code>persistentKeys</code> are removed, i.e. afterwards
    * the given {@link PersistentKey}s can no longer be used to retrieve the {@link ProcessInstanceGraphBlob}. This does <b>not</b>
    * remove the actual {@link ProcessInstanceGraphBlob} from the <i>Transient Process Instance In-Memory Storage</i>.
    * </p>
    *
    * <p>
    * If <code>purgePiGraph</code> is <code>true</code>, all {@link ProcessInstanceGraphBlob}s associated with the given
    * <code>rootPiOids</code> are removed from the <i>Transient Process Instance In-Memory Storage</i>.
    * </p>
    *
    * @param persistentKeys the {@link PersistentKey}s whose {@link ProcessInstanceGraphBlob} association should be removed
    * @param purgePiGraph whether {@link ProcessInstanceGraphBlob}s should be removed from the <i>Transient Process Instance In-Memory Storage</i>
    * @param rootPiOid the root process instance OID for which the {@link ProcessInstanceGraphBlob}s should be removed
    */
   public void delete(final Set<PersistentKey> persistentKeys, final boolean purgePiGraph, final Long rootPiOid)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }

      if (rootPiOid == null)
      {
         throw new NullPointerException("Root process instance OID must not be null.");
      }

      final DeleteOperation deleteOp = new DeleteOperation(persistentKeys, purgePiGraph, rootPiOid);
      piBlobsHolder.accessPiBlobs(deleteOp);
   }

   /**
    * <p>
    * This method evaluates only if {@link KernelTweakingProperties#TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE} is set
    * to <code>true</code>, meaning otherwise it will return <code>null</code> regardless of the <i>Transient Process Instance In-Memory Storage</i>'s
    * state.
    * </p>
    *
    * <p>
    * Returns the {@link ProcessInstanceGraphBlob} associated with the given {@link PersistentKey}, if any. Otherwise
    * this method returns <code>null</code>.
    * </p>
    *
    * @param key the {@link PersistentKey} whose {@link ProcessInstanceGraphBlob} should be returned
    * @return the {@link ProcessInstanceGraphBlob} associated with the given {@link PersistentKey}, if any; otherwise <code>null</code>
    */
   public ProcessInstanceGraphBlob select(final PersistentKey key)
   {
      if (key == null)
      {
         throw new NullPointerException("Persistent key must not be null.");
      }

      final SelectOperation selectOp = new SelectOperation(key);
      return piBlobsHolder.accessPiBlobs(selectOp);
   }

   /**
    * <p>
    * Returns the {@link ProcessInstanceGraphBlob} associated with the given root process instance OID, if any. Otherwise
    * this method returns <code>null</code>.
    * </p>
    *
    * @param rootPiOid the root process instance OID whose {@link ProcessInstanceGraphBlob} should be returned
    * @return the {@link ProcessInstanceGraphBlob} associated with the given root process instance OID, if any; otherwise <code>null</code>
    */
   public ProcessInstanceGraphBlob selectForRootPiOid(final long rootPiOid)
   {
      final SelectForRootPiOidOperation selectOp = new SelectForRootPiOidOperation(rootPiOid);
      return piBlobsHolder.accessPiBlobs(selectOp);
   }

   private TransientProcessInstanceStorage()
   {
      piBlobsHolder = new ProcessInstanceBlobsHolder();
   }

   /**
    * <p>
    * This immutable class represents the key used to identify a {@link Persistent}
    * in the in-memory storage, comprising the {@link Persistent}'s OID and its class.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class PersistentKey extends Pair<Long, Class<? extends Persistent>>
   {
      private static final long serialVersionUID = 7050984137906918466L;

      /**
       * <p>
       * Initializes an object of this class with the given data.
       * </p>
       *
       * @param oid the {@link Persistent}'s OID
       * @param clazz the {@link Persistent}'s class
       */
      public PersistentKey(final long oid, final Class<? extends Persistent> clazz)
      {
         super(oid, clazz);

         if (clazz == null)
         {
            throw new NullPointerException("The persistent's class must not be empty.");
         }
      }

      /**
       * @return the {@link Persistent}'s OID
       */
      public long oid()
      {
         return getFirst().longValue();
      }

      /**
       * @return the {@link Persistent}'s class
       */
      public Class<? extends Persistent> clazz()
      {
         return getSecond();
      }
   }

   /**
    * <p>
    * The class encapsulating the process instance blob's raw data.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class ProcessInstanceGraphBlob implements Serializable
   {
      private static final long serialVersionUID = -5320735898795375549L;

      final byte[] blob;

      /**
       * <p>
       * Initializes an object of this class with the given process instance blob.
       * </p>
       *
       * @param blob the to be encapsulated process instance blob
       */
      public ProcessInstanceGraphBlob(final byte[] blob)
      {
         if (blob == null)
         {
            throw new NullPointerException("Blob must not be null.");
         }
         if (blob.length == 0)
         {
            throw new IllegalArgumentException("Blob must not be empty.");
         }

         this.blob = blob;
      }

      /**
       * @return the process instance blob's raw data
       */
      public byte[] blob()
      {
         return blob;
      }

      /* (non-Javadoc)
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return Arrays.hashCode(blob);
      }

      /* (non-Javadoc)
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(final Object obj)
      {
         if ( !(obj instanceof ProcessInstanceGraphBlob))
         {
            return false;
         }

         final ProcessInstanceGraphBlob that = (ProcessInstanceGraphBlob) obj;
         return Arrays.equals(this.blob, that.blob);
      }

      /* (non-Javadoc)
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return Arrays.toString(blob);
      }
   }

   /**
    * <p>
    * The exception thrown when an operation performed on the transient process instance in-memory storage
    * is unable to complete successfully.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class TransientProcessInstanceStorageException extends PublicException
   {
      private static final long serialVersionUID = 8860981344823291826L;

      private TransientProcessInstanceStorageException(final String errorMsg, final Exception e)
      {
         super(errorMsg, e);
      }
   }

   /**
    * <p>
    * The class holding {@link ProcessInstanceGraphBlob} and its associations
    * with {@link PersistentKey}s and the root process instance OID. There are
    * two maps:
    * <ul>
    *    <li>{@link PersistentKey} &#8614; root process instance OID</li>
    *    <li>root process instance OID &#8614; {@link ProcessInstanceGraphBlob}</li>
    * </ul>
    * This separation allows for performance optimizations if the {@link ProcessInstanceGraphBlob}
    * does not need to be accessible via {@link PersistentKey}s: in that case the first map is
    * not maintained at all which decreases map operations significantly.
    * </p>
    */
   /* package-private */ static class ProcessInstanceBlobsHolder
   {
      private final Map<PersistentKey, Long> persistentToRootPi;
      private final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob;

      /**
       * <p>
       * Initializes an object of this class.
       * </p>
       */
      public ProcessInstanceBlobsHolder()
      {
         this.persistentToRootPi = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(PERSISTENT_TO_ROOT_PI_MAP_ID);
         this.rootPiToPiBlob = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(ROOT_PI_TO_PI_BLOB_MAP_ID);
      }

      /**
       * <p>
       * A template method for operations on the <i>Transient Process Instance In-Memory Storage</i> encapsulated
       * in an instance of {@link TxAwareClusterSafeOperation<T>}. This method does the needed setup and teardown
       * work needed before and after accessing the <i>Transient Process Instance In-Memory Storage</i>.
       * </p>
       *
       * @param <T> the return type of the given operation <code>op</code>
       * @param op the operation to be performed on the <i>Transient Process Instance In-Memory Storage</i>
       * @return the value returned by the given operation <code>op</code>
       */
      public <T> T accessPiBlobs(final TxAwareClusterSafeOperation<T> op)
      {
         final T result;
         try
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();
            result = op.execute(persistentToRootPi, rootPiToPiBlob);
         }
         catch (final Exception e)
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.exception(e);

            final String errorMsg = "Unable to access process instance storage.";
            throw new TransientProcessInstanceStorageException(errorMsg, e);
         }
         finally
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
         }

         return result;
      }
   }

   /**
    * <p>
    * The interface operations on the <i>Transient Process Instance In-Memory Storage</i> need to implement.
    * </p>
    *
    * @param <T> the return type of the operation
    */
   /* package-private */ static interface TxAwareClusterSafeOperation<T>
   {
      /**
       * <p>
       * The operation to be performed on the <i>Transient Process Instance In-Memory Storage</i>.
       * </p>
       *
       * @param persistentToRootPi the map associating {@link PersistentKey}s with root process instance OIDs
       * @param rootPiToPiBlob the map associating root process instance OIDs with {@link ProcessInstanceGraphBlob}s
       * @return the result an operation wants to return
       */
      T execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob);
   }

   /**
    * <p>
    * The {@link TxAwareClusterSafeOperation<T>} encapsulating an insert or update operation.
    * </p>
    */
   /* package-private */ static final class InsertOrUpdateOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final ProcessInstanceGraphBlob blob;
      private final long rootPiOid;
      private final Set<PersistentKey> persistentKeys;

      /**
       * <p>
       * Initializes an object of this class with the given data.
       * </p>
       *
       * @param blob the {@link ProcessInstanceGraphBlob} to insert or update
       * @param rootPiOid the root process instance OID to be associated with the {@link ProcessInstanceGraphBlob}
       * @param persistentKeys the {@link PersistentKey} to be associated with the {@link ProcessInstanceGraphBlob}
       */
      public InsertOrUpdateOperation(final ProcessInstanceGraphBlob blob, final long rootPiOid, final Set<PersistentKey> persistentKeys)
      {
         this.blob = blob;
         this.rootPiOid = rootPiOid;
         this.persistentKeys = persistentKeys;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TxAwareClusterSafeOperation#execute(java.util.Map, java.util.Map)
       */
      @Override
      public Void execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         if (Parameters.instance().getBoolean(TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE_DEFAULT_VALUE))
         {
            final Map<PersistentKey, Long> persistentMappingsToAdd = newHashMap();
            for (final PersistentKey p : persistentKeys)
            {
               persistentMappingsToAdd.put(p, rootPiOid);
            }
            persistentToRootPi.putAll(persistentMappingsToAdd);
         }

         rootPiToPiBlob.put(rootPiOid, blob);

         return null;
      }
   }

   /**
    * <p>
    * The {@link TxAwareClusterSafeOperation<T>} encapsulating a delete operation.
    * </p>
    */
   /* package-private */ static final class DeleteOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final boolean removeBlob;
      private final Long rootPiOid;

      /**
       * <p>
       * Initializes an object of this class with the given data.
       * </p>
       *
       * @param persistentKeys the {@link PersistentKey}s whose association with the {@link ProcessInstanceGraphBlob} should be removed
       * @param removeBlob whether the {@link ProcessInstanceGraphBlob} should be removed
       * @param rootPiOid the root process instance OID whose {@link ProcessInstanceGraphBlob}s should be removed
       */
      public DeleteOperation(final Set<PersistentKey> persistentKeys, final boolean removeBlob, final Long rootPiOid)
      {
         this.persistentKeys = persistentKeys;
         this.removeBlob = removeBlob;
         this.rootPiOid = rootPiOid;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TxAwareClusterSafeOperation#execute(java.util.Map, java.util.Map)
       */
      @Override
      public Void execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         if (Parameters.instance().getBoolean(TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE_DEFAULT_VALUE))
         {
            for (final PersistentKey p : persistentKeys)
            {
               /* not every persistent is included in the map:              */
               /* we'd like to avoid aquiring a lock for entries not        */
               /* existing in the map; hence, we're using Map#containsKey() */
               /* up front which does not aquire a possibly unnecessary     */
               /* lock - as opposed to Map#remove()                         */
               if (persistentToRootPi.containsKey(p))
               {
                  persistentToRootPi.remove(p);
               }
            }
         }

         if (removeBlob)
         {
            rootPiToPiBlob.remove(rootPiOid);
         }

         return null;
      }
   }

   /**
    * <p>
    * The {@link TxAwareClusterSafeOperation<T>} encapsulating a select operation.
    * </p>
    */
   /* package-private */ static final class SelectOperation implements TxAwareClusterSafeOperation<ProcessInstanceGraphBlob>
   {
      private final PersistentKey key;

      /**
       * <p>
       * Initializes an object of this class with the given data.
       * </p>
       *
       * @param key the {@link PersistentKey} whose {@link ProcessInstanceGraphBlob} should be returned
       */
      public SelectOperation(final PersistentKey key)
      {
         this.key = key;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TxAwareClusterSafeOperation#execute(java.util.Map, java.util.Map)
       */
      @Override
      public ProcessInstanceGraphBlob execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         final Long rootPiOid = persistentToRootPi.get(key);
         if (rootPiOid == null)
         {
            return null;
         }
         return rootPiToPiBlob.get(rootPiOid);
      }
   }

   /**
    * <p>
    * The {@link TxAwareClusterSafeOperation<T>} encapsulating a select for root process instance OID operation.
    * </p>
    */
   /* package-private */ static final class SelectForRootPiOidOperation implements TxAwareClusterSafeOperation<ProcessInstanceGraphBlob>
   {
      private final long rootPiOid;

      /**
       * <p>
       * Initializes an object of this class with the given data.
       * </p>
       *
       * @param rootPiOid the root process instance OID whose {@link ProcessInstanceGraphBlob} should be returned
       */
      public SelectForRootPiOidOperation(final long rootPiOid)
      {
         this.rootPiOid = rootPiOid;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TxAwareClusterSafeOperation#execute(java.util.Map, java.util.Map)
       */
      @Override
      public ProcessInstanceGraphBlob execute(final Map<PersistentKey, Long> ignored, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         return rootPiToPiBlob.get(rootPiOid);
      }
   }

   /**
    * <p>
    * This class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom).
    * </p>
    */
   private static final class TransientProcessInstanceStorageHolder
   {
      public static final TransientProcessInstanceStorage instance = new TransientProcessInstanceStorage();
   }
}
