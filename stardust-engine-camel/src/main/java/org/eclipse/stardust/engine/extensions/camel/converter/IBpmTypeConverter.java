/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.Map;

import org.eclipse.stardust.engine.api.model.DataMapping;

public interface IBpmTypeConverter
{
   void unmarshal(DataMapping mapping, Map<String, Object> extendedAttributes);

   void marshal(DataMapping mapping, Map<String, Object> extendedAttributes);
}
