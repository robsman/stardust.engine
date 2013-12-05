package org.eclipse.stardust.engine.extensions.camel.converter;

public interface DataConverter {
   public abstract String getFromEndpoint();

/**
 * @param endpoint
 */
public abstract void setFromEndpoint(String endpoint);

/**
* @return the target type
*/
public abstract String getTargetType();

/**
* @param clazz
*/
public abstract void setTargetType(String clazz);
}
