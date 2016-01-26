/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.AttributeOrder;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Tests WorkflowService.getWorklist(WorklistQuery query); is @TransientState.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class CreateMultiThreadedDescriptorDataValueTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private static final String MODEL_NAME = "DescriptorDataValueTestModel";
   private static final String PD_1_ID = "Process";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf);

   private CountDownLatch countDownLatch;
   public WorkflowService workflowService;

   @Before
   public void setUp()
   {
      workflowService = adminSf.getWorkflowService();
   }

   /**
    * <p>
    * Tests in multi threading all descriptor data values are written correct.</code>.
    * </p>
    */
   @Test
   public void testMultiThreadedDescriptorDataValueTest() throws Exception
   {
      Date start = new Date();
      
      for(int i = 0; i < 700; i++)
      {
         workflowService.startProcess(PD_1_ID, null, true);
      }
            
      List<Runnable> runnables = new LinkedList<Runnable>();

      for (int i = 0; i < 7; i++ )
      {
         runnables.add(new TestRunnable(i));
      }

      launchThreads(runnables);
      
      try
      {
         Thread.sleep(10000);
      }
      catch (Exception e)
      {
      }
      
      testDataValue();
      
      Date end = new Date();      
      long nmb = end.getTime() - start.getTime();
   }
   
   private void testDataValue() throws SQLException
   {
      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);
      Connection connection = session.getConnection();
      
      Statement selectStmt = connection.createStatement();
      String selectString = "SELECT count(*) FROM data_value WHERE string_value = ''";
      ResultSet result = selectStmt.executeQuery(selectString);
      
      if (result.next())
      {
         int number = result.getInt(1);
         Assert.assertEquals("Should be 0 empty data value.", 0, number);
      }
      else
      {
         Assert.fail("No data value found!");
      }
      
      selectString = "SELECT count(*) FROM data_value WHERE string_value = 'TEST'";
      result = selectStmt.executeQuery(selectString);
      
      if (result.next())
      {
         int number = result.getInt(1);
         Assert.assertEquals("Should be 700 data value 'TEST'.", 700, number);
      }
      else
      {
         Assert.fail("No data value found!");
      }
   }
      
   private void launchThreads(List<Runnable> runnables)
   {
      countDownLatch = new CountDownLatch(runnables.size());
      List<Thread> threads = new LinkedList<Thread>();

      for (Runnable runnable : runnables)
      {
         threads.add(new Thread(runnable));
      }

      for (Thread thread : threads)
      {
         thread.start();
      }

      try
      {
         countDownLatch.await(20, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
      }
   }
   
   class TestRunnable implements Runnable
   {
      private int counter = 5;
      
      private static final String IPP_REALM_NAME = "carnot";
      private static final String IPP_DOMAIN_NAME = "default";
      private static final String IPP_PARTITION_NAME = "default";
      private WorkflowService workflowService;
      
      public TestRunnable(int i)
      {
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(SecurityProperties.CRED_PARTITION, IPP_PARTITION_NAME);
         properties.put(SecurityProperties.CRED_DOMAIN, IPP_DOMAIN_NAME);
         properties.put(SecurityProperties.CRED_REALM, IPP_REALM_NAME);
         
         ServiceFactory factory = ServiceFactoryLocator.get(MOTU, MOTU, properties);
         workflowService = factory.getWorkflowService();         
      }

      public void run()
      {
         while (true) 
         {
         
            WorklistQuery query = WorklistQuery.findCompleteWorklist();
            query.getFilter().add(ActivityFilter.forAnyProcess("ACT1"));
            query.getOrderCriteria().and(
                  new AttributeOrder(WorklistQuery.PROCESS_INSTANCE_OID));
   
            ActivityInstance ai = null;
            
            try
            {
               ai = workflowService.activateNextActivityInstance(query);
            }
            catch (Exception e1)
            {
               try
               {
                  Thread.sleep(300);
               }
               catch (InterruptedException e)
               {
               }
            }
                     
            if (ai != null) 
            {
               try 
               {
                  Map<String, Object> wfData = new HashMap<String, Object>();
                  wfData.put("PrimitiveData1", "TEST");
                  workflowService.complete(ai.getOID(), "default", wfData);
   
               } catch (ConcurrencyException concEx) 
               {
                  try
                  {
                     Thread.sleep(300);
                  }
                  catch (InterruptedException e)
                  {
                  }
               } catch(Exception ex)
               {
                  try
                  {
                     Thread.sleep(300);
                  }
                  catch (InterruptedException e)
                  {
                  }
               }            
            }
            else
            {
               counter--;
               if(counter <= 0)
               {
                  break;                  
               }
               try
               {
                  Thread.sleep(300);
               }
               catch (InterruptedException e)
               {
               }               
            }    
         }
      }      
   }
}