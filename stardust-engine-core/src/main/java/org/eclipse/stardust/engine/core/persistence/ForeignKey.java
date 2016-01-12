package org.eclipse.stardust.engine.core.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;

/**
 * ForeignKey annotation is typically used to indicate to which type of object 
 * a primitive field relates to. This should typically be used on long fields
 * where the long value is the identifier for an instance of the annotated type.
 * 
 * @author jsaayman
 *
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface ForeignKey
{
   Class<? extends IdentifiableElement> modelElement() default IdentifiableElement.class;  

   Class<? extends IdentifiablePersistent> persistentElement() default IdentifiablePersistent.class;  
 
}
