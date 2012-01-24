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

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public interface Deployment extends Serializable
{
   public enum Type
   {
      Version, Overwrite, Link
   }
   
   User getDeployer();
   
   Calendar getDate();
   
   String getComment();
   
   List<DeployedModelDescription> getModels();
   
   Type getType();
}
