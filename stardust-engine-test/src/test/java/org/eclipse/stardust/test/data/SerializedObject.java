package org.eclipse.stardust.test.data;

import java.io.Serializable;
import java.util.Random;

public class SerializedObject implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String firstName;

   private String lastName;

   private String randomString;

   public SerializedObject()
   {
      firstName = "John";
      lastName = "Doe";

      randomString = VolatileDataTest.generateString(new Random(), "ABCEFGHIJKLMNOPQRSTUVWXYZ", 500);
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public String getRandomString()
   {
      return randomString;
   }

   public void setRandomString(String randomString)
   {
      this.randomString = randomString;
   }
   
   
}