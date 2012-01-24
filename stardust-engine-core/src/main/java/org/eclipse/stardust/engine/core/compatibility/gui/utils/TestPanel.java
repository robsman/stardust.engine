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

import javax.swing.BoxLayout;
import javax.swing.JDialog;

import org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner.JSpinnerTime;


/**
 * @author rsauer
 * @version $Revision$
 */
public class TestPanel extends JDialog
{
   public TestPanel()
   {
      Container content = getContentPane();

      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      content.add(new DateEntry());
      content.add(new TimeEntry());
      content.add(new DateTimeEntry());
      content.add(new JSpinnerTime());

      pack();
   }

   public static void main(String[] args)
   {
      TestPanel panel = new TestPanel();
      panel.show();
   }
}
