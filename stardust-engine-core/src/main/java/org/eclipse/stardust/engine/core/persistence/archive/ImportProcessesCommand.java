package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

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

   private Date fromDate;

   private Date toDate;

   private final List<Long> processInstanceOids;

   private ImportMetaData importMetaData;

   private final Operation operation;

   private ImportProcessesCommand(Operation operation, IArchive archive,
         List<Long> processInstanceOids, Date fromDate, Date toDate,
         ImportMetaData importMetaData)
   {
      super();
      this.operation = operation;
      this.processInstanceOids = processInstanceOids;
      this.archive = archive;
      this.fromDate = fromDate;
      this.toDate = toDate;
      this.importMetaData = importMetaData;
   }

   /**
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format
    * @param importMetaData
    *           provide importMetaData if you already validated the model, else set it as
    *           null
    */
   public ImportProcessesCommand(Operation operation, IArchive archive,
         ImportMetaData importMetaData)
   {
      this(operation, archive, null, null, null, importMetaData);
   }

   /**
    * If processInstanceOids is null everything will be imported. If processInstanceOids
    * is null nothing will be imported If processInstanceOIDs and ModelOIDs are provided
    * we perform AND logic between the processInstanceOIDs and ModelOIDs provided.
    * 
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format
    * @param processInstanceOids
    *           Oids of process instances to import.
    * @param importMetaData
    *           provide importMetaData if you already validated the model, else set it as
    *           null
    */
   public ImportProcessesCommand(Operation operation, IArchive archive,
         List<Long> processInstanceOids, ImportMetaData importMetaData)
   {
      this(operation, archive, processInstanceOids, null, null, importMetaData);
   }

   /**
    * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
    * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null
    * fromDate and toDate is provided then all processes will be imported.
    * 
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format
    * @param fromDate
    *           includes processes with a start time greator or equal to fromDate
    * @param toDate
    *           includes processes with a termination time less or equal than toDate
    * @param importMetaData
    *           provide importMetaData if you already validated the model, else set it as
    *           null
    */
   public ImportProcessesCommand(Operation operation, IArchive archive, Date fromDate,
         Date toDate, ImportMetaData importMetaData)
   {
      this(operation, archive, null, fromDate, toDate, importMetaData);
   }

   /**
    * Use this constructor to determine which archives to load
    * 
    * @param processOids
    */
   public ImportProcessesCommand(List<Long> processInstanceOids)
   {
      this(Operation.QUERY, null, processInstanceOids, null, null, null);
   }

   /**
    * Use this constructor to determine which archives to load
    * 
    * @param operation
    * @param fromDate2
    * @param toDate2
    */
   public ImportProcessesCommand(Date fromDate, Date toDate)
   {
      this(Operation.QUERY, null, null, fromDate, toDate, null);
   }

   /**
    * Use this constructor to determine which archives to load
    */
   public ImportProcessesCommand()
   {
      this(Operation.QUERY, null, null, null, null, null);
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      Serializable result;
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("START Import Operation " + operation.name());
      }

      if (archive != null)
      {
         switch (operation)
         {
            case VALIDATE:
               validate(sf);
               result = importMetaData;
               break;
            case IMPORT:
               result = importData(sf, null);
               break;
            case VALIDATE_AND_IMPORT:
               result = validateAndImport(sf);
               break;
            default:
               throw new IllegalArgumentException("No valid operation provided");
         }
         ;
      }
      else
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Received no data to import.");
         }
         result = 0;
      }
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("END Import " + operation.name());
      }
      return result;
   }

   private int validateAndImport(ServiceFactory sf)
   {
      if (importMetaData != null)
      {
         throw new IllegalArgumentException(
               "When using VALIDATE_AND_IMPORT, provide the model data and the export data. Do not provide importMetaData");
      }
      Map<String, List<byte[]>> dataByTable = validate(sf);
      return importData(sf, dataByTable);
   }

   private int importData(ServiceFactory sf, Map<String, List<byte[]>> dataByTable)
   {
      int importCount;
      validateDates();
      ImportOidResolver oidResolver = new ImportOidResolver(importMetaData);
      final Session session = (Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      ImportFilter filter;
      if (processInstanceOids != null)
      {
         filter = new ImportFilter(processInstanceOids);
      }
      else if (fromDate != null && toDate != null)
      {
         filter = new ImportFilter(fromDate, toDate);
      }
      else
      {
         filter = new ImportFilter();
      }
      try
      {
         if (CollectionUtils.isEmpty(dataByTable))
         {
            dataByTable = ExportImportSupport.getDataByTable(archive.getData());
         }
         importCount = ExportImportSupport.importProcessInstances(dataByTable, session,
               filter, oidResolver);
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

   private Map<String, List<byte[]>> validate(ServiceFactory sf)
   {
      importMetaData = new ImportMetaData();
      Map<String, List<byte[]>> dataByTable;
      try
      {
         byte[] data;
         if (archive.getModelData() == null)
         {
            data = archive.getData();
         }
         else
         {
            data = archive.getModelData();
         }
         dataByTable = ExportImportSupport.validateModel(data,
               importMetaData);
      }
      catch (IllegalStateException e)
      {
         dataByTable = null;
         LOGGER.error(e.getMessage(), e);
      }
      catch (Exception e)
      {
         dataByTable = null;
         LOGGER.error("Failed to import processes from input provided", e);
      }
      return dataByTable;
   }

   private void validateDates()
   {
      if (fromDate != null || toDate != null)
      {
         if (fromDate == null)
         {
            this.fromDate = new Date(0);
         }
         if (toDate == null)
         {
            this.toDate = TimestampProviderUtils.getTimeStamp();
         }
         if (toDate.before(fromDate))
         {
            throw new IllegalArgumentException(
                  "Import from date can not be before import to date");
         }
         this.fromDate = ExportImportSupport.getStartOfDay(fromDate);
         this.toDate = ExportImportSupport.getEndOfDay(toDate);
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
      QUERY;
   };

   public static class ImportMetaData implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private final HashMap<Class, Map<Long, Long>> classToRuntimeOidMap;

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
   }
}
