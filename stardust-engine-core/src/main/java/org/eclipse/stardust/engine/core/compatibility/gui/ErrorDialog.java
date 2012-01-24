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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;

import org.eclipse.stardust.common.error.PublicException;


/**
 * A message dialog for showing error information. It can optionally show the
 * exception stacktrace.
 * <p>
 * The usage is like follows:
 * <code>ErrorDialog.showDialog(frame, message, exception);</code>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ErrorDialog extends JDialog
{
   private static final String SHOW_DETAILS = "Show Details";
   private static final String HIDE_DETAILS = "Hide Details";
   private static final String PROPERTY_CHANGE_EVENT_INIT_VALUE = "-1";
   private static final String OK = "OK";
   private JButton okButton;
   private Throwable throwable;
   private JTextArea detailsText;
   private boolean isShowingDetails;
   private JButton detailsButton;
   private String stackTrace;
   private JPanel details;
   private JTextArea messageLabel;
   private JScrollPane detailsPane;
   private JOptionPane optionPane;

   /**
    *
    */
   protected ErrorDialog(Dialog owner)
   {
      super(owner, "Warning !");
      init();
   }

   /**
    *
    */
   protected ErrorDialog(Frame owner)
   {
      super(owner, "Warning !");
      init();
   }

   /**
    *
    */
   protected void init()
   {
      setModal(true);

      final String btnString1 = OK;
      final String btnString2 = SHOW_DETAILS;
      final String btnString3 = HIDE_DETAILS;
      final Object[] showDetailOptions = {btnString1, btnString2};
      final Object[] hideDetailOptions = {btnString1, btnString3};

      details = new JPanel();
      details.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Exception Details"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
      ));
      detailsText = new JTextArea("");
      detailsText.setEditable(false);
      detailsPane = new JScrollPane(detailsText);

      details.add(detailsPane);
      details.setVisible(false);

      messageLabel = new JTextArea();
      messageLabel.setEditable(false);
      messageLabel.setDisabledTextColor(Color.black);
      messageLabel.setOpaque(false);

      messageLabel.setFont(Font.getFont("Times New Roman"));

      Object[] array = {details, messageLabel};


      optionPane = new JOptionPane(array,
            JOptionPane.WARNING_MESSAGE,
            JOptionPane.OK_OPTION,
            null,
            showDetailOptions,
            showDetailOptions[0]);

      final JButton _okButton = (JButton)
            ((JPanel)optionPane.getComponent(1)).getComponent(0);

      messageLabel.addFocusListener(new FocusListener()
      {
         public void focusGained(FocusEvent e)
         {
            _okButton.requestFocus();
         }

         public void focusLost(FocusEvent e)
         {
         }
      });
      optionPane.addPropertyChangeListener(new PropertyChangeListener()
         {

            public void propertyChange(PropertyChangeEvent evt)
            {
               Object evtSource = evt.getNewValue();
               if (SHOW_DETAILS.equals(evtSource))
               {
                  showDetails();
                  optionPane.setOptions(hideDetailOptions);
               }
               else if (HIDE_DETAILS.equals(evtSource))
               {
                  hideDetails();
                  optionPane.setOptions(showDetailOptions);
               }
               else if (evtSource != null &&
                     PROPERTY_CHANGE_EVENT_INIT_VALUE.equals(evtSource.toString()))
               {
                  _okButton.doClick();
               }
               else if (OK.equals(evtSource))
               {
                  setVisible(false);
               }
            }
         });

      setContentPane(optionPane);
      setResizable(false);
   }
   /**
    *
    */
   private void showDetails()
   {
      detailsPane.setPreferredSize(new Dimension(
            Math.max((int) messageLabel.getSize().getWidth(), 350), 200));
      detailsText.setText(stackTrace);
//      detailsText.setEditable(false);
      details.setVisible(true);
      pack();

   }

   /**
    *
    */
   private void hideDetails()
   {
      details.setVisible(false);
      pack();
   }

   /**
    *
    */
   public static void showDialog(Component owner, String message, Throwable e)
   {
      ErrorDialog dialog;
      if (owner instanceof Frame)
      {
         dialog =  new ErrorDialog((Frame)owner);
      }
      else if (owner instanceof Dialog)
      {
         dialog =  new ErrorDialog((Dialog)owner);
      }
      else
      {
         dialog = new ErrorDialog((Frame)null);
      }
      dialog.setThrowable(message, e);
      dialog.pack();
      dialog.setLocationRelativeTo(owner);
      dialog.setVisible(true);
   }
   /**
    *
    */
   public void setThrowable(String message, Throwable e)
   {
      throwable = e;
      if (throwable == null || throwable instanceof PublicException)
      {
         stackTrace = "No Details available.";
      }
      else
      {
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         throwable.printStackTrace(writer);
         stackTrace = stringWriter.getBuffer().toString();
      }
      if (message != null && message.length() != 0)
      {
         messageLabel.setText(message);
      }
      else if (e.getLocalizedMessage() != null && e.getLocalizedMessage().length() != 0)
      {
         messageLabel.setText(e.getLocalizedMessage());
      }
      else
      {
         messageLabel.setText(e.getClass().getName());
      }
   }

   /**
    *
    */
   public static void main(String[] args)
   {

      final JFrame frame = new JFrame("bla");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      JPanel contents = new JPanel();
      frame.setContentPane(contents);

      final JLabel label = new JLabel("Waiting for you:");
      contents.add(label);

      JButton pressMe = new JButton("Show Dialog");
      pressMe.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               throw new Exception();
            }
            catch (Exception ex)
            {
               showDialog(frame,
                     "guck ma hier ein supalanga text hier kommt ein umbruch mal "
                     + "sehn ob's was wird:\nnaja mal sehen was wir noch sehen.", ex);
               showDialog(frame, "without exception", null);
               showDialog(frame, null, ex);
            }
         }
      });
      contents.add(pressMe);

      frame.pack();
      frame.setVisible(true);
   }
}
