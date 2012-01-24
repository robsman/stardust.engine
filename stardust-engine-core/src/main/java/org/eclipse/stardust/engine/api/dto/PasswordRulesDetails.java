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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.runtime.PasswordRules;

public class PasswordRulesDetails implements PasswordRules
{
   private static final long serialVersionUID = 1L;   

   private boolean uniquePassword = false;
   private int passwordTracking = 0;
   private int differentCharacters = 0;
      
   private boolean forcePasswordChange = false;
   private int expirationTime = 90;
   private int disableUserTime = -1;
   private int sendNotificationMails = 0;
   
   private boolean strongPassword = false;   
   private int minimalPasswordLength = 6;
   private int letters = 0;
   private int digits = 0;
   private int mixedCase = 0;
   private int punctuation = 0;

   public boolean isUniquePassword()
   {
      return uniquePassword;
   }

   public void setUniquePassword(boolean uniquePassword)
   {
      this.uniquePassword = uniquePassword;
   }

   public boolean isForcePasswordChange()
   {
      return forcePasswordChange;
   }

   public void setForcePasswordChange(boolean forcePasswordChange)
   {
      this.forcePasswordChange = forcePasswordChange;
   }

   public boolean isStrongPassword()
   {
      return strongPassword;
   }

   public void setStrongPassword(boolean strongPassword)
   {
      this.strongPassword = strongPassword;
   }

   public void setNotificationMails(int days)
   {
      sendNotificationMails = days;
   }   
   
   public int getNotificationMails()
   {
      return sendNotificationMails;
   }   
   
   public void setPasswordTracking(int days)
   {
      passwordTracking = days;
   }
   
   public int getPasswordTracking()
   {
      return passwordTracking;
   }   
      
   public void setExpirationTime(int days)
   {
      expirationTime = days;
   }
   
   public int getExpirationTime()
   {
      return expirationTime;
   }
      
   public void setDisableUserTime(int days)
   {
      disableUserTime = days;
   }
   
   public int getDisableUserTime()
   {
      return disableUserTime;
   }

   public void setDifferentCharacters(int length)
   {
      differentCharacters = length;
   }
   
   public int getDifferentCharacters()
   {
      return differentCharacters;
   }
   
   public void setMinimalPasswordLength(int length)
   {
      minimalPasswordLength = length;
   }
   
   public int getMinimalPasswordLength()
   {
      int cntLetters = (mixedCase * 2);
      if(letters > cntLetters)
      {
         cntLetters = letters;
      }
      
      int cnt = cntLetters + digits + punctuation;
      if(cnt > minimalPasswordLength)
      {
         return cnt;
      }
      
      return minimalPasswordLength;
   }

   public void setLetters(int length)
   {
      letters = length;
   }

   public int getLetters()
   {
      return letters;
   }
   
   public void setDigits(int length)
   {
      digits = length;
   }
   
   public int getDigits()
   {
      return digits;
   }

   public void setMixedCase(int length)
   {
      mixedCase = length;
   }
   
   public int getMixedCase()
   {
      return mixedCase;
   }

   public void setPunctuation(int length)
   {
      punctuation = length;
   }   
   
   public int getPunctuation()
   {
      return punctuation;
   }
}