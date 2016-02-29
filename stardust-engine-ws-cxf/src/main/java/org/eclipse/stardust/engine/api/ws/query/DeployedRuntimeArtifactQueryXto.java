
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *       Query container for building complex queries for runtime artifacts.
 *       
 * 
 * <p>Java class for DeployedRuntimeArtifactQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeployedRuntimeArtifactQuery">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}Query">
 *       &lt;sequence>
 *         &lt;element name="includeOnlyActive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeployedRuntimeArtifactQuery", propOrder = {
    "includeOnlyActive"
})
public class DeployedRuntimeArtifactQueryXto
    extends QueryXto
{

    protected Boolean includeOnlyActive;

    /**
     * Gets the value of the includeOnlyActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeOnlyActive() {
        return includeOnlyActive;
    }

    /**
     * Sets the value of the includeOnlyActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeOnlyActive(Boolean value) {
        this.includeOnlyActive = value;
    }

}
