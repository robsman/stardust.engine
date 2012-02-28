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

import java.awt.Component;
import java.awt.Container;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.eclipse.stardust.common.Assert;


/**
 * MandatoryManager <P> stores the mandatory entries internally.
 *
 */
public class MandatoryManager
{
   protected static int INITIAL_VECTOR_LENGTH = 30;
   private Vector entries;
   private Vector names;
   private Vector listeners;

   /**
    * Constructor
    */
   public MandatoryManager()
   {
      entries = new Vector(INITIAL_VECTOR_LENGTH);
      names = new Vector(INITIAL_VECTOR_LENGTH);
      listeners = new Vector(INITIAL_VECTOR_LENGTH);
   }

   /**
    * Constructor with message box on empty fields.
    */
   public MandatoryManager(boolean isUsingMessageBox)
   {
      this();

      if (isUsingMessageBox)
      {
         addMessageBoxListener();
      }
   }

   /**
    * Constructor with MandatoryListener
    */
   public MandatoryManager(MandatoryListener adapter)
   {
      this();

      Assert.isNotNull(adapter);

      addListener(adapter);
   }

   /**
    * Adds a mandatory entry.
    */
   public void add(JComponent component)
   {
      Assert.isNotNull(component);

      add(component, "" + entries.size());
   }

   /**
    * Adds a mandatory entry.
    */
   public void add(JComponent component, String name)
   {
      Assert.isNotNull(component);
      Assert.isNotNull(name);

      if (component == null)
      {
         return;
      }

      entries.add(component);
      names.add(name);

      // Our entries receive a special notifcation

      if (component instanceof Entry)
      {
         ((Entry) component).setMandatory(true);
      }
   }

   /**
    * Remove a mandatory entry.
    */
   public void remove(JComponent component)
   {
      int i = entries.indexOf(component);

      if (i >= 0)
      {
         // Our entries receive a special notifcation

         if (component instanceof Entry)
         {
            ((Entry) component).setMandatory(false);
         }

         entries.removeElementAt(i);
         names.removeElementAt(i);
      }
   }

   /**
    * Remove all mandatories.
    */
   public void removeAll()
   {
      if (entries != null)
      {
         for (int i = 0; i < entries.size(); i++)
         {
            Object component = entries.elementAt(i);

            // IWEntries receive a special notifcation

            if (component instanceof Entry)
            {
               ((Entry) component).setMandatory(false);
            }
         }

         entries.removeAllElements();
         names.removeAllElements();
      }
   }

   /**
    * Adds a listener.
    */
   public void addListener(MandatoryListener listener)
   {
      Assert.isNotNull(listener);

      listeners.add(listener);
   }

   /**
    * Adds a listener.
    */
   public void removeListener(MandatoryListener listener)
   {
      listeners.remove(listener);
   }

   /**
    * Adds a message box listener with the given parent.
    */
   public void addMessageBoxListener()
   {
      addListener(createMessageBoxAdapter());
   }

   /**
    * Adds a JTextComponent listener with the given parent
    */
   public void addTextFieldListener(JTextComponent component)
   {
      addListener(createTextFieldAdapter(component));
   }

   /**
    * Finds first empty mandatory. Returns internal storage position if empty
    * mandatory is found. Otherwise returns negative number.
    */
   private int findFirstEmpty()
   {
      int pos = -1;
      int size = entries.size();

      // Go for next empty component

      for (int i = 0; i < size; i++)
      {
         String tempString = null;
         Object entry = entries.elementAt(i);

         // Do not check readonly or disabled fields

         if (entry instanceof Entry)
         {
            if (((Entry) entry).isReadonly())
            {
               continue;
            }
         }
         else
         {
            if (!((JComponent) entry).isEnabled())
            {
               continue;
            }
         }

         // Check field content, only components implementing Entry are checked

         if (entry instanceof Entry)
         {
            if (((Entry) entry).getObjectValue() == null)
            {
               pos = i;

               break;
            }
         }
      }

      return pos;
   }

   /**
    * Checks if all MandatoryFields are not empty. If so returns true;
    */
   public boolean isDone()
   {
      if (findFirstEmpty() < 0)
      {
         return true;
      }

      return false;
   }

   /**
    * Makes visible the next mandatory Entry that is to be filled <P>
    * returns true if there is an element
    */
   public boolean show()
   {
      int pos = findFirstEmpty();

      if (pos < 0)
      {
         return false;
      }

      perform(pos);
      return true;
   }

   /**
    * Makes visible the given mandatory entry that is to be filled <P>
    * returns true if component is found
    */
   public boolean show(JComponent component)
   {
      int i = entries.indexOf(component);

      if (i >= 0)
      {
         perform(i);
         return true;
      }

      return false;
   }

   /**
    * Makes visible the given mandatory Entry that is to be filled.
    */
   public boolean show(int i)
   {
      if (i < entries.size() && i >= 0)
      {
         perform(i);
         return true;
      }

      return false;
   }

   /**
    * Return all mandatory entries.
    */
   public Vector getAll()
   {
      return entries;
   }

   /**
    * Return all mandatory names.
    */
   public Vector getAllNames()
   {
      return names;
   }

   /**
    * Generic perform int corresponds to Vector.elementAt().
    */
   private void perform(int i)
   {
      JComponent wantedComponent = (JComponent) entries.elementAt(i);

      Assert.isNotNull(wantedComponent);

      if (!wantedComponent.isShowing())
      {
         // Make it visible again

         Vector containers = new Vector();

         containers.add(wantedComponent);
         Container container = wantedComponent.getParent();

         while (!wantedComponent.isShowing() && container != null)
         {
            if (container instanceof JTabbedPane)
            {
               JTabbedPane tab = (JTabbedPane) container;
               Component component = (Component) containers.elementAt((containers.size() - 1));
               tab.setSelectedIndex(tab.indexOfComponent(component));
            }

            containers.add(container);
            container = container.getParent();
         }
      }

      // Bring component to front

      wantedComponent.getTopLevelAncestor().requestFocus();
      wantedComponent.requestFocus();

      // Notify listeners

      doListeners(i);
   }

   /**
    * Call the listeners.
    */
   private void doListeners(int pos)
   {
      int max = listeners.size();

      for (int i = 0; i < max; i++)
      {
         ((MandatoryListener) listeners.elementAt(i)).processEvent(new MandatoryEvent((JComponent) entries.elementAt(pos),
               (String) names.elementAt(pos),
               nameType((JComponent) entries.elementAt(pos)),
               MandatoryEvent.DEFAULT));
      }
   }

   /**
    * Return a default MessageBoxListener.
    */
   public static MandatoryListener createMessageBoxAdapter()
   {
      return new MessageBoxAdapter();
   }

   /**
    * Return a default MessageBoxListener.
    */
   public static MandatoryListener createTextFieldAdapter(JTextComponent component)
   {
      return new TextFieldAdapter(component);
   }

   /** Map the component to a readable name */
   private String nameType(JComponent component)
   {
      String name = "das Feld";
      String typeName = component.getClass().toString();

      if (typeName.endsWith("Entry"))
      {
         if (typeName.endsWith(".MoneyEntry"))
         {
            name = "das Betragsfeld";
         }
         else if (typeName.endsWith("DateEntry"))
         {
            name = "das Datumsfeld";
         }
         else if (typeName.endsWith(".IntegerEntry"))
         {
            name = "das Ganzzahlenfeld";
         }
         else if (typeName.endsWith(".LongEntry"))
         {
            name = "das Ganzzahlenfeld";
         }
         else if (typeName.endsWith(".ShortEntry"))
         {
            name = "das Ganzzahlenfeld";
         }
         else if (typeName.endsWith(".TextEntry"))
         {
            name = "das Textfeld";
         }
         else if (typeName.endsWith(".StringBufferEntry"))
         {
            name = "das Textfeld";
         }
         else if (typeName.endsWith(".DoubleEntry"))
         {
            name = "das Zahlenfeld";
         }
         else if (typeName.endsWith(".FloatEntry"))
         {
            name = "das Zahlenfeld";
         }
         else if (typeName.endsWith(".PasswordEntry"))
         {
            name = "das Passwortfeld";
         }
      }
      else if (component instanceof JComboBox)
      {
         name = "die Auswahlbox";
      }
      else if (component instanceof JCheckBox)
      {
         name = "das Auswahlfeld";
      }
      else if (component instanceof JPasswordField)
      {
         name = "das Passwortfeld";
      }
      else if (component instanceof JTextField)
      {
         name = "das Textfeld";
      }

      return name;
   }
}

/**
 *
 */
class MessageBoxAdapter implements MandatoryListener
{
   /**
    *
    */
   public MessageBoxAdapter()
   {
   }

   /**
    *
    */
   public void processEvent(MandatoryEvent e)
   {
      String message = "Zur korrekten Datenübernahme ist noch\n";

      String text = e.getText();

      if (text == null || text.length() < 1)
      {
         message += e.getType() + " auszufüllen.";
      }
      else
      {
         message += e.getType() + " \"" + text + "\" auszufüllen.";
      }

      JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
      JDialog dialog = pane.createDialog(null, "Hinweis");

      dialog.setLocation(e.getComponent().getTopLevelAncestor().getLocationOnScreen().x + 50,
            e.getComponent().getTopLevelAncestor().getLocationOnScreen().y + 50);
      dialog.show();
   }
}

/**
 *
 */
class TextFieldAdapter implements MandatoryListener
{
   JTextComponent textComponent;

   /**
    * Constructor
    */
   public TextFieldAdapter(JTextComponent textComponent)
   {
      Assert.isNotNull(textComponent);

      this.textComponent = textComponent;
   }

   /**
    *
    */
   public void processEvent(MandatoryEvent e)
   {
      String text = e.getText();

      if (text == null || text.length() < 1)
      {
         textComponent.setText("Bitte noch das aktuelle Feld ausfüllen.");
      }
      else
      {
         textComponent.setText("Bitte noch " + e.getType()
               + " \"" + text + "\" ausfüllen.");
      }
   }
}





