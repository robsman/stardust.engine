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
 * An interface for different entry fields. Implementors must implement
 * methods to set and get their content as an object (String, Integer, Date).
 *
 * Additionally an implementor Entry can be set mandatory and readonly.
 */
public interface Entry
{
   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent();

   /**
    * Retrieves the content of the entry as an object.
    */
   public Object getObjectValue();

   /**
    * Sets the content of the entry with providing an object.
    *
    * @throws IllegalArgumentException If the object class does not match the type
    *                                  supported by the entry.
    */
   public void setObjectValue(Object object) throws IllegalArgumentException;

   /**
    * @return <code>true</code> if the entry is enabled, <code>false</code> otherwise.
    */
   public boolean isEnabled();

   /**
    * Specifies, that the graphical appearance of the entry should reflect enabled state.
    */
   public void setEnabled(boolean isEnabled);

   /**
    * @return <code>true</code> if the entry is mandatory, <code>false</code> otherwise.
    */
   public boolean isMandatory();

   /**
    * Specifies, that the graphical appearance of the entry should reflect mandatory state.
    * Management of mandatory fields must be performed outside of the entry implementation.
    */
   public void setMandatory(boolean isMandatory);

   /**
    * @return <code>true</code> if the entry is readonly, <code>false</code> otherwise.
    */
   public boolean isReadonly();

   /**
    * Specifies, that the graphical appearance and the behavior of the entry should
    * reflect the readonly state. An entry is readonly, if its content can not be edited, but
    * read and especially used by copy and paste mechanisms.
    */
   public void setReadonly(boolean isReadonly);

   /**
    * Specifies, wether the entry is used as a table cell editor or renderer. This
    * may slightly modify its appearance (border, color) and behavior.
    */
   public void setUsedAsTableCell(boolean isCell);

   /**
    * @return <code>true</code> if the entry is empty (contains no input), <code>false</code> otherwise.
    */
   public boolean isEmpty();

}

