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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.common.Key;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Provides some additional methods for writing data to an DOM-Node.
 */
public class NodeWriter
{
   private Element node;

   public NodeWriter(Element node)
   {
      this.node = node;
   }

   public Node appendChild(Node childNode)
   {
      return node.appendChild(childNode);
   }

   public void writeAttribute(String name, int value)
   {
      writeAttribute(name, Integer.toString(value));
   }

   public void writeAttribute(String name, long value)
   {
      writeAttribute(name, Long.toString(value));
   }

   public void writeAttribute(String name, double value)
   {
      writeAttribute(name, Double.toString(value));
   }

   public void writeAttribute(String name, Money value)
   {
      if (value != null)
      {
         writeAttribute(name, value.getValue().toString());
      }
   }

   public void writeAttribute(String name, Key value)
   {
      if (value != null)
      {
         writeAttribute(name, value.toString());
      }
   }

   public void writeAttribute(String name, StringKey value)
   {
      if (value != null)
      {
         writeAttribute(name, value.getId());
      }
   }

   public void writeAttribute(String name, Date value)
   {
      if (value != null)
      {
         writeAttribute(name, value.toString());
      }
   }

   public void writeAttribute(String name, String value)
   {
      if (!StringUtils.isEmpty(value))
      {
         node.setAttribute(name, value);
      }
   }

   public void writeAttribute(String name, boolean value)
   {
      writeAttribute(name, String.valueOf(value));
   }

   public void writeAttribute(String name, Iterator value)
   {
      StringBuffer result = new StringBuffer("");
      while (value.hasNext())
      {
         if (result.length() != 0)
         {
            result.append(", ");
         }
         Object item = value.next();
         if (item instanceof IdentifiableElement)
         {
            result.append(((IdentifiableElement) item).getId());
         }
         else if (item instanceof StringKey)
         {
            result.append(((StringKey) item).getId());
         }
         else
         {
            result.append(item);
         }
      }
      writeAttribute(name, result.toString());
   }

   /**
    * Appends a new Childnode (element type) with the submitted value.
    * If isMultiLineText is true than a CDATA section is used to store
    * the value.
    */
   public void appendChildElement(String elementName, String elementValue)
   {
      if (!StringUtils.isEmpty(elementValue))
      {
         Node child = node.getOwnerDocument().createElementNS(
               XMLConstants.NS_CARNOT_WORKFLOWMODEL_31, elementName);

         node.appendChild(child);

         Node cdata = child.getOwnerDocument().createCDATASection(elementValue);
         child.appendChild(cdata);
      }
   }

   public void writeReference(String name, ModelElement element)
   {
      if (element == null)
      {
         // @todo (france, ub): warn?!
         return;
      }
      if (element instanceof IdentifiableElement)
      {
         writeAttribute(name, ((IdentifiableElement) element).getId());
      }
      else
      {
         writeAttribute(name, element.getElementOID());
      }
   }
}