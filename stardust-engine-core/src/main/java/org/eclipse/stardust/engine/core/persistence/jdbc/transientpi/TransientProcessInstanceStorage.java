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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.Persistent;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceStorage
{
   private static final String TRANSIENT_PROCESS_STORAGE_MAP_ID = "TransientProcessStorageMapID";
   
   private static final String TRANSIENT_PROCESS_STORAGE_LOCK_ID = "TransientProcessStorageLockID";

   private final ProcessInstanceBlobsHolder piBlobsHolder;
   
   public static TransientProcessInstanceStorage instance()
   {
      return TransientProcessInstanceStorageHolder.instance;
   }
   
   public void insert(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }
      if (blob == null)
      {
         throw new NullPointerException("Blob must not be null.");
      }
      
      final InsertOperation insertOp = new InsertOperation(persistentKeys, blob);
      piBlobsHolder.accessPiBlobs(insertOp);
   }

   public void delete(final Set<PersistentKey> persistentKeys)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }

      final DeleteOperation deleteOp = new DeleteOperation(persistentKeys);
      piBlobsHolder.accessPiBlobs(deleteOp);
   }
   
   public ProcessInstanceGraphBlob select(final PersistentKey key)
   {
      if (key == null)
      {
         throw new NullPointerException("Persistent key must not be null.");
      }
      
      final SelectOperation selectOp = new SelectOperation(key);
      final ProcessInstanceGraphBlob result = piBlobsHolder.accessPiBlobs(selectOp);
      return result;
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
   
   private static final class ProcessInstanceBlobsHolder
   {
      private final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs;
      
      private final Lock lock;
      
      public ProcessInstanceBlobsHolder()
      {
         this.piBlobs = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(TRANSIENT_PROCESS_STORAGE_MAP_ID);
         this.lock = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeLock(TRANSIENT_PROCESS_STORAGE_LOCK_ID);
      }
      
      public <T> T accessPiBlobs(final TxAwareClusterSafeOperation<T> op)
      {
         final T result;
         try
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();
            lock.lock();
            result = op.execute(piBlobs);
         }
         catch (final Exception e)
         {
            final String errorMsg = "Unable to access process instance storage.";
            throw new TransientProcessInstanceStorageException(errorMsg, e);
         }
         finally
         {
            try
            {
               lock.unlock();
            }
            finally
            {
               ClusterSafeObjectProviderHolder.OBJ_PROVIDER.afterAccess();
            }
         }
         
         return result;
      }
   }
   
   private static interface TxAwareClusterSafeOperation<T>
   {
      T execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs);
   }      
   
   private static final class InsertOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final ProcessInstanceGraphBlob blob;
      
      public InsertOperation(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob)
      {
         this.persistentKeys = persistentKeys;
         this.blob = blob;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs)
      {
         final Set<PersistentKey> intersection = new HashSet<PersistentKey>(piBlobs.keySet());
         intersection.retainAll(persistentKeys);
         
         if (intersection.isEmpty())
         {
            for (final PersistentKey p : persistentKeys)
            {
               piBlobs.put(p, blob);
            }
         }
         else
         {
            throw new IllegalStateException("Trying to override an already existing mapping.");
         }

         return null;
      }
   }
   
   private static final class DeleteOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      
      public DeleteOperation(final Set<PersistentKey> persistentKeys)
      {
         this.persistentKeys = persistentKeys;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs)
      {
         final Set<PersistentKey> intersection = new HashSet<PersistentKey>(persistentKeys);
         intersection.retainAll(piBlobs.keySet());
         final boolean piBlobsToDelete = !intersection.isEmpty();
         if (piBlobsToDelete)
         {
            final PersistentKey key = intersection.iterator().next();
            final ProcessInstanceGraphBlob result = piBlobs.remove(key);
            
            for (final Iterator<ProcessInstanceGraphBlob> iter = piBlobs.values().iterator(); iter.hasNext();)
            {
               final ProcessInstanceGraphBlob next = iter.next();
               if (result.equals(next))
               {
                  iter.remove();
               }
            }
         }
         return null;
      }
   }
   
   private static final class SelectOperation implements TxAwareClusterSafeOperation<ProcessInstanceGraphBlob>
   {
      private final PersistentKey key;
      
      public SelectOperation(final PersistentKey key)
      {
         this.key = key;
      }
      
      @Override
      public ProcessInstanceGraphBlob execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs)
      {
         return piBlobs.get(key);
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
