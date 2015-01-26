package org.eclipse.stardust.engine.core.compatibility.ipp;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AsynchronousApplicationInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;

public class DeprecatedApplicationInstance implements SynchronousApplicationInstance,
   AsynchronousApplicationInstance
{
   private String applicationName;
   
   @Override
   public void bootstrap(ActivityInstance activityInstance)
   {
      this.applicationName = activityInstance.getActivity().getApplication().getName();
      throw new UnsupportedOperationException("Application type of " +
            this.applicationName + " is no longer supported");
   }

   @Override
   public void setInAccessPointValue(String name, Object value)
   {
      throw new UnsupportedOperationException("Application type of " +
            this.applicationName + " is no longer supported");
   }

   @Override
   public Object getOutAccessPointValue(String name)
   {
      throw new UnsupportedOperationException("Application type of " +
            this.applicationName + " is no longer supported");
   }

   @Override
   public void cleanup()
   {}

   @Override
   public void send() throws InvocationTargetException
   {}

   @Override
   public Map receive(Map data, Iterator outDataTypes)
   {
      return null;
   }

   @Override
   public boolean isSending()
   {
      return false;
   }

   @Override
   public boolean isReceiving()
   {
      return false;
   }

   @Override
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      return Collections.EMPTY_MAP;
   }

}
