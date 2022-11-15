package project;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface for each Man RMI call.
 * Prepare is the RMI that proposer sends prepare request to acceptors.
 * Accept is the RMI that proposer sends accept request to acceptors.
 * Decide is the RMI that proposer broadcasts decision once consensus reaches.
 */
public interface WomanRMI extends Remote{
    Response Proposal(Request req) throws RemoteException;
}
