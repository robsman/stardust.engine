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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.LabeledComponentsPanel;
import org.eclipse.stardust.engine.core.compatibility.gui.TextEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.DataPropertiesPanel;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SerializablePropertiesEditor extends DataPropertiesPanel
{
   private static final String CLASS_NAME = "Class Name:";
   private static final short DEFAULT_ENTRY_SIZE = 25;
   private static final String LABEL_BROWSE_BUTTON = "Browse";

   private LabeledComponentsPanel components;
   private TextEntry classEntry;
   private JButton selectClassButton;

   public SerializablePropertiesEditor()
   {
      components = new LabeledComponentsPanel();

      classEntry = new TextEntry(DEFAULT_ENTRY_SIZE);
      classEntry.setMandatory(true);
      selectClassButton = new JButton(LABEL_BROWSE_BUTTON);

      selectClassButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            SelectClassDialog.showDialog(getParentComponent());

            Class type;

            if ((type = SelectClassDialog.getSelectedClass()) != null)
            {
               String className = type.getName();
               classEntry.setValue(className);
            }
         }
      });

      components.add(
            new JComponent[]{classEntry, selectClassButton},
            new String[]{CLASS_NAME, ""},
            new int[]{'n', 'a'});

      components.pack();

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(" Serializable Properties "));
      add(components, BorderLayout.CENTER);
   }

   public void setAttributes(Map attributes, Map typeAttributes)
   {
      classEntry.setValue((String) attributes.get(PredefinedConstants.CLASS_NAME_ATT));
   }

   public Map getAttributes()
   {
      Map attributes = new HashMap();
      attributes.put(PredefinedConstants.CLASS_NAME_ATT, classEntry.getValue());
      return attributes;
   }

   public void checkProperties() throws ValidationException
   {
      try
      {
         Reflect.getClassFromClassName(classEntry.getText());
      }
      catch (InternalException e)
      {
         throw new ValidationException("Class " + classEntry.getText() +
               " could not be loaded.", true);
      }
   }

   private Component getParentComponent()
   {
      return SwingUtilities.getAncestorOfClass(Component.class, this);
   }
}
