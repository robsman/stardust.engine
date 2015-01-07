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

import java.awt.Frame;

/** Abstract base class for dialogs with an 'readonly' flag
 * (e.g. used for property dialogs)
 *
 * If the readonly flag is true only the close-button is shown
 * otherwise appear an ok and a cancel button.
 */
public abstract class AbstractStatefulDialog extends AbstractDialog
{
   static protected final String READ_ONLY_SUBTITLE = " (read only)";

   private boolean readOnlyState = false;

   protected AbstractStatefulDialog(Frame parent)
   {
      super(parent);
   }

   /**
    * @todo Insert the method's description here.
    * @return boolean
    */
   protected boolean isReadOnlyState()
   {
      return readOnlyState;
   }

   /**
    * @todo Insert the method's description here.
    * @param newReadonlyState boolean
    */
   protected void setReadOnlyState(boolean newReadonlyState)
   {
      if (readOnlyState != newReadonlyState)
      {
         readOnlyState = newReadonlyState;
         if (readOnlyState)
         {
            setType(CLOSE_TYPE);
         }
         else
         {
            setType(OK_CANCEL_TYPE);
         }
      }
   }

   /**
    * Sets the title of the Dialog.
    * If the readonnly flag is true the term 'readonly' appends on the title
    */
   public void setTitle(String title)
   {
      if (readOnlyState)
      {
         super.setTitle(title + READ_ONLY_SUBTITLE);
      }
      else
      {
         super.setTitle(title);
      }
   }
}
