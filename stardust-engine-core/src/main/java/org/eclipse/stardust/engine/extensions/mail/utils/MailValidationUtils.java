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
package org.eclipse.stardust.engine.extensions.mail.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fuhrmann
 * @version $Revision$
 */
public class MailValidationUtils
{
   public static boolean isValidEMail(String mailAddress)
   {
      Pattern p = Pattern.compile("^\\w+[\\w-\\.]+@([\\w-]+\\.)+[a-zA-Z]{2,4}$");
      Matcher matcher = p.matcher(mailAddress);
      return matcher.find();
   }
}
