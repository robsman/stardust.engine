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

import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;





public abstract class AbstractPreferenceStore implements IPreferenceStore
{
   protected final static Logger trace = LogManager.getLogger(AbstractPreferenceStore.class);
   
   private IPreferenceStore parent;

   /**
    * Gets the preferences of the corresponding scope.
    * <p>Because of any performance issues, this method is only invoked once.</p>
    * @return Map with all preferences of corresponding scope
    */
   public abstract Map getPreferences();

   /**
    * Gets the next sibling element in the chain of responsibility.
    * A <code>null</code> value means that no further elements exists in the chain. 
    * @return Any parent preference store
    */
   public IPreferenceStore getParent()
   {
      return parent;
   }
   
   public void setParent(IPreferenceStore parent)
   {
      this.parent = parent;
   }

   protected String updatePreferencesValue(String name, String value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }
   
   protected Boolean updatePreferencesValue(String name, boolean value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }
   
   protected Double updatePreferencesValue(String name, double value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }
   
   protected Float updatePreferencesValue(String name, float value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }
   
   protected Integer updatePreferencesValue(String name, int value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }
   
   protected Long updatePreferencesValue(String name, long value)
   {
      throw new IllegalOperationException(
            BpmRuntimeError.PREF_PREF_STORE_READONLY.raise());
   }

   public boolean isEmpty()
   {
      return (null == getPreferences()) || getPreferences().isEmpty();
   }

   public boolean contains(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         return true;
      }
      return parent != null ? parent.contains(name) : false;
   }

   public boolean getBoolean(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         if(value instanceof Boolean)
         {
            return ((Boolean)value).booleanValue();
         }
         else if(value instanceof String)
         {
            return Boolean.valueOf((String) value).booleanValue();
         }
         return false;
      }
      return parent != null ? parent.getBoolean(name) : false;
   }

   public double getDouble(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         if(value instanceof Number)
         {
            return ((Number)value).doubleValue();
         }
         else if(value instanceof String)
         {
            try
            {
               return Double.valueOf((String) value).doubleValue();
            }
            catch(NumberFormatException e)
            {
               trace.error("unable to get value of type double for property '" + name + "'", e);
            }
         }
         return -1;
      }
      return parent != null ? parent.getDouble(name) : -1;
   }

   public float getFloat(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         if(value instanceof Number)
         {
            return ((Number)value).floatValue();
         }
         else if(value instanceof String)
         {
            try
            {
               return Float.valueOf((String) value).floatValue();
            }
            catch(NumberFormatException e)
            {
               trace.error("unable to get value of type float for property '" + name + "'", e);
            }
         }
         return -1f;
      }
      return parent != null ? parent.getFloat(name) : -1f;
   }

   public int getInt(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         if(value instanceof Number)
         {
            return ((Number)value).intValue();
         }
         else if(value instanceof String)
         {
            try
            {
               return Integer.valueOf((String) value).intValue();
            }
            catch(NumberFormatException e)
            {
               trace.error("unable to get value of type integer for property '" + name + "'", e);
            }
         }
         return -1;
      }
      return parent != null ? parent.getInt(name) : -1;
   }

   public long getLong(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         if(value instanceof Number)
         {
            return ((Number)value).longValue();
         }
         else if(value instanceof String)
         {
            try
            {
               return Long.valueOf((String) value).longValue();
            }
            catch(NumberFormatException e)
            {
               trace.error("unable to get value of type long for property '" + name + "'", e);
            }
         }
         return -1l;
      }
      return parent != null ? parent.getLong(name) : -1l;
   }

   public String getString(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         Object value = getPreferences().get(name);
         return value instanceof String ? (String)value : 
            (value != null ? value.toString() : null);
      }
      return parent != null ? parent.getString(name) : null;
   }
   
   public PreferenceScope getScope(String name)
   {
      if((null != getPreferences()) && getPreferences().containsKey(name))
      {
         return getScope();
      }
      return parent != null ? parent.getScope(name) : null;
   }

}
