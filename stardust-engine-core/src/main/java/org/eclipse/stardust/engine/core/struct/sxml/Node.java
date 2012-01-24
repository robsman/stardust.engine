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
package org.eclipse.stardust.engine.core.struct.sxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML node (see concrete sub types)
 *
 * @author robert.sauer
 */
public abstract class Node
{
   private ParentNode parent;

   /**
    * @return the document this node is part of
    */
   public Document getDocument()
   {
      Node node = this;
      while ((null != node) && !(node instanceof Document))
      {
         node = node.getParent();
      }

      return (Document) node;
   }

   /**
    * @return the immediate parent node (element or document) of this node
    */
   public ParentNode getParent()
   {
      return parent;
   }

   /**
    * Sets the immediate parent node of this node (may be null).
    */
   public void setParent(ParentNode parent)
   {
      this.parent = parent;
   }

   /**
    * @return the string-value of this node
    */
   public abstract String getValue();

   /**
    * @return the number of children for this node
    */
   public abstract int getChildCount();

   /**
    * @return the child node at the given position
    */
   public abstract Node getChild(int pos);

   /**
    * Detaches the node from its immediate parent (so it can, e.g., be attached to a
    * different parent afterwards).
    */
   public void detach()
   {
      if (null != getParent())
      {
         if (this instanceof Attribute)
         {
            ((Element) getParent()).removeAttribute((Attribute) this);
         }
         else
         {
            getParent().removeChild(this);
         }
      }
   }

   /**
    * @return a detached copy of this node.
    */
   public abstract Node copy();

   /**
    * @return an XML representation of this node
    */
   public abstract String toXML();

   abstract void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException;
}
