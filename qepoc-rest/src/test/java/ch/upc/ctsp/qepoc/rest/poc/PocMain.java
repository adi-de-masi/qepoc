/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.poc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;

import ch.upc.ctsp.qepoc.client.Client;
import ch.upc.ctsp.qepoc.rest.Query;
import ch.upc.ctsp.qepoc.rest.impl.QueryImpl;
import ch.upc.ctsp.qepoc.rest.model.CallbackFuture;
import ch.upc.ctsp.qepoc.rest.model.PathDescription;
import ch.upc.ctsp.qepoc.rest.model.QueryRequest;
import ch.upc.ctsp.qepoc.rest.model.QueryResult;
import ch.upc.ctsp.qepoc.rest.rules.Alias;
import ch.upc.ctsp.qepoc.rest.spi.Backend;
import ch.upc.ctsp.qepoc.rest.spi.DirectResult;

/**
 * TODO: add type comment.
 * 
 */
public class PocMain {
    private static class SimpleMockBackend implements Backend {
        @Override
        public CallbackFuture<QueryResult> query(final QueryRequest request, final Map<String, String> parameters, final Query executingQuery) {
            final List<String> path = request.getPath();
            final List<String> remainingPath = path.subList(Integer.parseInt(parameters.get("match-length")), path.size());
            try {
                final String result = Client.query("/" + StringUtils.join(remainingPath, '/'));
                return new DirectResult<QueryResult>(new QueryResult(result.trim()));
            } catch (final Throwable e) {
                throw new RuntimeException("Cannot call " + remainingPath, e);
            }
        }
    }

    public static void main(final String args[]) throws IOException, InterruptedException, ExecutionException {
        final QueryImpl query = new QueryImpl();
        query.registerBackend(PathDescription.createFromString("mock"), new SimpleMockBackend());

        final Properties aliases = new Properties();
        aliases.load(ClassLoader.getSystemResourceAsStream("rules.properties"));
        for (final Entry<PathDescription, Alias> aliasEntry : Alias.parseProperties(aliases).entrySet()) {
            query.registerBackend(aliasEntry.getKey(), aliasEntry.getValue());
        }

        // query.registerBackend(PathDescription.createFromString("modem/{mac}/ip"), new Alias.Builder().addConstEntry("mock").addConstEntry("modem")
        // .addVariableEntry("mac").addConstEntry("ip").build());
        //
        // final Builder modemScopeBuilder = new Alias.Builder().addConstEntry("mock").addConstEntry("scopes");
        // modemScopeBuilder.createSubpath().addConstEntry("modem").addVariableEntry("mac").addConstEntry("ip");
        // query.registerBackend(PathDescription.createFromString("modem/{mac}/scope"), modemScopeBuilder.build());

        // final Builder cmtsNameBuilder = new Alias.Builder().addConstEntry("mock").addConstEntry("scopes");
        // cmtsNameBuilder.createSubpath().addConstEntry("modem").addVariableEntry("mac").addConstEntry("scope");
        // cmtsNameBuilder.addConstEntry("cmts");
        // query.registerBackend(PathDescription.createFromString("modem/{mac}/cmts/name"), cmtsNameBuilder.build());

        // final Builder cmIdBuilder = new Alias.Builder().addConstEntry("mock").addConstEntry("cmts");
        // cmIdBuilder.createSubpath().addConstEntry("modem").addVariableEntry("mac").addConstEntry("cmts").addConstEntry("name");
        // cmIdBuilder.addConstEntry("modem").addVariableEntry("mac").addConstEntry("cmId");
        // query.registerBackend(PathDescription.createFromString("modem/{mac}/cmts/cmId"), cmIdBuilder.build());

        // final Builder cmtsIpBuilder = new Alias.Builder().addConstEntry("mock").addConstEntry("cmts");
        // cmtsIpBuilder.createSubpath().addConstEntry("modem").addVariableEntry("mac").addConstEntry("cmts").addConstEntry("name");
        // cmtsIpBuilder.addConstEntry("ip");
        // query.registerBackend(PathDescription.createFromString("modem/{mac}/cmts/ip"), cmtsIpBuilder.build());
        //
        // final Builder upstreamIfcBuilder = new Alias.Builder().addConstEntry("mock").addConstEntry("snmp");
        // upstreamIfcBuilder.createSubpath().addConstEntry("modem").addVariableEntry("mac").addConstEntry("cmts").addConstEntry("ip");
        // upstreamIfcBuilder.createPatternEntry("1.3.6.2.1.2.3.4.{0}").createSubpath().addConstEntry("modem").addVariableEntry("mac")
        // .addConstEntry("cmts").addConstEntry("cmId");
        // query.registerBackend(PathDescription.createFromString("modem/{mac}/mainUpstreamIfc"), upstreamIfcBuilder.build());

        System.out.println(query.dump());

        final CallbackFuture<QueryResult> response = query.query(QueryRequest.createRequest("modem/00AB123456/mainUpstreamIfc"));
        System.out.println("------------");
        System.out.println(response.get().getValue());
    }
}
