/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public enum PredefinedProcessInstanceLinkTypes
{
   SWITCH("Peer Process Instance"),
   JOIN("Join Process Instance"),
   UPGRADE("Upgrade Process Instance"),
   SPAWN("Spawn Process Instance"),
   RELATED("Related Process Instance"),
   INSERT("Inserted Process Instance");

   private String id;
   private String description;

   private PredefinedProcessInstanceLinkTypes(String description)
   {
      id = name().toLowerCase();
      this.description = description;
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }
}
