/*
 * Copyright (C) 2017 Samsung Electronics Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.artik.example.hellocloud;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Call;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cloud.artik.api.DevicesApi;
import cloud.artik.api.DevicesStatusApi;
import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.Action;
import cloud.artik.model.ActionArray;
import cloud.artik.model.Actions;
import cloud.artik.model.DeviceStatus;
import cloud.artik.model.DeviceStatusBatch;
import cloud.artik.model.DeviceStatusData;
import cloud.artik.model.Message;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.NormalizedMessagesEnvelope;
import cloud.artik.model.OutputRule;
import cloud.artik.model.RulesEnvelope;
import cloud.artik.model.UserEnvelope;

import static cloud.artik.example.hellocloud.Config.ACTION_ID;
import static cloud.artik.example.hellocloud.Config.SENSOR_ID;

public class MessageActivity extends Activity {
    private static final String TAG = "MessageActivity";

    private UsersApi mUsersApi = null;
    private MessagesApi mMessagesApi = null;
    private DevicesStatusApi mDevicesStatusApi = null;

    private String mAccessToken;
    private TextView mWelcome;
    private TextView mGetTemp;
    private TextView mGetNtu;
    private TextView mGetLat;

    private ImageButton ledonIbtn;
    private ImageButton ledoffIbtn;
    private ImageButton feedIbtn;




    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        AuthStateDAL authStateDAL = new AuthStateDAL(this);
        mAccessToken = authStateDAL.readAuthState().getAccessToken();
        Log.v(TAG, "::onCreate get access token = " + mAccessToken);

        //Button sendMsgBtn = (Button)findViewById(R.id.send_btn);
        Button getLatestMsgBtn = (Button)findViewById(R.id.getlatest_btn);
        ImageButton ledonIbtn = (ImageButton)findViewById(R.id.ledOn_button);
        ImageButton ledoffIbtn = (ImageButton)findViewById(R.id.ledOff_button);
        ImageButton feedIbtn = (ImageButton)findViewById(R.id.feed_button);
        mWelcome = (TextView)findViewById(R.id.welcome);
        mGetTemp = (TextView)findViewById(R.id.displayTemp);
        mGetNtu = (TextView)findViewById(R.id.displayNtu);
        mGetLat = (TextView)findViewById(R.id.displayLat);

        
        setupArtikCloudApi();
        getUserInfo();

//        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                Log.v(TAG, ": send button is clicked.");
//
//                // Reset UI
//                postMsg();
//            }
//        });

        getLatestMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, ": get latest message button is clicked.");

                // Reset UI
                mGetTemp.setText("temp:");
                mGetNtu.setText("ntu:");
                mGetLat.setText("lat:");

                // Now get the message
                //getLatestMsg();
                getDeviceStatus();
            }
        });

        Button cRulesBtn = (Button) findViewById(R.id.change_rules_btn);
        cRulesBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                getRules();

                //Intent intent = new Intent(this,RuleActivity.class);;
            }
        });

        ledonIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, ": ledOn button is clicked.");
                sndAction("ledOn");
            }
        });
        ledoffIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, ": ledOff button is clicked.");
                sndAction("ledOff");
            }
        });
        feedIbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, ": feed button is clicked.");
                //sndAction("Feeder");
                sndAction("Feeder");
            }
        });
    }

    private void setupArtikCloudApi() {
        ApiClient mApiClient = new ApiClient();
        mApiClient.setAccessToken(mAccessToken);
        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
        mDevicesStatusApi = new DevicesStatusApi(mApiClient);
    }

    private void getUserInfo()
    {
        final String tag = TAG + " getSelfAsync";
        try {
            mUsersApi.getSelfAsync(new ApiCallback<UserEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int statusCode, Map<String, List<String>> map) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(UserEnvelope result, int statusCode, Map<String, List<String>> map) {
                    Log.v(TAG, "getSelfAsync::setupArtikCloudApi self name = " + result.getData().getFullName());
                    updateWelcomeViewOnUIThread("Welcome " + result.getData().getFullName());
                    userId = result.getData().getId();
                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }

    private void getDeviceStatus(){
        final String tag = TAG + " getDevicesStatus";

        try{
            mDevicesStatusApi.getDevicesStatusAsync(SENSOR_ID, true, true,
                    new ApiCallback<DeviceStatusBatch>() {
                        @Override
                        public void onFailure(ApiException exc, int statusCode, Map<String, List<String>> responseHeaders) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(DeviceStatusBatch result, int statusCode, Map<String, List<String>> responseHeaders) {
                            String temp="none"; // 온
                            String lat="none"; //조도
                            String ntu="none"; // 탁도
                            if (!result.getData().isEmpty()) {
                                Log.d("hng_did",result.getData().get(0).getDid());
                                Object status = result.getData().get(0).getData().getSnapshot();
                                Gson gson = new Gson();
                                String json = gson.toJson(status);
                                try {
                                    JSONObject jsonObject = new JSONObject(json);
                                    lat = jsonObject.getJSONObject("lat").getString("value");
                                    ntu = jsonObject.getJSONObject("long").getString("value");
                                    temp = jsonObject.getJSONObject("temp").getString("value");
                                    Log.d("hng_lat",lat+"  "+ntu+"  "+temp);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            updateGetResponseOnUIThread(lat,ntu,temp);
                        }

                        @Override
                        public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                        }

                        @Override
                        public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                        }
                    });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }
    private void sndAction(String status){
        final String tag = TAG + " sendMessageActionAsync";

        Actions data = new Actions();
        data=setAction(status);
        try{
            mMessagesApi.sendActionsAsync(data, new ApiCallback<MessageIDEnvelope>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    processFailure(tag,e);
                    Log.d("hng",e.toString());
                }

                @Override
                public void onSuccess(MessageIDEnvelope result, int statusCode, Map<String, List<String>> responseHeaders) {

                }

                @Override
                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                }

                @Override
                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }


    }
    private void getLatestMsg() {
        final String tag = TAG + " getLastNormalizedMessagesAsync";
        try {
            int messageCount = 10;
            //mMessagesApi.getLastNormalizedMessagesAsync(messageCount, DEVICE_ID, null,
              mMessagesApi.getLastNormalizedMessagesAsync(messageCount, SENSOR_ID, null,
                new ApiCallback<NormalizedMessagesEnvelope>() {
                        @Override
                        public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(NormalizedMessagesEnvelope result, int i, Map<String, List<String>> stringListMap) {
                            //Log.v(tag, " onSuccess latestMessage = " + result.getData().toString());
                            String mid = "";
                            String fullData = "";
                            String data ="";
                            if (!result.getData().isEmpty()) {
                                mid = result.getData().get(0).getMid();
                                // 이 부분이 데이터 키,value 값 가져오는 부분인듯
                                // 여기서 아에 제이슨 파싱해도 좋을 것 같다.
                                fullData = result.getData().toString();
                                data = result.getData().get(0).getData().toString();

                            }
                            updateGetResponseOnUIThread(mid, data,fullData);
                        }

                        @Override
                        public void onUploadProgress(long bytes, long contentLen, boolean done) {
                        }

                        @Override
                        public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                        }
                    });

        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }


    private void postMsg() {
        final String tag = TAG + " sendMessageActionAsync";

        Message msg = new Message();
        msg.setSdid(Config.ACTION_ID);
        msg.getData().put("stepCount", 4393);
        msg.getData().put("heartRate", 110);
        msg.getData().put("description", "Run");
        msg.getData().put("activity", 2);
//          msg.setSdid(Config.DID_KAKAO);
//          msg.getData().put("hello",1);
        // msg.setSdid(config.어느디바이스로 보낼지)
        //msg.getData().put("key", value);


        try {
            mMessagesApi.sendMessageAsync(msg, new ApiCallback<MessageIDEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(MessageIDEnvelope result, int i, Map<String, List<String>> stringListMap) {
                   // Log.v(tag, " onSuccess response to sending message = " + result.getData().toString());
                    updateSendResponseOnUIThread(result.getData().toString());
                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }



    static void showErrorOnUIThread(final String text, final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(activity.getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    private void updateWelcomeViewOnUIThread(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWelcome.setText(text);
            }
        });
    }

    private void updateGetResponseOnUIThread(final String lat, final String ntu , final String temp) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGetTemp.setText("temp : " + temp);
                mGetNtu.setText("ntu : " + ntu);
                mGetLat.setText("lat : " + lat);



                //Log.d("hng",msgFullData);
            }
        });
    }

    private void updateSendResponseOnUIThread(final String response) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mGetTemp.setText("Response: " + response);
            }
        });
    }

    private void processFailure(final String context, ApiException exc) {
        String errorDetail = " onFailure with exception" + exc;
        Log.w(context, errorDetail);
        exc.printStackTrace();
        showErrorOnUIThread(context+errorDetail, MessageActivity.this);
    }

    private void getRules(){
//        ApiClient defaultClient = Configuration.getDefaultApiClient();

//        OAuth artikcloud_oauth = (OAuth) defaultClient.getAuthentication("artikcloud_oauth");
//        artikcloud_oauth.setAccessToken("mAccessToken");


        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                String rUserId = userId; // String | User ID.
                Boolean excludeDisabled = true; // Boolean | Exclude disabled rules in the result.
                Integer count = 100; // Integer | Desired count of items in the result set.
                Integer offset = 0; // Integer | Offset for pagination.
                String owner = "owner_example";
                try {
                    RulesEnvelope result = mUsersApi.getUserRules(rUserId, excludeDisabled, count, offset,owner);
                    handleGetRulesSuccessOnUIThread(result);
                    Intent intent = new Intent(getApplicationContext(),RuleActivity.class);
                    intent.putExtra("data", String.valueOf(result));
                    Log.d("hng",String.valueOf(result));
                    startActivity(intent);

                    System.out.println(result);
                } catch (ApiException e) {
                    System.err.println("Exception when calling UsersApi#getUserRules");
                    Log.d("hng",e.toString());
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    private void handleGetRulesSuccessOnUIThread(final RulesEnvelope rules) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RuleManager rM =new RuleManager();
                String displayInfo = rules.toString();
                int receivedRules = rules.getCount();
                rM.numOfRules = receivedRules;
                rM.mRuleIdx = 0;
                if (receivedRules <= 0) {
                    rM.mRuleIds = null;
                } else {
                    List<OutputRule> rulesObj = rules.getData();
                    int count = receivedRules > 2? 2 : receivedRules;
                    rM.mRuleIds = new String[2];
                    displayInfo = "total:" + rules.getTotal() + ", count:" + receivedRules +'\n';

                    for (int i = 0; i < count; i++) {
                        OutputRule thisRule = rulesObj.get(i);
                        rM.mRuleIds[i] = thisRule.getId();
                        displayInfo += "id:" + rM.mRuleIds[i] + '\n';
                        displayInfo += "description:" + thisRule.getDescription() +'\n';
                    }
                }
                //displayRulesApiCallResponse("Rules:\n" + displayInfo);

            }
        });
    }
    Actions setAction(String status){
        Actions data = new Actions();

        ActionArray aArray = new ActionArray();
        Action aAction = new Action();
        aAction.setName(status);
        aArray.addActionsItem(aAction);
        data.setDdid(ACTION_ID);
        data.setData(aArray);
//        Log.d("hng_aArrat",data.toString());
//        Log.d("hng_aArrat",aArray.toString());
//        Log.d("hng_aArrat",aAction.toString());
        return data;
    }

} //MessageActivity

