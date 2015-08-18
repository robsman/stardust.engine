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

import java.awt.*;

import javax.swing.*;

import org.eclipse.stardust.common.AttributeHolder;
import org.eclipse.stardust.common.error.ValidationException;


/** */
public class StringPropertyDialog extends AbstractDialog
{
   protected static StringPropertyDialog singleton = null;

   private AttributeHolder propertyHolder;
   private TextEntry nameEntry;
   private TextEntry valueEntry;

   /** */
   protected StringPropertyDialog()
   {
      this(null);
   }

   /** */
   protected StringPropertyDialog(Frame parent)
   {
      super(parent);
   }

   /** */
   public JComponent createContent()
   {
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      components.add(nameEntry = new TextEntry(10), "Name:");
      components.add(valueEntry = new TextEntry(30), "Value:");
      components.pack();

      panel.add(components);

      return panel;
   }

   public void onOK()
   {
      propertyHolder.setAttribute(nameEntry.getText(), valueEntry.getValue());
   }

   public void validateSettings() throws ValidationException
   {
   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(AttributeHolder propertyHolder)
   {
      return showDialog(propertyHolder, null);
   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(AttributeHolder propertyHolder, Frame parent)
   {
      if (singleton == null)
      {
         singleton = new StringPropertyDialog(parent);
      }

      singleton.propertyHolder = propertyHolder;

      return showDialog("Create String Property", singleton);
   }
}
