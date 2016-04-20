package org.eclipse.stardust.common.security;

import java.util.Arrays;

public class DefaultSecurityProviderConstants
{

   /**
    * 0-9
    */
   public final static char[] CHAR_DIGITS = {
         '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

   /**
    * A-Z
    */
   public final static char[] CHAR_UPPERS = {
         'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
         'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

   /**
    * a-z
    */
   public final static char[] CHAR_LOWERS = {
         'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
         'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

   public final static char[] CHAR_ALPHANUMERICS = combine(CHAR_DIGITS, CHAR_LOWERS,
         CHAR_UPPERS);

   private static char[] combine(char[] ... listOfArrays)
   {
      StringBuilder sb = new StringBuilder();

      for (char[] characters : listOfArrays)
      {

         for (int i = 0; i < characters.length; i++ )
         {
            if ( !contains(sb, characters[i]))
               sb.append(characters[i]);
         }
      }

      char[] combinedArray = new char[sb.length()];
      sb.getChars(0, sb.length(), combinedArray, 0);
      Arrays.sort(combinedArray);
      return combinedArray;
   }

   private static boolean contains(StringBuilder input, char c)
   {
      for (int i = 0; i < input.length(); i++ )
      {
         if (input.charAt(i) == c)
            return true;
      }
      return false;
   }
}
