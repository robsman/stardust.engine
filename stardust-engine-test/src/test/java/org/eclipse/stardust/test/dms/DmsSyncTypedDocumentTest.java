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

import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_SYNC_MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
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
 * Tests synchronization of workflow document data having a document type assigned and jcr documents.
 * </p>
 *
 * @author Roland.Stamm
 * @version $Revision$
 */
public class DmsSyncTypedDocumentTest
{
   private static final String DOC_NAME = "DmsTypedSyncTest.txt";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DMS_SYNC_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Before
   public void setup()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();

      for (int i = 1; i < 6; i++ )
      {
         dms.removeDocument("/" + i + DOC_NAME);
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(i + DOC_NAME);
         docInfo.setContentType("text/plain");
         docInfo.setDescription("default description");
         
         docInfo.setProperty("myString", "metaDataString");
         
         dms.createDocument("/", docInfo, ("this is content no# ").getBytes(), null);
      }

      /**
       * TEST_USER needs Role1
       */
      UserService us = sf.getUserService();
      User motu = us.getUser();
      UserHome.addGrants(sf, motu, "Role1");
      us.modifyUser(motu);
   }

   /**
    * Update via dms should also update all document and documentList data references to
    * the document in the workflow.
    */
   @Test
   public void testUpdateDocumentViaDms()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      WorkflowService wfs = sf.getWorkflowService();

      Document doc = dms.getDocument("/1" + DOC_NAME);

      List<Document> docList = new ArrayList<Document>();
      docList.add(doc);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      dataMap.put("Document2", doc);
      dataMap.put("DocumentList2", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocsTyped", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      @SuppressWarnings("unchecked")
      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/1" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via dms
      dms.updateDocument(doc2, false, null, null, false);

      // assert dmsDocument data updated
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc2.getDescription(), wfDoc2.getDescription());

      // assert dmsDocumentList data updated
      @SuppressWarnings("unchecked")
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());

      wfs.suspend(ai.getOID(), null);
   }

   /**
    * Update via workflow should update the document in the jcr and all references to the
    * same document in workflow document or documentList data.
    */
   @Test
   public void testUpdateDocumentViaWorkflow()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      WorkflowService wfs = sf.getWorkflowService();

      Document doc = dms.getDocument("/2" + DOC_NAME);

      List<Document> docList = new ArrayList<Document>();
      docList.add(doc);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      dataMap.put("Document2", doc);
      dataMap.put("DocumentList2", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocsTyped", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      @SuppressWarnings("unchecked")
      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/2" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via workflow dmsDocument data
      wfs.suspend(ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document2", doc2)));

      // assert jcr doc updated
      Document dmsDoc2 = dms.getDocument("/2" + DOC_NAME);
      Assert.assertEquals(doc2.getDescription(), dmsDoc2.getDescription());

      // assert doc updated in dmsDocumentList data
      @SuppressWarnings("unchecked")
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());
   }

   /**
    * Removing the physical document should also remove all workflow document and
    * documentList data references to it.
    */
   @Test
   public void testDeleteDocumentViaDms()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      WorkflowService wfs = sf.getWorkflowService();

      Document doc = dms.getDocument("/3" + DOC_NAME);

      List<Document> docList = new ArrayList<Document>();
      docList.add(doc);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      dataMap.put("Document2", doc);
      dataMap.put("DocumentList2", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocsTyped", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      @SuppressWarnings("unchecked")
      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      // remove via dms
      dms.removeDocument("/3" + DOC_NAME);

      // assert dmsDocument data updated
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertNull(wfDoc2);

      // assert dmsDocumentList data updated
      @SuppressWarnings("unchecked")
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertNull(wfDocList2);

      wfs.suspend(ai.getOID(), null);
   }

   /**
    * Removing the document data in workflow should not delete the physical document and
    * it should not affect any other workflow data references to the same document.
    */
   @Test
   public void testRemoveDocumentInWorkflow()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      WorkflowService wfs = sf.getWorkflowService();

      Document doc = dms.getDocument("/4" + DOC_NAME);

      List<Document> docList = new ArrayList<Document>();
      docList.add(doc);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      dataMap.put("Document2", doc);
      dataMap.put("DocumentList2", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocsTyped", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      @SuppressWarnings("unchecked")
      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      // remove workflow dmsDocument data
      wfs.suspend(ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document2", null)));

      // assert doc in dmsDocument data does not exist anymore
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertNull(wfDoc2);

      // assert jcr doc exists
      Document dmsDoc2 = dms.getDocument("/4" + DOC_NAME);
      Assert.assertEquals(doc.getDescription(), dmsDoc2.getDescription());

      // assert doc in dmsDocumentList data exists
      @SuppressWarnings("unchecked")
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(doc.getDescription(), wfDocList2.get(0).getDescription());
   }

   /**
    * Tests updates only targeting a specific xPath
    */
   @Test
   public void testPartialUpdateDocumentViaWorkflow()
   {
      DocumentManagementService dms = sf.getDocumentManagementService();
      WorkflowService wfs = sf.getWorkflowService();

      Document doc = dms.getDocument("/5" + DOC_NAME);

      List<Document> docList = new ArrayList<Document>();
      docList.add(doc);

      Map<String, Object> dataMap = new HashMap<String, Object>();

      dataMap.put("Document2", doc);
      dataMap.put("DocumentList2", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocsTyped", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document2");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      @SuppressWarnings("unchecked")
      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/5" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via workflow dmsDocument data
      wfs.suspend(
            ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document2Description",
                  doc2.getDescription())));

      // assert jcr doc updated
      Document dmsDoc2 = dms.getDocument("/5" + DOC_NAME);
      Assert.assertEquals(doc2.getDescription(), dmsDoc2.getDescription());

      // assert doc updated in dmsDocumentList data
      @SuppressWarnings("unchecked")
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList2");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());
   }

}
