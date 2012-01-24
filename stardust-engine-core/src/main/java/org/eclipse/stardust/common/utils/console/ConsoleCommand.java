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
package org.eclipse.stardust.common.utils.console;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ConsoleCommand
{
   private static final Logger trace = LogManager.getLogger(ConsoleCommand.class);

   public static final String GLOBAL_OPTION_FORCE = "force";

   public static final int OPERATION_CANCELLED = -100;

   protected Map globalOptions;

   public abstract Options getOptions();

   public abstract int run(Map options);

   public abstract String getSummary();
   
   public void preprocessOptions(Map options)
   {
   }

   public boolean delayExit()
   {
      return false;
   }

   public void bootstrapGlobalOptions(Map globalOptions)
   {
      this.globalOptions = globalOptions;
   }

   public  boolean isVerbose()
   {
      return globalOptions.containsKey("verbose");
   }

   public boolean force()
   {
      return globalOptions.containsKey("force");
   }

   public boolean confirm(String message)
   {
      try
      {
         System.out.print(message);

         // enough space for 'y' or 'n' and new line characters.
         byte in[] = new byte[3];
         int count = System.in.read(in);

         System.out.println("");

         if (count > 0 && (in[0] == 'y' || in[0] == 'Y'))
         {
            return true;
         }
      }
      catch (IOException e)
      {
         //ignore
      }
      return false;
   }

   public void print(String message)
   {
      System.out.println(message);
      trace.info(message);
   }

   public static int getIntegerOption(Map options, String name)
   {
      String result = (String) options.get(name);
      if (result == null)
      {
         return 0;
      }
      try
      {
         return Integer.parseInt(result);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalUsageException("The argument provided for the '"
               + name + "' option is not a valid number.");
      }
   }

   public static Date getDateOption(Map options, String name)
   {
      if (options.containsKey(name))
      {
         try
         {
            return DateUtils.getNoninteractiveDateFormat().parse((String) options.get(name));
         }
         catch (ParseException e)
         {
            throw new PublicException(
                  "Date value '" + (String) options.get(name)
                  + "' for option '" + name + "' is not in correct format. Format has to be "
                  + DateUtils.getNoninteractiveDateFormat().toPattern());
         }
      }
      return null;
   }
}
