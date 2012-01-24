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
package org.eclipse.stardust.engine.cli.sysconsole;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AuditTrailCommand extends ConsoleCommand
{

   protected void setConnectionOptions()
   {
      // @todo (france, ub): can be replaced by system properties
      if (globalOptions.containsKey("dbschema"))
      {
         Parameters.instance().set(
               SessionFactory.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX,
               globalOptions.get("dbschema"));
      }
      if (globalOptions.containsKey("dbuser"))
      {
         Parameters.instance().set(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_USER_SUFFIX,
               globalOptions.get("dbuser"));
      }
      if (globalOptions.containsKey("dbpassword"))
      {
         Parameters.instance().set(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_PASSWORD_SUFFIX,
               globalOptions.get("dbpassword"));
      }
      if (globalOptions.containsKey("dburl"))
      {
         Parameters.instance().set(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_URL_SUFFIX,
               globalOptions.get("dburl"));
      }
      if (globalOptions.containsKey("dbdriver"))
      {
         Parameters.instance().set(
               SessionProperties.DS_NAME_AUDIT_TRAIL
                     + SessionProperties.DS_DRIVER_CLASS_SUFFIX,
               globalOptions.get("dbdriver"));
      }
      if (globalOptions.containsKey("dbtype"))
      {
         Parameters.instance().set(
               SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_TYPE_SUFFIX,
               globalOptions.get("dbtype"));
      }
   }
   
   public int run(Map options)
   {
      setConnectionOptions();
      
      printCommand(options);

      print("Database type   : "
            + Parameters.instance().getString(
                  SessionProperties.DS_NAME_AUDIT_TRAIL
                        + SessionProperties.DS_TYPE_SUFFIX));
      print("Database URL    : "
            + Parameters.instance()
                  .getString(
                        SessionProperties.DS_NAME_AUDIT_TRAIL
                              + SessionProperties.DS_URL_SUFFIX));
      print("Database user   : "
            + Parameters.instance().getString(
                  SessionProperties.DS_NAME_AUDIT_TRAIL
                        + SessionProperties.DS_USER_SUFFIX));
      if ( !StringUtils.isEmpty(Parameters.instance().getString(
            SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX)))
      {
         print("Database schema : "
               + Parameters.instance().getString(
                     SessionProperties.DS_NAME_AUDIT_TRAIL
                           + SessionProperties.DS_SCHEMA_SUFFIX));
      }      
      print("Database driver : "
            + Parameters.instance().getString(
                  SessionProperties.DS_NAME_AUDIT_TRAIL
                        + SessionProperties.DS_DRIVER_CLASS_SUFFIX));
      try
      {
         Class.forName(Parameters.instance().getString(
               SessionProperties.DS_NAME_AUDIT_TRAIL
                     + SessionProperties.DS_DRIVER_CLASS_SUFFIX));
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException("Driver not found.");
      }
      print("");

      if (!force())
      {
         if (!confirm("Do you want to proceed? (Y/N): "))
         {
            return -1;
         };
      }

      Session session = SessionFactory.createSession(SessionFactory.AUDIT_TRAIL);

      try
      {
         Map locals = new HashMap();
         locals.put(SessionFactory.AUDIT_TRAIL + SessionProperties.DS_SESSION_SUFFIX, session);
         ParametersFacade.pushLayer(locals);
         return doRun(options);
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public abstract int doRun(Map options);

   public abstract void printCommand(Map options);
}
