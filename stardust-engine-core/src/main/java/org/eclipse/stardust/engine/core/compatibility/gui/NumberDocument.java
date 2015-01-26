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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Document to restrict entry content to string input compatible to number
 * types.
 */
public class NumberDocument extends PlainDocument
{
   private static final Logger trace = LogManager.getLogger(NumberDocument.class);

   static protected final String STRING_MINUS = "-";
   static protected final String STRING_NULL = "0";

   private Class type;
   private NumberFormat format;
   private char separator;

   /**
    * Constrcuts a document which carries locale compliant representations of
    * the instances of <code>type</code>.
    */
   public NumberDocument(Class type)
   {
      super();

      this.type = type;

      String formatPattern = null;

      if (type == Byte.class)
      {
         formatPattern = "####";
      }
      else if (type == Short.class)
      {
         formatPattern = "###.###;-###.###";
      }
      else if (type == Integer.class)
      {
         formatPattern = "##,###,###,##0;-##,###,###,##0";
      }
      else if (type == Long.class)
      {
         formatPattern = "#,###,###,###,###,###,###,###,###,###,##0;-#,###,###,###,###,###,###,###,###,###,##0";
      }
      else if (type == Float.class)
      {
         formatPattern = "#,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,##0.#####################;-#,###,###,###,###,###,###,###,###,###,##0.#####################";
      }
      else if (type == Double.class)
      {
         formatPattern = "#,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,##0.#####################;-#,###,###,###,###,###,###,###,###,###,##0.#####################";
      }
      else
      {
         trace.debug( "[Error] unexpected class type: " + type.getName());
      }

      format = new DecimalFormat(formatPattern);

      if (type == Float.class || type == Double.class)
      {
         // Trick to obtain the fraction separator of the locale

         separator = format.format(1.5).charAt(1);
      }

      format.setParseIntegerOnly(false);
   }
   
   public Number getNumber()
   {
      Number number;
      try
      {
         number = format.parse(getText(0, getLength()));
      }
      catch (ParseException e)
      {
         number = null;
      }
      catch (BadLocationException e)
      {
         number = null;
      }
      return number;
   }

   /**
    */
   protected NumberFormat getFormatObject()
   {
      return format;
   }

   /**
    * Handles string insertion
    * @param offs The offset as a starting point for the insertion
    * @param str The string that will be inserted at position offs
    * @param a An AttributeSet
    */
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      if (str == null)
      {
         return;
      }

      String _oldString = getText(0, getLength());
      String _newString = _oldString.substring(0, offs);

      _newString += str;
      _newString += _oldString.substring(offs, getLength());

      if (_newString.length() == 0)
      {
         return;
      }

      // Allow single leading minus
      // hint: this case is not covered by the decimal format anyway.
      if (_newString.equals(STRING_MINUS) &&
            _oldString.length() == 0)
      {
         super.insertString(offs, str, a);
         return;
      }

      // Possibly allow single trailing separator
      if (_newString.charAt(_newString.length() - 1) == separator &&
            (type == Float.class || type == Double.class))
      {
         if (_oldString.lastIndexOf(separator) >= 0)
         {
            return;
         }

         super.insertString(offs, str, a);
         return;
      }

      try
      {
         Number number = format.parse(_newString);
         if (type == Byte.class)
         {
            if (number instanceof Long)
            {
               long value = number.longValue();

               if (value >= Byte.MIN_VALUE &&
                     value <= Byte.MAX_VALUE &&
                     value != Unknown.BYTE)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else if (type == Short.class)
         {
            if (number instanceof Long)
            {
               long value = number.longValue();

               if (value >= Short.MIN_VALUE &&
                     value <= Short.MAX_VALUE &&
                     value != Unknown.SHORT)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else if (type == Integer.class)
         {
            if (number instanceof Long)
            {
               long value = number.longValue();

               if (value >= Integer.MIN_VALUE &&
                     value <= Integer.MAX_VALUE &&
                     value != Unknown.INT)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else if (type == Long.class)
         {
            if (number instanceof Long)
            {
               long value = number.longValue();

               if (value >= Long.MIN_VALUE &&
                     value <= Long.MAX_VALUE &&
                     value != Unknown.LONG)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else if (type == Float.class)
         {
            // Allow a '0' at the end of the string's fractional part (otherwise
            // it will be cut of by the format call for float or double)
            if (str.equals(STRING_NULL) && (-1 != _oldString.indexOf(separator))
                  && (offs >= _oldString.length())
            )
            {
               super.insertString(offs, str, a);
            }
            else
            {
               float value = number.floatValue();
               // hint: Remind that Float.MIN_VALUE isn't the absolut smallest
               //       float value!! It is the smalles positive (!!!) value.
               if (Math.abs(value) <= Float.MAX_VALUE &&
                     value != Unknown.FLOAT)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else if (type == Double.class)
         {
            // Allow a '0' at the end of the string's fractional part (otherwise
            // it will be cut of by the format call for float or double)
            if (str.equals(STRING_NULL) && (-1 != _oldString.indexOf(separator))
                  && (offs >= _oldString.length())
            )
            {
               super.insertString(offs, str, a);
            }
            else
            {
               double value = number.doubleValue();
               if (value != Unknown.DOUBLE)
               {
                  remove(0, getLength());
                  super.insertString(0, format.format(value), a);
               }
            }
         }
         else
         {
            trace.debug( "[Error] unexpected number class: " + type);
         }
      }
      catch (NumberFormatException x)
      {
         trace.warn("", x);
      }
      catch (ParseException x)
      {
         trace.warn("", x);
      }
   }
}
