import java.util.Scanner;

public class Linker {
	public static void main (String[] args) {
		
		int lineNumber = 0;
		int baseAddress = 0;
		int number = 0;
		String [][] finalAddress = new String [300][3];
		String	[][] absoluteAddress = new String [300][4];
		int absoluteAddressIndex = 0;
		String [][] useAbsoluteAddress = new String [300][300];
		int useAbsoluteAddressIndex = -1;
		
		Scanner input = new Scanner (System.in);
		
		while (input.hasNext()) {
			lineNumber ++;
			if (lineNumber > 300) {
				System.out.println("Too many lines.");
				break;
			}
			number = input.nextInt();
			
			// For definition 
			if (lineNumber % 3 == 1) {
				for (int i = 0; i < number; i++) {
					String symbol = input.next();
					int symbolInteger = input.nextInt();
					
					boolean found = false;
					
					for (int j = 0; j < absoluteAddressIndex; j++) {
						if (absoluteAddress[j][0].equals(symbol)) {
							absoluteAddress[j][1] = Integer.toString(symbolInteger + baseAddress) + " Error: This variable is multiply defined; last value used.";
							absoluteAddress[j][2] = Integer.toString(lineNumber/3);
							found = true;
							break;
						}
					}
					if (!found) {
						absoluteAddress[absoluteAddressIndex][0] = symbol;
						absoluteAddress[absoluteAddressIndex][1] = Integer.toString(symbolInteger + baseAddress);
						absoluteAddress[absoluteAddressIndex][2] = Integer.toString(lineNumber/3);
						absoluteAddressIndex++;
					}
				}
			}
			
			// For use
			if (lineNumber % 3 == 2) {
				for (int i = 0; i < number; i++) {
					String useSymbol = input.next();
					boolean find = false;
					for (int j = 0; j < useAbsoluteAddressIndex + 1; j++) {
						if (useAbsoluteAddress [j][0].equals(useSymbol)) {
							int k = 0;
							while (useAbsoluteAddress[j][k] != null)
								k++;
							while (true) {
								int u = input.nextInt();
								if (u == -1) 
									break;
								int v = u + baseAddress;
								useAbsoluteAddress[j][k] = Integer.toString(v);
								k++;
							}
							find = true;
							break;
						}
					}
					if (find == false) {
						useAbsoluteAddressIndex++;
						useAbsoluteAddress[useAbsoluteAddressIndex][0] = useSymbol;
						int k = 1;
						while (true) {
							int u = input.nextInt();
							if (u == -1)
								break;
							int v = u + baseAddress;
							useAbsoluteAddress[useAbsoluteAddressIndex][k] = Integer.toString(v);
							k++;
						}
					}
					
				}
			}
			
			// For words
			if (lineNumber % 3 == 0) {
				
				for (int p = 0; p < absoluteAddressIndex; p++) {
					if (absoluteAddress[p][2].equals(Integer.toString((lineNumber-1)/3))) {
						absoluteAddress[p][3] = Integer.toString(number + baseAddress);
					}
				}
				
				for (int q = 0; q < useAbsoluteAddressIndex + 1; q++) {
					int r = 1;
					while (true) {
						if (useAbsoluteAddress[q][r] == null)
							break;
						if (Integer.parseInt(useAbsoluteAddress[q][r]) >= baseAddress + number) {
							useAbsoluteAddress[q][r] = Integer.toString(-1);
						}
						r++;
					}
				}
				
				for (int i = 0; i < number; i++) {
					int a = input.nextInt();
					int b = a % 10;
					
					switch (b) {
					case 1:
						finalAddress[baseAddress + i][0] = Integer.toString(a/10);
						break;
					case 2: 
						int c = a/10;
						int d = c/1000;
						int e = c - d*1000;
						if (e > 299) 
							finalAddress[baseAddress + i][0] = Integer.toString(d*1000+299) + " Error: Absolute address exceeds machine size; largest address used.";
						else 
							finalAddress[baseAddress + i][0] = Integer.toString(a/10);
						break;
					case 3:
						int f = a/10;
						int g = f/1000;
						int h = f - g*1000;
						if (h >= number) {
							finalAddress[baseAddress + i][0] = Integer.toString(g*1000 + number -1 + baseAddress) + " Error: Relative address exceeds module size; largest module address used.";
						}
						else 
							finalAddress[baseAddress + i][0] = Integer.toString(a/10 + baseAddress);
						break;
					case 4: 
						finalAddress[baseAddress + i][0] = Integer.toString((a/10000) * 1000);
						break;
					}
				}
				baseAddress += number;
			}
			
		}
		
		// update the absoluteAddressArray
		for (int i = 0; i < absoluteAddressIndex; i++) {
			if (Integer.parseInt(absoluteAddress[i][1].replaceAll("[^0-9]", "")) >= Integer.parseInt(absoluteAddress[i][3])) {
				absoluteAddress[i][1] = Integer.toString((Integer.parseInt(absoluteAddress[i][3])-1)) + " Error: Definition exceeds module size; last word in module used.";
			}
		}
		
		// update the finalAddress array
		for (int z = useAbsoluteAddressIndex; z > -1; z--) {
			boolean find2 = false;
			for (int y = 0; y < absoluteAddressIndex; y++) {
				if (absoluteAddress[y][0].equals(useAbsoluteAddress[z][0])) {
					absoluteAddress[y][2] = "used";
					int c = 1;
					while (true) {
						if (useAbsoluteAddress[z][c] == null)
							break;
						if (!useAbsoluteAddress[z][c].equals("-1")) {
							if (finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][1] == null) {
								int d = Integer.parseInt(finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][0]);
								int e = d + Integer.parseInt((absoluteAddress[y][1].replaceAll("[^0-9]", "")));
								finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][0] = Integer.toString(e);
								finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][1] = "updated";
							}
							else {
								if (finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][2] == null) {
									finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][2] = "error";
								}
							}
						}
						c++;
					}
					find2 = true;
					break;
				}
			}
			if (find2 == false) {
				int c = 1;
				while (true) {
					if (useAbsoluteAddress[z][c] == null)
						break;
					if (finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][1] == null) {
						int d = Integer.parseInt(finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][0]);
						int e = d + 111;
						finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][0] = Integer.toString(e) + " Error: " + 
						useAbsoluteAddress[z][0] + " is not defined; 111 used.";
						finalAddress[Integer.parseInt(useAbsoluteAddress[z][c])][1] = "updated";
					}
					c++;
				}
			}
		}
		
		// print the output 
		System.out.println("Symbol Table");
		for (int i = 0; i < absoluteAddressIndex; i++) {
			System.out.println(absoluteAddress[i][0] + "=" + absoluteAddress[i][1]);
		}
		System.out.println("\nMemory Map");
		for (int j = 0; j < baseAddress; j++) {
			if (finalAddress[j][2] != null) 
				System.out.println(j + ":\t" + finalAddress[j][0] + " Error: Multiple variables used in instruction; all but last ignored.");
			else
				System.out.println(j + ":\t" + finalAddress[j][0]);
		}

		System.out.println("\n");
		
		// print warning: defined but never used
		for (int y = 0; y < absoluteAddressIndex; y++) {
			if (absoluteAddress[y][2].equals("used") == false)
				System.out.println("Warning: " + absoluteAddress[y][0] + " was defined in module " + absoluteAddress[y][2] + " but never used.");
		}
		
	}
}
