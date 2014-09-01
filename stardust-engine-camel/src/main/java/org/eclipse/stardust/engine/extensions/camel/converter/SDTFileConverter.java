package org.eclipse.stardust.engine.extensions.camel.converter;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;

import java.io.IOException;
import java.util.Map;
import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.extensions.camel.util.converter.StructuredDataTranslator;

/**
 * @deprecated will be removed in IPP 8.2 version
 */
public class SDTFileConverter implements DataConverter {

    private String fromEndpoint;
    private String targetType;
    private StructuredDataTranslator translator;

    /**
     * @return the from Endpoint
     */
    public String getFromEndpoint() {
        return fromEndpoint;
    }

    /**
     * @param fromEndpoint
     */
    public void setFromEndpoint(String fromEndpoint) {
        this.fromEndpoint = fromEndpoint;
    }

    /**
     * @return the target type
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * @param targetType
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * @return translator the translator
     */
    public StructuredDataTranslator getTranslator() {
        return translator;
    }

    /**
     * @param translator the translator
     */
    public void setTranslator(StructuredDataTranslator translator) {
        this.translator = translator;
    }

    /**
     * Create a SDT from an XSD schema
     * 
     * @param schemaPath
     * @param schemaElementName
     * @param exchange
     * @return convert XSD to SDT
     * @throws IOException
     */
    public Map<?, ?> genericXSDToSDT(String schemaPath,
            String schemaElementName, Exchange exchange) throws IOException {
        // the following line was added to work around a bug in Camel
        
        if (exchange.getIn().getBody() instanceof Map<?, ?>)
        {
            return (Map<?, ?>) exchange.getIn().getBody();
        }
        else
        {   
            String partition = (String) exchange.getIn().getHeader(PARTITION);
            
            schemaPath = schemaPath.replace('*', '.');
            
            translator.setXsdSchemaClasspathLocation(schemaPath, partition != null ? partition : "default");
            
            String sdtContent = exchange.getContext().getTypeConverter()
                    .convertTo(String.class, exchange, exchange.getIn().getBody());
            
            return (translator.convert(schemaElementName, sdtContent));
        }
    }
}
