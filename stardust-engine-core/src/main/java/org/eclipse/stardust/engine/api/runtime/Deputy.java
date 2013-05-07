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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;

/**
 * The <code>Deputy</code> represents a snapshot of the deputy user association.
 *
 * @author Stephan.Born
 *
 */
public interface Deputy extends Serializable
{
   /**
    * Returns the user for which a deputy user is set.
    * 
    * @return the user
    */
   UserInfo getUser();
   
   /**
    * @return the deputy user.
    */
   UserInfo getDeputyUser();
   
   /**
    * Returns the date from when the deputy association is active.
    * 
    * @return the activation date
    */
   Date getFromDate();
   
   
   /**
    * Returns the date when the deputy association is deactivated.
    * 
    * @return the deactivation date. null means that no deactivation date is set.
    */
   Date getUntilDate();
   
   /**
    * Return the participant which are used to restrict the grants inherited by deputy
    * user from user.
    * 
    * @return Set of participants used for restricting grant inheritance.
    */
   public Set<ModelParticipantInfo> getParticipints();
}
