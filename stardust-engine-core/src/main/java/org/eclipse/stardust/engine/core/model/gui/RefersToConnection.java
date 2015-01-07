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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;


/**
 * A connection representing the "refers to"-association between a
 * annotationsymbol itself and the annotated symbol.
 */
public class RefersToConnection extends AbstractWorkflowLineConnection
      implements ActionListener
{
   static protected final String STRING_CONNECTION_NAME = "RefersToConnection";

   protected static final float[] dash = {5.0f};
   protected static final Stroke standardDashedStroke = new BasicStroke(0.75f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , dash
         , 0.0f);
   protected static final Stroke selectedDashedStroke = new BasicStroke(2.0f
         , BasicStroke.CAP_BUTT
         , BasicStroke.JOIN_MITER
         , 10.0f
         , dash
         , 0.0f);

   private boolean addAnnotationSymbol;

   /**
    *
    */
   public RefersToConnection()
   {
   }

   /**
    *
    */
   public RefersToConnection(Symbol firstSymbol)
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
      return new RefersToConnection();
   }

   /** */
   public void deleteAll()
   {
      if (JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(getDrawArea())
            , "This operation cannot be undone. Continue?",
            "\"Refers To\"-Connection Deletion", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
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
    * Insert the method's description here.
    * Creation date: (12.07.2000 16:23:49)
    * @return java.awt.Stroke
    */
   protected Stroke getStroke()
   {

      if (getSelected())
      {
         return selectedDashedStroke;
      }
      else
      {
         return standardDashedStroke;
      }
   }

   /**
    *
    */
   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      // check that one of the symbols is an annotation
      if ((!(getFirstSymbol() instanceof AnnotationSymbol))
            && (!(secondSymbol instanceof AnnotationSymbol))
      )
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ONE_OF_THE_CONNECTED_SYMBOLS_MUST_BE_AN_ANNOTATION
                     .raise());
      }
      else if (getFirstSymbol().equals(secondSymbol))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_ANNOTATION_CANNOT_REFER_TO_ITSELF.raise());
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , RefersToConnection.class, false))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_CONNECTION_BETWEEN_SYMBOLS_ALREADY_EXIST.raise());
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
      return "RefersTo-Connection";
   }

}
