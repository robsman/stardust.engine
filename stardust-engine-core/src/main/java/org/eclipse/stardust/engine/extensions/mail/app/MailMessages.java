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
package org.eclipse.stardust.engine.extensions.mail.app;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 
 * @author mgille
 */
public class MailMessages
{
	private static final String BUNDLE_NAME = "org.eclipse.stardust.engine.extensions.mail.app.mail_messages"; 
	private static ResourceBundle bundle;
	
	private MailMessages()
	{
	}

	static
	{
		// Initialize resource bundle
		
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
	}

	public static String getString(String property)
	{
		try
		{
			String string = bundle.getString(property);			
			
			return string;
		}
		catch (MissingResourceException e)
		{
			return "!" + property + "!";
		}
	}
}
