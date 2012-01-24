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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** */
public class SwapListPanel extends JPanel
      implements ActionListener,
      ListSelectionListener
{
   JList leftList;
   JList rightList;
   JButton addButton;
   JButton removeButton;
   String leftTitle;
   String rightTitle;

   // static boolean for valueChanged
   boolean isAdjusting;

   /** */
   public SwapListPanel(JList leftList,
         JList rightList,
         String leftTitle,
         String rightTitle)
   {
      this.leftList = leftList;
      this.rightList = rightList;

      setBorder(new EmptyBorder(10,
            10,
            10,
            10));

      setLayout(new BoxLayout(this,
            BoxLayout.X_AXIS));

      JScrollPane scrollPane;
      JLabel label;

      // Add left list

      Box box = Box.createVerticalBox();

      box.add(label = new JLabel(leftTitle));

      label.setAlignmentX(-0.5f);

      box.add(Box.createVerticalStrut(GUI.VerticalWidgetDistance));
      box.add(scrollPane = new JScrollPane(leftList));

      /*scrollPane.setPreferredSize(new Dimension(100,
                                                150));
   */
      leftList.setBackground(Color.white);
      leftList.getSelectionModel().addListSelectionListener(this);

      add(box);
      add(Box.createHorizontalStrut(GUI.HorizontalWidgetDistance));
      add(Box.createHorizontalGlue());

      // Add remove and add buttons

      box = Box.createVerticalBox();

      box.add(Box.createVerticalGlue());
      JPanel helper = new JPanel(new java.awt.BorderLayout());

      // add button
      addButton = new JButton("Add",
            new ImageIcon(GUI.class.getResource("images/ArrowLeft.gif")));
      addButton.addActionListener(this);
      addButton.setMargin(new Insets(0, 0, 0, 0));
      addButton.setHorizontalAlignment(SwingConstants.LEFT);
      addButton.setMnemonic('a');

      // remove button
      removeButton = new JButton("Remove",
            new ImageIcon(GUI.class.getResource("images/ArrowRight.gif")));
      removeButton.setHorizontalAlignment(SwingConstants.RIGHT);
      removeButton.setMargin(new Insets(0, 0, 0, 0));
      removeButton.setHorizontalTextPosition(SwingConstants.LEFT);
      removeButton.addActionListener(this);
      removeButton.setMnemonic('r');

      helper.add("North", addButton);
      helper.add("Center", Box.createVerticalStrut(GUI.VerticalWidgetDistance * 2));
      helper.add("South", removeButton);
      helper.setMaximumSize(helper.getPreferredSize());

      box.add(helper);

      //box.add(Box.createVerticalStrut(GUI.VerticalWidgetDistance));

      // make button size equal
      /*	Dimension remDim = removeButton.getPreferredSize();
         Dimension addDim = addButton.getPreferredSize();
         addDim.width = remDim.width + 16 + 160 ;
         addButton.setMinimumSize(addDim);
         addButton.setPreferredSize(addDim);
      */
      // add glue
      box.add(Box.createVerticalGlue());

      add(box);
      add(Box.createHorizontalGlue());

      // Add right list
      add(Box.createHorizontalStrut(GUI.HorizontalWidgetDistance));
      box = Box.createVerticalBox();

      box.add(label = new JLabel(rightTitle));

      label.setAlignmentX(-0.5f);

      box.add(Box.createVerticalStrut(10));
      box.add(scrollPane = new JScrollPane(rightList));

      /*scrollPane.setPreferredSize(new Dimension(100,
                                                150));
   */
      rightList.setBackground(Color.white);
      rightList.getSelectionModel().addListSelectionListener(this);

      add(box);

      // init isAdjusting
      isAdjusting = false;
   }

   /** */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource().equals(addButton))
      {
         Object[] objects = rightList.getSelectedValues();

         for (int n = 0; n < objects.length; ++n)
         {
            ((DefaultListModel) rightList.getModel()).removeElement(objects[n]);
            ((DefaultListModel) leftList.getModel()).addElement(objects[n]);
            leftList.setSelectedValue(objects[n],
                  true);
         }
      }
      else if (event.getSource().equals(removeButton))
      {
         Object[] objects = leftList.getSelectedValues();

         for (int n = 0; n < objects.length; ++n)
         {
            ((DefaultListModel) leftList.getModel()).removeElement(objects[n]);
            ((DefaultListModel) rightList.getModel()).addElement(objects[n]);
            rightList.setSelectedValue(objects[n],
                  true);
         }
      }
   }

   /** */
   public void valueChanged(ListSelectionEvent event)
   {// Note: Clearing the selection in other list throws another valueChanged
      //		 So we have to make use of a helper boolean.

      if (isAdjusting)
      {
         return;
      }

      if (event.getSource() == leftList.getSelectionModel())
      {
         addButton.setEnabled(false);
         removeButton.setEnabled(true);
         isAdjusting = true;
         rightList.clearSelection();
         isAdjusting = false;
      }
      else if (event.getSource() == rightList.getSelectionModel())
      {
         addButton.setEnabled(true);
         removeButton.setEnabled(false);
         isAdjusting = true;
         leftList.clearSelection();
         isAdjusting = false;
      }
   }

}// SwapListPanel
