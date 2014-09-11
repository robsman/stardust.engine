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
package org.eclipse.stardust.common.config;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * Provides a means to inject configuration properties during engine bootstrap.
 * <p>
 * The most prominent source of such configuration properties is {@code carnot.properties}
 * . Using this extension point allows to contribute configuration properties from
 * different sources, like a database, other property files from the classpath or even
 * computed values like random hashes.
 * <p>
 * While (re-)initializing the engine, all provider factories are discovered and sorted
 * with increasing priority. The resulting list of factories is then traversed to obtain
 * one provider per factory. Each provider, in turn, will be asked for its set of
 * properties, which will all be merged into {@code GlobalParameters}.
 * <p>
 * Providers with higher priority may overwrite values from previous providers by either
 * yielding a new value for a given key or by yielding {@code null} to remove a property.
 */
@SPI(status = Status.Stable, useRestriction = UseRestriction.Public)
public interface GlobalParametersProviderFactory
{
   /**
    * @return The priority of the associated provider. Providers are consulted in
    *         increasing order, thus a provider with priority 10 might override override
    *         values yielded by another provider with priority 1.
    *         <p>
    *         {@code carnot.properties} will be fetched with priority 1.
    */
   int getPriority();

   /**
    * @return The associated property provider.
    *         <p>
    *         Its up to the factory to decide if instantiates a new provider instance per
    *         request or uses some kind of caching / singleton.
    */
   PropertyProvider getPropertyProvider();
}
