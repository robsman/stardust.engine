/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.emfxsd.ClasspathUriConverter;
import org.eclipse.stardust.engine.core.struct.emfxsd.CustomURIConverter;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.spi.ISchemaTypeProvider;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.emfxsd.DmsSchemaProvider;
import org.eclipse.xsd.*;
import org.eclipse.xsd.impl.XSDImportImpl;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.w3c.dom.*;

public class StructuredTypeRtUtils
{
   private static final Logger trace = LogManager.getLogger(StructuredTypeRtUtils.class);

   private static final String EXTERNAL_SCHEMA_MAP = "com.infinity.bpm.rt.data.structured.ExternalSchemaMap";

   private static final XSDResourceFactoryImpl XSD_RESOURCE_FACTORY = new XSDResourceFactoryImpl();
   
   public static ThreadLocal<CustomURIConverter> uriConverters = new ThreadLocal<CustomURIConverter>()
   {
      @Override
      protected CustomURIConverter initialValue()
      {
         return new ClasspathUriConverter();
      }
   };

   public static IXPathMap getXPathMap(IData data)
   {
      String typeId = data.getType().getId();
      if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
      {
         IReference ref = data.getExternalReference();
         if (ref != null)
         {
            return new ClientXPathMap(getAllXPaths(ref));
         }
         String typeDeclarationId = data.getStringAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         
         IModel model = (IModel) data.getModel();
         ITypeDeclaration typeDeclaration = model.findTypeDeclaration(typeDeclarationId);
         if (typeDeclaration == null)
         {
            return null;
         }
         return new ClientXPathMap(getAllXPaths(model, typeDeclaration));
      }
      else
      {
         // build-in schema
         String metadataComplexTypeName = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         IModel model = (IModel) data.getModel();
         ITypeDeclaration metadataTypeDeclaration = model.findTypeDeclaration(metadataComplexTypeName);
         
         XSDNamedComponent metadataXsdComponent = null;
         if (metadataTypeDeclaration != null)
         {
            XSDSchema metadataSchema = StructuredTypeRtUtils.getXSDSchema(model, metadataTypeDeclaration);
            metadataXsdComponent = StructuredTypeRtUtils.findElementOrTypeDeclaration(metadataSchema, metadataTypeDeclaration.getId(), true);
         }

         return getBuiltInXPathMap(typeId, metadataXsdComponent);
      }
   }

   /**
    * Retrieves the XPath map corresponding to the data argument.
    * 
    * @param model the model containing the type declaration the data is referring to.
    * @param data the data object for which we want to retrive the xpath map.
    * @return the XPath map (may be empty).
    */
   public static IXPathMap getXPathMap(Model model, Data data)
   {
      String dataTypeId = data.getTypeId();
      if (dataTypeId.equals(StructuredDataConstants.STRUCTURED_DATA))
      {
         // user-defined structured data
         Reference ref = data.getReference();
         String typeDeclarationId = ref == null
            ? (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT)
            : ref.getId();
         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
         return new ClientXPathMap(getAllXPaths(model, typeDeclaration));
      }
      else
      {
         // build-in schema
         String metadataComplexTypeName = (String)data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         TypeDeclaration metadataTypeDeclaration = model.getTypeDeclaration(metadataComplexTypeName);
         
         XSDNamedComponent metadataXsdComponent = null;
         if (metadataTypeDeclaration != null)
         {
            XSDSchema metadataSchema = StructuredTypeRtUtils.getXSDSchema(model, metadataTypeDeclaration);
            metadataXsdComponent = StructuredTypeRtUtils.findElementOrTypeDeclaration(metadataSchema, metadataTypeDeclaration.getId(), true);
         }

         return getBuiltInXPathMap(dataTypeId, metadataXsdComponent);
      }
   }

   private static IXPathMap getBuiltInXPathMap(String dataTypeId,
         XSDNamedComponent metadataXsdComponent)
   {
      Map parameters = metadataXsdComponent == null
            ? Collections.emptyMap()
            : Collections.singletonMap(DmsSchemaProvider.PARAMETER_METADATA_TYPE, metadataXsdComponent);

      for (ISchemaTypeProvider.Factory stpFactory : ExtensionProviderUtils.getExtensionProviders(ISchemaTypeProvider.Factory.class))
      {
         ISchemaTypeProvider provider = stpFactory.getSchemaTypeProvider(dataTypeId);
         if (null != provider)
         {
            Set result = provider.getSchemaType(dataTypeId, parameters);
            if (null != result)
            {
               return new ClientXPathMap(result);
            }
         }
      }
      throw new InternalException("Could not find predefined XPaths for data type '"
            + dataTypeId + "'. Check if schema providers are configured correctly.");
   }
   
   public static Set<TypedXPath> getAllXPaths(IModel model, String declaredTypeId)
   {
      if (declaredTypeId != null && declaredTypeId.startsWith("typeDeclaration:{"))
      {
         QName qname = QName.valueOf(declaredTypeId.substring(16));
         IExternalPackage pkg = model.findExternalPackage(qname.getNamespaceURI());
         if (pkg != null)
         {
            IModel otherModel = pkg.getReferencedModel();
            if (otherModel != null)
            {
               model = otherModel;
               declaredTypeId = qname.getLocalPart();
            }
         }
      }
      ITypeDeclaration typeDeclaration = model.findTypeDeclaration(declaredTypeId);
      return getAllXPaths(model, typeDeclaration);
   }
   
   public static Set<TypedXPath> getAllXPaths(IReference reference)
   {
      IExternalPackage pkg = reference.getExternalPackage();
      if (pkg != null)
      {
         IModel model = pkg.getReferencedModel();
         if (model != null)
         {
            return getAllXPaths(model, reference.getId());
         }
         else
         {
            trace.info("Unable to find referenced model for: " + pkg.getHref());
         }
      }
      return Collections.emptySet();
   }
   
   public static Set<TypedXPath> getAllXPaths(Model model, TypeDeclaration typeDeclaration)
   {
      XpdlType xpdlType = typeDeclaration.getXpdlType();

      if (xpdlType instanceof ExternalReference)
      {
         // ExternalReference
         ExternalReference externalReference = (ExternalReference)xpdlType;
         
         XSDSchema xsdSchema = externalReference.getSchema(model);
         return XPathFinder.findAllXPaths(xsdSchema, externalReference.getXref(), false);
      } 
      else if (xpdlType instanceof SchemaType)
      {
         // Internally defined type
         return XPathFinder.findAllXPaths(((SchemaType)xpdlType).getSchema(), typeDeclaration.getId(), true);
      }
      else
      {
         throw new RuntimeException(
               "Neither external reference not schema type is set in the type declaration for '"
                     + typeDeclaration.getId() + "'.");
      }
   }

   public static XSDSchema getXSDSchema(Model model, TypeDeclaration typeDeclaration)
   {
      XpdlType xpdlType = typeDeclaration.getXpdlType();

      if (xpdlType instanceof ExternalReference)
      {
         // ExternalReference
         ExternalReference externalReference = (ExternalReference)xpdlType;
         return externalReference.getSchema(model);
      } 
      else if (xpdlType instanceof SchemaType)
      {
         // Internally defined type
         return ((SchemaType)xpdlType).getSchema();
      }
      else
      {
         throw new RuntimeException(
               "Neither external reference not schema type is set in the type declaration for '"
                     + typeDeclaration.getId() + "'.");
      }
   }

   public static Set<TypedXPath> getAllXPaths(IModel model, ITypeDeclaration typeDeclaration)
   {
      IXpdlType xpdlType = typeDeclaration.getXpdlType();

      if (xpdlType instanceof IExternalReference)
      {
         // ExternalReference
         IExternalReference externalReference = (IExternalReference)xpdlType;
         
         XSDSchema xsdSchema = externalReference.getSchema(model);
         // (fh) moved below call to synchronized method ExternalReferenceBean.getExternalSchema
         // patchAnnotations(xsdSchema, externalReference.getExternalAnnotations());
         return XPathFinder.findAllXPaths(xsdSchema, externalReference.getXref(), false);
      } 
      else if (xpdlType instanceof ISchemaType)
      {
         // Internally defined type
         return XPathFinder.findAllXPaths(((ISchemaType)xpdlType).getSchema(), typeDeclaration.getId(), true);
      }
      else
      {
         throw new RuntimeException(
               "Neither external reference not schema type is set in the type declaration for '"
                     + typeDeclaration.getId() + "'.");
      }
   }

   public static XSDSchema getXSDSchema(IModel model, ITypeDeclaration typeDeclaration)
   {
      IXpdlType xpdlType = typeDeclaration.getXpdlType();
      
      if (xpdlType instanceof IExternalReference)
      {
         // ExternalReference
         IExternalReference externalReference = (IExternalReference)xpdlType;
         
         XSDSchema xsdSchema = externalReference.getSchema(model);
         // (fh) moved below call to synchronized method ExternalReferenceBean.getExternalSchema
         // patchAnnotations(xsdSchema, externalReference.getExternalAnnotations());
         return xsdSchema;
      } 
      else if (xpdlType instanceof ISchemaType)
      {
         // Internally defined type
         return ((ISchemaType)xpdlType).getSchema();
      }
      else
      {
         throw new RuntimeException(
               "Neither external reference not schema type is set in the type declaration for '"
                     + typeDeclaration.getId() + "'.");
      }
   }
   
   public static void patchAnnotations(XSDSchema xsdSchema, Element externalAnnotations)
   {
      if (externalAnnotations == null)
      {
         return;
      }
      
      NodeList appInfoNodes = externalAnnotations.getElementsByTagNameNS(DefaultXMLReader.NS_XSD_2001, XSDConstants.APPINFO_ELEMENT_TAG);
      for (int i = 0; i < appInfoNodes.getLength(); i++)
      {
         Element appInfo = (Element)appInfoNodes.item(i);
         
         String sourcePath = appInfo.getAttribute(XSDConstants.SOURCE_ATTRIBUTE);
         if (!StringUtils.isEmpty(sourcePath))
         {
            patchAnnotationsForSourcePath(xsdSchema, sourcePath, appInfo.getChildNodes());
         }
      }
   }

   private static void patchAnnotationsForSourcePath(XSDSchema xsdSchema, String sourcePath, NodeList childNodes)
   {
      String [] sourcePathParts = sourcePath.split("/");
      XSDComponent component = findElementOrTypeDeclaration(xsdSchema, sourcePathParts[0], false);
      
      if (component != null)
      {
         for (int i = 1; i < sourcePathParts.length; i++)
         {
            component = findDefinitionPart(component, sourcePathParts[i]);
         }
         
         if (component != null)
         {
            patchComponentAnnotations(component, childNodes);
         }
      }
   }

   public static XSDNamedComponent findElementOrTypeDeclaration(XSDSchema xsdSchema, String name, boolean returnFirstIfNoMatch)
   {
      XSDTypeDefinition xsdTypeDefinition = XPathFinder.findTypeDefinition(xsdSchema, name);
      if(xsdTypeDefinition != null)
      {
         return xsdTypeDefinition;
      }

      XSDElementDeclaration xsdElementDeclaration = XPathFinder.findElement(xsdSchema, name);
      if (xsdElementDeclaration != null)
      {
         return xsdElementDeclaration;
      }
      else if (returnFirstIfNoMatch)
      {
         EList elementDeclarations = xsdSchema.getElementDeclarations();
         if (elementDeclarations.size() == 1)
         {
            return (XSDElementDeclaration) elementDeclarations.get(0);
         }
         else
         {
            EList typeDefinitions = xsdSchema.getTypeDefinitions();
            if (elementDeclarations.isEmpty() && typeDefinitions.size() == 1)
            {
               return (XSDTypeDefinition) typeDefinitions.get(0);
            }
         }
      }
      return null;
   }

   private static XSDComponent findDefinitionPart(XSDComponent component, String name)
   {
      if (name.startsWith("@"))
      {
         name = name.substring(1);
         // attribute can only be specified in complex type definitions
         EList attributeContents;
         if (component instanceof XSDElementDeclaration)
         {
            attributeContents = ((XSDComplexTypeDefinition)((XSDElementDeclaration)component).getTypeDefinition()).getAttributeContents();
         }
         else if (component instanceof XSDComplexTypeDefinition)
         {
            attributeContents = ((XSDComplexTypeDefinition)component).getAttributeContents();
         }
         else
         {
            throw new RuntimeException("Unsupported type '"+component.getClass().getName()+"'");
         }

         for (int i=0; i<attributeContents.size(); i++)
         {
            XSDAttributeGroupContent xsdAttributeGroupContent = (XSDAttributeGroupContent)attributeContents.get(i);
            if (xsdAttributeGroupContent instanceof XSDAttributeUse)
            {
               XSDAttributeUse xsdAttributeUse = (XSDAttributeUse)xsdAttributeGroupContent;
               if (name.equals(xsdAttributeUse.getAttributeDeclaration().getName()))
               {
                  return xsdAttributeUse;
               }
            }
            else if (xsdAttributeGroupContent instanceof XSDAttributeGroupDefinition)
            {
               XSDAttributeGroupDefinition xsdAttributeGroupDefinition = (XSDAttributeGroupDefinition)xsdAttributeGroupContent;
               List /*<XSDAttributeUse>*/ xsdAttributeUses = xsdAttributeGroupDefinition.getResolvedAttributeGroupDefinition().getAttributeUses();
               for (int j=0; j<xsdAttributeUses.size(); j++)
               {
                  XSDAttributeUse xsdAttributeUse =  (XSDAttributeUse) xsdAttributeUses.get(j);
                  if (name.equals(xsdAttributeUse.getAttributeDeclaration().getName()))
                  {
                     return xsdAttributeUse;
                  }
               }
            }
            else
            {
               throw new RuntimeException("Unsupported XSD: "+xsdAttributeGroupContent);
            }
         }
         return null;
      }
      else
      {
         // element
         XSDComplexTypeContent xsdComplexTypeContent;
         if (component instanceof XSDElementDeclaration)
         {
            xsdComplexTypeContent = ((XSDComplexTypeDefinition)((XSDElementDeclaration)component).getTypeDefinition()).getContent();
         }
         else if (component instanceof XSDComplexTypeDefinition)
         {
            xsdComplexTypeContent = ((XSDComplexTypeDefinition)component).getContent();
         }
         else
         {
            throw new RuntimeException("Unsupported type '"+component.getClass().getName()+"'");
         }

         if (xsdComplexTypeContent == null)
         {
            // <complexType/>
            return null;
         }
         
         if (xsdComplexTypeContent instanceof XSDParticle)
         {
            XSDParticle xsdParticle = (XSDParticle)xsdComplexTypeContent;
            return findComponent(xsdParticle, name);
         } 
         else if (xsdComplexTypeContent instanceof XSDSimpleTypeDefinition)
         {
            // ignore, since this was already explored by recursion in getBigDataType()
         }
         else
         {
            throw new RuntimeException("Unsupported XSD: "+xsdComplexTypeContent);
         }
         return null;
      }
   }

   private static XSDComponent findComponent(XSDParticle xsdParticle, String name)
   {
      XSDTerm xsdTerm = xsdParticle.getTerm();
      
      if (xsdTerm instanceof XSDModelGroup)
      {
         XSDModelGroup xsdModelGroup = (XSDModelGroup)xsdTerm;
         // all, choice, sequence
         for (Iterator elements = xsdModelGroup.getContents().iterator(); elements.hasNext(); )
         {
            XSDParticle childParticle = (XSDParticle) elements.next();
            
            if (childParticle.getTerm() instanceof XSDElementDeclaration)
            {
               XSDElementDeclaration xsdElementDeclaration = (XSDElementDeclaration)childParticle.getTerm();
               if (name.equals(xsdElementDeclaration.getName()))
               {
                  return xsdElementDeclaration;
               }
            }
            else
            {
               return findComponent(childParticle, name);
            }
         }
      }
      else if (xsdTerm instanceof XSDWildcard)
      {
         // ignore wildcards, no specific type information can be retrieved
         return null;
      }
      else 
      {
         throw new RuntimeException("Unsupported XSD: "+xsdTerm);
      }
      return null;
   }

   private static void patchComponentAnnotations(XSDComponent component,
         NodeList patch)
   {
      XSDAnnotation annotation = null;
      if (component instanceof XSDElementDeclaration)
      {
         annotation = ((XSDElementDeclaration)component).getAnnotation();
      }
      else if (component instanceof XSDAttributeUse)
      {
         annotation = ((XSDAttributeUse)component).getContent().getAnnotation();
      }
      else if (component instanceof XSDComplexTypeDefinition)
      {
         annotation = ((XSDComplexTypeDefinition)component).getAnnotation();
      }
      else
      {
         throw new RuntimeException("Unsupported type '"+component.getClass().getName()+"'");
      }
      
      if (annotation == null)
      {
         annotation = XSDFactory.eINSTANCE.createXSDAnnotation();
         if (component instanceof XSDElementDeclaration)
         {
            ((XSDElementDeclaration)component).setAnnotation(annotation);
         }
         else if (component instanceof XSDAttributeUse)
         {
            ((XSDAttributeUse)component).getContent().setAnnotation(annotation);
         }
         else if (component instanceof XSDComplexTypeDefinition)
         {
            ((XSDComplexTypeDefinition)component).setAnnotation(annotation);
         }
      }
      List originalAnnotations = annotation.getApplicationInformation();
      for (int i = 0; i < patch.getLength(); i++)
      {
         Node n = patch.item(i);
         if (n.getNodeType() == Node.ELEMENT_NODE)
         {
            Element patchElement = (Element)n;
            Element originalElement = findExactlyOneAppInfoChildElement(originalAnnotations, patchElement.getNamespaceURI(), patchElement.getLocalName());
            if (originalElement == null)
            {
               Node newAppInfoSubelement = component.getElement().getOwnerDocument().importNode(patchElement, true);
               if (originalAnnotations.size() > 0)
               {
                  // put the newAppInfoSubelement to an existing appinfo
                  ((Element)originalAnnotations.get(0)).appendChild(newAppInfoSubelement);
               }
               else
               {
                  // no appinfos yet, create one
                  Element newAppInfoElement = component.getElement()
                        .getOwnerDocument()
                        .createElementNS(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001,
                              XSDConstants.APPINFO_ELEMENT_TAG);
                  newAppInfoElement.appendChild(newAppInfoSubelement);
                  originalAnnotations.add(newAppInfoElement);
               }
            }
            else
            {
               patchElement(originalElement, patchElement);
            }
         }
      }
   }

   private static Element findExactlyOneAppInfoChildElement(List originalAnnotations,
         String namespaceUri, String localName)
   {
      for (int i = 0; i < originalAnnotations.size(); i++)
      {
         Element appInfoElement = (Element)originalAnnotations.get(i);
         Element childElement = findExactlyOneChildElement(appInfoElement, namespaceUri, localName);
         if (childElement != null)
         {
            return childElement;
         }
      }
      return null;
   }

   private static void patchElement(Element originalElement, Element patchElement)
   {
      // for every element, overwrite the attributes, if it has no child elements 
      // and the trimmed text content is not empty, overwrite the content of 
      // the patched element, recurse for children

      NamedNodeMap attributes = patchElement.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++)
      {
         Attr attribute = (Attr)attributes.item(i);
         originalElement.setAttributeNS(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getValue());
      }

      NodeList patchChildren = patchElement.getElementsByTagName("*");
      
      String patchElementContent = findNodeValue(patchElement);
      if (patchChildren.getLength() == 0 && !StringUtils.isEmpty(patchElementContent))
      {
         // replace node value in original element
         replaceWithText(originalElement, patchElementContent);
      }
      else
      {
         for (int i = 0; i < patchChildren.getLength(); i++)
         {
            Element patchChild = (Element)patchChildren.item(i);
            Element originalChild = findExactlyOneChildElement(originalElement, patchChild.getNamespaceURI(), patchChild.getLocalName());
            if (originalChild == null)
            {
               originalElement.appendChild(originalElement.getOwnerDocument().importNode(patchChild, true));
            }
            else
            {
               patchElement(originalChild, patchChild);
            }
         }

      }
   }
   
   private static void replaceWithText(Element parentElement, String text)
   {
      NodeList nl = parentElement.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = (Node)nl.item(i);
         parentElement.removeChild(n);
      }
      parentElement.appendChild(parentElement.getOwnerDocument().createTextNode(text));
   }

   public static String findNodeValue(Node node)
   {
      // node value if any
      for (int i = 0; i < node.getChildNodes().getLength(); i++ )
      {
         Node childNode = node.getChildNodes().item(i);
         if (childNode.getNodeType() == Node.TEXT_NODE)
         {
            String nodeValue = childNode.getNodeValue();
            if (StringUtils.getNormalized(nodeValue) != null)
            {
               return childNode.getNodeValue();
            }
         }
      }
      return null;
   }

   private static Element findExactlyOneChildElement(Element parent, String namespaceUri, String localName)
   {
      // TODO (ab) should be getElementsByTagNameNS, but the namespace coming from the patch is 
      // the IPP namespace, elements can not be found this way, so try both
      
      NodeList nl = parent.getElementsByTagName(localName);
      if (nl.getLength() == 1)
      {
         return (Element)nl.item(0);
      }
      else
      {
         nl = parent.getElementsByTagNameNS(namespaceUri, localName);
         if (nl.getLength() == 1)
         {
            return (Element)nl.item(0);
         }
      }
      return null;
   }
   
   public static XSDSchema loadExternalSchema(String schemaLocation)
   {
      try
      {
         return getSchema(schemaLocation, null, null);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static URI getURI(CustomURIConverter uriConverter, String path)
   {
      URI uri = URI.createURI(path);
      //only normalize if its NOT an url(in this case it will have no scheme part)
      if(uriConverter != null && uri.scheme() == null)
      {
         uri = uriConverter.normalize(uri);
      }
      
      return uri;
   }   
   
   /**
    * Returns the first schema in the document at the specified location that matches the namespaceURI.
    * If there is no such schema, then it returns the first schema that has an import for the namespaceURI.
    * If the namespaceURI is null then the first schema in the document is returned.
    * 
    * @param location the document location either as an absolute 
    * @param namespaceURI the namespace to match the schema agains or null if the first schema should be returned
    * @param customMap - a map used for customizing the {@link CustomURIConverter}, it will be passed to the
    * {@link CustomURIConverter} via {@link CustomURIConverter#setCustomMap(Map)}, can be null
    * @return the XSDSchema or null if no schema matches the criteria above.
    * @throws IOException
    */
   public static XSDSchema getSchema(String location, String namespaceURI, Map customMap) throws IOException
   {
	  Parameters parameters = Parameters.instance();
	  Map loadedSchemas = null;
	  synchronized (StructuredTypeRtUtils.class)
	  {
		  loadedSchemas = (Map) parameters.get(EXTERNAL_SCHEMA_MAP);
		  if (loadedSchemas == null)
		  {
			 // (fh) using Hashtable to avoid concurrency problems.
			 loadedSchemas = new Hashtable();
			 parameters.set(EXTERNAL_SCHEMA_MAP, loadedSchemas);
		  }
	  }
	  String key = '{' + namespaceURI + '}' + location;
	  Object o = loadedSchemas.get(key);
	  if (o != null)
	  {
		  return o instanceof XSDSchema ? (XSDSchema) o : null;
	  }
	  
      ResourceSetImpl resourceSet = new ResourceSetImpl();
      //prepare the uri converter
      CustomURIConverter uriConverter = uriConverters.get();
      if (uriConverter != null)
      {
         uriConverter.setCustomMap(customMap);
         resourceSet.setURIConverter(uriConverter);
      }
      URI uri = getURI(uriConverter, location);
      
      // (fh) register the resource factory directly with the resource set and do not tamper with the global registry.
      resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(uri.scheme(), XSD_RESOURCE_FACTORY);
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", XSD_RESOURCE_FACTORY);
      Resource resource = resourceSet.createResource(uri);
      Map options = new HashMap();
      options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
      resource.load(options);
      
      boolean hasSchema = false;
      List l = resource.getContents();
      for (int i = 0; i < l.size(); i++)
      {
         EObject eObject = (EObject) l.get(i);
         if (eObject instanceof XSDSchema)
         {
            hasSchema = true;
            XSDSchema schema = (XSDSchema) eObject;
            if (namespaceURI == null || CompareHelper.areEqual(namespaceURI, schema.getTargetNamespace()))
            {
               resolveImports(schema);
               if (trace.isDebugEnabled())
               {
                  trace.debug("Found schema for namespace: " + namespaceURI + " at location: " + uri.toString());
               }
               loadedSchemas.put(key, schema);
               return schema;
            }
         }
      }
      
      // no schema matching the namespaceURI found, so try a second round by searching through imports.
      // this is indirect resolving, so it will return the first schema that has an import for the namespaceURI
      if (hasSchema)
      {
         for (int i = 0; i < l.size(); i++)
         {
            EObject eObject = (EObject) l.get(i);
            if (eObject instanceof XSDSchema)
            {
               XSDSchema schema = (XSDSchema) eObject;
               List contents = schema.getContents();
               for (int j = 0; j < contents.size(); j++)
               {
                  Object item = contents.get(j);
                  if (item instanceof XSDImportImpl)
                  {
                     XSDImportImpl directive = (XSDImportImpl) item;
                     XSDSchema ref = directive.importSchema();
                     if (ref != null && CompareHelper.areEqual(namespaceURI, ref.getTargetNamespace()))
                     {
                        resolveImports(schema);
                        if (trace.isDebugEnabled())
                        {
                           trace.debug("Found schema for namespace: " + namespaceURI + " at location: " + uri.toString());
                        }
                        loadedSchemas.put(key, schema);
                        return schema;
                     }
                  }
               }
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("No schema found for namespace: " + namespaceURI + " at location: " + uri.toString());
      }
      loadedSchemas.put(key, "NULL");
      return null;
   }

   public static XSDSchema deserializeSchema(byte [] xsdSchema)
   {
      ResourceSet resourceSet = new ResourceSetImpl();
      URI uri = URI.createURI("http://dummy/dummy.xsd");
      resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(uri.scheme(), XSD_RESOURCE_FACTORY);
      Resource resource = resourceSet.createResource(uri);
      try
      {
         HashMap options = new HashMap();
         options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
         resource.load(new ByteArrayInputStream(xsdSchema), options);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      List l = resource.getContents();
      return(XSDSchema) l.get(0);
   }
   
   public static byte [] serializeSchema(XSDSchema xsdSchema)
   {
      HashMap options = new HashMap();
      //options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
      try
      {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         xsdSchema.eResource().save(out, options);
         out.flush();
         return out.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   public static String parseLocalName(String qNameAsString)
   {
      if (qNameAsString == null)
      {
         return null;
      }

      if (qNameAsString.length() == 0)
      {
         return qNameAsString;
      }

      // local part only?
      if (qNameAsString.charAt(0) != '{')
      {
          return qNameAsString;
      }

      // Namespace URI improperly specified?
      if (qNameAsString.startsWith("{}"))
      {
         return qNameAsString.substring(2);
      }

      // Namespace URI and local part specified
      int endOfNamespaceURI = qNameAsString.indexOf('}');
      if (endOfNamespaceURI == -1)
      {
          throw new IllegalArgumentException(
              "Cannot parse QName from \"" + qNameAsString + "\", missing closing \"}\"");
      }
      return qNameAsString.substring(endOfNamespaceURI + 1);
   }

   public static String parseNamespaceURI(String qNameAsString)
   {
      if (qNameAsString == null)
      {
         return null;
      }

      if (qNameAsString.length() == 0)
      {
         return null;
      }

      // local part only?
      if (qNameAsString.charAt(0) != '{')
      {
          return null;
      }

      // Namespace URI improperly specified?
      if (qNameAsString.startsWith("{}"))
      {
         return null;
      }

      // Namespace URI and local part specified
      int endOfNamespaceURI = qNameAsString.indexOf('}');
      if (endOfNamespaceURI == -1)
      {
          throw new IllegalArgumentException(
              "Cannot parse QName from \"" + qNameAsString + "\", missing closing \"}\"");
      }
      return qNameAsString.substring(1, endOfNamespaceURI);
   }

   public static boolean isStructuredType(String dataTypeId)
   {
      return StructuredDataConstants.STRUCTURED_DATA.equals(dataTypeId);
   }
   
   public static boolean isDmsType(String dataTypeId)
   {
      return DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId) 
            || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId) 
            || DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId) 
            || DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId);
   }

   public static void resolveImports(XSDSchema schema)
   {
      List contents = schema.getContents();
      for (Object item : contents)
      {
         if (item instanceof XSDImportImpl)
         {
            // force schema resolving.
            // it's a noop if the schema is already resolved.
            XSDImportImpl importItem = (XSDImportImpl) item;
            if (importItem.getResolvedSchema() == null)
            {
               importItem.reset();
               importItem.importSchema();
               if (trace.isDebugEnabled())
               {
                  trace.debug("Resolved " + importItem.getNamespace() + " @ " + importItem.getSchemaLocation() + " : " + importItem.getResolvedSchema());
               }
            }
         }
      }
   } 
   
   public static ITypeDeclaration getTypeDeclaration(AccessPoint data, IModel model)
   {
      ITypeDeclaration decl = null;

      if (data instanceof IData)
      {
         IReference ref = ((IData) data).getExternalReference();
         if (ref != null)
         {
            IExternalPackage pkg = ref.getExternalPackage();

            if (pkg != null)
            {
               // handle UnresolvedExternalReference               
               IModel otherModel = pkg.getReferencedModel(); 
               decl = otherModel.findTypeDeclaration(ref.getId());
            }
         }         
         
      }

      if (decl == null)
      {         
         Object type = data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         if (type != null) {                    
            String typeString = type.toString();    
            if (data instanceof IData)
            {
               IModel refModel = (IModel) ((IData) data).getModel();
               decl = refModel.findTypeDeclaration(typeString);
            }
            else 
            {
               decl = model.findTypeDeclaration(typeString);
            }
         }         
      }
      
      return decl;
   }
   

   private StructuredTypeRtUtils()
   {
      // utility class
   }
}
