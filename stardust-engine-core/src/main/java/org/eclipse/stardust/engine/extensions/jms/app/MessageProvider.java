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
package org.eclipse.stardust.engine.extensions.jms.app;


import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

import java.util.Collection;
import java.util.Map;

public interface MessageProvider
{
   public Message createMessage(Session jmsSession, ActivityInstance activityInstance,
         Map accessPoints);

   public String getName();

   public boolean hasPredefinedAccessPoints(StringKey messageType);

   public Collection getIntrinsicAccessPoints(StringKey messageType);

   public Collection getMessageTypes();
}
