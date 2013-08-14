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
package org.eclipse.stardust.common.utils.console;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Options
{
   public static String NO_SHORTNAME = null;
   
   public static final DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");

   public static final DateFormat ISO_TIME_MINUTES = new SimpleDateFormat("HH:mm");
   public static final DateFormat ISO_TIME_SECONDS = new SimpleDateFormat("HH:mm:ss");
   public static final DateFormat ISO_TIME_MILLISECONDS = new SimpleDateFormat("HH:mm:ss:SSS");

   public static final DateFormat ISO_DATETIME_MINUTES = new SimpleDateFormat("yyyy-MM-dd HH:mm");
   public static final DateFormat ISO_DATETIME_SECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   public static final DateFormat ISO_DATETIME_MILLISECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
   
   public static final DateFormat ISO_DATETIME_T_MINUTES = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
   public static final DateFormat ISO_DATETIME_T_SECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
   public static final DateFormat ISO_DATETIME_T_MILLISECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");
   
   private TreeMap longnames = new TreeMap();
   private Map shortnames = new HashMap();
   private List rules = CollectionUtils.newList();

   public Options()
   {
   }
   
   public static Long getLongValue(Map options, String key)
   {
      Long result = null;
      
      Object rawValue = options.get(key);

      if (rawValue instanceof String)
      {
         try
         {
            rawValue = NumberFormat.getNumberInstance().parse((String) rawValue);
         }
         catch (ParseException e)
         {
            throw new PublicException("Invalid numeric argument.", e);
         }
      }

      if (rawValue instanceof Number)
      {
         result = new Long(((Number) rawValue).longValue());
      }
      
      return result;
   }

   public static List getLongValues(Map options, String key)
   {
      List result = Collections.EMPTY_LIST;

      Object rawValue = options.get(key);
      if (rawValue instanceof String)
      {
         result = new ArrayList();
         
         StringTokenizer tkr = new StringTokenizer((String) rawValue, ", ");
         while (tkr.hasMoreTokens())
         {
            String t = tkr.nextToken().trim();

            try
            {
               Number part = NumberFormat.getNumberInstance().parse(t);
               result.add(new Long(part.longValue()));
            }
            catch (ParseException e)
            {
               throw new PublicException("Illegal '" + key + "' option value: " + t
                     + " is not a long value.", e);
            }
         }
      }
      
      return result;
   }
   
   public static boolean getBooleanValue(Map options, String key)
   {
      Object rawValue = options.get(key);
      boolean result = false;
      if (rawValue instanceof String)
      {
         result = Boolean.parseBoolean((String) rawValue);

      }
      return result;
   }
   
   public static Date getDateValue(Map options, String key)
   {
      Date result = null;
      
      Object rawValue = options.get(key);

      if (rawValue instanceof String)
      {
         DateFormat[] formats = new DateFormat[] {
               DateUtils.getNoninteractiveDateFormat(), ISO_DATETIME_T_MILLISECONDS,
               ISO_DATETIME_MILLISECONDS, ISO_DATETIME_T_SECONDS, ISO_DATETIME_SECONDS,
               ISO_DATETIME_T_MINUTES, ISO_DATETIME_MINUTES, ISO_DATE};
         for (int i = 0; i < formats.length; i++ )
         {
            try
            {
               if (DateUtils.isValidISODateFormat((String) rawValue)
                     || DateUtils.isValidNonInteractiveFormat((String) rawValue))
               {
                  rawValue = formats[i].parse((String) rawValue);
               }
               break;
            }
            catch (ParseException e)
            {
               // ignore;
            }
         }
      }

      if (rawValue instanceof Date)
      {
         result = (Date) rawValue;
      }
      
      return result;
   }
   
   public void register(String longname, String shortname, String keyname, String summary,
         boolean hasArg)
   {
      Option o = new Option(longname, shortname, keyname, summary, hasArg);
      if (longnames.containsKey(longname))
      {
         throw new InternalException(
               "Option with long name '" + longname + "' already registered.");
      }
      longnames.put(longname, o);
      
      if (NO_SHORTNAME != null && !NO_SHORTNAME.equals(shortname))
      {
         if (shortnames.containsKey(shortname))
         {
            throw new InternalException("Option with short name '" + shortname
                  + "' already registered.");
         }
         shortnames.put(shortname, o);
      }
   }

   public int eat(Map options, String[] args, int i)
   {
      Option o = (Option) longnames.get(args[i]);
      if (o == null)
      {
         o = (Option) shortnames.get(args[i]);
      }
      if (o == null)
      {
         throw new IllegalUsageException("Unknown option: '" + args[i] + "'.");
      }
      if (options.containsKey(o.keyname))
      {
         throw new IllegalUsageException("Duplicate option: '" + args[i] + "'.");
      }
      if (o.hasArg)
      {
         ++i;
         if (i == args.length)
         {
            throw new IllegalUsageException(
                  "Missing argument for option: '" + args[i-1] + "'.");
         }
         options.put(o.keyname, args[i]);
      }
      else
      {
         options.put(o.keyname, Boolean.TRUE);
      }

      return i;
   }

   public Iterator getAllOptions()
   {
      return longnames.values().iterator();
   }

   public void checkRules(Map options)
   {
      for (Iterator i = rules.iterator(); i.hasNext();)
      {
         Rule rule = (Rule) i.next();
         rule.check(options);
      }
   }

   /**
    * Adds a rule that all options from a given list are mutually exclusive.
    * @param options The array of mutually exclusive options
    * @param mandatory Whether specifying one of the options is mandatory
    */
   public void addExclusionRule(String[] options, boolean mandatory)
   {
      rules.add(new ExclusionRule(options, mandatory));
   }

   public void addInclusionRule(String key, String dependent)
   {
      rules.add(new InclusionRule(key, dependent));
   }

   public void addValueRangeRule(String name, String[] values)
   {
      rules.add(new ValueRangeRule(name, values));
   }

   public void addMandatoryRule(String key)
   {
      rules.add(new MandatoryRule(key));
   }

   public class Option
   {
      private boolean hasArg;
      private String keyname;
      private String summary;
      private String longname;
      private String shortname;

      public Option(String longname, String shortname, String keyname, String summary,
            boolean hasArg)
      {
         this.longname = longname;
         this.shortname = shortname;
         this.keyname = keyname;
         this.hasArg = hasArg;
         this.summary = summary;
      }

      public boolean hasArg()
      {
         return hasArg;
      }

      public String getKeyname()
      {
         return keyname;
      }

      public String getSummary()
      {
         return summary;
      }

      public String getLongname()
      {
         return longname;
      }

      public String getShortname()
      {
         return shortname;
      }
   }

   private interface Rule
   {
      void check(Map options) throws IllegalUsageException;
   }

   private class InclusionRule implements Rule
   {
      private String key, dependent;

      public InclusionRule(String key, String dependent)
      {
         this.key = key;
         this.dependent = dependent;
      }

      public void check(Map options) throws IllegalUsageException
      {
         if (options.containsKey(key) && !options.containsKey(dependent))
         {
            throw new IllegalUsageException("The option '" + key + "' requires the option '"
                  + dependent + "' too.");
         }
      }
   }

   private class ExclusionRule implements Rule
   {
      private String[] a;
      private boolean mandatory;

      public ExclusionRule(String[] a, boolean mandatory)
      {
         this.mandatory = mandatory;
         this.a = a;
      }

      public void check(Map options) throws IllegalUsageException
      {
         String match = null;
         for (int i = 0; i < a.length; i++)
         {
            String s = a[i];
            if (options.containsKey(s))
            {
               if (match != null)
               {
                  throw new IllegalUsageException("The options '" + s + "' and '"
                           + match + "' are mutually exclusive.");
                  }
               match = s;
            }
         }
         if (mandatory && match == null)
         {
            throw new IllegalUsageException("You have to specify at least one of "
                  + "the options " + optionList());
         }
      }

      private String optionList()
      {
         StringBuffer result = new StringBuffer();
         for (int i = 0; i < a.length; i++)
         {
            result.append("'").append(a[i]).append("'");
            if (i < a.length-1)
            {
               result.append(", ");
            }
         }
         return result.toString();
      }
   }

   private class MandatoryRule implements Rule
   {
      private String key;

      public MandatoryRule(String key)
      {
         this.key = key;
      }

      public void check(Map options) throws IllegalUsageException
      {
         if (!options.containsKey(key))
         {
            throw new IllegalUsageException(
                  "You have to specify the option '" + key + "'.");
         }
      }
   }

   private class ValueRangeRule implements Rule
   {
      private String[] values;
      private String name;

      public ValueRangeRule(String name, String[] values)
      {
         this.name = name;
         this.values = values;
      }

      public void check(Map options) throws IllegalUsageException
      {
         String value = (String) options.get(name);
         if (value == null)
         {
            return;
         }
         for (int i = 0; i < values.length; i++)
         {
            if (value.equals(values[i]))
            {
               return;
            }
         }
         throw new IllegalUsageException("Option '" + name + "' has to be one of "
                  + "the values " + valueList());
      }

      private String valueList()
      {
         StringBuffer result = new StringBuffer();
         for (int i = 0; i < values.length; i++)
         {
            result.append("'").append(values[i]).append("'");
            if (i < values.length-1)
            {
               result.append(", ");
            }
         }
         return result.toString();
      }
   }
}
