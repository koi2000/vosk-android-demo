package org.vosk.demo;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class Lcs {
    private int length;
    private int[][] dp =null;
    private int[][] flag =null;
    private List<String> answer,s1,s2;
    private int commonLength;
    private List<String> sentence_splited;
    private List<String> test_splited;

    public List<String> getSentence_splited() {
        return sentence_splited;
    }

    public List<String> getTest_splited() {
        return test_splited;
    }

    Lcs(String a, String b){
        s1 = init(a);
        s2 = init(b);
        sentence_splited = s1;
        test_splited = s2;
        length = Math.max(s1.size(),s2.size())+1;
        dp =new int[length][length];
        flag =new int[length][length];
        answer=new ArrayList<>();
        for(int i=0;i<length;i++)
            for(int j=0;j<length;j++){
                dp[i][j]=0;
                flag[i][j]=0;
            }
        answer.clear();
        commonLength = 0;
    }
    private List<String> init(String s){
        List<String>lStrings = new ArrayList<>();
        String[] strings =  s.split(" ");
        lStrings.addAll(Arrays.asList(strings));
        return lStrings;
    }
    private int getLcs(){

        List<String>x = s1;
        List<String>y = s2;
        for(int i=0;i<x.size();i++)
        {
            for(int j=0;j<y.size();j++)
            {
                if(x.get(i).equals(y.get(j)) ){
                    dp[i+1][j+1] = dp[i][j]+1;
                    flag[i+1][j+1]=0;
                }
                else if(dp[i][j+1]>=dp[i+1][j]){
                    dp[i+1][j+1]=dp[i][j+1];
                    flag[i+1][j+1]=1;
                }
                else {
                    dp[i+1][j+1]=dp[i+1][j];
                    flag[i+1][j+1]=-1;
                }
            }
        }
        return dp[x.size()][y.size()];
    }
    private void printLcs(int i,int j){
        if(i==0||j==0){
            return;
        }
        if(flag[i][j]==0){
            printLcs(i-1, j-1);
            answer.add(s1.get(i-1));

        }
        else if(flag[i][j]==1){
            printLcs(i-1, j);
        }
        else{
            printLcs(i, j-1);
        }
    }

    public List<String> getAnswerCommonList(){
        return answer;
    }

    public int getAnswerCommonLength(){
        return commonLength;
    }

    public void run(){
        commonLength = getLcs();
        printLcs(s1.size(), s2.size());
    }
}
