package org.eclipse.stardust.common.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class DefaultSecurityProviderUtilities
{
   private final static String ESCAPE_CHARS = "<>&\"\'";

   private final static List<String> ESCAPE_STRINGS = Collections.unmodifiableList(Arrays.asList(new String[] {
         "&lt;", "&gt;", "&amp;", "&quot;", "&apos;"}));

   private static String UNICODE_LOW = "" + ((char) 0x20); // space

   private static String UNICODE_HIGH = "" + ((char) 0x7f);

   /******************************************** XML Encoding ********************************************/

   /**
    * Method to escape special characters in XML elements or attributes
    * 
    * @param content
    * @return
    */
   public static String encodeXml(String content)
   {
      String result = content;

      if ((content != null) && (content.length() > 0))
      {
         boolean modified = false;
         StringBuilder stringBuilder = new StringBuilder(content.length());
         for (int i = 0, count = content.length(); i < count; ++i)
         {
            String character = content.substring(i, i + 1);
            int pos = ESCAPE_CHARS.indexOf(character);
            if (pos > -1)
            {
               stringBuilder.append(ESCAPE_STRINGS.get(pos));
               modified = true;
            }
            else
            {
               if ((character.compareTo(UNICODE_LOW) > -1)
                     && (character.compareTo(UNICODE_HIGH) < 1))
               {
                  stringBuilder.append(character);
               }
               else
               {
                  stringBuilder.append("&#" + ((int) character.charAt(0)) + ";");
                  modified = true;
               }
            }
         }
         if (modified)
         {
            result = stringBuilder.toString();
         }
      }

      return result;
   }

   /******************************************** JavaScript Encoding ********************************************/

   public static String encodeForJavaScript(String input)
   {
      String result = input;

      if (input != null && input.length() > 0)
      {
         boolean modified = false;
         StringBuilder stringBuilder = new StringBuilder(input.length());

         for (int i = 0; i < input.length(); i++ )
         {
            char character = input.charAt(i);

            // handle unicode
            if (character > 0xfff)
            {
               stringBuilder.append("\\u" + toHex(character));
               modified = true;
            }
            else if (character > 0xff)
            {
               stringBuilder.append("\\u0" + toHex(character));
               modified = true;
            }
            else if (character > 0x7f)
            {
               stringBuilder.append("\\u00" + toHex(character));
               modified = true;
            }
            else if (character < 32)
            {
               switch (character)
               {
               case '\b':
                  stringBuilder.append('\\').append('b');
                  modified = true;
                  break;
               case '\f':
                  stringBuilder.append('\\').append('f');
                  modified = true;
                  break;
               case '\n':
                  stringBuilder.append('\\').append('n');
                  modified = true;
                  break;
               case '\r':
                  stringBuilder.append('\\').append('r');
                  modified = true;
                  break;
               case '\t':
                  stringBuilder.append('\\').append('t');
                  modified = true;
                  break;
               default:
                  if (character > 0xf)
                  {
                     stringBuilder.append("\\u00" + toHex(character));
                     modified = true;
                  }
                  else
                  {
                     stringBuilder.append("\\u000" + toHex(character));
                     modified = true;
                  }
                  break;
               }
            }
            else
            {
               switch (character)
               {
               case '\'':
                  stringBuilder.append('\\').append('\'');
                  modified = true;
                  break;
               case '"':
                  stringBuilder.append('\\').append('"');
                  modified = true;
                  break;
               case '\\':
                  stringBuilder.append('\\').append('\\');
                  modified = true;
                  break;
               case '/':
                  stringBuilder.append('\\').append('/');
                  modified = true;
                  break;
               default:
                  stringBuilder.append(character);
                  break;
               }
            }
         }
         if (modified)
         {
            result = stringBuilder.toString();
         }
      }
      return result;
   }

   /******************************************** HTML Encoding ********************************************/

   private static char[] hex = {
         '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

   /**
    * Method for html escaping a String, for use in a textarea
    * 
    * @param original
    *           The String to escape
    * @return The escaped String
    */
   public static String encodeForHtmlTextArea(String input)
   {
      if (input != null && input.length() > 0)
      {
         return escapeTags(escapeSpecial(input));
      }
      return input;
   }

   /**
    * Normal escape function, for Html escaping Strings
    * 
    * @param original
    *           The original String
    * @return The escape String
    */
   public static String encodeForHtml(String input)
   {
      if (input != null && input.length() > 0)
      {
         return escapeBr(escapeTags(escapeSpecial(input)));
      }
      return input;

   }

   private static String escapeTags(String input)
   {
      StringBuffer result = new StringBuffer();
      char[] chars = input.toCharArray();
      for (int i = 0; i < chars.length; i++ )
      {
         boolean found = true;
         switch (chars[i])
         {
         case 60:
            result.append("&lt;");
            break; // <
         case 62:
            result.append("&gt;");
            break; // >
         case 34:
            result.append("&quot;");
            break; // "
         default:
            found = false;
            break;
         }
         if ( !found)
            result.append(chars[i]);

      }
      return result.toString();

   }

   private static String escapeBr(String input)
   {
      StringBuffer result = new StringBuffer();
      char[] chars = input.toCharArray();
      for (int i = 0; i < chars.length; i++ )
      {
         boolean found = true;
         switch (chars[i])
         {
         case '\n':
            result.append("<br/>");
            break; // newline
         case '\r':
            break;
         default:
            found = false;
            break;
         }
         if ( !found)
            result.append(chars[i]);

      }
      return result.toString();
   }

   public static String escapeSpecial(String original)
   {
      StringBuffer result = new StringBuffer("");
      char[] chars = original.toCharArray();
      for (int i = 0; i < chars.length; i++ )
      {
         boolean found = true;
         switch (chars[i])
         {
         case 38:
            result.append("&amp;");
            break; // &
         case 198:
            result.append("&AElig;");
            break; // Æ
         case 193:
            result.append("&Aacute;");
            break; // Á
         case 194:
            result.append("&Acirc;");
            break; // Â
         case 192:
            result.append("&Agrave;");
            break; // À
         case 197:
            result.append("&Aring;");
            break; // Å
         case 195:
            result.append("&Atilde;");
            break; // Ã
         case 196:
            result.append("&Auml;");
            break; // Ä
         case 199:
            result.append("&Ccedil;");
            break; // Ç
         case 208:
            result.append("&ETH;");
            break; // Ð
         case 201:
            result.append("&Eacute;");
            break; // É
         case 202:
            result.append("&Ecirc;");
            break; // Ê
         case 200:
            result.append("&Egrave;");
            break; // È
         case 203:
            result.append("&Euml;");
            break; // Ë
         case 205:
            result.append("&Iacute;");
            break; // Í
         case 206:
            result.append("&Icirc;");
            break; // Î
         case 204:
            result.append("&Igrave;");
            break; // Ì
         case 207:
            result.append("&Iuml;");
            break; // Ï
         case 209:
            result.append("&Ntilde;");
            break; // Ñ
         case 211:
            result.append("&Oacute;");
            break; // Ó
         case 212:
            result.append("&Ocirc;");
            break; // Ô
         case 210:
            result.append("&Ograve;");
            break; // Ò
         case 216:
            result.append("&Oslash;");
            break; // Ø
         case 213:
            result.append("&Otilde;");
            break; // Õ
         case 214:
            result.append("&Ouml;");
            break; // Ö
         case 222:
            result.append("&THORN;");
            break; // Þ
         case 218:
            result.append("&Uacute;");
            break; // Ú
         case 219:
            result.append("&Ucirc;");
            break; // Û
         case 217:
            result.append("&Ugrave;");
            break; // Ù
         case 220:
            result.append("&Uuml;");
            break; // Ü
         case 221:
            result.append("&Yacute;");
            break; // Ý
         case 225:
            result.append("&aacute;");
            break; // á
         case 226:
            result.append("&acirc;");
            break; // â
         case 230:
            result.append("&aelig;");
            break; // æ
         case 224:
            result.append("&agrave;");
            break; // à
         case 229:
            result.append("&aring;");
            break; // å
         case 227:
            result.append("&atilde;");
            break; // ã
         case 228:
            result.append("&auml;");
            break; // ä
         case 231:
            result.append("&ccedil;");
            break; // ç
         case 233:
            result.append("&eacute;");
            break; // é
         case 234:
            result.append("&ecirc;");
            break; // ê
         case 232:
            result.append("&egrave;");
            break; // è
         case 240:
            result.append("&eth;");
            break; // ð
         case 235:
            result.append("&euml;");
            break; // ë
         case 237:
            result.append("&iacute;");
            break; // í
         case 238:
            result.append("&icirc;");
            break; // î
         case 236:
            result.append("&igrave;");
            break; // ì
         case 239:
            result.append("&iuml;");
            break; // ï
         case 241:
            result.append("&ntilde;");
            break; // ñ
         case 243:
            result.append("&oacute;");
            break; // ó
         case 244:
            result.append("&ocirc;");
            break; // ô
         case 242:
            result.append("&ograve;");
            break; // ò
         case 248:
            result.append("&oslash;");
            break; // ø
         case 245:
            result.append("&otilde;");
            break; // õ
         case 246:
            result.append("&ouml;");
            break; // ö
         case 223:
            result.append("&szlig;");
            break; // ß
         case 254:
            result.append("&thorn;");
            break; // þ
         case 250:
            result.append("&uacute;");
            break; // ú
         case 251:
            result.append("&ucirc;");
            break; // û
         case 249:
            result.append("&ugrave;");
            break; // ù
         case 252:
            result.append("&uuml;");
            break; // ü
         case 253:
            result.append("&yacute;");
            break; // ý
         case 255:
            result.append("&yuml;");
            break; // ÿ
         case 162:
            result.append("&cent;");
            break; // ¢
         default:
            found = false;
            break;
         }
         if ( !found)
         {
            if (chars[i] > 127)
            {
               char c = chars[i];
               int a4 = c % 16;
               c = (char) (c / 16);
               int a3 = c % 16;
               c = (char) (c / 16);
               int a2 = c % 16;
               c = (char) (c / 16);
               int a1 = c % 16;
               result.append("&#x" + hex[a1] + hex[a2] + hex[a3] + hex[a4] + ";");
            }
            else
            {
               result.append(chars[i]);
            }
         }
      }
      return result.toString();
   }

   /******************************************** Utility Methods ********************************************/

   private static String toHex(char character)
   {
      return Integer.toHexString(character).toUpperCase(Locale.ENGLISH);
   }

   public static String replaceLinearWhiteSpace(String input)
   {
      Pattern pattern = Pattern.compile("\\s");
      return pattern.matcher(input).replaceAll(" ");
   }
}