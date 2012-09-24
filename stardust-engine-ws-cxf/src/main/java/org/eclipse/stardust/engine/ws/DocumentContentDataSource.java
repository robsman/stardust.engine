/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.eclipse.stardust.engine.api.runtime.DocumentInfo;


/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class DocumentContentDataSource implements DataSource
{

   private final DocumentInfo docInfo;
   
   private final byte[] content;

   public DocumentContentDataSource(DocumentInfo docInfo, byte[] content)
   {
      this.docInfo = docInfo;
      this.content = content;
   }

   public String getContentType()
   {
      return (null != docInfo) ? docInfo.getContentType() : "application/octet-stream";
   }

   public InputStream getInputStream() throws IOException
   {
      return new ByteArrayInputStream(content);
   }

   public String getName()
   {
      return (null != docInfo) ? docInfo.getName() : null;
   }

   public OutputStream getOutputStream() throws IOException
   {
      throw new UnsupportedOperationException();
   }

}
