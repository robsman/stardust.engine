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
   
   public static String toNamespaceQualifiedXPath(StructuredDataConverter converter, String unqualifiedXPath,
         TypedXPath rootXPath, Map<String, String> nsMappings)
   {
      LocationPath parsedUnqualifiedPath = XPathEvaluator.parseLocationPath(unqualifiedXPath);
      if (null != parsedUnqualifiedPath)
      {
         String qualifiedPath = "";

         NamespaceQualifier nsQualifier = new NamespaceQualifier(converter, rootXPath, nsMappings);
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

      private StructuredDataConverter converter;

      public NamespaceQualifier(StructuredDataConverter converter, TypedXPath rootXPath, Map<String, String> nsMappings)
      {
         this.converter = converter;
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
         if (isEmpty(prefix) && !isEmpty(xPath.getXsdElementNs()))
         {
            if (nsMappings.containsValue(xPath.getXsdElementNs()))
            {
               for (Map.Entry<String, String> nsMapping : nsMappings.entrySet())
               {
                  if (xPath.getXsdElementNs().equals(nsMapping.getValue()))
                  {
                     prefix = nsMapping.getKey();
                     break;
                  }
               }
            }
            else
            {
               prefix = "ns" + Integer.toString(nsMappings.size());
               nsMappings.put(prefix, xPath.getXsdElementNs());
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
         // (fh) wildcard ?
         return converter == null ? null : converter.getRootXPath(step);
      }
   }
}
