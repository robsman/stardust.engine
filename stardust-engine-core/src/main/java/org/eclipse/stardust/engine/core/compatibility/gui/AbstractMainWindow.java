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
import java.awt.Dimension;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.*;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;


/**
 * Abstract class for standard main windows
 * Note: You must subclass AbstractMainWindow to use it's functionality
 */
public abstract class AbstractMainWindow extends JFrame
      implements ActionListener, MouseListener, Runnable, SimpleShutDownListener
{
   public static final String HELP_SET_FILE = "AbstractMainWindow.HelpSetFile";
   public static final String FILE_MENU_LABEL = "AbstractMainWindow.FileMenuLabel";
   public static final String FILE_OPEN_ITEM_LABEL = "AbstractMainWindow.FileOpenItemLabel";
   public static final String FILE_EXIT_ITEM_LABEL = "AbstractMainWindow.FileExitItemLabel";
   public static final String FILE_CLOSE_ITEM_LABEL = "AbstractMainWindow.FileCloseLabel";
   public static final String FILE_SAVE_ITEM_LABEL = "AbstractMainWindow.FileSaveItemLabel";
   public static final String SETTINGS_MENU_LABEL = "AbstractMainWindow.SettingsMenuLabel";
   public static final String SETTINGS_MENU_MNEMONIC = "AbstractMainWindow.SettingsMenuMnemonic";
   public static final String LOCALE_ITEM_LABEL = "AbstractMainWindow.LocaleItemLabel";
   public static final String LOCALE_ITEM_MNEMONIC = "AbstractMainWindow.LocaleItemMnemonic";
   public static final String HELP_MENU_LABEL = "AbstractMainWindow.HelpMenuLabel";
   public static final String HELP_INDEX_ITEM_LABEL = "AbstractMainWindow.HelpIndexItemLabel";
   public static final String HELP_ABOUT_ITEM_LABEL = "AbstractMainWindow.HelpAboutItemLabel";

   protected static final long DATE_REFRESH_INTERVALL_IN_MSEC = 5000;

   private JPanel contentPane;
   private JComponent centerContent;
   private JMenuItem saveItem;
   private JToolBar toolBar;
   private StatusEntry statusEntry;
   private StatusEntry timeEntry;
   private JMenuItem localeItem;
   private JMenu helpMenu;
   private JMenuItem indexItem;
   private Thread timeThread;

   protected ActionListener helpListener;
   protected DateFormat dateFormat;
   protected DateFormat timeFormat;

   /**
    * Protected constructor
    */
   protected AbstractMainWindow(String resourceBundleName)
   {
      // Initialize locale, resources,

      initialize();

      // Set look and feel

      // Adjsut UI settings

      patchUISettings();

      // Add listener for close events
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            exit();
         }
      });


      // Setup GUI's busy mode

      GUI.enableGlassPane(true);

      // Initialize title

      setTitle("CARNOT");

      // Initialize size

      setSize(new Dimension(850, 550));

      // Create and layout the content of the main window

      createContent();

      // Show the main frame

      show();
   }

   /** ActionListener protocol. */
   public void actionPerformed(ActionEvent event)
   {
      try
      {
         setWaiting(true);

         if (event.getSource() == indexItem)
         {
            helpListener.actionPerformed(event);
         }
         else if (event.getSource() == localeItem)
         {
            SelectLocaleDialog.showDialog(this);
         }
      }
      catch (Exception _ex)
      {
         throw new InternalException(_ex);
      }
      finally
      {
         setWaiting(false);
      }
   }

   /** Sets the content of the main window center.
    *  Note: Must be implemented by subclasses
    */
   public abstract JComponent createCenterContent();

   /** Creates or recreates the content of the window. */
   public void createContent()
   {
      // Specify content

      contentPane = (JPanel) getContentPane();

      contentPane.removeAll();
      contentPane.setLayout(new BorderLayout());

      // Create toolbars

      getRootPane().setJMenuBar(createMenuBar());

      // Create toolbars

      contentPane.add("North", createToolBar());

      // Create content

      contentPane.add("Center", centerContent = createCenterContent());

      // Create status bar

      contentPane.add("South", createStatusBar());

      validate();
      repaint();
   }

   /** Creates an exit menu item for exiting the application
    *  @return Returns the menu item with exit handler
    */
   public JMenuItem createExitMenuItem()
   {
      JMenuItem menuItem = new JMenuItem(Parameters.instance().getString(FILE_EXIT_ITEM_LABEL, "Exit"));

      menuItem.setMnemonic('e');
      menuItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            exit();
         }
      });
      return menuItem;
   }

   /** Creates a standard file menu
    *  @return Returns the menu
    */
   public JMenu createFileMenu()
   {
      JMenu menu;
      JMenuItem menuItem;

      menu = new JMenu(Parameters.instance().getString(FILE_MENU_LABEL, "File"));

      menu.setMnemonic('d');

      menuItem = menu.add(new JMenuItem(Parameters.instance().getString(FILE_OPEN_ITEM_LABEL, "Open") + " ..."));

      menuItem.setMnemonic('o');
      menuItem = menu.add(new JMenuItem(Parameters.instance().getString(FILE_CLOSE_ITEM_LABEL, "Close")));

      menuItem.setMnemonic('c');
      menu.addSeparator();

      saveItem = menu.add(new JMenuItem(Parameters.instance().getString(FILE_SAVE_ITEM_LABEL, "Save")));

      saveItem.addActionListener(this);
      saveItem.setMnemonic('s');

      menu.addSeparator();

      menu.add(createExitMenuItem());

      return menu;
   }

   /** Creates the standard help menu
    *  @return Returns the help menu
    */
   public JMenu createHelpMenu()
   {
      JMenu menu;
      JMenuItem menuItem;
      menu = new JMenu(Parameters.instance().getString(HELP_MENU_LABEL, "Help"));

      menu.setMnemonic('h');

      indexItem = menu.add(new JMenuItem(Parameters.instance().getString(HELP_INDEX_ITEM_LABEL, "Index")));

      indexItem.addActionListener(this);

      menuItem = menu.add(new JMenuItem(Parameters.instance().getString(HELP_ABOUT_ITEM_LABEL, "About") + " ..."));

      return menu;
   }

   /** Creates the main menubar conatining a standard file menu, settings and help
    *  @return Returns the menubar
    */
   public JMenuBar createMenuBar()
   {
      JMenuBar menuBar = new JMenuBar();

      menuBar.add(createFileMenu());
      menuBar.add(createSettingsMenu());
      menuBar.add(createHelpMenu());

      return menuBar;
   }

   /** Creates a settings menu for changing the locale
    *  @return Returns the menu
    */
   public JMenu createSettingsMenu()
   {
      JMenu menu;
      JMenuItem menuItem;

      menu = new JMenu(Parameters.instance().getString(SETTINGS_MENU_LABEL, "Settings"));

      menu.setMnemonic(Parameters.instance().getString(SETTINGS_MENU_MNEMONIC, "s").charAt(0));

      menu.add(localeItem = new JMenuItem(Parameters.instance().getString(LOCALE_ITEM_LABEL, "Locale") + " ..."));
      localeItem.addActionListener(this);
      localeItem.setMnemonic(Parameters.instance().getString(LOCALE_ITEM_LABEL, "l").charAt(0));

      return menu;
   }

   /**
    * Creates the status bar.
    */
   public JComponent createStatusBar()
   {
      JPanel statusBar = new JPanel();

      statusBar.add(Box.createHorizontalGlue());
      statusBar.add(statusEntry = new StatusEntry(80, true));
      statusBar.add(timeEntry = new StatusEntry(15));

      return statusBar;
   }

   /** Creates a standard toolbar with no buttons
    *  @return Returns the toolbar object
    */
   public JToolBar createToolBar()
   {
      toolBar = new JToolBar();

      toolBar.setFloatable(false);
      //		toolBar.setBorderPainted(false);

      toolBar.add(Box.createHorizontalGlue(), 0);

      return toolBar;
   }

   /** Initializes all of the functional "background" for the main window, such
    as locale, help. */
   public void initialize()
   {
      //      initializeHelp();
   }

   /** Uses the default locale.
    Other settings must be overloaded by subclasses.*/
   public void initializeLocale()
   {
   }

   /** Adapter for mouse events */
   public void mouseClicked(MouseEvent e)
   {
   }

   /** Adapter for mouse events */
   public void mouseEntered(MouseEvent e)
   {
   }

   /** Adapter for mouse events */
   public void mouseExited(MouseEvent e)
   {
   }

   /** Adapter for mouse events */
   public void mousePressed(MouseEvent e)
   {
   }

   /** Adapter for mouse events */
   public void mouseReleased(MouseEvent e)
   {
   }

   /** Patch a couple of UI settings; mainly fonts. */
   protected void patchUISettings()
   {
      // change foreground colors of disabled (Text)Entries

      UIManager.put("TextField.inactiveForeground", java.awt.Color.black);
      UIManager.put("ComboBox.disabledForeground", java.awt.Color.black);

      UIDefaults defaults = UIManager.getDefaults();

      javax.swing.plaf.FontUIResource font;
      Object object;
      Object key;

      // Patch a couple of default settings, mainly font size

      for (java.util.Enumeration keys = defaults.keys(); keys.hasMoreElements();)
      {
         key = keys.nextElement();
         object = defaults.get(key);

         if (javax.swing.plaf.FontUIResource.class.isInstance(object))
         {
            font = (javax.swing.plaf.FontUIResource) object;

            defaults.put(key, new javax.swing.plaf.FontUIResource(font.getName(), font.getStyle(), 11));
         }
      }
   }

   /** Resets the content of the status entry in the status bar.
    */
   public void resetStatus()
   {
      statusEntry.setText("");
   }

   /** Starts the thread and updates the clock in the statusbar
    */
   public void run()
   {
      do
      {
         try
         {
            Thread.sleep(DATE_REFRESH_INTERVALL_IN_MSEC);
         }
         catch (InterruptedException ex)
         {
         }

         if (dateFormat == null)
         {
            dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
         }
         if (timeFormat == null)
         {
            timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault());
         }

         Date currentTime = TimestampProviderUtils.getTimeStamp();

         String dateString = dateFormat.format(currentTime) + "   " + timeFormat.format(currentTime);

         timeEntry.setText(dateString);
      }
      while (true);
   }

   /**
    * Implements the shutdown sequence. Default implementation is empty
    * A Subclass should overwrite this method if it wants release some
    * resources.
    *
    * To activate this method you can use the registerForShutdown()
    * method in class SimpleShutDownTread()
    *
    * Attention: Dont call dispose() or exit() in this method!!
    */
   public void shutDown()
   {
   }

   /** Sets the center panel and performs an automatic repaint */
   public void setCenterContent(JComponent content)
   {
      contentPane.remove(centerContent);
      contentPane.add(content);

      validate();
      repaint();
   }

   /** (Re)sets the locale of the main window and recreates the whole content
    *  to reflect the locale change. Changes to the component hierarchy being
    *  not reflected by the create protocol (createMenuBar(), createToolBar(),
    *  createCenterPanel()) will be lost.
    *  @param language The language (e.g. de, en)
    *  @param country The country of the locale (e.g. DE, US)
    */
   public void setLocale(String language, String country)
   {
      setLocale(new Locale(language, country));
   }

   /** (Re)sets the locale of the main window and recreates the whole content
    *  to reflect the locale change. Changes to the component hierarchy being
    *  not reflected by the create protocol (createMenuBar(), createToolBar(),
    *  createCenterPanel()) will be lost.
    *  @param locale The locale object
    */
   public void setLocale(Locale locale)
   {
      // Set the local for the entire JVM

      Locale.setDefault(locale);
   }

   /** Sets the content of the status entry in the status bar.
    *  @param status The text of the status message
    */
   public void setStatus(String status)
   {
      statusEntry.setText(status);
   }

   /** Sets the window waiting/not waiting cursor. During waiting state, the wait
    *  cursor is displayed and all input is blocked for the main window.
    *  @param busy Indicates wheter to show the wait cursor or not
    */
   public void setWaiting(boolean busy)
   {
      GUI.setBusy(this, busy);
   }

   /** Shows the window
    */
   public void show()
   {
      start();
      super.show();
   }

   /** Used to start the timer thread
    */
   public void start()
   {
      timeThread = new Thread(this);

      timeThread.start();
   }

   /**
    * close method for the frame
    * The default implementation close the frame after a confirmation dialog.
    */
   public void exit()
   {
      if (JOptionPane.showConfirmDialog(this, "Close the main window?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
      {
         dispose();
         System.exit(0);
      }
   }
}
