
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="runtimeArtifact" type="{http://eclipse.org/stardust/ws/v2012a/api}RuntimeArtifact"/>
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
    "runtimeArtifact"
})
@XmlRootElement(name = "getRuntimeArtifactResponse")
public class GetRuntimeArtifactResponse {

    @XmlElement(required = true, nillable = true)
    protected RuntimeArtifactXto runtimeArtifact;

    /**
     * Gets the value of the runtimeArtifact property.
     * 
     * @return
     *     possible object is
     *     {@link RuntimeArtifactXto }
     *     
     */
    public RuntimeArtifactXto getRuntimeArtifact() {
        return runtimeArtifact;
    }

    /**
     * Sets the value of the runtimeArtifact property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuntimeArtifactXto }
     *     
     */
    public void setRuntimeArtifact(RuntimeArtifactXto value) {
        this.runtimeArtifact = value;
    }

}
