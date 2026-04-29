# Producer-Consumer Simulation

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

## Simulation Results

| Producers / Consumers | c = 2 | c = 5 | c = 10 |
| --------------------- | ----- | ----- | ------ |
| **p = 2** |  12610 ms | 12878 ms |  12553 ms |
| **p = 5** | 5178 ms | 5182 ms | 5158 ms |
| **p = 10** | 2537 ms | 2586 ms | 2614 ms | 

## Analysis
Effect of producers: Increasing the number of producers dramatically reduces simulation time. Going from p=2 to p=10 reduces runtime by about 5x because more threads are generated, making the program reach the 1000 item target more quickly.
Effect of consumers: The number of consumers makes very little difference with runtime. Producers are the bottleneck because of the 5-40ms sleep between records, while consumers are always read to consume. 