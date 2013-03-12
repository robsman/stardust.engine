
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 * 			The Daemon represents a snapshot of a workflow daemon.
 *  			The workflow engine contains two types of daemons: the event daemon and the trigger daemons.
 *  			Daemons can be started and stopped using the AdministrationService.
 * 			
 * 
 * <p>Java class for Daemon complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Daemon">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="startTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="lastExecutionTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="running" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="acknowledgementState" type="{http://eclipse.org/stardust/ws/v2012a/api}AcknowledgementState" minOccurs="0"/>
 *         &lt;element name="daemonExecutionState" type="{http://eclipse.org/stardust/ws/v2012a/api}DaemonExecutionState" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Daemon", propOrder = {
    "type",
    "startTime",
    "lastExecutionTime",
    "running",
    "acknowledgementState",
    "daemonExecutionState"
})
public class DaemonXto {

    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date startTime;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date lastExecutionTime;
    protected boolean running;
    protected AcknowledgementStateXto acknowledgementState;
    protected DaemonExecutionStateXto daemonExecutionState;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(Date value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the lastExecutionTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getLastExecutionTime() {
        return lastExecutionTime;
    }

    /**
     * Sets the value of the lastExecutionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastExecutionTime(Date value) {
        this.lastExecutionTime = value;
    }

    /**
     * Gets the value of the running property.
     * 
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sets the value of the running property.
     * 
     */
    public void setRunning(boolean value) {
        this.running = value;
    }

    /**
     * Gets the value of the acknowledgementState property.
     * 
     * @return
     *     possible object is
     *     {@link AcknowledgementStateXto }
     *     
     */
    public AcknowledgementStateXto getAcknowledgementState() {
        return acknowledgementState;
    }

    /**
     * Sets the value of the acknowledgementState property.
     * 
     * @param value
     *     allowed object is
     *     {@link AcknowledgementStateXto }
     *     
     */
    public void setAcknowledgementState(AcknowledgementStateXto value) {
        this.acknowledgementState = value;
    }

    /**
     * Gets the value of the daemonExecutionState property.
     * 
     * @return
     *     possible object is
     *     {@link DaemonExecutionStateXto }
     *     
     */
    public DaemonExecutionStateXto getDaemonExecutionState() {
        return daemonExecutionState;
    }

    /**
     * Sets the value of the daemonExecutionState property.
     * 
     * @param value
     *     allowed object is
     *     {@link DaemonExecutionStateXto }
     *     
     */
    public void setDaemonExecutionState(DaemonExecutionStateXto value) {
        this.daemonExecutionState = value;
    }

}
