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

/**
 * @author Mark Gille, j.talk() GmbH
 * @version 	%I%, %G%
 */

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.eclipse.stardust.common.Assert;


/**
 *	Abstract base class for entry fields. Handles the mandatory and readonly
 * behavior and appearance.
 *
 * This class implements a proxy pattern for the class JTextField.
 */
public abstract class AbstractEntry extends JPanel
      implements Entry, FocusListener, DocumentListener
{
   private JTextField field;
   private JLabel flagLabel;
   protected boolean mandatory = false;
   protected boolean readonly = false;

   /**
    *	Default constructor.
    */
   public AbstractEntry()
   {
      this(20);
   }

   /**
    * Constructor that sets the visual size of this entry field.
    */
   public AbstractEntry(int size)
   {
      this(new JTextField(size), false, false);
   }

   /**
    * Constructor that sets the visual size and the mandatory flag of this entry
    *	field.
    */
   public AbstractEntry(int size, boolean isMandatory)
   {
      this(new JTextField(size), isMandatory, false);
   }

   /**
    * Constructor that sets the visual size of this entry field to the size of
    * the provided string.
    */
   public AbstractEntry(String text)
   {
      this(new JTextField(text), false, false);
   }

   /**
    * Protected Constructor that uses a textfield.
    * This constructor can be used for specialized text fields (like JPasswordField)
    */
   protected AbstractEntry(JTextField textField)
   {
      this(textField, false, false);
   }

   /**
    * Protected Constructor that uses a textfield.
    * This constructor can be used for specialized text fields (like JPasswordField)
    */
   protected AbstractEntry(JTextField textField, boolean isMandatory, boolean isReadonly)
   {
      this.field = textField;

      mandatory = isMandatory;
      readonly = isReadonly;

      initialize();
   }

   /**
    *
    */
   public void addFocusListener(FocusListener l)
   {
      // Can be called in base class constructor

      if (field != null)
      {
         field.addFocusListener(l);
      }
   }

   /**
    *
    */
   public void addKeyListener(KeyListener l)
   {
      // Can be called in base class constructor
      if (field != null)
      {
         field.addKeyListener(l);
      }
   }

   /**
    * Gives notification that an attribute or set of attributes changed.
    */
   public void changedUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    *
    */
   protected Document createDefaultModel()
   {
      return new DefaultDocument(this);
   }

   /**
    * Invoked when a component gains the keyboard focus.
    * Used to skip the madatory/optional labels.
    */
   public void focusGained(FocusEvent e)
   {
      // Hint: I added the class as focuslistener to the contained
      //       JTextField to implement the code for select the content. [aj]

      if (e.getSource() == flagLabel)
      {
         transferFocus();
      }

      if (isEnabled() && !isReadonly())
      {
         if (getTextField() != null)
         {
            getTextField().selectAll();
         }
      }
   }

   /**
    * Invoked when a component loses the keyboard focus.
    */
   public void focusLost(FocusEvent e)
   {
   }

   /**
    *	Gets the border of the entry.
    */
   public Border getBorder()
   {
      return null;
   }

   /**
    *	Gets the caret position of the entry.
    */
   public int getCaretPosition()
   {
      return field.getCaretPosition();
   }

   /**
    *	Gets the columns of the entry.
    */
   public int getColumns()
   {
      return field.getColumns();
   }

   /**
    *	Gets the document of the entry.
    */
   public Document getDocument()
   {
      return field.getDocument();
   }

   /**
    *
    */
   public boolean getEnabled()
   {
      return super.isEnabled();
   }

   /**
    *	Gets the horizontal alignment of the entry.
    */
   public int getHorizontalAlignment()
   {
      return field.getHorizontalAlignment();
   }

   /**
    *	Gets mandatory status.
    */
   public boolean getMandatory()
   {
      return mandatory;
   }

   /**
    *	@return <code>true</code> if the entry is readonly. <code>false</code>
    *         otherwise.
    */
   public boolean getReadonly()
   {
      return isReadonly();
   }

   /**
    *	Gets the text of the entry.
    */
   public String getText()
   {
      return field.getText();
   }

   /**
    * Returns the text field wrapped by this object.
    */
   public JTextField getTextField()
   {
      return field;
   }

   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent()
   {
      return field;
   }

   /**
    *	Do the initialization (called from constructors).
    */
   protected void initialize()
   {
      // Initialize document

      Document document = createDefaultModel();

      field.setDocument(document);
      document.addDocumentListener(this);

      // Layout panel

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      flagLabel = new JLabel(GUI.getMandatoryIcon());

      flagLabel.addFocusListener(this);

      if (getTextField() != null)
      {
         getTextField().addFocusListener(this);
      }

      add(flagLabel);
      add(Box.createHorizontalStrut(3));
      add(field);

      if (field.getColumns() > 0)
      {
         setMaximumSize(new Dimension(getPreferredSize().width, field.getPreferredSize().height));
         setMinimumSize(new Dimension(getMinimumSize().width, field.getPreferredSize().height));
      }
      else
      {
         setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
         setPreferredSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
         setMinimumSize(new Dimension(getMinimumSize().width, getPreferredSize().height));
      }

      performFlags();
      field.setCursor(GUI.ENTRY_CURSOR);
   }

   /**
    * Gives notification that there was an insert into the document.
    */
   public void insertUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    *	Gets the editable of the entry.
    */
   public boolean isEditable()
   {
      return field.isEditable();
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return (getText() == null || getText().length() == 0);
   }

   /**
    *
    */
   public boolean isEnabled()
   {
      return super.isEnabled();
   }

   /**
    *	Gets mandatory status.
    */
   public boolean isMandatory()
   {
      return getMandatory();
   }

   /**
    *	@return <code>true</code> if the entry is readonly. <code>false</code>
    *         otherwise.
    */
   public boolean isReadonly()
   {
      return readonly;
   }

   /**
    *	Check mandatory and is editable state for color settings.
    */
   protected void performFlags()
   {
      Assert.isNotNull(field);
      Assert.isNotNull(flagLabel);

      if (!isEnabled())
      {
         field.setBackground(GUI.DisabledColor);
         field.setForeground(GUI.DisabledTextColor);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else if (isReadonly())
      {
         field.setBackground(GUI.ReadOnlyColor);
         field.setForeground(GUI.ReadOnlyTextColor);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else // regular state
      {
         field.setBackground(GUI.DefaultColor);
         field.setForeground(GUI.DefaultTextColor);

         if (mandatory && (getText() == null || getText().length() == 0))
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
      }
      repaint();
   }

   /**
    *
    */
   public void removeFocusListener(FocusListener l)
   {
      // Can be called in base class constructor

      if (field != null)
      {
         field.addFocusListener(l);
      }
   }

   /**
    *
    */
   public void removeKeyListener(KeyListener l)
   {
      // Can be called in base class constructor

      if (field != null)
      {
         field.addKeyListener(l);
      }
   }

   /**
    * Gives notification that a portion of the document has been removed.
    */
   public void removeUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    *	Sets the border of the entry.
    */
   public void setBorder(Border border)
   {
      if (field == null)
      {
         return;
      }

      field.setBorder(border);
   }

   /**
    *	Sets the columns of the entry.
    */
   public void setColumns(int columns)
   {
      field.setColumns(columns);
   }

   /**
    *	Sets the editable of the entry.
    */
   public void setEditable(boolean editable)
   {
      field.setEditable(editable);
      performFlags();
   }

   /**
    *
    */
   public void setEnabled(boolean isEnabled)
   {
      super.setEnabled(isEnabled);
      if (!isEnabled)
      {
         setText(null);
      }
      field.setEnabled(isEnabled);
      performFlags();
   }

   /**
    *	Sets the horizontal alignment of the entry.
    */
   public void setHorizontalAlignment(int horizontalAlignment)
   {
      field.setHorizontalAlignment(horizontalAlignment);
   }

   /**
    *	En/disables appearance for mandatory input.
    */
   public void setMandatory(boolean mandatory)
   {
      this.mandatory = mandatory;
      performFlags();
   }

   /**
    *	Sets readonly state.
    */
   public void setReadonly(boolean readonly)
   {
      this.readonly = readonly;

      field.setEditable(!readonly);
      performFlags();
   }

   /**
    *	Sets the text of the entry.
    */
   public void setText(String text)
   {
      field.setText(text);
   }

   /** Sets focus on the receiving component if isRequestFocusEnabled
    *  returns true and the component doesn't already have focus. **/
   public void requestFocus()
   {
      super.requestFocus();
      field.requestFocus();
   }

   /**
    * Marks, wether the key box is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
      if (isCell)
      {
         removeAll();
         add("West", field);
         setBackground(field.getBackground());
         field.setBorder(null);
         field.setPreferredSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
      }
   }
}
