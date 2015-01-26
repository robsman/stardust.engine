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
package org.eclipse.stardust.engine.core.compatibility.diagram;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class TextSymbol extends AbstractNodeSymbol
      implements DocumentListener, KeyListener, EditableSymbol
{
   private static final Logger trace = LogManager.getLogger(TextSymbol.class);

   private static Font font = new Font("Arial", Font.PLAIN, 11);
   private static JLabel renderer = new JLabel()
   {
      /**
       * (fh) Overridden for performance reasons.
       * See the DefaultTreeCellRenderer for more information.
       */
      public void validate() {}
      public void revalidate() {}
      public void repaint(long tm, int x, int y, int width, int height) {}
      public void repaint(Rectangle r) {}
      protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
      {
         if (propertyName.equals("text"))
         {
            super.firePropertyChange(propertyName, oldValue, newValue);
         }
      }
      public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
      public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
      public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
      public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
      public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
      public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
      public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
      public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
   };

   private String text;
   private boolean isTextMandatory;

   private transient JTextField field;
   private transient Vector listeners;

   public TextSymbol()
   {
      this(null);
   }

   /**
    * Creates a TextSymbol with a mandatory Text.
    */
   public TextSymbol(String text)
   {
      this(text, true);
   }

   /**
    * Creates a TextSymbol
    * 
    * @param isTextMandatory defines wheter the text is mandatory or not
    */
   public TextSymbol(String text, boolean isTextMandatory)
   {
      this.isTextMandatory = isTextMandatory;
      listeners = new Vector();
      field = null;
      setText(text);
   }

   public Component activateEditing()
   {
      field = new JTextField(text);
      field.setFont(font);
      field.setForeground(Color.black);
      field.setOpaque(true);
      field.setBorder(null);
      field.setBounds(getX(), getY(), field.getPreferredSize().width, field.getPreferredSize().height);
      field.addKeyListener(this);
      field.getDocument().addDocumentListener(this);
      field.selectAll();
      return field;
   }

   public void addListener(TextSymbolListener listener)
   {
      listeners.add(listener);
   }

   public void changedUpdate(DocumentEvent event)
   {
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      TextSymbol copy = new TextSymbol(getText(), isTextMandatory);
      copy.setX(getX());
      copy.setY(getY());
      copy.setParent(getParent());

      return copy;
   }

   public void deactivateEditing()
   {
      Assert.isNotNull(field);

      String text = field.getText();
      if (!isTextMandatory || ((text != null) && (text.length() > 0)))
      {
         this.text = text;
         TextSymbolEvent event = new TextSymbolEvent(this, text);
         for (Iterator i = listeners.iterator(); i.hasNext();)
         {
            ((TextSymbolListener) i.next()).textSymbolDeselected(event);
         }
      }
      else if (getDrawArea() != null)
      {
         getDrawArea().notifyAllStatusListeners("The symbol text cannot be empty!", true);
      }
      field = null;
   }

   public void draw(Graphics g)
   {
      if (field == null)
      {
         g.translate(getX(), getY());
         renderer.setText(getText());
         renderer.setSize(getWidth() + 2, getHeight());
//         renderer.setOpaque(false);
         renderer.paint(g);
         g.translate(-getX(), -getY());
      }
   }

   public String getText()
   {
      return field == null ? text : field.getText();
   }

   public int getHeight()
   {
      return Toolkit.getDefaultToolkit().getFontMetrics(font).getHeight();
   }

   public int getWidth()
   {
      return Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(getText());
   }

   public void insertUpdate(DocumentEvent event)
   {
      notifyOnTextFieldChange();
   }

   /**
    * Invoked when a key has been pressed.
    */
   public void keyPressed(KeyEvent e)
   {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE
            || e.getKeyCode() == KeyEvent.VK_ENTER)
      {
         Assert.isNotNull(getDrawArea());
         if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
         {
            // (fh) Escape will revert to previous text.
            field.setText(text);
         }
         getDrawArea().deactivateEditing();
      }
   }

   /**
    * Invoked when a key has been released.
    */
   public void keyReleased(KeyEvent e)
   {
   }

   /**
    * Invoked when a key has been typed.
    */
   public void keyTyped(KeyEvent e)
   {
   }

   public void mouseMoved(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      }
   }

   public void move(int xDelta, int yDelta)
   {
      super.move(xDelta, yDelta);
      if (field != null)
      {
         field.setLocation(xDelta, yDelta);
      }
   }

   private void notifyOnTextFieldChange()
   {
      if (field != null)
      {
         field.setSize(getWidth() + 2, field.getHeight());
      }
      TextSymbolEvent event = new TextSymbolEvent(this, field.getText());
      for (Iterator i = listeners.iterator(); i.hasNext();)
      {
         ((TextSymbolListener) i.next()).textSymbolChanged(event);
      }
   }

   public void onPress(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         if (getDrawArea() != null)
         {
            getDrawArea().activateEditing(this);
         }
         else
         {
            trace.debug("[!!!] drawArea is null !!!");
         }
      }
   }

   public void removeListener(TextSymbolListener listener)
   {
      listeners.remove(listener);
   }

   public void removeUpdate(DocumentEvent event)
   {
      field.repaint();
      notifyOnTextFieldChange();
   }

   public void setFont(Font font)
   {
      this.font = font;
   }

   public boolean setPoint(int x, int y)
   {
      setX(x);
      setY(y);
      return false;
   }

   public void setText(String text)
   {
      markModified();
      if (isTextMandatory)
      {
         Assert.isNotNull(text, "Mandatory text is not null");
      }

      this.text = text;
      if (field != null)
      {
         field.setText(text);
      }
      // @todo sometimes getDrawArea() is null. no idea why.		getDrawArea().repaint();
   }

   public void setX(int x)
   {
      super.setX(x);
      if (field != null)
      {
         field.setLocation(getX(), getY());
      }
   }

   public void setY(int y)
   {
      super.setY(y);
      if (field != null)
      {
         field.setLocation(getX(), getY());
      }
   }
}
