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
/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *	A helper class for general JFC GUI programming. It contains
 * <ul>
 * <li> constants for general stylguide settings (distcances, colors)
 * <li> helper functionality for glass panes and disabling of windows
 * </ul>
 */
public class GUI
{
   public static final Logger trace = LogManager.getLogger(GUI.class);

   /**
    * Background color of standard fields.
    */
   public final static Color DefaultColor = SystemColor.window;
   /**
    * Background color of mandatory fields.
    */
   public final static Color MandatoryColor = Color.yellow;
   /**
    * Background color of accepted (mandatory) fields.
    */
   public final static Color AcceptedColor = Color.green;
   /**
    * Background color of readonly fields.
    */
   public final static Color ReadOnlyColor = SystemColor.control;
   /**
    * Background color of disabled fields.
    */
   public final static Color DisabledColor = ReadOnlyColor;
   /**
    * text color of standard fields.
    */
   public final static Color DefaultTextColor = SystemColor.textText;
   /**
    * text color of mandatory fields.
    */
   public final static Color MandatoryTextColor = DefaultTextColor;
   /**
    * text color of accepted (mandatory)  fields.
    */
   public final static Color AcceptedTextColor = DefaultTextColor;
   /**
    * text color of readonly fields.
    */
   public final static Color ReadOnlyTextColor = DefaultTextColor;
   /**
    * text color of disabled fields.
    */
   public final static Color DisabledTextColor = SystemColor.textInactiveText;
   /**
    * Cursor for EntryFields.
    */
   public final static Cursor ENTRY_CURSOR = new Cursor(Cursor.TEXT_CURSOR);
   /**
    * Minimal vertical space (in pixels) between entry fields etc.
    */
   public final static int VerticalWidgetDistance = 10;
   /**
    * Minimal horizontal space (in pixels) between entry fields etc.
    */
   public final static int HorizontalWidgetDistance = 10;
   /**
    * Minimal vertical space (in pixels) between label and entry field etc.
    */
   public final static int VerticalLabelDistance = 5;
   /**
    * Minimal horizontal space (in pixels) between label and entry field etc.
    */
   public final static int HorizontalLabelDistance = 5;
   /**
    * Left free space
    */
   public static int PanelDistanceLeft = VerticalWidgetDistance;
   /**
    * Right free space
    */
   public static int PanelDistanceRight = HorizontalWidgetDistance;
   /**
    * Top free space
    */
   public static int PanelDistanceTop = VerticalWidgetDistance;
   /**
    * Bottom free space.
    */
   public static int PanelDistanceBottom = HorizontalWidgetDistance;
   /**
    * A panel's inner free space.
    */
   public static int InPanelDistance = VerticalWidgetDistance;
   /**
    * Default method for busy mode is <code>super.setEnabled</code>
    */
   static boolean isUsingGlassPane = false;
   /**
    * Using a MouseMotionAdapter for not losing the busy pointer after mouse motion.
    */
   static MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter()
   {
   };
   /**
    * Hashtable to store all busy JFrames etc. in.
    */
   static Hashtable busyTable = new Hashtable();
   /**
    *	constant as command for disabling glasspane (currently using CTRL+ALT+D).
    */
   public static final String DISABLE_GLASSPANE = "GlassPane.disable";
   /**
    * Unrestricted size for any object.
    */
   public static final Dimension SIZE_MAX = new Dimension(Integer.MAX_VALUE,
         Integer.MAX_VALUE);

   /**
    *	Icon preceeding optional fields
    */
   private static Icon optionalIcon;
   /**
    *	Icon preceeding mandatory fields
    */
   private static Icon mandatoryIcon;

   public static Insets noInsets = new Insets(0, 0, 0, 0);
   private static HashMap icons = new HashMap();

   /**
    * Get a default empty border with correct distances
    */
   public static AbstractBorder getEmptyPanelBorder()
   {
      return new EmptyBorder(VerticalWidgetDistance, VerticalWidgetDistance,
            VerticalWidgetDistance, VerticalWidgetDistance);
   }

   /**
    * Get a default titled titled border with correct distances
    *
    *	@param title the string to be used for titling
    */
   public static AbstractBorder getTitledPanelBorder(String title)
   {
      return new CompoundBorder(new TitledBorder(new EtchedBorder(), title),
            new EmptyBorder(PanelDistanceTop, PanelDistanceLeft,
                  PanelDistanceBottom, PanelDistanceRight));
   }

   /**
    *	If isUsingGlassPane is true busy mode will use a glasspane
    *
    *	@param useGlassPane set to true if glasspane should be used
    */
   public static void enableGlassPane(boolean useGlassPane)
   {
      isUsingGlassPane = useGlassPane;
   }

   /**
    * If isUsingGlassPane is true will use a glasspane
    *
    * @param object a JComponent that is used to determine the current JFrame,
    *        JWindow or JDialog which will used for the busy mode
    *	@param isBusy indicates the desired busy state <code>(true/false)</code>
    */
   public static void setWaiting(final JComponent object, boolean isBusy)
   {
      if (object == null)
      {
         return;
      }

      java.awt.Component busyGlassPane = null;

      // rootPane only needed for registering keyboard action

      JRootPane rootPane = null;

      // Top level container JFrame/JWindow/JDialog

      Container container = object.getTopLevelAncestor();

      // Find top level container and use correct busy mode

      if (container == null)
      {
         if (isBusy == true)
         {
            return;
         }

         // Object not in hashtable and should not be in busy state, return

         if ((container = (Container) busyTable.get(object)) == null)
         {
            return;
         }
      }

      // Not using glasspane

      if (!isUsingGlassPane)
      {
         if (isBusy)
         {
            container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Add container to hashtable

            busyTable.put(object, container);
         }
         else
         {
            container.setCursor(Cursor.getDefaultCursor());

            // remove container from hashtable

            busyTable.remove(object);
         }

         container.setEnabled(!isBusy);

         return;
      }

      // Using glasspane; try to get one

      if (container instanceof JFrame)
      {
         busyGlassPane = ((JFrame) container).getGlassPane();
         rootPane = ((JFrame) container).getRootPane();
      }
      else if (container instanceof JWindow)
      {
         busyGlassPane = ((JWindow) container).getGlassPane();
         rootPane = ((JWindow) container).getRootPane();
      }
      else if (container instanceof JDialog)
      {
         busyGlassPane = ((JDialog) container).getGlassPane();
         rootPane = ((JDialog) container).getRootPane();
      }
      else
      {
         throw new InternalException("Using setBusyMode on a JComponent with unknown top level anchestor: " + container.getClass());
      }

      // Add a special Key for hiding GlassPane after error that does not
      // allow us to reach the normal hide method

      ActionListener keyListener = new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            setWaiting(object, false);
         }
      };

      rootPane.registerKeyboardAction(
            keyListener, DISABLE_GLASSPANE,
            KeyStroke.getKeyStroke(KeyEvent.VK_D,
                  Event.ALT_MASK + Event.CTRL_MASK, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

      // Set the cursor and update hashtable

      if (isBusy)
      {
         // Use container for faster cursor change

         container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

         // Set the glasspane's cursor

         busyGlassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

         // Add a MouseMotionListener to ensure that cursor is visible after mouse movement

         busyGlassPane.addMouseMotionListener(mouseMotionAdapter);

         // Add container to hashtable

         busyTable.put(object, container);
      }
      else
      {
         // Set default cursor again for container

         container.setCursor(Cursor.getDefaultCursor());

         // Set the glasspane's cursor (otherwise a mouse click could bring up wrong cursor)

         busyGlassPane.setCursor(Cursor.getDefaultCursor());

         // Remove mousemotionlistener again (gc)

         busyGlassPane.removeMouseMotionListener(mouseMotionAdapter);

         // Remove container from hashtable

         busyTable.remove(object);
      }

      // Show/hide the glasspane

      busyGlassPane.setVisible(isBusy);
   }

   /**
    *	@deprecated Use <code>setWaiting</code> instead.
    */
   public static void setBusy(JFrame frame, boolean isBusy)
   {
      setWaiting(frame, isBusy);
   }

   /**
    *	If <code>isUsingGlassPane</code> is true will use a glasspane.
    *
    *	@param frame a JFrame to be set to busy mode
    * @param isBusy indicates the wanted busy stat <code>(true/false)</code>
    */
   public static void setWaiting(JFrame frame, boolean isBusy)
   {
      JComponent object = frame.getRootPane();

      setWaiting(object, isBusy);
   }

   /**
    * @return The icon preceeding optional fields.
    */
   public static Icon getOptionalIcon()
   {
      loadImages();

      return optionalIcon;
   }

   /**
    * @return The icon preceeding empty mandatory fields.
    */
   public static Icon getMandatoryIcon()
   {
      loadImages();

      return mandatoryIcon;
   }

   /**
    */
   private static void loadImages()
   {
      if (optionalIcon != null &&
            mandatoryIcon != null)
      {
         return;
      }

      try
      {
         optionalIcon = new ImageIcon(AbstractEntry.class.getResource("images/cross_blank.gif"));
         mandatoryIcon = new ImageIcon(AbstractEntry.class.getResource("images/cross.gif"));
      }
      catch (Exception x)
      {
         throw new InternalException("Cannot load images for optional and mandatory fields.");
      }
   }

   /**
    *	@deprecated Who needs this class for what?
    */
   static class DefaultClipboardObserver implements ClipboardOwner
   {
      public void lostOwnership(Clipboard clipboard, Transferable contents)
      {
      }
   }

   /**
    * helperfunction for enable-management in container
    */
   static public void setEnableState(Container container, boolean enabled)
   {
      Component _child = null;

      if (container != null)
      {
         if (container instanceof JComponent)
         {
            container.setEnabled(enabled);
         }

         int _count = container.getComponentCount();
         for (int _i = 0; _i < _count; _i++)
         {
            _child = container.getComponent(_i);

            if ((_child instanceof AbstractEntry)
                  || (_child instanceof AbstractDateEntry)
                  || (_child instanceof TimeEntry)
                  || (_child instanceof MandatoryWrapper)
            )
            {
               _child.setEnabled(enabled);
            }
            else if (_child instanceof Container)
            {
               GUI.setEnableState((Container) _child, enabled);
            }
            else
            {
               _child.setEnabled(enabled);
            }
         }
      }
   }

   /**
    * helperfunction for register a keyListener at all components in a container
    */
   static public void registerKeyListener(JComponent component, KeyListener listener)
   {
      Component _child = null;

      if (component != null)
      {
         component.addKeyListener(listener);

         int count = component.getComponentCount();
         for (int _i = 0; _i < count; _i++)
         {
            _child = component.getComponent(_i);
            if ((_child instanceof AbstractDateEntry)
                  || (_child instanceof AbstractEntry)
                  || !(_child instanceof JComponent)
            )
            {
               _child.addKeyListener(listener);
            }
            else
            {
               GUI.registerKeyListener((JComponent) _child, listener);
            }
         }
      }
   }

   /**
    * helperfunction for remove a keyListener at all components in a container
    */
   static public void removeKeyListener(JComponent component, KeyListener listener)
   {
      Component _child = null;

      if (component != null)
      {
         component.removeKeyListener(listener);

         int count = component.getComponentCount();
         for (int _i = 0; _i < count; _i++)
         {
            _child = component.getComponent(_i);

            if ((_child instanceof AbstractDateEntry)
                  || (_child instanceof AbstractEntry)
                  || !(_child instanceof JComponent)
            )
            {
               _child.removeKeyListener(listener);
            }
            else
            {
               GUI.removeKeyListener((JComponent) _child, listener);
            }
         }
      }
   }

   /**
    * show a "yet not implemented"-Messagebox
    */
   static public void showNotImplementedMessage(Component component)
   {
      JOptionPane.showMessageDialog(component, "Sorry, not implemented yet...");
   }

   /**
    * show a "yet not implemented"-Messagebox
    */
   static public void showNotImplementedMessage()
   {
      showNotImplementedMessage(null);
   }

   /**
    * Show the popup.
    * The method make sure that the popop is complete visible at the screen
    * and avoid the clipping of menu at the screenborder.
    */
   static public void showPopup(JPopupMenu menu, Component invoker, int xPos, int yPos)
   {
      Dimension _screenSize = null;
      Dimension _menuSize = null;
      Point _invokerOrigin = null;
      int _x = xPos;
      int _y = yPos;

      if (menu != null)
      {
         if (invoker != null)
         {
            _screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            _menuSize = menu.getPreferredSize();
            _invokerOrigin = invoker.getLocationOnScreen();

            // make sure that the whole menu is visble on the screen
            _x = Math.min(xPos, _screenSize.width - _menuSize.width - _invokerOrigin.x);
            _y = Math.min(yPos, _screenSize.height - _menuSize.height - _invokerOrigin.y);
         }
         menu.show(invoker, _x+10, _y);
         menu.requestFocus();
      }
   }

   public static boolean isPopupTrigger(MouseEvent e)
   {
      return e.isPopupTrigger() ||
            (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
   }

   public static Icon getIcon(String s)
   {
      if (icons.containsKey(s))
      {
         return (Icon) icons.get(s);
      }
      Icon icon = null;
      try
      {
         icon = new ImageIcon(GUI.class.getResource(s));
      }
      catch (Exception e)
      {
         trace.info("Unable to load icon from '" + s + "'", e);
      }
      icons.put(s, icon);
      return icon;
   }
}


