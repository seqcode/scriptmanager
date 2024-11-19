package scriptmanager.objects.Exceptions;

/**
 * Exception to track internally thrown exceptions
 * 
 * @author Olivia Lang
 */
@SuppressWarnings("serial")
public class ScriptManagerException extends Exception {

	public ScriptManagerException(String message) {
		super(message);
	}
}
