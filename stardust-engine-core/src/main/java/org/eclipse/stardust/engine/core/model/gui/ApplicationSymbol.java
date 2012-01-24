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
import java.util.Iterator;

import javax.swing.*;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author mgille
 */
public class ApplicationSymbol extends NamedSymbol
{
   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem executedByItem;
   private transient JMenuItem traverseItem;

   public ApplicationSymbol()
   {
      super("Application");
   }

   public ApplicationSymbol(IApplication application)
   {
      super("Application");
      setApplication(application);
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
      ApplicationSymbol copy = new ApplicationSymbol(getApplication());
      copy.setX(getX());
      copy.setY(getY());

      return copy;
   }

   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      popupMenu.addSeparator();

      executedByItem = new JMenuItem("Executed By");
      executedByItem.addActionListener(this);
      executedByItem.setMnemonic('e');
      popupMenu.add(executedByItem);

      popupMenu.addSeparator();
      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('s');
      popupMenu.add(traverseItem);
   }

   public void deleteAll()
   {
      if (getApplication() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete the application '" + getApplication().getName()
            + "'.\n\n" + "This operation cannot be undone. Continue?", "Application Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();

         if (getApplication() != null)
         {
            getApplication().delete();

            // @todo/hiob (ub) optimize
            if (getDiagram() != null)
            {
               Iterator activitySymbols = getDiagram().getAllNodes(ActivitySymbol.class);
               while (activitySymbols.hasNext())
               {
                  ActivitySymbol activitySymbol = (ActivitySymbol) activitySymbols.next();
                  activitySymbol.updateDataMappingConnections();
               }
            }
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

   public IApplication getApplication()
   {
      return (IApplication) getUserObject();
   }

   public int getHeight()
   {
      return imageHeight + getNameSymbol().getHeight() + MARGIN;
   }

   public int getWidth()
   {
      return Math.max(getNameSymbol().getWidth(), imageWidth);
   }

   public void loadImage()
   {
     // if (image == null)
     // {
         try
         {
            ImageIcon icon;
            if (getApplication()!= null)
            {
               icon = SymbolIconProvider.instance().getIcon(getApplication());
            }
            else
            {
            icon =
                  new ImageIcon(ApplicationSymbol.class.getResource(
                  "images/application_symbol.gif"));
            }
            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new InternalException("Can't load application symbol.", x);
         }
      //}
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      super.preparePopupMenu();
      executedByItem.setEnabled(_isWritable);
      traverseItem.setEnabled(_isWritable);
   }

   public void setApplication(IApplication application)
   {
      setUserObject(application);
   }

   public boolean setPoint(int x, int y)
   {
      if (getApplication() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultApplicationId();
         setApplication(model.createApplication(id, id, "", 0));
      }
      return super.setPoint(x, y);
   }

   public String toString()
   {
      return "Application Symbol for " + getUserObject();
   }
}
