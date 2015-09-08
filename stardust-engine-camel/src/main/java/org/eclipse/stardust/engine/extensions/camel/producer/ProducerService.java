package org.eclipse.stardust.engine.extensions.camel.producer;

import java.util.Map;

public interface ProducerService
{
   public Object send(Object message, Map<String, Object> headers) throws Exception;
}
