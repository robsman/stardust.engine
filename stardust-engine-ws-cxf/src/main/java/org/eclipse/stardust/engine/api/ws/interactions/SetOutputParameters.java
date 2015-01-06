
package org.eclipse.stardust.engine.api.ws.interactions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.ParametersXto;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="interactionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="outputParameters" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "interactionId",
    "outputParameters"
})
@XmlRootElement(name = "setOutputParameters")
public class SetOutputParameters {

    @XmlElement(required = true, nillable = true)
    protected String interactionId;
    @XmlElement(required = true, nillable = true)
    protected ParametersXto outputParameters;

    /**
     * Gets the value of the interactionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInteractionId() {
        return interactionId;
    }

    /**
     * Sets the value of the interactionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInteractionId(String value) {
        this.interactionId = value;
    }

    /**
     * Gets the value of the outputParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getOutputParameters() {
        return outputParameters;
    }

    /**
     * Sets the value of the outputParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setOutputParameters(ParametersXto value) {
        this.outputParameters = value;
    }

}
