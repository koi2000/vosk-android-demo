package org.vosk.demo;

import java.util.ArrayList;
import java.util.List;

public class checkTrue {
    private List<String> standardSentence;
    private List<String> lcs;
    private int removeNumber;

    checkTrue(List<String> list1, List<String> list2) {
        standardSentence = list1;
        lcs = list2;
        removeNumber = 0;
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
