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
package org.eclipse.stardust.engine.extensions.web.jsp.contexts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;

import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.gui.LabeledComponentsPanel;
import org.eclipse.stardust.engine.core.compatibility.gui.TextEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationContextPropertiesPanel;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JSPContextTypePanel extends ApplicationContextPropertiesPanel implements ActionListener
{
   private TextEntry urlEntry;
   private JButton testURLButton;

   public JSPContextTypePanel()
   {
      initComponents();
   }

   private void initComponents()
   {
      LabeledComponentsPanel components;

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(GUI.getTitledPanelBorder("Java Server Page"));

      components = new LabeledComponentsPanel();
      components.add(
            new JComponent[]
            {
               urlEntry = new TextEntry(30),
               testURLButton = new JButton("Test")
            },
            new String[]{"JSP URL:", ""},
            new int[]{'u', 't'});

      urlEntry.setMandatory(true);
      testURLButton.addActionListener(this);
      components.pack();

      add(components);
   }

   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == testURLButton)
      {
         try
         {
            URL url = new URL(urlEntry.getValue());

            url.openStream();

            JOptionPane.showMessageDialog(this,
                  "The URL exists!", "Close", JOptionPane.INFORMATION_MESSAGE);
         }
         catch (Exception x)
         {
            JOptionPane.showMessageDialog(this,
                  "The URL cannot be resolved!", "Close", JOptionPane.ERROR_MESSAGE);
         }
      }

   }

   public Collection getAccessPoints()
   {
      return Collections.EMPTY_LIST;
   }

   public void setData(Map attributes, Iterator accessPoints)
   {
      String htmlPath = (String) attributes.get(PredefinedConstants.HTML_PATH_ATT);
      urlEntry.setText(htmlPath);
   }

   public Map getAttributes()
   {
      Map attributes = new HashMap();
      attributes.put(PredefinedConstants.HTML_PATH_ATT, urlEntry.getText());
      return attributes;
   }

   public void reset()
   {
      urlEntry.setText(null);
   }

   public void createAccessPoints(IApplicationContext context)
   {
   }

   public void validatePanel()
   {
    // @todo (france, ub): 
   }
}
