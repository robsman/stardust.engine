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
package org.eclipse.stardust.engine.core.model.convert.bpml;

import org.w3c.dom.Node;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class XMLUtil extends org.eclipse.stardust.engine.core.model.convert.XMLUtil
{
   private static final String NAME_ATTRIBUTE = "name";
   private static final String IDENTITY_ATTRIBUTE = "identity";

   public static String getName(Node node)
   {
      return getNamedAttribute(node, NAME_ATTRIBUTE);
   }

   public static String getIdentifier(Node node)
   {
      return getNamedAttribute(node, IDENTITY_ATTRIBUTE);
   }
}
