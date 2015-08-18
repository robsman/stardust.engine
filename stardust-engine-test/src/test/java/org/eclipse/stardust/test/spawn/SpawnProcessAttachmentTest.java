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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class contains tests for the <i>Spawn Process</i> functionality,
 * which allows for ad hoc spawning of process instances (refer to the Stardust documentation
 * for details about <i>Spawn Process</i>).
 * </p>
 *
 * @author Roland.Stamm
 */
public class SpawnProcessAttachmentTest
{
   public static final String MODEL_NAME = "BabelTest";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @SuppressWarnings("unchecked")
   @Test
   public void testUniquePerRootWithRootAttachment() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
 
      ProcessInstance pi = wfs.startProcess("BabelTestProc", null, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());
      attachToProcess(getDoc(sf, ai.getProcessInstance()), sf, ai.getProcessInstanceOID());

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(ai.getProcessInstanceOID(), "SpawnedSubProc", true, null);
      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());
      attachToProcess(getDoc(sf, spawnSubprocessInstance), sf, spawnSubprocessInstance.getOID());
      
      Object inDataPath = wfs.getInDataPath(pi.getOID(), "PROCESS_ATTACHMENTS");
      Assert.assertNotNull("PROCESS_ATTACHMENTS must be not null", inDataPath); 
      if (inDataPath instanceof List)
      {
         Assert.assertEquals("2 documents expected", 2, ((List<Document>) inDataPath).size());         
         Document doc = ((List<Document>) inDataPath).get(0);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());
         doc = ((List<Document>) inDataPath).get(1);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());         
      }
      else
      {
         Assert.fail("Should be List<Document> but is: "+ inDataPath);
      }
            
      Object inDataPathSubprocessInstance = wfs.getInDataPath(spawnSubprocessInstance.getOID(), "PROCESS_ATTACHMENTS");
      Assert.assertNotNull("PROCESS_ATTACHMENTS must be not null", inDataPathSubprocessInstance); 
      if (inDataPath instanceof List)
      {
         Assert.assertEquals("2 documents expected", 2, ((List<Document>) inDataPathSubprocessInstance).size());         
         Document doc = ((List<Document>) inDataPathSubprocessInstance).get(0);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());
         doc = ((List<Document>) inDataPathSubprocessInstance).get(1);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());         
      }
      else
      {
         Assert.fail("Should be List<Document> but is: "+ inDataPathSubprocessInstance);
      }
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testUniquePerRootWithoutRootAttachment() throws Exception
   {
      WorkflowService wfs = sf.getWorkflowService();
 
      ProcessInstance pi = wfs.startProcess("BabelTestProc", null, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      // Spawn process
      ProcessInstance spawnSubprocessInstance = wfs.spawnSubprocessInstance(ai.getProcessInstanceOID(), "SpawnedSubProc", true, null);
      ActivityInstanceStateBarrier.instance().awaitAlive(spawnSubprocessInstance.getOID());
      attachToProcess(getDoc(sf, spawnSubprocessInstance), sf, spawnSubprocessInstance.getOID());
      
      Object inDataPath = wfs.getInDataPath(pi.getOID(), "PROCESS_ATTACHMENTS");
      Assert.assertNotNull("PROCESS_ATTACHMENTS must be not null", inDataPath); 
      if (inDataPath instanceof List)
      {
         Assert.assertEquals("1 documents expected", 1, ((List<Document>) inDataPath).size());         
         Document doc = ((List<Document>) inDataPath).get(0);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());
      }
      else
      {
         Assert.fail("Should be List<Document> but is: "+ inDataPath);
      }
            
      Object inDataPathSubprocessInstance = wfs.getInDataPath(spawnSubprocessInstance.getOID(), "PROCESS_ATTACHMENTS");
      Assert.assertNotNull("PROCESS_ATTACHMENTS must be not null", inDataPathSubprocessInstance); 
      if (inDataPath instanceof List)
      {
         Assert.assertEquals("1 documents expected", 1, ((List<Document>) inDataPathSubprocessInstance).size());         
         Document doc = ((List<Document>) inDataPathSubprocessInstance).get(0);
         Assert.assertNotNull("Doc should exist." ,doc);
         Assert.assertEquals("spawnTest.txt should exist", "spawnTest.txt", doc.getName());
      }
      else
      {
         Assert.fail("Should be List<Document> but is: "+ inDataPathSubprocessInstance);
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