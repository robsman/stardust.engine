/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.command.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;




/**
 *
 *	Service command class for ExtractPage
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ExtractPageCommand implements ServiceCommand
{
   public static final String PROCESS_ATTACHMENT_DATA = "process-attachment";
   public static final String PROCESS_ATTACHMENTS = "PROCESS_ATTACHMENTS";
   public static final String ZERO = "0";
   private static final long serialVersionUID = 753587711788587975L;
   private ProcessInstance processInstance;
   private List<PageModel> pages;
   private String sourceDocumentName;
   private String sourceDocumentPath;
   private ServiceFactory sf;
   private String mimeType;
   private String fileName;
   private String fileExtn;

   /**
     *
     */
   public Serializable execute(ServiceFactory sf)
   {
      this.sf = sf;
      init();
      if (null != processInstance)
      {
         return (Serializable) spawnProcesses();
      }
      // process context is not available then start process instead of spawn process
      else
      {
         return (Serializable) startProcesses();
      }

   }

   private void init()
   {
      fileName = stripExtension(sourceDocumentName);
      fileExtn = sourceDocumentName.length() > fileName.length()
            ? sourceDocumentName.substring(fileName.length() + 1)
            : "";
   }

   /**
    * if source document is not associated with any process then call this method create
    * document in source document folder then start process and move document to newly
    * create process folder only if document data is selected
    */
   private List<ProcessInstance> startProcesses()
   {
      List<ProcessInstance> processInstances = CollectionUtils.newArrayList();
      for (PageModel page : pages)
      {
         Map<String, Object> data = null;
         Document document = null;
         String newDocumentName = generateName();
         if (StringUtils.isNotEmpty(page.getDataId()) && !PROCESS_ATTACHMENT_DATA.equals(page.getDataId()))
         {
            Map<String, Object> properties = CollectionUtils.newHashMap();
            // properties.put(CommonProperties.DESCRIPTION, page.getVersionComment());
            // properties.put(CommonProperties.COMMENTS, page.getDescription());
            document = ExtractPageUtil.createDocument(sf, sourceDocumentPath, page.content, newDocumentName, mimeType,
                  properties, page.getAnnotations());

            data = CollectionUtils.newHashMap();
            data.put(page.getDataId(), document);
         }

         ProcessInstance pi = sf.getWorkflowService().startProcess(page.processId, data, false);
         processInstances.add(pi);
         // attach to process only if attachment is allowed
         // also move document to process-attachment folder and attach to process
         if (PROCESS_ATTACHMENT_DATA.equals(page.getDataId()) && ExtractPageUtil.isProcessAttachmentAllowed(sf, pi))
         {
            Folder targetFolder = ExtractPageUtil.getProcessAttachmentsFolder(sf, pi);
            Map<String, Object> properties = CollectionUtils.newHashMap();
            // properties.put(CommonProperties.DESCRIPTION, page.getVersionComment());
            // properties.put(CommonProperties.COMMENTS, page.getDescription());
            document = ExtractPageUtil.createDocument(sf, targetFolder.getId(), page.content, newDocumentName,
                  mimeType, properties, page.getAnnotations());
            // Overwrite eventually copied process attachments.
            List<Document> documents = CollectionUtils.newArrayList();
            documents.add(document);
            ExtractPageUtil.saveProcessAttachments(sf, pi, documents);
         }

         page.setStartedProcessInstance(pi);
         page.setDocument(document);
      }
      return processInstances;
   }

   /**
    * if document is associated with process then call this method create document in root
    * process then spawn process and move document to newly create process folder only if
    * document data is selected
    *
    * @return
    */
   private List<ProcessInstance> spawnProcesses()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<SubprocessSpawnInfo> infoList = CollectionUtils.newArrayList();

      Map<String, Document> data = CollectionUtils.newHashMap();
      List<ProcessInstance> spawnProcesses = new ArrayList<ProcessInstance>();
      for (PageModel page : pages)
      {
         String newDocumentName = generateName();
         if (StringUtils.isNotEmpty(page.getDataId()) && !PROCESS_ATTACHMENT_DATA.equals(page.getDataId()))
         {
            Map<String, Object> properties = CollectionUtils.newHashMap();

            // properties.put(CommonProperties.DESCRIPTION, page.getVersionComment());
            // properties.put(CommonProperties.COMMENTS, page.getDescription());
            Document document = ExtractPageUtil.createDocument(sf, sourceDocumentPath, page.content, newDocumentName,
                  mimeType, properties, page.getAnnotations());
            // set DATAID to new spawn process
            data.put(page.getDataId(), document);
            page.setDocument(document);
         }
         
         // DMS data should not be copied, so initialize it with null values.
         setDmsDataNull(data, page);
         
         if(!page.isAbortProcessInstance())
         {
            spawnProcesses.add(sf.getWorkflowService().spawnPeerProcessInstance(processInstance.getOID(), page.getProcessId(), page.isCopyData(), data, page.isAbortProcessInstance(), page.getLinkComment()));
         }
         else
         {
            SubprocessSpawnInfo info = new SubprocessSpawnInfo(page.getProcessId(), page.isCopyData(), data);
            infoList.add(info);   
         }
      }

      // spawn process
      if(!infoList.isEmpty())
      {
         if (spawnProcesses.isEmpty())
         {
            spawnProcesses = workflowService.spawnSubprocessInstances(processInstance.getOID(), infoList);
         }
         else
         {
            spawnProcesses.addAll(workflowService.spawnSubprocessInstances(processInstance.getOID(), infoList));
         }
      }
      

      // start process
      for (int i = 0; i < spawnProcesses.size(); i++)
      {
         ProcessInstance pi = spawnProcesses.get(i);
         PageModel page = pages.get(i);

         if (PROCESS_ATTACHMENT_DATA.equals(page.getDataId()) && ExtractPageUtil.isProcessAttachmentAllowed(sf, pi))
         {
            String newDocumentName = generateName();
            Folder processFolder = ExtractPageUtil.getProcessAttachmentsFolder(sf, pi);
            Map<String, Object> properties = CollectionUtils.newHashMap();

            // properties.put(CommonProperties.DESCRIPTION, page.getVersionComment());
            // properties.put(CommonProperties.COMMENTS, page.getDescription());
            Document document = ExtractPageUtil.createDocument(sf, processFolder.getId(), page.content,
                  newDocumentName, mimeType, properties, page.getAnnotations());
            // Overwrite eventually copied process attachments.
            List<Document> documents = CollectionUtils.newArrayList();
            documents.add(document);
            ExtractPageUtil.saveProcessAttachments(sf, pi, documents);
            page.setDocument(document);
         }
         page.setStartedProcessInstance(pi);
      }
      return spawnProcesses;
   }

   /**
    * Add dataId with null value for all existing dms data that does not already contain
    * the extracted page.
    * 
    * @param data data that can already contain an extracted page.
    * @param page 
    */
   private void setDmsDataNull(Map<String, Document> data, PageModel page)
   {
      IModel model = getModel(page.getProcessId());
      
      if (model == null)
      {
         if (processInstance != null)
         {
            ModelManager modelManager = ModelManagerFactory.getCurrent();
            model = modelManager.findModel(processInstance.getModelOID());
         }
      }

      if (model != null)
      {
         ModelElementList<IData> allData = model.getData();
         for (IData iData : allData)
         {
            if (StructuredTypeRtUtils.isDmsType(iData.getType().getId()) && !data.containsKey(iData.getId()))
            {
               data.put(iData.getId(), null);
            }
         }
      }
   }
   
   /**
    * @param processId
    * @return
    */
   private IModel getModel(String processId)
   {
      String namespace = null;
      if (processId.startsWith("{"))
      {
         QName qname = QName.valueOf(processId);
         namespace = qname.getNamespaceURI();
         processId = qname.getLocalPart();
      }

      if (namespace != null)
      {
         IModel model = ModelManagerFactory.getCurrent().findActiveModel(namespace);
         return model;
      }
      return null;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public String getSourceDocumentPath()
   {
      return sourceDocumentPath;
   }

   public void setSourceDocumentPath(String sourceDocumentPath)
   {
      this.sourceDocumentPath = sourceDocumentPath;
   }

   public List<PageModel> getPages()
   {
      return pages;
   }

   public void setPages(List<PageModel> pages)
   {
      this.pages = pages;
   }

   public String getSourceDocumentName()
   {
      return sourceDocumentName;
   }

   public void setSourceDocumentName(String sourceDocumentName)
   {
      this.sourceDocumentName = sourceDocumentName;
   }


   public String getMimeType()
   {
      return mimeType;
   }

   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }

   /**
    *
    */
   public static class PageModel implements Serializable
   {
      private static final long serialVersionUID = 1L;
      private final byte[] content;
      private final String versionComment;
      private final String description;
      private final DocumentAnnotations annotations;

      private final String processId;
      private final boolean copyData;
      private final String dataId;
      private final boolean abortProcessInstance;
      private Document document;
      private ProcessInstance startedProcessInstance;
      private String linkComment;

      public PageModel(byte[] content, String versionComment, String description, DocumentAnnotations annotations,
            String processId, boolean copyData, String dataId, boolean abortProcessInstance)
      {
         this.content = content;
         this.versionComment = versionComment;
         this.description = description;
         this.annotations = annotations;

         this.processId = processId;
         this.copyData = copyData;
         this.dataId = dataId;
         this.abortProcessInstance = abortProcessInstance;
      }

      public byte[] getContent()
      {
         return content;
      }

      public String getVersionComment()
      {
         return versionComment;
      }

      public String getDescription()
      {
         return description;
      }

      public DocumentAnnotations getAnnotations()
      {
         return annotations;
      }

      // public String getPath()
      // {
      // return path;
      // }

      public String getProcessId()
      {
         return processId;
      }

      public boolean isCopyData()
      {
         return copyData;
      }

      public String getDataId()
      {
         return dataId;
      }

      public Document getDocument()
      {
         return document;
      }

      public void setDocument(Document document)
      {
         this.document = document;
      }

      public ProcessInstance getStartedProcessInstance()
      {
         return startedProcessInstance;
      }

      public void setStartedProcessInstance(ProcessInstance startedProcessInstance)
      {
         this.startedProcessInstance = startedProcessInstance;
      }

      public boolean isAbortProcessInstance()
      {
         return abortProcessInstance;
      }

      public String getLinkComment()
      {
         return linkComment;
      }

      public void setLinkComment(String linkComment)
      {
         this.linkComment = linkComment;
      }

   }

   /**
    *
    */
   private static class ExtractPageUtil
   {
      /**
       * Returns the folder if exist otherwise create new folder
       *
       * @param folderPath
       * @return
       */
      public static Folder createFolderIfNotExists(ServiceFactory sf, String folderPath)
      {
         Folder folder = sf.getDocumentManagementService().getFolder(folderPath, Folder.LOD_NO_MEMBERS);

         if (null == folder)
         {
            // folder does not exist yet, create it
            String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
            String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

            if (StringUtils.isEmpty(parentPath))
            {
               // top-level reached
               return sf.getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
            }
            else
            {
               Folder parentFolder = createFolderIfNotExists(sf, parentPath);

               return sf.getDocumentManagementService().createFolder(parentFolder.getId(),
                     DmsUtils.createFolderInfo(childName));
            }
         }
         else
         {
            return folder;
         }
      }

      /**
       *
       * @param targetId
       * @param byteContents
       * @param fileName
       * @param contentType
       * @param properties
       * @param annotation
       * @return
       */
      public static Document createDocument(ServiceFactory sf, String targetId, byte[] byteContents, String fileName,
            String contentType, Map<String, Object> properties, DocumentAnnotations annotation)
      {
         Document doc = null;
         DocumentManagementService documentManagementService = sf.getDocumentManagementService();

         if (null != byteContents)
         {
            DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);
            docInfo.setOwner(sf.getUserService().getUser().getAccount());
            docInfo.setContentType(contentType);
            docInfo.setDocumentAnnotations(annotation);

            if (null != properties)
            {
               docInfo.setProperties(properties);
            }

            doc = documentManagementService.createDocument(targetId, docInfo, byteContents, null);
            // for creating version
            documentManagementService.versionDocument(doc.getId(), null, ZERO);
         }

         return doc;
      }


      /**
       * return process attachment folder path
       *
       * @param pi
       * @return
       */
      public static Folder getProcessAttachmentsFolder(ServiceFactory sf, ProcessInstance pi)
      {
         String path = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) + "/"
               + DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER;
         Folder folder = createFolderIfNotExists(sf, path);

         return folder;
      }

      /**
       * @param pi
       * @return
       */
      public static List<Document> fetchProcessAttachments(ServiceFactory sf, ProcessInstance processInstance)
      {
         List<Document> processAttachments = new ArrayList<Document>();
         Object object = sf.getWorkflowService().getInDataPath(processInstance.getOID(), PROCESS_ATTACHMENTS);

         if (object != null)
         {
            processAttachments.addAll((Collection) object);
         }

         return processAttachments;
      }

      /**
       * @param processAttachments
       */
      public static void saveProcessAttachments(ServiceFactory sf, ProcessInstance processInstance,
            List<Document> processAttachments)
      {
         sf.getWorkflowService().setOutDataPath(processInstance.getOID(), PROCESS_ATTACHMENTS, processAttachments);
      }

      /**
       *
       * @param pi
       * @return
       */
      public static boolean isProcessAttachmentAllowed(ServiceFactory sf, ProcessInstance pi)
      {
         if (pi.getState().getValue() == ProcessInstanceState.ACTIVE)
         {
            ProcessDefinition pd = sf.getQueryService().getProcessDefinition(pi.getModelOID(), pi.getProcessID());
            List<DataPath> dataPaths = pd.getAllDataPaths();

            for (DataPath dataPath : dataPaths)
            {
               if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId())
                     && dataPath.getDirection().equals(Direction.IN))
               {
                  return true;
               }
            }
         }

         return false;
      }

   }
   /**
    * generate new file name by appending date-time in source file name
    *
    * @return
    */
   private String generateName()
   {
      StringBuilder name = new StringBuilder().append(fileName).append("_")
            .append(String.valueOf(TimestampProviderUtils.getTimeStampValue()));

      if (StringUtils.isNotEmpty(fileExtn))
      {
         name.append(".").append(fileExtn);
      }
      return name.toString();
   }

   /**
    *
    * @param fileName
    */
   private String stripExtension(String fileName)
   {
      int index = fileName.lastIndexOf('.');
      if (index > 0 && index <= fileName.length() - 2)
      {
         return fileName.substring(0, index);
      }
      return fileName;

   }
}

