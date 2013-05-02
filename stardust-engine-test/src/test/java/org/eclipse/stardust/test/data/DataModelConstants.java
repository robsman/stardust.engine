/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.common.DateUtils;

/**
 * <p>
 * This class contains constants related to the model
 * used for tests dealing with process data.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
/* package-private */ class DataModelConstants
{
  private static final Log LOG = LogFactory.getLog(DataModelConstants.class);
  
   static
   {
      final Calendar calendar = Calendar.getInstance();
      final DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
      try
      {
         final Date date = dateInstance.parse("4/19/12");
         
         calendar.setTime(date);
         DEFAULT_VALUE_DEFAULT_CALENDAR = calendar;

         DEFAULT_VALUE_DEFAULT_TIMESTAMP = date;
      }
      catch (final ParseException e)
      {
         throw new ParseDateException(e);
      }
   }
   
   /**
    * the name of the model used for process data tests
    */
   /* package-private */ static final String MODEL_NAME = "DataModel";
   
   
   /**
    * Process Definition #1
    */
   /* package-private */ static final String PROCESS_ID_1 = "ProcessDefinition_1";
   
   /**
    * Process Definition #2
    */
   /* package-private */ static final String PROCESS_ID_2 = "ProcessDefinition_2";
   
   
   /**
    * an in data mapping for 'My Calendar'
    */
   /* package-private */ static final String MY_CALENDAR_IN_DATA_MAPPING = "MyCalendar";
   
   /**
    * an out data mapping for 'My Calendar'
    */
   /* package-private */ static final String MY_CALENDAR_OUT_DATA_MAPPING = "MyCalendar";
   
   /**
    * an in data mapping for 'My String'
    */
   /* package-private */ static final String MY_STRING_IN_DATA_MAPPING = "MyString";

   /**
    * an out data mapping for 'My String'
    */
   /* package-private */ static final String MY_STRING_OUT_DATA_MAPPING = "MyString";
   
   /**
    * an in data mapping for 'My Timestamp'
    */
   /* package-private */ static final String MY_TIMESTAMP_IN_DATA_MAPPING = "MyTimestamp";
   
   /**
    * an out data mapping for 'My Timestamp'
    */
   /* package-private */ static final String MY_TIMESTAMP_OUT_DATA_MAPPING = "MyTimestamp";
   
   /**
    * an in data mapping for 'My Boolean'
    */
   /* package-private */ static final String MY_BOOLEAN_IN_DATA_MAPPING = "MyBoolean";
   
   /**
    * an out data mapping for 'My Boolean'
    */
   /* package-private */ static final String MY_BOOLEAN_OUT_DATA_MAPPING = "MyBoolean";
   
   /**
    * an in data mapping for 'My Byte'
    */
   /* package-private */ static final String MY_BYTE_IN_DATA_MAPPING = "MyByte";
   
   /**
    * an out data mapping for 'My Byte'
    */
   /* package-private */ static final String MY_BYTE_OUT_DATA_MAPPING = "MyByte";
   
   /**
    * an in data mapping for 'My Char'
    */
   /* package-private */ static final String MY_CHAR_IN_DATA_MAPPING = "MyChar";
   
   /**
    * an out data mapping for 'My Char'
    */
   /* package-private */ static final String MY_CHAR_OUT_DATA_MAPPING = "MyChar";
   
   /**
    * an in data mapping for 'My Short'
    */
   /* package-private */ static final String MY_SHORT_IN_DATA_MAPPING = "MyShort";
   
   /**
    * an out data mapping for 'My Short'
    */
   /* package-private */ static final String MY_SHORT_OUT_DATA_MAPPING = "MyShort";
   
   /**
    * an in data mapping for 'My Long'
    */
   /* package-private */ static final String MY_LONG_IN_DATA_MAPPING = "MyLong";
   
   /**
    * an out data mapping for 'My Long'
    */
   /* package-private */ static final String MY_LONG_OUT_DATA_MAPPING = "MyLong";
   
   /**
    * an in data mapping for 'My Int'
    */
   /* package-private */ static final String MY_INT_IN_DATA_MAPPING = "MyInt";
   
   /**
    * an out data mapping for 'My Int'
    */
   /* package-private */ static final String MY_INT_OUT_DATA_MAPPING = "MyInt";
   
   /**
    * an in data mapping for 'My Float'
    */
   /* package-private */ static final String MY_FLOAT_IN_DATA_MAPPING = "MyFloat";
   
   /**
    * an out data mapping for 'My Float'
    */
   /* package-private */ static final String MY_FLOAT_OUT_DATA_MAPPING = "MyFloat";
   
   /**
    * an in data mapping for 'My Double'
    */
   /* package-private */ static final String MY_DOUBLE_IN_DATA_MAPPING = "MyDouble";
   
   /**
    * an out data mapping for 'My Double'
    */
   /* package-private */ static final String MY_DOUBLE_OUT_DATA_MAPPING = "MyDouble";
   
   
   /**
    * an in data path for 'My Calendar'
    */
   /* package-private */ static final String MY_CALENDAR_IN_DATA_PATH = "MyCalendarInDataPath";
   
   /**
    * an out data path for 'My Calendar'
    */
   /* package-private */ static final String MY_CALENDAR_OUT_DATA_PATH = "MyCalendarOutDataPath";

   /**
    * an in data path for 'My String'
    */
   /* package-private */ static final String MY_STRING_IN_DATA_PATH = "MyStringInDataPath";
   
   /**
    * an out data path for 'My String'
    */
   /* package-private */ static final String MY_STRING_OUT_DATA_PATH = "MyStringOutDataPath";
   
   /**
    * an in data path for 'My Timestamp'
    */
   /* package-private */ static final String MY_TIMESTAMP_IN_DATA_PATH = "MyTimestampInDataPath";
   
   /**
    * an out data path for 'My Timestamp'
    */
   /* package-private */ static final String MY_TIMESTAMP_OUT_DATA_PATH = "MyTimestampOutDataPath";
   
   /**
    * an in data path for 'My Boolean'
    */
   /* package-private */ static final String MY_BOOLEAN_IN_DATA_PATH = "MyBooleanInDataPath";
   
   /**
    * an out data path for 'My Boolean'
    */
   /* package-private */ static final String MY_BOOLEAN_OUT_DATA_PATH = "MyBooleanOutDataPath";
   
   /**
    * an in data path for 'My Byte'
    */
   /* package-private */ static final String MY_BYTE_IN_DATA_PATH = "MyByteInDataPath";
   
   /**
    * an out data path for 'My Byte'
    */
   /* package-private */ static final String MY_BYTE_OUT_DATA_PATH = "MyByteOutDataPath";
   
   /**
    * an in data path for 'My Char'
    */
   /* package-private */ static final String MY_CHAR_IN_DATA_PATH = "MyCharInDataPath";
   
   /**
    * an out data path for 'My Char'
    */
   /* package-private */ static final String MY_CHAR_OUT_DATA_PATH = "MyCharOutDataPath";
   
   /**
    * an in data path for 'My Short'
    */
   /* package-private */ static final String MY_SHORT_IN_DATA_PATH = "MyShortInDataPath";
   
   /**
    * an out data path for 'My Short'
    */
   /* package-private */ static final String MY_SHORT_OUT_DATA_PATH = "MyShortOutDataPath";
   
   /**
    * an in data path for 'My Long'
    */
   /* package-private */ static final String MY_LONG_IN_DATA_PATH = "MyLongInDataPath";
   
   /**
    * an out data path for 'My Long'
    */
   /* package-private */ static final String MY_LONG_OUT_DATA_PATH = "MyLongOutDataPath";
   
   /**
    * an in data path for 'My Int'
    */
   /* package-private */ static final String MY_INT_IN_DATA_PATH = "MyIntInDataPath";
   
   /**
    * an out data path for 'My Int'
    */
   /* package-private */ static final String MY_INT_OUT_DATA_PATH = "MyIntOutDataPath";
   
   /**
    * an in data path for 'My Float'
    */
   /* package-private */ static final String MY_FLOAT_IN_DATA_PATH = "MyFloatInDataPath";
   
   /**
    * an out data path for 'My Float'
    */
   /* package-private */ static final String MY_FLOAT_OUT_DATA_PATH = "MyFloatOutDataPath";
   
   /**
    * an in data path for 'My Double'
    */
   /* package-private */ static final String MY_DOUBLE_IN_DATA_PATH = "MyDoubleInDataPath";
   
   /**
    * an out data path for 'My Double'
    */
   /* package-private */ static final String MY_DOUBLE_OUT_DATA_PATH = "MyDoubleOutDataPath";
   
   
   /**
    * an in data mapping for 'Default Calendar'
    */
   /* package-private */ static final String DEFAULT_CALENDAR_IN_DATA_MAPPING = "DefaultCalendar";
   
   /**
    * an in data mapping for 'Default String'
    */
   /* package-private */ static final String DEFAULT_STRING_IN_DATA_MAPPING = "DefaultString";
   
   /**
    * an in data mapping for 'Default Timestamp'
    */
   /* package-private */ static final String DEFAULT_TIMESTAMP_IN_DATA_MAPPING = "DefaultTimestamp";
   
   /**
    * an in data mapping for 'Default Boolean'
    */
   /* package-private */ static final String DEFAULT_BOOLEAN_IN_DATA_MAPPING = "DefaultBoolean";
   
   /**
    * an in data mapping for 'Default Byte'
    */
   /* package-private */ static final String DEFAULT_BYTE_IN_DATA_MAPPING = "DefaultByte";
   
   /**
    * an in data mapping for 'Default Char'
    */
   /* package-private */ static final String DEFAULT_CHAR_IN_DATA_MAPPING = "DefaultChar";
   
   /**
    * an in data mapping for 'Default Short'
    */
   /* package-private */ static final String DEFAULT_SHORT_IN_DATA_MAPPING = "DefaultShort";
   
   /**
    * an in data mapping for 'Default Long'
    */
   /* package-private */ static final String DEFAULT_LONG_IN_DATA_MAPPING = "DefaultLong";
   
   /**
    * an in data mapping for 'Default Int'
    */
   /* package-private */ static final String DEFAULT_INT_IN_DATA_MAPPING = "DefaultInt";
   
   /**
    * an in data mapping for 'Default Float'
    */
   /* package-private */ static final String DEFAULT_FLOAT_IN_DATA_MAPPING = "DefaultFloat";
   
   /**
    * an in data mapping for 'Default Double'
    */
   /* package-private */ static final String DEFAULT_DOUBLE_IN_DATA_MAPPING = "DefaultDouble";
   
   
   /**
    * an in data path for 'Default Calendar'
    */
   /* package-private */ static final String DEFAULT_CALENDAR_IN_DATA_PATH = "DefaultCalendarInDataPath";

   /**
    * an in data path for 'Default String'
    */
   /* package-private */ static final String DEFAULT_STRING_IN_DATA_PATH = "DefaultStringInDataPath";
   
   /**
    * an in data path for 'Default Timestamp'
    */
   /* package-private */ static final String DEFAULT_TIMESTAMP_IN_DATA_PATH = "DefaultTimestampInDataPath";
   
   /**
    * an in data path for 'Default Boolean'
    */
   /* package-private */ static final String DEFAULT_BOOLEAN_IN_DATA_PATH = "DefaultBooleanInDataPath";
   
   /**
    * an in data path for 'Default Byte'
    */
   /* package-private */ static final String DEFAULT_BYTE_IN_DATA_PATH = "DefaultByteInDataPath";
   
   /**
    * an in data path for 'Default Char'
    */
   /* package-private */ static final String DEFAULT_CHAR_IN_DATA_PATH = "DefaultCharInDataPath";
   
   /**
    * an in data path for 'Default Short'
    */
   /* package-private */ static final String DEFAULT_SHORT_IN_DATA_PATH = "DefaultShortInDataPath";
   
   /**
    * an in data path for 'Default Long'
    */
   /* package-private */ static final String DEFAULT_LONG_IN_DATA_PATH = "DefaultLongInDataPath";
   
   /**
    * an in data path for 'Default Int'
    */
   /* package-private */ static final String DEFAULT_INT_IN_DATA_PATH = "DefaultIntInDataPath";
   
   /**
    * an in data path for 'Default Float'
    */
   /* package-private */ static final String DEFAULT_FLOAT_IN_DATA_PATH = "DefaultFloatInDataPath";
   
   /**
    * an in data path for 'Default Double'
    */
   /* package-private */ static final String DEFAULT_DOUBLE_IN_DATA_PATH = "DefaultDoubleInDataPath";
   
   
   /**
    * the modeled default value of process data 'Default Calendar'
    */
   /* package-private */ static final Calendar DEFAULT_VALUE_DEFAULT_CALENDAR;
   
   /**
    * the modeled default value of process data 'Default String'
    */
   /* package-private */ static final String DEFAULT_VALUE_DEFAULT_STRING = "default string";
   
   /**
    * the modeled default value of process data 'Default Timestamp'
    */
   /* package-private */ static final Date DEFAULT_VALUE_DEFAULT_TIMESTAMP;
   
   /**
    * the modeled default value of process data 'Default Boolean'
    */
   /* package-private */ static final Boolean DEFAULT_VALUE_DEFAULT_BOOLEAN = Boolean.TRUE;
   
   /**
    * the modeled default value of process data 'Default Byte'
    */
   /* package-private */ static final Byte DEFAULT_VALUE_DEFAULT_BYTE = Byte.valueOf((byte) 8);
   
   /**
    * the modeled default value of process data 'Default Char'
    */
   /* package-private */ static final Character DEFAULT_VALUE_DEFAULT_CHAR = Character.valueOf('x');
   
   /**
    * the modeled default value of process data 'Default Double'
    */
   /* package-private */ static final Double DEFAULT_VALUE_DEFAULT_DOUBLE = Double.valueOf(81.18);
   
   /**
    * the modeled default value of process data 'Default Float'
    */
   /* package-private */ static final Float DEFAULT_VALUE_DEFAULT_FLOAT = Float.valueOf(18.81F);
   
   /**
    * the modeled default value of process data 'Default Int'
    */
   /* package-private */ static final Integer DEFAULT_VALUE_DEFAULT_INT = Integer.valueOf(81);
   
   /**
    * the modeled default value of process data 'Default Long'
    */
   /* package-private */ static final Long DEFAULT_VALUE_DEFAULT_LONG = Long.valueOf(818);
   
   /**
    * the modeled default value of process data 'Default Short'
    */
   /* package-private */ static final Short DEFAULT_VALUE_DEFAULT_SHORT = Short.valueOf((short) 18);
   
   private static final class ParseDateException extends RuntimeException
   {
      private static final long serialVersionUID = -1647113152346094388L;

      public ParseDateException(final ParseException e)
      {
         super("Cannot initialize constants.", e);
      }
   }
}
