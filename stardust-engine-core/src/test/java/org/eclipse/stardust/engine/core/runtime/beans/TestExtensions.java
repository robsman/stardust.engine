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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.annotations.ConfigurationProperty;
import org.eclipse.stardust.common.annotations.PropertyValueType;
import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Stateful;
import org.eclipse.stardust.common.annotations.Stateless;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

public class TestExtensions
{
   @ConfigurationProperty(status = Status.Internal, useRestriction = UseRestriction.Internal)
   @PropertyValueType(Spi1.class)
   public static final String PRP_SPI1 = "TestConfig.Extensions.SPI1";

   @ConfigurationProperty(status = Status.Internal, useRestriction = UseRestriction.Internal)
   @PropertyValueType(Spi2.class)
   public static final String PRP_SPI2 = "TestConfig.Extensions.SPI2";

   @SPI(status = Status.Internal, useRestriction = UseRestriction.Internal)
   public static interface Spi1
   {
   }

   public static class Spi1ImplA implements Spi1
   {
   }

   public static class Spi1ImplB implements Spi1
   {
   }

   public static class Spi1ImplC implements Spi1
   {
   }

   public static class Spi1ImplD implements Spi1
   {
   }

   @Stateful
   public static class Spi1StatefulImpl implements Spi1
   {
   }

   @Stateless
   public static class Spi1StatelessImpl implements Spi1
   {
   }

   @SPI(status = Status.Internal, useRestriction = UseRestriction.Internal)
   public static interface Spi2
   {
   }

   public static class Spi2ImplA implements Spi2
   {
   }

   public static class Spi2ImplB implements Spi2
   {
   }

   public static class Spi2ImplC implements Spi2
   {
   }
}
