/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO: add type comment.
 * 
 */
@Data
@EqualsAndHashCode(
        callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        namespace = "http://www.upc-cablecom.ch/query-engine/config")
public class Conditional extends RuleCollection {
    @XmlAttribute
    private String condition;
}
