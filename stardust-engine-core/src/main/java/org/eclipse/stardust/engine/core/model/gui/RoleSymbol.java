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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * A symbol representing role workflow participants.
 */
public class RoleSymbol extends NamedSymbol
{
   private static final String ADMINISTRATOR = "Administrator";

   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem performsItem;
   private transient JMenuItem worksForItem;
   private transient JMenuItem traverseItem;

   public RoleSymbol()
   {
      super("Role");
   }

   public RoleSymbol(IRole role)
   {
      super("Role");
      setRole(role);
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
      RoleSymbol copy = new RoleSymbol(getRole());
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

      worksForItem = new JMenuItem("Works For");
      worksForItem.addActionListener(this);
      worksForItem.setMnemonic('w');
      popupMenu.add(worksForItem);

      popupMenu.addSeparator();

      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('s');
      popupMenu.add(traverseItem);
   }

   public void deleteAll()
   {
      // @todo (egypt): removal prevention of predefined stuff should be more robust.
      /*if (ADMINISTRATOR.equals(getRole().getId()))
      {
         JOptionPane.showMessageDialog(
               JOptionPane.getFrameForComponent(getDrawArea()),
               "The predefined role 'Administrator' cannot be deleted!");
         return;

      }*/
      if (getRole() != null && getRole().isPredefined())
      {
         JOptionPane.showMessageDialog(
               JOptionPane.getFrameForComponent(getDrawArea()),
               "The predefined role '" + getRole().getId() + "' cannot be deleted!");
         return;
      }

      if (getRole() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete the role \"" + getRole().getName() +
            "\".\n\n" +
            "This operation cannot be undone. Continue?", "Role Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         // delete the user object; it's deletion will notify all model
         // listeners including those who are in charge of cleaning up
         // the symbols for the user object

         if (getRole() != null)
         {
            getRole().delete();
            setRole(null);
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

   public IRole getRole()
   {
      return (IRole) getUserObject();
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
            ImageIcon icon = new ImageIcon(RoleSymbol.class.getResource("images/role_symbol.gif"));

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new PublicException(
                  BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE
                        .raise("images/role_symbol.gif"));
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

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultRoleId();
         setRole(model.createRole(id, id, "", 0));
      }
      return super.setPoint(x, y);
   }

   public void setRole(IRole role)
   {
      setUserObject(role);
   }

   public void addConnectionsFromModel()
   {
      if (getUserObject() != null)
      {
         // check for works for
         Iterator iterator = getRole().getAllOrganizations();
         if (iterator != null)
         {
            while (iterator.hasNext())
            {
               Symbol searchedSymbol = getDiagram().findSymbolForUserObject(iterator.next());
               if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(this, searchedSymbol, WorksForConnection.class, true)))
               {
                  ConnectionSymbol _addedConnection = new WorksForConnection(this);
                  _addedConnection.setSecondSymbol(searchedSymbol, false);
                  getDiagram().addToConnections(_addedConnection, 0);
                  //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
               }
            }
         }

         // check for executes
         iterator = getDiagram().getAllNodes(ActivitySymbol.class);
         if (iterator != null)
         {
            while (iterator.hasNext())
            {
               ActivitySymbol searchedSymbol = (ActivitySymbol) iterator.next();
               if (searchedSymbol.getActivity() != null)
               {
                  IActivity activity = searchedSymbol.getActivity();

                  if ((activity.getPerformer() != null) && (getRole().getId().equals(activity.getPerformer().getId())))
                  {
                     if (!getDiagram().existConnectionBetween(this, searchedSymbol, PerformsConnection.class, true))
                     {
                        ConnectionSymbol _addedConnection = new PerformsConnection(this);
                        _addedConnection.setSecondSymbol(searchedSymbol, false);
                        getDiagram().addToConnections(_addedConnection, 0);
                        //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
                     }
                  }
               }
            }
         }
      }
   }

   public String toString()
   {
      return "Role Symbol for " + getUserObject();
   }
}
