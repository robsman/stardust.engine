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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * A symbol representing an organization.
 */
public class OrganizationSymbol extends NamedSymbol
{
   private static final Logger trace = LogManager.getLogger(OrganizationSymbol.class);

   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem performsItem;
   private transient JMenuItem partOfItem;
   private transient JMenuItem traverseItem;

   public OrganizationSymbol()
   {
      super("Organization");
   }

   public OrganizationSymbol(IOrganization organization)
   {
      super("Organization");
      setOrganization(organization);
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
      OrganizationSymbol _copy = new OrganizationSymbol(getOrganization());
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
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

      partOfItem = new JMenuItem("Part Of");
      partOfItem.addActionListener(this);
      partOfItem.setMnemonic('t');
      popupMenu.add(partOfItem);

      popupMenu.addSeparator();

      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('s');
      popupMenu.add(traverseItem);
   }

   public void deleteAll()
   {
      if (getOrganization() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea())
            , "You are going to delete the organization \"" + getOrganization().getName() +
            "\".\n\n" +
            "This operation cannot be undone. Continue?", "Organization Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         // delete the user object; it's deletion will notify all model listeners including
         // those who are in charge of cleaning up the symbols for the user object
         if (getOrganization() != null)
         {
            getOrganization().delete();
            setOrganization(null);
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

   public IOrganization getOrganization()
   {
      return (IOrganization) getUserObject();
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
            ImageIcon icon = new ImageIcon(OrganizationSymbol.class.getResource(
                  "images/organization_symbol.gif"));

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            trace.warn("Cannot load resource \"images/organization_symbol.gif\".", x);
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

      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      performsItem.setEnabled(_isWritable);
      partOfItem.setEnabled(_isWritable);
      traverseItem.setEnabled(_isWritable);
   }

   public void setOrganization(IOrganization organization)
   {
      setUserObject(organization);
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultOrganizationId();
         setOrganization(model.createOrganization(id, id, "", 0));
      }
      return super.setPoint(x, y);
   }

   public void addConnectionsFromModel()
   {
      if (getUserObject() == null)
      {
         return;
      }
      // check for part of connection	(for SubOrganizations)
      Iterator iterator = getOrganization().getSubOrganizations();
      while (iterator.hasNext())
      {
         IOrganization _participant = (IOrganization) iterator.next();
         Symbol searchedSymbol = getDiagram().findSymbolForUserObject(_participant);

         if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(searchedSymbol, this, PartOfConnection.class, true)))
         {
            ConnectionSymbol _addedConnection = new PartOfConnection((OrganizationSymbol) searchedSymbol);
            _addedConnection.setSecondSymbol(this, false);
            getDiagram().addToConnections(_addedConnection, 0);
            //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
         }
      }

      // check for part of connection (for SuperOrganizations)

      iterator = getOrganization().getAllOrganizations();

      while (iterator.hasNext())
      {
         IOrganization _participant = (IOrganization) iterator.next();
         Symbol searchedSymbol = getDiagram().findSymbolForUserObject(_participant);

         if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(this, searchedSymbol, PartOfConnection.class, true)))
         {
            ConnectionSymbol _addedConnection = new PartOfConnection(this);
            _addedConnection.setSecondSymbol(searchedSymbol, false);
            getDiagram().addToConnections(_addedConnection, 0);
            //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
         }
      }
      // check for works for connection
      iterator = getOrganization().getAllParticipants();

      if (iterator != null)
      {
         while (iterator.hasNext())
         {
            IModelParticipant _participant = (IModelParticipant) iterator.next();
            Symbol searchedSymbol = getDiagram().findSymbolForUserObject(_participant);

            if ((searchedSymbol != null) && (!getDiagram().existConnectionBetween(searchedSymbol, this, WorksForConnection.class, true)))
            {
               ConnectionSymbol _addedConnection = null;
               if (searchedSymbol instanceof RoleSymbol)
               {
                  _addedConnection = new WorksForConnection((RoleSymbol) searchedSymbol);
               }
               else if (searchedSymbol instanceof OrganizationSymbol)
               {
                  // hint: Suborganizations and other participants are
                  //			stored in the same collection but use different
                  //			connections (part-of and works-for).
                  // 		The works-for-connections are already handled.
                  // intentionally left empty.
                  _addedConnection = null;
               }
               else
               {
                  Assert.lineNeverReached();
               }

               if (_addedConnection != null)
               {
                  _addedConnection.setSecondSymbol(this, false);
                  getDiagram().addToConnections(_addedConnection, 0);
                  //getDrawArea().fireUndoableEdit(new CreateSymbolEdit(_addedConnection));
               }
            }
         }
      }

      // check for executes
      // hint: Remember a organization doesn't know anything about its performed
      //			activity.
      //			So it's not so easy to find the related Activity in the model.
      //@optimize ... use a better algorithm to find the related activities
      iterator = getDiagram().getAllNodes(ActivitySymbol.class);

      if (iterator != null)
      {
         while (iterator.hasNext())
         {
            ActivitySymbol searchedSymbol = (ActivitySymbol) iterator.next();
            if (searchedSymbol.getActivity() != null)
            {
               IActivity activity = searchedSymbol.getActivity();

               if ((activity.getPerformer() != null) && (getOrganization().getId().equals(activity.getPerformer().getId())))
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

   public String toString()
   {
      return "Organization Symbol for " + getUserObject();
   }

}
