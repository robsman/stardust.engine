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
package org.eclipse.stardust.engine.core.model.gui;


import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.JList;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.core.compatibility.gui.IconProvider;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;

import java.awt.Font;
import java.awt.Component;

public class IdentifiableComboBoxRenderer extends BasicComboBoxRenderer
{
   private Font bold;
   private IconProvider provider;

   public IdentifiableComboBoxRenderer()
   {
   }

   public IdentifiableComboBoxRenderer(IconProvider iconProvider)
   {
      setIconProvider(iconProvider);
   }

   public Component getListCellRendererComponent(JList list, Object value, int index,
         boolean isSelected, boolean cellHasFocus)
   {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (isSelected)
      {
         setFont(bold == null ? bold = getFont().deriveFont(Font.BOLD) : bold);
      }
      setText(getText(value));
      if (provider != null &&
            (value instanceof IdentifiableElement || value instanceof ModelElement))
      {
         setIcon(provider.getIcon(value));
      }
      else
      {
         setIcon(null);
      }
      return this;
   }

   protected String getText(Object value)
   {
      if (value instanceof IdentifiableElement)
      {
         return ((IdentifiableElement) value).getName();
      }
      else if (value instanceof ModelElement)
      {
         return ((ModelElement) value).getName();
      }
      else if (value instanceof StringKey)
      {
         return ((StringKey) value).getName();
      }
      return value == null ? "" : String.valueOf(value);
   }

   public void setIconProvider(IconProvider iconProvider)
   {
      provider = iconProvider;
   }
}
