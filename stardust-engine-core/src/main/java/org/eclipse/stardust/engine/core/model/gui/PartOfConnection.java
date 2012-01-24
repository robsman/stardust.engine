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
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/**
 * A connection representing the "is part of"-association between a
 * suborganization and a superorganization.
 */
public class PartOfConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static protected final String STRING_CONNECTION_NAME = "PartOfConnection";

   /**
    *
    */
   public PartOfConnection()
   {
      setSecondArrow(ArrowKey.EMPTY_RHOMBUS);
   }

   /**
    *
    */
   public PartOfConnection(OrganizationSymbol firstSymbol)
   {
      setFirstSymbol(firstSymbol);
      setSecondArrow(ArrowKey.EMPTY_RHOMBUS);
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      return new PartOfConnection();
   }

   /**
    *
    */
   public void deleteAll()
   {
      try
      {
         IOrganization _fromOrganization = ((OrganizationSymbol) getFirstSymbol()).getOrganization();
         IOrganization _toOrganization = ((OrganizationSymbol) getSecondSymbol()).getOrganization();

         // todo: (fh) this assertion may disrupt deletion in case the same connection
         // is refered by 2 symbols that are simultaneous deleted. please check.
         Assert.isNotNull(_toOrganization);
         Assert.isNotNull(_fromOrganization);

         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea()), "You are going to delete the \"part of\"-connection from the organization\n\""
               + _fromOrganization.getName() + "\" to the organization \"" + _toOrganization.getName()
               + "\".\n\n" +
               "This operation cannot be undone. Continue?", "\"Part Of\"-Connection Deletion",
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
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getName
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
         throw new PublicException("The selected symbol does not represent an organization.");
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , PartOfConnection.class, true))
      {
         throw new PublicException("Such a connection already exist between this symbols.");
      }

      if (link)
      {
         IOrganization fromOrganization = ((OrganizationSymbol) getFirstSymbol()).getOrganization();
         IOrganization toOrganization = ((OrganizationSymbol) secondSymbol).getOrganization();

         toOrganization.addToSubOrganizations(fromOrganization);
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
      return "Part-Of Connection";
   }

}
