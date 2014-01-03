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
package org.eclipse.stardust.test.spi;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.FastCachingSequenceGenerator;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.SequenceGenerator;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * TODO javadoc
 *
 * @author Stephan.Born
 * @version $Revision$
 */
public class FastCachingSequenceGeneratorTest
{
   private static final String SEQUENCE_BATCH_SIZE_STRING = "5";
   private static final Long SEQUENCE_BATCH_SIZE = Long.valueOf(SEQUENCE_BATCH_SIZE_STRING);
   private static final String ALL_USERS_WILDCARD_PROPERTY_VALUE = "*";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);


   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   private static TypeDescriptor aiDescr;
   private static TypeDescriptor piDescr;
   private static TypeDescriptor userDescr;

   private TestSession session;

   static
   {
      Logger.getRootLogger().setLevel(Level.WARN);
   }

   @BeforeClass
   public static void setUpOnce() throws SQLException
   {
      incrementDbSequenceSize();

      aiDescr = TypeDescriptor.get(ActivityInstanceBean.class);
      piDescr = TypeDescriptor.get(ProcessInstanceBean.class);
      userDescr = TypeDescriptor.get(UserBean.class);
   }

   @AfterClass
   public static void tearDownOnce()
   {
   }

   @Before
   public void setUp()
   {
      final GlobalParameters params = GlobalParameters.globals();
      params.set(SessionManager.PRP_SESSION_NO_TRACKING, ALL_USERS_WILDCARD_PROPERTY_VALUE);
      params.set(SecurityProperties.LOGIN_USERS_WITHOUT_LOGIN_LOGGING, ALL_USERS_WILDCARD_PROPERTY_VALUE);

      session = new TestSession();
   }

   @After
   public void tearDown()
   {
      // flushes cached extension provider instances used by ExtensionProviderUtils
      Parameters.instance().flush();
   }

   @Test
   public void testZeroSizeCache() throws Exception
   {
      FastCachingSequenceGenerator sequenceGenerator = getFastCachingSequenceGenerator(0L);
      try
      {
         sequenceGenerator.getNextSequence(
               TypeDescriptor.get(ActivityInstanceBean.class), session);
      }
      catch (Assert x)
      {
         return;
      }

      Assert.lineNeverReached("Expected Assertion on empty cache.");
   }

   @Test
   public void testDefaultSizeCacheContinuousValues() throws Exception
   {
      testDefaultSizeCache_X_TimesContinuousValues(1);
   }

   @Test
   public void testDefaultSizeCacheTwoTimesContinuousValues() throws Exception
   {
      testDefaultSizeCache_X_TimesContinuousValues(2);
   }

   @Test
   public void testDefaultSizeCacheHundredTimesContinuousValues() throws Exception
   {
      testDefaultSizeCache_X_TimesContinuousValues(100);
   }

   @Test
   public void testDefaultSizeCacheHundredTimesContinuousValuesForThreeTypes() throws Exception
   {
      FastCachingSequenceGenerator sequenceGenerator = getFastCachingSequenceGenerator(SEQUENCE_BATCH_SIZE);

      for (TypeDescriptor descr : new TypeDescriptor[]{aiDescr, piDescr, userDescr})
      {
         long lastValue = -1;

         for (long x = 1; x <= SEQUENCE_BATCH_SIZE * 100; x++)
         {
            long nextValue = sequenceGenerator.getNextSequence(descr, session);

            if (lastValue != -1)
            {
               Assert.condition(nextValue == (lastValue + 1), "Expected " + (lastValue + 1)
                     + " but got " + nextValue);
            }

            lastValue = nextValue;
         }
      }
   }

   @Test
   public void testDefaultSizeCacheHundredTimesContinuousValuesForThreeTypesMultiThreaded() throws Exception
   {
      final FastCachingSequenceGenerator sequenceGenerator = getFastCachingSequenceGenerator(SEQUENCE_BATCH_SIZE);

      List<Thread> threads = CollectionUtils.newArrayList();

      int threadNum = 100;
      TypeDescriptor[] typeDescriptors = new TypeDescriptor[] { aiDescr, piDescr, userDescr };

      for (int x = 0; x < threadNum; x++)
      {
         final TypeDescriptor descr = typeDescriptors[x % 3];

         Thread thread = new Thread(new Runnable()
         {
            TestSession session = new TestSession();

            public void run()
            {
               long lastValue = -1;

               for (long x = 1; x <= SEQUENCE_BATCH_SIZE * 100; x++)
               {
                  long nextValue = sequenceGenerator.getNextSequence(descr, session);

                  if (lastValue != -1)
                  {
                     Assert.condition(nextValue > lastValue,
                           "Expected something bigger than" + lastValue + " but got "
                                 + nextValue);
                  }

                  lastValue = nextValue;
               }
            };
         });
         threads.add(thread);

         thread.start();
      }

      // wait for _all_ threads to complete
      for (Thread thread : threads)
      {
         thread.join();
      }
   }

   private void testDefaultSizeCache_X_TimesContinuousValues(int factor)
   {
      FastCachingSequenceGenerator sequenceGenerator = getFastCachingSequenceGenerator(SEQUENCE_BATCH_SIZE);

      Set<Long> idSet = CollectionUtils.newHashSet();

      for (long x = 1; x <= SEQUENCE_BATCH_SIZE * factor; x++)
      {
         idSet.add(sequenceGenerator.getNextSequence(aiDescr, session));
      }

      Assert.condition(idSet.size() == SEQUENCE_BATCH_SIZE * factor, "Expected "
            + SEQUENCE_BATCH_SIZE + " different values but got " + idSet.size());

      Long minValue = Collections.min(idSet);
      Long maxValue = Collections.max(idSet);
      long diff = maxValue - minValue;
      Assert.condition(diff + 1 == SEQUENCE_BATCH_SIZE * factor,
            "Expected continuous values but got range of " + (diff + 1));
   }

   private static void incrementDbSequenceSize() throws SQLException
   {
      final Connection connection = testClassSetup.dataSource().getConnection();
      final Statement stmt = connection.createStatement();

      stmt.addBatch("ALTER SEQUENCE activity_instance_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE user_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE process_instance_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);

      stmt.executeBatch();
   }

   private static FastCachingSequenceGenerator getFastCachingSequenceGenerator(Long cacheSize)
   {
      final GlobalParameters params = GlobalParameters.globals();

      if (cacheSize == null)
      {
         cacheSize = SEQUENCE_BATCH_SIZE;
      }

      params.set(KernelTweakingProperties.SEQUENCE_BATCH_SIZE, cacheSize);
      final FastCachingSequenceGenerator sequenceGenerator = new FastCachingSequenceGenerator();
      final DBDescriptor dbDescriptor = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      final SqlUtils sqlUtils = new SqlUtils((String) params.get(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX), dbDescriptor);
      sequenceGenerator.init(dbDescriptor, sqlUtils);

      params.set(SequenceGenerator.UNIQUE_GENERATOR_PARAMETERS_KEY, sequenceGenerator);

      return sequenceGenerator;
   }

   private static class TestSession extends Session
   {
      private Connection conn;

      public TestSession()
      {
         super(SessionProperties.DS_NAME_AUDIT_TRAIL);
      }

      @Override
      public Connection getConnection() throws SQLException
      {
         if (conn == null)
         {
            conn = testClassSetup.dataSource().getConnection();
         }

         return conn;
      }

      @Override
      public boolean isUsingPreparedStatements(@SuppressWarnings("rawtypes") Class persistentClass)
      {
         return true;
      }
   }

}
