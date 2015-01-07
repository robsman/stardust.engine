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
package org.eclipse.stardust.engine.core.struct.sxml.xpath;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.Parser;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;


public class XPathEvaluator
{
   private static final TreeCompiler XPATH_COMPILER = new TreeCompiler();

   static
   {
      // register SXML object model support
      JXPathContextReferenceImpl.addNodePointerFactory(new SxmlNodePointerFactory());
   }

   private final CompiledExpression compiledXPath;

   private final boolean absoluteLocationPath;

   private final Map<String, String> nsMappings;

   public static XPathEvaluator compileXPath(String xPathExpression)
   {
      return compileXPath(xPathExpression, null);
   }

   public static XPathEvaluator compileXPath(String xPathExpression,
         Map<String, String> nsMappings)
   {
      Expression parsedXPath = parseXPath(xPathExpression);

      boolean isAbsoluteLocationPath = (parsedXPath instanceof LocationPath)
            && ((LocationPath) parsedXPath).isAbsolute();

      return new XPathEvaluator(
            new JXPathCompiledExpression(xPathExpression, parsedXPath),
            isAbsoluteLocationPath, nsMappings);
   }

   public static LocationPath parseLocationPath(String xPathExpression)
   {
      Expression parsedXPath = parseXPath(xPathExpression);

      return (parsedXPath instanceof LocationPath) ? (LocationPath) parsedXPath : null;
   }

   protected static Expression parseXPath(String xPathExpression)
   {
      if (xPathExpression.equals("@"))
      {
         xPathExpression = "text()";
      }
      else if (xPathExpression.endsWith("/@"))
      {
         xPathExpression = xPathExpression.substring(0, xPathExpression.length() - 1) + "text()";
      }
      return (Expression) Parser.parseExpression(xPathExpression, XPATH_COMPILER);
   }

   protected XPathEvaluator(CompiledExpression compiledXPath,
         boolean isAbsoluteLocationPath, Map<String, String> nsContext)
   {
      this.compiledXPath = compiledXPath;
      this.absoluteLocationPath = isAbsoluteLocationPath;
      this.nsMappings = nsContext;
   }

   public List<Node> selectNodes(Document context) throws XPathException
   {
      return doSelectNodes(context);
   }

   public List<Node> selectNodes(Element context) throws XPathException
   {
      // working around https://issues.apache.org/jira/browse/JXPATH-12
      return absoluteLocationPath
            ? doSelectNodes(context.getDocument())
            : doSelectNodes(context);
   }

   private <C extends Node> List<Node> doSelectNodes(C context) throws XPathException
   {
      JXPathContext xPathContext = JXPathContext.newContext(context);
      if (null != nsMappings)
      {
         for (Map.Entry<String, String> nsMapping : nsMappings.entrySet())
         {
            xPathContext.registerNamespace(nsMapping.getKey(), nsMapping.getValue());
         }
      }

      List nodes = newArrayList();
      try
      {
         for (Iterator<Pointer> pointers = compiledXPath.iteratePointers(xPathContext); pointers.hasNext();)
         {
            Pointer pointer = pointers.next();
            nodes.add(pointer.getNode());
         }
      }
      catch (Exception e)
      {
         throw new XPathException("Failed evaluating XPath " + compiledXPath, e);
      }
      return nodes;
   }
}
