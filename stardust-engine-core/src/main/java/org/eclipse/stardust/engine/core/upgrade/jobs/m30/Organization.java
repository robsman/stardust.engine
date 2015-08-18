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
public class Organization extends IdentifiableElement
{
   private Vector participants = new Vector();

   public Organization(String id, String name, String description, int elementOID, Model model)
   {
      super(id, name, description);
      model.register(this, elementOID);
   }

   public void addToParticipants(String id)
   {
      participants.add(id);
   }

   public Iterator getAllParticipants()
   {
      return participants.iterator();
   }

}
