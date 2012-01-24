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
import java.awt.event.ActionListener;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/** */
public class ExecutedByConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static protected final String STRING_CONNECTION_NAME = "ExecutedByConnection";

   /** */
   public ExecutedByConnection()
   {
   }

   /** */
   public ExecutedByConnection(ApplicationSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }
   /** */
   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      return new ExecutedByConnection();
   }

   /**
    *
    */
   public void deleteAll()
   {
      try
      {
         IApplication _application = ((ApplicationSymbol) getFirstSymbol()).getApplication();
         ActivitySymbol _activitySymbol = (ActivitySymbol) getSecondSymbol();
         IActivity _activity = _activitySymbol.getActivity();

         if (_application == null || _activity == null ||
               JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
               , "Deleting the \"Executed By\"-connection of the application\n\""
               + _application.getName() + "\"\nwill override the settings for the activity\n\""
               + _activity.getName() + "\". Continue?",
               "\"Executed By\"-Connection Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
         {
            super.deleteAll();
            _activity.setApplication(null);
            _activitySymbol.updateDataMappingConnections();
         }
      }
      catch (NullPointerException _ex)
      {
         // could appear in complexe delete operation (delete more then one symbol)
         super.deleteAll();
      }
   }

   /** */
   public void draw(Graphics graphics)
   {
      Color _oldColor = graphics.getColor();

      graphics.setColor(CI.LIGHTGREY);
      super.draw(graphics);
      graphics.setColor(_oldColor);
   }

   /**
    * Returns the name of the connection.
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getConnectionName
    */
   public String getConnectionName()
   {
      return STRING_CONNECTION_NAME;
   }

   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      if (!(secondSymbol instanceof ActivitySymbol))
      {
         throw new PublicException("The selected symbol does not represent an activity.");
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , ExecutedByConnection.class, true))
      {
         throw new PublicException("Such a connection already exist between this symbols.");
      }

      if (link)
      {
         IApplication _application = ((ApplicationSymbol) getFirstSymbol()).getApplication();
         IActivity _activity = ((ActivitySymbol) secondSymbol).getActivity();

         Assert.isNotNull(_application, "Application is not null");
         Assert.isNotNull(_activity, "Activity is not null");

         // check if the old and new application of the activity are different
         if ((_activity.getImplementationType().equals(ImplementationType.Route))
               || (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
                     , "Assigning the application to this activity \nwill override all previous settings."
                     , "Confirmation"
                     , JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
         )
         {
            _activity.setApplication(_application);
            ((ActivitySymbol) secondSymbol).refreshFromModel();

            super.setSecondSymbol(secondSymbol, link);
         }
      }
      else
      {
         super.setSecondSymbol(secondSymbol, link);
      }

   }
   /*
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   public void userObjectChanged()
   {
   }

   public String toString()
   {
      return "ExecutedByConnection";
   }

}
