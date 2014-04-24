package org.eclipse.stardust.engine.extensions.camel.component.process.subcommand;

import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.engine.api.runtime.DmsUtils.createFolderInfo;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_ATTACHMENTS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Exchange;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.CreateDocumentException;

public class StartProcessAndAttachDocumentCommand implements ServiceCommand {
	
	private static final long serialVersionUID = 1L;

	private String processId;
	private final Boolean startSynchronously;
	private String dataId ;
	private Exchange exchange;
	private Boolean processAttachmentSupport = false;
	
	public StartProcessAndAttachDocumentCommand(String processId,
			String dataId, Boolean startSynchronously, Exchange exchange) {
		this.processId = processId;
		this.dataId = dataId;
		this.startSynchronously = startSynchronously;
		this.exchange = exchange;
	}

	public ProcessInstance execute(ServiceFactory sFactory) {

		WorkflowService wService = sFactory.getWorkflowService();
		
		byte[] content = (byte[]) exchange.getIn().getHeader("CamelDocumentContent");

		ProcessInstance pInstance = wService.startProcess(processId, null,
				startSynchronously);

		ProcessDefinition processDefinition 	= sFactory.getQueryService().getProcessDefinition(processId);
		DataPath attachmentsDefinition = processDefinition.getDataPath(PROCESS_ATTACHMENTS);
		if(attachmentsDefinition != null) {
			processAttachmentSupport = true;
		}
		
		String fileName = (String) exchange.getIn().getHeader("CamelFileNameOnly");
		Document document;
		try {
			document = this.storeDocument(sFactory, pInstance,
					content, fileName, processAttachmentSupport);
		} catch (CreateDocumentException e) {
			
			throw new RuntimeException("Failed creating document.", e);
		}
		
		if(processAttachmentSupport) {
			
			@SuppressWarnings("unchecked")
			List<Document> attachments = (List<Document>) wService.getInDataPath(pInstance.getOID(), PROCESS_ATTACHMENTS);

	         // initialize it if necessary
	         if (null == attachments)
	         {
	            attachments = new ArrayList<Document>();
	         }
	         // add the new document
	         attachments.add(document);

	         // update the attachments
	         wService.setOutDataPath(pInstance.getOID(), PROCESS_ATTACHMENTS, attachments);
		}	
		
			ModelManager modelManager = ModelManagerFactory.getCurrent();
			IModel iModel = modelManager.findModel(pInstance.getModelOID());
			IData iData = iModel.findData(DataUtils
					.getUnqualifiedProcessId(dataId));

			DocumentTypeUtils
					.inferDocumentTypeAndStoreDocument(iData, document);

			ProcessInstanceBean iPi = ProcessInstanceBean.findByOID(pInstance
					.getOID());
			iPi.setOutDataValue(iData, "", document);

		return pInstance;
	}

	public Document storeDocument(ServiceFactory sf, ProcessInstance pi,
			byte[] content, String fileName, Boolean processAttachmentSupport) throws CreateDocumentException {

		StringBuilder defaultPath = new StringBuilder(
				DmsUtils.composeDefaultPath(
						pi.getScopeProcessInstanceOID(), pi.getStartTime()))
				.append("/");

		if(processAttachmentSupport) {
			defaultPath
			.append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER);
		}
		else {
			defaultPath
			.append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER);
		}

		ensureFolderExists(sf.getDocumentManagementService(),
				defaultPath.toString());

		Document doc = storeDocumentIntoDms(
				sf.getDocumentManagementService(), defaultPath.toString(),
				content, fileName);

		return doc;
	}

	private Document storeDocumentIntoDms(DocumentManagementService dms,
			String folderId, byte[] content, String fileName) throws CreateDocumentException {

		try {
			DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);

			String documentPath = folderId;

			if (!folderId.endsWith("/")) {
				documentPath += "/";
			}

			documentPath += docInfo.getName();

			Document doc = dms.createDocument(folderId, docInfo,
					extractContentByteArray(content), null);

			return doc;

		} catch (DocumentManagementServiceException ex) {
			
			throw new CreateDocumentException("Failed creating document.", ex);
		}
	}

	public byte[] extractContentByteArray(byte[] content) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[4096];

		try {
			InputStream from = new ByteArrayInputStream(content);

			try {
				int bytesRead;
				while (0 < (bytesRead = from.read(buffer))) {
					baos.write(buffer, 0, bytesRead);
				}
			} finally {
				from.close();
			}
		} catch (IOException ioe) {
			throw new PublicException(
					"Failed retrieving document content.", ioe);
		}

		return baos.toByteArray();
	}

	public void ensureFolderExists(DocumentManagementService dms,
			String folderId) throws DocumentManagementServiceException {
		if (!StringUtils.isEmpty(folderId) && folderId.startsWith("/")) {
			// try to create folder
			String[] segments = folderId.substring(1).split("/");

			// walk backwards to find existing path prefix, then go forward
			// again creating missing segments

			Folder folder = null;
			LinkedList<String> missingSegments = newLinkedList();
			for (int i = segments.length - 1; i >= 0; --i) {
				StringBuilder path = new StringBuilder();
				for (int j = 0; j <= i; ++j) {
					path.append("/").append(segments[j]);
				}

				folder = dms.getFolder(path.toString(),
						Folder.LOD_NO_MEMBERS);
				if (null != folder) {
					// found existing prefix
					break;
				} else {
					// folder missing?
					missingSegments.add(0, segments[i]);
				}
			}

			String currentPath = (null != folder) ? folder.getPath() : "";
			while (!missingSegments.isEmpty()) {
				String parentFolderId = StringUtils.isEmpty(currentPath) ? "/"
						: currentPath;

				String segment = missingSegments.remove(0);

				// create missing sub folder
				folder = dms.createFolder(parentFolderId,
						createFolderInfo(segment));
				currentPath = folder.getPath();
			}
		}
	}

}
