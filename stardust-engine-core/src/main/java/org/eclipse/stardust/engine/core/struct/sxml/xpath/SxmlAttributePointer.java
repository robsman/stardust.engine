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

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.stardust.engine.core.struct.sxml.Attribute;


public class SxmlAttributePointer extends NodePointer
{
   private static final long serialVersionUID = 1L;

   private Attribute node;

   public SxmlAttributePointer(NodePointer parent, Attribute node)
   {
      super(parent);

      this.node = node;
   }

   @Override
   public QName getName()
   {
      return new QName(node.getNamespacePrefix(), node.getLocalName());
   }

   @Override
   public String getNamespaceURI()
   {
      return node.getNamespaceURI();
   }

   @Override
   public Object getValue()
   {
      return node.getValue();
   }

   public Object getBaseValue()
   {
      return node;
   }

   public boolean isCollection()
   {
      return false;
   }

   public int getLength()
   {
      return 1;
   }

   public Object getImmediateNode()
   {
      return node;
   }

   public boolean isActual()
   {
      return true;
   }

   public boolean isLeaf()
   {
      return true;
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
