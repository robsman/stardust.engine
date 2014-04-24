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
package org.eclipse.stardust.engine.extensions.mail.web.servlet;

import java.util.Map;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


public class MailConfirmationAction
	implements Action
{
   private long activityInstanceOID;
   private Map data;

   public MailConfirmationAction(long aiOid, Map data)
	{
		super();

		this.activityInstanceOID = aiOid;
		this.data = data;
	}

	public Object execute()
   {
      // TODO catch ObjectNotFoundException

      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);

      if (null != activityInstance)
      {
         activityInstance.lock();
         activityInstance.activate();

         ActivityThread activityThread = new ActivityThread(
               null, null, activityInstance, null, data, false);
         activityThread.run();
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.JMS_MATCHING_AI_FOUND_BUT_IT_IS_NOT_OF_RECEIVING_NATURE
                     .raise(activityInstanceOID));
      }

      return null;
   }
}
