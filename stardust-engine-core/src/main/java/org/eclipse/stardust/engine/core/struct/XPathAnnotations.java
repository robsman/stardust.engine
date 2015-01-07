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

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.MultiIterator;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations.XPathAnnotation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XPathAnnotations implements Serializable, Iterable<XPathAnnotation>
{
   private static final long serialVersionUID = 2L;
   
   public static final String IPP_ANNOTATIONS_NAMESPACE = "http://www.carnot.ag/workflowmodel/3.1/struct";

   public final static XPathAnnotations DEFAULT_ANNOTATIONS = new XPathAnnotations(Collections.EMPTY_LIST, Collections.EMPTY_MAP); 
   
   private final static String GLOBAL_SCOPE = "global";
   private final static String STORAGE_SCOPE = "storage";
   
   private final static String INDEXED_ANNOTATION = "indexed";
   private final static String PERSISTENT_ANNOTATION = "persistent";
   
   public final static String DEFAULTVALUE_ANNOTATION = "defaultValue";   
   
   private final static String[] INDEXED_PATH = {STORAGE_SCOPE, INDEXED_ANNOTATION};
   private final static String[] PERSISTENT_PATH = {STORAGE_SCOPE, PERSISTENT_ANNOTATION};

   private boolean indexed;
   private boolean persistent;
   
   /**
    * List of original appInfo dom elements.
    */
   private final List<Element> appInfos;
   
   public XPathAnnotations (List<Element> appInfos, Map /* <String,Map<String, String>> */ annotationsByScope)
   {
      this.appInfos = appInfos;

      // cache default annotations
      String indexedValue = getElementValue(IPP_ANNOTATIONS_NAMESPACE, INDEXED_PATH);
      String persistentValue = getElementValue(IPP_ANNOTATIONS_NAMESPACE, PERSISTENT_PATH);
      indexed = Boolean.valueOf(indexedValue == null
            ? getValueFromScope(annotationsByScope, GLOBAL_SCOPE, INDEXED_ANNOTATION, "true")
            : indexedValue).booleanValue();
      persistent = Boolean.valueOf(persistentValue == null
            ? (String) getValueFromScope(annotationsByScope, GLOBAL_SCOPE, PERSISTENT_ANNOTATION, "true")
            : persistentValue).booleanValue();
   }
   
   public String getElementValue(String namespaceURI, String[] path)
   {
      for (Iterator<Element> i = appInfos.iterator(); i.hasNext();)
      {
         Element appInfoElement = (Element) i.next();
         Element valueElement = getElement(namespaceURI, path, appInfoElement, 0);
         if (valueElement != null)
         {
            return getValue(valueElement);
         }
      }
      return null;
   }

   private Element getElement(String namespaceURI, String[] path, Element startingElement,
         int pathIndex)
   {
      if (pathIndex < path.length)
      {
         NodeList list = namespaceURI == null
               ? startingElement.getElementsByTagName(path[pathIndex])
               : startingElement.getElementsByTagNameNS(namespaceURI, path[pathIndex]);
         for (int i = 0; i < list.getLength(); i++)
         {
            Element element = getElement(namespaceURI, path, (Element) list.item(i), pathIndex + 1);
            if (element != null)
            {
               return element;
            }
         }
         return null;
      }
      return startingElement;
   }

   public static String getValue(Element element)
   {
      StringBuilder sb = new StringBuilder();
      Node child = element.getFirstChild();
      while (child != null)
      {
         if (child.getNodeType() == Node.TEXT_NODE)
         {
            String value = ((Text) child).getNodeValue();
            if (value.length() > 0)
            {
               int start = 0;
               int end = value.length();
               while (start < end && (value.charAt(start) == '\r' || value.charAt(start) == '\n'))
               {
                  start++;
               }
               while (start < end && (value.charAt(end - 1) == '\r' || value.charAt(end - 1) == '\n'))
               {
                  end--;
               }
               if (start < end)
               {
                  sb.append(value, start, end);
               }
            }
         }
         child = child.getNextSibling();
      }
      return sb.length() == 0 ? null : sb.toString();
   }

   /**
    * This method only exists to support old scope-style annotations
    */
   private String getValueFromScope(Map<String,Map<String, String>>annotationsByScope, String scope, String annotationName, String defaultValue)
   {
      Map<String, String> annotationsForScope = (Map) annotationsByScope.get(scope);
      if (annotationsForScope == null || !annotationsForScope.containsKey(annotationName))
      {
         return defaultValue; 
      }
      return annotationsForScope.get(annotationName);
   }
   
   public boolean isIndexed()
   {
      return indexed;
   }
   
   public boolean isPersistent()
   {
      return persistent;
   }
   

   public Iterator<XPathAnnotation> iterator()
   {
      return new MultiIterator<XPathAnnotation>(
            new TransformingIterator<Element, Iterable<XPathAnnotation>>(appInfos.iterator(),
                  new Functor<Element, Iterable<XPathAnnotation>>()
                  {
                     public Iterable<XPathAnnotation> execute(Element source)
                     {
                        return new XPathAnnotation(source, null, true);
                     }
                  }));
   }
   
   private static class AnnotationIterator implements Iterator<XPathAnnotation>
   {
      private Element current;
      private XPathAnnotation parent;

      private AnnotationIterator(Element element, XPathAnnotation parent)
      {
         this.parent = parent;
         Node node = element.getFirstChild();
         while (node != null && !(node instanceof Element))
         {
            node = node.getNextSibling();
         }
         current = (Element) node;
      }

      public boolean hasNext()
      {
         return current != null;
      }

      public XPathAnnotation next()
      {
         if (current == null)
         {
            throw new NoSuchElementException();
         }
         XPathAnnotation next = new XPathAnnotation(current, parent, false);
         Node node = current.getNextSibling();
         while (node != null && !(node instanceof Element))
         {
            node = node.getNextSibling();
         }
         current = (Element) node;
         return next;
      }

      public void remove()
      {
         throw new UnsupportedOperationException("remove");
      }
   }

   public static class XPathAnnotation implements Iterable<XPathAnnotation>
   {
      private static final String[] NONE = new String[0];
      
      private Element element;
      private String[] path;
      
      private XPathAnnotation(Element element, XPathAnnotation parent, boolean wrapper)
      {
         this.element = element;
         if (!wrapper && parent != null)
         {
            path = new String[parent.path.length + 1];
            System.arraycopy(parent.path, 0, path, 0, parent.path.length);
            path[parent.path.length] = getName();
         }
         else if (!wrapper)
         {
            path = new String[] {getName()};
         }
         else
         {
            path = NONE;
         }
      }

      public String[] getPath()
      {
         return path;
      }

      public String getName()
      {
         return element.getLocalName();
      }
      
      public String getNamespace()
      {
         return element.getNamespaceURI();
      }
      
      public String getValue()
      {
         return XPathAnnotations.getValue(element);
      }

      public Iterator<XPathAnnotation> iterator()
      {
         return new AnnotationIterator(element, this);
      }
   }
}