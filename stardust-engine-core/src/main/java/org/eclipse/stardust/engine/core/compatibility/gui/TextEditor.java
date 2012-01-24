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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 *
 */
public class TextEditor extends JPanel
{
   static protected final char NEW_LINE = '\n';

   private JTextPane textPane;
   private JToolBar toolBar;

   /**
    * Constructor that sets the entry field, the name, the id and type of entry
    * field.
    */
   public TextEditor(String title)
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(), title),
            new EmptyBorder(5, 5, 5, 5)));

      /**   add(toolBar = new JToolBar());
       toolBar.add(new ToolbarButton(new ImageIcon(getClass().getResource("images/cross.gif"))));
       toolBar.add(Box.createHorizontalGlue()); **/

      add(new JScrollPane(textPane = new JTextPane()));

      textPane.setBorder(new EtchedBorder());
   }

   /**
    */
   public JTextPane getTextPane()
   {
      return textPane;
   }

   /**
    */
   public String getText()
   {
      return textPane.getText();
   }

   /**
    */
   public void setText(String text)
   {
      if (text == null)
      {
         textPane.setText("");
      }
      else
      {
         textPane.setText(text);
      }
   }

   /**
    */
   public void addLine(String text)
   {
      if ((getText() == null) || (getText().length() == 0))
      {
         setText(text);
      }
      else
      {
         setText(getText() + NEW_LINE + text);
      }
   }
}
