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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.common.error.IErrorMessageProvider.Factory;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.Inconsistency;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ValidationExceptionDialog
{
   private ArrayList<Factory> translators;

   private ValidationExceptionDialog()
   {
   }

   public static boolean showDialog(Component owner, ValidationException e)
   {
      return showDialog(owner, e, true);
   }

   /**
    * @param owner owner window
    * @param e the exception to be displayed
    * @param showCloseMessage flag to suppress "close" message when displaying deployment warnings.
    * @return <code>true</code> if the user clicked Yes
    */
   public static boolean showDialog(Component owner, ValidationException e, boolean showCloseMessage)
   {
      int messageType;
      int optionType;
      Object[] options;
      List content = new ArrayList();
      if (e.canClose())
      {
         messageType = JOptionPane.WARNING_MESSAGE;
         optionType = JOptionPane.YES_NO_OPTION;
         options = new Object[]{"Yes", "No"};
      }
      else
      {
         messageType = JOptionPane.ERROR_MESSAGE;
         optionType = JOptionPane.OK_CANCEL_OPTION;
         options = new Object[]{"Ok"};
      }

      if (e.getMessages().size() == 0)
      {
         content.add(e.getMessage());
      }
      else
      {
         final JTable detailsTable = new JTable()
         {
            public String getToolTipText(MouseEvent event)
            {
               int hitRowIndex = rowAtPoint(event.getPoint());
               if (hitRowIndex >= 0)
               {
                  Object value = getValueAt(hitRowIndex, 0);
                  if (value != null)
                  {
                     return value.toString();
                  }
               }
               return getToolTipText();
            }
         };
         detailsTable.setShowGrid(true);
         Vector tableData = new Vector(e.getMessages().size());
         CollectionUtils.transform(tableData, e.getMessages(), new Functor()
         {
            public Object execute(Object source)
            {
               Vector result = new Vector(1);
               if (source instanceof Inconsistency)
               {
                  Inconsistency inc = (Inconsistency)source;
                  if (inc.getError() != null) {
                     result.add(getMessageFromErrorCase(inc.getError()));
                  } else {
                     result.add(((Inconsistency) source).getMessage());
                  }
               }
               else
               {
                  result.add(source);
               }
               return result;
            }
         });
         Vector columns = new Vector(1);
         columns.add("Validation Details:");
         detailsTable.setModel(new DefaultTableModel(tableData, columns)
         {
            public boolean isCellEditable(int row, int column)
            {
               return false;
            }
         });
         detailsTable.addMouseListener(new MouseAdapter()
         {
            public void mouseClicked(MouseEvent e)
            {
               if (e.getClickCount() == 2)
               {
                  int hitRowIndex = detailsTable.rowAtPoint(e.getPoint());
                  if (hitRowIndex >= 0)
                  {
                     Object value = detailsTable.getValueAt(hitRowIndex, 0);
                     if (value != null)
                     {
                        JOptionPane.showMessageDialog(detailsTable, value.toString());
                     }
                  }
               }
            }
         });

         JScrollPane detailsPane = new JScrollPane(detailsTable);
         detailsPane.setPreferredSize(new Dimension(400, 100));

         content.add(e.getMessage());
         content.add(detailsPane);
      }
      if (e.canClose() && showCloseMessage)
      {
         content.add("Do you want to close anyway?");
      }
      int result = JOptionPane.showOptionDialog(owner, content.toArray(), "Error",
            optionType, messageType, null, options, null);
      if (e.canClose())
      {
         return result == JOptionPane.YES_OPTION;
      }
      else
      {
         return false;
      }
   }

   public static String getMessageFromErrorCase(ErrorCase errorCase)
   {
      ArrayList<Factory> translators = new ArrayList<IErrorMessageProvider.Factory>(
            ExtensionProviderUtils.getExtensionProviders(IErrorMessageProvider.Factory.class));

      Locale locale = Locale.ENGLISH;
      String loc = System.getProperty("user.language");
      Iterator<IErrorMessageProvider.Factory> tIter = translators.iterator();
      while (tIter.hasNext())
      {
         IErrorMessageProvider.Factory msgFactory = (IErrorMessageProvider.Factory) tIter.next();
         IErrorMessageProvider msgProvider = msgFactory.getProvider(errorCase);
         if (msgProvider != null)
         {
            if (loc != null)
            {
               Locale[] s = Locale.getAvailableLocales();
               for (int i = 0; i < s.length; i++ )
               {
                  if (s[i].getLanguage().equalsIgnoreCase(loc))
                  {
                     locale = s[i];
                  }
               }
            }
            System.out.println("Choosen locale: " + locale.getDisplayName() + " " + locale.getLanguage());
            return msgProvider.getErrorMessage(errorCase, null, locale);
         }
      }

      return null;
   }

   public static void main(String[] args)
   {
      List c = new ArrayList();
      c.add("bla");
      c.add("blub");
      boolean result = showDialog(null, new ValidationException("Schwerer Fehler", c, false));
      System.out.println("result = " + result);
      result = showDialog(null, new ValidationException("Leichter Fehler", true));
      System.out.println("result = " + result);
   }
}
