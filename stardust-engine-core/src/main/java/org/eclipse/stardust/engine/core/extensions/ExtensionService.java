/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.extensions;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Modules;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ExtensionService
{
   /**
    * Injects definitions of extended data types.
    * 
    * @param model the model in which the definition of extended data types should be injected.
    */
   public static void createExtendedDataTypes(IModel model)
   {
      // (fh) none for the moment.
   }

   /**
    * Injects definitions of extended application types.
    * 
    * @param model the model in which the definition of extended application types should be injected.
    */
   public static void createExtendedApplicationTypes(IModel model)
   {
      // (fh) none for the moment.
   }

   /**
    * Injects definitions of extended event condition types.
    * 
    * @param model the model in which the definition of extended event condition types should be injected.
    */
   public static void createExtendedEventConditionTypes(IModel model)
   {
      // (fh) removed from code base.

/*    if (null == model.findEventConditionType(PredefinedConstants.EXPRESSION_CONDITION))
      {
         IEventConditionType expressionCondition = model.createEventConditionType(
         PredefinedConstants.EXPRESSION_CONDITION, "On Data Change", true,
         EventType.Engine, true, true, 0);
         expressionCondition.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_PANEL_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.VALIDATOR_CLASS_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_VALIDATOR_CLASS);
         expressionCondition.setAttribute(PredefinedConstants.ICON_ATT,
         PredefinedConstants.EXPRESSION_CONDITION_ICON_LOCATION);
      }

      if (null == model.findEventConditionType(PredefinedConstants.EXTERNAL_EVENT_CONDITION))
      {
         IEventConditionType externalCondition = model.createEventConditionType(
               PredefinedConstants.EXTERNAL_EVENT_CONDITION, "External Event", true,
               EventType.Push, true, true, 0);
         externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXTERNAL_CONDITION_CLASS);
         externalCondition.setAttribute(PredefinedConstants.ICON_ATT,
               PredefinedConstants.EXTERNAL_CONDITION_ICON_LOCATION);
      }*/
   }
   
   /**
    * Injects definitions of extended event action types.
    * 
    * @param model the model in which the definition of extended event action types should be injected.
    */
   public static void createExtendedEventActionTypes(IModel model)
   {
      // (fh) none for the moment.
   }

   /**
    * Initializes extensions of the given module.
    */
   public static void initializeModuleExtensions(Modules module)
   {
      // TODO:
   }

   /**
    * Initializes user realm extensions.
    */
   public static void initializeRealmExtensions()
   {
      // TODO:
   }
   
   /**
    * TODO:
    * 
    * @param options
    */
   public static void resetModuleExtensions(String options)
   {
      // TODO:
   }
}
