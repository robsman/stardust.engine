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

import java.util.List;

import org.eclipse.stardust.engine.api.model.EventActionContext;
import org.eclipse.stardust.engine.api.model.IBindAction;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class BindActionBean extends ActionBean implements IBindAction
{
   public BindActionBean()
   {
      super();
   }

   public BindActionBean(String id, String name)
   {
     super(id, name);
   }

   public void checkConsistency(List inconsistencies)
   {
      super.checkConsistency(inconsistencies, "BindAction");
   }

   public EventActionContext getContext()
   {
      return EventActionContext.Bind;
   }

   public String toString()
   {
     return  "BindAction: " + getName();
   }
}
