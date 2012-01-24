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

import javax.swing.text.Document;

import org.eclipse.stardust.common.error.InternalException;


/** class for editing integer entries */
public class PositiveIntegerEntry extends IntegerEntry
{

   /** default constructor */
   public PositiveIntegerEntry()
   {
      super();

      initialize();
   }

   /** constructor that sets the visual size of this entry field */
   public PositiveIntegerEntry(int size)
   {
      super(size);

      initialize();
   }

   /** constructor that sets the visual size and the mandatory flag of this entry
    field */
   public PositiveIntegerEntry(int size, boolean mandatory)
   {
      super(size, mandatory);

      initialize();
   }

   /** create a default model that allows only positve numbers & 0 */
   protected Document createDefaultModel()
   {
      return new NumberDocument(Integer.class);
   }

   /** setter Method a*/
   public void setValue(int value)
   {
      if (value < 0)
      {
         throw new InternalException("Value " + value + " is neither a positive integer nor 0.");
      }

      super.setValue(value);
   }
}
