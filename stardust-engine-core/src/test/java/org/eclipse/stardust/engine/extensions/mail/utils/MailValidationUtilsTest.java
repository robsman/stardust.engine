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
package org.eclipse.stardust.engine.extensions.mail.utils;

import org.eclipse.stardust.engine.extensions.mail.utils.MailValidationUtils;

import junit.framework.TestCase;

/**
 * @author fuhrmann
 * @version $Revision$
 */
public class MailValidationUtilsTest extends TestCase
{
   public void testIsValidEMailValid()
   {
      assertTrue("Expected true: email is valid", MailValidationUtils
            .isValidEMail("test@email.com"));
      assertTrue("Expected true: email is valid", MailValidationUtils
            .isValidEMail("test@email.co.uk"));
      assertTrue("Expected true: email is valid", MailValidationUtils
            .isValidEMail("test123@email.info"));
   }

   public void testIsValidEMailNoDomain()
   {
      String noDomain = "Expected false, email has no domain: ";
      assertFalse(noDomain, MailValidationUtils.isValidEMail("test@email"));
      assertFalse(noDomain, MailValidationUtils.isValidEMail("test@email."));
   }

   public void testIsValidEMailInvalidDomain()
   {
      String invalidDomain = "Expected false, email has an invalid domain: ";
      assertFalse(invalidDomain, MailValidationUtils.isValidEMail("test@email.a"));
      assertFalse(invalidDomain, MailValidationUtils.isValidEMail("test@email.123"));
      assertFalse(invalidDomain, MailValidationUtils.isValidEMail("test@email.abcde"));
   }

   public void testIsValidEMailInvalidStart()
   {
      String invalidStart = "Expected false, email has an invalid start: ";
      assertFalse(invalidStart, MailValidationUtils.isValidEMail("-test@email.com"));
      assertFalse(invalidStart, MailValidationUtils.isValidEMail("+test@email.com"));
      assertFalse(invalidStart, MailValidationUtils.isValidEMail("\\test@email.com"));
      assertFalse(invalidStart, MailValidationUtils.isValidEMail("&test@email.com"));
   }

   public void testIsValidEMailInvalidCharacters()
   {
      String invalidChar = "Expected false, email has invalid characters: ";
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("test/@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("tes%t@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("te&st@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("test?@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("testß@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("test+@email.com"));
      assertFalse(invalidChar, MailValidationUtils.isValidEMail("test@.email.com"));
   }

   public void testIsValidEMailValidCharacters()
   {
      String invalidChar = "Expected true, email has no invalid characters: ";
      assertTrue(invalidChar, MailValidationUtils.isValidEMail("te-st@email.com"));
      assertTrue(invalidChar, MailValidationUtils.isValidEMail("te_st@email.com"));
   }

}
