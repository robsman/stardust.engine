
package org.eclipse.stardust.engine.api.ws.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * 
 *         Restricts the resulting items to the ones having or not having a specific activity state.
 *         
 * 
 * <p>Java class for ActivityStateFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivityStateFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="states">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityStateFilter", propOrder = {
    "states"
})
public class ActivityStateFilterXto
    extends PredicateBaseXto
{

    @XmlElement(required = true)
    protected ActivityStateFilterXto.StatesXto states;

    /**
     * Gets the value of the states property.
     * 
     * @return
     *     possible object is
     *     {@link ActivityStateFilterXto.StatesXto }
     *     
     */
    public ActivityStateFilterXto.StatesXto getStates() {
        return states;
    }

    /**
     * Sets the value of the states property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivityStateFilterXto.StatesXto }
     *     
     */
    public void setStates(ActivityStateFilterXto.StatesXto value) {
        this.states = value;
    }


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
     *         &lt;element name="state" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "state"
    })
    public static class StatesXto {

        @XmlElement(type = String.class)
        @XmlJavaTypeAdapter(Adapter2 .class)
        protected List<ActivityInstanceState> state;
        @XmlAttribute(name = "inclusive")
        protected Boolean inclusive;

        /**
         * Gets the value of the state property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the state property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getState().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<ActivityInstanceState> getState() {
            if (state == null) {
                state = new ArrayList<ActivityInstanceState>();
            }
            return this.state;
        }

        /**
         * Gets the value of the inclusive property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isInclusive() {
            if (inclusive == null) {
                return true;
            } else {
                return inclusive;
            }
        }

        /**
         * Sets the value of the inclusive property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setInclusive(Boolean value) {
            this.inclusive = value;
        }

    }

}
