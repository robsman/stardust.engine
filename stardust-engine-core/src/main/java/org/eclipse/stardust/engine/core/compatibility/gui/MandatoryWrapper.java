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
/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.*;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;


/**
 *	Wraps user defined components with styleguide compliant, preceeding
 * mandatory padding.
 */
public class MandatoryWrapper extends JPanel
{
   private JComponent wrappedComponent;
   private JLabel flagLabel;

   private boolean mandatory;

   public MandatoryWrapper(JComponent component)
   {
      this.wrappedComponent = component;

      setLayout(new BorderLayout());

      flagLabel = new JLabel(GUI.getOptionalIcon());

      add(BorderLayout.WEST, flagLabel);
      add(BorderLayout.CENTER, Box.createHorizontalStrut(3));
      add(BorderLayout.EAST, wrappedComponent);

      mandatory = false;

   }

   public void setPreferredSize(Dimension dimension)
   {
      wrappedComponent.setPreferredSize(dimension);
   }
   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent()
   {
      return wrappedComponent;
   }

   /**
    *	Check mandatory and is editable state for color settings.
    */
   protected void performFlags()
   {
      Assert.isNotNull(flagLabel);

      if (!isEnabled())
      {
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else // regular state
      {
         if (mandatory)
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
      }
      repaint();
   }

   /**
    * Specifies, that the graphical appearance of the entry should reflect enabled state.
    */
   public void setReadonly(boolean readOnly)
   {
      setEnabled(!readOnly);
      if (wrappedComponent != null)
      {
         wrappedComponent.setEnabled(!readOnly);
      }
      performFlags();
   }

   public boolean isReadonly()
   {
      return !isEnabled();
   }

   /**
    * @return <code>true</code> if the entry is mandatory, <code>false</code> otherwise.
    */
   public boolean isMandatory()
   {
      return mandatory;
   }

   /**
    * Specifies, that the graphical appearance of the entry should reflect mandatory state.
    * Management of mandatory fields must be performed outside of the entry implementation.
    */
   public void setMandatory(boolean isMandatory)
   {
      this.mandatory = isMandatory;
      performFlags();
   }

}


