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
package org.eclipse.stardust.engine.ws.processinterface;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.ws.XmlValueXto;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.ws.DataFlowUtils;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class focuses on building a map of Serializable data based on an incoming WS
 * request and the underlying XPDL model and also in the reverse direction.
 * </p>
 *
 * @author Nicolas.Werlein, Roland.Stamm
 */
public class FormalParameterConverter
{
   public static Map<String, Serializable> buildMap(final String modelId,
         final String processId, final Document docArgs)
   {
      final Map</* fParameterId: */String, Serializable> dataMap = CollectionUtils.newMap();
      final List<FormalParameter> fParameterMappings = getFormalParameterMappings(
            modelId, processId);
      final Map</* fParameterId: */String, Element> elementArgs = extractArguments(
            docArgs, processId);

      for (final Entry<String, Element> e : elementArgs.entrySet())
      {
         final Serializable value = unmarshalMapValue(e, fParameterMappings, modelId);
         dataMap.put(e.getKey(), value);
      }

      return dataMap;
   }

   public static void buildNode(String modelId, String processId, Document doc,
         Element formalParameters, Map<String, Serializable> dataMap)
   {
      if (dataMap != null && !dataMap.isEmpty())
      {
         final List<FormalParameter> fParameterMappings = getFormalParameterMappings(
               modelId, processId);

         TreeMap<String, Serializable> sortedMap = new TreeMap<String, Serializable>();

         sortedMap.putAll(dataMap);

         for (Map.Entry<String, Serializable> entry : sortedMap.entrySet())
         {
            marshalMapValue(doc, formalParameters, entry, fParameterMappings, modelId);
         }
      }
   }

   private static List<FormalParameter> getFormalParameterMappings(final String modelId,
         final String processId)
   {
      final Model activeModelForId = WebServiceEnv.currentWebServiceEnvironment().getActiveModel(modelId);
      final ProcessDefinition pd = activeModelForId.getProcessDefinition(processId);
      final ProcessInterface dpi = pd.getDeclaredProcessInterface();
      final List<FormalParameter> fParameterMappings = dpi.getFormalParameters();
      return fParameterMappings;
   }

   private static Map<String, Element> extractArguments(final Document args,
         final String processId)
   {
      final Map<String, Element> result = new HashMap<String, Element>();

      final NodeList childNodes = args.getDocumentElement().getChildNodes();

      Node argsNode = null;
      for (int i = 0; i < childNodes.getLength(); i++ )
      {
         Node item = childNodes.item(i);
         if (item instanceof Element)
         {
            argsNode = item;
         }
      }

      final NodeList nodes = argsNode.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++ )
      {
         final Node node = nodes.item(i);
         if (node instanceof Element)
         {
            final String fParameterId = node.getLocalName();
            final Element element = (Element) node.cloneNode(true);
            result.put(fParameterId, element);
         }
      }

      return result;
   }

   private static Serializable unmarshalMapValue(final Entry<String, Element> arg,
         final List<FormalParameter> fParameterMappings, String modelId)
   {
      Serializable value = null;

      final String fParameterId = arg.getKey();
      final Element element = arg.getValue();
      boolean found = false;
      for (final FormalParameter m : fParameterMappings)
      {
         if (fParameterId.equals(m.getId()))
         {
            value = parseMapValue(m, element, modelId);

            found = true;
            break;
         }
      }

      if ( !found)
      {
         throw new IllegalArgumentException("Parameter Mapping for Parameter Id '"
               + fParameterId + "' not found.");
      }

      return value;
   }

   private static Serializable parseMapValue(final FormalParameter mapping,
         final Element element, String modelId)
   {
      Serializable value = null;

      final String typeId = mapping.getTypeId();

      if (StructuredTypeRtUtils.isStructuredType(typeId))
      {
         String typeDeclarationId = (String) mapping.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
         Model resolvedModel = wsEnv.getActiveModel(modelId);
         if (typeDeclarationId == null)
         {
            // try to resolve external reference
            String dataId = mapping.getDataId();
            Data resolvedData = resolvedModel.getData(dataId);
            if (resolvedData != null)
            {
               Reference reference = resolvedData.getReference();
               if (reference != null)
               {
                  typeDeclarationId = reference.getId();
                  resolvedModel = wsEnv.getModel(reference.getModelOid());
               }
            }
         }

         Element structuredDataElement = null;
         NodeList childNodes = element.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++ )
         {
            Node item = childNodes.item(i);
            if (item instanceof Element)
            {
               structuredDataElement = (Element) item;
            }
         }
         TypeDeclaration typeDeclaration = resolvedModel.getTypeDeclaration(typeDeclarationId);
         value = DataFlowUtils.unmarshalStructValue(resolvedModel, typeDeclaration, "", structuredDataElement);
      }
      else if (PredefinedConstants.PRIMITIVE_DATA.equals(typeId))
      {
         Type primitiveType = (Type) mapping.getAttribute(TYPE_ATT);

         value = DataFlowUtils.unmarshalPrimitiveValue(primitiveType,
               element.getTextContent());
      }
      else
      {
         throw new UnsupportedOperationException("Unsupported Type Id found '" + typeId
               + "'.");
      }
      return value;
   }

   private static void marshalMapValue(Document targetDoc, Element targetElement,
         Entry<String, Serializable> entry, List<FormalParameter> fParameterMappings,
         String modelId)
   {
      final String fParameterId = entry.getKey();
      final Serializable value = entry.getValue();
      boolean found = false;
      for (final FormalParameter m : fParameterMappings)
      {
         if (fParameterId.equals(m.getId()))
         {
            printMapValue(targetDoc, targetElement, m, value, modelId);

            found = true;
            break;
         }
      }

      if ( !found)
      {
         throw new IllegalArgumentException("Parameter Mapping for Parameter Id '"
               + fParameterId + "' not found.");
      }
   }

   private static void printMapValue(Document targetDoc, Element targetElement,
         FormalParameter mapping, Serializable value, String modelId)
   {
      final String typeId = mapping.getTypeId();

      if (StructuredTypeRtUtils.isStructuredType(typeId))
      {
         String typeDeclarationId = (String) mapping.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         Model resolvedModel = WebServiceEnv.currentWebServiceEnvironment().getActiveModel(modelId);
         if (typeDeclarationId == null)
         {
            // try to resolve external reference
            String dataId = mapping.getDataId();
            Data resolvedData = resolvedModel.getData(dataId);
            if (resolvedData != null)
            {
               Reference reference = resolvedData.getReference();
               if (reference != null)
               {
                  typeDeclarationId = reference.getId();
                  resolvedModel = WebServiceEnv.currentWebServiceEnvironment().getModel(reference.getModelOid());
               }
            }
         }

         XmlValueXto marshalStructValue = DataFlowUtils.marshalStructValue(
               resolvedModel, typeDeclarationId, "", value);
         Element structParameterElement = marshalStructValue.getAny().get(0);
         Node structNode = targetDoc.importNode(structParameterElement, true);

         Element parameterNode = targetDoc.createElementNS(targetElement.getNamespaceURI(), mapping.getId());

         parameterNode.appendChild(structNode);

         targetElement.appendChild(parameterNode);
      }
      else if (PredefinedConstants.PRIMITIVE_DATA.equals(typeId))
      {
         Element parameterElement = targetDoc.createElementNS(
               targetElement.getNamespaceURI(), mapping.getId());
         parameterElement.setTextContent(DataFlowUtils.marshalPrimitiveValue(value));
         targetElement.appendChild(parameterElement);
      }
      else
      {
         throw new UnsupportedOperationException("Unsupported Type Id found '" + typeId
               + "'.");
      }
   }

   private FormalParameterConverter()
   {
      /* utility class */
   }
}