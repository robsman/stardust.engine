/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
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

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
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

   private boolean validating;

   public StructuredDataConverter(IXPathMap xPathMap)
   {
      this(xPathMap, false);
   }

   public StructuredDataConverter(IXPathMap xPathMap, boolean validating)
   {
      this.xPathMap = xPathMap;
      this.validating = validating;
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
         throw new PublicException(BpmRuntimeError.SDT_FAILED_READING_XML_INPUT.raise(), e);
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
      TypedXPath rootXPath = xPathMap.getRootXPath();
      TypedXPath currentXPath = rootXPath;

      String xPathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(xPath);
      String targetElementName = StructuredDataXPathUtils.getLastXPathPart(xPathWithoutIndexes);
      if ( !StringUtils.isEmpty(targetElementName)
            && rootNode.getLocalName().equals(targetElementName)
            && (rootNode.getChildCount() == 0 || !hasSameNameChild(rootNode,
                  targetElementName)))
      {
         // a subdocument (not the whole document) is supplied
         // create additional nodes and attach rootNode to it
         Element newRootNode = StructuredDataXPathUtils.createElement(rootXPath, namespaceAware);

         StringTokenizer st = StructuredDataXPathUtils.getXPathPartTokenizer(
               StructuredDataXPathUtils.getParentXPath(xPathWithoutIndexes));

         Element e = newRootNode;
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

      if (StructuredDataXPathUtils.returnsSinglePrimitive(xPath, xPathMap))
      {
         // return primitive type
         if (nodelist.size() > 1)
         {
            throw new PublicException(
                  BpmRuntimeError.SDT_EXPRESSION_WAS_EXPECTED_TO_RETURN_0_OR_1_HITS
                        .raise(xPath, nodelist.size()));
         }
         if (nodelist.isEmpty())
         {
            return null;
         }
         else
         {
            Node node = (Node) nodelist.get(0);
            TypedXPath typedXPath = getXPath(currentXPath, rootNode, node);
            if (typedXPath == null)
            {
               return null;
            }
            String nodeValue = StructuredDataXPathUtils.findNodeValue(node);
            validate(typedXPath, nodeValue);
            return StructuredDataValueFactory.convertTo(typedXPath.getType(), nodeValue);
         }
      }
      else if (StructuredDataXPathUtils.canReturnList(xPath, xPathMap))
      {
         // return List (List of complexTypes or primitives)
         List list = new ArrayList();
         for (int i=0; i<nodelist.size(); i++)
         {
            Node node = (Node) nodelist.get(i);

            TypedXPath typedXPath = getXPath(currentXPath, rootNode, node);
            if (typedXPath.getType() == BigData.NULL)
            {
               // complex type
               list.add(createComplexType(currentXPath, rootNode, (Element) node));
            }
            else
            {
               // primitive
               String nodeValue = StructuredDataXPathUtils.findNodeValue(node);
               validate(typedXPath, nodeValue);
               list.add(StructuredDataValueFactory.convertTo(typedXPath.getType(), nodeValue));
            }
         }
         return list;
      }
      else
      {
         // return Map (complexType)
         if (nodelist.size() > 1)
         {
            throw new PublicException(
                  BpmRuntimeError.SDT_EXPRESSION_WAS_EXPECTED_TO_RETURN_0_OR_1_HITS
                        .raise(xPath, nodelist.size()));
         }

         if (nodelist.isEmpty())
         {
            return Collections.EMPTY_MAP;
         }

         Element node = (Element) nodelist.get(0);
         return createComplexType(currentXPath, rootNode, node);
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
               XPathEvaluator xPathEvaluator = StructuredDataXPathUtils.getXPathEvaluator(xPathMap, xPath, namespaceAware);
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

   private Map createComplexType(TypedXPath parentXPath, final Element rootNode, Element parentNode)
   {
      Map complexType = new HashMap();

      // node value
      if (parentXPath == null)
      {
         return complexType;
      }

      if (parentXPath.getType() != BigData.NULL)
      {
         // there is a node value
         String parentNodeValue = StructuredDataXPathUtils.findNodeValue(parentNode);
         validate(parentXPath, parentNodeValue);
         complexType.put(NODE_VALUE_KEY, StructuredDataValueFactory.convertTo(parentXPath.getType(), parentNodeValue));
      }
      else
      {
         TypedXPath childXPath = parentXPath.getChildXPath(NODE_VALUE_KEY);
         if (childXPath != null)
         {
            // there is a node value
            String parentNodeValue = StructuredDataXPathUtils.findNodeValue(parentNode);
            validate(childXPath, parentNodeValue);
            complexType.put(NODE_VALUE_KEY, StructuredDataValueFactory.convertTo(childXPath.getType(), parentNodeValue));
         }
      }

      // attributes
      for (int i = 0; i < parentNode.getAttributeCount(); i++)
      {
         Attribute attribute = parentNode.getAttribute(i);

         String xPath = StructuredDataXPathUtils.getNodeXPath(attribute, rootNode);
         if (!xPathMap.containsXPath(xPath))
         {
            if ("http://www.w3.org/2001/XMLSchema-instance".equals(attribute.getNamespaceURI()) ||
                  IStructuredDataValue.STRUCTURED_DATA_NAMESPACE.equals(attribute.getNamespaceURI()))
            {
               // ignore unknown attributes from xsi namespace (http://www.w3.org/2001/XMLSchema-instance)
               continue;
            }
         }
         TypedXPath typedXPath = getXPath(parentXPath, rootNode, attribute);
         if (typedXPath != null)
         {
            // (fh) TODO: qualified name if wildcard
            // attributes are always primitives
            String nodeValue = StructuredDataXPathUtils.findNodeValue(attribute);
            validate(typedXPath, nodeValue);
            complexType.put(
               StructuredDataXPathUtils.getLastXPathPart(typedXPath.getXPath()),
               StructuredDataValueFactory.convertTo(typedXPath.getType(), nodeValue));
         }
      }

      // elements
      for (Element childNode : parentNode.getChildElements())
      {
         TypedXPath typedXPath = getXPath(parentXPath, rootNode, childNode);
         if (typedXPath == null && !isStrict)
         {
            continue;
         }
         String nodeName = childNode.getLocalName();
         String xPath = typedXPath.getXPath();
         if (xPath.isEmpty() && xPathMap.getXPath(xPath) != typedXPath)
         {
            nodeName = "{" + childNode.getNamespaceURI() + "}" + nodeName;
         }
         if (typedXPath.getType() == BigData.NULL || typedXPath.getChildXPaths().size() > 0)
         {
            // complexType or List of complexTypes
            if (typedXPath.isList())
            {
               // List of complexTypes
               if (!complexType.containsKey(nodeName))
               {
                  complexType.put(nodeName, new ArrayList());
               }
               ((List) complexType.get(nodeName)).add(createComplexType(typedXPath, rootNode, childNode));
            }
            else
            {
               // complexType
               complexType.put(nodeName, createComplexType(typedXPath, rootNode, childNode));
            }
         }
         else
         {
            // primitive or list of primitives
            String childNodeValue = StructuredDataXPathUtils.findNodeValue(childNode);
            if (typedXPath.isList())
            {
               // List of primitives
               if ( !complexType.containsKey(nodeName))
               {
                  complexType.put(nodeName, new ArrayList());
               }
               validate(typedXPath, childNodeValue);
               ((List)complexType.get(nodeName)).add(StructuredDataValueFactory.convertTo(typedXPath.getType(), childNodeValue));
            }
            else
            {
               // primitive
               validate(typedXPath, childNodeValue);
               complexType.put(nodeName, StructuredDataValueFactory.convertTo(typedXPath.getType(), childNodeValue));
            }
         }
      }
      return complexType;
   }

   private TypedXPath getXPath(TypedXPath currentXPath, Node rootNode, Node node)
   {
      TypedXPath xPath = null;

      String xxx = StructuredDataXPathUtils.getXPathPart(node);
      if (node instanceof NamedNode)
      {
         for (TypedXPath childXPath : currentXPath.getChildXPaths())
         {
            if (xxx.equals(childXPath.getXPath()) && CompareHelper.areEqual(((NamedNode) node).getNamespaceURI(), childXPath.getXsdElementNs()))
            {
               xPath = childXPath;
               break;
            }
         }
      }

      if (xPath == null)
      {
         String path = StructuredDataXPathUtils.getNodeXPath(node, rootNode);
         if (xPathMap.containsXPath(path))
         {
            xPath = xPathMap.getXPath(path);
         }
         else if (!path.isEmpty() && xPathMap instanceof DataXPathMap)
         {
            xPath = ((DataXPathMap) xPathMap).findXPath(getNodeXPath(node, rootNode));
         }

         // (fh) no xpaths found, giving up
         if (xPath == null && isStrict)
         {
            throw new PublicException(BpmRuntimeError.MDL_UNKNOWN_XPATH.raise(path));
         }
      }

      if (node instanceof Element)
      {
         Attribute xsiTypeAttribute = ((Element) node).getAttribute("type", XMLConstants.NS_XSI);
         if (xsiTypeAttribute != null)
         {
            String xsiTypeValue = xsiTypeAttribute.getValue();
            QName xsiType = ((Element) node).resolve(xsiTypeValue);
            QName currentType = new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName());
            if (!currentType.equals(xsiType))
            {
               // TODO: search the corresponding xpath !!!
               System.out.println(xsiType + " <> " + currentType);
               if (xPathMap instanceof DataXPathMap)
               {
                  xPath = ((DataXPathMap) xPathMap).resolve(xsiType, xPath);
               }
            }
         }
      }
      return xPath;
   }

   private static List<String> getNodeXPath(Node node, Node rootNode)
   {
      if (rootNode == node)
      {
         return Collections.emptyList();
      }

      List<Node> nodes = CollectionUtils.newList();
      nodes.add(node);
      Node parentNode = node.getParent();
      while (parentNode != null)
      {
         if (parentNode.equals(rootNode))
         {
            break;
         }
         nodes.add(parentNode);
         parentNode = parentNode.getParent();
      }

      String ref = ((NamedNode) rootNode).getNamespaceURI();
      List<String> result = CollectionUtils.newList(nodes.size());
      for (int i = nodes.size() - 1; i >= 0; i--)
      {
         Node n = nodes.get(i);
         String part = StructuredDataXPathUtils.getXPathPart(n);
         if (n instanceof NamedNode)
         {
            String ns = ((NamedNode) n).getNamespaceURI();
            if (!ns.isEmpty() && !CompareHelper.areEqual(ref, ns))
            {
               part = '{' + ns + '}' + part;
               ref = ns;
            }
         }
         result.add(part);
      }
      return result;
   }

   TypedXPath getTypedXPath(String path)
   {
      if (xPathMap.containsXPath(path))
      {
         return xPathMap.getXPath(path);
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
      else
      {
         String xPathWithoutIndexes = StructuredDataXPathUtils.getXPathWithoutIndexes(targetXPath);
         TypedXPath typedXPath = xPathMap.getXPath(xPathWithoutIndexes);
         if (object instanceof List)
         {
            // List
            Element node = StructuredDataXPathUtils.createElement(typedXPath, namespaceAware);
            fillNodeList((List)object, node, new Pair(xPathWithoutIndexes, typedXPath), namespaceAware, ignoreUnknownXPaths);
            List<Element> childNodes = node.getChildElements();
            Element [] nodes = new Element[childNodes.size()];
            for (int i = 0; i < nodes.length; i++)
            {
               nodes[i] = childNodes.get(i);
            }
            return nodes;
         }
         else if (object instanceof Map)
         {
            // complexType
            Element node = StructuredDataXPathUtils.createElement(typedXPath, namespaceAware);
            fillComplexType((Map)object, node, xPathWithoutIndexes, namespaceAware, ignoreUnknownXPaths);
            return new Element[]{node};
         }
         else
         {
            // primitive
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
            TypedXPath childXPath1 = composeChildXPath(xPath, (String) o1, ignoreUnknownXPaths).getSecond();
            int orderKey1 = (childXPath1 == null) ? -1 : childXPath1.getOrderKey();
            TypedXPath childXPath2 = composeChildXPath(xPath, (String) o2, ignoreUnknownXPaths).getSecond();
            int orderKey2 = (childXPath2 == null) ? -1 :childXPath2.getOrderKey();
            int result = new Integer(orderKey1).compareTo(new Integer(orderKey2));
            return result == 0 ? ((String) o1).compareTo((String) o2) : result;
         }
      });
      sortedKeys.addAll(complexType.keySet());

      for (Iterator i = sortedKeys.iterator(); i.hasNext(); )
      {
         String key = (String) i.next();
         Object value = complexType.get(key);

         Pair<String, TypedXPath> childXPath = composeChildXPath(xPath, key, ignoreUnknownXPaths);

         if (childXPath.getSecond() != null && childXPath.getSecond().getAnnotations().isPersistent())
         {
            processObject(value, key, childXPath, node, namespaceAware, ignoreUnknownXPaths);
         }
      }
   }

   private Pair<String, TypedXPath> composeChildXPath (String parentXPath, String child, boolean ignoreUnknownXPaths)
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

   private Pair<String, TypedXPath> getXPathIfExists(String path, boolean ignoreUnknownXPaths)
   {
      TypedXPath xPath = null;
      if (xPathMap.containsXPath(path))
      {
         xPath = xPathMap.getXPath(path);
      }
      else if (!path.isEmpty() && xPathMap instanceof DataXPathMap)
      {
         xPath = ((DataXPathMap) xPathMap).findXPath(Arrays.asList(path.split("/")));
      }
      return new Pair(path,
            xPath == null && !ignoreUnknownXPaths && isStrict ? xPathMap.getXPath(path) : xPath);
   }

   private void fillNodeList(List complexTypeList, Element node, Pair<String, TypedXPath> childXPath,
         boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      String key = StructuredDataXPathUtils.getLastXPathPart(childXPath.getFirst());
      for (Iterator i = complexTypeList.iterator(); i.hasNext(); )
      {
         processObject(i.next(), key, childXPath, node, namespaceAware, ignoreUnknownXPaths);
      }

   }

   private void processObject(Object value, String key, Pair<String, TypedXPath> childXPath,
         Element parentNode, boolean namespaceAware, boolean ignoreUnknownXPaths)
   {
      TypedXPath path = childXPath.getSecond();
      if (key.equals(NODE_VALUE_KEY))
      {
         // special case for node value
         // (fh) if this is a complex type with simple content and attributes,
         // fetch the actual value xpath from the children.
         TypedXPath xPath = path.getChildXPath(key);
         if (xPath == null)
         {
            xPath = path;
         }
         String valueString = StructuredDataValueFactory.convertToString(xPath.getType(), xPath.getXsdTypeName(), value);
         parentNode.appendChild(new Text(valueString));
      }
      else if (value instanceof List)
      {
         fillNodeList((List) value, parentNode, childXPath, namespaceAware, ignoreUnknownXPaths);
      }
      else if (value instanceof Map)
      {
         Element childNode = StructuredDataXPathUtils.createElement(path, namespaceAware);
         fillComplexType((Map) value, childNode, childXPath.getFirst(), namespaceAware, ignoreUnknownXPaths);
         parentNode.appendChild(childNode);
      }
      else if ( -1 != key.indexOf('@'))
      {
         // TODO namespace
         // set an attribute
         String valueString = StructuredDataValueFactory.convertToString(path.getType(), path.getXsdTypeName(), value);
         parentNode.addAttribute(StructuredDataXPathUtils.createAttribute(path, valueString));
      }
      else
      {
         // create an element
         Element childNode = StructuredDataXPathUtils.createElement(path, namespaceAware);
         boolean isPlainText = true;
         if (isAnyType(path) && value != null && value.toString().indexOf('<') >= 0)
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
            String valueString = StructuredDataValueFactory.convertToString(path.getType(), path.getXsdTypeName(), value);
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

   private void validate(TypedXPath typedXPath, String stringValue)
   {
      if (validating)
      {
         StructuredDataValueFactory.validate(typedXPath, stringValue);
      }
   }
}
