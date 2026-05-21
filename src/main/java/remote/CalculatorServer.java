package remote;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class CalculatorServer {
    public static final String SERVICE_URL = "rmi://localhost:1099/CalculatorService";
    private static boolean started;

    public static synchronized void startServer() throws Exception {
        if (started) {
            return;
        }
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException ignored) {
            // Registry is already running on this machine.
        }

        CalculatorService service = new CalculatorService();
        try {
            Naming.bind(SERVICE_URL, service);
        } catch (AlreadyBoundException ex) {
            Naming.rebind(SERVICE_URL, service);
        }
        started = true;
        System.out.println("Calculator RMI server is running at " + SERVICE_URL);
    }

    public static void startServerInBackground() {
        Thread serverThread = new Thread(() -> {
            try {
                startServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "calculator-rmi-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static void main(String[] args) {
        try {
            startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}