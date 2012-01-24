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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.stardust.common.config.Parameters;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 */
public class ParametersTest extends TestCase
{

   /**
    * String properties
    */
   private static final String STRING_SMALL = "Test.Properties.String.Small";
   private static final String STRING_SMALL_VALUE = "a";

   private static final String STRING_LARGE = "Test.Properties.String.Large";
   private static final String STRING_LARGE_VALUE = "This is a larger string";

   private static final String STRING_EMPTY = "Test.Properties.String.Empty"; 

   /**
    * Integer properties
    */
   private static final String INTEGER_POSITIVE = 
         "Test.Properties.Integer.Positive";
   private static final int INTEGER_POSITIVE_VALUE = 42;

   private static final String INTEGER_NEGATIVE = 
         "Test.Properties.Integer.Negative";
   private static final int INTEGER_NEGATIVE_VALUE = -42;

   /**
    * Long properties
    */
   private static final String LONG_POSITIVE = "Test.Properties.Long.Positive";
   private static final long LONG_POSITIVE_VALUE = 1214;

   private static final String LONG_NEGATIVE = "Test.Properties.Long.Negative";
   private static final long LONG_NEGATIVE_VALUE = -3876;
         
   /**
    * Boolean properties
    */
   private static final String BOOLEAN_TRUE_1 = 
         "Test.Properties.Boolean.True.1";

   private static final String BOOLEAN_TRUE_2 = 
         "Test.Properties.Boolean.True.2";

   private static final String BOOLEAN_TRUE_3 = 
         "Test.Properties.Boolean.True.3";

   private static final String BOOLEAN_ENABLED_1 = 
         "Test.Properties.Boolean.Enabled.1";

   private static final String BOOLEAN_ENABLED_2 = 
         "Test.Properties.Boolean.Enabled.2";

   private static final String BOOLEAN_ENABLED_3 = 
         "Test.Properties.Boolean.Enabled.3";

   private static final String BOOLEAN_ON_1 = 
         "Test.Properties.Boolean.On.1";

   private static final String BOOLEAN_ON_2 = 
         "Test.Properties.Boolean.On.2";

   private static final String BOOLEAN_FALSE_1 = 
         "Test.Properties.Boolean.False.1";

   private static final String  BOOLEAN_FALSE_2 = 
         "Test.Properties.Boolean.False.2";

   private static final String  BOOLEAN_FALSE_3 = 
         "Test.Properties.Boolean.False.3";

   private static final String BOOLEAN_DISABLED_1 = 
         "Test.Properties.Boolean.Disabled.1";

   private static final String BOOLEAN_DISABLED_2 = 
         "Test.Properties.Boolean.Disabled.2";

   private static final String BOOLEAN_DISABLED_3 = 
         "Test.Properties.Boolean.Disabled.3";

   private static final String BOOLEAN_OFF_1 = 
         "Test.Properties.Boolean.Off.1";

   private static final String BOOLEAN_OFF_2 = 
         "Test.Properties.Boolean.Off.2";

   /**
    * Date properties
    */
   private static final String DATE_VALID = "Test.Properties.Date.Valid";
   private Calendar DATE_VALUE;

   private static final String DATE_INVALID = "Test.Properties.Date.Invalid";

   /**
    * non existing properties
    */
   private static final String STRING_NON_EXISTENT = 
         "Test.Properties.String.NonExistent";

   private static final String STRING_NON_EXISTENT_DEFAULT = "NonExistDefault"; 

   private static final String INTEGER_NON_EXISTENT = 
         "Test.Properties.Integer.NonExistent";

   private static final int INTEGER_NON_EXISTENT_DEFAULT = 42;

   private static final String LONG_NON_EXISTENT = 
         "Test.Properties.Long.NonExistent";

   private static final long LONG_NON_EXISTENT_DEFAULT = 12345;
 
   private static final String BOOLEAN_NON_EXISTENT = 
         "Test.Properties.Boolean.NonExistent";

   private static final String DATE_NON_EXISTENT = 
         "Test.Properties.Date.NonExistent";

   private static final boolean BOOLEAN_NON_EXISTENT_DEFAULT = true;

   /**
    * instance members
    */
   private SimpleDateFormat format = null;

   /**
    *
    */
   public static TestSuite suite()
   {
      return new TestSuite(ParametersTest.class);
   }

   /**
    *
    */
   public ParametersTest(String name)
   {
      super(name);
   }

   /**
    *
    */
   public void setUp() throws Exception
   {
      super.setUp();

      DATE_VALUE = Calendar.getInstance();
      DATE_VALUE.set(2000, 11, 21);

      try
      {
         Parameters.instance().addProperties("org.eclipse.stardust.common.config.test-parameters");
      }
      catch(Exception e)
      {
         fail(e.getMessage());
      }
      
      format = new SimpleDateFormat("yyyy-MM-dd");
   }

   public void testStringPropertiesFile()
   {
      assertEquals(
            "The property '" + STRING_SMALL + "' have to have the value '"
            + STRING_SMALL_VALUE +"'", 
            STRING_SMALL_VALUE, Parameters.instance().getString(STRING_SMALL));

      assertEquals(
            "The property '" + STRING_LARGE + "' have to have the value '"
            + STRING_LARGE_VALUE +"'", 
            STRING_LARGE_VALUE, Parameters.instance().getString(STRING_LARGE));

      assertEquals(
            "The property '" + STRING_EMPTY 
            + "' have to have the value 'null'", 
            "", Parameters.instance().getString(STRING_EMPTY));
   }

   /**
    *
    */
   public void testIntegerPropertiesFile()
   {
      assertEquals(
            "The property '" + INTEGER_POSITIVE + "' have to have the value '"
            + INTEGER_POSITIVE_VALUE + "'",
            INTEGER_POSITIVE_VALUE, 
            Parameters.instance().getInteger(
                  INTEGER_POSITIVE,
                  INTEGER_POSITIVE_VALUE));

      assertEquals(
            "The property '" + INTEGER_NEGATIVE + "' have to have the value '"
            + INTEGER_NEGATIVE_VALUE + "'",
            INTEGER_NEGATIVE_VALUE, 
            Parameters.instance().getInteger(
                  INTEGER_NEGATIVE,
                  INTEGER_NEGATIVE_VALUE));

   }

   /**
    *
    */
   public void testLongPropertiesFile()
   {
      assertEquals(
            "The property '" + LONG_POSITIVE + "' have to have the value '"
            + LONG_POSITIVE_VALUE + "'",
            LONG_POSITIVE_VALUE, 
            Parameters.instance().getLong(
                  LONG_POSITIVE,
                  LONG_POSITIVE_VALUE));

      assertEquals(
            "The property '" + LONG_NEGATIVE + "' have to have the value '"
            + LONG_NEGATIVE_VALUE + "'",
            LONG_NEGATIVE_VALUE, 
            Parameters.instance().getLong(
                  LONG_NEGATIVE,
                  LONG_NEGATIVE_VALUE));

   }


   /**
    *
    */
   public void testBooleanPropertiesFile()
   {
      assertEquals(
            "The property '" + BOOLEAN_TRUE_1 + "' have to have to value TRUE",
            true, 
            Parameters.instance().getBoolean(BOOLEAN_TRUE_1, false)); 

      assertEquals(
            "The property '" + BOOLEAN_TRUE_2 + "' have to have to value TRUE",
            true, 
            Parameters.instance().getBoolean(BOOLEAN_TRUE_2, false)); 

      assertEquals(
            "The property '" + BOOLEAN_TRUE_3 + "' have to have to value TRUE",
            true, 
            Parameters.instance().getBoolean(BOOLEAN_TRUE_3, false)); 

      assertEquals(
            "The property '" + BOOLEAN_ENABLED_1 
            + "' have to have to value TRUE",
            true, Parameters.instance().getBoolean(BOOLEAN_ENABLED_1, false)); 

      assertEquals(
            "The property '" + BOOLEAN_ENABLED_2
            + "' have to have to value TRUE",
            true, Parameters.instance().getBoolean(BOOLEAN_ENABLED_2, false)); 

      assertEquals(
            "The property '" + BOOLEAN_ENABLED_3 
            + "' have to have to value TRUE",
            true, Parameters.instance().getBoolean(BOOLEAN_ENABLED_3, false)); 

      assertEquals(
            "The property '" + BOOLEAN_ON_1 + "' have to have to value TRUE",
            true, Parameters.instance().getBoolean(BOOLEAN_ON_1, false)); 

      assertEquals(
            "The property '" + BOOLEAN_ON_2 + "' have to have to value TRUE",
            true, Parameters.instance().getBoolean(BOOLEAN_ON_2, false)); 

      assertEquals(
            "The property '" + BOOLEAN_FALSE_1 
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_FALSE_1, true)); 

      assertEquals(
            "The property '" + BOOLEAN_FALSE_2 
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_FALSE_2, true)); 

      assertEquals(
            "The property '" + BOOLEAN_FALSE_3
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_FALSE_3, true)); 

      assertEquals(
            "The property '" + BOOLEAN_DISABLED_1 
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_DISABLED_1, true)); 

      assertEquals(
            "The property '" + BOOLEAN_DISABLED_2 
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_DISABLED_2, true)); 

      assertEquals(
            "The property '" + BOOLEAN_DISABLED_3
            + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_DISABLED_3, true)); 

      assertEquals(
            "The property '" + BOOLEAN_OFF_1 + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_OFF_1, true)); 

      assertEquals(
            "The property '" + BOOLEAN_OFF_2 + "' have to have to value FALSE",
            false, Parameters.instance().getBoolean(BOOLEAN_OFF_2, true));
   }

   /**
    *
    */
   public void testDatePropertiesFile()
   {        
      assertEquals(
            "The property '" + DATE_VALID + "' has to have the value '" 
            + format.format(DATE_VALUE.getTime()) + "'.",
            format.format(DATE_VALUE.getTime()), 
            format.format(
                  Parameters.instance().getDate(DATE_VALID).getTime()));

      try
      {
         Parameters.instance().getDate(DATE_INVALID);
         fail("Requested property contains an invalid date,"
              + " exception expected");
            
      }
      catch(Throwable throwable) {}
   }

   /**
    *
    */
   public void testDefaultValues()
   {
      assertEquals(
            "The property '" + STRING_NON_EXISTENT 
            + "' have to have the value '" + STRING_NON_EXISTENT_DEFAULT + "'.",
            STRING_NON_EXISTENT_DEFAULT, 
            Parameters.instance().getString(
                  STRING_NON_EXISTENT,
                  STRING_NON_EXISTENT_DEFAULT));

      assertEquals(
            "The property '" + STRING_NON_EXISTENT 
            + "' have to have the value 'null'.",
            null, Parameters.instance().getString(STRING_NON_EXISTENT));

      assertEquals(
            "The property '" + INTEGER_NON_EXISTENT 
            + "' have to have the value '" + INTEGER_NON_EXISTENT_DEFAULT 
            + "'.",
            INTEGER_NON_EXISTENT_DEFAULT, 
            Parameters.instance().getInteger(
                  INTEGER_NON_EXISTENT,
                  INTEGER_NON_EXISTENT_DEFAULT));

      assertEquals(
            "The property '" + LONG_NON_EXISTENT 
            + "' have to have the value '" + LONG_NON_EXISTENT_DEFAULT + "'.",
            LONG_NON_EXISTENT_DEFAULT, 
            Parameters.instance().getLong(
                  LONG_NON_EXISTENT,
                  LONG_NON_EXISTENT_DEFAULT));

      assertEquals(
            "The property '" + BOOLEAN_NON_EXISTENT 
            + "' have to have the value '" + BOOLEAN_NON_EXISTENT_DEFAULT 
            + "'.",
            BOOLEAN_NON_EXISTENT_DEFAULT, 
            Parameters.instance().getBoolean(
                  BOOLEAN_NON_EXISTENT,
                  BOOLEAN_NON_EXISTENT_DEFAULT));

      assertEquals(
            "The property '" + DATE_VALID + "' have to have the value '"
            + format.format(DATE_VALUE.getTime()),
            format.format(DATE_VALUE.getTime()), 
            format.format(
                  Parameters.instance().getDate(
                        DATE_VALID, DATE_VALUE).getTime()));

      assertEquals(
            "The property '" + DATE_NON_EXISTENT 
            + "' have to have the value 'null'",
            null, 
            Parameters.instance().getDate(DATE_NON_EXISTENT));
   }
}
