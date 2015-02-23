
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * 
 * 		    The HistoricalState represents a snapshot of the historic states of an
 *             activity instance.
 *             
 * 
 * <p>Java-Klasse für HistoricalState complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="HistoricalState">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="activityDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityState" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState"/>
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="until" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *         &lt;element name="onBehalfOfParticipant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *         &lt;element name="onBehalfOfUser" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoricalState", propOrder = {
    "activityOid",
    "activityDefinitionId",
    "processInstanceOid",
    "processDefinitionId",
    "activityState",
    "from",
    "until",
    "participant",
    "onBehalfOfParticipant",
    "onBehalfOfUser",
    "user"
})
public class HistoricalStateXto {

    protected long activityOid;
    @XmlElement(required = true)
    protected String activityDefinitionId;
    protected long processInstanceOid;
    @XmlElement(required = true)
    protected String processDefinitionId;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter4 .class)
    protected ActivityInstanceState activityState;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date from;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date until;
    @XmlElement(required = true)
    protected ParticipantInfoXto participant;
    @XmlElement(required = true)
    protected ParticipantInfoXto onBehalfOfParticipant;
    @XmlElement(required = true)
    protected UserInfoXto onBehalfOfUser;
    @XmlElement(required = true)
    protected UserXto user;

    /**
     * Ruft den Wert der activityOid-Eigenschaft ab.
     * 
     */
    public long getActivityOid() {
        return activityOid;
    }

    /**
     * Legt den Wert der activityOid-Eigenschaft fest.
     * 
     */
    public void setActivityOid(long value) {
        this.activityOid = value;
    }

    /**
     * Ruft den Wert der activityDefinitionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityDefinitionId() {
        return activityDefinitionId;
    }

    /**
     * Legt den Wert der activityDefinitionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityDefinitionId(String value) {
        this.activityDefinitionId = value;
    }

    /**
     * Ruft den Wert der processInstanceOid-Eigenschaft ab.
     * 
     */
    public long getProcessInstanceOid() {
        return processInstanceOid;
    }

    /**
     * Legt den Wert der processInstanceOid-Eigenschaft fest.
     * 
     */
    public void setProcessInstanceOid(long value) {
        this.processInstanceOid = value;
    }

    /**
     * Ruft den Wert der processDefinitionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * Legt den Wert der processDefinitionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionId(String value) {
        this.processDefinitionId = value;
    }

    /**
     * Ruft den Wert der activityState-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ActivityInstanceState getActivityState() {
        return activityState;
    }

    /**
     * Legt den Wert der activityState-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityState(ActivityInstanceState value) {
        this.activityState = value;
    }

    /**
     * Ruft den Wert der from-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Legt den Wert der from-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(Date value) {
        this.from = value;
    }

    /**
     * Ruft den Wert der until-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getUntil() {
        return until;
    }

    /**
     * Legt den Wert der until-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUntil(Date value) {
        this.until = value;
    }

    /**
     * Ruft den Wert der participant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getParticipant() {
        return participant;
    }

    /**
     * Legt den Wert der participant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setParticipant(ParticipantInfoXto value) {
        this.participant = value;
    }

    /**
     * Ruft den Wert der onBehalfOfParticipant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getOnBehalfOfParticipant() {
        return onBehalfOfParticipant;
    }

    /**
     * Legt den Wert der onBehalfOfParticipant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setOnBehalfOfParticipant(ParticipantInfoXto value) {
        this.onBehalfOfParticipant = value;
    }

    /**
     * Ruft den Wert der onBehalfOfUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getOnBehalfOfUser() {
        return onBehalfOfUser;
    }

    /**
     * Legt den Wert der onBehalfOfUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setOnBehalfOfUser(UserInfoXto value) {
        this.onBehalfOfUser = value;
    }

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setUser(UserXto value) {
        this.user = value;
    }

}
