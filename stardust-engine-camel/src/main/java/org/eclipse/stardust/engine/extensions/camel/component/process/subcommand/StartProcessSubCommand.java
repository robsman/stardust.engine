package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_DOCUMENT_NAME_KEY;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.component.mail.MailEndpoint;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;

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

      ProcessInstance pi;
      // Determine data for start process
      @SuppressWarnings("unchecked")
      Map<String, Object> data = (Map<String, Object>) endpoint.evaluateData(exchange);
      
      String camelDocumentName = (String) exchange.getIn().getHeader(CAMEL_DOCUMENT_NAME_KEY);
      Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
      ProcessDefinition processDefinition 	= sf.getQueryService().getProcessDefinition(processId);
      DataPath attachmentsDefinition = processDefinition.getDataPath(PROCESS_ATTACHMENTS);
      Endpoint fromEndpoint =  exchange.getFromEndpoint();
      if(camelDocumentName == null && attachments.isEmpty() && attachmentsDefinition == null ||
		  (camelDocumentName == null && !(fromEndpoint instanceof MailEndpoint))) {
    	  pi = getWorkflowService().startProcess(fullyQualifiedName, data, endpoint.isSynchronousMode());
      }
      else {
    	  // Retrieve Document dataID
		  @SuppressWarnings("rawtypes")
		  Set listKeys = data.keySet();
		  @SuppressWarnings("unchecked")
		  Iterator<String> it = listKeys.iterator();
		  String dataId = (String) it.next();
	  StartProcessAndAttachDocumentCommand command = new StartProcessAndAttachDocumentCommand(fullyQualifiedName, data, dataId, endpoint.isSynchronousMode(), exchange);
          pi = (ProcessInstance) getWorkflowService().execute(command);
      }
     
      // manipulate exchange
      if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         exchange.getOut().setHeader(PROCESS_INSTANCE_OID, pi.getOID());
      else
         exchange.getIn().setHeader(PROCESS_INSTANCE_OID, pi.getOID());

   }

}
