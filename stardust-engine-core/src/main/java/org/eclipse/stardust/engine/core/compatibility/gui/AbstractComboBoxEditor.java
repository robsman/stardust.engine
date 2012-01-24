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

import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxEditor;

public abstract class AbstractComboBoxEditor implements ComboBoxEditor
{
   protected Vector actionListeners = new Vector();
   Object item;
   public void setItem(Object anObject)
   {
      this.item = anObject;
   }

   public Object getItem()
   {
      return item;
   }

   public void selectAll()
   {
   }

   public void addActionListener(ActionListener l)
   {
      actionListeners.add(l);
   }

   public void removeActionListener(ActionListener l)
   {
      actionListeners.remove(l);
   }

}
