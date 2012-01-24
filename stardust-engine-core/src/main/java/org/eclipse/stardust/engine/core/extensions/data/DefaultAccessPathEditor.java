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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.InputVerifier;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.compatibility.gui.ErrorDialog;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.GuiUtils;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.AccessPathEditor;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.factories.FormFactory;


public abstract class DefaultAccessPathEditor extends AccessPathEditor
{
   private JTextField field;
   private JButton button;
   private Direction direction;
   private AccessPoint accessPoint;

   public DefaultAccessPathEditor(final AccessPathEditor delegate, int size)
   {
      field = new JTextField(size);
      button = new JButton("...");
      button.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               if (AccessPathBrowserDialog.showDialog(delegate, accessPoint,
                     field.getText(), direction))
               {
                  String ap = AccessPathBrowserDialog.instance().getAccessPath();
                  field.setText(ap);
                  if (getInputVerifier() != null)
                  {
                     getInputVerifier().verify(DefaultAccessPathEditor.this);
                  }
               }
            }
            catch (Exception ex)
            {
               ErrorDialog.showDialog(DefaultAccessPathEditor.this,
                     "Unable to browse data access paths.", ex);
            }
         }
      });

      FormLayout layout = new FormLayout(new ColumnSpec[]
      {
         new ColumnSpec("default:grow"),
         FormFactory.RELATED_GAP_COLSPEC,
         FormFactory.MIN_COLSPEC
      }, new RowSpec[] {FormFactory.DEFAULT_ROWSPEC});
      setLayout(layout);
      CellConstraints cc = new CellConstraints();
      add(field, cc.xy(1, 1));
      add(button, cc.xy(3, 1));
   }

   public void setInputVerifier(InputVerifier inputVerifier)
   {
      super.setInputVerifier(inputVerifier);
      field.setInputVerifier(inputVerifier);
   }

   public String getPath()
   {
      return field.getText();
   }

   public void setValue(AccessPoint accessPoint, String accessPath,
                        Direction direction)
   {
      this.accessPoint = accessPoint;
      field.setText(accessPath);
      this.direction = direction;
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      GuiUtils.setEnabled(field, enabled);
      button.setEnabled(enabled);
   }
}
