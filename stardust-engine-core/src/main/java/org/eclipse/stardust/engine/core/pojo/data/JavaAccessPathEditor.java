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
package org.eclipse.stardust.engine.core.pojo.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.AccessPathEditor;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * @author ubirkemeyer
 */
public class JavaAccessPathEditor extends AccessPathEditor
{
   private static final Logger trace = LogManager.getLogger(JavaAccessPathEditor.class);

   private static boolean iconsLoaded;
   private static Icon inIcon;
   private static Icon outIcon;

   private JTree tree;
   private JScrollPane scrollPane;
   private Direction direction;
   public static final String SEPERATOR = ".";

   public JavaAccessPathEditor()
   {
      setLayout(new BorderLayout());

      setBorder(new CompoundBorder(new TitledBorder(" Methods "), new EmptyBorder(10, 10, 3, 3)));

      tree = new JTree();
      tree.setRootVisible(true);
      tree.setShowsRootHandles(true);
      // the SelectionListener will prohibit the selection of incompatible tree nodes
      // with the current direction
      tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent e)
         {
            TreePath path = e.getPath();
            if (path.getPathCount() > 1)
            {
               Method method = ((MethodNode) path.getLastPathComponent()).method;
               if (direction == Direction.IN && method.getParameterTypes().length == 0)
               {
                  tree.clearSelection();
               }
            }
            else
            {
               if (path.getPathCount() == 1)
               {
                  AccessPoint ap = ((AccessPointNode) path.getLastPathComponent()).ap;
                  if (!ap.getDirection().isCompatibleWith(direction))
                  {
                     tree.clearSelection();
                  }
               }
            }
         }
      });
      scrollPane = new JScrollPane(tree);

      add(scrollPane, BorderLayout.CENTER);
      setPreferredSize(new Dimension(400, 400));
   }

   public String getPath()
   {
      TreePath path = tree.getSelectionPath();
      if (path == null)
      {
         return null;
      }
      StringBuffer sb = new StringBuffer();
      for (int i = 1; i < path.getPathCount(); i++)
      {
         if (i > 1)
         {
            sb.append(SEPERATOR);
         }
         Method method = ((MethodNode) path.getPathComponent(i)).method;
         sb.append(Reflect.encodeMethod(method));
      }
      return sb.toString();
   }

   public void setValue(AccessPoint data, String path, Direction direction)
   {
      this.direction = direction;
      MyTreeModel model = new MyTreeModel(data);
      tree.setModel(model);
      tree.setCellRenderer(new MyTreeRenderer());
      TreePath treePath = new TreePath(new AccessPointNode(data));
      if (!StringUtils.isEmpty(path))
      {
         try
         {
            Iterator iterator = JavaDataTypeUtils.parse(path).iterator();
            Class type = JavaDataTypeUtils.getReferenceClass(data);
            while (iterator.hasNext())
            {
               String methodName = (String) iterator.next();
               Method method = Reflect.decodeMethod(type, methodName);
               treePath = treePath.pathByAddingChild(new MethodNode(method, false));
               type = method.getReturnType();
            }
         }
         catch (Exception e)
         {
            trace.warn("Invalid access path", e);
         }
      }
      tree.setSelectionPath(treePath);
      // somehow tree.scrollPathToVisible(tree.getSelectionPath()) does not work !!!
      if (tree.getSelectionPath() != null)
      {
         scrollPane.getViewport().setViewPosition(tree.getRowBounds(
               tree.getRowForPath(tree.getSelectionPath())).getLocation());
      }
   }

   private static void loadIcons()
   {
      try
      {
         inIcon = new ImageIcon(JavaAccessPathEditor.class.getResource("images/in_point.gif"));
         outIcon = new ImageIcon(JavaAccessPathEditor.class.getResource("images/out_point.gif"));
      }
      catch (Exception x)
      {
         throw new PublicException(BpmRuntimeError.DIAG_CANNOT_LOAD_IMAGE_ICON.raise(), x);
      }
      iconsLoaded = true;
   }

   private class MyTreeModel implements TreeModel
   {
      private Node root;
      private Vector listeners = new Vector();

      private MyTreeModel(AccessPoint root)
      {
         this.root = new AccessPointNode(root);
      }

      public Object getRoot()
      {
         return root;
      }

      public Object getChild(Object parent, int index)
      {
         return ((Node) parent).getChild(index);
      }

      public int getChildCount(Object parent)
      {
         return ((Node) parent).getChildCount();
      }

      public boolean isLeaf(Object node)
      {
         return ((Node) node).getChildCount() == 0;
      }

      public void valueForPathChanged(TreePath path, Object newValue)
      {
      }

      public int getIndexOfChild(Object parent, Object child)
      {
         return ((Node) parent).getIndexOfChild(child);
      }

      public void addTreeModelListener(TreeModelListener l)
      {
         listeners.add(l);
      }

      public void removeTreeModelListener(TreeModelListener l)
      {
         listeners.remove(l);
      }
   }

   private abstract class Node
   {
      Vector children;
      boolean nextNodeIsLeaf;

      abstract void bootstrap();

      abstract Icon getIcon();

      abstract String getText();

      private Object getChild(int index)
      {
         if (children == null)
         {
            bootstrap();
         }
         return children.elementAt(index);
      }

      private int getChildCount()
      {
         if (children == null)
         {
            bootstrap();
         }
         return children.size();
      }

      private int getIndexOfChild(Object child)
      {
         if (children == null)
         {
            bootstrap();
         }
         return children.indexOf(child);
      }

      boolean isValidPathElement(Method method)
      {
         nextNodeIsLeaf = false;
         Class[] parameterTypes = method.getParameterTypes();
         if (parameterTypes.length == 0)
         {
            Class rt = method.getReturnType();
            if (rt == Void.TYPE)
            {
               return false;
            }
            if (direction == Direction.IN && (rt.isPrimitive() || rt.isArray()))
            {
               return false;
            }
            return true;
         }
         if (parameterTypes.length > 1 || direction == Direction.OUT)
         {
            return false;
         }
         nextNodeIsLeaf = true;
         return true;
      }
   }

   private class AccessPointNode extends Node
   {
      private AccessPoint ap;

      private AccessPointNode(AccessPoint ap)
      {
         this.ap = ap;
      }

      void bootstrap()
      {
         children = new Vector();

         if (direction == Direction.IN
               && !ap.getBooleanAttribute(PredefinedConstants.BROWSABLE_ATT))
         {
            return;
         }
         Method[] methods;
         try
         {
            Class type = JavaDataTypeUtils.getReferenceClass(ap);
            methods = type.getMethods();
         }
         catch (Exception e)
         {
            // @todo/hiob (ub): backwards compatibility for primitive types
            trace.warn("", e);
            return;
         }

         for (int i = 0; i < methods.length; ++i)
         {
            if (isValidPathElement(methods[i]))
            {
               children.add(new MethodNode(methods[i], nextNodeIsLeaf));
            }

         }

         Collections.sort(children, new NodeComparator());
      }

      Icon getIcon()
      {
         // todo: (fh) add the possibility for custom icon providers
         return IconProvider.instance().getAccessPointIcon(ap);
      }

      String getText()
      {
         return ap.getId();
      }

      public boolean equals(Object o)
      {
         return (this == o) ||
               ((o instanceof AccessPointNode) && ap.equals(((AccessPointNode) o).ap));
      }

      public int hashCode()
      {
         return ap.hashCode();
      }
   }

   private class MethodNode extends Node
   {
      private Method method;
      private boolean isLeaf;

      private MethodNode(Method method, boolean isLeaf)
      {
         this.method = method;
         this.isLeaf = isLeaf;
      }

      void bootstrap()
      {
         children = new Vector();

         if (isLeaf)
         {
            return;
         }

         Method[] methods = method.getReturnType().getMethods();

         for (int i = 0; i < methods.length; ++i)
         {
            if (isValidPathElement(methods[i]))
            {
               children.add(new MethodNode(methods[i], nextNodeIsLeaf));
            }
         }

         Collections.sort(children, new NodeComparator());
      }

      Icon getIcon()
      {
         if (!iconsLoaded)
         {
            loadIcons();
         }
         return method.getParameterTypes().length == 0 ? outIcon : inIcon;
      }

      String getText()
      {
         return Reflect.getSortableMethodName(method);
      }

      public boolean equals(Object o)
      {
         return (this == o) ||
               ((o instanceof MethodNode) && method.equals(((MethodNode) o).method));
      }

      public int hashCode()
      {
         return method.hashCode();
      }
   }

   private class NodeComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         return ((Node) o1).getText().compareTo(((Node) o2).getText());
      }
   }

   private class MyTreeRenderer extends DefaultTreeCellRenderer
   {
      public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
      {
         super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
         setText(((Node) value).getText());
         setIcon(((Node) value).getIcon());
         return this;
      }
   }
}
