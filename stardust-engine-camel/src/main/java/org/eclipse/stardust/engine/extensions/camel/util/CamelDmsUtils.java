package org.eclipse.stardust.engine.extensions.camel.util;

import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.engine.api.runtime.DmsUtils.createFolderInfo;
import static org.eclipse.stardust.engine.extensions.camel.Util.getBodyOutAccessPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.ParameterMapping;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.app.CamelMessageLocation;
import org.eclipse.stardust.engine.extensions.camel.converter.MimeMultipartTypeConverter;
import org.eclipse.stardust.engine.extensions.camel.converter.MimeTypeUtils;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.CreateDocumentException;

/**
 * Utility class for Document Management Service
 * @author Sabri.Bousselmi
 * @version $Revision: $
 */
public class CamelDmsUtils
{
   public static Document storeDocument(DocumentManagementService dms, ProcessInstance pi,
         byte[] content, String fileName, Boolean processAttachmentSupport) throws CreateDocumentException {

      StringBuilder defaultPath = new StringBuilder(
            org.eclipse.stardust.engine.api.runtime.DmsUtils.composeDefaultPath(
                  pi.getScopeProcessInstanceOID(), pi.getStartTime()))
            .append("/");

      if(processAttachmentSupport) {
         defaultPath
         .append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER);
      }
      else {
         defaultPath
         .append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER);
      }

      ensureFolderExists(dms, defaultPath.toString());
      
      Document doc = storeDocumentIntoDms(dms, defaultPath.toString(),
            content, fileName);
      
      return doc;
   }

   private static Document storeDocumentIntoDms(DocumentManagementService dms,
         String folderId, byte[] content, String fileName) throws CreateDocumentException {

      try {
         DocumentInfo docInfo = org.eclipse.stardust.engine.api.runtime.DmsUtils.createDocumentInfo(fileName);

         String documentPath = folderId;

         if (!folderId.endsWith("/")) {
            documentPath += "/";
         }

         documentPath += docInfo.getName();
         
         // check if document already created for PI
         Document doc = dms.getDocument(documentPath);
         
         if(doc == null)
         {
            doc = dms.createDocument(folderId, docInfo,
                  extractContentByteArray(content), null);
         }

         return doc;

      } catch (DocumentManagementServiceException ex) {
         
         throw new CreateDocumentException("Failed creating document.", ex);
      }
   }

   private static byte[] extractContentByteArray(byte[] content) {

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[4096];

      try {
         InputStream from = new ByteArrayInputStream(content);

         try {
            int bytesRead;
            while (0 < (bytesRead = from.read(buffer))) {
               baos.write(buffer, 0, bytesRead);
            }
         } finally {
            from.close();
         }
      } catch (IOException ioe) {
         throw new PublicException(
               "Failed retrieving document content.", ioe);
      }

      return baos.toByteArray();
   }

   private static void ensureFolderExists(DocumentManagementService dms,
         String folderId) throws DocumentManagementServiceException {
      if (!StringUtils.isEmpty(folderId) && folderId.startsWith("/")) {
         // try to create folder
         String[] segments = folderId.substring(1).split("/");

         // walk backwards to find existing path prefix, then go forward
         // again creating missing segments

         Folder folder = null;
         LinkedList<String> missingSegments = newLinkedList();
         for (int i = segments.length - 1; i >= 0; --i) {
            StringBuilder path = new StringBuilder();
            for (int j = 0; j <= i; ++j) {
               path.append("/").append(segments[j]);
            }

            folder = dms.getFolder(path.toString(),
                  Folder.LOD_NO_MEMBERS);
            if (null != folder) {
               // found existing prefix
               break;
            } else {
               // folder missing?
               missingSegments.add(0, segments[i]);
            }
         }

         String currentPath = (null != folder) ? folder.getPath() : "";
         while (!missingSegments.isEmpty()) {
            String parentFolderId = StringUtils.isEmpty(currentPath) ? "/"
                  : currentPath;

            String segment = missingSegments.remove(0);

            // create missing sub folder
            folder = dms.createFolder(parentFolderId,
                  createFolderInfo(segment));
            currentPath = folder.getPath();
         }
      }
   }
   
   public static Document toDocument(Object messageContent, Exchange exchange, String fileName, DocumentManagementService dms, ProcessInstance pi, boolean processAttachmentSupport)
   {
      Document document = null;
      fileName = getDocumentName(messageContent, fileName);
      
      try
      {
         byte[] jcrDocumentContent = messageContentToByte(messageContent, exchange);
         document = CamelDmsUtils.storeDocument(dms, pi, jcrDocumentContent, fileName, processAttachmentSupport);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed creating document.", e);
      }
      return document; 
   }
   
   @SuppressWarnings("unchecked")
   public static List<String> getDocumentAccessPointListForTrigger(ProcessDefinition processDefinition)
   { 
      List<String> documentAccessPointList = new ArrayList<String>();
      Iterator<Trigger>  triggers = processDefinition.getAllTriggers().iterator();
      while (triggers.hasNext())
      {
         Trigger trigger = triggers.next();
         Iterator<ParameterMapping> ParameterMappings = trigger.getAllParameterMappings().iterator();
         while (ParameterMappings.hasNext())
         {
            ParameterMapping parameterMapping = ParameterMappings.next();
            AccessPoint accessPoint = parameterMapping.getParameter();
            if(accessPoint.getAccessPathEvaluatorClass().equals(CamelConstants.VFS_DOCUMENT_ACCESS_PATHE_EVALUATOR_CLASS))
            {
               documentAccessPointList.add(parameterMapping.getDataId());
            }
         }
      }
      return documentAccessPointList;
   }
   
   public static byte[] messageContentToByte(Object messageContent, Exchange exchange)
         throws IOException, MessagingException
   {
      byte[] jcrDocumentContent = null;

      if (exchange != null)
      {
         if (messageContent instanceof GenericFile< ? >)
         {
            ((GenericFile< ? >) messageContent).getBinding().loadContent(exchange,
                  ((GenericFile< ? >) messageContent));
            jcrDocumentContent = exchange.getContext().getTypeConverter()
                  .convertTo(byte[].class, exchange,
                        ((GenericFile< ? >) messageContent).getBody());

         }
         else if (messageContent instanceof String)
         {
            jcrDocumentContent = ((String) messageContent).getBytes();
         }
         else if (messageContent instanceof MimeMultipart)
         {
            MimeMultipart mimeMultipart = (MimeMultipart) messageContent;
            jcrDocumentContent = MimeMultipartTypeConverter.mimeMultipartToString(
                  mimeMultipart).getBytes();
         }
         else if (messageContent instanceof InputStream)
         {
            jcrDocumentContent = IOUtils.toByteArray(InputStream.class
                  .cast(messageContent));
         }
         else if (messageContent instanceof byte[])
         {
            jcrDocumentContent = (byte[]) messageContent;
         }

      }
      return jcrDocumentContent;
   }
   
   public static Map<String, Document> initializeDocumentData(Trigger trigger, ProcessInstance pi, Exchange exchange, ServiceFactory sf)
   {
      Map<String, Document> initialDocumentDataValues = new HashMap<String, Document>();
      Iterator<ParameterMapping> parameterMappings = trigger.getAllParameterMappings().iterator();
      while (parameterMappings.hasNext())
      {
         ParameterMapping parameterMapping = parameterMappings.next();
         AccessPoint accessPoint = parameterMapping.getParameter();
         Object bodyOutAP = getBodyOutAccessPoint(trigger) != null
               ? getBodyOutAccessPoint(trigger)
               : "returnValue";

         CamelMessageLocation location = accessPoint.getId().equals(bodyOutAP) || bodyOutAP == null
         ? CamelMessageLocation.BODY
         : CamelMessageLocation.HEADER;
         
         if(accessPoint.getAccessPathEvaluatorClass().equals(CamelConstants.VFS_DOCUMENT_ACCESS_PATHE_EVALUATOR_CLASS))
         {
            Object messageContent = null;
            if(CamelMessageLocation.HEADER.equals(location))
            {
               messageContent = exchange.getIn().getHeader(accessPoint.getName());
               if(messageContent != null)
               {
                  initialDocumentDataValues.put(
                        parameterMapping.getDataId(),
                        toDocument(messageContent, exchange, accessPoint.getName(), sf.getDocumentManagementService(), pi, false));
               }
               
            } else
            {
               messageContent = exchange.getIn().getBody();
               if(messageContent != null)
               {
                  initialDocumentDataValues.put(
                        parameterMapping.getDataId(),
                        toDocument(messageContent, exchange, accessPoint.getName(), sf.getDocumentManagementService(), pi, false));
               
               }
            }
            
         }
         
      }
      return initialDocumentDataValues;
   }
   
   private static String getDocumentName(Object messageContent,   String fileName)
   {
      if(FilenameUtils.getExtension(fileName).isEmpty())
      {
         if(messageContent  instanceof GenericFile<?>)
         {
            fileName = ((GenericFile<?>)messageContent).getFileName();
            
         } else if(messageContent instanceof String)
         {
            fileName += ".txt";
            
         } else  if(messageContent instanceof MimeMultipart)
         {
            String ext = MimeTypeUtils.getExtensionFromBodyPartContentType((MimeMultipart) messageContent);
            if(!ext.isEmpty())
            {
               fileName += "." + ext;
            }
         }
      }
      return fileName;
   }
   
   public static Document getDocumentUsingRepositoryLocation(DocumentManagementService dms, String repositoryLocation)
   {
      Document document = null;
      if (!repositoryLocation.startsWith("/"))
      {
         if(!repositoryLocation.contains("templates/"))
            repositoryLocation = "templates/" + repositoryLocation;
         
         repositoryLocation = "/artifacts/" + repositoryLocation;
      }
      document = dms.getDocument(repositoryLocation);
      return document;
   }
}
