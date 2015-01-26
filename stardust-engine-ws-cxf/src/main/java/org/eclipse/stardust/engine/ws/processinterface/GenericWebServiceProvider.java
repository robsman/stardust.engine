/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws.processinterface;

import java.io.Serializable;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ProcessInterfaceCommand;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * <p>
 * This class acts as a generic WS endpoint.
 * </p>
 *
 * @author Nicolas.Werlein, Roland.Stamm
 */
public abstract class GenericWebServiceProvider implements Provider<Source>
{
   private static final Logger LOGGER = LogManager.getLogger(GenericWebServiceProvider.class);

   private static final short XML_INDENT = 3;

   private static final GenericWebServiceEnv env = GenericWebServiceEnv.instance();

//   private static final String PROCESS_INTERFACE_NAMESPACE = "http://eclipse.org/stardust/ws/v2012a/pi";

   public Source invoke(final Source args)
   {
      final Document argsDoc = createArgsDocument(args);

      String modelId = getModelId(argsDoc);

      LOGGER.debug("--> invoke()\n" + WsUtils.dom2String(argsDoc, XML_INDENT));

      final String qualifiedProcessId = WsUtils.createQualifiedProcessId(modelId,
            env.processId());
      LOGGER.debug("I am '" + qualifiedProcessId + "'!");

      String soapAction  = env.soapAction();

      if ("startProcess".equals(soapAction))
      {
         Map<String, ? > dataMap = FormalParameterConverter.buildMap(modelId,
               env.processId(), argsDoc);
         
         ProcessInterfaceCommand.Result result = null;
         
         try
         {
            WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
            
            ProcessInterfaceCommand command = new ProcessInterfaceCommand(qualifiedProcessId, dataMap, true);
            result = (ProcessInterfaceCommand.Result) 
            	wsEnv.getServiceFactory().getWorkflowService().execute(command);
         }
         catch (final Throwable e)
         {
            final String errorMsg = "Could not start process '" + qualifiedProcessId + "'. ";
            LOGGER.error(errorMsg, e);
            throw new WebServiceException(errorMsg+ e.getMessage());
         }

         // Response
         final Document returnDoc = createStartProcessResponse(modelId, env.processId(), result);
         LOGGER.debug("<-- invoke()\n" + WsUtils.dom2String(returnDoc, XML_INDENT));
         return new DOMSource(returnDoc);
      }
      else if ("getProcessResults".equals(soapAction))
      {
         Element piOidElement = findElement("ProcessInstanceOid", argsDoc.getDocumentElement());
         Map<String, Serializable> processResults = null;
         try
         {
            WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
             processResults = wsEnv.getServiceFactory().getWorkflowService().getProcessResults(
                  Long.valueOf(piOidElement.getTextContent()));
         }
         catch (final Throwable e)
         {
            final String errorMsg = "Could not get process results for 'processInstanceOid: " + piOidElement.getTextContent()+ "'. ";
            LOGGER.error(errorMsg, e);
            throw new WebServiceException(errorMsg+ e.getMessage());
         }

         // Response
         final Document returnDoc = createStartProcessResponse(modelId, env.processId(), processResults);
         LOGGER.debug("<-- invoke()\n" + WsUtils.dom2String(returnDoc, XML_INDENT));
         return new DOMSource(returnDoc);
      }
      else
      {
         throw new WebServiceException("Unknown SOAP Action: "+ soapAction);
      }
   }

   private String getModelId(Document argsDoc)
   {
      // String modelId = null;
      // Element argsElement = findElement(env.processId(), argsDoc.getDocumentElement());
      // modelId = WsUtils.extractModelId(argsElement.getNamespaceURI());
      String modelId = GenericWebServiceEnv.instance().getModelId();
      String partitionId = GenericWebServiceEnv.instance().getPartitionId();
      if (StringUtils.isEmpty(partitionId))
      {
         partitionId = PredefinedConstants.DEFAULT_PARTITION_ID;
      }
      if (StringUtils.isEmpty(modelId))
      {
         modelId = WsUtils.getDefaultModelId(partitionId);
      }
      WsUtils.ensureModelIdExists(modelId);
      return modelId;
   }

   private Element findElement(String suffix, Element element)
   {
      String name = element.getNodeName();
      if (name != null && name.endsWith(suffix))
      {
         return element;
      }

      NodeList childNodes = element.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++ )
      {
         Node item = childNodes.item(i);
         if (item instanceof Element)
         {
            Element ret2 = findElement(suffix,(Element) item);
            if (ret2 != null)
            {
               return ret2;
            }
         }
      }
      return null;
   }

   private Document createArgsDocument(final Source args)
   {
      final DOMResult result = new DOMResult();
      XmlUtils.transform(args, null, result, null, XML_INDENT, null);
      final Document doc = (Document) result.getNode();
      return doc;
   }

   private Document createStartProcessResponse(final String modelId, final String processId,
         final ProcessInterfaceCommand.Result result)
   {
      String nsModelId = WsUtils.getNamespaceSafeModelID(modelId);
      final Document doc = XmlUtils.newDocument();

      final Element root = doc.createElementNS(nsModelId, "startProcessResponse"
            + processId);

      doc.appendChild(root);

      Element processInstanceOid = doc.createElementNS(nsModelId, "ProcessInstanceOid");
      processInstanceOid.setTextContent(Long.valueOf(result.getProcessInstance().getOID()).toString());
      root.appendChild(processInstanceOid);

      final Element returnElement = doc.createElementNS(nsModelId, "Return");
      root.appendChild(returnElement);

      if (ProcessInstanceState.Completed.equals(result.getProcessInstance().getState()) && result.getProcessResults() != null)
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         Map<String, Serializable> dataMap = result.getProcessResults();

         FormalParameterConverter.buildNode(modelId, processId, doc, returnElement,
               dataMap);
      }
      return doc;
   }

   private Document createStartProcessResponse(final String modelId, final String processId, Map<String, Serializable> dataMap)
   {
      String nsModelId = WsUtils.getNamespaceSafeModelID(modelId);
      final Document doc = XmlUtils.newDocument();

      final Element root = doc.createElementNS(nsModelId, "getProcessResultsResponse"
            + processId);

      doc.appendChild(root);

      final Element returnElement = doc.createElementNS(nsModelId, "Return");
      root.appendChild(returnElement);

      FormalParameterConverter.buildNode(modelId, processId, doc, returnElement, dataMap);

      return doc;
   }
}
