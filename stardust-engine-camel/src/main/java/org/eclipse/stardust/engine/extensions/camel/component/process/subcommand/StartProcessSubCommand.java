package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ATTACHMENT_FILE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

public class StartProcessSubCommand extends AbstractSubCommand
{
   public StartProcessSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint,sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      // Find the process ID
      String processId = endpoint.evaluateProcessId(exchange, true);
      String modelId = endpoint.evaluateModelId(exchange, true);
      String fullyQualifiedName = null;
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

      ProcessInstance pi = getWorkflowService().startProcess(fullyQualifiedName, data, endpoint.isSynchronousMode());

      if (exchange.getProperty(ATTACHMENT_FILE_CONTENT) != null
            || exchange.getIn().getHeader(ATTACHMENT_FILE_CONTENT) != null)
      {

         DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());
         String path = processId;
         String jcrDocumentContent = endpoint.evaluateAttachementContent(exchange);
         dmsFileArchiver.setRootFolderPath("/");
         String documents = "/documents";
         Document newDocument = dmsFileArchiver.archiveFile(jcrDocumentContent.getBytes(), path, documents);

         List<Document> attachments = (List<Document>) getWorkflowService().getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);

         // initialize it if necessary
         if (null == attachments)
         {
            attachments = new ArrayList<Document>();
         }
         // add the new document
         attachments.add(newDocument);

         // update the attachments
         getWorkflowService().setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, attachments);
      }

      // Start a process instance

      // manipulate exchange
      if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
      else
         exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());

   }

}
