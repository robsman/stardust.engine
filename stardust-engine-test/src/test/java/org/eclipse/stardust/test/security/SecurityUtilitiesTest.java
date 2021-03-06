/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Thomas.Wolfram (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.security;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.security.SecurityProvider;
import org.eclipse.stardust.common.security.SecurityUtilities;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.vfs.impl.utils.CollectionUtils;

public class SecurityUtilitiesTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BasicWorkflowModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf).around(testMethodSetup);

   @Test
   public void testGenerateRandoms()
   {
      SecurityProvider provider = SecurityUtilities.getSecurityProvider();

      boolean theBoolean = provider.getRandomBoolean();
      assertNotNull(theBoolean);

      byte[] theBytes = provider.getRandomBytes(5);
      assertNotNull(theBytes);
      assertTrue(theBytes.length == 5);

      String theFilename = provider.getRandomFilename("txt");
      assertNotNull(theFilename);
      assertTrue(theFilename.endsWith(".txt"));

      String theGUID = provider.getRandomGUID();
      assertNotNull(theGUID);
      assertEquals("UUID has expected length", 36, theGUID.length());

      int theInt = provider.getRandomInteger(0, Integer.MAX_VALUE);
      assertNotNull(theInt);

      long theLong = provider.getRandomLong();
      assertNotNull(theLong);

      float theFloat = provider.getRandomReal(Float.MIN_VALUE, Float.MAX_VALUE);
      assertNotNull(theFloat);

      char[] alphabet = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
      String theString = provider.getRandomString(50, alphabet);
      assertNotNull(theString);
      assertTrue(theString.length() == 50);
   }

   @Test
   public void testEncode()
   {
      SecurityProvider provider = SecurityUtilities.getSecurityProvider();

      String encodedURLString = provider.encodeForURL("random word £500 bank $");
      assertFalse(encodedURLString.contains("$"));
      assertFalse(encodedURLString.contains("£"));

      String encodedJavaScript = provider.encodeForJavaScript("Need tips? 'Visit W3Schools!'");
      // Test condition to be inserted

      String encodedForXml = provider.encodeForXML("characters such as & and > are not allowed");
      assertTrue(encodedForXml.contains("&#x26;") || encodedForXml.contains("&amp;"));
      assertTrue(encodedForXml.contains("&#x3e;") || encodedForXml.contains("&gt;"));

      String encodedForXmlAttribute = provider.encodeForXMLAttribute("characters such as & and > are not allowed");
      assertTrue(encodedForXmlAttribute.contains("&#x26;")
            || encodedForXmlAttribute.contains("&amp;"));
      assertTrue(encodedForXmlAttribute.contains("&#x3e;")
            || encodedForXmlAttribute.contains("&gt;"));

      String encodedForHtml = provider.encodeForHTML("characters like ä and ü as well as < and > should be escaped");
      assertFalse(encodedForHtml.contains("ä"));
      assertFalse(encodedForHtml.contains("ü"));
      assertFalse(encodedForHtml.contains("<"));
      assertFalse(encodedForHtml.contains(">"));

   }

   @Test
   public void testValidateValues()
   {
      SecurityProvider provider = SecurityUtilities.getSecurityProvider();

      DateFormat df = new SimpleDateFormat("d.M.y");
      Calendar dateFormatCalendar = df.getCalendar();
      Date validDate = provider.getValidDate("A_Date", "02.03.2016", df, false);
      dateFormatCalendar.setTime(validDate);
      assertThat(dateFormatCalendar.get(Calendar.DAY_OF_MONTH), is(2));
      assertThat(dateFormatCalendar.get(Calendar.MONTH), is(Calendar.MARCH));
      assertThat(dateFormatCalendar.get(Calendar.YEAR), is(2016));

      Double validNumber = provider.getValidNumber("A_Number", "48.5", 0, 100, false);
      assertTrue(validNumber == 48.5);

      List<String> extensions = CollectionUtils.newList();
      extensions.add("txt");
      extensions.add("log");

      String validFileName = provider.getValidFileName("A_Filename", "logger.txt",
            extensions, false);
      assertNotNull(validFileName);
      assertTrue(validFileName.endsWith(".txt"));

      String validInput = provider.getValidInput("A_Input", "MyInput", "SafeString", 50,
            false);
      assertNotNull(validInput);

   }
}
