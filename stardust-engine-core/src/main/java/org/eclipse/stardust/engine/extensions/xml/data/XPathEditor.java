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
package org.eclipse.stardust.engine.extensions.xml.data;

import java.awt.BorderLayout;

import javax.swing.JTextField;
import javax.swing.InputVerifier;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.GuiUtils;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.AccessPathEditor;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


// (fh) not actually needed, but present if future enhancements will be provided

/**
 * @author rsauer
 * @version $Revision$
 */
public class XPathEditor extends AccessPathEditor
{
   JTextField xpathEditor;

   public XPathEditor()
   {
      setLayout(new BorderLayout());
      add(xpathEditor = new JTextField(30));
   }

   public String getPath()
   {
      return xpathEditor.getText();
   }

   public void setValue(AccessPoint ap, String path, Direction direction)
   {
      xpathEditor.setText(path);
   }

   public void setInputVerifier(InputVerifier inputVerifier)
   {
      super.setInputVerifier(inputVerifier);
      xpathEditor.setInputVerifier(inputVerifier);
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      GuiUtils.setEnabled(xpathEditor, enabled);
   }
}
