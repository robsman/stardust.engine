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
package org.eclipse.stardust.engine.extensions.xml.data;

import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDialog;
import org.eclipse.stardust.engine.core.compatibility.gui.ErrorDialog;


/**
 * @author fherinean
 * @version $Revision$
 */
public class TypeSelectorDialog extends AbstractDialog
{
   private static TypeSelectorDialog instance;
   private JList list;
   
   public static List getDeclaredTypes(String type, String url)
   {
      throw new UnsupportedOperationException("Swing definition desktop is no longer supported.");
   }

   protected JComponent createContent()
   {
      list = new JList();
      JScrollPane scroller = new JScrollPane(list);
      scroller.setPreferredSize(new Dimension(400, 300));
      return scroller;
   }

   public void validateSettings() throws ValidationException
   {
      if (list.getSelectedValue() == null)
      {
         throw new ValidationException("No element was selected.", true);
      }
   }

   private void setData(String type, String url, String typeId)
   {
      QName name = StringUtils.isEmpty(typeId) ? null : QName.valueOf(typeId);
      try
      {
         list.setListData(getDeclaredTypes(type, url).toArray());
      }
      catch (Exception e)
      {
         list.setListData(Collections.EMPTY_LIST.toArray());
         ErrorDialog.showDialog(this, "Cannot retrieve schema elements.", e);
      }
      list.setSelectedValue(name, true);
   }

   public static boolean showDialog(JComponent parent, String type, String url, String typeId)
   {
      if (instance == null)
      {
         instance = new TypeSelectorDialog();
      }
      instance.setData(type, url, typeId);
      return showDialog("Select " + type.toUpperCase() + " Element", instance, (JDialog)
            SwingUtilities.getAncestorOfClass(JDialog.class, parent));
   }

   public static QName getSelectedName()
   {
      return instance == null ? null : (QName) instance.list.getSelectedValue();
   }
}
