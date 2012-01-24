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

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

public interface IMessageFormat
{
	// TODO make object more specific, at the moment
	/**
	 * Parses a message in the representation of this format into a canonical DOM representation.
	 * 
	 * The schema argument will become XSD as soon as MINT supports this.
	 * @param input
	 * @return
	 */
	public Document parse(InputStream input, Object schema) throws ParsingException;
	
	/**
	 * Serializes a message in the representation of this format into a canonical DOM representation.
	 * 
	 * @param document
	 * @return
	 */
	public void serialize(Document document, OutputStream output, Object schema) throws SerializationException;
	
	interface Factory
	{
	   IMessageFormat getMessageFormat(String formatId);
	}
}
