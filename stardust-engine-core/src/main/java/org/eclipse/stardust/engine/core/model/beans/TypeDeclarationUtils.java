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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IExternalReference;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.model.IXpdlType;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDParticleContent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.impl.XSDImportImpl;

public class TypeDeclarationUtils
{
   public static final int XPDL_TYPE = 0;

   public static final int SIMPLE_TYPE = 1;

   public static final int COMPLEX_TYPE = 2;

   public static ThreadLocal<URIConverter> defaultURIConverter = new ThreadLocal<URIConverter>();



   public static XSDSimpleTypeDefinition getSimpleType(ITypeDeclaration declaration)
   {
      XSDNamedComponent component = findElementOrTypeDeclaration(declaration);
      if (component instanceof XSDElementDeclaration)
      {
         component = ((XSDElementDeclaration) component).getTypeDefinition();
      }
      return component instanceof XSDSimpleTypeDefinition ? (XSDSimpleTypeDefinition) component : null;
   }

   public static XSDComplexTypeDefinition getComplexType(ITypeDeclaration declaration)
   {
      XSDNamedComponent component = findElementOrTypeDeclaration(declaration);
      if (component instanceof XSDElementDeclaration)
      {
         component = ((XSDElementDeclaration) component).getTypeDefinition();
      }
      return component instanceof XSDComplexTypeDefinition ? (XSDComplexTypeDefinition) component : null;
   }

   public static XSDNamedComponent findElementOrTypeDeclaration(ITypeDeclaration declaration)
   {
      return findElementOrTypeDeclaration(declaration, declaration.getId());
   }

   public static XSDNamedComponent findElementOrTypeDeclaration(ITypeDeclaration declaration, String id)
   {
      IXpdlType type = declaration.getXpdlType();
      if (type instanceof SchemaTypeBean)
      {
         SchemaTypeBean schemaType = (SchemaTypeBean) type;
         XSDSchema xsdSchema = schemaType.getSchema();
         if (xsdSchema != null)
         {
            if (type instanceof SchemaTypeBean)
            {
               return findElementOrTypeDeclaration(xsdSchema, id, schemaType.getSchema().getTargetNamespace(), true);
            }
            if (type instanceof IExternalReference)
            {
               IExternalReference reference = (IExternalReference) type;
               return findElementOrTypeDeclaration(xsdSchema, QNameUtil.parseLocalName(reference.getXref()),
                     QNameUtil.parseNamespaceURI(reference.getXref())/* reference.getNamespace() */, false);
            }
         }
      }

      return null;
   }

   public static XSDNamedComponent findElementOrTypeDeclaration(XSDSchema schema, String localName, String namespace,
         boolean returnFirstIfNoMatch)
   {
      if (schema == null)
      {
         return null;
      }
      XSDNamedComponent decl = null;
      List<XSDElementDeclaration> elements = schema.getElementDeclarations();
      List<XSDTypeDefinition> types = schema.getTypeDefinitions();
      if (localName != null)
      {
         // scan all elements to find the one with the name matching the id.
         for (XSDElementDeclaration element : elements)
         {
            if (localName.equals(element.getName()) && CompareHelper.areEqual(namespace, element.getTargetNamespace()))
            {
               decl = element;
               break;
            }
         }
         if (decl == null)
         {
            // scan all types now
            for (XSDTypeDefinition type : types)
            {
               if (localName.equals(type.getName()) && CompareHelper.areEqual(namespace, type.getTargetNamespace()))
               {
                  decl = type;
                  break;
               }
            }
         }
      }
      if (decl == null && returnFirstIfNoMatch)
      {
         if (elements.size() == 1)
         {
            decl = elements.get(0);
         }
         else if (elements.isEmpty() && types.size() == 1)
         {
            decl = types.get(0);
         }
      }
      return decl;
   }

   // we can have more than one XSDImport
   public static List<XSDImport> getImports(XSDSchema schema)
   {
      List<XSDImport> xsdImports = new ArrayList<XSDImport>();
      List<XSDSchemaContent> contents = schema.getContents();
      Iterator<XSDSchemaContent> it = contents.iterator();
      while (it.hasNext())
      {
         XSDSchemaContent content = (XSDSchemaContent) it.next();
         if (content instanceof XSDImport)
         {
            xsdImports.add((XSDImport) content);
         }
      }
      if (!xsdImports.isEmpty())
      {
         return xsdImports;
      }
      return null;
   }

   public static XSDImport getImportByNamespace(XSDSchema schema, String nameSpace)
   {
      List<XSDImport> xsdImports = getImports(schema);
      if (xsdImports != null)
      {
         for (Iterator<XSDImport> i = xsdImports.iterator(); i.hasNext();)
         {
            XSDImport xsdImport = i.next();
            String importNameSpace = xsdImport.getNamespace();
            if (CompareHelper.areEqual(nameSpace, importNameSpace))
            {
               return xsdImport;
            }
         }
      }
      return null;
   }

   public static boolean hasImport(XSDSchema schema, ITypeDeclaration type)
   {
      for (XSDSchemaContent content : schema.getContents())
      {
         if (content instanceof XSDImport)
         {
            String location = ((XSDImport) content).getSchemaLocation();
            if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
            {
               String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
               if (typeId.equals(type.getId()))
               {
                  return true;
               }
            }
         }
      }
      return false;
   }

   public static void findElementsForType(ITypeDeclaration declaration, Set<XSDElementDeclaration> elements,
         String elementName)
   {
      XSDComplexTypeDefinition complexType = getComplexType(declaration);
      if (complexType != null)
      {
         visit(complexType, elements, elementName);
      }
   }

   public static void visit(XSDComplexTypeDefinition complexType, Set<XSDElementDeclaration> elements,
         String elementName)
   {
      XSDComplexTypeContent content = complexType.getContent();
      if (content instanceof XSDParticle)
      {
         visit((XSDParticle) content, elements, elementName);
      }
   }

   public static void visit(XSDParticle particle, Set<XSDElementDeclaration> elements, String elementName)
   {
      XSDParticleContent particleContent = particle.getContent();
      if (particleContent instanceof XSDModelGroupDefinition)
      {
         //
      }
      else if (particleContent instanceof XSDTerm)
      {
         visit((XSDTerm) particleContent, elements, elementName);
      }
   }

   public static void visit(XSDTerm term, Set<XSDElementDeclaration> elements, String elementName)
   {
      if (term instanceof XSDElementDeclaration)
      {
         visit((XSDElementDeclaration) term, elements, elementName);
      }
      else if (term instanceof XSDModelGroup)
      {
         visit((XSDModelGroup) term, elements, elementName);
      }
   }

   public static void visit(XSDModelGroup group, Set<XSDElementDeclaration> elements, String elementName)
   {
      for (XSDParticle xsdParticle : group.getContents())
      {
         visit(xsdParticle, elements, elementName);
      }
   }

   public static void visit(XSDElementDeclaration element, Set<XSDElementDeclaration> elements, String elementName)
   {
      XSDTypeDefinition type = element.getAnonymousTypeDefinition();
      if (type instanceof XSDComplexTypeDefinition)
      {
         visit((XSDComplexTypeDefinition) type, elements, elementName);
      }
      else if (type == null)
      {
         type = element.getType();
         if (type != null)
         {
            String qName = type.getQName();
            if (elementName.equals(qName))
            {
               elements.add(element);
            }
         }
      }
   }

   public static void resolveImports(XSDSchema schema)
   {
      List<XSDSchemaContent> contents = schema.getContents();
      for (XSDSchemaContent item : contents)
      {
         if (item instanceof XSDImportImpl)
         {
            // force schema resolving.
            // it's a noop if the schema is already resolved.
            ((XSDImportImpl) item).importSchema();
         }
      }
   }


   public static String getNamespacePrefix(XSDSchema schema, String targetNameSpace)
   {
      String prefix = null;
      for (Map.Entry<String, String> entry : schema.getQNamePrefixToNamespaceMap().entrySet())
      {
         if (entry.getValue().equals(targetNameSpace))
         {
            prefix = entry.getKey();
            break;
         }
      }
      return prefix;
   }

   public static void collectAllNamespaces(XSDSchema schema, Map<String, String> qNamePrefixToNamespaceMap)
   {
      List<XSDImport> imports = TypeDeclarationUtils.getImports(schema);
      if(imports != null)
      {
         for(XSDImport xsdImport : imports)
         {
            if (xsdImport.getSchemaLocation().startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
            {
               XSDSchema importetSchema = xsdImport.getResolvedSchema();
               if(importetSchema != null)
               {
                  String targetNamespace = importetSchema.getTargetNamespace();
                  String namespacePrefix = TypeDeclarationUtils.getNamespacePrefix(importetSchema, targetNamespace);

                  if(!qNamePrefixToNamespaceMap.containsValue(targetNamespace))
                  {
                     qNamePrefixToNamespaceMap.put(namespacePrefix, targetNamespace);
                     collectAllNamespaces(importetSchema, qNamePrefixToNamespaceMap);
                  }
               }
            }
         }
      }
   }
}