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
package org.eclipse.stardust.engine.core.model.gui;

import java.util.HashMap;

import javax.swing.ImageIcon;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.gui.IconProvider;
import org.eclipse.stardust.engine.core.model.utils.ModelElements;
import org.eclipse.stardust.engine.core.model.utils.ModifiedProperty;
import org.eclipse.stardust.engine.core.model.utils.RemovedReference;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SymbolIconProvider implements IconProvider
{
   private static final Logger trace = LogManager.getLogger(SymbolIconProvider.class);

   private static SymbolIconProvider instance;
   private HashMap icons = new HashMap();

   public ImageIcon getIcon(Object object)
   {
      String icon = null;
      String fallbackIcon = null;
      if (object instanceof IModel)
      {
         icon = "model.gif";
      }
      else if (object instanceof Typeable && ((Typeable) object).getType() != null)
      {
         return getIcon(((Typeable) object).getType());
      }
      else if (object instanceof PluggableType)
      {
         icon = ((PluggableType)object).getStringAttribute(PredefinedConstants.ICON_ATT);
         if (icon == null)
         {
            if (object instanceof IApplicationType)
            {
               icon = "application.gif";
            }
            else if (object instanceof IApplicationContextType)
            {
               icon = "context.gif";
            }
            else if (object instanceof IDataType)
            {
               icon = "data.gif";
            }
            else if (object instanceof ITriggerType)
            {
               icon = "trigger.gif";
            }
            else if (object instanceof IEventConditionType)
            {
               // @todo (france, ub): only placeholder
               icon = "condition.gif";
            }
            else if (object instanceof IEventActionType)
            {
               // @todo (france, ub): only placeholder
               icon = "action.gif";
            }
         }
      }
      else if (object instanceof IApplication)
      {
         // hint: (fh) interactive application doesn't have an ApplicationType
         icon = "application.gif";
      }
      else if (object instanceof IActivity)
      {
         icon = "activity_20_16.gif";
      }
      else if (object instanceof IDataMapping)
      {
         icon = "datamapping.gif";
         if (((IDataMapping) object).getDirection() == Direction.IN)
         {
            icon = "in_datamapping.gif";
         }
         else if (((IDataMapping) object).getDirection() == Direction.OUT)
         {
            icon = "out_datamapping.gif";
         }
      }
      else if (object instanceof IDataPath)
      {
         icon = "datapath.gif";
         if (((IDataPath) object).getDirection() == Direction.IN)
         {
            icon = ((IDataPath) object).isDescriptor() ?
                  "descriptor.gif" : "in_datapath.gif";
         }
         else if (((IDataPath) object).getDirection() == Direction.OUT)
         {
            icon = "out_datapath.gif";
         }
      }
      else if (object instanceof Diagram)
      {
         icon = "diagram.gif";
      }
      else if (object instanceof IModeler)
      {
         icon = "modeler.gif";
      }
      else if (object instanceof IConditionalPerformer)
      {
         icon = "conditional.gif";
      }
      else if (object instanceof ILinkType)
      {
         icon = "link_type.gif";
      }
      else if (object instanceof IOrganization)
      {
         icon = "organization.gif";
      }
      else if (object instanceof IProcessDefinition)
      {
         icon = "process.gif";
      }
      else if (object instanceof IRole)
      {
         icon = "role.gif";
      }
      else if (object instanceof ITransition)
      {
         icon = "transition.gif";
      }
      else if (object instanceof IView)
      {
         icon = "view.gif";
      }
      else if (object instanceof ModelElements
        || object instanceof ModifiedProperty
        || object instanceof RemovedReference)
      {
         // @todo (france, ub): only placeholder
         icon ="sphere_red.gif";
      }
      else
      {
         trace.warn("No icon defined for object '"
               + (object==null ? null : object.getClass().getName()) + "'.", new Exception());
         return null;
      }

      ImageIcon image = (ImageIcon) icons.get(icon);
      if (image == null)
      {
         try
         {
            if (icon.startsWith("/"))
            {
               image = new ImageIcon(getClass().getResource(icon));
            }
            else
            {
               image = new ImageIcon(getClass().getResource("images/" + icon));
            }
         }
         catch (Exception x)
         {
            if (!StringUtils.isEmpty(fallbackIcon))
            {
               try
               {
                  if (fallbackIcon.startsWith("/"))
                  {
                     image = new ImageIcon(getClass().getResource(fallbackIcon));
                  }
                  else
                  {
                     image = new ImageIcon(getClass().getResource("images/" + fallbackIcon));
                  }
               }
               catch (Exception x2)
               {
                  trace.info("The resource '" + fallbackIcon
                        + "' for object '" + object + "' couldn't be loaded.");
               }
            }
            else
            {
               trace.info("The resource '" + icon + "' for object '" + object
                     + "' couldn't be loaded.");
            }
         }
         icons.put(icon, image);
      }
      return image;
   }

   public static IconProvider instance()
   {
      if (instance == null)
      {
         instance = new SymbolIconProvider();
      }
      return instance;
   }
}
