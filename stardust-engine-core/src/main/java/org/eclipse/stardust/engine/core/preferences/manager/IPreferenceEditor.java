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


/**
 * @author sauer
 * @version $Revision: $
 */
public interface IPreferenceEditor extends IPreferenceStore
{

   // TODO public void addPropertyChangeListener(IPropertyChangeListener listener);

   // TODO public void firePropertyChangeEvent(String name, Object oldValue, Object newValue);

   // TODO public void removePropertyChangeListener(IPropertyChangeListener listener);

   // TODO add API to fix preferences at specific scopes, i.e. enforce partition global
   // preference without users being able to override the value
   
   public void save();
   
   /**
    * Resets the current value of the preference with the given, effectively reverting to
    * the parent scope's value if existent.
    *
    * @param name the name of the preference
    */
   public void resetValue(String name);

   /**
    * Sets the current value of the double-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, double value);

   /**
    * Sets the current value of the float-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, float value);

   /**
    * Sets the current value of the integer-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, int value);

   /**
    * Sets the current value of the long-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, long value);

   /**
    * Sets the current value of the string-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, String value);

   /**
    * Sets the current value of the boolean-valued preference with the
    * given name.
    * <p>
    * A property change event is reported if the current value of the 
    * preference actually changes from its previous value. In the event
    * object, the property name is the name of the preference, and the
    * old and new values are wrapped as objects.
    * </p>
    * <p>
    * Note that the preferred way of re-initializing a preference to its
    * default value is to call <code>setToDefault</code>.
    * </p>
    *
    * @param name the name of the preference
    * @param value the new current value of the preference
    */
   public void setValue(String name, boolean value);

}
