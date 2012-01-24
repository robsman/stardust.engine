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
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.FieldDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.LinkDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * @author sauer
 * @version $Revision$
 */
public class ProcessBlobWriter
{

   public static void writeInstances(BlobBuilder blobBuilder, TypeDescriptor td,
         List /*<Persistent>*/ instances) throws PublicException
   {
      try
      {
         if ( !instances.isEmpty())
         {
            blobBuilder.startInstancesSection(td.getTableName(), instances.size());
            
            final List fields = td.getPersistentFields();
            final List links = td.getLinks();
            for (int i = 0; i < instances.size(); ++i)
            {
               final Persistent instance = (Persistent) instances.get(i);
               
               for (int j = 0; j < fields.size(); ++j)
               {
                  FieldDescriptor field = (FieldDescriptor) fields.get(j);

                  Field javaField = field.getField();
                  Class fieldType = javaField.getType();

                  writeField(blobBuilder, instance, javaField, fieldType);
               }
               
               for (int j = 0; j < links.size(); ++j)
               {
                  LinkDescriptor link = (LinkDescriptor) links.get(j);
                  
                  Field javaField = link.getField();
                  
                  Field fkJavaField = link.getFkField();
                  Class fkFieldType = fkJavaField.getType();
                  
                  Persistent linkedInstance = (Persistent) javaField.get(instance);
                  writeField(blobBuilder, linkedInstance, fkJavaField, fkFieldType);
               }
            }
         }
      }
      catch (InternalException ie)
      {
         throw new PublicException("Failed writing process BLOB at table "
               + td.getTableName(), ie);
      }
      catch (IllegalArgumentException iae)
      {
         throw new PublicException("Failed writing process BLOB at table "
               + td.getTableName(), iae);
      }
      catch (IllegalAccessException iae)
      {
         throw new PublicException("Failed writing process BLOB at table "
               + td.getTableName(), iae);
      }
   }

   private static void writeField(BlobBuilder blobBuilder, Persistent instance,
         Field javaField, Class fieldType) throws InternalException,
         IllegalArgumentException, IllegalAccessException
   {
      // ordering by likelyhood of occurance
      
      if (Long.TYPE == fieldType)
      {
         blobBuilder.writeLong((null != instance) ? javaField.getLong(instance) : 0L);
      }
      else if (Long.class == fieldType)
      {
         blobBuilder.writeLong((null != instance)
               ? ((Long) javaField.get(instance)).longValue()
               : 0L);
      }
      else if (Integer.TYPE == fieldType)
      {
         blobBuilder.writeInt((null != instance) ? javaField.getInt(instance) : 0);
      }
      else if (String.class == fieldType)
      {
         String string = (null != instance) ? (String) javaField.get(instance) : null;
         blobBuilder.writeString((null != string) ? string : "");
      }
      else if (Boolean.TYPE == fieldType)
      {
         blobBuilder.writeBoolean((null != instance) ? javaField.getBoolean(instance) : false);
      }
      else if (Byte.TYPE == fieldType)
      {
         blobBuilder.writeByte((null != instance) ? javaField.getByte(instance) : 0);
      }
      else if (Character.TYPE == fieldType)
      {
         blobBuilder.writeChar((null != instance) ? javaField.getChar(instance) : 0);
      }
      else if (Short.TYPE == fieldType)
      {
         blobBuilder.writeShort((null != instance) ? javaField.getShort(instance) : 0);
      }
      else if (Float.TYPE == fieldType)
      {
         blobBuilder.writeFloat((null != instance) ? javaField.getFloat(instance) : 0.0F);
      }
      else if (Double.TYPE == fieldType)
      {
         blobBuilder.writeDouble((null != instance) ? javaField.getDouble(instance) : 0.0D);
      }
      else if (Date.class == fieldType)
      {
         Date date = (Date) javaField.get(instance);
         blobBuilder.writeLong((null != date) ? date.getTime() : 0L);
      }
   }
}
