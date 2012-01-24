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
package org.eclipse.stardust.engine.core.preferences.manager;

import org.eclipse.stardust.engine.core.preferences.PreferenceScope;


/**
 * @author sauer
 * @version $Revision: $
 */
public interface IPreferenceStore
{

   // TODO public void addPropertyChangeListener(IPropertyChangeListener listener);

   // TODO public void firePropertyChangeEvent(String name, Object oldValue, Object newValue);

   // TODO public void removePropertyChangeListener(IPropertyChangeListener listener);
   
   /**
    * Returns whether the named preference is known to this preference
    * store.
    *
    * @param name the name of the preference
    * @return <code>true</code> if either a current value or a default
    *  value is known for the named preference, and <code>false</code> otherwise
    */
   public boolean contains(String name);

   /**
    * Indicates from which scope the preference got its effective value.
    *
    * @param name the name of the preference
    * @return
    */
   PreferenceScope getScope(String name);

   /**
    * Indicates which type of scope the implementation makes available
    *
    * @return
    */
   PreferenceScope getScope();
   
   /**
    * Returns the current value of the boolean-valued preference with the
    * given name.
    * Returns the default-default value (<code>false</code>) if there
    * is no preference with the given name, or if the current value 
    * cannot be treated as a boolean.
    *
    * @param name the name of the preference
    * @return the boolean-valued preference
    */
   public boolean getBoolean(String name);

   /**
    * Returns the current value of the double-valued preference with the
    * given name.
    * Returns the default-default value (<code>0.0</code>) if there
    * is no preference with the given name, or if the current value 
    * cannot be treated as a double.
    *
    * @param name the name of the preference
    * @return the double-valued preference
    */
   public double getDouble(String name);

   /**
    * Returns the current value of the float-valued preference with the
    * given name.
    * Returns the default-default value (<code>0.0f</code>) if there
    * is no preference with the given name, or if the current value 
    * cannot be treated as a float.
    *
    * @param name the name of the preference
    * @return the float-valued preference
    */
   public float getFloat(String name);

   /**
    * Returns the current value of the integer-valued preference with the
    * given name.
    * Returns the default-default value (<code>0</code>) if there
    * is no preference with the given name, or if the current value 
    * cannot be treated as an integter.
    *
    * @param name the name of the preference
    * @return the int-valued preference
    */
   public int getInt(String name);

   /**
    * Returns the current value of the long-valued preference with the
    * given name.
    * Returns the default-default value (<code>0L</code>) if there
    * is no preference with the given name, or if the current value 
    * cannot be treated as a long.
    *
    * @param name the name of the preference
    * @return the long-valued preference
    */
   public long getLong(String name);

   /**
    * Returns the current value of the string-valued preference with the
    * given name.
    * Returns the default-default value (the empty string <code>""</code>)
    * if there is no preference with the given name, or if the current value 
    * cannot be treated as a string.
    *
    * @param name the name of the preference
    * @return the string-valued preference
    */
   public String getString(String name);

}
