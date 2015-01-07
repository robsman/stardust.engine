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

import java.awt.event.ActionEvent;

import javax.swing.*;

import org.eclipse.stardust.engine.core.compatibility.diagram.PathConnection;


/**
 * @todo Insert the type's description here.
 */
public abstract class AbstractWorkflowPathConnection extends PathConnection
{
   private transient JMenuItem annotatedByItem;
   private transient JMenuItem createAnotationItem;
   private transient JMenuItem removeSymbolItem;
   private transient JMenuItem deleteAllItem;

   /**
    * AbstractWorkflowLineConnection constructor comment.
    */
   public AbstractWorkflowPathConnection()
   {
      super();
   }

   /** */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == annotatedByItem)
      {
         getDrawArea().startConnectionDefinition(new RefersToConnection(this));
      }
      else if (event.getSource() == createAnotationItem)
      {
         getDrawArea().startSymbolDefinition(new AnnotationSymbol("", this));
      }
      else if (event.getSource() == removeSymbolItem)
      {
         delete();
      }
      else if (event.getSource() == deleteAllItem)
      {
         deleteAll();
      }
      else
      {
         super.actionPerformed(event);
      }
   }

   /** */
   public void createPopupMenu()
   {
      JPopupMenu _popupMenu = null;

      super.createPopupMenu();
      if (getPopupMenu() == null)
      {
         _popupMenu = new JPopupMenu();
         setPopupMenu(_popupMenu);
      }
      else
      {
         _popupMenu = getPopupMenu();
         _popupMenu.addSeparator();
      }

      annotatedByItem = new JMenuItem("Annotated by");
      annotatedByItem.addActionListener(this);
      annotatedByItem.setMnemonic('a');
      _popupMenu.add(annotatedByItem);

      createAnotationItem = new JMenuItem("Create Annotation");
      createAnotationItem.addActionListener(this);
      createAnotationItem.setMnemonic('c');
      _popupMenu.add(createAnotationItem);

      _popupMenu.addSeparator();

      removeSymbolItem = new JMenuItem("Remove symbol");
      removeSymbolItem.setMnemonic('r');
      removeSymbolItem.addActionListener(this);
      _popupMenu.add(removeSymbolItem);

      deleteAllItem = new JMenuItem("Delete all");
      deleteAllItem.setMnemonic('a');
      deleteAllItem.addActionListener(this);
      _popupMenu.add(deleteAllItem);

   }

   /*
    * Called before a popup menu is activated and is used to enable or
    * disable menu items according to the state of the activity.
    */
   public void preparePopupMenu()
   {
      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      annotatedByItem.setEnabled(_isWritable);
      createAnotationItem.setEnabled(_isWritable);
      removeSymbolItem.setEnabled(_isWritable);
      deleteAllItem.setEnabled(_isWritable);
   }
}
