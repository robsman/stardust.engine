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



/** */
public interface NodeSymbol extends Symbol
{
   /**
     */
    public boolean isDeleted();

   /**
     * Markes the Symbol as deleted
     */
    public void markAsDeleted();

   /*
     * Called before a popup menu is activated and may be used to enable or
     * disable menu items according to the state of the represented object.
     */
    public void preparePopupMenu();

   /**
     *
     */
    public void setX(int x);

    /**
     *
     */
    public void setY(int y);

   void setDiagram(Diagram diagram);

   Diagram getDiagram();
}
