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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;



/**
 * @author rsauer
 * @version $Revision$
 */
public class DocumentStorageBean
      implements Serializable, Document, org.eclipse.stardust.engine.core.compatibility.extensions.dms.Document
{
   // backwards compatibility with release <= 4.5.3
   private static final long serialVersionUID = 5803453052485440655L;

   public static final String TABLE_NAME = "dms_document";

   private long oid;

   private String id;

   private String displayName;

   private String documentType;

   private Map properties;

   private Map changeDescriptor;

   private DocumentAnnotations documentAnnotations;

   ////
   //// ResourceInfo
   ////

   public String getName()
   {
      return displayName;
   }

   public void setName(String name)
   {
      this.displayName = name;
   }

   public String getDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setDescription(String description)
   {
      // TODO Auto-generated method stub

   }

   public String getOwner()
   {
      return null;
   }

   public void setOwner(String owner)
   {
      // TODO Auto-generated method stub

   }

   public Date getDateCreated()
   {
      return null;
   }

   public Date getDateLastModified()
   {
      return null;
   }

   public Map getProperties()
   {
      return (null != properties)
            ? Collections.unmodifiableMap(properties)
            : Collections.EMPTY_MAP;
   }

   public void setProperties(Map properties)
   {
      // TODO Auto-generated method stub

   }

   public Serializable getProperty(String name)
   {
      return ((null != properties) ? (Serializable) properties.get(name) : null);
   }

   public void setProperty(String name, Serializable value)
   {
      // TODO write into change descriptor

      if (null != value)
      {
         if (null == properties)
         {
            this.properties = new HashMap();
         }
         properties.put(name, value);
      }
      else if (null != properties)
      {
         properties.remove(name);
         if (properties.isEmpty())
         {
            this.properties = null;
         }
      }
   }

   ////
   //// DocumentInfo
   ////

   public String getContentType()
   {
      return documentType;
   }

   public void setContentType(String type)
   {
      this.documentType = type;
   }

   ////
   //// Resource
   ////

   public String getRepositoryId()
   {
      return null;
   }

   public String getId()
   {
      return id;
   }

   public String getPath()
   {
      return null;
   }

   ////
   //// Document
   ////

   public long getSize()
   {
      return 0;
   }

   public String getRevisionId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getRevisionName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List getVersionLabels()
   {
      return null;
   }

   public String getEncoding()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public DocumentAnnotations getDocumentAnnotations()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setDocumentAnnotations(DocumentAnnotations documentAnnotations)
   {
      // TODO Auto-generated method stub

   }

   public DocumentType getDocumentType()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setDocumentType(DocumentType documentType)
   {
      // TODO Auto-generated method stub

   }

   ////
   //// legacy org.eclipse.stardust.engine.core.compatibility.extensions.dms.Document
   ////

   public long getOid()
   {
      return oid;
   }

   public void setOid(long oid)
   {
      this.oid = oid;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public void mergeDocuments(DocumentStorageBean doc)
   {
      mergeDocuments(doc, true);
   }

   public void mergeDocuments(DocumentStorageBean doc, boolean mergeOid)
   {
      if (mergeOid)
      {
         this.oid = doc.oid;
      }

      this.id = doc.id;
      this.displayName = doc.displayName;
      this.documentType = doc.documentType;
      // TODO consider change descriptor
      this.properties = doc.properties;
   }

   public String getDisplayName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setDisplayName(String name)
   {
      // TODO Auto-generated method stub

   }

   public void setDocumentType(String type)
   {
      // TODO Auto-generated method stub

   }



}
