/***********************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 ***********************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;

import javax.jms.MapMessage;

import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;

/**
 * <p>
 * A Signal {@code MapMessage} fired by a BPMN event.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public interface ISignalMessage extends IdentifiablePersistent
{
   public long getPartitionOid();

   public String getSignalName();

   public MapMessage getMessage();

   public Date getTimestamp();
}
