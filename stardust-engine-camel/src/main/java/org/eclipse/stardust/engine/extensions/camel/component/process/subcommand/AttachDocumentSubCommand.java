package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import java.util.ArrayList;
import java.util.List;
import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.component.ProcessEndpoint;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

public class AttachDocumentSubCommand extends AbstractSubCommand
{
   public AttachDocumentSubCommand(ProcessEndpoint endpoint, ServiceFactory sf)
   {
      super(endpoint, sf);
   }

   public void process(Exchange exchange) throws Exception
   {
      Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
      ProcessInstance pi = getWorkflowService().getProcessInstance(processInstanceOid);
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
         folderName = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
         LOG.debug("No folder name provided, default location set to " + folderName);
         folderName = folderName.substring(1);
      }

      String data = endpoint.evaluateContent(exchange);

      if (data != null && pi != null && !pi.getState().equals(ProcessInstanceState.Completed)
            && !pi.getState().equals(ProcessInstanceState.Interrupted))
      {
         DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());

         String jcrDocumentContent = data;
         // dmsFileArchiver.setRootFolderPath("/");

         Document newDocument = dmsFileArchiver.archiveFile(jcrDocumentContent.getBytes(), fileName, folderName);
         List<Document> attachments = (List<Document>) getWorkflowService().getInDataPath(pi.getOID(),
               PROCESS_ATTACHMENTS);

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

   }

}
