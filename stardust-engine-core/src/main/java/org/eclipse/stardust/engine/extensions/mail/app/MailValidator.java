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
package org.eclipse.stardust.engine.extensions.mail.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx;
import org.eclipse.stardust.engine.extensions.mail.utils.MailValidationUtils;


/**
 * 
 */
public class MailValidator implements ApplicationValidator, ApplicationValidatorEx
{
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   private boolean isValidEmailAddress(String mailList)
   {
      Iterator<String> i = StringUtils.split(mailList, ';');
      while (i.hasNext())
      {
         String mail = i.next();
         if (!StringUtils.isEmpty(mail) && !MailValidationUtils.isValidEMail(mail))
         {
            return false;
         }
      }
      return true;
   }

   /**
    * @see org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidatorEx#validate(org.eclipse.stardust.engine.api.model.IApplication)
    */
   public List validate(IApplication app)
   {
      List result = new ArrayList();
      
      if (!isValidEmailAddress((String) app.getAttribute(MailConstants.DEFAULT_MAIL_FROM))
            || !isValidEmailAddress((String) app.getAttribute(MailConstants.DEFAULT_MAIL_TO))
            || !isValidEmailAddress((String) app.getAttribute(MailConstants.DEFAULT_MAIL_BCC))
            || !isValidEmailAddress((String) app.getAttribute(MailConstants.DEFAULT_MAIL_CC)))
      {
         result.add(new Inconsistency("Invalid mail address.", app, Inconsistency.WARNING));
      }
      
      return result;
   }
}