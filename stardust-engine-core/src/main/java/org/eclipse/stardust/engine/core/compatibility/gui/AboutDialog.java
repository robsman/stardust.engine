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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDialog;


/**
 *
 */
public class AboutDialog extends AbstractDialog
      implements KeyListener
{
   private static AboutDialog singleton;

   private JLabel picture;
   private boolean eastereggActive = false;

   /**
    *
    */
   protected AboutDialog()
   {
      super(AbstractDialog.CLOSE_TYPE);
   }

   /**
    *
    */
   protected AboutDialog(Frame parent)
   {
      super(AbstractDialog.CLOSE_TYPE, parent);
   }

   /**
    *
    */
   public JComponent createContent()
   {
      JPanel panel = new JPanel();

      panel.setLayout(new BorderLayout());
      panel.setBorder(new EmptyBorder(10, 10, 10, 10));

      panel.add(BorderLayout.CENTER, picture = new JLabel(new ImageIcon(getClass().getResource("images/splash.gif"))));

      panel.addKeyListener(this);

      Box box = Box.createVerticalBox();

      box.add(Box.createVerticalStrut(10));
      box.add(new JLabel("CARNOT (TM) Version " + CurrentVersion.getVersionName()));
      box.add(new JLabel("Copyright (C) SunGard Systeme GmbH, " + CurrentVersion.COPYRIGHT_YEARS + ". All rights reserved.\n"));
      box.add(Box.createVerticalStrut(10));
      box.add(new JLabel("Serial Number: 6346 3726 2331 2815"));
      box.add(Box.createVerticalStrut(10));
      box.add(new JLabel("Technical Support Phone: +49 69 351 02 200"));
      box.add(new JLabel("Technical Support E-Mail: support@carnot.ag"));
      box.add(Box.createVerticalStrut(10));
      panel.add(BorderLayout.SOUTH, box);

      return panel;
   }

   public void validateSettings() throws ValidationException
   {
   }

   /**
    *
    */
   public static AboutDialog instance()
   {
      return instance(null);
   }

   /**
    *
    */
   public static AboutDialog instance(Frame parent)
   {
      if ((singleton == null)
            || (!singleton.getParent().equals(parent))
      )
      {
         singleton = new AboutDialog(parent);
      }

      return singleton;
   }

   /**
    *
    */
   public void setVisible(boolean visible)
   {
      if (eastereggActive)
      {
         picture.setIcon(new ImageIcon(getClass().getResource("images/splash.gif")));
         eastereggActive = false;
      }

      super.setVisible(visible);
   }

   /**
    *
    */
   public static void showDialog()
   {
      showDialog(null);
   }

   /**
    *
    */
   public static void showDialog(Frame parent)
   {
      showDialog("About Carnot", instance(parent));
   }

   /**
    * Invoked when a key has been typed.
    * This event occurs when a key press is followed by a key release.
    */
   public void keyTyped(KeyEvent e)
   {
   }

   /**
    * Invoked when a key has been pressed.
    */
   public void keyPressed(KeyEvent e)
   {
      if ((e.isAltDown())
            && (e.isShiftDown())
            && (e.getKeyChar() == 'C')
      )
      {
         if (picture != null)
         {
            if (eastereggActive)
            {
               picture.setIcon(new ImageIcon(getClass().getResource("images/splash.gif")));
               eastereggActive = false;
            }
            else
            {
               picture.setIcon(new ImageIcon(getClass().getResource("images/development.gif")));
               eastereggActive = true;
            }
            picture.repaint();
         }
      }
   }

   /**
    * Invoked when a key has been released.
    */
   public void keyReleased(KeyEvent e)
   {
   }
}
