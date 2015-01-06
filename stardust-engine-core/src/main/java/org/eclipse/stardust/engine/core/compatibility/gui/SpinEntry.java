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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;


/** SpinEntry for any Entry-Element that implements Entry <p>

 Note: Per default only IntegerEntry is supported.<br>
 If you want to use it for any Entry just override the methods
 set/getValue(). Also you are free to set your own
 Action-/MouseListener */
public class SpinEntry extends JPanel
{
   protected Entry entry;

   protected JButton upButton;
   protected JButton downButton;

   protected int max = Integer.MAX_VALUE;
   protected int min = Integer.MIN_VALUE;

   protected EventListener upListener;
   protected EventListener downListener;

   /** constuctor for IntegerEntry with default add */
   public SpinEntry(Entry entry)
   {
      this.entry = entry;

      initialize();
   }

   /** constuctor for IntegerEntry with default add */
   public SpinEntry(Entry entry, int min, int max)
   {
      this.entry = entry;

      // min<max (!)
      if (min < max)
      {
         this.min = min;
         this.max = max;
      }
      else
      {
         this.min = max;
         this.max = min;
      }

      initialize();
   }

   /** */
   public void initialize()
   {
      setLayout(new BorderLayout());

      // init the spin buttons
      upButton =
            new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.NORTH);
      downButton =
            new javax.swing.plaf.basic.BasicArrowButton(SwingConstants.SOUTH);

      // panel to put spin buttons in
      JPanel spinButtonPanel = new JPanel();
      spinButtonPanel.setLayout(new BoxLayout(spinButtonPanel,
            BoxLayout.Y_AXIS));
      spinButtonPanel.add(upButton);
      spinButtonPanel.add(downButton);
      // modify size
      Dimension spinPanelDim = upButton.getPreferredSize();
      spinPanelDim.height = ((JComponent) entry).getPreferredSize().height;
      spinButtonPanel.setPreferredSize(spinPanelDim);
      spinButtonPanel.setMaximumSize(spinPanelDim);

      // add them
      add((JComponent) entry);
      add("East", spinButtonPanel);

      // finally the listeners
      upListener = getIntegerListener();
      downListener = getIntegerListener();
      setListeners(upListener, downListener);
   }

   /** currently Mouse-&ActionListener is supported */
   public void setListeners(EventListener upListener,
         EventListener downListener)
   {
      // remove old listeners
      removeListener(upListener, upButton);
      removeListener(downListener, downButton);

      // set the new listeners
      addListener(upListener, upButton);
      addListener(downListener, downButton);
   }

   /** currently Mouse-&ActionListener is supported */
   protected void addListener(EventListener listener, JButton button)
   {
      if (listener instanceof ActionListener)
      {
         button.addActionListener((ActionListener) listener);
      }
      else if (listener instanceof MouseListener)
      {
         button.addMouseListener((MouseListener) listener);
      }
      else // unsupported listener
      {
         throw new InternalException("Listener not supported: " + listener);
      }
   }

   /** currently Mouse-&ActionListener is supported */
   protected void removeListener(EventListener listener, JButton button)
   {
      if (listener == null)
      {
         return;
      }
      else if (listener instanceof ActionListener)
      {
         button.removeActionListener((ActionListener) listener);
      }
      else if (listener instanceof MouseListener)
      {
         button.removeMouseListener((MouseListener) listener);
      }
   }

   /** get default listener for upButton */
   public EventListener getIntegerListener()
   {
      return new MouseListener()
      {
         public void mouseClicked(MouseEvent e)
         {
         };
         public void mouseReleased(MouseEvent e)
         {
         };
         public void mouseEntered(MouseEvent e)
         {
         };
         public void mouseExited(MouseEvent e)
         {
         };

         public void mousePressed(MouseEvent e)
         {
            // event source
            Object source = e.getSource();
            // current int
            int currentInt = getValue();
            //trace.debug( "val1: "+currentInt);

            // only allow min <= value <= max
            if (source == upButton)
            {
               if (currentInt < min)
               {
                  currentInt = min;
               }
               else if (currentInt < max)
               {
                  currentInt++;
               }
               else
               {
                  getToolkit().beep();
               }
            }
            else if (source == downButton)
            {
               if (currentInt > max)
               {
                  currentInt = max;
               }
               else if (currentInt > min)
               {
                  currentInt--;
               }
               else
               {
                  getToolkit().beep();
               }
            }
            else // nothing
            {
               return;
            }

            //trace.debug( "val2: "+currentInt);
            setValue(currentInt);
         }// mousePressed
      };
   }

   /** getMin */
   public int getMin()
   {
      return min;
   }

   /** setMin */
   public void setMin(int min)
   {
      this.min = min;

      if (getValue() < min)
      {
         setValue(min);
      }
   }

   /** getMax */
   public int getMax()
   {
      return max;
   }

   /** setMax */
   public void setMax(int max)
   {
      this.max = max;

      if (getValue() > max)
      {
         setValue(max);
      }
   }

   /** getValue */
   public int getValue()
   {
      int value = Unknown.INT;

      if (entry instanceof IntegerEntry)
      {
         value = ((IntegerEntry) entry).getValue();
      }

      return value;
   }

   /** setMax */
   public void setValue(int max)
   {
      if (entry instanceof IntegerEntry)
      {
         ((IntegerEntry) entry).setValue(max);
      }
   }

   /** Returns upper SpinButton <p>
    Note: this should only needed if you want to use a single listener for
    both spinbuttons.*/
   public JButton getUpButton()
   {
      return upButton;
   }

   /** Returns lower SpinButton <p>
    Note: this should only needed if you want to use a single listener for
    both spinbuttons.*/
   public JButton getDownButton()
   {
      return downButton;
   }

   /** called on object destruction */
   public void finalize()
   {
      // remove listeners
      removeListener(upListener, upButton);
      removeListener(downListener, downButton);

      // remove ref to entry
      entry = null;
   }

}// SpinEntry
