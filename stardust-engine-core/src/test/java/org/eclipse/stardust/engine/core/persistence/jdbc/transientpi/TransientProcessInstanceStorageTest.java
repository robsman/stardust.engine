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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import junit.framework.Assert;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.PersistentKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.TransientProcessInstanceStorageException;
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
   
   private static final String PI_BLOBS_FIELD_NAME = "piBlobs";
   
   private static final PersistentKey KEY_1_1 = new PersistentKey(01, ProcessInstanceBean.class);
   private static final PersistentKey KEY_1_2 = new PersistentKey(10, ActivityInstanceBean.class);
   private static final PersistentKey KEY_1_3 = new PersistentKey(11, ActivityInstanceBean.class);
   private static final ProcessInstanceGraphBlob BLOB_1 = new ProcessInstanceGraphBlob(new byte[] { 1, 1, 1, 0, -1});
   
   private static final PersistentKey KEY_2_1 = new PersistentKey(02, ProcessInstanceBean.class);
   private static final PersistentKey KEY_2_2 = new PersistentKey(12, ActivityInstanceBean.class);
   private static final PersistentKey KEY_2_3 = new PersistentKey(13, ActivityInstanceBean.class);
   private static final ProcessInstanceGraphBlob BLOB_2 = new ProcessInstanceGraphBlob(new byte[] { 1, 0, 0, 1, -1});

   private static final PersistentKey KEY_3_1 = new PersistentKey(02, ProcessInstanceBean.class);
   private static final PersistentKey KEY_3_2 = new PersistentKey(03, ProcessInstanceBean.class);
   private static final PersistentKey KEY_3_3 = new PersistentKey(04, ProcessInstanceBean.class);
   private static final ProcessInstanceGraphBlob BLOB_3 = new ProcessInstanceGraphBlob(new byte[] { 1, 1, 0, 1, -1});
   
   private static final Set<PersistentKey> PERSISTENT_KEYS_1;
   private static final Set<PersistentKey> PERSISTENT_KEYS_2;
   private static final Set<PersistentKey> PERSISTENT_KEYS_3;
   
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
      
      PERSISTENT_KEYS_3 = newHashSet();
      PERSISTENT_KEYS_3.add(KEY_3_1);
      PERSISTENT_KEYS_3.add(KEY_3_2);
      PERSISTENT_KEYS_3.add(KEY_3_3);
   }
   
   @BeforeClass
   public static void setUpOnce()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.CLUSTER_SAFE_OBJ_PROVIDER, NonClusteredEnvObjectProvider.class.getName());
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
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_1, BLOB_1);
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_2, BLOB_2);
      
      final ProcessInstanceGraphBlob retrievedBlob1 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob retrievedBlob2 = TransientProcessInstanceStorage.instance().select(KEY_2_3);
      
      assertThat(retrievedBlob1, equalTo(BLOB_1));
      assertThat(retrievedBlob2, equalTo(BLOB_2));
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insert(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testDelete()
   {
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_1, BLOB_1);
      TransientProcessInstanceStorage.instance().delete(Collections.singleton(KEY_1_2));
      
      final ProcessInstanceGraphBlob removedBlob1_1 = TransientProcessInstanceStorage.instance().select(KEY_1_1);
      final ProcessInstanceGraphBlob removedBlob1_2 = TransientProcessInstanceStorage.instance().select(KEY_1_2);
      final ProcessInstanceGraphBlob removedBlob1_3 = TransientProcessInstanceStorage.instance().select(KEY_1_3);
      
      assertThat(removedBlob1_1, nullValue());
      assertThat(removedBlob1_2, nullValue());
      assertThat(removedBlob1_3, nullValue());
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#select(PersistentKey)} is working.
    * </p>
    */
   @Test
   public void testIllegalInsert()
   {
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_2, BLOB_2);
      try
      {
         TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_3, BLOB_3);
         Assert.fail();
      }
      catch (final TransientProcessInstanceStorageException e)
      {
         /* expected */
         assertThat(e.getCause(), instanceOf(IllegalStateException.class));
      }
      
      final ProcessInstanceGraphBlob commonKeyBlob = TransientProcessInstanceStorage.instance().select(KEY_2_1);
      final ProcessInstanceGraphBlob blob2_2 = TransientProcessInstanceStorage.instance().select(KEY_2_2);
      final ProcessInstanceGraphBlob blob2_3 = TransientProcessInstanceStorage.instance().select(KEY_2_3);
      final ProcessInstanceGraphBlob blob3_2 = TransientProcessInstanceStorage.instance().select(KEY_3_2);
      final ProcessInstanceGraphBlob blob3_3 = TransientProcessInstanceStorage.instance().select(KEY_3_3);
      
      assertThat(commonKeyBlob, equalTo(BLOB_2));
      assertThat(blob2_2, equalTo(BLOB_2));
      assertThat(blob2_3, equalTo(BLOB_2));
      assertThat(blob3_2, nullValue());
      assertThat(blob3_3, nullValue());
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#select(PersistentKey)} is working.
    * </p>
    */
   @Test
   public void testConcurrentInsert() throws Exception
   {
      final Pair pair1 = new Pair<Set<PersistentKey>, ProcessInstanceGraphBlob>(PERSISTENT_KEYS_2, BLOB_2);
      final Inserter inserter1 = new Inserter(pair1);
      final Pair pair2 = new Pair<Set<PersistentKey>, ProcessInstanceGraphBlob>(PERSISTENT_KEYS_3, BLOB_3);
      final Inserter inserter2 = new Inserter(pair2);
      
      final ExecutorService executorService = Executors.newFixedThreadPool(2);
      final Future<Void> result1 = executorService.submit(inserter1);
      final Future<Void> result2 = executorService.submit(inserter2);
      result1.get(5, TimeUnit.SECONDS);
      result2.get(5, TimeUnit.SECONDS);

      final ProcessInstanceGraphBlob commonKeyBlob = TransientProcessInstanceStorage.instance().select(KEY_2_1);
      final ProcessInstanceGraphBlob blob2_2 = TransientProcessInstanceStorage.instance().select(KEY_2_2);
      final ProcessInstanceGraphBlob blob2_3 = TransientProcessInstanceStorage.instance().select(KEY_2_3);
      final ProcessInstanceGraphBlob blob3_2 = TransientProcessInstanceStorage.instance().select(KEY_3_2);
      final ProcessInstanceGraphBlob blob3_3 = TransientProcessInstanceStorage.instance().select(KEY_3_3);
      
      if (commonKeyBlob.equals(BLOB_2))
      {
         assertThat(blob2_2, equalTo(BLOB_2));
         assertThat(blob2_3, equalTo(BLOB_2));
         assertThat(blob3_2, nullValue());
         assertThat(blob3_3, nullValue());
      }
      else if (commonKeyBlob.equals(BLOB_3))
      {
         assertThat(blob2_2, nullValue());
         assertThat(blob2_3, nullValue());
         assertThat(blob3_3, equalTo(BLOB_3));
         assertThat(blob3_3, equalTo(BLOB_3));
      }
      else
      {
         Assert.fail("Blob must be either BLOB_2 or BLOB_3.");
      }
   }
   
   /**
    * <p>
    * <b>Note</b> Assumes that {@link TransientProcessInstanceStorage#insert(Set, ProcessInstanceGraphBlob)} and
    * {@link TransientProcessInstanceStorage#select(PersistentKey)} are working.
    * </p>
    */
   @Test
   public void testConcurrentDelete() throws Exception
   {
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_1, BLOB_1);
      TransientProcessInstanceStorage.instance().insert(PERSISTENT_KEYS_2, BLOB_2);
      
      final ExecutorService executorService = Executors.newFixedThreadPool(2);
      final Future<Void> result1 = executorService.submit(new Deleter(PERSISTENT_KEYS_1));
      final Future<Void> result2 = executorService.submit(new Deleter(PERSISTENT_KEYS_2));
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
   
   private void dropTransientProcessInstanceStorage()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      final Map<?, ?> piBlobs = (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PI_BLOBS_FIELD_NAME);
      piBlobs.clear();
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
   
   private static final class Inserter implements Callable<Void>
   {
      private final Pair<Set<PersistentKey>, ProcessInstanceGraphBlob> entry;
      
      public Inserter(final Pair<Set<PersistentKey>, ProcessInstanceGraphBlob> entry)
      {
         if (entry == null)
         {
            throw new NullPointerException("Entry must not be null.");
         }
         
         this.entry = entry;
      }
      
      @Override
      public Void call()
      {
         try
         {
            TransientProcessInstanceStorage.instance().insert(entry.getFirst(), entry.getSecond());
         }
         catch (final TransientProcessInstanceStorageException e)
         {
            /* will happen for one of the inserters */
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
         }
         
         return null;
      }
   }
   
   private static final class Deleter implements Callable<Void>
   {
      private final Set<PersistentKey> persistentKeys;
      
      public Deleter(final Set<PersistentKey> persistentKeys)
      {
         if (persistentKeys == null)
         {
            throw new NullPointerException("Persistent keys must not be null.");
         }
         if (persistentKeys.isEmpty())
         {
            throw new IllegalArgumentException("Persistent keys must not be null.");
         }
         
         this.persistentKeys = persistentKeys;
      }
      
      @Override
      public Void call()
      {
         TransientProcessInstanceStorage.instance().delete(persistentKeys);
         return null;
      }
   }
}
