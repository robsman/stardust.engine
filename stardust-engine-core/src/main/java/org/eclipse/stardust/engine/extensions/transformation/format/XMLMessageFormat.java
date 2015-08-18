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
import java.io.PrintStream;
import java.io.Reader;

import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


public class XMLMessageFormat implements IMessageFormat
{
	public Document parse(InputStream input, Object schema) throws ParsingException
	{
		try
		{
	      return XmlUtils.parseSource(new InputSource(input), new ClasspathEntityResolver(), true);
		}
		catch (Exception e)
		{
			throw new ParsingException(e);
		}
		
	}

    public Document parse(Reader input, Object schema) throws ParsingException
    {
        try
        {
          return XmlUtils.parseSource(new InputSource(input), new ClasspathEntityResolver(), true);
        }
        catch (Exception e)
        {
            throw new ParsingException(e);
        }
        
    }

	public void serialize(Document document, OutputStream output, Object schema) throws SerializationException
	{
	   
	   writeStringToOutputStream(output, XmlUtils.toString(document));	   
	}
	
	private void writeStringToOutputStream(OutputStream stream, String message)
	{
	   new PrintStream(stream).print(message);
	}
}
