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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.*;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.DataPropertiesPanel;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PrimitivePropertiesEditor extends DataPropertiesPanel
{
   private static final String LABEL_TYPE = "Type:";
   private static final short DEFAULT_ENTRY_SIZE = 20;

   private LabeledComponentsPanel components;
   private StringKeyBox typeBox;
   private JComponent defaultValueEntry;
   private int defaultValueEntryIndex;

   private static Map defaultValueEntries = new HashMap();

   private AccessPathEvaluator evaluator = new PrimitiveAccessPathEvaluator();

   static
   {
      defaultValueEntries.put(Type.Boolean, new BooleanEntry());
      defaultValueEntries.put(Type.Byte, new ByteEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Char, new CharEntry(2, false));
      defaultValueEntries.put(Type.Calendar, new CalendarEntry());
      defaultValueEntries.put(Type.Timestamp, new DateEntry());
      defaultValueEntries.put(Type.Double, new DoubleEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Float, new FloatEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Integer, new IntegerEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Long, new LongEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Short, new ShortEntry(DEFAULT_ENTRY_SIZE));
      defaultValueEntries.put(Type.Money, new MoneyEntry());
      defaultValueEntries.put(Type.String, new TextEntry());
   }

   public PrimitivePropertiesEditor()
   {
      components = new LabeledComponentsPanel();

      typeBox = new StringKeyBox(Type.class, false, true);

      defaultValueEntry = new TextEntry(DEFAULT_ENTRY_SIZE);

      typeBox.getComboBox().addItemListener(new ItemListener()
      {
         public void itemStateChanged(ItemEvent e)
         {
            if (null != defaultValueEntry)
            {
               components.removeAt(defaultValueEntryIndex);
               defaultValueEntry = null;
            }
            if (null != typeBox.getValue())
            {
               defaultValueEntry = (JComponent) defaultValueEntries.get(typeBox.getValue());
            }
            defaultValueEntry.setEnabled(isEnabled());
            components.add(defaultValueEntryIndex, defaultValueEntry, "Default Value:",
                  'v');
            components.pack();
            components.validate();
            components.repaint();
         }
      });

      // @todo (ub): think about the sort order of 'typeBox'
      components.add(typeBox, LABEL_TYPE, 't');

      components.add(defaultValueEntry, "Default Value:", 'v');
      defaultValueEntryIndex = components.getCount() - 1;

      components.pack();

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder("Primitive Data Properties"));
      add(components, BorderLayout.CENTER);
   }

   public void setAttributes(Map attributes, Map typeAttributes)
   {
      Type type = (Type) attributes.get(PredefinedConstants.TYPE_ATT);

      if (type == null)
      {
         type = Type.Integer;
      }

      typeBox.setValue(type);

      ((Entry) defaultValueEntry).setObjectValue(evaluator.createDefaultValue(attributes));

      typeBox.setEnabled(isEnabled());
   }

   public Map getAttributes()
   {
      Map attributes = new HashMap();

      Type type = (Type) typeBox.getValue();

      attributes.put(PredefinedConstants.TYPE_ATT, type);

      attributes.put(PredefinedConstants.DEFAULT_VALUE_ATT,
            Reflect.convertObjectToString(((Entry) defaultValueEntry).getObjectValue()));

      return attributes;
   }

   public void checkProperties()
   {
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      typeBox.setEnabled(enabled);
   }
}
