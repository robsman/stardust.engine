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
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.Persistent;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceStorage
{
   private static final String TRANSIENT_PROCESS_STORAGE_MAP_ID = "TransientProcessStorageMapID";
   
   private final ProcessInstanceBlobsHolder piBlobsHolder;
   
   public static TransientProcessInstanceStorage instance()
   {
      return TransientProcessInstanceStorageHolder.instance;
   }
   
   public void insertOrUpdate(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob)
   {
      if (persistentKeys == null)
      {
         throw new NullPointerException("Persistent keys must not be null.");
      }
      if (blob == null)
      {
         throw new NullPointerException("Blob must not be null.");
      }
      
      final InsertOrUpdateOperation insertOp = new InsertOrUpdateOperation(persistentKeys, blob);
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
   
   /* package-private */ static class ProcessInstanceBlobsHolder
   {
      private final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs;
      
      public ProcessInstanceBlobsHolder()
      {
         this.piBlobs = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(TRANSIENT_PROCESS_STORAGE_MAP_ID);
      }
      
      public <T> T accessPiBlobs(final TxAwareClusterSafeOperation<T> op)
      {
         final T result;
         try
         {
            ClusterSafeObjectProviderHolder.OBJ_PROVIDER.beforeAccess();
            result = op.execute(piBlobs);
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
      T execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs);
   }      
   
   /* package-private */ static final class InsertOrUpdateOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final ProcessInstanceGraphBlob blob;
      
      public InsertOrUpdateOperation(final Set<PersistentKey> persistentKeys, final ProcessInstanceGraphBlob blob)
      {
         this.persistentKeys = persistentKeys;
         this.blob = blob;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs)
      {
         final Map<PersistentKey, ProcessInstanceGraphBlob> blobsToAdd = newHashMap();
         for (final PersistentKey p : persistentKeys)
         {
            blobsToAdd.put(p, blob);
         }
         piBlobs.putAll(blobsToAdd);
         
         return null;
      }
   }
   
   /* package-private */ static final class DeleteOperation implements TxAwareClusterSafeOperation<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      
      public DeleteOperation(final Set<PersistentKey> persistentKeys)
      {
         this.persistentKeys = persistentKeys;
      }
      
      @Override
      public Void execute(final Map<PersistentKey, ProcessInstanceGraphBlob> piBlobs)
      {
         for (final PersistentKey p : persistentKeys)
         {
            piBlobs.remove(p);
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
