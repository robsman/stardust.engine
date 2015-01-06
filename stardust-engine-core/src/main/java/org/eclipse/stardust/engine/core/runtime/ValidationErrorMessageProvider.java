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
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.ErrorMessageUtils;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;



/**
 * @author pielmann
 * @version $Revision: $
 */
public class ValidationErrorMessageProvider implements IErrorMessageProvider
{

   public static final String MSG_BUNDLE_NAME = "ipp-bpm-validation-errors";

   private Map bundles = new ConcurrentHashMap();

   public String getErrorMessage(ErrorCase error, Object[] context, Locale locale)
   {
      // implement locale support
      ResourceBundle messages = ErrorMessageUtils.getErrorBundle(bundles, MSG_BUNDLE_NAME, locale);

      String msg = null;
      if (error instanceof BpmValidationError)
      {
         BpmValidationError vdErrorCode = (BpmValidationError) error;
         msg = ErrorMessageUtils.getErrorMessage(messages, vdErrorCode);

         if (StringUtils.isEmpty(msg))
         {
            // fall back to default message
            msg = vdErrorCode.getDefaultMessage();
         }

         if ( !StringUtils.isEmpty(msg) && (null != vdErrorCode.getMessageArgs())
               && (0 < vdErrorCode.getMessageArgs().length))
         {
            msg = MessageFormat.format(msg, vdErrorCode.getMessageArgs());
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
      private final ValidationErrorMessageProvider INSTANCE = new ValidationErrorMessageProvider();

      public IErrorMessageProvider getProvider(ErrorCase errorCase)
      {
         return (errorCase instanceof BpmValidationError) ? INSTANCE : null;
      }
   }

}
