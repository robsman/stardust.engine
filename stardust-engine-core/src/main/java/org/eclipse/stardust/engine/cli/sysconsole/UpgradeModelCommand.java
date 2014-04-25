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
package org.eclipse.stardust.engine.cli.sysconsole;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.removethis.ModelProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.RepositoryItem;
import org.eclipse.stardust.engine.core.upgrade.framework.RepositoryUpgrader;
import org.eclipse.stardust.engine.core.upgrade.framework.Upgrader;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UpgradeModelCommand extends ConsoleCommand
{
   private static final Logger trace = LogManager.getLogger(UpgradeModelCommand.class);

   private static final Options argTypes = new Options();

   static
   {
      argTypes.register("-file", "-f", "file", "The model file to upgrade inplace.", true);
      argTypes.register("-source", "-s", "source", "The source file to upgrade", true);
      argTypes.register("-target", "-t", "target", "The target file for upgrade", true);
      argTypes.register("-repository", "-r", "repository",
            "Indicates that the whole model repository should be upgraded.", false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public int run(Map options)
   {
      boolean repository = options.containsKey("repository");
      String sourceFileName = (String) options.get("source");
      String targetFileName = (String) options.get("target");
      String modelFileName = (String) options.get("file");

      if (repository)
      {
         print("Upgrading repository:\n");
      }
      else
      {
         if (sourceFileName != null && targetFileName == null)
         {
            throw new PublicException(
                  BpmRuntimeError.CLI_PLEASE_PROVIDE_TARGET_FILENAME.raise());
         }
         if (sourceFileName != null)
         {
            print("Upgrading modelfile '" + sourceFileName + "' to '" +
                  targetFileName + "':\n");
         }
         else
         {
            if (modelFileName == null)
            {
               throw new PublicException(
                     BpmRuntimeError.CLI_NEITHER_REPOSITORY_NOR_MODEL_FILE_PROVIDED
                           .raise());
            }
            print("Upgrading modelfile '" + modelFileName +
                  "' (will be overwritten):\n");
         }
      }

      if (!force() && !confirm("Do you want to proceed? (Y/N): "))
      {
         return OPERATION_CANCELLED;
      }

      if (repository)
      {
         upgradeRepository();
         print("Repository upgraded.");
      }
      else
      {
         if (modelFileName != null)
         {
            upgradeModelFile(modelFileName, modelFileName);
         }
         if (sourceFileName != null)
         {
            upgradeModelFile(sourceFileName, targetFileName);
         }
         print("Model upgraded.");
      }
      return 0;
   }

   private void upgradeRepository()
   {
      RepositoryItem repositoryItem = new RepositoryItem();
      Upgrader upgrader = new RepositoryUpgrader(repositoryItem);
      upgrader.upgrade(false);

      // @todo (france, ub): rework
      for (Iterator i = getRepositoryFiles(); i.hasNext();)
      {
         String fileName = (String) i.next();
         upgradeModelFile(fileName, fileName);
      }
   }

   String upgradeModel(String model)
   {
      ModelItem modelItem = new ModelItem(model);
      Upgrader upgrader = new ModelUpgrader(modelItem);
      return ((ModelItem) upgrader.upgrade(false)).getModel();
   }

   void upgradeModelFile(String source, String dest)
   {
      String model = readFile(source);
      model = upgradeModel(model);
      writeFile(dest, model);
   }

   Iterator getRepositoryFiles()
   {
      List repositoryFiles = CollectionUtils.newList();
      String repositoryPath = Parameters.instance().getString(
            ModelProperties.REPOSITORY_PATH, ".");
      File repository = new File(repositoryPath);
      String[] filelist = repository.list();
      for (int i = 0; i < filelist.length; i++)
      {
         if (filelist[i].endsWith(".mod"))
         {
            repositoryFiles.add(repositoryPath + "/" + filelist[i]);
         }
      }
      return repositoryFiles.iterator();
   }

   String readFile(String fileName)
   {
      try
      {
         return XmlUtils.getXMLString(fileName);
      }
      catch (Exception x)
      {
         trace.warn("", x);
         throw new PublicException(
               BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(x.getMessage()));
      }
   }

   void writeFile(String filename, String model)
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
         throw new PublicException(
               BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(x.getMessage()));
      }
   }

   public String getSummary()
   {
      return "Upgrades a model or a model repository from a previous Infinity version.";
   }
}
