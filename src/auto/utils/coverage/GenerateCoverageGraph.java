package auto.utils.coverage;

import auto.utils.graph.FlowGenerator;

public class GenerateCoverageGraph {

	public static void main(String[] args) {
		
		String[] actColArgs = new String[1];
		ActivityCollator.main(actColArgs); // Generate coverage for automation with progress delta
		
		actColArgs[0] = "-a";
		ActivityCollator.main(actColArgs); // Generage coverage files for just automation
		
		FlowGenerator.main(new String[0]);
		
	}

}
