
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
 * <p>Java class for Deputy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the user property.
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
     * Sets the value of the user property.
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
     * Gets the value of the deputyUser property.
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
     * Sets the value of the deputyUser property.
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
     * Gets the value of the fromDate property.
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
     * Sets the value of the fromDate property.
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
     * Gets the value of the untilDate property.
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
     * Sets the value of the untilDate property.
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
     * Gets the value of the participants property.
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
     * Sets the value of the participants property.
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
