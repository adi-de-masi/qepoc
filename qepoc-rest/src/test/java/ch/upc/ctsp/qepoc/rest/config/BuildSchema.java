/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * TODO: add type comment.
 * 
 */
public class BuildSchema {
    public static void main(final String[] args) throws IOException, JAXBException {
        JAXBContext.newInstance(RuleSet.class).generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(final String namespaceUri, final String suggestedFileName) throws IOException {
                return new StreamResult(new File("target/" + suggestedFileName));
            }
        });
    }
}
