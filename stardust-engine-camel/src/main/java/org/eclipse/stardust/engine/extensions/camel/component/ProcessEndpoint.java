package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.parseSimpleExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.language.simple.SimpleLanguage;
import org.apache.camel.model.language.ConstantExpression;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;

/**
 * <h2>Commands:</h2>
 * <ul>
 * <li>start</li>
 * <br/>
 * Starts a new process instance. <br/>
 * When using the ExchangePattern OutOnly, no message properties are preserved and the
 * ippProcessInstanceOid is the only piece of information returned in the exchange.
 * Otherwise the ippProcessInstanceOid is set in the header of the original IN message. <br/>
 * Supported parameters:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Required</th>
 * <th>Description</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td>processId</td>
 * <td>Yes</td>
 * <td>Specifies the ID of the process to start. If not given as parameter, the message
 * header "ippProcessId" must be set.</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>data</td>
 * <td>No</td>
 * <td>The start data to be passed to the process instance</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>dataMap</td>
 * <td>No</td>
 * <td>The start data to be passed to the process instance as Map<String,?></td>
 * <td></td>
 * </tr>
 * </table>
 * <li>continue</li>
 * <br/>
 * Tries to find any hibernated or suspended activities for the current process instance
 * context and completes them. <br/>
 * Supported parameters:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Required</th>
 * <th>Description</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td>processInstanceOid</td>
 * <td>Yes</td>
 * <td>Specifies the OID of the process instance. If not given as parameter, the message
 * header "ippProcessInstanceOid" must be set</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>dataOutput</td>
 * <td>No</td>
 * <td>The data to be passed to the activity upon completion</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>dataOutputMap</td>
 * <td>No</td>
 * <td>The data to be passed to the activity upon completion</td>
 * <td></td>
 * </tr>
 * </table>
 * <li>set</li>
 * <br/>
 * Sets the properties of the current process instance. <br/>
 * Supported parameters:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Required</th>
 * <th>Description</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td>processInstanceOid</td>
 * <td>Yes</td>
 * <td>Specifies the OID of the process instance. If not given as parameter, the message
 * header "ippProcessInstanceOid" must be set</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>properties</td>
 * <td>No</td>
 * <td>The properties in String format</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>propertiesMap</td>
 * <td>No</td>
 * <td>The properties as Map</td>
 * <td></td>
 * </tr>
 * </table>
 * </ul>
 * 
 * @author JanHendrik.Scheufen
 */
public class ProcessEndpoint extends AbstractIppEndpoint
{

   private static final transient Logger logger = LogManager.getLogger(ProcessEndpoint.class);

   protected Expression processId;
   private Expression modelId;
   protected Expression processInstanceOid;
   protected Expression ippAttachmentFileName;
   protected Expression ippAttachmentFolderName;
   // TODO since the data/dataMap, dataOutput/dataOutputMap, properties/propertiesMap
   // fields are mutually
   // exclusive per subcommand, they could be represented by a single generic field
   // checks in setter methods needed in order not to overwrite if someone illegaly
   // specified
   // mutually excluding options.
   private String ippAttachmentFileContent;
   protected String data;
   protected Expression dataMap;
   protected String dataOutput;
   protected Expression dataOutputMap;
   protected String properties;
   protected Expression propertiesMap;
   protected boolean synchronousMode = true;
   protected String state;
   protected String states;
   protected List<ProcessInstanceState> processInstanceStates;
   protected Expression parentProcessInstanceOid;
   protected Expression spawnProcessID;
   protected Expression copyData;
   public ProcessEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }

   /**
    * Creates a new producer which is used send messages into the endpoint
    * 
    * @return a newly created producer
    * @throws Exception
    *            can be thrown
    */
   public Producer createProducer() throws Exception
   {
      if (SubCommand.Process.COMMAND_START.equals(this.subCommand) && (StringUtils.isNotEmpty(data) && dataMap != null))
      {
         throw new IllegalArgumentException("You cannot set both 'data' and 'dataMap' options.");
      }
      if (SubCommand.Process.COMMAND_ATTACH.equals(this.subCommand)
            && (StringUtils.isNotEmpty(data) && dataMap != null))
      {
         throw new IllegalArgumentException("You cannot set both 'data' and 'dataMap' options.");
      }
      if (SubCommand.Process.COMMAND_CONTINUE.equals(this.subCommand)
            && (StringUtils.isNotEmpty(dataOutput) && dataOutputMap != null))
      {
         throw new IllegalArgumentException("You cannot set both 'dataOutput' and 'dataOutputMap' options.");
      }
      if (SubCommand.Process.COMMAND_SET_PROPERTIES.equals(this.subCommand)
            && (StringUtils.isNotEmpty(properties) && propertiesMap != null))
      {
         throw new IllegalArgumentException("You cannot set both 'properties' and 'propertiesMap' options.");
      }
      return new ProcessProducer(this);
   }

   /**
    * Creates a new Event Driven Consumer which consumes messages from the endpoint using
    * the given processor
    * 
    * @param processor
    *           the given processor
    * @return a newly created consumer
    * @throws Exception
    *            can be thrown
    */
   public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

   /**
    * Evaluates process instance Oid value
    * 
    * @param exchange
    *           the camel exchange
    * @param strict
    *           (boolean)
    * @return the value of the ProcessInstanceOid attribute
    */
   public Long evaluateProcessInstanceOid(Exchange exchange, boolean strict)
   {
      if (null != this.processInstanceOid){
         logger.info("Simple Expression detected for Process Instance OID attribute, Expression evaluated to  < " + this.processInstanceOid.evaluate(exchange, Long.class)+">");
         return this.processInstanceOid.evaluate(exchange, Long.class);
      }else
      {
         Long piOid = exchange.getIn().getHeader(MessageProperty.PROCESS_INSTANCE_OID, Long.class);
         if (null == piOid && strict)
         {
            throw new IllegalStateException("Missing required process instance OID.");
         }
         logger.info("Process instance OID value will be retrieved from exchange header <"+MessageProperty.PROCESS_INSTANCE_OID+">, found <" + piOid+">");
         return piOid;
      }
   }

   /**
    * Evaluates attachment file name value
    * 
    * @param exchange
    * @param strict
    *           (boolean)
    * @return File Name
    */
   public String evaluateFileName(Exchange exchange, boolean strict)
   {

      if (null != this.ippAttachmentFileName)
      {
         logger.info("Simple Expression detected for File Name attribute, Expression evaluated to  < " + this.ippAttachmentFileName.evaluate(exchange, String.class)+">");
         return this.ippAttachmentFileName.evaluate(exchange, String.class);
      }
      else
      {
         String attachmentFileName = exchange.getProperty(MessageProperty.ATTACHMENT_FILE_NAME, String.class);
         logger.info("Attachment File Name value will be retrieved from exchange header <"+MessageProperty.ATTACHMENT_FILE_NAME+">, found <" + attachmentFileName+">");
         return attachmentFileName;
      }

   }

   /**
    * Evaluates attachment folder name value
    * 
    * @param exchange
    * @param strict
    *           (boolean)
    * @return Folder Name
    */
   public String evaluateFolderName(Exchange exchange, boolean strict)
   {

      if (null != this.ippAttachmentFolderName)
      {
         logger.info("Simple Expression detected for Folder Name attribute, Expression evaluated to  <" + this.ippAttachmentFolderName.evaluate(exchange, String.class)+">");
         return this.ippAttachmentFolderName.evaluate(exchange, String.class);
      }
      else
      {
         String attachmentFolderName = exchange.getProperty(MessageProperty.ATTACHMENT_FOLDER_NAME, String.class);
         //logger.info("Folder Name = " + attachmentFolderName);
         logger.info("Attachment Folder Name value will be retrieved from exchange header <"+MessageProperty.ATTACHMENT_FILE_NAME+">, found <" + attachmentFolderName+">");
         return attachmentFolderName;
      }
   }

   /**
    * Uses the Exchange to evaluate and return the process ID that was set either via URI
    * parameter in {@link #processId} or in the message header.
    * 
    * @param exchange
    *           the camel exchange
    * @param strict
    *           whether to perform strict evaluation
    * @return the process ID
    * @throws IllegalStateException
    *            if the process ID cannot be found and strict evaluation was requested
    */
   public String evaluateProcessId(Exchange exchange, boolean strict)
   {
      if (null != this.processId){
         logger.info("Simple Expression detected for ProcessId attribute, Expression evaluated to  <" + this.processId.evaluate(exchange, String.class)+">");
         return this.processId.evaluate(exchange, String.class);
      }else
      {
         String id = exchange.getIn().getHeader(MessageProperty.PROCESS_ID, String.class);
         if (StringUtils.isEmpty(id) && strict)
         {
            throw new IllegalStateException("Missing required process ID.");
         }
         logger.info("Process ID value will be retrieved from exchange header <"+MessageProperty.PROCESS_ID+">, found <" + id+">");
         return id;
      }
   }

   /**
    * Evaluates attachment file content
    * 
    * @param exchange
    * @return attachment file content
    */
   public String evaluateContent(Exchange exchange)
   {

      if (!StringUtils.isEmpty(this.ippAttachmentFileContent) && parseSimpleExpression(this.ippAttachmentFileContent)!=null)
      {
         Expression expr = SimpleLanguage.simple(this.ippAttachmentFileContent);
         logger.debug("Simple Expression detected for Attachment File Content attribute, Expression evaluated to  < " + expr.evaluate(exchange, String.class)+">");
         return expr.evaluate(exchange, String.class);
      }
      else 
      {
         // get from header property
         String exchangeBody=exchange.getProperty(CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT, String.class);
         logger.info("Attachment File Content value will be retrieved from exchange header <"+CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT+">, found <" + exchangeBody+">");
         return exchangeBody;
      }
   }

   /**
    * Creates a Map object from the specified data using the specified Exchange to replace
    * values declared with Camel's Simple language. Data keys specifying Structured Data
    * Types will be converted to Map types.
    * 
    * @param exchange
    * @return Map object
    */
   @SuppressWarnings("unchecked")
   public Map<String, ? > evaluateData(Exchange exchange)
   {
      if (null != this.dataMap)
      {
         logger.info("Simple Expression detected for DataMap attribute, Expression evaluated to  < " + this.dataMap.evaluate(exchange, Map.class)+">");
         return this.dataMap.evaluate(exchange, Map.class);
      }
      else if (StringUtils.isNotEmpty(this.data))
      {
         return CamelHelper.createStructuredDataMap(this.data, exchange);
      }
      return null;
   }

   /**
    * Returns a Map of DataOutputs
    * 
    * @param exchange
    * @return a Map of DataOutput
    */
   @SuppressWarnings("unchecked")
   public Map<String, ? > evaluateDataOutput(Exchange exchange)
   {
      if (null != this.dataOutputMap)
      {
         return this.dataOutputMap.evaluate(exchange, Map.class);
      }
      if (StringUtils.isNotEmpty(this.dataOutput))
      {
         return CamelHelper.createStructuredDataMap(this.dataOutput, exchange);
      }
      return null;
   }

   /**
    * Returns the value of the properties on the given exchange
    * 
    * @param exchange
    *           the message exchange on which to evaluate the properties
    * @param strict
    *           if true, throws exception if properties not found
    * @return the value of process instance properties as a map
    */
   @SuppressWarnings("unchecked")
   public Map<String, ? > evaluateProperties(Exchange exchange, boolean strict)
   {
      Map<String, ? > props = null;
      if (null != this.propertiesMap)
      {
         props = this.propertiesMap.evaluate(exchange, Map.class);
      }
      else if (StringUtils.isNotEmpty(this.properties))
      {
         props = CamelHelper.createStructuredDataMap(this.properties, exchange);
      }
      else
      {
         props = exchange.getIn().getHeader(MessageProperty.PROCESS_INSTANCE_PROPERTIES, Map.class);
      }
      if (null == props && strict)
      {
         throw new IllegalStateException("Missing required process instance properties.");
      }
      return props;
   }

   /**
    * @param exchange
    * @return the attachment content
    */
   public String evaluateAttachementContent(Exchange exchange)
   {
      if (exchange.getProperty(ATTACHMENT_FILE_CONTENT) != null)
      {
         return (String) exchange.getProperty(ATTACHMENT_FILE_CONTENT);
      }
      else
      {
         if (exchange.getIn().getHeader(ATTACHMENT_FILE_CONTENT) != null)
            return (String) exchange.getIn().getHeader(ATTACHMENT_FILE_CONTENT);
         return null;
      }
   }

   /**
    * @param processId
    */
   public void setProcessId(String processId)
   {
//      if (processId.startsWith("${") && processId.endsWith("}"))
//         this.processId = SimpleLanguage.simple(extractTokenFromExpression(processId));
//      else
//         this.processId = new ConstantExpression(processId); // SimpleLanguage.simple(processId);
      this.processId = parseSimpleExpression(processId);
   }

   /**
    * @return TRUE if it's a synchronous Mode else FALSE
    */
   public boolean isSynchronousMode()
   {
      return synchronousMode;
   }

   /**
    * @param synchronous
    */
   public void setSynchronousMode(boolean synchronous)
   {
      this.synchronousMode = synchronous;
   }

   /**
    * @return data
    */
   public String getData()
   {
      return data;
   }

   /**
    * @param data
    */
   public void setData(String data)
   {
      this.data = data;
   }

   /**
    * @return data Output
    */
   public String getDataOutput()
   {
      return dataOutput;
   }

   /**
    * @param dataOutput
    */
   public void setDataOutput(String dataOutput)
   {
      this.dataOutput = dataOutput;
   }

   /**
    * @return properties
    */
   public String getProperties()
   {
      return properties;
   }

   /**
    * @param properties
    */
   public void setProperties(String properties)
   {
      this.properties = properties;
   }

   /**
    * @param dataMap
    */
   public void setDataMap(String dataMap)
   {
//      if (dataMap.startsWith("${") && dataMap.endsWith("}"))
//
//         this.dataMap = SimpleLanguage.simple(extractTokenFromExpression(dataMap));
//      else
//         this.dataMap = SimpleLanguage.simple(dataMap);
      this.dataMap = parseSimpleExpression(dataMap);
   }

   /**
    * @param dataOutputMap
    */
   public void setDataOutputMap(String dataOutputMap)
   {
//      if (dataOutputMap.startsWith("${") && dataOutputMap.endsWith("}"))
//         this.dataOutputMap = SimpleLanguage.simple(extractTokenFromExpression(dataOutputMap));
//      else
//         this.dataOutputMap = SimpleLanguage.simple(dataOutputMap);
      this.dataOutputMap  = parseSimpleExpression(dataOutputMap);
   }

   /**
    * @param propertiesMap
    */
   public void setPropertiesMap(String propertiesMap)
   {
//      if (propertiesMap.startsWith("${") && propertiesMap.endsWith("}"))
//         this.propertiesMap = SimpleLanguage.simple(extractTokenFromExpression(propertiesMap));
//      else
//         this.propertiesMap = SimpleLanguage.simple(propertiesMap);
      this.propertiesMap  = parseSimpleExpression(propertiesMap);
   }

   /**
    * @param processInstanceOid
    */
   public void setProcessInstanceOid(String processInstanceOid)
   {
//      if (processInstanceOid.startsWith("${") && processInstanceOid.endsWith("}"))
//         this.processInstanceOid = SimpleLanguage.simple(extractTokenFromExpression(processInstanceOid));
//      else
//         this.processInstanceOid = SimpleLanguage.simple(processInstanceOid);
      this.processInstanceOid = parseSimpleExpression(processInstanceOid);
   }

   /**
    * @return Ipp attachment file name
    */
   public Expression getIppAttachmentFileName()
   {
      return ippAttachmentFileName;
   }

   /**
    * @return Ipp attachment folder name
    */
   public Expression getIppAttachmentFolderName()
   {
      return ippAttachmentFolderName;
   }

   /**
    * @return Ipp attachment file content
    */
   public String getIppAttachmentFileContent()
   {
      return ippAttachmentFileContent;
   }

   /**
    * @param ippAttachmentFileName
    */
   public void setIppAttachmentFileName(String ippAttachmentFileName)
   {
      // if (ippAttachmentFileName.startsWith("${") &&
      // ippAttachmentFileName.endsWith("}"))
      // this.ippAttachmentFileName =
      // SimpleLanguage.simple(extractTokenFromExpression(ippAttachmentFileName));
      // else
      // this.ippAttachmentFileName = SimpleLanguage.simple(ippAttachmentFileName);
      if (StringUtils.isNotEmpty(ippAttachmentFileName) && parseSimpleExpression(ippAttachmentFileName) != null)
      {
         this.ippAttachmentFileName = parseSimpleExpression(ippAttachmentFileName);
      }

   }

   /**
    * @param ippAttachmentFolderName
    */
   public void setIppAttachmentFolderName(String ippAttachmentFolderName)
   {
      if (StringUtils.isNotEmpty(ippAttachmentFolderName) && parseSimpleExpression(ippAttachmentFolderName) != null)
      {
         this.ippAttachmentFolderName = parseSimpleExpression(ippAttachmentFolderName);
      }
      
//      if (ippAttachmentFolderName.startsWith("${") && ippAttachmentFolderName.endsWith("}"))
//         this.ippAttachmentFolderName = SimpleLanguage.simple(extractTokenFromExpression(ippAttachmentFolderName));
//      else
//         this.ippAttachmentFolderName = SimpleLanguage.simple(ippAttachmentFolderName);
   }

   /**
    * @param ippAttachmentFileContent
    */
   public void setIppAttachmentFileContent(String ippAttachmentFileContent)
   {
      this.ippAttachmentFileContent = ippAttachmentFileContent;
   }

   /**
    * Returns the value of ModelId
    * 
    * @param exchange
    * @param strict
    *           (boolean)
    * @return ModelId
    */
   public String evaluateModelId(Exchange exchange, boolean strict)
   {
      logger.debug("evaluateProcessId(" + exchange + ", " + strict + ") - start"); //$NON-NLS-1$

      if (null != exchange.getIn().getHeader(MessageProperty.MODEL_ID, String.class))
      {
         logger.debug("Returning value provided by the header Property <" + MessageProperty.MODEL_ID + ">..");
         String modelId = exchange.getIn().getHeader(MessageProperty.MODEL_ID, String.class);
         if (StringUtils.isEmpty(modelId) && strict)
         {
            throw new IllegalStateException("Missing required process ID.");
         }
         return modelId;
      }
      else
      {
         if (null != this.modelId)
         {
            logger.debug("Returning value provided by modelId attribute..");
            return this.modelId.evaluate(exchange, String.class).replaceAll("\"", "");

         }
         else
         {
            logger.warn("Invalid configuration, modelId attribute is empty, The header property"
                  + MessageProperty.MODEL_ID + " is not populated.");
            return null;
         }
      }
   }

   /**
    * @param modelId
    */
   public void setModelId(String modelId)
   {
//      if (modelId.startsWith("${") && modelId.endsWith("}"))
//         this.modelId = SimpleLanguage.simple(extractTokenFromExpression(modelId));
//      else
//         this.modelId = new ConstantExpression(modelId); // SimpleLanguage.simple(processId);
      this.modelId = parseSimpleExpression(modelId);
   }

   public void setState(String state)
   {
      this.state = state;
      setStates(state);
   }

   public void setStates(String states)
   {
      if (StringUtils.isEmpty(states))
         return;

      final List<ProcessInstanceState> piStates = new ArrayList<ProcessInstanceState>();
      String[] stateArray = states.split(",");
      for (int i = 0; i < stateArray.length; i++)
      {
         ProcessInstanceState state = getProcessInstanceStateByName(stateArray[i]);
         if (null != state)
            piStates.add(state);
      }
      this.processInstanceStates = piStates;
   }

   public List<ProcessInstanceState> getProcessInstanceStates()
   {
      return processInstanceStates;
   }

   /**
    * Returns the {@link ProcessInstanceState} for the specified name or <code>null</code>
    * if the state cannot be determined.
    * 
    * @param state
    * @return
    */
   public static ProcessInstanceState getProcessInstanceStateByName(String state)
   {
      if (ProcessInstanceState.Aborted.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Aborted;
      }
      else if (ProcessInstanceState.Aborting.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Aborting;
      }
      else if (ProcessInstanceState.Completed.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Completed;
      }
      else if (ProcessInstanceState.Created.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Created;
      }
      else if (ProcessInstanceState.Interrupted.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Interrupted;
      }
      else if (ProcessInstanceState.Active.getName().equalsIgnoreCase(state))
      {
         return ProcessInstanceState.Active;
      }
      else
      {
         logger.warn("Unknown ProcessInstanceState specified: " + state);
         return null;
      }
   }
   public Long evaluateParentProcessInstanceOid(Exchange exchange, boolean strict)
   {
      if (null != this.parentProcessInstanceOid)
      {
         logger.info("Simple Expression detected for Parent OID attribute, Expression evaluated to  < "
               + this.parentProcessInstanceOid.evaluate(exchange, Long.class) + ">");
         return this.parentProcessInstanceOid.evaluate(exchange, Long.class);
      }
      else
      {
         Long parentiOid = exchange.getIn().getHeader(MessageProperty.PARENT_PROCESS_INSTANCE_OID, Long.class);
         if (null == parentiOid && strict)
         {
            throw new IllegalStateException("Missing required parent OID.");
         }
         logger.info("Parent OID value will be retrieved from exchange header <"
               + MessageProperty.PARENT_PROCESS_INSTANCE_OID + ">, found <" + parentiOid + ">");
         return parentiOid;
      }
   }
   public Expression getParentProcessInstanceOid()
   {
      return parentProcessInstanceOid;
   }

   public void setParentProcessInstanceOid(String parentProcessInstanceOid)
   {
      this.parentProcessInstanceOid =SimpleLanguage.simple( parentProcessInstanceOid);
   }
   public Expression getSpawnProcessID()
   {
      return spawnProcessID;
   }

   public void setSpawnProcessID(Expression spawnProcessID)
   {
      this.spawnProcessID = spawnProcessID;
   }
   public Boolean evaluateCopyData(Exchange exchange, boolean strict)
   {
      if (null != this.copyData)
      {
         logger.info("Simple Expression detected for CopyDate attribute, Expression evaluated to  < "
               + this.copyData.evaluate(exchange, Boolean.class) + ">");
         return this.copyData.evaluate(exchange, Boolean.class);
      }
      else
      {
         Boolean copyData = exchange.getIn().getHeader(MessageProperty.COPY_DATA, Boolean.class);
         if (null == copyData && strict)
         {
            throw new IllegalStateException("Missing required parent OID.");
         }
         logger.info("Attribute CopyData value will be retrieved from exchange header <" + MessageProperty.COPY_DATA
               + ">, found <" + copyData + ">");
         return copyData;
      }
   }

   public Expression getCopyData()
   {
      return copyData;
   }

   public void setCopyData(String copyData)
   {
      this.copyData =  SimpleLanguage.simple(copyData);
      
  
   }
}
