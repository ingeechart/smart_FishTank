package cloud.artik.example.hellocloud;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.RulesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.client.ApiResponse;
import cloud.artik.client.Configuration;
import cloud.artik.client.Pair;
import cloud.artik.client.ProgressRequestBody;
import cloud.artik.client.ProgressResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import cloud.artik.client.auth.OAuth;
import cloud.artik.model.RuleCreationInfo;
import cloud.artik.model.RuleEnvelope;
import cloud.artik.model.RuleUpdateInfo;
import cloud.artik.model.UserEnvelope;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class RuleActivity extends AppCompatActivity {

    private UsersApi mUsersApi = null;
    //private MessagesApi mMessagesApi = null;
    private String mAccessToken;

    private TextView mRulesAPICallResponse;
    private EditText edValue;
    private Button mCreateRuleBtn;
    private Button mDeleteRuleBtn;
    private Button mGetRulesBtn;
    private Button mUpdateRulesBtn;
    private String[] mRuleIds = null;
    private int mRuleIdx = 0; // The index of the current rule in mRuleIds[]
    //++ simple name
    private static final String TAG = RuleActivity.class.getSimpleName();
    private RulesApi apiInstance;
    private String ruleId = "8ec8842c5c934315aeeca52b4f4a962f";
    public int modifiedValue=0;

    public Map<String,Object> oldRule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);

        edValue = (EditText) findViewById(R.id.modfiedValue);


        AuthStateDAL authStateDAL = new AuthStateDAL(this);
        mAccessToken = authStateDAL.readAuthState().getAccessToken();

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setAccessToken(mAccessToken);
        apiInstance = new RulesApi();
        OAuth artikcloud_oauth = (OAuth) defaultClient.getAuthentication("artikcloud_oauth");
        artikcloud_oauth.setAccessToken(mAccessToken);




        //Buttons
        mGetRulesBtn = (Button)findViewById(R.id.getRulesBtn);
        mGetRulesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": get a rule button is clicked.");
                    mRuleIdx = 0; //reset the index, starting from zero
                    modifiedValue=Integer.parseInt(edValue.getText().toString());
                    getRules(ruleId);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });

        mCreateRuleBtn = (Button)findViewById(R.id.createRuleBtn);
        mCreateRuleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": create a rule button is clicked.");
                    mRuleIdx = 0; //reset the index, starting from zero
                    createRules(ruleId);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });



        mUpdateRulesBtn = (Button)findViewById(R.id.updateRuleBtn);
        mUpdateRulesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": create a rule button is clicked.");
                    mRuleIdx = 0; //reset the index, starting from zero
                    getRules(ruleId);
                    modifiedValue=Integer.parseInt(edValue.getText().toString());
                    updateRules(ruleId);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });

        mDeleteRuleBtn = (Button)findViewById(R.id.deleteRuleBtn);
        mDeleteRuleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": create a rule button is clicked.");
                    mRuleIdx = 0; //reset the index, starting from zero
                    createRules(ruleId);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });

    }

    public void createRules(String userId) {
        RuleCreationInfo ruleInfo = new RuleCreationInfo(); // RuleCreationInfo | Rule object that needs to be added
        try {
            RuleEnvelope result = apiInstance.createRule(ruleInfo, userId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RulesApi#createRule");
            e.printStackTrace();
        }
    }

    public void deleteRules(String ruleId) {
        try {
            RuleEnvelope result = apiInstance.deleteRule(ruleId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RulesApi#deleteRule");
            e.printStackTrace();
        }

    }

    public void getRules(String ruleId) {
        try {
            AsyncTask<String, Void, RuleEnvelope> asyncTask = new AsyncTask<String, Void, RuleEnvelope>() {
                @Override
                protected RuleEnvelope doInBackground(String... strings) {
                    String ruleId = strings[0];
                    try {
                        RuleEnvelope result = apiInstance.getRule(ruleId);
                        parcingRules(result);
                        //Log.d("hng",String.valueOf(result));
                        return result;
                    } catch (ApiException e) {
                        System.err.println("Exception when calling RulesApi#getRule");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };

            RuleEnvelope response = asyncTask.execute(ruleId).get();
            System.out.println(response);
        }
        catch (Exception e) {
            // No error
        }
    }

    public void updateRules(String ruleId) throws ExecutionException, InterruptedException {

        AsyncTask<String, Void, RuleEnvelope> asyncTask = new AsyncTask<String, Void, RuleEnvelope>() {
            @Override
            protected RuleEnvelope doInBackground(String... strings) {
                String ruleId = strings[0];
                RuleUpdateInfo ruleInfo = new RuleUpdateInfo();
                ruleInfo.name("example2");
                ruleInfo.setRule(oldRule);
                Log.d("hng",ruleInfo.toString());
                try {
                    RuleEnvelope result = apiInstance.updateRule(ruleId, ruleInfo);
                    System.out.println(result);
                } catch (ApiException e) {
                    System.err.println("Exception when calling RulesApi#updateRule");
                    e.printStackTrace();
                    Log.d("hng",e.getResponseBody());
                }

                return null;
            }
        };
        RuleEnvelope response = asyncTask.execute(ruleId).get();
        System.out.println(response);

    }

    // modify rule value! part
    public void parcingRules(RuleEnvelope rule) throws JSONException {
         String data = rule.getData().getName();
         oldRule = rule.getData().getRule();// 여기서 object 가 string type 인것 같다.

        //json parsing
         Object first = oldRule.get("if");
         Gson gson = new Gson();
         String json = gson.toJson(first);
         JSONObject jsonObj = new JSONObject(json);
         JSONArray jarray = jsonObj.getJSONArray("and");
         JSONObject reader = jarray.getJSONObject(0);
         JSONObject operand  = reader.getJSONObject("operand");
         //이부분을 입력값이 들어가게 만들어야함
         operand.put("value",modifiedValue);
         //json update
         reader.put("operand",operand);
         jarray.put(0,reader);
         jsonObj.put("and",jarray);
         json = jsonObj.toString();
         first = gson.fromJson(json,first.getClass());
         oldRule.put("if",first);

            Log.d("hng",data);
         rule.getData().setName("example2");

    }

}
