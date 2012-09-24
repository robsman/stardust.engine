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

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;

import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.utils.xml.StaticNamespaceContext;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.ws.DataFlowUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * <p>
 * Generates a WSDL file based on a template and the given XPDL process model.
 * </p>
 *
 * @author Nicolas.Werlein, Roland.Stamm
 */
public class WSDLGenerator
{
   static final String XPDL_SCHEMA_URL = "http://www.wfmc.org/2008/XPDL2.1";

   static final String WSDL_SCHEMA_URL = "http://schemas.xmlsoap.org/wsdl/";

   public static final String XSD_SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";

   static final String CARNOT_SCHEMA_URL = XpdlUtils.CARNOT_XPDL_XSD_URL;

   private static final NamespaceContext XPATH_CONTEXT;

   private static final String WSDL_TEMPLATE_PATH = "/META-INF/wsdl/templates/StardustProcessInterface.template.wsdl";

   private static final String WSDL_ENCODING = "UTF-8";

   private static final short INDENT_SIZE = 3;

   private final Document wsdlDoc;

   private final IModel model;

   private Map<String,NSPrefixPair> nsPairs = CollectionUtils.newMap();

   static
   {
      StaticNamespaceContext staticNamespaceContext = new StaticNamespaceContext();
      staticNamespaceContext.defineNamespace("xpdl", XPDL_SCHEMA_URL);
      staticNamespaceContext.defineNamespace("xsd", XSD_SCHEMA_URL);
      staticNamespaceContext.defineNamespace("wsdl", WSDL_SCHEMA_URL);
      XPATH_CONTEXT = staticNamespaceContext;
   }

   /**
    * ctor
    */
   public WSDLGenerator(final String modelString)
   {
      String modelString2 = modelString;
      if (modelString == null)
      {
         throw new NullPointerException("Process Model must not be null.");
      }

      if (ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
      {
         // convert to CWM format
         String encoding = Parameters.instance().getObject(
               PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         modelString2 = XpdlUtils.convertXpdl2Carnot(modelString, encoding);
      }
      this.model = new DefaultXMLReader(false).importFromXML(new StringReader(modelString2));

      InputStream wsdlInputStream = WSDLGenerator.class.getResourceAsStream(WSDL_TEMPLATE_PATH);
      this.wsdlDoc = DomUtils.createDocument(wsdlInputStream, WSDL_TEMPLATE_PATH);
   }

   @Deprecated
   public WSDLGenerator(final byte[] modelBytes)
   {
     this(new String(modelBytes));
   }

   public byte[] generate()
   {
      Document internalWsdlDoc = generateInternal();
      String xmlString = XmlUtils.toString(internalWsdlDoc);
      return xmlString.getBytes();
   }

   public byte[] generateFormatted()
   {
      final Document unformattedDoc = generateInternal();
      final String formattedDoc = DomUtils.formatDocument(unformattedDoc, INDENT_SIZE,
            WSDL_ENCODING);
      return formattedDoc.getBytes();
   }

   public Document generateDocument()
   {
      return generateInternal();
   }

   private Document generateInternal()
   {
      final Map<String, List<IFormalParameter>> usedTypes = collectFormalParameters();
      final Set<IFormalParameter> parameters2Define = mergeAndReduceNonCustomTypes(usedTypes);
      if ( !parameters2Define.isEmpty())
      {
         insertCustomTypeDefinitions(parameters2Define);
      }
      assembleWebServiceDefinitions(usedTypes);
      return wsdlDoc;
   }

   private String getModelId()
   {
      String modelId = this.model.getId();

      return WsUtils.getNamespaceSafeModelID(modelId);
   }


   private Map<String, List<IFormalParameter>> collectFormalParameters()
   {
      Map<String,List<IFormalParameter>> formalParametersPerProcess = CollectionUtils.newMap();
      List<IProcessDefinition> pds = new ArrayList<IProcessDefinition>();

      Link mel = (Link) this.model.getProcessDefinitions();
      for (Iterator<?> iterator = mel.iterator(); iterator.hasNext();)
      {
         Object me = iterator.next();
         if (me instanceof IProcessDefinition)
         {
            pds.add((IProcessDefinition) me);
         }
      }

      for (IProcessDefinition pd : pds)
      {
         List<IFormalParameter> formalParameters = pd.getFormalParameters();
         if (formalParameters!=null)
         {
            formalParametersPerProcess.put(pd.getId(), sortById(formalParameters));
         }
      }
      return formalParametersPerProcess;
   }

   private List<IFormalParameter> sortById(List<IFormalParameter> formalParameters)
   {
      TreeMap<String, IFormalParameter> map = new TreeMap<String, IFormalParameter>();

      for (IFormalParameter formalParameter : formalParameters)
      {
         map.put(formalParameter.getId(), formalParameter);
      }

      return new LinkedList<IFormalParameter>(map.values());
   }

   private void assembleWebServiceDefinitions(
         final Map<String, List<IFormalParameter>> formalParameters)
   {
      modifyModelNamespace();
      insertNamespaceDeclarations(formalParameters.values());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:types/xsd:schema/xsd:element[@name='startProcess']",
            new WebServiceRequestTypesAssembler());
      assembleWebServiceTemplate(
            formalParameters,
            "/wsdl:definitions/wsdl:types/xsd:schema/xsd:element[@name='startProcessResponse']",
            new WebServiceResponseTypesAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:message[@name='startProcessRequest']",
            new WebServiceMessageAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:message[@name='startProcessResponse']",
            new WebServiceMessageAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:portType/wsdl:operation[@name='startProcess']",
            new WebServiceOperationAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:binding/wsdl:operation[@name='startProcess']",
            new WebServiceBindingAssembler());

      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:types/xsd:schema/xsd:element[@name='getProcessResults']",
            new WebServiceRequestAssembler());
      assembleWebServiceTemplate(
            formalParameters,
            "/wsdl:definitions/wsdl:types/xsd:schema/xsd:element[@name='getProcessResultsResponse']",
            new WebServiceResponseTypesAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:message[@name='getProcessResultsRequest']",
            new WebServiceMessageAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:message[@name='getProcessResultsResponse']",
            new WebServiceMessageAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:portType/wsdl:operation[@name='getProcessResults']",
            new WebServiceOperationAssembler());
      assembleWebServiceTemplate(formalParameters,
            "/wsdl:definitions/wsdl:binding/wsdl:operation[@name='getProcessResults']",
            new WebServiceBindingAssembler());
   }

   private void modifyModelNamespace()
   {
      String modelId = getModelId();

      Element definitions = DomUtils.retrieveElementByXPath(wsdlDoc, "/wsdl:definitions",
            XPATH_CONTEXT);
//      definitions.setAttribute("xmlns:mdl", modelId);
      definitions.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:mdl", modelId);


      // set modelId as targetNamespace
      final Element typesElement = DomUtils.retrieveElementByXPath(wsdlDoc,
            "/wsdl:definitions/wsdl:types", XPATH_CONTEXT);
      Element schemaElement = DomUtils.getFirstChildElement(typesElement);

      Attr targetNamespace = schemaElement.getAttributeNode("targetNamespace");
      targetNamespace.setValue(modelId);
   }

   private void insertNamespaceDeclarations(
         final Collection<List<IFormalParameter>> formalParameters)
   {
      final Element typesElement = DomUtils.retrieveElementByXPath(wsdlDoc,
            "/wsdl:definitions/wsdl:types", XPATH_CONTEXT);
      Element schemaElement = DomUtils.getFirstChildElement(typesElement);

      final Set<String> namespacesToImport = new HashSet<String>();
      for (final List<IFormalParameter> fs : formalParameters)
      {
         for (final IFormalParameter f : fs)
         {
            if (!isPrimitiveType(f.getData()))
            {
               final NSPrefixPair nsPrefixPair = getNSPrefixPair(f);
               schemaElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"
                     + nsPrefixPair.prefix(), nsPrefixPair.namespace());
               namespacesToImport.add(nsPrefixPair.namespace());
            }
         }
      }

      final Element firstSchemaChild = DomUtils.getFirstChildElement(schemaElement);
      for (final String namespace : namespacesToImport)
      {
         final Element importElement = schemaElement.getOwnerDocument().createElementNS(XSD_SCHEMA_URL, "xsd:import");
         importElement.setAttribute("namespace", namespace);

        firstSchemaChild.getParentNode().insertBefore(importElement, firstSchemaChild);
      }
   }

   private NSPrefixPair getNSPrefixPair(
         IFormalParameter f)
   {

     return nsPairs.get(f.getId());
   }

   private void assembleWebServiceTemplate(
         final Map<String, List<IFormalParameter>> formalParameters,
         final String xPathQuery, final WebServiceAssembler assembler)
   {
      final Element templateElement = DomUtils.retrieveElementByXPath(wsdlDoc,
            xPathQuery, XPATH_CONTEXT);

      for (final Entry<String, List<IFormalParameter>> e : formalParameters.entrySet())
      {
         final String processId = e.getKey();
         final Element element = (Element) templateElement.cloneNode(true);

         assembler.assemble(e.getValue(), element, processId);

         templateElement.getParentNode().insertBefore(element, templateElement);
      }

      templateElement.getParentNode().removeChild(templateElement);
   }

   private void insertCustomTypeDefinitions(final Set<IFormalParameter> usedParameters)
   {
      final Element wsdlTypesNode = DomUtils.retrieveElementByXPath(wsdlDoc,
            "/wsdl:definitions/wsdl:types", XPATH_CONTEXT);

      for (final IFormalParameter f : usedParameters)
      {
         if (StructuredTypeRtUtils.isStructuredType(f.getData().getType().getId()))
         {
         String typeDeclarationId = (String) f.getData().getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);

         ITypeDeclaration typeDef = this.model.findTypeDeclaration(typeDeclarationId);
         IXpdlType xpdlType = typeDef.getXpdlType();
         org.w3c.dom.Element schemaW3CElement = null;
         if (xpdlType instanceof IExternalReference)
         {
            schemaW3CElement = ((IExternalReference) xpdlType).getSchema(this.model).getDocument()
            .getDocumentElement();
         }
         else if (xpdlType instanceof ISchemaType)
         {
            schemaW3CElement = ((ISchemaType) xpdlType).getSchema().getDocument()
            .getDocumentElement();
         }
         insertElementDefinition(f, schemaW3CElement, typeDeclarationId);
            wsdlTypesNode.appendChild(wsdlTypesNode.getOwnerDocument().importNode(
                  schemaW3CElement, true));
         }
      }
   }

   private void insertElementDefinition(final IFormalParameter f,
         final Element schemaElement, final String type)
   {
      final String targetNamespace = schemaElement.getAttribute("targetNamespace");

      final int nsIndex = NamespaceHolder.getNamespaceIndex(targetNamespace);
      String prefix = "ns" + nsIndex;
      this.nsPairs.put(f.getId(), new NSPrefixPair(prefix, targetNamespace));

      final Set<String> elementNames = new HashSet<String>();

      elementNames.add(type);

      final Element elementDefTemplate = DomUtils.getFirstChildElement(schemaElement);
      for (final String elementName : elementNames)
      {
         final Element element = (Element) elementDefTemplate.cloneNode(true);
         element.setAttribute("name", elementName);
         elementDefTemplate.getParentNode().insertBefore(element, elementDefTemplate);
      }
      elementDefTemplate.getParentNode().removeChild(elementDefTemplate);
   }

   private Set<IFormalParameter> mergeAndReduceNonCustomTypes(
         final Map<String, List<IFormalParameter>> usedTypes)
   {
      final Set<IFormalParameter> mergedTypes = new HashSet<IFormalParameter>();

      for (final List<IFormalParameter> l : usedTypes.values())
      {
         for (final IFormalParameter f : l)
         {
            if (!isPrimitiveType(f.getData()))
            {
               mergedTypes.add(f);
            }
         }
      }

      return mergedTypes;
   }

   private Element createElementElement(Element parent, final IFormalParameter f, NSPrefixPair nsPrefixPair)
   {
      Document doc = parent.getOwnerDocument();
      final Element element = doc.createElementNS(XSD_SCHEMA_URL, "xsd:element");

      if (isPrimitiveType(f.getData()))
      {
         Type primitiveType = (Type) f.getData().getAttribute(TYPE_ATT);
         QName type = DataFlowUtils.marshalPrimitiveType(primitiveType);

         element.setAttribute("name", f.getId());
         element.setAttribute("type", "xsd" + ":" + type.getLocalPart());
      }
      else if (StructuredTypeRtUtils.isStructuredType(f.getData().getType().getId()))
      {
         String typeDeclarationId = (String) f.getData().getAttribute(
               StructuredDataConstants.TYPE_DECLARATION_ATT);

         final Element reference = doc.createElementNS(XSD_SCHEMA_URL, "xsd:element");
         reference.setAttribute("ref", nsPrefixPair.prefix() + ":"
               + typeDeclarationId);

         final Element sequence = doc.createElementNS(XSD_SCHEMA_URL, "xsd:sequence");
         sequence.appendChild(reference);

         final Element complexType = doc.createElementNS(XSD_SCHEMA_URL, "xsd:complexType");
         complexType.appendChild(sequence);

         element.setAttribute("name", f.getId());
         element.appendChild(complexType);

      }

      parent.appendChild(element);

      return element;
   }

   private boolean isPrimitiveType(IData data)
   {
      return PredefinedConstants.PRIMITIVE_DATA.equals(data.getType().getId());
   }

   private interface WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId);
   }

   private final class WebServiceRequestAssembler implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);
      }
   }

   private final class WebServiceRequestTypesAssembler
         implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);

         final Element requestMessageTypes = DomUtils.retrieveElementByXPath(
               element,
               "xsd:complexType/xsd:sequence/xsd:element[@name='Args']/xsd:complexType/xsd:sequence",
               XPATH_CONTEXT);

         for (final IFormalParameter f : formalParameters)
         {
            if (Direction.IN.equals(f.getDirection())|| Direction.IN_OUT.equals(f.getDirection()))
            {
               createElementElement(requestMessageTypes, f, getNSPrefixPair(f));
            }
         }
      }
   }

   private final class WebServiceResponseTypesAssembler
         implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);

         final Element responseMessageTypes = DomUtils.retrieveElementByXPath(
               element,
               "xsd:complexType/xsd:sequence/xsd:element[@name='Return']/xsd:complexType/xsd:sequence",
               XPATH_CONTEXT);

         for (final IFormalParameter f : formalParameters)
         {
            if (Direction.OUT.equals(f.getDirection())|| Direction.IN_OUT.equals(f.getDirection()))
            {
               createElementElement(responseMessageTypes, f, getNSPrefixPair(f));
            }
         }
      }
   }

   private final class WebServiceMessageAssembler implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);
         final Element requestMessagePart = DomUtils.getFirstChildElement(element);
         DomUtils.appendAttributeValuePostfix(requestMessagePart, "element", processId);
      }
   }

   private final class WebServiceOperationAssembler implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);

         final List<Element> elementChilds = DomUtils.getChildElements(element);
         final Element operationInput = (Element) elementChilds.get(0);
         DomUtils.appendAttributeValuePostfix(operationInput, "message", processId);
         final Element operationOutput = (Element) elementChilds.get(1);
         DomUtils.appendAttributeValuePostfix(operationOutput, "message", processId);
      }
   }

   private final class WebServiceBindingAssembler implements WebServiceAssembler
   {
      public void assemble(final List<IFormalParameter> formalParameters,
            final Element element, final String processId)
      {
         DomUtils.appendAttributeValuePostfix(element, "name", processId);

         final Element soapOperation = DomUtils.getFirstChildElement(element);
         DomUtils.appendAttributeValuePostfix(soapOperation, "soapAction", processId);
      }
   }

   private static final class NamespaceHolder
   {
      private static final Map<String, Integer> namespaceDeclarations = new HashMap<String, Integer>();

      public static int getNamespaceIndex(final String namespace)
      {
         if (namespaceDeclarations.containsKey(namespace))
         {
            return namespaceDeclarations.get(namespace);
         }
         else
         {
            final int nsIndex = namespaceDeclarations.size();
            namespaceDeclarations.put(namespace, nsIndex);
            return nsIndex;
         }
      }
   }

   public static class NSPrefixPair extends Pair<String, String>
   {
      private static final long serialVersionUID = 8692652926850310799L;

      public static final NSPrefixPair XSD_NS_PAIR = new NSPrefixPair("xsd", XSD_SCHEMA_URL);

      public NSPrefixPair(final String prefix, final String namespace)
      {
         super(prefix, namespace);

         WsUtils.ensureNeitherNullNorEmpty(prefix, "Namespace Prefix");
         WsUtils.ensureNeitherNullNorEmpty(namespace, "Namespace");
      }

      public String prefix()
      {
         return getFirst();
      }

      public String namespace()
      {
         return getSecond();
      }
   }
}
