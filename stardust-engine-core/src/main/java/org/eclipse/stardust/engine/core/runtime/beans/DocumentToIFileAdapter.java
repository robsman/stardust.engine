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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.AnnotationUtils;


import org.eclipse.stardust.vfs.IFile;

/**
 * IFile-view of a Document
 */
public class DocumentToIFileAdapter extends ResourceToIResourceAdapter implements IFile
{

   private final Document document;

   public DocumentToIFileAdapter(Document document, String prefixPath)
   {
      super(document, prefixPath);

      this.document = document;
   }

   public String getContentType()
   {
      return document.getContentType();
   }

   public String getRevisionId()
   {
      return document.getRevisionId();
   }

   public String getRevisionName()
   {
      return document.getRevisionName();
   }

   public String getRevisionComment()
   {
      return document.getRevisionComment();
   }

   public List getVersionLabels()
   {
      return document.getVersionLabels();
   }

   public long getSize()
   {
      return document.getSize();
   }

   public void setContentType(String contentType)
   {
      throw new UnsupportedOperationException();
   }

   public String getEncoding()
   {
      return document.getEncoding();
   }

   public String getLockOwner()
   {
      // TODO implement when available with jcr-vfs
      return null; // document.getLockOwner();
   }

   public Map<String, Serializable> getAnnotations()
   {
      Map<String, Serializable> annotations = AnnotationUtils.toMap(document.getDocumentAnnotations());
      if (annotations == null)
      {
         annotations = Collections.EMPTY_MAP;
      }
      return annotations;
   }

   public void setAnnotations(Map< ? extends String, ? extends Serializable> arg0)
   {
       throw new UnsupportedOperationException();
   }

   @Override
   public String getPropertiesTypeId()
   {
      DocumentType documentType = document.getDocumentType();

      if (documentType != null)
      {
         return documentType.getDocumentTypeId();
      }
      return null;
   }

   @Override
   public String getPropertiesTypeSchemaLocation()
   {
      DocumentType documentType = document.getDocumentType();

      if (documentType != null)
      {
         return documentType.getSchemaLocation();
      }
      return null;
   }

}
