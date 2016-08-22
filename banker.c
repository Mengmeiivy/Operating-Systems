//
//  main.c
//  C
//
//  Created by Mengmei Chen on 11/11/15.
//  Copyright (c) 2015 Mengmei Chen. All rights reserved.
//


#include <stdio.h>
#include <string.h>

int numOfResource;
int numOfTask;

// The safe method for Banker's algorithm.
// It checks whether the state is safe by checking if all the tasks can be finished with the remaining resources, assuming that each task will ask for the maximum amount of resources they are allowed.
// If the state is safe, return 1. Otherwise, return 0.
int safe(int numOfRemaining, int statusArray[numOfTask], int resourcesArrayCopy [numOfTask], int claimsCopy[numOfTask][numOfResource], int taskResourcesCopy[numOfTask][numOfResource]) {
    
    int find;
    
    // while there is still some task unfinished, keep looking for task to finish
    while (numOfRemaining != 0) {
        
        find = 0;
        
        // check if at least one task can be found to be finished in this round
        for (int i = 0; i < numOfTask; i++) {
            if (statusArray[i] == 0)
                continue;
            int finish = 1;
            for (int j = 0; j < numOfResource; j++) {
                if (claimsCopy[i][j] > resourcesArrayCopy[j]) {
                    finish = 0;
                }
            }
            // if the claims of the resources do not exceed the available resources, finish the current task
            if (finish == 1) {
                numOfRemaining--;
                find = 1;
                statusArray[i] = 0;
                
                for (int k = 0; k < numOfResource; k++) {
                    resourcesArrayCopy[k] += taskResourcesCopy[i][k];
                }
                
                break;
            }
        }
        
        // if none of the process can be finished with the available resources, the state is unsafe. Return 0.
        if (find == 0) {
            return 0;
        }

    }
    
    
    return 1;
}


// FIFO method
// The optimistic resource manager that satisfy request on a first-come-first-served basis as long as the requests do not exceed the remaining resources
void FIFO (int actions[400][numOfTask][3], int resourcesArray[2][numOfResource]) {
    
    // initiate the array for total time and total waiting time for each task
    int times [numOfTask][2];
    
    int waitingArray[100];
    int waitingArrayIndex = 0;
    
    // initiate the array for the number of resources each task holds
    int taskResources [numOfTask][numOfResource];
    
    // initiate taskResoures
    for (int p = 0; p < numOfTask; p++) {
        for (int q = 0; q < numOfResource; q++) {
            taskResources[p][q] = 0;
        }
    }

    // initiate times
    for (int j = 0; j < numOfTask; j++) {
        times[j][0] = numOfResource;
        times[j][1] = 0;
    }
    
    int numOfTerminated = 0;
    int index;
    int deadlocked = 0;
    int unsatisfiedRequest;
    int numOfAborted = 0;
    
    for (int h = 0; h < numOfTask; h++) {
        actions[0][h][0] = 2;
        actions[1][h][0] = 0; // mark it running in the beginning
    }
    
    // keep going through the cycles as long as not all tasks have been finished
    while ((numOfTerminated + numOfAborted) != numOfTask) {
        
        unsatisfiedRequest = 0;
        
        // update the available resouces from last cycle
        for (int z = 0; z < numOfResource; z++) {
            resourcesArray[0][z] += resourcesArray[1][z];
            resourcesArray[1][z] = 0;
        }
        
        // check the blocked tasks in the waiting list first
        for (int c = 0; c < waitingArrayIndex; c++) {
            if (waitingArray[c] != -1)  {
                times[waitingArray[c]][0]++;
                index = actions[0][waitingArray[c]][0];
                
                // if there is enough resources to grant the task, grant it and unblock it for the next cycle
                if (resourcesArray[0][actions[index][waitingArray[c]][1]] >= actions[index][waitingArray[c]][2]) {
                    
                    resourcesArray[0][actions[index][waitingArray[c]][1]] -= actions[index][waitingArray[c]][2];
                    taskResources[waitingArray[c]][actions[index][waitingArray[c]][1]] += actions[index][waitingArray[c]][2];
                    actions[0][waitingArray[c]][0]++;
                    actions[1][waitingArray[c]][0] = 2; //unblock later
                    waitingArray[c] = -1;
                    
                }
                // if there is not enough resources, keep the task blocked in the waiting list
                else {
                    times[waitingArray[c]][1]++;
                    unsatisfiedRequest++;
                }
                
            }
        }
        
        
        
        // deal with the remaining running tasks
        for (int u = 0; u < numOfTask; u++) {
            
            if (actions[1][u][0] != 0) // if blocked, terminated or aborted in this cycle, break the loop
                continue;
            
            index = actions[0][u][0];
            
            // if the task requests resources, check if they can be granted
            if (actions[index][u][0] == 1) {
                
                times[u][0]++;
                if (resourcesArray[0][actions[index][u][1]] >= actions[index][u][2]) {
                    resourcesArray[0][actions[index][u][1]] -= actions[index][u][2];
                    actions[0][u][0]++;
                    taskResources[u][actions[index][u][1]] += actions[index][u][2];
                }
                else {
                    times[u][1]++;
                    actions[1][u][0] = 1;
                    waitingArray[waitingArrayIndex] = u;
                    waitingArrayIndex++;
                    unsatisfiedRequest++;
                }
            }
            
            // if the task computes, decrease its computing time till it reaches 0
            else if (actions[index][u][0] == 2) {
                times[u][0]++;
                actions[index][u][1]--;
                if (actions[index][u][1] == 0)
                    actions[0][u][0]++;
            }
            
            // if the task releases resources, add the resources to the total resources available
            else if (actions[index][u][0] == 3) {
                times[u][0]++;
                resourcesArray[1][actions[index][u][1]] = actions[index][u][2];
                taskResources[u][actions[index][u][1]] -= actions[index][u][2];
                actions[0][u][0]++;
            }
            
            // task terminates
            else if (actions[index][u][0] == 4) {
                actions[1][u][0] = 3;
                numOfTerminated++;
            }
            ;
            
        }
        
        // unblock the tasks for next cycle
        for (int d = 0; d < numOfTask; d++) {
            if (actions[1][d][0] == 2) {
                actions[1][d][0] = 0;
            }
        }
        
        // abort tasks in order if deadlock occurs
        if ((unsatisfiedRequest == numOfTask - numOfTerminated - numOfAborted) && ((numOfTerminated+numOfAborted) != numOfTask)) {
            deadlocked = 1;
            while (deadlocked == 1) {
                //abort the lowest numbered task that is blocked
                for (int w = 0; w < numOfTask; w++) {

                    if (actions[1][w][0] == 1) {
                        actions[1][w][0] = 4;
                        for (int v = 0; v < numOfResource; v++) {
                            resourcesArray[0][v] += taskResources[w][v];
                        }
                        for (int g = 0; g < waitingArrayIndex; g++) {
                            if (waitingArray[g] == w) {
                                waitingArray[g] = -1;
                            }
                        }
                        numOfAborted++;
                        printf("%s %d\n", "Deadlock occured. Abort task:", w+1);
                        break;
                    }
                }
                
                // test if deadlock still exists
                for (int d = 0; d < numOfTask; d++) {
                    
                    if (actions[1][d][0] != 1)
                        continue;
                    
                    index = actions[0][d][0];
                    if (resourcesArray[0][actions[index][d][1]] >= actions[index][d][2]) {
                        deadlocked = 0;
                    }
                    
                }
                
            }
        }
        
        
    }
    
    
    // print output for FIFO
    int totalTime = 0;
    int totalWaitingTime = 0;
    printf("FIFO output:\n");
    for (int m = 0; m < numOfTask; m++) {
        
        int n = m+1;
        if (actions[1][m][0] == 3) {
            printf("%s %d\t\t%d\t%d\t%d%s\n", "Task", n, times[m][0], times[m][1], (int)(((times[m][1]*100.0)/times[m][0])+0.5), "%");
            totalTime += times[m][0];
            totalWaitingTime += times[m][1];
        }
        
        if (actions[1][m][0] == 4) {
            printf("%s %d\t\t%s\n", "Task", n, "aborted");
        }
    }
    printf("%s %s\t\t%d\t%d\t%d%s\n\n", "Total", " ", totalTime, totalWaitingTime, (int)(((totalWaitingTime*100.0)/totalTime)+0.5), "%");
}


// Banker method
// The banker's algorithm satisfies a request only when the resulting state is safe to finish all processes in the worst cast scenario.

void Banker (int claims [numOfTask][numOfResource], int actions[400][numOfTask][3], int resourcesArray[2][numOfResource]) {
    
    // initiate the array for total time and total waiting time for each task
    int times [numOfTask][2];
    
    int waitingArray[100];
    int waitingArrayIndex = 0;
    
    // initiate the array for the number of resources each task holds
    int taskResources [numOfTask][numOfResource];
    
    // initiate taskResoures
    for (int p = 0; p < numOfTask; p++) {
        for (int q = 0; q < numOfResource; q++) {
            taskResources[p][q] = 0;
        }
    }
    
    // initiate times
    for (int j = 0; j < numOfTask; j++) {
        times[j][0] = numOfResource;
        times[j][1] = 0;
    }
    
    int numOfTerminated = 0;
    int index;
    int numOfAborted = 0;
    int numOfRemaining;
    
    int statusArray[numOfTask];
    
    for (int h = 0; h < numOfTask; h++) {
        actions[0][h][0] = 2;
        actions[1][h][0] = 0; // mark it as running in the beginning
    }
    
    // check initial claims. If initial claims exceed available resources, abort the tasks.
    for (int e = 0; e < numOfTask; e++) {
        for (int f = 0; f < numOfResource; f++) {
            if (claims[e][f] > resourcesArray[0][f]) {
                //abort the task
                actions[1][e][0] = 4;
                numOfAborted++;
                printf("%s %d %s %d %s%d%s%d%s\n", "Banker aborts task", e+1, "before run begins: claim for resource", f+1, "(", claims[e][f], ") exceeds number of units present (", resourcesArray[0][f], ")");
                break;
            }
        }
    }
    
    // while there is still unfinished tasks, keep going through the cycles
    while ((numOfTerminated + numOfAborted) != numOfTask) {
        
        // update the available resouces from last cycle
        for (int z = 0; z < numOfResource; z++) {
            resourcesArray[0][z] += resourcesArray[1][z];
            resourcesArray[1][z] = 0;
        }
        
        // check the blocked tasks first
        for (int c = 0; c < waitingArrayIndex; c++) {
            if (waitingArray[c] != -1)  {
                times[waitingArray[c]][0]++;
                
                index = actions[0][waitingArray[c]][0];
                
                // prepare for passing values to safe()
                numOfRemaining = 0;
                for (int g = 0; g < numOfTask; g++)  {
                    if (actions[1][g][0] == 3 || actions[1][g][0] == 4) {
                        statusArray[g] = 0;
                    }
                    else {
                        statusArray[g] = 1;
                        numOfRemaining++;
                    }
                }
                
                int resourcesArrayCopy[numOfResource];
                for (int h = 0; h < numOfResource; h++) {
                    resourcesArrayCopy[h] = resourcesArray[0][h];
                }
                resourcesArrayCopy[actions[index][waitingArray[c]][1]] -= actions[index][waitingArray[c]][2];
                
                int good = 1;
                if (resourcesArrayCopy[actions[index][waitingArray[c]][1]] < 0) {
                    good = 0;
                }
                
                int claimsCopy[numOfTask][numOfResource];
                for (int y = 0; y < numOfTask; y++) {
                    for (int z = 0; z < numOfResource; z++) {
                        claimsCopy[y][z] = claims[y][z];
                    }
                }
                claimsCopy[waitingArray[c]][actions[index][waitingArray[c]][1]] -= actions[index][waitingArray[c]][2];
                
                
                int taskResourcesCopy [numOfTask][numOfResource];
                for (int r = 0; r < numOfTask; r++) {
                    for (int q = 0; q < numOfResource; q++) {
                        taskResourcesCopy[r][q] = taskResources[r][q];
                    }
                }
                taskResourcesCopy[waitingArray[c]][actions[index][waitingArray[c]][1]] += actions[index][waitingArray[c]][2];
                
                
                // if resources do not go under 0 and the state is safe, grant the resource
                if ((good == 1) && (safe(numOfRemaining, statusArray, resourcesArrayCopy, claimsCopy, taskResourcesCopy) == 1)) {

                    claims[waitingArray[c]][actions[index][waitingArray[c]][1]] -= actions[index][waitingArray[c]][2];
                    resourcesArray[0][actions[index][waitingArray[c]][1]] -= actions[index][waitingArray[c]][2];
                    taskResources[waitingArray[c]][actions[index][waitingArray[c]][1]] += actions[index][waitingArray[c]][2];
                    actions[0][waitingArray[c]][0]++;
                    actions[1][waitingArray[c]][0] = 2; //unblock later
                    waitingArray[c] = -1;
                    
                }
                // otherwise, keep the task blocked in the waiting list
                else {
                    times[waitingArray[c]][1]++;
                }
                
            }
        }
        
        
        
        // deal with the unblocked tasks
        for (int u = 0; u < numOfTask; u++) {
            
            if (actions[1][u][0] != 0) // if blocked, terminated or aborted in this cycle, break the loop
                continue;
            
            index = actions[0][u][0];
            
            // if a task requests resources, check if it exceeds its claims and if the state is safe
            if (actions[index][u][0] == 1) {
                
                times[u][0]++;
                
                if (claims[u][actions[index][u][1]] < actions[index][u][2]) {
                    //abort the task
                    actions[1][u][0] = 4;
                    numOfAborted++;
                    for (int w = 0; w < numOfResource; w++) {
                        resourcesArray[1][w] += taskResources[u][w];
                    }
                    printf("%s %d%s%d %s %d%s\n", "During cycle", times[u][0]-1, "-", times[u][0], "of Banker's algorithms, Task", u+1, "'s request exceeds its claim; aborted");
                }
                
                else {
                    
                    // prepare for passing values to safe()
                    numOfRemaining = 0;
                    for (int g = 0; g < numOfTask; g++)  {
                        if (actions[1][g][0] == 3 || actions[1][g][0] == 4) {
                            statusArray[g] = 0;
                        }
                        else {
                            statusArray[g] = 1;
                            numOfRemaining++;
                        }
                    }
                    
                    int resourcesArrayCopy[numOfResource];
                    for (int h = 0; h < numOfResource; h++) {
                        resourcesArrayCopy[h] = resourcesArray[0][h];
                    }
                    resourcesArrayCopy[actions[index][u][1]] -= actions[index][u][2];
                    int fine = 1;
                    if (resourcesArrayCopy[actions[index][u][1]] < 0) {
                        fine = 0;
                    }
                    
                    int claimsCopy[numOfTask][numOfResource];
                    for (int y = 0; y < numOfTask; y++) {
                        for (int z = 0; z < numOfResource; z++) {
                            claimsCopy[y][z] = claims[y][z];
                        }
                    }
                    claimsCopy[u][actions[index][u][1]] -= actions[index][u][2];
                    
                    int taskResourcesCopy [numOfTask][numOfResource];
                    for (int r = 0; r < numOfTask; r++) {
                        for (int q = 0; q < numOfResource; q++) {
                            taskResourcesCopy[r][q] = taskResources[r][q];
                        }
                    }
                    taskResourcesCopy[u][actions[index][u][1]] += actions[index][u][2];
                    
                    // if the resources do not go under 0 and the state is safe, grant the resource
                    if ((fine ==1) && (safe(numOfRemaining, statusArray, resourcesArrayCopy, claimsCopy, taskResourcesCopy) == 1)){
                        claims[u][actions[index][u][1]] -= actions[index][u][2];
                        resourcesArray[0][actions[index][u][1]] -= actions[index][u][2];
                        actions[0][u][0]++;
                        taskResources[u][actions[index][u][1]] += actions[index][u][2];
                    }
                    // otherwise, block the task
                    else {
                        times[u][1]++;
                        actions[1][u][0] = 1;
                        waitingArray[waitingArrayIndex] = u;
                        waitingArrayIndex++;
                    }
                }
            }
            
            // if a task computes, decrease its computing time till it reaches 0
            else if (actions[index][u][0] == 2) {
                times[u][0]++;
                actions[index][u][1]--;
                if (actions[index][u][1] == 0)
                    actions[0][u][0]++;
            }
            
            // if the task releases resources, add them to the total resources available
            else if (actions[index][u][0] == 3) {
                times[u][0]++;
                resourcesArray[1][actions[index][u][1]] = actions[index][u][2];
                taskResources[u][actions[index][u][1]] -= actions[index][u][2];
                claims[u][actions[index][u][1]] += actions[index][u][2];
                actions[0][u][0]++;
            }
            
            // task terminates
            else if (actions[index][u][0] == 4) {
                //printf("4\n");
                actions[1][u][0] = 3;
                numOfTerminated++;
            }
            
            
        }
        
        // unblock tasks for the next cycle
        for (int d = 0; d < numOfTask; d++) {
            if (actions[1][d][0] == 2) {
                actions[1][d][0] = 0;
            }
        }
        
        
    }
    
    // print output for Banker's algorithm
    int totalTime = 0;
    int totalWaitingTime = 0;
    printf("Banker output:\n");
    for (int m = 0; m < numOfTask; m++) {
        
        int n = m+1;
        if (actions[1][m][0] == 3) {
            printf("%s %d\t\t%d\t%d\t%d%s\n", "Task", n, times[m][0], times[m][1], (int)(((times[m][1]*100.0)/times[m][0])+0.5), "%");
            totalTime += times[m][0];
            totalWaitingTime += times[m][1];
        }
        
        if (actions[1][m][0] == 4) {
            printf("%s %d\t\t%s\n", "Task", n, "aborted");
        }
    }
    printf("%s %s\t\t%d\t%d\t%d%s\n", "Total", " ", totalTime, totalWaitingTime, (int)(((totalWaitingTime*100.0)/totalTime)+0.5), "%");

}

//main method
int main(int argc, const char * argv[]) {
    
    // read the file in
    FILE *f = fopen(argv[1], "r");

    char word;
    int task;
    int resources;
    int unitOfResources;

    char oneWord[100];

    fscanf(f, "%d", &numOfTask);
    fscanf(f, "%d", &numOfResource);

    
    //initiate the array for total available resources
    int resourcesArray [2][numOfResource];
    for (int i = 0; i < numOfResource; i++) {
        fscanf(f, "%d", &resourcesArray[0][i]);
        resourcesArray[1][i] = 0;
    }
    
    //initiate the array for claims of each tasks
    int claims [numOfTask][numOfResource];
    
    //initiate the array for actions that records each action of each task in order
    int actions [400][numOfTask][3];
    int actionsIndex[numOfTask];
    
    for (int i = 0; i < numOfTask; i++) {
        actionsIndex[i] = 2;
    }
    

    //read all the input into the actions array
    while ((word = fscanf(f, "%s", oneWord))!= EOF) {
        
        fscanf(f, "%d", &task);
        fscanf(f, "%d", &resources);
        fscanf(f, "%d", &unitOfResources);
        
        if (strcmp(oneWord, "initiate") == 0) {
            claims[task - 1][resources - 1] = unitOfResources;
        }
        
        else {
            int actionCode;
            if (strcmp(oneWord, "request") == 0)
                actionCode = 1;
            else if (strcmp(oneWord, "compute") == 0)
                actionCode = 2;
            else if (strcmp(oneWord, "release") == 0)
                actionCode = 3;
            else if (strcmp(oneWord, "terminate") == 0)
                actionCode = 4;
            
            actions[actionsIndex[task-1]][task - 1][0] = actionCode;
            
            if (actionCode == 2)
                actions[actionsIndex[task-1]][task - 1][1] = resources;
            else
                actions[actionsIndex[task-1]][task - 1][1] = resources - 1;
            
            actions[actionsIndex[task-1]][task - 1][2] = unitOfResources;
            
            actionsIndex[task-1]++;
        }
        
        
    }
    
    // find the maximum index for actions
    int maxActionsIndex = 0;
    for (int i = 0; i < numOfTask; i++) {
        if (actionsIndex[i] > maxActionsIndex)
            maxActionsIndex = actionsIndex[i];
    }
    
    fclose(f);
    
    // make duplicate copies of arrays to pass to FIFO
    int resourcesArrayCopy[2][numOfResource];
    for (int i = 0; i < 2; i++) {
        for (int j = 0; j < numOfResource; j++) {
            resourcesArrayCopy[i][j] = resourcesArray[i][j];
        }
    }
    
    int actionsCopy[400][numOfTask][3];
    for (int i = 0; i < maxActionsIndex; i++) {
        for (int j = 0; j < numOfTask; j++) {
            for (int k = 0; k < 3; k++) {
                actionsCopy[i][j][k] = actions[i][j][k];
            }
        }
    }
    
    // call FIFO method
    FIFO(actionsCopy, resourcesArrayCopy);
    // call Banker method
    Banker(claims, actions, resourcesArray);
    
    return 0;
    
}


