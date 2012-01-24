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
package org.eclipse.stardust.engine.core.model.repository;

import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IModel;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ModelNode
{
   Iterator getAllPrivateVersions();

   String getPrivateVersionOwner();

   Iterator getAllPublicVersions();

   boolean isPrivateVersion();

   String getVersion();

   String getName();

   IModel getModel();

   IModel getModel(boolean injectNodeAttributes);

   ModelNode getParent();

   String getId();

   boolean isRoot();

   void setModel(IModel model);

   int getModelOID();

   /**
    * Creates a new version as a copy of this model version.
    */
   ModelNode createPublicVersion();

   ModelNode createPublicVersion(IModel model, String name, Date validFrom, Date validTo);

   /**
    * Creates a private workspace for the user <code>user</user>
    * as a copy of this model version.
    */
   ModelNode createPrivateVersion(String owner, String version);

   void setId(String id);

   boolean isReleased();

   void setName(String name);

   ModelNode attachPublicVersion(String id, String name, String version,
         int versionCount);

   int getVersionCount();

   ModelNode attachPrivateVersion(String owner, String version);

   String getFullVersion();

   Date getValidFrom();

   Date getValidTo();

   void setValidFrom(Date date);

   void setValidTo(Date date);

   void setDescription(String text);

   String getDescription();

   void setModelOID(int modelOID);

   ModelRepository getRepository();

   Date getDeploymentTime();

   void setDeploymentTime(Date time);

   void release(Date releaseTime);

   Date getReleaseTime();

   boolean hasPublicVersions();

   void setDeploymentComment(String comment);

   String getDeploymentComment();

   void exportAsXML(OutputStream outStream, boolean includeDiagrams);

   void setRevision(int revision);
}
