package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

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

   public ImportProcessesCommand(Operation operation, IArchive archive, 
         ArchiveFilter filter, ImportMetaData importMetaData, Map<String, String> preferences)
   {
      super();
      this.operation = operation;
      this.filter = filter;
      this.archive = archive;
      this.importMetaData = importMetaData;
      this.preferences = preferences;
   }

   /**
    * Use this constructor to determine which archives to load
    * 
    * @param processOids
    */
   public ImportProcessesCommand(ArchiveFilter filter, Map<String, String> preferences)
   {
      this(Operation.QUERY, null, filter, null, preferences);
   }
   
   public ImportProcessesCommand(Operation operation, Map<String, String> preferences)
   {
      this(operation, null, null, null, preferences);
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      if (ArchiveManagerFactory.getArchiveManager(preferences) == null)
      {
         throw new IllegalStateException("A valid Archive Manager could not be found or created");
      }
      Serializable result;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("START Import Operation " + operation.name() + " for " + ArchiveManagerFactory.getArchiveManager(preferences).getArchiveManagerId());
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
         case REMOVE_MANAGER:
            ArchiveManagerFactory.removeArchiveManager(preferences);
            result = true;
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
      IArchiveManager archiveManager = ArchiveManagerFactory.getArchiveManager(preferences);
      ArrayList<IArchive> archives = archiveManager.findArchives(filter);
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

   private void validate(ServiceFactory sf)
   {
      importMetaData = new ImportMetaData();
      try
      {
         ExportModel exportModel = archive.getExportModel();
         ExportImportSupport.validateModel(sf.getQueryService(), exportModel, importMetaData);
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
   public static enum Operation
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
      QUERY, 
      /**
       * In case custom preferences was used we may want to remove the custom
       * archivemanger created for this purpose
       */
      REMOVE_MANAGER;
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
