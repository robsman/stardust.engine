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
package org.eclipse.stardust.engine.core.struct.sxml.xpath;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.dom.DOMPointerFactory;
import org.eclipse.stardust.engine.core.struct.sxml.Node;


public class SxmlNodePointerFactory implements NodePointerFactory
{

   public int getOrder()
   {
      return DOMPointerFactory.DOM_POINTER_FACTORY_ORDER;
   }

   public NodePointer createNodePointer(QName name, Object object, Locale locale)
   {
      return (object instanceof Node) ? new SxmlNodePointer((Node) object, locale) : null;
   }

   public NodePointer createNodePointer(NodePointer parent, QName name, Object object)
   {
      return (object instanceof Node) ? new SxmlNodePointer(parent, (Node) object) : null;
   }

}
