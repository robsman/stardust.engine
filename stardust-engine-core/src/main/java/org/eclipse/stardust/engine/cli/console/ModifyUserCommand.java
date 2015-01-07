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
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserRealm;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModifyUserCommand extends UserCommand
{
   private static final String ADDGRANTS = "addgrants";
   private static final String REMOVEGRANTS = "removegrants";
   private static final String NEWACCOUNT = "newaccount";
   private ServiceFactory sf;

   public ModifyUserCommand()
   {
      argTypes.register("-newaccount", "-n", NEWACCOUNT,
            "The new account of the user, in case it should be changed.", true);
      argTypes.register("-addgrants", "-g", ADDGRANTS,
            "Grants to be given to the user. Specify as comma separated list of participant IDs.\n"
            + "Department constraints can be given with the format 'id@departmentoid', e.g.\n"
            + "'engineer@33,clerk'.\n or as id@departmentpath e.g. engineer@root/child,clerk"
            + "Model version constraints can be given with the format 'id:modeloid', e.g.\n"
            + "'engineer:47,clerk'.\n"
            + "Model version and department constraints are mutually exclusive.", true);
      argTypes.register("-removegrants", "-r", REMOVEGRANTS,
            "Grants to be removed the user. Specify as comma separated list of participant IDs.\n"
    		+ "Department constraints can be given with the format 'id@departmentoid', e.g.\n"
            + "'engineer@33,clerk'.\n or as id@departmentpath e.g. engineer@root/child,clerk"
            + "Model version constraints can be given with the format 'id:modeloid', e.g.\n"
            + "'engineer:47,clerk'.\n"
            + "Model version and department constraints are mutually exclusive.", true);
   }

   public int run(Map options)
   {
      String account = (String) options.get(ACCOUNT);
      String realm = (String) options.get(REALM);
      String confirmMessage;
      if (StringUtils.isEmpty(realm))
      {
         confirmMessage = "Modify user with account ''{0}''?: ";
      }
      else
      {
         confirmMessage = "Modify user with account ''{0}'' (realm {1})?: ";
      }
      
      if ( !force()
            && !confirm(MessageFormat.format(confirmMessage, new Object[] { account,
                  realm })))
      {
         return -1;
      }
      
      sf = ServiceFactoryLocator.get(globalOptions);
      User user;
      try
      {
         if (StringUtils.isEmpty(realm))
         {
            user = sf.getUserService().getUser(account);
         }
         else
         {
            user = sf.getUserService().getUser(realm, account);
         }
         
         if (options.containsKey(NEWACCOUNT))
         {
            user.setAccount((String) options.get(NEWACCOUNT));
         }
         if (options.containsKey(FIRST_NAME))
         {
            user.setFirstName((String) options.get(FIRST_NAME));
         }
         if (options.containsKey(LAST_NAME))
         {
            user.setLastName((String) options.get(LAST_NAME));
         }
         if (options.containsKey(DESCRIPTION))
         {
            user.setDescription((String) options.get(DESCRIPTION));
         }
         if (options.containsKey(PASSWORD))
         {
            user.setPassword((String) options.get(PASSWORD));
         }
         if (options.containsKey(VALIDFROM))
         {
            user.setValidFrom(getDateOption(options, VALIDFROM));
         }
         if (options.containsKey(VALIDTO))
         {
            user.setValidTo(getDateOption(options, VALIDTO));
         }
         if (options.containsKey(EMAIL))
         {
            user.setEMail((String) options.get(EMAIL));
         }
         if (options.containsKey(ADDGRANTS))
         {
            addGrants((String) options.get(ADDGRANTS), user);
         }
         if (options.containsKey(REMOVEGRANTS))
         {
            removeGrants((String) options.get(REMOVEGRANTS), user);
         }
         
         sf.getUserService().modifyUser(user);
      }
      finally
      {
         sf.close();
      }
      
      UserRealm userRealm = user.getRealm();
      print(MessageFormat.format("User with account ''{0}'' (realm: {1}) modified.",
            new Object[] {user.getAccount(), userRealm == null ? "" : userRealm.getId() }));
      return 0;
   }

   public String getSummary()
   {
      return "Modifies a Infinity user.";
   }
}
