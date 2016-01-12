package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.List;
import java.util.Map;

import javax.jms.ObjectMessage;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand.ExportMetaData;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;

/**
 * This service provides methods to archive or backup processes, and import them again
 * @author jsaayman
 *
 */
public class ArchivingService
{
   private final ServiceFactory serviceFactory;

   public ArchivingService(final ServiceFactory serviceFactory)
   {
      this.serviceFactory = serviceFactory;
   }

   /**
    * Finds processes using the filter, and writes them to an archive
    * The processes will be purged.
    * @param filter
    * @param documentOption
    * @return
    */
   public Boolean archive(final ArchiveFilter filter, final DocumentOption documentOption)
   {
      WorkflowService workflowService = serviceFactory
            .getWorkflowService();
      ExportResult exportResult = (ExportResult) workflowService.execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null, documentOption));
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, null, documentOption);
      Boolean success = (Boolean) workflowService.execute(command);
      return success;
   }
   /**
    * Finds processes using the filter, and dumps them to an archive in the specified location.
    * The processes will not be purged.
    * @param filter
    * @param documentOption
    * @param backupLocation
    * @return
    */
   public Boolean backup(final ArchiveFilter filter, final DocumentOption documentOption, final String backupLocation)
   {
      WorkflowService workflowService = serviceFactory
            .getWorkflowService();
      ExportResult exportResult = (ExportResult) workflowService.execute(new ExportProcessesCommand(
                  ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter,
                  null, documentOption));
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.ARCHIVE, exportResult, backupLocation, documentOption);
      Boolean success = (Boolean) workflowService.execute(command);
      return success;
   }
   
   /**
    * Find processes to export using the filter and returns them in ExportMetaData object.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param filter
    * @param backupLocation 
    * @param documentOption
    * @return
    */
   public ExportMetaData findProcessesToExport(final ArchiveFilter filter,
         final String backupLocation, final DocumentOption documentOption)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY, filter, backupLocation, documentOption);
      ExportMetaData exportMetaData = (ExportMetaData) serviceFactory
            .getWorkflowService().execute(command);
      return exportMetaData;
   }

   /**
    * Find processes to export using the filter, find all their export data (models and processes) and return it as an ExportResult. This method will not write the archive.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param filter
    * @param backupLocation
    * @param documentOption
    * @return
    */
   public ExportResult getExportResult(
         final ArchiveFilter filter, final String backupLocation, final DocumentOption documentOption)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.QUERY_AND_EXPORT, filter, backupLocation, documentOption);
      ExportResult exportResult = (ExportResult) serviceFactory.getWorkflowService()
            .execute(command);
      return exportResult;
   }
   
   /**
    * Find all the model export data for the processes in the ExportMetaData and return it as an ExportResult. This method will not write the archive.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param exportMetaData
    * @param backupLocation
    * @param documentOption
    * @return
    */
   public ExportResult getExportResultForModels(
         final ExportMetaData exportMetaData, final String backupLocation, final DocumentOption documentOption)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.EXPORT_MODEL, exportMetaData, backupLocation, documentOption);
      ExportResult exportResult = (ExportResult) serviceFactory.getWorkflowService()
            .execute(command);
      return exportResult;
   }
   
   /**
    * Find all the process export data for the processes in the ExportMetaData and return it as an ExportResult. This method will not write the archive.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param exportMetaData
    * @param backupLocation
    * @param documentOption
    * @return
    */
   public ExportResult getExportResultForProcesses(final ExportMetaData exportMetaData,
         final String backupLocation, final DocumentOption documentOption)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(
            ExportProcessesCommand.Operation.EXPORT_BATCH, exportMetaData, backupLocation,
            documentOption);
      ExportResult result = (ExportResult) serviceFactory.getWorkflowService().execute(
            command);
      return result;
   }
   
   /**
    * Writes the ExportResult to an archive
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param exportResult
    * @param backupLocation
    * @param documentOption
    * @return
    */
   public boolean archiveExportResults(final ExportResult exportResult, final String backupLocation, final DocumentOption documentOption)
   {
      ExportProcessesCommand command = new ExportProcessesCommand(ExportProcessesCommand.Operation.ARCHIVE, exportResult, backupLocation, documentOption);
      Boolean success = (Boolean) serviceFactory.getWorkflowService().execute(command);
      return success;
   }
   
   /**
    * This method finds all the archives the contains processes filtered by the ArchiveFilter provided.
    * This method does not import the data in the archives.
    * @param filter
    * @param preferences
    * @return
    */
   public List<IArchive> findArchives(final ArchiveFilter filter, final Map<String, String> preferences)
   {
      return (List<IArchive>)serviceFactory.getWorkflowService().execute(new ImportProcessesCommand(filter, preferences));
   }
   
   /**
    * This method validates that the provided archive can be imported in the current environment. If not an error message will be set on ImportMetaData returned.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param archive
    * @param filter
    * @param preferences
    * @param documentOption
    * @return
    */
   public ImportMetaData validate(final IArchive archive, final ArchiveFilter filter, Map<String, String> preferences, final DocumentOption documentOption)
   {
      ImportMetaData importMetaData = (ImportMetaData) serviceFactory.getWorkflowService().execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE, archive, filter, null, null, documentOption));
      return importMetaData;
   }
   
   /**
    * This method imports the archive provided. ImportMetaData must be provided, i.e. the archive should have been validated before.
    * Use this method if you want to write custom controller logic handling threading and batching of archiving operations 
    * @param archive
    * @param filter
    * @param importMetaData
    * @param preferences
    * @param documentOption
    * @return
    */
   public int importData(final IArchive archive, final ArchiveFilter filter, final ImportMetaData importMetaData, Map<String, String> preferences, final DocumentOption documentOption)
   {
      int count =  (Integer)  serviceFactory.getWorkflowService().execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.IMPORT, archive, filter, importMetaData, preferences, documentOption));
      return count;
   }
   
  /**
   * This method validates the provided archive and imports the data in it that is filtered by the ArchiveFilter provided
   * @param archive
   * @param filter
   * @param documentOption
   * @return
   */
   public int validateAndImport(final IArchive archive, final ArchiveFilter filter, final DocumentOption documentOption)
   {
      int count = (Integer) serviceFactory.getWorkflowService().execute(new ImportProcessesCommand(
            ImportProcessesCommand.Operation.VALIDATE_AND_IMPORT, archive, filter, null, null, documentOption));
      return count;
   }
   
   /**
    * 
    * Use this method if you want to write custom logic to archive a message containing an ExportResult 
    * @param message
    */
   public void archiveMessages(ObjectMessage message)
   {
      final ExportProcessesCommand command = new ExportProcessesCommand((ObjectMessage) message);
      serviceFactory.getWorkflowService().execute(command);
   }
}
