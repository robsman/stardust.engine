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
package org.eclipse.stardust.engine.extensions.jms.trigger;

import java.util.Collection;
import java.util.Map;

import javax.jms.Message;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.model.Trigger;


/**
 * Interace used in the <code>ResponseHandler</code> to define a matching rule for an
 * incoming trigger message and to extract data out of it.
 *
 * @see org.eclipse.stardust.engine.extensions.jms.trigger.DefaultTriggerMessageAcceptor
 *
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface TriggerMessageAcceptor
{
   /**
    * Describes the list of accepted JMS message types.
    *
    * @return The list of accepted JMS message types.
    */
   Collection getMessageTypes();

   String getName();

   boolean hasPredefinedParameters(StringKey messageType);

   Collection getPredefinedParameters(StringKey messageType);

   Map acceptMessage(Message message, Trigger trigger);
}
