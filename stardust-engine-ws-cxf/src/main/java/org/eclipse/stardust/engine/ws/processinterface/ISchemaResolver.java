package org.eclipse.stardust.engine.ws.processinterface;

import org.eclipse.xsd.XSDSchema;

public interface ISchemaResolver
{

   XSDSchema resolveSchema (String modelId, String typeDeclationId);
   
}
