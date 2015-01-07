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
public class Transition extends IdentifiableElement
{
   private String sourceId;
   private String targetID;
   private String condition;
   private boolean forkOnTraversal;

   public Transition(String id, String name, String description,
         String sourceID, String targetID, String condition,
         boolean forkOnTraversal, int elementOID, Model model)
   {
      super(id, name, description);
      this.sourceId = sourceID;
      this.targetID = targetID;
      this.condition = condition;
      this.forkOnTraversal = forkOnTraversal;
      model.register(this, elementOID);
   }

   public String getCondition()
   {
      return condition;
   }

   public boolean isForkOnTraversal()
   {
      return forkOnTraversal;
   }

   public String getSourceId()
   {
      return sourceId;
   }

   public String getTargetID()
   {
      return targetID;
   }
}
