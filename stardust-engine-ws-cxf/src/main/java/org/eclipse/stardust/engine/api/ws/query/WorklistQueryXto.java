
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	    Additionally supports optional elements:
 * 	    A 'userContribution' which enables or disables the retrieval of the user's private worklist with an optional 'subsetPolicy'.
 * 	    A list of 'participantContributions' limiting the result to specified participants. One 'subsetPolicy' per 'participantContribution' can be set.
 * 	    
 * 
 * <p>Java-Klasse für WorklistQuery complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="WorklistQuery">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}Query">
 *       &lt;sequence>
 *         &lt;element name="userContribution" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserContribution" minOccurs="0"/>
 *         &lt;element name="participantContributions" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ParticipantContributions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorklistQuery", propOrder = {
    "userContribution",
    "participantContributions"
})
public class WorklistQueryXto
    extends QueryXto
{

    protected UserContributionXto userContribution;
    protected ParticipantContributionsXto participantContributions;

    /**
     * Ruft den Wert der userContribution-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserContributionXto }
     *     
     */
    public UserContributionXto getUserContribution() {
        return userContribution;
    }

    /**
     * Legt den Wert der userContribution-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserContributionXto }
     *     
     */
    public void setUserContribution(UserContributionXto value) {
        this.userContribution = value;
    }

    /**
     * Ruft den Wert der participantContributions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantContributionsXto }
     *     
     */
    public ParticipantContributionsXto getParticipantContributions() {
        return participantContributions;
    }

    /**
     * Legt den Wert der participantContributions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantContributionsXto }
     *     
     */
    public void setParticipantContributions(ParticipantContributionsXto value) {
        this.participantContributions = value;
    }

}
