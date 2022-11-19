package project;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Man extends Caller implements ManRMI, Runnable{
    List<Integer> mPref;
    List<Integer> mRank;
    List<HashSet<Proposal>> prereq;
    int proposal;   //Gi - proposal number by Pi\

    ManRMI stub;

    public Man(int id, String[] mpeers, int[] mports, String[] wpeers, int[] wports, ArrayList<Integer> mPref){
        super(id, wpeers, wports);
        this.mPref = mPref;
        this.mRank = new ArrayList<>();
        for (int i = 0; i < this.mPref.size(); i++) {
            this.mRank.add(-1);
        }

        for (int i = 0; i < this.mPref.size(); i++){
            this.mRank.set(this.mPref.get(i), i);
        }

        this.proposal = 0;
        this.prereq = new ArrayList<>();

        for (int i = 0; i < this.mPref.size(); i++){
            this.prereq.add(new HashSet<>());
        }

        try{
            System.setProperty("java.rmi.server.hostname", mpeers[this.me]);
            this.registry = LocateRegistry.createRegistry(mports[this.me]);
            this.stub = (ManRMI) UnicastRemoteObject.exportObject(this, mports[this.me]);
            this.registry.rebind("Man", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run(){
        for (Proposal p : prereq.get(proposal)){
            //todo: Call ("advance", w) on Man Pm
            Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man, 1), p.man);
        }
        while (this.proposal < this.mPref.size()) {
            //todo: Call ("propose", i) to woman mpref[Gi]
            Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId(), 1), getProposal());
//            System.out.println("Pid: "+this.me+" Messages: "+response.numMessages);
            if (response != null && response.accept)
                break;
            this.proposal++;
        }
    }

    public int getId(){
        return this.me;
    }

    public int getProposal() {return (this.proposal < this.mPref.size()) ? this.mPref.get(this.proposal) : -1 ;}

    @Override
    public Response Advance(Request req) throws RemoteException {
        int numMessages = req.numMessages;
        while (mRank.get(req.woman) > proposal){
            proposal++;
            for (Proposal p : prereq.get(proposal)){
                Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man, numMessages+1), p.man);
                numMessages += response.numMessages;
            }
        }
        Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId(), numMessages+1), getProposal());
        numMessages += response.numMessages;
        return new Response(true, numMessages);
    }

    @Override
    public Response Reject(Request req) throws RemoteException {
//        System.out.println("Man "+this.me+" received Reject from woman"+req.woman);
        int numMessages = req.numMessages;
        if (getProposal() == req.woman){
            proposal++;
            if (proposal == mPref.size()){
                return new Response(false, numMessages);
            }
            for (Proposal p : prereq.get(proposal)){
                Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man, numMessages+1), p.man);
                numMessages += response.numMessages;
            }
            while (this.proposal < this.mPref.size()) {
                Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId(), 1), getProposal());
                numMessages += response.numMessages;
                if (response.accept)
                    break;
                this.proposal++;
            }
        }
        return new Response(true, numMessages);
    }
}
