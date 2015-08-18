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

import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * Provides a generic panel for any kind of object.
 * Note: This class is extensivley used by dialogs
 */
public class GenericObjectPanel extends JPanel
{
   private Class type;
   private Object object;
   private Method[] getMethods;
   private Method[] setMethods;
   private JComponent[] components;

   /**
    * Generic Panel for editing attributes of any object. Init for a class to
    * reuse it for several objects of same class
    * @param type The type of object to be used by this panel
    */
   public GenericObjectPanel(Class type)
   {
      Assert.isNotNull(type);

      this.type = type;

      createContent();
   }

   /**
    * Generic Panel for editing attributes of any object.
    * @param object The object to be used in this panel
    */
   public GenericObjectPanel(Object object)
   {
      super();

      this.object = object;
      type = object.getClass();

      createContent();
   }

   /**
    * Creates the visual content of the panel
    */
   public void createContent()
   {
      setLayout(new java.awt.BorderLayout());

      List setGetMethods = Reflect.collectGetSetMethods(type);

      setMethods = new Method[setGetMethods.size()];
      getMethods = new Method[setGetMethods.size()];
      components = new JComponent[setGetMethods.size()];

      LabeledComponentsPanel labeledComponents = new LabeledComponentsPanel();

      for (int n = 0; n < setGetMethods.size(); ++n)
      {
         getMethods[n] = ((Method[]) setGetMethods.get(n))[0];
         setMethods[n] = ((Method[]) setGetMethods.get(n))[1];
         components[n] = GuiMapper.getComponentForClass(getMethods[n].getReturnType());

         labeledComponents.add(components[n],
               getMethods[n].getName().substring(3, getMethods[n].getName().length()));
      }

      labeledComponents.pack();
      add("North", labeledComponents);

      if (object != null)
      {
         setObject(object);
      }
   }

   /**
    * Setter for assigning an object to the panel
    * @param object The object to be assigned to the panel
    */
   public void setObject(Object object)
   {
      for (int n = 0; n < getMethods.length; ++n)
      {
         Object value = null;

         try
         {
            value = getMethods[n].invoke(object);
         }
         catch (Exception x)
         {
            throw new InternalException("Cannot invoke method \"" + getMethods[n].getName() + "\"");
         }

         GuiMapper.setComponentValue(components[n], value);
      }
   }

   /**
    * Returns the object that has been assigned to the panel
    */
   public Object getObject()
   {
      return null;
   }
}
