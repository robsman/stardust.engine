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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.utils.xml.StaticNamespaceContext;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IFormalParameter;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.ws.DataFlowUtils;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.impl.XSDImportImpl;
import org.eclipse.xsd.impl.XSDIncludeImpl;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

   private Map<String, NSPrefixPair> nsPairs = CollectionUtils.newMap();

   private ISchemaResolver schemaResolver;

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
      this.model = new DefaultXMLReader(false).importFromXML(new StringReader(
            modelString2));

      InputStream wsdlInputStream = WSDLGenerator.class.getResourceAsStream(WSDL_TEMPLATE_PATH);
      this.wsdlDoc = DomUtils.createDocument(wsdlInputStream, WSDL_TEMPLATE_PATH);
   }

   @Deprecated
   public WSDLGenerator(final byte[] modelBytes)
   {
      this(new String(modelBytes));
   }

   public WSDLGenerator(String modelString, ISchemaResolver schemaResolver)
   {
      this(modelString);
      this.schemaResolver = schemaResolver;
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
      Map<String, List<IFormalParameter>> formalParametersPerProcess = CollectionUtils.newMap();
      List<IProcessDefinition> pds = new ArrayList<IProcessDefinition>();

      Link mel = (Link) this.model.getProcessDefinitions();
      for (Iterator< ? > iterator = mel.iterator(); iterator.hasNext();)
      {
         Object me = iterator.next();
         if (me instanceof IProcessDefinition)
         {
            pds.add((IProcessDefinition) me);
         }
      }

      for (IProcessDefinition pd : pds)
      {
         if (PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP.equals(pd.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE))
               || PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH.equals(pd.getAttribute(PredefinedConstants.PROCESSINTERFACE_INVOCATION_TYPE)))
         {
            List<IFormalParameter> formalParameters = pd.getFormalParameters();
            if (formalParameters != null)
            {
               formalParametersPerProcess.put(pd.getId(), sortById(formalParameters));
            }
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

      assembleWebServiceTemplate(
            formalParameters,
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
      // definitions.setAttribute("xmlns:mdl", modelId);
      definitions.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:mdl",
            modelId);

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
            if ( !isPrimitiveType(f.getData()))
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
         final Element importElement = schemaElement.getOwnerDocument().createElementNS(
               XSD_SCHEMA_URL, "xsd:import");
         importElement.setAttribute("namespace", namespace);

         firstSchemaChild.getParentNode().insertBefore(importElement, firstSchemaChild);
      }
   }

   private NSPrefixPair getNSPrefixPair(IFormalParameter f)
   {

      return nsPairs.get(f.getId() + f.getData().getId());
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
      TreeMap<String, XSDSchema> schemaMap = CollectionUtils.newTreeMap();
      Set<String> unresolvedSchemaLocations = CollectionUtils.newHashSet();

      for (final IFormalParameter f : usedParameters)
      {
         if (StructuredTypeRtUtils.isStructuredType(f.getData().getType().getId()))
         {

            String typeDeclarationId = null;
            XSDSchema xsdSchema = null;
            String xsdSchemaModelId = null;

            IReference extReference = f.getData().getExternalReference();
            if (extReference != null && this.schemaResolver != null)
            {
               typeDeclarationId = (String) extReference.getId();

               xsdSchemaModelId = extReference.getExternalPackage().getHref();
               xsdSchema = this.schemaResolver.resolveSchema(xsdSchemaModelId, typeDeclarationId);
            }
            else
            {
               typeDeclarationId = (String) f.getData().getAttribute(
                     StructuredDataConstants.TYPE_DECLARATION_ATT);

               ITypeDeclaration typeDef = this.model.findTypeDeclaration(typeDeclarationId);
               if (typeDef != null)
               {
		  xsdSchemaModelId = this.model.getId();
                  xsdSchema = StructuredTypeRtUtils.getXSDSchema(this.model, typeDef);
               }
            }

            if (xsdSchema != null)
            {
               schemaMap.put(xsdSchema.getSchemaLocation(), xsdSchema);

               // save namespace pairs for imports
               final String targetNamespace = xsdSchema.getDocument()
                     .getDocumentElement()
                     .getAttribute("targetNamespace");
               final int nsIndex = NamespaceHolder.getNamespaceIndex(targetNamespace);
               String prefix = "ns" + nsIndex;
               this.nsPairs.put(f.getId() + f.getData().getId(), new NSPrefixPair(prefix,
                     targetNamespace));

               // resolve schemas transitively
               resolveSchemaImports(schemaMap, unresolvedSchemaLocations, xsdSchema, xsdSchemaModelId);
            }
         }

      }
      final Element wsdlTypesNode = DomUtils.retrieveElementByXPath(wsdlDoc,
            "/wsdl:definitions/wsdl:types", XPATH_CONTEXT);

      for (XSDSchema xsdSchema : schemaMap.values())
      {
         if (xsdSchema != null)
         {
            // modify the cloned xml document to be included in the WSDL, not the original which is used by the xsd schema.
            Element schemaW3CElement = ((Document) xsdSchema.getDocument().cloneNode(true)).getDocumentElement();

            // remove resolved schemaLocation attributes on xsd:import
            removeResolvedSchemaLocation(schemaW3CElement, unresolvedSchemaLocations);

            // remove resolved xsd:include
            removeResolvedInclude(schemaW3CElement, unresolvedSchemaLocations);

            insertElementDefinition(schemaW3CElement);
            wsdlTypesNode.appendChild(wsdlTypesNode.getOwnerDocument().importNode(
                  schemaW3CElement, true));
         }
      }
   }

   private void removeResolvedSchemaLocation(Element element,
         Set<String> unresolvedSchemaLocations)
   {
      NodeList imports = DomUtils.retrieveElementsByXPath(element,
            "/xsd:schema/xsd:import", XPATH_CONTEXT);

      for (int i = 0; i < imports.getLength(); i++ )
      {
         Node node = imports.item(i);

         NamedNodeMap attributes = node.getAttributes();

         Node namedItem = attributes.getNamedItem("schemaLocation");
         if (namedItem != null)
         {
            String schemaLocation = namedItem.getTextContent();
            if (schemaLocation != null
                  && !unresolvedSchemaLocations.contains(schemaLocation))
            {
               // remove schemaLocation Attribute
               attributes.removeNamedItem("schemaLocation");
            }
         }
      }
   }

   private void removeResolvedInclude(Element element,
	         Set<String> unresolvedSchemaLocations)
	   {
	      NodeList includes = DomUtils.retrieveElementsByXPath(element,
	            "/xsd:schema/xsd:include", XPATH_CONTEXT);

	      for (int i = 0; i < includes.getLength(); i++ )
	      {
	         Node node = includes.item(i);

	         NamedNodeMap attributes = node.getAttributes();

	         Node namedItem = attributes.getNamedItem("schemaLocation");
	         if (namedItem != null)
	         {
	            String schemaLocation = namedItem.getTextContent();
	            if (schemaLocation != null
	                  && !unresolvedSchemaLocations.contains(schemaLocation))
	            {
	               // remove xsd:include
	               node.getParentNode().removeChild(node);
	            }
	         }
	      }
	   }

   /**
    * Resolves all schemas imported by urn:internal imports
    * Resolves schema includes and imports on the classpath
    *
    * @param schemaMap
    *           After execution this map holds resolved schemas by schemaLocation.
    * @param unresolvedSchemaLocations
    *           The schemaLocation's which could not be resolved but should not be
    *           removed.
    * @param xsdSchema
    *           The schema to resolve imports and includes in.
    */
   private void resolveSchemaImports(TreeMap<String, XSDSchema> schemaMap,
         Set<String> unresolvedSchemaLocations, XSDSchema xsdSchema, String xsdSchemaModelId)
   {
      List< ? > contents = xsdSchema.getContents();
      for (int j = 0; j < contents.size(); j++ )
      {
         Object item = contents.get(j);
         if (item instanceof XSDImportImpl)
         {
            XSDImportImpl xsdImport = (XSDImportImpl) item;
            String schemaLocation = xsdImport.getSchemaLocation();
            if (schemaLocation != null
                  && schemaLocation.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
            {

               String toResolveTypeDeclarationId = schemaLocation.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
               QName uriRef = QName.valueOf(toResolveTypeDeclarationId);

               XSDSchema resolvedXsdSchema = null;
               if ( !schemaMap.containsKey(schemaLocation))
               {
                  // remove schemaLocation not handled here because eclipse XSD hangs
                  // 60sec at xsdImport.setSchemaLocation(null);

		  String resolvedXsdSchemaModelId = null;
                  if (uriRef.getNamespaceURI().isEmpty() && this.model.getId().equals(xsdSchemaModelId))
                  {
			 // xsd is in same model as formal parameters.
                     ITypeDeclaration toResolveTypeDef = this.model.findTypeDeclaration(toResolveTypeDeclarationId);
                     if (toResolveTypeDef != null)
                     {
                        resolvedXsdSchema = StructuredTypeRtUtils.getXSDSchema(
                              this.model, toResolveTypeDef);
                        resolvedXsdSchemaModelId = this.model.getId();
                     }
                  }
                  else if (uriRef.getNamespaceURI().isEmpty())
                  {
			 // xsd is in same model as resolved schema but not the one with formal parameters.
                     if (this.schemaResolver != null)
                     {
			resolvedXsdSchemaModelId = xsdSchemaModelId;
                        resolvedXsdSchema = this.schemaResolver.resolveSchema(
					resolvedXsdSchemaModelId, uriRef.getLocalPart());
                     }
                  }
                  else
                  {
			 // xsd is in other model.
                     if (this.schemaResolver != null)
                     {
			resolvedXsdSchemaModelId = uriRef.getNamespaceURI();
                        resolvedXsdSchema = this.schemaResolver.resolveSchema(
					resolvedXsdSchemaModelId, uriRef.getLocalPart());
                     }
                  }

                  if (resolvedXsdSchema != null)
                  {

                     schemaMap.put(resolvedXsdSchema.getSchemaLocation(), resolvedXsdSchema);

                     // recurse until all includes/imports are resolved.
                     resolveSchemaImports(schemaMap, unresolvedSchemaLocations,
                           resolvedXsdSchema, resolvedXsdSchemaModelId);
                  }
                  else
                  {
                     // could not resolve urn:internal: schema but urn:internal: schema
                     // location still needs to be removed for a valid WSDL.
                  }
               }
            }
            else if (schemaLocation != null)
            {
               XSDSchema resolvedXsdSchema = xsdImport.getResolvedSchema();
               if (resolvedXsdSchema == null)
               {
                  xsdImport.reset();
                  xsdImport.importSchema();
               }
               resolvedXsdSchema = xsdImport.getResolvedSchema();

               if (resolvedXsdSchema == null)
               {
                  // try to search on classpath
                  resolvedXsdSchema = resolveFromClasspath(schemaLocation);
               }

               if (resolvedXsdSchema != null)
               {
                  schemaMap.put(resolvedXsdSchema.getSchemaLocation(), resolvedXsdSchema);

                  // recurse until all includes/imports are resolved.
                  resolveSchemaImports(schemaMap, unresolvedSchemaLocations,
                        resolvedXsdSchema, xsdSchemaModelId);
               }
               else
               {
                  // failed to resolve import.
                  unresolvedSchemaLocations.add(schemaLocation);
               }
            }
         }
         else if (item instanceof XSDIncludeImpl)
         {
		 XSDIncludeImpl xsdInclude = (XSDIncludeImpl) item;
             String schemaLocation = xsdInclude.getSchemaLocation();
             if (schemaLocation != null)
             {
                 XSDSchema resolvedXsdSchema = xsdInclude.getResolvedSchema();

                 if (resolvedXsdSchema == null)
                 {
                    // try to search on classpath
                    resolvedXsdSchema = resolveFromClasspath(schemaLocation);
                 }

                 if (resolvedXsdSchema != null)
                 {
                    schemaMap.put(resolvedXsdSchema.getSchemaLocation(), resolvedXsdSchema);

                    // recurse until all imports/includes are resolved.
                    resolveSchemaImports(schemaMap, unresolvedSchemaLocations,
                          resolvedXsdSchema, xsdSchemaModelId);
                 }
                 else
                 {
                    // failed to resolve include.
                    unresolvedSchemaLocations.add(schemaLocation);
                 }
             }
         }
      }
   }

   private XSDSchema resolveFromClasspath(String schemaLocation)
   {
      XSDSchema resolvedXsdSchema = null;
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL schemaUrl = null;
      if (cl != null)
      {
         schemaUrl = cl.getResource(schemaLocation);
      }
      else
      {
         schemaUrl = this.getClass().getResource(schemaLocation);
      }
      if (schemaUrl != null)
      {
         ResourceSet resourceSet = new ResourceSetImpl();
         XSDResourceImpl xsdMainResource;
         try
         {
            xsdMainResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(schemaUrl.toURI()
                  .toString()));

            try
            {
               xsdMainResource.load(resourceSet.getLoadOptions());
            }
            catch (IOException e)
            {
               throw new RuntimeException(e);
            }
         }
         catch (URISyntaxException e1)
         {
            throw new RuntimeException(e1);
         }

         for (Object resource : resourceSet.getResources())
         {
            if (resource instanceof XSDResourceImpl)
            {
               XSDResourceImpl xsdResource = (XSDResourceImpl) resource;
               resolvedXsdSchema = xsdResource.getSchema();
               break;
            }
         }
      }
      return resolvedXsdSchema;
   }

   private void insertElementDefinition(final Element schemaElement)
   {
      final Element elementDefTemplate = DomUtils.getFirstChildElement(schemaElement);
      final Element element = (Element) elementDefTemplate.cloneNode(true);
      elementDefTemplate.getParentNode().insertBefore(element, elementDefTemplate);
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
            if ( !isPrimitiveType(f.getData()))
            {
               mergedTypes.add(f);
            }
         }
      }

      return mergedTypes;
   }

   private Element createElementElement(Element parent, final IFormalParameter f,
         NSPrefixPair nsPrefixPair)
   {
      Document doc = parent.getOwnerDocument();
      final Element element = doc.createElementNS(XSD_SCHEMA_URL, "xsd:element");

      if (isPrimitiveType(f.getData()))
      {
         Type primitiveType = (Type) f.getData().getAttribute(TYPE_ATT);
         
         if (Type.Enumeration.equals(primitiveType))
         {
            element.setAttribute("name", f.getId());
            
            
         }
         else
         {
            QName type = DataFlowUtils.marshalPrimitiveType(primitiveType);

            element.setAttribute("name", f.getId());
            element.setAttribute("type", "xsd" + ":" + type.getLocalPart());
         }
      }
      else if (StructuredTypeRtUtils.isStructuredType(f.getData().getType().getId()))
      {

         String typeDeclarationId = null;

         IReference extReference = f.getData().getExternalReference();
         if (extReference != null && this.schemaResolver != null)
         {
            typeDeclarationId = (String) extReference.getId();
         }
         else
         {
            typeDeclarationId = (String) f.getData().getAttribute(
                  StructuredDataConstants.TYPE_DECLARATION_ATT);
         }

         final Element reference = doc.createElementNS(XSD_SCHEMA_URL, "xsd:element");
         reference.setAttribute("ref", nsPrefixPair.prefix() + ":" + typeDeclarationId);

         final Element sequence = doc.createElementNS(XSD_SCHEMA_URL, "xsd:sequence");
         sequence.appendChild(reference);

         final Element complexType = doc.createElementNS(XSD_SCHEMA_URL,
               "xsd:complexType");
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

   private final class WebServiceRequestTypesAssembler implements WebServiceAssembler
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
            if (Direction.IN.equals(f.getDirection())
                  || Direction.IN_OUT.equals(f.getDirection()))
            {
               createElementElement(requestMessageTypes, f, getNSPrefixPair(f));
            }
         }
      }
   }

   private final class WebServiceResponseTypesAssembler implements WebServiceAssembler
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
            if (Direction.OUT.equals(f.getDirection())
                  || Direction.IN_OUT.equals(f.getDirection()))
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

      public static final NSPrefixPair XSD_NS_PAIR = new NSPrefixPair("xsd",
            XSD_SCHEMA_URL);

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
