package org.eclipse.stardust.engine.extensions.camel.util;

import static org.eclipse.stardust.engine.api.runtime.DmsUtils.createDocumentInfo;
import static org.eclipse.stardust.engine.api.runtime.DmsUtils.ensureFolderHierarchyExists;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * Class capable of archiving files into a JCR repository via IPP's
 * DocumentManagementService.
 * 
 */
public class DmsFileArchiver
{

   private static Logger log = LogManager.getLogger(DmsFileArchiver.class);

   private String rootFolderPath = "";

   /**
    * The content-type of the files being archived. Default is 'text/plain'.
    */
   private String contentType = "text/plain";

   private String createOperator = "";

   private String documentType = "";

   private String documentIdPrefix = "Document-";

   /**
    * The format for the timestamp appended to the documentId of the file being archived.
    * Default is 'yyyy-MM-dd-hhmmsss'.
    */
   private String documentIdDateFormatPattern = "yyyy-MM-dd-hhmmsss";

   private SimpleDateFormat documentIdDateFormat;

   private ServiceFactory serviceFactory;

   public DmsFileArchiver()
   {}

   public DmsFileArchiver(ServiceFactory factory)
   {
      this.serviceFactory = factory;
   }

   /**
    * Stores or updates the file with the specified filename at a certain folder location
    * using the specified byte[].<br/>
    * The folder location is determined via {@link #rootFolderPath} + the folderName
    * parameter. If both are empty, the root folder "/" is used (which is not good
    * practice for production scenarios).
    * 
    * @param fileContent
    * @param filename
    * @param folderName
    * @return document
    */
   public Document archiveFile(byte[] fileContent, String filename, String folderName)
   {
      return archiveFile(fileContent, filename, folderName, null, null, null);
   }

   public Document archiveFile(byte[] fileContent, String filename, String folderName,
         Map<String, Serializable> properties)
   {
      return archiveFile(fileContent, filename, folderName, null, null, properties);
   }

   /**
    * Stores or updates the file with the specified filename at a certain folder location
    * using the specified byte[].The specified properties are attached to the document as
    * metadata.<br/>
    * The folder location is determined via {@link #rootFolderPath} + the folderName
    * parameter. If both are empty, the root folder "/" is used (which is not good
    * practice for production scenarios).
    * 
    * @param fileContent
    *           the file content
    * @param filename
    *           the file name
    * @param folderName
    *           the folder name
    * @param contentType
    *           the content type
    * @param owner
    *           the owner
    * @param properties
    *           the document's metadata
    * @return document
    */
   public Document archiveFile(byte[] fileContent, String filename, String folderName, String contentType,
         String owner, Map<String, Serializable> properties)
   {

      ServiceFactory sf = null;
      if (null != this.serviceFactory)
         sf = this.serviceFactory;
      else
         sf = ClientEnvironment.getCurrentServiceFactory();

      if (null == sf)
         throw new IllegalStateException("No ServiceFactory set!");

      if (null == documentIdDateFormat)
         documentIdDateFormat = new SimpleDateFormat(documentIdDateFormatPattern);

      try
      {
         if (StringUtils.isEmpty(rootFolderPath) && StringUtils.isEmpty(folderName))
         {
            // For some reason archiving in the root folder "/" throws an exception
            // regarding the path being a relPath.
            // TODO Retest with IPP > 5.3.x
            throw new UnsupportedOperationException("Archiving into the root folder '/' is"
                  + "not supported. Either field 'rootFolderPath' " + "or argument 'folderName' needs to be set!");
         }

         // Assemble target folder path
         String targetFolderPath=null;
         if (!StringUtils.isEmpty(folderName))
            targetFolderPath = folderName;
         else
         {
             targetFolderPath = StringUtils.isEmpty(rootFolderPath) ? "/" : rootFolderPath;
            if (!StringUtils.isEmpty(folderName))
            {
               targetFolderPath += "/".equals(targetFolderPath) ? folderName : "/" + folderName;
            }
         }
         DocumentManagementService dmService = sf.getDocumentManagementService();

         Folder targetFolder = ensureFolderHierarchyExists(targetFolderPath, dmService);

         Document document = dmService.getDocument(targetFolder.getPath() + "/" + filename);
         if (null == document)
         {
            DocumentInfo fileInfo = createDocumentInfo(filename);

            if (StringUtils.isNotEmpty(contentType))
               fileInfo.setContentType(contentType);
            else
               fileInfo.setContentType(this.contentType);

            if (StringUtils.isNotEmpty(owner))
               fileInfo.setOwner(owner);
            else
               fileInfo.setOwner(this.createOperator);

            // Add document metadata
            if (null == properties)
            {
               properties = new HashMap<String, Serializable>();
               String timestamp = SimpleDateFormat.getInstance().format(
                     TimestampProviderUtils.getTimeStamp());
               properties.put("documentId", documentIdPrefix + documentIdDateFormat.format(
                     TimestampProviderUtils.getTimeStamp()));
               properties.put("documentType", documentType);
               properties.put("createDate", timestamp);
            }

            fileInfo.setProperties(properties);
            if (log.isDebugEnabled())
               log.debug("Creating file: " + targetFolder.getPath() + "/" + filename + ".");

            document = dmService.createDocument(targetFolder.getId(), fileInfo, fileContent, null);
         }
         else
         {
            if (log.isDebugEnabled())
               log.debug("Updating test file: " + targetFolder.getPath() + "/" + filename + ".");

            document = dmService.updateDocument(document, fileContent, null, true, "", "", false);
         }

         return document;
      }
      catch (ServiceNotAvailableException e)
      {
         log.error("WorkflowService not available.");
         throw e;
      }
//      finally
//      {
//         if (null != sf)
//            ClientEnvironment.instance().returnServiceFactory(sf);
//      }
   }

   /**
    * @param rootFolderPath
    */
   public void setRootFolderPath(String rootFolderPath)
   {
      this.rootFolderPath = rootFolderPath;
   }

   /**
    * @param contentType
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }

   /**
    * @param createOperator
    */
   public void setCreateOperator(String createOperator)
   {
      this.createOperator = createOperator;
   }

   /**
    * @param documentType
    */
   public void setDocumentType(String documentType)
   {
      this.documentType = documentType;
   }

   /**
    * @param documentIdDateFormat
    */
   public void setDocumentIdDateFormatPattern(String documentIdDateFormat)
   {
      if (StringUtils.isEmpty(documentIdDateFormat))
         throw new IllegalArgumentException("Cannot set empty documentIdDateFormat: " + documentIdDateFormat);
      this.documentIdDateFormat = new SimpleDateFormat(documentIdDateFormatPattern);
      this.documentIdDateFormatPattern = documentIdDateFormat;
   }

   /**
    * @param serviceFactory
    */
   public void setServiceFactory(ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
   }
}
