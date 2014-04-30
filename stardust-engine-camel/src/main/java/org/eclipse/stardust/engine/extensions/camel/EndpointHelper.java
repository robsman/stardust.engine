package org.eclipse.stardust.engine.extensions.camel;

public class EndpointHelper
{
   private EndpointHelper()
   {

   }

   /**
    * @param uri
    * @return sanitize a uri in case it includes special character (i.e. &-->&amp;)
    */
   public static String sanitizeUri(final String uri)
   {

      if (uri != null && uri.contains("&"))
      {
         int indexOf = uri.indexOf('&');
         String firstPart = uri.substring(0, indexOf);
         String lastPart = uri.substring(indexOf + 1, uri.length());
         return firstPart + "&amp;" + sanitizeUri(lastPart);

      }
      return uri;

   }

   public static String replaceHtmlCodeByCharacter(final String input)
   {
      String answer = input;

      if (answer.contains("&amp;"))
         answer = answer.replaceAll("&amp;", "\\&");
      if (answer.contains("&lt;"))
         answer = answer.replaceAll("&lt;", "<");
      if (answer.contains("&gt;"))
         answer = answer.replaceAll("&gt;", ">");
      if (answer.contains("&quot;"))
         answer = answer.replaceAll("&quot;", "\"");

      return answer;
   }

   public static String replaceCharacterByHtmlCode(final String input)
   {
      String answer = input;

      if (answer.contains("&"))
         answer = answer.replaceAll("&", "&amp;");
      if (answer.contains("<"))
         answer = answer.replaceAll("<", "&lt;");
      if (answer.contains(">"))
         answer = answer.replaceAll(">", "&gt;");
      if (answer.contains("\""))
         answer = answer.replaceAll("\"", "&quot;");

      return answer;
   }
}
