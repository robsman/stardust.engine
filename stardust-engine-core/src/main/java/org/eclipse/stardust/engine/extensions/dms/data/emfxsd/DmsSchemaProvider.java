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
package org.eclipse.stardust.engine.extensions.dms.data.emfxsd;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ISchemaType;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.emfxsd.ClasspathUriConverter;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.spi.ISchemaTypeProvider;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentAccessPoint;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentListAccessPoint;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderAccessPoint;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderListAccessPoint;
import org.eclipse.stardust.engine.extensions.dms.data.DmsVersioningAccessPoint;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;



/**
 * @author rsauer
 * @version $Revision$
 */
public class DmsSchemaProvider implements ISchemaTypeProvider
{

   public static final String PROPERTIES_ELEMENT_NAME = "properties";

   public static final String FOLDER_INFO_COMPLEX_TYPE_NAME = "FolderInfo";
   public static final String RESOURCE_PROPERTY_COMPLEX_TYPE_NAME = "ResourceProperty";
   public static final String RESOURCE_PROPERTIES_COMPLEX_TYPE_NAME = "ResourceProperties";
   public static final String FOLDER_COMPLEX_TYPE_NAME = "Folder";
   public static final String FOLDER_LIST_COMPLEX_TYPE_NAME = "FolderList";
   public static final String DOCUMENT_COMPLEX_TYPE_NAME = "Document";
   public static final String DOCUMENT_LIST_COMPLEX_TYPE_NAME = "DocumentList";
   public static final String VERSIONING_COMPLEX_TYPE_NAME = "Versioning";

   public static final String PARAMETER_METADATA_TYPE = "metadataType";
   
   public Set /*<TypedXPath>*/ getSchemaType(String dataTypeId, Map parameters)
   {
      XSDNamedComponent metadataXsdComponent = (XSDNamedComponent) parameters.get(PARAMETER_METADATA_TYPE);
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
      {
         return declareDocumentSchema(metadataXsdComponent); 
      }
      else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
      {
         return declareDocumentListSchema(metadataXsdComponent);
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId))
      {
         return declareFolderSchema(metadataXsdComponent);
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId))
      {
         return declareFolderListSchema(metadataXsdComponent);
      }
      else
      {
         throw new InternalException("Unsupported data type id: '"+dataTypeId+"'");
      }
   }
   
   public Set /*<TypedXPath>*/ getSchemaType(AccessPoint accessPoint)
   {
      if (accessPoint instanceof IData)
      {
         IData data = (IData)accessPoint;
         String metadataComplexTypeName = (String)data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         ITypeDeclaration metadataTypeDeclaration = ((IModel)data.getModel()).findTypeDeclaration(metadataComplexTypeName);
         
         XSDNamedComponent metadataXsdComponent = null;
         if (metadataTypeDeclaration != null)
         {
            XSDSchema metadataSchema = StructuredTypeRtUtils.getXSDSchema((IModel)data.getModel(), metadataTypeDeclaration);
            metadataXsdComponent = StructuredTypeRtUtils.findElementOrTypeDeclaration(metadataSchema, metadataTypeDeclaration.getId(), true);
         }
         Map parameters = CollectionUtils.newMap();
         parameters.put(PARAMETER_METADATA_TYPE, metadataXsdComponent);
         return getSchemaType(data.getType().getId(), parameters);
      }
      else if (accessPoint instanceof DmsDocumentAccessPoint)
      {
         return declareDocumentSchema(null);
      }
      else if (accessPoint instanceof DmsDocumentListAccessPoint)
      {
         return declareDocumentListSchema(null);
      }
      else if (accessPoint instanceof DmsFolderAccessPoint)
      {
         return declareFolderSchema(null);
      }
      else if (accessPoint instanceof DmsFolderListAccessPoint)
      {
         return declareFolderListSchema(null);
      }
      else if (accessPoint instanceof DmsVersioningAccessPoint)
      {
         return declareVersioningSchema();
      }
      else
      {
         throw new InternalException("Unsupported accessPoint: '"+((accessPoint==null)?"null":accessPoint.getClass().getName())+"'");
      }
   }
   
   public static class Factory implements ISchemaTypeProvider.Factory
   {
      private static final DmsSchemaProvider INSTANCE = new DmsSchemaProvider();

      public ISchemaTypeProvider getSchemaTypeProvider(String dataTypeId)
      {
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
         {
            return INSTANCE;
         }
         else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId))
         {
            return INSTANCE;
         }
         else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
         {
            return INSTANCE;
         }
         else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId))
         {
            return INSTANCE;
         }
         else 
         {
            return null;
         }
      }
      
      public ISchemaTypeProvider getSchemaTypeProvider(AccessPoint accessPoint)
      {
         if (accessPoint instanceof DmsFolderAccessPoint ||
               accessPoint instanceof DmsDocumentAccessPoint || 
               accessPoint instanceof DmsFolderListAccessPoint ||
               accessPoint instanceof DmsDocumentListAccessPoint || 
               accessPoint instanceof DmsVersioningAccessPoint)
         {
            return INSTANCE;
         }
         
         return null;
      }
      
   }

   private Set /*<TypedXPath>*/ declareVersioningSchema()
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      return XPathFinder.findAllXPaths(xsdSchema, VERSIONING_COMPLEX_TYPE_NAME, false);
   }
      
   private static XSDParticle findParticle(String elementName, XSDComplexTypeDefinition xsdComplexTypeDefinition)
   {
      EList xsdParticles = ((XSDModelGroup)((XSDParticle)xsdComplexTypeDefinition.getContent()).getTerm()).getContents();
      
      for (int i = 0; i < xsdParticles.size(); i++)
      {
         XSDParticle xsdParticle = (XSDParticle) xsdParticles.get(i);
         if (xsdParticle.getTerm() instanceof XSDElementDeclaration)
         {
            XSDElementDeclaration xsdElementDeclaration = (XSDElementDeclaration)xsdParticle.getTerm();
            if (elementName.equals(xsdElementDeclaration.getName()))
            {
               return xsdParticle;
            }
         }
      }
      throw new PublicException("Element '"+elementName+"' is not found.");
   }
   
   public static Set /*<TypedXPath>*/ declareDocumentSchema(XSDNamedComponent metadataXsdComponent)
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      
      if (metadataXsdComponent != null) 
      {
         // set type of the properties element to the custom type declared in metadataTypeDeclaration
         injectProperties(metadataXsdComponent, (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(DOCUMENT_COMPLEX_TYPE_NAME));
      }
      
      return XPathFinder.findAllXPaths(xsdSchema, DOCUMENT_COMPLEX_TYPE_NAME, false);
   }

   public static Set /*<TypedXPath>*/ declareFolderSchema(XSDNamedComponent metadataXsdComponent)
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      
      if (metadataXsdComponent != null) 
      {
         // set type of the properties element to the custom type declared in metadataTypeDeclaration
         injectProperties(metadataXsdComponent, (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(FOLDER_COMPLEX_TYPE_NAME));
      }

      return XPathFinder.findAllXPaths(xsdSchema, FOLDER_COMPLEX_TYPE_NAME, false);
   }

   public static Set /*<TypedXPath>*/ declareFolderInfoSchema(XSDNamedComponent metadataXsdComponent)
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      
      if (metadataXsdComponent != null) 
      {
         // set type of the properties element to the custom type declared in metadataTypeDeclaration
         injectProperties(metadataXsdComponent, (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(FOLDER_INFO_COMPLEX_TYPE_NAME));
      }

      return XPathFinder.findAllXPaths(xsdSchema, FOLDER_INFO_COMPLEX_TYPE_NAME, false);
   }

   public static Set /*<TypedXPath>*/ declareDocumentListSchema(XSDNamedComponent metadataXsdComponent)
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      
      if (metadataXsdComponent != null) 
      {
         // set type of the properties element to the custom type declared in metadataTypeDeclaration
         injectProperties(metadataXsdComponent, (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(DOCUMENT_COMPLEX_TYPE_NAME));
      }
      
      return XPathFinder.findAllXPaths(xsdSchema, DOCUMENT_LIST_COMPLEX_TYPE_NAME, false);
   }

   private static void injectProperties(XSDNamedComponent metadataXsdComponent, XSDComplexTypeDefinition targetType)
   {
      XSDParticle propertiesParticle = findParticle(PROPERTIES_ELEMENT_NAME, targetType);
      propertiesParticle.setMinOccurs(1);
      propertiesParticle.setMaxOccurs(1);
      if (metadataXsdComponent instanceof XSDTypeDefinition)
      {
         ((XSDElementDeclaration)propertiesParticle.getTerm()).setTypeDefinition((XSDTypeDefinition) metadataXsdComponent);
      }
      else if (metadataXsdComponent instanceof XSDElementDeclaration)
      {
         XSDTypeDefinition xsdTypeDefinition = ((XSDElementDeclaration) metadataXsdComponent).getTypeDefinition();
         ((XSDElementDeclaration)propertiesParticle.getTerm()).setTypeDefinition(xsdTypeDefinition);
      }
   }

   public static Set /*<TypedXPath>*/ declareFolderListSchema(XSDNamedComponent metadataType)
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      
      if (metadataType != null) 
      {
         // set type of the properties element to the custom type declared in metadataTypeDeclaration
         injectProperties(metadataType, (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(FOLDER_COMPLEX_TYPE_NAME));
      }

      return XPathFinder.findAllXPaths(xsdSchema, FOLDER_LIST_COMPLEX_TYPE_NAME, false);
   }

   public static XSDComplexTypeDefinition getResourcePropertyType()
   {
      XSDSchema xsdSchema = loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);
      return (XSDComplexTypeDefinition)xsdSchema.resolveTypeDefinition(RESOURCE_PROPERTY_COMPLEX_TYPE_NAME);
   }
   
   public static XSDSchema getXSDSchema(ITypeDeclaration typeDeclaration)
   {
      IXpdlType xpdlType = typeDeclaration.getXpdlType();

      if (xpdlType instanceof IExternalReference)
      {
         // ExternalReference
         IExternalReference externalReference = (IExternalReference)xpdlType;
         
         return loadExternalSchema(externalReference.getLocation());
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
   
   public static XSDSchema loadExternalSchema(String schemaLocation)
   {
      if ( !Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("xsd"))
      {
         // this is needed when XSD ECORE is used standalone
         Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xsd", new XSDResourceFactoryImpl());
      }
      
      ResourceSet resourceSet = new ResourceSetImpl();
      
      HashMap options = new HashMap();
      options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
       
      resourceSet.setURIConverter(new ClasspathUriConverter());
      if(schemaLocation.startsWith("/"))
      {
         schemaLocation = schemaLocation.substring(1);
      }
      Resource resource = resourceSet.createResource(URI.createURI(ClasspathUriConverter.CLASSPATH_SCHEME+":/"+schemaLocation));
      try
      {
         resource.load(options);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      
      List l = resource.getContents();
      return(XSDSchema) l.get(0);
   }
}
