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
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;


/**
 *
 */
public class DataMappingConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static private final String STRING_CONNECTION_NAME = "DataMappingConnection";

   private SingleRef data = new SingleRef(this, "Data");
   private SingleRef activity = new SingleRef(this, "Activity");

   private Direction direction;

   private transient JMenuItem propertiesItem;
   private JRadioButtonMenuItem inItem;
   private JRadioButtonMenuItem outItem;
   private JRadioButtonMenuItem inoutItem;

   DataMappingConnection()
   {
   }

   public DataMappingConnection(IData data, IActivity activity, Direction direction)
   {
      this.direction = direction;
      this.data.setElement(data);
      this.activity.setElement(activity);
   }

   /**
    *
    */
   public DataMappingConnection(org.eclipse.stardust.engine.api.model.IData data, IActivity activity)
   {
      this.data.setElement(data);
      this.activity.setElement(activity);

      refreshFromModel();
   }

   /**
    *
    */
   public DataMappingConnection(DataSymbol firstSymbol)
   {
      direction = Direction.IN;
      data.setElement(firstSymbol.getData());
      setFirstSymbol(firstSymbol);
   }

   public DataMappingConnection(ActivitySymbol firstSymbol)
   {
      direction = Direction.OUT;
      activity.setElement(firstSymbol.getActivity());
      setFirstSymbol(firstSymbol);
   }

   public void actionPerformed(ActionEvent event)
   {
   }

   private IData getData()
   {
      return (IData) data.getElement();
   }

   private IActivity getActivity()
   {
      return (IActivity) activity.getElement();
   }

   public void updateDirection(Direction direction)
   {
      if (this.direction == direction)
      {
         return;
      }
      this.direction = direction;
      updateArrows();
   }

   private void updateArrows()
   {
      if (getFirstSymbol() instanceof ActivitySymbol)
      {
         if (Direction.IN == direction)
         {
            setFirstArrow(ArrowKey.FILLED_TRIANGLE);
            setSecondArrow(ArrowKey.NO_ARROW);
         }
         else if (Direction.OUT == direction)
         {
            setFirstArrow(ArrowKey.NO_ARROW);
            setSecondArrow(ArrowKey.FILLED_TRIANGLE);
         }
         else if (Direction.IN_OUT == direction)
         {
            setFirstArrow(ArrowKey.FILLED_TRIANGLE);
            setSecondArrow(ArrowKey.FILLED_TRIANGLE);
         }
      }
      else
      {
         if (Direction.IN == direction)
         {
            setFirstArrow(ArrowKey.NO_ARROW);
            setSecondArrow(ArrowKey.FILLED_TRIANGLE);
         }
         else if (Direction.OUT == direction)
         {
            setFirstArrow(ArrowKey.FILLED_TRIANGLE);
            setSecondArrow(ArrowKey.NO_ARROW);
         }
         else if (Direction.IN_OUT == direction)
         {
            setFirstArrow(ArrowKey.FILLED_TRIANGLE);
            setSecondArrow(ArrowKey.FILLED_TRIANGLE);
         }
      }
      if (getDrawArea() != null)
      {
         getDrawArea().repaint();
      }

   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      return new DataMappingConnection(getData(), getActivity());
   }

   private Iterator findSimilarConnection()
   {
      Vector connections = new Vector();
      if (getDiagram() != null)
      {
         Iterator itr = getDiagram().getAllConnections(DataMappingConnection.class);
         while (itr.hasNext())
         {
            DataMappingConnection connection = (DataMappingConnection) itr.next();
            if (getActivity().equals(connection.getActivity()) && getData().equals(connection.getData()))
            {
               connections.add(connection);
            }
         }
      }
      return connections.iterator();
   }

   /** */
   public void createPopupMenu()
   {
      super.createPopupMenu();

      JPopupMenu popupMenu = getPopupMenu();
      if (popupMenu == null)
      {
         popupMenu = new JPopupMenu();
         setPopupMenu(popupMenu);
      }
      else
      {
         popupMenu.addSeparator();
      }

      propertiesItem = new JMenuItem("Properties ...");

      propertiesItem.addActionListener(this);
      propertiesItem.setMnemonic('s');
      popupMenu.add(propertiesItem);

      popupMenu.addSeparator();
      ButtonGroup group = new ButtonGroup();
      inItem = new JRadioButtonMenuItem("IN");
      group.add(inItem);
      inItem.addActionListener(this);
      popupMenu.add(inItem);
      outItem = new JRadioButtonMenuItem("OUT");
      group.add(outItem);
      outItem.addActionListener(this);
      popupMenu.add(outItem);
      inoutItem = new JRadioButtonMenuItem("INOUT");
      group.add(inoutItem);
      inoutItem.addActionListener(this);
      popupMenu.add(inoutItem);
   }

   /**
    *
    */
   public void deleteAll()
   {
      if (confirmDeletionWarning(""))
      {
         super.deleteAll();
         Iterator itr = getActivity().findDataMappings(getData(), null);
         if (itr.hasNext())
         {
            while (itr.hasNext())
            {
               IDataMapping mapping = (IDataMapping) itr.next();
               getActivity().removeFromDataMappings(mapping);
            }
         }
      }
   }

   /** */
   public void draw(Graphics graphics)
   {
      Color _oldColor = graphics.getColor();

      graphics.setColor(hasExceptionHandlers(getActivity(), getData())
            ? CI.RED : CI.LIGHTGREY);
      super.draw(graphics);
      graphics.setColor(_oldColor);
   }

   /**
    * Returns the name of the connection.
    */
   public String getConnectionName()
   {
      return STRING_CONNECTION_NAME;
   }

   public void onDoubleClick(int x, int y)
   {
   }

   /*
    * Called before a popup menu is activated and is used to enable or
    * disable menu items according to the state of the activity.
    */
   public void preparePopupMenu()
   {
      super.preparePopupMenu();

      if (/*DataTypeUtils.getReferenceClass(getData()) != null
            && */(((getActivity().getApplication() != null))
            || getActivity().getImplementationType().equals(ImplementationType.Manual)))
      {
         propertiesItem.setEnabled(true);
      }
      else
      {
         propertiesItem.setEnabled(false);
      }
      if (Direction.IN == direction)
      {
         inItem.setSelected(true);
      }
      else if (Direction.OUT == direction)
      {
         outItem.setSelected(true);
      }
      else if (Direction.IN_OUT == direction)
      {
         inoutItem.setSelected(true);
      }
   }

   /** */
   public void refreshFromModel()
   {
      Direction direction = getConnectionDirection(getActivity(), getData());
      if (direction != null)
      {
         this.direction = direction;
         updateArrows();
      }
      else
      {
         delete();
      }
   }

   /** */
   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      if (getFirstSymbol() instanceof DataSymbol)
      {
         setSecondSymbolAsActivitySymbol(secondSymbol, link);
      }
      else
      {
         setSecondSymbolAsDataSymbol(secondSymbol, link);
      }

      super.setSecondSymbol(secondSymbol, link);
      updateArrows();
   }

   private void setSecondSymbolAsDataSymbol(Symbol secondSymbol, boolean link)
   {
      if (!(secondSymbol instanceof DataSymbol))
      {
         throw new PublicException("The selected Symbol does not represent an activity.");
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(
            getFirstSymbol(), secondSymbol, DataMappingConnection.class, true))
      {
         throw new PublicException("Connection between symbols already exists.");
      }
      data.setElement(((DataSymbol) secondSymbol).getData());
      if (link)
      {
         generateDefaultMappings();
      }
   }

   private void setSecondSymbolAsActivitySymbol(Symbol secondSymbol, boolean link)
   {
      if (!(secondSymbol instanceof ActivitySymbol))
      {
         throw new PublicException("The selected Symbol does not represent an activity.");
      }
      else if (((ActivitySymbol) secondSymbol).getActivity().
            getImplementationType().equals(ImplementationType.Route))
      {
         throw new PublicException(
               "A route activity does not participate in data flow.");
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(
            getFirstSymbol(), secondSymbol, DataMappingConnection.class, true))
      {
         throw new PublicException("Connection between symbols already exists.");
      }

      activity.setElement(((ActivitySymbol) secondSymbol).getActivity());
      if (link)
      {
         generateDefaultMappings();
      }
   }

   private void generateDefaultMappings()
   {
      if (direction == Direction.IN)
      {
         generateDefaultInMapping();
      }
      else if (direction == Direction.OUT)
      {
         generateDefaultOutMapping();
      }
      else
      {
         generateDefaultInMapping();
         generateDefaultOutMapping();
      }
   }

   private void generateDefaultOutMapping()
   {
      Iterator outMappings = getActivity().getAllOutDataMappings();
      boolean matchFound = false;
      while (outMappings.hasNext())
      {
         IDataMapping mapping = (IDataMapping) outMappings.next();
         if (mapping.getData().equals(getData()))
         {
            matchFound = true;
            break;
         }
      }
      if (!matchFound)
      {
         IDataMapping mapping = getActivity().createDataMapping(getData().getId(), getData().getName(), getData(),
               Direction.OUT);
         if (getActivity().isInteractive())
         {
            mapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
         }
         else
         {
            mapping.setContext(PredefinedConstants.ENGINE_CONTEXT);
         }
      }
   }

   private void generateDefaultInMapping()
   {
      Iterator inMappings = getActivity().getAllInDataMappings();
      boolean matchFound = false;
      while (inMappings.hasNext())
      {
         IDataMapping mapping = (IDataMapping) inMappings.next();
         if (mapping.getData().equals(getData()))
         {
            matchFound = true;
            break;
         }
      }
      if (!matchFound)
      {
         IDataMapping mapping = getActivity().createDataMapping(getData().getId(), getData().getName(), getData(),
               Direction.IN);
         if (getActivity().isInteractive())
         {
            mapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
         }
         else
         {
            mapping.setContext(PredefinedConstants.ENGINE_CONTEXT);
         }
      }
   }

   private boolean confirmDeletionWarning(String type)
   {
      return getData() == null || getActivity() == null ||
            JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete all " + type + " data mappings between the data '" +
            getData().getName() + "'\n and the activity '" + getActivity().getName() +
            "' from the model.\n\n" +
            "This operation cannot be undone. Continue?", "Data Mapping Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
   }

   public static boolean hasExceptionHandlers(IActivity activity, IData data)
   {
      Iterator exceptionHandlers = activity.findExceptionHandlers(data);
      if (exceptionHandlers.hasNext())
      {
         return true;
      }
      return false;
   }

   public static Direction getConnectionDirection(IActivity activity, IData data)
   {
      boolean inDirection = false;
      boolean outDirection = hasExceptionHandlers(activity, data);

      Iterator mappings = activity.findDataMappings(data, null);
      while (mappings.hasNext())
      {
         IDataMapping mapping = (IDataMapping) mappings.next();
         if (mapping.getDirection().equals(Direction.IN))
         {
            inDirection = true;
         }
         else
         {
            outDirection = true;
         }
      }
      if (inDirection)
      {

         if (outDirection)
         {
            return Direction.IN_OUT;
         }
         else
         {
            return Direction.IN;
         }
      }
      else
      {
         if (outDirection)
         {
            return Direction.OUT;
         }
         else
         {
            return null;
         }
      }
   }

   /*
    * Retrieves the (model) information represented by the symbol. If not set,
    * the method returns <code>null</code>.
    */
   public Object getUserObject()
   {
      // @todo (egypt): this is not nice, maybe we find a way to have a user object always
      // being a model element;
      if (activity.isEmpty())
      {
         return null;
      }
      return getActivity().findDataMappings(getData(), null);
   }

   public String toString()
   {
      return "Data Mapping Connection for " + activity.getElement() + " / " + data.getElement();
   }
}