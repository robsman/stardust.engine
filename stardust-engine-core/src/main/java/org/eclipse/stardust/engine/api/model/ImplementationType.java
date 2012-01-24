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
package org.eclipse.stardust.engine.api.model;

import java.util.List;

import org.eclipse.stardust.common.StringKey;


/**
 * The implementation type of an activity.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ImplementationType extends StringKey
{
   private static final long serialVersionUID = 5034016640086191938L;

   private static final String[] defaultContexts = {
      PredefinedConstants.DEFAULT_CONTEXT,
      PredefinedConstants.ENGINE_CONTEXT
   };

   private static final String[] engineContext = {
      PredefinedConstants.ENGINE_CONTEXT
   };

   /**
    * An interactive activity which does not execute any application.
    */
   public static final ImplementationType Manual = new ImplementationType(
         "Manual", "Manual", defaultContexts);
   /**
    * An activity which executes an application, either interactive or non-interactive.
    */
   public static final ImplementationType Application = new ImplementationType(
         "Application", "Application", null);
   /**
    * An activity which contains a sub process.
    */
   public static final ImplementationType SubProcess = new ImplementationType(
         "Subprocess", "Subprocess", engineContext);

   /**
    * An activity which is used for routing to other activities via the transitions.
    */
   public static final ImplementationType Route = new ImplementationType(
         "Route", "Route", engineContext);
   
   private static final List VALUE_CACHE = getKeys(ImplementationType.class);
   
   public static ImplementationType get(String id)
   {
      return (ImplementationType) getKey(ImplementationType.class, id, VALUE_CACHE);
   }

   private String[] contexts;

   private ImplementationType(String id, String name, String[] contexts)
   {
      super(id, name);
      this.contexts = contexts;
   }

   /**
    * Gets the identifiers of the predefined application contexts.
    *
    * @return an array containing the valid application contexts for this implementation type.
    */
   public String[] getContexts()
   {
      return contexts;
   }
   
   public final boolean isSubProcess()
   {
      return this == SubProcess;
   }
}
