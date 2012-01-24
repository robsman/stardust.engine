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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.engine.api.model.ITriggerType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TriggerTypeBean extends IdentifiableElementBean implements ITriggerType
{
   static final String PULL_TRIGGER="Pull Trigger";
   private boolean pullTrigger;

   public TriggerTypeBean()
   {
   }

   public TriggerTypeBean(String id, String name, boolean predefined, boolean pullTrigger)
   {
      super(id, name);
      setPredefined(predefined);
      this.pullTrigger = pullTrigger;
   }

   public boolean isPullTrigger()
   {
      return pullTrigger;
   }

   public String toString()
   {
      return "Trigger Type: " + getName();
   }
}
