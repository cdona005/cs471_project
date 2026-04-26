# CPU Scheduler Simulation

Simulates the Producer-Consumer problem using threads and semaphores.
Producers generate random sales records into a shared buffer; consumers read them. 

## Compile (from root)
To compile, run:
```bash
cd PRODUCER-CONSUMER
javac producer_consumer.java
```

## Running the Program
To run the program, run:
```bash
java producer_consumer <numProducers> <numConsumers>
```
## All 9 Required Runs
```bash
java producer_consumer 2 2
java producer_consumer 2 5
java producer_consumer 2 10
java producer_consumer 5 2
java producer_consumer 5 5
java producer_consumer 5 10
java producer_consumer 10 2
java producer_consumer 10 5
java producer_consumer 10 10
```

## Input 
Randomly generated sales records per producer:
- Date: DD (1-30), MM (01-12), YY (16)
- Store ID: 1 to p (number of producers)
- Register number: 1-6
- Sale amount: $0.50 - $999.99

## Output
- Per-consumer local statistics (items consumed, store sales, month sales)
- Global statistics (aggregate sales, store-wide totals, month-wise totals, simulation time)
- Each run saved to `output_data/pc_p{p}_c{c}.txt`

## Semaphores Used
- `mutex` — mutual exclusion on the shared buffer
- `empty` — tracks available empty buffer slots (blocks producers when full)
- `full` — tracks filled buffer slots (blocks consumers when empty)
- `statsMutex` — mutual exclusion when consumers write to global statistics