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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.engine.api.model.IApplicationType;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ApplicationTypeBean extends IdentifiableElementBean implements IApplicationType
{
   public static final String SYNCHRONOUS_ATT = "Synchronous";
   private boolean synchronous;

   public ApplicationTypeBean()
   {
   }

   public ApplicationTypeBean(String id, String name, boolean predefined,
         boolean synchronous)
   {
      super(id, name);
      setPredefined(predefined);
      this.synchronous = synchronous;
   }

   public boolean isSynchronous()
   {
      return synchronous;
   }

   public void setSynchronous(boolean synchronous)
   {
      this.synchronous = synchronous;
   }

   public String toString()
   {
      return "ApplicationType: " + getName();
   }
}
