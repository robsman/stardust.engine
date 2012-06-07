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
package org.eclipse.stardust.engine.core.runtime.removethis;

import org.eclipse.stardust.engine.api.model.Modules;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class EngineProperties
{
   public static final String FORKING_SERVICE_HOME = "Engine.ForkingServiceHome";
   public static final String THREAD_MODE = "ThreadMode";

   public static final String THREAD_MODE_ASYNCHRONOUS = "asynchronous";
   public static final String THREAD_MODE_SYNCHRONOUS = "synchronous";
   
   public static final String PROCESS_TERMINATION_THREAD_MODE = Modules.ENGINE.getId() + ".ProcessTermination." + THREAD_MODE;
   public static final String NOTIFICATION_THREAD_MODE = Modules.ENGINE.getId() + ".Notification." + THREAD_MODE;
   public static final String ACTIVITY_THREAD_CONTEXT = "ActivityThread.Context";
   public static final String FORK_LIST = "Engine.ForkList";
   public static final String MAIL_SENDER = "Mail.Sender";
   public static final String MAIL_HOST = "Mail.Host";
   public static final String MAIL_DEBUG = "Mail.Debug";
   public static final String CLIENT_SERVICE_FACTORY = "Client.ServiceFactory";
   public static final String SERVICEFACTORY_POOL = "ServiceFactory.Pool";
   public static final String WATCHER_PROPERTY = "Model.Watcher";
   
   public static final String DISABLED_DAYS_OF_WEEK = "DisabledDaysOfWeek";
}
