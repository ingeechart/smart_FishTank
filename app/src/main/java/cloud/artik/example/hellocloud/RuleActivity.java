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
    private TextView tvRuleName;
    private TextView tvRuleId;
    private TextView tvRuleStatus;
    private TextView tvRuleRule;
    private TextView tvRuleName2;
    private TextView tvRuleId2;
    private TextView tvRuleStatus2;
    private TextView tvRuleRule2;


    private EditText edValue;
    private EditText etRName;
    private Button mCreateRuleBtn;
    private Button mDeleteRuleBtn;
    private Button mGetRulesBtn;
    private Button mUpdateRulesBtn;
    private String[] mRuleIds = null;
    private int mRuleIdx = 0; // The index of the current rule in mRuleIds[]
    //++ simple name
    private static final String TAG = RuleActivity.class.getSimpleName();
    private RulesApi apiInstance;
    //private String ruleId = "8ec8842c5c934315aeeca52b4f4a962f";
    private String ruleIdFilterOn= "cc628a5b9c8648c7b11f6dffdb78beb8";
    private String ruleIdFilterOff= "2e9409d888e54db280c5bbd2377b21d9";
    private String ruleIdHitterOn = "1d13737a27a54bda828af2a38f443e24";
    private String ruleIdHitterOff= "5871aa54f3544459ad3a9a3244cb2270";

    public int modifiedValue=0;
    public String modRname="error";

    public Map<String,Object> oldRule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);

        edValue = (EditText) findViewById(R.id.modfiedValue);
        etRName =  (EditText) findViewById(R.id.modRname);
        tvRuleName= (TextView) findViewById(R.id.rule_name);
        tvRuleId= (TextView) findViewById(R.id.rule_id);
        tvRuleStatus= (TextView) findViewById(R.id.rule_status);
        tvRuleRule= (TextView) findViewById(R.id.rule_rule);
        tvRuleName2= (TextView) findViewById(R.id.rule_name2);
        tvRuleId2= (TextView) findViewById(R.id.rule_id2);
        tvRuleStatus2= (TextView) findViewById(R.id.rule_status2);
        tvRuleRule2= (TextView) findViewById(R.id.rule_rule2);

        tvRuleName.setText("name");
        tvRuleId.setText("ID");
        tvRuleStatus.setText("status");
        tvRuleRule.setText("rule");

        tvRuleName2.setText("name");
        tvRuleId2.setText("ID");
        tvRuleStatus2.setText("status");
        tvRuleRule2.setText("rule");

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
                modRname = etRName.getText().toString();

                try {
                    Log.v(TAG, ": get a rule button is clicked.");
                    mRuleIdx = 0; //reset the index, starting from zero
                    if(modRname.equals("ntu")){
                        getRules2(ruleIdFilterOn);
                        getRules2(ruleIdFilterOff);
                        Log.d("hng_rule",modRname +"done");

                    }else if(modRname.equals("temp")){
                        getRules2(ruleIdHitterOn);
                        getRules2(ruleIdHitterOff);
                        Log.d("hng_rule",modRname +"done");

                    }
                } catch (Exception e) {
                    Log.v("hng", "Run into Exception");
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
                    modifiedValue=Integer.parseInt(edValue.getText().toString());
                    modRname = etRName.getText().toString();
                    Log.d("hng_rule",modRname);

                    if(modRname.equals("ntu")){
                        getRules(ruleIdFilterOn);
                        updateRules(ruleIdFilterOn);
                        getRules(ruleIdFilterOff);
                        updateRules(ruleIdFilterOff);
                        Log.d("hng_rule",modRname +"done");

                    }else if(modRname.equals("temp")){
                        getRules(ruleIdHitterOn);
                        updateRules(ruleIdHitterOn);
                        getRules(ruleIdHitterOff);
                        updateRules(ruleIdHitterOff);
                        Log.d("hng_rule",modRname +"done");

                    }

                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                    Log.d("hng_rule",e.toString());

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

    public void getRules2(String ruleId) {
        try {
            AsyncTask<String, Void, RuleEnvelope> asyncTask = new AsyncTask<String, Void, RuleEnvelope>() {
                @Override
                protected RuleEnvelope doInBackground(String... strings) {
                    String ruleId = strings[0];
                    try {
                        RuleEnvelope result = apiInstance.getRule(ruleId);
                        if (ruleId.equals(ruleIdFilterOn) || ruleId.equals(ruleIdHitterOn)) {
                            updateGetResponseOnUIThread(result,true);
                        } else if (ruleId.equals(ruleIdFilterOff) || ruleId.equals(ruleIdHitterOff))
                            updateGetResponseOnUIThread(result,false);

                        return result;
                    } catch (ApiException e) {
                        System.err.println("Exception when calling RulesApi#getRule");
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
//                ruleInfo.name("example2");
                ruleInfo.setRule(oldRule);
                Log.d("hng",ruleInfo.toString());
                try {
                    RuleEnvelope result = apiInstance.updateRule(ruleId, ruleInfo);
                    System.out.println(result);
                    if (ruleId.equals(ruleIdFilterOn) || ruleId.equals(ruleIdHitterOn)) {
                        updateGetResponseOnUIThread(result,true);
                    } else if (ruleId.equals(ruleIdFilterOff) || ruleId.equals(ruleIdHitterOff))
                        updateGetResponseOnUIThread(result,false);


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
         Log.d("hng_rule",rule.toString());
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

    }

    private void updateGetResponseOnUIThread(final RuleEnvelope rule,final boolean on) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(on) {
                    tvRuleName.setText("name : " + rule.getData().getName());
                    tvRuleId.setText("ID : " + rule.getData().getId());
                    tvRuleStatus.setText("status : " + rule.getData().getEnabled());
                    tvRuleRule.setText("rule : " + rule.getData().getRule());
                }else{
                    tvRuleName2.setText("name : " + rule.getData().getName());
                    tvRuleId2.setText("ID : " + rule.getData().getId());
                    tvRuleStatus2.setText("status : " + rule.getData().getEnabled());
                    tvRuleRule2.setText("rule : " + rule.getData().getRule());
                }
                //Log.d("hng",msgFullData);
            }
        });
    }


}
