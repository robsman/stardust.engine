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
public interface BlobBuilder
{
   
   final byte SECTION_MARKER_INSTANCES = 1;

   final byte SECTION_MARKER_EOF = -1;

   void init(Parameters params) throws PublicException;

   void persistAndClose() throws PublicException;
   
   void startInstancesSection(String tableName, int nInstances) throws InternalException;

   void writeBoolean(boolean value) throws InternalException;

   void writeChar(char value) throws InternalException;

   void writeByte(byte value) throws InternalException;

   void writeShort(short value) throws InternalException;

   void writeInt(int value) throws InternalException;

   void writeLong(long value) throws InternalException;

   void writeFloat(float value) throws InternalException;

   void writeDouble(double value) throws InternalException;

   void writeString(String value) throws InternalException;

}
