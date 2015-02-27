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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * Entry for displaying and modifying Money values.
 */
public class MoneyEntry extends JPanel
      implements Entry, ActionListener, DocumentListener
{
   /**
    * Vector holding all money entries to alter them all.
    */
   private static Vector allMoneyEntries = new Vector();
   private static ClipboardOwner defaultClipboardOwner = new GUI.DefaultClipboardObserver();
   private static ActionListener currencyMenuListener;
   private JLabel flagLabel;
   private LimitedNumberField moneyField;
   private JLabel currencyLabel;
   private boolean isCurrencySelectable = true;
   private Document textDocument;
   private LimitedMoneyDocument moneyDocument;
   private PopupAdapter moneyPopup;
   private JDialog calculatorDialog;
   private Calculator calculator;
   private JMenu europeanCurrenciesMenu;
   private JMenu otherCurrenciesMenu;
   private JMenuItem copyItem;
   private JMenuItem pasteItem;
   private JMenuItem calculatorItem;
   private JMenuItem lastCurrencyItem;
   private JMenuItem defaultMenuItems[];
   private boolean mandatory;
   private boolean isReadonly = false;
   /** boolean that indicates status for overloading of setOpaque&setBorder */
   private boolean guiInitialized = false;
   // @todo weg damit!
   private String defaultCurrencies[] = {"EUR", "DM"};

   /*
    *
    */
   public MoneyEntry()
   {
      this(-1, false);
   }
   /*
    *
    */
   public MoneyEntry(int digits)
   {
      this(digits, false);
   }
   /*
    *
    */
   public MoneyEntry(int size, boolean mandatory)
   {
      this.mandatory = mandatory;
      allMoneyEntries.add(this);

      initialize(size);
   }

   /**
    * Initialization called by constructors.
    */
   protected void initialize(int size)
   {
      // Check desired size

      if (size <= 0)
      {
         size = 7;
      }

      // Construct fields

      moneyField = new LimitedNumberField(size);
      moneyField.setHorizontalAlignment(SwingConstants.RIGHT);

      currencyLabel = new JLabel();

      // Popup menu

      JPopupMenu popupMenu = new JPopupMenu();

      copyItem = new JMenuItem("Copy");
      copyItem.addActionListener(this);
      popupMenu.add(copyItem);

      pasteItem = new JMenuItem("Paste");
      pasteItem.addActionListener(this);
      popupMenu.add(pasteItem);

      popupMenu.add(new JPopupMenu.Separator());

      // Default currencies

      JMenuItem menuItem;
      defaultMenuItems = new JMenuItem[defaultCurrencies.length];

      for (int i = 0; i < defaultCurrencies.length; i++)
      {
         menuItem = new CurrencyMenuItem(defaultCurrencies[i]);
         menuItem.addActionListener(this);
         popupMenu.add(menuItem);

         defaultMenuItems[i] = menuItem;
      }

      popupMenu.add(new JPopupMenu.Separator());

      // european currencies
      String europeanCurrencies[] = Money.getEuropeanCurrencyList();
      europeanCurrenciesMenu = new JMenu("Europe");

      for (int i = 0; i < europeanCurrencies.length; i++)
      {
         menuItem = new CurrencyMenuItem(europeanCurrencies[i]);
         menuItem.addActionListener(this);
         europeanCurrenciesMenu.add(menuItem);

         if (i == 0)
         {
            europeanCurrenciesMenu.add(new JPopupMenu.Separator());
         }
      }

      popupMenu.add(europeanCurrenciesMenu);

      // Calculator

      popupMenu.add(new JPopupMenu.Separator());

      calculatorItem = new JMenuItem("Calculator");
      calculatorItem.addActionListener(this);
      popupMenu.add(calculatorItem);


      // Add the popup

      moneyPopup = PopupAdapter.create(this.moneyField, popupMenu);

      KeyStroke SHIFT_F10 = KeyStroke.getKeyStroke(KeyEvent.VK_F10,
            InputEvent.SHIFT_MASK);
      moneyField.registerKeyboardAction(this, "popup", SHIFT_F10, JComponent.WHEN_FOCUSED);

      // Now init documents

      textDocument = new JTextField().getDocument();
      moneyDocument = (LimitedMoneyDocument) moneyField.getDocument();

      moneyDocument.addDocumentListener(this);

      // Layout elements

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      flagLabel = new JLabel(GUI.getMandatoryIcon());

      add("West", flagLabel);
      add("Center", Box.createHorizontalStrut(3));
      add(moneyField);
      add(Box.createHorizontalStrut(GUI.HorizontalLabelDistance));
      add(currencyLabel);


      // Check mandatory and readonly state

      performFlags();

      // Set cursor

      setCursor(GUI.ENTRY_CURSOR);

      // Finally set the initial value

      Money money = new Money();
      moneyField.setValue(money, false);
      currencyLabel.setText(Money.getCurrencyFor(money.getCurrency()));

      guiInitialized = true;
   }

   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent()
   {
      return this;
   }

   /**
    *	Add FocusListener.
    */
   public void addFocusListener(FocusListener listener)
   {
      if (moneyField != null)
      {
         moneyField.addFocusListener(listener);
      }

      super.addFocusListener(listener);
   }

   /**
    * Remove FocusListener.
    */
   public void removeFocusListener(FocusListener listener)
   {
      if (moneyField != null)
      {
         moneyField.removeFocusListener(listener);
      }

      super.removeFocusListener(listener);
   }

   /**
    *	Overload hasFocus.
    */
   public boolean hasFocus()
   {
      if (moneyField.hasFocus()
            || (calculator != null && calculator.hasFocus()))
      {
         return true;
      }

      return false;
   }

   /**
    * Marks, wether this date entry is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
      if (isCell)
      {
         removeAll();
         setBackground(moneyField.getBackground());
         moneyField.setBorder(null);
         add("East", moneyField);
         add(Box.createHorizontalStrut(GUI.HorizontalLabelDistance));
         add(currencyLabel);
      }
   }

   /**
    *	@return Money value
    */
   public Money getValue()
   {
      if (!isInitialized())
      {
         return null;
      }
      else
      {
         return moneyField.getValue();
      }
   }

   /**
    * Sets the Money value.
    */
   public void setValue(Money money)
   {
      if (money == null)
      {
         setValue(new Money());
         clearMoney();

         performFlags();

         return;
      }

      // Check displayed currency

      int newCurrency = money.getCurrency();

      if (newCurrency != moneyField.getValue().getCurrency())
      {
         currencyLabel.setText(Money.getCurrencyFor(newCurrency));
      }

      // Set value

      moneyField.setValue(money);
      performFlags();
   }

   /**
    *	Generic setter method.
    */
   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null || value instanceof Money)
      {
         setValue((Money) value);
      }
      else
      {
         throw new IllegalArgumentException("Object is not compatible to Money.");
      }
   }

   /**
    *	Generic getter method.
    */
   public Object getObjectValue()
   {
      return getValue();
   }

   /**
    *	Check mandatory and isEditable to set color settings.
    */
   protected void performFlags()
   {
      Assert.isNotNull(moneyField);
      Assert.isNotNull(flagLabel);

      if (isReadonly())
      {
         moneyField.setBackground(SystemColor.control);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else
      {
         // @todo find appropriate system color
         moneyField.setBackground(Color.white);

         if (mandatory && getObjectValue() == null)
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
      }
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return (getValue() == null);
   }

   /**
    *
    */
   public boolean isInitialized()
   {
      return (!"".equals(moneyField.getText()));
   }

   /**
    *	Save clear of moneyEntry.
    */
   public void clearMoney()
   {
      if (moneyField.getDocument() == moneyDocument)
      {
         moneyDocument.clear();
      }
      else
      {
         moneyField.setText("");
      }
   }

   /**
    *	En/disable mandatory input.
    */
   public void setMandatory(boolean mandatory)
   {
      this.mandatory = mandatory;
      performFlags();
   }

   /**
    *	Get mandatory status.
    */
   public boolean isMandatory()
   {
      return mandatory;
   }

   /**
    *
    */
   public boolean isReadonly()
   {
      return isReadonly;
   }
   /*
    *
    */
   public void setReadonly(boolean isReadonly)
   {
      this.isReadonly = isReadonly;
      moneyField.setEditable(!isReadonly);

      // En/disable currency popup also

      if (moneyPopup != null)
      {
         moneyPopup.setShowing(!isReadonly);
      }

      performFlags();
   }

   /** Sets BackgroundColor of this entry */
   public void setBackground(Color color)
   {
      if (moneyField != null)
      {
         moneyField.setBackground(color);
         currencyLabel.setBackground(color);
      }
   }

   /** Gets BackgroundColor of this entry */
   public Color getBackground(Color color)
   {
      return moneyField.getBackground();
   }

   /**
    *
    */
   public void setEnabled(boolean isEnabled)
   {
      setReadonly(!isEnabled);
      moneyField.setEditable(isEnabled);

      if (!isEnabled)
      {
         moneyField.setDocument(textDocument);
         moneyField.setText("");
      }
      else
      {
         moneyField.setDocument(moneyDocument);
      }
   }

   /**
    *
    */
   public boolean isEnabled()
   {
      return moneyField.isEditable() && !isReadonly();
   }

   /**
    * Change current currency.
    */
   void changeCurrency(String newCurrency, boolean isChangingAllMoneyFields)
   {
      if (isChangingAllMoneyFields)
      {
         changeAllMoneyFields(newCurrency);
      }
      else
      {

         moneyField.setValue(moneyField.getValue().getConverted(newCurrency),
               isInitialized());
         currencyLabel.setText(newCurrency);
      }
   }

   /**
    * Change currencies of all known money entries.
    */
   public static void changeAllMoneyFields(String newCurrency)
   {
      int max = allMoneyEntries.size();

      for (int i = 0; i < max; i++)
      {
         MoneyEntry entry = (MoneyEntry) allMoneyEntries.elementAt(i);

         entry.moneyField.setValue(
               entry.moneyField.getValue().getConverted(newCurrency),
               entry.isInitialized());
         entry.currencyLabel.setText(newCurrency);
      }
   }

   /**
    *	En/disable currency change for newly created objects <p>
    *	Note: this is a static, non dynamic method
    * e.g. the value that is set here is only checked in the constructor
    */
   public void setCurrencySelectable(boolean isCurrencySelectable)
   {
      if (this.isCurrencySelectable == isCurrencySelectable)
      {
         return;
      }

      this.isCurrencySelectable = isCurrencySelectable;

      // Dis/enable popup

      europeanCurrenciesMenu.setEnabled(isCurrencySelectable);
      otherCurrenciesMenu.setEnabled(isCurrencySelectable);
      lastCurrencyItem.setEnabled(isCurrencySelectable);

      for (int i = 0; i < defaultMenuItems.length; i++)
      {
         defaultMenuItems[i].setEnabled(isCurrencySelectable);
      }
   }

   /**
    *	Gets currency status.
    */
   public boolean isCurrencySelectable()
   {
      return isCurrencySelectable;
   }

   /**
    *	Border delegation.
    */
   public void setBorder(Border border)
   {
      if (guiInitialized)
      {
         moneyField.setBorder(border);
      }
   }

   /**
    *	Sets opaqueness of this entry.
    */
   public void setOpaque(boolean opaque)
   {
      if (guiInitialized)
      {
         moneyField.setOpaque(opaque);
      }

      super.setOpaque(opaque);
   }

   /**
    *	Gets the opaqueness of this entry.
    */
   public boolean isOpaque()
   {
      return moneyField.isOpaque();
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (calculator != null && source == calculator.getOKButton())
      {
         calculatorDialog.setVisible(false);
         setValue(new Money(calculator.convertOutput(), moneyField.getValue().getCurrency()));
      }
      else if (source == copyItem)
      {
         Clipboard clipboard = getToolkit().getSystemClipboard();
         Money value = getValue();

         if (value == null)
         {
            getToolkit().beep();
            return;
         }

         StringSelection contents = new StringSelection(value.toString());
         clipboard.setContents(contents, defaultClipboardOwner);
      }
      else if (source == pasteItem)
      {
         Clipboard clipboard = getToolkit().getSystemClipboard();
         Transferable content = clipboard.getContents(this);

         if (content == null)
         {
            setValue(null);
            getToolkit().beep();
            return;
         }

         try
         {
            String moneyData = (String) (content.getTransferData(DataFlavor.stringFlavor));
            int index = moneyData.indexOf(' ');

            if (index <= 0)
            {
               getToolkit().beep();
               return;
            }

            String value = moneyData.substring(0, index);
            String currencyAbbreviation = moneyData.substring(index + 1);

            value = value.replace(',', '.');
            Money money = new Money(value,
                  Money.getCurrencyValueFor(currencyAbbreviation));
            setValue(money);
         }
         catch (Exception ex)
         {
            getToolkit().beep();
         }
      }
      else if (source == calculatorItem)
      {
         Dimension size = this.moneyField.getSize();

         runCalculatorDialog(new MouseEvent(this,
               0, // id
               TimestampProviderUtils.getTimeStampValue(), // when
               0, // modifiers
               size.width, // x
               size.height, // y
               1, // clickCount
               true //popuptrigger
         ));
      }
      else if (source instanceof JMenuItem)
      {
         JMenuItem menuItem = (JMenuItem) source;

         // Perform 'changed currency' - action

         String actionText = menuItem.getActionCommand();

         // Cut off additional information from actionCommand

         int currencyDelimPos = actionText.indexOf(" ");

         if (currencyDelimPos > 0)
         {
            actionText = actionText.substring(0, currencyDelimPos);
         }

         // lastCurrencyItem will show any currency except the default ones

         boolean isNotDefault = true;

         for (int i = 0; i < defaultCurrencies.length; i++)
         {
            if (defaultCurrencies[i].equals(actionText))
            {
               isNotDefault = false;
               break;
            }
         }

         // @todo ... check if lastCurrencyItem is needed or not [aj] seems to be old stuff
         if (isNotDefault && lastCurrencyItem != null)
         {
            lastCurrencyItem.setText(actionText);
         }

         // Change the currency

         changeCurrency(actionText, false);
      }
      else if ("popup".equals(e.getActionCommand())) // key: SHIFT+F10 <=> popup
      {
         Point point = getLocation();
         moneyPopup.doPopup(0, getHeight());
      }
   }

   /**
    *
    */
   public void runCalculatorDialog(MouseEvent e)
   {
      if (calculatorDialog == null)
      {
         JFrame frame = null;
         Container container = getTopLevelAncestor();

         if (container instanceof JFrame)
         {
            frame = (JFrame) container;
         }

         calculatorDialog = new JDialog(frame, "Rechner", true);
         calculator = new Calculator();
         calculator.getOKButton().addActionListener(this);
         calculatorDialog.getContentPane().add(calculator);
         calculatorDialog.pack();
      }

      // Set calculator's init value

      Money money = getValue();

      if (money != null)
      {
         calculator.setValue(money.getValue());
      }
      else
      {
         calculator.setValue(0.0);
      }

      // calculate POP (point of presentation)

      Point point = ((JComponent) e.getSource()).getLocationOnScreen();
      int x = point.x + e.getX();
      int y = point.y + e.getY();

      // ensure that it does not exceed the screen limits

      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dim = calculatorDialog.getPreferredSize();
      if (x + dim.width >= screenDim.width)
      {
         x = screenDim.width - dim.width - 1;
      }
      if (y + dim.height >= screenDim.height)
      {
         y = screenDim.height - dim.height - 1;
      }

      // make it visible at wanted location

      calculatorDialog.setLocation(x, y);
      calculatorDialog.show();
   }

   /**
    * Gives notification that an attribute or set of attributes changed.
    */
   public void changedUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    * Gives notification that there was an insert into the document.
    */
   public void insertUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    * Gives notification that a portion of the document has been removed.
    */
   public void removeUpdate(DocumentEvent e)
   {
      performFlags();
   }

   /**
    * Finally clean up.
    */
   protected void finalize() throws Throwable
   {
      allMoneyEntries.remove(this);

      KeyStroke SHIFT_F10 = KeyStroke.getKeyStroke(KeyEvent.VK_F10,
            InputEvent.SHIFT_MASK);
      moneyField.unregisterKeyboardAction(SHIFT_F10);

      super.finalize();
   }

   /** return a menu for DesktopWindow */
   public static JMenu getCurrencyMenu()
   {
      if (currencyMenuListener == null)
      {
         currencyMenuListener = new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               JMenuItem menuItem = (JMenuItem) e.getSource();

               // perform 'changed currency' - action

               String actionText = menuItem.getActionCommand();

               // cut off additional information from actionCommand

               int currencyDelimPos = actionText.indexOf(" ");

               if (currencyDelimPos > 0)
               {
                  actionText = actionText.substring(0, currencyDelimPos);
               }

               // change the currency

               changeAllMoneyFields(actionText);
               Money.setDefaultCurrency(Money.getCurrencyValueFor(actionText));
            }
         };
      }

      return getCurrencyMenu(currencyMenuListener);
   }

   /**
    * Return a menu for DesktopWindow and add a special listener.
    */
   public static JMenu getCurrencyMenu(ActionListener listener)
   {
      JRadioButtonMenuItem menuItem;
      String defaultCurrency = Money.getCurrencyFor(Money.getDefaultCurrency());

      JMenu menu = new JMenu("Währung");
      ButtonGroup group = new ButtonGroup();

      JMenu europeanMenu = new JMenu("Europa");
      String europeanCurrencies[] = Money.getEuropeanCurrencyList();

      for (int i = 0; i < europeanCurrencies.length; i++)
      {
         menuItem = new RadioCurrencyMenuItem(europeanCurrencies[i]);
         menuItem.addActionListener(listener);
         europeanMenu.add(menuItem);

         group.add(menuItem);

         if (europeanCurrencies[i].startsWith(defaultCurrency))
         {
            menuItem.setSelected(true);
         }

         if (i == 0)
         {
            europeanMenu.addSeparator();
         }
      }

      menu.add(europeanMenu);

      return menu;
   }
}

/**
 * Allows only limited number for money.
 */
class LimitedMoneyDocument extends PlainDocument
{
   private static NumberFormat myFormat;

   public /*static*/ char intDelim = '.';
   public /*static*/ char realDelim = ',';

   private int maxCount;
   private boolean realDelimFound;
   private LimitedNumberField textField;

   /**
    * Set max number of input chars at start
    */
   public LimitedMoneyDocument(LimitedNumberField textField, int maxCount)
   {
      this.maxCount = maxCount;

      String formatPattern = "###,##0.00";
      myFormat = new DecimalFormat(formatPattern);

      this.textField = textField;
   }

   /**
    * Remove delimiters
    */
   public String getRealString(String oldString)
   {
      char addChar;
      String newString = "";
      realDelimFound = false;

      int length = oldString.length();
      for (int i = 0; i < length; i++)
      {
         addChar = oldString.charAt(i);
         if (Character.isDigit(addChar))
         {
            newString += addChar;
         }
         else if (addChar == realDelim)
         {
            if (realDelimFound == true)
            {
               break;
            }

            realDelimFound = true;
            newString += ".";
         }
      }

      return newString;
   }

   /**
    * Handle string insertion.
    */
   public void insertString(int offs, String str, AttributeSet a)
         throws BadLocationException
   {
      // Store source data

      int sourceLength = getLength();
      String sourceText = getText(0, sourceLength);
      StringBuffer strBuffer = new StringBuffer(sourceText);

      // Check if old value is zero

      if (offs == 0 && sourceLength > 0 && str.length() > 0)
      {
         long oldValue;

         try
         {
            oldValue = myFormat.parse(strBuffer.toString()).longValue();

            if (oldValue == 0)
            {
               strBuffer.deleteCharAt(0);
            }
         }
         catch (Exception e)
         {
         }
      }

      // Now add new string

      strBuffer.insert(offs, str);

      BigDecimal value;

      try
      {
         value = new BigDecimal(myFormat.parse(
               strBuffer.toString()).doubleValue());
      }
      catch (Exception e)
      {
         if (sourceLength > 0)
         {
            if (sourceText.startsWith(","))
            {
               sourceText = "0" + sourceText;
            }

            value = new BigDecimal(getRealString(sourceText));
         }
         else
         {
            value = new BigDecimal(0.0);
         }
      }

      // Set the new value

      if (textField == null)
      {
         return;
      }

      textField.setValue(
            new Money(value, textField.getValue().getCurrency()), false);

      super.remove(0, sourceLength);
      super.insertString(0, myFormat.format(value.doubleValue()), a);

      // Set caret to correct caret position

      if (!"".equals(sourceText)) // <=> initilized
      {
         int lengthDiff = getLength() - sourceLength;
         int caretPos = lengthDiff + offs;
         int caretDiff = sourceLength - offs;

         // Adjust for columns after centSperator (currently Diff < 3)

         if ((caretDiff > 0 && caretDiff < 3)
               || (value.abs().longValue() < 10 && caretPos == 0))
         {
            caretPos += 1;
         }

         if (caretPos < 0)
         {
            caretPos = 0;
         }

         textField.setCaretPosition(caretPos);
      }
      else
      {
         textField.setCaretPosition(1);
      }
   }

   /**
    * Handle remove.
    */
   public void remove(int offs, int length)
         throws BadLocationException
   {
      int sourceLength = getLength();

      // Allow user to restore uninitialized state again by removing all

      if (offs == 0 && sourceLength == length)
      {
         super.remove(0, sourceLength);
         return;
      }

      // Do custom remove

      String sourceText = getText(0, sourceLength);
      StringBuffer strBuffer = new StringBuffer(sourceText.substring(0, offs));
      int counter;

      for (counter = offs; counter < offs + length; counter++)
      {
         // Only remove digits and intDelims

         char currChar = sourceText.charAt(counter);

         if (Character.isDigit(currChar)
               || currChar == intDelim)
         {
            continue;
         }

         strBuffer.append(currChar);
      }

      // Append last part of sourceText

      if (counter < sourceLength)
      {
         strBuffer.append(sourceText.substring(counter));
      }

      // Set text in field

      super.remove(0, sourceLength);
      insertString(0, strBuffer.toString(), (AttributeSet) getDefaultRootElement());

      // Set caret pos

      int newDiff = sourceLength - getLength() - 1;
      if (newDiff < 0)
      {
         newDiff = 0;
      }
      if (offs - newDiff < 0)
      {
         newDiff = 0;
      }
      textField.setCaretPosition(offs - newDiff);
   }

   /**
    * Allow total clear.
    */
   public void clear()
   {
      try
      {
         super.remove(0, getLength());
      }
      catch (BadLocationException e)
      {
      }
   }
}

/**
 * Specialized input for <code>MoneyEntry</code>.
 */
class LimitedNumberField extends JTextField
      implements KeyListener, FocusListener
{
   Money value;
   public static NumberFormat myFormat = NumberFormat.getInstance();

   /**
    * Constructor.
    */
   public LimitedNumberField(int columns)
   {
      super(columns);

      addKeyListener(this);
      setMargin(new Insets(0, 2, 0, 2));

      addFocusListener(this);
   }

   /**
    * Create a default model.
    */
   protected Document createDefaultModel()
   {
      return new LimitedMoneyDocument(this, getColumns());
   }

   /**
    * Invoked when a component gains the keyboard focus.
    */
   public void focusGained(FocusEvent e)
   {
      if (isEnabled() && isEditable())
      {
         selectAll();
      }
   }

   /**
    * Invoked when a component loses the keyboard focus.
    */
   public void focusLost(FocusEvent e)
   {
   }

   /**
    * @return Money value
    */
   public Money getValue()
   {
      return value;
   }

   /**
    * Sets money value
    */
   public void setValue(Money value)
   {
      setValue(value, true);
   }

   /**
    * Sets money value.
    */
   public void setValue(Money value, boolean updateGui)
   {
      this.value = value;

      if (updateGui)
      {
         setText(myFormat.format(value.doubleValue()));
      }
   }

   /**
    * Used to catch delete and backspace chars (currently not used).
    */
   public void keyPressed(KeyEvent e)
   {
   }

   /**
    * Invoked when a key has been released.
    */
   public void keyReleased(KeyEvent e)
   {
      switch (e.getKeyCode())
      {
         case KeyEvent.VK_COMMA:
         case KeyEvent.VK_PERIOD:
         case KeyEvent.VK_DECIMAL:
            setCaretPosition(getText().length() - 2);
            break;
      }
   }

   /**
    * Invoked when a key has been typed.
    */
   public void keyTyped(KeyEvent e)
   {
   }
}


