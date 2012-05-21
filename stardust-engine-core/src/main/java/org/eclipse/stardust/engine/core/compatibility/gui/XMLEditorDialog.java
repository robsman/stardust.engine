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


import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import org.eclipse.stardust.common.error.ValidationException;

import java.awt.Component;
import java.awt.Frame;

/**
 * @author fherinean
 * @version $Revision$
 */
public class XMLEditorDialog extends AbstractDialog
{
   private static XMLEditorDialog instance;
   private JTextArea editor;

   public XMLEditorDialog(Frame frame)
   {
      super(frame);
      editor.removeKeyListener(this);
   }

   protected JComponent createContent()
   {
      editor = new JTextArea(20, 80);
      return new JScrollPane(editor);
   }

   public void validateSettings() throws ValidationException
   {
   }

   public void setData(String value)
   {
      editor.setText(value);
   }

   public String getData()
   {
      return editor.getText();
   }

   public static boolean showDialog(Component parent, String value)
   {
      if (instance == null)
      {
         Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
         instance = new XMLEditorDialog(frame);
      }
      instance.setSize(400, 300);
      instance.setData(value);
      return showDialog("XML Editor", instance);
   }

   public static String getText()
   {
      return instance.getData();
   }
}
