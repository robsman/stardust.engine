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
package org.eclipse.stardust.engine.core.struct;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.beans.IStructuredDataValue;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * Engine runtime implementation of IStructuredDataValueFactory
 */
public class StructuredDataValueFactory implements IStructuredDataValueFactory
{

   private static final String XSD_DATE_FORMAT = "yyyy-MM-dd";
   private static final String XSD_TIME_FORMAT = "HH:mm:ss";
   private final static String XSD_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
   // TODO remove this workaround for P&M
   private final static String P_AND_M_DATETIME_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";
   
   public static Object convertTo(int typeKey, String stringValue)
   {
      if (stringValue == null) 
      {
         return null;
      }
      
      if (typeKey == BigData.NULL)
      {
         return "";
      }
      else if (typeKey == BigData.SHORT)
      {
         return new Short(stringValue);
      }
      else if (typeKey == BigData.INTEGER)
      {
         return new Integer(stringValue);
      }
      else if (typeKey == BigData.LONG)
      {
         return new Long(stringValue);
      }
      else if (typeKey == BigData.BYTE)
      {
         return new Byte(stringValue);
      }
      else if (typeKey == BigData.BOOLEAN)
      {
         return new Boolean(stringValue);
      }
      else if (typeKey == BigData.DATE)
      {
         SimpleDateFormat df;
         try
         {
            df = new SimpleDateFormat(XSD_DATETIME_FORMAT);
            df.setLenient(false);
            return df.parse(stringValue);
         }
         catch (Exception e1)
         {
            try
            {
               //RPI: Workaround for CRNT-25389  
               SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
               String dateString = sd.format(new Date()); 
               String tempStrValue = dateString + "T" + stringValue;
               df = new SimpleDateFormat(XSD_DATETIME_FORMAT);
               df.setLenient(false);
               return df.parse(tempStrValue);
            }
            catch (Exception e2)
            {
               try
               {
                  df = new SimpleDateFormat(XSD_DATE_FORMAT);
                  df.setLenient(false);
                  return df.parse(stringValue);
               }
               catch (Exception e3)
               {
                  try
                  {
                     df = new SimpleDateFormat(P_AND_M_DATETIME_FORMAT, Locale.US);
                     df.setLenient(false);
                     return df.parse(stringValue);
                  }
                  catch (Exception e4)
                  {
                     throw new PublicException("Could not parse date/time/datetime '"
                           + stringValue
                           + "' using standard XSD date/time/datetime formats '"
                           + XSD_DATE_FORMAT + "', '" + XSD_TIME_FORMAT + "', '"
                           + XSD_DATETIME_FORMAT + "' and additional format '"
                           + P_AND_M_DATETIME_FORMAT + "'.");
                  }
               }
            }
         }
      }
      else if (typeKey == BigData.FLOAT)
      {
         return new Float(stringValue);
      }
      else if (typeKey == BigData.DOUBLE)
      {
         return new Double(stringValue);
      }
      else if (typeKey == BigData.STRING)
      {
         return stringValue;
      }
      else if (typeKey == BigData.BIG_STRING)
      {
         return stringValue;
      } 
      else if (typeKey == BigData.PERIOD)
      {
         return new Period(stringValue);
      } 
      else 
      {
         throw new PublicException("BigData type '"+typeKey+"' is supported yet");
      }
   }
   
   public static String convertToString(int typeKey, String xsdTypeName, Object value)
   {
      if (value == null)
      {
         return null;
      }

      if (value instanceof String)
      {
         // no need to convert, since it is already a string 
         // (can occur if the map is filled by a JSF app)
         return (String)value;
      }
      else if (typeKey == BigData.NULL)
      {
         return "";
      }
      else if (typeKey == BigData.BOOLEAN || typeKey == BigData.STRING
            || typeKey == BigData.BIG_STRING || typeKey == BigData.PERIOD)
      {
         return value.toString();
      }
      else if (typeKey == BigData.FLOAT || typeKey == BigData.DOUBLE)
      {
         return value.toString();
      }
      else if (typeKey == BigData.SHORT)
      {
         Number number = (Number)value;
         return Short.toString(number.shortValue());
      }
      else if (typeKey == BigData.INTEGER)
      {
         Number number = (Number)value;
         return Integer.toString(number.intValue());
      }
      else if (typeKey == BigData.LONG)
      {
         Number number = (Number)value;
         return Long.toString(number.longValue());
      }
      else if (typeKey == BigData.BYTE)
      {
         Number number = (Number)value;
         return Byte.toString(number.byteValue());
      }
      else if (typeKey == BigData.DATE)
      {
         String dateFormatString;
         if ("time".equals(xsdTypeName))
         {
            dateFormatString = XSD_TIME_FORMAT;
         }
         else if ("date".equals(xsdTypeName))
         {
            dateFormatString = XSD_DATE_FORMAT;
         }
         else if ("dateTime".equals(xsdTypeName))
         {
            dateFormatString = XSD_DATETIME_FORMAT;
         }
         else
         {
            // unknown date format, take the full XSD_DATETIME_FORMAT
            dateFormatString = XSD_DATETIME_FORMAT;
         }
         SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
         df.setLenient(false);
         return df.format(value);
      }
      else
      {
         throw new PublicException("BigData type '" + typeKey + "' is supported yet");
      }
   }

   public IStructuredDataValue createKeyedElementEntry(long rootOid, long parentOid,
         long xPathOid, String entryKey, String stringValue, int typeKey)
   {
      return new StructuredDataValueBean(rootOid, parentOid, xPathOid, convertTo(typeKey, stringValue), entryKey, typeKey);
   }

   public IStructuredDataValue createRootElementEntry(long rootOid, long xPathOid, String entryKey,
         String stringValue)
   {
      return new StructuredDataValueBean(rootOid,
            IStructuredDataValue.NO_PARENT, xPathOid, stringValue, entryKey, BigData.NULL);
   }
}
