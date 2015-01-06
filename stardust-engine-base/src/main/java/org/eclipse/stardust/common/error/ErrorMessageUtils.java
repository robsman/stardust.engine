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
package org.eclipse.stardust.common.error;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.IErrorMessageProvider.Factory;


/**
 * @author sauer
 * @version $Revision: $
 */
public class ErrorMessageUtils
{
   private static  final Object  NO_BUNDLE = new Object();
   
   public static String getErrorMessage(ErrorCase error)
   {
      return getErrorMessage(error, null);
   }
   
   public static String getErrorMessage(ErrorCase error, Locale locale)
   {
      String message = null;
      
      List providerFactories = ExtensionProviderUtils.getExtensionProviders(IErrorMessageProvider.Factory.class);
      for (int i = 0; i < providerFactories.size(); ++i)
      {
         Factory factory = (Factory) providerFactories.get(i);
         IErrorMessageProvider provider = factory.getProvider(error);
         if (null != provider)
         {
            message = provider.getErrorMessage(error, null, locale);
            if ( !StringUtils.isEmpty(message))
            {
               break;
            }
         }
      }
      
      if (StringUtils.isEmpty(message))
      {
         message = error.toString();
      }
      
      return message;
   }
   
   public static ResourceBundle getErrorBundle(Map bundles, String bundleName, Locale locale)
   {
      if (null == locale)
      {
         locale = Locale.getDefault();
      }
      if(null == bundles)
      { 
         bundles = new ConcurrentHashMap();
      }
      
      Object messages = bundles.get(locale.toString());
      if (null == messages)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (null == cl)
         {
            cl = ErrorMessageUtils.class.getClass().getClassLoader();
         }
         
         try
         {
            ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale, cl);
            // We have to check the locale of the resource bundle to ensure
            // that the bundle of the given locale was loaded and not the
            // bundle for the system default locale.
            if(locale.getLanguage().equals(rb.getLocale().getLanguage()))
            {
               messages = rb; 
            }
         }
         catch (MissingResourceException mre)
         {
            messages = NO_BUNDLE;
         }
         bundles.put(locale.toString(), (null != messages) ? messages : NO_BUNDLE);
      }
      return messages instanceof ResourceBundle ? (ResourceBundle)messages : null;
   }
   
   public static String getErrorMessage(ResourceBundle bundle, ErrorCase errorCase)
   {
      String msg = null;
      if(bundle != null && errorCase != null)
      {
         try
         {
            msg = bundle.getString(errorCase.getId());
         }
         catch (MissingResourceException mre) 
         {
            return null;
         }
      }
      return msg;
   }
}
