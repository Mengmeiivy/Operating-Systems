import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Lab2 {
	
	static void swap(int i, int j, int[][] statusArray) {
		for (int k = 0; k < 4; k++) {
			int temp = statusArray[i][k];
			statusArray[i][k] = statusArray[j][k];
			statusArray[j][k] = temp;
		}
	}
	
	static int randomOS(int B, Scanner inputRandomNumbers) throws FileNotFoundException {
		int x = inputRandomNumbers.nextInt();
		//System.out.println("The random number is: " + x);
		return 1 + (x % B);
	}
	
	static void FCFS(int numberOfProcesses, int[][] statusArray, boolean verbose) throws FileNotFoundException {
		// simulating preparation 
		int time = 0;
		int cpuIdleTime = 0;
		int blockedTime = 0;
		int numberOfFinishedProcess = 0;
		int programFinishTime = 0;
		double averageTurnaroundTime = 0;
		double averageWaitingTime = 0;
		boolean done = false;
		boolean running = false;
		boolean blocked = false;
		int [] addingArray = new int[numberOfProcesses];
		String [][] stateArray = new String[numberOfProcesses][2];
		int indexOfAddingArray = 0;
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		Queue<Integer> readyQueueDuplicate;
		Scanner inputRandomNumbers = new Scanner (new FileReader("random-numbers"));
		
		// initialize the state array
		for (int i = 0; i < numberOfProcesses; i++)	{
			stateArray[i][0] = "unstarted";
			stateArray[i][1] = "0";
		}
		
		// print details if there is --verbose flag 
		if (verbose) {
			System.out.println();
			System.out.println("This detailed printout gives the state and remaining burst for each process:");
			System.out.println();
		}
		
		//time loop
		while (!done) {
			
			//System.out.println("Time: " + time);
			
			// reinitializes the addingArray and its index 
			for (int i = 0; i < numberOfProcesses; i++) {
				addingArray[i] = 0;
			}
			indexOfAddingArray = 0;
			
			// add new arrivals to the adding array
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][0] == time-1) {
					addingArray[indexOfAddingArray] = i;
					indexOfAddingArray++;
					stateArray[i][0] = "ready";
					stateArray[i][1] = "0";
				}
			}
			
			// add new unblocked processes to the adding array
			blocked = false;
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][4] != 0) {
					blocked = true;
					statusArray[i][4]--;
					stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
					if (statusArray[i][4] == 0) {
						addingArray[indexOfAddingArray] = i;
						indexOfAddingArray++;
						stateArray[i][0] = "ready";
						stateArray[i][1] = "0";
					}
				}
			}
			
			if (blocked) {
				blockedTime++;
				//System.out.println("blocked time is: " + blockedTime);
			}
			
			/*
			System.out.println("Adding array is now: ");
			for (int i = 0; i < indexOfAddingArray; i++) {
				System.out.println(addingArray[i]);
			}
			*/
			
			
			
			// sort the adding array
			for (int i = 0; i < indexOfAddingArray; i++) {
				for (int j = i+1; j < indexOfAddingArray; j++) {
					if (addingArray[i] > addingArray[j]) {
						int temp = addingArray[i];
						addingArray[i] = addingArray[j];
						addingArray[j] = temp;
					}
				}
			}
			
			/*
			System.out.println("Sorted adding array is now: ");
			for (int i = 0; i < indexOfAddingArray; i++) {
				System.out.println(addingArray[i]);
			}
			*/
			
			// adding the elements of the sorted adding array to the ready queue
			for (int i = 0; i < indexOfAddingArray; i++) {
				readyQueue.add(addingArray[i]);
			}
			
			
			// run processes 
			if (running) {
				for (int i = 0; i < numberOfProcesses; i++) {
					if (statusArray[i][6] != 0) {
						statusArray[i][6]--;
						statusArray[i][7]--;
						// System.out.println("remaining cpu time is " + statusArray[i][7]);
						// System.out.println("current burst is " + statusArray[i][6]);
						
						// if the remaining CPU of the process equals 0, the process terminates.
						if (statusArray[i][7] == 0) {
							numberOfFinishedProcess++;
							int finishTime = time - 1;
							statusArray[i][9] = finishTime;
							//System.out.println("Process " + i + " finishes at time " + finishTime);
							running = false;
							stateArray[i][0] = "terminated";
							stateArray[i][1] = "0";
						}
						
						// else if the process finishes current burst, it gets blocked. 
						else if (statusArray[i][6] == 0) {
							int currentBlockedTime = statusArray[i][5]*statusArray[i][3];
							statusArray[i][4] = currentBlockedTime;
							statusArray[i][11] += currentBlockedTime;
							running = false;
							stateArray[i][0] = "blocked";
							stateArray[i][1] = statusArray[i][4] + "";
						}
						
						else {
							stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
						}
						
					}
				}
			}
			
			if (!running) {
				if (!readyQueue.isEmpty()) {
					int runningIndex = readyQueue.remove();
					// System.out.println("running index is now: " + runningIndex);
					// generate current CPU burst, store it in index 5 and 6. 
					int currentCpuBurst = randomOS(statusArray[runningIndex][1], inputRandomNumbers);
					currentCpuBurst = Math.min(currentCpuBurst, statusArray[runningIndex][7]);
					statusArray[runningIndex][5] = currentCpuBurst;
					statusArray[runningIndex][6] = currentCpuBurst;
					stateArray[runningIndex][0] = "running";
					stateArray[runningIndex][1] = currentCpuBurst + "";
					running = true;
					
				}
				else {
					cpuIdleTime++;
				}
			}
			
			// update waiting time using a duplicate ready queue
			readyQueueDuplicate = readyQueue;
			Iterator<Integer> itr = readyQueueDuplicate.iterator();
			while (itr.hasNext()) {
				statusArray[itr.next()][10]++;
			}
			
			
			if (numberOfFinishedProcess == numberOfProcesses) {
				done = true;
				programFinishTime = time - 1;
				break;
			}

			// print details if there is --verbose flag 
			if (verbose) {
				System.out.print("Before cycle");
				System.out.printf("%5s", time);
				System.out.print(":");
				
				for (int i = 0; i < numberOfProcesses; i++) {
					System.out.printf("%12s", stateArray[i][0]);
					System.out.printf("%3s", stateArray[i][1]);
				}
				
				System.out.println(".");
			}
			
			time++;
			
		}
		
		
		// calculate average turnaround time and waiting time
		for (int i = 0; i < numberOfProcesses; i++) {
			averageTurnaroundTime += statusArray[i][9] - statusArray[i][0];
			averageWaitingTime += statusArray[i][10];
		}
		averageTurnaroundTime /= numberOfProcesses;
		averageWaitingTime /= numberOfProcesses;
		
		// print outcome for FCFS
		System.out.println();
		
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A,B,C,M) = (" + statusArray[i][0] + "," + statusArray[i][1] + "," + statusArray[i][2] + "," + statusArray[i][3] + ")");
			System.out.println("\tFinishing time: " + statusArray[i][9]);
			System.out.println("\tTurnaround time: " + (statusArray[i][9] - statusArray[i][0]));
			System.out.println("\tI/O time: " + statusArray[i][11]);
			System.out.println("\tWaiting time: " + statusArray[i][10]);
			System.out.println();
		}
		
		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + programFinishTime);
		System.out.println("\tCPU Utilization: " + String.format("%.6f", (double) (time - cpuIdleTime + 1) / programFinishTime));
		System.out.println("\tI/O Utilization: " + String.format("%.6f", (double) blockedTime /  programFinishTime));
		System.out.println("\tThroughput: " + String.format("%.6f", (double) numberOfProcesses / programFinishTime * 100) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + String.format("%.6f", averageTurnaroundTime));
		System.out.println("\tAverage waiting time: " + String.format("%.6f", averageWaitingTime));
	}

	static void RR(int numberOfProcesses, int[][] statusArray, boolean verbose) throws FileNotFoundException {
		// simulating preparation 
		int time = 0;
		int cpuIdleTime = 0;
		int blockedTime = 0;
		int numberOfFinishedProcess = 0;
		int programFinishTime = 0;
		double averageTurnaroundTime = 0;
		double averageWaitingTime = 0;
		boolean done = false;
		boolean running = false;
		boolean blocked = false;
		int [] addingArray = new int[numberOfProcesses];
		String [][] stateArray = new String[numberOfProcesses][2];
		int indexOfAddingArray = 0;
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		Queue<Integer> readyQueueDuplicate;
		Scanner inputRandomNumbers = new Scanner (new FileReader("random-numbers"));
		
		// initialize the state array
		for (int i = 0; i < numberOfProcesses; i++)	{
			stateArray[i][0] = "unstarted";
			stateArray[i][1] = "0";
		}
		
		// print details if there is --verbose flag 
		if (verbose) {
			System.out.println();
			System.out.println("This detailed printout gives the state and remaining burst for each process:");
			System.out.println();
		}
		
		//time loop
		while (!done) {
			
			//System.out.println("Time: " + time);
			
			// reinitializes the addingArray and its index 
			for (int i = 0; i < numberOfProcesses; i++) {
				addingArray[i] = 0;
			}
			
			indexOfAddingArray = 0;
			
			// add new arrivals to the adding array
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][0] == time-1) {
					addingArray[indexOfAddingArray] = i;
					indexOfAddingArray++;
					stateArray[i][0] = "ready";
					stateArray[i][1] = "0";
				}
			}
			
			// add new unblocked processes to the adding array
			blocked = false;
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][4] != 0) {
					blocked = true;
					statusArray[i][4]--;
					stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
					if (statusArray[i][4] == 0) {
						addingArray[indexOfAddingArray] = i;
						indexOfAddingArray++;
						stateArray[i][0] = "ready";
						stateArray[i][1] = "0";
					}
				}
			}
			
			if (blocked) {
				blockedTime++;
				//System.out.println("blocked time is: " + blockedTime);
			}
			
			/*
			System.out.println("Adding array is now: ");
			for (int i = 0; i < indexOfAddingArray; i++) {
				System.out.println(addingArray[i]);
			}
			*/
			
			
			// run processes 
			if (running) {
				for (int i = 0; i < numberOfProcesses; i++) {
					if (stateArray[i][0].equals("running")) {
						statusArray[i][6]--;
						statusArray[i][7]--;
						stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
						
						// System.out.println("remaining cpu time is " + statusArray[i][7]);
						// System.out.println("current burst is " + statusArray[i][6]);
						
						// if the remaining CPU of the process equals 0, the process terminates.
						if (statusArray[i][7] == 0) {
							numberOfFinishedProcess++;
							int finishTime = time - 1;
							statusArray[i][9] = finishTime;
							//System.out.println("Process " + i + " finishes at time " + finishTime);
							running = false;
							stateArray[i][0] = "terminated";
							stateArray[i][1] = "0";
						}
						
						// else if the process finishes current burst, it gets blocked. 
						else if (statusArray[i][6] == 0) {
							int currentBlockedTime = statusArray[i][5]*statusArray[i][3];
							statusArray[i][4] = currentBlockedTime;
							statusArray[i][11] += currentBlockedTime;
							running = false;
							stateArray[i][0] = "blocked";
							stateArray[i][1] = statusArray[i][4] + "";
						}
						
						else if (stateArray[i][1].equals("0")) {
							addingArray[indexOfAddingArray] = i;
							indexOfAddingArray++;
							running = false;
							stateArray[i][0] = "ready";
						}
						
					}
				}
			}
			
			
			// sort the adding array
			for (int i = 0; i < indexOfAddingArray; i++) {
				for (int j = i+1; j < indexOfAddingArray; j++) {
					if (addingArray[i] > addingArray[j]) {
						int temp = addingArray[i];
						addingArray[i] = addingArray[j];
						addingArray[j] = temp;
					}
				}
			}
			
			// adding the elements of the sorted adding array to the ready queue
			for (int i = 0; i < indexOfAddingArray; i++) {
				readyQueue.add(addingArray[i]);
			}
			
			if (!running) {
				if (!readyQueue.isEmpty()) {
					int runningIndex = readyQueue.remove();
					// System.out.println("running index is now: " + runningIndex);
					// generate current CPU burst, store it in index 5 and 6. 
					if (statusArray[runningIndex][6] == 0) {
						int currentCpuBurst = randomOS(statusArray[runningIndex][1], inputRandomNumbers);
						currentCpuBurst = Math.min(currentCpuBurst, statusArray[runningIndex][7]);
						statusArray[runningIndex][5] = currentCpuBurst;
						statusArray[runningIndex][6] = currentCpuBurst;
						stateArray[runningIndex][0] = "running";
						stateArray[runningIndex][1] = Math.min(currentCpuBurst, 2) + "";
					}
					else {
						stateArray[runningIndex][0] = "running";
						stateArray[runningIndex][1] = Math.min(2, statusArray[runningIndex][7]) + "";
					}
					running = true;
					
				}
				else {
					cpuIdleTime++;
				}
			}
			
			// update waiting time using a duplicate ready queue
			readyQueueDuplicate = readyQueue;
			Iterator<Integer> itr = readyQueueDuplicate.iterator();
			while (itr.hasNext()) {
				statusArray[itr.next()][10]++;
			}
			
			
			if (numberOfFinishedProcess == numberOfProcesses) {
				done = true;
				programFinishTime = time - 1;
				break;
			}

			// print details if there is --verbose flag 
			if (verbose) {
				System.out.print("Before cycle");
				System.out.printf("%5s", time);
				System.out.print(":");
				
				for (int i = 0; i < numberOfProcesses; i++) {
					System.out.printf("%12s", stateArray[i][0]);
					System.out.printf("%3s", stateArray[i][1]);
				}
				
				System.out.println(".");
			}
			
			time++;
			
		}
		
		
		// calculate average turnaround time and waiting time
		for (int i = 0; i < numberOfProcesses; i++) {
			averageTurnaroundTime += statusArray[i][9] - statusArray[i][0];
			averageWaitingTime += statusArray[i][10];
		}
		averageTurnaroundTime /= numberOfProcesses;
		averageWaitingTime /= numberOfProcesses;
		
		// print outcome for RR
		System.out.println();
		
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A,B,C,M) = (" + statusArray[i][0] + "," + statusArray[i][1] + "," + statusArray[i][2] + "," + statusArray[i][3] + ")");
			System.out.println("\tFinishing time: " + statusArray[i][9]);
			System.out.println("\tTurnaround time: " + (statusArray[i][9] - statusArray[i][0]));
			System.out.println("\tI/O time: " + statusArray[i][11]);
			System.out.println("\tWaiting time: " + statusArray[i][10]);
			System.out.println();
		}
		
		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + programFinishTime);
		System.out.println("\tCPU Utilization: " + String.format("%.6f", (double) (time - cpuIdleTime + 1) / programFinishTime));
		System.out.println("\tI/O Utilization: " + String.format("%.6f", (double) blockedTime /  programFinishTime));
		System.out.println("\tThroughput: " + String.format("%.6f", (double) numberOfProcesses / programFinishTime * 100) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + String.format("%.6f", averageTurnaroundTime));
		System.out.println("\tAverage waiting time: " + String.format("%.6f", averageWaitingTime));
	}
	
	static void Uniprogram(int numberOfProcesses, int[][] statusArray, boolean verbose) throws FileNotFoundException {
		// simulating preparation 
		int time = 0;
		int cpuIdleTime = 0;
		int blockedTime = 0;
		int numberOfFinishedProcess = 0;
		int programFinishTime = 0;
		int runningIndex = 0;
		double averageTurnaroundTime = 0;
		double averageWaitingTime = 0;
		boolean done = false;
		boolean complete = true;
		String [][] stateArray = new String[numberOfProcesses][2];
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		Queue<Integer> readyQueueDuplicate;
		Scanner inputRandomNumbers = new Scanner (new FileReader("random-numbers"));
		
		// initialize the state array
		for (int i = 0; i < numberOfProcesses; i++)	{
			stateArray[i][0] = "unstarted";
			stateArray[i][1] = "0";
		}
		
		// print details if there is --verbose flag 
		if (verbose) {
			System.out.println();
			System.out.println("This detailed printout gives the state and remaining burst for each process:");
			System.out.println();
		}
		
		//time loop
		while (!done) {
			
			//System.out.println("Time: " + time);
			
			
			// add new arrivals to the ready queue
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][0] == time-1) {
					readyQueue.add(i);
					stateArray[i][0] = "ready";
					stateArray[i][1] = "0";
				}
			}
			
			// 
		
			
			if (!complete) {
				if (stateArray[runningIndex][0].equals("blocked")) {
					statusArray[runningIndex][4]--;
					stateArray[runningIndex][1] = Integer.parseInt(stateArray[runningIndex][1]) - 1 + "";
					blockedTime++;
					cpuIdleTime++;
					
					if (statusArray[runningIndex][4] == 0) {
						int currentCpuBurst = randomOS(statusArray[runningIndex][1], inputRandomNumbers);
						currentCpuBurst = Math.min(currentCpuBurst, statusArray[runningIndex][7]);
						statusArray[runningIndex][5] = currentCpuBurst;
						statusArray[runningIndex][6] = currentCpuBurst;
						stateArray[runningIndex][0] = "running";
						stateArray[runningIndex][1] = currentCpuBurst + "";
					}
					
				}
				
				else if (stateArray[runningIndex][0].equals("running")) {
					statusArray[runningIndex][6]--;
					statusArray[runningIndex][7]--;
					
					if (statusArray[runningIndex][7] == 0) {
						numberOfFinishedProcess++;
						int finishTime = time - 1;
						statusArray[runningIndex][9] = finishTime;
						//System.out.println("Process " + i + " finishes at time " + finishTime);
						stateArray[runningIndex][0] = "terminated";
						stateArray[runningIndex][1] = "0";
						complete = true;
					}
					
					else if (statusArray[runningIndex][6] == 0) {
						int currentBlockedTime = statusArray[runningIndex][5]*statusArray[runningIndex][3];
						statusArray[runningIndex][4] = currentBlockedTime;
						statusArray[runningIndex][11] += currentBlockedTime;
						stateArray[runningIndex][0] = "blocked";
						stateArray[runningIndex][1] = statusArray[runningIndex][4] + "";
					}
					
					else {
						stateArray[runningIndex][1] = Integer.parseInt(stateArray[runningIndex][1]) - 1 + "";
					}
				}
			}
			
			if (complete) {
				if (!readyQueue.isEmpty()) {
					runningIndex = readyQueue.remove();
					int currentCpuBurst = randomOS(statusArray[runningIndex][1], inputRandomNumbers);
					currentCpuBurst = Math.min(currentCpuBurst, statusArray[runningIndex][7]);
					statusArray[runningIndex][5] = currentCpuBurst;
					statusArray[runningIndex][6] = currentCpuBurst;
					stateArray[runningIndex][0] = "running";
					stateArray[runningIndex][1] = currentCpuBurst + "";
					complete = false;
					
				}
				else {
					cpuIdleTime++;
				}
				
			}
			
			// update waiting time using a duplicate ready queue
			readyQueueDuplicate = readyQueue;
			Iterator<Integer> itr = readyQueueDuplicate.iterator();
			while (itr.hasNext()) {
				statusArray[itr.next()][10]++;
			}
			
			
			if (numberOfFinishedProcess == numberOfProcesses) {
				done = true;
				programFinishTime = time - 1;
				break;
			}

			// print details if there is --verbose flag 
			if (verbose) {
				System.out.print("Before cycle");
				System.out.printf("%5s", time);
				System.out.print(":");
				
				for (int i = 0; i < numberOfProcesses; i++) {
					System.out.printf("%12s", stateArray[i][0]);
					System.out.printf("%3s", stateArray[i][1]);
				}
				
				System.out.println(".");
			}
			
			time++;
			
		}
		
		
		// calculate average turnaround time and waiting time
		for (int i = 0; i < numberOfProcesses; i++) {
			averageTurnaroundTime += statusArray[i][9] - statusArray[i][0];
			averageWaitingTime += statusArray[i][10];
		}
		averageTurnaroundTime /= numberOfProcesses;
		averageWaitingTime /= numberOfProcesses;
		
		// print outcome for FCFS
		System.out.println();
		
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A,B,C,M) = (" + statusArray[i][0] + "," + statusArray[i][1] + "," + statusArray[i][2] + "," + statusArray[i][3] + ")");
			System.out.println("\tFinishing time: " + statusArray[i][9]);
			System.out.println("\tTurnaround time: " + (statusArray[i][9] - statusArray[i][0]));
			System.out.println("\tI/O time: " + statusArray[i][11]);
			System.out.println("\tWaiting time: " + statusArray[i][10]);
			System.out.println();
		}
		
		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + programFinishTime);
		System.out.println("\tCPU Utilization: " + String.format("%.6f", (double) (time - cpuIdleTime + 1) / programFinishTime));
		System.out.println("\tI/O Utilization: " + String.format("%.6f", (double) blockedTime /  programFinishTime));
		System.out.println("\tThroughput: " + String.format("%.6f", (double) numberOfProcesses / programFinishTime * 100) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + String.format("%.6f", averageTurnaroundTime));
		System.out.println("\tAverage waiting time: " + String.format("%.6f", averageWaitingTime));
	}

	static void SJF(int numberOfProcesses, int[][] statusArray, boolean verbose) throws FileNotFoundException {
		// simulating preparation 
		int time = 0;
		int cpuIdleTime = 0;
		int blockedTime = 0;
		int numberOfFinishedProcess = 0;
		int programFinishTime = 0;
		double averageTurnaroundTime = 0;
		double averageWaitingTime = 0;
		boolean done = false;
		boolean running = false;
		boolean blocked = false;
		String [][] stateArray = new String[numberOfProcesses][2];
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		Queue<Integer> readyQueueDuplicate;
		Scanner inputRandomNumbers = new Scanner (new FileReader("random-numbers"));
		
		// initialize the state array
		for (int i = 0; i < numberOfProcesses; i++)	{
			stateArray[i][0] = "unstarted";
			stateArray[i][1] = "0";
		}
		
		// print details if there is --verbose flag 
		if (verbose) {
			System.out.println();
			System.out.println("This detailed printout gives the state and remaining burst for each process:");
			System.out.println();
		}
		
		//time loop
		while (!done) {
			
			//System.out.println("Time: " + time);
			
			
			// updating new arrivals 
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][0] == time-1) {
					stateArray[i][0] = "ready";
					stateArray[i][1] = "0";
				}
			}
			
			// update new unblocked processes 
			blocked = false;
			for (int i = 0; i < numberOfProcesses; i++) {
				if (statusArray[i][4] != 0) {
					blocked = true;
					statusArray[i][4]--;
					stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
					if (statusArray[i][4] == 0) {
						stateArray[i][0] = "ready";
						stateArray[i][1] = "0";
					}
				}
			}
			
			if (blocked) {
				blockedTime++;
				//System.out.println("blocked time is: " + blockedTime);
			}
			
			
			// run processes 
			if (running) {
				for (int i = 0; i < numberOfProcesses; i++) {
					if (statusArray[i][6] != 0) {
						statusArray[i][6]--;
						statusArray[i][7]--;
						// System.out.println("remaining cpu time is " + statusArray[i][7]);
						// System.out.println("current burst is " + statusArray[i][6]);
						
						// if the remaining CPU of the process equals 0, the process terminates.
						if (statusArray[i][7] == 0) {
							numberOfFinishedProcess++;
							int finishTime = time - 1;
							statusArray[i][9] = finishTime;
							//System.out.println("Process " + i + " finishes at time " + finishTime);
							running = false;
							stateArray[i][0] = "terminated";
							stateArray[i][1] = "0";
						}
						
						// else if the process finishes current burst, it gets blocked. 
						else if (statusArray[i][6] == 0) {
							int currentBlockedTime = statusArray[i][5]*statusArray[i][3];
							statusArray[i][4] = currentBlockedTime;
							statusArray[i][11] += currentBlockedTime;
							running = false;
							stateArray[i][0] = "blocked";
							stateArray[i][1] = statusArray[i][4] + "";
						}
						
						else {
							stateArray[i][1] = Integer.parseInt(stateArray[i][1]) - 1 + "";
						}
						
					}
				}
			}
			
			if (!running) {
				// find the index of the shortest job in the ready status
				int runningIndex = 0;
				int shortestRemainingTime = 0;
				boolean first = true;
				boolean find = false;
				for (int i = 0; i < numberOfProcesses; i++) {
					if (stateArray[i][0].equals("ready")) {
						find = true;
						if (first) {
							shortestRemainingTime = statusArray[i][7];
							runningIndex = i;
							first = false;
							continue;
						}
						
						if (statusArray[i][7] < shortestRemainingTime) {
							shortestRemainingTime = statusArray[i][7];
							runningIndex = i;
						}
						
					}
				}
				
				if (find) {
					
					int currentCpuBurst = randomOS(statusArray[runningIndex][1], inputRandomNumbers);
					currentCpuBurst = Math.min(currentCpuBurst, statusArray[runningIndex][7]);
					statusArray[runningIndex][5] = currentCpuBurst;
					statusArray[runningIndex][6] = currentCpuBurst;
					stateArray[runningIndex][0] = "running";
					stateArray[runningIndex][1] = currentCpuBurst + "";
					running = true;
					
				}
				else {
					cpuIdleTime++;
				}
			}
			
			// update waiting time for all those who are still in ready state
			for (int i = 0; i < numberOfProcesses; i++) {
				if (stateArray[i][0].equals("ready")) {
					statusArray[i][10]++;
				}
			}
			
			// determine whether all the processes have finished
			if (numberOfFinishedProcess == numberOfProcesses) {
				done = true;
				programFinishTime = time - 1;
				break;
			}

			// print details if there is --verbose flag 
			if (verbose) {
				System.out.print("Before cycle");
				System.out.printf("%5s", time);
				System.out.print(":");
				
				for (int i = 0; i < numberOfProcesses; i++) {
					System.out.printf("%12s", stateArray[i][0]);
					System.out.printf("%3s", stateArray[i][1]);
				}
				
				System.out.println(".");
			}
			
			time++;
			
		}
		
		
		// calculate average turnaround time and waiting time
		for (int i = 0; i < numberOfProcesses; i++) {
			averageTurnaroundTime += statusArray[i][9] - statusArray[i][0];
			averageWaitingTime += statusArray[i][10];
		}
		averageTurnaroundTime /= numberOfProcesses;
		averageWaitingTime /= numberOfProcesses;
		
		// print outcome for FCFS
		System.out.println();
		
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.println("Process " + i + ":");
			System.out.println("\t(A,B,C,M) = (" + statusArray[i][0] + "," + statusArray[i][1] + "," + statusArray[i][2] + "," + statusArray[i][3] + ")");
			System.out.println("\tFinishing time: " + statusArray[i][9]);
			System.out.println("\tTurnaround time: " + (statusArray[i][9] - statusArray[i][0]));
			System.out.println("\tI/O time: " + statusArray[i][11]);
			System.out.println("\tWaiting time: " + statusArray[i][10]);
			System.out.println();
		}
		
		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + programFinishTime);
		System.out.println("\tCPU Utilization: " + String.format("%.6f", (double) (time - cpuIdleTime + 1) / programFinishTime));
		System.out.println("\tI/O Utilization: " + String.format("%.6f", (double) blockedTime /  programFinishTime));
		System.out.println("\tThroughput: " + String.format("%.6f", (double) numberOfProcesses / programFinishTime * 100) + " processes per hundred cycles");
		System.out.println("\tAverage turnaround time: " + String.format("%.6f", averageTurnaroundTime));
		System.out.println("\tAverage waiting time: " + String.format("%.6f", averageWaitingTime));
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// import file into Scanner input that contains only digits
		boolean verbose = false;
		Scanner input;

		if (args[0].equals("--verbose")) {
			verbose = true;
			File file = new File (args[1]);
			input = new Scanner(file).useDelimiter("\\D+");
		}
		else {
			File file = new File (args[0]);
			input = new Scanner(file).useDelimiter("\\D+");
		}
		
		// put digits into the status array
		int numberOfProcesses = input.nextInt();
		
		int [][] statusArray = new int[numberOfProcesses][12];
		for (int i = 0; i < numberOfProcesses; i++) {
			statusArray[i][0] = input.nextInt();
			statusArray[i][1] = input.nextInt();
			statusArray[i][2] = input.nextInt();
			statusArray[i][3] = input.nextInt();
		}
		
		// print original input
		System.out.print("The original input was: " + numberOfProcesses);
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.print(" (");
			System.out.print(" " + statusArray[i][0]);
			System.out.print(" " + statusArray[i][1]);
			System.out.print(" " + statusArray[i][2]);
			System.out.print(" " + statusArray[i][3]);
			System.out.print(" )");
		}
		System.out.println();
		
		// sort the status array
		for (int i = 0; i < numberOfProcesses; i++) {
			for (int j = i+1; j < numberOfProcesses; j++) {
				if (statusArray[i][0] > statusArray[j][0]) {
					swap (i, j, statusArray);
				}
			}
		}
		
		// print the sorted input
		System.out.print("The (sorted) input is:  " + numberOfProcesses);
		for (int i = 0; i < numberOfProcesses; i++) {
			System.out.print(" (");
			System.out.print(" " + statusArray[i][0]);
			System.out.print(" " + statusArray[i][1]);
			System.out.print(" " + statusArray[i][2]);
			System.out.print(" " + statusArray[i][3]);
			System.out.print(" )");
		}
		System.out.println();
		
		
		// initialize remaining CPU time for each process
		for (int i = 0; i < numberOfProcesses; i++) {
			statusArray[i][7] = statusArray[i][2];
		}
		
		// make duplicates of the status array 
		int [][] statusArrayDuplicate1 = new int[numberOfProcesses][12];
		int [][] statusArrayDuplicate2 = new int[numberOfProcesses][12];
		int [][] statusArrayDuplicate3 = new int[numberOfProcesses][12];
		int [][] statusArrayDuplicate4 = new int[numberOfProcesses][12];
		
		for (int i = 0; i < numberOfProcesses; i++) {
			for (int j = 0; j < 12; j++) {
				statusArrayDuplicate1[i][j] = statusArray[i][j];
				statusArrayDuplicate2[i][j] = statusArray[i][j];
				statusArrayDuplicate3[i][j] = statusArray[i][j];
				statusArrayDuplicate4[i][j] = statusArray[i][j];
			}
		}
		
		// FCFS algorithm
		System.out.println("\n");
		System.out.println("The scheduling algorithm used was First Come First Served.");
		FCFS(numberOfProcesses, statusArrayDuplicate1, verbose);
		
		// RR algorithm
		System.out.println("\n\n");
		System.out.println("The scheduling algorithm used was Round Robin.");
		System.out.println("The quantum used is 2.");
		RR(numberOfProcesses, statusArrayDuplicate2, verbose);
		
		// Uniprogram 
		System.out.println("\n\n");
		System.out.println("The scheduling algorithm used was Uniprocessing.");
		Uniprogram(numberOfProcesses, statusArrayDuplicate3, verbose);
		
		//SJF
		System.out.println("\n\n");
		System.out.println("The scheduling algorithm used was Shortest Job First.");
		SJF(numberOfProcesses, statusArrayDuplicate4, verbose);
		
		
		
		
		
	}
	
}
