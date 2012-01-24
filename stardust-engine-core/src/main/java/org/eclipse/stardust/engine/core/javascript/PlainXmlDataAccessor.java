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
package org.eclipse.stardust.engine.core.javascript;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.utils.xml.XPathUtils;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.mozilla.javascript.Delegator;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author sauer
 * @version $Revision: 25852 $
 */
public class PlainXmlDataAccessor extends Delegator
{
   private static final String XMLNS = "xmlns:";

   private final Document document;

   public PlainXmlDataAccessor(String xmlString)
   {
      this.document = XmlUtils.parseString(xmlString);
   }

   public boolean has(String name, Scriptable start)
   {
      // always return true, to save performance
      return true;
   }

   public Object get(String name, Scriptable start)
   {
      try
      {
         String path = name;
         Map<String, String> ns = newHashMap();
         int ix = path.indexOf(' ');
         while (ix > 0)
         {
            String mapping = path.substring(0, ix);
            if (!mapping.startsWith(XMLNS))
            {
               break;
            }
            int eqx = mapping.indexOf('=');
            String prefix = mapping.substring(XMLNS.length(), eqx);
            String namespace = URLDecoder.decode(mapping.substring(eqx + 1), "UTF-8");
            ns.put(prefix, namespace);
            path = path.substring(ix + 1);
            ix = path.indexOf(' ');
         }
         Object result = XPathUtils.evaluateXPath(document.getDocumentElement(), path, ns);
         if (result instanceof List)
         {
            // extract String values from the nodes
            return wrap((List)result);
         }
         else
         {
            // assume it is Double, Boolean or String, as stated in the XPathUtils javadoc
            return result;
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private Object wrap(List nodeList)
   {
      if (nodeList.size() == 1)
      {
         // return the value without wrapper list if only
         // one node is returned
         return ((Node) nodeList.get(0)).getTextContent();
      }

      List /*<String>*/ result = CollectionUtils.newList();
      for (int i=0; i<nodeList.size(); i++)
      {
         result.add(nodeList.get(i));
      }
      return result;
   }

   public void put(String name, Scriptable start, Object value)
   {
      // do nothing, PlainXmlData values can not (yet) be changed by Javascript
   }
}
