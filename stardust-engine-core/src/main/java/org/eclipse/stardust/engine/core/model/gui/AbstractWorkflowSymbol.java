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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ILinkType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.compatibility.diagram.AbstractNodeSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.DrawArea;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;


/**
 * @todo Insert the type's description here.
 */
public abstract class AbstractWorkflowSymbol extends AbstractNodeSymbol
{
   private static final Logger trace = LogManager.getLogger(AbstractWorkflowSymbol.class);

   protected static final int MARGIN = 10;
   protected static final Color PEN_COLOR = Color.black;
   protected static final Color FILL_COLOR = new Color(0.9f, 0.9f, 0.9f);

   private transient JMenuItem annotatedByItem;
   private transient JMenuItem createAnotationItem;
   private transient JMenuItem genericLinkMenu;
   private transient JMenuItem removeSymbolItem;
   private transient JMenuItem deleteAllItem;
   private transient JMenuItem propertiesItem;

   public abstract String getName();

   /**
    * inner class for a specialized action, used for the GenericLink-Menu
    */
   protected class CreateGenericLinkAction extends AbstractAction
   {
      protected ILinkType linkType;

      public CreateGenericLinkAction(ILinkType linkType)
      {
         super(linkType.getName());
         this.linkType = linkType;
      }

      public void actionPerformed(ActionEvent event)
      {
         getDrawArea().startConnectionDefinition(new GenericLinkConnection(linkType
               , AbstractWorkflowSymbol.this));
      }
   }

   /**
    * AbstractWorkflowSymbol constructor comment.
    */
   public AbstractWorkflowSymbol()
   {
      super();
   }

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
      else if (event.getSource() == propertiesItem)
      {
         editProperties();
      }
   }

   /**
    * The implementation calls everytime the method createPopupMenu()
    *
    * Method was overritten to support dynamic menu content for GenericLink submenu
    *
    */
   public void activatePopupMenu(DrawArea drawArea, int x, int y)
   {
      // delete the existing menuitems
      setPopupMenu(null);

      // create the new menu
      createPopupMenu();

      if (getPopupMenu() != null)
      {
         preparePopupMenu();

         // @test -------------------------
         trace.debug("===[ activatePopupMenu() ]===");
         GUI.showPopup(getPopupMenu(), drawArea, x, y);
      }
   }

   public void createPopupMenu()
   {
      JPopupMenu popupMenu = new JPopupMenu();
      setPopupMenu(popupMenu);

      annotatedByItem = new JMenuItem("Annotated by");
      annotatedByItem.addActionListener(this);
      annotatedByItem.setMnemonic('a');
      popupMenu.add(annotatedByItem);

      createAnotationItem = new JMenuItem("Create Annotation");
      createAnotationItem.addActionListener(this);
      createAnotationItem.setMnemonic('c');
      popupMenu.add(createAnotationItem);

      popupMenu.addSeparator();

      removeSymbolItem = new JMenuItem("Remove symbol");
      removeSymbolItem.setMnemonic('r');
      removeSymbolItem.addActionListener(this);
      popupMenu.add(removeSymbolItem);

      deleteAllItem = new JMenuItem("Delete all");
      deleteAllItem.setMnemonic('a');
      deleteAllItem.addActionListener(this);
      popupMenu.add(deleteAllItem);

      popupMenu.addSeparator();

      propertiesItem = new JMenuItem("Properties ...");
      propertiesItem.addActionListener(this);
      propertiesItem.setMnemonic('e');
      popupMenu.add(propertiesItem);

      genericLinkMenu = getGenericLinkMenu();
      if (genericLinkMenu != null)
      {
         popupMenu.addSeparator();
         popupMenu.add(genericLinkMenu);
      }
   }

   /**
    * Show and edit the Properties
    */
   abstract protected void editProperties();

   /**
    * Creates an Menu for all valid GenericLinks. If no such GenericLink exist
    * a disabled Menu is returned.
    *
    * A GenericLink is valid, if its SourceClass is assignable by the UserObject
    * of the Symbol.
    *
    * @return javax.swing.JMenuItem
    */
   protected JMenuItem getGenericLinkMenu()
   {
      JMenu _menu = null;
      Iterator _linkTypeList = null;
      ILinkType _linkType = null;

      if ((getModel() != null) && (getUserObject() != null))
      {
         _linkTypeList = ((IModel)getModel()).getAllLinkTypesForType(getUserObject().getClass());
         _menu = new JMenu("Generic Link");

         if ((_linkTypeList != null) && (_linkTypeList.hasNext()))
         {

            while (_linkTypeList.hasNext())
            {
               try
               {
                  _linkType = (ILinkType) _linkTypeList.next();
                  if (_linkType.getFirstClass().isAssignableFrom(getUserObject().getClass()))
                  {
                     _menu.add(new CreateGenericLinkAction(_linkType));
                  }
               }
               catch (Exception _ex)
               {
               }
            }
         }
         else
         {
            _menu.setEnabled(false);
         }
      }

      return _menu;
   }

   public void onDoubleClick(int x, int y)
   {
      editProperties();
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
      if (genericLinkMenu != null)
      {
         // hint: item could be disabled if there doesn't exist
         // 		any GenericLinkType for this modelelement
         genericLinkMenu.setEnabled(_isWritable && genericLinkMenu.isEnabled());
      }
      removeSymbolItem.setEnabled(_isWritable);
      deleteAllItem.setEnabled(_isWritable);
      //		propertiesItem.setEnabled(true);	 // item is always enabled
   }

   public void addConnectionsFromModel()
   {
   }
}
