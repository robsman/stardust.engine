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
import java.awt.event.ActionEvent;

import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * class implements an tri-state-button
 * these states are
 *		- selected
 *		- unselected
 *		- parts selected
 * an action-performed event changes the button state
 */
public class TriStateCheckBox extends JButton
{
   private static final Logger trace = LogManager.getLogger(TriStateCheckBox.class);

   static public final int UN_SELECTED = 0;
   static public final int SELECTED = 1;
   static public final int PARTS_SELECTED = 2;

   private Icon[] icons = new Icon[3];
   private int value;
   private boolean thirdStateEnabled = true;

   protected class TriStateModel extends DefaultButtonModel
   {

      protected void fireActionPerformed(ActionEvent e)
      {
         handleActionPerformed(e);
         super.fireActionPerformed(e);
      }
   }

   /**
    * TriStateBox constructor comment.
    */
   public TriStateCheckBox()
   {
      this(null);
   }

   /**
    * TriStateBox constructor comment.
    * @param text java.lang.String
    */
   public TriStateCheckBox(String text)
   {
      super();

      setModel(new TriStateModel());
      setText(text);
      setMargin(new Insets(0, 0, 0, 0));
      setBorderPainted(false);
      setFocusPainted(false);

      icons[SELECTED] = new ImageIcon(TriStateCheckBox.class.getResource("images/selected.gif"));
      icons[UN_SELECTED] = new ImageIcon(TriStateCheckBox.class.getResource("images/unselected.gif"));
      icons[PARTS_SELECTED] = new ImageIcon(TriStateCheckBox.class.getResource("images/parts_selected.gif"));

      setValue(UN_SELECTED);
      setThirdStateEnabled(true);

   }

   /**
    * @todo Insert the method's description here.
    * @return javax.swing.Icon
    */
   public Icon getPartsSelectedIcon()
   {
      return icons[PARTS_SELECTED];
   }

   /**
    * @todo Insert the method's description here.
    * @return javax.swing.Icon
    */
   public Icon getSelectedIcon()
   {
      return icons[SELECTED];
   }

   /**
    * @todo Insert the method's description here.
    * @return javax.swing.Icon
    */
   public Icon getUnselectedIcon()
   {
      return icons[UN_SELECTED];
   }

   /**
    * @todo Insert the method's description here.
    * @return int
    */
   public int getValue()
   {
      return value;
   }

   /**
    * @todo Insert the method's description here.
    */
   public void handleActionPerformed(ActionEvent e)
   {
      if (e.getID() == ActionEvent.ACTION_PERFORMED)
      {
         switch (getValue())
         {
            case UN_SELECTED:
               {
                  if (isThirdStateEnabled())
                  {
                     setValue(PARTS_SELECTED);
                  }
                  else
                  {
                     setValue(SELECTED);
                  }
                  break;
               }
            case PARTS_SELECTED:
               {
                  setValue(SELECTED);
                  break;
               }
            case SELECTED:
               {
                  setValue(UN_SELECTED);
                  break;
               }
            default:
               {
                  Assert.lineNeverReached();
               }
         }
      }
   }

   /**
    * @todo Insert the method's description here.
    * @return boolean
    */
   public boolean isThirdStateEnabled()
   {
      return thirdStateEnabled;
   }

   /**
    * @todo Insert the method's description here.
    * @param newPartsSelectedIcon javax.swing.Icon
    */
   public void setPartsSelectedIcon(Icon newPartsSelectedIcon)
   {
      icons[PARTS_SELECTED] = newPartsSelectedIcon;
   }

   /**
    * @todo Insert the method's description here.
    * @param newSelectedIcon javax.swing.Icon
    */
   public void setSelectedIcon(Icon newSelectedIcon)
   {
      icons[SELECTED] = newSelectedIcon;
   }

   /**
    * @todo Insert the method's description here.
    * @param newThirdStateEnabled boolean
    */
   public void setThirdStateEnabled(boolean newThirdStateEnabled)
   {
      thirdStateEnabled = newThirdStateEnabled;
      if ((newThirdStateEnabled == false)
            && (getValue() == PARTS_SELECTED)
      )
      {
         trace.debug( "[Warning] TriStateCheckBox.setThirdSateEnabled(false) contradict the current state.");
      }
   }

   /**
    * @todo Insert the method's description here.
    * @param newUnselectedIcon javax.swing.Icon
    */
   public void setUnselectedIcon(Icon newUnselectedIcon)
   {
      icons[UN_SELECTED] = newUnselectedIcon;
   }

   /**
    * @todo Insert the method's description here.
    * @param newValue int
    */
   public void setValue(int newValue)
   {
      switch (newValue)
      {
         case SELECTED:
            {
               setIcon(getSelectedIcon());
               break;
            }
         case UN_SELECTED:
            {
               setIcon(getUnselectedIcon());
               break;
            }
         case PARTS_SELECTED:
            {
               Assert.condition(isThirdStateEnabled(), "the third state is enabled");
               setIcon(getPartsSelectedIcon());
               break;
            }
         default:
            {
               throw new IllegalArgumentException("illegal value for state");
            }
      }
      value = newValue;
   }
}
