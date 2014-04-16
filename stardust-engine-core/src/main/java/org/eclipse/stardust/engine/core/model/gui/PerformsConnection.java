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
import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/** */
public class PerformsConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   protected static final String STRING_CONNECTION_NAME = "PerformsConnection";

   /**
    *
    */
   public PerformsConnection()
   {
   }

   /**
    *
    */
   public PerformsConnection(ModelerSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }

   /** */
   public PerformsConnection(ConditionalPerformerSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }

   /**
    *
    */
   public PerformsConnection(OrganizationSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }

   /**
    *
    */
   public PerformsConnection(RoleSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      return new PerformsConnection();
   }

   /**
    *
    */
   public void deleteAll()
   {
      try
      {
         IActivity _activity = ((ActivitySymbol) getSecondSymbol()).getActivity();

         // todo: (fh) this assertion may disrupt deletion in case the same connection
         // is refered by 2 symbols that are simultaneous deleted. please check.
//         Assert.isNotNull(_activity);
//         Assert.isNotNull(_activity.getPerformer());

         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
               , "You are going to delete the \"performs\"-connection\nbetween performer\""
               + _activity.getPerformer().getName() + "\" and the activity \"" + _activity.getName()
               + "\".\n\n" +
               "This operation cannot be undone. Continue?", "\"Performs\"-Connection Deletion",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
         {
            super.deleteAll();
            _activity.setPerformer(null);
         }
      }
      catch (NullPointerException _ex)
      {
         // could appear in complexe delete operation (delete more then one symbol)
         super.deleteAll();
      }
   }

   /**
    *
    */
   public void draw(Graphics graphics)
   {
      Color _oldColor = graphics.getColor();

      graphics.setColor(CI.LIGHTGREY);
      super.draw(graphics);
      graphics.setColor(_oldColor);
   }

   /**
    * Returns the name of the connection.
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getConnectionName()
    */
   public String getConnectionName()
   {
      return STRING_CONNECTION_NAME;
   }

   /**
    *
    */
   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      if (!(secondSymbol instanceof ActivitySymbol))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_THE_SELECTED_SYMBOL_DOES_NOT_REPRESENT_AN_ACTIVITY
                     .raise());
      }

      IActivity activity = ((ActivitySymbol) secondSymbol).getActivity();

      if (ImplementationType.Route.equals(activity.getImplementationType()))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ROUTE_ACTIVITY_CANNOT_HAVE_A_PERFORMER.raise());
      }
      else if (ImplementationType.SubProcess.equals(activity.getImplementationType()))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_SUBPROCESS_ACTIVITY_CANNOT_HAVE_A_PERFORMER.raise());
      }
      else if (ImplementationType.Application.equals(activity.getImplementationType())
            && !activity.getApplication().isInteractive())
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ACTIVITY_PERFORMING_NON_INTERACTIVE_APPLICATION_CANNOT_HAVE_PERFORMER
                     .raise());
      }
      else if ((getDiagram() != null)
            && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol,
                  PerformsConnection.class, true))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_CONNECTION_BETWEEN_SYMBOLS_ALREADY_EXIST.raise());
      }

      super.setSecondSymbol(secondSymbol, link);

      if (link)
      {
         IModelParticipant participant = null;

         if (getFirstSymbol() instanceof OrganizationSymbol)
         {
            participant = ((OrganizationSymbol) getFirstSymbol()).getOrganization();
         }
         else if (getFirstSymbol() instanceof RoleSymbol)
         {
            participant = ((RoleSymbol) getFirstSymbol()).getRole();
         }
         else if (getFirstSymbol() instanceof ConditionalPerformerSymbol)
         {
            participant = ((ConditionalPerformerSymbol) getFirstSymbol()).getPerformer();
         }

         activity.setPerformer(participant);
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
      return "Performs-Connection";
   }

}
