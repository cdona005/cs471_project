package CPUSCHED;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class cpu_scheduler {
    public static void main(String[] args) throws IOException {
        PrintStream fileOut = new PrintStream(new File("output_data/cpu_output.txt"));
        System.setOut(fileOut);
        List<Process> processes = new ArrayList<>();
        Scanner sc = null;

        try {
            sc = new Scanner(new File("input_data/Datafile1-txt.txt"));
            sc.nextLine(); // Skip header
        int pid = 0;

        while (sc.hasNextInt() && pid < 500) {
            int arrival = sc.nextInt();
            int burst = sc.nextInt();
            processes.add(new Process(pid++, arrival, burst));
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (sc != null) {
            sc.close();
        }
    }
    System.out.println("Loaded " + processes.size() + " processes.");
    processes.sort((p1, p2) -> Integer.compare(p1.arrivalTime, p2.arrivalTime));

    fifo(processes);
    sjf(processes);
    }

private static void fifo(List<Process> processes) {
    int currentTime = 0;
    long totalWaiting = 0, totalTurnaround = 0, totalResponse = 0, totalBurst = 0;
    int firstStart = -1, lastFinish = 0;

    for (Process p : processes) {
        if (currentTime < p.arrivalTime) {
            currentTime = p.arrivalTime;
        }

        int startTime     = currentTime;
        int finishTime    = currentTime + p.burstTime;
        int waiting       = startTime - p.arrivalTime;
        int turnaround    = finishTime - p.arrivalTime;
        int response      = waiting; // FIFO: first CPU time = start time

        totalWaiting    += waiting;
        totalTurnaround += turnaround;
        totalResponse   += response;
        totalBurst      += p.burstTime;

        if (firstStart == -1) firstStart = startTime;
        lastFinish = finishTime;

        currentTime = finishTime;
    }

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

private static void sjf(List<Process> processes) {
    int currentTime = 0;
    long totalWaiting = 0, totalTurnaround = 0, totalResponse = 0, totalBurst = 0;
    int firstStart = -1, lastFinish = 0;

    List<Process> remaining = new ArrayList<>(processes);

    while (!remaining.isEmpty()) {
        // Get all processes that have arrived by currentTime
        List<Process> ready = new ArrayList<>();
        for (Process p : remaining) {
            if (p.arrivalTime <= currentTime) ready.add(p);
        }

        if (ready.isEmpty()) {
            // CPU is idle — jump to next arrival
            currentTime = remaining.get(0).arrivalTime;
            continue;
        }

        // Pick the one with shortest burst
        Process p = ready.stream()
            .min(Comparator.comparingInt(x -> x.burstTime))
            .get();

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


