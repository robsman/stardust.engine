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
package org.eclipse.stardust.common.config;

import java.text.MessageFormat;

import org.eclipse.stardust.common.error.ErrorCase;

/**
 * All configuration related errors.
 * 
 * Please take care if you refactor the class name or <code>ConfigurationError</code> fields.
 * It can be that they're loaded per reflection.
 * @author rottstock
 * @version $Revision: $
 */
public class ConfigurationError extends ErrorCase
{
   private static final long serialVersionUID = -727047172288805951L;
   
   // fields are used by DumpReader and ExtensionService
   public final static ConfigurationError LIC_WRONG_RELEASE = new ConfigurationError("LIC00001",
         // License is not valid for this release.
         "\\{FH-1\\}qq(mw{((i{qv(|w~(tpl{nzzt|iqm(Tmkmv{m6");
   public final static ConfigurationError LIC_EXPIRED = new ConfigurationError("LIC00002",
         //Your license for running the component of the Infinity (TM) Process Workbench has expired.\n\nPlease contact your Infinity sales representative to renew it.
         "\\{FH-1\\}mw{z(twk(v}mvnvz(zpv(qwox|vmvk(unw|mm|Qwn(vp|((v\\q1qX\"w0mU{(_zzkj{v(pwps{mmkx(zil(\n!Xqmm{6(\nwt|ikm(kwvziQ|n\"v}|((viqmq(\"m{zt{{vzixqmmm||(|m~m((w|zav} (qq6");
   public final static Args1 LIC_MAX_USER_EXCEEDED = new Args1("LIC00003",
         //The maximum number of active users supported with your license ({0}) is exceeded.\n\nPlease contact your Infinity sales representative.
         "\\{FH-1\\}upu(viuqm}((n}ij|z~w(({kzq(m}}xmz{m{(xqwp|\"l} (|q(mw{z(t$k&v(m{0m8k1mqm(6!\nmtlilm\nkXvmi{|(\"w}|(kv(qwqz\"Q{ntv{|z(ximmm(|m|z~{6v\\imqum!");
   
   private final static String ENC_ID = "\\{FH-1\\}";
   
   private final String defaultMessage;
   private final Object[] args;
   private static final Object[] NONE = {};
   
   protected ConfigurationError(String id, String defaultMessage)
   {
      this(id, defaultMessage, NONE);
   }
   
   protected ConfigurationError(String id, String defaultMessage, Object[] args)
   {
      super(id);
      this.defaultMessage = ENC_ID.equals(defaultMessage.substring(0, ENC_ID.length())) ? 
            msg(defaultMessage.substring(ENC_ID.length())) : defaultMessage;
      this.args = args;
   }
      
   public Object[] getMessageArgs()
   {
      return args;
   }
   
   public String toString()
   {
      return getId() + " - " + MessageFormat.format(getDefaultMessage(), args); //$NON-NLS-1$
   }
   
   // method is used by ExtensionService via reflection
   public static String msg(String encMessage)
   {
      char[] c = encMessage.toCharArray();
      int offset = -8; // (fh) for encoding, use the positive value
      int low = 32;
      int high = 127;
      int diff = high - low;
      boolean doIt = true;
      char[] u = new char[c.length];
      int dist = c.length;
      if ((dist & 1) == 1)
      {
         dist += 1;
      }
      for (int i = 0; i < c.length; i++)
      {
         if (doIt)
         {
            int j = i + offset;
            if (j < 0)
            {
               j += dist;
            }
            else if (j >= c.length)
            {
               j -= dist;
            }
            u[i] = c[j];
         }
         else
         {
            u[i] = c[i];
         }
         if (u[i] >= low && u[i] < high)
         {
            char r = (char) (u[i] + offset);
            if (r < low)
            {
               r += diff;
            }
            else if (r >= high)
            {
               r -= diff;
            }
            u[i] = r;
         }
         doIt = !doIt;
      }
      return new String(u);
   }

   public String getDefaultMessage()
   {
      return defaultMessage;
   }
   
   public static class Args1 extends AbstractErrorFactory
   {
      private Args1(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      public ConfigurationError raise(Object arg)
      {
         return buildError(new Object[] {arg});
      }

      public ConfigurationError raise(long arg)
      {
         return buildError(new Object[] {new Long(arg)});
      }
   }
   
   static abstract class AbstractErrorFactory extends ConfigurationError
   {
      protected AbstractErrorFactory(String errorCode, String defaultMessage)
      {
         super(errorCode, defaultMessage);
      }

      protected ConfigurationError buildError(Object[] args)
      {
         return new ConfigurationError(getId(), getDefaultMessage(), args);
      }
   }
}
