
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	    A query contains three optional elements:
 * 	    The 'predicate' element used for filtering contains an and-conjunction of predicates.
 * 	    The 'order' element used to specify a custom order.
 * 	    The 'policy' element used to specify policies e.g. a 'subsetPolicy' to limit results to a subset.
 * 		
 * 
 * <p>Java class for Query complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Query">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="predicate" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AndTerm" minOccurs="0"/>
 *         &lt;element name="order" type="{http://eclipse.org/stardust/ws/v2012a/api/query}OrderCriteria" minOccurs="0"/>
 *         &lt;element name="policy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}Policy" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Query", propOrder = {
    "predicate",
    "order",
    "policy"
})
@XmlSeeAlso({
    LogEntryQueryXto.class,
    UserQueryXto.class,
    PreferenceQueryXto.class,
    UserGroupQueryXto.class,
    DeployedModelQueryXto.class,
    ProcessDefinitionQueryXto.class,
    DocumentQueryXto.class,
    ProcessQueryXto.class,
    VariableDefinitionQueryXto.class,
    WorklistQueryXto.class,
    ActivityQueryXto.class
})
public class QueryXto {

    protected AndTermXto predicate;
    protected OrderCriteriaXto order;
    protected PolicyXto policy;

    /**
     * Gets the value of the predicate property.
     * 
     * @return
     *     possible object is
     *     {@link AndTermXto }
     *     
     */
    public AndTermXto getPredicate() {
        return predicate;
    }

    /**
     * Sets the value of the predicate property.
     * 
     * @param value
     *     allowed object is
     *     {@link AndTermXto }
     *     
     */
    public void setPredicate(AndTermXto value) {
        this.predicate = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link OrderCriteriaXto }
     *     
     */
    public OrderCriteriaXto getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderCriteriaXto }
     *     
     */
    public void setOrder(OrderCriteriaXto value) {
        this.order = value;
    }

    /**
     * Gets the value of the policy property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyXto }
     *     
     */
    public PolicyXto getPolicy() {
        return policy;
    }

    /**
     * Sets the value of the policy property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyXto }
     *     
     */
    public void setPolicy(PolicyXto value) {
        this.policy = value;
    }

}
