package project;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

public class Man implements Runnable{
    List<Woman> mPref;
    List<Integer> mRank;
    List<HashSet<Proposal>> prereq;
    int id;
    int proposal;

    public Man(int id, ArrayList<Woman> mPref){
        this.id = id;
        this.mPref = mPref;
        this.mRank = new ArrayList<>(mPref.size());

        for (int i = 0; i < mPref.size(); i++){
            mRank.set(mPref.get(i).getId(), i);
        }

        proposal = 0;
        prereq = new ArrayList<>();

        HashSet<Proposal> set = new HashSet<>();
        for (int i = 0; i < mPref.size(); i++){
            prereq.add(new HashSet<>(set));
            set.add(new Proposal(this, mPref.get(i)));
        }
    }

    public void run(){
        for (Proposal p : prereq.get(proposal)){
            p.m.advance(p.w);
            mPref.get(proposal).propose(this);
        }
    }

    public boolean reject(Woman j){
        if (mPref.get(proposal) == j){
            if (proposal == mPref.size()){
                return false;
            }

            proposal++;
            for (Proposal p : prereq.get(proposal)){
                p.m.advance(p.w);
                if (mPref.get(proposal).propose(this)){
                    break;
                }
            }
        }

        return true;
    }

    public void advance(Woman q){
        while (mRank.get(q.getId()) > proposal){
            proposal++;
            for (Proposal p : prereq.get(proposal)){
                p.m.advance(p.w);
            }
        }
        mPref.get(proposal).propose(this);
    }

    public int getId(){
        return id;
    }
}
