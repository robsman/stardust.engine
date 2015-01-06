
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
 *         &lt;element name="spawnProcessId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="copyData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="parameters" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *         &lt;element name="abortProcessInstance" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "spawnProcessId",
    "copyData",
    "parameters",
    "abortProcessInstance",
    "comment"
})
@XmlRootElement(name = "spawnPeerProcessInstance")
public class SpawnPeerProcessInstance {

    protected long processInstanceOid;
    @XmlElement(required = true)
    protected String spawnProcessId;
    protected boolean copyData;
    @XmlElement(required = true)
    protected ParametersXto parameters;
    protected boolean abortProcessInstance;
    @XmlElement(required = true)
    protected String comment;

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
     * Gets the value of the spawnProcessId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpawnProcessId() {
        return spawnProcessId;
    }

    /**
     * Sets the value of the spawnProcessId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpawnProcessId(String value) {
        this.spawnProcessId = value;
    }

    /**
     * Gets the value of the copyData property.
     * 
     */
    public boolean isCopyData() {
        return copyData;
    }

    /**
     * Sets the value of the copyData property.
     * 
     */
    public void setCopyData(boolean value) {
        this.copyData = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setParameters(ParametersXto value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the abortProcessInstance property.
     * 
     */
    public boolean isAbortProcessInstance() {
        return abortProcessInstance;
    }

    /**
     * Sets the value of the abortProcessInstance property.
     * 
     */
    public void setAbortProcessInstance(boolean value) {
        this.abortProcessInstance = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
