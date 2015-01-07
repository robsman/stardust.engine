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
package org.eclipse.stardust.engine.core.runtime.command;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * Client side wrapper around a series of service invocations that have to be performed
 * in a single server side transaction.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ServiceCommand extends Serializable
{
   /**
    * Execute the service command.
    * 
    * @param sf a {@link ServiceFactory} provided by the engine that will ensure that all service calls
    *        will be performed in the parent transaction.
    * @return the result of the service call execution. This value will be returned to the client.
    */
   Serializable execute(ServiceFactory sf);
}