package remote;

import java.rmi.Naming;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculatorClient {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "calculator-rmi-client");
        thread.setDaemon(true);
        return thread;
    });

    public CompletableFuture<Double> calculateBalanceAsync(double income, double expense) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RemoteCalculator calculator = (RemoteCalculator) Naming.lookup(CalculatorServer.SERVICE_URL);
                return calculator.calculateBalance(income, expense);
            } catch (Exception e) {
                throw new IllegalStateException("Remote calculator is unavailable.", e);
            }
        }, executorService);
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}