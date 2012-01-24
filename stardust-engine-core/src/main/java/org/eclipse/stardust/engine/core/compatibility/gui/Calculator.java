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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * Very simple calculator for floating point arithmetics.
 * Used in numeric fields via the popup menu
 */
public class Calculator extends JPanel implements ActionListener
{
   public static final int OP_DIV = 0;
   public static final int OP_MUL = 1;
   public static final int OP_PLUS = 2;
   public static final int OP_MINUS = 3;
   public static final int OP_RESULT = 4;
   public static final String PLUS_MINUS = "\u00B1";
   public static final String DELETE_LAST = "DEL";
   /**
    * Output
    */
   private JLabel output;
   /**
    * Value stored in memory
    */
   private JLabel memDisplay;
   /**
    * Show operand
    */
   private TextField opDisplay;
   /**
    * OK Button to take the calculator's result
    */
   private JButton OK;
   /**
    * Number of typed digits
    */
   private int digitsTyped = 0;
   /**
    * Internal var for storing temporary result in
    */
   private double numMemory;
   /**
    * internal var to store kind of operation in
    */
   private int operatorMemory = -1;
   /**
    * Output format
    */
   DecimalFormat format;
   /**
    * Indicate if decimal separator is already used
    */
   private boolean decimalOn = false;
   /**
    * Boolean to incate if cancel has been pressed
    */
   public boolean useValue = true;

   /**
    * Output in desired format.
    */
   private String format(String string)
   {
      return (string);
   }

   /**
    * Default Constructor
    */
   public Calculator()
   {
      setLayout(new BorderLayout());

      output = new JLabel(("0"));
      output.setHorizontalAlignment(SwingConstants.RIGHT);

      JPanel outputPanel = new JPanel();

      outputPanel.setLayout(new BorderLayout());

      JLabel distLabel = new JLabel(".");

      distLabel.setForeground(Color.white);
      outputPanel.add("West", distLabel);
      outputPanel.add("East", output);
      outputPanel.setBackground(Color.white);
      outputPanel.setBorder(new JTextField().getBorder());

      NumArrayListener numListener = new NumArrayListener();
      OpArrayListener opListener = new OpArrayListener();

      JPanel panel1 = new JPanel();

      panel1.setLayout(new GridLayout(4, 3, 4, 4));

      IndexedButton indexButton;

      for (int y = 0; y < 3; y++)
      {
         for (int x = 0; x < 3; x++)
         {
            int value = (x + 1) + 3 * (2 - y);
            indexButton = new IndexedButton(Integer.toString(value), value, Color.blue);
            indexButton.addActionListener(numListener);
            panel1.add(indexButton);
         }
      }

      indexButton = new IndexedButton("0", 0, Color.blue);
      indexButton.addActionListener(numListener);
      panel1.add(indexButton);

      // Comma separator

      JButton tempButton = new JButton(",");
      tempButton.setForeground(Color.blue);
      tempButton.addActionListener(this);
      panel1.add(tempButton);

      // +/-

      tempButton = new JButton(PLUS_MINUS);
      tempButton.setForeground(Color.blue);
      tempButton.addActionListener(this);
      panel1.add(tempButton);

      JPanel panel2 = new JPanel();
      panel2.setLayout(new GridLayout(4, 2, 4, 4));

      // Multiply

      indexButton = new IndexedButton("*", OP_MUL, Color.red);
      indexButton.addActionListener(opListener);
      panel2.add(indexButton);

      // Divide

      indexButton = new IndexedButton("/", OP_DIV, Color.red);
      indexButton.addActionListener(opListener);
      panel2.add(indexButton);

      // Plus

      indexButton = new IndexedButton("+", OP_PLUS, Color.red);
      indexButton.addActionListener(opListener);
      panel2.add(indexButton);

      // Minus

      indexButton = new IndexedButton("-", OP_MINUS, Color.red);
      indexButton.addActionListener(opListener);
      panel2.add(indexButton);

      // Not used yet - just for reserving space

      panel2.add(new JLabel(" "));
      panel2.add(new JLabel(" "));

      indexButton = new IndexedButton("=", OP_RESULT, Color.red);
      indexButton.addActionListener(this);
      panel2.add(indexButton);

      // Clear, Cancel, OK

      JPanel panel3 = new JPanel(new BorderLayout());
      JPanel boxPanel = new JPanel(new GridLayout(1, 3));

      tempButton = new JButton("Clear");
      tempButton.addActionListener(this);
      boxPanel.add(tempButton);

      tempButton = new JButton("Cancel");
      tempButton.addActionListener(this);
      boxPanel.add(tempButton);

      OK = new JButton("OK");
      boxPanel.add(OK);

      panel3.add(Box.createVerticalStrut(GUI.VerticalWidgetDistance),
            BorderLayout.NORTH);
      panel3.add(boxPanel, BorderLayout.CENTER);

      JPanel panel4 = new JPanel();
      panel4.setLayout(new BorderLayout());
      memDisplay = new JLabel(" ");
      opDisplay = new TextField(1);
      opDisplay.setEditable(false);
      opDisplay.setEnabled(false);

      panel4.add(outputPanel, BorderLayout.NORTH);
      panel4.add("South",
            Box.createVerticalStrut(GUI.VerticalWidgetDistance / 2));

      // Compose all

      add(panel4, BorderLayout.NORTH);
      add(panel1, BorderLayout.WEST);
      add(panel2, BorderLayout.EAST);
      add(panel3, BorderLayout.SOUTH);

      // Spacing

      add(Box.createHorizontalStrut(GUI.HorizontalWidgetDistance),
            BorderLayout.CENTER);
      setBorder(new EtchedBorder());

      // Register digit

      for (int i = 0; i <= 9; i++)
      {
         int val = i + KeyEvent.VK_0;
         String command = Integer.toString(val);

         registerKeyboardAction(numListener, command,
               KeyStroke.getKeyStroke(val, 0, false),
               JComponent.WHEN_IN_FOCUSED_WINDOW);
         registerKeyboardAction(numListener, command,
               KeyStroke.getKeyStroke(i + KeyEvent.VK_NUMPAD0, 0, false),
               JComponent.WHEN_IN_FOCUSED_WINDOW);
      }

      // Register ops

      registerKeyboardAction(opListener, Integer.toString(OP_DIV),
            KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(opListener, Integer.toString(OP_MUL),
            KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(opListener, Integer.toString(OP_MINUS),
            KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(opListener, Integer.toString(OP_PLUS),
            KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

      // Register enter and decimal (also comma and period)

      registerKeyboardAction(this, "=",
            KeyStroke.getKeyStroke(KeyEvent./*VK_ENTER*/VK_PAGE_DOWN, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, ".",
            KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, ",",
            KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, ",",
            KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, PLUS_MINUS,
            KeyStroke.getKeyStroke(KeyEvent./*VK_NUMBER_SIGN*/VK_PAGE_UP, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, DELETE_LAST,
            KeyStroke.getKeyStroke(KeyEvent./*VK_NUMBER_SIGN*/VK_DELETE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
      registerKeyboardAction(this, DELETE_LAST,
            KeyStroke.getKeyStroke(KeyEvent./*VK_NUMBER_SIGN*/VK_BACK_SPACE, 0, false),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

   /**
    * Set the value using number objects
    * @param initValue The Number representation of the value
    */
   public void setValue(Number initValue)
   {
      if (initValue == null)
      {
         output.setText("0");
      }
      else
      {
         output.setText(initValue.toString());
      }
   }

   /**
    * Set vthe alue using a double
    * @param initValue The double value
    */
   public void setValue(double initValue)
   {
      clear();

      if (initValue == 0.0)
      {
         output.setText("0");
      }
      else
      {
         output.setText("" + initValue);
         decimalOn = true;
      }
   }

   /**
    * Set value using BigDecimal
    * @param initValue A BigDecimal for setting the value
    */
   public void setValue(BigDecimal initValue)
   {
      clear();
      BigDecimal intVal = new BigDecimal(initValue.toBigInteger());
      if (initValue.subtract(intVal).doubleValue() == 0.0)
      {
         output.setText("" + intVal);
      }
      else
      {
         output.setText("" + initValue.doubleValue());
         decimalOn = true;
      }
   }

   /**
    * Handles the ActionEvents for this field
    */
   public void actionPerformed(ActionEvent e)
   {
      String command = e.getActionCommand();

      if ((!decimalOn) && (command.equals(",") || command.equals(".")))
      {
         decimalOn = true;
         output.setText(output.getText() + ".");
      }
      else if (command.equals(PLUS_MINUS))
      {
         if (!decimalOn)
         {
            output.setText(
                  Integer.toString((new Double(convertOutput() * (-1))).intValue()));
         }
         else
         {
            output.setText(Double.toString(convertOutput() * (-1.0)));
         }
      }
      else if (command.equals("Clear"))
      {
         output.setText("0");
         clear();
      }
      else if (command.equals(DELETE_LAST))
      {
         String text = output.getText();
         int newLength = text.length() - 1;

         if (newLength >= 0)
         {
            output.setText(text.substring(0, newLength));

            char delChar = text.charAt(newLength);
            if (delChar == '.' || delChar == ',')
            {
               decimalOn = false;
            }
         }
      }
      else if (command.equals("Cancel"))
      {
         useValue = false;
         getTopLevelAncestor().setVisible(false);
      }
      else if (command.equals("="))
      {
         double temp = convertOutput();
         switch (operatorMemory)
         {
            case OP_DIV:
               output.setText(Double.toString(numMemory / temp));
               clear();
               break;
            case OP_MUL:
               output.setText(Double.toString(temp * numMemory));
               clear();
               break;
            case OP_MINUS:
               output.setText(Double.toString(numMemory - temp));
               clear();
               break;
            case OP_PLUS:
               output.setText(Double.toString(temp + numMemory));
               clear();
               break;
         }

         if (output.getText().indexOf('.') != -1)
         {
            decimalOn = true;
         }
      }
   }

   /**
    * Clear the calculator including the display
    */
   public void clear()
   {
      decimalOn = false;
      opDisplay.setText(" ");
      memDisplay.setText("Ready.");
      numMemory = 0;
      operatorMemory = -1;
      digitsTyped = 0;
   }

   /**
    * Convert text to double or Integer (set by decimalOn)
    */
   public double convertOutput()
   {
      String data = output.getText();
      if (data == null)
      {
         return 0.0;
      }

      // Do the conversion

      double num;
      try
      {
         if (!(decimalOn))
         {
            num = (Integer.parseInt(output.getText()));
         }
         else
         {
            num = (Double.valueOf(output.getText())).doubleValue();
         }
      }
      catch (Exception e)
      {
         return numMemory;
      }

      return num;
   }

   /**
    * return the OK-button
    * @return The OK button
    */
   public JButton getOKButton()
   {
      return OK;
   }

   /** Listener for a numeric array that belongs to number buttons */
   class NumArrayListener implements ActionListener
   {
      /** Default constructor */
      public NumArrayListener()
      {
      }

      /** implements ActionListener: actionPerformed */
      public void actionPerformed(ActionEvent e)
      {
         int value = Integer.parseInt(e.getActionCommand());

         if (value >= KeyEvent.VK_0 && value <= KeyEvent.VK_9)
         {
            performString(Integer.toString(value - KeyEvent.VK_0));
         }
         else if (value >= 0 && value <= 9)
         {
            performString(Integer.toString(((IndexedButton) e.getSource()).index));
         }
      }

      /**
       *
       */
      public void performString(String string)
      {
         if (((!(decimalOn)) && (digitsTyped < 9)) || ((decimalOn) && (digitsTyped < 15)))
         {
            digitsTyped++;

            if (output.getText().equals("0"))
            {
               output.setText(string);
            }
            else
            {
               output.setText((output.getText() + string));
            }

         }
      }
   }
   /**
    * Listener for an array of elements that call operations.
    */
   class OpArrayListener implements ActionListener
   {
      /**
       * Default Constructor
       */
      public OpArrayListener()
      {
      }

      /**
       * Perform the actions
       */
      public void actionPerformed(ActionEvent e)
      {
         Object source = e.getSource();

         if (source instanceof IndexedButton)
         {
            operatorMemory = ((IndexedButton) source).index;
            opDisplay.setText(((IndexedButton) source).getText());
         }
         else
         {
            operatorMemory = Integer.parseInt(e.getActionCommand());

            switch (operatorMemory)
            {
               case OP_DIV:
                  opDisplay.setText("/");
                  break;
               case OP_MUL:
                  opDisplay.setText("*");
                  break;
               case OP_PLUS:
                  opDisplay.setText("+");
                  break;
               case OP_MINUS:
                  opDisplay.setText("-");
                  break;
               default:
                  return;
            }
         }

         memDisplay.setText((output.getText()));

         numMemory = convertOutput();

         output.setText("");

         decimalOn = false;
         digitsTyped = 0;
      }
   }
   /**
    * A button that receives an index number.
    */
   class IndexedButton extends JButton
   {
      public int index;

      /**
       * Construct an indexed button.
       * @param txt The label of the button
       * @param index The index
       */
      public IndexedButton(String txt, int index)
      {
         this(txt, index, null);
      }

      /**
       * Construct an indexed button and set text color.
       */
      public IndexedButton(String txt, int index, Color color)
      {
         super(txt);
         this.index = index;

         // Set color

         if (color != null)
         {
            setForeground(color);
         }
      }
   }
}






