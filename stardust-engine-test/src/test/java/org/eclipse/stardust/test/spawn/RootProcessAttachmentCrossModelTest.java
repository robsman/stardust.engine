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
package org.eclipse.stardust.test.spawn;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class tests for Process Attachments having the "unique per root process instance"
 * flag enabled for subprocesses that are invoked in a different model.
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RootProcessAttachmentCrossModelTest
{
   public static final String MODEL_NAME1 = "RootPA_CapitalDisabilityClaim";

   public static final String MODEL_NAME2 = "RootPA_DisabilityEvent";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME1,
         MODEL_NAME2);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Test
   public void testUniquePerRootWithRootAttachment() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{DisabilityEvent}DisabilityEvent", null,
            true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi
            .getOID());

      // attach new document to root process
      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      // assert in root process
      assertDocumentsExist(pi.getOID(), 1);

      // complete to invoke subprocess
      wfs.complete(ai.getOID(), null, null);
      ActivityInstance aiSubProcess = wfs
            .activateNextActivityInstanceForProcessInstance(pi.getOID());

      // attach new document to subprocess
      attachToProcess(getDoc(sf, aiSubProcess.getProcessInstance()), sf,
            aiSubProcess.getProcessInstanceOID());

      // assert in subprocess
      assertDocumentsExist(aiSubProcess.getProcessInstanceOID(), 2);
      // assert in rootprocess
      assertDocumentsExist(pi.getOID(), 2);

      // complete to sub process activity 2
      wfs.activateAndComplete(aiSubProcess.getOID(), null, null);
      ActivityInstance aiSubProcess2 = wfs.activateNextActivityInstance(aiSubProcess
            .getOID());
      // complete to return to main process
      wfs.complete(aiSubProcess2.getOID(), null, null);
      ActivityInstance aiMainProcess = wfs
            .activateNextActivityInstanceForProcessInstance(pi.getOID());

      // assert in main process
      assertDocumentsExist(aiMainProcess.getProcessInstanceOID(), 2);

      // test attachment removal
      wfs.setOutDataPath(pi.getOID(), "PROCESS_ATTACHMENTS", null);
      assertDocumentsExist(aiMainProcess.getProcessInstanceOID(), 0);
   }

   @Test
   public void testUniquePerRootWithoutRootAttachment() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.startProcess("{DisabilityEvent}DisabilityEvent", null,
            true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi
            .getOID());

      // complete to invoke subprocess
      wfs.complete(ai.getOID(), null, null);
      ActivityInstance ai2 = wfs.activateNextActivityInstanceForProcessInstance(pi
            .getOID());

      // attach new document to subprocess
      attachToProcess(getDoc(sf, ai2.getProcessInstance()), sf,
            ai2.getProcessInstanceOID());

      // assert in rootprocess
      assertDocumentsExist(pi.getOID(), 1);
      // assert in subprocess
      assertDocumentsExist(ai2.getProcessInstanceOID(), 1);

      // test attachment removal
      wfs.setOutDataPath(pi.getOID(), "PROCESS_ATTACHMENTS", null);
      assertDocumentsExist(pi.getOID(), 0);
   }

   @SuppressWarnings("unchecked")
   public void assertDocumentsExist(long piOid, int count)
   {
      Object inDataPath = sf.getWorkflowService().getInDataPath(piOid,
            "PROCESS_ATTACHMENTS");

      if (count > 0)
      {
         Assert.assertNotNull("PROCESS_ATTACHMENTS must be not null", inDataPath);
      }
      else
      {
         if (inDataPath == null)
         {
            return;
         }
         if (inDataPath instanceof List)
         {
            Assert.assertTrue(((List<?>) inDataPath).isEmpty());
            return;
         }
      }
      if (inDataPath instanceof List)
      {
         Assert.assertEquals(count + " documents expected", count,
               ((List<Document>) inDataPath).size());
         for (int i = 0; i < count; i++)
         {
            Document doc = ((List<Document>) inDataPath).get(i);
            Assert.assertNotNull("Doc should exist.", doc);
            Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt",
                  doc.getName());
         }
      }
      else
      {
         Assert.fail("Should be List<Document> but is: " + inDataPath);
      }
   }

   private void attachToProcess(Document doc, ServiceFactory sf, long oid)
   {
      WorkflowService wfs = sf.getWorkflowService();

      Object processAttachments = wfs.getInDataPath(oid, "PROCESS_ATTACHMENTS");

      if (processAttachments == null)
      {
         processAttachments = new ArrayList<Document>();
      }

      if (processAttachments instanceof Collection)
      {
         @SuppressWarnings("unchecked")
         Collection<Document> processAttachmentsCollection = (Collection<Document>) processAttachments;

         Document oldDoc = getDocumentById(processAttachmentsCollection, doc);
         if (null != oldDoc)
         {
            processAttachmentsCollection.remove(oldDoc);
         }
         processAttachmentsCollection.add(doc);
      }

      wfs.setOutDataPath(oid, "PROCESS_ATTACHMENTS", processAttachments);
   }

   private Document getDocumentById(Collection<Document> processAttachments, Document v0)
   {
      for (Object doc : processAttachments)
      {
         if (doc instanceof Document)
         {
            if (((Document) doc).getId().equals(v0.getId()))
            {
               return (Document) doc;
            }
         }
      }
      return null;
   }

   private Document getDoc(ServiceFactory sf, ProcessInstance pi)
   {
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      String s = defaultPath + "/process-attachments";

      DocumentManagementService dms = sf.getDocumentManagementService();
      DmsUtils.ensureFolderHierarchyExists(s, dms);

      Document document = dms.getDocument(s + "/spawnTest.txt");

      if (document == null)
      {
         DocumentInfo docInfo = new DmsDocumentBean();

         docInfo.setName("spawnTest.txt");
         docInfo.setContentType("text/plain");
         Map<String, String> map = newHashMap();
         map.put("myString", "myMetaDataString");
         docInfo.setProperties(map);
         document = dms.createDocument(s, docInfo);
      }

      return document;
   }
}
