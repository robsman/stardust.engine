
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
 *         &lt;element name="processInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processSpawnInfos" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessSpawnInfos"/>
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
    "processInstanceOid",
    "processSpawnInfos"
})
@XmlRootElement(name = "spawnSubprocessInstances")
public class SpawnSubprocessInstances {

    protected long processInstanceOid;
    @XmlElement(required = true)
    protected ProcessSpawnInfosXto processSpawnInfos;

    /**
     * Gets the value of the processInstanceOid property.
     * 
     */
    public long getProcessInstanceOid() {
        return processInstanceOid;
    }

    /**
     * Sets the value of the processInstanceOid property.
     * 
     */
    public void setProcessInstanceOid(long value) {
        this.processInstanceOid = value;
    }

    /**
     * Gets the value of the processSpawnInfos property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessSpawnInfosXto }
     *     
     */
    public ProcessSpawnInfosXto getProcessSpawnInfos() {
        return processSpawnInfos;
    }

    /**
     * Sets the value of the processSpawnInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessSpawnInfosXto }
     *     
     */
    public void setProcessSpawnInfos(ProcessSpawnInfosXto value) {
        this.processSpawnInfos = value;
    }

}
