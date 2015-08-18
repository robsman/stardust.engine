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

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.stardust.engine.core.struct.sxml.NamedNode;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.ParentNode;
import org.eclipse.stardust.engine.core.struct.sxml.Text;

public class SxmlNodeIterator implements NodeIterator
{
   private final NodePointer parent;

   private final ParentNode node;

   private final NodeTest test;

   private final boolean reverse;

   private Node child;

   private int pos;
   private int childIndex;

   public SxmlNodeIterator(NodePointer parent, NodeTest test, boolean reverse,
         NodePointer startWith)
   {
      this.parent = parent;
      this.node = (ParentNode) parent.getNode();
      // position is 1 based
      if (null != startWith)
      {
         this.child = (Node) startWith.getNode();
         this.childIndex = node.indexOf(child);
      }
      else
      {
         this.child = null;
         this.childIndex = -1;
      }
      this.pos = 0;
      this.test = test;
      this.reverse = reverse;
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

   public NodePointer getNodePointer()
   {
      if (0 == pos)
      {
         // initialize lazily
         setPosition(1);
      }

      return (null != child) ? new SxmlNodePointer(parent, child) : null;
   }

   private boolean findNextChild()
   {
      ++pos;
      if ( !reverse)
      {
         do
         {
            ++childIndex;
            if ((0 <= childIndex) && (childIndex < node.getChildCount()))
            {
               this.child = node.getChild(childIndex);
               if (isMatchingNode(child, test))
               {
                  break;
               }
            }
            else
            {
               this.child = null;
               break;
            }
         }
         while (true);
      }
      else
      {
         // TODO
      }

      return null != child;
   }

   private boolean findPreviousChild()
   {
      // TODO
      return false;
   }

   private boolean isMatchingNode(Node node, NodeTest test)
   {
      if (test instanceof NodeNameTest)
      {
         NodeNameTest nameTest = (NodeNameTest) test;

         QName testedName = nameTest.getNodeName();
         if (nameTest.isWildcard() && (null == testedName.getPrefix()))
         {
            // wildcard without namespace
            return true;
         }
         else if (node instanceof NamedNode)
         {
            NamedNode namedNode = (NamedNode) node;

            if (nameTest.isWildcard()
                  || areEqual(testedName.getName(), namedNode.getLocalName()))
            {
               // compare namespaces
               if (areEqual(nameTest.getNamespaceURI(), namedNode.getNamespaceURI()))
               {
                  return true;
               }
               else if (isEmpty(namedNode.getNamespaceURI()))
               {
                  return ((null == testedName.getPrefix()) && isEmpty(namedNode.getNamespacePrefix()))
                        || areEqual(testedName.getPrefix(),
                              namedNode.getNamespacePrefix());
               }
               else
               {
                  return false;
               }
            }
         }
      }
      else if (test instanceof NodeTypeTest)
      {
         switch (((NodeTypeTest) test).getNodeType())
         {
         case Compiler.NODE_TYPE_NODE :
            return true;
         case Compiler.NODE_TYPE_TEXT :
            return node instanceof Text;
         default:
            return false;
         }
      }
      else if (test instanceof ProcessingInstructionTest)
      {
         // TODO
      }

      return false;
   }
}
