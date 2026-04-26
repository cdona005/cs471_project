# CPU Scheduler Simulation

Simulates FIFO and SJF (non-preemptive) CPU scheduling algorithms on 500 processes found in (input_data)[https://github.com/cdona005/cs471_project/blob/main/CPUSCHED/input_data/Datafile1-txt.txt]. 


## Compile
To compile, run:

```bash
cd CPUSCHED
javac cpu_scheduler.java
```

## Running the Program
To run the program, run:
```bash
java cpu_scheduler
```

## Input 
- 'input_data/Datafile1-txt.txt' - 500 processes with arrival time and CPU burst length

## Output

- Prints FIFO and SJF statistics to [output_data/cpu_output.txt](https://github.com/cdona005/cs471_project/blob/main/CPUSCHED/output_data/cpu_output.txt)
- Statistics include: total elapsed time, throughput, CPU utilization,
  average waiting time, average turnaround time, average response time