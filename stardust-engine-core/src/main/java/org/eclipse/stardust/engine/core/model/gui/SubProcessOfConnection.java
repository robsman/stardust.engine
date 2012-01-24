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
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/** */
public class SubProcessOfConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static protected final String STRING_CONNECTION_NAME = "SubprocessConnection";

   /** */
   public SubProcessOfConnection()
   {
      setSecondArrow(ArrowKey.EMPTY_RHOMBUS);
   }

   /** */
   public SubProcessOfConnection(ProcessDefinitionSymbol firstSymbol)
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
      return new SubProcessOfConnection();
   }

   /**
    *
    */
   public void deleteAll()
   {
      try
      {
         IProcessDefinition _subProcess = ((ProcessDefinitionSymbol) getFirstSymbol()).getProcessDefinition();
         IProcessDefinition _superProcess = ((ProcessDefinitionSymbol) getSecondSymbol()).getProcessDefinition();

         // todo: (fh) this assertion may disrupt deletion in case the same connection
         // is refered by 2 symbols that are simultaneous deleted. please check.
         Assert.isNotNull(_subProcess);
         Assert.isNotNull(_superProcess);

         if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
               , "You are going to delete the \"subprocess of\"-connection between process definition \n\""
               + _subProcess.getName() + "\" and process definition \"" + _superProcess.getName()
               + "\".\n\n" +
               "This operation cannot be undone. Continue?", "\"Subprocess Of\"-Connection Deletion",
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
    * 
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getName
    */
   public String getConnectionName()
   {
      return STRING_CONNECTION_NAME;
   }

   /** */
   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      if (!(secondSymbol instanceof ProcessDefinitionSymbol))
      {
         throw new PublicException("The selected symbol does not represent an process definition.");
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , SubProcessOfConnection.class, true))
      {
         throw new PublicException("Such a connection already exist between this symbols.");
      }

      super.setSecondSymbol(secondSymbol, link);
   }

   /**
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   public void userObjectChanged()
   {
   }

   public String toString()
   {
      return "Subprocess-Of Connection";
   }

}
