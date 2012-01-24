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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.*;
import javax.swing.tree.TreePath;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.common.reflect.ClassPath;
import org.eclipse.stardust.engine.core.compatibility.gui.*;


/**
 *
 */
public class SelectClassDialog extends AbstractDialog
      implements MouseListener
{
   private final static Class[] treeClasses = {ClassPath.class, org.eclipse.stardust.common.reflect.Package.class, Class.class};
   private final static String[][] treeTraverseMethods = {{"Packages", "Classes"}, {"Packages", "Classes"}, null};
   private final static String[] treeLabelMethods = {null, "Name", "Name"};
   private static Class selectedClass;
   private static SelectClassDialog instance = null;

   private GenericTree tree;
   private GenericList list;
   private ClassPath classPath;

   /**
    *
    */
   protected SelectClassDialog()
   {
      super();
   }

   /**
    *
    */
   protected SelectClassDialog(Frame parent)
   {
      super(parent);
      setResizable(true);
   }

   /**
    *
    */
   public JComponent createContent()
   {
      classPath = ClassPath.instance();

      JPanel _classPathPanel = new JPanel(new BorderLayout());
      _classPathPanel.setBorder(GUI.getTitledPanelBorder("Class Path Entries:"));

      list = new GenericList(File.class, "Path");
      JScrollPane _classPathPane = new JScrollPane(list);
      _classPathPanel.add(_classPathPane);

      _classPathPane.setPreferredSize(new Dimension(500, 250));

      JPanel _classesPanel = new JPanel(new BorderLayout());
      _classesPanel.setBorder(GUI.getTitledPanelBorder("Loaded Classes:"));

      tree = new GenericTree(treeClasses, treeTraverseMethods, treeLabelMethods);
      JScrollPane _classesPane = new JScrollPane(tree);
      _classesPanel.add(_classesPane);

      _classesPane.setPreferredSize(new Dimension(500, 250));

      // create top-level arrangement

      JPanel _panel = new JPanel();
      _panel.setLayout(new GridLayout(2, 1, 0, 20));
      _panel.add(_classPathPanel);
      _panel.add(_classesPanel);

      // set up dialog behavior

      tree.addMouseListener(this);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      tree.setRootObject(classPath);
      tree.setLoadIncrement(100000);
      list.setIterator(classPath.getAllEntries());

      return _panel;
   }

   /**
    *
    */
   public static Class getSelectedClass()
   {
      return selectedClass;
   }

   /**
    *
    */
   public void mouseClicked(MouseEvent e)
   {
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
      TreePath path;

      if ((path = tree.getClosestPathForLocation(e.getX(), e.getY())) != null &&
            e.getClickCount() == 2)
      {
         GenericTreeNode node = (GenericTreeNode) path.getLastPathComponent();

         Object object = node.getUserObject();

         if (object instanceof Class)
         {
            selectedClass = (Class) object;

            doClickOkButton();
         }
      }
   }

   /**
    *
    */
   public void mouseReleased(MouseEvent e)
   {
   }

   /**
    *
    */
   public void onOK()
   {
      Object object = tree.getLastSelectedObject();

      if (object != null && object instanceof Class)
      {
         selectedClass = (Class) object;
      }
      else
      {
         selectedClass = null;
         throw new InternalException("No class selected.");
      }
   }

   public void validateSettings() throws ValidationException
   {
   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(Component parent)
   {
      if (instance == null)
      {
         Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
         instance = new SelectClassDialog(frame);
      }

      selectedClass = null;

      instance.setSize(400, 300);

      return showDialog("Select Class", instance);
   }
}
