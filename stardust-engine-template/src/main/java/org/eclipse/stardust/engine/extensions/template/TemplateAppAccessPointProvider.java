package org.eclipse.stardust.engine.extensions.template;

import java.util.Iterator;
import java.util.Map;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;

public class TemplateAppAccessPointProvider
implements AccessPointProvider{

   @Override
   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      return null;
   }
}
