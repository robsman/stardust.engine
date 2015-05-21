/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;


/**
 * @author roland.stamm
 */
public class ByteContentDataSource implements DataSource
{

   private final String name;

   private final byte[] content;

   public ByteContentDataSource(String name, byte[] content)
   {
      this.name = name;
      this.content = content;
   }

   public String getContentType()
   {
      return "application/octet-stream";
   }

   public InputStream getInputStream() throws IOException
   {
      return new ByteArrayInputStream(content);
   }

   public String getName()
   {
      return name;
   }

   public OutputStream getOutputStream() throws IOException
   {
      throw new UnsupportedOperationException();
   }

}
