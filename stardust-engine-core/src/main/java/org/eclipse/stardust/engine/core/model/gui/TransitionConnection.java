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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.JoinSplitType;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.DecorationStrategy;
import org.eclipse.stardust.engine.core.compatibility.diagram.Stylesheet;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.gui.CI;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;


/**
 * @author mgille
 */
public class TransitionConnection extends AbstractWorkflowPathConnection
      implements ActionListener
{
   private static final String STRING_CONNECTION_NAME = "Transition";

   private static final double MAX_WIDTH = 20.0;
   private static final int CONDITION_LENGTH = 20;

   // @todo use a constant from class Transition

   private static final String DEFAULT_CONDITION = "TRUE";
   private static final String CONDITION_PREFIX = "";//= "IF (";
   private static final String CONDITION_POSTFIX = "";//")";

   private static final int CAPPED_CONDITION_LENGTH =
         CONDITION_LENGTH - CONDITION_PREFIX.length() - CONDITION_POSTFIX.length();

   private static boolean resourcesInitialized;
   private static String STYLE;
   private static Color DEFAULT_PATH_COLOR;
   private static Color TRAVERSED_PATH_COLOR;
   private static Color ACTIVE_PATH_COLOR;
   private static Color CRITICAL_PATH_COLOR;
   private static Color CONDITION_COLOR;

   private SingleRef transition = new SingleRef(this, "Transition");
   private int percentage;
   private boolean critical;

   private transient JMenuItem rerouteItem;
//   private transient JMenuItem sourceItem;
   private transient JMenuItem targetItem;
   private transient JMenuItem propertiesItem;
   private transient BasicStroke stroke;

   public static synchronized void initializeResources()
   {
      if (!resourcesInitialized)
      {
         STYLE = Stylesheet.instance().getString("Transition", "style", "carnot");
         DEFAULT_PATH_COLOR = Stylesheet.instance().getColor("Transition", "default-path-color", CI.BLUE);
         TRAVERSED_PATH_COLOR = Stylesheet.instance().getColor("Transition", "traversed-path-color", CI.LIGHTGREY);
         ACTIVE_PATH_COLOR = Stylesheet.instance().getColor("Transition", "active-path-color", CI.RED);
         CRITICAL_PATH_COLOR = Stylesheet.instance().getColor("Transition", "critical-path-color", CI.RED);
         CONDITION_COLOR = Stylesheet.instance().getColor("Transition", "condition-color", CI.BLUE);

         resourcesInitialized = true;
      }
   }

   TransitionConnection()
   {
   }

   public TransitionConnection(ActivitySymbol firstSymbol)
   {
      initializeResources();
      setFirstSymbol(firstSymbol);
   }

   /**
    * Load constructor. Used by XML import.
    */
   public TransitionConnection(ActivitySymbol firstSymbol,
         ActivitySymbol secondSymbol,
         ITransition transition)
   {
      Assert.isNotNull(firstSymbol);
      Assert.isNotNull(secondSymbol);
      Assert.isNotNull(transition);

      initializeResources();

      super.setFirstSymbol(firstSymbol);
      super.setSecondSymbol(secondSymbol, false);

      this.transition.setElement(transition);
   }

   public TransitionConnection(ITransition transition)
   {
      initializeResources();
      this.transition.setElement(transition);
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
      return new TransitionConnection(getTransition());
   }

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

      rerouteItem = new JMenuItem("Reroute");
      rerouteItem.addActionListener(this);
      rerouteItem.setMnemonic('o');
      _popupMenu.add(rerouteItem);

//      sourceItem = new JMenuItem("Change Source");
//      sourceItem.addActionListener(this);
//      sourceItem.setMnemonic('s');
//      _popupMenu.add(sourceItem);

      targetItem = new JMenuItem("Change Target Activity");
      targetItem.addActionListener(this);
      targetItem.setMnemonic('t');
      _popupMenu.add(targetItem);

      _popupMenu.addSeparator();

      propertiesItem = new JMenuItem("Properties ...");
      propertiesItem.addActionListener(this);
      propertiesItem.setMnemonic('e');

      _popupMenu.add(propertiesItem);
   }

   public void deleteAll()
   {
      if (getTransition() == null || JOptionPane.showConfirmDialog(
            JOptionPane.getFrameForComponent(getDrawArea()),
            "You are going to delete the transition between \"" + getTransition().getFromActivity().getName() +
            "\" and \"" + getTransition().getToActivity().getName() + "\".\n\n" +
            "This operation cannot be undone. Continue?", "Transition Deletion",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
      {
         super.deleteAll();

         if (getTransition() != null)
         {
            getTransition().delete();
         }
      }
   }

   public void mouseMoved(MouseEvent event)
   {
      super.mouseMoved(event);
      String toolTipText = null;
      if (getTransition() != null)
      {
         String condition = getTransition().getCondition();
         if (condition != null && condition.length() > CAPPED_CONDITION_LENGTH)
         {
            toolTipText = getTransition().getCondition();
         }
      }
      getDrawArea().setToolTipText(toolTipText);
   }

   public void draw(Graphics g)
   {
      Graphics2D _graphics = (Graphics2D) g;
      Color _oldColor = _graphics.getColor();
      Stroke _oldStroke = _graphics.getStroke();
      Color _penColor = null;
      String _condition = null;
      int _conditionWidth = 0;
      int _conditionX = 0;
      int _conditionY = 0;
      int _fontHeight = 0;

      int symbolStyle = getDrawArea().getDecorationStrategy().getSymbolStyle(this);
      if (DecorationStrategy.STYLE_PLAIN != symbolStyle)
      {
         switch (symbolStyle)
         {
            case DecorationStrategy.STYLE_ACTIVE:
               _penColor = ACTIVE_PATH_COLOR;
               _graphics.setStroke(selectedStroke);
               break;
            case DecorationStrategy.STYLE_TRAVERSED:
               _penColor = TRAVERSED_PATH_COLOR;
               _graphics.setStroke(selectedStroke);
               break;
            default:
               Assert.lineNeverReached("Invalid symbol style.");
         }
      }
      else if (stroke != null)
      {
         _penColor = CI.BLUE;

         _graphics.setStroke(stroke);
      }
      else if (isCritical())
      {
         _penColor = CRITICAL_PATH_COLOR;

         _graphics.setStroke(selectedStroke);
      }
      else
      {
         _penColor = DEFAULT_PATH_COLOR;
      }

      _graphics.setColor(_penColor);

      super.draw(_graphics);

      // Draw the condition

      if (getTransition() != null)
      {
         _condition = getTransition().getCondition();

         if ((_condition != null)
               && (_condition.length() > 0)
               && (!_condition.equals(DEFAULT_CONDITION))
         )
         {
            _condition = CONDITION_PREFIX + _condition + CONDITION_POSTFIX;

            if (_condition.length() > CONDITION_LENGTH)
            {
               _condition = _condition.substring(0, CONDITION_LENGTH) + " ...";
            }

            _conditionWidth = _graphics.getFontMetrics().stringWidth(_condition);
            _fontHeight = _graphics.getFontMetrics().getHeight();

            Point point = getPath().getMiddlePoint();

            _conditionX = point.x - _conditionWidth / 2;
            _conditionY = point.y + _fontHeight / 2;

            _graphics.setColor(Color.white);
            _graphics.fillRect(_conditionX - 3
                  , _conditionY - _fontHeight + 3
                  , _conditionWidth + 4
                  , _fontHeight);

            _graphics.setColor(_penColor);

            if (STYLE.equalsIgnoreCase("carnot"))
            {
               _graphics.drawRect(_conditionX - 3
                     , _conditionY - _fontHeight + 3
                     , _conditionWidth + 4
                     , _fontHeight);
            }

            _graphics.setColor(CONDITION_COLOR);
            _graphics.drawString(_condition, _conditionX, _conditionY);
         }
      }

      // Draw path density

      if (stroke != null)
      {
         String _percentageString = "" + percentage + "%";
         int _percentageWidth = _graphics.getFontMetrics().stringWidth(_percentageString);
         _fontHeight = _graphics.getFontMetrics().getHeight();
         int _percentageX = getX() + (getWidth() - _conditionWidth) / 2;
         int _percentageY = getY() + (getHeight() - _fontHeight) / 2;

         _graphics.setColor(Color.white);
         _graphics.fillRect(_percentageX - 3, _percentageY - _fontHeight + 3,
               _percentageWidth + 4, _fontHeight);
         _graphics.setColor(_penColor);
         _graphics.drawString(_percentageString, _percentageX, _percentageY);
      }

      // Restore graphic attributes

      _graphics.setColor(_oldColor);
      _graphics.setStroke(_oldStroke);
   }

   /**
    * Returns the name of the connection.
    * @see org.eclipse.stardust.engine.core.compatibility.diagram.AbstractConnectionSymbol#getConnectionName
    */
   public String getConnectionName()
   {
      return STRING_CONNECTION_NAME;
   }

   public ITransition getTransition()
   {
      return (ITransition) transition.getElement();
   }

   /*
    * Retrieves the (model) information represented by the symbol. If not set,
    * the method returns <code>null</code>.
    */
   public Object getUserObject()
   {
      return getTransition();
   }

   public void onDoubleClick(int x, int y)
   {
   }

   /*
    * Called before a popup menu is activated and may be used to enable or
    * disable menu items according to the state of the represented object.
    */
   public void preparePopupMenu()
   {
      super.preparePopupMenu();

      // all additional items are always enabled
      // rerouteItem.setEnabled(true);
      // propertiesItem.setEnabled(true);
   }

   public void setSecondSymbol(Symbol secondSymbol, boolean link)
   {
      // check that both symbols are activitysymbols and not identical
      if (!(getFirstSymbol() instanceof ActivitySymbol)
            || !(secondSymbol instanceof ActivitySymbol))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_BOTH_CONNECTED_SYMBOLS_MUST_BE_ACTIVITIES.raise());
      }
      else if ((getDiagram() != null) && getDiagram().existConnectionBetween(getFirstSymbol(), secondSymbol
            , TransitionConnection.class, true))
      {
         throw new PublicException(
               BpmRuntimeError.MDL_CONNECTION_BETWEEN_SYMBOLS_ALREADY_EXIST.raise());
      }

      if (link)
      {
         IActivity fromActivity = ((ActivitySymbol) getFirstSymbol()).getActivity();
         IActivity toActivity = ((ActivitySymbol) secondSymbol).getActivity();

         if (getTransition() != null)
         {
            // (fh) this check duplicates the one in ProcessDefinitionBean
            if (toActivity.getJoinType().equals(JoinSplitType.None) &&
                  toActivity.getAllInTransitions().hasNext())
            {
               throw new PublicException(
                     BpmRuntimeError.MDL_MULTIPLE_INCOMING_TRANSITIONS_ARE_ONLY_ALLOWED_FOR_AND_OR_XOR_ACTIVITY_JOINS
                           .raise(getTransition().getOID(), toActivity.getOID()));
            }
            getTransition().setSecond(toActivity);
         }
         else
         {
            IProcessDefinition processDefinition = toActivity.getProcessDefinition();
            String id = processDefinition.getDefaultTransitionId();
            transition.setElement(processDefinition.createTransition(id, id, "", fromActivity, toActivity));
         }
      }

      super.setSecondSymbol(secondSymbol, link);
   }

   public void setTransition(ITransition transition)
   {
      this.transition.setElement(transition);
   }

   public void setUserObject(IdentifiableElement userObject)
   {
      setTransition((ITransition) userObject);
   }

   /*
    * Forces the symbol to change its appearance according to the changes on its
    * user object.
    */
   public void userObjectChanged()
   {
   }

   public void activateDensity(int percentage)
   {
      this.percentage = percentage;
      stroke = new BasicStroke((float) (MAX_WIDTH * percentage * 0.01));
   }

   public void deactivateDensity()
   {
      stroke = null;
   }

   public void setCritical(boolean critical)
   {
      this.critical = critical;
   }

   public boolean isCritical()
   {
      return critical;
   }

   public String toString()
   {
      return "Transition Connection for " + transition.getElement();
   }
}
