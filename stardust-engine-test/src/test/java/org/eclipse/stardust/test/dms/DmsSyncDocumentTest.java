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

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class DmsSyncDocumentTest
{
   private static final String MODEL_NAME = "DmsSyncTest";

   private static final String DOC_NAME = "DmsSyncTest.txt";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   @Before
   public void setup()
   {
      // DocumentTypes need to be present for every test run. They get deleted if model is deployed in LocalJcrH2TestSetup
      RtEnvHome.deploy(sf.getAdministrationService(), MODEL_NAME);
      
      DocumentManagementService dms = sf.getDocumentManagementService();

      for (int i = 1; i < 6; i++ )
      {
         dms.removeDocument("/" + i + DOC_NAME);
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(i + DOC_NAME);
         docInfo.setContentType("text/plain");
         docInfo.setDescription("default description");

         docInfo.setProperty("someDefaultMetaDataString", "default");

         dms.createDocument("/", docInfo, ("this is content no# ").getBytes(), null);
      }

      /**
       * TEST_USER needs Role1
       */
      UserService us = sf.getUserService();
      User motu = us.getUser();
      motu.addGrant("Role1");
      us.modifyUser(motu);
   }

   /**
    * Update via dms should also update all document and documentList data references to
    * the document in the workflow.
    * ( with document security AccessDenied )
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

      dataMap.put("Document1", doc);
      dataMap.put("DocumentList1", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocs", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/1" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via dms
      dms.updateDocument(doc2, false, null, null, false);

      // assert dmsDocument data updated
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc2.getDescription(), wfDoc2.getDescription());

      // assert dmsDocumentList data updated
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());

      wfs.suspend(ai.getOID(), null);
   }

   /**
    * Update via workflow should update the document in the jcr and all references to the
    * same document in workflow document or documentList data.
    * ( with document security AccessDenied )
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

      dataMap.put("Document1", doc);
      dataMap.put("DocumentList1", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocs", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/2" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via workflow dmsDocument data
      wfs.suspend(ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document1", doc2)));

      // assert jcr doc updated
      Document dmsDoc2 = dms.getDocument("/2" + DOC_NAME);
      Assert.assertEquals(doc2.getDescription(), dmsDoc2.getDescription());

      // assert doc updated in dmsDocumentList data
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());
   }

   /**
    * Removing the physical document should also remove all workflow document and
    * documentList data references to it.
    * ( with document security AccessDenied )
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

      dataMap.put("Document1", doc);
      dataMap.put("DocumentList1", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocs", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      // remove via dms
      dms.removeDocument("/3" + DOC_NAME);

      // assert dmsDocument data updated
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertNull(wfDoc2);

      // assert dmsDocumentList data updated
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertNull(wfDocList2);

      wfs.suspend(ai.getOID(), null);
   }

   /**
    * Removing the document data in workflow should not delete the physical document and
    * it should not affect any other workflow data references to the same document.
    * ( with document security: works because only removing a reference in the workflow )
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

      dataMap.put("Document1", doc);
      dataMap.put("DocumentList1", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocs", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      // remove workflow dmsDocument data
      wfs.suspend(ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document1", null)));

      // assert doc in dmsDocument data does not exist anymore
      Document wfDoc2 = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertNull(wfDoc2);

      // assert jcr doc exists
      Document dmsDoc2 = dms.getDocument("/4" + DOC_NAME);
      Assert.assertEquals(doc.getDescription(), dmsDoc2.getDescription());

      // assert doc in dmsDocumentList data exists
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(doc.getDescription(), wfDocList2.get(0).getDescription());
   }

   /**
    * Tests updates only targeting a specific xPath
    * ( with document security AccessDenied )
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

      dataMap.put("Document1", doc);
      dataMap.put("DocumentList1", docList);

      ProcessInstance pi = wfs.startProcess("{DmsSyncTest}AccessDocs", dataMap, true);
      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(pi.getOID());

      Document wfDoc = (Document) wfs.getInDataValue(ai.getOID(), null, "Document1");
      Assert.assertEquals(doc.getDescription(), wfDoc.getDescription());

      List<Document> wfDocList = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(docList.get(0).getDescription(), wfDocList.get(0)
            .getDescription());

      Document doc2 = dms.getDocument("/5" + DOC_NAME);
      doc2.setDescription("newDescr");
      doc2.setContentType("xml/plain");

      // update via workflow dmsDocument data
      wfs.suspend(
            ai.getOID(),
            new ContextData(null, Collections.singletonMap("Document1Description",
                  doc2.getDescription())));

      // assert jcr doc updated
      Document dmsDoc2 = dms.getDocument("/5" + DOC_NAME);
      Assert.assertEquals(doc2.getDescription(), dmsDoc2.getDescription());

      // assert doc updated in dmsDocumentList data
      List<Document> wfDocList2 = (List<Document>) wfs.getInDataValue(ai.getOID(), null,
            "DocumentList1");
      Assert.assertEquals(doc2.getDescription(), wfDocList2.get(0).getDescription());
   }

}
