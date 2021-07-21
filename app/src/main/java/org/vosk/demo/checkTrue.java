package org.vosk.demo;

import java.util.ArrayList;
import java.util.List;

public class checkTrue {
    private String s1;
    private List<String> standardSentence;
    private List<String> lcs;
    private int removeNumber;
    private int wordNumber;

    checkTrue(String str, List<String> list1, List<String> list2, int number) {
        s1 = str;
        standardSentence = list1;
        lcs = list2;
        removeNumber = 0;
        wordNumber = number;
    }

    public List<Boolean> getResult() {
        List<Boolean> result = new ArrayList<>();
        for (int i = 0; i < standardSentence.size(); i++) {
            String stand = standardSentence.get(i);
            int lcsIndex = lcs.indexOf(stand);
            if ((lcsIndex+removeNumber) <= i && lcsIndex != -1) {
                result.add(true);
                result.remove(lcsIndex);
                removeNumber++;
            } else {
                result.add(false);
            }
        }
        return result;
    }
}
