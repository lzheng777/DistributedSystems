package project;

import java.rmi.RemoteException;
import java.util.*;

public class Man extends Caller implements ManRMI, Runnable{
    List<Integer> mPref;
    List<Integer> mRank;
    List<HashSet<Proposal>> prereq;
    int proposal;   //Gi - proposal number by Pi

    public Man(int id, String[] peers, int[] ports, ArrayList<Integer> mPref){
        super(id, peers, ports);
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
    }

    public void run(){
        for (Proposal p : prereq.get(proposal)){
            //todo: Call ("advance", w) on Man Pm
            Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man), p.man);
        }
        //todo: Call ("propose", i) to woman mpref[Gi]
        Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId()), getProposal());
    }

    public int getId(){
        return this.me;
    }

    public int getProposal() {return this.mPref.get(this.proposal);}

    @Override
    public Response Advance(Request req) throws RemoteException {
        while (mRank.get(req.woman) > proposal){
            proposal++;
            for (Proposal p : prereq.get(proposal)){
                Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man), p.man);
            }
        }
        Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId()), getProposal());
        return null;
    }

    @Override
    public Response Reject(Request req) throws RemoteException {
        if (getProposal() == req.woman){
            proposal++;
            if (proposal == mPref.size()){
                return new Response(false);
            }
            for (Proposal p : prereq.get(proposal)){
                Response response = CallMan(Message.ADVANCE, new Request(p.woman, p.man), p.man);
            }
            Response response = CallWoman(Message.PROPOSE, new Request(getProposal(), getId()), getProposal());
        }
        return new Response(true);
    }
}
