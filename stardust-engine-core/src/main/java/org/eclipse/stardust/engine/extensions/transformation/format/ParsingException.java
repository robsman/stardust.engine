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

public class ParsingException extends Exception
{
	public ParsingException()
	{
		super();
	}

	public ParsingException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public ParsingException(String arg0)
	{
		super(arg0);
	}

	public ParsingException(Throwable arg0)
	{
		super(arg0);
	}
}
