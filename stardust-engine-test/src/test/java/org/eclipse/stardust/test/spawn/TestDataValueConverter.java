/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.test.spawn;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.DataValueConverter;
import org.eclipse.stardust.engine.api.runtime.DataValueProvider;

public class TestDataValueConverter implements DataValueConverter
{
   @Override
   public Set<String> convertDataValues(DataValueProvider provider)
   {
      String value = (String) provider.getSourceValue("Primitive1");
      HashMap<String, Object> result = new HashMap<String, Object>();
      result.put("myString", value);
      provider.setValue("Struct1", result);
      return null;
   }
}
