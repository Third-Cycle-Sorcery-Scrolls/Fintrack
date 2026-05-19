package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorService extends UnicastRemoteObject {
    public CalculatorService() throws RemoteException{
        super();
    }

    public double add(double a, double b) throws RemoteException{
        return a + b;
    }
    public double subtract(double a, double b) throws RemoteException{
        return a - b;
    }
    public double calculateBalance(double income, double expense) throws RemoteException{
        return subtract(income, expense);
    }
}
