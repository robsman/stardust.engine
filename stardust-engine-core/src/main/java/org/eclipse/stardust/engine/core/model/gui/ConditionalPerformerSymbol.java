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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * A symbol representing a conditional performer.
 */
public class ConditionalPerformerSymbol extends NamedSymbol
{
   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem performsItem;

   public ConditionalPerformerSymbol()
   {
      super("Performer");
   }

   public ConditionalPerformerSymbol(IConditionalPerformer performer)
   {
      super("Performer");
      Assert.isNotNull(performer, "Condional Performer is not null.");

      setPerformer(performer);
   }

   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == performsItem)
      {
         getDrawArea().startConnectionDefinition(new PerformsConnection(this));
      }
      else
      {
         super.actionPerformed(event);
      }
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      ConditionalPerformerSymbol _copy = new ConditionalPerformerSymbol(getPerformer());
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
   }

   public void deleteAll()
   {
      if (getPerformer() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea())
            , "You are going to delete the conditional perfomer \"" + getPerformer().getName() +
            "\".\n\n" +
            "This operation cannot be undone. Continue?", "Conditional Performer Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         // delete the user object; it's deletion will notify all model listeners including
         // those who are in charge of cleaning up the symbols for the user object
         if (getPerformer() != null)
         {
            getPerformer().delete();
            setPerformer(null);
         }
      }
   }

   /**
    * Show and edit the Properties
    */
   protected void editProperties()
   {
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

   public int getHeight()
   {
      return imageHeight + getNameSymbol().getHeight() + MARGIN;
   }

   public IConditionalPerformer getPerformer()
   {
      return (IConditionalPerformer) getUserObject();
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
            ImageIcon icon = new ImageIcon(ConditionalPerformerSymbol.class.getResource("images/conditional_symbol.gif"));

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new PublicException(
                  BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE
                        .raise("images/conditional_symbol.gif"));
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
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultConditionalPerformerId();
         setPerformer(model.createConditionalPerformer(id, id, "", null, 0));

      }
      return super.setPoint(x, y);
   }

   public void setPerformer(IConditionalPerformer performer)
   {
      setUserObject(performer);
   }

   public void addConnectionsFromModel()
   {
      if (getUserObject() != null)
      {
         // check for executes

         Iterator iterator = getDiagram().getAllNodes(ActivitySymbol.class);
         if (iterator != null)
         {
            while (iterator.hasNext())
            {
               ActivitySymbol searchedSymbol = (ActivitySymbol) iterator.next();
               if (searchedSymbol.getActivity() != null)
               {
                  IActivity activity = searchedSymbol.getActivity();

                  if ((activity.getPerformer() != null) && (getPerformer().getId().equals(activity.getPerformer().getId())))
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
      return "Conditional Performer Symbol for " + getUserObject();
   }
}
