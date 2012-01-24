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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.EnumerationIteratorWrapper;
import org.eclipse.stardust.common.OneElementIterator;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * The node maintains a vector of objects retrieved by the sets of association
 * traversal methods for this node. This vector is populated immediately, when
 * the node is created. This may be changed in upcoming releases.<p>
 * Furthermore, it maintains a vector of tree nodes. These are created, when
 * a fan out of the node is requested.<p>
 * If the load increment of the tree node is not reached,
 * all associated objects are loaded at once. If more objects than the load
 * increment can be retrieved, the iteration is interrupted.
 */
public class GenericTreeNode implements MutableTreeNode
{
   public static final Logger trace = LogManager.getLogger(GenericTreeNode.class);

   protected GenericTree tree;
   private MutableTreeNode parent;
   /** User object associated with this node. */
   private Object object;
   public String label;
   public ImageIcon icon;
   protected Vector objects;
   protected Vector nodes;
   private Method[] traverseMethods;
   private int traverseMethodIndex;
   private java.util.Iterator currentIterator;
   private boolean sorted = true;
   private String displayValue = null;
   private int leafCount = 0;

   /**
    * Set label and icon only once. Refreshing must be requested explicitely.<p>
    * Root node constructor.
    */
   public GenericTreeNode(GenericTree tree, Object object, boolean isSorted)
   {
      this(tree, null, object, isSorted);
   }

   /**
    *
    */
   public GenericTreeNode(GenericTree tree, MutableTreeNode parent, Object object, boolean isSorted)
   {
      this.tree = tree;
      this.parent = parent;
      this.object = object;
      this.sorted = tree.isSorted();
      refreshLabelAndIcon();

      // The tree itself as the object is a cookie for the fetch node

      if (object.equals(tree))
      {
         return;
      }

      // Initialize traverse methods

      this.traverseMethods = tree.traverseMethods[tree.getClassIndex(object.getClass())];

      // Collect children objects

      objects = new Vector();

      loadObjects();
   }

   /**
    * (Re)loads all objects associated with this user object.
    */
   public void loadObjects()
   {
      traverseMethodIndex = 0;
      currentIterator = null;
      int traversableCount = 0;
      leafCount = 0;
      objects.clear();
      boolean navigated = false;
      // Find next available iterator

      nextIterator();

      // Populate objects
      while (hasMoreElements() && objects.size() <
            tree.getLoadIncrementForType(object.getClass()) + traversableCount)
      {
         Object element = nextElement();
         if (isTraversable(element))
         {
            traversableCount++;
         }
         else
         {
            leafCount++;
            if (!navigated)
            {
               if (tree.isRotating() && isTraversable(object) && hasPreviousChunk())
               {
                  objects.add(new TreeNavigatorUp());
                  traversableCount++;
               }
               navigated = true;
            }
         }
         objects.add(element);
      }

      if (leafCount == 0 && hasPreviousChunk() && tree.isRotating() && isTraversable(object))
      {
         objects.add(new TreeNavigatorUp());
      }

      if (sorted)
      {
         Object[] _array = objects.toArray();
         Arrays.sort(_array, new Comparator()
         {
            public int compare(Object o1, Object o2)
            {
               return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
            }

         }
         );
         objects.clear();
         for (int i = 0; i < _array.length; i++)
         {
            objects.add(_array[i]);
         }
      }

      if (isTraversable(object))
      {
         if (hasNextChunk())
         {
            if (tree.isRotating())
            {
               objects.add(new TreeNavigatorDown());
            }
            else
            {
               objects.add(tree);
            }
         }
      }

      // Clear the children vector

      getChildrenVector().clear();

      // Set the size of the children node vector at to the size of the initially retrieved objects

      getChildrenVector().setSize(objects.size());
   }

   private boolean hasPreviousChunk()
   {
      if (tree.getHasPreviousMethod() != null)
      {
         try
         {
            Method m = object.getClass().getMethod(tree.getHasPreviousMethod());
            Boolean b = (Boolean) m.invoke(object);
            return b.booleanValue();
         }
         catch (Exception e)
         {
            //ignore
         }
      }
      return false;
   }

   private boolean hasNextChunk()
   {
      if (tree.getHasNextMethod() != null)
      {
         try
         {
            Method m = object.getClass().getMethod(tree.getHasNextMethod());
            Boolean b = (Boolean) m.invoke(object);
            return b.booleanValue();
         }
         catch (Exception e)
         {
            //ignore
         }
      }
      return false;
   }

   /**
    * Fetches the next <code>LOAD_INCREMENT</code> objects for the tree node
    * children.
    */
   public void fetchNext()
   {
      int oldSize = objects.size();

      objects.remove(oldSize - 1);
      try
      {
         getChildrenVector().remove(oldSize - 1);
      }
      catch (Exception e)
      {
         //ignore
      }

      Object newObject;

      while (hasMoreElements() && objects.size() < oldSize + tree.getLoadIncrement())
      {
         newObject = nextElement();

         objects.add(newObject);
         getChildrenVector().add(new GenericTreeNode(tree, this, newObject, sorted));
      }

      // If still more objects are available, add the dummy node for the next action

      if (hasMoreElements())
      {
         objects.add(tree);
         getChildrenVector().add(new GenericTreeNode(tree, this, tree, sorted));
      }
   }

   /**
    * @return Label of the node.
    */
   public String toString()
   {
      return getLabel();
   }

   /**
    * @return Label of the node.
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * Sets a node's label dynamically.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Refreshes the label and the icon from the model data.
    */
   private void refreshLabelAndIcon()
   {
      if (object.equals(tree))
      {
         label = "More ...";
         icon = null;

         return;
      }

      int index = ((GenericTree) tree).getClassIndex(object.getClass());
      Method labelMethod = ((GenericTree) tree).labelMethods[index];
      Method iconMethod = ((GenericTree) tree).iconMethods[index];

      // Set label

      if (labelMethod != null)
      {

         String oldLabel = label;
         try
         {
            label = (String) labelMethod.invoke(object);
            if (label == null)
            {
               label = "NULL";
            }
            if (!label.equals(oldLabel) && oldLabel != null)
            {
               tree.reload();
            }
         }
         catch (IllegalAccessException e)
         {

         }
         catch (IllegalArgumentException e)
         {

         }
         catch (InvocationTargetException e)
         {

         }

      }
      else
      {
         label = object.toString();
      }

      // Set icon
      icon = null;
      if (tree.getIconProvider() != null)
      {
         icon = tree.getIconProvider().getIcon(object);
      }
      if (icon == null && iconMethod != null)
      {
         try
         {
            icon = (ImageIcon) iconMethod.invoke(object);
         }
         catch (Exception x)
         {
            throw new InternalException(x);
         }
      }
   }

   /**
    * Returns the children of the receiver as an Enumeration.
    */
   public Enumeration children()
   {
      return new GenericTreeNodeEnumeration(this);
   }

   /**
    * Returns true if the receiver allows children.
    */
   public boolean getAllowsChildren()
   {
      if (traverseMethods == null || traverseMethods.length == 0)
      {
         return false;
      }

      try
      {
         java.util.Iterator iterator;

         for (int n = 0; n < traverseMethods.length; ++n)
         {
            Object iteratorObject = traverseMethods[n].invoke(object);
            if (iteratorObject instanceof Iterator)
            {
               iterator = (java.util.Iterator) iteratorObject;
            }
            else
            {
               iterator = new OneElementIterator(iteratorObject);
            }

            if (iterator.hasNext() || (tree.isRotating()
                  && hasPreviousChunk() && isTraversable(object)))
            {
               return true;
            }
         }
      }
      catch (Exception x)
      {
         throw new InternalException(x);
      }

      return false;
   }

   /**
    * Returns the child TreeNode at index childIndex. Creates the tree nodes on
    * demand.
    */
   public TreeNode getChildAt(int index)
   {
      if (index > objects.size() || index < 0)
      {
         return null;
      }

      GenericTreeNode node = null;

      try
      {
         node = (GenericTreeNode) getChildrenVector().get(index);
      }
      catch (ArrayIndexOutOfBoundsException x)
      {
      }

      if (node == null)
      {
         node = new GenericTreeNode(tree, this, objects.get(index), sorted);

         getChildrenVector().set(index, node);
      }

      return node;
   }

   /**
    * Returns the number of children TreeNodes the receiver contains.
    */
   public int getChildCount()
   {
      return objects.size();
   }

   /**
    * Returns the index of node in the receivers children.
    */
   public int getIndex(TreeNode node)
   {
      return getChildrenVector().indexOf(node);
   }

   /**
    * Returns the index of node in the receivers children.
    */
   public int getIndexOf(Object object)
   {
      if (getUserObject().equals(object))
      {
         return 0;
      }
      int index = 0;
      Enumeration childs = children();
      while (childs.hasMoreElements())
      {
         GenericTreeNode node = (GenericTreeNode) childs.nextElement();
         if (node.getUserObject().equals(object))
         {
            return index;
         }
         index++;
      }
      return 0;
   }

   /**
    * Returns the parent TreeNode of the receiver.
    */
   public TreeNode getParent()
   {
      return parent;
   }

   /**
    *
    */
   public boolean isLeaf()
   {
      return objects.isEmpty();
   }

   /**
    * Adds child to the receiver at index.
    */
   public void insert(MutableTreeNode child, int index)
   {
      if (index >= getChildrenVector().size())
      {
         getChildrenVector().setSize(index + 1);
      }

      getChildrenVector().insertElementAt(child, index);
   }

   /**
    * Removes the child at index from the receiver.
    */
   public void remove(int index)
   {
      getChildrenVector().remove(index);
   }

   /**
    * Removes node from the receiver.
    */
   public void remove(MutableTreeNode node)
   {
      getChildrenVector().remove(node);
   }

   /**
    * Removes the receiver from its parent.
    */
   public void removeFromParent()
   {
      parent.remove(this);
   }

   /**
    * Sets the parent of the receiver to newParent.
    */
   public void setParent(MutableTreeNode parent)
   {
      removeFromParent();

      this.parent = parent;
   }

   /**
    * Resets the user object of the receiver to object.
    */
   public void setUserObject(Object object)
   {
      this.object = object;

      refreshLabelAndIcon();
   }

   /**
    *
    */
   public Object getUserObject()
   {
      return object;
   }

   /**
    *
    */
   private boolean hasMoreElements()
   {
      if (currentIterator == null)
      {
         return false;
      }
      else if (currentIterator.hasNext())
      {
         return true;
      }
      else
      {
         nextIterator();

         if (currentIterator != null)
         {
            return true;
         }

         return false;
      }
   }

   /**
    *
    */
   private Object nextElement()
   {
      if (!currentIterator.hasNext())
      {
         nextIterator();

         if (currentIterator == null)
         {
            return null;
         }
      }

      return currentIterator.next();
   }

   /**
    *
    */
   public boolean isNodeAncestor(TreeNode node)
   {
      Assert.isNotNull(node);

      if (parent == null || node == this)
      {
         return false;
      }
      else if (parent == node)
      {
         return true;
      }
      else
      {
         return ((GenericTreeNode) parent).isNodeAncestor(node);
      }
   }

   /**
    *
    */
   protected Vector getChildrenVector()
   {
      if (nodes == null)
      {
         nodes = new Vector();
      }

      return nodes;
   }

   /**
    * Retrieves the next iterator whith at least one element.
    */
   private void nextIterator()
   {
      while (traverseMethods != null &&
            traverseMethods.length != 0 &&
            traverseMethodIndex < traverseMethods.length)
      {
         try
         {
            Object iteratorObject = traverseMethods[traverseMethodIndex].invoke(object);

            if (iteratorObject instanceof java.util.Iterator)
            {
               currentIterator = (java.util.Iterator) iteratorObject;
            }
            else if (iteratorObject instanceof java.util.Enumeration)
            {
               currentIterator = new EnumerationIteratorWrapper((java.util.Enumeration) iteratorObject);
            }
            else if (iteratorObject instanceof java.util.Collection)
            {
               currentIterator = ((java.util.Collection) iteratorObject).iterator();
            }
            else
            {
               currentIterator = new OneElementIterator(iteratorObject);
            }

            Assert.isNotNull(currentIterator);

            ++traverseMethodIndex;

            if (currentIterator.hasNext())
            {
               return;
            }
         }
         catch (Exception x)
         {
            trace.warn("", x);
            throw new InternalException(x);
         }
      }

      currentIterator = null;
   }

   /**
    * Returns the node containing the provided user object.
    */
   public GenericTreeNode lookupNode(Object object)
   {
      return lookupNode(object, Short.MAX_VALUE, true);
   }

   /**
    * Returns the node containing the provided user object.
    */
   public GenericTreeNode lookupNode(Object object, boolean deep)
   {
      return lookupNode(object, Short.MAX_VALUE, deep);
   }

   /**
    *
    */
   public GenericTreeNode lookupNode(Object object, int serachDepth, boolean deep)
   {
      int depth = 0;
      int childCount = this.getChildCount();
      LinkedList nodes = new LinkedList();

      nodes.add(this);
      GenericTreeNode node = null;

      while ((!nodes.isEmpty()) && (depth < serachDepth))
      {
         boolean shouldAdd = true;

         if ((!deep) && depth > childCount)
         {
            shouldAdd = false;
         }
         node = (GenericTreeNode) nodes.removeFirst();

         if (node.getUserObject().equals(object))
         {
            return node;
         }
         else
         {
            java.util.Enumeration subNodes = node.children();

            while (subNodes.hasMoreElements() && shouldAdd)
            {
               nodes.add(subNodes.nextElement());
               depth++;
            }
         }
      }
      return null;
   }

   /**
    * Refreshes this node and its subnodes from their corresponding objects.
    * <p/>
    * Attention: The method refresh only the icons and labels.
    * NOT the tree structur. So new inserted tree nodes doesn't appear!
    * If you want to update the structure use the reload()-methods
    */
   public void refreshFromModel()
   {
      // Refresh this node

      refreshLabelAndIcon();

      // Notify the tree about the change

      ((DefaultTreeModel) tree.getModel()).nodeChanged(this);

      // Refresh its children

      java.util.Enumeration subNodes = children();

      while (subNodes.hasMoreElements())
      {
         ((GenericTreeNode) subNodes.nextElement()).refreshFromModel();
      }
   }

   public boolean isSorted()
   {
      return sorted;
   }

   public void setSorted(boolean sorted)
   {
      this.sorted = sorted;
   }

   private boolean isTraversable(Object object)
   {
      int idx = tree.getClassIndex(object.getClass());
      if (idx == -1)
      {
         return false;
      }
      if (null == tree.traverseMethods[idx])
      {
         return false;
      }
      return true;
   }

   public String getDisplayValue()
   {
      return displayValue;
   }

   public void setDisplayValue(String value)
   {
      this.displayValue = value;
   }

   public int getLeafCount()
   {
      return leafCount;
   }
}
