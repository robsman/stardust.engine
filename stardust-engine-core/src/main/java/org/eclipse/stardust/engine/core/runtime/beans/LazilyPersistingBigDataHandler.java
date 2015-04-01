package org.eclipse.stardust.engine.core.runtime.beans;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListenerAction;

public class LazilyPersistingBigDataHandler implements BigDataHandler
{
   private Object transientValue;

   @Override
   public void write(Object value, boolean forceRefresh)
   {
      this.transientValue = value;
   }

   @Override
   public Object read()
   {
      return transientValue;
   }

   @Override
   public void refresh()
   {
      // nothing to be done
   }
   
   public static class LazilySerializedDataValueListener implements IPersistentListener, IPersistentListener.Factory
   {

      public void created(Persistent persistent)
      {
         if (persistent instanceof DataValueBean)
         {
            // ensure the current value gets properly serialized
            ((DataValueBean) persistent).triggerSerialization();
         }

      }

      public void updated(Persistent persistent)
      {
         // forward to the CREATED handler in order to DRY
         created(persistent);
      }

      public List<IPersistentListener> createListener(Class< ? extends Persistent> clazz)
      {
         if (DataValueBean.class.equals(clazz))
         {
            return singletonList((IPersistentListener) this);
         }

         return emptyList();
      }
   }
   
}