package cloud.artik.example.hellocloud;

public class RuleManager {

    public int mRuleIdx; // The index of the current rule in mRuleIds[]
    public String[] mRuleIds = null;
    public int numOfRules = 2;

    public int getRuleIdx(){
        return mRuleIdx;
    }
}
