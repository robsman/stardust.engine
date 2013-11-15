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
package org.eclipse.stardust.engine.core.struct.emfxsd;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeGroupContent;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.XSDWildcard;
import org.eclipse.xsd.util.XSDConstants;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XPathFinder
{
   private static final Logger trace = LogManager.getLogger(XPathFinder.class);
   
   public static Set<TypedXPath> findAllXPaths(XSDSchema xsdSchema, String rootNamedComponent, boolean fallbackToFirstNamedComponentIfNoMatch) 
   {
      return findAllXPaths(xsdSchema, rootNamedComponent, fallbackToFirstNamedComponentIfNoMatch, null);
   }

   public static Set<TypedXPath> findAllXPaths(XSDSchema xsdSchema, String rootNamedComponent, boolean fallbackToFirstNamedComponentIfNoMatch, Set<XSDTypeDefinition> allVisitedTypes)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Looking up: " + rootNamedComponent + " in schema: " + xsdSchema.getSchemaLocation() + "/" + xsdSchema.getTargetNamespace());
      }
      XSDNamedComponent root = findElement(xsdSchema, rootNamedComponent);
      if (root == null)
      {
         root = findTypeDefinition(xsdSchema, rootNamedComponent);
      }

      if (root == null && fallbackToFirstNamedComponentIfNoMatch)
      {
         List elements = xsdSchema.getElementDeclarations();
         List types = xsdSchema.getTypeDefinitions();
         if (elements.size() == 1)
         {
            root = (XSDElementDeclaration) elements.get(0);
         }
         else if (elements.isEmpty() && types.size() == 1)
         {
            root = (XSDTypeDefinition) types.get(0);
         }
      }

      if (root == null)
      {
         if (fallbackToFirstNamedComponentIfNoMatch)
         {
            throw new RuntimeException("Neither type definition or top-level element '"
                  + rootNamedComponent
                  + "' nor any type definition or top-level element found in the schema");
         }
         else
         {
            throw new RuntimeException("Type definition or top-level element '" + rootNamedComponent + "' not found");
         }
      }
      
      return findAllXPaths(xsdSchema, root, allVisitedTypes);
   }
   
   public static Set<TypedXPath> findAllXPaths(XSDSchema xsdSchema, XSDNamedComponent root)
   {
      return findAllXPaths(xsdSchema, root, null);
   }

   public static Set<TypedXPath> findAllXPaths(XSDSchema xsdSchema, XSDNamedComponent root, Set<XSDTypeDefinition> allVisitedTypes)
   {
      String elementName = null;
      String elementNs = null;
      
      XSDTypeDefinition xsdTypeDefinition = null;
      if (root instanceof XSDTypeDefinition)
      {
         xsdTypeDefinition = (XSDTypeDefinition) root;
         elementName = xsdTypeDefinition.getName();
         elementNs = xsdTypeDefinition.getTargetNamespace();
      }
      else if (root instanceof XSDElementDeclaration)
      {
         XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) root;
         elementName = elementDeclaration.getName();
         elementNs = elementDeclaration.getTargetNamespace();
         xsdTypeDefinition = elementDeclaration.getType();
      }
      else
      {
         throw new RuntimeException("Root component must be either a type definition or an element declaration");
      }

      Stack visitedTypes = new Stack();
      String xpath = "";

      Set<TypedXPath> allXPaths = CollectionUtils.newSet();
      XPathAnnotations xsdAnnotations = findAnnotations(xsdTypeDefinition.getAnnotation());
      if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition)
      {
         // the one and only XPath if enumeration is top-level element
         allXPaths.add(new TypedXPath(null, allXPaths.size(), xpath, false, elementName,
               elementNs, xsdTypeDefinition.getName(), xsdTypeDefinition.getTargetNamespace(),
               getBigDataType(xsdTypeDefinition), false, xsdAnnotations,
               getEnumerationValues(xsdTypeDefinition)));
      }
      else if (xsdTypeDefinition instanceof XSDComplexTypeDefinition)
      {
         XSDComplexTypeDefinition xsdComplexTypeDefinition = (XSDComplexTypeDefinition) xsdTypeDefinition;
         findComplexTypeXpaths(null, xpath, elementName, elementNs, xsdComplexTypeDefinition, xsdAnnotations, allXPaths,
               visitedTypes, allVisitedTypes, false);
      }
      else
      {
         throw new RuntimeException("Unsupported XSD: " + xsdTypeDefinition);
      }
      
      return allXPaths;
   }

   private static String getTargetNamespace(XSDTypeDefinition typeDefinition)
   {
      String targetNamespace = typeDefinition.getTargetNamespace();
      if (targetNamespace == null)
      {
         XSDTypeDefinition base = typeDefinition.getBaseType();
         if (base != null)
         {
            return getTargetNamespace(base);
         }
      }
      return targetNamespace;
   }

   private static String getName(XSDTypeDefinition typeDefinition)
   {
      String name = typeDefinition.getName();
      if (name == null)
      {
         XSDTypeDefinition base = typeDefinition.getBaseType();
         if (base != null)
         {
            return getName(base);
         }
      }
      return name;
   }

   public static XSDElementDeclaration findElement(XSDSchema xsdSchema,
         String elementName)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Scanning elements for: " + elementName + " ...");
      }
      String namespaceURI = StructuredTypeRtUtils.parseNamespaceURI(elementName);
      String localName = StructuredTypeRtUtils.parseLocalName(elementName);
      for (XSDElementDeclaration xsdElementDeclaration : xsdSchema.getElementDeclarations())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("  Element found: {" + xsdElementDeclaration.getTargetNamespace() + "}" + xsdElementDeclaration.getName());
         }
         if (localName.equals(xsdElementDeclaration.getName())
               && (namespaceURI == null || CompareHelper.areEqual(namespaceURI, xsdElementDeclaration.getTargetNamespace())))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug(" Success: " + xsdElementDeclaration);
            }
            return xsdElementDeclaration;
         }
      }
      return null;
   }

   public static XSDTypeDefinition findTypeDefinition(XSDSchema xsdSchema,
         String typeName)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Scanning types for: " + typeName + " ...");
      }
      String namespaceURI = StructuredTypeRtUtils.parseNamespaceURI(typeName);
      String localName = StructuredTypeRtUtils.parseLocalName(typeName);
      for (XSDTypeDefinition xsdTypeDefinition : xsdSchema.getTypeDefinitions())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("  Type found: {" + xsdTypeDefinition.getTargetNamespace() + "}" + xsdTypeDefinition.getName());
         }
         if (localName.equals(xsdTypeDefinition.getName())
               && (namespaceURI == null || CompareHelper.areEqual(namespaceURI, xsdTypeDefinition.getTargetNamespace())))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug(" Success: " + xsdTypeDefinition);
            }
            return xsdTypeDefinition;
         }
      }
      return null;
   }

   private static void findAllXPaths(TypedXPath parentXPath,
         XSDComplexTypeDefinition xsdComplexTypeDefinition, Set allXPaths,
         Stack visitedTypes, Set<XSDTypeDefinition> allVisitedTypes)
   {
      // handle extensions
      XSDTypeDefinition baseTypeDefinition = xsdComplexTypeDefinition.getBaseTypeDefinition();
      if (baseTypeDefinition != null && !XSDConstants.isAnyType(baseTypeDefinition))
      {
         if (baseTypeDefinition instanceof XSDComplexTypeDefinition)
         {
            XSDComplexTypeDefinition xsdBaseComplexTypeDefinition = (XSDComplexTypeDefinition)baseTypeDefinition;
            findAttributes(parentXPath, xsdBaseComplexTypeDefinition.getAttributeContents(), allXPaths);
            findAllXPaths(parentXPath, xsdBaseComplexTypeDefinition, allXPaths, visitedTypes, allVisitedTypes);
         } 
         else if (baseTypeDefinition instanceof XSDSimpleTypeDefinition) 
         {
            // ignore, since this was already explored by recursion in getBigDataType()
         }
         else
         {
            throw new RuntimeException("Unsupported XSD: "+baseTypeDefinition);
         }
      }
      
      XSDComplexTypeContent xsdComplexTypeContent = xsdComplexTypeDefinition.getContent();
      
      if (xsdComplexTypeContent == null)
      {
         // <complexType/>
         return;
      }
      
      if (xsdComplexTypeContent instanceof XSDParticle)
      {
         XSDParticle xsdParticle = (XSDParticle)xsdComplexTypeContent;

         findAllXPaths(parentXPath, xsdParticle, allXPaths, visitedTypes, allVisitedTypes);
      } 
      else if (xsdComplexTypeContent instanceof XSDSimpleTypeDefinition)
      {
         // ignore, since this was already explored by recursion in getBigDataType()
      }
      else
      {
         throw new RuntimeException("Unsupported XSD: "+xsdComplexTypeContent);
      }
   }

   private static int getBigDataType(XSDTypeDefinition xsdTypeDefinition)
   {
      String typeName = xsdTypeDefinition.getName();
      if ("string".equals(typeName))
      {
         return BigData.STRING;
      }
      else if ("boolean".equals(typeName))
      {
         return BigData.BOOLEAN;
      }
      else if ("int".equals(typeName) || "integer".equals(typeName))
      {
         return BigData.INTEGER;
      }
      else if ("long".equals(typeName))
      {
         return BigData.LONG;
      }
      else if ("decimal".equals(typeName))
      {
         return BigData.STRING;
      }
      else if ("short".equals(typeName))
      {
         return BigData.SHORT;
      }
      else if ("byte".equals(typeName))
      {
         return BigData.BYTE;
      }
      else if ("double".equals(typeName))
      {
         return BigData.DOUBLE;
      }
      else if ("float".equals(typeName))
      {
         return BigData.FLOAT;
      }
      else if ("dateTime".equals(typeName))
      {
         return BigData.DATE;
      }
      else if ("date".equals(typeName))
      {
         return BigData.DATE;
      }
      else if ("gYearMonth".equals(typeName) || "gYear".equals(typeName)
            || "gDay".equals(typeName) || "gMonthDay".equals(typeName)
            || "gMonth".equals(typeName))
      {
         return BigData.STRING;
      }
      else if ("time".equals(typeName))
      {
         return BigData.DATE;
      }
      else if ("duration".equals(typeName))
      {
         return BigData.PERIOD;
      }
      else
      {
         if (XSDConstants.isAnyType(xsdTypeDefinition))
         {
            // any types are treated as raw string 
            return BigData.STRING;
         }
         // look up the base type definition if Simple Type
         if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition || xsdTypeDefinition.getBaseType() instanceof XSDSimpleTypeDefinition)
         {
            if (xsdTypeDefinition.getBaseType() == null)
            {
               // if null, top of the type hierarchy is reached (anySimpleType) and no known type could be found
               // fall back to string 
               return BigData.STRING;
            }
            else
            {
               // go up the hierarchy
               return getBigDataType(xsdTypeDefinition.getBaseType());
            }
         }
         return BigData.NULL;
      }
   }

   private static void findAllXPaths(TypedXPath parentXPath, final XSDParticle xsdParticle,
         Set allXPaths, Stack visitedTypes, Set<XSDTypeDefinition> allVisitedTypes)
   {
      XSDTerm xsdTerm = xsdParticle.getTerm();
      
      if (xsdTerm instanceof XSDModelGroup)
      {
         XSDModelGroup xsdModelGroup = (XSDModelGroup)xsdTerm;
         // all, choice, sequence
         for (Iterator elements = xsdModelGroup.getContents().iterator(); elements.hasNext(); )
         {
            XSDParticle childParticle = (XSDParticle) elements.next();
            findAllXPaths(parentXPath, childParticle, allXPaths, visitedTypes, allVisitedTypes);
         }
      }
      else if (xsdTerm instanceof XSDElementDeclaration)
      {
         XSDElementDeclaration xsdElementDeclaration = (XSDElementDeclaration)xsdTerm;
         findAllXPaths(parentXPath, xsdElementDeclaration, xsdParticle, allXPaths, visitedTypes, allVisitedTypes);
      }
      else if (xsdTerm instanceof XSDWildcard)
      {
         // ignore wildcards, no specific type information can be retrieved
         parentXPath.enableWildcards();
      }
      else 
      {
         throw new RuntimeException("Unsupported XSD: "+xsdTerm);
      }
   }
   
   private static void findAllXPaths(TypedXPath parentXPath,
         final XSDElementDeclaration xsdElementDeclaration, XSDParticle xsdParticle, Set allXPaths,
         Stack<XSDTypeDefinition> visitedTypes, Set<XSDTypeDefinition> allVisitedTypes)
   {
      XSDTypeDefinition xsdTypeDefinition = xsdElementDeclaration.getTypeDefinition();
      
      if (xsdTypeDefinition == null)
      {
         trace.warn("No type definition can be found for element '"
               + xsdElementDeclaration
               + "', will ignore it. Check the schema for inconsistencies.");
         return;
      }
      
      String elementName = xsdElementDeclaration.getName();
      
      if (visitedTypes.contains(xsdTypeDefinition))
      {
         // The type is already in the stack, therefore if we were to continue we would
         // infinitely recurse.
      }
      else
      {
         if (allVisitedTypes != null)
         {
            allVisitedTypes.add(xsdTypeDefinition);
         }
         visitedTypes.push(xsdTypeDefinition);
         try
         {
            String childXPathString;
            if (StringUtils.isEmpty(parentXPath.getXPath())) 
            {
               childXPathString = elementName;
            }
            else 
            {
               childXPathString = parentXPath.getXPath() + "/" + elementName;
            }
   
            trace.debug("xpath found: " + childXPathString);
            
            boolean isList = false;
            if (xsdParticle.getMaxOccurs() > 1 || xsdParticle.getMaxOccurs() == XSDParticle.UNBOUNDED)
            {
               isList = true;
            }
            String elementTargetNamespace = xsdElementDeclaration.getTargetNamespace();
            if (elementTargetNamespace == null)
            {
               elementTargetNamespace = "";
            }
            
            if (containsChildFromDifferentNamespace(parentXPath, elementName, elementTargetNamespace))
            {
               throw new RuntimeException("Equally named subelements (equal local names) are not supported: '"+elementName+"'");
            }
            
            if (xsdTypeDefinition instanceof XSDComplexTypeDefinition)
            {
               findComplexTypeXpaths(parentXPath, childXPathString, xsdElementDeclaration.getName(),
                     elementTargetNamespace, (XSDComplexTypeDefinition) xsdTypeDefinition,
                     findAnnotations(xsdElementDeclaration.getAnnotation()),
                     allXPaths, visitedTypes, allVisitedTypes, isList);
            }
            else
            {
               TypedXPath typedXPath = new TypedXPath(parentXPath, allXPaths.size(),
                     childXPathString, false,
                     xsdElementDeclaration.getName(), elementTargetNamespace,
                     xsdTypeDefinition.getName(),
                     xsdTypeDefinition.getTargetNamespace(), getBigDataType(xsdTypeDefinition),
                     isList, findAnnotations(xsdElementDeclaration.getAnnotation()),
                     getEnumerationValues(xsdTypeDefinition));
               allXPaths.add(typedXPath);
            }
         }
         finally
         {
            visitedTypes.pop();
         }
      }
   }

   private static void findComplexTypeXpaths(TypedXPath parent, String xpath, String elementName, String elementNs,
         XSDComplexTypeDefinition xsdComplexTypeDefinition, XPathAnnotations xsdAnnotations, Set<TypedXPath> allXPaths,
         Stack visitedTypes, Set<XSDTypeDefinition> allVisitedTypes, boolean isList)
   {
      List<XSDAttributeGroupContent> attributeContents = xsdComplexTypeDefinition.getAttributeContents();
      if (xsdComplexTypeDefinition.getContent() instanceof XSDSimpleTypeDefinition)
      {
         // the one and only XPath if enumeration is top-level element
         XSDSimpleTypeDefinition xsdSimpleTypeDefinition = (XSDSimpleTypeDefinition) xsdComplexTypeDefinition.getContent();
         if (attributeContents.isEmpty())
         {
            // keep compatibility with old style complex types with simple content if they do not have attributes
            TypedXPath typedXPath = new TypedXPath(parent, allXPaths.size(), xpath, false,
                  elementName, elementNs,
                  xsdComplexTypeDefinition.getName(), xsdComplexTypeDefinition.getTargetNamespace(),
                  getBigDataType(xsdSimpleTypeDefinition), isList, xsdAnnotations,
                  getEnumerationValues(xsdSimpleTypeDefinition));
            allXPaths.add(typedXPath);
         }
         else
         {
            // the root XPath
            TypedXPath rootXPath = new TypedXPath(parent, allXPaths.size(), xpath, false,
                  elementName, elementNs,
                  xsdComplexTypeDefinition.getName(), xsdComplexTypeDefinition.getTargetNamespace(),
                  BigData.NULL, isList, xsdAnnotations, Collections.EMPTY_LIST);
            allXPaths.add(rootXPath);
            TypedXPath typedXPath = new TypedXPath(rootXPath, allXPaths.size(), 
                  parent == null ? StructuredDataConverter.NODE_VALUE_KEY : xpath + "/" + StructuredDataConverter.NODE_VALUE_KEY, false,
                  StructuredDataConverter.NODE_VALUE_KEY, "",
                  getName(xsdSimpleTypeDefinition), getTargetNamespace(xsdSimpleTypeDefinition),
                  getBigDataType(xsdSimpleTypeDefinition), false, xsdAnnotations,
                  getEnumerationValues(xsdSimpleTypeDefinition));
            allXPaths.add(typedXPath);
            findAttributes(rootXPath, attributeContents, allXPaths);
         }
      }
      else
      {
         // the root XPath
         TypedXPath typedXPath = new TypedXPath(parent, allXPaths.size(), xpath, false,
               elementName, elementNs,
               xsdComplexTypeDefinition.getName(), xsdComplexTypeDefinition.getTargetNamespace(),
               BigData.NULL, isList, xsdAnnotations, Collections.EMPTY_LIST);
         allXPaths.add(typedXPath);
         findAttributes(typedXPath, attributeContents, allXPaths);
         findAllXPaths(typedXPath, xsdComplexTypeDefinition, allXPaths, visitedTypes, allVisitedTypes);
      }
   }

   private static void findAttributes(TypedXPath parentXPath,
         List<XSDAttributeGroupContent> attributeContents, Set allXPaths)
   {
      for (XSDAttributeGroupContent xsdAttributeGroupContent : attributeContents)
      {
         if (xsdAttributeGroupContent instanceof XSDAttributeUse)
         {
            addAttribute(parentXPath, (XSDAttributeUse) xsdAttributeGroupContent, allXPaths);
         }
         else if (xsdAttributeGroupContent instanceof XSDAttributeGroupDefinition)
         {
            XSDAttributeGroupDefinition attributeGroupDefinition = ((XSDAttributeGroupDefinition) xsdAttributeGroupContent)
                  .getResolvedAttributeGroupDefinition();
            List<XSDAttributeUse> xsdAttributeUses = attributeGroupDefinition.getAttributeUses();
            for (XSDAttributeUse use : xsdAttributeUses)
            {
               addAttribute(parentXPath, use, allXPaths);
            }
         }
         // (fh) TODO wildcard attributes
         else
         {
            throw new RuntimeException("Unsupported XSD: " + xsdAttributeGroupContent);
         }
      }
   }

   private static void addAttribute(TypedXPath parentXPath, XSDAttributeUse xsdAttributeUse, Set allXPaths)
   {
      XSDSimpleTypeDefinition xsdSimpleTypeDefinition = xsdAttributeUse.getAttributeDeclaration().getTypeDefinition();
      
      String attributeXPath;
      String attributeName = xsdAttributeUse.getAttributeDeclaration().getName();
      if (StringUtils.isEmpty(parentXPath.getXPath())) 
      {
         attributeXPath = "@" + attributeName;
      }
      else 
      {
         attributeXPath = parentXPath.getXPath() + "/@" + attributeName;
      }
      
      trace.debug("xpath found: " + attributeXPath);
      
      if (containsChildFromDifferentNamespace(parentXPath, "@"+attributeName, xsdAttributeUse.getAttributeDeclaration().getTargetNamespace()))
      {
         throw new RuntimeException("Equally named attributes (equal local names) are not supported: '"+attributeName+"'");
      }

      allXPaths.add(new TypedXPath(parentXPath, allXPaths.size(), attributeXPath,
            true, attributeName,
            xsdAttributeUse.getAttributeDeclaration().getTargetNamespace(),
            getName(xsdSimpleTypeDefinition),
            xsdSimpleTypeDefinition.getTargetNamespace(),
            getBigDataType(xsdSimpleTypeDefinition), false,
            findAnnotations(xsdAttributeUse.getContent().getAnnotation()),
            getEnumerationValues(xsdSimpleTypeDefinition)));
   }

   private static boolean containsChildFromDifferentNamespace(TypedXPath parentXPath, String name, String namespace)
   {
      TypedXPath anotherChild = parentXPath.getChildXPath(name);
      if (anotherChild == null)
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
   private static List getEnumerationValues(XSDTypeDefinition xsdTypeDefinition)
   {
      // find out enumeration values (if they are fixed)
      // if no fixed list of values can be found, this is not an "enumeration"
      // in our understanding
      
      if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition)
      {
         XSDSimpleTypeDefinition xsdSimpleTypeDefinition = (XSDSimpleTypeDefinition)xsdTypeDefinition;
         XSDEnumerationFacet xsdEnumerationFacet = xsdSimpleTypeDefinition.getEffectiveEnumerationFacet();
         if (xsdEnumerationFacet != null)
         {
            EList xsdEnumerationValues = xsdEnumerationFacet.getValue();
            List enumerationValues = CollectionUtils.newList();
            for (int i = 0; i < xsdEnumerationValues.size(); i++)
            {
               enumerationValues.add(xsdEnumerationValues.get(i).toString());
            }
            return enumerationValues;
         }
      }
      
      return Collections.EMPTY_LIST;
   }

   private static XPathAnnotations findAnnotations(XSDAnnotation xsdAnnotation)
   {
      // scopeMap is only to support old scope-style annotations
      Map /* <String,Map> */ scopeMap = CollectionUtils.newHashMap();
      List /* <Element> */ appInfos = CollectionUtils.newList() /* <Element> */;
      if (xsdAnnotation != null)
      {
         List /*<Element>*/ appInfoElements = xsdAnnotation.getApplicationInformation(); 
         for (int i=0; i<appInfoElements.size(); i++)
         {
            Element appInfoElement = (Element)appInfoElements.get(i);
            appInfos.add(appInfoElement);
            NodeList scopes = appInfoElement.getElementsByTagNameNS("http://www.carnot.ag/workflowmodel/3.1/struct", "scope");
            for (int j=0; j<scopes.getLength(); j++)
            {
               Element scope = (Element)scopes.item(j);
               String scopeName = scope.getAttribute("name");
               Map /* <String,String> */ annotationsForScope = CollectionUtils.newHashMap();
               scopeMap.put(scopeName, annotationsForScope);
               NodeList scopeContents = scope.getElementsByTagName("entry");
               for (int k=0; k<scopeContents.getLength(); k++)
               {
                  Element entry = (Element)scopeContents.item(k);
                  annotationsForScope.put(entry.getAttribute("name"), entry.getAttribute("value"));
               }
            }
         }
      }
      return new XPathAnnotations(appInfos, scopeMap);
   }

}
