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
/*
 * $Id: $
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.rest.processinterface;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.FormalParameter;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.ProcessInterface;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.ws.DataFlowUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * <p>
 * This class is responsible for transforming formal parameter maps (<code>Map&lt;String, Serializable&gt;</code>)
 * to and from <code>org.w3c.dom.Document</code>. Both transformations include EPM API calls in order to retrieve
 * type information about the formal parameters.
 * </p>
 *
 * <p>
 * Currently only primitive and structured types are supported.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class FormalParameterTransformer
{
   private final ServiceFactory sf;
   private final String qualifiedProcessId;

   /**
    * Creates a new formal parameter transformer.
    *
    * @param serviceFactory the service factory
    * @param qualifiedProcessId the qualified process ID
    */
   public FormalParameterTransformer(final ServiceFactory serviceFactory, final String qualifiedProcessId)
   {
      if (serviceFactory == null)
      {
         throw new NullPointerException("Service Factory must not be null.");
      }
      if (qualifiedProcessId == null)
      {
         throw new NullPointerException("Process ID must not be null.");
      }
      if ("".equals(qualifiedProcessId))
      {
         throw new IllegalArgumentException("Process ID must not be empty.");
      }

      this.sf = serviceFactory;
      this.qualifiedProcessId = qualifiedProcessId;
   }

   /**
    * Transforms the XML representation (encapsulated in an input stream) of the
    * parameters into a map representation (formal parameter id |-> value).
    *
    * @param in the input stream containing the parameters in XML
    * @return the map representation of the parameters
    */
   public Map<String, Serializable> unmarshalParameters(final InputStream in)
   {
      final Map<String, Serializable> parameters;

      final Document doc = createParametersDocument(in);
      parameters = unmarshalDocument(doc);
      return parameters;
   }

   /**
    * Transforms the map representation (formal parameter id |-> value) of the return
    * values into an XML representation.
    *
    * @param returnValues the map representation of the return values (formal parameter id |-> value)
    * @return the XML representation
    */
   public Document marshalDocument(ProcessInstance processInstance, final Map<String, Serializable> returnValues)
   { 
      Document doc = XmlUtils.newDocument();

      Element root = (Element) doc.appendChild(doc.createElementNS(ProcessesRestlet.TYPES_NS, ProcessesRestlet.RESULTS_ELEMENT_NAME));

      QName qualifiedProcessId = QName.valueOf(this.qualifiedProcessId);

      String processId = processInstance.getProcessID();

      DeployedModel implModel = sf.getQueryService().getModel(processInstance.getModelOID());
      ProcessDefinition implProcessDefinition = implModel.getProcessDefinition(processId);

      ProcessInterface implementedProcessInterface = implProcessDefinition.getImplementedProcessInterface();
      QName resolvedProcessId = null;

      if (implementedProcessInterface != null)
      {
         resolvedProcessId = implementedProcessInterface.getDeclaringProcessDefinitionId();
      }
      else
      {
         resolvedProcessId = QName.valueOf(implProcessDefinition.getQualifiedId());
      }

      if ( !qualifiedProcessId.equals(resolvedProcessId))
      {
         final String errorMsg = "Target path incorrect. The target Process definition '"
               + qualifiedProcessId
               + "' is not compatible with Process instance OID '" + processInstance.getOID()
               + "'. The correct Process definition is '" + resolvedProcessId
               + "'. Please correct the request path.";
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
               .entity(errorMsg)
               .build());
      }

      final Models models = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(qualifiedProcessId.getNamespaceURI()));
      if (models.isEmpty())
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }
      final DeployedModelDescription modelDesc = models.get(0);
      final int modelOID = modelDesc.getModelOID();

      ProcessDefinition processDefinition = null;
      DeployedModel model = null;
      if (modelOID == implModel.getModelOID()
            && implProcessDefinition.getQualifiedId().equals(qualifiedProcessId))
      {
         model = implModel;
         processDefinition = implProcessDefinition;
      }
      else
      {
         model = sf.getQueryService().getModel(modelOID);
         processDefinition = sf.getQueryService().getProcessDefinition(modelOID,
               qualifiedProcessId.getLocalPart());
      }

      final List<FormalParameter> fParameters = findFormalParametersFor(processDefinition);

      for (FormalParameter f : fParameters)
      {
            if (f.getDirection().equals(Direction.OUT) || f.getDirection().equals(Direction.IN_OUT))
            {
               addMarshalledValue(root, f, model, returnValues.get(f.getId()));
            }
      }

      return doc;
   }

   private Document createParametersDocument(final InputStream in)
   {
      final DocumentBuilder builder = XmlUtils.newDomBuilder();

      final Document doc;
      try
      {
         doc = builder.parse(in);
      }
      catch (final Exception e)
      {
         final String errorMsg = "Unable to parse given XML.";
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
      }

      return doc;
   }

   private Map<String, Serializable> unmarshalDocument(final Document doc)
   {
      final Map<String, Serializable> parameters = new HashMap<String, Serializable>();

      final ProcessDefinition pd = sf.getQueryService().getProcessDefinition(qualifiedProcessId);
      final List<FormalParameter> fParameters = findFormalParametersFor(pd);
      final Model model = sf.getQueryService().getModel(pd.getModelOID());

      final NodeList nodes = doc.getDocumentElement().getChildNodes();
      for (int i=0; i<nodes.getLength(); i++)
      {
         if (nodes.item(i) instanceof Element)
         {
            final Element element = (Element) nodes.item(i);
            final String fParameterId = element.getLocalName();

            final FormalParameter fp = findFormalParameterForId(fParameterId,
                  fParameters, Direction.IN);
            final Serializable value = getUnmarshalledValue(fp, model, element);
            parameters.put(fParameterId, value);
         }
      }

      return parameters;
   }

   private Serializable getUnmarshalledValue(final FormalParameter fp, final Model model, final Element element)
   {
      final Serializable value;
      String typeDeclarationId = getStructTypeDeclarationId(fp);
      Model resolvedModel = null;
      if (typeDeclarationId == null)
      {
         // resolve external reference
         Data data = model.getData(fp.getDataId());
         
         if (data != null && data.getReference() != null)
         {
            Reference reference = data.getReference();
            resolvedModel = sf.getQueryService().getModel(reference.getModelOid());
            Data resolvedData = resolvedModel.getData(reference.getId());
            typeDeclarationId = (String) resolvedData.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         }
      }
      else
      {
         // data is in the same model
         resolvedModel = model;
      }
      
      
      if (typeDeclarationId == null)
      {
         final Type type = getPrimitiveType(fp);
         if (type == null)
         {
            final String errorMsg = "Unable to find type for formal parameter '" + fp.getId() + "'.";
            throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(errorMsg).build());
         }
         value = DataFlowUtils.unmarshalPrimitiveValue(type, element.getTextContent());
      }
      else
      {
         value = DataFlowUtils.unmarshalStructValue(resolvedModel, typeDeclarationId, null, getFirstChildElement(element));
      }
      return value;
   }

   private Element getFirstChildElement(Element element)
   {
      NodeList childNodes = element.getChildNodes();

      for (int i = 0; i < childNodes.getLength(); i++ )
      {
         Node item = childNodes.item(i);
         if (item instanceof Element)
         {
            return (Element) item;
         }
      }
      return null;
   }

   private void addMarshalledValue(Element root, final FormalParameter fp, final Model model, final Serializable value)
   {
      final String typeDeclarationId = getStructTypeDeclarationId(fp);
      Document doc = root.getOwnerDocument();
      if (typeDeclarationId == null)
      {
         Element element = doc.createElementNS(WADLGenerator.W3C_XML_SCHEMA, fp.getId());
         element.setTextContent(DataFlowUtils.marshalSimpleTypeXsdValue(value));
         root.appendChild(element);
      }
      else
      {
         final Element formalParameterElement = doc.createElementNS(WADLGenerator.W3C_XML_SCHEMA, fp.getId());
         root.appendChild(formalParameterElement);         
         
         final Element structElement = DataFlowUtils.marshalStructValue(model, typeDeclarationId, null, value).getAny().get(0);
         Node importNode = doc.importNode(structElement, true);
         formalParameterElement.appendChild(importNode);
         formalParameterElement.appendChild(doc.createTextNode("\n"));
      }
      root.appendChild(doc.createTextNode("\n"));
   }

   private List<FormalParameter> findFormalParametersFor(final ProcessDefinition pd)
   {
      ProcessInterface processInterface = null;
      if (PredefinedConstants.PROCESSINTERFACE_INVOCATION_REST.equals(pd.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE))
            || PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH.equals(pd.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE)))
      {
         processInterface = pd.getDeclaredProcessInterface();
      }

      if (processInterface == null)
      {
         final String errorMsg = "Process Definition '" + pd.getId() + "' does not expose an interface.";
         throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(errorMsg).build());
      }
      return processInterface.getFormalParameters();
   }

   private FormalParameter findFormalParameterForId(final String fParameterId, final List<FormalParameter> fParameters, final Direction direction)
   {
      for (final FormalParameter f : fParameters)
      {
         if (fParameterId.equals(f.getId()) && (f.getDirection().equals(direction) || f.getDirection().equals(Direction.IN_OUT)))
         {
            return f;
         }
      }

      final String errorMsg = "Formal Parameter '" + fParameterId + "' not found.";
      throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(errorMsg).build());
   }

   private String getStructTypeDeclarationId(final FormalParameter fp)
   {
      return (String) fp.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
   }

   private Type getPrimitiveType(final FormalParameter fp)
   {
      return (Type) fp.getAttribute(PredefinedConstants.TYPE_ATT);
   }
}
