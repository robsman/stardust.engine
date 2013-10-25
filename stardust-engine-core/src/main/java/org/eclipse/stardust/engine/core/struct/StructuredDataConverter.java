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

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.xml.transform.TransformerException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.sxml.*;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator;
import org.eclipse.xsd.util.XSDConstants;


/**
 * Conversions DOM -> Collection (Map/List/Primitive) and vice versa
 */
public class StructuredDataConverter
{
   private static boolean isStrict = Parameters.instance().getBoolean("XPath.StrictEvaluation", true);

   public static final String NODE_VALUE_KEY = "@";
   private IXPathMap xPathMap;

   public StructuredDataConverter(IXPathMap xPathMap)
   {
      this.xPathMap = xPathMap;
   }

   /**
    * Converts elements returned by xPath to Map/List/Primitive
    * @param xml The source XML to convert.
    * @param xPath
    * @param namespaceAware
    * @return
    * @throws TransformerException
    * @throws PublicException
    */
   public Object toCollection(String xml, String xPath, boolean namespaceAware) throws PublicException
   {
      Document document;
      try
      {
         document = DocumentBuilder.buildDocument(new java.io.StringReader(xml));

         return toCollection(document.getRootElement(), xPath, namespaceAware);
      }
      catch (PublicException e)
      {
         throw e;
      }
      catch (IOException e)
      {
         throw new PublicException("Failed reading XML input.", e);
      }
   }

   /**
    * Converts elements returned by xPath to Map/List/Primitive
    * @param rootNode
    * @param xPath
    * @param namespaceAware
    * @return
    * @throws TransformerException
    */
   public Object toCollection(Element rootNode, String xPath, boolean namespaceAware)
   {
      String xPathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(xPath);
      String targetElementName = StructuredDataXPathUtils.getLastXPathPart(this.xPathMap.getXPath(xPathWithoutIndexes).getXPath());
      if ( !StringUtils.isEmpty(targetElementName)
            && rootNode.getLocalName().equals(targetElementName)
            && (rootNode.getChildCount() == 0 || !hasSameNameChild(rootNode,
                  targetElementName)))
      {
         // a subdocument (not the whole document) is supplied
         // create additional nodes and attach rootNode to it
         TypedXPath rootXPath = xPathMap.getRootXPath();
         Element newRootNode = StructuredDataXPathUtils.createElement(rootXPath, namespaceAware);

         StringTokenizer st = StructuredDataXPathUtils.getXPathPartTokenizer(
               StructuredDataXPathUtils.getParentXPath(xPathWithoutIndexes));

         Element e = newRootNode;
         TypedXPath currentXPath = rootXPath;
         while (st.hasMoreTokens())
         {
            String xPathPart = st.nextToken();
            currentXPath = currentXPath.getChildXPath(xPathPart);
            Element newChild = StructuredDataXPathUtils.createElement(currentXPath, namespaceAware);
            e.appendChild(newChild);
            e = newChild;
         }

         // need to copy because if rootNode is a root element of a docuemnt
         // it is impossible to detach it, anyway, it is better to copy
         // since it is dangerous to manipulate the original rootNode
         // Note: this if is only entered in process debugger
         e.appendChild(rootNode.copy());
         rootNode = newRootNode;
      }

      List<Node> nodelist = evaluateXPath(rootNode, xPath, namespaceAware);

      if (StructuredDataXPathUtils.returnsSinglePrimitive(xPath, this.xPathMap))
      {
         // return primitive type
         if (nodelist.size() > 1)
         {
            throw new PublicException("Expression '"+xPath+"' was expected to return 0 or 1 hits, but it returned '"+nodelist.size()+"'");
         }
         if (nodelist.isEmpty())
         {
            return null;
         }
         else
         {
            Node node = (Node) nodelist.get(0);
            TypedXPath typedXPath = getXPath(rootNode, node);
            if (typedXPath == null)
            {
               return null;
            }
            return StructuredDataValueFactory.convertTo(typedXPath.getType(), StructuredDataXPathUtils.findNodeValue(node));
         }
      }
      else if (StructuredDataXPathUtils.canReturnList(xPath, this.xPathMap))
      {
         // return List (List of complexTypes or primitives)
         List list = new ArrayList();
         for (int i=0; i<nodelist.size(); i++)
         {
            Node node = (Node) nodelist.get(i);

            TypedXPath typedXPath = getXPath(rootNode, node);
            if (typedXPath.getType() == BigData.NULL)
            {
               // complex type
               list.add(createComplexType(rootNode, (Element) node));
            }
            else
            {
               // primitive
               list.add(StructuredDataValueFactory.convertTo(typedXPath.getType(), StructuredDataXPathUtils.findNodeValue(node)));
            }
         }
         return list;
      }
      else
      {
         // return Map (complexType)
         if (nodelist.size() > 1)
         {
            throw new PublicException("Expression '"+xPath+"' was expected to return 0 or 1 hits, but it returned '"+nodelist.size()+"'");
         }

         if (nodelist.isEmpty())
         {
            return Collections.EMPTY_MAP;
         }

         Element node = (Element) nodelist.get(0);
         return createComplexType(rootNode, node);
      }
   }

   private boolean hasSameNameChild(Element rootNode, String targetElementName)
   {
      List<Element> childElements = rootNode.getChildElements();
      if (childElements != null)
      {
         for (Element element : childElements)
         {
            String localName = element.getLocalName();
            if (!StringUtils.isEmpty(localName) && localName.equals(targetElementName))
            {
               return true;
            }

            if (hasSameNameChild(element, targetElementName))
            {
               return true;
            }
         }
      }

      return false;
   }

   public List<Node> evaluateXPath(Element rootNode, String xPath, boolean namespaceAware)
   {
      List<Node> nodelist;
      if (StructuredDataXPathUtils.isRootXPath(xPath))
      {
         nodelist = Collections.singletonList((Node) rootNode);
      }
      else
      {
         if (StructuredDataXPathUtils.isSimpleElementAccess(xPath))
         {
            // try to find child element directly
            List<Element> children;
            if (namespaceAware)
            {
               children = rootNode.getChildElements(xPath, xPathMap.getXPath(xPath).getXsdElementNs());
            }
            else
            {
               children = rootNode.getChildElements(xPath);
            }

            if (0 == children.size())
            {
               nodelist = Collections.EMPTY_LIST;
            }
            else if (1 == children.size())
            {
               nodelist = Collections.singletonList((Node) children.get(0));
            }
            else
            {
               nodelist = CollectionUtils.newList(children.size());
               for (int i = 0; i < children.size(); ++i)
               {
                  nodelist.add(children.get(i));
               }
            }
         }
         else if (StructuredDataXPathUtils.isSimpleAttributeAccess(xPath))
         {
            Node leaf = null;
            // try to find attribute element directly
            String attributeName = xPath.substring(xPath.indexOf('@')+1);
            if (attributeName.isEmpty())
            {
               for (Node child : rootNode.getChildren())
               {
                  if (child instanceof Text)
                  {
                     leaf = child;
                     break;
                  }
               }
            }
            else
            {
               leaf = rootNode.getAttribute(attributeName);
            }
            nodelist = leaf == null ? Collections.<Node>emptyList() : Collections.singletonList(leaf);
         }
         else
         {
            try
            {
               XPathEvaluator xPathEvaluator;
               if (this.xPathMap.containsXPath(xPath))
               {
                  // reuse cached xPath
                  xPathEvaluator = this.xPathMap.getXPath(xPath).getCompiledXPath(namespaceAware);
               }
               else
               {
                  xPathEvaluator = StructuredDataXPathUtils.createXPathEvaluator(xPath, xPathMap.getRootXPath(), namespaceAware);
               }
               nodelist = xPathEvaluator.selectNodes(rootNode);
            }
            catch (Exception e)
            {
               throw new InternalException(e);
            }
         }
      }
      return nodelist;
   }

   private Map createComplexType(final Element rootNode, Element parentNode)
   {
      Map complexType = new HashMap();

      // node value
      TypedXPath parentXPath = getXPath(rootNode, parentNode);
      if (parentXPath == null)
      {
         return complexType;
      }

      if (parentXPath.getType() != BigData.NULL)
      {
         // there is a node value
         complexType.put(NODE_VALUE_KEY, StructuredDataValueFactory.convertTo(parentXPath.getType(),
               StructuredDataXPathUtils.findNodeValue(parentNode)));
      }
      else
      {
         TypedXPath childXPath = parentXPath.getChildXPath(NODE_VALUE_KEY);
         if (childXPath != null)
         {
            // there is a node value
            complexType.put(NODE_VALUE_KEY, StructuredDataValueFactory.convertTo(childXPath.getType(),
                  StructuredDataXPathUtils.findNodeValue(parentNode)));
         }
      }

      // attributes
      for (int i=0; i<parentNode.getAttributeCount(); i++)
      {
         Attribute attribute = parentNode.getAttribute(i);

         String xPath = StructuredDataXPathUtils.getNodeXPath(attribute, rootNode);
         if (!this.xPathMap.containsXPath(xPath))
         {
            if ("http://www.w3.org/2001/XMLSchema-instance".equals(attribute.getNamespaceURI()) ||
                  IStructuredDataValue.STRUCTURED_DATA_NAMESPACE.equals(attribute.getNamespaceURI()))
            {
               // ignore unknown attributes from xsi namespace (http://www.w3.org/2001/XMLSchema-instance)
               continue;
            }
            else if (isStrict)
            {
               throw new PublicException("Undefined XPath: '"+xPath+"'.");
            }
            else
            {
               continue;
            }
         }
         TypedXPath typedXPath = xPathMap.getXPath(xPath);

         // attributes are always primitives
         complexType.put(
               StructuredDataXPathUtils.getLastXPathPart(typedXPath.getXPath()),
               StructuredDataValueFactory.convertTo(typedXPath.getType(), StructuredDataXPathUtils.findNodeValue(attribute)));
      }

      // elements
      List<Element> childElements = parentNode.getChildElements();

      for (int i = 0; i < childElements.size(); i++ )
      {
         Element childNode = childElements.get(i);
         TypedXPath typedXPath = getXPath(rootNode, childNode);
         if (typedXPath == null && !isStrict)
         {
            continue;
         }
         String nodeName = childNode.getLocalName();
         if (typedXPath.getType() == BigData.NULL || typedXPath.getChildXPaths().size() > 0) {
            // complexType or List of complexTypes
            if (typedXPath.isList())
            {
               // List of complexTypes
               if ( !complexType.containsKey(nodeName))
               {
                  complexType.put(nodeName, new ArrayList());
               }
               ((List)complexType.get(nodeName)).add(this.createComplexType(rootNode, childNode));
            }
            else
            {
               // complexType
               complexType.put(nodeName, this.createComplexType(rootNode, childNode));
            }
         }
         else
         {
            // primitive or list of primitives
            if (typedXPath.isList())
            {
               // List of primitives
               if ( !complexType.containsKey(nodeName))
               {
                  complexType.put(nodeName, new ArrayList());
               }
               ((List)complexType.get(nodeName)).add(StructuredDataValueFactory.convertTo(typedXPath.getType(), StructuredDataXPathUtils.findNodeValue(childNode)));
            }
            else
            {
               // primitive
               complexType.put(nodeName, StructuredDataValueFactory.convertTo(typedXPath.getType(), StructuredDataXPathUtils.findNodeValue(childNode)));
            }
         }
      }
      return complexType;
   }

   private TypedXPath getXPath(final Node rootNode, Node node)
   {
      String xPath = StructuredDataXPathUtils.getNodeXPath(node, rootNode);

      if (this.xPathMap.containsXPath(xPath))
      {
         return xPathMap.getXPath(xPath);
      }
      else if (isStrict)
      {
         throw new PublicException("Undefined XPath: '"+xPath+"'.");
      }
      return null;
   }

   /**
    * Converts object (List/Map/Primitive) from location specified by targetXPath to DOM
    * node(s)
    *
    * @param object
    * @param targetXPath
    * @param namespaceAware
    * @return
    */
   public Node[] toDom(Object object, String targetXPath, boolean namespaceAware)
   {
      return toDom(object, targetXPath, namespaceAware, false);
   }

   /**
    * Converts object (List/Map/Primitive) from location specified by targetXPath to DOM
    * node(s)
    *
    * @param object
    * @param targetXPath
    * @param namespaceAware
    * @param ignoreUnknownXPaths if true, do not throw errors on unknown XPaths
    * @return
    */
   public Node[] toDom(Object object, String targetXPath, boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      if (object == null)
      {
         // special handling for null values
         return new Element[0];
      }
      else if (object instanceof org.w3c.dom.Element)
      {
         // already a DOM element, convert to SXML
         return new Node[]{org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter.convert((org.w3c.dom.Element)object)};
      }
      else if (object instanceof List)
      {
         // List
         TypedXPath typedXPath = this.xPathMap.getXPath(StructuredDataXPathUtils.getXPathWithoutIndexes(targetXPath));
         Element node = StructuredDataXPathUtils.createElement(typedXPath, namespaceAware);
         fillNodeList ((List)object, node, typedXPath, namespaceAware, ignoreUnknownXPaths);
         List<Element> childNodes = node.getChildElements();
         Element [] nodes = new Element[childNodes.size()];
         for (int i=0; i<nodes.length; i++)
         {
            nodes[i] = childNodes.get(i);
         }
         return nodes;
      }
      else if (object instanceof Map)
      {
         // complexType
         TypedXPath typedXPath = this.xPathMap.getXPath(StructuredDataXPathUtils.getXPathWithoutIndexes(targetXPath));
         Element node = StructuredDataXPathUtils.createElement(typedXPath, namespaceAware);
         fillComplexType((Map)object, node, StructuredDataXPathUtils.getXPathWithoutIndexes(targetXPath), namespaceAware, ignoreUnknownXPaths);
         return new Element[]{node};
      }
      else
      {
         // primitive
         TypedXPath typedXPath = this.xPathMap.getXPath(StructuredDataXPathUtils.getXPathWithoutIndexes(targetXPath));
         String valueString = StructuredDataValueFactory.convertToString(typedXPath.getType(), typedXPath.getXsdTypeName(), object);

         String lastXPathPart = StructuredDataXPathUtils.getLastXPathPart(typedXPath.getXPath());
         if (StructuredDataXPathUtils.isRootXPath(targetXPath))
         {
            // no element must be created if the root is a primitive
            return new Node[]{new Text(valueString)};
         }
         else if ( -1 != lastXPathPart.indexOf('@'))
         {
            // create an attribute node
            return new Node[]{StructuredDataXPathUtils.createAttribute(typedXPath, valueString)};
         }
         else
         {
            // create an element node
            Element node = StructuredDataXPathUtils.createElement(typedXPath, namespaceAware);
            node.appendChild(new Text(valueString));
            return new Node[]{node};
         }
      }
   }

   public String toString(Object value, String sourceXPath)
   {
      Node [] nodes = toDom(value, sourceXPath, true);
      return toString(nodes, sourceXPath);
   }

   private String toString(Node[] nodes, String sourceXPath)
   {
      if (nodes.length == 1)
      {
         Document document = new Document((Element)nodes[0]);
         return document.toXML();
      }
      else if (nodes.length > 1)
      {
         String pathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(sourceXPath);
         String[] parents = pathWithoutIndexes.split("/");
         StringBuffer sb = new StringBuffer();
         sb.append("<dummy>");
         for (int i = 0; i < parents.length - 1; i++)
         {
            sb.append('<');
            sb.append(parents[i]);
            sb.append('>');
         }
         for (int i = 0; i < nodes.length; i++)
         {
            sb.append(nodes[i].toXML());
         }
         for (int i = parents.length - 2; i >= 0; i--)
         {
            sb.append("</");
            sb.append(parents[i]);
            sb.append('>');
         }
         sb.append("</dummy>");
         return sb.toString();
      }
      return null;
   }

   private void fillComplexType(Map /* <String,Object> */complexType, Element node,
         final String xPath, boolean namespaceAware, final boolean ignoreUnknownXPaths)
   {
      SortedSet /*<String>*/ sortedKeys = new TreeSet(new Comparator ()
      {
         public int compare(Object o1, Object o2)
         {
            TypedXPath childXPath1 = composeChildXPath(xPath, (String) o1, ignoreUnknownXPaths);
            int orderKey1 = (childXPath1 == null) ? -1 : childXPath1.getOrderKey();
            TypedXPath childXPath2 = composeChildXPath(xPath, (String) o2, ignoreUnknownXPaths);
            int orderKey2 = (childXPath2 == null) ? -1 :childXPath2.getOrderKey();
            return new Integer(orderKey1).compareTo(new Integer(orderKey2));
         }
      });
      sortedKeys.addAll(complexType.keySet());

      for (Iterator i = sortedKeys.iterator(); i.hasNext(); )
      {
         String key = (String) i.next();
         Object value = complexType.get(key);

         TypedXPath childXPath = composeChildXPath(xPath, key, ignoreUnknownXPaths);

         if (childXPath != null && childXPath.getAnnotations().isPersistent())
         {
            processObject(value, key, childXPath, node, namespaceAware, ignoreUnknownXPaths);
         }
      }
   }

   private TypedXPath composeChildXPath (String parentXPath, String child, boolean ignoreUnknownXPaths)
   {
      // node value in a complex type
      if (child.equals(NODE_VALUE_KEY))
      {
         return getXPathIfExists(parentXPath, ignoreUnknownXPaths);
      }
      else
      {
         if (StructuredDataXPathUtils.isRootXPath(parentXPath))
         {
            return getXPathIfExists(child, ignoreUnknownXPaths);
         }
         else
         {
            StringBuffer childXPath = new StringBuffer(parentXPath);
            childXPath.append("/");
            childXPath.append(child);
            return getXPathIfExists(childXPath.toString(), ignoreUnknownXPaths);
         }
      }
   }

   private TypedXPath getXPathIfExists(String xPath, boolean ignoreUnknownXPaths)
   {
      if (this.xPathMap.containsXPath(xPath) || (!ignoreUnknownXPaths && isStrict))
      {
         return this.xPathMap.getXPath(xPath);
      }
      else
      {
         return null;
      }
   }

   private void fillNodeList(List complexTypeList, Element node, TypedXPath childXPath,
         boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      String key = StructuredDataXPathUtils.getLastXPathPart(childXPath.getXPath());
      for (Iterator i = complexTypeList.iterator(); i.hasNext(); )
      {
         processObject(i.next(), key, childXPath, node, namespaceAware, ignoreUnknownXPaths);
      }

   }

   private void processObject(Object value, String key, TypedXPath childXPath,
         Element parentNode, boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      if (key.equals(NODE_VALUE_KEY))
      {
         // special case for node value
         // (fh) if this is a complex type with simple content and attributes,
         // fetch the actual value xpath from the children.
         TypedXPath xPath = childXPath.getChildXPath(key);
         if (xPath != null)
         {
            childXPath = xPath;
         }
         String valueString = StructuredDataValueFactory.convertToString(childXPath.getType(), childXPath.getXsdTypeName(), value);
         parentNode.appendChild(new Text(valueString));
      }
      else if (value instanceof List)
      {
         fillNodeList ((List)value, parentNode, childXPath, namespaceAware, ignoreUnknownXPaths);
      }
      else if (value instanceof Map)
      {
         Element childNode = StructuredDataXPathUtils.createElement(childXPath, namespaceAware);
         fillComplexType((Map)value, childNode, childXPath.getXPath(), namespaceAware, ignoreUnknownXPaths);
         parentNode.appendChild(childNode);
      }
      else if ( -1 != key.indexOf('@'))
      {
         // TODO namespace
         // set an attribute
         String valueString = StructuredDataValueFactory.convertToString(childXPath.getType(), childXPath.getXsdTypeName(), value);
         parentNode.addAttribute(StructuredDataXPathUtils.createAttribute(childXPath, valueString));
      }
      else
      {
         // create an element
         Element childNode = StructuredDataXPathUtils.createElement(childXPath, namespaceAware);
         boolean isPlainText = true;
         if (isAnyType(childXPath) && value != null && value.toString().indexOf('<') >= 0)
         {
            try
            {
               // (fh) we need to wrap in case the value is a list of nodes (Text or Element)
               String content = "<wrapper>" + value + "</wrapper>";
               Document doc = DocumentBuilder.buildDocument(new StringReader(content));
               Element wrapper = doc.getRootElement();
               isPlainText = false;
               while (wrapper.getChildCount() > 0)
               {
                  Node node = wrapper.getChild(0);
                  node.detach();
                  childNode.appendChild(node);
               }
            }
            catch (Exception e)
            {
               // fallback to plain text
            }
         }
         if (isPlainText)
         {
            String valueString = StructuredDataValueFactory.convertToString(childXPath.getType(), childXPath.getXsdTypeName(), value);
            childNode.appendChild(new Text(valueString));
         }
         parentNode.appendChild(childNode);
      }
   }

   // (fh) From XSDConstants.isAnyType(XSDTypeDefinition) adapted to use a TypedXPath.
   private boolean isAnyType(TypedXPath childXPath)
   {
      return XSDConstants.isSchemaForSchemaNamespace(childXPath.getXsdTypeNs()) &&
            "anyType".equals(childXPath.getXsdTypeName());
   }
}
