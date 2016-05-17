/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.common.security;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.error.SecurityException;

/**
 * 
 * @author thomas.wolfram
 *
 */

@SPI(status = Status.Internal, useRestriction = UseRestriction.Internal)
public interface SecurityProvider
{

   // Encoder

   /**
    * 
    * Encodes data for use in XPath query
    * 
    * @param input
    *           - the text to encode for XPath
    * @return encoded String for use in XPath
    */
   String encodeForXPath(String input);

   /**
    * 
    * Encodes data for use in an XML attribite
    * 
    * @param input
    *           - the text to encode for an XML attribute
    * @return encoded String for use in XML attribute
    */
   String encodeForXMLAttribute(String input);

   /**
    * 
    * Encode data for use in an XML element
    * 
    * @param input
    *           - the text to encode for an XML element
    * @return encoded String for use in an XML element
    */
   String encodeForXML(String input);

   /**
    * 
    * Encode string for use in an URL
    * 
    * @param input
    *           - the text to encode for an URL
    * @return encoded String for use in an URL
    */
   String encodeForURL(String input);

   /**
    * 
    * Encode data for insertion inside a data value or function argument in JavaScript
    * 
    * @param input
    *           - the text to encode for JavaScript
    * @return encoded String for use in JavaScript
    */
   String encodeForJavaScript(String input);

   /**
    * 
    * Encode data for use in HTML attributes
    * 
    * @param input
    *           - the text to encode for an HTML attribute
    * @return encoded String for use in HTML attribute
    */
   String encodeForHTMLAttribute(String input);

   /**
    * 
    * Encode data for use in HTML elements
    * 
    * @param input
    *           - the text to encode for an HTML elements
    * @return encoded String for use in HTML element
    */
   String encodeForHTML(String input);

   // Randomizer

   /**
    * 
    * Returns a random boolean
    * 
    * @return true or false
    */
   boolean getRandomBoolean();

   /**
    * 
    * Generates a specified number of random bytes
    * 
    * @param n
    *           - requested number of random bytes
    * @return random bytes
    */
   byte[] getRandomBytes(int n);

   /**
    * 
    * Generates a specified number of random bytes
    * 
    * @param extension
    *           - extension to add to the random filename
    * @return random filename
    */
   String getRandomFilename(String extension);

   /**
    * 
    * Generates a random GUID
    * 
    * @return the GUID
    */
   String getRandomGUID();

   /**
    * 
    * Generates a random integer
    * 
    * @param min
    *           - minimum integer that will be returned
    * @param max
    *           - maximum integer that will be returned
    * @return the random integer
    */
   int getRandomInteger(int min, int max);

   /**
    * 
    * Generates a random long
    * 
    * @return the random long
    */
   long getRandomLong();

   /**
    * 
    * Generates a reandom real
    * 
    * @param min
    *           - minimum real number that will be returned
    * @param max
    *           - maximum real number that will be returned
    * @return the random real
    */
   float getRandomReal(float min, float max);

   /**
    * 
    * Generates a random string of a desired length and character set
    * 
    * @param length
    *           - the length of the string
    * @param characterSet
    *           - set of charaters allowed in the generated string
    * @return the random string
    */
   String getRandomString(int length, char[] characterSet);

   // Validator

   /**
    * 
    * Returns a valid date as a Date. Invalid input will generate a
    * {@link SecurityException}
    * 
    * @param context
    *           - A descriptive name of the parameter that you are validating
    * @param input
    *           - The actual user input data to validate
    * @param format
    *           - Required formatting of date
    * @param allowNull
    *           - If allowNull is true then an input that is NULL or an empty string will
    *           be legal
    * @return a valid Date
    */
   Date getValidDate(String context, String input, DateFormat format, boolean allowNull);

   /**
    * 
    * Returns a valid number. Invalid input will generate a {@link SecurityException}
    * 
    * @param context
    *           - A descriptive name of the parameter that you are validating
    * @param input
    *           - The actual user input data to validate
    * @param minValue
    *           - Lowest legal value for input
    * @param maxValue
    *           - Highest legal value for input
    * @param allowNull
    *           - If allowNull is true then an input that is NULL or an empty string will
    *           be legal
    * @return A validated number
    */
   Double getValidNumber(String context, String input, long minValue, long maxValue,
         boolean allowNull);

   /**
    * 
    * Returns a valid file name. Invalid input will generate a {@link SecurityException}
    * 
    * @param context
    *           - A descriptive name of the parameter that you are validating
    * @param input
    *           - The actual user input data to validate
    * @param allowedExtensions
    *           - A list of allowed file extensions
    * @param allowNull
    *           - If allowNull is true then an input that is NULL or an empty string will
    *           be legal
    * @return a valid file name
    */
   String getValidFileName(String context, String input, List<String> allowedExtensions,
         boolean allowNull);

   /**
    * Returns a valid redirection location</br> <b>Attention:</b> This is for local
    * redirects only and must be configured in the specific security provider
    * implementation
    * 
    * @param context
    *           - A descriptive name of the parameter that you are validating
    * @param input
    *           - The actual user input data to validate
    * @param allowNull
    *           - If allowNull is true then an input that is NULL or an empty string will
    *           be legal
    * @return A valid redirection location
    */
   String getValidRedirectionLocation(String context, String input, boolean allowNull);

   /**
    * 
    * Returns validated input as a String
    * 
    * @param context
    *           A descriptive name of the parameter that you are validating
    * @param input
    *           - The actual user input data to validate
    * @param type
    *           - The type of the configured regular expression to validate against
    * @param maxLength
    *           - The maximum String length allowed
    * @param allowNull
    *           - If allowNull is true then an input that is NULL or an empty string will
    *           be legal
    * @return A valid user input
    */
   String getValidInput(String context, String input, String type, int maxLength,
         boolean allowNull);

   // HTTPUtilities

   /**
    * Add a header to the response
    * 
    * @param response
    *           - the servlet response
    * @param name
    *           - name of the header field
    * @param value
    *           - value of the header field
    */
   void addHeader(HttpServletResponse response, String name, String value);

   /**
    * Set a header for the response
    * 
    * @param response
    *           - the servlet response
    * @param name
    *           - name of the header field
    * @param value
    *           - value of the header field
    */   
   void setHeader(HttpServletResponse response, String name, String value);

   /**
    * Factory for {@link SecurityProvider}
    */
   public interface Factory
   {
      SecurityProvider getInstance();
   }

}
