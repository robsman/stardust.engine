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

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.stardust.engine.core.struct.sxml.NamedNode;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.ParentNode;


public class SxmlNodePointer extends NodePointer
{
   private static final long serialVersionUID = 1L;

   private Node node;

   public SxmlNodePointer(Node node, Locale locale)
   {
      super(null, locale);

      this.node = node;
   }

   public SxmlNodePointer(NodePointer parent, Node node)
   {
      super(parent);

      this.node = node;
   }

   @Override
   public QName getName()
   {
      if (node instanceof NamedNode)
      {
         NamedNode element = (NamedNode) node;
         return new QName(element.getNamespacePrefix(), element.getLocalName());
      }
      else
      {
         return null;
      }
   }

   @Override
   public String getNamespaceURI()
   {
      if (node instanceof NamedNode)
      {
         return ((NamedNode) node).getNamespaceURI();
      }
      else
      {
         return super.getNamespaceURI();
      }
   }

   @Override
   public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith)
   {
      return (node instanceof ParentNode) //
            ? new SxmlNodeIterator(this, test, reverse, startWith)
            : super.childIterator(test, reverse, startWith);
   }

   @Override
   public NodeIterator attributeIterator(QName qname)
   {
      return new SxmlAttributeIterator(this, qname);
   }

   @Override
   public Object getBaseValue()
   {
      return node;
   }

   @Override
   public Object getImmediateNode()
   {
      return node;
   }

   @Override
   public boolean isActual()
   {
      return true;
   }

   @Override
   public boolean isCollection()
   {
      return false;
   }

   @Override
   public int getLength()
   {
      return 1;
   }

   @Override
   public boolean isLeaf()
   {
      return !(node instanceof ParentNode) || (0 == ((ParentNode) node).getChildCount());
   }

   @Override
   public void setValue(Object value)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2)
   {
      // TODO Auto-generated method stub
      return 0;
   }

}
