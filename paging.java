import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class mc4522_Lab4 {

	public static void main(String[] args) throws FileNotFoundException {
		
		//initialize the basic parameters 
		int memorySize, pageSize, processSize, jobMix, numberOfReferences;
		String replacementAlgorithm;
		memorySize = Integer.parseInt(args[0]);
		pageSize = Integer.parseInt(args[1]); 
		processSize = Integer.parseInt(args[2]);
		jobMix = Integer.parseInt(args[3]);
		numberOfReferences = Integer.parseInt(args[4]);
		replacementAlgorithm = args[5];
		Scanner inputRandomNumbers = new Scanner (new FileReader("random-numbers"));
		
		//output the basic parameters
		System.out.println("The machine size is " + memorySize + ".\nThe page size is " + pageSize +
				".\nThe process size is " + processSize + ".\nThe job mix number is " + jobMix +
				".\nThe number of references per process is " + numberOfReferences + ".\nThe replacement algorithm is " +
				replacementAlgorithm + ".\n");
				
		// assign values to number of processes
		int numberOfProcesses;
		if (jobMix == 1) {
			numberOfProcesses = 1;
		}
		else {
			numberOfProcesses = 4;
		}
		
		//declare the output arrays
		int [] numberOfFaults = new int [numberOfProcesses];
		int [] numberOfEvictions = new int [numberOfProcesses];
		double [] totalResidencyTime = new double [numberOfProcesses];
		
		
		// assign values to the initial reference for each process
		int [] currentReference = new int [numberOfProcesses];
		for (int i = 0; i < numberOfProcesses; i++) {
			currentReference[i] = (111*(i+1)) % processSize;
		}
		
		// assign probabilities of reference patterns to each process
		double [][] referenceWeights = new double [numberOfProcesses][4];
		if (jobMix == 1) {
			referenceWeights[0][0] = 1;
			referenceWeights[0][1] = 0;
			referenceWeights[0][2] = 0;
			referenceWeights[0][3] = 0;
		}
		if (jobMix == 2) {
			for (int k = 0; k < numberOfProcesses; k++) {
				referenceWeights[k][0] = 1;
				referenceWeights[k][1] = 0;
				referenceWeights[k][2] = 0;
				referenceWeights[k][3] = 0;
			}
		}
		if (jobMix == 3) {
			for (int k = 0; k < numberOfProcesses; k++) {
				referenceWeights[k][0] = 0;
				referenceWeights[k][1] = 0;
				referenceWeights[k][2] = 0;
				referenceWeights[k][3] = 1;
			}
		}
		if (jobMix == 4) {
			referenceWeights[0][0] = 0.75;
			referenceWeights[0][1] = 0.25;
			referenceWeights[0][2] = 0;
			referenceWeights[0][3] = 0;
			
			referenceWeights[1][0] = 0.75;
			referenceWeights[1][1] = 0;
			referenceWeights[1][2] = 0.25;
			referenceWeights[1][3] = 0;
			
			referenceWeights[2][0] = 0.75;
			referenceWeights[2][1] = 0.125;
			referenceWeights[2][2] = 0.125;
			referenceWeights[2][3] = 0;
			
			referenceWeights[3][0] = 0.5;
			referenceWeights[3][1] = 0.125;
			referenceWeights[3][2] = 0.125;
			referenceWeights[3][3] = 0.25;
		}
		
		// build the frame table for simulation 
		int numberOfFrames = memorySize / pageSize;
		int [][] frameTable = new int[numberOfFrames][4];
		for (int i = 0; i < numberOfFrames; i++) {
			frameTable[i][0] = -1;
		}
				
		
		int currentNumberOfReferences = 0;
		boolean done = false;
		int time = 0;
		int iteration = 3;
		
		//keeps generating and simulating references till the number of references is reached
		while (!done) {
			//round robin scheduling with quantum = 3
			for (int i = 0; i < numberOfProcesses; i++) {
				for (int j = 0; j < iteration; j++) {
					time++;
					//simulate the current reference
					int pageNumber = currentReference[i] / pageSize;
					//check if there is pageFault
					boolean pageFault = true;
					for (int k = 0; k < numberOfFrames; k++) {
						if (frameTable[k][0] == i && frameTable[k][1] == pageNumber) {
							frameTable[k][3] = time;
							pageFault = false;
							break;
						}
					}
					//if there is page fault, check if there is a free frame
					if (pageFault == true) {
						numberOfFaults[i]++;
						boolean freeFrame = false;
						for (int u = numberOfFrames - 1; u > -1; u--) {
							if (frameTable[u][0] == -1) {
								frameTable[u][0] = i;
								frameTable[u][1] = pageNumber;
								frameTable[u][2] = time;
								frameTable[u][3] = time;
								freeFrame = true;
								break;
							}
						}
						//if no free frame is found, use the replacement algorithm 
						int victimFrame = 0;
						if (freeFrame == false) {
							//LIFO algorithm chooses the page that is the newest in the frame
							if (replacementAlgorithm.equals("lifo")) {
								int maxTimeLoaded = 0;
								for (int v = 0; v < numberOfFrames; v++) {
									if (frameTable[v][2] > maxTimeLoaded) {
										maxTimeLoaded = frameTable[v][2];
										victimFrame = v;
									}
								}
							}
							//Random algorithm randomly chooses a page in the frame
							if (replacementAlgorithm.equals("random")) {
								victimFrame = inputRandomNumbers.nextInt() % numberOfFrames;
							}
							//LRU algorithm chooses the page that is least recently used in the frame
							if (replacementAlgorithm.equals("lru")) {
								int minTimeReferenced = frameTable[0][3];
								for (int m = 1; m < numberOfFrames; m++) {
									if (frameTable[m][3] < minTimeReferenced) {
										minTimeReferenced = frameTable[m][3];
										victimFrame = m;
									}
								}
							}
							
							//evict the victim page and replace it with the current page
							int victimProcess = frameTable[victimFrame][0];
							numberOfEvictions[victimProcess]++;
							totalResidencyTime[victimProcess] += time - frameTable[victimFrame][2];
							
							frameTable[victimFrame][0] = i;
							frameTable[victimFrame][1] = pageNumber;
							frameTable[victimFrame][2] = time;
							frameTable[victimFrame][3] = time;
							
						}
					}
						
					
					
					//calculate the next reference
					int randomNumber = inputRandomNumbers.nextInt();
					double randomDistribution = randomNumber / (Integer.MAX_VALUE +1d);
					if (randomDistribution < referenceWeights[i][0]) {
						currentReference[i] = (currentReference[i] + 1) % processSize;
					}
					else if (randomDistribution < referenceWeights[i][0] + referenceWeights[i][1]) {
						currentReference[i] = (currentReference[i] - 5 + processSize) % processSize;
					}
					else if (randomDistribution < referenceWeights[i][0] + referenceWeights[i][1] 
							+ referenceWeights[i][2]) {
						currentReference[i] = (currentReference[i] + 4) % processSize;
					}
					else {
						currentReference[i] = inputRandomNumbers.nextInt() % processSize;
					}
					
					//end the loop if the number of references is reached
					if (i == 0) {
						currentNumberOfReferences++;
						if (currentNumberOfReferences == numberOfReferences) {
							done = true;
							iteration = j + 1;
						}
					}
					
				}
			}
			
		}
	
		
		//print output
		int totalFaults = 0;
		int overallEvictions = 0;
		double overallResidency = 0;
		
		//print output for each process
		for (int i = 0; i < numberOfProcesses; i++) {
			totalFaults += numberOfFaults[i];
			overallEvictions += numberOfEvictions[i];
			overallResidency += totalResidencyTime[i];
			if (numberOfEvictions[i] != 0) {
				System.out.println("Process " + (i+1) + " had " + numberOfFaults[i] + " faults and "
						+ totalResidencyTime[i] / numberOfEvictions[i] + " average residency.");
			}
			else {
				System.out.println("Process " + (i+1) + " had " + numberOfFaults[i] + " faults. With no evictions, "
						+ "the average residency is undefined.");
			}
		}
		//print the overall output
		if (overallEvictions != 0) {
			System.out.println("\nThe total number of faults is " + totalFaults + " and the overall average residency is " + overallResidency / overallEvictions + ".");
		}
		else {
			System.out.println("\nThe total number of faults is " + totalFaults + ". With no evictions, the overall average "
					+ "residency is undefined.");
		}
	}

}
