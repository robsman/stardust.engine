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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;



/**
 * @author mgille
 */
public class ApplicationException extends RuntimeException
{
   private static final Logger trace = LogManager.getLogger(ApplicationException.class);

   private static final long serialVersionUID = 2L;
   
   private final ErrorCase error;

   private List inner = Collections.EMPTY_LIST;

   private boolean isLogged;
   
   private Map<String, String> messageLocalizationMap 
      = new HashMap<String, String>();
   
   public ApplicationException(String message)
   {
      this(null, message);
   }

   public ApplicationException(ErrorCase error, String message)
   {
      super(message);
      
      this.error = error;
   }

   /**
    * This constructor is used for exception conversion.
    */
   public ApplicationException(Throwable e)
   {
      this("", e);
   }

   /**
    * This constructor is used for exception conversion.
    */
   public ApplicationException(String message, Throwable e)
   {
      this(null, message, e);
   }

   /**
    * This constructor is used for exception conversion.
    */
   public ApplicationException(ErrorCase error, String message, Throwable e)
   {
      super(message + ": " + describeInner(e), e);
      
      this.error = error;
      
      if (null != e)
      {
         Throwable throwable = e;

         // Unpack nested exceptions
         while (throwable instanceof java.rmi.RemoteException)
         {
            Throwable t = ((java.rmi.RemoteException) throwable).detail;
            if (t == null || throwable == t)
            {
               break;
            }
            throwable = t;
         }
         inner = LogUtils.getStackTrace(throwable);
         
         boolean initialLogging = true;
         if (this instanceof ExceptionLogHint)
         {
            ExceptionLogHint logHint = (ExceptionLogHint) this;
            initialLogging = logHint.getInitialLogging();
         }

         if (initialLogging)
         {
            trace.warn(message, throwable);
            isLogged = true;
         }
      }
   }

   public ErrorCase getError()
   {
      return error;
   }

   public List getInner()
   {
      return Collections.unmodifiableList(inner);
   }

   private static String describeInner(Throwable inner)
   {
      String description = (null != inner) ? inner.getMessage() : null;
      if ((null == description) || (0 == description.length()))
      {
         description = String.valueOf(inner);
      }
      return description;
   }

   public boolean isLogged()
   {
      return isLogged;
   }

   public void setLogged(boolean logged)
   {
      isLogged = logged;
   }
   
   /**
    * Sets the Resource Bundle for localizing the error message.
    * Localization will be performed base on the id of the {@link ErrorCase} set for this exception
    * @param resourceBundle
    */
   public void setResourceBundle(ResourceBundle resourceBundle)
   {
      //ResourceBundle is not serializable, convert it to map
      messageLocalizationMap.clear();
      if(resourceBundle != null && error != null)
      {
         String msgKey = error.getId();
         String msgValue = null;
         try 
         {
            msgValue = resourceBundle.getString(msgKey);
            messageLocalizationMap.put(msgKey, msgValue);
         }
         catch(MissingResourceException e) 
         {}
      }
   }

   @Override
   public String getLocalizedMessage()
   {
      String localizedMessage = null;
      ErrorCase ec = getError();
      //cant do anything meaningful - sticking to default behaviour
      if(ec == null || messageLocalizationMap.isEmpty())
      {
         localizedMessage = super.getLocalizedMessage();
      }
      else
      {
         String msgKey = ec.getId();
         Object[] msgArgs = new Object[0];
         if(ec instanceof BaseErrorCase )
         {
            msgArgs = ((BaseErrorCase) ec).getMessageArgs();
         }
         
         String msgTemplate = messageLocalizationMap.get(msgKey);
         localizedMessage = MessageFormat.format(msgTemplate, msgArgs);
      }
      
      return localizedMessage;
   }
   
}
