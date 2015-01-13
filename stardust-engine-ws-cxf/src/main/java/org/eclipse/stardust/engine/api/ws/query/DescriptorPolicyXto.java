
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
 * <p>Java-Klasse für DescriptorPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der includeDescriptors-Eigenschaft ab.
     * 
     */
    public boolean isIncludeDescriptors() {
        return includeDescriptors;
    }

    /**
     * Legt den Wert der includeDescriptors-Eigenschaft fest.
     * 
     */
    public void setIncludeDescriptors(boolean value) {
        this.includeDescriptors = value;
    }

}
