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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.sxml.Attribute;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.NamedNode;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.Text;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathException;


/**
 * Searches for XPaths in an XSD
 */
public class StructuredDataXPathUtils
{

   private static final Logger trace = LogManager.getLogger(StructuredDataXPathUtils.class);

   /**
    * Computes "canonical" XPath for given node
    * @param node node to compute the XPath for
    * @param rootNode root to use for computing
    * @return
    */
   public static String getNodeXPath(org.eclipse.stardust.engine.core.struct.sxml.Node node, org.eclipse.stardust.engine.core.struct.sxml.Node rootNode)
   {
      if (rootNode == node)
      {
         return "";
      }

      String xPath = getXPathPart(node);

      org.eclipse.stardust.engine.core.struct.sxml.Node parentNode = node.getParent();
      while (parentNode != null)
      {
         if (parentNode.equals(rootNode))
         {
            break;
         }
         xPath = getXPathPart(parentNode) + "/" + xPath;
         parentNode = parentNode.getParent();
      }

      return xPath;
   }
   
   /**
     * checks if XPath is indexed 
     * @param XPath
     * @return
     */   
    public static boolean isIndexedXPath(String xPath) 
    {
       if (-1 == xPath.indexOf("[") && !isRootXPath(xPath))
       {
          return false;
       }
       
       return true;
    }   

   /**
    * Computes "indexed" XPath for given node
    * @param node node to compute the XPath for
    * @param rootNode root to use for computing
    * @return
    */
   public static String getIndexedNodeXPath(org.eclipse.stardust.engine.core.struct.sxml.Node node, NamedNode element)
   {
      String xPath = getXPathPart(node) + "["+getNodeIndex(node)+"]";

      org.eclipse.stardust.engine.core.struct.sxml.Node parentNode = node.getParent();
      while (parentNode != null)
      {
         if (parentNode.equals(element))
         {
            break;
         }
         xPath = getXPathPart(parentNode) + "["+getNodeIndex(parentNode)+"]" + "/" + xPath;
         parentNode = parentNode.getParent();
      }

      return xPath;
   }

   private static int getNodeIndex(org.eclipse.stardust.engine.core.struct.sxml.Node node)
   {
      int positionInEquallyNamedNodes = 1;
      org.eclipse.stardust.engine.core.struct.sxml.Node parentNode = node.getParent();

      if (node instanceof org.eclipse.stardust.engine.core.struct.sxml.Document)
      {
         return 1;
      }

      if (node instanceof org.eclipse.stardust.engine.core.struct.sxml.Attribute)
      {
         return 1;
      }

      String nodeName = ((NamedNode)node).getLocalName();

      for (int i=0; i<parentNode.getChildCount(); i++)
      {
         org.eclipse.stardust.engine.core.struct.sxml.Node child = parentNode.getChild(i);
         if (child.equals(node))
         {
            return positionInEquallyNamedNodes;
         }
         if (child instanceof Element && ((NamedNode)child).getLocalName().equals(nodeName))
         {
            positionInEquallyNamedNodes++;
         }
      }
      throw new InternalException("Should never reach. Node is not inside its parent.");
   }

   private static String getXPathPart(org.eclipse.stardust.engine.core.struct.sxml.Node node)
   {
      if (node instanceof org.eclipse.stardust.engine.core.struct.sxml.Attribute)
      {
         StringBuffer sb = new StringBuffer();
         sb.append('@');
         sb.append(((org.eclipse.stardust.engine.core.struct.sxml.Attribute)node).getLocalName());
         return sb.toString();
      }
      else if (node instanceof Element)
      {
         return ((NamedNode)node).getLocalName();
      }
      else if (node instanceof org.eclipse.stardust.engine.core.struct.sxml.Document)
      {
         return "";
      }
      else
      {
         throw new RuntimeException("unsupported node type <" + node.getClass().getName() + ">");
      }
   }

   /**
    * Return the last part (element or attribute name) of the XPath
    * @param xPath
    * @return
    */
   public static String getLastXPathPart(String xPath)
   {
      if (xPath.indexOf("/") == -1)
      {
         return xPath;
      }
      else
      {
         return xPath.substring(xPath.lastIndexOf("/") + 1);
      }
   }

   /**
    * Creates a {@link StringTokenizer} to iterate over XPath parts
    * @param xPath
    * @return
    */
   public static StringTokenizer getXPathPartTokenizer (String xPath)
   {
      return new StringTokenizer(xPath, "/");
   }

   /**
    * Removes parts of XPath in brackets, including the brackets
    * @param xPath original XPath
    * @return modified XPath
    */
   public static String getXPathWithoutIndexes (String xPath)
   {
      if (isRootXPath(xPath))
      {
         return "";
      }

      if (-1 == xPath.indexOf("["))
      {
         // path does not contain any subscript operator
         return xPath;
      }

      StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
      StringBuffer xPathWithoutIndexes = new StringBuffer(xPath.length());
      while (xPathParts.hasMoreTokens())
      {
         String xPathPart = xPathParts.nextToken();
         if (xPathWithoutIndexes.length() > 0)
         {
            xPathWithoutIndexes.append("/");
         }
         int bracketIndex = xPathPart.indexOf("[");
         if (bracketIndex == -1)
         {
            xPathWithoutIndexes.append(xPathPart);
         }
         else
         {
            xPathWithoutIndexes.append(xPathPart.substring(0, bracketIndex));
         }
      }
      return xPathWithoutIndexes.toString();
   }

   /**
    * Determines if xPath is a root XPath (".", "/", null, "")
    * @param xPath
    * @return
    */
   public static boolean isRootXPath(String xPath)
   {
      if (StringUtils.isEmpty(xPath) || xPath.equals(".") || xPath.equals("/"))
      {
         return true;
      }
      return false;
   }

   /**
    * Determines if xPath is a complex XPath (contains '/')
    * @param xPath
    * @return
    */
   private static boolean hasMultipleSteps(String xPath)
   {
      return (-1 != xPath.indexOf("/"));
   }

   /**
    * Determines if xPath is a simple sub-element access (does not contain '/', '@' and '[')
    * @param xPath
    * @return
    */
   public static boolean isSimpleElementAccess(String xPath)
   {
      return !isRootXPath(xPath) && !hasMultipleSteps(xPath)
         && ( -1 == xPath.indexOf("[")) && ( -1 == xPath.indexOf("@"));
   }

   /**
    * Determines if xPath is a simple attribute access (does not contain '/' and '['
    * but contains '@')
    * @param xPath
    * @return
    */
   public static boolean isSimpleAttributeAccess(String xPath)
   {
      return !isRootXPath(xPath) && !hasMultipleSteps(xPath)
         && ( -1 == xPath.indexOf("[")) && ( -1 != xPath.indexOf("@"));
   }

   /**
    * Returns the node name of the XPath part
    * @param xPathPart
    * @return
    */
   public static String getXPathPartNode (String xPathPart)
   {
      int bracketIndex = xPathPart.indexOf("[");
      if (bracketIndex == -1)
      {
         return xPathPart;
      }
      else
      {
         return xPathPart.substring(0, bracketIndex);
      }
   }

   /**
    * Assumes the brackets always contain an index (integer or double)
    * @param xPathPart
    * @return the integer index value as string, empty string if no index is specified
    */
   public static String getXPathPartIndex (String xPathPart)
   {
      String index = "";
      int bracketIndex = xPathPart.indexOf("[");
      if (bracketIndex != -1)
      {
         index = xPathPart.substring(bracketIndex+1, xPathPart.indexOf("]"));
      }

      // workaround for expressions like "order[1.0]/qty" that come from transition conditions
      int indexOfDotZero = index.indexOf(".0");
      if (indexOfDotZero != -1)
      {
         index = index.substring(0, indexOfDotZero);
      }

      return index;
   }

   /**
    * Analyses the XPath and returns type of single primitive leaf node
    * @param xPath
    * @param xPathMap
    * @return
    */
   public static int returnSinglePrimitiveType(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         TypedXPath rootXPath = xPathMap.getRootXPath();
         return rootXPath.getType();
      }

      if (hasMultipleSteps(xPath))
      {
         // check all XPath elements that no lists are returned
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
         StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();

            if (currentXPathWithoutIndexes.length() > 0)
            {
               currentXPathWithoutIndexes.append("/");
            }
            currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

            TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes.toString());

            String index = getXPathPartIndex(xPathPart);
            if (typedXPath == null || canIndexReturnList(index, typedXPath))
            {
               return BigData.NULL;
            }
         }
      }

      String index = getXPathPartIndex(xPath);
      TypedXPath typedXPath = xPathMap.getXPath(getXPathWithoutIndexes(xPath));
      if (typedXPath == null ||
            !hasMultipleSteps(xPath) && canIndexReturnList(index, typedXPath))
      {
         return BigData.NULL;
      }

      return typedXPath.getType();
   }

   /**
    * Analyses the XPath and returns true if the XPath can only return a single primitive
    * value (no lists, no complex types)
    * @param xPath
    * @param xPathMap
    * @return
    */
   public static boolean returnsSinglePrimitive(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         TypedXPath rootXPath = xPathMap.getRootXPath();
         if (rootXPath.getType() == BigData.NULL)
         {
            return false;
         }
         else
         {
            return true;
         }
      }

      if (hasMultipleSteps(xPath))
      {
         // check all XPath elements that no lists are returned
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
         StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();

            if (currentXPathWithoutIndexes.length() > 0)
            {
               currentXPathWithoutIndexes.append("/");
            }
            currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

            TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes
                  .toString());

            String index = getXPathPartIndex(xPathPart);
            if (canIndexReturnList(index, typedXPath))
            {
               return false;
            }
         }
      }

      // check that the last part type is a primitive
      String xPathWithoutIndexes = getXPathWithoutIndexes(xPath);
      TypedXPath typedXPath = xPathMap.getXPath(xPathWithoutIndexes);
      if (typedXPath.getType() == BigData.NULL || typedXPath.isList())
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public static XPathAnnotations getXPathAnnotations(String xPath, IXPathMap xPathMap)
   {
      String xPathWithoutIndexes = getXPathWithoutIndexes(xPath);
      TypedXPath typedXPath = xPathMap.getXPath(xPathWithoutIndexes);
      return typedXPath.getAnnotations();
   }

   /**
    * Analyses the XPath and returns true if the XPath can only return a single complex
    * type value (no lists, no primitives)
    * @param xPath
    * @param xPathMap
    * @return
    */
   public static boolean returnsSingleComplex(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         TypedXPath rootXPath = xPathMap.getRootXPath();
         if (rootXPath.getType() == BigData.NULL)
         {
            return true;
         }
         else
         {
            return false;
         }
      }

      if (hasMultipleSteps(xPath))
      {
         // check all XPath elements that no lists are returned
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
         StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();

            if (currentXPathWithoutIndexes.length() > 0)
            {
               currentXPathWithoutIndexes.append("/");
            }
            currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

            TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes
                  .toString());

            String index = getXPathPartIndex(xPathPart);
            if (canIndexReturnList(index, typedXPath))
            {
               return false;
            }
         }
      }

      // check that the last part type is a complex type
      String xPathWithoutIndexes = getXPathWithoutIndexes(xPath);
      TypedXPath typedXPath = xPathMap.getXPath(xPathWithoutIndexes);
      if (typedXPath.getType() == BigData.NULL && !typedXPath.isList())
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * Analyses the XPath and returns true if the XPath can only return a list of
    * complex type values (no primitives)
    * @param xPath
    * @param xPathMap
    * @return
    */
   public static boolean returnsListOfComplex(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         return false;
      }

      boolean lastListIsComplex = false;
      if (true/*hasMultipleSteps(xPath)*/)
      {
         // check all XPath elements that no lists are returned
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
         StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();

            if (currentXPathWithoutIndexes.length() > 0)
            {
               currentXPathWithoutIndexes.append("/");
            }
            currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

            TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes
                  .toString());

            String index = getXPathPartIndex(xPathPart);
            if (canIndexReturnList(index, typedXPath)
                  && typedXPath.getType() == BigData.NULL)
            {
               lastListIsComplex = true;
            }
            else
            {
               lastListIsComplex = false;
            }
         }
      }

      return lastListIsComplex;
   }

   /**
    * Analyses the XPath and returns true if the XPath can return lists of primitive or
    * complex values
    * @param xPath
    * @param xPathMap
    * @return
    */
   public static boolean canReturnList(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         return false;
      }

      if ( !hasMultipleSteps(xPath))
      {
         TypedXPath typedXPath = xPathMap.getXPath(getXPathWithoutIndexes(xPath));

         return canIndexReturnList(getXPathPartIndex(xPath), typedXPath);
      }

      StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
      StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
      while (xPathParts.hasMoreTokens())
      {
         String xPathPart = xPathParts.nextToken();

         if (currentXPathWithoutIndexes.length() > 0)
         {
            currentXPathWithoutIndexes.append("/");
         }
         currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

         TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes.toString());

         String index = getXPathPartIndex(xPathPart);
         if (canIndexReturnList(index, typedXPath))
         {
            return true;
         }
      }
      return false;
   }

   private static boolean canIndexReturnList (String index, TypedXPath typedXPath)
   {
      if (typedXPath.getType() != BigData.NULL && !typedXPath.isList() && StringUtils.isEmpty(index))
      {
         return false;
      }

      if (indexPointsToSingleElement(index) && typedXPath.isList())
      {
         // no or single item will be returned
         return false;
      }

      if (StringUtils.isEmpty(index) && !typedXPath.isList())
      {
         // index is empty and xpath ends with element which has maxOccurs=1
         return false;
      }

      return true;
   }

   private static boolean indexPointsToSingleElement (String index)
   {
      if ("last()".equals(index) || "first()".equals(index) || isInteger(index))
      {
         return true;
      }
      return false;
   }

   /**
    * If except for the last XPath part, any of its parts can return lists,
    * this XPath can NOT be used in IN data mappings
    * @param xPath
    * @return
    */
   public static boolean canBeUsedForInDataMapping(String xPath, IXPathMap xPathMap)
   {
      if (isRootXPath(xPath))
      {
         return true;
      }
      else if ( !hasMultipleSteps(xPath))
      {
         return true;
      }
      else
      {
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);

         StringBuffer currentXPathWithoutIndexes = new StringBuffer(xPath.length());
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();
            if (currentXPathWithoutIndexes.length() > 0)
            {
               currentXPathWithoutIndexes.append("/");
            }
            currentXPathWithoutIndexes.append(getXPathPartNode(xPathPart));

            TypedXPath typedXPath = xPathMap.getXPath(currentXPathWithoutIndexes.toString());

            String index = getXPathPartIndex(xPathPart);
            if (canIndexReturnList(index, typedXPath) && xPathParts.hasMoreTokens())
            {
               // multiple items can be returned from non-terminal step - can't write into such XPath
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isInteger(String s)
   {
      if (StringUtils.isEmpty(s))
      {
         return false;
      }
      else if (1 == s.length())
      {
         return Character.isDigit(s.charAt(0));
      }
      else
      {
         try
         {
            Integer.parseInt(s);
            return true;
         }
         catch (Exception e)
         {
            return false;
         }
      }
   }

   /**
    * Searches for the subnode of type TEXT_NODE
    * @param node
    * @return text node value or null if no subnode of type TEXT_NODE found
    */
   public static String findNodeValue(org.eclipse.stardust.engine.core.struct.sxml.Node node)
   {
      String nodeValue = node.getValue();
      if (nodeValue != null && (nodeValue.length() > 0 || node instanceof Attribute))
      {
         return nodeValue;
      }
      else
      {
         return null;
      }
   }

   /**
    * Initialize structured data instance. All Maps and Lists will be created
    *
    * @param xPathMap
    * @param toplevelXPath only initialize below toplevelXPath
    * @param createPrimitiveValues if true, also default values of primitives will be created
    * @return
    */
   public static Object createInitialValue(IXPathMap xPathMap, String toplevelXPath, boolean createPrimitiveValues)
   {
      TypedXPath typedXPath = xPathMap.getXPath(getXPathWithoutIndexes(toplevelXPath));

      if (typedXPath.isList())
      {
         List initialValue = new ArrayList();
         if (typedXPath.getType() == BigData.NULL || typedXPath.getChildXPaths().size() > 0)
         {
            // only add a map value if required (xpath is a list and list entries are complex types or
            // simple types with attributes (CRNT-9711))
            initialValue.add(createInitialValueMap(xPathMap, typedXPath.getXPath(), createPrimitiveValues));
         }
         else if (createPrimitiveValues)
         {
            // otherwise, create one primitive value only if required
            initialValue.add(createSingleInitialValue(typedXPath, createPrimitiveValues));
         }
         return initialValue;
      }
      else
      {
         return createInitialValueMap(xPathMap, typedXPath.getXPath(), createPrimitiveValues);
      }
   }

   /**
    * Initialize structured data instance. All Maps and Lists will be created.
    * No default values of primitives will be created.
    *
    * @param xPathMap
    * @param toplevelXPath only initialize below toplevelXPath
    * @return
    */
   public static Object createInitialValue(IXPathMap xPathMap, String toplevelXPath)
   {
      return createInitialValue(xPathMap, toplevelXPath, false);
   }

   private static Map createInitialValueMap(IXPathMap xPathMap, String toplevelXPath, boolean createPrimitiveValues)
   {
      Map initialValue = new HashMap();

      for (Iterator i = xPathMap.getAllXPaths().iterator(); i.hasNext();)
      {
         TypedXPath typedXPath = (TypedXPath) i.next();

         if ( !typedXPath.getXPath().startsWith(toplevelXPath) || typedXPath.getXPath().equals(toplevelXPath)) {
            // only initialize parts of data specified by toplevelXPath
            continue;
         }

         String subXPath;
         if (toplevelXPath.length() == 0)
         {
            subXPath = typedXPath.getXPath();
         }
         else
         {
            subXPath = typedXPath.getXPath().substring(typedXPath.getXPath().indexOf(toplevelXPath)+toplevelXPath.length()+1);
         }

         // create Maps and Lists for this typedXPath if they do not exist yet
         StringTokenizer xPathParts = getXPathPartTokenizer(subXPath);
         Map currentContainer = initialValue;
         String currentXPath = toplevelXPath;
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();

            if (currentXPath.length() > 0)
            {
               currentXPath += "/";
            }
            currentXPath += xPathPart;

            TypedXPath currentTypedXPath = xPathMap.getXPath(currentXPath);

            Map m = currentContainer;
            if (m.containsKey(xPathPart) == false)
            {
               if (currentTypedXPath.isList())
               {
                  List l = new ArrayList();
                  if (currentTypedXPath.getType() == BigData.NULL || currentTypedXPath.getChildXPaths().size() > 0 || createPrimitiveValues)
                  {
                     l.add(createSingleInitialValue(currentTypedXPath, createPrimitiveValues));
                  }
                  m.put(xPathPart, l);
               }
               else if (currentTypedXPath.getType() == BigData.NULL || currentTypedXPath.getChildXPaths().size() > 0 || createPrimitiveValues)
               {
                  m.put(xPathPart, createSingleInitialValue(currentTypedXPath, createPrimitiveValues));
               }
            }

            // determine next container only if there are tokens to analyse
            if (xPathParts.hasMoreTokens())
            {
               // skip lists for next container
               Object nextContainer = m.get(xPathPart);
               if (nextContainer instanceof List)
               {
                  List l = (List) nextContainer;
                  currentContainer = (Map) l.get(0);
               }
               else
               {
                  currentContainer = (Map) nextContainer;
               }
            }
         }
      }
      return initialValue;
   }

   private static Object createSingleInitialValue(TypedXPath xPath, boolean createPrimitiveValues)
   {
      int typeKey = xPath.getType();

      if (xPath.getChildXPaths().size() > 0)
      {
         // with attributes/subelements
         if (typeKey == BigData.NULL)
         {
            // element without element value and attributes/subelements
            return CollectionUtils.newMap();
         }
         else
         {
            // element with element value and attributes/subelements
            Map m = CollectionUtils.newMap();
            if (createPrimitiveValues)
            {
               m.put(StructuredDataConverter.NODE_VALUE_KEY, createSingleInitialPrimitiveValue(xPath));
            }
            return m;
         }
      }
      else
      {
         // without attributes/subelements
         if (typeKey == BigData.NULL)
         {
            // element without element value and without
            return CollectionUtils.newMap();
         }
         else
         {
            // element with element value and without attributes/subelements
            return createSingleInitialPrimitiveValue(xPath);
         }
      }
   }

   private static Object createSingleInitialPrimitiveValue(TypedXPath xPath)
   {
      int typeKey = xPath.getType();
      if (typeKey == BigData.SHORT)
      {
         return new Short((short)0);
      }
      else if (typeKey == BigData.INTEGER)
      {
         return new Integer(0);
      }
      else if (typeKey == BigData.LONG)
      {
         return new Long(0);
      }
      else if (typeKey == BigData.BYTE)
      {
         return new Byte((byte)0);
      }
      else if (typeKey == BigData.BOOLEAN)
      {
         return new Boolean(false);
      }
      else if (typeKey == BigData.DATE)
      {
         return new Date();
      }
      else if (typeKey == BigData.FLOAT)
      {
         return new Float(0);
      }
      else if (typeKey == BigData.DOUBLE)
      {
         return new Double(0);
      }
      else if (typeKey == BigData.STRING)
      {
         return "";
      }
      else if (typeKey == BigData.BIG_STRING)
      {
         return "";
      }
      else if (typeKey == BigData.PERIOD)
      {
         return new Period((short)0, (short)0, (short)0, (short)0, (short)0, (short)0);
      }
      else
      {
         throw new PublicException("BigData type '"+typeKey+"' is supported yet");
      }
   }

   /**
    * Returns XPath to parent element (or root XPath if no parent)
    * @param xPath
    * @return
    */
   public static String getParentXPath(String xPath)
   {
      if (isRootXPath(xPath))
      {
         return xPath;
      }
      String lastPart = getLastXPathPart(xPath);
      String parent = xPath.substring(0, xPath.length()-lastPart.length());
      if (parent.endsWith("/"))
      {
         parent = parent.substring(0, parent.length()-1);
      }
      return parent;
   }

   private static Element[] toArray(List<Element> elements)
   {
      if (elements == null)
      {
         return new Element[0];
      }
      Element[] nodes = new Element[elements.size()];
      for (int i = 0; i < elements.size(); i++ )
      {
         nodes[i] = elements.get(i);
      }
      return nodes;
   }

   private static boolean isSameOrigin(List nodelist, IXPathMap xPathMap)
   {
      org.eclipse.stardust.engine.core.struct.sxml.Node firstNode = (org.eclipse.stardust.engine.core.struct.sxml.Node) nodelist.get(0);
      TypedXPath firstXPath = xPathMap.getXPath(getNodeXPath(
            firstNode, firstNode.getDocument().getRootElement()));
      for (int i = 1; i < nodelist.size(); i++ )
      {
         TypedXPath p = xPathMap.getXPath(getNodeXPath(
               firstNode, firstNode.getDocument().getRootElement()));
         if ( !firstXPath.equals(p))
         {
            return false;
         }
      }
      return true;
   }

   private static void replaceChildren(Element contextElement, org.eclipse.stardust.engine.core.struct.sxml.Node[] newChildren)
   {
      // remove all children
      contextElement.removeChildren();

      // append new children
      for (int i = 0; i < newChildren.length; i++ )
      {
         newChildren[i].detach();
         contextElement.appendChild(newChildren[i]);
      }
   }

   private static void copyAttributes(Element contextElement, Element newNode)
   {
      for (int i=0; i<newNode.getAttributeCount(); i++)
      {
         Attribute attribute = newNode.getAttribute(i);
         contextElement.addAttribute(new Attribute(attribute.getQualifiedName(), attribute.getNamespaceURI(), attribute.getValue()));
      }
   }

   private static Element findElement(org.eclipse.stardust.engine.core.struct.sxml.Document document, String xPath,
         IXPathMap xPathMap, boolean namespaceAware) throws XPathException
   {
      if (isSimpleElementAccess(xPath))
      {
         // avoid full XPath overhead for plain sub-element access
         return document.getRootElement().getFirstChildElement(xPath, xPathMap.getXPath(xPath).getXsdElementNs());
      }
      else
      {
         XPathEvaluator xPathEvaluator;
         if (xPathMap.containsXPath(xPath))
         {
            // reuse cached xPath
            xPathEvaluator = xPathMap.getXPath(xPath).getCompiledXPath(namespaceAware);
         }
         else
         {
            xPathEvaluator = StructuredDataXPathUtils.createXPathEvaluator(xPath, xPathMap.getRootXPath(), namespaceAware);
         }

         List<Node> nodeList = xPathEvaluator.selectNodes(document.getRootElement());
         if ((null != nodeList ) && !nodeList.isEmpty())
         {
            // "nodeList length>1" is only possible for the last parts of the IN-XPath
            // expression
            // invalid IN-XPath expressions are filtered out before (see
            // StructuredDataXPathUtils.canBeUsedForInDataMapping)
            // so the first node can be returned here

            return (Element)nodeList.get(0);
         }
         return null;
      }
   }

   public static void putValue(org.eclipse.stardust.engine.core.struct.sxml.Document document, IXPathMap xPathMap, String xPath,
         Object value, boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      try
      {
         if (isSimpleElementAccess(xPath)
               && ((null == value)
                     || ( !(value instanceof Collection) && !(value instanceof Map))))
         {
            // optimization for replacing child element values

            Element child = findElement(document, xPath, xPathMap, namespaceAware);
            if (null != value)
            {
               TypedXPath typedXPath = xPathMap.getXPath(xPath);
               String valueString = StructuredDataValueFactory.convertToString(
                     typedXPath.getType(), typedXPath.getXsdTypeName(), value);

               if (null == child)
               {
                  TypedXPath elementXPath = xPathMap.getXPath(xPath);
                  child = createElement(elementXPath, namespaceAware);
                  child.appendChild(new Text(valueString));
                  appendChild(document.getRootElement(), child, elementXPath);
                  return;
               }
               else if ((1 == child.getChildCount())
                     && (child.getChild(0) instanceof Text))
               {
                  Text childValue = (Text) child.getChild(0);
                  childValue.setValue(valueString);
                  return;
               }
            }
            else
            {
               if (null != child)
               {
                  document.getRootElement().removeChild(child);
               }
               return;
            }
         }

         if (isSimpleAttributeAccess(xPath))
         {
            // optimization for setting attributes
            TypedXPath typedXPath = xPathMap.getXPath(xPath);
            String valueString = StructuredDataValueFactory.convertToString(
                  typedXPath.getType(), typedXPath.getXsdTypeName(), value);
            document.getRootElement().addAttribute(createAttribute(typedXPath, valueString));
            return;
         }

         putValues(document, xPathMap, Collections.singletonMap(xPath, value), namespaceAware, ignoreUnknownXPaths);
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   private static void appendChild(Element parent, Element child,
         TypedXPath childXPath)
   {
      // iterate over all children of parent and insert the child only before
      // the sibling with the orderKey > childXPath.orderKey
      List<Element> childElements = parent.getChildElements();
      TypedXPath parentXPath = childXPath.getParentXPath();
      for (int i=0; i<childElements.size(); i++)
      {
         NamedNode sibling = childElements.get(i);
         TypedXPath siblingXPath = parentXPath.getChildXPath(sibling.getLocalName());
         if (siblingXPath == null)
         {
            trace.warn("unexpected child element '" + sibling.getLocalName()
                  + "' found in element '" + parent.getLocalName()
                  + "', it does not correspond to the XPaths derived from the XSD");
         }
         if (siblingXPath.getOrderKey() > childXPath.getOrderKey())
         {
            parent.insertChild(child, i);
            return;
         }
      }
      // otherwise, append at the end
      parent.appendChild(child);
   }

   public static void putValues(org.eclipse.stardust.engine.core.struct.sxml.Document document, IXPathMap xPathMap,
         Map /*<String,Object>*/ values, boolean namespaceAware, boolean ignoreUnknownXPaths)
         throws XPathException, ParserConfigurationException, FactoryConfigurationError
   {
      for (Iterator i = values.keySet().iterator(); i.hasNext(); )
      {
         String xPath = (String)i.next();
         createTree(xPathMap, document, xPath, namespaceAware);
      }

      // now put the value to the prepared tree
      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);

      for (Iterator ee = values.entrySet().iterator(); ee.hasNext(); )
      {
         Entry e = (Entry)ee.next();
         String xPath = (String)e.getKey();
         Object value = e.getValue();

         org.eclipse.stardust.engine.core.struct.sxml.Node[] newNodes = converter.toDom(value, xPath, namespaceAware, ignoreUnknownXPaths);

         if (isRootXPath(xPath))
         {
            if (newNodes.length == 0)
            {
               // whole data is set to null
               replaceChildren(document.getRootElement(), new Element[0]);
            }
            else
            {
               Assert.condition(newNodes.length == 1);
               org.eclipse.stardust.engine.core.struct.sxml.Node newNode = newNodes[0];
               if (newNode instanceof org.eclipse.stardust.engine.core.struct.sxml.Text)
               {
                  // special case - enumeration as top-level element
                  replaceChildren(document.getRootElement(), new org.eclipse.stardust.engine.core.struct.sxml.Node[]{newNode});
               }
               else
               {
                  replaceChildren(document.getRootElement(),
                        toArray(((Element)newNode).getChildElements()));
                  copyAttributes(document.getRootElement(), (Element)newNode);
               }
            }
         }
         else
         {
            Object result;
            if (isSimpleElementAccess(xPath))
            {
               // avoid full XPath overhead for plain sub-element access
               List<Element> childs = document.getRootElement().getChildElements(xPath,
                     xPathMap.getXPath(xPath).getXsdElementNs());
               if (0 == childs.size())
               {
                  result = Collections.EMPTY_LIST;
               }
               else if (1 == childs.size())
               {
                  result = Collections.singletonList(childs.get(0));
               }
               else
               {
                  result = CollectionUtils.newList(childs.size());
                  for (int i = 0; i < childs.size(); ++i)
                  {
                     ((List) result).add(childs.get(i));
                  }
               }
            }
            else if (isSimpleAttributeAccess(xPath))
            {
               Attribute attribute = document.getRootElement().getAttribute(getAttributeName(xPath));
               result = Collections.singletonList(attribute);
            }
            else
            {
               XPathEvaluator xPathEvaluator;
               if (xPathMap.containsXPath(xPath))
               {
                  // reuse cached xPath
                  xPathEvaluator = xPathMap.getXPath(xPath).getCompiledXPath(namespaceAware);
               }
               else
               {
                  xPathEvaluator = StructuredDataXPathUtils.createXPathEvaluator(xPath, xPathMap.getRootXPath(), namespaceAware);
               }
               result = xPathEvaluator.selectNodes(document.getRootElement());
            }

            if (result instanceof List)
            {
               List nodelist = (List) result;
               if (nodelist.size() == 1)
               {
                  // target is a single node
                  org.eclipse.stardust.engine.core.struct.sxml.Node contextElement = (org.eclipse.stardust.engine.core.struct.sxml.Node) nodelist.get(0);

                  if (contextElement instanceof Element)
                  {
                     Element parentNode = (Element)((Element)contextElement).getParent();

                     // append new children
                     for (int i = 0; i < newNodes.length; i++ )
                     {
                        newNodes[i].detach();
                        parentNode.insertChild(newNodes[i], parentNode.indexOf(contextElement));
                     }

                     parentNode.removeChild(contextElement);
                  }
                  else if (contextElement instanceof org.eclipse.stardust.engine.core.struct.sxml.Text)
                  {
                     if (newNodes.length > 1)
                     {
                        throw new PublicException("XPath '" + xPath
                              + "' can not be assigned multiple values");
                     }
                     ((org.eclipse.stardust.engine.core.struct.sxml.Text) contextElement).setValue(findNodeValue(newNodes[0]));
                  }
                  else if (contextElement instanceof Attribute)
                  {
                     ((org.eclipse.stardust.engine.core.struct.sxml.Attribute)contextElement).setValue(findNodeValue(newNodes[0]));
                  }
                  else
                  {
                     throw new PublicException("XPath '" + xPath
                           + "' can not be used to set data value");
                  }
               }
               else if (nodelist.isEmpty())
               {
                  // no target found
                  throw new PublicException("No data found for XPath '" + xPath + "'");
               }
               else
               {
                  // multiple nodes as a target
                  if ( !isSameOrigin(nodelist, xPathMap))
                  {
                     // they are NOT from the same XPath
                     throw new PublicException(
                           "Xath '"
                                 + xPath
                                 + "' can not be used to set data value since it returns nodes from different origins");
                  }

                  Element parentNode = (Element)((org.eclipse.stardust.engine.core.struct.sxml.Node) nodelist.get(0)).getParent();

                  // append new children
                  for (int i = 0; i < newNodes.length; i++ )
                  {
                     newNodes[i].detach();
                     parentNode.insertChild(newNodes[i], i);
                  }

                  // remove all that must be replaced
                  for (int i = 0; i < nodelist.size(); i++ )
                  {
                     parentNode.removeChild((org.eclipse.stardust.engine.core.struct.sxml.Node) nodelist.get(i));
                  }
               }
            }
            else
            {
               throw new PublicException("inPath '" + xPath
                     + "' can not be used to set data value");
            }
         }
      }
   }

   private static void createTree(IXPathMap xPathMap, org.eclipse.stardust.engine.core.struct.sxml.Document document, String xPath,
         boolean namespaceAware) throws XPathException, ParserConfigurationException, FactoryConfigurationError
   {
      if (isRootXPath(xPath))
      {
         // nothing to create/check if the whole value will be replaced
         return;
      }

      if ( !hasMultipleSteps(xPath))
      {
         if (-1 == getLastXPathPart(xPath).indexOf('@'))
         {
            NamedNode element = findElement(document, xPath, xPathMap, namespaceAware);

            if (null == element)
            {
               // element must be created
               TypedXPath elementXPath = xPathMap.getXPath(getXPathWithoutIndexes(xPath));
               element = createNeededElements(elementXPath, xPath, document.getRootElement(), namespaceAware);
            }
         }
         else
         {
            if (document.getRootElement().getAttribute(getAttributeName(xPath)) == null)
            {
               document.getRootElement().addAttribute(createAttribute(xPathMap.getXPath(xPath), ""));
            }
         }
      }
      else
      {
         StringTokenizer xPathParts = getXPathPartTokenizer(xPath);
         StringBuffer currentXPath = new StringBuffer(xPath.length());
         Element parentElement = document.getRootElement();
         while (xPathParts.hasMoreTokens())
         {
            String xPathPart = xPathParts.nextToken();
            if (currentXPath.length() > 0)
            {
               currentXPath.append("/");
            }
            currentXPath.append(xPathPart);

            if (-1 == getLastXPathPart(currentXPath.toString()).indexOf('@'))
            {
               Element element = findElement(document, currentXPath.toString(), xPathMap, namespaceAware);

               if (element == null)
               {
                  // element must be created
                  TypedXPath elementXPath = xPathMap.getXPath(getXPathWithoutIndexes(currentXPath.toString()));
                  element = createNeededElements(elementXPath, xPathPart, parentElement, namespaceAware);
               }
               parentElement = element;
            }
            else
            {
               if (parentElement.getAttribute(getAttributeName(currentXPath.toString())) == null)
               {
                  String xPathWithoutPrefixes = getXPathWithoutIndexes(currentXPath.toString());
                  parentElement.addAttribute(createAttribute(xPathMap.getXPath(xPathWithoutPrefixes), ""));
               }
               // not setting parentElement, since attributes are always last part of the xpath
            }
         }
      }
   }

   private static Element createNeededElements(TypedXPath elementXPath, String xPathPart, Element parentElement, boolean namespaceAware)
   {
      Element result = null;

      String indexExpression = getXPathPartIndex(xPathPart);

      int targetIndex;
      if (isEmpty(indexExpression))
      {
         targetIndex = 1;
      }
      else
      {
      try
      {
            targetIndex = new Double(indexExpression).intValue();
      }
      catch (NumberFormatException e)
      {
         // ignore unknown index syntax (or unspecified), assume only one element must be created
         targetIndex = 1;
      }
      }

      if (targetIndex == 1)
      {
         // assume only one element should be created if no index is specified
         result = createElement(elementXPath, namespaceAware);
         appendChild(parentElement, result, elementXPath);
      }
      else
      {
         // several elements must be created
         int currentCount;
         if (namespaceAware)
         {
            currentCount = parentElement.getChildElements(xPathPart,
                  elementXPath.getXsdElementNs()).size();
         }
         else
         {
            currentCount = parentElement.getChildElements(xPathPart).size();
         }

         for (int i = currentCount; i < targetIndex; i++)
         {
            result = createElement(elementXPath, namespaceAware);
            appendChild(parentElement, result, elementXPath);
         }

      }

      return result;
   }

   public static String getAttributeName(String xPath)
   {
      String lastXPathPart = StructuredDataXPathUtils.getLastXPathPart(xPath);
      return lastXPathPart.substring(lastXPathPart.indexOf('@')+1);
   }

   public static Attribute createAttribute(TypedXPath typedXPath, String valueString)
   {
      return new Attribute(getAttributeName(typedXPath.getXPath()), /*typedXPath.getXsdTypeNs(),*/ valueString);
   }

   public static Element createElement(TypedXPath typedXPath,
         boolean namespaceAware)
   {
      String elementName;
      if (isRootXPath(typedXPath.getXPath()))
      {
         if ( !StringUtils.isEmpty(typedXPath.getXsdElementName()))
         {
            elementName = typedXPath.getXsdElementName();
         }
         else if ( !StringUtils.isEmpty(typedXPath.getXsdTypeName()))
         {
            elementName = typedXPath.getXsdTypeName();
         }
         else
         {
            elementName = IStructuredDataValue.ROOT_ELEMENT_NAME;
         }
      }
      else
      {
         elementName = StructuredDataXPathUtils.getLastXPathPart(typedXPath.getXPath());
      }
      return namespaceAware //
            ? new Element(elementName, typedXPath.getXsdElementNs())
            : new Element(elementName);
   }

   /**
    * Returns true, if the element denoted by the supplied XPath
    * can have attributes as well as content (e.g. complex type
    * with attributes and with simple content extending a simple type)
    * @param typedXPath
    * @return
    */
   public static boolean canHaveContentAndAttributes(TypedXPath typedXPath)
   {
      if (typedXPath.getChildXPaths().size() > 0 && typedXPath.getType() != BigData.NULL)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * Creates an XPathEvaluator from an xpath supplied by the modeler
    * @param unqualifiedXPath
    * @param rootXPath
    * @param namespaceAware if set to true, the unprefixed steps in the XPath will
    * be prefixed with namespaces according to the XSD definition
    * @return
    */
   public static XPathEvaluator createXPathEvaluator(String unqualifiedXPath, TypedXPath rootXPath, boolean namespaceAware)
   {
      try
      {
         if (namespaceAware)
         {
            Map<String, String> nsMappings = newHashMap();
            String qualifiedXPath = NamespaceContextBuilder.toNamespaceQualifiedXPath(
                  unqualifiedXPath, rootXPath, nsMappings);
            return XPathEvaluator.compileXPath(qualifiedXPath, nsMappings);
         }
         else
         {
            return XPathEvaluator.compileXPath(unqualifiedXPath);
         }
      }
      catch (Exception e)
      {
         throw new PublicException("Could not create qualified XPath from xPath '"+unqualifiedXPath+"'", e);
      }
   }

   /**
    * Searches for root xpath starting from some xpath
    * @param typedXPath
    * @return
    */
   public static TypedXPath findRootXPath(TypedXPath typedXPath)
   {
      while ( typedXPath.getParentXPath() != null)
      {
         typedXPath = typedXPath.getParentXPath();
      }
      return typedXPath;
   }

   /**
    * Checks if the document is "namespaceaware" (e.g. uses namespaces)
    * @param document
    * @return
    */
   public static boolean isNamespaceAware(Document document)
   {
      if (StringUtils.isEmpty(document.getRootElement().getNamespaceURI()))
      {
         return false;
      }
      return true;

   }
}