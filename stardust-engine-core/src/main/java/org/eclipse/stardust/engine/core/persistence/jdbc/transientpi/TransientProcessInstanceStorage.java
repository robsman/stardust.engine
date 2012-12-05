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
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceStorage
{
   private static final String PERSISTENT_TO_ROOT_PI_MAP_ID = "PersistentToRootProcessInstanceMapID";
   
   private static final String ROOT_PI_TO_PI_BLOB_MAP_ID = "RootProcessInstanceToProcessInstanceBlobMapID";
   
   private final ProcessInstanceBlobsHolder piBlobsHolder;
   
   public static TransientProcessInstanceStorage instance()
   {
      return TransientProcessInstanceStorageHolder.instance;
   }
   
   public void insertOrUpdate(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob, final long rootPiOid)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }
      if (blob == null)
      {
         throw new NullPointerException("Blob must not be null.");
      }
      
      final InsertOrUpdateOperation insertOp = new InsertOrUpdateOperation(persistentKeys, blob, rootPiOid);
      piBlobsHolder.accessPiBlobs(insertOp);
   }

   public void delete(final Set<PersistentKey> persistentKeys, final boolean purgePiGraph, final Set<Long> rootPiOids)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }
      
      if (rootPiOids == null)
      {
         throw new NullPointerException("Root process instance OIDs must not be null.");
      }
      if (rootPiOids.isEmpty())
      {
         throw new IllegalArgumentException("Root process instance OIDs must not be empty.");
      }

      final DeleteOperation deleteOp = new DeleteOperation(persistentKeys, purgePiGraph, rootPiOids);
      piBlobsHolder.accessPiBlobs(deleteOp);
   }
   
   public ProcessInstanceGraphBlob select(final PersistentKey key)
   {
      if (key == null)
      {
         throw new NullPointerException("Persistent key must not be null.");
      }
      
      final SelectOperation selectOp = new SelectOperation(key);
      return piBlobsHolder.accessPiBlobs(selectOp);
   }
   
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
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class PersistentKey extends Pair<Long, Class<? extends Persistent>>
   {
      private static final long serialVersionUID = 7050984137906918466L;

      public PersistentKey(final long oid, final Class<? extends Persistent> clazz)
      {
         super(oid, clazz);
         
         if (clazz == null)
         {
            throw new NullPointerException("The persistent's class must not be empty.");
         }
      }
      
      public long oid()
      {
         return getFirst().longValue();
      }
      
      public Class<? extends Persistent> clazz()
      {
         return getSecond();
      }
   }
   
   /**
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class ProcessInstanceGraphBlob implements Serializable
   {
      private static final long serialVersionUID = -5320735898795375549L;

      final byte[] blob;
      
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
      
      public byte[] blob()
      {
         return blob;
      }
      
      @Override
      public int hashCode()
      {
         return Arrays.hashCode(blob);
      }
      
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
      
      @Override
      public String toString()
      {
         return Arrays.toString(blob);
      }
   }
   
   public static final class TransientProcessInstanceStorageException extends PublicException
   {
      private static final long serialVersionUID = 8860981344823291826L;
      
      private TransientProcessInstanceStorageException(final String errorMsg, final Exception e)
      {
         super(errorMsg, e);
      }
   }
   
   /* package-private */ static class ProcessInstanceBlobsHolder
   {
      /* implicit link between persistents and process instance blob */
      /* to ensure process instance blob is stored only once:        */
      /* a clustered map might store the process instance blob       */
      /* in a serialized value for every single entry                */
      /* (Hazelcast does so, see CRNT-27238)                         */
      private final Map<PersistentKey, Long> persistentToRootPi;
      private final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob;
      
      public ProcessInstanceBlobsHolder()
      {
         this.persistentToRootPi = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(PERSISTENT_TO_ROOT_PI_MAP_ID);
         this.rootPiToPiBlob = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(ROOT_PI_TO_PI_BLOB_MAP_ID);
      }
      
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
   
   /* package-private */ static interface TxAwareClusterSafeOperation<T>
   {
      T execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob);
   }      
   
   /* package-private */ static final class InsertOrUpdateOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final ProcessInstanceGraphBlob blob;
      private final long rootPiOid;
      
      public InsertOrUpdateOperation(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob, final long rootPiOid)
      {
         this.persistentKeys = persistentKeys;
         this.blob = blob;
         this.rootPiOid = rootPiOid;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         if (Parameters.instance().getBoolean(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true))
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
   
   /* package-private */ static final class DeleteOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final boolean removeBlob;
      private final Set<Long> rootPiOids;
      
      public DeleteOperation(final Set<PersistentKey> persistentKeys, final boolean removeBlob, final Set<Long> rootPiOids)
      {
         this.persistentKeys = persistentKeys;
         this.removeBlob = removeBlob;
         this.rootPiOids = rootPiOids;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, Long> persistentToRootPi, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         if (Parameters.instance().getBoolean(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, true))
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
            for (final Long l : rootPiOids)
            {
               rootPiToPiBlob.remove(l);
            }
         }
         
         return null;
      }
   }
   
   /* package-private */ static final class SelectOperation implements TxAwareClusterSafeOperation<ProcessInstanceGraphBlob>
   {
      private final PersistentKey key;
      
      public SelectOperation(final PersistentKey key)
      {
         this.key = key;
      }
      
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
   
   /* package-private */ static final class SelectForRootPiOidOperation implements TxAwareClusterSafeOperation<ProcessInstanceGraphBlob>
   {
      private final long rootPiOid;
      
      public SelectForRootPiOidOperation(final long rootPiOid)
      {
         this.rootPiOid = rootPiOid;
      }
      
      @Override
      public ProcessInstanceGraphBlob execute(final Map<PersistentKey, Long> ignored, final Map<Long, ProcessInstanceGraphBlob> rootPiToPiBlob)
      {
         return rootPiToPiBlob.get(rootPiOid);
      }
   }
   
   /**
    * this class' only purpose is to ensure both safe publication and lazy initialization
    * (see 'lazy initialization class holder' idiom)
    */
   private static final class TransientProcessInstanceStorageHolder
   {
      public static final TransientProcessInstanceStorage instance = new TransientProcessInstanceStorage();
   }
}
