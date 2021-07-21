// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.vosk.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.*;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.SpeechStreamService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VoskActivity extends Activity implements
        RecognitionListener {
    private final static String TAG = "VoskActivity";

    static private final int STATE_START = 0;
    static private final int STATE_READY = 1;
    static private final int STATE_DONE = 2;
    static private final int STATE_FILE = 3;
    static private final int STATE_MIC = 4;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private SpeechService speechService;
    private SpeechStreamService speechStreamService;
    private TextView resultView;
    private StringBuilder stringBuilder = new StringBuilder();

    JSONObject jsonObject;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);

        // Setup layout
        resultView = findViewById(R.id.result_text);
        setUiState(STATE_START);

        findViewById(R.id.recognize_file).setOnClickListener(view -> recognizeFile());
        findViewById(R.id.recognize_mic).setOnClickListener(view -> recognizeMicrophone());
        ((ToggleButton) findViewById(R.id.pause)).setOnCheckedChangeListener((view, isChecked) -> pause(isChecked));

        LibVosk.setLogLevel(LogLevel.INFO);

        // Check if user has given permission to record audio, init the model after permission is granted
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
    }

    private void initModel() {
        StorageService.unpack(this, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    setUiState(STATE_READY);
                },
                (exception) -> setErrorState("Failed to unpack the model" + exception.getMessage()));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                initModel();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }

        if (speechStreamService != null) {
            speechStreamService.stop();
        }
    }

    @Override
    public void onResult(String hypothesis) {
        //resultView.append(hypothesis + "\n");
        String text = "";
        try {
            jsonObject = new JSONObject(hypothesis);
            text = (String) jsonObject.get("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d(TAG,"onResult方法被调用");
        stringBuilder.append(text);
    }

    @Override
    public void onFinalResult(String hypothesis) {

        Log.d(TAG,"onFinalResult方法被调用");
        //resultView.append(hypothesis + "\n");
        //stringBuilder.append(hypothesis);
        resultView.append(stringBuilder.toString());
        //Log.d(TAG,stringBuilder.toString());

        setUiState(STATE_DONE);
        if (speechStreamService != null) {
            speechStreamService = null;
        }
        check();
    }

    @Override
    public void onPartialResult(String hypothesis) {
        Log.d(TAG,"onPartialResult方法被调用");
        //resultView.append(hypothesis + "\n");
    }

    @Override
    public void onError(Exception e) {
        setErrorState(e.getMessage());
    }

    @Override
    public void onTimeout() {
        setUiState(STATE_DONE);
    }

    private void setUiState(int state) {
        switch (state) {
            case STATE_START:
                resultView.setText(R.string.preparing);
                resultView.setMovementMethod(new ScrollingMovementMethod());
                findViewById(R.id.recognize_file).setEnabled(false);
                findViewById(R.id.recognize_mic).setEnabled(false);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_READY:
                resultView.setText(R.string.ready);
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
                findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_DONE:
                ((Button) findViewById(R.id.recognize_file)).setText(R.string.recognize_file);
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
                findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_FILE:
                ((Button) findViewById(R.id.recognize_file)).setText(R.string.stop_file);
                resultView.setText(getString(R.string.starting));
                findViewById(R.id.recognize_mic).setEnabled(false);
                findViewById(R.id.recognize_file).setEnabled(true);
                findViewById(R.id.pause).setEnabled((false));
                break;
            case STATE_MIC:
                ((Button) findViewById(R.id.recognize_mic)).setText(R.string.stop_microphone);
                resultView.setText(getString(R.string.say_something));
                findViewById(R.id.recognize_file).setEnabled(false);
                findViewById(R.id.recognize_mic).setEnabled(true);
                findViewById(R.id.pause).setEnabled((true));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    private void setErrorState(String message) {
        resultView.setText(message);
        ((Button) findViewById(R.id.recognize_mic)).setText(R.string.recognize_microphone);
        findViewById(R.id.recognize_file).setEnabled(false);
        findViewById(R.id.recognize_mic).setEnabled(false);
    }

    private void recognizeFile() {
        if (speechStreamService != null) {
            setUiState(STATE_DONE);
            speechStreamService.stop();
            speechStreamService = null;
        } else {
            setUiState(STATE_FILE);
            try {
                /*
                FileReader fr=new FileReader("src/main/assets/A00001.txt");

                char[]cs=new char[1024];//存储读取到的多个字符
                int len=0;
                StringBuilder str = new StringBuilder();
                while((len=fr.read(cs))!=-1){
                    str.append(new String(cs, 0, len));
                }
                String s = str.toString();
                fr.close();

                 */
                String s = "[\"Injuries are part of everyday life\",\" from a scratch on the skin to a broken bone to a fatal trauma\",\" \",\"Although many injuries are accidental\",\" others can arise as a consequence of an individual\",\"s or a group\",\"s behaviour\",\" activity or social norms\",\" \",\" characteristics that tell us about societies and the inherent tensions and risks within and between different groups\",\"On page \",\" Beier et al\",\" provide evidence that challenges the long\",\"standing view\",\" that Neanderthal populations experienced a level of traumatic injuries that was significantly higher than that of humans\",\" The result calls into question claims that the behaviour and technologies \",\"of Neanderthals exposed them to particularly high levels of risk and danger\",\"Reports of injuries and deaths are constantly in the news\",\" As well as being drawn to read the stories of individuals\",\" such information is of interest because of what it tells us about our societies\",\" However\",\" to fully understand what might determine the current degree of violence and injuries\",\" \",\"we also need to look back at the past and identify the causal under pinnings\",\" But how far back should we look\",\" \",\"Arguably\",\" right back to the evolutionary origins of processes that shape behavioural\",\" social and cognitive tendencies and abilities\",\" \",\"Anthropologists study skeletal remains to reconstruct aspects of ancient lives\",\" building an \",\"osteo\",\"biography\",\" that casts light on part of the life history of an individual\",\" Skeletons preserve \",\" in the form of holes\",\" misshapen surfaces\",\" bone misalignments and secondary fractures\",\" radiating out from a point of impact \",\" a signature of the traumas that resulted in fractured\",\" cut or perforated bones\",\" even if the injuries subsequently healed\",\" \",\"Traumatic lesions have been frequently identified in Neanderthal fossils\",\" particularly in the head and neck\",\" leading to the view that higher levels of skeletal injury occurred in Neanderthal populations than in human populations\",\" However\",\" this is not so\",\" say Beier and colleagues\",\" The authors assessed published descriptions of Neanderthal and modern human fossil skulls\",\" found in Eurasia from approximately \",\" to \",\" years ago\",\"Comparing the number of injured and non\",\"injured Neanderthal and human skulls\",\" the authors report similar levels of head trauma in both groups\",\" \",\"The power of Beier and colleagues\",\" analyses lies in their study design\",\" Instead of comparing Neanderthal data with those of more\",\"recent or living human populations\",\" as previous studies have done\",\" the authors based their comparisons on humans \",\"who not only shared aspects of their environment with Neanderthals\",\" but whose fossil record also has a similar level of preservation\",\" Beier et al\",\" analyzed data for \",\" Neanderthal skulls and \",\" human skulls\",\" They gathered the data for \",\" skull bones\",\" \",\"and obtained information that ranged from \",\" bone in poorly preserved fossils to data for all \",\" bones\",\"per individual for well\",\"preserved ones\",\" In total\",\" the authors recorded trauma incidence in \",\" Neanderthal bones and \",\" human bones\",\"They also collected other information\",\" such as the percentage of each of the \",\" bones that was preserved for each individual\",\" \",\"as well as details including sex\",\" age at death and the fossil\",\"s geographic location\",\" \",\"Beier et al\",\" ran two sets of statistical analyzes \",\" one based on the presence or absence of trauma in each of the skull bones\",\" the other on individual fossil skulls as a whole\",\" \",\" to test whether there were any statistically significant differences between the prevalence of trauma in the Neanderthal and human fossils\",\" The authors also assessed whether trauma prevalence was linked to sex or age\",\" taking into account fossil preservation\",\" geographic location and possible inter action effects \",\"between the different variables\",\" The two analyses gave similar results\",\" \",\"The more complete the fossils are\",\" the more likely they are to have preserved evidence of injuries\",\" This might seem obvious\",\" but is an issue often ignored in such studies\",\" Beier et al\",\" offer a way to deal with this type of bias in the available material\",\" Once the authors take into account the extent of fossil preservation\",\" \",\"the predicted prevalence of trauma in Neanderthals and humans is almost the same\",\" \",\"Both Neanderthal and human males had a much greater incidence of trauma than did the females of their respective species\",\" \",\"This pattern remains the same for humans today\",\" One final intriguing result is that\",\" although traumatic injuries were present across all of the age ranges studied\",\" Neanderthals that had trauma to the head were more likely to have died under the age of \",\" than the humans were\",\" The authors interpret this result as evidence that\",\" compared with humans\",\" Neanderthals either had more injuries\",\" when they were young or were more likely to have died after being injured\",\"   \",\"Beier and colleagues\",\" study does not invalidate previous estimates of trauma among Neanderthals\",\" Instead\",\" it provides a new framework for interpreting these data by showing that the level of Neanderthal trauma was not uniquely\",\" high relative to that of early humans in Eurasia\",\" \",\"This implies that Neanderthal trauma does not require its own special explanations\",\" and that risk and danger were as much a part of the life of Neanderthals \",\"as they were of our own evolutionary past\",\" The result adds to growing evidence that Neanderthals had much in common with early human groups\",\" However\",\" the finding that Neanderthals might have experienced trauma at a younger age than humans\",\" or that they had a greater risk of death after injury\",\" is fascinating\",\" and might be a key insight into why our species had such a demographic advantage over Neanderthals\",\"Is this the final word on the subject of Neanderthal trauma\",\" The answer is no\",\" Beier and colleagues assessed only skull trauma\",\" \",\"What if Neanderthals accumulated more injuries to their bodies than did humans\",\" There are data suggesting that this might be the case\",\" \",\"Furthermore\",\" although the authors\",\" analyzes demonstrate the power of a well\",\"designed study based on large samples\",\" \",\"the data they used were recorded by many researchers and at varying levels of detail\",\" raising the possibility of methodological biases\",\" \",\"Lastly\",\" the causes of the injuries could provide some elusive insights into behaviour\",\" activities or social norms in the past\",\" From the shape\",\" location and extent of traumatic injuries in skeletons\",\" and characteristics such as the sharpness of fracture edges or the degree to which injuries had healed\",\" it is sometimes possible to establish the most likely cause of a trauma\",\" \",\" for example\",\" whether the injury probably arose as a consequence of a hunting accident\",\" \",\"interpersonal violence or inter\",\"group conflict\",\" Moreover\",\" surviving severe trauma might indicate that the injured person was cared for by members of their society\",\" Establishing the likelihood of each of these scenarios among Neanderthals and early modern humans\",\" will no doubt continue to challenge scientists for many years to come\", \"[unk]\"]";
                /*Recognizer rec = new Recognizer(model, 16000.f, "[\"one zero zero zero one\", " +
                        "\"oh zero one two three four five six seven eight nine\", \"[unk]\"]");*/

                Recognizer rec = new Recognizer(model, 128000.f, s);
                //InputStream ais = getAssets().open(
                //        "10001-90210-01803.wav");
                InputStream ais = getAssets().open(
                        "A00001.wav");
                if (ais.skip(44) != 44) throw new IOException("File too short");

                speechStreamService = new SpeechStreamService(rec, ais, 128000.f);
                speechStreamService.start(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
        //resultView.append(stringBuilder.toString());
    }

    private void recognizeMicrophone() {
        if (speechService != null) {
            setUiState(STATE_DONE);
            speechService.stop();
            speechService = null;
        } else {
            setUiState(STATE_MIC);
            try {
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                setErrorState(e.getMessage());
            }
        }
    }


    private void pause(boolean checked) {
        if (speechService != null) {
            speechService.setPause(checked);
        }
    }

    private List<String> check() {
        Log.d(TAG, "check方法被调用");
        String s = "Injuries are part of everyday life from a scratch on the skin to a broken bone to a fatal trauma Although many injuries are accidental others can arise as a consequence of an individuals or a groups behaviour activity or social norms  characteristics that tell us about societies and the inherent tensions and risks within and between different groupsOn page  Beier et al provide evidence that challenges the longstanding view that Neanderthal populations experienced a level of traumatic injuries that was significantly higher than that of humans The result calls into question claims that the behaviour and technologies of Neanderthals exposed them to particularly high levels of risk and dangerReports of injuries and deaths are constantly in the news As well as being drawn to read the stories of individuals such information is of interest because of what it tells us about our societies However to fully understand what might determine the current degree of violence and injuries we also need to look back at the past and identify the causal under pinnings But how far back should we look Arguably right back to the evolutionary origins of processes that shape behavioural social and cognitive tendencies and abilities Anthropologists study skeletal remains to reconstruct aspects of ancient lives building an osteobiography that casts light on part of the life history of an individual Skeletons preserve  in the form of holes misshapen surfaces bone misalignments and secondary fractures radiating out from a point of impact  a signature of the traumas that resulted in fractured cut or perforated bones even if the injuries subsequently healed Traumatic lesions have been frequently identified in Neanderthal fossils particularly in the head and neck leading to the view that higher levels of skeletal injury occurred in Neanderthal populations than in human populations However this is not so say Beier and colleagues The authors assessed published descriptions of Neanderthal and modern human fossil skulls found in Eurasia from approximately  to  years agoComparing the number of injured and noninjured Neanderthal and human skulls the authors report similar levels of head trauma in both groups The power of Beier and colleagues analyses lies in their study design Instead of comparing Neanderthal data with those of morerecent or living human populations as previous studies have done the authors based their comparisons on humans who not only shared aspects of their environment with Neanderthals but whose fossil record also has a similar level of preservation Beier et al analyzed data for  Neanderthal skulls and  human skulls They gathered the data for  skull bones and obtained information that ranged from  bone in poorly preserved fossils to data for all  bonesper individual for wellpreserved ones In total the authors recorded trauma incidence in  Neanderthal bones and  human bonesThey also collected other information such as the percentage of each of the  bones that was preserved for each individual as well as details including sex age at death and the fossils geographic location Beier et al ran two sets of statistical analyzes  one based on the presence or absence of trauma in each of the skull bones the other on individual fossil skulls as a whole  to test whether there were any statistically significant differences between the prevalence of trauma in the Neanderthal and human fossils The authors also assessed whether trauma prevalence was linked to sex or age taking into account fossil preservation geographic location and possible inter action effects between the different variables The two analyses gave similar results The more complete the fossils are the more likely they are to have preserved evidence of injuries This might seem obvious but is an issue often ignored in such studies Beier et al offer a way to deal with this type of bias in the available material Once the authors take into account the extent of fossil preservation the predicted prevalence of trauma in Neanderthals and humans is almost the same Both Neanderthal and human males had a much greater incidence of trauma than did the females of their respective species This pattern remains the same for humans today One final intriguing result is that although traumatic injuries were present across all of the age ranges studied Neanderthals that had trauma to the head were more likely to have died under the age of  than the humans were The authors interpret this result as evidence that compared with humans Neanderthals either had more injuries when they were young or were more likely to have died after being injured   Beier and colleagues study does not invalidate previous estimates of trauma among Neanderthals Instead it provides a new framework for interpreting these data by showing that the level of Neanderthal trauma was not uniquely high relative to that of early humans in Eurasia This implies that Neanderthal trauma does not require its own special explanations and that risk and danger were as much a part of the life of Neanderthals as they were of our own evolutionary past The result adds to growing evidence that Neanderthals had much in common with early human groups However the finding that Neanderthals might have experienced trauma at a younger age than humans or that they had a greater risk of death after injury is fascinating and might be a key insight into why our species had such a demographic advantage over NeanderthalsIs this the final word on the subject of Neanderthal trauma The answer is no Beier and colleagues assessed only skull trauma What if Neanderthals accumulated more injuries to their bodies than did humans There are data suggesting that this might be the case Furthermore although the authors analyzes demonstrate the power of a welldesigned study based on large samples the data they used were recorded by many researchers and at varying levels of detail raising the possibility of methodological biases Lastly the causes of the injuries could provide some elusive insights into behaviour activities or social norms in the past From the shape location and extent of traumatic injuries in skeletons and characteristics such as the sharpness of fracture edges or the degree to which injuries had healed it is sometimes possible to establish the most likely cause of a trauma  for example whether the injury probably arose as a consequence of a hunting accident interpersonal violence or intergroup conflict Moreover surviving severe trauma might indicate that the injured person was cared for by members of their society Establishing the likelihood of each of these scenarios among Neanderthals and early modern humans will no doubt continue to challenge scientists for many years to come";
        Lcs lcs = new Lcs(s, stringBuilder.toString());
        lcs.run();
        List<String> answerCommonList = lcs.getAnswerCommonList();
        for (String s1 : answerCommonList) {
            System.out.println(s1);
        }
        return answerCommonList;
    }

    private void show(Boolean[] res){

    }

}
