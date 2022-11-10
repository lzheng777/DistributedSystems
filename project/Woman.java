package project;

import java.util.List;
import java.util.ArrayList;

public class Woman implements Runnable{
    List<Integer> wRank;
    Man partner;

    int id;

    public Woman(int id, ArrayList<Integer> wRank){
        this.id = id;
        this.wRank = wRank;
        partner = null;
    }

    public void run(){
        // wait for proposals to come in
    }

    public boolean propose(Man j){
        if (partner == null){
            partner = j;
            return true;
        }
        // woman likes man j more than current partner
        else if (wRank.get(j.getId()) < wRank.get(partner.getId())){
            partner.reject(this);
            partner = j;
            return true;
        }

        return false;
    }

    public int getId(){
        return id;
    }
}
