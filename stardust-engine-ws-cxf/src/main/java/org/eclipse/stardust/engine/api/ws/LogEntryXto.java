
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *  			The LogEntry provides information about the various messages the engine is
 *  			logging into AuditTrail.
 * 			
 * 
 * <p>Java-Klasse für LogEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LogEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="timeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="subject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://eclipse.org/stardust/ws/v2012a/api}LogType"/>
 *         &lt;element name="code" type="{http://eclipse.org/stardust/ws/v2012a/api}LogCode"/>
 *         &lt;element name="userOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
 *         &lt;element name="context" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogEntry", propOrder = {
    "oid",
    "timeStamp",
    "subject",
    "activityOid",
    "processOid",
    "type",
    "code",
    "userOid",
    "user",
    "context"
})
public class LogEntryXto {

    protected long oid;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date timeStamp;
    @XmlElement(required = true)
    protected String subject;
    protected long activityOid;
    protected long processOid;
    @XmlElement(required = true)
    protected LogTypeXto type;
    @XmlElement(required = true)
    protected LogCodeXto code;
    protected long userOid;
    @XmlElement(required = true)
    protected UserXto user;
    @XmlElement(required = true)
    protected String context;

    /**
     * Ruft den Wert der oid-Eigenschaft ab.
     * 
     */
    public long getOid() {
        return oid;
    }

    /**
     * Legt den Wert der oid-Eigenschaft fest.
     * 
     */
    public void setOid(long value) {
        this.oid = value;
    }

    /**
     * Ruft den Wert der timeStamp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Legt den Wert der timeStamp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeStamp(Date value) {
        this.timeStamp = value;
    }

    /**
     * Ruft den Wert der subject-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Legt den Wert der subject-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

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
     * Ruft den Wert der processOid-Eigenschaft ab.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Legt den Wert der processOid-Eigenschaft fest.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LogTypeXto }
     *     
     */
    public LogTypeXto getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LogTypeXto }
     *     
     */
    public void setType(LogTypeXto value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der code-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LogCodeXto }
     *     
     */
    public LogCodeXto getCode() {
        return code;
    }

    /**
     * Legt den Wert der code-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LogCodeXto }
     *     
     */
    public void setCode(LogCodeXto value) {
        this.code = value;
    }

    /**
     * Ruft den Wert der userOid-Eigenschaft ab.
     * 
     */
    public long getUserOid() {
        return userOid;
    }

    /**
     * Legt den Wert der userOid-Eigenschaft fest.
     * 
     */
    public void setUserOid(long value) {
        this.userOid = value;
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

    /**
     * Ruft den Wert der context-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContext() {
        return context;
    }

    /**
     * Legt den Wert der context-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContext(String value) {
        this.context = value;
    }

}
