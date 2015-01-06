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

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/** ToolbarButton copies the appearance of Explorer & Netscape Buttons*/
public class ToolbarButton extends JButton
{
   // currently these attributes are not really needed but for future use
   private Icon atStart = null;
   private Icon atRollover = null;
   private Icon atClick = null;

   /** standard constructor without anything */
   public ToolbarButton()
   {
      this(null, null, null, null);
   }

   /** standard constructor without icons but with text */
   public ToolbarButton(String text)
   {
      this(null, null, null, text);
   }

   /** constructor for button showing image */
   public ToolbarButton(ImageIcon atStart)
   {
      this(atStart, atStart, atStart, null);
   }

   /** constructor for button showing image<P>changes in case of rollover */
   public ToolbarButton(ImageIcon atStart,
         ImageIcon atRollover)
   {
      this(atStart, atRollover, atRollover, null);
   }

   /** constructor that allows to set all three possible icons */
   public ToolbarButton(ImageIcon atStart,
         ImageIcon atRollover,
         ImageIcon atClick)
   {
      this(atStart, atRollover, atClick, null);
   }

   /** constructor that allows to set all three possible icons + text <p>
    text is shown below the ImageIcon by default */
   public ToolbarButton(ImageIcon atStart,
         ImageIcon atRollover,
         ImageIcon atClick,
         String text)
   {
      super(atStart);
      this.atStart = atStart;
      this.atRollover = atRollover;
      this.atClick = atClick;

      // set the rollover icon

      if (atRollover != null)
      {
         setRolloverIcon(atRollover);
      }

      // set the buttonPressed icon

      if (atClick != null)
      {
         setPressedIcon(atClick);
      }

      // text adding

      setVerticalTextPosition(SwingConstants.BOTTOM);

      if (text != null)
      {
         setText(text);
      }

      // Hide the buttons's border

      setBorderPainted(false);

      //setBorder(new BevelBorder(BevelBorder.RAISED, Color.white, Color.black, null, null));
      setMargin(new Insets(4, 4, 4, 4));

      // add a listener for drawing the border

      addMouseListener(new MouseAdapter()
      {
         // Display the buttons's border if enabled

         public void mouseEntered(MouseEvent mouseEvent)
         {

            if (!isEnabled())
            {
               return;
            }

            setBorderPainted(true);
         }

         // Hide the button's border if it has been shown

         public void mouseExited(MouseEvent mouseEvent)
         {
            if (!isBorderPainted())
            {
               return;
            }

            setBorderPainted(false);
         }
      });
   }

   /** set the standard icon */
   public void setIcon(Icon icon)
   {
      atStart = icon;
      super.setIcon(atStart);
   }

   /** set the rollover icon */
   public void setRolloverIcon(Icon icon)
   {
      atRollover = icon;
      super.setRolloverIcon(atRollover);
      if (atRollover != null)
      {
         setRolloverEnabled(true);
      }
      else // no icon to show
      {
         setRolloverEnabled(false);
      }
   }

   /** set the pressed state icon */
   public void setPressedIcon(Icon icon)
   {
      atClick = icon;
      super.setPressedIcon(atClick);
   }
}// ToolbarButton
