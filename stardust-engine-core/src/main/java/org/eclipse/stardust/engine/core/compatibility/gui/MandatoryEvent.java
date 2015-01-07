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

import javax.swing.JComponent;

/**
 * Mandatory Event
 */
public class MandatoryEvent
{
   public static final int DEFAULT = 0;

   private JComponent component;
   private String text;
   private String type;
   private int id;

   /**
    * Constructor that sets the entry field, the name, the id and type of entry
    * field.
    */
   public MandatoryEvent(JComponent component, String text, String type, int id)
   {
      this.component = component;
      this.text = text;
      this.id = id;
      this.type = type;
   }

   /**
    *
    */
   public JComponent getComponent()
   {
      return component;
   }

   /**
    *
    */
   public String getText()
   {
      return text;
   }

   /**
    *
    */
   public int getId()
   {
      return id;
   }

   /**
    *
    */
   public String getType()
   {
      return type;
   }
}
