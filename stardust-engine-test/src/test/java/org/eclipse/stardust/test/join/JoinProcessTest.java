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
package org.eclipse.stardust.test.join;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.query.EvaluationPolicy;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO javadoc
 * </p>
 * 
 * @author Roland.Stamm
 * @version $Revision$
 */
public class JoinProcessTest
{
   private static final String MODEL_NAME = "JoinProcessModel";
   
   private static final String PROCESS_ATTACHMENTS = "PROCESS_ATTACHMENTS";
   
   private final ClientServiceFactory sf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(sf, MODEL_NAME);
   
   @ClassRule
   public static LocalJcrH2Test testSetup = new LocalJcrH2Test();
   
   @Rule
   public TestRule chain = RuleChain.outerRule(sf)
                                    .around(rtConfigurer);
   
   /**
    * Data doc1 is copied to target process because outDataPath exists and it is not initialized.
    */
   @Test
   public void testJoinProcessCopy()
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      Document doc1 = getCreateDocument("joinProcess.txt", sf);
      map.put("Doc1", doc1);

      ProcessInstance sourceProcess = wfs.startProcess("Source", map, true);

      ProcessInstance targetProcess = wfs.startProcess("TargetWithDoc1", null, true);

      ProcessInstance joinedTargetProcess = wfs.joinProcessInstance(
            sourceProcess.getOID(), targetProcess.getOID(), "join test");

      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(joinedTargetProcess.getOID());

      Document inDataValue = (Document) wfs.getInDataValue(ai.getOID(), null, "Doc1");
      Assert.assertEquals(doc1.getId(), inDataValue.getId());

      ProcessInstanceQuery query = ProcessInstanceQuery.findLinked(joinedTargetProcess.getOID(), LinkDirection.TO, PredefinedProcessInstanceLinkTypes.JOIN);

      query.setPolicy(getWithLinkPolicy());

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(query);
      Assert.assertEquals(1, allProcessInstances.getSize());
   }

   /**
    * Primitive1 is not copied, doc1 data gets attached to process attachments.
    */
   @Test
   public void testJoinProcessToProcessAttachments()
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("Primitive1", "default");
      Document doc1 = getCreateDocument("joinProcess.txt", sf);
      map.put("Doc1", doc1);

      ProcessInstance sourceProcess = wfs.startProcess("Source", map, true);

      ProcessInstance targetProcess = wfs.startProcess("Target", null, true);

      ProcessInstance joinedTargetProcess = wfs.joinProcessInstance(
            sourceProcess.getOID(), targetProcess.getOID(), "join test");

      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(joinedTargetProcess.getOID());

      Assert.assertEquals("", wfs.getInDataValue(ai.getOID(), null, "Primitive1"));

      validateExistsInProcessAttachments(doc1, sf, joinedTargetProcess.getOID());
   }

   /**
    * Data doc1 is already initialized in target process. No overwrite.
    */
   @Test
   public void testJoinProcessNoOverwrite()
   {
      WorkflowService wfs = sf.getWorkflowService();

      Map<String, Serializable> map = new HashMap<String, Serializable>();
      Document doc1 = getCreateDocument("joinProcess.txt", sf);
      map.put("Doc1", doc1);

      ProcessInstance sourceProcess = wfs.startProcess("Source", map, true);

      Map<String, Serializable> map2 = new HashMap<String, Serializable>();
      Document doc2 = getCreateDocument("joinProcess2.txt", sf);
      map2.put("Doc1", doc2);

      ProcessInstance targetProcess = wfs.startProcess("TargetWithDoc1", map2, true);

      ProcessInstance joinedTargetProcess = wfs.joinProcessInstance(
            sourceProcess.getOID(), targetProcess.getOID(), "join test");

      ActivityInstance ai = wfs.activateNextActivityInstanceForProcessInstance(joinedTargetProcess.getOID());

      Document inDataValue = (Document) wfs.getInDataValue(ai.getOID(), null, "Doc1");
      Assert.assertEquals(doc2.getId(), inDataValue.getId());
   }

   /**
    * Merge to target process attachments
    */
   @Test
   public void testJoinProcessMergeProcessAttachments()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance sourceProcess = wfs.startProcess("Source", null, true);

      ProcessInstance targetProcess = wfs.startProcess("Target", null, true);

      Document doc1 = getCreateDocument("dupl_att_doc1.txt", sf);
      Document doc2 = getCreateDocument("dupl_att_doc2.txt", sf);

      attachToProcess(doc1, sourceProcess.getOID());
      attachToProcess(doc2, sourceProcess.getOID());

      attachToProcess(doc1, targetProcess.getOID());

      ProcessInstance joinedTargetProcess = wfs.joinProcessInstance(
            sourceProcess.getOID(), targetProcess.getOID(), "join test");

      validateExistsInProcessAttachments(doc1, sf, sourceProcess.getOID());
      validateExistsInProcessAttachments(doc2, sf, sourceProcess.getOID());

      validateExistsInProcessAttachments(doc1, sf, joinedTargetProcess.getOID());
      validateExistsInProcessAttachments(doc2, sf, joinedTargetProcess.getOID());
   }

   /**
    * Target process attachments empty.
    */
   @Test
   public void testJoinProcessMergeProcessAttachments2()
   {
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance sourceProcess = wfs.startProcess("Source", null, true);

      ProcessInstance targetProcess = wfs.startProcess("Target", null, true);

      Document doc2 = getCreateDocument("dupl_att_doc2.txt", sf);

      attachToProcess(doc2, sourceProcess.getOID());

      ProcessInstance joinedTargetProcess = wfs.joinProcessInstance(
            sourceProcess.getOID(), targetProcess.getOID(), "join test");

      validateExistsInProcessAttachments(doc2, sf, sourceProcess.getOID());
      validateExistsInProcessAttachments(doc2, sf, joinedTargetProcess.getOID());

   }

   private void validateExistsInProcessAttachments(Document doc, ServiceFactory sf, long oid)
   {
      Object pAttachment = sf.getWorkflowService()
            .getInDataPath(oid, PROCESS_ATTACHMENTS);

      if (pAttachment == null)
      {
         Assert.fail("Document not found. ProcessAttachments == null");
         return;
      }

      if (pAttachment instanceof Collection)
      {
         Collection<?> pAttCol = (Collection<?>) pAttachment;

         for (Object obj : pAttCol)
         {
            if (obj instanceof Document)
            {
               if (((Document) obj).getId().equals(doc.getId()))
               {
                  /* found document */
                  return;
               }
            }
         }
      }
      Assert.fail("Document not found in attachments. " + doc.getId());
   }

   private void attachToProcess(Document doc, long oid)
   {
      WorkflowService wfs = sf.getWorkflowService();

      Object processAttachments = wfs.getInDataPath(oid, PROCESS_ATTACHMENTS);

      if (processAttachments == null)
      {
         processAttachments = CollectionUtils.newArrayList();
      }

      if (processAttachments instanceof Collection)
      {
         @SuppressWarnings("unchecked")
         Collection<? super Serializable> processAttachmentsCollection = (Collection<? super Serializable>) processAttachments;

         Document oldDoc = getDocumentById(processAttachmentsCollection, doc);
         if (null != oldDoc)
         {
            processAttachmentsCollection.remove(oldDoc);
         }
         processAttachmentsCollection.add(doc);
      }

      wfs.setOutDataPath(oid, PROCESS_ATTACHMENTS, processAttachments);

   }

   private Document getDocumentById(Collection<?> processAttachments, Document v0)
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

   private EvaluationPolicy getWithLinkPolicy()
   {
      ProcessInstanceDetailsPolicy result = new ProcessInstanceDetailsPolicy(ProcessInstanceDetailsLevel.Default);
      result.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
      return result;
   }
   
   private static Document getCreateDocument(String docName, ServiceFactory sf)
   {
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(docName);
      docInfo.setContentType("text/plain");
      DocumentManagementService dms = sf.getDocumentManagementService();

      dms.removeDocument("/" + docName);
      return dms.createDocument("/", docInfo);
   }
}
