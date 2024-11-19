package scriptmanager.objects;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Object to store a script execution in the unit of a single CLI call.
 * 
 * @author Olivia Lang
 * @see scriptmanager.main.LogManagerWindow
 */
public class LogItem {

	private Timestamp start;
	private Timestamp stop = null;
	private String command;
	private int status = -1;	// -1=incomplete, 0=complete, 1=error

	/**
	 * Initialize LogItem right before execution (sets start time to current instantiation time)
	 * 
	 * @param cmd the string for cli execution (java -jar $SCRIPTMANAGER ...)
	 */
	public LogItem(String cmd) {
		start = new Timestamp(new Date().getTime());
		command = cmd;
	}

	/**
	 * Initialize LogItem post-execution
	 * 
	 * @param cmd
	 * @param st
	 * @param sp
	 * @param s
	 */
	public LogItem(String cmd, Timestamp st, Timestamp sp, int s) {
		command = cmd;
		start = st;
		stop = sp;
		status = s;
	}

	// Getters
	public Timestamp getStartTime() { return start; }
	public Timestamp getStopTime() { return stop; }
	public String getCommand() { return command; }
	public int getStatus() { return status; }
	
	public String getStatusString() {
		if (status == -1) {
			return("incomplete");
		} else if (status == 0) {
			return("complete");
		}
		return("error");
	}

	// Setters
	public void setStartTime(Timestamp s) { start = s; }
	public void setStopTime(Timestamp s) { stop = s; }
	public void changeCommand(String cmd) { command = cmd; }
	public void setStatus(int s) { status = s; }


	/**
	 * Format LogItem to primitive String array for JTable compatibility as a row item.
	 *
	 * @return
	 */
	public String[] toStringArray() {
		String[] row = new String[4];
		row[0] = getStatusString();
		row[1] = command.toString();
		row[2] = start.toString();
		row[3] = stop == null ? "N/A" : stop.toString();
		return(row);
	}

	// Comparators -- for JTable sorts
	// TODO: By start value
	// TODO: By status
}
