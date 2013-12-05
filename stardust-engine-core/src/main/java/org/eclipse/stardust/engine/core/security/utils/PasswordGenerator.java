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
package org.eclipse.stardust.engine.core.security.utils;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;


public class PasswordGenerator
{
   private static Integer OTHER = new Integer(0);
   private static Integer LOWER = new Integer(1);
   private static Integer UPPER = new Integer(2);
   private static Integer LETTER = new Integer(3);
   private static Integer DIGIT = new Integer(4);
   private static Integer PUNCTUATION = new Integer(5);   
   
   public static char[] generatePassword(PasswordRules rules, List<String> previousPasswords)
   {
      boolean isValid = false;
      //Random randomGenerator = new Random(); 
      SecureRandom randomGenerator = new SecureRandom();

      int length = 10;
      if(rules != null && rules.getMinimalPasswordLength() > length)
      {
         length = rules.getMinimalPasswordLength();
      }      
      char[] password = new char[length];
      
      while(!isValid)
      {
         Map<Integer, Integer> pool = createPool(rules);
         for (int idx = 0; idx < length; idx++)
         {
            if(rules == null || !rules.isStrongPassword())
            {
               password[idx] = getCharacter(OTHER);                        
            }
            else
            {
               if(pool.size() == 0)
               {
                  password[idx] = getCharacter(OTHER);                                          
               }
               else
               {
                  int selection = randomGenerator.nextInt(pool.size());
                  Integer choice = updatePool(pool, selection);               
                  password[idx] = getCharacter(choice);
               }
            }
         }      
         
         try
         {
            PasswordValidation.validate(password, rules, previousPasswords);
            isValid = true;
         }
         catch (InvalidPasswordException e)
         {
         }
      }      
      
      return password;
   }
   
   private static char getCharacter(Integer choice)
   {
      SecureRandom randomGenerator = new SecureRandom();      

      String letterCharactersLower = "abcdefghijklmnopqrstuvwxyz";
      String letterCharactersUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      String letterCharacters = new String(letterCharactersLower).concat(letterCharactersUpper);      
      
      String digitCharacters = "1234567890";
      String punctuationCharacters = "'!$%&#()=?+*/"; // without \" to be able to parse 
      String characters = new String(letterCharacters).concat(digitCharacters).concat(punctuationCharacters);
      
      if(choice == LOWER)
      {
         int randomInt = randomGenerator.nextInt(letterCharactersLower.length());
         return letterCharactersLower.charAt(randomInt);                                 
      }
      else if(choice == UPPER)
      {
         int randomInt = randomGenerator.nextInt(letterCharactersUpper.length());
         return letterCharactersUpper.charAt(randomInt);                            
      }
      else if(choice == LETTER)
      {
         int randomInt = randomGenerator.nextInt(letterCharacters.length());
         return letterCharacters.charAt(randomInt);                                          
      }
      else if(choice == DIGIT)
      {
         int randomInt = randomGenerator.nextInt(digitCharacters.length());
         return digitCharacters.charAt(randomInt);                                                   
      }
      else if(choice == PUNCTUATION)
      {
         int randomInt = randomGenerator.nextInt(punctuationCharacters.length());
         return punctuationCharacters.charAt(randomInt);                                                            
      }
      else
      {
         int randomInt = randomGenerator.nextInt(characters.length());
         return characters.charAt(randomInt);                                                            
      }
   }   
   
   private static Integer updatePool(Map<Integer, Integer> pool, int selection)
   {
      int cnt = 0;
      Integer value = null;
      Integer key = null;
      
      for(Iterator<Integer> itr = pool.keySet().iterator(); itr.hasNext();)
      {
         key = itr.next();
         if(cnt == selection)
         {
            value = pool.get(key);            
            break;
         }         
         cnt++;
      }
      
      int newValue = value.intValue();
      newValue--;
      if(newValue == 0)
      {
         pool.remove(key);
      }
      else
      {
         pool.put(key, new Integer(newValue));
      }
      return key;
   }
      
   public static Map<Integer, Integer> createPool(PasswordRules rules)
   {
      if(rules == null)
      {
         return null;
      }
      Map<Integer, Integer> pool = new TreeMap<Integer, Integer>();
      
      int letterLower = 0; // 1
      int letterUpper = 0; // 2
      int letter = 0; // 3
      int digits = 0; // 4
      int punctuation = 0; // 5
      
      int mixedCase = rules.getMixedCase();
      letterLower = mixedCase;
      letterUpper = mixedCase;
      letter = rules.getLetters() - (mixedCase * 2);
      if(letter < 0)
      {
         letter = 0;
      }
      digits = rules.getDigits();
      punctuation = rules.getPunctuation();

      if(letterLower > 0)
      {
         pool.put(LOWER, new Integer(letterLower));
      }
      if(letterUpper > 0)
      {
         pool.put(UPPER, new Integer(letterUpper));         
      }
      if(letter > 0)
      {
         pool.put(LETTER, new Integer(letter));         
      }
      if(digits > 0)
      {
         pool.put(DIGIT, new Integer(digits));         
      }
      if(punctuation > 0)
      {
         pool.put(PUNCTUATION, new Integer(punctuation));         
      }
      return pool;
   }
}