package org.vosk.demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.res.AssetManager;

public class ConverterUtils extends Activity{

    private final static String TAG = "ConverterUtils";

    public String fileTogrammer(String pathname) throws IOException {


        /*
        InputStream open = null ;
        try{
            Log.d(TAG,"开始创建");
            AssetManager assets = getAssets();
            Log.d(TAG,"创建成功"+assets.toString());
            open = assets.open("A00001.txt");
        }catch (Exception e){
            Log.d(TAG,"打开文件出错");
            e.printStackTrace();
        }
         */

        InputStream open = getClass().getResourceAsStream("/assets/page1.txt");

        //InputStream open = new FileInputStream("src/asserts/A00001.txt");
        byte[] bytes=new byte[1024];
        StringBuilder buffer = new StringBuilder();
        int n = 0;
        while ((n=open.read(bytes))!=-1){
            String s = new String(bytes,0,n);
            buffer.append(s);
        }
        String sAll = buffer.toString();

        Set<Character> sp = new HashSet<>();
        for (int i = 0; i < sAll.length(); i++) {
            if(!((sAll.charAt(i)>=65&&sAll.charAt(i)<=90)||((sAll.charAt(i)>=97)&&sAll.charAt(i)<=122))){
                sp.add(sAll.charAt(i));
            }
        }
        for(char ch : sp){
            if(ch!=' '){
                sAll = sAll.replace(ch,' ');
            }
        }

        /*
        String[] split = sAll.split("\n");
        StringBuilder fin = new StringBuilder("[\\");
        for(String s :split){
            if (s.length()!=0){
                fin.append("\"");
                fin.append(s+"\\");
                fin.append("\",\\");
            }
        }
        //fin.delete(());
        fin.deleteCharAt(fin.length()-1);
        fin.append(" \\");
        String s ="[\"Injuries are part of everyday life\",\" from a scratch on the skin to a broken bone to a fatal trauma\",\" \",\"Although many injuries are accidental\",\" others can arise as a consequence of an individual\",\"s or a group\",\"s behaviour\",\" activity or social norms\",\" \",\" characteristics that tell us about societies and the inherent tensions and risks within and between different groups\",\"On page \",\" Beier et al\",\" provide evidence that challenges the long\",\"standing view\",\" that Neanderthal populations experienced a level of traumatic injuries that was significantly higher than that of humans\",\" The result calls into question claims that the behaviour and technologies \",\"of Neanderthals exposed them to particularly high levels of risk and danger\",\"Reports of injuries and deaths are constantly in the news\",\" As well as being drawn to read the stories of individuals\",\" such information is of interest because of what it tells us about our societies\",\" However\",\" to fully understand what might determine the current degree of violence and injuries\",\" \",\"we also need to look back at the past and identify the causal under pinnings\",\" But how far back should we look\",\" \",\"Arguably\",\" right back to the evolutionary origins of processes that shape behavioural\",\" social and cognitive tendencies and abilities\",\" \",\"Anthropologists study skeletal remains to reconstruct aspects of ancient lives\",\" building an \",\"osteo\",\"biography\",\" that casts light on part of the life history of an individual\",\" Skeletons preserve \",\" in the form of holes\",\" misshapen surfaces\",\" bone misalignments and secondary fractures\",\" radiating out from a point of impact \",\" a signature of the traumas that resulted in fractured\",\" cut or perforated bones\",\" even if the injuries subsequently healed\",\" \",\"Traumatic lesions have been frequently identified in Neanderthal fossils\",\" particularly in the head and neck\",\" leading to the view that higher levels of skeletal injury occurred in Neanderthal populations than in human populations\",\" However\",\" this is not so\",\" say Beier and colleagues\",\" The authors assessed published descriptions of Neanderthal and modern human fossil skulls\",\" found in Eurasia from approximately \",\" to \",\" years ago\",\"Comparing the number of injured and non\",\"injured Neanderthal and human skulls\",\" the authors report similar levels of head trauma in both groups\",\" \",\"The power of Beier and colleagues\",\" analyses lies in their study design\",\" Instead of comparing Neanderthal data with those of more\",\"recent or living human populations\",\" as previous studies have done\",\" the authors based their comparisons on humans \",\"who not only shared aspects of their environment with Neanderthals\",\" but whose fossil record also has a similar level of preservation\",\" Beier et al\",\" analyzed data for \",\" Neanderthal skulls and \",\" human skulls\",\" They gathered the data for \",\" skull bones\",\" \",\"and obtained information that ranged from \",\" bone in poorly preserved fossils to data for all \",\" bones\",\"per individual for well\",\"preserved ones\",\" In total\",\" the authors recorded trauma incidence in \",\" Neanderthal bones and \",\" human bones\",\"They also collected other information\",\" such as the percentage of each of the \",\" bones that was preserved for each individual\",\" \",\"as well as details including sex\",\" age at death and the fossil\",\"s geographic location\",\" \",\"Beier et al\",\" ran two sets of statistical analyzes \",\" one based on the presence or absence of trauma in each of the skull bones\",\" the other on individual fossil skulls as a whole\",\" \",\" to test whether there were any statistically significant differences between the prevalence of trauma in the Neanderthal and human fossils\",\" The authors also assessed whether trauma prevalence was linked to sex or age\",\" taking into account fossil preservation\",\" geographic location and possible inter action effects \",\"between the different variables\",\" The two analyses gave similar results\",\" \",\"The more complete the fossils are\",\" the more likely they are to have preserved evidence of injuries\",\" This might seem obvious\",\" but is an issue often ignored in such studies\",\" Beier et al\",\" offer a way to deal with this type of bias in the available material\",\" Once the authors take into account the extent of fossil preservation\",\" \",\"the predicted prevalence of trauma in Neanderthals and humans is almost the same\",\" \",\"Both Neanderthal and human males had a much greater incidence of trauma than did the females of their respective species\",\" \",\"This pattern remains the same for humans today\",\" One final intriguing result is that\",\" although traumatic injuries were present across all of the age ranges studied\",\" Neanderthals that had trauma to the head were more likely to have died under the age of \",\" than the humans were\",\" The authors interpret this result as evidence that\",\" compared with humans\",\" Neanderthals either had more injuries\",\" when they were young or were more likely to have died after being injured\",\"   \",\"Beier and colleagues\",\" study does not invalidate previous estimates of trauma among Neanderthals\",\" Instead\",\" it provides a new framework for interpreting these data by showing that the level of Neanderthal trauma was not uniquely\",\" high relative to that of early humans in Eurasia\",\" \",\"This implies that Neanderthal trauma does not require its own special explanations\",\" and that risk and danger were as much a part of the life of Neanderthals \",\"as they were of our own evolutionary past\",\" The result adds to growing evidence that Neanderthals had much in common with early human groups\",\" However\",\" the finding that Neanderthals might have experienced trauma at a younger age than humans\",\" or that they had a greater risk of death after injury\",\" is fascinating\",\" and might be a key insight into why our species had such a demographic advantage over Neanderthals\",\"Is this the final word on the subject of Neanderthal trauma\",\" The answer is no\",\" Beier and colleagues assessed only skull trauma\",\" \",\"What if Neanderthals accumulated more injuries to their bodies than did humans\",\" There are data suggesting that this might be the case\",\" \",\"Furthermore\",\" although the authors\",\" analyzes demonstrate the power of a well\",\"designed study based on large samples\",\" \",\"the data they used were recorded by many researchers and at varying levels of detail\",\" raising the possibility of methodological biases\",\" \",\"Lastly\",\" the causes of the injuries could provide some elusive insights into behaviour\",\" activities or social norms in the past\",\" From the shape\",\" location and extent of traumatic injuries in skeletons\",\" and characteristics such as the sharpness of fracture edges or the degree to which injuries had healed\",\" it is sometimes possible to establish the most likely cause of a trauma\",\" \",\" for example\",\" whether the injury probably arose as a consequence of a hunting accident\",\" \",\"interpersonal violence or inter\",\"group conflict\",\" Moreover\",\" surviving severe trauma might indicate that the injured person was cared for by members of their society\",\" Establishing the likelihood of each of these scenarios among Neanderthals and early modern humans\",\" will no doubt continue to challenge scientists for many years to come\",]\n";
        //fin.append("]");
        fin.append("\"[unk]\\\"]");
        return fin.toString();
         */
        return sAll;
    }

}
