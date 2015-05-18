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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IWorkItem
{
   public void update(IActivityInstance activityInstance);
   
   public void doUpdate(IActivityInstance activityInstance);
   
   public long getActivityInstanceOID();

   public long getProcessInstanceOID();
   
   public IActivity getActivity();
   
   public IUser getCurrentUserPerformer();
   
   public IParticipant getCurrentPerformer();
   
   public Date getLastModificationTime();
   
   public Date getStartTime();
   
   public ActivityInstanceState getState();
   
   public IDepartment getDepartment();
   
   public long getDepartmentOid();

   public long getCurrentPerformerOID();

   public long getCurrentUserPerformerOID();
   
   public double getCriticality();
   
   public int getBenchmarkValue();
}
