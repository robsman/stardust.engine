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
package org.eclipse.stardust.engine.core.compatibility.gui.dialogs;

import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public abstract class WizardDialog extends JDialog
{
   private JButton back;
   private JButton next;
   private JButton finish;
   private JButton cancel;
   private CardLayout layout;
   private int position;
   private JPanel wizard;
   private Component[] cards;
   private String[] cardNames;
   private static final String BACK_LABEL = "<< Back";
   private static final String NEXT_LABEL = ">> Next";
   private static final String FINISH_LABEL = "Finish";
   private static final String CANCEL_LABEL = "Cancel";

   /**
    * Creates a non-modal dialog without a title and without a specified
    * <code>Frame</code> owner.  A shared, hidden frame will be
    * set as the owner of the dialog.
    */
   public WizardDialog()
   {
      super();
   }

   /**
    * Creates a non-modal dialog without a title with the
    * specifed <code>Frame</code> as its owner.
    *
    * @param owner the <code>Frame</code> from which the dialog is displayed
    */
   public WizardDialog(Frame owner)
   {
      super(owner);
   }

   /**
    * Creates a modal or non-modal dialog without a title and
    * with the specified owner <code>Frame</code>.
    *
    * @param owner the <code>Frame</code> from which the dialog is displayed
    * @param modal  true for a modal dialog, false for one that allows
    *               others windows to be active at the same time
    */
   public WizardDialog(Frame owner, boolean modal)
   {
      super(owner, modal);
   }

   /**
    * Creates a non-modal dialog with the specified title and
    * with the specified owner frame.
    *
    * @param owner the <code>Frame</code> from which the dialog is displayed
    * @param title  the <code>String</code> to display in the dialog's
    *			title bar
    */
   public WizardDialog(Frame owner, String title)
   {
      super(owner, title);
   }

   /**
    * Creates a modal or non-modal dialog with the specified title
    * and the specified owner <code>Frame</code>.  All constructors
    * defer to this one.
    * <p>
    * NOTE: Any popup components (<code>JComboBox</code>,
    * <code>JPopupMenu</code>, <code>JMenuBar</code>)
    * created within a modal dialog will be forced to be lightweight.
    *
    * @param owner the <code>Frame</code> from which the dialog is displayed
    * @param title  the <code>String</code> to display in the dialog's
    *			title bar
    * @param modal  true for a modal dialog, false for one that allows
    *               other windows to be active at the same time
    */
   public WizardDialog(Frame owner, String title, boolean modal)
   {
      super(owner, title, modal);
   }

   /**
    * Creates a non-modal dialog without a title with the
    * specifed <code>Dialog</code> as its owner.
    *
    * @param owner the <code>Dialog</code> from which the dialog is displayed
    */
   public WizardDialog(Dialog owner)
   {
      super(owner);
   }

   /**
    * Creates a modal or non-modal dialog without a title and
    * with the specified owner dialog.
    * <p>
    *
    * @param owner the <code>Dialog</code> from which the dialog is displayed
    * @param modal  true for a modal dialog, false for one that allows
    *               other windows to be active at the same time
    */
   public WizardDialog(Dialog owner, boolean modal)
   {
      super(owner, modal);
   }

   /**
    * Creates a non-modal dialog with the specified title and
    * with the specified owner dialog.
    *
    * @param owner the <code>Dialog</code> from which the dialog is displayed
    * @param title  the <code>String</code> to display in the dialog's
    *			title bar
    */
   public WizardDialog(Dialog owner, String title)
   {
      super(owner, title);
   }

   /**
    * Creates a modal or non-modal dialog with the specified title
    * and the specified owner frame.
    *
    * @param owner the <code>Dialog</code> from which the dialog is displayed
    * @param title  the <code>String</code> to display in the dialog's
    *			title bar
    * @param modal  true for a modal dialog, false for one that allows
    *               other windows to be active at the same time
    */
   public WizardDialog(Dialog owner, String title, boolean modal)
   {
      super(owner, title, modal);
   }

   /** Called by the constructors to init the <code>JDialog</code> properly. */
   protected void dialogInit()
   {
      super.dialogInit();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(createWizardPanel(), BorderLayout.CENTER);
      getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
      getRootPane().setDefaultButton(next);
   }

   private Component createWizardPanel()
   {
      wizard = new JPanel();
      wizard.setLayout(layout = new CardLayout());
      cards = getCards();
      cardNames = getCardNames();
      for (int i = 0; i < cards.length; i++)
      {
         wizard.add(cardNames[i], cards[i]);
      }
      position = 0;
      return wizard;
   }

   protected abstract Component[] getCards();

   protected abstract String[] getCardNames();

   protected abstract void setupCurrentPanel(String cardName, Component card);

   private Component createButtonsPanel()
   {
      Box box = Box.createVerticalBox();
      box.add(new JSeparator());
      box.add(Box.createVerticalStrut(5));
      JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));

      buttonsPanel.add(back = new JButton(BACK_LABEL));
      back.setEnabled(false);
      back.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doBack();
         }
      });

      buttonsPanel.add(next = new JButton(NEXT_LABEL));
      next.setDefaultCapable(true);
      next.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doNext();
         }
      });

      buttonsPanel.add(finish = new JButton(FINISH_LABEL));
      finish.setDefaultCapable(true);
      finish.setEnabled(false);
      finish.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFinish();
         }
      });

      buttonsPanel.add(cancel = new JButton(CANCEL_LABEL));
      cancel.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doCancel();
         }
      });

      box.add(buttonsPanel);
      return box;
   }

   protected void doCancel()
   {
      dispose();
   }

   protected void doFinish()
   {
      dispose();
   }

   protected void doNext()
   {
      if (!isLastPosition())
      {
         if (isFirstPosition())
         {
            back.setEnabled(true);
         }
         position++;
         if (isLastPosition())
         {
            finish.setEnabled(true);
            next.setEnabled(false);
         }
         layout.next(wizard);
         setupCurrentPanel(cardNames[position], cards[position]);
      }
   }

   protected void doBack()
   {
      if (!isFirstPosition())
      {
         if (isLastPosition())
         {
            finish.setEnabled(false);
            next.setEnabled(true);
         }
         position--;
         if (isFirstPosition())
         {
            back.setEnabled(false);
         }
         layout.previous(wizard);
      }
   }

   private boolean isFirstPosition()
   {
      return position == 0;
   }

   private boolean isLastPosition()
   {
      return position == wizard.getComponentCount() - 1;
   }

}
