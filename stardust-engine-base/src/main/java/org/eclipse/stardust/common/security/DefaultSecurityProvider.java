package org.eclipse.stardust.common.security;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * 
 * @author thomas.wolfram
 *
 */
public class DefaultSecurityProvider
      implements SecurityProvider, SecurityProvider.Factory
{

   Logger trace = LogManager.getLogger(DefaultSecurityProvider.class);

   public static final String DEFAULT_CHARSET = "UTF-8";

   private SecureRandom secureRandom = null;

   private Map<String, String> validatorMap;

   // Validatior Keys
   private static final String VALIDATOR_KEY_HTTP_HEADER_NAME = "HTTPHeaderName";

   private static final String VALIDATOR_KEY_HTTP_HEADER_VALUE = "HTTPHeaderValue";

   private static final String VALIDATOR_KEY_FILENAME = "FileName";

   private static final String VALIDATOR_KEY_SAFE_STRING = "SafeString";

   public DefaultSecurityProvider()
   {
      secureRandom = new SecureRandom();
      initValidatorMap();
   }

   private void initValidatorMap()
   {
      this.validatorMap = CollectionUtils.newMap();

      this.validatorMap.put(VALIDATOR_KEY_HTTP_HEADER_NAME, "^[a-zA-Z0-9\\-_]{1,32}$");
      this.validatorMap.put(VALIDATOR_KEY_HTTP_HEADER_VALUE,
            "^[a-zA-Z0-9()\\-=\\*\\.\\?;,+\\/:&_ ]*$");
      this.validatorMap.put(VALIDATOR_KEY_FILENAME,
            "^[a-zA-Z0-9!@#$%^&{}\\[\\]()_+\\-=,.~'` ]{1,255}$");
      this.validatorMap.put(VALIDATOR_KEY_SAFE_STRING, "^[.\\p{Alnum}\\p{Space}*,()&+-]{0,1024}$");
      
   }

   @Override
   public boolean getRandomBoolean()
   {
      return secureRandom.nextBoolean();
   }

   @Override
   public byte[] getRandomBytes(int n)
   {
      byte[] byteArray = new byte[n];
      secureRandom.nextBytes(byteArray);
      return byteArray;
   }

   @Override
   public String getRandomFilename(String extension)
   {
      String filename = getRandomString(12,
            DefaultSecurityProviderConstants.CHAR_ALPHANUMERICS) + "." + extension;
      return filename;
   }

   @Override
   public String getRandomGUID()
   {
      return UUID.randomUUID().toString();
   }

   @Override
   public int getRandomInteger(int min, int max)
   {
      return secureRandom.nextInt(max - min) + min;
   }

   @Override
   public long getRandomLong()
   {
      return secureRandom.nextLong();
   }

   @Override
   public float getRandomReal(float min, float max)
   {
      float factor = max - min;
      return secureRandom.nextFloat() * factor + min;
   }

   @Override
   public String getRandomString(int length, char[] characterSet)
   {
      StringBuffer randomString = new StringBuffer();
      for (int i = 0; i < length; i++ )
      {
         randomString.append(characterSet[secureRandom.nextInt(characterSet.length)]);
      }
      return randomString.toString();
   }

   @Override
   public SecurityProvider getInstance()
   {
      return new DefaultSecurityProvider();
   }

   @Override
   public String encodeForXPath(String input)
   {
      // Implement default
      return input;
   }

   @Override
   public String encodeForXMLAttribute(String input)
   {
      return DefaultSecurityProviderUtilities.encodeXml(input);
   }

   @Override
   public String encodeForXML(String input)
   {
      return DefaultSecurityProviderUtilities.encodeXml(input);
   }

   @Override
   public String encodeForURL(String input)
   {
      if (input == null)
      {
         return null;
      }
      else
      {
         try
         {
            return URLEncoder.encode(input, DEFAULT_CHARSET);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new InternalException(e);
         }
      }
   }

   @Override
   public String encodeForJavaScript(String input)
   {
      return DefaultSecurityProviderUtilities.encodeForJavaScript(input);
   }

   @Override
   public String encodeForHTMLAttribute(String input)
   {
      return DefaultSecurityProviderUtilities.encodeForHtml(input);
   }

   @Override
   public String encodeForHTML(String input)
   {
      return DefaultSecurityProviderUtilities.encodeForHtml(input);
   }

   @Override
   public Date getValidDate(String context, String input, DateFormat format,
         boolean allowNull)
   {
      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null vallue not allowed for context " + context);
      }
      try
      {
         return format.parse(input);
      }
      catch (Exception e)
      {
         throw new SecurityException("Date not valid. Expected format is "
               + format.getNumberFormat());
      }
   }

   @Override
   public String getValidSafeHTML(String context, String input, int maxLength,
         boolean allowNull)
   {
      // only minimal implementation 
      
      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null value not allowed for context " + context);
      }
      
      if (input.length() > maxLength)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Length of value in context " + context
                     + " exceeds maximum allowed length");
      }
      
      return input;
   }

   @Override
   public Double getValidNumber(String context, String input, long minValue,
         long maxValue, boolean allowNull)
   {

      Double maxDouble = new Double(maxValue);
      Double minDouble = new Double(minValue);

      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null value not allowed for context " + context);
      }

      Double number = null;
      try
      {
         number = Double.parseDouble(input);
      }
      catch (NumberFormatException e)
      {
         throw new org.eclipse.stardust.common.error.SecurityException("Value " + input
               + " in context " + context + " cannot be parsed to number");
      }

      if (number > maxDouble || number < minDouble)
      {
         throw new org.eclipse.stardust.common.error.SecurityException("Value " + input
               + " in context" + context + " is out of bounds");
      }

      return number;
   }

   @Override
   public String getValidFileName(String context, String input,
         List<String> allowedExtensions, boolean allowNull)
   {
      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null vallue not allowed for context " + context);
      }

      String validFileName = getValidInput(context, input, "FileName", 255, true);

      Iterator<String> extensionIterator = allowedExtensions.iterator();

      while (extensionIterator.hasNext())
      {
         String extension = extensionIterator.next();
         if (validFileName.toLowerCase().endsWith(extension.toLowerCase()))
         {
            return validFileName;
         }
      }
      throw new org.eclipse.stardust.common.error.SecurityException(
            "Extension for filename " + validFileName + "not valid");

   }

   @Override
   public String getValidRedirectionLocation(String context, String input,
         boolean allowNull)
   {
      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null vallue not allowed for context " + context);
      }
      return getValidInput(context, input, "Redirect", 512, allowNull);
   }

   @Override
   public String getValidInput(String context, String input, String type, int maxLength,
         boolean allowNull)
   {
      if (input == null && !allowNull)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Null vallue not allowed for context " + context);
      }

      if (input.length() > maxLength)
      {
         throw new org.eclipse.stardust.common.error.SecurityException(
               "Input value length for " + context + " exceeds limit");
      }

      if (this.validatorMap.containsKey(type))
      {
         String validatorRegEx = this.validatorMap.get(type);

         Pattern pattern = Pattern.compile(validatorRegEx);
         if ( !pattern.matcher(input).matches())
         {
            throw new org.eclipse.stardust.common.error.SecurityException(
                  "Input value does match valid type pattern for context " + context);
         }
      }

      return input;
   }

   @Override
   public void addHeader(HttpServletResponse response, String name, String value)
   {
      String noLNWSName = DefaultSecurityProviderUtilities.replaceLinearWhiteSpace(name);
      String noLNWSValue = DefaultSecurityProviderUtilities.replaceLinearWhiteSpace(value);
      String validName = getValidInput("addHeader", noLNWSName, "HTTPHeaderName", 50,
            false);
      String validValue = getValidInput("addHeader", noLNWSValue, "HTTPHeaderValue", 500,
            false);

      response.addHeader(validName, validValue);

   }

   @Override
   public void setHeader(HttpServletResponse response, String name, String value)
   {
      String noLNWSName = DefaultSecurityProviderUtilities.replaceLinearWhiteSpace(name);
      String noLNWSValue = DefaultSecurityProviderUtilities.replaceLinearWhiteSpace(value);
      String validName = getValidInput("addHeader", noLNWSName, "HTTPHeaderName", 50,
            false);
      String validValue = getValidInput("addHeader", noLNWSValue, "HTTPHeaderValue", 500,
            false);

      response.setHeader(validName, validValue);
   }

}
