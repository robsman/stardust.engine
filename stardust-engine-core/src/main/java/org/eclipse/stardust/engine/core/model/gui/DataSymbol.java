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

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.diagram.Stylesheet;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;


public class DataSymbol extends NamedSymbol
{
   private static boolean resourcesInitialized;

   private static Image image;
   private static int imageWidth = 10;
   private static int imageHeight = 10;

   private static String STYLE;
   private static int LEFT_MARGIN;
   private static int RIGHT_MARGIN;
   private static int TOP_MARGIN;
   private static int BOTTOM_MARGIN;
   private static int MARGIN;
   private static int EDGE_WIDTH;
   private static int EDGE_HEIGHT;

   private transient JMenuItem dataMappingItem;
   private transient JMenuItem traverseItem;

   /**
    * Initializes all constants and graphics resources from the stylesheet.
    */
   public static synchronized void initializeResources()
   {
      if (!resourcesInitialized)
      {
         STYLE = Stylesheet.instance().getString("Data", "style", "carnot");
         TOP_MARGIN = Stylesheet.instance().getInteger("Data", "top-margin", 20);
         BOTTOM_MARGIN = Stylesheet.instance().getInteger("Data", "bottom-margin", 20);
         LEFT_MARGIN = Stylesheet.instance().getInteger("Data", "left-margin", 20);
         RIGHT_MARGIN = Stylesheet.instance().getInteger("Data", "right-margin", 20);
         MARGIN = Stylesheet.instance().getInteger("Data", "margin", 10);
         EDGE_WIDTH = Stylesheet.instance().getInteger("Data", "edge-width", 10);
         EDGE_HEIGHT = Stylesheet.instance().getInteger("Data", "edge-height", 10);

         resourcesInitialized = true;
      }
   }

   public DataSymbol()
   {
      super("Data");
      initializeResources();
   }

   public DataSymbol(IData data)
   {
      super("Data");
      initializeResources();
      setData(data);
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
      DataSymbol _copy = new DataSymbol(getData());
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
   }

   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      popupMenu.addSeparator();

      dataMappingItem = new JMenuItem("Input/Output For");
      dataMappingItem.addActionListener(this);
      dataMappingItem.setMnemonic('e');
      popupMenu.add(dataMappingItem);

      popupMenu.addSeparator();

      traverseItem = new JMenuItem("Traverse ...");
      traverseItem.addActionListener(this);
      traverseItem.setMnemonic('s');
      popupMenu.add(traverseItem);
   }

   public void deleteAll()
   {
      if (getData() != null && getData().isPredefined())
      {
         JOptionPane.showMessageDialog(
               JOptionPane.getFrameForComponent(getDrawArea()),
               "Predefined data cannot be deleted!");
         return;
      }

      if (getData() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete the data  '" + getData().getName() +
            "' from the model.\n\n" +
            "This operation cannot be undone. Continue?", "Data Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();

         if (getData() != null)
         {
            getData().delete();
         }
      }
   }

   public void draw(Graphics g)
   {
      super.draw(g);

      Graphics2D graphics = (Graphics2D) g;

      if (STYLE.equalsIgnoreCase("bpmn"))
      {
         Stroke oldStroke = graphics.getStroke();
         Color oldColor = graphics.getColor();

         int[] _xPoints = new int[]{getX() + LEFT_MARGIN + getNameSymbol().getWidth() + RIGHT_MARGIN - EDGE_WIDTH,
                                    getX(),
                                    getX(),
                                    getX() + LEFT_MARGIN + getNameSymbol().getWidth() + RIGHT_MARGIN,
                                    getX() + LEFT_MARGIN + getNameSymbol().getWidth() + RIGHT_MARGIN};
         int[] _yPoints = new int[]{getY(),
                                    getY(),
                                    getY() + TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN,
                                    getY() + TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN,
                                    getY() + EDGE_HEIGHT};
         Polygon _body = new Polygon(_xPoints, _yPoints, 5);

         graphics.setColor(Color.white);
         graphics.fillPolygon(_body);
         graphics.setColor(PEN_COLOR);
         graphics.drawPolygon(_body);
         graphics.drawLine(getRight() - EDGE_WIDTH, getTop(), getRight() - EDGE_WIDTH, getTop() + EDGE_HEIGHT);
         graphics.drawLine(getRight() - EDGE_WIDTH, getTop() + EDGE_HEIGHT, getRight(), getTop() + EDGE_HEIGHT);

         graphics.setColor(oldColor);
         graphics.setStroke(oldStroke);
      }
      else
      {
         loadImage();
         graphics.setColor(PEN_COLOR);
         graphics.drawImage(image, getX() + (getWidth() - imageWidth) / 2, getY(), null);
      }
      getNameSymbol().setPoint(getX() + (getWidth() - getNameSymbol().getWidth()) / 2, getY() + MARGIN + imageHeight);
      getNameSymbol().draw(graphics);
   }

   /**
    * Show and edit the Properties
    */
   protected void editProperties()
   {
   }

   public IData getData()
   {
      return (IData) getUserObject();
   }

   public int getHeight()
   {
      if (STYLE.equalsIgnoreCase("carnot"))
      {
         return imageHeight + getNameSymbol().getHeight() + MARGIN;
      }
      else
      {
         return TOP_MARGIN + getNameSymbol().getHeight() + BOTTOM_MARGIN;
      }
   }

   public int getWidth()
   {
      if (STYLE.equalsIgnoreCase("carnot"))
      {
         return Math.max(getNameSymbol().getWidth(), imageWidth);
      }
      else
      {
         return LEFT_MARGIN + getNameSymbol().getWidth() + RIGHT_MARGIN;
      }
   }

   public void loadImage()
   {
      //if (image == null)
      //{
         try
         {
            ImageIcon icon;
            if (getData() != null)
            {
               icon = SymbolIconProvider.instance().getIcon(getData());
            }
            else
            {
               icon= new ImageIcon(DataSymbol.class.getResource("images/data_symbol.gif"));
            }

            image = icon.getImage();
            imageWidth = icon.getIconWidth();
            imageHeight = icon.getIconHeight();
         }
         catch (Exception x)
         {
            throw new PublicException(
                  "Resource 'images/data_symbol.gif' cannot be loaded.", x);
         }
      //}
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      super.preparePopupMenu();

      boolean _isWritable = (getDrawArea() != null) && (!getDrawArea().isReadOnly());

      dataMappingItem.setEnabled(_isWritable);
      traverseItem.setEnabled(_isWritable);
   }

   public void setData(IData data)
   {
      setUserObject(data);
   }

   public boolean setPoint(int x, int y)
   {
      if (getUserObject() == null)
      {
         IModel model = (IModel) ((ModelElement) getDrawArea().getUserObject()).getModel();
         String id = model.getDefaultDataId();
         setData(model.createData(id,
               model.findDataType(PredefinedConstants.PRIMITIVE_DATA), id, "", false, 0,
               JavaDataTypeUtils.initPrimitiveAttributes(Type.Integer, "0")));
      }
      return super.setPoint(x, y);
   }

   public void addConnectionsFromModel()
   {
      if (getUserObject() != null)
      {
         Iterator iterator = getDiagram().getAllNodes(ActivitySymbol.class);
         while (iterator.hasNext())
         {
            ActivitySymbol activitySymbol = (ActivitySymbol) iterator.next();
            activitySymbol.updateDataMappingConnection(this);
         }
      }
   }

   public String toString()
   {
      return "Data Symbol for " + getUserObject();
   }
}
