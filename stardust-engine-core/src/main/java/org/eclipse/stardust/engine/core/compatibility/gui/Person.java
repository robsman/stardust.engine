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

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.stardust.common.Money;


/**
 * Test class yielding extended Bean pattern. <p>
 * To produce a certain count of Person instances use constructor
 * <code>Person(int)</code>.<p>
 * <b>Caution:</b> Each Person instance you create is memorized in a
 * static variable. <code>getAllPersons()</code> returns them all.
 */
public class Person
{
   private static Vector persons = new Vector();
   private static Person first = new Person("Trump", "Zappo");

   private Vector children;
   private String firstName;
   private String name;
   private Calendar dateOfBirth;
   private TitleKey title;
   private Address privateAddress;
   private Address businessAddress;
   private Money salary;
   private boolean married;

   /**
    *
    */
   private void createChildren()
   {
      if (children != null)
      {
         return;
      }

      children = new Vector();

      children.add(new Person("Zadeh", "Aziza Mustafa"));
      children.add(new Person("Parker", "Charlie"));
   }

   /**
    * Create person with first name and name. Other props are fix.
    */
   public Person(String firstName, String name)
   {
      this.name = name;
      this.firstName = firstName;
      dateOfBirth = Calendar.getInstance();
      title = new TitleKey(TitleKey.DR);
      salary = new Money("15031.00");

      persons.add(this);
   }

   /**
    * Creates Person with specific number of children.
    */
   public Person(int count)
   {
      children = new Vector();
      for (int i = 0; i < count; i++)
      {
         children.add(new Person(i + "First" + i, i + "Last" + i));
      }
   }

   /**
    * Create person with first name and name with specific number of children.
    */
   public Person(String firstName, String name, int count)
   {
      children = new Vector();
      this.name = name;
      this.firstName = firstName;
      children.add(new Person("Sara", "Klein"));
      children.add(new Person("Laura", "Miela"));
      for (int i = 2; i < count; i++)
      {
         children.add(new Person(i + "first" + i, i + "Last" + i));
         persons.add(this);
      }
   }

   /**
    *
    */
   public static java.util.Enumeration getAllPersons()
   {
      return persons.elements();
   }

   /**
    * Converts rows from GenericTableStyle to JTable style
    */
   public static Object[] convertToObjects(Person pers)
   {
      Object[] res = new Object[5];
      res[0] = pers.getFirstName();
      res[1] = pers.getName();
      res[2] = pers.getDateOfBirth();
      res[3] = pers.getSalary();
      res[4] = pers.getTitle();
      return res;
   }

   /**
    * Converts table from GenericTableStyle to JTable style
    */
   public static Object[][] convertToObjects(Person pers[])
   {
      Object[][] res = new Object[pers.length][5];
      for (int i = 0; i < pers.length; i++)
      {
         res[i][0] = pers[i].getFirstName();
         res[i][1] = pers[i].getName();
         res[i][2] = pers[i].getDateOfBirth();
         res[i][3] = pers[i].getSalary();
         res[i][4] = pers[i].getTitle();
      }
      return res;
   }

   /**
    * Returns Person data in a form that can be handled by JTable.
    */
   public static Object[][] getAllPersonsAsArray()
   {
      Enumeration allPersons = getAllPersons();
      Object[][] res = new Object[persons.size()][5];
      for (int i = 0; allPersons.hasMoreElements(); i++)
      {
         Person pers = (Person) allPersons.nextElement();
         res[i][0] = pers.getFirstName();
         res[i][1] = pers.getName();
         res[i][2] = pers.getDateOfBirth();
         res[i][3] = pers.getSalary();
         res[i][4] = pers.getTitle();
      }
      return res;
   }

   /**
    */
   public static java.util.Enumeration getAllPersons(String predicate)
   {
      return persons.elements();
   }

   /**
    */
   public static Person getFirstPerson()
   {
      return first;
   }

   /**
    */
   public String getName()
   {
      return name;
   }

   /** */
   public void setName(String name)
   {
      this.name = name;
   }

   /** */
   public String getFirstName()
   {
      return firstName;
   }

   /** */
   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   /** */
   public TitleKey getTitle()
   {
      return title;
   }

   /** */
   public void setTitle(TitleKey title)
   {
      this.title = title;
   }

   /** */
   public Money getSalary()
   {
      return salary;
   }

   /** */
   public void setSalary(Money money)
   {
      this.salary = money;
   }

   /** */
   public Calendar getDateOfBirth()
   {
      return dateOfBirth;
   }

   /** */
   public void setDateOfBirth(Calendar dateOfBirth)
   {
      this.dateOfBirth = dateOfBirth;
   }

   /** */
   public boolean getMarried()
   {
      return married;
   }

   /** */
   public void setMarried(boolean married)
   {
      this.married = married;
   }

   /** */
   public java.util.Iterator getAllChildren()
   {
      createChildren();

      return children.iterator();
   }

   /** */
   public void addToChildren(Person child)
   {
      children.add(child);
   }

   /** */
   public void removeFromChildren(Person child)
   {
      children.remove(child);
   }

   /**
    *
    */
   public Address getPrivateAddress()
   {
      return privateAddress;
   }

   /**
    *
    */
   public Address getBusinessAddress()
   {
      return businessAddress;
   }
}
