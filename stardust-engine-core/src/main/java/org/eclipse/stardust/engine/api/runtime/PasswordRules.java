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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * @author Barry.Grotjahn
 * @version $Revision: $
 */
public interface PasswordRules extends Serializable
{  
   /**
    * Checks if flag for unique password is set.
    * 
    * @return if is enabled.
    */
   boolean isUniquePassword();
   
   /**
    * Set unique password flag.
    * The flag is the super flag if to compare passwords with previous passwords.
    * 
    * @param true or false
    */
   void setUniquePassword(boolean uniquePassword);

   /**
    * Checks if force password change flag is set.
    * 
    * @return if is enabled.
    */
   boolean isForcePasswordChange();
   
   /**
    * Set force password change flag.
    * The flag is the super flag if passwords will expire.
    * 
    * @param true or false
    */
   void setForcePasswordChange(boolean forcePasswordChange);

   /**
    * Checks if strong password flag is set.
    * 
    * @return if is enabled.
    */
   boolean isStrongPassword();
   
   /**
    * Set strong password flag.
    * The flag is the super flag if to follow password rules.
    * 
    * @param true or false
    */
   void setStrongPassword(boolean strongPassword);
      
   /**
    * Enables when notification mail should be send out before password expires.
    * If days is set to 0 no mails will be send.
    * 
    * @param days Number of days before password expires.
    */
   void setNotificationMails(int days);
   
   /**
    * Check if notification mails should be send.
    * 
    * @return Number of days before password expires or 0.
    */
   int getNotificationMails();

   
   /**
    * Enables password tracking if value is > 0.
    * If password tracking is enabled store a number of previous passwords.
    * The new password should not be one of the stored previous passwords. 
    * 
    * @param days Number of old passwords to store.
    */
   void setPasswordTracking(int number);
   
   /**
    * Check if password tracking is enabled.
    * 
    * @return Number of passwords to track.
    */
   int getPasswordTracking();
   
   
   /**
    * Sets the days a new password will be valid.
    * 
    * @param Number of days.
    */
   void setExpirationTime(int days); 
   
   /**
    * Return number of days after the password will expire (starting from day the current pasword was set).
    * 
    * @return Number of days.
    */
   int getExpirationTime();
   

   /**
    * Sets the days after the user will be disabled (after password expired).
    * 
    * @param Number of days (-1 will never disable the user).
    */
   void setDisableUserTime(int days);
   
   /**
    * Gets the days after the user will be disabled (after password expired).
    * 
    * @return Number of days.
    */
   int getDisableUserTime();

   
   /**
    * Sets the number of minimum different characters in the new password compared to previous passwords.
    * 
    * @param length Number of different characters.
    */
   void setDifferentCharacters(int length);
   
   /**
    * ´Gets the number of minimum different characters in the new password compared to previous passwords.
    * 
    * @return Number.
    */
   int getDifferentCharacters();
   
   
   /**
    * Sets the minimum password length.
    * 
    * @param length Length.
    */
   void setMinimalPasswordLength(int length);
   
   /**
    * Gets the minimum password length.
    * 
    * @return Length.
    */
   int getMinimalPasswordLength();
   
   
   /**
    * Sets the minimum number of Letters in new password.
    * 
    * @param length Number.
    */
   void setLetters(int length);
   
   /**
    * Gets the minimum number of Letters in new password.
    * 
    * @return Number.
    */
   int getLetters();

   
   /**
    * Sets the minimum number of Digits in new password.
    * 
    * @param length Number.
    */
   void setDigits(int length);
   
   /**
    * Gets the minimum number of Digits in new password.
    * 
    * @return Number.
    */
   int getDigits();

   
   /**
    * The password needs to include at least n lowercase (a-z) and n uppercase (A-Z) characters.
    * 
    * @param length Number.
    */
   void setMixedCase(int length);
   
   /**
    * Gets the number for mixed case characters.
    * 
    * @return Number.
    */
   int getMixedCase();
   
   
   /**
    * Sets the minimum number of punctuation characters ('!"$%&#()=?+/*) in new password.
    * 
    * @param length Number.
    */
   void setPunctuation(int length);
   
   /**
    * Sets the minimum number of punctuation characters ('!"$%&#()=?+/*) in new password.
    * 
    * @return Number.
    */
   int getPunctuation();
}