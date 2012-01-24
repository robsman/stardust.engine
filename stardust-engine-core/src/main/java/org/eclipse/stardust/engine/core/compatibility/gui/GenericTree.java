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

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *
 */
public class GenericTree extends JTree implements MouseListener,
      CellEditorListener,
      DropTargetListener,
      DragSourceListener, DragGestureListener
{
   private static final Logger trace = LogManager.getLogger(GenericTree.class);

   private static final int MandatoryModifiers = InputEvent.BUTTON3_MASK;
   private static final int ArbitraryModifiers = InputEvent.BUTTON3_MASK;
   private static final int LOAD_INCREMENT = 400;

   protected int loadIncrement;
   protected boolean dragEnabled;
   protected boolean dropEnabled;
   protected Class[] classes;
   protected Method[] labelMethods;
   protected Method[] labelSetMethods;
   protected Method[] iconMethods;
   protected String[][] roleNames;
   protected Method[][] traverseMethods;
   protected Method[][] addToMethods;
   protected Method[][] removeFromMethods;
   protected JPopupMenu[] popupMenus;
   protected JPopupMenu activePopupMenu;
   protected GenericTreeNode popupNode;
   protected GenericTreeNode dragNode;
   protected DropTarget dropTarget;
   protected DragSource dragSource;
   protected Hashtable customPopupMenus;
   protected Color labelSelectionBackgroundColor;
   protected boolean lastRootVisible;
   protected boolean isSorted;
   private Vector popupMenuListener;
   private HashMap loadIncrements = new HashMap();
   private boolean rotate = false;
   private String hasNextMethod;
   private String hasPreviousMethod;
   private IconProvider iconProvider;

   /**
    *
    * @param classes
    * @param roleNames
    * @param labelMethodNames
    */
   public GenericTree(Class[] classes, String[][] roleNames,
         String[] labelMethodNames)
   {
      this(classes, roleNames, labelMethodNames, true);
   }
   /**
    *
    * <strong>Hint:</strong> The default selection model is SINGLE_TREE_SELECTION
    *
    * @see javax.swing.tree.TreeSelectionModel#SINGLE_TREE_SELECTION
    */
   public GenericTree(Class[] classes, String[][] roleNames,
         String[] labelMethodNames, boolean isSorted)
   {
      super(new GenericTreeModel(new DefaultMutableTreeNode("X")));

      // Validate parameters

      if (classes.length != roleNames.length)
      {
         throw new InternalException("Length mismatch between classes and role names.");
      }

      if (classes.length != labelMethodNames.length)
      {
         throw new InternalException("Length mismatch between classes and label method names.");
      }

      this.isSorted = isSorted;

      Class[] changedClasses = new Class[classes.length+2];
      for (int i=0; i<classes.length;i++)
      {
         changedClasses[i]=classes[i];
      }
      changedClasses[classes.length]=TreeNavigatorUp.class;
      changedClasses[classes.length+1]=TreeNavigatorDown.class;

      this.classes = changedClasses;

      String[] changedLabelMethodNames = new String[labelMethodNames.length+2];
      for (int i=0; i<labelMethodNames.length;i++)
      {
         changedLabelMethodNames[i] = labelMethodNames[i];
      }
      changedLabelMethodNames[labelMethodNames.length]=null;
      changedLabelMethodNames[labelMethodNames.length+1]=null;

      labelMethodNames = changedLabelMethodNames;

      String[][] changedRoleNames = new String[roleNames.length+2][];
      for (int i=0; i<roleNames.length; i++)
      {
         changedRoleNames[i] = roleNames[i];
      }
      changedRoleNames[roleNames.length]=null;
      changedRoleNames[roleNames.length+1]=null;

      this.roleNames = changedRoleNames;

      // Lookup all traverse methods

      traverseMethods = new Method[this.classes.length][];

      for (int n = 0; n < this.classes.length; ++n)
      {
         if (this.roleNames[n] == null)
         {
            continue;
         }

         traverseMethods[n] = new Method[this.roleNames[n].length];

         for (int m = 0; m < this.roleNames[n].length; ++m)
         {
            try
            {
               trace.debug("Looking up method '" + this.classes[n] + ".getAll" + this.roleNames[n][m] + "'");
               traverseMethods[n][m] = this.classes[n].getMethod("getAll" + this.roleNames[n][m]);
            }
            catch (NoSuchMethodException x)
            {
               throw new InternalException(
                     "Traversal Method not found: 'getAll" + this.roleNames[n][m] +"'");
            }
         }
      }

      // Lookup addTo-methods

      addToMethods = new Method[this.classes.length][];

      Method[] methods;

      for (int n = 0; n < this.classes.length; ++n)
      {
         if (this.roleNames[n] == null)
         {
            continue;
         }

         addToMethods[n] = new Method[this.roleNames[n].length];

         for (int m = 0; m < this.roleNames[n].length; ++m)
         {
            methods = this.classes[n].getMethods();

            for (int l = 0; l < methods.length; ++l)
            {
               if (methods[l].getName().equals("addTo" + this.roleNames[n][m]))
               {
                  addToMethods[n][m] = methods[l];

                  break;
               }
            }
         }
      }

      // Lookup removeFrom-methods

      removeFromMethods = new Method[this.classes.length][];

      for (int n = 0; n < this.classes.length; ++n)
      {
         if (this.roleNames[n] == null)
         {
            continue;
         }

         removeFromMethods[n] = new Method[this.roleNames[n].length];

         for (int m = 0; m < this.roleNames[n].length; ++m)
         {
            methods = this.classes[n].getMethods();

            for (int l = 0; l < methods.length; ++l)
            {
               if (methods[l].getName().equals("removeFrom" + this.roleNames[n][m]))
               {
                  removeFromMethods[n][m] = methods[l];

                  break;
               }
            }
         }
      }

      // Lookup label methods

      labelMethods = new Method[this.classes.length];
      labelSetMethods = new Method[this.classes.length];

      for (int n = 0; n < this.classes.length; ++n)
      {
         if (labelMethodNames[n] != null)
         {
            // Lookup get method

            try
            {
               labelMethods[n] = this.classes[n].getMethod("get" + labelMethodNames[n]);
            }
            catch (NoSuchMethodException x)
            {
               throw new InternalException("The method get" + labelMethodNames[n] + "() provided to initialize the tree node label " +
                     "is not defined in class " + this.classes[n] + ".");
            }

            // Lookup set method

            try
            {
               labelSetMethods[n] = this.classes[n].getMethod("set" + labelMethodNames[n], new Class[]{String.class});
            }
            catch (NoSuchMethodException x)
            {
               labelSetMethods[n] = null;
            }
         }
         else
         {
            labelMethods[n] = null;
         }
      }

      iconMethods = new Method[this.classes.length];
      popupMenus = new JPopupMenu[this.classes.length];
      activePopupMenu = null;
      popupNode = null;
      dragNode = null;
      dropTarget = null;
      dragSource = null;

      addMouseListener(this);

      // Set cell renderer

      setCellRenderer(createTreeNodeRenderer());

      // Set cell editor
      setCellEditor(createTreeCellEditor());
      getCellEditor().addCellEditorListener(this);

      setRootVisible(true);
      setShowsRootHandles(true);


      // setSelectionMode
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

      // Init a hashtable for custom popup nodes

      customPopupMenus = new Hashtable();

      loadIncrement = LOAD_INCREMENT;
      dragEnabled = false;
      dropEnabled = false;
      labelSelectionBackgroundColor = Color.blue;
      lastRootVisible = isRootVisible();

      popupMenuListener = new Vector(20);

      addKeyShortCutListener();
   }

   /**
    * Creates a KeyListener for a GenericTree to handle keyboard shortcuts
    */
   private void addKeyShortCutListener()
   {
      KeyListener _listener = new KeyListener(){
         public void keyTyped(KeyEvent e)
         {
         }

         public void keyPressed(KeyEvent e)
         {
            final char keyChar = e.getKeyChar();
            if (Character.isLetterOrDigit(keyChar))
            {
               try
               {
                  TreePath selectionPath = getSelectionPath();

                  TreeNode contextNode = (TreeNode) selectionPath.getLastPathComponent();
                  if (contextNode instanceof GenericTreeNode)
                  {
                     GenericTreeNode context = (GenericTreeNode) contextNode;
                     TreeNode parentNode = context.getParent();

                     TreePath foundItem = null;

                     for (int i = 0; i < context.getChildCount(); ++i)
                     {
                        TreeNode child = context.getChildAt(i);
                        if (child instanceof GenericTreeNode)
                        {
                           if (((GenericTreeNode) child).getLabel().toLowerCase()
                                 .charAt(0) == Character.toLowerCase(keyChar))
                           {
                              foundItem = selectionPath.pathByAddingChild(child);
                              break;
                           }
                        }
                     }

                     if ((null == foundItem) && (null != parentNode))
                     {
                        int contextIndex = parentNode.getIndex(contextNode);
                        for (int i = contextIndex + 1; i < parentNode.getChildCount(); ++i)
                        {
                           TreeNode candidate = parentNode.getChildAt(i);
                           if (candidate instanceof GenericTreeNode)
                           {
                              if (((GenericTreeNode) candidate).getLabel().toLowerCase()
                                    .charAt(0) == Character.toLowerCase(keyChar))
                              {
                                 foundItem = selectionPath.getParentPath()
                                       .pathByAddingChild(candidate);
                                 break;
                              }
                           }
                        }
                     }

                     if (null != foundItem)
                     {
                        int row = getRowForPath(foundItem);
                        if (-1 != row)
                        {
                           setSelectionRow(row);
                        }
                     }
                  }
               }
               catch (Exception e1)
               {
                  return;
               }
            }
         }

         public void keyReleased(KeyEvent e)
         {
         }
      };
      this.addKeyListener(_listener);
   }

   /**
    *
    */
   public void addCustomPopup(MutableTreeNode node, JPopupMenu menu)
   {
      if (node == null)
      {
         throw new InternalException("Could not add popup menu: Node is null.");
      }

      customPopupMenus.put(node, menu);
   }

   /** Adds a node to the node, for which the last popup menu is opened. */
   public void addNodeToPopupNode(Object object)
   {
      addNodeToPopupNode(object, false);
   }

   /** Adds a node to the node, for which the last popup menu is opened and
    possibly connects the corresponding user objects. There must be only one method
    with name "addTo" + <role>. */
   public void addNodeToPopupNode(Object object, boolean link)
   {
      if (popupNode != null)
      {
         if (link)
         {
            Method addToMethod = getAddToMethod(popupNode.getUserObject().getClass(),
                  object.getClass());

            // Establish association link

            if (addToMethod != null)
            {
               try
               {
                  addToMethod.invoke(popupNode.getUserObject(), new Object[]{object});
               }
               catch (Exception x)
               {
                  throw new InternalException(x);
               }
            }
         }

         // Add node to the popup node

         DefaultTreeModel treeModel = (DefaultTreeModel) getModel();

         treeModel.insertNodeInto(createTreeNode(object),
               popupNode, popupNode.getChildCount());
         //         treeModel.reload(popupNode);
         repaint();
      }
   }

   /** Adds a node to the node, for which the last popup menu is opened and
    connects the corresponding user objects. */
   public void addNodeToPopupNodeAndLink(Object object)
   {
      addNodeToPopupNode(object, true);
   }

   /**
    */
   public void addNodeToRootNode(Object object)
   {
      Assert.isNotNull(object);

      addNodeToRootNode(object, false);
   }

   /**
    * Adds a node to the root node and possibly connects the corresponding user
    * objects. There must be only one method with name <code>addTo<role></code>.
    */
   public void addNodeToRootNode(Object object, boolean link)
   {
      Assert.isNotNull(object);

      if (link)
      {
         Method addToMethod = getAddToMethod(getRootObject().getClass(),
               object.getClass());

         // Establish association link

         if (addToMethod != null)
         {
            try
            {
               addToMethod.invoke(getRootObject(), new Object[]{object});
            }
            catch (Exception x)
            {
               throw new InternalException(x);
            }
         }
      }

      // Add node to the popup node

      DefaultTreeModel treeModel = (DefaultTreeModel) getModel();

      treeModel.insertNodeInto(createTreeNode(object),
            getRootNode(), getRootNode().getChildCount());
      //         treeModel.reload(popupNode);
      repaint();
   }

   /**
    * Adds a node the current selection.
    *
    * @param node				The node that should be added to the selection
    * @param makeVisible	if its <code>true</code> the node will make visible
    (that means the parent node will be expanded)
    */
   public void addNodeToSelection(TreeNode node
         , boolean makeVisible)
   {
      TreePath _path = null;

      if (node != null)
      {
         if (getModel() instanceof DefaultTreeModel)
         {
            _path = new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(node));
            addSelectionPath(_path);
            if (makeVisible)
            {
               makeVisible(_path);
            }
         }
      }

   }

   /**
    *
    */
   public void addPopupMenuListeners(TreePopupMenuListener listener)
   {
      if (listener != null)
      {
         popupMenuListener.add(listener);
      }
   }

   /**
    * Allows subclasses to create other CellEditor types than the default type
    * <tt>DefaultTreeCellEditor</tt>. The returned node must be implement the
    * interface <tt>TreeCellEditor</tt>.
    *
    * The implementation creates an DefaultTreeCellEditor with the
    * current CellRenderer created during the methodcall createTreeNodeRenderer()
    * and the TreeNodeEditor created by the call createTreeNodeEditor()
    */
   protected TreeCellEditor createTreeCellEditor()
   {
      return new DefaultTreeCellEditor(this, (DefaultTreeCellRenderer) getCellRenderer(),
            createTreeNodeEditor());
   }

   /**
    * Allows subclasses to create other node types than the default type
    * <tt>GenericTreeNode</tt>. The returned node must be a subclass of
    * <tt>GenericTreeNode</tt>.
    */
   protected GenericTreeNode createTreeNode(Object nodeObject)
   {
      return new GenericTreeNode(this, nodeObject, isSorted);
   }

   /**
    * Allows subclasses to create other node types than the default type
    * <tt>GenericTreeNodeEditor</tt>. The returned node must be a subclass of
    * <tt>GenericTreeNodeEditor</tt>.
    */
   protected TreeCellEditor createTreeNodeEditor()
   {
      return new GenericTreeNodeEditor();
   }

   /**
    * Allows subclasses to create other node types than the default type
    * <tt>GenericTreeNodeRenderer</tt>. The returned node must be a subclass of
    * <tt>GenericTreeNodeRenderer</tt>.
    */
   protected TreeCellRenderer createTreeNodeRenderer()
   {
      return new GenericTreeNodeRenderer();
   }

   /** */
   public void dragDropEnd(DragSourceDropEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dragDropEnd(DragSourceDropEvent) ]===");
      if (!getDragEnabled())
      {
         return;
      }
   }

   /** */
   public void dragEnter(DragSourceDragEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dragEnter(DragSourceDragEvent) ]===");
      if (getDragEnabled())
      {
         // Hint: We need to set the cursor to null to avoid flickering
      }

   }

   /**
    *
    */
   public void dragEnter(DropTargetDragEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dragEnter(DropTargetDragEvent) ]===");
      if (!getDropEnabled())
      {
         return;
      }

      e.acceptDrag(DnDConstants.ACTION_COPY);
   }

   /** */
   public void dragExit(DragSourceEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dragExit(DragSourceEvent) ]===");
      if (!getDragEnabled())
      {
         return;
      }
   }

   /**
    *
    */
   public void dragExit(DropTargetEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dragExit(DropTargetEvent) ]===");
      if (!getDropEnabled())
      {
         return;
      }
   }

   /**
    *
    */
   public void dragGestureRecognized(DragGestureEvent e)
   {
      if (!getDragEnabled())
      {
         return;
      }

      // Get selected object

      dragNode = (GenericTreeNode) getLastSelectedPathComponent();

      if (dragNode == null)
      {
         return;
      }

      // Do not allow root to be drag source

      if (dragNode.equals(getModel().getRoot()))
      {
         return;
      }

      // Fire drag event with hashcode of node

      try
      {
         LocalObjectTransferable transfer = new LocalObjectTransferable(dragNode.getUserObject());

         // @test --------------------------
         //			dragSource.startDrag(e, DragSource.DefaultLinkDrop, transfer, this);
         dragSource.startDrag(e, DragSource.DefaultCopyDrop, transfer, this);
      }
      catch (Exception x)
      {
         throw new InternalException("Drag start failed because of: " + x.getMessage());
      }
   }

   /** */
   public void dragOver(DragSourceDragEvent e)
   {
      if (getDragEnabled())
      {
         // Hint: We need to set the cursor to null to avoid flickering
         e.getDragSourceContext().setCursor(null);
         e.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
      }
   }

   /**
    *
    */
   public void dragOver(DropTargetDragEvent e)
   {
      if (!getDropEnabled())
      {
         return;
      }

      /***
       TreePath path;

       if ((path = getPathForLocation(e.getLocation().x, e.getLocation().y)) != null)
       {
       setCursor(DragSource.DefaultCopyDrop);
       }
       else
       {
       setCursor(DragSource.DefaultCopyNoDrop);
       }***/
   }

   /**
    *
    */
   public void drop(DropTargetDropEvent e)
   {
      if (!getDropEnabled())
      {
         e.rejectDrop();

         return;
      }

      try
      {
         Transferable tr = e.getTransferable();

         DataFlavor objectFlavor = null;

         try
         {
            objectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
         }
         catch (ClassNotFoundException x)
         {
            e.rejectDrop();

            return;
         }

         if (tr.isDataFlavorSupported(objectFlavor))
         {
            e.acceptDrop(DnDConstants.ACTION_COPY);

            Object sourceObject = tr.getTransferData(objectFlavor);

            e.getDropTargetContext().dropComplete(true);

            GenericTreeNode dragNode = lookupNode(sourceObject);
            GenericTreeNode dragNodeParent = null;

            if (dragNode != null)
            {
               dragNodeParent = (GenericTreeNode) dragNode.getParent();
            }

            // Get target object

            TreePath path = getClosestPathForLocation(e.getLocation().x, e.getLocation().y);
            GenericTreeNode dropNode = (GenericTreeNode) path.getLastPathComponent();
            Object targetObject = dropNode.getUserObject();

            // Get add/remove methods

            Method addToMethod = getAddToMethod(targetObject.getClass(),
                  sourceObject.getClass());
            Method removeFromMethod = getRemoveFromMethod(targetObject.getClass(),
                  sourceObject.getClass());

            // Do not allow recursive DnD in same tree (parent to child etc ...)

            /***               if (dropNode.isNodeAncestor(dragNode))
             {
             e.getDropTargetContext().dropComplete(false);

             return;
             }***/

            // Kill association link

            if (dragNode != null &&
                  removeFromMethod != null)
            {
               try
               {
                  removeFromMethod.invoke(dragNodeParent.getUserObject(), new Object[]{sourceObject});

                  ((DefaultTreeModel) getModel()).reload(dragNodeParent);
               }
               catch (Exception x)
               {
                  throw new InternalException(x);
               }
            }

            // Establish association link

            if (addToMethod != null)
            {
               try
               {
                  addToMethod.invoke(targetObject, new Object[]{sourceObject});
                  ((DefaultTreeModel) getModel()).reload(dropNode);
               }
               catch (Exception x)
               {
                  throw new InternalException(x);
               }
            }
         }
         else
         {
            // Flavour is not supported

            e.rejectDrop();
         }
      }
      catch (IOException io)
      {
         e.rejectDrop();
      }
      catch (UnsupportedFlavorException ufe)
      {
         e.rejectDrop();
      }
   }

   /** */
   public void dropActionChanged(DragSourceDragEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dropActionChanged(DragSourceDragEvent) ]===");
      if (!getDragEnabled())
      {
         return;
      }
   }

   /**
    *
    */
   public void dropActionChanged(DropTargetDragEvent e)
   {
      //@test ---------------------
      trace.debug( "===[ dropActionChanged(DropTargetDragEvent) ]===");
      if (!getDropEnabled())
      {
         return;
      }
   }

   /**
    * This tells the listeners the editor has canceled editing.
    */
   public void editingCanceled(ChangeEvent e)
   {
   }

   /**
    * This tells the listeners the editor has ended editing.
    */
   public void editingStopped(ChangeEvent e)
   {
      GenericTreeNodeEditor editor = (GenericTreeNodeEditor) e.getSource();

      Object object = editor.getCellEditorValue();

      if (object == null)
      {
         return;
      }

      // Retrieve set method

      Method setMethod = labelSetMethods[getClassIndex(object.getClass())];

      if (setMethod == null)
      {
         return;
      }

      // Invoke set method

      try
      {
         setMethod.invoke(object, new Object[]{editor.getText()});
         editor.getNode().setLabel(editor.getText());
      }
      catch (Exception x)
      {
         throw new InternalException("Failed to execute method \"" + setMethod.getName() + "\".");
      }
   }

   /**
    *
    */
   protected Method getAddToMethod(Class type, Class otherType)
   {
      int classIndex = getClassIndex(type);
      int associationIndex = getAssociationIndex(classIndex, otherType);

      if (associationIndex == -1)
      {
         return null;
      }

      return addToMethods[classIndex][associationIndex];
   }

   /**
    *
    */
   protected Iterator getAllPopupMenuListeners()
   {
      if (popupMenuListener != null)
      {
         return popupMenuListener.iterator();
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the index of the association, which allows traversal from node
    * with class index classIndex to class type.
    * The method may return -1 if one or more "addTo" methods are null.
    */
   protected int getAssociationIndex(int classIndex, Class type)
   {
      int index = -1;
      boolean nullMethod = false;

      for (int n = 0; n < addToMethods[classIndex].length; ++n)
      {
         if (addToMethods[classIndex][n] == null)
         {
            nullMethod = true;

            continue;
         }

         Class associationType = addToMethods[classIndex][n].getParameterTypes()[0];

         if (associationType.isAssignableFrom(type))
         {
            index = n;

            break;
         }
      }

      if (index == -1 && !nullMethod)
      {
         throw new InternalException("Class \"" + type.getName() + "\" unavailable as an association end in generic tree.");
      }
      else
      {
         return index;
      }
   }

   /**
    * Returns the index of the "closest" superclass/superinterface or the
    * class/interface itself of <type> in the array of support node classes.
    */
   protected int getClassIndex(Class type)
   {
      int index = -1;
      Class lastClass = null;

      for (int n = 0; n < classes.length; ++n)
      {
         if (classes[n].isAssignableFrom(type))
         {
            if (lastClass == null || lastClass.isAssignableFrom(classes[n]))
            {
               lastClass = classes[n];
               index = n;
            }
         }
      }

      if (index == -1)
      {
         throw new InternalException("Class \"" + type.getName() + "\" not found in generic tree.");
      }
      return index;
   }

   /**
    * @return <code>true</code>, if the drag and drop functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean getDragAndDropEnabled()
   {
      return dragEnabled && dropEnabled;
   }

   /**
    * @return <code>true</code>, if the drag functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean getDragEnabled()
   {
      return dragEnabled;
   }

   /**
    * @return <code>true</code>, if the drop functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean getDropEnabled()
   {
      return dropEnabled;
   }

   /**
    *
    */
   public Color getLabelSelectionBackgroundColor()
   {
      return labelSelectionBackgroundColor;
   }

   /** Returns the user object attached to the last selected tree node. */
   public Object getLastSelectedObject()
   {
      GenericTreeNode node = (GenericTreeNode) getLastSelectedPathComponent();

      if (node == null)
      {
         return null;
      }

      return node.getUserObject();
   }

   /**
    *
    */
   public int getLoadIncrement()
   {
      return loadIncrement;
   }

   /**
    * Returns the node for that the most recent popup menu is opened.
    */
   public GenericTreeNode getPopupNode()
   {
      return popupNode;
   }

   /**
    * Returns the object, for whose node the most recent popup menu is opened.
    */
   public Object getPopupObject()
   {
      if (popupNode != null)
      {
         return popupNode.getUserObject();
      }

      return null;
   }

   /**
    * Returns the object of the parent node of the node, whose node the
    * most recent popup menu is opened.
    */
   public Object getPopupObjectParent()
   {
      if (popupNode != null)
      {
         GenericTreeNode _parentNode = (GenericTreeNode) popupNode.getParent();

         if (_parentNode != null)
         {
            return _parentNode.getUserObject();
         }
      }

      return null;
   }

   /**
    *
    */
   protected Method getRemoveFromMethod(Class type, Class otherType)
   {
      int classIndex = getClassIndex(type);
      int associationIndex = getAssociationIndex(classIndex, otherType);

      if (associationIndex == -1)
      {
         return null;
      }

      return removeFromMethods[classIndex][associationIndex];
   }

   /**
    *
    */
   public GenericTreeNode getRootNode()
   {
      Assert.isNotNull(getModel());

      return (GenericTreeNode) ((DefaultTreeModel) getModel()).getRoot();
   }

   /**
    *
    */
   public Object getRootObject()
   {
      Assert.isNotNull(getModel());

      return getRootNode().getUserObject();
   }

   /**
    * @return <code>true</code>, if the drag and drop functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean isDragAndDropEnabled()
   {
      return getDragAndDropEnabled();
   }

   /**
    * @return <code>true</code>, if the drag functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean isDragEnabled()
   {
      return getDragEnabled();
   }

   /**
    * @return <code>true</code>, if the drop functionality of the tree
    *         is enabled; <code>false</code> otherwise.
    */
   public boolean isDropEnabled()
   {
      return getDropEnabled();
   }

   /**
    * Returns the node containing the provided user object.
    */
   public GenericTreeNode lookupNode(Object object)
   {
      return ((GenericTreeNode) ((DefaultTreeModel) getModel()).getRoot()).lookupNode(object);
   }

   /**
    *
    */
   public void mouseClicked(MouseEvent e)
   {
      //@this implementation does not work
/*
      TreePath path;

      if ((path = getClosestPathForLocation(e.getX(), e.getY())) != null &&
            e.getClickCount() == 2)
      {
         GenericTreeNode node = (GenericTreeNode) path.getLastPathComponent();

         if (node.getUserObject() == this)
         {
            ((DefaultTreeModel) getModel()).removeNodeFromParent(node);

            // Fetch next increment

            ((GenericTreeNode) node.getParent()).fetchNext();
         }
      }
*/
   }

   /**
    *
    */
   public void mouseEntered(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseExited(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mousePressed(MouseEvent e)
   {
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent e)
   {
      TreePath path = null;
      Iterator _iterator = null;

      if ((path = getPathForLocation(e.getX(), e.getY())) != null &&
            (e.getModifiers() & MandatoryModifiers) == MandatoryModifiers &&
            (e.getModifiers() & ArbitraryModifiers) != 0)
      {
         GenericTreeNode node = (GenericTreeNode) path.getLastPathComponent();

         // move the selection (but only if this is a SingleSelectionModel !!!
         // otherwise we can't call a contextmenu for a multiselection
         if ((getSelectionModel() != null)
               && (getSelectionModel().getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION)
         )
         {
            clearSelection();
            setSelectionPath(path);
         }

         if (node.getUserObject() != null)
         {
            int index = getClassIndex(node.getUserObject().getClass());

            if (popupMenus[index] != null)
            {
               popupNode = node;

               // Search custom popups first

               activePopupMenu = (JPopupMenu) customPopupMenus.get(node);

               // Default popups

               if (activePopupMenu == null)
               {
                  activePopupMenu = popupMenus[index];
               }

               // inform popupMenuListener
               if (popupMenuListener != null && !popupMenuListener.isEmpty())
               {
                  _iterator = popupMenuListener.iterator();
                  while (_iterator.hasNext())
                  {
                     ((TreePopupMenuListener) _iterator.next()).updateMenuState(this
                           , popupNode
                           , activePopupMenu);
                  }
               }

               // Show real popup
               GUI.showPopup(activePopupMenu, this, e.getX(), e.getY());
            }
         }
      }
      else if (activePopupMenu != null)
      {
         activePopupMenu.setVisible(false);

         activePopupMenu = null;
      }
   }

   /**
    * Refreshes the presentation of the node representing the object
    * <code>object</code>.
    */
   public void objectChanged(Object object)
   {
      DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
      GenericTreeNode node = lookupNode(object);

      if (node != null)
      {
         node.refreshFromModel();
         treeModel.nodeChanged(node);
      }
   }

   /**
    * Refreshes all labels and icons.
    *
    * Attention: The method refresh only the icons and labels.
    *            NOT the tree structur. So new inserted tree nodes
    *			  doesn't appear!
    *            If you want to update the structure use the reload()-methods
    */
   public void refreshFromModel()
   {
      DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
      GenericTreeNode rootNode = (GenericTreeNode) treeModel.getRoot();

      rootNode.refreshFromModel();
   }

   /**
    * Refreshes (and collapses) the whole tree assuming complex model changes.
    */
   public void reload()
   {
      DefaultTreeModel treeModel = (DefaultTreeModel) getModel();

      treeModel.reload();
   }

   /**
    * Refreshes (and collapses) the presentation of the node representing
    * the object <code>object</code> including its subtree.
    */
   public void reload(Object object)
   {
      DefaultTreeModel treeModel = (DefaultTreeModel) getModel();
      GenericTreeNode node = lookupNode(object);

      if (node != null)
      {
         treeModel.reload(node);
      }
   }

   /**
    * Refreshes the presentation of the node including its subtree.
    */
   public void reload(GenericTreeNode node)
   {
      if (node != null)
      {
         ((DefaultTreeModel) getModel()).reload(node);
      }
   }

   /**
    *
    */
   public void removeCustomPopup(MutableTreeNode node)
   {
      if (node == null)
      {
         return;
      }

      customPopupMenus.remove(node);
   }

   /**
    *
    */
   public void removePopupMenuListeners(TreePopupMenuListener listener)
   {
      if (listener != null)
      {
         popupMenuListener.remove(listener);
      }
   }

   /**
    * Removes the node, for which the last popup menu is opened.
    */
   public void removePopupNode()
   {
      removePopupNode(false);
   }

   /**
    * Removes the node, for which the last popup menu is opened and possibly disconnects
    * the corresponding user objects. There must be only one method with name
    * "removeFrom" + <role>.
    */
   public void removePopupNode(boolean unlink)
   {
      if (popupNode != null)
      {
         GenericTreeNode popupNodeParent = (GenericTreeNode) popupNode.getParent();

         if (unlink)
         {
            Method removeFromMethod = getRemoveFromMethod(popupNodeParent.getUserObject().getClass(),
                  popupNode.getUserObject().getClass());

            // Unlink association

            if (removeFromMethod != null)
            {
               try
               {
                  removeFromMethod.invoke(popupNodeParent.getUserObject(), new Object[]{popupNode.getUserObject()});
               }
               catch (Exception x)
               {
                  throw new InternalException(x);
               }
            }
         }

         // add node to the popup node

         DefaultTreeModel treeModel = (DefaultTreeModel) getModel();

         treeModel.removeNodeFromParent(popupNode);
         repaint();
      }
   }

   /**
    * Removes the node, for which the last popup menu is opened and disconnects
    * the corresponding user objects.
    */
   public void removePopupNodeAndUnlink()
   {
      removePopupNode(true);
   }

   /**
    * Enables/disables the drag and drop functionality of the tree for all nodes.
    */
   public void setDragAndDropEnabled(boolean dragAndDropEnabled)
   {
      setDragEnabled(dragAndDropEnabled);
      setDropEnabled(dragAndDropEnabled);
   }

   /**
    * Enables/disables the drag functionality of the tree for all nodes.
    */
   public void setDragEnabled(boolean dragEnabled)
   {
      this.dragEnabled = dragEnabled;

      if (dragEnabled)
      {
         dragSource = new DragSource();

         dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
      }
      else
      {
         dragSource = null;
      }
   }

   /**
    * Enables/disables the drop functionality of the tree for all nodes.
    */
   public void setDropEnabled(boolean dropEnabled)
   {
      this.dropEnabled = dropEnabled;

      if (dropEnabled)
      {
         dropTarget = new DropTarget(this, this);
      }
      else
      {
         dropTarget = null;
      }
   }

   /** Sets the method for retrieving the icons of the nodes representing
    objects of class/interface type or subinterfaces/subclasses.
    The method named name must return an object assignable to IconImage */
   public void setIconMethod(String name, Class type)
   {
      try
      {
         iconMethods[getClassIndex(type)] = type.getMethod(name);
      }
      catch (NoSuchMethodException x)
      {
         throw new InternalException("Class " + type, x);
      }
   }

   public void setIconProvider(IconProvider iconProvider)
   {
      this.iconProvider = iconProvider;
   }

   public IconProvider getIconProvider()
   {
      return iconProvider;
   }

   /**
    * Set a nodes label dynamically.
    */
   public void setLabelOfNode(String label, TreeNode node)
   {
      ((GenericTreeNode) node).setLabel(label);
   }

   /**
    *
    */
   public void setLabelSelectionBackgroundColor(Color color)
   {
      this.labelSelectionBackgroundColor = color;
   }

   /**
    *
    */
   public void setLoadIncrement(int loadIncrement)
   {
      this.loadIncrement = loadIncrement;
   }

   /** Sets the popup menu for the nodes representing objects of class/interface
    type or subinterfaces/subclasses. */
   public void setPopupMenu(JPopupMenu popupMenu, Class type)
   {
      popupMenus[getClassIndex(type)] = popupMenu;
   }

   /**
    *
    */
   public void setRootObject(Object object)
   {
      Assert.isNotNull(getModel());
      if (object != null)
      {
         boolean[] rows = new boolean[getRowCount()];
         for (int i=0; i<getRowCount();i++)
         {
            if (!isCollapsed(getPathForRow(i)))
            {
               rows[i] = true;
            }
         }
         ((DefaultTreeModel) getModel()).setRoot(createTreeNode(object));
         setEnabled(true);
         setRootVisible(lastRootVisible);
         getRootNode().loadObjects();
         for (int i=0; i<rows.length; i++)
         {
            if (rows[i])
            {
               expandRow(i);
            }
         }

      }
      else
      {
         collapseRow(0);
         super.setRootVisible(false);
         setEnabled(false);
      }
   }

   /**
    *
    */
   public void setRootVisible(boolean rootVisible)
   {
      this.lastRootVisible = rootVisible;

      super.setRootVisible(rootVisible);
   }

   /**
    * Determines whether the tree is isSorted
    * @return true, if tree is isSorted, otherwise false
    */
   public boolean isSorted()
   {
      return isSorted;
   }

   /**
    * Sets the tree to be isSorted
    * @param isSorted true, if tree is isSorted, otherwise false
    */
   public void setSorted(boolean isSorted)
   {
      this.isSorted = isSorted;
   }

   public void setLoadIncrementForType(Class type, int i)
   {
      loadIncrements.put(type, new Integer(i));
   }

   public int getLoadIncrementForType(Class type)
   {
      Integer returnValue = (Integer)loadIncrements.get(type);
      if (returnValue == null)
      {
         return loadIncrement;
      }
      return returnValue.intValue();
   }

   public void setRotation(boolean rotate)
   {
      this.rotate = rotate;
   }

   public boolean isRotating()
   {
      return rotate;
   }

   public void setHasNextMethod(String hasNextMethod)
   {
      this.hasNextMethod = hasNextMethod;
   }

   public void setHasPreviousMethod(String hasPreviousMethod)
   {
      this.hasPreviousMethod = hasPreviousMethod;
   }

   public String getHasNextMethod()
   {
      return hasNextMethod;
   }

   public String getHasPreviousMethod()
   {
      return hasPreviousMethod;
   }
}
