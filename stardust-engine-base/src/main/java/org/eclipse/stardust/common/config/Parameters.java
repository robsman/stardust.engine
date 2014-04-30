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

package org.eclipse.stardust.common.config;


import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.BaseErrorCase;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * The JDK has various mechanisms to obtain environmental parameters such as system
 * properties, resources etc. which are confusing to use. This class provides a homogenous
 * mechanism to access all this information. <p/> It is possible to store additional
 * properties sets, being considered as a "chain of responsibilities" in a "last in-first
 * considered" manner. <p/> Parameters are key/value-pairs. The name may be a list of
 * identifiers separated by ".". The identifiers are considered case-insensitive.
 *
 * @version $Revision$
 */
public abstract class Parameters
{
   private static final String CARNOT_CURRENT_VERSION = "CARNOT version: " + CurrentVersion.getBuildVersionName();
   private static final String[] TRUE_WORDS = {"true", "enabled", "on"};
   private static final String[] FALSE_WORDS = {"false", "disabled", "off"};

   private static final Logger trace = LogManager.getLogger(Parameters.class);

   /**
    * Used to distinguish between nonexisting properties and properties having a value of
    * <code>null</code>.
    */
   static final Object NULL_VALUE = new Object();

   private static ThreadLocal<Parameters> singleton = new ThreadLocal<Parameters>();

   private static String defaultPropertiesBaseName = "carnot";

   /**
    * There must be a a file "<baseName>.properties" in one of the directories provided
    * with the classpath. This file will be used as the default properties file when
    * calling <code>instance()</code>.
    *
    * @param baseName
    *           the name of the file storing your properties (e.g. "carnot" will render
    *           "carnot.properties")
    */
   public static synchronized void setDefaultProperties(String baseName)
   {
      defaultPropertiesBaseName = baseName;
   }

   public static synchronized String getDefaultProperties()
   {
      return defaultPropertiesBaseName;
   }

   /**
    * @return a singleton object of this class
    */
   public static Parameters instance()
   {
      Parameters parameters = singleton.get();
      if (parameters == null)
      {
         parameters = new ParametersFacade();
         singleton.set(parameters);
         if (trace.isInfoEnabled())
         {
            trace.info(CARNOT_CURRENT_VERSION);
         }
      }
      return parameters;
   }

   /**
    *
    */
   public abstract Object get(String name);

   public <T> T getObject(String name)
   {
      return (T) get(name);
   }

   public <T> T getObject(String name, T defaultValue)
   {
      T value = (T) get(name);
      return value == null ? defaultValue : value;
   }

   /**
    * Gets the value of the property.
    *
    * @param name
    *           the name of the property
    * @return a string representing of the property value if the property is found or
    *         <code>null</code>.
    * @see #getString(String, String)
    */
   public final String getString(String name)
   {
      Object value = get(name);
      return value == null ? null : value.toString().trim();
   }

   /**
    * Gets the value of the property.
    *
    * @param name
    *           the name of the property
    * @param defaultValue
    *           the default value of the property
    * @return a string representing the property value if the property is found or the
    *         default value passed to the method.
    * @see #getString(String)
    */
   public final String getString(String name, String defaultValue)
   {
      String result = getString(name);
      return StringUtils.isEmpty(result) ? defaultValue : result;
   }

   /**
    * Gets the value of the property.
    *
    * @param name
    *           the name of the property
    * @param defaultValue
    *           the default value of the property
    * @return an int representing the property value if the property is found or the
    *         default value passed to the method.
    */
   public final int getInteger(String name, int defaultValue)
   {
      Object value = get(name);

      if (value == null)
      {
         return defaultValue;
      }

      if (value instanceof Integer)
      {
         return ((Integer) value).intValue();
      }

      if (value instanceof Long)
      {
         return ((Long) value).intValue();
      }

      if (value instanceof Short)
      {
         return ((Short) value).intValue();
      }

      if (value instanceof Byte)
      {
         return ((Byte) value).intValue();
      }

      String sValue = value.toString().trim();

      if (StringUtils.isEmpty(sValue))
      {
         return defaultValue;
      }

      try
      {
         return Integer.parseInt(sValue);
      }
      catch (NumberFormatException x)
      {
         throw new PublicException(
               BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER.raise(
                     value, name));
      }
   }

   /**
    *
    */
   public final long getLong(String name, long defaultValue)
   {
      Object value = get(name);

      if (value == null)
      {
         return defaultValue;
      }

      if (value instanceof Integer)
      {
         return ((Integer) value).longValue();
      }

      if (value instanceof Long)
      {
         return ((Long) value).longValue();
      }

      if (value instanceof Short)
      {
         return ((Short) value).longValue();
      }

      if (value instanceof Byte)
      {
         return ((Byte) value).longValue();
      }

      String sValue = value.toString().trim();

      if (StringUtils.isEmpty(sValue))
      {
         return defaultValue;
      }

      try
      {
         return (new Long(sValue)).longValue();
      }
      catch (NumberFormatException x)
      {
         throw new PublicException(
               BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_INTEGER.raise(
                     value, name));
      }
   }

   /**
    * Gets the value of the property.
    *
    * @param name
    *           the name of the property
    * @param defaultValue
    *           the default value of the property
    * @return an int representing the property value if the property is found or the
    *         default value passed to the method.
    */
   public double getDouble(String name, double defaultValue)
   {
      String value = getString(name);

      if (value == null)
      {
         return defaultValue;
      }

      value = value.trim();

      if (StringUtils.isEmpty(value))
      {
         return defaultValue;
      }

      try
      {
         return Double.parseDouble(value);
      }
      catch (NumberFormatException x)
      {
         throw new PublicException(
               BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_DOUBLE.raise(
                     value, name));
      }
   }

   /**
    *
    */
   public final boolean getBoolean(String name, boolean defaultValue)
   {
      Object obj = get(name);
      if (obj == null)
      {
         return defaultValue;
      }

      if (obj instanceof Boolean)
      {
         return ((Boolean) obj).booleanValue();
      }

      String value = obj.toString().toLowerCase().trim();

      if (StringUtils.isEmpty(value))
      {
         return defaultValue;
      }

      for (int i = 0; i < TRUE_WORDS.length; ++i)
      {
         if (TRUE_WORDS[i].equals(value))
         {
            return true;
         }
      }

      for (int i = 0; i < FALSE_WORDS.length; ++i)
      {
         if (FALSE_WORDS[i].equals(value))
         {
            return false;
         }
      }

      throw new PublicException(
            BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_TRUE_OR_FALSE
                  .raise(value, name));
   }

   /**
    * We expect the format <code>yyyy-mm-dd</code>.
    */
   public final Calendar getDate(String name, Calendar defaultValue)
   {
      Calendar value = getDate(name);

      if (value == null)
      {
         return defaultValue;
      }

      return value;
   }

   /**
    * We expect the format <code>yyyy-mm-dd</code>.
    */
   public final Calendar getDate(String name)
   {
      Object value = get(name);

      if (value == null)
      {
         return null;
      }

      if (value instanceof Calendar)
      {
         return (Calendar) value;
      }

      String sValue = value.toString().trim();

      if (StringUtils.isEmpty(sValue))
      {
         return null;
      }

      try
      {
         int year = Integer.parseInt(sValue.substring(0, 4));
         int month = Integer.parseInt(sValue.substring(5, 7));
         int day = Integer.parseInt(sValue.substring(8));

         Calendar result = Calendar.getInstance();

         result.set(year, month - 1, day);

         return result;
      }
      catch (Exception x)
      {
         throw new PublicException(
               BaseErrorCase.BASE_ENTRY_FOR_PROPERTY_CANNOT_BE_MAPPED_TO_VALID_DATE
                     .raise(value, name));
      }
   }

   public final List<String> getStrings(String name)
   {
      return getStrings(name, ",");
   }

   /**
    * Returns a list of values. The values have to be comma separated in the property
    * file.
    *
    * @return a LinkedList of the values. If no value is found, an empty list is returned.
    */
   public final List<String> getStrings(String name, String separators)
   {
      LinkedList<String> result = new LinkedList<String>();
      String str = getString(name);

      if (str == null)
      {
         return result;
      }
      else
      {
         StringTokenizer tkr = new StringTokenizer(str, separators);
         while (tkr.hasMoreTokens())
         {
            result.add(tkr.nextToken().trim());
         }
         return result;
      }
   }

   /**
    *
    * @param name
    * @param value
    */
   public abstract void set(String name, Object value);

   /**
    *
    */
   public final void setString(String name, String value)
   {
      set(name, value);
   }

   /**
    *
    */
   public final void setInteger(String name, int value)
   {
      set(name, new Integer(value));
   }

   public final void setBoolean(String name, boolean value)
   {
      set(name, value ? Boolean.TRUE : Boolean.FALSE);
   }

   /**
    * Resets the cached configuration, effectively enforcing a configuration reload when
    * properties are retrieved the next time.
    */
   public abstract void flush();

   /**
    * There must be a a file "<fileName>.properties" in one of the directories provided
    * with the classpath. This file will be used as the properties file.
    *
    * @param fileName
    *           the name of the file storing your properties (e.g. "carnot" will render
    *           "carnot.properties")
    */
   public abstract void addProperties(String fileName);

   public interface IDisposable
   {
      void dispose();
   }

}
