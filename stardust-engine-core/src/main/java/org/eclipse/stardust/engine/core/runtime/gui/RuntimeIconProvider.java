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
package org.eclipse.stardust.engine.core.runtime.gui;


import java.util.HashMap;

import javax.swing.ImageIcon;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.core.compatibility.gui.IconProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RuntimeIconProvider implements IconProvider
{
   private static final Logger trace = LogManager.getLogger(RuntimeIconProvider.class);

   private static RuntimeIconProvider instance;
   private static HashMap mapIcon = new HashMap();

   public ImageIcon getIcon(Object object)
   {
      String icon = null;
      if (object instanceof DeployedModel)
      {
         icon = "released_version.gif";
      }
      else if (object instanceof Activity)
      {
         icon = "activity.gif";
      }
      else if (object instanceof ActivityInstance)
      {
         // todo (fh) same as activity ???
         icon = "activity_instance.gif";
      }
      else if (object instanceof DataMapping)
      {
         icon = "datamapping.gif";
      }
      else if (object instanceof DataPath)
      {
         icon = "datapath.gif";
      }
      else if (object instanceof Grant)
      {
         icon = ((Grant) object).isOrganization() ? "organization.gif" : "role.gif";
      }
      else if (object instanceof Organization)
      {
         icon = "organization.gif";
      }
      else if (object instanceof ProcessDefinition)
      {
         icon = "process.gif";
      }
      else if (object instanceof Role)
      {
         icon = "role.gif";
      }
      else if (object instanceof Worklist)
      {
         if (object instanceof ParticipantWorklist)
         {
            if (((ParticipantWorklist) object).getOwner() instanceof Organization)
            {
               icon = "organization.gif";
            }
            else // defaults to role
            {
               icon = "role.gif";
            }
         }
      }
/*      else if (object instanceof Trigger)
      {
         TriggerType type = ((Trigger) object).getType();
         if (type instanceof ManualTriggerType)
         {
            icon = "manual_trigger.gif";
         }
         else if (type instanceof JMSTriggerType)
         {
            icon = "jms_trigger.gif";
         }
         else if (type instanceof TimerTriggerType)
         {
            icon = "timer_based_trigger.gif";
         }
         else if (type instanceof MailTriggerType)
         {
            icon = "mail_trigger.gif";
         }
      }*/
      else
      {
         trace.warn("No icon defined for object '" + object + "'.");
         return null;
      }

      try
      {
         ImageIcon result = (ImageIcon) mapIcon.get(icon);
         if (result == null)
         {
            result = new ImageIcon(RuntimeIconProvider.class.getResource("images/" + icon));
            mapIcon.put(icon, result);
         }
         return result;
      }
      catch (Exception x)
      {
         throw new PublicException(
               "The resource '" + icon + "' for object '" + object + "' couldn't be loaded.");
      }
   }

   public static IconProvider instance()
   {
      if (instance == null)
      {
         instance = new RuntimeIconProvider();
      }
      return instance;
   }
}
