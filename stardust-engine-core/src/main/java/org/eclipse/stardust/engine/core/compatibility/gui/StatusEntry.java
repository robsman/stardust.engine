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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 *
 */
public class StatusEntry extends TextEntry
      implements ActionListener
{
   static protected final int DEFAULT_EXPIRE_TIME = 10000;

   private Timer resetTimer;
   private int expireTime = DEFAULT_EXPIRE_TIME;
   private boolean automaticResetOn = false;

   /**
    * Creates an entry with the specified count of columns and without
    * an automatically reset
    */
   public StatusEntry(int columns)
   {
      this(columns, false);
   }

   /**
    * Creates an entry with the specified count of columns.
    * If <code>useAutomaticReset</code> is <code>true</code> the content of the entry
    * will be reset after the expire time.
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    */
   public StatusEntry(int columns, boolean useAutomaticReset)
   {
      super(columns);

      setAutomaticResetOn(useAutomaticReset);

      setReadonly(true);
      performFlags();
   }

   /**
    * Invoked when an action occurs.
    */
   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == resetTimer)
      {
         setValue(null);
      }
   }

   /**
    *
    */
   public boolean isFocusTraversable()
   {
      return false;
   }

   /**
    * Returns <code>true</code> if the entry has a automatic content reset.
    *
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    */
   public boolean isAutomaticResetOn()
   {
      return this.automaticResetOn;
   }

   /**
    * If <code>useAutomaticReset</code> is <code>true</code> the entry use
    * a automatic content reset.
    *
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    */
   public void setAutomaticResetOn(boolean useAutomaticReset)
   {
      if (automaticResetOn != useAutomaticReset)
      {
         this.automaticResetOn = useAutomaticReset;
         if (automaticResetOn)
         {
            resetTimer = new Timer(getExpireTime(), this);
            resetTimer.start();
         }
         else
         {
            resetTimer = null;
         }
      }
   }

   /**
    * Returns the time (in milliseconds) after that the content will be reset
    *
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    */
   public int getExpireTime()
   {
      return expireTime;
   }

   /**
    * Sets the time (in milliseconds) after that the content will be reset
    *
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    */
   public void setExpireTime(int expireTime)
   {
      if (expireTime < 0)
      {
         throw new IllegalArgumentException("The expire time for a status entry can't be negative!");
      }

      this.expireTime = expireTime;
   }

   /**
    * Sets the content of the entry to <code>text</code>. If <code>text</code> is
    * <code>null</code>, the entry will be empty.
    * If the flag for automatically reset is set the timer will be restarted.
    *
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#getExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setExpireTime
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#isAutomaticResetOn
    * @see org.eclipse.stardust.engine.core.compatibility.gui.StatusEntry#setAutomaticResetOn
    *
    */
   public void setValue(String text)
   {
      if (!isEnabled())
      {
         return;
      }

      super.setValue(text);

      if (isAutomaticResetOn())
      {
         resetTimer.restart();
      }
   }

}
