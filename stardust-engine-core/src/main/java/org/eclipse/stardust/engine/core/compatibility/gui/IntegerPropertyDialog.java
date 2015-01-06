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


/**
 * Class for entering an Integer value
 */
public class IntegerPropertyDialog extends AbstractDialog
{
   protected static IntegerPropertyDialog singleton = null;

   private AttributeHolder propertyHolder;
   private TextEntry nameEntry;
   private IntegerEntry valueEntry;

   /**
    * Default protected constructor
    */
   protected IntegerPropertyDialog()
   {
      this(null);
   }

   /**
    * Default protected constructor
    */
   protected IntegerPropertyDialog(Frame parent)
   {
      super(parent);
   }

   /**
    * Overridden createContent to specialize content for this dialog
    * 
    * @return The component for the content
    */
   public JComponent createContent()
   {
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      components.add(nameEntry = new TextEntry(10), "Name:");
      components.add(valueEntry = new IntegerEntry(10), "Value:");
      components.pack();

      panel.add(components);

      return panel;
   }

   /**
    * Default behavior when OK-Button has been clicked
    */
   public void onOK()
   {
      propertyHolder.setAttribute(nameEntry.getText(), valueEntry.getObjectValue());
   }

   public void validateSettings() throws ValidationException
   {
   }

   /**
    * Shows the dialog
    * 
    * @param propertyHolder The value holder for the property
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(AttributeHolder propertyHolder)
   {
      return showDialog(propertyHolder, null);
   }

   /**
    * Shows the dialog
    * 
    * @param propertyHolder The value holder for the property
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(AttributeHolder propertyHolder, Frame parent)
   {
      if (singleton == null)
      {
         singleton = new IntegerPropertyDialog(parent);
      }

      singleton.propertyHolder = propertyHolder;

      return showDialog("Create Integer Property", singleton);
   }
}
