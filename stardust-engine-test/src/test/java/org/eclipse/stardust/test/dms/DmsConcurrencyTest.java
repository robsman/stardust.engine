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
package org.eclipse.stardust.test.dms;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests Concurrent access and transaction roll-back for DMS Documents.
 * </p>
 *
 * @author Roland.Stamm
 * @version $Revision: 66871 $
 */
public class DmsConcurrencyTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DmsModelConstants.DMS_CONCURRENT_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   /**
    *  Count of repeated operations.
    */
   private static final int COUNT = 1;

   /**
    * Count of parallel threads.
    */
   private static final int THREADS = 30;

   private CountDownLatch countDownLatch;

   private String initDocument(String docName, String owner, String contentType,
         Map<String, ? extends Serializable> metaData)
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

      dms.removeDocument("/" + docName);

      DocumentInfo doc = new DmsDocumentBean();
      byte[] content;

      // version 0 (unversioned)
      doc.setContentType(contentType);
      doc.setName(docName);
      doc.setDescription("testFile");
      doc.setOwner(owner);
      doc.setProperties(metaData);
      content = "this is content no# ".getBytes();
      Document v0 = dms.createDocument("/", doc, content, "");
      return v0.getId();
   }

   @Before
   public void setUp()
   {
      for (int i = 0; i < THREADS; i++ )
      {
         initDocument(i + "test.txt", "motu", "text/plain", null);
      }

      UserService userService = sf.getUserService();

      for (int i = 0; i < THREADS; i++ )
      {
         String u = "u" + i;
         User createdUser = userService.createUser(u, u, u, null, u, null, null, null);
         createdUser.addGrant(ModelParticipantInfo.ADMINISTRATOR);
         userService.modifyUser(createdUser);
      }
   }


   /**
    *  After execution all documents should be rolled back to *text.txt.
    */
   @Test
   public void testRollbackDmsProcess()
   {
      List<Runnable> runnables = new LinkedList<Runnable>();


      List<ServiceFactory> sfs = new ArrayList<ServiceFactory>();

      for (int i = 0; i < THREADS; i++ )
      {
         ServiceFactory sf = ServiceFactoryLocator.get("u" + i, "u" + i);
         sfs.add(sf);
         runnables.add(new StartProcess(Integer.valueOf(i).toString(), 1, sf));
      }

      launchThreads(runnables);

      for (ServiceFactory sf : sfs)
      {
         sf.close();
      }

      // assert all text files are still named *test.txt
      ServiceFactory sf = ServiceFactoryLocator.get("motu", "motu");
      DocumentManagementService dms = sf.getDocumentManagementService();
      for (int i = 0; i < THREADS; i++ )
      {
         Document document = dms.getDocument("/" + i + "test.txt");
         Assert.assertNotNull("/" + i + "test.txt should exist but was not found.", document);
      }
      sf.close();

   }

   /**
    * Create Document operation always works
    */
   @Test
   public void testCreateDocuments()
   {
      List<Runnable> runnables = new LinkedList<Runnable>();


      List<ServiceFactory> sfs = new ArrayList<ServiceFactory>();

      for (int i = 0; i < THREADS; i++ )
      {
         ServiceFactory sf = ServiceFactoryLocator.get("u" + i, "u" + i);
         sfs.add(sf);
         runnables.add(new CreateDocument(Integer.valueOf(i).toString(), COUNT, sf));
      }

      launchThreads(runnables);

      for (ServiceFactory sf : sfs)
      {
         sf.close();
      }

      // assert all text files are still named *test.txt
      ServiceFactory sf = ServiceFactoryLocator.get("motu", "motu");
      DocumentManagementService dms = sf.getDocumentManagementService();
      for (int i = 0; i < THREADS; i++ )
      {
         for (int j = 0; j < COUNT; j++ )
         {
            Document document = dms.getDocument("/Document" + i + " " + j + ".txt");
            Assert.assertNotNull("/Document" + i + " " + j + ".txt should exist!", document);
            Assert.assertEquals(RepositoryConstants.VERSION_UNVERSIONED, document.getRevisionId());
         }
      }
      sf.close();

   }

   /**
    *  Two versioning operations cause NPE on jackrabbit-2.2.5
    *  With jackrabbit-2.6.1 concurrent version operations fail often for a fresh repository
    *  but failure rate gets eliminated with repeated execution.
    *
    *  TODO Enable version tests if newer jackrabbit version is stable on concurrent versioning.
    */
//   @Test
   public void testCreateDocumentsTwoVersions()
   {
      List<Runnable> runnables = new LinkedList<Runnable>();


      List<ServiceFactory> sfs = new ArrayList<ServiceFactory>();

      for (int i = 0; i < THREADS; i++ )
      {
         ServiceFactory sf = ServiceFactoryLocator.get("u" + i, "u" + i);
         sfs.add(sf);
         runnables.add(new CreateDocumentVersion(Integer.valueOf(i).toString(), COUNT, sf));
      }

      launchThreads(runnables);

      for (ServiceFactory sf : sfs)
      {
         sf.close();
      }

      // assert all text files are still named *test.txt
      ServiceFactory sf = ServiceFactoryLocator.get("motu", "motu");
      DocumentManagementService dms = sf.getDocumentManagementService();
      for (int i = 0; i < THREADS; i++ )
      {
         for (int j = 0; j < COUNT; j++ )
         {
            Document document = dms.getDocument("/Version" + i + " " + j + ".txt");
            Assert.assertNotNull("Document should exist", document);
            List<Document> documentVersions = dms.getDocumentVersions(document.getId());
            Assert.assertNotNull("Document should be versioned", documentVersions);
            Assert.assertEquals("Document '" + document.getName() + "' should have 2 versions", 2,
                  documentVersions.size());
         }
      }
      sf.close();

   }

   private void launchThreads(List<Runnable> runnables)
   {
      countDownLatch = new CountDownLatch(runnables.size());
      List<Thread> threads = new LinkedList<Thread>();

      for (Runnable runnable : runnables)
      {
         threads.add(new Thread(runnable));
      }

      int i = 0;
      for (Thread thread : threads)
      {
         System.out.println("Starting Thread " + i++ );
         thread.start();
      }

      try
      {
         countDownLatch.await();
      }
      catch (InterruptedException e)
      {
         e.printStackTrace();
      }
      System.out.println("Threads Done.");

   }

   public class StartProcess extends AbstractEngineCommand
   {

      public StartProcess(String id, int count, ServiceFactory sf)
      {
         super(id, count, sf);
      }

      @Override
      protected void doWithEngine(int i)
      {
         PropertyLayer layer = null;
         try
         {
            // only works in local spring
            layer = ParametersFacade.pushLayer(Collections.singletonMap(
                  "Carnot.Engine.ErrorHandling.ApplicationExceptionPropagation", "always"));

            Map<String, Serializable> map = new HashMap<String, Serializable>();
            map.put("ExistingDocName", "/"+ id + "test.txt");
            map.put("NewDocName", id + "updated.txt");
            sf.getWorkflowService().startProcess("ProcessDefinition_1", map, true);
         }
         catch (Exception e)
         {
            System.out.println("Exception in Thread:" + id);
            e.printStackTrace();
            // item does not exist
         }
         finally
         {
            if (layer != null)
            {
               ParametersFacade.popLayer();
               layer = null;
            }
         }
      }
   }

   public class CreateDocument extends AbstractEngineCommand
   {
      public CreateDocument(String id, int count, ServiceFactory sf)
      {
         super(id, count, sf);
      }

      @Override
      protected void doWithEngine(int i)
      {
         DocumentManagementService dms = sf.getDocumentManagementService();
         DocumentInfo docInfo = DmsUtils.createDocumentInfo("Document" + id + " " + i
               + ".txt");
         docInfo.setContentType("text/plain");
         docInfo.setOwner(id);
         dms.createDocument("/", docInfo,
               ("this is content no# " + i).getBytes(), null);
      }
   }

   public class CreateDocumentVersion extends AbstractEngineCommand
      {
         public CreateDocumentVersion(String id, int count, ServiceFactory sf)
         {
            super(id, count, sf);
         }

         @Override
         protected void doWithEngine(int i)
         {
            DocumentManagementService dms = sf.getDocumentManagementService();
            DocumentInfo docInfo = DmsUtils.createDocumentInfo("Version" + id + " " + i
                  + ".txt");
            docInfo.setContentType("text/plain");
            docInfo.setOwner(id);
            Document createDocument = dms.createDocument("/", docInfo,
                  ("this is content no# " + i).getBytes(), null);

            dms.versionDocument(createDocument.getId(), "v0", "v0");

            dms.versionDocument(createDocument.getId(), "v1", "v1");
         }
      }

   public abstract class AbstractEngineCommand implements Runnable
   {
      protected String id;

      protected int count;

      protected ServiceFactory sf;

      public AbstractEngineCommand(String id, int count, ServiceFactory sf)
      {
         this.id = id;
         this.count = count;
         this.sf = sf;
      }

      public void run()
      {
         try
         {
            for (int i = 0; i < count; i++ )
            {
               doWithEngine(i);
               try
               {
                  Thread.sleep(1);
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
            }
         }
         finally
         {
            countDownLatch.countDown();
         }
      }

      protected abstract void doWithEngine(int i);
   }




}
