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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author sauer
 * @version $Revision$
 */
public class ByteArrayBlobBuilder implements BlobBuilder
{

   private ByteArrayOutputStream baos;

   private DataOutputStream msg;
   
   private byte[] blob;
   
   public byte[] getBlob()
   {
      return blob;
   }

   public void init(Parameters params) throws PublicException
   {
      this.baos = new ByteArrayOutputStream(10 * 1024);
      
      this.msg = new DataOutputStream(baos);
   }
   
   public void persistAndClose() throws PublicException
   {
      try
      {
         msg.writeByte(SECTION_MARKER_EOF);
         
         msg.close();
         baos.close();
         
         this.blob = baos.toByteArray();
      }
      catch (IOException ioe)
      {
         throw new PublicException(ioe);
      }
   }
   
   public void startInstancesSection(String tableName, int nInstances)
         throws InternalException
   {
      try
      {
         msg.writeByte(SECTION_MARKER_INSTANCES);
         msg.writeUTF(tableName);
         msg.writeInt(nInstances);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }

   public void writeBoolean(boolean value) throws InternalException
   {
      try
      {
         msg.writeBoolean(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }

   public void writeChar(char value) throws InternalException
   {
      try
      {
         msg.writeChar(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }
   
   public void writeByte(byte value) throws InternalException
   {
      try
      {
         msg.writeByte(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }

   public void writeShort(short value) throws InternalException
   {
      try
      {
         msg.writeShort(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }
   
   public void writeInt(int value) throws InternalException
   {
      try
      {
         msg.writeInt(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }

   public void writeLong(long value) throws InternalException
   {
      try
      {
         msg.writeLong(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }

   public void writeFloat(float value) throws InternalException
   {
      try
      {
         msg.writeFloat(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }
   
   public void writeDouble(double value) throws InternalException
   {
      try
      {
         msg.writeDouble(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }
   
   public void writeString(String value) throws InternalException
   {
      try
      {
         msg.writeUTF(value);
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed writing value to blob.", ioe);
      }
   }
   
}
