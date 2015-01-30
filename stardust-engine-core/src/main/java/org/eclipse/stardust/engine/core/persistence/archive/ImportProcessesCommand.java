package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * <li/>completely li/>by root process instance OID <li/>by business
 * identifier (unique primitive key descriptor) <li/>by from/to filter (start time to termination time) <br/>
 * 
 * If processInstanceOids is null everything will be imported. If processInstanceOids is null nothing will be imported
 * 
 * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
 * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
 * and toDate is provided then all processes will be imported.
 * 
 * @author Jolene.Saayman
 * @version $Revision: $
 */
public class ImportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private static final Logger LOGGER = LogManager
         .getLogger(ImportProcessesCommand.class);

   private final byte[] rawData;

   private Date fromDate;

   private Date toDate;

   private final List<Long> processInstanceOids;
   
   /**
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format
    */
   public ImportProcessesCommand(byte[] rawData)
   {
      super();
      this.processInstanceOids = null;
      this.rawData = rawData;
      this.fromDate = null;
      this.toDate = null;
   }

   /**
    * If processInstanceOids is null everything will be imported. If processInstanceOids is null nothing will be imported
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format
    * @param processInstanceOids
    *           Oids of process instances to import. 
    */
   public ImportProcessesCommand(byte[] rawData, List<Long> processInstanceOids)
   {
      super();
      this.processInstanceOids = processInstanceOids;
      this.rawData = rawData;
      this.fromDate = null;
      this.toDate = null;
   }
   
   /**
    * If a fromDate is provided, but no toDate then toDate defaults to now. If a toDate is
    * provided, but no fromDate then fromDate defaults to 1 January 1970. If a null fromDate
    * and toDate is provided then all processes will be imported.
    * @param rawData
    *           This contains the data that needs to be imported in byte[] format 
    * @param fromDate includes processes with a start time greator or equal to fromDate
    * @param toDate includes processes with a termination time less or equal than toDate
    */
   public ImportProcessesCommand(byte[] rawData, Date fromDate, Date toDate)
   {
      super();
      this.processInstanceOids = null;
      this.rawData = rawData;
      this.fromDate = fromDate;
      this.toDate = toDate;
   }
   
   @Override
   public Serializable execute(ServiceFactory sf)
   {
      int importCount;
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("START Import");
      }
      if (rawData != null)
      {
         validateDates();
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
            Map<Class, Map<Long, Long>> keyToRuntimeOidMap = new HashMap<Class, Map<Long,Long>>();
            Map<String, byte[]> data = ExportImportSupport.validateModel(rawData, keyToRuntimeOidMap);
            ImportOidResolver oidResolver = new ImportOidResolver(keyToRuntimeOidMap);
            importCount = ExportImportSupport.importProcessInstances(data, session, filter, oidResolver);
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
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Imported " + importCount + " process instances.");
         }
      }
      else
      {
         if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received no data to import.");
         }
         importCount = 0;
      }
      if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("END Import");
      }
      return importCount;
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
            throw new IllegalArgumentException("Import from date can not be before export to date");
         }
      }
   }
}
