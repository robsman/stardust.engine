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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Map;
import java.util.Stack;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.eclipse.stardust.engine.core.struct.sxml.xpath.XPathEvaluator;

public class NamespaceContextBuilder
{
   public static String toNamespaceQualifiedXPath(String unqualifiedXPath,
         TypedXPath rootXPath, Map<String, String> nsMappings)
   {
      return toNamespaceQualifiedXPath(null, unqualifiedXPath, rootXPath, nsMappings);
   }
   
   static String toNamespaceQualifiedXPath(IXPathMap map, String unqualifiedXPath,
         TypedXPath rootXPath, Map<String, String> nsMappings)
   {
      unqualifiedXPath = extractEmbeddedNamespaces(unqualifiedXPath, nsMappings);
      LocationPath parsedUnqualifiedPath = XPathEvaluator.parseLocationPath(unqualifiedXPath);
      if (null != parsedUnqualifiedPath)
      {
         String qualifiedPath = "";

         NamespaceQualifier nsQualifier = new NamespaceQualifier(map, rootXPath, nsMappings);
         if (parsedUnqualifiedPath.isAbsolute())
         {
            nsQualifier.startAbsoluteLocationPath();
         }
         else
         {
            nsQualifier.startRelativeLocationPath();
         }

         for (Step step : parsedUnqualifiedPath.getSteps())
         {
            if ( !isEmpty(qualifiedPath))
            {
               qualifiedPath += "/";
            }

            switch (step.getAxis())
            {
            case Compiler.AXIS_ANCESTOR:
            case Compiler.AXIS_ANCESTOR_OR_SELF:
            case Compiler.AXIS_ATTRIBUTE:
            case Compiler.AXIS_CHILD:
            case Compiler.AXIS_DESCENDANT:
            case Compiler.AXIS_DESCENDANT_OR_SELF:
            case Compiler.AXIS_SELF:
               if (step.getNodeTest() instanceof NodeNameTest)
               {
                  NodeNameTest nameTest = (NodeNameTest) step.getNodeTest();
                  QName nodeName = nameTest.getNodeName();
                  if ( !nameTest.isWildcard() && isEmpty(nameTest.getNamespaceURI()))
                  {
                     QName qualifiedNodeName = nsQualifier.startNameStep(step.getAxis(),
                           nodeName.getPrefix(), nodeName.getName());

                     StringBuilder patchedStep = new StringBuilder(step.toString());
                     int idxNodeName = patchedStep.indexOf(nameTest.toString());
                     if (-1 != idxNodeName)
                     {
                        patchedStep.replace(
                              idxNodeName, //
                              idxNodeName + nodeName.toString().length(),
                              qualifiedNodeName.toString());
                     }

                     qualifiedPath += patchedStep.toString();
                  }
                  else
                  {
                     qualifiedPath += step.toString();
                  }
               }
               else
               {
                  qualifiedPath += step.toString();
               }
               break;

            case Compiler.AXIS_PARENT:
               // TODO pop XPath context
               qualifiedPath += step.toString();
               break;

            default:
               qualifiedPath += step.toString();
            }
         }

         if (parsedUnqualifiedPath.isAbsolute())
         {
            nsQualifier.endAbsoluteLocationPath();
         }
         else
         {
            nsQualifier.endRelativeLocationPath();
         }

         if (parsedUnqualifiedPath.isAbsolute())
         {
            qualifiedPath = "/" + qualifiedPath;
         }

         return qualifiedPath;
      }
      else
      {
         return unqualifiedXPath;
      }
   }

   private static String extractEmbeddedNamespaces(String unqualifiedXPath, Map<String, String> nsMappings)
   {
      boolean hasMore;
      unqualifiedXPath = unqualifiedXPath.trim();
      do
      {
         hasMore = false;
         int ix = unqualifiedXPath.indexOf(' ');
         if (ix >= 0)
         {
            String def = unqualifiedXPath.substring(0,  ix);
            unqualifiedXPath = unqualifiedXPath.substring(ix + 1).trim();
            ix = def.indexOf('=');
            String prefix = def.substring(0, ix).trim();
            String namespace = def.substring(ix + 1).trim();
            if (!prefix.isEmpty() && !namespace.isEmpty())
            {
               nsMappings.put(prefix, namespace);
            }
            hasMore = true;
         }
         else
         {
            ix = unqualifiedXPath.indexOf('{');
            if (ix >= 0)
            {
               int iy = unqualifiedXPath.indexOf('}', ix);
               if (iy >= 0)
               {
                  String namespace = unqualifiedXPath.substring(ix + 1, iy);
                  if (namespace.isEmpty())
                  {
                     break;
                  }
                  String prefix = null;
                  if (nsMappings.containsValue(namespace))
                  {
                     prefix = extractPrefix(nsMappings, namespace);
                  }
                  else
                  {
                     prefix = "ns" + Integer.toString(nsMappings.size());
                     nsMappings.put(prefix, namespace);
                  }
                  unqualifiedXPath = unqualifiedXPath.replace(unqualifiedXPath.substring(ix, iy + 1), prefix + ':');
                  hasMore = true;
               }
            }
         }
      } while (hasMore);
      return unqualifiedXPath;
   }

   private static String extractPrefix(Map<String, String> nsMappings, String namespace)
   {
      for (Map.Entry<String, String> nsMapping : nsMappings.entrySet())
      {
         if (namespace.equals(nsMapping.getValue()))
         {
            return nsMapping.getKey();
         }
      }
      return null;
   }

   /**
    * Tries to qualify all XPath steps with a prefix, creates a NamespaceContext that can
    * be used during the XPath evaluation
    */
   private static class NamespaceQualifier
   {
      private TypedXPath rootXPath;

      private Stack parentXPathStack;

      /**
       * Maps prefixes to namespace URIs
       */
      private Map<String, String> nsMappings;

      private IXPathMap map;

      public NamespaceQualifier(IXPathMap map, TypedXPath rootXPath, Map<String, String> nsMappings)
      {
         this.map = map;
         this.rootXPath = rootXPath;
         parentXPathStack = new Stack();
         this.nsMappings = nsMappings;
      }

      public void startAbsoluteLocationPath()
      {
         parentXPathStack.push(this.rootXPath);
      }

      public void endAbsoluteLocationPath()
      {
         parentXPathStack.pop();
      }

      public void startRelativeLocationPath()
      {
         if (parentXPathStack.isEmpty())
         {
            parentXPathStack.push(rootXPath);
         }
         else
         {
            parentXPathStack.push(parentXPathStack.peek());
         }
      }

      public void endRelativeLocationPath()
      {
         parentXPathStack.pop();
      }

      public QName startNameStep(int axis, String prefix, String localName)
      {
         TypedXPath parentXPath = (TypedXPath) parentXPathStack.pop();
         TypedXPath xPath = parentXPath.getChildXPath(localName);
         if (xPath == null)
         {
            // child not found, try to guess since this can be a relative path
            xPath = findBelow(parentXPath, localName);
            if (xPath == null)
            {
               // TODO consider axis when testing for attributes
               // may be it is an attribute and here it is not possible to tell elements
               // from attributes!
               // of course there is a danger that the element has a child element and
               // attribute
               // with equal names, but we need a xPath here
               xPath = findBelow(parentXPath, "@" + localName);
               if (xPath == null)
               {
                  throw new RuntimeException("Unknown child XPath '" + localName
                        + "' of parent XPath '" + parentXPath + "'");
               }
            }
         }
         String namespace = xPath.getXsdElementNs();
         if (isEmpty(prefix) && !isEmpty(namespace))
         {
            if (nsMappings.containsValue(namespace))
            {
               prefix = extractPrefix(nsMappings, namespace);
            }
            else
            {
               prefix = "ns" + Integer.toString(nsMappings.size());
               nsMappings.put(prefix, namespace);
            }
         }

         this.parentXPathStack.push(xPath);

         return new QName(prefix, localName);
      }

      private TypedXPath findBelow(TypedXPath parentXPath, String step)
      {
         for (int i = 0; i < parentXPath.getChildXPaths().size(); i++ )
         {
            TypedXPath p = (TypedXPath) parentXPath.getChildXPaths().get(i);
            if (StructuredDataXPathUtils.getLastXPathPart(p.getXPath()).equals(step))
            {
               return p;
            }
            TypedXPath childXPath = findBelow(p, step);
            if (childXPath != null)
            {
               return childXPath;
            }
         }
         return parentXPath.hasWildcards() && map instanceof DataXPathMap ? ((DataXPathMap) map).getRootXPath(step) : null;
      }
   }
}
