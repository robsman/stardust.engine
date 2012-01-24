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

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.stardust.common.SplicingIterator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Diagram extends ModelElement implements SymbolOwner
{
   private String name;
   private Vector nodes = new Vector();
   private Vector connections = new Vector();

   public Diagram(String name, int elementOID, Model model)
   {
      this.name = name;
      model.register(this, elementOID);
   }

   public void addToNodes(NodeSymbol symbol)
   {
      nodes.add(symbol);
   }

   public void addToConnections(Connection connection)
   {
      connections.add(connection);
   }

   public Iterator getAllSymbols()
   {
      return new SplicingIterator(nodes.iterator(), connections.iterator());
   }

   public String getName()
   {
      return name;
   }
}
