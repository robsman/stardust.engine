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

import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.ejb2.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;


/**
 * @author holger.prause
 * @version
 * 
 *          Abstract baseclass for dealing with events on the console
 */
public abstract class EventHandlerCommand extends ConsoleCommand
{

   public static final String TYPE_OPTION_KEY = "type";
   public static final String TYPE_OPTION_AI_VALUE = "ai";
   public static final String TYPE_OPTION_PI_VALUE = "pi";
   public static final String OID_OPTION_KEY = "oid";
   public static final String HANDLER_OPTION_KEY = "handler";
   private Options argTypes = new Options();

   protected EventHandlerCommand()
   {
      // register type option
      String[] validOptionValues = {TYPE_OPTION_AI_VALUE, TYPE_OPTION_PI_VALUE};
      String longname = "-type";
      String shortname = "-t";
      String keyname = TYPE_OPTION_KEY;
      String summary = "The type to register for - valid values are 'ai' for ActivityInstance"
            + " or 'pi' for ProcessInstance";
      boolean hasArg = true;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
      argTypes.addMandatoryRule(keyname);
      argTypes.addValueRangeRule(keyname, validOptionValues);

      // register oid option
      longname = "-oid";
      shortname = "-oid";
      keyname = OID_OPTION_KEY;
      summary = "The oid for the ActivityInstance or ProcessInstance";
      hasArg = true;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
      argTypes.addMandatoryRule(keyname);

      // register handler option
      longname = "-handler";
      shortname = "-h";
      keyname = HANDLER_OPTION_KEY;
      summary = "The id of the EventHandler";
      hasArg = true;
      argTypes.register(longname, shortname, keyname, summary, hasArg);
      argTypes.addMandatoryRule(keyname);
   }

   @Override
   public Options getOptions()
   {
      return argTypes;
   }

   protected EventHandlerConfig getConfig(Map options)
   {
      EventHandlerConfig config = new EventHandlerConfig();
      String userName = (String) globalOptions.get("user");
      String passWord = (String) globalOptions.get("password");
      String type = (String) options.get(TYPE_OPTION_KEY);
      String handler = (String) options.get(HANDLER_OPTION_KEY);
      Long oid = Options.getLongValue(options, OID_OPTION_KEY);
      // userName and password have no mandatory rule set - so do validation here
      if (StringUtils.isEmpty(userName))
      {
         throw new IllegalArgumentException("the global option user must be provided");
      }
      if (StringUtils.isEmpty(passWord))
      {
         throw new IllegalArgumentException("the global option password must be provided");
      }

      config.setUserName(userName);
      config.setPassWord(passWord);
      config.setHandler(handler);
      config.setType(type);
      config.setOid(oid);
      return config;
   }

   protected WorkflowService getWorkflowService(EventHandlerConfig config)
   {
      ServiceFactory serviceFactory = ServiceFactoryLocator.get(config.getUserName(),
            config.getPassWord());
      WorkflowService workflowService = serviceFactory.getWorkflowService();
      return workflowService;
   }

   protected class EventHandlerConfig
   {
      private String handler;
      private Long oid;
      private String type;
      private String userName;
      private String passWord;

      public String getHandler()
      {
         return handler;
      }

      public void setHandler(String handler)
      {
         this.handler = handler;
      }

      public Long getOid()
      {
         return oid;
      }

      public void setOid(Long oid)
      {
         this.oid = oid;
      }

      public String getType()
      {
         return type;
      }

      public void setType(String type)
      {
         this.type = type;
      }

      public String getUserName()
      {
         return userName;
      }

      public void setUserName(String userName)
      {
         this.userName = userName;
      }

      public String getPassWord()
      {
         return passWord;
      }

      public void setPassWord(String passWord)
      {
         this.passWord = passWord;
      }
   }
}
