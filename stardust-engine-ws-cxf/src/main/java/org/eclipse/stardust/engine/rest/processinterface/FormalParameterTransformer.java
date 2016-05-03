/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
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
import org.eclipse.stardust.common.utils.xml.SecureEntityResolver;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.interactions.ModelResolver;
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
   private ModelResolver resolver;
   private ProcessDefinition pd;

   /**
    * Creates a new formal parameter transformer.
    *
    * @param serviceFactory the service factory
    * @param pd the process definition
    */
   public FormalParameterTransformer(ModelResolver resolver, ProcessDefinition pd)
   {
      if (resolver == null)
      {
         throw new NullPointerException("Model resolver must not be null.");
      }
      if (pd == null)
      {
         throw new NullPointerException("Process ID must not be null.");
      }
      if ("".equals(pd))
      {
         throw new IllegalArgumentException("Process ID must not be empty.");
      }

      this.resolver = resolver;
      this.pd = pd;
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

      QName qualifiedProcessId = QName.valueOf(pd.getQualifiedId());
      String processId = processInstance.getProcessID();

      Model model = resolver.getModel(processInstance.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(processId);

      ProcessInterface implementedProcessInterface = processDefinition.getImplementedProcessInterface();
      if (implementedProcessInterface != null)
      {
         QName resolvedProcessId = implementedProcessInterface.getDeclaringProcessDefinitionId();
         if (!qualifiedProcessId.equals(resolvedProcessId))
         {
            final String errorMsg = "Target path incorrect. The target Process definition '"
                  + qualifiedProcessId
                  + "' is not compatible with Process instance OID '" + processInstance.getOID()
                  + "'. The correct Process definition is '" + resolvedProcessId
                  + "'. Please correct the request path.";
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
         }
         model = resolver.getModel(model.getResolvedModelOid(qualifiedProcessId.getNamespaceURI()));
         if (null == model)
         {
            throw new WebApplicationException(Status.NOT_FOUND);
         }
         processDefinition = model.getProcessDefinition(qualifiedProcessId.getLocalPart());
      }

      List<FormalParameter> fParameters = findFormalParametersFor(processDefinition);
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
      builder.setEntityResolver(SecureEntityResolver.INSTANCE);

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

      final List<FormalParameter> fParameters = findFormalParametersFor(pd);
      final Model model = resolver.getModel(pd.getModelOID());

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
      Model resolvedModel = model;
      if (typeDeclarationId == null)
      {
         // resolve external reference
         Data data = model.getData(fp.getDataId());
         if (data != null && data.getReference() != null)
         {
            Reference reference = data.getReference();
            resolvedModel = resolver.getModel(reference.getModelOid());
            typeDeclarationId = reference.getId();
         }
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
         TypeDeclaration typeDeclaration = resolvedModel.getTypeDeclaration(typeDeclarationId);
         value = DataFlowUtils.unmarshalStructValue(resolvedModel, typeDeclaration, null, getFirstChildElement(element));
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
      String typeDeclarationId = getStructTypeDeclarationId(fp);
      Model resolvedModel = model;
      if (typeDeclarationId == null)
      {
         // resolve external reference
         Data data = model.getData(fp.getDataId());

         if (data != null && data.getReference() != null)
         {
            Reference reference = data.getReference();
            resolvedModel = resolver.getModel(reference.getModelOid());
            typeDeclarationId = reference.getId();
         }
      }

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

         final Element structElement = DataFlowUtils.marshalStructValue(resolvedModel, typeDeclarationId, null, value).getAny().get(0);
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

