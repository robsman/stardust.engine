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
package org.eclipse.stardust.engine.core.extensions.data;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDialog;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.AccessPathEditor;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


public class AccessPathBrowserDialog extends AbstractDialog
{
   private static AccessPathBrowserDialog instance;
   private JPanel panel;
   private AccessPathEditor delegate;

   public static boolean showDialog(AccessPathEditor delegate, AccessPoint accessPoint,
         String accessPath, Direction direction)
   {
      instance().setEditor(instance().delegate = delegate);
      delegate.setValue(accessPoint, accessPath, direction);
      return showDialog("Dereference Path Browser", instance());
   }

   protected JComponent createContent()
   {
      return panel = new JPanel(new BorderLayout());
   }

   public void validateSettings() throws ValidationException
   {
   }

   private void setEditor(AccessPathEditor delegate)
   {
      panel.removeAll();
      panel.add(delegate);
   }

   public static AccessPathBrowserDialog instance()
   {
      if (instance == null)
      {
         instance = new AccessPathBrowserDialog();
      }
      return instance;
   }

   public String getAccessPath()
   {
      return delegate.getPath();
   }
}
