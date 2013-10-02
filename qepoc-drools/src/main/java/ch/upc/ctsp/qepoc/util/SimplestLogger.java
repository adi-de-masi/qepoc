package ch.upc.ctsp.qepoc.util;

/**
 * Shortcut for System.out.println.
 * 
 * @author ademasi
 * 
 */
public class SimplestLogger {
	public static void log(String message, Object... arguments) {
		String m = String.format(message, arguments);
		System.out.println(m);
	}
}
