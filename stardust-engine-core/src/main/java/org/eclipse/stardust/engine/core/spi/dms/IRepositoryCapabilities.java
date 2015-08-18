/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * Represents the capabilities of a repository. By marking a capability as not supported
 * the client code is able to decide which methods from {@link IRepositoryService} are
 * available for use and which ones are not implemented.
 * 
 * @author Roland.Stamm
 * 
 */
public interface IRepositoryCapabilities extends Serializable
{
   /**
    * Indicates if queries can use filters on the content of a document.
    * 
    * @return <code>true</code> if the capability is supported.
    * 
    * @see IRepositoryService#findDocuments(DocumentQuery)
    * @see DocumentQuery#CONTENT
    */
   public boolean isFullTextSearchSupported();

   /**
    * Indicates if queries can use filters on meta data properties that are contained in the {@link Document#getProperties()} map.
    * 
    * @return <code>true</code> if the capability is supported.
    * 
    * @see IRepositoryService#findDocuments(DocumentQuery)
    * @see DocumentQuery#META_DATA
    * @see Document#getProperties()
    */
   public boolean isMetaDataSearchSupported();
   
   /**
    * Indicates if the repository supports write operations for the {@link Document#getProperties()} map.
    * 
    * @return <code>true</code> if the capability is supported.
    */
   public boolean isMetaDataWriteSupported();

   /**
    * Indicates if the repository supports versioning operations.
    * 
    * @return <code>true</code> if the capability is supported.
    * 
    * @see IRepositoryService#getDocumentVersions(String)
    * @see IRepositoryService#versionDocument(String, String, String)
    * @see IRepositoryService#updateDocument(Document, boolean, String, String, boolean)
    * @see IRepositoryService#updateDocument(Document, byte[], String, boolean, String, String, boolean)
    */
   public boolean isVersioningSupported();

   /**
    * Indicates if the repository integrates into the container managed transaction.
    * 
    * @return <code>true</code> if the capability is supported.
    */
   public boolean isTransactionSupported();

   /**
    * Indicates if access control policies are supported.
    * 
    * @return <code>true</code> if the capability is supported.
    * 
    * @see IRepositoryService#getApplicablePolicies(String)
    * @see IRepositoryService#getEffectivePolicies(String)
    * @see IRepositoryService#getPolicies(String)
    * @see IRepositoryService#setPolicy(String, org.eclipse.stardust.engine.api.runtime.AccessControlPolicy)
    * @see IRepositoryService#getPrivileges(String)
    */
   public boolean isAccessControlPolicySupported();
   
   /**
    * Indicates if the repository supports write operations.
    * 
    * @return <code>true</code> if the capability is supported.
    */
   public boolean isWriteSupported();
}
