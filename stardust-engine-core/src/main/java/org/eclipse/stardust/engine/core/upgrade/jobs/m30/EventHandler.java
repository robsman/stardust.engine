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
public class EventHandler extends IdentifiableElement
{
   private String type;
   private Model model;
   private Vector actions = new Vector();
   private boolean autobind;
   private boolean unbindOnMatch;

   public EventHandler(String id, String type, int elementOID, Model model)
   {
      super(id, id, null);
      this.type = type;
      this.model = model;
      model.register(this, elementOID);
   }

   public EventAction createEventAction(String type, String id, String name)
   {
      EventAction result = new EventAction(type, id, name);
      actions.add(result);
      model.register(result, 0);
      return result;
   }

   public Iterator getAllActions()
   {
      return actions.iterator();
   }

   public String getType()
   {
      return type;
   }

   public void setAutobind(boolean autobind)
   {
      this.autobind = autobind;
   }

   public boolean isAutoBind()
   {
      return autobind;
   }

   public boolean isConsumeOnMatch()
   {
      return false;
   }

   public boolean isUnbindOnMatch()
   {
      return unbindOnMatch;
   }

   public boolean isLogHandler()
   {
      return true;
   }

   public void setUnbindOnMatch(boolean b)
   {
      unbindOnMatch = b;
   }
}
