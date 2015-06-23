package org.eclipse.stardust.engine.core.persistence.jms;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Date;
import java.util.Map;

public abstract class AbstractBlobReader implements BlobReader
{
   protected final Map<Class< ? >, ReadOp> readOps;

   public AbstractBlobReader()
   {
      this.readOps = initReadOpMap();
   }

   protected static interface ReadOp
   {
      Object read(final BlobReader reader);
   }

   public Object readFieldValue(final Class< ? > fieldType)
   {
      final ReadOp readOp = readOps.get(fieldType);
      if (readOp == null)
      {
         throw new IllegalArgumentException("Unsupported field type '" + fieldType + "'.");
      }

      return readOp.read(this);
   }

   protected Map<Class< ? >, ReadOp> initReadOpMap()
   {
      final Map<Class< ? >, ReadOp> result = newHashMap();

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

   protected static class BooleanReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readBoolean();
      }
   }

   protected static class ByteReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readByte();
      }
   }

   protected static class CharacterReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readChar();
      }
   }

   protected static class ShortReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readShort();
      }
   }

   protected static class IntegerReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readInt();
      }
   }

   protected static class LongReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readLong();
      }
   }

   protected static class FloatReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readFloat();
      }
   }

   protected static class DoubleReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readDouble();
      }
   }

   protected static class StringReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return reader.readString();
      }
   }

   protected static class DateReadOp implements ReadOp
   {
      @Override
      public Object read(final BlobReader reader)
      {
         return new Date(reader.readLong());
      }
   }

}