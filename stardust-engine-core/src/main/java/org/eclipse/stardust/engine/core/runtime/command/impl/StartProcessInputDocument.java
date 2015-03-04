/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.command.impl;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.api.runtime.DocumentInfo;

public class StartProcessInputDocument implements Serializable
{

   private static final long serialVersionUID = 1L;

   private QName metaDataType;

   private byte[] content;

   private String label;

   private String comment;

   private boolean version;

   private String targetFolder;

   private String globalVariableId;

   private DocumentInfo documentInfo;

   public QName getMetaDataType()
   {
      return metaDataType;
   }

   public byte[] getContent()
   {
      return content;
   }

   public void setMetaDataType(QName metaDataType)
   {
      this.metaDataType = metaDataType;
   }

   public void setContent(byte[] content)
   {
      this.content = content;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public void setVersion(boolean version)
   {
      this.version = version;
   }

   public void setGlobalVariableId(String globalVariableId)
   {
      this.globalVariableId = globalVariableId;
   }

   public void setDocumentInfo(DocumentInfo documentInfo)
   {
      this.documentInfo = documentInfo;
   }

   public String getLabel()
   {
      return label;
   }

   public String getComment()
   {
      return comment;
   }

   public boolean isVersion()
   {
      return version;
   }

   public String getTargetFolder()
   {
      return targetFolder;
   }

   public void setTargetFolder(String targetFolder)
   {
      this.targetFolder = targetFolder;
   }

   public String getGlobalVariableId()
   {
      return globalVariableId;
   }

   public DocumentInfo getDocumentInfo()
   {
      return documentInfo;
   }
}