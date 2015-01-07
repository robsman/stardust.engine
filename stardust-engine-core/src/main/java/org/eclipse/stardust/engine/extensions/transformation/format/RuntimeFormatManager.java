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
package org.eclipse.stardust.engine.extensions.transformation.format;

import java.util.List;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;


/**
 * Used as long as the runtime cannot rely on OSGI.
 * TODO needs to be made configurable via properties.
 * 
 * @author Marc Gille
 *
 */
public class RuntimeFormatManager
{
	public static IMessageFormat getMessageFormat(String formatId)
	{
	   List formatFactories = ExtensionProviderUtils.getExtensionProviders(IMessageFormat.Factory.class);
	   
	   IMessageFormat format = null;
	   for (int i = 0; i < formatFactories.size(); ++i)
      {
         IMessageFormat.Factory factory = (IMessageFormat.Factory) formatFactories.get(i);
         format = factory.getMessageFormat(formatId);
         if (null != format)
         {
            return format;
         }
      }
	   
		throw new IllegalArgumentException("Unknown format id " + formatId);
	}
}
