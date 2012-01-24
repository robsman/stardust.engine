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
package org.eclipse.stardust.engine.cli.console;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class RealmCommand extends ConsoleCommand
{
   private static final Logger trace = LogManager.getLogger(RealmCommand.class);
   
   protected static final String CMD_CREATE = "create";
   protected static final String CMD_DROP = "drop";
   protected static final String CMD_LIST = "list";

   protected Options argTypes = new Options();

   public RealmCommand()
   {
      argTypes.register("-" + CMD_CREATE, Options.NO_SHORTNAME, CMD_CREATE,
            "Create a new realm to be identified by the given ID.", true);
      argTypes.register("-" + CMD_DROP, Options.NO_SHORTNAME, CMD_DROP,
            "Drops the realm being identified by the given ID.", true);
      argTypes.register("-" + CMD_LIST, Options.NO_SHORTNAME, CMD_LIST,
            "Lists all available realms.", false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public String getSummary()
   {
      return "Manages Infinity user realms.";
   }

   public int run(Map options)
   {
      if (options.containsKey(CMD_CREATE))
      {
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         String id = (String) options.get(CMD_CREATE);
         
         try
         {
            serviceFactory.getUserService().createUserRealm(id, id, "");
            print(MessageFormat.format("Created user realm ''{0}''.", new Object[] {id}));
         }
         catch (Exception e)
         {
            trace.warn("", e);
            print(MessageFormat.format("User realm ''{0}'' could not be created: {1}.",
                  new Object[] {id, e.getMessage()}));
            return -1;
         }
         finally
         {
            serviceFactory.close();
         }
      }
      else if (options.containsKey(CMD_DROP))
      {
         if (!force() && !confirm("You are going to drop a user realm. Continue?"))
         {
            return -1;
         }
         
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         String id = (String) options.get(CMD_DROP);
         
         try
         {
            serviceFactory.getUserService().dropUserRealm(id);
         }
         catch (Exception e)
         {
            trace.warn("", e);
            print(MessageFormat.format("User realm ''{0}'' could not be dropped: {1}.",
                  new Object[] {id, e.getMessage()}));
            return -1;
         }
         finally
         {
            serviceFactory.close();
         }
         
         print(MessageFormat.format("Dropped user realm ''{0}''.", new Object[] {id}));
      }
      else if (options.containsKey(CMD_LIST))
      {
         ServiceFactory serviceFactory = ServiceFactoryLocator.get(globalOptions);
         
         try
         {
            List userRealms = serviceFactory.getUserService().getUserRealms();
            if (userRealms.isEmpty())
            {
               print(MessageFormat.format("There exists no user realm.", new Object[] {}));
            }
            else
            {
               print(MessageFormat.format("List of existing user realms:", new Object[] {}));
               for (Iterator iter = userRealms.iterator(); iter.hasNext();)
               {
                  print(iter.next().toString());
               }
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
            print(MessageFormat.format("User realms could not be listed: {0}.",
                  new Object[] {e.getMessage()}));
            return -1;
         }
         finally
         {
            serviceFactory.close();
         }
      }
      
      return 0;
   }
}
