package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCES;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_PROPERTIES;
import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;
import org.eclipse.stardust.engine.extensions.camel.component.exception.MissingEndpointException;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.data.KeyValueList;
import org.eclipse.stardust.engine.extensions.camel.util.search.ActivityInstanceSearch;

/**
 * 
 * @author JanHendrik.Scheufen
 */
public class ProcessProducer extends AbstractIppProducer
{
   private static final String SPECIAL_CHARACTER_SET = "[\\\\/:*?\"<>|\\[\\]]";
   static Logger LOG = LogManager.getLogger(ProcessProducer.class);

   private ProcessEndpoint endpoint;

   public ProcessProducer(ProcessEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   /**
    * Processes the message exchange
    * 
    * @param exchange the message exchange
    * @throws Exception if an internal processing error has occurred.
    */
@SuppressWarnings("unchecked")
   public void process(Exchange exchange) throws Exception
   {
      if (getServiceFactory(this.endpoint, exchange) == null)
      {
         throw new MissingEndpointException("Authentication Endpoint is missing. You have to specify one.");
      }
      ServiceFactory sf = getServiceFactory(this.endpoint, exchange);
      WorkflowService wfService = sf.getWorkflowService();

      // *** FIND PROCESSES ***
      if (SubCommand.Process.COMMAND_FIND.equals(endpoint.getSubCommand()))
      {
         ProcessInstances result = findProcesses(exchange, sf);
         exchange.getIn().setHeader(PROCESS_INSTANCES, result);
      }
      // *** START PROCESS ***
      else if (SubCommand.Process.COMMAND_START.equals(endpoint.getSubCommand()))
      {
         // Find the process ID
         String processId = endpoint.evaluateProcessId(exchange, true);
         String modelId = endpoint.evaluateModelId(exchange, true);
         String fullyQualifiedName=null;
         if (!StringUtils.isEmpty(modelId) && !StringUtils.isEmpty(processId))
         {
            fullyQualifiedName = "{" + modelId + "}" + processId;
         }
         else
         {
            if (!StringUtils.isEmpty(processId))
            {
               fullyQualifiedName = processId;
            }
         }

         // Determine data for start process
         Map<String, Object> data = (Map<String, Object>) endpoint.evaluateData(exchange);

         ProcessInstance pi = wfService.startProcess(fullyQualifiedName, data, endpoint.isSynchronousMode());

         if (exchange.getProperty(ATTACHMENT_FILE_CONTENT) != null
               || exchange.getIn().getHeader(ATTACHMENT_FILE_CONTENT) != null)
         {

            DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());
            String path = processId;
            String jcrDocumentContent = endpoint.evaluateAttachementContent(exchange);
            dmsFileArchiver.setRootFolderPath("/");
            String documents = "documents";
            Document newDocument = dmsFileArchiver.archiveFile(jcrDocumentContent.getBytes(), path, documents);

            List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);

            // initialize it if necessary
            if (null == attachments)
            {
               attachments = new ArrayList<Document>();
            }
            // add the new document
            attachments.add(newDocument);

            // update the attachments
            wfService.setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, attachments);
         }

         // Start a process instance

         // manipulate exchange
         if (exchange.getPattern().equals(ExchangePattern.OutOnly))
            exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
         else
            exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());

      }
      // *** ATTACH DOCUMENT ***
      else if (SubCommand.Process.COMMAND_ATTACH.equals(endpoint.getSubCommand()))
      {
         Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
         ProcessInstance pi = wfService.getProcessInstance(processInstanceOid);
         String fileName = endpoint.evaluateFileName(exchange, true);
         String folderName = endpoint.evaluateFolderName(exchange, true);
         if (processInstanceOid == null)
         {
            LOG.error("No process instance OID found");
            return;
         }

         if (fileName == null)
         {
            LOG.error("No file name provided");
            return;
         }

         if (folderName == null)
         {
            folderName=DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
            LOG.debug("No folder name provided, default location set to "+folderName);
            folderName=folderName.substring(1);
         }

         String data = endpoint.evaluateContent(exchange);
         
         if (data != null && pi != null && !pi.getState().equals(ProcessInstanceState.Completed)
               && !pi.getState().equals(ProcessInstanceState.Interrupted))
         {
            DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());
            
            String jcrDocumentContent = data;
            //dmsFileArchiver.setRootFolderPath("/");
            
            Document newDocument = dmsFileArchiver.archiveFile(jcrDocumentContent.getBytes(), fileName, folderName);
            List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);

            // initialize it if necessary
            if (null == attachments)
            {
               attachments = new ArrayList<Document>();
            }
            // add the new document
            attachments.add(newDocument);

            // update the attachments
            wfService.setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, attachments);
         }
      }
      // *** CONTINUE ***
      else if (SubCommand.Process.COMMAND_CONTINUE.equals(endpoint.getSubCommand()))
      {
         // Find the process instance context
         Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);

         // Find waiting AIs
         QueryService qService = getServiceFactory(this.endpoint, exchange).getQueryService();
         ActivityInstances result = ActivityInstanceSearch.findWaitingForProcessInstance(qService, processInstanceOid);
         if (result.size() == 0)
         {
            // TODO implement a "strict" mode to signal that at least one AI
            // must have
            // been found and no result should throw an exception
            LOG.warn("No waiting activity instance found for process instance OID: " + processInstanceOid);
            return;
         }
         // Determine dataOutput
         Map<String, ? > dataOutput = endpoint.evaluateDataOutput(exchange);
         // Complete AIs
         // WorkflowService wfService =
         // ClientEnvironment.getCurrentServiceFactory().getWorkflowService();
         for (ActivityInstance ai : (List<ActivityInstance>) result)
         {
            wfService.activateAndComplete(ai.getOID(), null, dataOutput);
         }
         // TODO set a list of the activity OIDs in the message header to
         // provide context
         // ... can be reused for activity:search
      }
      // *** SET PROPERTIES ***
      else if (SubCommand.Process.COMMAND_SET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         // Find the process instance context
         Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
         // which properties?
         Map<String, ? > properties = endpoint.evaluateProperties(exchange, true);
         // update
         // WorkflowService wfService =
         // ClientEnvironment.getCurrentServiceFactory().getWorkflowService();
         wfService.setOutDataPaths(processInstanceOid, properties);
      }
      // *** GET PROPERTIES ***
      else if (SubCommand.Process.COMMAND_GET_PROPERTIES.equals(endpoint.getSubCommand()))
      {
         // Find the process instance context
         Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
         // which properties?
         Set<String> propertyIds;
         // WorkflowService wfService =
         // ClientEnvironment.getCurrentServiceFactory().getWorkflowService();
         if (StringUtils.isNotEmpty(endpoint.getProperties()))
         {
            propertyIds = new HashSet<String>(Arrays.asList(endpoint.getProperties().split(",")));
         }
         else
         {
            // find all process IN data paths
            ProcessInstance pi = wfService.getProcessInstance(processInstanceOid);
            QueryService qService = ClientEnvironment.getCurrentServiceFactory().getQueryService();
            ProcessDefinition pDef = qService.getProcessDefinition(pi.getProcessID());
            List<String> dataPathIds = new ArrayList<String>();
            for (DataPath dp : (List<DataPath>) pDef.getAllDataPaths())
            {
               if (Direction.IN.equals(dp.getDirection()))
                  dataPathIds.add(dp.getId());
            }
            propertyIds = new HashSet<String>(dataPathIds);
         }
         // retrieve properties
         LOG.info("Retrieving properties for process instance OID <" + processInstanceOid + ">");
         Map<String, Serializable> processProperties = wfService.getInDataPaths(processInstanceOid, propertyIds);
         // manipulate exchange
         if (exchange.getPattern().equals(ExchangePattern.OutOnly))
            exchange.getOut().setHeader(PROCESS_INSTANCE_PROPERTIES, processProperties);
         else
            exchange.getIn().setHeader(PROCESS_INSTANCE_PROPERTIES, processProperties);
      }else if (SubCommand.Process.COMMAND_SPAWN_SUB_PROCESS.equals(endpoint.getSubCommand()))
      {
         String processId = endpoint.evaluateProcessId(exchange, true);
         String modelId = endpoint.evaluateModelId(exchange, false);
         Long refPiOid = endpoint.evaluateParentProcessInstanceOid(exchange, false);
         if (refPiOid == null)
         {

            refPiOid = endpoint.evaluateProcessInstanceOid(exchange, false);
            if (refPiOid == null)
               throw new IllegalStateException("Missing required parent OID.");
         }
         String fullyQualifiedName = processId;
         if (StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(processId))
         {
            fullyQualifiedName = "{" + modelId + "}" + processId;
         }
         

         Boolean copyData = endpoint.evaluateCopyData(exchange, false);
         if (copyData == null)
            copyData = true;
         // Determine data for start process
         Map<String, Object> data = null;
         if (!copyData)
         {
            data = (Map<String, Object>) endpoint.evaluateData(exchange);
         }
         ProcessInstance pi = wfService.spawnSubprocessInstance(refPiOid, fullyQualifiedName, copyData, data);

         // manipulate exchange
         if (exchange.getPattern().equals(ExchangePattern.OutOnly))
            exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
         else
            exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
      }
   }

   private ProcessInstances findProcesses(Exchange exchange, ServiceFactory sf)
     {
         ProcessInstanceQuery piQuery;
           // Look for search parameters
           String processId = endpoint.evaluateProcessId(exchange, false);
           Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, false);
           List<ProcessInstanceState> piStates = endpoint.getProcessInstanceStates();
           Map<String,Serializable> dataFilters = endpoint.evaluateDataFilters(exchange, false);
           // possible search combinations
           // state
           // states
           // processId
           // processId, state
           // processId, states
           // ...
   
           // Apply states
           if( null != piStates && piStates.size() > 0 )
               piQuery = ProcessInstanceQuery.findInState(piStates.toArray(new ProcessInstanceState[]{}));
           else {
               piQuery = ProcessInstanceQuery.findAll();
           }
           // apply process filter
           if (null != processInstanceOid)
           {
              piQuery.where(new ProcessInstanceFilter(processInstanceOid));
              if (StringUtils.isNotEmpty(processId ))
                 LOG.warn("Found a process instance OID (" + processInstanceOid + ") and the search parameter "
                       + "processId (" + processId + ") will be ignored!");
           }
           else if( StringUtils.isNotEmpty(processId) ) {
               piQuery.where( new ProcessDefinitionFilter(processId,false) );
           }
           // apply data filters
           if( null != dataFilters ) {
                 for( String key : dataFilters.keySet() ) {
                     // detect structured data filter
                     int idx = key.indexOf(KeyValueList.STRUCT_PATH_DELIMITER);
                     if( idx != -1 ) {
                         String structId = key.substring(0, idx);
                         String structPath = key.substring(idx + KeyValueList.STRUCT_PATH_DELIMITER.length());
                         piQuery.where(DataFilter.isEqual(structId, structPath, dataFilters.get(key)));
                     }
                     else {
                         piQuery.where(DataFilter.isEqual(key, dataFilters.get(key)));
                     }
                 }
           }
   
           ProcessInstances result = sf.getQueryService().getAllProcessInstances(piQuery);
           //TODO implement result size matching logic to throw UnexpectedResultException
   
           return result;
     }
   
   
   /**
    * creates new folder based on the input parameters This function creates a single
    * folder on the provided path which must be valid
    * 
    * @param folderPath
    * @param folderName
    * @return
    */
   public static Folder createFolder(String folderPath, String folderName)
   {
      if (null != folderName)
      {
         // append
         folderPath = folderPath + "/" + folderName;
      }
      return createFolderIfNotExists(folderPath);
   }
   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   public static Folder createFolderIfNotExists(String folderPath)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath, Folder.LOD_NO_MEMBERS);
    
         if (null == folder)
         {
            // folder does not exist yet, create it
            String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
            String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

            if (StringUtils.isEmpty(parentPath))
            {
               // top-level reached
               return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
            }
            else
            {
               Folder parentFolder = createFolderIfNotExists(parentPath);
               return getDocumentManagementService().createFolder(parentFolder.getId(),
                     DmsUtils.createFolderInfo(childName));
            }
         }
         else
         {
            return folder;
         }
   }
   public static DocumentManagementService getDocumentManagementService()
   {
      return ClientEnvironment.getCurrentServiceFactory().getDocumentManagementService();
   }
   public static Folder getFolder(String path)
   {
      Folder folder = null;
      String searchString = substringAfterLast(path, "/");
      searchString = replaceIllegalXpathSearchChars(searchString);
      List<Folder> newlist = getDocumentManagementService().findFoldersByName(searchString, Folder.LOD_NO_MEMBERS);
      for (Folder tempFolder : newlist)
      {
         if (path.equalsIgnoreCase(tempFolder.getPath()))
         {
            folder = tempFolder;
            break;
         }
      }

      return folder;
   }
   /**
    * @param source
    * @param separator
    * @return
    */
   public static final String substringAfterLast(String source, String separator)
   {
      if (org.eclipse.stardust.common.StringUtils.isEmpty(source))
      {
         return source;
      }
      if (org.eclipse.stardust.common.StringUtils.isEmpty(separator))
      {
         return "";
      }
      int pos = source.lastIndexOf(separator);
      if (pos == -1 || pos == (source.length() - separator.length()))
      {
         return "";
      }
      return source.substring(pos + separator.length());
   }
   public static String replaceIllegalXpathSearchChars(String s)
   {
      return s.replaceAll("'", "%");
   }
}