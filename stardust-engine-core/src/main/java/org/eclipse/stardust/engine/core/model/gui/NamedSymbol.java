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
package org.eclipse.stardust.engine.core.model.gui;

import java.awt.event.MouseEvent;
import java.awt.Cursor;

import org.eclipse.stardust.engine.core.compatibility.diagram.TextSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.TextSymbolEvent;
import org.eclipse.stardust.engine.core.compatibility.diagram.TextSymbolListener;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;


public abstract class NamedSymbol extends AbstractWorkflowSymbol
      implements TextSymbolListener
{
   private SingleRef userObject;
   private transient TextSymbol nameSymbol;

   public NamedSymbol(String name)
   {
      userObject = new SingleRef(this, name);
   }

   public String getName()
   {
      if (userObject.isEmpty())
      {
         return "";
      }
      return ((IdentifiableElement) userObject.getElement()).getName();
   }

   public void textSymbolChanged(TextSymbolEvent event)
   {
   }

   public void textSymbolDeselected(TextSymbolEvent event)
   {
      if (!userObject.isEmpty())
      {
         IdentifiableElement ie = (IdentifiableElement) userObject.getElement();
         ie.setName(nameSymbol.getText());
      }
   }

   public Object getUserObject()
   {
      return userObject.getElement();
   }

   public boolean isReadOnly()
   {
      return getDrawArea().isReadOnly() ||
            getUserObject() instanceof ModelElement &&
            ((ModelElement) getUserObject()).isPredefined();
   }

   public void mouseMoved(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         if (getNameSymbol().isHitBy(event.getX(), event.getY()))
         {
            getNameSymbol().mouseMoved(event);
         }
         else
         {
            event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         }
      }
   }

   public void onPress(MouseEvent event)
   {
      getNameSymbol().onPress(event);
   }

   public void setUserObject(IdentifiableElement userObject)
   {
      this.userObject.setElement(userObject);
      refreshFromModel();
   }

   public boolean setPoint(int x, int y)
   {
      setX(x);
      setY(y);
      return false;
   }

   /*
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   public void userObjectChanged()
   {
      refreshFromModel();
   }

   /**
    * Name symbols may have changed.
    */
   public void refreshFromModel()
   {
   }

   protected TextSymbol getNameSymbol()
   {
      if (nameSymbol == null)
      {
         nameSymbol = new TextSymbol(getName());
         nameSymbol.setParent(this);
         nameSymbol.addListener(this);
      }
      return nameSymbol;
   }
}
