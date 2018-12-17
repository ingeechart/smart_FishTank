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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.Configuration;
import cloud.artik.client.auth.OAuth;
import cloud.artik.model.DevicesEnvelope;
import cloud.artik.model.Message;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.NormalizedMessagesEnvelope;
import cloud.artik.model.OutputRule;
import cloud.artik.model.RulesEnvelope;
import cloud.artik.model.UserEnvelope;

import static cloud.artik.example.hellocloud.Config.DEVICE_ID;
import static cloud.artik.example.hellocloud.Config.DID_KAKAO;
import static cloud.artik.example.hellocloud.Config.LED_INGEE;

public class MessageActivity extends Activity {
    private static final String TAG = "MessageActivity";

    private UsersApi mUsersApi = null;
    private MessagesApi mMessagesApi = null;
    
    private String mAccessToken;
    private TextView mWelcome;
    private TextView mSendResponse;
    private TextView mGetLatestResponseId;
    private TextView mGetLatestResponseData;

    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        AuthStateDAL authStateDAL = new AuthStateDAL(this);
        mAccessToken = authStateDAL.readAuthState().getAccessToken();
        Log.v(TAG, "::onCreate get access token = " + mAccessToken);

        Button sendMsgBtn = (Button)findViewById(R.id.send_btn);
        Button getLatestMsgBtn = (Button)findViewById(R.id.getlatest_btn);
        mWelcome = (TextView)findViewById(R.id.welcome);
        mSendResponse = (TextView)findViewById(R.id.sendmsg_response);
        mGetLatestResponseId = (TextView)findViewById(R.id.getlatest_response_mid);
        mGetLatestResponseData = (TextView)findViewById(R.id.getlatest_response_mdata);
        
        setupArtikCloudApi();
        getUserInfo();

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, ": send button is clicked.");

                // Reset UI
                mSendResponse.setText("Response:");

                postMsg();
            }
        });

        getLatestMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, ": get latest message button is clicked.");

                // Reset UI
                mGetLatestResponseId.setText("id:");
                mGetLatestResponseData.setText("data:");

                // Now get the message
                getLatestMsg();
            }
        });

        Button cRulesBtn = (Button) findViewById(R.id.change_rules_btn);
        cRulesBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                getRules();

                //Intent intent = new Intent(this,RuleActivity.class);;
            }
        });
    }

    private void setupArtikCloudApi() {
        ApiClient mApiClient = new ApiClient();
        mApiClient.setAccessToken(mAccessToken);

        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
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

    private void getLatestMsg() {
        final String tag = TAG + " getLastNormalizedMessagesAsync";
        try {
            int messageCount = 1;
            //mMessagesApi.getLastNormalizedMessagesAsync(messageCount, DEVICE_ID, null,
              mMessagesApi.getLastNormalizedMessagesAsync(messageCount, LED_INGEE, null,
                new ApiCallback<NormalizedMessagesEnvelope>() {
                        @Override
                        public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(NormalizedMessagesEnvelope result, int i, Map<String, List<String>> stringListMap) {
                            //Log.v(tag, " onSuccess latestMessage = " + result.getData().toString());
                            String mid = "";
                            String data = "";
                            if (!result.getData().isEmpty()) {
                                mid = result.getData().get(0).getMid();
                                // 이 부분이 데이터 키,value 값 가져오는 부분인듯
                                // 여기서 아에 제이슨 파싱해도 좋을 것 같다.
                                data = result.getData().get(0).getData().toString();
                            }
                            updateGetResponseOnUIThread(mid, data);
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
        msg.setSdid(Config.DEVICE_ID);
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

    private void updateGetResponseOnUIThread(final String mid, final String msgData) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGetLatestResponseId.setText("id:" + mid);
                mGetLatestResponseData.setText("data:" + msgData);
            }
        });
    }

    private void updateSendResponseOnUIThread(final String response) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendResponse.setText("Response: " + response);
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
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        OAuth artikcloud_oauth = (OAuth) defaultClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken("mAccessToken");


        new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] objects) {
                String rUserId = userId; // String | User ID.
                Boolean excludeDisabled = true; // Boolean | Exclude disabled rules in the result.
                Integer count = 10; // Integer | Desired count of items in the result set.
                Integer offset = 0; // Integer | Offset for pagination.
                try {
                    RulesEnvelope result = mUsersApi.getUserRules(rUserId, excludeDisabled, count, offset);
                    handleGetRulesSuccessOnUIThread(result);
                    Intent intent = new Intent(getApplicationContext(),RuleActivity.class);
                    intent.putExtra("data", String.valueOf(result));
                    //Log.d("hng",String.valueOf(result));
                    startActivity(intent);

                    System.out.println(result);
                } catch (ApiException e) {
                    System.err.println("Exception when calling UsersApi#getUserRules");
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

} //MessageActivity

