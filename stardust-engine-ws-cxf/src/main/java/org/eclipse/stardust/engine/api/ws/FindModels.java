
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.query.DeployedModelQueryXto;


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
 *         &lt;element name="deployedModelQuery" type="{http://eclipse.org/stardust/ws/v2012a/api/query}DeployedModelQuery"/>
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
    "deployedModelQuery"
})
@XmlRootElement(name = "findModels")
public class FindModels {

    @XmlElement(required = true, nillable = true)
    protected DeployedModelQueryXto deployedModelQuery;

    /**
     * Gets the value of the deployedModelQuery property.
     * 
     * @return
     *     possible object is
     *     {@link DeployedModelQueryXto }
     *     
     */
    public DeployedModelQueryXto getDeployedModelQuery() {
        return deployedModelQuery;
    }

    /**
     * Sets the value of the deployedModelQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeployedModelQueryXto }
     *     
     */
    public void setDeployedModelQuery(DeployedModelQueryXto value) {
        this.deployedModelQuery = value;
    }

}
