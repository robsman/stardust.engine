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

import static org.eclipse.stardust.common.CompareHelper.areEqual;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.stardust.engine.core.struct.sxml.Attribute;
import org.eclipse.stardust.engine.core.struct.sxml.Element;


public class SxmlAttributeIterator implements NodeIterator
{
   private final NodePointer parent;

   private final Element node;

   private final QName testedName;

   private Attribute attr;

   private int pos;
   private int childIndex;

   public SxmlAttributeIterator(NodePointer parent, QName attrName)
   {
      this.parent = parent;
      this.node = (Element) parent.getNode();
      // position is 1 based
      this.attr = null;
      this.childIndex = -1;

      this.pos = 0;
      this.testedName = attrName;
   }

   public NodePointer getNodePointer()
   {
      return new SxmlAttributePointer(parent, attr);
   }

   public int getPosition()
   {
      return pos;
   }

   public boolean setPosition(int position)
   {
      while (this.pos < position)
      {
         if ( !findNextChild())
         {
            return false;
         }
      }

      while (position < this.pos)
      {
         if ( !findPreviousChild())
         {
            return false;
         }
      }

      return true;
   }

   private boolean findNextChild()
   {
      ++pos;
      do
      {
         ++childIndex;
         if ((0 <= childIndex) && (childIndex < node.getAttributeCount()))
         {
            this.attr = node.getAttribute(childIndex);
            if (isMatchingNode(attr))
            {
               break;
            }
         }
         else
         {
            this.attr = null;
            break;
         }
      }
      while (true);

      return null != attr;
   }

   private boolean findPreviousChild()
   {
      // TODO
      return false;
   }

   private boolean isMatchingNode(Attribute attr)
   {
      if ("*".equals(testedName.getName()) && (null == testedName.getPrefix()))
      {
         // wildcard without namespace
         return true;
      }
      else
      {
         if ("*".equals(testedName.getName())
               || areEqual(testedName.getName(), attr.getLocalName()))
         {
            String testedNsUri = (null == testedName.getPrefix())
                  ? null
                  : parent.getNamespaceURI(testedName.getPrefix());

            // compare namespaces
            if (areEqual(testedNsUri, attr.getNamespaceURI()))
            {
               return true;
            }
            else if (isEmpty(attr.getNamespaceURI()))
            {
               return ((null == testedName.getPrefix()) && isEmpty(attr.getNamespacePrefix()))
                     || areEqual(testedName.getPrefix(),
                           attr.getNamespacePrefix());
            }
            else
            {
               return false;
            }
         }
      }

      return false;
   }
}
