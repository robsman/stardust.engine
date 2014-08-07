/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
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
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class CreateUserCommand extends UserCommand
{
   private static final Logger trace = LogManager.getLogger(CreateUserCommand.class);

   private static final String GRANTS = "grants";

   public CreateUserCommand()
   {
      argTypes.register("-grants", "-g", GRANTS,
            "Grants to be given to the user. Specify as comma separated list of participant IDs.\n"
            + "Department constraints can be given with the format 'id@departmentoid', e.g.\n"
            + "'engineer@33,clerk'.\n or as id@departmentpath e.g. engineer@root/child,clerk"
            + "Model version constraints can be given with the format 'id:modeloid', e.g.\n"
            + "'engineer:47,clerk'.\n"
            + "Model version and department constraints are mutually exclusive.", true);
      argTypes.addMandatoryRule(PASSWORD);
   }

   public int run(Map options)
   {
      String account = (String) options.get(ACCOUNT);
      String realm = (String) options.get(REALM);
      String confirmMessage;
      if (StringUtils.isEmpty(realm))
      {
         confirmMessage = "Create user with account ''{0}''?: ";
      }
      else
      {
         confirmMessage = "Create user with account ''{0}'' (realm {1})?: ";
      }

      if ( !force()
            && !confirm(MessageFormat.format(confirmMessage, new Object[] { account,
                  realm })))
      {
         return -1;
      }

      ServiceFactory sf = ServiceFactoryLocator.get(globalOptions);
      final User user;
      try
      {
         if (StringUtils.isEmpty(realm))
         {
            user = sf.getUserService().createUser(
                  account,
                  (String) options.get(FIRST_NAME), (String) options.get(LAST_NAME),
                  (String) options.get(DESCRIPTION), (String) options.get(PASSWORD),
                  (String) options.get(EMAIL),
                  getDateOption(options, VALIDFROM), getDateOption(options, VALIDTO));
         }
         else
         {
            user = sf.getUserService().createUser(
                  realm, account,
                  (String) options.get(FIRST_NAME), (String) options.get(LAST_NAME),
                  (String) options.get(DESCRIPTION), (String) options.get(PASSWORD),
                  (String) options.get(EMAIL),
                  getDateOption(options, VALIDFROM), getDateOption(options, VALIDTO));
         }

         if (options.containsKey(GRANTS))
         {
            addGrants((String) options.get(GRANTS), user);
            try
            {
               sf.getUserService().modifyUser(user);
            }
            catch (Exception e)
            {
               trace.warn("", e);
               print(MessageFormat.format(
                           "Grants of user ''{0}'' (realm: {1}) couldn't be created: {2}",
                           new Object[] { user.getAccount(), user.getRealm().getId(),
                                 e.getMessage() }));
               return -1;
            }
         }
      }
      finally
      {
         sf.close();
      }
      print(MessageFormat.format("User with account ''{0}'' (realm: {1}) created.",
            new Object[] { user.getAccount(), user.getRealm().getId() }));
      return 0;
   }

   public String getSummary()
   {
      return "Creates a Infinity user.";
   }
}
