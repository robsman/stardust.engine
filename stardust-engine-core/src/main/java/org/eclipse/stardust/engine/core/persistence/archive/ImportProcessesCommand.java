package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryAuditTrailUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;

/**
 * This class allows a request to import archived processes instances. The processes are
 * imported from a byte[]. The class returns the number of processes imported.
 * 
 * Processes can be imported:<br/>
 * <li/>completely <li/>by root process instance OIDs <li/>by business identifier (unique
 * primitive key descriptor) <li/>by from/to filter (start time to termination time) Dates
 * are inclusive<br/>
 * 
 * If processInstanceOids is null everything will be imported. If processInstanceOids is
 * empty nothing will be imported. <br/>
 * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
 * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
 * and toDate is provided then all processes will be imported. <br/>
 *
 * @author Jolene.Saayman
 * @version $Revision: $
 */
public class ImportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private static final Logger LOGGER = LogManager
         .getLogger(ImportProcessesCommand.class);

   private final IArchive archive;

   private final ArchiveFilter filter;

   private ImportMetaData importMetaData;

   private final Operation operation;
   
   private final Map<String, String> preferences;
   
   private IArchiveReader reader;
   
   private DocumentOption documentOption;

   protected ImportProcessesCommand(Operation operation, IArchive archive, 
         ArchiveFilter filter, ImportMetaData importMetaData, Map<String, String> preferences, DocumentOption documentOption)
   {
      super();
      this.operation = operation;
      this.filter = filter;
      this.archive = archive;
      this.importMetaData = importMetaData;
      this.preferences = preferences;
      this.documentOption = documentOption;
   }

   /**
    * Use this constructor to determine which archives to load
    * 
    * @param processOids
    */
   protected ImportProcessesCommand(ArchiveFilter filter, Map<String, String> preferences)
   {
      this(Operation.QUERY, null, filter, null, preferences, DocumentOption.NONE);
   }
  
   @Override
   public Serializable execute(ServiceFactory sf)
   {
      this.reader = ArchiveManagerFactory.getArchiveReader(preferences);
      if (reader == null)
      {
         throw new IllegalStateException("A valid Archive Reader could not be created");
      }
      Serializable result;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("START Import Operation " + operation.name());
      }
      switch (operation)
      {
         case QUERY:
            result = query(sf);
            break;
         case VALIDATE:
            validate(sf);
            result = importMetaData;
            break;
         case IMPORT:
            result = importData(sf);
            break;
         case VALIDATE_AND_IMPORT:
            result = validateAndImport(sf);
            break;
         default:
            throw new IllegalArgumentException("No valid operation provided");
      }
      ;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("END Import " + operation.name());
      }
      return result;
   }

   private ArrayList<IArchive> query(ServiceFactory sf)
   {
      filter.validateDates();
      ArrayList<IArchive> archives = reader.findArchives(filter);
      return archives;
   }

   private int validateAndImport(ServiceFactory sf)
   {
      if (importMetaData != null)
      {
         throw new IllegalArgumentException(
               "When using VALIDATE_AND_IMPORT, provide the model data and the export data. Do not provide importMetaData");
      }
      validate(sf);
      return importData(sf);
   }

   private int importData(ServiceFactory sf)
   {
      int importCount;
      filter.validateDates();
      ImportOidResolver oidResolver = new ImportOidResolver(importMetaData);
      final Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      try
      {
         Map<String, List<byte[]>> dataByTable;
         importCount = 0;
         Set<Long> exportProcesses = archive
               .getExportIndex().getProcesses(filter);
         List<Long> processes = new ArrayList<Long>();
         for (Long oid : exportProcesses)
         {
            ProcessInstanceBean existing = (ProcessInstanceBean) session.findByOID(
                        ProcessInstanceBean.class, oid);
            // this process already exists, do not import it again
            if (existing != null)
            {
               continue;
            }
            processes.add(oid);
         }
         
         if (processes.size() > 0)
         {
            dataByTable = ExportImportSupport.getDataByTable(archive.getData(processes));
            importCount = ExportImportSupport.importProcessInstances(dataByTable,
                  session, oidResolver);
         }
            
         // create the export process ids, unless we are importing a dump
         if (archive.getExportIndex().getDumpLocation() == null)
         {
            for (Long oid : processes)
            {
               ProcessInstanceBean instance = ProcessInstanceBean.findByOID(oid);
               instance.createProperty(
                     ProcessElementExporter.EXPORT_PROCESS_ID, archive.getExportIndex().getUuid(oid));
            }
         }
         if (documentOption != DocumentOption.NONE)
         {
            for (Long oid : exportProcesses)
            {
               addDocumentsToProcess(session, sf.getDocumentManagementService(), oid);
            }
         }
      }
      catch (IllegalStateException e)
      {
         importCount = 0;
         LOGGER.error(e.getMessage(), e);
      }
      catch (Exception e)
      {
         importCount = 0;
         LOGGER.error("Failed to import processes from input provided", e);
      }
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Imported " + importCount + " process instances");
      }
      return importCount;

   }
   

   private void setDocumentProperties(ImportDocument documentMetaData, DmsDocumentBean document)
   {
      DmsDocumentBean temp = new DmsDocumentBean(documentMetaData.getVfsResource());
      document.setDocumentAnnotations(temp.getDocumentAnnotations());
      document.setProperties(temp.getProperties());
      document.setOwner(temp.getOwner());
      document.setContentType(temp.getContentType());
      document.setDocumentType(temp.getDocumentType());
      document.setDescription(temp.getDescription());
      document.setEncoding(temp.getEncoding());
      document.setRevisionComment(temp.getRevisionComment());
      document.setRevisionName(temp.getRevisionName());
      document.setSize(temp.getSize());
      document.setVersionLabels(temp.getVersionLabels());
      document.setPath(temp.getPath());
   }
   
   private void addDocumentsToProcess(Session session, DocumentManagementService dms, Long piOid)
   {
     
      Map<Document, String> attachments = ExportImportSupport.fetchProcessAttachments(piOid);
      if (attachments != null)
      {
         ProcessInstanceBean pi = ProcessInstanceBean.findByOID(piOid);;
         for (Document doc : attachments.keySet())
         {
            DmsDocumentBean document = (DmsDocumentBean)doc;
            String docName = ExportImportSupport.getDocumentNameInArchive(piOid, doc);
            ImportDocument documentMetaData =  archive.getDocumentProperties(docName);
            byte[] content = archive.getDocumentContent(docName);
            if (content != null)
            {
               Document existingDoc = dms.getDocument(document.getId());
               if (existingDoc == null)
               {
                  String latestRevComment = document.getRevisionComment();
                  String latestRevName = document.getRevisionName();
                  if (documentOption == DocumentOption.ALL && CollectionUtils.isNotEmpty(documentMetaData.getRevisions()))
                  {
                     boolean firstDoc = true;
                     for (String revision : documentMetaData.getRevisions())
                     {
                        String revisionName = ExportImportSupport.getDocumentNameInArchive(docName, revision);
                        byte[] prevContent = archive.getDocumentContent(revisionName);

                        if (prevContent != null)
                        {
                           ImportDocument props = archive.getDocumentProperties(revisionName);
                           setDocumentProperties(props, document);
                           String revisionComment = document.getRevisionComment();
                           if (firstDoc)
                           {
                              document = (DmsDocumentBean) dms.createDocument(getFolderName(dms, document), document, prevContent,
                                    document.getEncoding());
                              document = (DmsDocumentBean) dms.versionDocument(document.getId(),
                                    revisionComment, revision);
                              firstDoc = false;
                           }
                           else
                           {
                              document = (DmsDocumentBean) dms.updateDocument(document, prevContent, document.getEncoding(), true, revisionComment, revision, false);
                           }
                        }
                        RepositoryAuditTrailUtils.storeImportDocument(document);
                     }
                     setDocumentProperties(documentMetaData, document);
                     document = (DmsDocumentBean) dms.updateDocument(document, content, document.getEncoding(), true, latestRevComment, latestRevName, false);
                     RepositoryAuditTrailUtils.storeImportDocument(document);
                     ExportImportSupport.updateAttachment(session, pi, document, documentMetaData.getDataId());
                  } 
                  else // create the one and only version
                  {
                     setDocumentProperties(documentMetaData, document);
                     document = (DmsDocumentBean)dms.createDocument(getFolderName(dms, document), document, content, document.getEncoding());
                     if (!"UNVERSIONED".equals(latestRevName))
                     {
                        document =(DmsDocumentBean)dms.versionDocument(document.getId(), latestRevComment, latestRevName);
                     }
                     RepositoryAuditTrailUtils.storeImportDocument(document);
                     ExportImportSupport.updateAttachment(session, pi, document, documentMetaData.getDataId());
                  }
               }
            }
            else
            {
               LOGGER.warn("Document " + docName + " not found in archive");
            }
         }
      }
   }
   
   private String getFolderName(DocumentManagementService dms, Document document)
   {
      String path = document.getPath().substring(0, document.getPath().length() - document.getName().length() - 1);
      DmsUtils.ensureFolderHierarchyExists(path, dms);
      return path;
   }

   private void validate(ServiceFactory sf)
   {
      importMetaData = new ImportMetaData();
      try
      {
         ExportModel exportModel = archive.getExportModel();
         ExportImportSupport.validateModel(sf.getQueryService(), exportModel, 
               importMetaData, archive.getExportIndex().getVersion());
      }
      catch (Exception e)
      {
         LOGGER.error(e.getMessage(), e);
         importMetaData.setErrorMessage("Failed to import processes from input provided. " + e.getMessage());
      }
   }


   /**
    * @author jsaayman
    */
   protected static enum Operation
   {
      /**
       * Validate if import environment contains compatible model and partition. Imports
       * process instances.
       */
      VALIDATE_AND_IMPORT,
      /**
       * Validate if import environment contains compatible model and partition.
       */
      VALIDATE,
      /**
       * Imports process instances
       */
      IMPORT,
      /**
       * Find archives to load
       */
      QUERY;
   };

   public static class ImportMetaData implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private final HashMap<Class, Map<Long, Long>> classToRuntimeOidMap;
      
      private String errorMessage;

      public ImportMetaData()
      {
         classToRuntimeOidMap = new HashMap<Class, Map<Long, Long>>();
      }

      public void addMappingForClass(Class type, Long exportId, Long importId)
      {
         Map<Long, Long> idMap = classToRuntimeOidMap.get(type);
         if (idMap == null)
         {
            idMap = new HashMap<Long, Long>();
            classToRuntimeOidMap.put(type, idMap);
         }
         idMap.put(exportId, importId);
      }

      public Long getImportId(Class type, Long exportId)
      {
         return classToRuntimeOidMap.get(type).get(exportId);
      }

      public String getErrorMessage()
      {
         return errorMessage;
      }

      public void setErrorMessage(String errorMessage)
      {
         this.errorMessage = errorMessage;
      }
      
   }
}
