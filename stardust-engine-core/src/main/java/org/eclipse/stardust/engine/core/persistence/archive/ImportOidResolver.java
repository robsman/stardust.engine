package org.eclipse.stardust.engine.core.persistence.archive;

import java.lang.reflect.Field;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.persistence.ForeignKey;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand.ImportMetaData;

public class ImportOidResolver
{

   private final ImportMetaData importMetaData;

   /**
    * 
    * @param classToRuntimeOidMap
    *           Map with element class as Key to Map of imported runtimeOid to current
    *           environment's runtimeOid
    */
   public ImportOidResolver(ImportMetaData importMetaData)
   {
      super();
      this.importMetaData = importMetaData;
   }

   public Object resolve(Field field, Long fieldValue)
   {
      Long resolvedValue;

      ForeignKey annotation = field.getAnnotation(ForeignKey.class);

      if (annotation != null
            && !IdentifiableElement.class.equals(annotation.modelElement()))
      {
         resolvedValue = importMetaData.getImportId(annotation.modelElement(), fieldValue);
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
