package ch.upc.ctsp.qepoc.engine;

/**
 * Implementations are able to receive responses from the query engine in an
 * asynchronous way.
 * 
 * @author ademasi
 * 
 */
public interface Querist {

    /**
     * Callback method for the query engine, will be executed as soon as the
     * result of a request is retrieved.
     * 
     * @param parameter
     *            Name of the parameter, e.g.
     *            cmIdCpeCdxHitMyFaceAndMakeItMoreComplicated
     * @param result
     *            The value of the result.
     */
    public void retrieveAnswer(String parameter, String result);
}
