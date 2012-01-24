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

import java.util.Iterator;
import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EventActionType extends IdentifiableElement
{
   private boolean activityAction;
   private boolean processAction;
   private Vector supportedConditionTypes = new Vector();

   public EventActionType(String id, String name, boolean predefined,
         boolean processAction, boolean activityAction)
   {
      super(id, name, null);
      this.predefined = predefined;
      this.processAction = processAction;
      this.activityAction = activityAction;
   }

   public boolean isActivityAction()
   {
      return activityAction;
   }

   public boolean isProcessAction()
   {
      return processAction;
   }

   public void addSupportedConditionType(String type)
   {
      supportedConditionTypes.add(type);
   }

   public Iterator getAllSupportedConditionTypes()
   {
      return supportedConditionTypes.iterator();
   }
}
