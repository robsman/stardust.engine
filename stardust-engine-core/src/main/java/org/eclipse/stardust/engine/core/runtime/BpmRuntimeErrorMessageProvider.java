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
package org.eclipse.stardust.engine.core.runtime;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ConfigurationError;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.ErrorMessageUtils;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;



/**
 * @author sauer
 * @version $Revision: $
 */
public class BpmRuntimeErrorMessageProvider implements IErrorMessageProvider
{
   
   public static final String MSG_BUNDLE_NAME = "ipp-bpm-runtime-errors";
   
   private Map bundles = new ConcurrentHashMap();

   public String getErrorMessage(ErrorCase error, Object[] context, Locale locale)
   {
      // implement locale support
      ResourceBundle messages = ErrorMessageUtils.getErrorBundle(bundles, MSG_BUNDLE_NAME, locale);
      
      String msg = null;
      if (error instanceof BpmRuntimeError)
      {
         BpmRuntimeError rtErrorCode = (BpmRuntimeError) error;
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
      else if(error instanceof ConfigurationError)
      {
         ConfigurationError rtErrorCode = (ConfigurationError) error;
         msg = ErrorMessageUtils.getErrorMessage(messages, rtErrorCode);
         
         if (StringUtils.isEmpty(msg))
         {
            // fall back to default message
            msg = rtErrorCode.getDefaultMessage();
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
      private final BpmRuntimeErrorMessageProvider INSTANCE = new BpmRuntimeErrorMessageProvider();

      public IErrorMessageProvider getProvider(ErrorCase errorCase)
      {
         return (errorCase instanceof BpmRuntimeError) || (errorCase instanceof ConfigurationError) ? INSTANCE : null;
      }
   }

}
