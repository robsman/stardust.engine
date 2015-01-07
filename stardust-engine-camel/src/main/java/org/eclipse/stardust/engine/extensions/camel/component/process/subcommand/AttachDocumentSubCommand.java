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

   @SuppressWarnings("unchecked")
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
      }

      if (pi != null && !pi.getState().equals(ProcessInstanceState.Completed)
            && !pi.getState().equals(ProcessInstanceState.Interrupted))
      {
         if(!folderName.startsWith("/")){
            folderName="/"+folderName;
            LOG.debug("added leading / for the provided folder name, new folder location set to " + folderName);
         }
         DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(ClientEnvironment.getCurrentServiceFactory());

         Document newDocument = dmsFileArchiver.archiveFile(endpoint.evaluateContent(exchange), fileName, folderName);
         List<Document> attachments = (List<Document>) getWorkflowService().getInDataPath(pi.getOID(),
               PROCESS_ATTACHMENTS);
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
