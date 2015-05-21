
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.DeployedRuntimeArtifactQueryXto;


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
 *         &lt;element name="deployedRuntimeArtifactQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}DeployedRuntimeArtifactQuery"/>
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
    "deployedRuntimeArtifactQuery"
})
@XmlRootElement(name = "findRuntimeArtifacts")
public class FindRuntimeArtifacts {

    @XmlElement(required = true, nillable = true)
    protected DeployedRuntimeArtifactQueryXto deployedRuntimeArtifactQuery;

    /**
     * Gets the value of the deployedRuntimeArtifactQuery property.
     * 
     * @return
     *     possible object is
     *     {@link DeployedRuntimeArtifactQueryXto }
     *     
     */
    public DeployedRuntimeArtifactQueryXto getDeployedRuntimeArtifactQuery() {
        return deployedRuntimeArtifactQuery;
    }

    /**
     * Sets the value of the deployedRuntimeArtifactQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeployedRuntimeArtifactQueryXto }
     *     
     */
    public void setDeployedRuntimeArtifactQuery(DeployedRuntimeArtifactQueryXto value) {
        this.deployedRuntimeArtifactQuery = value;
    }

}
