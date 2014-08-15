package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;

import java.util.*;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.extensions.camel.util.CamelDmsUtils;

public class StartProcessAndAttachDocumentCommand implements ServiceCommand {
	
	private static final long serialVersionUID = 1L;

	private String processId;
	private final Boolean startSynchronously;
	private Map<String, Object> initialDataValues;
	private Exchange exchange;
	private Boolean processAttachmentSupport = false;
	
	public StartProcessAndAttachDocumentCommand(String processId, Map<String, Object> initialDataValues,
			Boolean startSynchronously, Exchange exchange) {
		this.processId = processId;
		this.initialDataValues = initialDataValues;
		this.startSynchronously = startSynchronously;
		this.exchange = exchange;
	}

	@SuppressWarnings("unchecked")
   public ProcessInstance execute(ServiceFactory sFactory) {

		WorkflowService wService = sFactory.getWorkflowService();
		ProcessDefinition processDefinition  = sFactory.getQueryService().getProcessDefinition(processId);
		Map<String, Object> initialDataValuesWithoutDocumentData = new HashMap<String, Object>();
		// Ignore Document data from initialDataValues
      if(initialDataValues != null) {
         initialDataValuesWithoutDocumentData = IgnoreDocumentData(initialDataValues, processDefinition);
      }
		ProcessInstance pInstance = wService.startProcess(processId, initialDataValuesWithoutDocumentData,
				startSynchronously);

		// handle document data
		if((initialDataValues != null) && !(initialDataValues.keySet().equals(initialDataValuesWithoutDocumentData.keySet())))
		{
		   ModelManager modelManager = ModelManagerFactory.getCurrent();
         IModel iModel = modelManager.findModel(pInstance.getModelOID());
		   Map<String, Document> initialDocumentDataValues = new HashMap<String, Document>();
	      Iterator<Trigger>  triggers = processDefinition.getAllTriggers().iterator();
	      while (triggers.hasNext())
	      {
	         Trigger trigger = (Trigger) triggers.next();
	         // Initialize document data
	         initialDocumentDataValues = CamelDmsUtils.initializeDocumentData(trigger, pInstance, exchange, sFactory);
	         
	         for (Map.Entry<String, Document> entry : initialDocumentDataValues.entrySet())
	         {
	            String dataId = entry.getKey();
	            Document document = entry.getValue();
	            IData iData = iModel.findData(DataUtils
	                  .getUnqualifiedProcessId(dataId));

	            DocumentTypeUtils
	                  .inferDocumentTypeAndStoreDocument(iData, document, sFactory.getDocumentManagementService());

	            ProcessInstanceBean iPi = ProcessInstanceBean.findByOID(pInstance
	                  .getOID());
	            iPi.setOutDataValue(iData, "", document);
	         }
	         
	      }
		}
		
		// handle process attachment
		DataPath attachmentsDefinition = processDefinition.getDataPath(PROCESS_ATTACHMENTS);
      if(attachmentsDefinition != null) {
         processAttachmentSupport = true;
      }
      
		if(processAttachmentSupport) {
			
			List<Document> attachments = (List<Document>) wService.getInDataPath(pInstance.getOID(), PROCESS_ATTACHMENTS);

	         // initialize it if necessary
	         if (null == attachments)
	         {
	            attachments = new ArrayList<Document>();
	         }
	         // Multiple Attachment
	         Map<String, DataHandler> exchangeAttachments = exchange.getIn().getAttachments();
	         if(!exchangeAttachments.isEmpty())
	         {
	            for(Map.Entry<String, DataHandler> entry: exchangeAttachments.entrySet()) 
	            {
	               Document attachmentDocument = null;
                  String documentName = entry.getKey();
                  
                  try 
                  {
                     Object messageContent = entry.getValue().getContent();
                     attachmentDocument = CamelDmsUtils.toDocument(messageContent, exchange, documentName, sFactory.getDocumentManagementService(), pInstance, processAttachmentSupport);
               
                  } catch (Exception e) 
                  {
                     throw new RuntimeException("Failed creating document.", e);
                  }
                  
                  attachments.add(attachmentDocument);
	            }
	         }
	         // update the attachments
	         if(!attachments.isEmpty()) {
			 wService.setOutDataPath(pInstance.getOID(), PROCESS_ATTACHMENTS, attachments);
	         }
		}	
		
		return pInstance;
	}

	private Map<String, Object> IgnoreDocumentData(Map<String, Object> initialDataValues, ProcessDefinition processDefinition) 
	{
		Map<String, Object> result = new HashMap<String, Object>();
		List<String> documentAccessPointList = CamelDmsUtils.getDocumentAccessPointListForTrigger(processDefinition);
		for(Map.Entry<String, Object> entry: initialDataValues.entrySet()) 
		{
			if(!documentAccessPointList.contains(entry.getKey()))
			{
	         result.put(entry.getKey(), entry.getValue());
			}

		}
		return result;
	}
	
}
