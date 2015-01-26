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

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author sauer
 * @version $Revision$
 */
public abstract class AbstractJmsBytesMessageReader implements BlobReader
{

   private BytesMessage msg;
   
   protected abstract BytesMessage nextBlobContainer() throws PublicException;

   public void init(Parameters params) throws PublicException
   {
      // nothing to be done
   }

   public void close() throws PublicException
   {
      this.msg = null;
   }

   public boolean nextBlob() throws PublicException
   {
      this.msg = nextBlobContainer();
      
      return (null != msg);
   }
   
   public boolean readBoolean() throws InternalException
   {
      try
      {
         return msg.readBoolean();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public char readChar() throws InternalException
   {
      try
      {
         return msg.readChar();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public byte readByte() throws InternalException
   {
      try
      {
         return msg.readByte();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public short readShort() throws InternalException
   {
      try
      {
         return msg.readShort();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public int readInt() throws InternalException
   {
      try
      {
         return msg.readInt();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public long readLong() throws InternalException
   {
      try
      {
         return msg.readLong();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public float readFloat() throws InternalException
   {
      try
      {
         return msg.readFloat();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public double readDouble() throws InternalException
   {
      try
      {
         return msg.readDouble();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

   public String readString() throws InternalException
   {
      try
      {
         return msg.readUTF();
      }
      catch (JMSException jmse)
      {
         throw new InternalException("Failed reading value from JMS blob.", jmse);
      }
   }

}