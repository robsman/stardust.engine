package org.eclipse.stardust.engine.extensions.camel.component;


import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Document.COMMAND_MOVE;
import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.getServiceFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class DocumentManagementProducer extends AbstractIppProducer
{
   private static final Logger logger = LogManager.getLogger(DocumentManagementProducer.class);

   private DocumentManagementEndpoint endpoint;

   public DocumentManagementProducer(DocumentManagementEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   @Override
   public void process(Exchange exchange) throws Exception
   {
      if (COMMAND_MOVE.equals(endpoint.getSubCommand()))
      {
         ServiceFactory sf = getServiceFactory(this.endpoint, exchange);
         DocumentManagementService dms = sf.getDocumentManagementService();
         WorkflowService wfService = sf.getWorkflowService();
         Long processInstanceOid = endpoint.evaluateProcessInstanceOid(exchange, true);
         ProcessInstance pi = wfService.getProcessInstance(processInstanceOid);
         String documentId = endpoint.evaluateDocumentId(exchange);
         String destination = endpoint.evaluateTargetPath(exchange);
         if (documentId == null)
            throw new CamelException("Missing required attribute DocumentId");
         Folder piAttachmentsFolder=null;
         if (destination == null)
         {
            long scopeProcessInstanceOID = -1;
            if (exchange.getIn().getHeader("ippProcessInstanceOid") != null)
            {
               scopeProcessInstanceOID = (Long) exchange.getIn().getHeader("ippProcessInstanceOid");
            }
            logger.debug("scopeProcessInstanceOID = " + scopeProcessInstanceOID);
            Date scopeProcessInstanceStartTime = (Date) exchange.getIn().getHeader("CamelFileLastModified");
            if ((scopeProcessInstanceStartTime != null) && (!("-1".equals(Long.toString(scopeProcessInstanceOID)))))
            {
               destination = DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime());
               piAttachmentsFolder= DmsUtils.ensureFolderHierarchyExists(destination, dms);
            }
         }
         Document document=dms.getDocument(documentId);
         byte[] documentContent=dms.retrieveDocumentContent(documentId);
         
         //Folder folder=dms.getFolder(document.getPath());
         DocumentInfo documentInfo=DmsUtils.createDocumentInfo(document.getName(), document.getId());
         //piAttachmentsFolder.
         Document newDocument=null;
         if(dms.getDocument(piAttachmentsFolder.getId())!=null)
         newDocument=dms.createDocument(piAttachmentsFolder.getId(), documentInfo,documentContent,null);
//        dms.updateDocument(newDocument, documentContent, null, false,null,null, false);
         //dms.updateFolder(piAttachmentsFolder);
         List<Document> attachments = (List<Document>) wfService.getInDataPath(pi.getOID(), PROCESS_ATTACHMENTS);
         if (null == attachments)
         {
            attachments = new ArrayList<Document>();
         }
         // add the new document
         attachments.add(newDocument);
         
         // update the attachments
         wfService.setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS, attachments);
       //  dms.removeFolder(folder.getId(), true);
        
      //   dms.moveDocument(documentId, destination);
         dms.removeDocument(documentId);
         exchange.getIn().removeHeader(CamelConstants.MessageProperty.DOCUMENT_Id);
      }

   }

}
