
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
 *         &lt;element name="deployedRuntimeArtifact" type="{http://eclipse.org/stardust/ws/v2012a/api}DeployedRuntimeArtifact"/>
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
    "deployedRuntimeArtifact"
})
@XmlRootElement(name = "deployRuntimeArtifactResponse")
public class DeployRuntimeArtifactResponse {

    @XmlElement(required = true)
    protected DeployedRuntimeArtifactXto deployedRuntimeArtifact;

    /**
     * Gets the value of the deployedRuntimeArtifact property.
     * 
     * @return
     *     possible object is
     *     {@link DeployedRuntimeArtifactXto }
     *     
     */
    public DeployedRuntimeArtifactXto getDeployedRuntimeArtifact() {
        return deployedRuntimeArtifact;
    }

    /**
     * Sets the value of the deployedRuntimeArtifact property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeployedRuntimeArtifactXto }
     *     
     */
    public void setDeployedRuntimeArtifact(DeployedRuntimeArtifactXto value) {
        this.deployedRuntimeArtifact = value;
    }

}
