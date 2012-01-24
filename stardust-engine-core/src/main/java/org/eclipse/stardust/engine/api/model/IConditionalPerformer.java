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

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;


/**
 *
 */
public interface IConditionalPerformer extends IModelParticipant
{
   public boolean isUser();

   public void setUser(boolean user);
   
   public ParticipantType getPerformerKind();

   /**
    * Returns the workflow data, the conditional performer is retrieved from.
    */
   public IData getData();

   /**
    * Sets the workflow data, the conditional performer is retrieved from.
    */
   public void setData(IData data);

   public String getDereferencePath();

   public void setDereferencePath(String dereferencePath);

   /**
    * Retrieves the actual performer of a conditional performer
    */
   public IParticipant retrievePerformer(IProcessInstance processInstance)
         throws PublicException;
}
