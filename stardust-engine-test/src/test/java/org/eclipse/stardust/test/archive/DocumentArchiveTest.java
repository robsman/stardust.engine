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
package org.eclipse.stardust.test.archive;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import org.junit.*;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.persistence.archive.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Note;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.TestTimestampProvider;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/*
 * 
 */
public class DocumentArchiveTest  
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS,
         ArchiveModelConstants.MODEL_ID, ArchiveModelConstants.MODEL_ID_OTHER);

   private static TestTimestampProvider testTimestampProvider = new TestTimestampProvider();

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @BeforeClass
   public static void clearManagers()
   {
      ArchiveManagerFactory.resetArchiveManagers();
   }
   
   @Before 
   public void init() throws Exception
   {
      setUp();
      ArchiveTest.deletePreferences();
      int id = ((BigDecimal)ArchiveTest.getEntryInDbForObject("PARTITION", "id", "default", "oid")).intValue();
      ArchiveTest.createPreference(id, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      ArchiveTest.createPreference(id, ArchiveManagerFactory.CARNOT_ARCHIVE_READER_MANAGER_TYPE,
            ArchiveManagerFactory.ArchiveManagerType.CUSTOM.name());
      ArchiveTest.createPreference(id,  ArchiveManagerFactory.CARNOT_ARCHIVE_READER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveReader");
      ArchiveTest.createPreference(id,  ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_CUSTOM,
            "org.eclipse.stardust.test.archive.MemoryArchiveWriter");
      ArchiveTest.createPreference(id, ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE,
            "false");      
   }
   
   public void setUp() throws Exception
   { 
      testTimestampProvider = new TestTimestampProvider();
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE,
            testTimestampProvider);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            3);

   }

   @After
   public void tearDown() throws Exception
   {
      ArchiveTest.clearArchiveManager("default");
      GlobalParameters.globals().set(
            TimestampProviderUtils.PROP_TIMESTAMP_PROVIDER_CACHED_INSTANCE, null);
      GlobalParameters.globals().set(KernelTweakingProperties.DELETE_PI_STMT_BATCH_SIZE,
            null);

   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test3Docs() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi1 = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi1.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);
      DocumentType type2 = documentTypes.get(1);
      DocumentType type3 = documentTypes.get(2);
      assertNotEquals(type1, type2);
      assertNotEquals(type3, type2);
      

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));

      final String docName1 = "TestDoc1.txt";
      final String docName2 = "TestDoc2.txt";
      final String docName3 = "TestDoc3.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] content1 = "My File Content A".getBytes();
      byte[] content2 = "My File Content B".getBytes();
      byte[] content3 = "My File Content C".getBytes();
      String path1 = addProcessAttachment(workflowService, dms, pi1, docName1, content1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      String path2 = addProcessAttachment(workflowService, dms, pi1, docName2, content2,"2.0 comment", "2.0", "descr 2.0",
            type1);
      String path3 = addProcessAttachment(workflowService, dms, pi1, docName3, content3,"3.0 comment", "3.0", "descr 3.0",
            type1);
      Document oldDocument1 = getDocumentInDms(dms, path1, docName1);
      assertNotNull(oldDocument1);
      assertEquals(new String(content1), new String(dms.retrieveDocumentContent(oldDocument1.getId())));
      Document oldDocument2 = getDocumentInDms(dms, path2, docName2);
      assertNotNull(oldDocument2);
      assertEquals(new String(content2), new String(dms.retrieveDocumentContent(oldDocument2.getId())));
      Document oldDocument3 = getDocumentInDms(dms, path3, docName3);
      assertNotNull(oldDocument3);
      assertEquals(new String(content3), new String(dms.retrieveDocumentContent(oldDocument3.getId())));
               
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path1, docName1);
      assertNull(temp);
      Folder folder1 = dms.getFolder(path1);
      assertNull(folder1);
      Document temp2 = getDocumentInDms(dms, path2, docName2);
      assertNull(temp2);
      Folder folder2 = dms.getFolder(path2);
      assertNull(folder2);
      Document temp3 = getDocumentInDms(dms, path3, docName3);
      assertNull(temp3);
      Folder folder3 = dms.getFolder(path3);
      assertNull(folder3);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      Thread.sleep(500L);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      
      Document newDocument1 = checkProcessDocInDMS(docName1, dms, content1, path1);
      Document newDocument2 = checkProcessDocInDMS(docName2, dms, content2, path2);
      Document newDocument3 = checkProcessDocInDMS(docName3, dms, content3, path3);
    
      assertEquals("a value", newDocument1.getProperties().get("MyFieldA"));
      assertEquals(123L, newDocument1.getProperties().get("MyFieldB"));
      assertObjectEquals(oldDocument3, newDocument3, oldDocument3, false);
      assertObjectEquals(oldDocument1, newDocument1, oldDocument1, false);
      assertObjectEquals(oldDocument2, newDocument2, oldDocument2, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }

   @SuppressWarnings("unchecked")
   @Test
   public void test3Processes() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi1 = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance pi2 = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      final ProcessInstance pi3 = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi1.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);
      DocumentType type2 = documentTypes.get(1);
      DocumentType type3 = documentTypes.get(2);
      assertNotEquals(type1, type2);
      assertNotEquals(type3, type2);
      

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi1.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi2.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi3.getOID()));

      final String docName1 = "TestDoc1.txt";
      final String docName2 = "TestDoc1.txt";
      final String docName3 = "TestDoc1.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] content1 = "My File Content A".getBytes();
      byte[] content2 = "My File Content B".getBytes();
      byte[] content3 = "My File Content C".getBytes();
      String path1 = addProcessAttachment(workflowService, dms, pi1, docName1, content1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      String path2 = addProcessAttachment(workflowService, dms, pi2, docName2, content2,"2.0 comment", "2.0", "descr 2.0",
            type1);
      String path3 = addProcessAttachment(workflowService, dms, pi3, docName3, content3,"3.0 comment", "3.0", "descr 3.0",
            type1);
      Document oldDocument1 = getDocumentInDms(dms, path1, docName1);
      assertNotNull(oldDocument1);
      assertEquals(new String(content1), new String(dms.retrieveDocumentContent(oldDocument1.getId())));
      Document oldDocument2 = getDocumentInDms(dms, path2, docName2);
      assertNotNull(oldDocument2);
      assertEquals(new String(content2), new String(dms.retrieveDocumentContent(oldDocument2.getId())));
      Document oldDocument3 = getDocumentInDms(dms, path3, docName3);
      assertNotNull(oldDocument3);
      assertEquals(new String(content3), new String(dms.retrieveDocumentContent(oldDocument3.getId())));
               
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(3, oldInstances.size());
      assertEquals(6, oldActivities.size());
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);

      ArchiveFilter filter = new ArchiveFilter(null, null,null, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path1, docName1);
      assertNull(temp);
      Folder folder1 = dms.getFolder(path1);
      assertNull(folder1);
      Document temp2 = getDocumentInDms(dms, path2, docName2);
      assertNull(temp2);
      Folder folder2 = dms.getFolder(path2);
      assertNull(folder2);
      Document temp3 = getDocumentInDms(dms, path3, docName3);
      assertNull(temp3);
      Folder folder3 = dms.getFolder(path3);
      assertNull(folder3);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,null, null, null, null, null);

      Thread.sleep(500L);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(3, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      Document newDocument1 = checkProcessAttachment(workflowService, pi1, dms, content1, DmsConstants.PATH_ID_ATTACHMENTS);
      Document newDocument2 = checkProcessAttachment(workflowService, pi2, dms, content2, DmsConstants.PATH_ID_ATTACHMENTS);
      Document newDocument3 = checkProcessAttachment(workflowService, pi3, dms, content3, DmsConstants.PATH_ID_ATTACHMENTS);
      newDocument1 = checkProcessDocInDMS(docName1, dms, content1, path1);
      newDocument2 = checkProcessDocInDMS(docName2, dms, content2, path2);
      newDocument3 = checkProcessDocInDMS(docName3, dms, content3, path3);
    
      assertEquals("a value", newDocument1.getProperties().get("MyFieldA"));
      assertEquals(123L, newDocument1.getProperties().get("MyFieldB"));
      assertObjectEquals(oldDocument1, newDocument1, oldDocument1, false);
      assertObjectEquals(oldDocument2, newDocument2, oldDocument2, false);
      assertObjectEquals(oldDocument3, newDocument3, oldDocument3, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }

   private Document checkProcessAttachment(WorkflowService workflowService,
         final ProcessInstance pi, DocumentManagementService dms, byte[] content, String pathId)
   {
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), pathId);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size()); 
      Document doc = processAttachments.get(0);
      assertEquals(new String(content), new String(dms.retrieveDocumentContent(doc.getId())));
      return doc;
   }

   private Document checkProcessDocInDMS(final String docName1,
         DocumentManagementService dms, byte[] content1, String path1)
   {
      Document newDocument = getDocumentInDms(dms, path1, docName1);
      assertNotNull(newDocument);
      assertEquals(new String(content1), new String(dms.retrieveDocumentContent(newDocument.getId())));
      List<Document> versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(1, versions.size());
      return newDocument;
   }

   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsExportAllImportAll() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("MyFieldA"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("MyFieldA"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);

      Thread.sleep(500L);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      for (Document doc : processAttachments)
      {
         assertNotNull(dms.retrieveDocumentContent(doc.getRevisionId()));
      }
      folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
      Document newDocument = getDocumentInDms(dms, path, docName);
      assertNotNull(newDocument);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(newDocument.getId())));
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(3, versions.size());
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);

      Document newDocumentv1 = null;
      Document newDocumentv2 = null;
      Document newDocumentv3 = null;
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            newDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals("john", version.getOwner());
            assertEquals("pdf", version.getContentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            newDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals("peter", version.getOwner());
            assertEquals("jpg", version.getContentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            newDocumentv3 = version;
         }
      }
      assertNotNull(newDocumentv1);
      assertNotNull(newDocumentv2);
      assertNotNull(newDocumentv3);
      assertObjectEquals(oldDocumentv1, newDocumentv1, oldDocumentv1, false);
      assertObjectEquals(oldDocumentv2, newDocumentv2, oldDocumentv2, false);
      assertObjectEquals(oldDocumentv3, newDocumentv3, oldDocumentv3, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testChatNoExt() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "mychat";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      Document oldDocumentv1 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(1, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);

      Thread.sleep(500L);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      for (Document doc : processAttachments)
      {
         assertNotNull(dms.retrieveDocumentContent(doc.getRevisionId()));
      }
      folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
      Document newDocument = getDocumentInDms(dms, path, docName);
      assertNotNull(newDocument);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(newDocument.getId())));
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(1, versions.size());

      Document newDocumentv1 = null;
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            newDocumentv1 = version;
         }
      }
      assertNotNull(newDocumentv1);
      assertObjectEquals(oldDocumentv1, newDocumentv1, oldDocumentv1, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDocumentDataWithPath() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = null;
      for (DocumentType type : documentTypes)
      {
         if (type.getDocumentTypeId().equals("{http://www.infinity.com/bpm/model/ArchiveModel/DataStructure2}DataStructure2"))
         {
            type1 = type;
            break;
         }
      }

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      Map<String, Serializable> props = new HashMap<String, Serializable>();
      props.put("a", "a");
      String path = addSpecificDocument(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1, ArchiveModelConstants.DATA_ID_DOCUMENTDATA2_PATH, props);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("a", "a value 1");
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("a", "a value 2");
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a", version.getProperty("a"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("a"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("a"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), ArchiveModelConstants.DATA_ID_DOCUMENTDATA2_PATH);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), ArchiveModelConstants.DATA_ID_DOCUMENTDATA2_PATH);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      for (Document doc : processAttachments)
      {
         assertNotNull(dms.retrieveDocumentContent(doc.getRevisionId()));
         assertEquals(type1, doc.getDocumentType());
      }
      folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
      Document newDocument = getDocumentInDms(dms, path, docName);
      assertNotNull(newDocument);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(newDocument.getId())));
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(3, versions.size());

      Document newDocumentv1 = null;
      Document newDocumentv2 = null;
      Document newDocumentv3 = null;
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            newDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals("john", version.getOwner());
            assertEquals("pdf", version.getContentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            newDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals("peter", version.getOwner());
            assertEquals("jpg", version.getContentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            newDocumentv3 = version;
         }
      }
      assertNotNull(newDocumentv1);
      assertNotNull(newDocumentv2);
      assertNotNull(newDocumentv3);
      assertObjectEquals(oldDocumentv1, newDocumentv1, oldDocumentv1, false);
      assertObjectEquals(oldDocumentv2, newDocumentv2, oldDocumentv2, false);
      assertObjectEquals(oldDocumentv3, newDocumentv3, oldDocumentv3, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDocumentNoDataPath() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = null;
      for (DocumentType type : documentTypes)
      {
         if (type.getDocumentTypeId().equals("{http://www.infinity.com/bpm/model/ArchiveModel/DataStructure3}DataStructure3"))
         {
            type1 = type;
            break;
         }
      }

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      Map<String, Serializable> props = new HashMap<String, Serializable>();
      props.put("b", "b");
      
      Document document = addSpecificDocument(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1, props);
      assertNotNull(document);
      
      ArchiveTest.completeNextActivity(pi,  ArchiveModelConstants.DATA_ID_DOCUMENTDATA3, document, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      Document oldDocumentv1 = null;
      
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(1, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "b", version.getProperty("b"));
            oldDocumentv1 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      String path = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) +
            "/" + DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER;
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      folder = dms.getFolder(path);
      assertNotNull(folder);
      assertEquals(1,  folder.getDocumentCount());
      Document newDocument = getDocumentInDms(dms, path, docName);
      assertNotNull(newDocument);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(newDocument.getId())));
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(1, versions.size());

      Document newDocumentv1 = null;
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            newDocumentv1 = version;
         }
      }
      assertNotNull(newDocumentv1);
      assertObjectEquals(oldDocumentv1, newDocumentv1, oldDocumentv1, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testDocumentProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_DOCUMENT, null, true);
      
    
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = null;
      for (DocumentType type : documentTypes)
      {
         if (type.getDocumentTypeId().equals("{http://www.infinity.com/bpm/model/ArchiveModel/DataStructure1}DataStructure1"))
         {
            type1 = type;
            break;
         }
      }

      final String testDocName = "TestDoc.txt";
      final String doc1Name = "Doc1Name.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentTestDoc = "My File Content TestDoc".getBytes();
      byte[] contentDoc1 = "My File Content Doc 1".getBytes();
      
      Document testDocument = addSpecificDocument(workflowService, dms, pi, testDocName, contentTestDoc,"1.0 comment", "1.0", "descr 1.0",
            type1, null);
      assertNotNull(testDocument);
      
      ArchiveTest.completeNextActivity(pi,  ArchiveModelConstants.DATA_ID_TESTDOCUMENT, testDocument, queryService, workflowService);
      
      Map<String, Serializable> props = new HashMap<String, Serializable>();
      props.put("MyFieldA", "a");
      props.put("MyFieldB", 123);
      String pathDoc1 = addSpecificDocument(workflowService, dms, pi, doc1Name, contentDoc1,"1.0 comment", "1.0", "descr 1.0",
            type1, ArchiveModelConstants.DATA_ID_DOCUMENTDATA1_PATH, props);
      Document document1 = getDocumentInDms(dms, pathDoc1, doc1Name);
      assertNotNull(document1);
      
      ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);
      ArchiveTest.completeNextActivity(pi, null, null, queryService, workflowService);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      Document oldTestDocument = null;
      Document oldDocument1 = null;
      
      assertEquals(new String(contentTestDoc), new String(dms.retrieveDocumentContent(testDocument.getId())));
      assertEquals(new String(contentDoc1), new String(dms.retrieveDocumentContent(document1.getId())));
      Thread.sleep(1000L);
      List<Document> versions = dms.getDocumentVersions(testDocument.getId());
      assertEquals(1, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentTestDoc), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            oldTestDocument = version;
         }
      }
      assertNotNull(oldTestDocument);
      versions = dms.getDocumentVersions(document1.getId());
      assertEquals(1, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentDoc1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            oldDocument1 = version;
         }
      }
      assertNotNull(oldDocument1);
      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      String path = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) +
            "/" + DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER;
      Folder folder = dms.getFolder(path);
      assertEquals(2,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(4, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      
      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, doc1Name);
      assertNull(temp);
      temp = getDocumentInDms(dms, path, testDocName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      Document newDocumentv1 = null;
      Document newTestDocumentv1 = null;
      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      folder = dms.getFolder(path);
      assertNotNull(folder);
      assertEquals(2,  folder.getDocumentCount());
      Document newDocument1 = getDocumentInDms(dms, path, doc1Name);
      assertNotNull(newDocument1);
      assertEquals(new String(contentDoc1), new String(dms.retrieveDocumentContent(newDocument1.getId())));
      versions = dms.getDocumentVersions(newDocument1.getId());
      assertEquals(1, versions.size());

      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentDoc1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            assertEquals( "a", version.getProperty("MyFieldA"));
            assertEquals(123L, version.getProperty("MyFieldB"));
            newDocumentv1 = version;
         }
      }
      Document newTestDocument = getDocumentInDms(dms, path, testDocName);
      assertNotNull(newTestDocument);
      assertEquals(new String(contentTestDoc), new String(dms.retrieveDocumentContent(newTestDocument.getId())));
      versions = dms.getDocumentVersions(newTestDocument.getId());
      assertEquals(1, versions.size());

      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentTestDoc), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals("bob", version.getOwner());
            assertEquals("xml", version.getContentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            PrintDocumentAnnotationsImpl annotations = (PrintDocumentAnnotationsImpl)version.getDocumentAnnotations();
            assertNotNull(annotations.getNotes());
            assertEquals(1,annotations.getNotes().size());
            assertEquals("blue", annotations.getNotes().iterator().next().getColor());
            newTestDocumentv1 = version;
         }
      }
      assertNotNull(newDocumentv1);
      assertNotNull(newTestDocumentv1);
      assertObjectEquals(oldDocument1, newDocumentv1, oldDocument1, false);
      assertObjectEquals(oldTestDocument, newTestDocumentv1, oldTestDocument, false);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsExportAllImportNone() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("MyFieldA"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("MyFieldA"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
      IArchive archive = archives.get(0);
      String documentNameInArchive = ExportImportSupport.getDocumentNameInArchive(pi.getOID(), document);
      assertNotNull(archive.getDocumentContent(documentNameInArchive));
      assertNotNull(archive.getDocumentProperties(documentNameInArchive));
      String documentNameV2InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv2.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV2InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV2InArchive));
      String documentNameV1InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv1.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV1InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV1InArchive));
      
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);

      Thread.sleep(500L);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null, DocumentOption.NONE));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsExportAllImportLatest() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("MyFieldA"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("MyFieldA"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      
      IArchive archive = archives.get(0);
      String documentNameInArchive = ExportImportSupport.getDocumentNameInArchive(pi.getOID(), document);
      assertNotNull(archive.getDocumentContent(documentNameInArchive));
      assertNotNull(archive.getDocumentProperties(documentNameInArchive));
      String documentNameV2InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv2.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV2InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV2InArchive));
      String documentNameV1InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv1.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV1InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV1InArchive));
      
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null, DocumentOption.LATEST));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      Document newDocument = getDocumentInDms(dms, path, docName);
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(1, versions.size());
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      for (Document doc : processAttachments)
      {
         assertNotNull(dms.retrieveDocumentContent(doc.getId()));
      }
      folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
      
      assertNotNull(newDocument);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(newDocument.getId())));

      Document newDocumentv3 = versions.get(0);
      assertEquals("1.0", newDocumentv3.getRevisionName());
      byte[] content = dms.retrieveDocumentContent(newDocumentv3.getRevisionId());
      assertEquals(new String(contentV3), new String(content));
      assertEquals("1.2 comment", newDocumentv3.getRevisionComment());
      assertEquals("descr 1.2", newDocumentv3.getDescription());
      assertEquals("peter", newDocumentv3.getOwner());
      assertEquals("jpg", newDocumentv3.getContentType());
      assertEquals(Arrays.asList("1.2"), newDocumentv3.getVersionLabels());
      assertNotNull(newDocumentv3);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsExportLatestImportAll() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("MyFieldA"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("MyFieldA"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      int countClobs = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.LATEST);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      IArchive archive = archives.get(0);

      String documentNameInArchive = ExportImportSupport.getDocumentNameInArchive(pi.getOID(), document);
      assertNotNull(archive.getDocumentContent(documentNameInArchive));
      assertNotNull(archive.getDocumentProperties(documentNameInArchive));
      String documentNameV2InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv2.getRevisionName());
      assertNull(archive.getDocumentContent(documentNameV2InArchive));
      assertNull(archive.getDocumentProperties(documentNameV2InArchive));
      String documentNameV1InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv1.getRevisionName());
      assertNull(archive.getDocumentContent(documentNameV1InArchive));
      assertNull(archive.getDocumentProperties(documentNameV1InArchive));
      
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archives.get(0), filter, null, null, DocumentOption.ALL));
      assertEquals(1, count);

      ArchiveTest.assertProcessAndActivities(queryService, pQuery, aQuery, oldInstances,
            oldActivities);
      Document newDocument = getDocumentInDms(dms, path, docName);
      versions = dms.getDocumentVersions(newDocument.getId());
      assertEquals(1, versions.size());
      processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      for (Document doc : processAttachments)
      {
         assertNotNull(dms.retrieveDocumentContent(doc.getId()));
      }
      folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
      
      assertNotNull(newDocument);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(newDocument.getId())));

      Document newDocumentv3 = versions.get(0);
      assertEquals("1.0", newDocumentv3.getRevisionName());
      byte[] content = dms.retrieveDocumentContent(newDocumentv3.getRevisionId());
      assertEquals(new String(contentV3), new String(content));
      assertEquals("1.2 comment", newDocumentv3.getRevisionComment());
      assertEquals("descr 1.2", newDocumentv3.getDescription());
      assertEquals("peter", newDocumentv3.getOwner());
      assertEquals("jpg", newDocumentv3.getContentType());
      assertEquals(Arrays.asList("1.2"), newDocumentv3.getVersionLabels());
      assertNotNull(newDocumentv3);
      int countClobsNew = ArchiveTest.countRows(ClobDataBean.TABLE_NAME);
      assertEquals(countClobs, countClobsNew);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsExportNone() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         byte[] content = dms.retrieveDocumentContent(version.getRevisionId());
         if (version.getRevisionName().equals("1.0"))
         {
            assertEquals(new String(contentV1), new String(content));
            assertEquals("1.0 comment", version.getRevisionComment());
            assertEquals("descr 1.0", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.0"), version.getVersionLabels());
            assertEquals( "a value", version.getProperty("MyFieldA"));
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            assertEquals(new String(contentV2), new String(content));
            assertEquals("1.1 comment", version.getRevisionComment());
            assertEquals("descr 1.1", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.1"), version.getVersionLabels());
            assertEquals( "a value 1", version.getProperty("MyFieldA"));
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            assertEquals(new String(contentV3), new String(content));
            assertEquals("1.2 comment", version.getRevisionComment());
            assertEquals("descr 1.2", version.getDescription());
            assertEquals(type1, version.getDocumentType());
            assertEquals(Arrays.asList("1.2"), version.getVersionLabels());
            assertEquals( "a value 2", version.getProperty("MyFieldA"));
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, null, DocumentOption.NONE);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNull(temp);
      folder = dms.getFolder(path);
      assertNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      IArchive archive = archives.get(0);
      String documentNameInArchive = ExportImportSupport.getDocumentNameInArchive(pi.getOID(), document);
      assertNull(archive.getDocumentContent(documentNameInArchive));
      assertNull(archive.getDocumentProperties(documentNameInArchive));
      String documentNameV2InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv2.getRevisionName());
      assertNull(archive.getDocumentContent(documentNameV2InArchive));
      assertNull(archive.getDocumentProperties(documentNameV2InArchive));
      String documentNameV1InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv1.getRevisionName());
      assertNull(archive.getDocumentContent(documentNameV1InArchive));
      assertNull(archive.getDocumentProperties(documentNameV1InArchive));
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void test1Doc3VersionsDumpAll() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance pi = ArchiveTest.startAndCompleteSimple(workflowService, queryService);
      DeployedModel activeModel = queryService.getModel(pi.getModelOID());
      List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(activeModel);
      DocumentType type1 = documentTypes.get(0);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      final String docName = "TestDoc.txt";
      DocumentManagementService dms = sf.getDocumentManagementService();
      byte[] contentV1 = "My File Content v1".getBytes();
      byte[] contentV2 = "My File Content v2".getBytes();
      byte[] contentV3 = "My File Content v3".getBytes();
      Document oldDocumentv1 = null;
      Document oldDocumentv2 = null;
      Document oldDocumentv3 = null;
      String path = addProcessAttachment(workflowService, dms, pi, docName, contentV1,"1.0 comment", "1.0", "descr 1.0",
            type1);
      Document document = getDocumentInDms(dms, path, docName);
      assertNotNull(document);
      assertEquals(new String(contentV1), new String(dms.retrieveDocumentContent(document.getId())));
      Thread.sleep(1000L);
      document.setDescription("descr 1.1");
      document.setOwner("john");
      document.setContentType("pdf");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 1");
      document.setProperty("MyFieldB", 222);
      document = dms.updateDocument(document, contentV2, "utf-81", true, "1.1 comment", "1.1", false);
      assertEquals(new String(contentV2), new String(dms.retrieveDocumentContent(document.getId())));
      document.setDescription("descr 1.2");
      document.setOwner("peter");
      document.setContentType("jpg");
      document.setDocumentType(type1);
      document.setProperty("MyFieldA", "a value 2");
      document.setProperty("MyFieldB", 333);
      document = dms.updateDocument(document, contentV3, "utf-82", true, "1.2 comment", "1.2", false);
      assertEquals(new String(contentV3), new String(dms.retrieveDocumentContent(document.getId())));
      List<Document> versions = dms.getDocumentVersions(document.getId());
      assertEquals(3, versions.size());
      for (Document version : versions)
      {
         if (version.getRevisionName().equals("1.0"))
         {
            oldDocumentv1 = version;
         }
         if (version.getRevisionName().equals("1.1"))
         {
            oldDocumentv2 = version;
         }
         if (version.getRevisionName().equals("1.2"))
         {
            oldDocumentv3 = version;
         }
      }
      assertNotNull(oldDocumentv1);
      assertNotNull(oldDocumentv2);
      assertNotNull(oldDocumentv3);
      
      List<Document> processAttachments = fetchProcessAttachments(workflowService, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
      assertNotNull(processAttachments);
      assertEquals(1, processAttachments.size());
      Folder folder = dms.getFolder(path);
      assertEquals(1,  folder.getDocumentCount());
    
      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      ArchiveFilter filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      ArchiveTest.exportAndArchive(workflowService, filter, "dumpLocation", DocumentOption.ALL);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(1, instances.size());
      assertEquals(2, activitiesCleared.size());
      Document temp = getDocumentInDms(dms, path, docName);
      assertNotNull(temp);
      folder = dms.getFolder(path);
      assertNotNull(folder);
           
      filter = new ArchiveFilter(null, null,null, null, null, null, null);
      List<IArchive> archives = (List<IArchive>) workflowService
            .execute(new ImportProcessesCommand(filter, null));
      assertEquals(1, archives.size());
      filter = new ArchiveFilter(null, null,oids, null, null, null, null);
      IArchive archive = archives.get(0);
      versions = dms.getDocumentVersions(oldDocumentv3.getId());
      assertEquals(3, versions.size());
      String documentNameInArchive = ExportImportSupport.getDocumentNameInArchive(pi.getOID(), document);
      assertNotNull(archive.getDocumentContent(documentNameInArchive));
      assertNotNull(archive.getDocumentProperties(documentNameInArchive));
      String documentNameV2InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv2.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV2InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV2InArchive));
      String documentNameV1InArchive = ExportImportSupport.getDocumentNameInArchive(documentNameInArchive, oldDocumentv1.getRevisionName());
      assertNotNull(archive.getDocumentContent(documentNameV1InArchive));
      assertNotNull(archive.getDocumentProperties(documentNameV1InArchive));
   }
   
   /**
    * @param pi
    * @return
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   protected static List<Document> fetchProcessAttachments(WorkflowService ws, Long piOid,
         String pathId)
   {
      List<Document> processAttachments = new ArrayList<Document>();

      Object object = ws.getInDataPath(piOid, pathId);

      if (object != null)
      {
         if (object instanceof Collection)
         {
            processAttachments.addAll((Collection) object);
         }
         else
         {
            processAttachments.add((Document) object);
         }
      }

      return processAttachments;
   }
   
   protected static Document getDocumentInDms(DocumentManagementService dms, String path, String name)
   {
      Document document = dms.getDocument(path + "/" + name);
      return document;
   }
   
   protected static String addProcessAttachment(WorkflowService ws, DocumentManagementService dms, ProcessInstance pi, String docName, byte[] content,
         String revisionComment, String revisionName, String descr, DocumentType type)
   {
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      defaultPath += "/" + DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER;
     
      DmsUtils.ensureFolderHierarchyExists(defaultPath, dms);

      DmsDocumentBean document = (DmsDocumentBean)dms.getDocument(defaultPath + "/" + docName);

      if (document == null)
      {
         DmsDocumentBean docInfo = new DmsDocumentBean();

         docInfo.setName(docName);
         docInfo.setContentType("text/plain");
         docInfo.setDescription(descr);
         docInfo.setOwner("bob");
         docInfo.setContentType("xml");
         docInfo.setDocumentType(type);
         docInfo.setEncoding("utf-8");
         Map<String, Serializable> props = new HashMap<String, Serializable>();
         props.put("MyFieldA", "a value");
         props.put("MyFieldB", 123);
         
         docInfo.setProperties(props);
         PrintDocumentAnnotationsImpl annotations = new PrintDocumentAnnotationsImpl();
         Note note = new Note();
         note.setColor("blue");
         annotations.setSender("sender");
         annotations.addNote(note);
        
         docInfo.setDocumentAnnotations(annotations);
         document = (DmsDocumentBean)dms.createDocument(defaultPath, docInfo, content, "utf-8");
         document = (DmsDocumentBean)dms.versionDocument(document.getId(), revisionComment, revisionName);
         
         List<Document> processAttachments = fetchProcessAttachments(ws, pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
         processAttachments.add(document);
         ws.setOutDataPath(pi.getOID(), DmsConstants.PATH_ID_ATTACHMENTS, processAttachments);
      }
      return defaultPath;
   }
   
   @SuppressWarnings("rawtypes")
   private String addSpecificDocument(WorkflowService ws, DocumentManagementService dms, ProcessInstance pi, String docName, byte[] content,
         String revisionComment, String revisionName, String descr, DocumentType type, String dataPathId, Map props)
   {
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      defaultPath += "/" + DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER;
     
      DmsUtils.ensureFolderHierarchyExists(defaultPath, dms);

      DmsDocumentBean document = (DmsDocumentBean)dms.getDocument(defaultPath + "/" + docName);

      if (document == null)
      {
         DmsDocumentBean docInfo = new DmsDocumentBean();

         docInfo.setName(docName);
         docInfo.setContentType("text/plain");
         docInfo.setDescription(descr);
         docInfo.setOwner("bob");
         docInfo.setContentType("xml");
         docInfo.setDocumentType(type);
         docInfo.setEncoding("utf-8");
         docInfo.setProperties(props);
         PrintDocumentAnnotationsImpl annotations = new PrintDocumentAnnotationsImpl();
         Note note = new Note();
         note.setColor("blue");
         annotations.setSender("sender");
         annotations.addNote(note);
        
         docInfo.setDocumentAnnotations(annotations);
         document = (DmsDocumentBean)dms.createDocument(defaultPath, docInfo, content, "utf-8");
         document = (DmsDocumentBean)dms.versionDocument(document.getId(), revisionComment, revisionName);
                 
         if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPathId))
         {
            List<Document> processAttachments = fetchProcessAttachments(ws, pi.getOID(), dataPathId);
            processAttachments.add(document);
            ws.setOutDataPath(pi.getOID(), dataPathId, processAttachments);
         }
         else
         {
            ws.setOutDataPath(pi.getOID(), dataPathId, document);
         }
      }
      return defaultPath;
   }
   
   @SuppressWarnings("rawtypes")
   private Document addSpecificDocument(WorkflowService ws, DocumentManagementService dms, ProcessInstance pi, String docName, byte[] content,
         String revisionComment, String revisionName, String descr, DocumentType type, Map props)
   {
      String defaultPath = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
      defaultPath += "/" + DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER;
     
      DmsUtils.ensureFolderHierarchyExists(defaultPath, dms);

      DmsDocumentBean document = (DmsDocumentBean)dms.getDocument(defaultPath + "/" + docName);

      if (document == null)
      {
         DmsDocumentBean docInfo = new DmsDocumentBean();

         docInfo.setName(docName);
         docInfo.setContentType("text/plain");
         docInfo.setDescription(descr);
         docInfo.setOwner("bob");
         docInfo.setContentType("xml");
         docInfo.setDocumentType(type);
         docInfo.setEncoding("utf-8");
         docInfo.setProperties(props);
         PrintDocumentAnnotationsImpl annotations = new PrintDocumentAnnotationsImpl();
         Note note = new Note();
         note.setColor("blue");
         annotations.setSender("sender");
         annotations.addNote(note);
        
         docInfo.setDocumentAnnotations(annotations);
         document = (DmsDocumentBean)dms.createDocument(defaultPath, docInfo, content, "utf-8");
         document = (DmsDocumentBean)dms.versionDocument(document.getId(), revisionComment, revisionName);
        
      }
      return document;
   }
   
   private static void assertObjectEquals(Object a, Object b, Object from, boolean compareRTOids)
         throws Exception
   {
      if (a == null && b == null)
      {
         return;
      }
      if (a == null && b != null)
      {
         fail("Original object was null but imported object is not: " + b);
         return;
      }
      if (a != null && b == null)
      {
         fail("Original object was not null but imported object is null: " + a);
         return;
      }
      BeanInfo beanInfo = Introspector.getBeanInfo(a.getClass());

      for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
      {
         if (a.getClass() == UserDetails.class) {
            continue;
         }
         if (property.getReadMethod() == null)
         {
            continue;
         }
         Object valueA = property.getReadMethod().invoke(a);
         Object valueB = property.getReadMethod().invoke(b);
         if (property.getPropertyType().getPackage() != null
               && property.getPropertyType().getPackage().getName()
                     .contains("org.eclipse.stardust.engine"))
         {
            if (from.equals(valueA))
            {
               return;
            }
            assertObjectEquals(valueA, valueB, valueA, compareRTOids);
         }
         else
         {
            if (property.getPropertyType().isArray())
            {
               assertArrayEquals(a.getClass().getSimpleName() + " " + property.getName()
                     + " not equals. Expected " + valueA + " but got " + valueB,
                     (Object[]) valueA, (Object[]) valueB);
            }
            else if (Collection.class.isAssignableFrom(property.getPropertyType()))
            {

            }
            else
            {

               boolean mustTest = false;

               if ("dateCreated".equalsIgnoreCase(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else if ("dateLastModified".equalsIgnoreCase(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else if ("id".equalsIgnoreCase(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else if ("revisionId".equalsIgnoreCase(property.getName()) && !compareRTOids)
               {
                  mustTest = false;
               }
               else
               {
                  mustTest = true;
               }

               if (mustTest)
               {
                  assertEquals(a.getClass().getSimpleName() + " " + property.getName()
                        + " not equals. Expected " + valueA + " but got " + valueB,
                        valueA, valueB);
               }
            }
         }
      }
   }


}
