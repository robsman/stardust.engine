
package org.eclipse.stardust.engine.api.ws.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.ParticipantInfoXto;


/**
 * 
 *         Restricts the resulting items to the ones that are performed on behalf of the specified user group(s) or model participant(s).
 *         
 * 
 * <p>Java class for PerformingOnBehalfOfFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerformingOnBehalfOfFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="modelParticipant">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *                   &lt;/sequence>
 *                   &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="modelParticipants">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="participants" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo" maxOccurs="unbounded" minOccurs="0"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformingOnBehalfOfFilter", propOrder = {
    "modelParticipant",
    "modelParticipants"
})
public class PerformingOnBehalfOfFilterXto
    extends PredicateBaseXto
{

    protected PerformingOnBehalfOfFilterXto.ModelParticipantXto modelParticipant;
    protected PerformingOnBehalfOfFilterXto.ModelParticipantsXto modelParticipants;

    /**
     * Gets the value of the modelParticipant property.
     * 
     * @return
     *     possible object is
     *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantXto }
     *     
     */
    public PerformingOnBehalfOfFilterXto.ModelParticipantXto getModelParticipant() {
        return modelParticipant;
    }

    /**
     * Sets the value of the modelParticipant property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantXto }
     *     
     */
    public void setModelParticipant(PerformingOnBehalfOfFilterXto.ModelParticipantXto value) {
        this.modelParticipant = value;
    }

    /**
     * Gets the value of the modelParticipants property.
     * 
     * @return
     *     possible object is
     *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto }
     *     
     */
    public PerformingOnBehalfOfFilterXto.ModelParticipantsXto getModelParticipants() {
        return modelParticipants;
    }

    /**
     * Sets the value of the modelParticipants property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto }
     *     
     */
    public void setModelParticipants(PerformingOnBehalfOfFilterXto.ModelParticipantsXto value) {
        this.modelParticipants = value;
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
     *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
     *       &lt;/sequence>
     *       &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "participant"
    })
    public static class ModelParticipantXto {

        @XmlElement(required = true)
        protected ParticipantInfoXto participant;
        @XmlAttribute
        protected Boolean recursively;

        /**
         * Gets the value of the participant property.
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
         * Sets the value of the participant property.
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
         * Gets the value of the recursively property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isRecursively() {
            if (recursively == null) {
                return true;
            } else {
                return recursively;
            }
        }

        /**
         * Sets the value of the recursively property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setRecursively(Boolean value) {
            this.recursively = value;
        }

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
     *         &lt;element name="participants" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo" maxOccurs="unbounded" minOccurs="0"/>
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
    @XmlType(name = "", propOrder = {
        "participants"
    })
    public static class ModelParticipantsXto {

        protected PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto participants;

        /**
         * Gets the value of the participants property.
         * 
         * @return
         *     possible object is
         *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto }
         *     
         */
        public PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto getParticipants() {
            return participants;
        }

        /**
         * Sets the value of the participants property.
         * 
         * @param value
         *     allowed object is
         *     {@link PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto }
         *     
         */
        public void setParticipants(PerformingOnBehalfOfFilterXto.ModelParticipantsXto.ParticipantsXto value) {
            this.participants = value;
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
         *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo" maxOccurs="unbounded" minOccurs="0"/>
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
            "participant"
        })
        public static class ParticipantsXto {

            protected List<ParticipantInfoXto> participant;

            /**
             * Gets the value of the participant property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the participant property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getParticipant().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link ParticipantInfoXto }
             * 
             * 
             */
            public List<ParticipantInfoXto> getParticipant() {
                if (participant == null) {
                    participant = new ArrayList<ParticipantInfoXto>();
                }
                return this.participant;
            }

        }

    }

}
