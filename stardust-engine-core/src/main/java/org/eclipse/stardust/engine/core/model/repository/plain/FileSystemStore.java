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
package org.eclipse.stardust.engine.core.model.repository.plain;

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLWriter;
import org.eclipse.stardust.engine.core.model.beans.NullConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.removethis.ModelProperties;
import org.eclipse.stardust.engine.core.model.repository.ModelNode;
import org.eclipse.stardust.engine.core.model.repository.ModelRepository;
import org.eclipse.stardust.engine.core.model.repository.RepositoryStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class FileSystemStore implements RepositoryStore
{
   public static final Logger trace = LogManager.getLogger(FileSystemStore.class);

   public static final String VALID_FROM_ATT = "validFrom";
   public static final String VALID_TO_ATT = "validTo";
   public static final String DEPLOYMENT_TIME_ATT = "deploymentTime";
   public static final String DEPLOYMENT_COMMENT_ATT = "comment";
   public static final String OID_ATT = "oid";
   public static final String PUBLIC = "public";
   public static final String NAME_ATT = "name";
   public static final String DESCRIPTION_ATT = "description";
   public static final String ID_ATT = "id";
   public static final String VERSION_COUNT_ATT = "versionCount";
   public static final String REPOSITORY = "repository";
   public static final String VERSION_ATT = "version";
   public static final String PRIVATE = "private";
   public static final String OWNER_ATT = "owner";
   private static final String RELEASED_ATT = "released";
   private static final String RELEASE_TIME_ATT = "releaseTime";

   private static final String BOOT_FILE = "repository.boot";

   private File repositoryDirectory;

   public FileSystemStore()
   {

      String repository = Parameters.instance().getString(
            ModelProperties.REPOSITORY_PATH, ".");

      trace.debug("Repository directory set to: " + repository);

      repositoryDirectory = new File(repository);

      if (!repositoryDirectory.isDirectory())
      {
         throw new PublicException("The model repository '"
               + repositoryDirectory.getName() + "' is no directory.");
      }
   }

   public void loadModel(ModelRepository repository, ModelNode node)
   {
      trace.info(
            "Loading model '" + node.getId() + "' for revision " + node.getFullVersion());
      String fileName = getModelFileName(node);

      IModel model = new DefaultXMLReader(true,
            new NullConfigurationVariablesProvider()).importFromXML(new File(
            repositoryDirectory, fileName));

      node.setModel(model);
   }

   public void deleteModel(ModelNode node)
   {
      if (node.getModel() == null)
      {
         return;
      }
      String fileName = getModelFileName(node);
      File file = new File(repositoryDirectory, fileName);
      if (file.exists())
      {
         file.delete();
      }
   }

   public String getXMLString(ModelNode node)
   {
      String file = getModelFileName(node);
      BufferedReader inStream = null;
      try
      {
         inStream =
               new BufferedReader(new FileReader(new File(repositoryDirectory, file)));
         StringBuffer xmlString = new StringBuffer();
         String line;
         while ((line = inStream.readLine()) != null)
         {
            xmlString.append(line);
         }
         inStream.close();
         return xmlString.toString();
      }
      catch (IOException e)
      {
         throw new InternalException(e);
      }
   }

   public void cleanup()
   {
      File bootFile = new File(repositoryDirectory, BOOT_FILE);

      bootFile.delete();

      FilenameFilter filter = new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            if (name.endsWith(".mod"))
            {
               return true;
            }

            return false;
         }
      };

      File[] files = repositoryDirectory.listFiles(filter);

      for (int n = 0; n < files.length; ++n)
      {
         files[n].delete();
      }
   }

   public void saveModel(ModelNode node)
   {
      if (node == null || node.getModel() == null)
      {
         return;
      }

      String fileName = getModelFileName(node);
      File modelFile = new File(repositoryDirectory, fileName);
      File modelLock = null;

      try
      {
         modelLock = lock(modelFile);
         trace.debug("Writing model " + node.getName() + " to file "
               + modelFile.getName());
         new DefaultXMLWriter(true).exportAsXML(node.getModel(true), modelFile);
      }
      finally
      {
         releaseLock(modelLock);
      }
   }

   public void saveRepository(ModelRepository repository)
   {
      File bootLock = null;
      try
      {
         File bootFile = new File(repositoryDirectory, BOOT_FILE);

         bootLock = lock(bootFile);

         Document document = XmlUtils.newDocument();

         Element repositoryElement = document.createElement(REPOSITORY);
         document.appendChild(repositoryElement);
         for (Iterator i = repository.getAllRootModels(); i.hasNext();)
         {
            ModelNode model = (ModelNode) i.next();

            createPublicModelNodeElementHierarchy(model, repositoryElement);
         }

         FileOutputStream stream = new FileOutputStream(bootFile);

         XmlUtils.serialize(document, new StreamResult(stream),
               XMLConstants.ENCODING_ISO_8859_1, 3, null, null);
         stream.close();
      }
      catch (IOException x)
      {
         throw new PublicException("I/O Error during save.", x);
      }
      finally
      {
         releaseLock(bootLock);
         trace.debug("Bootfile lock released.");
      }
   }


   private void createPublicModelNodeElementHierarchy(ModelNode model, Element parent)
   {
      Element version = createPublicVersionElement(model, parent);

      for (Iterator i = model.getAllPrivateVersions(); i.hasNext();)
      {
         createPrivateVersionElement((ModelNode) i.next(), version);
      }
      for (Iterator i = model.getAllPublicVersions(); i.hasNext();)
      {
         createPublicModelNodeElementHierarchy((ModelNode) i.next(), version);
      }
   }

   private Element createPrivateVersionElement(ModelNode node, Element parent)
   {
      Element versionElement = parent.getOwnerDocument().createElement(PRIVATE);
      parent.appendChild(versionElement);

      versionElement.setAttribute(VERSION_ATT, node.getVersion());
      versionElement.setAttribute(ID_ATT, node.getId());
      versionElement.setAttribute(NAME_ATT, node.getName());
      String owner = node.getPrivateVersionOwner();

      if (owner != null)
      {
         versionElement.setAttribute(OWNER_ATT, owner);
      }

      return versionElement;
   }

   private Element createPublicVersionElement(ModelNode node, Element parent)
   {
      Element element = parent.getOwnerDocument().createElement(PUBLIC);
      parent.appendChild(element);

      element.setAttribute(VERSION_ATT, node.getVersion());
      element.setAttribute(ID_ATT, node.getId());
      element.setAttribute(NAME_ATT, node.getName());
      element.setAttribute(VERSION_COUNT_ATT, String.valueOf(node.getVersionCount()));

      // @todo (france, ub): multiline descriptions
      if (!StringUtils.isEmpty(node.getDescription()))
      {
         element.setAttribute(DESCRIPTION_ATT, node.getDescription());
      }

      Date validFrom = node.getValidFrom();
      if (validFrom != null)
      {
         element.setAttribute(VALID_FROM_ATT,
               DateUtils.getNoninteractiveDateFormat().format(validFrom));
      }
      Date validTo = node.getValidTo();
      if (validTo != null)
      {
         element.setAttribute(VALID_TO_ATT,
               DateUtils.getNoninteractiveDateFormat().format(validTo));
      }

      String comment = node.getDeploymentComment();
      if (!StringUtils.isEmpty(comment))
      {
         element.setAttribute(DEPLOYMENT_COMMENT_ATT, comment);
      }

      Date deploymentTime = node.getDeploymentTime();
      if (deploymentTime != null)
      {
         element.setAttribute(DEPLOYMENT_TIME_ATT,
               DateUtils.getNoninteractiveDateFormat().format(deploymentTime));
      }

      if (node.getModelOID() != 0)
      {
         element.setAttribute(OID_ATT, String.valueOf(node.getModelOID()));
      }

      if (node.isReleased())
      {
         element.setAttribute(RELEASED_ATT, "true");
         Date releaseTime = node.getReleaseTime();
         if (releaseTime != null)
         {
            element.setAttribute(RELEASE_TIME_ATT,
                  DateUtils.getNoninteractiveDateFormat().format(releaseTime));
         }
      }

      return element;

   }

   public void loadRepository(ModelRepository repository)
   {
      File bootFile = new File(repositoryDirectory, BOOT_FILE);

      if (bootFile.exists())
      {
         Document document = XmlUtils.readDocument(null, bootFile, null);
         Element root = document.getDocumentElement();
         NodeList l = root.getChildNodes();
         for (int i = 0; i < l.getLength(); i++)
         {
            Node node = l.item(i);
            if (!PUBLIC.equals(node.getNodeName()))
            {
               continue;
            }

            Element element = ((Element) node);
            String versionCount = element.getAttribute(VERSION_COUNT_ATT);
            ModelNode modelNode = repository.attachRootModel(
                  element.getAttribute(ID_ATT),
                  element.getAttribute(NAME_ATT), Integer.parseInt(versionCount));
            attachPublicAttributes(modelNode, element);
            loadVersions(modelNode, (Element) node);
         }
      }
   }

   private void attachPublicAttributes(ModelNode node, Element element)
   {
      String description = element.getAttribute(DESCRIPTION_ATT);
      if (!StringUtils.isEmpty(description))
      {
         node.setDescription(description);
      }
      String validFrom = element.getAttribute(VALID_FROM_ATT);
      try
      {
         if (!StringUtils.isEmpty(validFrom))
         {
            node.setValidFrom(
                  DateUtils.getNoninteractiveDateFormat().parse(validFrom));
         }
      }
      catch (ParseException e)
      {
         trace.warn("", e);
      }
      String validTo = element.getAttribute(VALID_TO_ATT);
      try
      {
         if (!StringUtils.isEmpty(validTo))
         {
            node.setValidTo(DateUtils.getNoninteractiveDateFormat().parse(validTo));
         }
      }
      catch (ParseException e)
      {
         trace.warn("", e);
      }
      String comment = element.getAttribute(DEPLOYMENT_COMMENT_ATT);
      if (!StringUtils.isEmpty(comment))
      {
         node.setDeploymentComment(comment);
      }
      String deploymentTime = element.getAttribute(DEPLOYMENT_TIME_ATT);
      try
      {
         if (!StringUtils.isEmpty(deploymentTime))
         {
            node.setDeploymentTime(
                  DateUtils.getNoninteractiveDateFormat().parse(deploymentTime));
         }
      }
      catch (ParseException e)
      {
         trace.warn("", e);
      }
      try
      {
         String oid = element.getAttribute(OID_ATT);
         if (!StringUtils.isEmpty(oid))
         {
            node.setModelOID(Integer.parseInt(oid));
         }
      }
      catch (NumberFormatException e)
      {
         trace.warn("", e);
      }
      try
      {
         String released = element.getAttribute(RELEASED_ATT);
         if ("true".equalsIgnoreCase(released))
         {
            String ts = element.getAttribute(RELEASE_TIME_ATT);
            Date releaseTime = null;
            if (ts != null)
            {
               try
               {
                  releaseTime = DateUtils.getNoninteractiveDateFormat().parse(ts);
               }
               catch (ParseException e)
               {
                  trace.warn("", e);
               }
            }
            node.release(releaseTime);
         }
      }
      catch (NumberFormatException e)
      {
         trace.warn("", e);
      }
   }

   private void loadVersions(ModelNode modelNode, Element parent)
   {
      NodeList l = parent.getChildNodes();
      for (int i = 0; i < l.getLength(); i++)
      {
         Node node = l.item(i);
         if (PUBLIC.equals(node.getNodeName()))
         {
            Element element = (Element) node;
            String versionCount = element.getAttribute(VERSION_COUNT_ATT);
            ModelNode child = modelNode.attachPublicVersion(element.getAttribute(ID_ATT),
                  element.getAttribute(NAME_ATT), element.getAttribute(VERSION_ATT),
                  Integer.parseInt(versionCount));
            attachPublicAttributes(child, element);
            loadVersions(child, (Element) node);
         }
         else if (PRIVATE.equals(node.getNodeName()))
         {
            Element element = (Element) node;
            modelNode.attachPrivateVersion(element.getAttribute(OWNER_ATT),
                  element.getAttribute(VERSION_ATT));
         }
      }
   }

   private String getModelFileName(ModelNode node)
   {
      return node.getId() + "_" + node.getFullVersion() + ".mod";
   }

   /**
    * Creates a .lock file for file <tt>file</tt>.
    */
   private File lock(File file)
   {
      String path = file.getName();
      try
      {
         File lockFile = new File(repositoryDirectory, path + ".lock");

         if (lockFile.exists())
         {
            try
            {
               Thread.sleep(2000);
            }
            catch (InterruptedException x)
            {
            }

            if (lockFile.exists())
            {
               throw new PublicException("Cannot lock file '" + path + "'. ");
            }
         }

         lockFile.deleteOnExit();
         lockFile.createNewFile();
         return lockFile;
      }
      catch (IOException x)
      {
         throw new InternalException("Problems during lock file creation.", x);
      }
   }

   /**
    * Removes a .lock file for file <tt>file</tt>.
    */
   private void releaseLock(File file)
   {
      if (file == null)
      {
         return;
      }
      file.delete();
   }

}
