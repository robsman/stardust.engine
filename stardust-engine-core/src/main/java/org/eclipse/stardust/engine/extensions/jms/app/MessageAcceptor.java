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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


/**
 * Interface used in the ResponseHandler to define a matching rule for an incoming
 * Message and to extract data out of it.
 *
 * @see DefaultMessageAcceptor
 *
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface MessageAcceptor
{
   /**
    * Returns all hibernated activity instances possibly affected by a match of this
    * acceptor  against the provided message.
    * <p>
    * The engine will awake the first unused activity instance from this list.
    *
    * @param message
    * @return
    */
   Iterator<IActivityInstance> getMatchingActivityInstances(Message message);

   /**
    * Extracts the data
    *
    * @param message
    * @return
    */
   Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints);

   String getName();

   boolean hasPredefinedAccessPoints(StringKey id);

   Collection getAccessPoints(StringKey id);

   Collection getMessageTypes();

}
