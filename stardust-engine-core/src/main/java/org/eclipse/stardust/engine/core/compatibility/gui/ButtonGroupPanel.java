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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/** Panel for RadioButtons */
public class ButtonGroupPanel extends JPanel
{
   /** The buttons group */
   protected ButtonGroup group;
   /** The internal panel */
   protected JPanel tempPanel;

   /**
    * Constructor for creating a ButtonGroupPanel
    * @param label The Label for the panel's border
    */
   public ButtonGroupPanel(String label)
   {
      super();
      tempPanel = new JPanel();
      setBorder(new TitledBorder(new EtchedBorder(), label));
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
      group = new ButtonGroup();

      add(tempPanel);
      add(Box.createHorizontalGlue());
   }

   /**
    * Adds a button to the group
    * @param label The Label for the panel's border
    * @param state The state of the RadioButton object
    * @param mnemonic The accelerator key for the button
    */
   public JRadioButton addButton(String label, boolean state, int mnemonic)
   {
      JRadioButton button = new JRadioButton(label, state);
      button.setMnemonic(mnemonic);

      tempPanel.add(button);
      group.add(button);

      return button;
   }
}
