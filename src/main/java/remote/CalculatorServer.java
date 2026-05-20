package remote;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CalculatorServer {
    public static void main(String[] args) {
        try{
            LocateRegistry.createRegistry(1099);

            CalculatorService service = new CalculatorService();
            Naming.bind("rmi://localhost:1099/CalculatorService", service);
            System.out.println("Calculator RMI server is running.");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
