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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author sauer
 * @version $Revision$
 */
public abstract class AbstractByteArrayBlobReader implements BlobReader
{
   private byte[] blob;

   private ByteArrayInputStream bais;

   private DataInputStream dis;
   
   protected abstract byte[] nextByteArray();

   public byte[] getBlob()
   {
      return blob;
   }
   
   public void setBlob(byte[] blob)
   {
      this.blob = blob;
      
      if (null != dis)
      {
         close();
      }
      
      this.bais = new ByteArrayInputStream(blob);
      this.dis = new DataInputStream(bais);
   }
   
   public void init(Parameters params) throws PublicException
   {
      // nothing to be done
   }

   public boolean nextBlob() throws PublicException
   {
      if (null != dis)
      {
         try
         {
            if (null != dis)
            {
               dis.close();
               bais.close();
            }
            this.bais = null;
            this.dis = null;
         }
         catch (IOException ioe)
         {
            throw new PublicException("Failed closing BLOB after reading.", ioe);
         }
      }
      
      this.blob = nextByteArray();
      if (null != blob)
      {
         this.bais = new ByteArrayInputStream(blob);
         this.dis = new DataInputStream(bais);
      }

      return (null != dis);
   }

   public void close() throws PublicException
   {
      this.blob = null;

      try
      {
         if (null != dis)
         {
            dis.close();
            bais.close();
         }
      }
      catch (IOException ioe)
      {
         throw new PublicException("Failed closing BLOB after reading.", ioe);
      }
   }

   public boolean readBoolean() throws InternalException
   {
      try
      {
         return dis.readBoolean();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public char readChar() throws InternalException
   {
      try
      {
         return dis.readChar();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public byte readByte() throws InternalException
   {
      try
      {
         return dis.readByte();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public short readShort() throws InternalException
   {
      try
      {
         return dis.readShort();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public int readInt() throws InternalException
   {
      try
      {
         return dis.readInt();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public long readLong() throws InternalException
   {
      try
      {
         return dis.readLong();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public float readFloat() throws InternalException
   {
      try
      {
         return dis.readFloat();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public double readDouble() throws InternalException
   {
      try
      {
         return dis.readDouble();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

   public String readString() throws InternalException
   {
      try
      {
         return dis.readUTF();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }

}