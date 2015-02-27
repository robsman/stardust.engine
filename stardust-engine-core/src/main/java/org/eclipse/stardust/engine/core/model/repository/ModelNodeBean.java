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
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLWriter;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelNodeBean implements ModelNode
{
   // housekeeping (no export/import from xml)

   private IModel model;
   private int versionCount;
   private List versions = CollectionUtils.newList();
   private List privateVersions = CollectionUtils.newList();
   private ModelNodeBean parent;
   private ModelRepository repository;
   private String privateVersionOwner;

   // exportable stuff follows:

   private String version = "1";
   private boolean isReleased;
   private Date releaseTime;
   private String id;
   private String name;
   private Date validFrom;
   private Date validTo;
   private String description;

   // written 'back' from or generated during deployment (non editable)

   private int modelOID;
   private Date deploymentTime;
   private String deploymentComment;
   private int revision;

   ModelNodeBean(ModelRepository repository, String id, String name, String version)
   {
      this.repository = repository;
      this.id = id;
      this.name = name;
      this.version = version;
   }

   public ModelNode createPublicVersion()
   {
      return createPublicVersion((IModel) model.deepCopy(), name, validFrom, validTo);
   }

   public ModelNode createPublicVersion(IModel model, String name, Date validFrom,
         Date validTo)
   {
      release(TimestampProviderUtils.getTimeStamp());

      StringBuffer newVersionName = new StringBuffer(version);
      for (int i = 0; i < versionCount; i++)
      {
         newVersionName.append(".0");
      }
      String nv = newVersionName.toString();
      int ind = nv.lastIndexOf(".");
      int last = Integer.parseInt(nv.substring(ind + 1));
      final String version = nv.substring(0, ind +1) + (++last);

      ModelNodeBean result = new ModelNodeBean(repository, id, name, version);
      result.setModel(model);

      versionCount++;

      for (Iterator i = getAllPrivateVersions(); i.hasNext();)
      {
         ModelNodeBean privateVersion = (ModelNodeBean) i.next();
         result.addToPrivateVersions(privateVersion);
      }

      privateVersions.clear();

      result.validFrom = validFrom;
      result.validTo = validTo;

      addToPublicVersions(result);

      // @todo (france, ub): move this out (as in modelnode.create**version)
      repository.save();
      repository.saveModel(this);
      repository.saveModel(result);

      return result;
   }

   public ModelNode attachPublicVersion(String id, String name, String version,
         int versionCount)
   {
      ModelNodeBean result = new ModelNodeBean(repository, id, name, version);
      result.versionCount = versionCount;
      addToPublicVersions(result);
      return result;
   }

   public ModelNode attachPrivateVersion(String owner, String version)
   {
      ModelNodeBean result = new ModelNodeBean(repository, id, name, version);
      result.setPrivateVersionOwner(owner);
      addToPrivateVersions(result);
      return result;
   }

   public ModelNode createPrivateVersion(String owner, String version)
   {
      ModelNodeBean result = new ModelNodeBean(repository, id, name, version);
      result.setModel((IModel) model.deepCopy());

      addToPrivateVersions(result);
      result.setPrivateVersionOwner(owner);

      repository.save();
      repository.saveModel(this);
      repository.saveModel(result);

      return result;
   }

   public boolean isReleased()
   {
      return isReleased;
   }

   public int getVersionCount()
   {
      return versionCount;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void release(Date releaseTime)
   {
      this.isReleased = true;
      this.releaseTime = releaseTime;
   }

   public Date getReleaseTime()
   {
      return releaseTime;
   }

   public boolean hasPublicVersions()
   {
      return versions.size() > 0;
   }

   public void setDeploymentComment(String deploymentComment)
   {
      this.deploymentComment = deploymentComment;
   }

   public String getDeploymentComment()
   {
      return deploymentComment;
   }

   public void exportAsXML(OutputStream outStream, boolean includeDiagrams)
   {
      new DefaultXMLWriter(includeDiagrams).exportAsXML(getModel(true), outStream);
   }

   public void setRevision(int revision)
   {
      this.revision = revision;
   }

   private void addToPrivateVersions(ModelNodeBean model)
   {
      privateVersions.add(model);
      model.parent = this;
   }

   public void setId(String id)
   {
      this.id = id;

   }

   public Iterator getAllPrivateVersions()
   {
      return CollectionUtils.newList(privateVersions).iterator();
   }

   /**
    * Returns the open model version, this version is checkout from.
    * If this model version is not a private version, <code> null</code> is returned.
    */
   public ModelNode getParent()
   {
      return parent;
   }

   public String getId()
   {
      return id;
   }

   public boolean isRoot()
   {
      return parent == null;
   }

   public void setModel(IModel model)
   {
      this.model = model;
   }

   public int getModelOID()
   {
      return modelOID;
   }


   /**
    * Returns the owner of this private workspace model version.
    */
   public String getPrivateVersionOwner()
   {
      return privateVersionOwner;
   }

   /**
    * Returns all model versions being sucessors of this version.
    */
   public Iterator getAllPublicVersions()
   {
      return CollectionUtils.newList(versions).iterator();
   }

   /**
    * Add the model versions to the sucessorlist of this version.
    */
   private void addToPublicVersions(ModelNodeBean node)
   {
      versions.add(node);
      node.parent = this;
   }

   /**
    * Checks, wether this model version is a private workspace.
    */
   public boolean isPrivateVersion()
   {
      return getPrivateVersionOwner() != null;
   }

   void removeFromPublicVersions(ModelNodeBean model)
   {
      versions.remove(model);
      model.parent = null;
   }

   void removeFromPrivateVersions(ModelNodeBean model)
   {
      privateVersions.remove(model);
      model.parent = null;
   }

   protected void setPrivateVersionOwner(String owner)
   {
      privateVersionOwner = owner;
   }

   public String getVersion()
   {
      return version;
   }

   public String getName()
   {
      return name;
   }

   public IModel getModel()
   {
      return getModel(false);
   }

   public IModel getModel(boolean injectNodeAttributes)
   {
      if (model == null)
      {
         repository.loadModel(this);
      }

      if (injectNodeAttributes)
      {
         if ( !CompareHelper.areEqual(model.getId(), getId()))
         {
            model.setId(getId());
         }
         if ( !CompareHelper.areEqual(model.getName(), getName()))
         {
            model.setName(getName());
         }
         if ( !CompareHelper.areEqual(model.getDescription(), getDescription()))
         {
            model.setDescription(getDescription());
         }
         if ( !CompareHelper.areEqual(new Integer(model.getModelOID()), new Integer(
               modelOID)))
         {
            model.setModelOID(modelOID);
         }
         // TODO push equality check into setAttribute method
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.VALID_FROM_ATT), getValidFrom()))
         {
            model.setAttribute(PredefinedConstants.VALID_FROM_ATT, getValidFrom());
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.VALID_TO_ATT), getValidTo()))
         {
            model.setAttribute(PredefinedConstants.VALID_TO_ATT, getValidTo());
         }
         if ( !CompareHelper.areEqual(model.getAttribute(PredefinedConstants.VERSION_ATT),
               getVersion()))
         {
            model.setAttribute(PredefinedConstants.VERSION_ATT, getVersion());
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.IS_RELEASED_ATT), isReleased
                     ? Boolean.TRUE
                     : Boolean.FALSE))
         {
            model.setAttribute(PredefinedConstants.IS_RELEASED_ATT, isReleased
                  ? Boolean.TRUE
                  : Boolean.FALSE);
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.RELEASE_STAMP), releaseTime))
         {
            model.setAttribute(PredefinedConstants.RELEASE_STAMP, releaseTime);
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT), deploymentTime))
         {
            model.setAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT, deploymentTime);
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT),
               deploymentComment))
         {
            model.setAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT,
                  deploymentComment);
         }
         if ( !CompareHelper.areEqual(
               model.getAttribute(PredefinedConstants.REVISION_ATT), new Integer(revision)))
         {
            model.setAttribute(PredefinedConstants.REVISION_ATT, new Integer(revision));
         }
      }
      
      return model;
   }

   public void setVersionCount(int versionCount)
   {
      this.versionCount = versionCount;
   }


   public String getFullVersion()
   {
      return isPrivateVersion() ? parent.version + "_" +version : version;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }

   public ModelRepository getRepository()
   {
      return repository;
   }

   public Date getDeploymentTime()
   {
      return deploymentTime;
   }

   public void setDeploymentTime(Date time)
   {
      deploymentTime = time;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String text)
   {
      description = text;
   }

   public void setValidFrom(Date date)
   {
      validFrom = date;
   }

   public void setValidTo(Date date)
   {
      validTo = date;
   }
}
