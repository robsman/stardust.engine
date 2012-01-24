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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventConditionType extends IdentifiableElement
{
   private boolean activityCondition;
   private boolean processCondition;
   private String implementation;

   public EventConditionType(String id, String name, String implementation,
         boolean processCondition, boolean activityCondition, boolean predefined)
   {
      super(id, name, null);
      this.processCondition = processCondition;
      this.activityCondition = activityCondition;
      this.predefined = predefined;
      this.implementation = implementation;
   }

   public boolean isActivityCondition()
   {
      return activityCondition;
   }

   public boolean isProcessCondition()
   {
      return processCondition;
   }

   public String getImplementation()
   {
      return implementation;
   }
}
