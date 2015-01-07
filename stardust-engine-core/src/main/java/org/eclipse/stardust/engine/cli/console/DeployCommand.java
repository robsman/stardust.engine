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
package org.eclipse.stardust.engine.cli.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.common.error.IErrorMessageProvider.Factory;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.console.ConsoleCommand;
import org.eclipse.stardust.common.utils.console.IllegalUsageException;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.cli.common.DeploymentCallback;
import org.eclipse.stardust.engine.cli.common.DeploymentUtils;
import org.eclipse.stardust.engine.core.model.beans.DefaultConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeployCommand extends ConsoleCommand
{
   private static final Options argTypes = new Options();

   private static final String IGNORE_WARNINGS = "ignoreWarnings";

   private static final String COMMENT = "comment";

   private static final String OVERWRITE = "overwrite";

   private static final String DEPLOY_VERSION = "deployVersion";

   private static final String OVERWRITE_VERSION = "overwriteVersion";

   private static final String OVERWRITE_ACTIVE = "overwriteActive";

   private static final String FILENAME = "filename";

   private static final String VALIDFROM = "validfrom";

   private static final String PARTITION = "partition";

   private static final char PARTITION_SEPERATOR = ',';

   static
   {
      argTypes.register("-filename", "-n", FILENAME, "The file to be deployed.", true);
      argTypes
            .register(
                  "-deployVersion",
                  null,
                  DEPLOY_VERSION,
                  "If set the given model will only be deployed if no existing deployment\n"
                        + "of a model having the same ID and version exists.\n"
                        + "This option may be used in combination with the -overwriteVersion option.",
                  false);
      argTypes
            .register(
                  "-overwriteVersion",
                  null,
                  OVERWRITE_VERSION,
                  "If set an existing deployment of the model having the same ID and version\n"
                        + "as the model to be deployed will be overwritten.\n"
                        + "This option may be used in combination with the -deployVersion option.",
                  false);
      argTypes.register("-overwrite", "-o", OVERWRITE,
            "If set the model identified by the given OID will be overwritten.", true);
      argTypes.register("-overwriteActive", "-a", OVERWRITE_ACTIVE,
            "If set the active model will be overwritten", false);
      argTypes.register("-validfrom", "-f", VALIDFROM,
            "'Valid from' time of the deployment.", true);
      argTypes.register("-comment", "-c", COMMENT, "Deployment comment", true);
      argTypes.register("-ignoreWarnings", "-w", IGNORE_WARNINGS,
            "If set a deployment is done even in case of warnings.", false);
      argTypes
            .register(
                  "-" + PARTITION,
                  "-part",
                  PARTITION,
                  "Deploy the model to the partitions given by the comma separated list.",
                  true);

      argTypes.addExclusionRule(new String[] {FILENAME}, true);
      argTypes.addExclusionRule(new String[] {
            OVERWRITE, OVERWRITE_VERSION, OVERWRITE_ACTIVE}, false);
      argTypes.addExclusionRule(new String[] {OVERWRITE, DEPLOY_VERSION}, false);
      argTypes.addExclusionRule(new String[] {OVERWRITE_ACTIVE, DEPLOY_VERSION}, false);
   }

   public Options getOptions()
   {
      return argTypes;
   }

   private List iteratorToList(Iterator iter)
   {
      List list = new ArrayList();

      while (iter.hasNext())
      {
         list.add(iter.next());
      }

      return list;
   }

   public int run(Map options)
   {

      Date validFrom = getDateOption(options, VALIDFROM);
      String filename = (String) options.get(FILENAME);
      List partitions = iteratorToList(StringUtils.split((String) options.get(PARTITION),
            PARTITION_SEPERATOR));

      String id = null;
      String name = null;

      List<ModelFile> modelFiles = null;

      String newModelVersion = null;
      if (filename != null)
      {
         if (filename.endsWith(".zip"))
         {
            if (getIntegerOption(options, OVERWRITE) != 0)
            {
               throw new IllegalUsageException(
                     "The option 'overwrite' can not be used in combination with an archive.");
            }

            if (options.containsKey(OVERWRITE_ACTIVE))
            {
               throw new IllegalUsageException(
                     "The option 'overwriteActive' can not be used in combination with an archive.");
            }

            if (options.containsKey(OVERWRITE_VERSION))
            {
               throw new IllegalUsageException(
                     "The option 'overwriteVersion' can not be used in combination with an archive.");
            }
         }

         modelFiles = readModelsFromFile(filename);
         for (Iterator<ModelFile> i = modelFiles.iterator(); i.hasNext();)
         {
            ModelFile modelFile = i.next();
            newModelVersion = (String) modelFile.getModel().getAttribute(
                  PredefinedConstants.VERSION_ATT);
            if (validFrom == null)
            {
               validFrom = (Date) modelFile.getModel().getAttribute(
                     PredefinedConstants.VALID_FROM_ATT);
            }
            id = modelFile.getModel().getId();
            name = modelFile.getModel().getName();
            print("\nDeploying model:\n");
            printModel(id, name, newModelVersion, validFrom, null);
         }
      }

      int result = 0;
      if (partitions.isEmpty())
      {
         result = deployModel(ServiceFactoryLocator.get(globalOptions), options, id,
               validFrom, null, newModelVersion, modelFiles);
      }
      else
      {
         for (Iterator iter = partitions.iterator(); iter.hasNext() && 0 == result;)
         {
            String partitionId = (String) iter.next();
            print(MessageFormat.format("\nCurrent partition ID: {0}\n", partitionId));

            globalOptions.remove(SecurityProperties.CRED_PARTITION);
            globalOptions.put(SecurityProperties.CRED_PARTITION, partitionId);

            result = deployModel(ServiceFactoryLocator.get(globalOptions), options, id,
                  validFrom, null, newModelVersion, modelFiles);
         }
      }

      return result;
   }

   private int deployModel(ServiceFactory serviceFactory, Map options, String id,
         Date validFrom, Date validTo, String newModelVersion, List<ModelFile> units)
         throws ServiceNotAvailableException, LoginFailedException, PublicException,
         ObjectNotFoundException
   {
      String deploymentComment = (String) options.get(COMMENT);
      int overwrite = getIntegerOption(options, OVERWRITE);
      boolean deployVersion = options.containsKey(DEPLOY_VERSION);
      boolean overwriteVersion = options.containsKey(OVERWRITE_VERSION);
      boolean overwriteActive = options.containsKey(OVERWRITE_ACTIVE);
      boolean ignoreWarnings = options.containsKey(IGNORE_WARNINGS);

      try
      {
         QueryService service = serviceFactory.getQueryService();

         DeployedModelDescription activeModel = null;
         Models activeModelsForId = service.getModels(DeployedModelQuery
               .findActiveForId(id));
         if (activeModelsForId.isEmpty())
         {
            if (overwriteActive)
            {
               throw new PublicException(BpmRuntimeError.CLI_NO_MODEL_ACTIVE.raise());
            }
            else
            {
               print("No model active.\n");
            }
         }
         else
         {
            activeModel = activeModelsForId.get(0);
            print("Currently active model:\n");
            printModel(activeModel);
         }

         // check for deployment of model having the same ID and version as the model to
         // be deployed
         DeployedModelDescription deployedModelVersion = null;
         if (deployVersion || overwriteVersion)
         {
            if (StringUtils.isEmpty(newModelVersion))
            {
               print("The model to be deployed does not contain a version tag.");
               return -1;
            }

            List allModels = service.getAllModelDescriptions();
            for (Iterator i = allModels.iterator(); i.hasNext();)
            {
               DeployedModelDescription model = (DeployedModelDescription) i.next();
               if (CompareHelper.areEqual(id, model.getId())
                     && newModelVersion.equals(model.getVersion()))
               {
                  if (null != deployedModelVersion)
                  {
                     print("Found more than one deployment of the model with id '" + id
                           + "' and version '" + newModelVersion + "'.");
                     return -1;
                  }

                  deployedModelVersion = model;
               }
            }
         }

         if (overwrite != 0)
         {
            DeployedModelDescription deployedModel = service
                  .getModelDescription(overwrite);
            print("\nThe model you are deploying will overwrite the following model:");
            printModel(deployedModel);
         }
         else if (deployVersion || overwriteVersion)
         {
            if (null == deployedModelVersion)
            {
               if (!deployVersion)
               {
                  print("Unable to overwrite the model with id '" + id
                        + "' and version '" + newModelVersion
                        + "' as there currently exists no such model " + "deployment.");
                  return -1;
               }

               // create a new deployment of the given model
               overwrite = 0;
               print("\nCreating a new deployment of this model.\n");
            }
            else
            {
               if (!overwriteVersion)
               {
                  print("Unable to deploy the model with id '" + id + "' and version '"
                        + newModelVersion + "' as there already exists a deployment of "
                        + "a model having the same id and version.");
                  return -1;
               }

               // overwrite the existing deployment
               overwrite = deployedModelVersion.getModelOID();
               print("\nThe model you are deploying will overwrite the following model:\n");
               printModel(deployedModelVersion);
            }
         }
         else if (options.containsKey(OVERWRITE_ACTIVE))
         {
            overwrite = activeModel.getModelOID();
            print("\nThe model you are deploying will overwrite the active model.\n");
         }

         if (!force() && !confirm("Do you want to continue?: "))
         {
            return -1;
         }

         MyDeploymentCallback callback = new MyDeploymentCallback(ignoreWarnings);
         try
         {
            DeploymentOptions deploymentOptions = new DeploymentOptions();
            deploymentOptions.setValidFrom(validFrom);
            deploymentOptions.setComment(deploymentComment);
            deploymentOptions.setIgnoreWarnings(ignoreWarnings);

            if (overwrite != 0)
            {
               DeploymentUtils.overwriteFromFile(serviceFactory, callback,
                     getUnits(units).get(0), overwrite, deploymentOptions);
            }
            else
            {
               DeploymentUtils.deployFromFiles(serviceFactory, callback, getUnits(units),
                     deploymentOptions);
            }
            print("\nModel(s) deployed.");
            return 0;
         }
         catch (DeploymentException e)
         {
            print("\nDeployment errors found. Model not deployed.");
            return 1;
         }
      }
      finally
      {
         serviceFactory.close();
      }
   }

   private void printWarnings(List warnings)
   {
      for (Iterator i = warnings.iterator(); i.hasNext();)
      {
         Object warning = i.next();
         if (warning instanceof Inconsistency)
         {
            Inconsistency inc = (Inconsistency) warning;
            String message = inc.getMessage();
            if (inc.getError() != null)
            {
               message = getMessageFromErrorCase(inc.getError());
            }
            if (inc.getSourceElementOID() == 0)
            {
               print("  WARN : " + message);
            }
            else
            {
               print("  WARN : " + message + "; element oid = "
                     + inc.getSourceElementOID());
            }
         }
         else
         {
            print("  WARN : " + String.valueOf(warning));
         }
      }
   }

   private void printErrors(List errors)
   {
      for (Iterator i = errors.iterator(); i.hasNext();)
      {
         Object error = i.next();
         if (error instanceof Inconsistency)
         {
            Inconsistency inc = (Inconsistency) error;
            String message = inc.getMessage();
            if (inc.getError() != null)
            {
               message = getMessageFromErrorCase(inc.getError());
            }
            if (inc.getSourceElementOID() == 0)
            {
               print("  ERROR : " + message);
            }
            else
            {
               print("  ERROR : " + message + "; element oid = "
                     + inc.getSourceElementOID());
            }
         }
         else
         {
            print("  ERROR : " + String.valueOf(error));
         }
      }
   }

   private void printModel(DeployedModelDescription model)
   {
      print("Model Name: " + model.getName());
      print("Model Id:   " + model.getId());
      print("Model OID:  " + model.getModelOID());
      print("Version:    " + model.getVersion());
      print("Valid From: " + formatDate(model.getValidFrom()));
      print("");
   }

   private void printModel(String id, String name, String version, Date validFrom,
         Date validTo)
   {
      print("Model Name: " + name);
      print("Model Id:   " + id);
      print("Version:    " + version);
      print("Valid From: " + formatDate(validFrom));
      print("Valid To:   " + formatDate(validTo));
      print("");
   }

   private String formatDate(Date date)
   {
      if (date != null)
      {
         return DateFormat.getDateInstance().format(date);
      }
      else
      {
         return "Unspecified";
      }
   }

   public String getSummary()
   {
      return "Deploys a model.";
   }

   private class MyDeploymentCallback implements DeploymentCallback
   {
      private boolean ignoreWarnings;

      public MyDeploymentCallback(boolean ignoreWarnings)
      {
         this.ignoreWarnings = ignoreWarnings;
      }

      public void reportErrors(List errors)
      {
         printErrors(errors);
      }

      public boolean reportWarnings(List warnings)
      {
         printWarnings(warnings);
         if (warnings.isEmpty() || ignoreWarnings)
         {
            return true;
         }
         return false;
      }
   }

   public class ModelFile
   {
      private DeploymentElement deploymentElement;

      private boolean xpdl;

      private String xmlString;

      private IModel model;

      public ModelFile(DeploymentElement deploymentElement, String filename)
      {
         super();
         this.deploymentElement = deploymentElement;
         this.xpdl = filename.endsWith(XpdlUtils.EXT_XPDL);
         try
         {
            String encoding = Parameters.instance().getObject(
                  PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
            this.xmlString = new String(deploymentElement.getContent(), encoding);
            //this.xmlString = XmlUtils.getXMLString(this.xmlString.getBytes(encoding));
            final IConfigurationVariablesProvider confVarProvider = new DefaultConfigurationVariablesProvider();
            if (isXpdl())
            {
               model = XpdlUtils.loadXpdlModel(xmlString, confVarProvider, false);

               if (!ParametersFacade.instance().getBoolean(
                     KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
               {
                  xmlString = XpdlUtils.convertXpdl2Carnot(xmlString);
               }
            }
            else
            {
               model = new DefaultXMLReader(false, confVarProvider)
                     .importFromXML(new StringReader(xmlString));

               if (ParametersFacade.instance().getBoolean(
                     KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
               {
                  xmlString = XpdlUtils.convertCarnot2Xpdl(xmlString);
               }
            }
         }
         catch (UnsupportedEncodingException e)
         {
            e.printStackTrace();
         }
      }

      public boolean isXpdl()
      {
         return xpdl;
      }

      public DeploymentElement getDeploymentElement()
      {
         return deploymentElement;
      }

      public String getXmlString()
      {
         return this.xmlString;
      }

      public IModel getModel()
      {
         return model;
      }
   }

   private List<DeploymentElement> getUnits(List<ModelFile> contents)
   {
      List<DeploymentElement> units = new ArrayList<DeploymentElement>();
      for (Iterator<ModelFile> i = contents.iterator(); i.hasNext();)
      {
         ModelFile content = i.next();
         units.add(content.getDeploymentElement());
      }
      return units;
   }

   public List<ModelFile> readModelsFromFile(String filename)
   {
      List<ModelFile> bytesList = new ArrayList<ModelFile>();
      if (!filename.endsWith(".zip"))
      {
         try
         {
            DeploymentElement de = new DeploymentElement(XmlUtils.getContent(filename));
            ModelFile dc = new ModelFile(de, filename);
            bytesList.add(dc);
            return bytesList;
         }
         catch (IOException e)
         {
            throw new PublicException(e);
         }
      }
      ZipFile zip;
      try
      {
         zip = new ZipFile(new File(filename));
         for (Enumeration e = zip.entries(); e.hasMoreElements();)

         {
            ZipEntry entry = (ZipEntry) e.nextElement();
            InputStream is = zip.getInputStream(entry);
            byte[] bytes = new byte[(int) entry.getSize()];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                  && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
            {
               offset += numRead;
            }
            DeploymentElement de = new DeploymentElement(bytes);
            ModelFile dc = new ModelFile(de, entry.getName());
            bytesList.add(dc);
         }
      }
      catch (ZipException e1)
      {
         throw new PublicException(e1);
      }
      catch (IOException e1)
      {
         throw new PublicException(e1);
      }
      return bytesList;
   }

   public String getMessageFromErrorCase(ErrorCase errorCase)
   {
      ArrayList<Factory> translators = new ArrayList<IErrorMessageProvider.Factory>(
            ExtensionProviderUtils.getExtensionProviders(IErrorMessageProvider.Factory.class));

      Locale locale = Locale.getDefault();
      Iterator<IErrorMessageProvider.Factory> tIter = translators.iterator();
      while (tIter.hasNext())
      {
         IErrorMessageProvider.Factory msgFactory = (IErrorMessageProvider.Factory) tIter.next();
         IErrorMessageProvider msgProvider = msgFactory.getProvider(errorCase);
         if (msgProvider != null)
         {
            return msgProvider.getErrorMessage(errorCase, null, locale);
         }
      }

      return null;
   }

}
