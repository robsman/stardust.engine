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

import org.eclipse.stardust.engine.core.model.beans.XMLConstants;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class GenericLinkConnection extends Connection
{
   private String linkTypeId;

   public GenericLinkConnection(String linkTypeId, int sourceId, int targetId)
   {
      super(XMLConstants.GENERIC_LINK_CONNECTION, sourceId, targetId);
      this.linkTypeId = linkTypeId;
   }

   public String getLinkTypeId()
   {
      return linkTypeId;
   }
}
