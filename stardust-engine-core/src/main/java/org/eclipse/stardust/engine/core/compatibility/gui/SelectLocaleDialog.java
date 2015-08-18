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

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.stardust.common.error.ValidationException;


/** */
public class SelectLocaleDialog extends AbstractDialog
{
   protected static SelectLocaleDialog singleton = null;

   private TextEntry languageEntry;
   private TextEntry countryEntry;
   private AbstractMainWindow mainWindow;

   /** */
   public SelectLocaleDialog()
   {
      this(null);
   }

   /** */
   public SelectLocaleDialog(Frame parent)
   {
      super(parent);
   }

   /** */
   public JComponent createContent()
   {
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      components.add(languageEntry = new TextEntry(10), "Sprache:", 's');
      components.add(countryEntry = new TextEntry(10), "Land:", 'l');
      components.pack();

      panel.add(components);

      return panel;
   }

   /** */
   public void onOK()
   {
      mainWindow.setLocale(languageEntry.getText(), countryEntry.getText());
   }

   public void validateSettings() throws ValidationException
   {
      if (languageEntry.getText() == null ||
            languageEntry.getText().length() == 0 ||
            countryEntry.getText() == null ||
            countryEntry.getText().length() == 0)
      {
         throw new ValidationException("Sprache und Land dürfen nicht leer sein.", false);
      }

   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(AbstractMainWindow mainWindow)
   {
      if (singleton == null)
      {
         singleton = new SelectLocaleDialog(mainWindow);
      }

      singleton.mainWindow = mainWindow;

      return showDialog("Locale ändern", singleton, mainWindow);
   }
}
