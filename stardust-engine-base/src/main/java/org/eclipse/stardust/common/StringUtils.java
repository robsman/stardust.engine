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
package org.eclipse.stardust.common;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.*;

import org.eclipse.stardust.common.error.PublicException;


public class StringUtils
{
   public static final String[] EMPTY_STRING_ARRAY = new String[0];

   /**
    * Test the given {@link String} for emptiness. Empty either means being
    * <code>null</code> or having a zero length.
    *
    * @param string The string to be tested.
    * @return <code>true</code> if the string is empty, else <code>false</code>.
    */
   public static boolean isEmpty(String string)
   {
      return (null == string) || (0 == string.length());
   }
   
   /**
    * Test the given {@link String} for not being empty. Not empty means not being
    * <code>null</code> and having a length greater than zero.
    *
    * @param string The string to be tested.
    * @return <code>true</code> if the string is not empty, else <code>false</code>.
    */
   public static boolean isNotEmpty(String string)
   {
      return !isEmpty(string);
   }

   public static String printf(String string, Object[] args)
   {
      StringBuilder buf = new StringBuilder();
      StringTokenizer st = new StringTokenizer(string, "$$");
      Object value;

      int length = args.length;
      buf.append(st.nextToken());

      for (int i = 0; i < length; i++)
      {
         value = args[i];
         buf.append(value);
         if (st.hasMoreTokens())
            buf.append(st.nextToken());
      }
      return buf.toString();
   }

   public static final String getNormalized(String s)
   {
      final String trimmedS = s.trim();
      return StringUtils.isEmpty(trimmedS) ? null : trimmedS;
   }

   public static final String cutString(String src, int maxLength)
   {
      return (src == null) ? null : src.substring(0, Math.min(src.length(), maxLength));
   }

   public static String replace(String source, String repl, String with)
   {
      if (source == null)
      {
         return null;
      }

      StringBuilder buf = new StringBuilder(source.length());
      int start = 0, end;
      while ((end = source.indexOf(repl, start)) != -1)
      {
         buf.append(source.substring(start, end)).append(with);
         start = end + repl.length();
      }
      buf.append(source.substring(start));
      return buf.toString();
   }

   public static String join(String lhs, String rhs, String joinToken)
   {
      StringBuilder buffer = new StringBuilder((lhs == null ? 0 : lhs.length()) +
            (rhs == null ? 0 : rhs.length()) +
            (joinToken == null ? 0 : joinToken.length()));

      if (!isEmpty(lhs))
      {
         buffer.append(lhs);
      }
      
      if (!isEmpty(rhs))
      {
         if (0 < buffer.length())
         {
            buffer.append(joinToken);
         }
         buffer.append(rhs);
      }
      
      return buffer.toString();
   }

   public static String join(Iterator<?> parts, String joinToken)
   {
      if (parts.hasNext())
      {
         StringBuilder buffer = new StringBuilder();

         String token = "";
         while (parts.hasNext())
         {
            buffer.append(token).append(parts.next());
            token = joinToken;
         }

         return buffer.toString();
      }
      else
      {
         return "";
      }
   }

   public static final Iterator<String> split(String source, char splitToken)
   {
      return split(source, splitToken, false);
   }

   public static final Iterator<String> split(String source, char splitToken,
         boolean includeSplitToken)
   {
      if ( !isEmpty(source))
      {
         StringTokenizer token = new StringTokenizer(source,
               Character.valueOf(splitToken).toString(), includeSplitToken); 
         return new EnumerationIteratorWrapper<String>(new StringEnumeration(token));
      }
      else
      {
         return Collections.<String>emptyList().iterator();
      }
   }

   public static final Iterator<String> split(String source, String splitToken)
   {
      return split(source, splitToken, false);
   }

   public static final Iterator<String> split(String source, String splitToken,
         boolean includeSplitToken)
   {
      List<String> tokens;

      if ( !isEmpty(source))
      {
         tokens = newArrayList();

         int tokenStart = 0;
         int tokenEnd = source.indexOf(splitToken, tokenStart);
         while (-1 != tokenEnd)
         {
            tokens.add(source.substring(tokenStart, tokenEnd));

            if (includeSplitToken)
            {
               tokens.add(splitToken);
            }

            tokenStart = tokenEnd + splitToken.length();
            tokenEnd = source.indexOf(splitToken, tokenStart);
         }

         if (tokenStart < source.length())
         {
            tokens.add(source.substring(tokenStart));
         }

         return tokens.iterator();
      }
      else
      {
         tokens = emptyList();
      }
      
      return tokens.iterator();
   }

   public static final int extractAutoNumber(String id, String prefix)
   {
      try
      {
         return id != null && id.startsWith(prefix) ?
            Integer.parseInt(id.substring(prefix.length())) : 0;
      }
      catch (NumberFormatException nfe)
      {
         return 0;
      }
   }

   public static boolean isValidIdentifier(String id)
   {
      if (isEmpty(id))
      {
         return false;
      }
      if (!Character.isJavaIdentifierStart(id.charAt(0)))
      {
         return false;
      }
      for (int i = 1; i < id.length(); i++)
      {
         if (!Character.isJavaIdentifierPart(id.charAt(i)))
         {
            return false;
         }
      }
      return true;
   }
   
   public static boolean getBoolean(String name, boolean defaultValue)
   {
      final String[] trueWords = {"true", "enabled", "on"};
      final String[] falseWords = {"false", "disabled", "off"};

      if (isEmpty(name))
      {
         return defaultValue;
      }

      String value = name.toLowerCase().trim();

      for (int i = 0; i < trueWords.length; ++i)
      {
         if (trueWords[i].equals(value))
         {
            return true;
         }
      }

      for (int i = 0; i < falseWords.length; ++i)
      {
         if (falseWords[i].equals(value))
         {
            return false;
         }
      }

      throw new PublicException("The string '" + name
            + "' can neither be mapped to 'true' nor to 'false'.");
   }
   
   private static class StringEnumeration implements Enumeration<String>
   {
      private StringTokenizer tokenizer;
      
      public StringEnumeration(StringTokenizer tokenizer)
      {
         this.tokenizer = tokenizer;
      }
      
      public boolean hasMoreElements()
      {
         return tokenizer.hasMoreElements();
      }

      public String nextElement()
      {
         return tokenizer.nextToken();
      }
      
   }
}
