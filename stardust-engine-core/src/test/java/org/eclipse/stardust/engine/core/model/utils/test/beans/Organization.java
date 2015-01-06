/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision: 6165 $
 */
public class Organization extends Participant
{
   private static final long serialVersionUID = 7020681493425844588L;

   //MultiRef participants = new MultiRef(this, "Organizations", "organizations");
	Vector participants = new Vector();

   Organization() {}
   public Organization(String id)
   {
      super(id);
   }

   public void addToParticipants(Participant p)
   {
     participants.add(p);
   }

   public Iterator getAllParticipants()
   {
      return participants.iterator();
   }

   public Participant findParticipant(String id)
   {
	  
	  while (participants.iterator().hasNext()) {
		  Participant participant = (Participant) participants.iterator().next();
		  if (participant.getId().equals(id)) {
			  return participant;
		  }
	  }
      // return (Participant) participants.findById(id);
	  return null;
   }

   public long getParticipantsCount()
   {
      return participants.size();
   }

}
