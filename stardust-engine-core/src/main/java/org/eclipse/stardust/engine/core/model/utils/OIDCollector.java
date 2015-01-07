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
package org.eclipse.stardust.engine.core.model.utils;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class OIDCollector implements Collector
{
   private RootElement root;

   public OIDCollector(RootElement root)
   {
      this.root = root;
   }

   public void collect(ModelElement source, ModelElement target)
   {
   }

   public ModelElement findInTarget(ModelElement source)
   {
      return root.lookupElement(source.getElementOID());
   }

   public Object getLocalId(ModelElement element)
   {
      return new Integer(element.getElementOID());
   }
}
