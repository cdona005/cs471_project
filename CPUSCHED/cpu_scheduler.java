
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class cpu_scheduler {
    public static void main(String[] args) throws IOException {
        // Redirect output to file
        PrintStream fileOut = new PrintStream(new File("output_data/cpu_output.txt"));
        System.setOut(fileOut);
        // Load processes from file
        List<Process> processes = new ArrayList<>();
        // Use try-with-resources to ensure Scanner is closed
        Scanner sc = null;

        try {
            sc = new Scanner(new File("input_data/Datafile1-txt.txt"));
            sc.nextLine(); // Skip header
            int pid = 0;
            // Read pairs of integers until we hit the end of the file or 500 processes
            while (sc.hasNextInt() && pid < 500) {
                int arrival = sc.nextInt();
                int burst = sc.nextInt();
                processes.add(new Process(pid++, arrival, burst));
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
    // Scanner will be closed in the finally block, even if an exception occurs 
    finally {
        if (sc != null) {
            sc.close();
        }
    }
    System.out.println("Loaded " + processes.size() + " processes.");
    // Sort processes by arrival time for FIFO
    processes.sort((p1, p2) -> Integer.compare(p1.arrivalTime, p2.arrivalTime));
    // Run both algorithms
    fifo(processes);
    sjf(processes);
    }

// FIFO and SJF implementations
// Both methods calculate and print statistics: total elapsed time, throughput, CPU utilization, average waiting time, average turnaround time, and average response time.
// FIFO: First-Come, First-Served scheduling
// SJF: Shortest Job First scheduling (non-preemptive)
// Input: List of processes with arrival time and burst time
// Output: Statistics for each scheduling algorithm
private static void fifo(List<Process> processes) {
    // Initialize variables to track time and statistics
    int currentTime = 0;
    long totalWaiting = 0, totalTurnaround = 0, totalResponse = 0, totalBurst = 0;
    int firstStart = -1, lastFinish = 0;
    // Iterate through processes in order of arrival
    for (Process p : processes) {
        if (currentTime < p.arrivalTime) {
            currentTime = p.arrivalTime;
        }
        // Calculate start time, finish time, waiting time, turnaround time, and response time
        int startTime     = currentTime;
        int finishTime    = currentTime + p.burstTime;
        int waiting       = startTime - p.arrivalTime;
        int turnaround    = finishTime - p.arrivalTime;
        int response      = waiting;

        totalWaiting    += waiting;
        totalTurnaround += turnaround;
        totalResponse   += response;
        totalBurst      += p.burstTime;
        // Update first start time and last finish time
        if (firstStart == -1) firstStart = startTime;
        lastFinish = finishTime;

        currentTime = finishTime;
    }
    // Calculate and print statistics
    int n = processes.size();
    long totalElapsed = lastFinish - firstStart;

    System.out.println("\n----------- FIFO Statistics -----------");
    System.out.println("Number of processes:        " + n);
    System.out.println("Total elapsed time:         " + totalElapsed);
    System.out.printf( "Throughput:                 %.4f%n", (double) totalBurst / n);
    System.out.printf( "CPU utilization:            %.2f%%%n", (double) totalBurst / totalElapsed * 100);
    System.out.printf( "Average waiting time:       %.2f%n", (double) totalWaiting / n);
    System.out.printf( "Average turnaround time:    %.2f%n", (double) totalTurnaround / n);
    System.out.printf( "Average response time:      %.2f%n", (double) totalResponse / n);
}
// SJF scheduling algorithm implementation
// This method simulates the Shortest Job First scheduling algorithm and calculates statistics similar to FIFO
// It maintains a list of remaining processes and selects the one with the shortest burst time that has arrived by the current time
// Input: List of processes with arrival time and burst time
// Output: Statistics for SJF scheduling
private static void sjf(List<Process> processes) {
    int currentTime = 0;
    long totalWaiting = 0, totalTurnaround = 0, totalResponse = 0, totalBurst = 0;
    int firstStart = -1, lastFinish = 0;
    // Create a mutable list of processes to track which ones are still waiting
    List<Process> remaining = new ArrayList<>(processes);
    // Loop until all processes have been scheduled
    while (!remaining.isEmpty()) {
        // Get all processes that have arrived by currentTime
        List<Process> ready = new ArrayList<>();
        for (Process p : remaining) {
            if (p.arrivalTime <= currentTime) ready.add(p);
        }
        // If no processes are ready, jump to the next arrival time
        if (ready.isEmpty()) {
            // CPU is idle — jump to next arrival
            currentTime = remaining.get(0).arrivalTime;
            continue;
        }

        // Pick the one with shortest burst
        Process p = ready.stream()
            .min(Comparator.comparingInt(x -> x.burstTime))
            .get();
        // Calculate start time, finish time, waiting time, turnaround time, and response time
        int startTime  = currentTime;
        int finishTime = currentTime + p.burstTime;
        int waiting    = startTime - p.arrivalTime;
        int turnaround = finishTime - p.arrivalTime;

        totalWaiting    += waiting;
        totalTurnaround += turnaround;
        totalResponse   += waiting;
        totalBurst      += p.burstTime;

        if (firstStart == -1) firstStart = startTime;
        lastFinish = finishTime;

        currentTime = finishTime;
        remaining.remove(p);
    }
    // Calculate and print statistics
    int n = processes.size();
    long totalElapsed = lastFinish - firstStart;

    System.out.println("\n========== SJF Statistics ==========");
    System.out.println("Number of processes:        " + n);
    System.out.println("Total elapsed time:         " + totalElapsed);
    System.out.printf( "Throughput:                 %.4f%n", (double) totalBurst / n);
    System.out.printf( "CPU utilization:            %.2f%%%n", (double) totalBurst / totalElapsed * 100);
    System.out.printf( "Average waiting time:       %.2f%n", (double) totalWaiting / n);
    System.out.printf( "Average turnaround time:    %.2f%n", (double) totalTurnaround / n);
    System.out.printf( "Average response time:      %.2f%n", (double) totalResponse / n);
}
    // Process class to represent each process with its PID, arrival time, and burst time
    // Input: PID (process ID), arrival time, burst time
    // Output: A Process object that can be used in scheduling algorithms
    public static class Process {
        int pid;
        int arrivalTime;
        int burstTime;

    public Process(int pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
    }
}


}


