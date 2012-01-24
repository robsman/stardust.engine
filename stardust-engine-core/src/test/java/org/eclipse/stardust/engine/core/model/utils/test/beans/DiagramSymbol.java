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
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import java.util.List;

import javax.swing.*;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */
public class DiagramSymbol extends  IdentifiableElementBean
{
   private static final long serialVersionUID = -3424624999692860210L;

   private String id;

   private List inConnections = null;
   @SuppressWarnings("unused")
   private List outConnections = null;

   DiagramSymbol() {}

   public DiagramSymbol(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public ImageIcon getIcon()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public long getInConnectionsCount()
   {
      return inConnections == null ? 0 : inConnections.size();
   }
}
