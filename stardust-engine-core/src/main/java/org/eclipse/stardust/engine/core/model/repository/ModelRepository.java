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

import java.util.Date;
import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IModel;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ModelRepository
{
   ModelNode findRootModel(String id);

   Iterator getAllRootModels();

   ModelNode createRootModel(IModel model, String id, String name, String description,
         Date validFrom, Date validTo);

   void delete(ModelNode model);

   void deleteAllModels();

   void save();

   ModelNode attachRootModel(String id, String name, int versionCount);

   void loadModel(ModelNode modelNode);

   void saveModel(ModelNode modelNode);

   ModelNode getPublicVersion(String id, String version);
}
