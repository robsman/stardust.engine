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
package org.eclipse.stardust.engine.core.compatibility.diagram;

import java.util.Iterator;
import java.awt.Dimension;

import javax.swing.*;

import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 *
 */
public interface Diagram extends ModelElement, SymbolOwner
{
   /**
    *
    */
   public void addToNodes(NodeSymbol symbol, int elementOID);

   /**
    * Returns true if the symbol is contained in the diagram otherwise
    * false is returned.
    */
   public boolean contains(NodeSymbol symbol);

   /**
    *
    */
   public void dumpToJPEGFile(String filePath);

   /**
    * Returns <code>true</code> if at least one connection exist between
    * the two symbols
    */
   public boolean existConnectionBetween(NodeSymbol symbol1, NodeSymbol symbol2);

   /**
    * Returns <code>true</code> if at least one connection exist between
    * the two symbols. The connection must be a instance of the class <code>connectionType</code>.
    * If the parameter <code>uniDirectional</code> is <code>true</code> only
    * connection from <code>symbol1</code> to <code>symbol2</code> will be found.
    */
   public boolean existConnectionBetween(Symbol symbol1, Symbol symbol2
         , Class connectionType
         , boolean uniDirectional);

   /**
    * Returns an Iterator of connections corresponding to the two symbols.
    * 
    * @see #existConnectionBetween
    */
   public Iterator getExistingConnectionsBetween(Symbol symbol1, Symbol symbol2
         , Class connectionType
         , boolean uniDirectional);

   /**
    * Returns the first symbol that use the <code>searchedObject</code> as its
    * userobject.
    */
   public Symbol findSymbolForUserObject(Object searchedObject);

   /**
    * Returns  the draw area, the diagram is currently attached to.
    */
   public DrawArea getDrawArea();

   /*
    * Returns an icon to represent the diagram in a tree view or the like.
    */
   public ImageIcon getIcon();

   public String getName();

   /**
    *
    */
   public double getScale();

   /**
    *
    */
   public int getXTranslate();

   /**
    *
    */
   public int getYTranslate();

   /**
    * Initializes all symbols of the the diagram for drawing.
    */
   public void setDrawArea(DrawArea drawArea);

   /**
    *
    */
   public void setName(String name);

   /**
    *
    */
   public void setXTranslate(int xTranslate);

   /**
    *
    */
   public void setYTranslate(int yTranslate);

   /**
    * Forces all symbols representing the user object <code>userObject</code>
    * to adjust their appearance according to the changes.
    */
   public void userObjectChanged(Object userObject);

   /**
    * Removes all symbols representing the user object <code>userObject</code>.
    */
   public void userObjectDeleted(Object userObject);

   /**
    * Removes all connections representing the connection <code>firstUserObject</code> and
    * <code>secondUserObject</code>.
    * <p/>
    * Note, that this methode currently assumes, that there exists only one connection between
    * two symbols. This may be enhanced in the future.
    */
   public void userObjectsUnlinked(Object firstUserObject, Object secondUserObject);

   /**
    *
    */
   public Symbol findSymbolForUserObject(Class userObjectType, int userObjectOID);

   Iterator getAllNodes(Class type);

   void removeFromNodes(NodeSymbol symbol);

   void removeFromConnections(ConnectionSymbol connection);

   void addToConnections(ConnectionSymbol connection, int elementOID);

   Iterator getAllConnections(Class type);

   Iterator getAllNodeSymbols();

   Iterator getAllConnections();

   String getId();

   Dimension getMaximumSize();

}
