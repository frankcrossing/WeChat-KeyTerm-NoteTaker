package net.dalu2048.wechatgenius;

import android.content.ContentValues;

import net.dalu2048.wechatgenius.xposed.JavaMySql;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainXposed implements IXposedHookLoadPackage {

    private static final String[] keyTerms = {
            "CPA", "CPC", "CPS", "UV", "寻优质乙方", "实时后台", "日结", "量结", "无前期", "UV结算",
            "API对接", "现金贷系统", "自研系统", "优质甲方", "优质乙方", "新盘", "跑UV", "跑S", "跑A",
            "买系统", "日量", "高转化", "回款高", "代理", "盘贷超", "低逾期", "脱敏", "可预付",
            "转化好的系统", "短信渠道"
    };

    private static final int numberTerms = 30;
    private static final String WECHAT_DATABASE_PACKAGE_NAME = "com.tencent.wcdb.database.SQLiteDatabase";
    private static JavaMySql database = new JavaMySql();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable{
        if (!lpparam.processName.equals("com.tencent.mm")){
            return;
        }
        XposedBridge.log("Entering WeChat：" + lpparam.processName);

        hookDatabaseInsert(lpparam);
    }
    private void hookDatabaseInsert(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_PACKAGE_NAME,
                loadPackageParam.classLoader);
        if (classDb == null) {
            XposedBridge.log("Cannot Find ClassName " + WECHAT_DATABASE_PACKAGE_NAME);
            return;
        }
        XposedHelpers.findAndHookMethod(classDb,
                "insertWithOnConflict",
                String.class, String.class, ContentValues.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String tableName = (String) param.args[0];
                        ContentValues contentValues = (ContentValues) param.args[2];
                        if (tableName == null || tableName.length() == 0 || contentValues == null){
                            return;
                        }
                        if (!tableName.equals("message")) {
                            return;
                        }
                        printInsertLog(tableName,(String)param.args[1],contentValues,
                                (Integer)param.args[3]);

                        String strContent = contentValues.getAsString("content");
                        strContent = strContent.toUpperCase();

                        int keyTerm = containsKey(strContent);
                        if (keyTerm != -1) {

                            String time = contentValues.getAsString("createTime");
                            String term = keyTerms[keyTerm];

                            database.insertKey(term);
                            database.insertMessage(strContent, term, time);
                        }
                    }
                });
    }
    private void printInsertLog(String tableName, String nullColumnHack,
                                ContentValues contentValues, int conflictValue) {
        String[] arrayConflictValues =
                {"", " OR ROLLBACK ", " OR ABORT ", " OR FAIL ", " OR IGNORE ",
                    " OR REPLACE "};
        if (conflictValue < 0 || conflictValue > 5) {
            return;
        }

        XposedBridge.log("Database insert。table：" + tableName
                        + "; nullColumnHack: " + nullColumnHack
                        + "; CONFLICT_VALUES: " + arrayConflictValues[conflictValue]
                        + "; contentValues:" + contentValues);
    }

    private int containsKey(String message) {
        for (int k = 0; k < numberTerms ; k++)  {
            int length = keyTerms[k].length();
            String term = keyTerms[k];
            for (int i = 0; i<= message.length() - length ; i++) {

                if (term.compareTo(message.substring(i,i+length)) == 0){

                    return k;
                }
            }
        }

        return -1;
    }

}
