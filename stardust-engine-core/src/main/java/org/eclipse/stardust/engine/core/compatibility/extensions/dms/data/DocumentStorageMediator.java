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
package org.eclipse.stardust.engine.core.compatibility.extensions.dms.data;

import java.io.IOException;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.error.InternalException;


/**
 * @author rsauer
 * @version $Revision$
 */
public class DocumentStorageMediator
{
   public static String serializeDocument(DocumentStorageBean doc)
   {
      // TODO
      try
      {
         return new String(Base64.encode(Serialization.serializeObject(doc)));
      }
      catch (IOException e)
      {
         // TODO
         throw new InternalException("Failed serializing document memento.", e);
      }
   }

   public static DocumentStorageBean deserializeDocument(String memento)
   {
      // TODO
      try
      {
         return (DocumentStorageBean) Serialization.deserializeObject(Base64.decode(memento.getBytes()));
      }
      catch (IOException e)
      {
         // TODO
         throw new InternalException("Failed serializing document memento.", e);
      }
      catch (ClassNotFoundException e)
      {
         // TODO
         throw new InternalException("Failed serializing document memento.", e);
      }
   }

   public static String serializeDocumentSet(DocumentSetStorageBean docs)
   {
      // TODO
      try
      {
         return new String(Base64.encode(Serialization.serializeObject(docs)));
      }
      catch (IOException e)
      {
         // TODO
         throw new InternalException("Failed serializing document set memento.", e);
      }
   }

   public static DocumentSetStorageBean deserializeDocumentSet(String memento)
   {
      // TODO
      try
      {
         return (DocumentSetStorageBean) Serialization.deserializeObject(Base64.decode(memento.getBytes()));
      }
      catch (IOException e)
      {
         // TODO
         throw new InternalException("Failed serializing document set memento.", e);
      }
      catch (ClassNotFoundException e)
      {
         // TODO
         throw new InternalException("Failed serializing document set memento.", e);
      }
   }
}
