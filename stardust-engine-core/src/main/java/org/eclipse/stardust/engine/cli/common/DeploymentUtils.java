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
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeploymentUtils
{
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
