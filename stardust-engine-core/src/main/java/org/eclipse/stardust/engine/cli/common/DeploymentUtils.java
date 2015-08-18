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
package org.eclipse.stardust.engine.cli.common;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.model.repository.ModelNode;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeploymentUtils
{
   public static ModelNode deployFromRepository(ServiceFactory sf,
         DeploymentCallback callback, ModelNode model, boolean versionize,
         int predecessor, Date validFrom, Date validTo, String comment, boolean disabled,
         boolean ignoreWarnings)
         throws DeploymentException
   {
      String modelString = getXmlStringFromModelNode(model);

      if (ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
      {
         modelString = XpdlUtils.convertCarnot2Xpdl(modelString);
      }
      
      DeploymentInfo info = null;
      try
      {
         info = sf.getAdministrationService().deployModel(modelString, "",
                     predecessor, validFrom, validTo, comment, disabled, ignoreWarnings);
         if (!info.isValid())
         {
            callback.reportWarnings(info.getWarnings());
         }
      }
      catch (DeploymentException e)
      {
         info = e.getDeploymentInfo();
         if (info.hasErrors())
         {
            callback.reportErrors(info.getErrors());
            throw e;
         }
         List warnings = info.getWarnings();
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               info = sf.getAdministrationService().deployModel(modelString, "",
                     predecessor, validFrom, validTo, comment, disabled, true);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }

      upgradeModelNode(model, info);

      ModelNode result = null;
      if (versionize)
      {
         result = model.createPublicVersion();
      }

      model.getRepository().save();

      return result;
   }

   public static ModelNode overwriteFromRepository(ServiceFactory sf, DeploymentCallback callback,
         ModelNode model, boolean versionize,
         int modelOID, Date validFrom, Date validTo, String comment, boolean disabled,
         boolean ignoreWarnings)
         throws DeploymentException
   {
      String modelString = getXmlStringFromModelNode(model);

      if (ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
      {
         modelString = XpdlUtils.convertCarnot2Xpdl(modelString);
      }
      
      DeploymentInfo info;
      try
      {
         info = sf.getAdministrationService().overwriteModel(modelString,
                     "", modelOID, validFrom, validTo, comment, disabled, ignoreWarnings);
         if (!info.isValid())
         {
            callback.reportWarnings(info.getWarnings());
         }
      }
      catch (DeploymentException e)
      {
         info = e.getDeploymentInfo();
         if (info.hasErrors())
         {
            callback.reportErrors(info.getErrors());
            throw e;
         }
         List warnings = info.getWarnings();
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               info = sf.getAdministrationService().overwriteModel(modelString,
                  "", modelOID, validFrom, validTo, comment, disabled, true);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }

      upgradeModelNode(model, info);

      ModelNode result = null;
      if (versionize)
      {
         result = model.createPublicVersion();
      }

      model.getRepository().save();

      return result;
   }

   private static void upgradeModelNode(ModelNode model, DeploymentInfo info)
   {
      model.setModelOID(info.getModelOID());
      model.setValidFrom(info.getValidFrom());
      model.setDeploymentTime(info.getDeploymentTime());
      model.setDeploymentComment(info.getDeploymentComment());
      model.setRevision(info.getRevision());
      
      // inject modified attributes into model
      model.getModel(true);
   }


   /**
    * Converts the model to be deployed into XML String. This string will be then
    * serialized to engine for deployment.
    */
   private static String getXmlStringFromModelNode(ModelNode model)
   {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      model.exportAsXML(outputStream, false);
      return new String(outputStream.toByteArray());
   }

   public static void deployFromFiles(ServiceFactory serviceFactory,
         DeploymentCallback callback, List<DeploymentElement> elements, DeploymentOptions options) throws DeploymentException
   {
      try
      {
         List<DeploymentInfo> infos = serviceFactory.getAdministrationService().deployModel(elements, options);
         List<Inconsistency> warnings = CollectionUtils.newList();
         boolean valid = true;
         for (DeploymentInfo info : infos)
         {
            valid &= info.isValid();
            warnings.addAll(info.getWarnings());
         }
         if (!valid)
         {
            callback.reportWarnings(warnings);
         }
      }
      catch (DeploymentException e)
      {
         List<DeploymentInfo> infos = e.getInfos();
         List<Inconsistency> errors = CollectionUtils.newList();
         List<Inconsistency> warnings = CollectionUtils.newList();
         boolean errs = true;
         for (DeploymentInfo info : infos)
         {
            errs &= info.hasErrors();
            errors.addAll(info.getErrors());
            warnings.addAll(info.getWarnings());
         }
         if (errs)
         {
            callback.reportErrors(errors);
            throw e;
         }
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               options.setIgnoreWarnings(true);
               serviceFactory.getAdministrationService().deployModel(elements, options);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
   }

   public static void deployFromFile(ServiceFactory serviceFactory,
         DeploymentCallback callback, String xmlString, Date validFrom,
         Date validTo, String deploymentComment, boolean disabled,
         boolean ignoreWarnings, int predecessorOID)
         throws DeploymentException
   {
      try
      {
         DeploymentInfo info = serviceFactory.getAdministrationService().deployModel(
               xmlString, null, predecessorOID, validFrom, validTo, deploymentComment,
               disabled, ignoreWarnings);
         if (!info.isValid())
         {
            callback.reportWarnings(info.getWarnings());
         }
      }
      catch (DeploymentException e)
      {
         DeploymentInfo info = e.getDeploymentInfo();
         if (info.hasErrors())
         {
            callback.reportErrors(info.getErrors());
            throw e;
         }
         List warnings = info.getWarnings();
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               serviceFactory.getAdministrationService().deployModel(xmlString, null,
                  predecessorOID, validFrom, validTo, deploymentComment, disabled, true);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
   }

   public static void overwriteFromFile(ServiceFactory serviceFactory,
         DeploymentCallback callback, DeploymentElement element, int oid, DeploymentOptions options)
         throws DeploymentException
   {
      try
      {
         DeploymentInfo info = serviceFactory.getAdministrationService().overwriteModel(element, oid, options);
         if (!info.isValid())
         {
            callback.reportWarnings(info.getWarnings());
         }
      }
      catch (DeploymentException e)
      {
         DeploymentInfo info = e.getDeploymentInfo();
         if (info.hasErrors())
         {
            callback.reportErrors(info.getErrors());
            throw e;
         }
         List warnings = info.getWarnings();
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               serviceFactory.getAdministrationService().overwriteModel(element, oid, options);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
   }

   public static void overwriteFromFile(ServiceFactory serviceFactory,
         DeploymentCallback callback, int oid, String xmlString,
         Date validFrom, Date validTo, String deploymentComment, boolean disabled,
         boolean ignoreWarnings)
         throws DeploymentException
   {
      try
      {
         DeploymentInfo info = serviceFactory.getAdministrationService().overwriteModel(
               xmlString, null,
                     oid, validFrom, validTo, deploymentComment, disabled, ignoreWarnings);
         if (!info.isValid())
         {
            callback.reportWarnings(info.getWarnings());
         }
      }
      catch (DeploymentException e)
      {
         DeploymentInfo info = e.getDeploymentInfo();
         if (info.hasErrors())
         {
            callback.reportErrors(info.getErrors());
            throw e;
         }
         List warnings = info.getWarnings();
         if (warnings.isEmpty())
         {
            warnings = new ArrayList(1);
            warnings.add(new Inconsistency(e.getMessage(), Inconsistency.WARNING));
         }
         boolean proceed = callback.reportWarnings(warnings);
         if (proceed)
         {
            try
            {
               serviceFactory.getAdministrationService().overwriteModel(xmlString, null,
                  oid, validFrom, validTo, deploymentComment, disabled, true);
            }
            catch (DeploymentException e1)
            {
               callback.reportErrors(e.getDeploymentInfo().getErrors());
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
   }
}
