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

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */

public class DiagramConnection extends ConnectionBean implements Identifiable
{
   private static final long serialVersionUID = -3969749083049324365L;

   private String id;

   private List inConnections = null;
   @SuppressWarnings("unused")
   private List outConnections = null;

   public DiagramConnection()
   {

   }

   public DiagramConnection(String id, ModelElement  firstSymbol)
   {
      super(firstSymbol);
      this.id = id;
   }

   public DiagramConnection(String id, ModelElement first, ModelElement second)
   {
      super(first, second);
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
