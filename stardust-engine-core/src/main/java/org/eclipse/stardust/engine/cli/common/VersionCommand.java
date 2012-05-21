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
package org.eclipse.stardust.engine.cli.common;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class VersionCommand extends ConsoleCommand
{
   private static final String COMPONENT = "component";
   private static final String COMPONENT_ENGINE = "engine";
   private static final String COMPONENT_CLIENT = "client";
   private static final String COMPONENT_ALL = "all";
   private ServiceFactory sf;
   
   protected Options argTypes = new Options();

   public VersionCommand()
   {
      argTypes.register("-"+COMPONENT, "-c", COMPONENT,
            "The new account of the user, in case it should be changed.", true);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      String component = (String) options.get(COMPONENT);
      if (StringUtils.isEmpty(component))
      {
         component = COMPONENT_CLIENT;
      }

      if (COMPONENT_ENGINE.equals(component) || COMPONENT_ALL.equals(component))
      {
         sf = ServiceFactoryLocator.get(globalOptions);
         QueryService qs = sf.getQueryService();
         RuntimeEnvironmentInfo rtEnvInfo = qs.getRuntimeEnvironmentInfo();
         
         Version version = rtEnvInfo.getVersion();
         print(MessageFormat.format("IPP engine version: {0}.",
               new Object[] { version.toCompleteString() }));
      }

      if (COMPONENT_CLIENT.equals(component) || COMPONENT_ALL.equals(component))
      {
         Version version = CurrentVersion.getBuildVersion();
         print(MessageFormat.format("IPP client version: {0}.",
               new Object[] { version.toCompleteString() }));
      }

      return 0;
   }

   public String getSummary()
   {
      return "Returns version information for the Infinity process engine.";
   }
}
