package org.eclipse.stardust.common.error;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.StringUtils;

public class BaseErrorMessageProvider implements IErrorMessageProvider
{
public static final String MSG_BUNDLE_NAME = "base-errors";
   
   private Map bundles = new ConcurrentHashMap();

   public String getErrorMessage(ErrorCase error, Object[] context, Locale locale)
   {
      // implement locale support
      ResourceBundle messages = ErrorMessageUtils.getErrorBundle(bundles, MSG_BUNDLE_NAME, locale);
      
      String msg = null;
      if (error instanceof BaseErrorCase )
      {
         BaseErrorCase rtErrorCode = (BaseErrorCase) error;
         msg = ErrorMessageUtils.getErrorMessage(messages, rtErrorCode);
         
         if (StringUtils.isEmpty(msg))
         {
            // fall back to default message
            msg = rtErrorCode.getDefaultMessage();
         }
         
         if ( !StringUtils.isEmpty(msg) && (null != rtErrorCode.getMessageArgs())
               && (0 < rtErrorCode.getMessageArgs().length))
         {
            msg = MessageFormat.format(msg, rtErrorCode.getMessageArgs());
         }
      }
      
      return msg;
   }

   public String getErrorMessage(ApplicationException exception, Locale locale)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public static class Factory implements IErrorMessageProvider.Factory
   {
      private final BaseErrorMessageProvider INSTANCE = new BaseErrorMessageProvider();

      public IErrorMessageProvider getProvider(ErrorCase errorCase)
      {
         return (errorCase instanceof BaseErrorCase) ? INSTANCE : null;
      }
   }
}
