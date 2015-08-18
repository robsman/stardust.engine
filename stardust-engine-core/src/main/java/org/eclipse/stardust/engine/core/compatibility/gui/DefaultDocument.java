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

/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 */
class DefaultDocument extends PlainDocument
{
   private AbstractEntry entry;

   /**
    */
   public DefaultDocument(AbstractEntry entry)
   {
      super();

      this.entry = entry;
   }

   /**
    *	Handles string insertion.
    */
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      super.insertString(offs, str, a);

      entry.performFlags();
   }

   /**
    *	Handles string insertion.
    */
   public void remove(int offs, int length)
         throws BadLocationException
   {
      super.remove(offs, length);

      entry.performFlags();
   }
}
