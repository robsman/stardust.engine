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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;

import javax.swing.JComponent;

/** handles updates etc*/
public class TableEntryFocusListener implements FocusListener
{
   Method setterMethod;
   Object object;

   /** */
   public TableEntryFocusListener(Object object, Method setterMethod)
   {

      this.setterMethod = setterMethod;

      this.object = object;

   }

   /** does nothing if focus is gained */
   public void focusGained(FocusEvent e)
   {

   }

   /** updates the last value */
   public void focusLost(FocusEvent e)
   {
      Object component = e.getComponent();

      // Check if component is enabled otherwise no setter method needed

      if (!((JComponent) component).isEnabled())
      {
         return;
      }

      try
      {
         Object arguments[] = new Object[]{((Entry) component).getObjectValue()};

         setterMethod.invoke(object, arguments);

      }
      catch (Exception ex)	// could not set a value <<< should never be reached
      {
      }
   }
}

