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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.InsertOrUpdateOperation;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceBlobsHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TxAwareClusterSafeOperation;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceStorageTest
{
   private static final String PI_BLOBS_HOLDER_FIELD_NAME = "piBlobsHolder";
   
   private static final String PERSISTENT_TO_ROOT_PI_FIELD_NAME = "persistentToRootPi";
   
   private static final String ROOT_PI_TO_PI_BLOB_FIELD_NAME = "rootPiToPiBlob";
   
   private static final long ROOT_PI_OID_1 = 1;
   private static final PersistentKey KEY_1_1 = new PersistentKey(01, ProcessInstanceBean.class);
   private static final PersistentKey KEY_1_2 = new PersistentKey(10, ActivityInstanceBean.class);
   private static final PersistentKey KEY_1_3 = new PersistentKey(11, ActivityInstanceBean.class);
   private static final ProcessInstanceGraphBlob BLOB_1 = new ProcessInstanceGraphBlob(new byte[] { 1, 1, 1, 0, -1});
   
   private static final long ROOT_PI_OID_2 = 2;
   private static final PersistentKey KEY_2_1 = new PersistentKey(02, ProcessInstanceBean.class);
   private static final PersistentKey KEY_2_2 = new PersistentKey(12, ActivityInstanceBean.class);
   private static final PersistentKey KEY_2_3 = new PersistentKey(13, ActivityInstanceBean.class);
   private static final ProcessInstanceGraphBlob BLOB_2 = new ProcessInstanceGraphBlob(new byte[] { 1, 0, 0, 1, -1});
   
   private static final Set<PersistentKey> PERSISTENT_KEYS_1;
   private static final Set<PersistentKey> PERSISTENT_KEYS_2;
   
   static
   {
      PERSISTENT_KEYS_1 = newHashSet();
      PERSISTENT_KEYS_1.add(KEY_1_1);
      PERSISTENT_KEYS_1.add(KEY_1_2);
      PERSISTENT_KEYS_1.add(KEY_1_3);
      
      PERSISTENT_KEYS_2 = newHashSet();
      PERSISTENT_KEYS_2.add(KEY_2_1);
      PERSISTENT_KEYS_2.add(KEY_2_2);
      PERSISTENT_KEYS_2.add(KEY_2_3);
   }
   
   @BeforeClass
   public static void setUpOnce()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.CLUSTER_SAFE_OBJ_PROVIDER, TestingEnvObjectProvider.class.getName());
   }
   
   @Before
   public void setUp()
   {
      dropTransientProcessInstanceStorage();
   }

   @Test
   public void testInstanceThreadSafety() throws Exception
   {
      final int threadCount = 10;
      final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
      final List<TransientProcessInstanceStorage> storages = new CopyOnWriteArrayList<TransientProcessInstanceStorage>();
      final Set<Future<Void>> results = newHashSet();
      
      for (int i=0; i<threadCount; i++)
      {
         final Future<Void> result = executorService.submit(new InstanceResolver(storages));
         results.add(result);
      }
      for (final Future<Void> f : results)
      {
         f.get(5, TimeUnit.SECONDS);
      }
      
      assertThat(storages.size(), is(10));
      final TransientProcessInstanceStorage first = storages.get(0);
      for (final TransientProcessInstanceStorage s : storages)
      {
         assertThat(s == first, is(true));
      }
   }

   @Test
   public void testInsertAndSelect()
   {
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_1, BLOB_1, ROOT_PI_OID_1);
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_2, BLOB_2, ROOT_PI_OID_2);
      
      final ProcessInstanceGraphBlob retrievedBlob1 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob retrievedBlob2 = TransientProcessInstanceStorage.instance().select(KEY_2_3);
      
      assertThat(retrievedBlob1, equalTo(BLOB_1));
      assertThat(retrievedBlob2, equalTo(BLOB_2));
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insertOrUpdate(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testDelete()
   {
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_1, BLOB_1, ROOT_PI_OID_1);
      TransientProcessInstanceStorage.instance().delete(Collections.singleton(KEY_1_1), false, Collections.singleton(ROOT_PI_OID_1));
      TransientProcessInstanceStorage.instance().delete(Collections.singleton(KEY_1_2), false, Collections.singleton(ROOT_PI_OID_1));
      TransientProcessInstanceStorage.instance().delete(Collections.singleton(KEY_1_3), true, Collections.singleton(ROOT_PI_OID_1));
      
      final ProcessInstanceGraphBlob removedBlob1_1 = TransientProcessInstanceStorage.instance().select(KEY_1_1);
      final ProcessInstanceGraphBlob removedBlob1_2 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob removedBlob1_3 = TransientProcessInstanceStorage.instance().select(KEY_1_3);
      
      assertThat(removedBlob1_1, nullValue());
      assertThat(removedBlob1_2, nullValue());
      assertThat(removedBlob1_3, nullValue());
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insertOrUpdate(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testConcurrentDelete() throws Exception
   {
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_1, BLOB_1, ROOT_PI_OID_1);
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_2, BLOB_2, ROOT_PI_OID_2);
      
      final ExecutorService executorService = Executors.newFixedThreadPool(2);
      final Future<Void> result1 = executorService.submit(new Deleter(PERSISTENT_KEYS_1, ROOT_PI_OID_1));
      final Future<Void> result2 = executorService.submit(new Deleter(PERSISTENT_KEYS_2, ROOT_PI_OID_2));
      result1.get(5, TimeUnit.SECONDS);
      result2.get(5, TimeUnit.SECONDS);
      
      final ProcessInstanceGraphBlob blob1_1 = TransientProcessInstanceStorage.instance().select(KEY_1_1);
      final ProcessInstanceGraphBlob blob1_2 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob blob1_3 = TransientProcessInstanceStorage.instance().select(KEY_1_3);
      final ProcessInstanceGraphBlob blob2_1 = TransientProcessInstanceStorage.instance().select(KEY_2_1);
      final ProcessInstanceGraphBlob blob2_2 = TransientProcessInstanceStorage.instance().select(KEY_2_2);
      final ProcessInstanceGraphBlob blob2_3 = TransientProcessInstanceStorage.instance().select(KEY_2_3);
      
      assertThat(blob1_1, nullValue());
      assertThat(blob1_2, nullValue());
      assertThat(blob1_3, nullValue());
      assertThat(blob2_1, nullValue());
      assertThat(blob2_2, nullValue());
      assertThat(blob2_3, nullValue());
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insertOrUpdate(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testTxCommit()
   {
      TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_1, BLOB_1, ROOT_PI_OID_1);
      
      final ProcessInstanceGraphBlob blob1_1 = TransientProcessInstanceStorage.instance().select(KEY_1_1);
      final ProcessInstanceGraphBlob blob1_2 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob blob1_3 = TransientProcessInstanceStorage.instance().select(KEY_1_3);
      
      assertThat(blob1_1, equalTo(BLOB_1));
      assertThat(blob1_2, equalTo(BLOB_1));
      assertThat(blob1_3, equalTo(BLOB_1));
   }

   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insertOrUpdate(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testTxRollback()
   {
      /* causes the insert operation to fail */
      Reflect.setFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME, new TestProcessInstanceBlobsHolder());
      
      try
      {
         TransientProcessInstanceStorage.instance().insertOrUpdate(PERSISTENT_KEYS_1, BLOB_1, ROOT_PI_OID_1);
      }
      catch (final Exception ignored)
      {
         /* expected */
      }
      
      final ProcessInstanceGraphBlob blob1_1 = TransientProcessInstanceStorage.instance().select(KEY_1_1);
      final ProcessInstanceGraphBlob blob1_2 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob blob1_3 = TransientProcessInstanceStorage.instance().select(KEY_1_3);
      
      assertThat(blob1_1, nullValue());
      assertThat(blob1_2, nullValue());
      assertThat(blob1_3, nullValue());
      
      /* restore original object graph */
      Reflect.setFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME, new ProcessInstanceBlobsHolder());
   }
   
   private void dropTransientProcessInstanceStorage()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      
      final Map<?, ?> persistentToRootPi = (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PERSISTENT_TO_ROOT_PI_FIELD_NAME);
      persistentToRootPi.clear();
      
      final Map<?, ?> rootPiToPiBlob = (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, ROOT_PI_TO_PI_BLOB_FIELD_NAME);
      rootPiToPiBlob.clear();
   }
   
   private static final class InstanceResolver implements Callable<Void>
   {
      private final List<TransientProcessInstanceStorage> storages;
      
      public InstanceResolver(final List<TransientProcessInstanceStorage> storages)
      {
         if (storages == null)
         {
            throw new NullPointerException("Storages must not be null.");
         }
         
         this.storages = storages;
      }
      
      @Override
      public Void call()
      {
         final TransientProcessInstanceStorage instance = TransientProcessInstanceStorage.instance();
         storages.add(instance);
         return null;
      }
   }
   
   private static final class Deleter implements Callable<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      private final Long rootPiOid;
      
      public Deleter(final Set<PersistentKey> persistentKeys, final Long rootPiOid)
      {
         if (persistentKeys == null)
         {
            throw new NullPointerException("Persistent keys must not be null.");
         }
         if (persistentKeys.isEmpty())
         {
            throw new IllegalArgumentException("Persistent keys must not be null.");
         }
         if (rootPiOid == null)
         {
            throw new NullPointerException("Root process instance OID must not be null.");
         }
         
         this.persistentKeys = persistentKeys;
         this.rootPiOid = rootPiOid;
      }
      
      @Override
      public Void call()
      {
         TransientProcessInstanceStorage.instance().delete(persistentKeys, true, Collections.singleton(rootPiOid));
         return null;
      }
   }
   
   private static final class TestProcessInstanceBlobsHolder extends ProcessInstanceBlobsHolder
   {
      @Override
      public <T> T accessPiBlobs(final TxAwareClusterSafeOperation<T> op)
      {
         if (op instanceof InsertOrUpdateOperation)
         {
            return super.accessPiBlobs((TxAwareClusterSafeOperation<T>) null);
         }
         
         return super.accessPiBlobs(op);
      }
   }
}
