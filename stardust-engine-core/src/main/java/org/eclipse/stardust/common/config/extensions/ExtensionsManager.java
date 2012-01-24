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
package org.eclipse.stardust.common.config.extensions;

import java.util.List;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;


@SPI(status = Status.Experimental, useRestriction = UseRestriction.Internal)
public interface ExtensionsManager
{
   <T> T getFirstExtensionProvider(Class<T> providerIntfc, String configurationProperty);

   <T> List<T> getExtensionProviders(Class<T> providerIntfc, String configurationProperty);
}
