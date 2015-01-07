/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.config;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * This class contains methods for testing {@link GlobalParameters}
 * for thread-safety.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class GlobalParametersTest
{
   private static final int THREAD_COUNT = 1000;

   private static final String THREAD_ID_VALUE = "<a value>";

   //@Ignore("CRNT-30059")
   @Test
   public void testConcurrentModificationViaSet() throws InterruptedException
   {
      executeTest(new ThreadSubmission()
      {
         @Override
         public void submitThreads(final ExecutorService executorService)
         {
            for (int i=0; i<THREAD_COUNT; i++)
            {
               executorService.submit(new GlobalParametersModifierViaSet(String.valueOf(i)));
            }
         }
      });
   }

   @Test
   public void testConcurrentModificationViaGetOrInitialize() throws InterruptedException
   {
      executeTest(new ThreadSubmission()
      {
         @Override
         public void submitThreads(final ExecutorService executorService)
         {
            for (int i=0; i<THREAD_COUNT; i++)
            {
               executorService.submit(new GlobalParametersModifierViaGetOrInitialize(String.valueOf(i)));
            }
         }
      });
   }

   //@Ignore("CRNT-30059")
   @Test
   public void testConcurrentModificationViaSetAndGetOrInitialize() throws InterruptedException
   {
      executeTest(new ThreadSubmission()
      {
         @Override
         public void submitThreads(final ExecutorService executorService)
         {
            for (int i=0; i<THREAD_COUNT; i=i+2)
            {
               executorService.submit(new GlobalParametersModifierViaSet(String.valueOf(i)));
               executorService.submit(new GlobalParametersModifierViaGetOrInitialize(String.valueOf(i+1)));
            }
         }
      });
   }

   private void executeTest(final ThreadSubmission threadSubmission) throws InterruptedException
   {
      final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
      threadSubmission.submitThreads(executorService);
      executorService.shutdown();
      final boolean terminatedGracefully = executorService.awaitTermination(1, TimeUnit.MINUTES);
      if ( !terminatedGracefully)
      {
         Assert.fail("Timeout while waiting for executor service termination.");
      }

      final GlobalParameters globals = GlobalParameters.globals();
      for (int i=0; i<THREAD_COUNT; i++)
      {
         assertThat("Thread ID: " + i, (String) globals.get(String.valueOf(i)), equalTo(THREAD_ID_VALUE));
      }
   }

   private static interface ThreadSubmission
   {
      public void submitThreads(final ExecutorService executorService);
   }

   private static final class GlobalParametersModifierViaSet implements Callable<Void>
   {
      private final String id;

      public GlobalParametersModifierViaSet(final String id)
      {
         this.id = id;
      }

      @Override
      public Void call() throws InterruptedException
      {
         GlobalParameters.globals().set(id, THREAD_ID_VALUE);

         return null;
      }
   }

   private static final class GlobalParametersModifierViaGetOrInitialize implements Callable<Void>
   {
      private final String id;

      public GlobalParametersModifierViaGetOrInitialize(final String id)
      {
         this.id = id;
      }

      @Override
      public Void call() throws InterruptedException
      {
         GlobalParameters.globals().getOrInitialize(id, THREAD_ID_VALUE);

         return null;
      }
   }
}
