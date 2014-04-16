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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.FieldDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.LinkDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptorRegistry;


/**
 * @author sauer
 * @version $Revision$
 */
public class ProcessBlobUtils
{
   private static final Logger trace = LogManager.getLogger(ProcessBlobUtils.class);

   public static void copyBlob(BlobReader sourceBlob, BlobBuilder targetBlob)
   {
      final TypeDescriptorRegistry tdRegistry = TypeDescriptorRegistry.current();

      while (true)
      {
         final byte sectionMarker = sourceBlob.readByte();

         if (BlobBuilder.SECTION_MARKER_EOF == sectionMarker)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Reached end of BLOB. ");
            }

            targetBlob.writeByte(BlobBuilder.SECTION_MARKER_EOF);

            break;
         }
         else if (BlobBuilder.SECTION_MARKER_INSTANCES == sectionMarker)
         {
            final String tableName = sourceBlob.readString();
            final int nInstances = sourceBlob.readInt();

            targetBlob.writeByte(BlobBuilder.SECTION_MARKER_INSTANCES);
            targetBlob.writeString(tableName);
            targetBlob.writeInt(nInstances);

            final TypeDescriptor td = tdRegistry.getDescriptorForTable(tableName);

            final List fields = td.getPersistentFields();
            final List links = td.getLinks();

            try
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Extracting " + nInstances + " of type " + td.getType());
               }

               for (int i = 0; i < nInstances; ++i)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Reading instance " + i);
                  }

                  for (int j = 0; j < fields.size(); ++j)
                  {
                     FieldDescriptor field = (FieldDescriptor) fields.get(j);

                     Field javaField = field.getField();
                     copyField(sourceBlob, targetBlob, javaField.getType());
                  }

                  for (int j = 0; j < links.size(); ++j)
                  {
                     LinkDescriptor link = (LinkDescriptor) links.get(j);

                     copyField(sourceBlob, targetBlob, link.getFkField().getType());
                  }
               }

               if (trace.isDebugEnabled())
               {
                  trace.debug("Finished reading instances");
               }
            }
            catch (InternalException ie)
            {
               throw new PublicException(
                     BpmRuntimeError.JMS_FAILED_PERSISTING_BLOB_AT_TABLE.raise(td
                           .getTableName()), ie);
            }
         }
         else
         {
            throw new PublicException(
                  BpmRuntimeError.JMS_UNEXPECTED_SECTION_MARKER.raise(sectionMarker));
         }
      }
   }

   private static void copyField(BlobReader sourceBlob, BlobBuilder targetBlob,
         Class fieldType) throws InternalException
   {
      // ordering by likelihood of occurrence

      if ((Long.TYPE == fieldType) || (Long.class == fieldType))
      {
         targetBlob.writeLong(sourceBlob.readLong());
      }
      else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType))
      {
         targetBlob.writeInt(sourceBlob.readInt());
      }
      else if (String.class == fieldType)
      {
         targetBlob.writeString(sourceBlob.readString());
      }
      else if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType))
      {
         targetBlob.writeBoolean(sourceBlob.readBoolean());
      }
      else if ((Byte.TYPE == fieldType) || (Byte.class == fieldType))
      {
         targetBlob.writeByte(sourceBlob.readByte());
      }
      else if ((Character.TYPE == fieldType) || (Character.class == fieldType))
      {
         targetBlob.writeChar(sourceBlob.readChar());
      }
      else if ((Short.TYPE == fieldType) || (Short.class == fieldType))
      {
         targetBlob.writeShort(sourceBlob.readShort());
      }
      else if ((Float.TYPE == fieldType) || (Float.class == fieldType))
      {
         targetBlob.writeFloat(sourceBlob.readFloat());
      }
      else if ((Double.TYPE == fieldType) || (Double.class == fieldType))
      {
         targetBlob.writeDouble(sourceBlob.readDouble());
      }
      else if (Date.class == fieldType)
      {
         targetBlob.writeLong(sourceBlob.readLong());
      }
      else
      {
         throw new InternalException("Unsupported field type: " + fieldType);
      }
   }

}