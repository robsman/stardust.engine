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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;


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
   private final Map<Class<?>, ReadOp> readOps;

   public AbstractByteArrayBlobReader()
   {
      this.readOps = initReadOpMap();
   }
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
   
   public int getCurrentIndex() 
   {
      if (null != dis)
      {
         try 
         {
            return blob.length - dis.available();
         }
         catch (IOException ioe)
         {
            throw new PublicException(
                  BpmRuntimeError.JMS_FAILED_CLOSING_BLOB_AFTER_READING.raise(), ioe);
         }
      }
      return -1;
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
            throw new PublicException(
                  BpmRuntimeError.JMS_FAILED_CLOSING_BLOB_AFTER_READING.raise(), ioe);
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
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_CLOSING_BLOB_AFTER_READING.raise(), ioe);

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

   /**
    * Due to the maximum byte size limitations of {@link DataOutputStream#writeUTF(String)} and {@link DataInputStream#readUTF()}
    * for a {@link String}, the {@link String} to read might have been split up and needs to be reconstructed from separate portions.
    */
   public String readString() throws InternalException
   {
      try
      {
         /* Determine how many portions need to be read to be able to reconstruct the original string ... */
         int nPortions = dis.readInt();

         /* ... read all portions and reconstruct the string by concatenation */
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < nPortions; i++)
         {
            String s = dis.readUTF();
            sb.append(s);
         }
         return sb.toString();
      }
      catch (IOException ioe)
      {
         throw new InternalException("Failed reading value from blob.", ioe);
      }
   }
   
   public Object readFieldValue(final Class<?> fieldType)
   {
      final ReadOp readOp = readOps.get(fieldType);
      if (readOp == null)
      {
         throw new IllegalArgumentException("Unsupported field type '" + fieldType + "'.");
      }

      return readOp.read(this);
   }

   private Map<Class<?>, ReadOp> initReadOpMap()
   {
      final Map<Class<?>, ReadOp> result = newHashMap();

      result.put(Boolean.TYPE, new BooleanReadOp());
      result.put(Boolean.class, new BooleanReadOp());

      result.put(Byte.TYPE, new ByteReadOp());
      result.put(Byte.class, new ByteReadOp());

      result.put(Character.TYPE, new CharacterReadOp());
      result.put(Character.class, new CharacterReadOp());

      result.put(Short.TYPE, new ShortReadOp());
      result.put(Short.class, new ShortReadOp());

      result.put(Integer.TYPE, new IntegerReadOp());
      result.put(Integer.class, new IntegerReadOp());

      result.put(Long.TYPE, new LongReadOp());
      result.put(Long.class, new LongReadOp());

      result.put(Float.TYPE, new FloatReadOp());
      result.put(Float.class, new FloatReadOp());

      result.put(Double.TYPE, new DoubleReadOp());
      result.put(Double.class, new DoubleReadOp());

      result.put(String.class, new StringReadOp());

      result.put(Date.class, new DateReadOp());

      return result;
   }

   private static interface ReadOp
   {
      Object read(final BlobReader reader);
   }

   private static class BooleanReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readBoolean();
      }
   }

   private static class ByteReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readByte();
      }
   }

   private static class CharacterReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readChar();
      }
   }

   private static class ShortReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readShort();
      }
   }

   private static class IntegerReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readInt();
      }
   }

   private static class LongReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readLong();
      }
   }

   private static class FloatReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readFloat();
      }
   }

   private static class DoubleReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readDouble();
      }
   }

   private static class StringReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readString();
      }
   }

   private static class DateReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return new Date(reader.readLong());
      }
   }
}