package ch.upc.ctsp.qepoc.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.upc.ctsp.qepoc.engine.Querist;

/**
 * Represents a generic query engine request.
 * 
 * @author ademasi
 * 
 */
public class QueryEngineRequest implements Querist {
	private String name;
	private String queryBlueprint;
	private String poller;

	private Map<String, String> arguments = new HashMap<String, String>();

	private Querist requester;

	/**
	 * Checks if intermediate results are necessary in order to fulfil this
	 * request.
	 * 
	 * @return true if there is at least one sub request left.
	 */
	public boolean hasSubRequest() {
		if (getNextMissingParameter() == null) {
			return false;
		}
		return true;
	}

	private String getNextMissingParameter() {
		int i = 0;
		String missing = findPlaceholder(queryBlueprint, i);
		while (missing != null) {
			if (arguments.get(removeBraces(missing)) == null) {
				return missing;
			}
			i = queryBlueprint.indexOf(missing) + missing.length();
			missing = findPlaceholder(queryBlueprint, i);
		}

		return null;
	}

	private String findPlaceholder(String query, int start) {
		Pattern pattern = Pattern.compile("(\\{\\w+\\})");
		Matcher matcher = pattern.matcher(queryBlueprint);
		if (matcher.find(start)) {
			String placeholder = matcher.group();
			return placeholder;
		}
		return null;
	}

	private String removeBraces(String placeholder) {
		return placeholder.replaceAll("\\{", "").replaceAll("\\}", "");
	}

	private String escapeBraces(String placeholder) {
		String result = placeholder;
		result = result.replaceAll("\\{", "\\\\{");
		result = result.replaceAll("\\}", "\\\\}");
		return result;
	}

	/**
	 * Gets the executable query from the blueprint.
	 * 
	 * @return
	 */
	public String getQuery() {
		String result = queryBlueprint;
		int i = 0;
		String placeholder = findPlaceholder(queryBlueprint, i);
		while (placeholder != null) {
			String substitution = arguments.get(removeBraces(placeholder));
			result = result.replaceAll(escapeBraces(placeholder), substitution);
			i = queryBlueprint.indexOf(placeholder) + placeholder.length();
			placeholder = findPlaceholder(queryBlueprint, i);
		}
		return result;
	}

	/**
	 * Gets the next subrequest.
	 * 
	 * @return The next subrequest for this query.
	 * @throws IllegalStateException
	 *             if there is no subRequest left in the stack.
	 */
	public QueryEngineRequest getNextSubRequest() throws IllegalStateException {
		QueryEngineRequest subrequest = new QueryEngineRequest();
		subrequest.setName(getNextMissingParameter());
		subrequest.setArguments(this.arguments);
		subrequest.setRequester(this);
		return subrequest;
	}

	public Set<String> getRequiredParameters() {
		Set<String> result = new HashSet<String>();
		int i = 0;
		String next = findPlaceholder(queryBlueprint, i);
		while (next != null) {
			result.add(next);
			i = queryBlueprint.indexOf(next) + next.length();
			next = findPlaceholder(queryBlueprint, i);
		}
		return result;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return removeBraces(name);
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the arguments
	 */
	public Map<String, String> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return the requester
	 */
	public Querist getRequester() {
		return requester;
	}

	/**
	 * @param requester
	 *            the requester to set
	 */
	public void setRequester(Querist requester) {
		this.requester = requester;
	}

	public void retrieveAnswer(String parameter, String result) {
		arguments.put(parameter, result);
		requester.retrieveAnswer(parameter, result);
	}

	/**
	 * @return the query
	 */
	public String getQueryBlueprint() {
		return queryBlueprint;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQueryBlueprint(String query) {
		this.queryBlueprint = query;
	}

	/**
	 * @return the poller
	 */
	public String getPoller() {
		return poller;
	}

	/**
	 * @param poller
	 *            the poller to set
	 */
	public void setPoller(String poller) {
		this.poller = poller;
	}

	/**
	 * Checks integrity of this request: Is there a poller and a query
	 * blueprint?
	 * 
	 * @throws IllegalStateException
	 *             If this request cannot be executed by any poller.
	 */
	public void validate() throws IllegalStateException {
		if (queryBlueprint == null || poller == null || requester == null) {
			throw new IllegalStateException(
					String.format(
							"Cannot execute query for %s, [queryblueprint = %s] [poller = %s] [requester = %s]",
							new Object[] { name, queryBlueprint, poller,
									requester }));
		}
	}

}
