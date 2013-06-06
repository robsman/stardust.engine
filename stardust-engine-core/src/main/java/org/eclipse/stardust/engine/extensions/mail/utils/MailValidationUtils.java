/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
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

import org.eclipse.stardust.common.StringUtils;

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
   
   /**
    * @param piOid
    *           process instance OID
    * @param aiOid
    *           activity instance OID
    * @param investigate
    *           flag
    * @param outputValue
    *           output value string, <code>null</code> will be handled as empty string
    * @return Arguments will be concatenated with delimiter '|'. Result is value of
    *         <code>String.hashCode()</code> on resulting string.
    */
   public static int getQueryParametersHashCode(long piOid, long aiOid, String partition,
         boolean investigate, String outputValue)
   {
      StringBuffer buffer = new StringBuffer(200);
      
      buffer.append(piOid).append("|")
            .append(aiOid).append("|");
       
            //ensure backwards compatibility, if partition is not present existing hash codes must still be valid.
            if (StringUtils.isEmpty(partition))
                  {
               buffer.append(partition).append("|");
                  }
            
            buffer
            .append(Boolean.toString(investigate)).append("|")
            .append(outputValue == null ? "" : outputValue);
      
      return buffer.toString().hashCode();
   }
}
