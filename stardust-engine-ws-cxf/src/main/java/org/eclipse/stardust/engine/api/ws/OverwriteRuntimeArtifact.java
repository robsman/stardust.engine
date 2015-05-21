
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
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
    "oid",
    "runtimeArtifact"
})
@XmlRootElement(name = "overwriteRuntimeArtifact")
public class OverwriteRuntimeArtifact {

    protected long oid;
    @XmlElement(required = true)
    protected RuntimeArtifactXto runtimeArtifact;

    /**
     * Gets the value of the oid property.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Sets the value of the oid property.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
    }

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
