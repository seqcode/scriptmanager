package scriptmanager.objects.Exceptions;

/**
 * Custom exception used with LabelHeatmap and ThreeColorHeatMap tools
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Figure_Generation.LabelHeatMap
 * @see scriptmanager.scripts.Figure_Generation.ThreeColorHeatMap
 * @see scriptmanager.cli.Figure_Generation.LabelHeatMapCLI
 * @see scriptmanager.cli.Figure_Generation.ThreeColorHeatMapCLI
 * @see scriptmanager.window_interface.Figure_Generation.LabelHeatMapWindow
 * @see scriptmanager.window_interface.Figure_Generation.ThreeColorHeatMapWindow
 */
@SuppressWarnings("serial")
public class OptionException extends Exception {

	/**
	 * Creates a new OptionException with a given message
	 * 
	 * @param message Message to output
	 */
	public OptionException(String message) {
		super(message);
	}

}
