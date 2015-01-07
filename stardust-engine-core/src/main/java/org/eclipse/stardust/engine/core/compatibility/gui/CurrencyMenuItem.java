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

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.ToolTipManager;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Money;


/**
 * Extension of JMenuItem -> tooltips are on by default here
 */
class CurrencyMenuItem extends JMenuItem
{
   public final static ToolTipManager toolTipManager = ToolTipManager.sharedInstance();

   /**
    *
    */
   public CurrencyMenuItem(String currencyItem)
   {
      super(currencyItem);

      Assert.isNotNull(currencyItem);

      toolTipManager.registerComponent(this);
   }

   /**
    *
    */
   public String getToolTipText(MouseEvent event)
   {
      return Money.getFullName(
            ((JMenuItem) getComponentAt(event.getPoint())).getText());
   }

   /**
    *	Unregister from ToolTipManager at cleanup time.
    */
   protected void finalize() throws Throwable
   {
      toolTipManager.unregisterComponent(this);

      super.finalize();
   }
}

