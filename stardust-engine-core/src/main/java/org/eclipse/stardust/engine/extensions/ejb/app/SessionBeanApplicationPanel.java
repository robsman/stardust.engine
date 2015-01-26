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
package org.eclipse.stardust.engine.extensions.ejb.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
 * The panel to set the properties for the session bean application type.
 * 
 * @author jmahmood, ubirkemeyer
 * @version $Revision: 52518 $
 */
public class SessionBeanApplicationPanel extends ApplicationPropertiesPanel
{
   public static final Logger trace = LogManager.getLogger(SessionBeanApplicationPanel.class);

   private static final String LABEL_REMOTE_INTERFACE_NAME = "Remote Interface ClassName: ";
   private static final String LABEL_HOME_INTERFACE_CLASS = "Home Interface ClassName: ";
   private static final String LABEL_HOME_JNDIPATH = "Home Interface JNDI Path: ";
   private static final String LABAEL_LOCAL_BINDING = "Local Binding";
   private static final String LABEL_CREATION_METHOD = "Creation Method:";
   private static final String LABEL_COMPLETION_METHOD = "Completion Method:";

   private static final String CREATION_METHOD_PREFIX = "create";

   protected Component parent;

   private TextEntry remoteInterfaceEntry;
   private TextEntry homeInterfaceEntry;
   private TextEntry jndiPathEntry;
   private JCheckBox localBindingCheckBox;
   private JComboBox completeMethodList;
   private JComboBox createMethodList;

   private MandatoryWrapper completeMethodBox;
   private MandatoryWrapper createMethodBox;

   private JButton browseRemoteInterfaceButton;
   private JButton browseHomeInterfaceButton;

   private Class remoteInterfaceClass;
   private Class homeInterfaceClass;

   public SessionBeanApplicationPanel(Component parent)
   {
      this.parent = parent;

      initComponents();
   }

   public void setData(Map properties, Iterator accessPoints)
   {
      String remoteInterface = (String) properties.get(PredefinedConstants.REMOTE_INTERFACE_ATT);
      String homeInterface = (String) properties.get(PredefinedConstants.HOME_INTERFACE_ATT);
      homeInterface = StringUtils.isEmpty(homeInterface)
            ? remoteInterface + "Home" : homeInterface;

      String completeMethodName = (String) properties.get(PredefinedConstants.METHOD_NAME_ATT);
      String createMethodName = (String) properties.get(PredefinedConstants.CREATE_METHOD_NAME_ATT);

      Object isLocalProperty = properties.get(PredefinedConstants.IS_LOCAL_ATT);

      homeInterfaceEntry.setText(homeInterface);
      try
      {
         updateChangeHomeInterface();
         if (homeInterfaceClass != null)
         {
            createMethodList.setSelectedItem(new MethodWrapper(Reflect.decodeMethod(homeInterfaceClass, createMethodName)));
         }
      }
      catch (InternalException e)
      {
         trace.warn("", e);
         homeInterfaceClass = null;
      }

      remoteInterfaceEntry.setText(remoteInterface);
      try
      {
         updateChangeRemoteInterface();
         if (remoteInterfaceClass != null)
         {
            completeMethodList.setSelectedItem(new MethodWrapper(Reflect.decodeMethod(remoteInterfaceClass, completeMethodName)));
         }
      }
      catch (Exception e)
      {
         trace.warn("", e);
         remoteInterfaceClass = null;
      }

      jndiPathEntry.setText((String) properties.get(PredefinedConstants.JNDI_PATH_ATT));

      boolean isLocal = (isLocalProperty != null)
            ? ((Boolean) isLocalProperty).booleanValue() : false;
      localBindingCheckBox.setSelected(isLocal);
   }

   public Map getAttributes()
   {
      Map properties = new HashMap();
      properties.put(PredefinedConstants.HOME_INTERFACE_ATT,
            homeInterfaceEntry.getText());
      properties.put(PredefinedConstants.REMOTE_INTERFACE_ATT,
            remoteInterfaceEntry.getText());
      properties.put(PredefinedConstants.JNDI_PATH_ATT,
            jndiPathEntry.getText());
      properties.put(PredefinedConstants.IS_LOCAL_ATT,
            new Boolean(localBindingCheckBox.isSelected()));
      if (completeMethodList.getSelectedItem() != null)
      {
         properties.put(PredefinedConstants.METHOD_NAME_ATT,
               Reflect.encodeMethod(((MethodWrapper)
               completeMethodList.getSelectedItem()).getMethod()));
      }
      if (createMethodList.getSelectedItem() != null)
      {
         properties.put(PredefinedConstants.CREATE_METHOD_NAME_ATT,
               Reflect.encodeMethod(((MethodWrapper)
               createMethodList.getSelectedItem()).getMethod()));
      }
      return properties;
   }

   public String getName()
   {
      return "Session Bean";
   }

   private void checkMandatoryField(String value, String assertionText, boolean canClose) throws ValidationException
   {
      if (value == null || value.length() == 0)
      {
         throw new ValidationException(assertionText, canClose);
      }
   }

   public void validatePanel() throws ValidationException
   {
      checkMandatoryField(remoteInterfaceEntry.getText(), "No remote interface specified.", true);
      checkMandatoryField(homeInterfaceEntry.getText(), "No home interface specified.", true);
      checkMandatoryField(jndiPathEntry.getText(), "No JNDI path specified.", true);

      if (completeMethodList.getSelectedItem() == null)
      {
         throw new ValidationException("No completion method specified", true);
      }
      if (createMethodList.getSelectedItem() == null)
      {
         throw new ValidationException("No creation method specified", true);
      }
   }

   public void createAccessPoints(IApplication application)
   {
   }

   protected void initComponents()
   {
      setBorder(new CompoundBorder(new TitledBorder("Session Bean Application"),
            new EmptyBorder(10, 10, 10, 10)));

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      remoteInterfaceEntry = new TextEntry(35);

      remoteInterfaceEntry.addFocusListener(new FocusListener()
      {
         public void focusGained(FocusEvent e)
         {
         }

         public void focusLost(FocusEvent e)
         {
            try
            {
               updateChangeRemoteInterface();
            }
            catch (Exception e1)
            {
               trace.warn("", e1);
               fetchMethodList(null, completeMethodList, null);
            }
         }
      });

      browseRemoteInterfaceButton = new JButton("Browse");
      browseRemoteInterfaceButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (SelectClassDialog.showDialog(parent))
            {
               if (SelectClassDialog.getSelectedClass() != null)
               {
                  //@todo add a method for it
                  remoteInterfaceEntry.setText(SelectClassDialog.getSelectedClass()
                        .getName());

                  updateChangeRemoteInterface();
               }
               else
               {
                  remoteInterfaceEntry.setText(null);
                  homeInterfaceEntry.setText(null);

                  updateChangeRemoteInterface();
               }
            }
         }
      });

      homeInterfaceEntry = new TextEntry(35);

      homeInterfaceEntry.addFocusListener(new FocusListener()
      {
         public void focusGained(FocusEvent e)
         {
         }

         public void focusLost(FocusEvent e)
         {
            try
            {
               updateChangeHomeInterface();
            }
            catch (Exception e1)
            {
               trace.warn("", e1);
               fetchMethodList(null, createMethodList, null);
            }
         }
      });

      browseHomeInterfaceButton = new JButton("Browse");
      browseHomeInterfaceButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (SelectClassDialog.showDialog(parent))
            {
               if (SelectClassDialog.getSelectedClass() != null)
               {
                  homeInterfaceEntry.setText(SelectClassDialog.getSelectedClass()
                        .getName());

                  updateChangeHomeInterface();
               }
               else
               {
                  homeInterfaceEntry.setText(null);
                  fetchMethodList(null, createMethodList, null);
               }
            }
         }
      });

      jndiPathEntry = new TextEntry(35);

      localBindingCheckBox = new JCheckBox(LABAEL_LOCAL_BINDING);

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

      createMethodList = new JComboBox();
      createMethodBox = new MandatoryWrapper(createMethodList);
      createMethodList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            MethodWrapper wrapper = (MethodWrapper) createMethodList.getSelectedItem();
            createMethodBox.setMandatory(wrapper == null);
         }
      });

      createMethodList.setRenderer(new IdentifiableComboBoxRenderer());
      createMethodList.setPreferredSize(new Dimension(400, 20));

      LabeledComponentsPanel components = new LabeledComponentsPanel();

      components.add(new JComponent[]{remoteInterfaceEntry, browseRemoteInterfaceButton},
            new String[]{LABEL_REMOTE_INTERFACE_NAME, ""}, new int[]{'r', 'a'});

      components.add(new JComponent[]{homeInterfaceEntry, browseHomeInterfaceButton},
            new String[]{LABEL_HOME_INTERFACE_CLASS, ""}, new int[]{'h', 'b'});

      components.add(jndiPathEntry, LABEL_HOME_JNDIPATH, 'j');

      components.add(localBindingCheckBox, "");

      components.add(completeMethodBox, LABEL_COMPLETION_METHOD);

      components.add(createMethodBox, LABEL_CREATION_METHOD);

      components.pack();

      remoteInterfaceEntry.setMandatory(true);
      homeInterfaceEntry.setMandatory(true);
      jndiPathEntry.setMandatory(true);

      completeMethodList.setEditable(false);
      completeMethodBox.setMandatory(true);

      createMethodList.setEditable(false);
      createMethodBox.setMandatory(true);

      add(components);
   }

   private void updateChangeRemoteInterface()
   {
      try
      {
         remoteInterfaceClass = Reflect.getClassFromClassName(remoteInterfaceEntry
               .getText());
         remoteInterfaceEntry.setText(remoteInterfaceClass.getName());

         if (StringUtils.isEmpty(homeInterfaceEntry.getText()))
         {
            homeInterfaceEntry.setText(remoteInterfaceClass.getName() + "Home");

            updateChangeHomeInterface();
         }

         if (StringUtils.isEmpty(jndiPathEntry.getText()))
         {
            jndiPathEntry.setText(remoteInterfaceClass.getName());
         }

         fetchMethodList(remoteInterfaceClass, completeMethodList, null);
      }
      catch (Exception e)
      {
         trace.warn("", e);
      }
   }

   private void updateChangeHomeInterface()
   {
      try
      {
         homeInterfaceClass = Reflect.getClassFromClassName(homeInterfaceEntry.getText());
         fetchMethodList(homeInterfaceClass, createMethodList, CREATION_METHOD_PREFIX);
      }
      catch (Exception e)
      {
         trace.warn("", e);
      }
   }

   private void fetchMethodList(Class type, JComboBox methodBox, String prefix)
   {
      Vector data = new Vector();
      if (type != null)
      {
         try
         {
            Method[] methods = type.getMethods();
            TreeMap map = new TreeMap();
            for (int i = 0; i < methods.length; i++)
            {
               if (prefix == null || methods[i].getName().startsWith(prefix))
               {
                  MethodWrapper wrapper = new MethodWrapper(methods[i]);
                  map.put(wrapper.getName(), wrapper);
               }
            }
            data.addAll(map.values());
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
}
