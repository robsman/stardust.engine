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
package org.eclipse.stardust.engine.core.pojo.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.ConstructorWrapper;
import org.eclipse.stardust.common.reflect.MethodWrapper;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.LabeledComponentsPanel;
import org.eclipse.stardust.engine.core.compatibility.gui.MandatoryWrapper;
import org.eclipse.stardust.engine.core.compatibility.gui.TextEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationPropertiesPanel;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.pojo.data.SelectClassDialog;


/**
 * The panel to set the properties for the plain java application type.
 * 
 * @author jmahmood
 * @version $Revision$
 */
public class PlainJavaApplicationPanel extends ApplicationPropertiesPanel
{
   public static final Logger trace = LogManager.getLogger(
         PlainJavaApplicationPanel.class);

   private static final String LABEL_CLASS_NAME = "ClassName: ";
   private static final String LABEL_CONSTRUCTOR = "Constructor:";
   private static final String LABEL_COMPLETION_METHOD = "Completion Method:";

   protected Component parent;

   private TextEntry classNameEntry;
   private JComboBox completeMethodList;
   private JComboBox constructorList;

   private MandatoryWrapper completeMethodBox;
   private MandatoryWrapper constructorBox;

   private JButton browseClassButton;

   private Class clazz;

   public PlainJavaApplicationPanel(Component parent)
   {
      this.parent = parent;

      initComponents();
   }

   public void setData(Map properties, Iterator accessPoints)
   {
      String className = (String) properties.get(PredefinedConstants.CLASS_NAME_ATT);

      String completeMethodName = (String) properties.get(
            PredefinedConstants.METHOD_NAME_ATT);

      String constructorName = (String) properties.get(
            PredefinedConstants.CONSTRUCTOR_NAME_ATT);

      classNameEntry.setText(className);
      updateChangeClassName();
      if (clazz != null)
      {
         if (!StringUtils.isEmpty(completeMethodName))
         {
            completeMethodList.setSelectedItem(
                  new MethodWrapper(Reflect.decodeMethod(clazz, completeMethodName)));
         }
         if (!StringUtils.isEmpty(constructorName))
         {
            constructorList.setSelectedItem(new ConstructorWrapper(
                  Reflect.decodeConstructor(clazz, constructorName)));
         }
      }
   }

   public Map getAttributes()
   {
      Map properties = new HashMap();
      properties.put(PredefinedConstants.CLASS_NAME_ATT,
            classNameEntry.getText());
      if (completeMethodList.getSelectedItem() != null)
      {
         properties.put(PredefinedConstants.METHOD_NAME_ATT,
               Reflect.encodeMethod(((MethodWrapper)
               completeMethodList.getSelectedItem()).getMethod()));
      }
      if (constructorList.getSelectedItem() != null)
      {
         properties.put(PredefinedConstants.CONSTRUCTOR_NAME_ATT,
               Reflect.encodeConstructor(((ConstructorWrapper)
               constructorList.getSelectedItem()).getConstructor()));
      }
      return properties;
   }

   private void checkMandatoryField(String value, String assertionText, boolean canClose)
         throws ValidationException
   {
      if (value == null || value.length() == 0)
      {
         throw new ValidationException(assertionText, canClose);
      }
   }

   public void validatePanel() throws ValidationException
   {
      checkMandatoryField(classNameEntry.getText(), "No class name specified.",
            true);

      if (completeMethodList.getSelectedItem() == null)
      {
         throw new ValidationException("No completion method specified", true);
      }
      if (constructorList.getSelectedItem() == null)
      {
         throw new ValidationException("No constructor specified", true);
      }
   }

   public void createAccessPoints(IApplication application)
   {
   }

   protected void initComponents()
   {
      setBorder(new CompoundBorder(new TitledBorder("Plain Java Application"),
            new EmptyBorder(10, 10, 10, 10)));

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      classNameEntry = new TextEntry(35);

      classNameEntry.getTextField().setInputVerifier(new InputVerifier()
      {
         public boolean verify(JComponent input)
         {
            updateChangeClassName();
            return true;
         }
      });

      browseClassButton = new JButton("Browse");
      browseClassButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (SelectClassDialog.showDialog(parent))
            {
               if (SelectClassDialog.getSelectedClass() != null)
               {
                  classNameEntry.setText(SelectClassDialog.getSelectedClass()
                        .getName());
               }
               updateChangeClassName();
            }
         }
      });

      completeMethodList = new JComboBox();
      completeMethodBox = new MandatoryWrapper(completeMethodList);

      completeMethodList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            MethodWrapper wrapper = (MethodWrapper) completeMethodList.getSelectedItem();
            completeMethodBox.setMandatory(wrapper == null);
         }
      });

      completeMethodList.setRenderer(new IdentifiableComboBoxRenderer());
      completeMethodList.setPreferredSize(new Dimension(400, 20));

      constructorList = new JComboBox();
      constructorBox = new MandatoryWrapper(constructorList);
      constructorList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            ConstructorWrapper wrapper = (ConstructorWrapper) constructorList.getSelectedItem();
            constructorBox.setMandatory(wrapper == null);
         }
      });

      constructorList.setRenderer(new IdentifiableComboBoxRenderer());
      constructorList.setPreferredSize(new Dimension(400, 20));

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      components.add(new JComponent[]{classNameEntry, browseClassButton},
            new String[]{LABEL_CLASS_NAME, ""}, new int[]{'r', 'a'});

      components.add(completeMethodBox, LABEL_COMPLETION_METHOD);

      components.add(constructorBox, LABEL_CONSTRUCTOR);

      components.pack();

      classNameEntry.setMandatory(true);

      completeMethodList.setEditable(false);
      completeMethodBox.setMandatory(true);

      constructorList.setEditable(false);
      constructorBox.setMandatory(true);

      add(components);
   }

   private void updateChangeClassName()
   {
      clazz = null;
      try
      {
         clazz = Reflect.getClassFromClassName(classNameEntry.getText());
      }
      catch (Throwable t)
      {
      }
      fetchMethodList(clazz, completeMethodList);
      fetchConstructorList(clazz, constructorList);
   }

   private void fetchMethodList(Class type, JComboBox methodBox)
   {
      Vector data = new Vector();
      if (type != null)
      {
         try
         {
            Method[] methods = type.getMethods();
            Arrays.sort(methods, new Comparator()
            {
               public int compare(Object lhs, Object rhs)
               {
                  return ((Method) lhs).getName().compareTo(((Method) rhs).getName());
               }
            });
            for (int i = 0; i < methods.length; ++i)
            {
               data.add(new MethodWrapper(methods[i]));
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
            data.removeAllElements();
            JOptionPane.showMessageDialog(parent, "The class '" + type.getName()
                  + "' cannot be loaded.");
         }
      }
      methodBox.setModel(new DefaultComboBoxModel(data));
      methodBox.setSelectedItem(null);
   }

   private void fetchConstructorList(Class type, JComboBox constructorBox)
   {
      Vector data = new Vector();
      if (type != null)
      {
         try
         {
            Constructor[] constructors = type.getConstructors();
            Arrays.sort(constructors, new Comparator()
            {
               public int compare(Object lhs, Object rhs)
               {
                  return ((Constructor) lhs).getName().compareTo(
                        ((Constructor) rhs).getName());
               }
            });
            for (int i = 0; i < constructors.length; ++i)
            {
               data.add(new ConstructorWrapper(constructors[i]));
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
            data.removeAllElements();
            JOptionPane.showMessageDialog(parent, "The class '" + type.getName()
                  + "' cannot be loaded.");
         }
      }
      constructorBox.setModel(new DefaultComboBoxModel(data));
      constructorBox.setSelectedItem(null);
   }
}
