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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Data extends IdentifiableElement
{
   private String type;

   public Data(String id, String name, String description)
   {
      super(id, name, description);
      setAttribute(new Attribute(PredefinedConstants.BROWSABLE_ATT, "boolean", "true"));
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getType()
   {
      return type;
   }
}
