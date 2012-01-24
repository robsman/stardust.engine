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
package org.eclipse.stardust.common.utils.xml;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.eclipse.stardust.common.StringUtils;
import org.w3c.dom.Node;


public class XPathUtils
{
   /**
    * Evaluates an XPath expression in context of the given DOM node.
    *
    * @param context the context node
    * @param xPathExpression the XPath expression
    * @return either a {@link List} of nodes, or a direct primitive value ({@link String},
    *         {@link Double} or {@link Boolean} if the expression evaluates to a plain
    *         value)
    */
   public static Object evaluateXPath(Node context, String xPathExpression)
   {
      return evaluateXPath(context, xPathExpression, null);
   }

   /**
    * Evaluates an XPath expression in context of the given DOM node.
    *
    * @param context the context node
    * @param xPathExpression the XPath expression
    * @param nsMappings the prefix -> nsUri mappings to use when resolving namespace prefixes
    * @return either a {@link List} of nodes, or a direct primitive value ({@link String},
    *         {@link Double} or {@link Boolean} if the expression evaluates to a plain
    *         value)
    */
   public static Object evaluateXPath(Node context, String xPathExpression, Map<String, String> nsMappings)
   {
      // working around https://issues.apache.org/jira/browse/JXPATH-12
      boolean absoluteLocatiponPath = !StringUtils.isEmpty(xPathExpression)
            && xPathExpression.startsWith("/");
      JXPathContext xPathContext = JXPathContext.newContext(absoluteLocatiponPath
            ? context.getOwnerDocument()
            : context);
      if ( !isEmpty(nsMappings))
      {
         for (Map.Entry<String, String> nsMapping : nsMappings.entrySet())
         {
            xPathContext.registerNamespace(nsMapping.getKey(), nsMapping.getValue());
         }
      }
      Iterator<Pointer> pointers = xPathContext.iteratePointers(xPathExpression);
      if ((null == pointers) || !pointers.hasNext())
      {
         return emptyList();
      }

      boolean first = true;
      List<Object> result = null;
      while (pointers.hasNext())
      {
         Pointer pointer = pointers.next();

         if (first)
         {
            if ( !pointers.hasNext())
            {
               // single element result list, potentially unwrap primitive results
               if ((pointer.getNode() instanceof String)
                     || (pointer.getNode() instanceof Double)
                     || (pointer.getNode() instanceof Boolean))
               {
                  return pointer.getNode();
               }
            }
            result = newArrayList();
            first = false;
         }

         result.add(pointer.getNode());
      }
      return result;
   }
}
