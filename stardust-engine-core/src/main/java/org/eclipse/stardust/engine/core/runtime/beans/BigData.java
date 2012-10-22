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

/**
 * @todo This interface introduces a coupling from the big data handling mechanics to
 * the interfaces of the persistent objects which use it. Maybe it is more nice to hide
 * it a bit by using metadata hints in the definition of the persistent object fiels.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface BigData
{
   /**
    * Value will be written as either {@link #NULL_VALUE}, {@link #NUMERIC_VALUE} or
    * {@link #STRING_VALUE} depending on actual value.
    */
   int UNKNOWN_VALUE = 0;

   /**
    * Value will be written as <code>NULL</code>.
    */
   int NULL_VALUE = 1;

   /**
    * Value will be written as numerical value.
    */
   int NUMERIC_VALUE = 2;

   /**
    * Value will be written as stringified value.
    */
   int STRING_VALUE = 3;

   /**
    * Value will be written as stringified + double value, 
    * double value is used for sorting only.
    */
   int DOUBLE_VALUE = 4;

   int BOOLEAN = 0;
   int CHAR = 1;
   int BYTE = 2;
   int SHORT = 3;
   int INTEGER = 4;
   int LONG = 5;
   int FLOAT = 6;
   int DOUBLE = 7;
   int STRING = 8;
   int DATE = 9;
   int MONEY = 10;
   int BIG_STRING = 11;
   int SERIALIZABLE = 12;
   int BIG_SERIALIZABLE = 13;
   int NULL = -1;
   int PERIOD = 14;

   void setShortStringValue(String value);

   void setLongValue(long value);
   
   void setDoubleValue(double value);

   int getType();

   long getLongValue();

   long getOID();

   void setType(int type);

   String getShortStringValue();
   
   public double getDoubleValue();

   int getShortStringColumnLength();
}
