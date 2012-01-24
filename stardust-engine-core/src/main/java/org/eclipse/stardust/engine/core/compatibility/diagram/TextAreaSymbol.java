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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/** */
public class TextAreaSymbol extends AbstractNodeSymbol
      implements DocumentListener, KeyListener, EditableSymbol
{
   private static final Logger trace = LogManager.getLogger(TextAreaSymbol.class);

   static final protected int MIN_HEIGHT = 15;
   static final protected int MIN_WIDTH = 30;

   public static Font font = new Font("Arial", Font.PLAIN, 11);

   private String text;

   private transient JTextArea field;
   private transient Vector listeners = new Vector();

   /** */
   public TextAreaSymbol()
   {
      this(null);
   }

   /** */
   public TextAreaSymbol(String text)
   {
      this.text = text;
      field = new JTextArea(text);
      field.setFont(font);
      field.setForeground(Color.black);
      field.setOpaque(false);
      field.setEditable(false);
      field.addKeyListener(this);
      field.getDocument().addDocumentListener(this);
   }

   /** */
   public Component activateEditing()
   {
      field.setEditable(true);
      field.selectAll();

      return field;
   }

   /** */
   public void addListener(TextSymbolListener listener)
   {
      Assert.isNotNull(listeners);

      listeners.add(listener);
   }

   /** */
   public void changedUpdate(DocumentEvent event)
   {
      notifyOnTextFieldChange();
   }

   /**
    * Creates a copy of the symbol without the associated data
    * Used for copies in diagrams. The new Symbol refers the same
    * associated data object.
    */
   public Symbol copySymbol()
   {
      TextAreaSymbol _copy = new TextAreaSymbol(getText());
      _copy.setX(getX());
      _copy.setY(getY());

      return _copy;
   }

   /** */
   public void deactivateEditing()
   {
      Assert.isNotNull(field);
      field.setSelectionEnd(0);

      field.setEditable(false);

      // @todo ... change to an eventtype, that is acceptable for TextSymbol and TextAreaSymbol
      //		 ... (?) better use PropertyChangeListener?
      TextSymbolEvent _event = new TextSymbolEvent(null, getText());
      java.util.Enumeration _enum = listeners.elements();
      if (_enum != null)
      {
         while (_enum.hasMoreElements())
         {
            ((TextSymbolListener) _enum.nextElement()).textSymbolDeselected(_event);
         }
      }
   }

   /** */
   public void draw(Graphics g)
   {
      Assert.isNotNull(field, "JTextArea-instanz is not null");

      g.translate(getX(), getY());

      field.setSize(getWidth(), getHeight());

      field.setLocation(getX(), getY());
      if (field.isEditable() == false)
      {
         field.paint(g);
      }
      g.translate(-getX(), -getY());
   }

   public int getHeight()

   {
      if (field != null)
      {
         return Math.max(field.getPreferredSize().height, MIN_HEIGHT);
      }
      else
      {
         return MIN_HEIGHT;
      }
   }

   /** */
   public String getText()
   {
      return text;
   }

   /** */
   public int getWidth()

   {
      if (field != null)
      {
         return Math.max(field.getPreferredSize().width, MIN_WIDTH);
      }
      else
      {
         return MIN_WIDTH;
      }
   }

   /** */
   public void insertUpdate(DocumentEvent event)
   {
      notifyOnTextFieldChange();
   }

   /**
    * Invoked when a key has been pressed.
    */
   public void keyPressed(KeyEvent e)
   {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
      {
         Assert.isNotNull(getDrawArea());
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

   /** */
   public void mouseMoved(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         event.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
      }
   }

   /** */
   public void move(int xDelta, int yDelta)

   {
      super.move(xDelta, yDelta);
      if (field.isEditable())
      {
         field.setLocation(field.getX() + xDelta, field.getY() + yDelta);
      }
   }

   /** */
   private void notifyOnTextFieldChange()

   {
      Assert.isNotNull(listeners);
      Assert.isNotNull(field);


      //--- save the new text to the own attribute
      this.text = field.getText();

      //--- inform the listeners
      // @todo ... change to an eventtype, that is acceptable for TextSymbol and TextAreaSymbol
      //		 ... (?) better use PropertyChangeListener?
      TextSymbolEvent event = new TextSymbolEvent(null, field.getText());
      java.util.Enumeration _enum = listeners.elements();
      if (_enum != null)
      {
         while (_enum.hasMoreElements())
         {
            try
            {
               ((TextSymbolListener) _enum.nextElement()).textSymbolChanged(event);
            }
            catch (ClassCastException _ex)
            {
               trace.debug("wrong object in TextSymbolListener-list");
            }
         }
      }
   }

   /** */
   public void onPress(MouseEvent event)
   {
      if (isHitBy(event.getX(), event.getY()))
      {
         if (getDrawArea() != null)
         {
            getDrawArea().activateEditing(this);
         }
      }
   }

   /** */
   public void removeListener(TextSymbolListener listener)
   {
      listeners.remove(listener);
   }

   /** */
   public void removeUpdate(DocumentEvent event)
   {
      notifyOnTextFieldChange();
   }

   /** */
   public void setFont(Font font)
   {
      this.font = font;
      field.setFont(font);
   }

   /** */
   public boolean setPoint(int x, int y)
   {
      setX(x);
      setY(y);

      field.setLocation(x, y);
      return false;
   }

   /** */
   public void setText(String text)
   {
      markModified();

      this.text = text;
      field.setText(text);
   }
}
