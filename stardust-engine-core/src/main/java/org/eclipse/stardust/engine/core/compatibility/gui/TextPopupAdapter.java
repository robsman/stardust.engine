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

// java imports

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/** PopupAdapter for JTextComponents that allows to choose between given

 texts */

public abstract class TextPopupAdapter extends PopupAdapter

{

   protected String choices[];

   public TextPopupAdapter(JTextComponent Invoker, String choices[])

   {

      this.Invoker = Invoker;

      this.choices = choices;

      Invoker.addMouseListener(this);

      structor();

   }

   public static TextPopupAdapter create(JTextComponent invoker,

         String choices[])

   {

      return new TextPopupAdapter(invoker, choices)

      {

         protected JPopupMenu invokePopup;

         public void structor()

         {

            invokePopup = new JPopupMenu();

            if (this.choices == null)

            {// no string given

               return;

            }

            for (int i = 0; i < this.choices.length; i++)

            {

               final JMenuItem item = invokePopup.add(this.choices[i]);

               item.addActionListener(new ActionListener()

               {

                  public void actionPerformed(java.awt.event.ActionEvent e)

                  {

                     ((JTextComponent) Invoker).replaceSelection(item.getText());

                     //((JTextComponent)Invoker).setText(item.getText());

                  }

               });

            }// end for



            //String not longer needed -> just delete it

            this.choices = null;

         }

         public void doPopup(int X, int Y)

         {

            JTextComponent component = (JTextComponent) Invoker;

            if ((!component.isEditable())

                  && (!component.isEnabled()))

            {

               return;

            }

            invokePopup.show(this.Invoker, X + 1, Y + 1);

         }

      };

   }

}// TextPopupAdapter


