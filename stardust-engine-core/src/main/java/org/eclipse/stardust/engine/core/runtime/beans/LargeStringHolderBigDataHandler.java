/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LargeStringHolderBigDataHandler implements BigDataHandler
{
   public static final Logger trace = LogManager.getLogger(
         LargeStringHolderBigDataHandler.class);

   private static Object UNKNOWN_OBJECT = new Object();

   private BigData data;
   private Class persistentType;
   private Object value = UNKNOWN_OBJECT;

   /**
    * Classifies workflow data type representation into {@link BigData#NUMERIC_VALUE}s,
    * {@link org.eclipse.stardust.engine.core.runtime.beans.BigData#STRING_VALUE}s, {@link BigData#NULL_VALUE}s and
    * {@link BigData#UNKNOWN_VALUE}s. This classification gives a hint which
    * {@link BigData} representation will be used for the actual value.
    * 
    * @param data The workflow data to be classified.
    * @return Either {@link BigData#NUMERIC_VALUE}, {@link BigData#STRING_VALUE},
    *         {@link BigData#NULL_VALUE} or {@link BigData#UNKNOWN_VALUE}.
    */
   public static int classifyType(IData data)
   {
      if (data.getType().getId().equals(PredefinedConstants.PRIMITIVE_DATA))
      {
         Type type = (Type) data.getAttribute(PredefinedConstants.TYPE_ATT);
         if (type.equals(Type.Boolean)
               || type.equals(Type.Byte)
               || type.equals(Type.Short)
               || type.equals(Type.Integer)
               || type.equals(Type.Long)
               || type.equals(Type.Timestamp))
         {
            return BigData.NUMERIC_VALUE;
         }
         else 
         {
            return BigData.STRING_VALUE;
         }
      }
      else
      {
         return BigData.UNKNOWN_VALUE;
      }
   }
   
   /**
    * Classifies workflow data type representation into {@link BigData#NUMERIC_VALUE}s,
    * {@link org.eclipse.stardust.engine.core.runtime.beans.BigData#STRING_VALUE}s, {@link BigData#NULL_VALUE}s and
    * {@link BigData#UNKNOWN_VALUE}s. This classification gives a hint which
    * {@link BigData} representation will be used for the actual value.
    * 
    * @param data The workflow data to be classified.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @return Either {@link BigData#NUMERIC_VALUE}, {@link BigData#STRING_VALUE},
    *         {@link BigData#NULL_VALUE} or {@link BigData#UNKNOWN_VALUE}.
    */
   public static int classifyType(IData data, String attributeName)
   {
      if ( !(StructuredTypeRtUtils.isDmsType(data.getType().getId()) || StructuredTypeRtUtils.isStructuredType(data.getType().getId())))
      {
         // not a structured data
         return classifyType(data);
      }
      
      // and now special treatment for structured data
      if (attributeName == null)
      {
         // whole value of structured data is returned, neither NUMERIC_VALUE, nor STRING_VALUE 
         return BigData.UNKNOWN_VALUE;
      }
      
      IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
      int dataType = xPathMap.getXPath(attributeName).getType();
      if (dataType == BigData.BOOLEAN || dataType == BigData.BYTE
            || dataType == BigData.SHORT || dataType == BigData.INTEGER
            || dataType == BigData.LONG || dataType == BigData.DATE)
      {
         return BigData.NUMERIC_VALUE;
      }
      else
      {
         return BigData.STRING_VALUE;
      }
   }

   /**
    * Evaluates the canonical representation of the given data value.
    * 
    * @param dataValue 
    * @return <code>null</code>, a <code>Long</code> or a <code>String</code> representing
    *         the given data value.
    */
   public static Representation canonicalizeDataValue(int shortStringDataSize,
         Object dataValue)
   {
      Representation canonicalValue;

      if (dataValue instanceof Pair)
      {
         Pair pair = (Pair) dataValue;

         final Representation firstCanonicalValue = canonicalizeAtomicDataValue(
                     shortStringDataSize, pair.getFirst());
         final Representation secondCanonicalValue = canonicalizeAtomicDataValue(
                     shortStringDataSize, pair.getSecond());

         if (firstCanonicalValue.getSizeNeutralTypeKey()
               != secondCanonicalValue.getSizeNeutralTypeKey())
         {
            throw new PublicException("Inconsistent pair values : "
                  + pair.getFirst() + " - " + pair.getSecond());
         }

         final Representation representationTemplate = firstCanonicalValue.isLarge()
               ? firstCanonicalValue : secondCanonicalValue;

         canonicalValue = new Representation(representationTemplate.getClassificationKey(),
               representationTemplate.getTypeKey(),
               new Pair(firstCanonicalValue.getRepresentation(),
                     secondCanonicalValue.getRepresentation()));
      }
      else if (dataValue instanceof Collection)
      {
         Representation template = null;

         List values = new ArrayList(((Collection) dataValue).size());

         for (Iterator i = ((Collection) dataValue).iterator(); i.hasNext();)
         {
            Representation representation = canonicalizeAtomicDataValue(
                  shortStringDataSize, i.next());

            if ((null != template)
                  && (template.getSizeNeutralTypeKey()
                        != representation.getSizeNeutralTypeKey()))
            {
               throw new PublicException("Inconsistent collection values : "
                     + template.getRepresentation() + " - "
                     + representation.getRepresentation());
            }

            values.add(representation.getRepresentation());

            template = ((null == template) || representation.isLarge())
                  ? representation : template;
         }

         canonicalValue = new Representation(template.getClassificationKey(),
               template.getTypeKey(), values);
      }
      else
      {
         canonicalValue = canonicalizeAtomicDataValue(shortStringDataSize, dataValue);
      }

      return canonicalValue;
   }

   public static Representation canonicalizeAtomicDataValue(int shortStringDataSize,
         Object dataValue)
   {
      if (dataValue == null)
      {
         return new Representation(BigData.NULL_VALUE, BigData.NULL, null);
      }
      else if (dataValue instanceof Short)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.SHORT,
               new Long(((Short) dataValue).longValue()));
      }
      else if (dataValue instanceof Integer)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.INTEGER,
               new Long(((Integer) dataValue).longValue()));
      }
      else if (dataValue instanceof Long)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.LONG, dataValue);
      }
      else if (dataValue instanceof Byte)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.BYTE,
               new Long(((Byte) dataValue).longValue()));
      }
      else if (dataValue instanceof Boolean)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.BOOLEAN,
               new Long(((Boolean) dataValue).booleanValue() ? 1 : 0));
      }
      else if (dataValue instanceof Date)
      {
         return new Representation(BigData.NUMERIC_VALUE, BigData.DATE,
               new Long(((Date) dataValue).getTime()));
      }
      else if (dataValue instanceof Float)
      {
         return new Representation(BigData.STRING_VALUE, BigData.FLOAT,
               dataValue.toString());
      }
      else if (dataValue instanceof Double)
      {
         return new Representation(BigData.STRING_VALUE, BigData.DOUBLE,
               dataValue.toString());
      }
      else if (dataValue instanceof Character)
      {
         return new Representation(BigData.STRING_VALUE, BigData.CHAR,
               dataValue.toString());
      }
      else if (dataValue instanceof Money)
      {
         return new Representation(BigData.STRING_VALUE, BigData.MONEY,
               dataValue.toString());
      }
      else if (dataValue instanceof String)
      {
         String stringValue = dataValue.toString();
         if (stringValue.length() <= shortStringDataSize)
         {
            return new Representation(BigData.STRING_VALUE, BigData.STRING, stringValue);
         }
         else
         {
            return new Representation(BigData.STRING_VALUE, BigData.BIG_STRING,
                  stringValue);
         }
      }
      else if (dataValue instanceof Serializable)
      {
         try
         {
            String stringifiedObject = new String(Base64.encode(Serialization
                  .serializeObject((Serializable) dataValue)));
            if (stringifiedObject.length() <= shortStringDataSize)
            {
               return new Representation(BigData.STRING_VALUE, BigData.SERIALIZABLE,
                     stringifiedObject);
            }
            else
            {
               return new Representation(BigData.STRING_VALUE, BigData.BIG_SERIALIZABLE,
                     stringifiedObject);
            }
         }
         catch (IOException e)
         {
            throw new InternalException("Cannot serialize value for data.", e);
         }
      }
      else
      {
         throw new InternalException("Type '" + dataValue.getClass().getName()
               + "' not supported (check if the type is serializable).");
      }
   }

   /**
    * public static ClosableIterator getObjects(Class type, Object value)
    * {
    * // @todo 2.0.2
    * Class valueType = value.getClass();
    * if (valueType == String.class)
    * {
    * if (((String) value).length() < 1000)
    * {
    * DatabaseDriver databasedriver = (DatabaseDriver) AuditTrailTxContextHelper.
    * getCurrentConnection();
    * return databasedriver.getIterator(type,
    * "type_value = " + BigData.STRING + " and string_value = " + value);
    * }
    * }
    * }
    */
   public LargeStringHolderBigDataHandler(BigData data)
   {
      this.data = data;
      persistentType = data.getClass();
   }

   private long getOID()
   {
      return data.getOID();
   }
   
   private boolean considerDisk()
   {
      return (data instanceof Persistent)
            && (null != ((Persistent) data).getPersistenceController())
            && !((Persistent) data).getPersistenceController().isCreated();
   }

   public Object read()
   {
      if (value != UNKNOWN_OBJECT)
      {
         return value;
      }
      else if (data.getType() == BigData.NULL)
      {
         value = null;
      }
      else if (data.getType() == BigData.SHORT)
      {
         value = new Short((short) data.getLongValue());
      }
      else if (data.getType() == BigData.INTEGER)
      {
         value = new Integer((int) data.getLongValue());
      }
      else if (data.getType() == BigData.LONG)
      {
         value = new Long(data.getLongValue());
      }
      else if (data.getType() == BigData.BYTE)
      {
         value = new Byte((byte) data.getLongValue());
      }
      else if (data.getType() == BigData.BOOLEAN)
      {
         value = data.getLongValue() == 1 ? Boolean.TRUE : Boolean.FALSE;
      }
      else if (data.getType() == BigData.DATE)
      {
         value = new Date(data.getLongValue());
      }
      else if (data.getType() == BigData.FLOAT)
      {
         value = new Float(data.getShortStringValue());
      }
      else if (data.getType() == BigData.DOUBLE)
      {
         value = new Double(data.getShortStringValue());
      }
      else if (data.getType() == BigData.CHAR)
      {
         value = new Character(StringUtils.isEmpty(data.getShortStringValue())
               ? (char) 0
               : data.getShortStringValue().charAt(0));
      }
      else if (data.getType() == BigData.MONEY)
      {
         value = new Money(data.getShortStringValue());
      }
      else if (data.getType() == BigData.PERIOD)
      {
         value = new Period(data.getShortStringValue());
      }
      else if (data.getType() == BigData.STRING)
      {
         value = data.getShortStringValue();
      }
      else if (data.getType() == BigData.BIG_STRING)
      {
         value = LargeStringHolder.getLargeString(getOID(), persistentType, considerDisk());
      }
      else if (data.getType() == BigData.SERIALIZABLE)
      {
         try
         {
            value = Serialization.deserializeObject(
                  Base64.decode(data.getShortStringValue().getBytes()));
         }
         catch (IOException e)
         {
            throw new InternalException("Cannot deserialize value of data.", e);
         }
         catch (ClassNotFoundException e)
         {
            throw new InternalException("Cannot deserialize value of data.", e);
         }
      }
      else if (data.getType() == BigData.BIG_SERIALIZABLE)
      {
         final int maxTries = 2;
         int triesLeft = maxTries;
         while (triesLeft > 0)
         {
            --triesLeft;
            
            String stringifiedValue = LargeStringHolder.getLargeString(getOID(),
                  persistentType, considerDisk());
            if ( !StringUtils.isEmpty(stringifiedValue))
            {
               try
               {
                  value = Serialization.deserializeObject(Base64.decode(stringifiedValue
                        .getBytes()));
                  break;
               }
               catch (IOException e)
               {
                  if (0 == triesLeft)
                  {
                     throw new InternalException("Cannot deserialize value of data in " + maxTries + " tries.", e);
                  }
               }
               catch (ClassNotFoundException e)
               {
                  if (0 == triesLeft)
                  {
                     throw new InternalException("Cannot deserialize value of data in " + maxTries + " tries.", e);
                  }
               }
               catch(InternalException e)
               {
                  if (0 == triesLeft)
                  {
                     throw new InternalException("Cannot deserialize value of data in " + maxTries + " tries.", e);
                  }
               }
               
               trace.warn("Problems while deserializing data of type BIG_SERIALIZABLE. Try again.");
            }
            else
            {
               value = null;
               break;
            }
         }
      }
      else
      {
         throw new InternalException("Type '" + data.getType() + "' not known.");
      }
      return value;
   }

   public void refresh()
   {
      if (data.getType() == BigData.BIG_SERIALIZABLE || data.getType() == BigData.BIG_STRING)
      {
         LargeStringHolder.deleteAllForOID(getOID(), persistentType, considerDisk());
      }

      if (value == null)
      {
         data.setLongValue(0);
         data.setShortStringValue(null);
         data.setType(BigData.NULL);
         return;
      }

      if (value instanceof Short)
      {
         data.setLongValue(((Short) value).shortValue());
         data.setType(BigData.SHORT);
      }
      else if (value instanceof Integer)
      {
         data.setLongValue(((Integer) value).intValue());
         data.setType(BigData.INTEGER);
      }
      else if (value instanceof Long)
      {
         data.setLongValue(((Long) value).longValue());
         data.setType(BigData.LONG);
      }
      else if (value instanceof Byte)
      {
         data.setLongValue(((Byte) value).byteValue());
         data.setType(BigData.BYTE);
      }
      else if (value instanceof Boolean)
      {
         data.setLongValue(((Boolean) value).booleanValue() ? 1 : 0);
         data.setType(BigData.BOOLEAN);
      }
      else if (value instanceof Date)
      {
         data.setLongValue(((Date) value).getTime());
         data.setType(BigData.DATE);
      }
      else if (value instanceof Float)
      {
         data.setShortStringValue(value.toString());
         data.setType(BigData.FLOAT);
      }
      else if (value instanceof Double)
      {
         data.setShortStringValue(value.toString());
         data.setType(BigData.DOUBLE);
      }
      else if (value instanceof Character)
      {
         data.setShortStringValue((((char) 0) == ((Character) value).charValue())
               ? null
               : value.toString());
         data.setType(BigData.CHAR);
      }
      else if (value instanceof Money)
      {
         data.setShortStringValue(value.toString());
         data.setType(BigData.MONEY);
      }
      else if (value instanceof Period)
      {
         data.setShortStringValue(value.toString());
         data.setType(BigData.PERIOD);
      }
      else if (value instanceof String)
      {
         writeStringValue((String) value, BigData.STRING, BigData.BIG_STRING,
               considerDisk());
      }
      else
      {
         String stringifiedValue;
         try
         {
            if (value instanceof Serializable)
            {
               stringifiedValue = new String(Base64.encode(Serialization
                     .serializeObject((Serializable) value)));
            }
            else
            {
               throw new NotSerializableException(value.getClass().getName());
            }
         }
         catch (NotSerializableException e)
         {
            String message = "Input not serializable: " + e.getMessage();
            throw new PublicException(message);
         }
         catch (IOException e)
         {
            throw new InternalException("Cannot serialize value for data.", e);
         }
         writeStringValue(stringifiedValue, BigData.SERIALIZABLE,
               BigData.BIG_SERIALIZABLE, considerDisk());
      }
   }

   public void write(Object value, boolean forceRefresh)
   {
      if ( !forceRefresh)
      {
         if (((null == value) && (null == this.value))
               || ((null != value) && (null != this.value) && value.equals(this.value)))
         {
            return;
         }
      }

      this.value = value;

      refresh();
   }

   private void writeStringValue(String value, int smalltype, int bigtype,
         boolean considerDisk)
   {
      if (value.length() <= data.getShortStringColumnLength())
      {
         data.setShortStringValue(value);
         data.setType(smalltype);
      }
      else
      {
         data.setShortStringValue(value.substring(0, data.getShortStringColumnLength()));
         data.setType(bigtype);
         LargeStringHolder.setLargeString(getOID(), persistentType, value, considerDisk);
      }
   }

   public static class Representation
   {
      private final int classificationKey;
      private final int typeKey;
      private final Object representation;

      private Representation(int classificationKey, int typeKey, Object representation)
      {
         this.classificationKey = classificationKey;
         this.typeKey = typeKey;
         this.representation = representation;
      }

      public int getClassificationKey()
      {
         return classificationKey;
      }

      public int getTypeKey()
      {
         return typeKey;
      }

      public int getSizeNeutralTypeKey()
      {
         int sizeNeutralTypeKey;

         switch (typeKey)
         {
            case BigData.BIG_STRING:
               sizeNeutralTypeKey = BigData.STRING;
               break;

            case BigData.BIG_SERIALIZABLE:
               sizeNeutralTypeKey = BigData.SERIALIZABLE;
               break;

            default:
               sizeNeutralTypeKey = typeKey;
         }

         return sizeNeutralTypeKey;
      }

      public Object getRepresentation()
      {
         return representation;
      }

      public boolean isLarge()
      {
         return (typeKey == BigData.BIG_STRING)
               || (typeKey == BigData.BIG_SERIALIZABLE);
      }
   }
}