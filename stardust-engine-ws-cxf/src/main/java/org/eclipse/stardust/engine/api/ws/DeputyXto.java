
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java-Klasse für Deputy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Deputy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *         &lt;element name="deputyUser" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *         &lt;element name="fromDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="untilDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="participants" type="{http://eclipse.org/stardust/ws/v2012a/api}ModelParticipantInfos"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Deputy", propOrder = {
    "user",
    "deputyUser",
    "fromDate",
    "untilDate",
    "participants"
})
public class DeputyXto {

    @XmlElement(required = true)
    protected UserInfoXto user;
    @XmlElement(required = true)
    protected UserInfoXto deputyUser;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date fromDate;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date untilDate;
    @XmlElement(required = true)
    protected ModelParticipantInfosXto participants;

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setUser(UserInfoXto value) {
        this.user = value;
    }

    /**
     * Ruft den Wert der deputyUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserInfoXto }
     *     
     */
    public UserInfoXto getDeputyUser() {
        return deputyUser;
    }

    /**
     * Legt den Wert der deputyUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfoXto }
     *     
     */
    public void setDeputyUser(UserInfoXto value) {
        this.deputyUser = value;
    }

    /**
     * Ruft den Wert der fromDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Legt den Wert der fromDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromDate(Date value) {
        this.fromDate = value;
    }

    /**
     * Ruft den Wert der untilDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getUntilDate() {
        return untilDate;
    }

    /**
     * Legt den Wert der untilDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUntilDate(Date value) {
        this.untilDate = value;
    }

    /**
     * Ruft den Wert der participants-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ModelParticipantInfosXto }
     *     
     */
    public ModelParticipantInfosXto getParticipants() {
        return participants;
    }

    /**
     * Legt den Wert der participants-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelParticipantInfosXto }
     *     
     */
    public void setParticipants(ModelParticipantInfosXto value) {
        this.participants = value;
    }

}
