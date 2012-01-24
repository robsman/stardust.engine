/*
 * $Id$
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.stardust.engine.core.compatibility.gui.utils.Mandatory;


public class FormLayoutBuilder
{
   private static final Insets DEFAULT_SPACING = new Insets(5, 0, 5, 0);
   private JPanel panel;
   private GridBagLayout gridbag;
   private GridBagConstraints c;

   public FormLayoutBuilder(JPanel panel)
   {
      this.panel = panel;
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
      gridbag = new GridBagLayout();
      c = new GridBagConstraints();
      panel.setLayout(gridbag);
      c.fill = GridBagConstraints.BOTH;
   }

   public void addLabeledTextRow(String label, JTextField textField, Mandatory mandatory)
   {
      c.weightx = 0.0;
      c.insets = DEFAULT_SPACING;
      c.gridwidth = 1;
      JLabel l = new JLabel(label);
      gridbag.setConstraints(l, c);
      panel.add(l);
      
      gridbag.setConstraints(mandatory, c);
      panel.add(mandatory);
      
      c.weightx = 1.0;
      c.gridwidth = GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textField, c);
      panel.add(textField);
   }
}
