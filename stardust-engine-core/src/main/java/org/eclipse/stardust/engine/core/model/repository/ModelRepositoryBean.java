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
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.repository.plain.FileSystemStore;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelRepositoryBean implements ModelRepository
{
   private RepositoryStore store;
   private List rootModels = CollectionUtils.newList();

   public ModelRepositoryBean()
   {
      store = new FileSystemStore();
      store.loadRepository(this);
   }

   public ModelNode createRootModel(IModel model, String id, String name,
         String description, Date validFrom, Date validTo)
   {
      if (findRootModel(id) != null)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ROOT_MODEL_WITH_ID_ALREADY_EXISTS.raise(id));
      }
      ModelNode result = new ModelNodeBean(this, id, name, "1");
      result.setDescription(description);
      result.setValidFrom(validFrom);
      result.setValidTo(validTo);
      result.setModel(model);
      rootModels.add(result);
      // @todo (france, ub): move this out (as in modelnode.create**version)
      // the client should know when to store.
      store.saveRepository(this);
      store.saveModel(result);
      return result;
   }

   /**
    * Deletes the node including the subtree.
    */
   public void delete(ModelNode model)
   {
      // @todo (france, ub): remove dead models too
      for (Iterator i = model.getAllPrivateVersions(); i.hasNext();)
      {
         ModelNode modelNode = (ModelNode) i.next();
         delete(modelNode);
      }
      for (Iterator i = model.getAllPublicVersions(); i.hasNext();)
      {
         ModelNode modelNode = (ModelNode) i.next();
         delete(modelNode);
      }

      ModelNodeBean predecessor = (ModelNodeBean) model.getParent();

      if (predecessor != null)
      {
         if (model.isPrivateVersion())
         {
            predecessor.removeFromPrivateVersions((ModelNodeBean) model);
         }
         else
         {
            predecessor.removeFromPublicVersions((ModelNodeBean) model);
         }
      }
      else
      {
         rootModels.remove(model);
      }
      save();
   }

   public void save()
   {
      store.saveRepository(this);
   }

   public ModelNode attachRootModel(String id, String name, int versionCount)
   {
      if (findRootModel(id) != null)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ROOT_MODEL_WITH_ID_ALREADY_EXISTS.raise(id));
      }
      ModelNodeBean result = new ModelNodeBean(this, id, name, "1");
      result.setVersionCount(versionCount);
      rootModels.add(result);
      return result;
   }

   public void loadModel(ModelNode modelNode)
   {
      store.loadModel(this, modelNode);
   }

   public void saveModel(ModelNode modelNode)
   {
      store.saveModel(modelNode);
   }

   public ModelNode getPublicVersion(String id, String version)
   {
      ModelNode root = findRootModel(id);
      if (root == null)
      {
         return null;
      }
      return getPublicVersion(root, version);
   }

   private ModelNode getPublicVersion(ModelNode node, String version)
   {
      if (node.getVersion().equals(version))
      {
         return node;
      }
      for (Iterator i = node.getAllPublicVersions(); i.hasNext();)
      {
         ModelNode next = (ModelNode) i.next();
         ModelNode match = getPublicVersion(next, version);
         if (match != null)
         {
            return match;
         }
      }
      return null;
   }

   public ModelNode findRootModel(String id)
   {
      for (Iterator i = rootModels.iterator(); i.hasNext();)
      {
         ModelNode node = (ModelNode) i.next();
         if (node.getId().equals(id))
         {
            return node;
         }
      }
      return null;
   }

   public Iterator getAllRootModels()
   {
      // @todo (france, ub): sort root models
      return rootModels.iterator();
   }

   public void deleteAllModels()
   {
      rootModels.clear();
      store.cleanup();
   }
}
