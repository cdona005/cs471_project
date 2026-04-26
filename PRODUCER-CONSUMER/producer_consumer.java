
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

public class producer_consumer {

    static final int BUFFER_SIZE = 10;
    static final int MAX_ITEMS = 1000;

    // Circular buffer
    static SalesRecord[] buffer = new SalesRecord[BUFFER_SIZE];
    static int in = 0, out = 0;

    // SEMAPHORES — mutual exclusion and buffer slot tracking
    static Semaphore mutex = new Semaphore(1); // mutual exclusion on buffer
    static Semaphore empty = new Semaphore(BUFFER_SIZE); // tracks empty slots
    static Semaphore full = new Semaphore(0); // tracks filled slots
    static Semaphore statsMutex = new Semaphore(1); // mutual exclusion on global stats

    // SHARED VARIABLE — total items produced across all producers
    static AtomicInteger totalProduced = new AtomicInteger(0);

    // Global statistics (merged from consumers at end)
    static double[] storeWideSales;
    static double[] monthWiseSales = new double[12];
    static double aggregateSales = 0;

    static long startTime;

    public static void main(String[] args) throws InterruptedException, IOException {
        if (args.length < 2) {
            System.out.println("Usage: java PRODUCERCONSUMER.producer_consumer <numProducers> <numConsumers>");
            return;
        }

        int p = Integer.parseInt(args[0]);
        int c = Integer.parseInt(args[1]);

        PrintStream fileOut = new PrintStream(new File("output_data/pc_p" + p + "_c" + c + ".txt"));
        System.setOut(fileOut);
        storeWideSales = new double[p + 1]; // index 1..p

        System.out.println("Starting: " + p + " producers, " + c + " consumers, buffer size " + BUFFER_SIZE);
        startTime = System.currentTimeMillis();

        Thread[] producers = new Thread[p];
        Thread[] consumers = new Thread[c];

        for (int i = 0; i < c; i++) {
            consumers[i] = new Thread(new Consumer(i + 1, p));
            consumers[i].start();
        }
        for (int i = 0; i < p; i++) {
            producers[i] = new Thread(new Producer(i + 1));
            producers[i].start();
        }

        // Wait for all producers to finish
        for (Thread t : producers)
            t.join();

        // Main thread acts as the designated "flag" thread —
        // releases c extra permits so each consumer wakes up and sees empty buffer →
        // exits
        for (int i = 0; i < c; i++)
            full.release();

        for (Thread t : consumers)
            t.join();

        long totalTime = System.currentTimeMillis() - startTime;

        String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        System.out.println("\n========== Global Statistics ==========");
        System.out.println("Producers: " + p + "  |  Consumers: " + c);
        System.out.println("Total items produced:  " + MAX_ITEMS);
        System.out.println("Total simulation time: " + totalTime + " ms");
        System.out.printf("Aggregate sales:       $%.2f%n", aggregateSales);

        System.out.println("\nStore-wide total sales:");
        for (int i = 1; i <= p; i++)
            System.out.printf("  Store %d: $%.2f%n", i, storeWideSales[i]);

        System.out.println("\nMonth-wise total sales:");
        for (int i = 0; i < 12; i++)
            System.out.printf("  %s: $%.2f%n", months[i], monthWiseSales[i]);
    }

    // ===================== SalesRecord =====================
    static class SalesRecord {
        int dd, mm, yy, storeId, registerNum;
        double saleAmount;

        SalesRecord(int dd, int mm, int yy, int storeId, int registerNum, double saleAmount) {
            this.dd = dd;
            this.mm = mm;
            this.yy = yy;
            this.storeId = storeId;
            this.registerNum = registerNum;
            this.saleAmount = saleAmount;
        }
    }

    // ===================== Producer =====================
    static class Producer implements Runnable {
        int storeId;
        Random rand = new Random();

        Producer(int storeId) {
            this.storeId = storeId;
        }

        public void run() {
            while (true) {
                // Atomically claim the next production slot — SHARED VARIABLE access
                int count = totalProduced.incrementAndGet();
                if (count > MAX_ITEMS)
                    break; // 1000 items claimed, stop

                int dd = rand.nextInt(30) + 1;
                int mm = rand.nextInt(12) + 1;
                int registerNum = rand.nextInt(6) + 1;
                double amount = 0.50 + rand.nextDouble() * (999.99 - 0.50);
                SalesRecord rec = new SalesRecord(dd, mm, 16, storeId, registerNum, amount);

                try {
                    empty.acquire(); // SEMAPHORE: wait for an empty slot
                    mutex.acquire(); // SEMAPHORE: lock buffer for mutual exclusion
                    buffer[in] = rec;
                    in = (in + 1) % BUFFER_SIZE;
                    mutex.release(); // SEMAPHORE: release buffer lock
                    full.release(); // SEMAPHORE: signal that a new item is available

                    Thread.sleep(rand.nextInt(36) + 5); // sleep 5-40 ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // ===================== Consumer =====================
    static class Consumer implements Runnable {
        int consumerId, numProducers;
        double[] localStoreSales;
        double[] localMonthSales = new double[12];
        double localAggregate = 0;
        int itemsConsumed = 0;

        Consumer(int consumerId, int numProducers) {
            this.consumerId = consumerId;
            this.numProducers = numProducers;
            this.localStoreSales = new double[numProducers + 1];
        }

        public void run() {
            // ---- consuming loop ----
            while (true) {
                try {
                    full.acquire(); // SEMAPHORE: wait for an item

                    mutex.acquire(); // SEMAPHORE: lock buffer
                    if (in == out) { // buffer empty = termination signal
                        mutex.release();
                        break;
                    }
                    SalesRecord rec = buffer[out];
                    out = (out + 1) % BUFFER_SIZE;
                    mutex.release(); // SEMAPHORE: release buffer lock
                    empty.release(); // SEMAPHORE: signal empty slot available

                    // Update local statistics
                    localAggregate += rec.saleAmount;
                    localStoreSales[rec.storeId] += rec.saleAmount;
                    localMonthSales[rec.mm - 1] += rec.saleAmount;
                    itemsConsumed++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // ---- print local stats AFTER loop exits ----
            String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
            try {
                statsMutex.acquire(); // SEMAPHORE: prevent interleaved printing
                System.out.println("\n--- Consumer " + consumerId + " Local Statistics ---");
                System.out.println("Items consumed: " + itemsConsumed);
                System.out.printf("Local aggregate: $%.2f%n", localAggregate);
                for (int i = 1; i <= numProducers; i++)
                    System.out.printf("  Store %d: $%.2f%n", i, localStoreSales[i]);
                for (int i = 0; i < 12; i++)
                    System.out.printf("  %s: $%.2f%n", months[i], localMonthSales[i]);

                // Merge into global stats while we still hold the lock
                aggregateSales += localAggregate;
                for (int i = 1; i <= numProducers; i++)
                    storeWideSales[i] += localStoreSales[i];
                for (int i = 0; i < 12; i++)
                    monthWiseSales[i] += localMonthSales[i];
                statsMutex.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}