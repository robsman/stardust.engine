/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.model.removethis.ModelProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author fherinean
 * @version $Revision$
 */

public class X3_0_0from2_5_0Converter extends RepositoryUpgradeJob
{
   public static final Logger trace = LogManager.getLogger(X3_0_0from2_5_0Converter.class);

   private static final Version CURRENT_VERSION = Version.createFixedVersion(3, 0, 0);
   private static final String MODEL_ENCODING = "ISO-8859-1";

   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      RepositoryItem repository = (RepositoryItem) item;

      ModelRepository models = new ModelRepository();

      try
      {
         trace.debug("Opening and processing boot file.");

         FileInputStream _istream = new FileInputStream(repository.getBoot());
         ObjectInputStream bootFileStream = new ObjectInputStream(_istream);

         while (bootFileStream.available() > 0)
         {
            load(bootFileStream, models, recover);
         }

         bootFileStream.close();
      }
      catch (IOException x)
      {
         throw new PublicException("Could not read boot file.", x);
      }

      String repositoryPath = Parameters.instance().getString(
            ModelProperties.REPOSITORY_PATH, ".");
      File bootFile = new File(repositoryPath, "repository.boot");

      Document document = new RepositoryWriter().write(models);
      try
      {
         FileWriter writer = new FileWriter(bootFile);
         XmlUtils.serialize(document, new StreamResult(writer), MODEL_ENCODING, 2, null, null);
         repository.getBoot().delete();
      }
      catch (IOException ioex)
      {
         String message = "Error writing boot file: " + bootFile;
         trace.warn(message, ioex);
         throw new UpgradeException(message);
      }

      repository.setBoot(bootFile);
      repository.setVersion(Version.createFixedVersion(3, 0, 0));
      return repository;
   }

   private void load(ObjectInputStream bootFileStream, ModelRepository models, boolean recover)
   {
      ModelNode node = null;
      try
      {
         String fileName = bootFileStream.readUTF().trim();
         long predecessorOID = bootFileStream.readLong();
         String versionName = bootFileStream.readUTF();
         String workspaceOwnerID = bootFileStream.readUTF();
         bootFileStream.readInt();
         int[] versionIndex = (int[]) bootFileStream.readObject();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();
         bootFileStream.readInt();

         trace.debug("Reading model from file '" + fileName + "'.");

         int ix = fileName.lastIndexOf('_');
         String id = fileName.substring(0, ix);
         String oidString = fileName.substring(ix + 1, fileName.length() - 4);
         long oid = Long.parseLong(oidString);

         node = models.createModel(predecessorOID == 0, (int) (oid >>> 32));
         node.setId(id);
         if (predecessorOID != 0)
         {
            trace.debug("... predecessor model version: " + predecessorOID);
            ModelNode predecessor = models.findModelByModelOID((int) (predecessorOID >>> 32));

            // Due to the order of dump and load, the predecessor must have been read already
            Assert.isNotNull(predecessor, "Predecessor model version is not null.");

            predecessor.addVersion(node);
            if (!workspaceOwnerID.equals("null"))
            {
               node.setWorkspaceOwnerID(workspaceOwnerID);
            }
         }

         node.setVersionName(versionName);
         node.setVersionIndex(versionIndex);

         String repositoryPath = Parameters.instance().getString(
               ModelProperties.REPOSITORY_PATH, ".");
         String newFileName = node.getId() + "_" + node.getFullVersionString() + ".mod";

         String file = repositoryPath + "/" + fileName;

         ModelItem model = new ModelItem(readFile(file));
         if (model.getVersion().compareTo(CURRENT_VERSION) < 0)
         {
            ModelUpgrader modelUpgrader = new ModelUpgrader(model);
            model = (ModelItem) modelUpgrader.upgradeToVersion(
                  CURRENT_VERSION, recover);
            writeFile(file, model.getModel());
         }

         File oldFile = new File(repositoryPath, fileName);
         if (oldFile.exists())
         {
            File newFile = new File(repositoryPath, newFileName);
            oldFile.renameTo(newFile);
         }        trace.debug("add Version: " + node.getFullVersionName() /*+ " with OID: " + model.getOID()*/);

         Element el = model.getModelElement();
         node.setName(el.getAttribute(RepositoryWriter.NAME));
         NodeList list = el.getElementsByTagName("attribute");
         for (int i = 0; i < list.getLength(); i++)
         {
            Element att = (Element) list.item(i);
            String attName = att.getAttribute("name");
            if ("carnot:engine:validFrom".equals(attName))
            {
               node.setValidFrom(att.getAttribute("value"));
            }
            if ("carnot:engine:validTo".equals(attName))
            {
               node.setValidTo(att.getAttribute("value"));
            }
         }
      }
      catch (Exception x)
      {
         trace.info("Error during model read.", x);
      }
   }

   public Version getVersion()
   {
      return CURRENT_VERSION;
   }

   private String readFile(String fileName)
   {
      try
      {
         File file = new File(fileName);
         char[] buf = new char[(int) file.length()];
         FileReader reader = new FileReader(fileName);
         reader.read(buf);
         reader.close();
         return new String(buf);
      }
      catch (Exception x)
      {
         trace.warn("", x);
         throw new PublicException(x.getMessage());
      }
   }

   private void writeFile(String filename, String model)
   {
      try
      {
         FileWriter writer = new FileWriter(filename);
         writer.write(model);
         writer.close();
      }
      catch (IOException x)
      {
         trace.warn("", x);
         throw new PublicException(x.getMessage());
      }
   }

   private static class RepositoryWriter
   {
      private static final String REPOSITORY = "repository";
      private static final String PUBLIC = "public";
      private static final String PRIVATE = "private";
      private static final String OWNER_ID = "owner";
      private static final String ID = "id";
      private static final String NAME = "name";
      private static final String VERSION = "version";
      private static final String VERSION_COUNT = "versionCount";
      private static final String OID = "oid";
      private static final String RELEASED = "released";
      private static final String VALID_FROM = "validFrom";
      private static final String VALID_TO = "validTo";

      private Document document;

      private Document write(ModelRepository models)
      {
         document = XmlUtils.newDocument();
         document.appendChild(convertToNode(models));
         return document;
      }

      private Node convertToNode(ModelRepository models)
      {
         Element node = document.createElement(REPOSITORY);
         for (Iterator i = models.getRootModels(); i.hasNext();)
         {
            ModelNode modelNode = (ModelNode) i.next();
            node.appendChild(convertToNode(modelNode));
         }
         return node;
      }


      private Node convertToNode(ModelNode version)
      {
         Element node = document.createElement(version.isPublic() ? PUBLIC : PRIVATE);
         NodeWriter writer = new NodeWriter(node);
         writer.writeAttribute(ID, version.getId());
         writer.writeAttribute(NAME, version.getName());
         writer.writeAttribute(VERSION, version.getVersionString());
         writer.writeAttribute(VERSION_COUNT, 0);

         writer.writeAttribute(OID, version.getOid());
         writer.writeAttribute(VALID_FROM, version.getValidFrom());
         writer.writeAttribute(VALID_TO, version.getValidTo());

         if (version.isReleased())
         {
            writer.writeAttribute(RELEASED, true);
         }
         if (!version.isPublic())
         {
            writer.writeAttribute(OWNER_ID, version.getOwnerId());
         }

         for (Iterator i = version.getVersions(); i.hasNext();)
         {
            ModelNode modelNode = (ModelNode) i.next();
            node.appendChild(convertToNode(modelNode));
         }
         return node;
      }
   }

   private static class ModelRepository
   {
      ArrayList roots = new ArrayList();
      HashMap models = new HashMap();

      private ModelNode createModel(boolean root, int oid)
      {
         ModelNode node = new ModelNode();
         node.setOid(oid);
         models.put(new Integer(oid), node);
         if (root)
         {
            roots.add(node);
         }
         return node;
      }

      private ModelNode findModelByModelOID(int i)
      {
         return (ModelNode) models.get(new Integer(i));
      }

      public Iterator getRootModels()
      {
         return roots.iterator();
      }
   }

   private static class ModelNode
   {
      private String versionName;
      private int[] versionIndex;
      private String ownerId;
      private ArrayList versions = new ArrayList();
      private String name;
      private int oid;
      private String id;
      private boolean released;
      private String validTo;
      private String validFrom;
      private ModelNode parent;

      private void setVersionName(String versionName)
      {
         this.versionName = versionName;
      }

      private void setVersionIndex(int[] versionIndex)
      {
         this.versionIndex = versionIndex;
      }

      private String getFullVersionName()
      {
         if (ownerId != null)
         {
            return name + " " + versionName + " (" + ownerId + ")";
         }

         return name + " " + versionName;
      }

      private void setWorkspaceOwnerID(String ownerID)
      {
         this.ownerId = ownerID;
      }

      public void addVersion(ModelNode node)
      {
         versions.add(node);
         node.parent = this;
      }

      public String getVersionString()
      {
         if (ownerId == null)
         {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < versionIndex.length; i++)
            {
               if (i > 0)
               {
                  sb.append('.');
               }
               sb.append(Integer.toString(versionIndex[i]));
            }
            return sb.toString();
         }
         else
         {
            return versionName;
         }
      }

      public String getFullVersionString()
      {
         if (ownerId == null)
         {
            return getVersionString();
         }
         else
         {
            return parent.getVersionString() + "_" + versionName;
         }
      }

      public Iterator getVersions()
      {
         return versions.iterator();
      }

      public boolean isPublic()
      {
         return ownerId == null;
      }

      public String getOwnerId()
      {
         return ownerId;
      }

      public void setOid(int oid)
      {
         this.oid = oid;
      }

      public void setId(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
      }

      public String getVersionName()
      {
         return versionName;
      }

      public int getOid()
      {
         return oid;
      }

      public String getName()
      {
         return name == null ? id : name;
      }

      public boolean isReleased()
      {
         for (int i = 0; i < versions.size(); i++)
         {
            ModelNode node = (ModelNode) versions.get(i);
            if (node.isPublic())
            {
               return true;
            }
         }
         return released;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public void setValidFrom(String validFrom)
      {
         this.validFrom = validFrom;
      }

      public void setValidTo(String validTo)
      {
         this.validTo = validTo;
      }

      public void setReleased(String released)
      {
         this.released = "true".equalsIgnoreCase(released);
      }

      public String getValidFrom()
      {
         return validFrom;
      }

      public String getValidTo()
      {
         return validTo;
      }
   }
}
