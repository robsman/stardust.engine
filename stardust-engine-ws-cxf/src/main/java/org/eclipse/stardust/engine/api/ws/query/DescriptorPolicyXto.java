
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          Evaluation Policy to include or exclude descriptors.
 *          
 * 
 * <p>Java class for DescriptorPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescriptorPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="includeDescriptors" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptorPolicy", propOrder = {
    "includeDescriptors"
})
public class DescriptorPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(defaultValue = "true")
    protected boolean includeDescriptors;

    /**
     * Gets the value of the includeDescriptors property.
     * 
     */
    public boolean isIncludeDescriptors() {
        return includeDescriptors;
    }

    /**
     * Sets the value of the includeDescriptors property.
     * 
     */
    public void setIncludeDescriptors(boolean value) {
        this.includeDescriptors = value;
    }

}
