package remote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorService extends UnicastRemoteObject implements RemoteCalculator {
    public CalculatorService() throws RemoteException {
        super();
    }

    @Override
    public double add(double a, double b) throws RemoteException {
        return a + b;
    }

    @Override
    public double subtract(double a, double b) throws RemoteException {
        return a - b;
    }

    @Override
    public double calculateBalance(double income, double expense) throws RemoteException {
        return subtract(income, expense);
    }
}