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
package org.eclipse.stardust.engine.core.compatibility.gui.utils;

import java.awt.Container;
import java.awt.Component;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.eclipse.stardust.common.error.ValidationException;


public class GuiUtils
{
   private GuiUtils()
   {
   }

   public static void setEnabled(Component component, boolean enabled)
   {
      while (component instanceof JScrollPane)
      {
         component.setEnabled(enabled);
         component = ((JScrollPane) component).getViewport().getView();
      }
      if (component instanceof JPanel || component instanceof Box)
      {
         for (int i = 0; i < ((Container) component).getComponentCount(); i++)
         {
            setEnabled(((Container) component).getComponent(i), enabled);
         }
      }
      if (!(component instanceof JLabel) || component instanceof Mandatory)
      {
         if (component instanceof JTextComponent)
         {
            component.setBackground(UIManager.getColor(enabled ? "TextField.background" : "TextField.inactiveBackground"));
         }
         component.setEnabled(enabled);
      }
   }

   public static void validateSettings(Container container) throws ValidationException
   {
      validateSettings(container, false);
   }
   
   public static void validateSettings(Container container, boolean recursive)
         throws ValidationException
   {
      for (int i = 0; i < container.getComponentCount(); i++)
      {
         Component component = container.getComponent(i);
         if (component instanceof Mandatory)
         {
            ((Mandatory) component).checkMandatory();
         }
         else if (recursive && (component instanceof Container))
         {
            validateSettings((Container) component, true);
         }
      }
   }
}
