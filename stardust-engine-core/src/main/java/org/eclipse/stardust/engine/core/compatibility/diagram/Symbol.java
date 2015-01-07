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

import java.awt.*;
import java.awt.event.MouseEvent;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;



public interface Symbol extends ModelElement
{
   /**
    *
    */
   int getBottom();

   /**
    *
    */
   int getHeight();

   /**
     *
     */
   int getLeft();

   /**
     *
     */
   int getRight();

   /**
    *
    */
   int getTop();

   /**
     * Used during construction. As long as true is returned by this method, the
     * symbol needs more points for its definition.
     */
   boolean setPoint(int x, int y);

   /**
     * Creates a copy of the symbol without the associated data
     * Used for copies in diagrams. The new Symbol refers the same
     * associated data object.
     */
   Symbol copySymbol();

   /**
     *
     */
   void setSelected(boolean selected);

   /**
     * Sets the (model) information represented by the symbol.
     */
   void setUserObject(IdentifiableElement userObject);

   /*
     * Retrieves the (model) information represented by the symbol. If not set,
     * the method returns <code>null</code>.
     */
   Object getUserObject();

   /*
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   void userObjectChanged();

   /**
    *
    */
   void draw(Graphics graphics);

   /**
    *
    */
   boolean isContainedIn(int x1, int y1, int x2, int y2);

   /**
    *
    */
   boolean isHitBy(int x, int y);

   /**
     *
     */
   boolean getSelected();

   /**
    *
    */
   void mouseMoved(MouseEvent event);

   /**
    *
    */
   int getX();

   /**
     *
     */
   int getY();

   /**
     *
     */
   void move(int xDelta, int yDelta);

   /**
    *
    */
   void activatePopupMenu(DrawArea drawArea, int x, int y);

   /**
     *
     */
   int getWidth();

   /*
    *
    */
   void onDoubleClick(int x, int y);

   /**
     *
     */
   void onPress(MouseEvent event);

   /**
    *
    */
   void mouseDragged(MouseEvent event, int lastXDrag, int lastYDrag);

   /**
     * Removes the symbol from the drawing area and deletes a symbol completly
     */
   void deleteAll();

   /** */
   DrawArea getDrawArea();
}
