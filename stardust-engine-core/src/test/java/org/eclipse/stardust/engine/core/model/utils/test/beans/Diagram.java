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

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.Connections;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author ubirkemeyer
 * @version $Revision: 5142 $
 */
public class Diagram extends IdentifiableElementBean
{
   private static final long serialVersionUID = -5125739873905459718L;

   private Link symbols = new Link(this, "Symbols");
   private Connections connections = new Connections(this, "Connections", "outConnections", "inConnections");

   private String id;

   Diagram() {}
   public Diagram(String id)
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

   public DiagramSymbol createSymbol(String id)
   {
      DiagramSymbol result = new DiagramSymbol(id);
      symbols.add(result);
      result.register(0);
      return result;
   }

   public DiagramConnection createConnection(String id, ModelElement first)
   {
      DiagramConnection result = new DiagramConnection(id, first);
      connections.add(result);
      result.register(0);
      return result;
   }

   public DiagramConnection createConnection(String id, ModelElement  first, ModelElement second)
   {
      DiagramConnection result = new DiagramConnection(id, first, second);
      connections.add(result);
      result.register(0);
      return result;
   }

   public long getSymbolsCount()
   {
      return symbols.size();
   }

   public long getConnectionsCount()
   {
      return connections.size();
   }

   public DiagramSymbol findSymbol(String id)
   {
      return (DiagramSymbol) symbols.findById(id);
   }

   public DiagramConnection findConnection(String id)
   {
      return (DiagramConnection) connections.findById(id);
   }
}
