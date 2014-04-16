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

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/**
 *
 */
public class WorksForConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static protected final String STRING_CONNECTION_NAME = "WorksForConnection";

   /**
    *
    */
   public WorksForConnection()
   {
   }

   /**
    *
    */
   public WorksForConnection(ModelerSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
   }

   /**
    *
    */
   public WorksForConnection(RoleSymbol firstSymbol)
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
      return new WorksForConnection();
   }

   /**
    *
    */
   public void deleteAll()
   {
      IModelParticipant _participant = null;

      try
      {
         if (getFirstSymbol() instanceof RoleSymbol)
         {
            _participant = ((RoleSymbol) getFirstSymbol()).getRole();
         }

         IOrganization organization = ((OrganizationSymbol) getSecondSymbol()).getOrganization();

         // todo: (fh) this assertion may disrupt deletion in case the same connection
         // is refered by 2 symbols that are simultaneous deleted. please check.
         Assert.isNotNull(_participant);
         Assert.isNotNull(organization);

         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea()), "You are going to delete the \"works for\"-connection between workflow participant\n\""
               + _participant.getName() + "\" and the organization \"" + organization.getName()
               + "\".\n\n" +
               "This operation cannot be undone. Continue?", "\"Works For\"-Connection Deletion",
               JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
         {
            super.deleteAll();
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
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getConnectionName
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
      if (!(secondSymbol instanceof OrganizationSymbol))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_SELECTED_SYMBOL_DOES_NOT_REPRESENT_AN_ORGANIZATION
                     .raise());
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , WorksForConnection.class, true))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_CONNECTION_BETWEEN_SYMBOLS_ALREADY_EXIST.raise());
      }

      if (link)
      {
         IModelParticipant participant = null;

         if (getFirstSymbol() instanceof ModelerSymbol)
         {
            participant = ((ModelerSymbol) getFirstSymbol()).getModeler();
         }
         else if (getFirstSymbol() instanceof RoleSymbol)
         {
            participant = ((RoleSymbol) getFirstSymbol()).getRole();
         }

         Assert.isNotNull(participant);

         IOrganization organization = ((OrganizationSymbol) secondSymbol).getOrganization();

         Assert.isNotNull(organization);

         organization.addToParticipants(participant);
      }

      super.setSecondSymbol(secondSymbol, link);
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
      return "Works-For Connection";
   }

}
