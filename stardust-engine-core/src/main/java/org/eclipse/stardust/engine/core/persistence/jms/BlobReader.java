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
package org.eclipse.stardust.engine.core.persistence.jms;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author sauer
 * @version $Revision$
 */
public interface BlobReader
{

   void init(Parameters params) throws PublicException;
   
   boolean nextBlob() throws PublicException;
   
   void close() throws PublicException;
   
   boolean readBoolean() throws InternalException;

   char readChar() throws InternalException;

   byte readByte() throws InternalException;

   short readShort() throws InternalException;

   int readInt() throws InternalException;

   long readLong() throws InternalException;

   float readFloat() throws InternalException;

   double readDouble() throws InternalException;

   String readString() throws InternalException;

}
