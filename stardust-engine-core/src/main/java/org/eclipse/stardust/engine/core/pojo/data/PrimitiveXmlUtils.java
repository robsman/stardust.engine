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
package org.eclipse.stardust.engine.core.pojo.data;

import static javax.xml.bind.DatatypeConverter.parseBoolean;
import static javax.xml.bind.DatatypeConverter.parseByte;
import static javax.xml.bind.DatatypeConverter.parseDateTime;
import static javax.xml.bind.DatatypeConverter.parseDouble;
import static javax.xml.bind.DatatypeConverter.parseFloat;
import static javax.xml.bind.DatatypeConverter.parseInt;
import static javax.xml.bind.DatatypeConverter.parseLong;
import static javax.xml.bind.DatatypeConverter.parseShort;
import static javax.xml.bind.DatatypeConverter.parseString;
import static javax.xml.bind.DatatypeConverter.printBoolean;
import static javax.xml.bind.DatatypeConverter.printByte;
import static javax.xml.bind.DatatypeConverter.printDateTime;
import static javax.xml.bind.DatatypeConverter.printDouble;
import static javax.xml.bind.DatatypeConverter.printFloat;
import static javax.xml.bind.DatatypeConverter.printInt;
import static javax.xml.bind.DatatypeConverter.printLong;
import static javax.xml.bind.DatatypeConverter.printShort;
import static javax.xml.bind.DatatypeConverter.printString;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PRIMITIVE_DATA;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;



/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class PrimitiveXmlUtils
{
   
   private static final Logger trace = LogManager.getLogger(PrimitiveXmlUtils.class);
   
   public static Class<?> getMappedType(QName type)
   {
      if ((null == type) || (QNameConstants.QN_STRING.equals(type)))
      {
         return String.class;
      }
      else if (QNameConstants.QN_LONG.equals(type))
      {
         return Long.class;
      }
      else if (QNameConstants.QN_INT.equals(type))
      {
         return Integer.class;
      }
      else if (QNameConstants.QN_SHORT.equals(type))
      {
         return Short.class;
      }
      else if (QNameConstants.QN_BYTE.equals(type))
      {
         return Byte.class;
      }
      else if (QNameConstants.QN_DOUBLE.equals(type))
      {
         return Double.class;
      }
      else if (QNameConstants.QN_FLOAT.equals(type))
      {
         return Float.class;
      }
      else if (QNameConstants.QN_BOOLEAN.equals(type))
      {
         return Boolean.class;
      }
      else if (QNameConstants.QN_DATETIME.equals(type))
      {
         return Date.class;
      }
      else if (QNameConstants.QN_CHAR.equals(type))
      {
         return Character.class;
      }
      else if (QNameConstants.QN_BASE64BINARY.equals(type))
      {
         return Calendar.class;
      }
      else
      {
         trace.warn("Unsupported primitive type code " + type);

         return Object.class;
      }
   }
   
   public static QName marshalSimpleTypeXsdType(Class< ? > value)
   {
      QName ret = null;
      if (String.class.equals(value))
      {
         ret = QNameConstants.QN_STRING;
      }
      else if (Long.class.equals(value))
      {
         ret = QNameConstants.QN_LONG;
      }
      else if (Integer.class.equals(value))
      {
         ret = QNameConstants.QN_INT;
      }
      else if (Short.class.equals(value))
      {
         ret = QNameConstants.QN_SHORT;
      }
      else if (Byte.class.equals(value))
      {
         ret = QNameConstants.QN_BYTE;
      }
      else if (Double.class.equals(value))
      {
         ret = QNameConstants.QN_DOUBLE;
      }
      else if (Float.class.equals(value))
      {
         ret = QNameConstants.QN_FLOAT;
      }
      else if (Boolean.class.equals(value))
      {
         ret = QNameConstants.QN_BOOLEAN;
      }
      else if (Date.class.equals(value))
      {
         ret = QNameConstants.QN_DATETIME;
      }
      else if (Calendar.class.equals(value) || Money.class.equals(value))
      {
         ret = QNameConstants.QN_BASE64BINARY;
      }
      else if (Character.class.equals(value))
      {
         ret = QNameConstants.QN_CHAR;
      }
      return ret;
   }

   public static String marshalPrimitiveValue(Serializable value)
   {
      String ret = null;
      if (null != value)
      {
         // handle simpleType
         ret = marshalSimpleTypeXsdValue(value);

         if (value instanceof Character)
         {
            // param.setPrimitive(printString(((Character) value).toString()));

            ret = new String(
                  Base64.encode((((Character) value).toString().getBytes())));
         }
         else if (value instanceof Calendar || value instanceof Money)
         {
            try
            {
               ret = new String(
                     Base64.encode(Serialization.serializeObject(value)));
            }
            catch (IOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }

         // if (param.getType() == null)
         // {
         // throw new UnsupportedOperationException(
         // "Marshaling not supported for primitiveType: " + primitiveType.getName()
         // + " ValueClass: " + value.getClass());
         // }
      }
      return ret;
   }

   public static String marshalSimpleTypeXsdValue(Serializable value)
   {
      String ret = null;
      if (value instanceof String)
      {
         ret = printString((String) value);
      }
      else if (value instanceof Long)
      {
         ret = printLong((Long) value);
      }
      else if (value instanceof Integer)
      {
         ret = printInt((Integer) value);
      }
      else if (value instanceof Short)
      {
         ret = printShort((Short) value);
      }
      else if (value instanceof Byte)
      {
         ret = printByte((Byte) value);
      }
      else if (value instanceof Double)
      {
         ret = printDouble((Double) value);
      }
      else if (value instanceof Float)
      {
         ret = printFloat((Float) value);
      }
      else if (value instanceof Boolean)
      {
         ret = printBoolean((Boolean) value);
      }
      else if (value instanceof Date)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime((Date) value);
         ret = printDateTime(cal);
      }
      return ret;
   }

   public static Serializable unmarshalPrimitiveValue(QName type, String value)
   {
      Type targetType = unmarshalPrimitiveType(type);

      return unmarshalPrimitiveValue(targetType, value);
   }
   

   public static Type unmarshalPrimitiveType(QName type)
   {
      if ((null == type) || (QNameConstants.QN_STRING.equals(type)))
      {
         return Type.String;
      }
      else if (QNameConstants.QN_LONG.equals(type))
      {
         return Type.Long;
      }
      else if (QNameConstants.QN_INT.equals(type))
      {
         return Type.Integer;
      }
      else if (QNameConstants.QN_SHORT.equals(type))
      {
         return Type.Short;
      }
      else if (QNameConstants.QN_BYTE.equals(type))
      {
         return Type.Byte;
      }
      else if (QNameConstants.QN_DOUBLE.equals(type))
      {
         return Type.Double;
      }
      else if (QNameConstants.QN_FLOAT.equals(type))
      {
         return Type.Float;
      }
      else if (QNameConstants.QN_BOOLEAN.equals(type))
      {
         return Type.Boolean;
      }
      else if (QNameConstants.QN_DATETIME.equals(type))
      {
         return Type.Timestamp;
      }
      else if (QNameConstants.QN_CHAR.equals(type))
      {
         return Type.Char;
      }
      else if (QNameConstants.QN_BASE64BINARY.equals(type))
      {
         return Type.Calendar;
      }
      else
      {
         trace.warn("Unsupported primitive type code " + type);

         return null;
      }
   }   
   
   public static Serializable unmarshalPrimitiveValue(Type targetType, String value)
   {
      // TODO consider type codes
      if ((null == targetType) || (Type.String == targetType))
      {
         return parseString(value);
      }
      else if (Type.Long == targetType)
      {
         return parseLong(value);
      }
      else if (Type.Integer == targetType)
      {
         return parseInt(value);
      }
      else if (Type.Short == targetType)
      {
         return parseShort(value);
      }
      else if (Type.Byte == targetType)
      {
         return parseByte(value);
      }
      else if (Type.Double == targetType)
      {
         return parseDouble(value);
      }
      else if (Type.Float == targetType)
      {
         return parseFloat(value);
      }
      else if (Type.Boolean == targetType)
      {
         return parseBoolean(value);
      }
      else if (Type.Timestamp == targetType)
      {
         Calendar cal = parseDateTime(value);
         return cal.getTime();
      }
      else if (Type.Char == targetType)
      {
         // return value == null ? null : Character.valueOf(parseString(value).charAt(0));
         return value == null ? null : Character.valueOf(new String(
               Base64.decode(value.getBytes())).charAt(0));
      }
      else if (Type.Calendar == targetType)
      {
         Serializable ret = null;
         if (value != null)
         {
            ret = deserialize(Base64.decode(value.getBytes()));
         }
         return ret;
      }
      else
      {
         trace.warn("Ignoring primitive type code " + targetType);
      }
      return value;
   }
   
   private static Serializable deserialize(byte[] decode)
   {
      Serializable ret = null;

      try
      {
         ret = Serialization.deserializeObject(decode);
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         trace.error(e);
      }
      catch (ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         trace.error(e);
      }
      return ret;
   }
   
   public static boolean isPrimitiveType(Model model, AccessPoint ap)
   {
      return (ap.getAttribute(PredefinedConstants.TYPE_ATT) instanceof Type);
   }

   public static boolean isPrimitiveType(Model model, Data data)
   {
      boolean isPrimitive = false;

      if (null != model)
      {
         String dataType = getTypeId(data);
         isPrimitive = PRIMITIVE_DATA.equals(dataType);
      }

      return isPrimitive;
   }

   public static boolean isPrimitiveType(Model model, DataMapping dm)
   {
      boolean isPrimitive = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isPrimitive = isPrimitiveType(model, data);
      }

      return isPrimitive;
   }
      
      public static String getTypeId(Data data)
      {
         return (String) Reflect.getFieldValue(data, "typeId");
      }      
   
}
