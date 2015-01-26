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
/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;


/**
 *	Abstract class for displaying and editing numbers.
 */
public abstract class NumberEntry extends AbstractEntry
      implements ActionListener, FocusListener
{
   private static ClipboardOwner defaultClipboardOwner = new GUI.DefaultClipboardObserver();

   protected NumberFormat format;

   // @todo make the following static

   private PopupAdapter popupAdapter;
   private JDialog calculatorDialog;
   private Calculator calculator;
   private JMenuItem copyItem;
   private JMenuItem pasteItem;
   private JMenuItem calculatorItem;

   /**
    *	Default constructor.
    */
   public NumberEntry()
   {
      super();

      addFocusListener(this);
   }

   /**
    * Constructor that sets the visual size of this entry field.
    */
   public NumberEntry(int size)
   {
      super(size);

      addFocusListener(this);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    *	field.
    */
   public NumberEntry(int size, boolean mandatory)
   {
      super(size, mandatory);

      addFocusListener(this);
   }

   /**
    *	Do the initialization (called from constructors).
    */
   protected void initialize()
   {
      super.initialize();

      setHorizontalAlignment(JTextField.RIGHT);

      // Initialize format
      if ((getDocument() != null) && (getDocument() instanceof NumberDocument))
      {
         format = ((NumberDocument) getDocument()).getFormatObject();
      }
      else
      {
         format = NumberFormat.getInstance(Locale.getDefault());
      }

      // Popup menu

      JPopupMenu popupMenu = new JPopupMenu();

      copyItem = new JMenuItem("Copy");
      copyItem.addActionListener(this);
      popupMenu.add(copyItem);

      pasteItem = new JMenuItem("Paste");
      pasteItem.addActionListener(this);
      popupMenu.add(pasteItem);

      popupMenu.addSeparator();

      // Calculator

      calculatorItem = new JMenuItem("Taschenrechner");

      calculatorItem.addActionListener(this);
      popupMenu.add(calculatorItem);

      // Add the popup

      popupAdapter = PopupAdapter.create(this, popupMenu);

      KeyStroke SHIFT_F10 = KeyStroke.getKeyStroke(KeyEvent.VK_F10,
            InputEvent.SHIFT_MASK);
      registerKeyboardAction(this, "popup", SHIFT_F10, JComponent.WHEN_FOCUSED);
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (calculator != null && source == calculator.getOKButton())
      {
         calculatorDialog.setVisible(false);

         setObjectValue(null);
      }
      else if (source == copyItem)
      {
         Clipboard clipboard = getToolkit().getSystemClipboard();
         Object value = getObjectValue();

         if (value == null)
         {
            getToolkit().beep();

            return;
         }

         StringSelection contents = new StringSelection(value.toString());
         clipboard.setContents(contents, defaultClipboardOwner);
      }
      else if (source == pasteItem)
      {
         Clipboard clipboard = getToolkit().getSystemClipboard();
         Transferable content = clipboard.getContents(this);

         if (content == null)
         {
            setObjectValue(null);
            getToolkit().beep();

            return;
         }
      }
      else if (source == calculatorItem)
      {
         Dimension size = getSize();

         runCalculatorDialog(size.width, size.height);
      }
      else if ("popup".equals(e.getActionCommand()))
      {
         Point point = getLocation();

         popupAdapter.doPopup(0, getHeight());
      }
   }

   /**
    *
    */
   private void runCalculatorDialog(int width, int height)
   {
      if (calculatorDialog == null)
      {
         JFrame frame = null;
         Container container = getTopLevelAncestor();

         if (container instanceof JFrame)
         {
            frame = (JFrame) container;
         }

         calculatorDialog = new JDialog(frame, "Rechner", true);
         calculator = new Calculator();

         calculator.getOKButton().addActionListener(this);
         calculatorDialog.getContentPane().add(calculator);
         calculatorDialog.pack();
      }

      // Set calculator's initial value

      Object number = getObjectValue();

      Assert.condition(number instanceof Number);

      calculator.setValue((Number) number);

      // Calculate

      Point point = getLocationOnScreen();
      int x = point.x + width;
      int y = point.y + height;

      // @todo make generic in base class
      // Ensure that it does not exceed the screen limits

      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dim = calculatorDialog.getPreferredSize();

      if (x + dim.width >= screenDim.width)
      {
         x = screenDim.width - dim.width - 1;
      }

      if (y + dim.height >= screenDim.height)
      {
         y = screenDim.height - dim.height - 1;
      }

      // Make visible

      calculatorDialog.setLocation(x, y);
      calculatorDialog.show();
   }

   /**
    *
    */
   public void focusGained(FocusEvent e)
   {
      super.focusGained(e);
   }

   /**
    *
    */
   public void focusLost(FocusEvent e)
   {
      if (getText().equals("-"))
      {
         setText("");
      }
      else
      {
         // reformat the content
         setText(getText());
      }
   }
}
