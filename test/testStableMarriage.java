package test;

import project.Environment;
import project.Man;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

public class testStableMarriage {
    public boolean isStableMatching(Matching marriage) {
        int[][] locXemp=new int[marriage.getLocationCount()][marriage.getEmployeeCount()];
        for (int i=0;i<marriage.getLocationCount();i++){
            ArrayList<Integer> prefs=marriage.getLocationPreference().get(i);
            for (int j=0;j<marriage.getEmployeeCount();j++){
                locXemp[i][prefs.get(j)]=j;
            }
        }
        int [][] empXloc=new int[marriage.getEmployeeCount()][marriage.getLocationCount()];
        for (int i=0;i<marriage.getEmployeeCount();i++){
            ArrayList<Integer> prefs=marriage.getEmployeePreference().get(i);
            for (int j=0;j<marriage.getLocationCount();j++){
                empXloc[i][prefs.get(j)]=j;
            }
        }
        ArrayList<ArrayList<Integer>> locationMatching=new ArrayList<>();
        for(int i=0;i<marriage.getLocationCount();i++)
            locationMatching.add(new ArrayList<>());
        ArrayList<Integer> match=marriage.getEmployeeMatching();
        for(int i=0;i<marriage.getEmployeeCount();i++){
            int loc=match.get(i);
            if(loc>-1)
                locationMatching.get(loc).add(i);
        }
        //Data Structures
        //matrix of location: x employee = [preference]
        //matrix of employee: x location = [preference]
        //ArrayList of Arraylist of Integer: location = {employees}

        for(int i=0;i<marriage.getLocationCount();i++) { //i is location
            int lowestPref=-1;
            for(int j=0;j<locationMatching.get(i).size();j++){//j is the index employee
                int pref=locXemp[i][locationMatching.get(i).get(j)];
                if(pref>lowestPref)
                    lowestPref=pref;
            }
            for (int j=0;j<lowestPref;j++){//j is the preference
                int emp=marriage.getLocationPreference().get(i).get(j);
                int loc=marriage.getEmployeeMatching().get(emp);
                if(loc==-1)
                    return false;
                if(i!=loc){
                    if(empXloc[emp][i]<empXloc[emp][loc])
                        return false;
                }
            }
        }
        return true;
    }

    public void TestBasic(String filepath){
        Environment env = new Environment();
        env.runStableMarriage(filepath);
        ArrayList<Integer> finalMatching = new ArrayList<>();
        for (Man man :
                env.men) {
            finalMatching.add(man.getProposal());
        }
        assertTrue(isStableMatching(new Matching(env.m, env.n, env.wprefs, env.mprefs, finalMatching)));
    }

    @Test
    public void Test1(){
        TestBasic("src/data/small_inputs/1-5-5.in");
    }

    @Test
    public void Test2(){
        TestBasic("src/data/small_inputs/1-10-5.in");
    }

    @Test
    public void Test3(){
        TestBasic("src/data/small_inputs/3-3-3.in");
    }

    @Test
    public void Test4(){
        TestBasic("src/data/small_inputs/3-10-3.in");
    }
}

class Matching{
    private Integer m;
    private Integer n;
    private ArrayList<ArrayList<Integer>> woman_preference;
    private ArrayList<ArrayList<Integer>> man_preference;
    private ArrayList<Integer> matching;
    public Matching(
            Integer m,
            Integer n,
            ArrayList<ArrayList<Integer>> woman_preference,
            ArrayList<ArrayList<Integer>> man_preference,
            ArrayList<Integer> matching) {
        this.m = m;
        this.n = n;
        this.woman_preference = woman_preference;
        this.man_preference = man_preference;
        this.matching = matching;
    }
    public Integer getLocationCount() {
        return m;
    }
    public Integer getEmployeeCount() {
        return n;
    }
    public ArrayList<ArrayList<Integer>> getLocationPreference() {
        return woman_preference;
    }
    public ArrayList<ArrayList<Integer>> getEmployeePreference() {
        return man_preference;
    }
    public ArrayList<Integer> getEmployeeMatching() {
        return matching;
    }
}
