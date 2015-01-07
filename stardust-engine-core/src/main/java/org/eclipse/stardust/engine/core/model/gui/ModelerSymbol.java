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

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModeler;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


public class ModelerSymbol extends NamedSymbol
{
   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem performsItem;
   private transient JMenuItem worksForItem;
   private transient JMenuItem traverseItem;

   public ModelerSymbol()
   {
      super("Modeler");
   }

   public ModelerSymbol(IModeler human)
   {
      super("Modeler");
      setModeler(human);
   }

   public void actionPerformed(ActionEvent event)
   {
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      ModelerSymbol copy = new ModelerSymbol(getModeler());
      copy.setX(getX());
      copy.setY(getY());

      return copy;
   }

   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      popupMenu.addSeparator();

      performsItem = new JMenuItem("Executes");
      performsItem.addActionListener(this);
      performsItem.setMnemonic('f');
      popupMenu.add(performsItem);

      worksForItem = new JMenuItem("Member Of");
      worksForItem.addActionListener(this);
      worksForItem.setMnemonic('m');
      popupMenu.add(worksForItem);

      popupMenu.addSeparator();

      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('s');
      popupMenu.add(traverseItem);
   }

   public void deleteAll()
   {
      if (getModeler() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea())
            , "You are going to delete the modeler '" + getModeler().getName() +
            "'.\n\n" +
            "This operation cannot be undone. Continue?", "Modeler Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         // delete the user object; it's deletion will notify all model listeners including
         // those who are in charge of cleaning up the symbols for the user object
         if (getModeler() != null)
         {
            getModeler().delete();
            setModeler(null);
         }
      }
   }

   public void draw(Graphics g)
   {
      super.draw(g);

      Graphics2D graphics = (Graphics2D) g;
      loadImage();
      graphics.setColor(PEN_COLOR);
      graphics.drawImage(image, getX() + (getWidth() - imageWidth) / 2, getY(), null);
      getNameSymbol().setPoint(getX() + (getWidth() - getNameSymbol().getWidth()) / 2, getY() + MARGIN + imageHeight);
      getNameSymbol().draw(graphics);
   }

   /**
    * Show and edit the Properties
    */
   protected void editProperties()
   {
   }

   public int getHeight()
   {
      return imageHeight + getNameSymbol().getHeight() + MARGIN;
   }

   public IModeler getModeler()
   {
      return (IModeler) getUserObject();
   }

   public int getWidth()
   {
      return Math.max(getNameSymbol().getWidth(), imageWidth);
   }

   public void loadImage()
   {
      if (image == null)
      {
         try
         {
            ImageIcon icon = new ImageIcon(ModelerSymbol.class.getResource("images/modeler_symbol.gif"));

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new InternalException("Can't load modeler symbol.", x);
         }
      }
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      super.preparePopupMenu();

      performsItem.setEnabled(!isReadOnly());
      worksForItem.setEnabled(!isReadOnly());
      traverseItem.setEnabled(!isReadOnly());
   }

   public void setModeler(IModeler modeler)
   {
      setUserObject(modeler);
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultRoleId();
         setModeler(model.createModeler(id, id, "", "", 0));
      }
      return super.setPoint(x, y);
   }

   public String toString()
   {
      return "Modeler Symbol for " + getUserObject();
   }
}
