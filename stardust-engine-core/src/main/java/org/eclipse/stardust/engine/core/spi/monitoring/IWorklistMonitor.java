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
package org.eclipse.stardust.engine.core.spi.monitoring;

import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


/**
 * An implementor of this interface can provide a worklist monitor which will be informed about changes
 * to the worklist.<br>
 * <br>
 * Be aware that the notifications are not TX save: You will be informed even if later in TX a rollback 
 * will happen and the change is not commited to DB. 
 * <br>
 * To publish an implementor to the engine a file named by the interface's factory has to be created in
 * the '/META-INF/services' folder of the jar.<br>
 * In this case: <b>com.infinity.bpm.rt.monitoring.IWorklistMonitor</b><br>
 * This file needs to contain the qualified class name of the implementor of this interface.<br>
 * <br>
 * This pattern follows the concept of the JDK6 <code>ServiceLoader.</code>
 * 
 * @author robert.sauer
 */
public interface IWorklistMonitor
{

   /**
    * Will be called at the end of transaction when the currentPerformer of activity instance has changed and is not null.
    * 
    * @param participant the participant to whose worklist the activity instance has been added.
    * @param activityInstance the activity instance which is affected.
    */
   void addedToWorklist(IParticipant participant, IActivityInstance activityInstance);

   /**
    * Will be called at the end of transaction when previousPerformer of activity instance has changed and is not null.
    * 
    * @param participant the participant from whose worklist the activity instance has been removed.
    * @param activityInstance the activity instance which is affected.
    */
   void removedFromWorklist(IParticipant participant, IActivityInstance activityInstance);

}
