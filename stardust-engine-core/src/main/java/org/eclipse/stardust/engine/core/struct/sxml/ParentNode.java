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

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.stardust.common.error.PublicException;


/**
 * A node capable of containing child nodes.
 *
 * @author robert.sauer
 */
public abstract class ParentNode extends Node
{
   private List<Node> children = emptyList();

   /**
    * @return <code>true</code> if the provided node is a valid child of this parent node
    */
   protected abstract boolean isValidChild(Node child);

   @Override
   public int getChildCount()
   {
      return children.size();
   }

   @Override
   public Node getChild(int pos)
   {
      return children.get(pos);
   }
   
   public List<Node> getChildren()
   {
      return unmodifiableList(children);
   }

   /**
    * @return the position of the given node within the list of children
    */
   public int indexOf(Node child)
   {
      return children.indexOf(child);
   }

   /**
    * Appends the given node (must not be null) to the list of children.
    */
   public void appendChild(Node child) throws NullPointerException, PublicException
   {
      insertChild(child, getChildCount());
   }

   /**
    * Adds the given node (must not be null) to the list of children, at the specified position.
    */
   public void insertChild(Node child, int pos) throws NullPointerException, PublicException
   {
      if (null == child)
      {
         throw new NullPointerException("Child node must not be null.");
      }
      if (null != child.getParent())
      {
         throw new PublicException("Node must be detached.");
      }

      if ( !isValidChild(child))
      {
         throw new PublicException("Invalid child element: " + child.getClass());
      }

      if (children.isEmpty())
      {
         this.children = newArrayList();
      }
      children.add(pos, child);
      child.setParent(this);
   }

   /**
    * Removes the given node from the list of children.
    *
    * @return the removed node (will be detached)
    */
   public Node removeChild(Node child) throws PublicException
   {
      int pos = indexOf(child);
      if ( -1 == pos)
      {
         throw new PublicException("No such child: " + child);
      }
      else
      {
         children.remove(pos);
         child.setParent(null);
      }

      return child;
   }

   void clearChildren()
   {
      children.clear();
   }

   @Override
   void toXML(XMLStreamWriter xmlWriter) throws XMLStreamException
   {
      for (int i = 0; i < getChildCount(); ++i)
      {
         Node child = getChild(i);
         child.toXML(xmlWriter);
      }
   }
}
