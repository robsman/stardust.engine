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
package org.eclipse.stardust.common.security.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;



public class PasswordValidation
{
   public static void validate(char[] password, PasswordRules rules, List<String> previousPasswords) throws InvalidPasswordException
   {     
      if(rules == null)
      {
         return;
      }      
      
      List<InvalidPasswordException.FailureCode> failureCodes = new ArrayList<InvalidPasswordException.FailureCode>();
      
      String value = new String(password);
      
      if(rules.isStrongPassword())
      {
         if(value.length() < rules.getMinimalPasswordLength())
         {
            failureCodes.add(InvalidPasswordException.FailureCode.MINIMAL_PASSWORD_LENGTH);
         }
         
         if(rules.getLetters() > 0)
         {
            if(value.replaceAll("[^a-zA-z]", "").length() < rules.getLetters())
            {
               failureCodes.add(InvalidPasswordException.FailureCode.LETTER);
            }
         }
         
         if(rules.getDigits() > 0)
         {
            if(value.replaceAll("[^0-9]", "").length() < rules.getDigits())
            {
               failureCodes.add(InvalidPasswordException.FailureCode.DIGITS);
            }
         }
   
         if(rules.getMixedCase() > 0)
         {
            if(value.replaceAll("[^a-z]", "").length() < rules.getMixedCase()
                  || value.replaceAll("[^A-Z]", "").length() < rules.getMixedCase())
            {
               failureCodes.add(InvalidPasswordException.FailureCode.MIXED_CASE);
            }
         }
         
         if(rules.getPunctuation() > 0)
         {
            if(value.replaceAll("[^\'\"$!?#%&=+*)(/]", "").length() < rules.getPunctuation())
            {
               failureCodes.add(InvalidPasswordException.FailureCode.PUNCTUATION);
            }
         }
      }

      if(rules.isUniquePassword())
      {
         if(previousPasswords != null && !previousPasswords.isEmpty())
         {         
            for(String previousPassword : previousPasswords)
            {
               if(previousPassword.equals(value))
               {
                  failureCodes.add(InvalidPasswordException.FailureCode.PREVIOUS_PASSWORDS);
               }
               
               if(rules.getDifferentCharacters() > 0)
               {            
                  if(previousPassword.length() > value.length())
                  {
                     int difference = previousPassword.length() - value.length();                  
                     if(!hasDifferentCharacters(value, previousPassword, rules.getDifferentCharacters() - difference))
                     {
                        failureCodes.add(InvalidPasswordException.FailureCode.DIFFERENT_CHARACTERS);
                     }
                  }
                  else
                  {
                     int difference = value.length() - previousPassword.length();                  
                     if(!hasDifferentCharacters(previousPassword, value, rules.getDifferentCharacters() - difference))
                     {
                        failureCodes.add(InvalidPasswordException.FailureCode.DIFFERENT_CHARACTERS);
                     }               
                  }            
               }                        
            }
         }
      }
      
      if ( !failureCodes.isEmpty())
      {
         throw new InvalidPasswordException(
               BpmRuntimeError.AUTHx_CHANGE_PASSWORD_NEW_PW_VERIFICATION_FAILED.raise(),
               failureCodes);
      }
   }   
   
   private static boolean hasDifferentCharacters(String value, String otherValue, int differentCharacters)
   {               
      int cnt = 0;
      
      for(int j = 0; j < otherValue.length(); j++)
      {         
         if(j >= value.length())
         {
            break;
         }
            
         if(otherValue.charAt(j) != value.charAt(j))
         {
            cnt++;
         }
      }
      
      if(cnt >= differentCharacters)
      {
         return true;
      }         
      return false;
   }
}