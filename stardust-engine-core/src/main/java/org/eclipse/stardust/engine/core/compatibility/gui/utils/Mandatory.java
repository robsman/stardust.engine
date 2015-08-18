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
package org.eclipse.stardust.engine.core.compatibility.gui.utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;


public class Mandatory extends JLabel
{
   private static final long serialVersionUID = 1L;

   private final JComponent[] watched;
   
   private final String message;
   private final boolean canClose;

   private boolean all;
   private Set problems = new HashSet();

   public Mandatory(String message, JComponent watched, boolean canClose)
   {
      this(message, new JComponent[]{watched}, canClose, true);
   }

   public Mandatory(String message, JComponent[] watched, boolean canClose, boolean all)
   {
      this.watched = watched;
      this.message = message;
      this.canClose = canClose;
      this.all = all;

      for (int i = 0; i < watched.length; )
      {
         register(watched[i++ ]);
      }
      setHorizontalAlignment(SwingConstants.CENTER);
      Dimension size = getPreferredSize();
      size.width += 6;
      size.height += 6;
      setPreferredSize(size);
   }

   private void register(JComponent component)
   {
      if (component instanceof JTextComponent)
      {
         final JTextComponent textComponent = (JTextComponent) component;
         textComponent.getDocument().addDocumentListener(new DocumentListener()
         {
            public void insertUpdate(DocumentEvent e)
            {
               checkDocument(textComponent, e.getDocument());
            }

            public void removeUpdate(DocumentEvent e)
            {
               checkDocument(textComponent, e.getDocument());
            }

            public void changedUpdate(DocumentEvent e)
            {
               checkDocument(textComponent, e.getDocument());
            }
         });
         checkDocument(textComponent, textComponent.getDocument());
      }
      else if (component instanceof AbstractButton)
      {
         AbstractButton abstractButton = (AbstractButton) component;
         abstractButton.addChangeListener(new ChangeListener()
         {
            public void stateChanged(ChangeEvent e)
            {
               checkButton((AbstractButton) e.getSource());
            }
         });
         checkButton(abstractButton);
      }
      else if (component instanceof JComboBox)
      {
         ((JComboBox) component).addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               checkCombo((JComboBox) e.getSource());
            }
         });
         checkCombo((JComboBox) component);
      }
      else if (component instanceof ObservedEntry)
      {
         ((ObservedEntry) component).addChangeListener(new ChangeListener()
         {
            public void stateChanged(ChangeEvent e)
            {
               checkEntry((ObservedEntry) e.getSource());
            }
         });
         checkEntry((ObservedEntry) component);
      }
      else if (component instanceof JList)
      {
         ((JList) component).addListSelectionListener(new ListSelectionListener()
         {
            public void valueChanged(ListSelectionEvent e)
            {
               checkList((JList) e.getSource());
            }
         });
         checkList((JList) component);
      }

      component.addPropertyChangeListener("enabled", new PropertyChangeListener()
      {
         public void propertyChange(PropertyChangeEvent evt)
         {
            setIcon();
         }
      });
   }

   private void checkEntry(ObservedEntry entry)
   {
      if (entry.isEmpty())
      {
         if (!problems.contains(entry))
         {
            problems.add(entry);
         }
      }
      else
      {
         problems.remove(entry);
      }
      setIcon();
   }

   private void checkList(JList list)
   {
      if (list.getSelectedIndex() >= 0)
      {
         problems.remove(list);
      }
      else
      {
         if (!problems.contains(list))
         {
            problems.add(list);
         }
      }
      setIcon();
   }

   private void checkCombo(JComboBox combo)
   {
      if (combo.getSelectedIndex() >= 0)
      {
         problems.remove(combo);
      }
      else
      {
         if (!problems.contains(combo))
         {
            problems.add(combo);
         }
      }
      setIcon();
   }

   private void checkButton(AbstractButton button)
   {
      if (button.isSelected())
      {
         problems.remove(button);
      }
      else
      {
         if (!problems.contains(button))
         {
            problems.add(button);
         }
      }
      setIcon();
   }

   private void checkDocument(JTextComponent tc, Document doc)
   {
      if (doc.getLength() == 0)
      {
         if (!problems.contains(tc))
         {
            problems.add(tc);
         }
      }
      else
      {
         problems.remove(tc);
      }
      setIcon();
   }

   private void setIcon()
   {
      setIcon(isValidInput() ? GUI.getOptionalIcon() : GUI.getMandatoryIcon());
   }

   public boolean isValidInput()
   {
      int nProblems = 0;
      if (problems != null)
      {
	      for (Iterator i = problems.iterator(); i.hasNext();)
	      {
	         JComponent jcomp = (JComponent) i.next();
	         if (jcomp.isEnabled())
	         {
	            ++nProblems;
	         }
	      }
      }
      return !isEnabled() || (all ? (0 == nProblems) : (nProblems < watched.length));
   }

   public void checkMandatory() throws ValidationException
   {
      if (!isValidInput())
      {
         throw new ValidationException(message, canClose);
      }
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      setIcon();
   }
}
