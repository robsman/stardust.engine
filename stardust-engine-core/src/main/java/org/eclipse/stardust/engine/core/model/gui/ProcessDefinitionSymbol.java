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
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;


public class ProcessDefinitionSymbol extends NamedSymbol
{
   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private transient JMenuItem subProcessOfItem;

   public ProcessDefinitionSymbol()
   {
      super("Process Definition");
   }

   public ProcessDefinitionSymbol(IProcessDefinition processDefinition)
   {
      super("Process Definition");
      setProcessDefinition(processDefinition);
   }

   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == subProcessOfItem)
      {
         getDrawArea().startConnectionDefinition(new SubProcessOfConnection(this));
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
      ProcessDefinitionSymbol _copy = new ProcessDefinitionSymbol(getProcessDefinition());
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
   }

   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      popupMenu.addSeparator();

      subProcessOfItem = new JMenuItem("Subprocess Of");
      subProcessOfItem.addActionListener(this);
      subProcessOfItem.setMnemonic('p');
      popupMenu.add(subProcessOfItem);
   }

   public void deleteAll()
   {
      if (getProcessDefinition() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea())
            , "You are going to delete the process definition  \"" + getProcessDefinition().getName() +
            "\" from the model.\n\n" +
            "This operation cannot be undone. Continue?", "Process Definition Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();
         if (getProcessDefinition() != null)
         {
            getProcessDefinition().delete();
            setProcessDefinition(null);
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

   public IProcessDefinition getProcessDefinition()
   {
      return (IProcessDefinition) getUserObject();
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
            ImageIcon icon = new ImageIcon(ProcessDefinitionSymbol.class.getResource("images/process_symbol.gif"));

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new InternalException("Can't load process symbol.", x);
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

      subProcessOfItem.setEnabled(!isReadOnly());
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement)getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultProcessDefinitionId();
         setProcessDefinition(model.createProcessDefinition(id, id, ""));
      }
      return super.setPoint(x, y);
   }

   public void setProcessDefinition(IProcessDefinition process)
   {
      setUserObject(process);
   }

   public void addConnectionsFromModel()
   {
   }

   public String toString()
   {
      return "Process Definition Symbol for " + getUserObject();
   }
}
