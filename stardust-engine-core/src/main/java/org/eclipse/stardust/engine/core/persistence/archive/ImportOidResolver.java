package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.persistence.ForeignKey;

public class ImportOidResolver
{

   private final Map<Class, Map<Long, Long>> idMap;

   /**
    * 
    * @param classToRuntimeOidMap
    *           Map with element class as Key to Map of imported runtimeOid to current
    *           environment's runtimeOid
    */
   public ImportOidResolver(Map<Class, Map<Long, Long>> classToRuntimeOidMap)
   {
      super();
      this.idMap = classToRuntimeOidMap;
   }

   public Object resolve(Field field, Long fieldValue)
   {
      Long resolvedValue;

      ForeignKey annotation = field.getAnnotation(ForeignKey.class);

      if (annotation != null
            && !IdentifiableElement.class.equals(annotation.modelElement()))
      {
         resolvedValue = idMap.get(annotation.modelElement()).get(fieldValue);
         if (resolvedValue == null)
         {
            throw new IllegalStateException("Failed to resolve import oid for "
                  + field.getName() + " with old value: " + fieldValue);
         }
      }
      else
      {
         resolvedValue = fieldValue;
      }

      return resolvedValue;
   }

}
