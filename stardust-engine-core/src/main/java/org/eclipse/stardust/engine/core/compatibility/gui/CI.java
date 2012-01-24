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

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Class representing the Carnot Corporate Identity
 */
public class CI
{
   private static final Logger trace = LogManager.getLogger(CI.class);

   public final static Color BLUE = new Color(5, 74, 98);
   public final static Color RED = new Color(201, 24, 51);
   public final static Color LIGHTGREY = new Color(0.8f, 0.8f, 0.8f);
   public final static Color GREY = new Color(0.6f, 0.6f, 0.6f);

   /**
    *  Changes the DefaultValues for the UserInterface to the
    *  corporate identity values of CARNOT
    */
   public static void setCarnotUIDefaultValues()
   {
      String lookAndFeel = Parameters.instance().getString("Look.And.Feel",
            "com.jgoodies.plaf.plastic.Plastic3DLookAndFeel");
      try
      {
         UIManager.setLookAndFeel(lookAndFeel);
      }
      catch (Exception e)
      {
         trace.warn("Couldn't load look and feel '" + lookAndFeel
               + "', trying system look and feel instead.", e);
         try
         {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception e1)
         {
            throw new InternalException("Error loading Look and Feel.", e1);
         }
      }

      try
      {
         // change foreground colors of disabled (Text)Entries

         UIManager.put("TextField.inactiveForeground", UIManager.get("Label.foreground"));
         UIManager.put("TextField.inactiveBackground", UIManager.get("Label.background"));
         UIManager.put("TextField.selectionBackground", BLUE);
         UIManager.put("ComboBox.disabledForeground", UIManager.get("Label.foreground"));

         UIManager.put("ComboBox.selectionBackground", BLUE);
         UIManager.put("InternalFrame.activeTitleBackground", BLUE);
         UIManager.put("List.selectionBackground", BLUE);
         UIManager.put("MenuItem.selectionBackground", BLUE);
         UIManager.put("RadioButtonMenuItem.selectionBackground", BLUE);
         UIManager.put("CheckBoxMenuItem.selectionBackground", BLUE);
         UIManager.put("Menu.selectionBackground", BLUE);
         UIManager.put("ProgressBar.foreground", BLUE);
         UIManager.put("ProgressBar.selectionBackground", BLUE);
         UIManager.put("Table.selectionBackground", BLUE);
         UIManager.put("TextField.selectionBackground", BLUE);
         UIManager.put("PasswordField.selectionBackground", BLUE);
         UIManager.put("TextArea.selectionBackground", BLUE);
         UIManager.put("TextPane.selectionBackground", BLUE);
         UIManager.put("Tree.selectionBackground", BLUE);
         UIManager.put("Tree.selectionBorderColor", BLUE);
         UIManager.put("InternalFrame.icon", new ImageIcon(CI.class.getResource("images/carnot.gif")));

         UIDefaults defaults = UIManager.getDefaults();

         // Patch a couple of default settings, mainly font size
         for (Enumeration keys = defaults.keys(); keys.hasMoreElements();)
         {
            Object key = keys.nextElement();
            Object object = defaults.get(key);
//            System.out.println("key = " + key + ", object = " + object);
            if (FontUIResource.class.isInstance(object))
            {
               FontUIResource font = (FontUIResource) object;
               defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), 11));
            }
         }
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }

      // sets the time the tooltips stays displayed to 15 sec.
      ToolTipManager.sharedInstance().setDismissDelay(15000);
   }
}
