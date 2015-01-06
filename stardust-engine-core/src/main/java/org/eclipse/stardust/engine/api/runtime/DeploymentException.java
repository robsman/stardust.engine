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
package org.eclipse.stardust.engine.api.runtime;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.error.PublicException;


/**
 * Thrown if an exception occured during deployment.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeploymentException extends PublicException
{
   private static final long serialVersionUID = 1L;

   private List<DeploymentInfo> infos;

   /**
    * Creates a DeploymentException with a message and the corresponding deployment information.
    *
    * @param message the error message.
    * @param info the deployment information.
    */
   public DeploymentException(String message, DeploymentInfo infos)
   {
      this(message, Collections.singletonList(infos));
   }

   /**
    * Creates a DeploymentException with a message and the corresponding deployment information.
    *
    * @param message the error message.
    * @param info the deployment information.
    */
   public DeploymentException(String message, List<DeploymentInfo> infos)
   {
      super(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(message));
      this.infos = infos;
   }

   public DeploymentException(List<DeploymentInfo> infos, String pattern, Object... args)
   {
      super(BpmRuntimeError.GEN_AN_EXCEPTION_OCCURED_AND_MESSAGE.raise(MessageFormat.format(pattern, args)));
      this.infos = infos;
   }

   /**
    * Gets information about the deployment operation.
    *
    * @return the deployment information.
    */
   public DeploymentInfo getDeploymentInfo()
   {
      return infos == null || infos.isEmpty() ? null : infos.get(0);
   }

   /**
    * Gets information about the deployment operation.
    *
    * @return the deployment information.
    */
   public List<DeploymentInfo> getInfos()
   {
      return infos;
   }
}
