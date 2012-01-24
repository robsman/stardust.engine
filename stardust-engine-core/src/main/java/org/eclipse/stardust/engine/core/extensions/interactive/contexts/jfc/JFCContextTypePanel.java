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
package org.eclipse.stardust.engine.core.extensions.interactive.contexts.jfc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.MethodWrapper;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.gui.LabeledComponentsPanel;
import org.eclipse.stardust.engine.core.compatibility.gui.MandatoryWrapper;
import org.eclipse.stardust.engine.core.compatibility.gui.TextEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationContextPropertiesPanel;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.pojo.data.SelectClassDialog;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JFCContextTypePanel extends ApplicationContextPropertiesPanel
{
   public static final Logger trace = LogManager.getLogger(JFCContextTypePanel.class);

   private static final String LABEL_CLASS = "Class:";
   private static final String LABEL_COMPLETION = "Completion Method:";

   private TextEntry classNameEntry;
   private Class clazz;
   private JButton selectClassButton;
   private MandatoryWrapper methodBox;
   private JComboBox methodList;
   private Method method;

   public JFCContextTypePanel()
   {
      initComponents();
   }

   private void initComponents()
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      setBorder(GUI.getTitledPanelBorder("Visual Java Bean"));

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      classNameEntry = new TextEntry(25);
      classNameEntry.addFocusListener(new FocusListener()
      {
         public void focusGained(FocusEvent e)
         {
         }

         public void focusLost(FocusEvent e)
         {
            try
            {
               clazz = Reflect.getClassFromClassName(classNameEntry.getText());
               populateMethods(clazz);
            }
            catch (Exception e1)
            {
               clazz = null;
               trace.warn("", e1);
            }
         }
      });
      components.add(
            new JComponent[]
            {
               classNameEntry,
               selectClassButton = new JButton("Browse")
            },
            new String[]{LABEL_CLASS, ""},
            new int[]{'c', 'b'});

      selectClassButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            SelectClassDialog.showDialog(JFCContextTypePanel.this);

            Class type = null;

            if ((type = SelectClassDialog.getSelectedClass()) != null)
            {
               classNameEntry.setText(type.getName());
               clazz = Reflect.getClassFromClassName(type.getName());
               populateMethods(clazz);
            }
            else
            {
               classNameEntry.setText("");
               clazz = null;
               populateMethods(null);
            }
         }
      }
      );

      methodBox = new MandatoryWrapper(methodList = new JComboBox());
      methodList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            MethodWrapper wrapper = (MethodWrapper) methodList.getSelectedItem();
            if (wrapper != null)
            {
               method = wrapper.getMethod();
            }
            methodBox.setMandatory(wrapper == null);
         }
      });

      components.add(methodBox, LABEL_COMPLETION);
      methodList.setRenderer(new IdentifiableComboBoxRenderer());
      components.packAllAlignMax();
      methodList.setEditable(false);
      methodBox.setMandatory(true);

      classNameEntry.setMandatory(true);

      add(components);
   }

   /**
    *
    */
   public void populateMethods(Class type)
   {
      if (type == null)
      {
         methodList.removeAllItems();
         methodList.setSelectedItem(null);
      }
      else
      {
         try
         {
            methodList.removeAllItems();
            methodList.setSelectedItem(null);

            Method[] methods = type.getMethods();

            Arrays.sort(methods, new Comparator()
            {
               public int compare(Object o1, Object o2)
               {
                  return ((Method) o1).getName().compareTo(((Method) o2).getName());
               }
            });
            for (int n = 0; n < methods.length; ++n)
            {
               methodList.addItem(new MethodWrapper(methods[n]));
            }
         }
         catch (Exception e)
         {
            methodList.removeAllItems();
            methodList.setSelectedItem(null);
            JOptionPane.showMessageDialog(
                  this, "The class '" + type.getName() + "' cannot be loaded.");
         }
      }
   }

   // todo: (france, fh) set data type
   public Collection getAccessPoints()
   {
      return null; // @todo (france, ub): broken JavaApplicationTypeHelper.calculateAccessPoints(clazz, method, true, true, null, owna);
   }

   public void setData(Map attributes, Iterator accessPoints)
   {
      String className = (String) attributes.get(
            PredefinedConstants.CLASS_NAME_ATT);
      String initMethodName = (String) attributes.get(
            PredefinedConstants.METHOD_NAME_ATT);
      classNameEntry.setText(className);

      if (!StringUtils.isEmpty(className))
      {
         try
         {
            clazz = Reflect.getClassFromClassName(className);
            populateMethods(clazz);

            methodList.setSelectedItem(
                  new MethodWrapper(Reflect.decodeMethod(clazz, initMethodName)));
         }
         catch (Exception e)
         {
            trace.warn("", e);
            clazz = null;
         }
      }
   }

   public Map getAttributes()
   {

      Map attributes = new HashMap();

      attributes.put(PredefinedConstants.CLASS_NAME_ATT, classNameEntry.getText());
      if (methodList.getSelectedItem() != null)
      {
         attributes.put(PredefinedConstants.METHOD_NAME_ATT,
               Reflect.encodeMethod(((MethodWrapper) methodList.getSelectedItem()).getMethod()));
      }
      return attributes;
   }

   public void reset()
   {
      classNameEntry.setText(null);
      methodList.setSelectedIndex(-1);
   }

   public void createAccessPoints(IApplicationContext context)
   {
   }

   public void validatePanel()
   {
      // @todo (france, ub):
   }
}
