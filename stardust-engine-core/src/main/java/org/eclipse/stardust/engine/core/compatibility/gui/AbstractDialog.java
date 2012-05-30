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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *  Abstract base class for standard dialogs.
 */
public abstract class AbstractDialog extends JDialog
      implements ActionListener, KeyListener
{
   private static final Logger trace = LogManager.getLogger(AbstractDialog.class);

   public static final int OK_CANCEL_TYPE = 0;
   public static final int CLOSE_TYPE = 1;
   public static final int OK_TYPE = 2;

   protected static final char COLON_CHAR = ':';
   protected static final String COLON_STRING = ":";

   protected static final int BUTTON_WIDTH = 80;
   protected static final int BUTTON_HEIGHT = 25;

   private JPanel content;
   private JComponent centerContent;
   protected JButton okButton;
   protected JButton cancelButton;
   protected JButton closeButton;
   private int type;
   private boolean closedWithOk = false;

   /**
    * Default constructor.
    */
   protected AbstractDialog()
   {
      this(OK_CANCEL_TYPE);
   }

   protected AbstractDialog(int type)
   {
      this(type, (Frame) null);
   }

   protected AbstractDialog(int type, Frame parent)
   {
      super(parent);
      this.type = type;

      createPanel();
      setModal(true);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            cancelButton.doClick();
         }
      }
      );
   }

   protected AbstractDialog(Frame parent)
   {
      this(OK_CANCEL_TYPE, parent);
   }

   protected AbstractDialog(int type, Dialog parent)
   {
      super(parent);
      this.type = type;

      createPanel();
      setModal(true);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            cancelButton.doClick();
         }
      }
      );
   }

   protected AbstractDialog(Dialog parent)
   {
      this(OK_CANCEL_TYPE, parent);
   }

   /**
    * Handles action events and provides default behavior for the buttons.
    *  @param event An ActionEvent
    */
   public void actionPerformed(ActionEvent event)
   {
      try
      {
         setWaiting(true);
         if (event.getSource() == okButton)
         {
            boolean close = false;
            try
            {
               validateSettings();
               close = true;
            }
            catch (ValidationException e)
            {
               close = ValidationExceptionDialog.showDialog(this, e);
               setAlwaysOnTop(true);
            }
            if (close)
            {
               try
               {
                  onOK();
                  setClosedWithOk(true);
                  setVisible(false);
               }
               catch (PublicException ex)
               {
                  ErrorDialog.showDialog(this, null, ex);
               }
               catch (Exception e)
               {
                  trace.warn("", e);
                  ErrorDialog.showDialog(this, null, e);
               }
            }
         }
         else if (event.getSource() == cancelButton)
         {
            try
            {
               onCancel();
               setClosedWithOk(false);
               setVisible(false);
            }
            catch (Exception _ex)
            {
               trace.warn("", _ex);
               ErrorDialog.showDialog(this, null, _ex);
            }
         }
         else if (event.getSource() == closeButton)
         {
            try
            {
               onClose();
               setClosedWithOk(false);
               setVisible(false);
            }
            catch (Exception _ex)
            {
               trace.warn("", _ex);
               ErrorDialog.showDialog(this, null, _ex);
            }
         }
      }
      finally
      {
         setWaiting(false);
      }
   }

   /**
    * Checks wheter a mandatory ComboBox contains a selection or not.
    * If there is no selection an InternalException is throw.
    */
   protected void checkMandatoryComboBox(JComboBox box, String boxName) throws ValidationException
   {
      if ((box != null)
            && (box.isEnabled())
      )
      {
         if (box.getSelectedItem() == null)
         {
            box.requestFocus();
            throw new ValidationException("Mandatory ComboBox '"
                  + removeEndingColon(boxName) + "' contains no selection.", false);
         }
      }
   }

   /**
    * Checks wheter a mandatory field contains a value or not.
    * If the field is empty an InternalException is throw.
    */
   protected void checkMandatoryField(Entry entry, String fieldName) throws ValidationException
   {
      if ((entry != null)
            && (entry.isEnabled())
            && (!entry.isReadonly())
            && (entry.isMandatory())
            && (entry.isEmpty())
      )
      {
         if (entry.getWrappedComponent() != null)
         {
            entry.getWrappedComponent().requestFocus();
         }
         throw new ValidationException("Empty mandatory field '"
               + removeEndingColon(fieldName) + "'.", false);
      }
   }

   /**
    * Checks wheter a mandatory keybox contains a selection or not.
    * If there is no selection an InternalException is throw.
    */
   protected void checkMandatoryKeyBox(KeyBox keyList, String listName) throws ValidationException
   {
      if ((keyList != null) && (keyList.isEnabled())
      )
      {
         if (keyList.getValue() == null)
         {
            keyList.requestFocus();
            throw new ValidationException("Mandatory KeyBox '"
                  + removeEndingColon(listName) + "' contains no selection.", false);
         }
      }
   }

   /**
    * Checks wheter a mandatory list contains a selection or not.
    * If there is no selection an InternalException is throw.
    */
   protected void checkMandatoryList(JList list, String listName) throws ValidationException
   {
      if ((list != null) && (list.isEnabled())
      )
      {
         if (((list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)
               && (list.getSelectedValue() == null)
               )
               || ((list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION)
               && (list.getSelectedValues().length == 0)
               )
         )
         {
            list.requestFocus();
            throw new ValidationException("Mandatory list '"
                  + removeEndingColon(listName) + "' contains no selection!", false);
         }
      }
   }

   /**
    * Creates the button panel for AbstractDialog
    * e.g.: OK and Cancel button
    */
   protected JPanel createButtonPanel()
   {
      JPanel _buttonPanel = new JPanel(new FlowLayout());

      okButton = new JButton("OK");
      okButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
      okButton.addActionListener(this);
      _buttonPanel.add(okButton);

      cancelButton = new JButton("Cancel");
      cancelButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
      cancelButton.addActionListener(this);
      _buttonPanel.add(cancelButton);

      closeButton = new JButton("Close");
      closeButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
      closeButton.addActionListener(this);
      _buttonPanel.add(closeButton);

      return _buttonPanel;
   }

   /**
    * handles the creation of content for client area
    * Note: Must be implemented by subclasses
    */
   protected abstract JComponent createContent();

   /**
    * Creates the default gui for AbstractDialog
    * e.g.: OK and Cancel button
    */
   private void createPanel()
   {
      content = new JPanel();

      content.setLayout(new BorderLayout());
      content.setBorder(GUI.getEmptyPanelBorder());

      content.add(BorderLayout.SOUTH, createButtonPanel());
      centerContent = createContent();
      content.add(BorderLayout.CENTER, centerContent);

      // @optimize ... Try to find a more elegant way to "catch" KeyEvent from the hole
      //               panel. The registerKeyboardAction() doesn't catch all keys

      //      content.registerKeyboardAction(this, RETURN_CMD
      //                                     , KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)
      //                                     , JComponent.WHEN_IN_FOCUSED_WINDOW);
      GUI.registerKeyListener(content, this);

      content.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

      setContentPane(content);

      setType(type);
   }

   /**
    *
    */
   protected void doClickCancelButton()
   {
      if (cancelButton != null)
      {
         cancelButton.doClick();
      }
   }

   /**
    *
    */
   protected void doClickCloseButton()
   {
      if (closeButton != null)
      {
         closeButton.doClick();
      }
   }

   /**
    *
    */
   protected void doClickOkButton()
   {
      if (okButton != null)
      {
         okButton.doClick();
      }
   }

   /**
    *
    */
   public JComponent getCenterContent()
   {
      return centerContent;
   }

   /**
    * returns true if the dialog was closed with the button "ok"
    * @return boolean
    */
   public boolean isClosedWithOk()
   {
      return closedWithOk;
   }

   /**
    * Default handler when cancel-button has been pressed.
    *
    * Raise an exception to abort the dialog-close-process
    */
   public void onCancel()
   {
   }

   /**
    * Default handler when close-button has been pressed.
    *
    */
   public void onClose()
   {
   }

   /**
    * Default handler when OK button has been pressed.
    *
    * Raise an exception to abort the dialog-close-process
    */
   public void onOK()
   {
   }

   public abstract void validateSettings() throws ValidationException;

   protected void processKeyEvent(KeyEvent event)
   {
   }

   /**
    * Removes a colon at the end of the string.
    */
   protected String removeEndingColon(String aString)
   {
      return aString.lastIndexOf(COLON_CHAR) == aString.trim().length() - 1 ?
            aString.substring(0, aString.lastIndexOf(COLON_CHAR)) : aString;
   }

   /**
    * @param newClosedWithOk boolean
    */
   protected void setClosedWithOk(boolean newClosedWithOk)
   {
      closedWithOk = newClosedWithOk;
   }

   /**
    */
   public void setType(int newType)
   {
      type = newType;

      if ((okButton != null) && (cancelButton != null) && (closeButton != null))
      {
         okButton.setVisible((type == OK_CANCEL_TYPE) || (type == OK_TYPE));
         cancelButton.setVisible(type == OK_CANCEL_TYPE);
         closeButton.setVisible(type == CLOSE_TYPE);

         if ((type == OK_CANCEL_TYPE) || (type == OK_TYPE))
         {
            okButton.requestDefaultFocus();
            okButton.setFocusPainted(true);
            getRootPane().setDefaultButton(okButton);
         }
         else if (type == CLOSE_TYPE)
         {
            closeButton.requestDefaultFocus();
            closeButton.setFocusPainted(true);
            getRootPane().setDefaultButton(closeButton);
         }
         else
         {
            Assert.lineNeverReached();
         }
      }
   }

   /**
    * Shows or hides this component depending on the value of parameter
    *
    * Reset the Flag "closed with ok" if the dialog comes visible
    */
   public void setVisible(boolean makeVisible)
   {
      if (makeVisible)
      {
         setClosedWithOk(false);
         doLayout();
      }
      super.setVisible(makeVisible);
   }

   /**
    * Sets the dialog waiting/not waiting cursor. During waiting state, the wait
    *  cursor is displayed and all input is blocked for the dialog.
    * <p>
    *  @param busy Indicates wheter to show the wait cursor or not
    */
   public void setWaiting(boolean busy)
   {
      GUI.setWaiting(content, busy);
   }

   /** Brings up the dialog
    * @param title The title shown in the titlebar of the dialog
    * @param singleton The needed singleton
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(String title, AbstractDialog singleton)
   {
      Assert.isNotNull(singleton);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      singleton.pack();
      singleton.setLocation(screenSize.width / 2 - singleton.getSize().width / 2,
            screenSize.height / 2 - singleton.getSize().height / 2);
      singleton.setTitle(title);
      singleton.show();

      return singleton.isClosedWithOk();
   }

   /** Brings up the dialog
    * @param title The title shown in the titlebar of the dialog
    * @param singleton The needed singleton
    * @param x horizontal location on the screen
    * @param y vertical location on the screen
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(String title, AbstractDialog singleton,
         int x, int y)
   {
      Assert.isNotNull(singleton);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      singleton.pack();
      singleton.setLocation(screenSize.width / 2 - singleton.getSize().width / 2,
            screenSize.height / 2 - singleton.getSize().height / 2);
      singleton.setTitle(title);
      singleton.show();

      return singleton.isClosedWithOk();
   }

   /** Brings up the dialog
    * @param title The title shown in the titlebar of the dialog
    * @param singleton The needed singleton
    * @param parent The parent of the dialog (when the dialog is a child of another dialog)
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(String title, AbstractDialog singleton,
         JDialog parent)
   {
      Assert.isNotNull(singleton);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      singleton.pack();

      if (parent == null)
      {
         singleton.setLocation(screenSize.width / 2 - singleton.getSize().width / 2,
               screenSize.height / 2 - singleton.getSize().height / 2);
      }
      else
      {
         singleton.setLocation(Math.min(parent.getSize().width / 2 - singleton.getSize().width / 2,
               screenSize.width / 2 - singleton.getSize().width / 2),
               Math.min(parent.getSize().height / 2 - singleton.getSize().height / 2,
                     screenSize.height / 2 - singleton.getSize().height / 2));
      }

      singleton.setTitle(title);
      singleton.show();

      return singleton.isClosedWithOk();
   }

   /** Brings up the dialog
    *  usage: TestDialog.showDialog("Test", dialog);
    * @param title The title shown in the titlebar of the dialog
    * @param singleton The needed singleton
    * @param parent The parent of the dialog (when the dialog is a child of a JFrame)
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(String title, AbstractDialog singleton,
         JFrame parent)
   {
      Assert.isNotNull(singleton);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      singleton.pack();

      if (parent == null)
      {
         singleton.setLocation(screenSize.width / 2 - singleton.getSize().width / 2,
               screenSize.height / 2 - singleton.getSize().height / 2);
      }
      else
      {
         singleton.setLocation(
               Math.min
               (
                     Math.max(0, parent.getX() + parent.getSize().width / 2 - singleton.getSize().width / 2),
                     screenSize.width - singleton.getSize().width
               ),
               Math.min
               (
                     Math.max(0, parent.getY() + parent.getSize().height / 2 - singleton.getSize().height / 2),
                     screenSize.height - singleton.getSize().height
               ));
      }

      singleton.setTitle(title);
      singleton.show();

      return singleton.isClosedWithOk();
   }

   /**
    * Invoked when a key has been typed.
    * This event occurs when a key press is followed by a key release.
    */
   public void keyTyped(KeyEvent e)
   {
      // intentionally empty
   }

   /**
    * Invoked when a key has been pressed.
    */
   public void keyPressed(KeyEvent e)
   {
      // intentionally empty
   }

   /**
    * Invoked when a key has been released.
    */
   public void keyReleased(KeyEvent e)
   {
      if ((!e.isAltDown())
            && (!e.isAltGraphDown())
            && (!e.isShiftDown())
            && (!e.isControlDown())
            && (!e.isMetaDown())
            && (!e.isConsumed())
      )
      {
         if (e.getKeyCode() == KeyEvent.VK_ENTER)
         {
            if (!(e.getSource() instanceof TextArea)
                  && !(e.getSource() instanceof TextEditor)
                  && !(e.getSource() instanceof JTextPane)
            )
            {
               trace.debug("___________*** keyReleased with Enter ***____");
               trace.debug("___________    source: " + e.getSource().getClass().toString());

               if (getRootPane().getDefaultButton() == null)
               {
                  okButton.doClick();
               }
               else
               {
                  getRootPane().getDefaultButton().doClick();
               }
            }

         }
         else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
         {
            trace.debug("___________*** keyReleased with Escape ***____");
            cancelButton.doClick();
         }

      }
   }

   protected void setFocusTo(final JComponent component)
   {
      component.requestFocus();
      if (isDisplayable())
      {
         component.addFocusListener(new FocusAdapter()
         {
            public void focusLost(FocusEvent e)
            {
               component.requestFocus();
               component.removeFocusListener(this);
            }
         });
      }
   }

}
