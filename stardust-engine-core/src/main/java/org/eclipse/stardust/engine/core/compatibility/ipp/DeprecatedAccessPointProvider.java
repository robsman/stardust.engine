package org.eclipse.stardust.engine.core.compatibility.ipp;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;

public class DeprecatedAccessPointProvider implements AccessPointProvider
{

   @Override
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      return CollectionUtils.EMPTY_COLLECTION.iterator();
   }

}
