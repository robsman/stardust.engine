
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an WorklistQuery execution.
 * 			Containing 'userWorklist' and 'sharedWorklists' according to the user and participants contribution specified in the query.
 * 			
 * 
 * <p>Java-Klasse für Worklist complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Worklist">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userWorklist">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *                 &lt;sequence>
 *                   &lt;element name="owner" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *                   &lt;element name="workItems" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="sharedWorklists">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="sharedWorklist" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *                           &lt;sequence>
 *                             &lt;element name="owner" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
 *                             &lt;element name="workItems" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Worklist", propOrder = {
    "userWorklist",
    "sharedWorklists"
})
public class WorklistXto {

    @XmlElement(required = true)
    protected WorklistXto.UserWorklistXto userWorklist;
    @XmlElement(required = true)
    protected WorklistXto.SharedWorklistsXto sharedWorklists;

    /**
     * Ruft den Wert der userWorklist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link WorklistXto.UserWorklistXto }
     *     
     */
    public WorklistXto.UserWorklistXto getUserWorklist() {
        return userWorklist;
    }

    /**
     * Legt den Wert der userWorklist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link WorklistXto.UserWorklistXto }
     *     
     */
    public void setUserWorklist(WorklistXto.UserWorklistXto value) {
        this.userWorklist = value;
    }

    /**
     * Ruft den Wert der sharedWorklists-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link WorklistXto.SharedWorklistsXto }
     *     
     */
    public WorklistXto.SharedWorklistsXto getSharedWorklists() {
        return sharedWorklists;
    }

    /**
     * Legt den Wert der sharedWorklists-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link WorklistXto.SharedWorklistsXto }
     *     
     */
    public void setSharedWorklists(WorklistXto.SharedWorklistsXto value) {
        this.sharedWorklists = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="sharedWorklist" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
     *                 &lt;sequence>
     *                   &lt;element name="owner" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
     *                   &lt;element name="workItems" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "sharedWorklist"
    })
    public static class SharedWorklistsXto {

        protected List<WorklistXto.SharedWorklistsXto.SharedWorklistXto> sharedWorklist;

        /**
         * Gets the value of the sharedWorklist property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the sharedWorklist property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSharedWorklist().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link WorklistXto.SharedWorklistsXto.SharedWorklistXto }
         * 
         * 
         */
        public List<WorklistXto.SharedWorklistsXto.SharedWorklistXto> getSharedWorklist() {
            if (sharedWorklist == null) {
                sharedWorklist = new ArrayList<WorklistXto.SharedWorklistsXto.SharedWorklistXto>();
            }
            return this.sharedWorklist;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
         *       &lt;sequence>
         *         &lt;element name="owner" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
         *         &lt;element name="workItems" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances"/>
         *       &lt;/sequence>
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "owner",
            "workItems"
        })
        public static class SharedWorklistXto
            extends QueryResultXto
        {

            @XmlElement(required = true)
            protected UserInfoXto owner;
            @XmlElement(required = true)
            protected ActivityInstancesXto workItems;

            /**
             * Ruft den Wert der owner-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link UserInfoXto }
             *     
             */
            public UserInfoXto getOwner() {
                return owner;
            }

            /**
             * Legt den Wert der owner-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link UserInfoXto }
             *     
             */
            public void setOwner(UserInfoXto value) {
                this.owner = value;
            }

            /**
             * Ruft den Wert der workItems-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link ActivityInstancesXto }
             *     
             */
            public ActivityInstancesXto getWorkItems() {
                return workItems;
            }

            /**
             * Legt den Wert der workItems-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link ActivityInstancesXto }
             *     
             */
            public void setWorkItems(ActivityInstancesXto value) {
                this.workItems = value;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
     *       &lt;sequence>
     *         &lt;element name="owner" type="{http://eclipse.org/stardust/ws/v2012a/api}UserInfo"/>
     *         &lt;element name="workItems" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstances"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "owner",
        "workItems"
    })
    public static class UserWorklistXto
        extends QueryResultXto
    {

        @XmlElement(required = true)
        protected UserInfoXto owner;
        @XmlElement(required = true)
        protected ActivityInstancesXto workItems;

        /**
         * Ruft den Wert der owner-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link UserInfoXto }
         *     
         */
        public UserInfoXto getOwner() {
            return owner;
        }

        /**
         * Legt den Wert der owner-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link UserInfoXto }
         *     
         */
        public void setOwner(UserInfoXto value) {
            this.owner = value;
        }

        /**
         * Ruft den Wert der workItems-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ActivityInstancesXto }
         *     
         */
        public ActivityInstancesXto getWorkItems() {
            return workItems;
        }

        /**
         * Legt den Wert der workItems-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ActivityInstancesXto }
         *     
         */
        public void setWorkItems(ActivityInstancesXto value) {
            this.workItems = value;
        }

    }

}
