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
import javax.swing.table.DefaultTableCellRenderer;

import org.eclipse.stardust.common.StringKey;


/**
 * Enabled rendering of <code>StringKey</code> derived values in a <code>JTable</code>
 * cell.
 *
 * @author rsauer
 * @version $Revision$
 */
public class StringKeyTableCellRenderer extends DefaultTableCellRenderer
{
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus,
         int row, int column)
   {
      String content = "";
      if (value != null)
      {
         content = ((StringKey) value).getName();
      }

      return super.getTableCellRendererComponent(table, content, isSelected, hasFocus,
            row, column);
   }
}
