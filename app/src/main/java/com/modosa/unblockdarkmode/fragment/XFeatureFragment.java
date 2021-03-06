package com.modosa.unblockdarkmode.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.modosa.unblockdarkmode.BuildConfig;
import com.modosa.unblockdarkmode.R;
import com.modosa.unblockdarkmode.util.AppInfoUtil;
import com.modosa.unblockdarkmode.util.Constants;
import com.modosa.unblockdarkmode.util.OpUtil;
import com.modosa.unblockdarkmode.util.SpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author dadaewq
 */
@SuppressWarnings("ConstantConditions")
public class XFeatureFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private final String[] preference_keys = new String[]{
            "x_mobileqq_config",
            "x_wechat_config",
            "x_iflytek_input_config",
            "x_caij_see_config",
            "x_gboard_config",
            "x_bili_config",
            "x_bili_in_config",
            "x_quark_config",
            "x_wechat_hookBrand_config"
    };
    private Context context;
    private SpUtil spUtil;
    private Preference update_hook_config;
    private Preference[] preferences;

    private boolean isOpSuccess = false;
    private AlertDialog alertDialog;
    private EditText valueOfpreferenceKey;

    private ExecutorService singleThreadExecutor;
    private boolean isLock = false;
    private MyHandler mHandler;
    private int successNumber = 0;

    public static String readParse(String urlPath) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inStream = conn.getInputStream();
        while ((len = inStream.read(data)) != -1) {
            outStream.write(data, 0, len);
        }
        inStream.close();
        //通过out.Stream.toByteArray获取到写的数据
        return new String(outStream.toByteArray());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);
        init();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_xfeature, rootKey);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void init() {
        spUtil = new SpUtil(context);

        Preference check_xfeature = findPreference("check_xfeature");
        if (check_xfeature != null) {
            check_xfeature.setSummary("v" + BuildConfig.VERSION_NAME + "（" + BuildConfig.VERSION_CODE + "）");
            if (hook2ReturnTrue()) {
                check_xfeature.setTitle(R.string.check_xfeature_ok);
                check_xfeature.setIcon(R.drawable.ic_passed);
            }
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("x_mobileqq", "x_mobileqq_config");
        hashMap.put("x_wechat", "x_wechat_config");
        hashMap.put("x_wechat_hookBrand", "x_wechat_hookBrand_config");
        hashMap.put("x_iflytek_input", "x_iflytek_input_config");
        hashMap.put("x_caij_see", "x_caij_see_config");
        hashMap.put("x_gboard", "x_gboard_config");
        hashMap.put("x_bili", "x_bili_config");
        hashMap.put("x_bili_in", "x_bili_in_config");
        hashMap.put("x_quark", "x_quark_config");

        //设置图标
//        Drawable drawable;
//        drawable=AppInfoUtil.getApplicationIconDrawable(context,Constants.PACKAGE_NAME_FLYTEK_INPUTMETHOD);
//        if(drawable!=null){
//            findPreference("x_iflytek_input_config").setIcon(drawable);
//        }
//        drawable=AppInfoUtil.getApplicationIconDrawable(context,Constants.PACKAGE_NAME_CAIJ_SEE);
//        if(drawable!=null){
//            findPreference("x_caij_see_config").setIcon(drawable);
//        }

        //设置隐藏/显示 Preference
        for (String key : hashMap.keySet()) {
            SwitchPreferenceCompat switchPreferenceCompat = findPreference(key);
            Preference preference = findPreference(hashMap.get(key));

            preference.setVisible(switchPreferenceCompat.isChecked());

            switchPreferenceCompat.setOnPreferenceChangeListener((preference1, newValue) -> {
                preference.setVisible((boolean) newValue);
                return true;
            });

        }


        update_hook_config = findPreference("update_hook_config");

        preferences = new Preference[preference_keys.length];

        for (int i = 0; i < preference_keys.length; i++) {
            preferences[i] = findPreference(preference_keys[i]);
            preferences[i].setOnPreferenceClickListener(this);
        }

        // Preference "x_wechat_hookBrand"
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            findPreference("x_wechat_hookBrand").setSummary("建议您打开此项并将自定义品牌留空以达到最佳效果");
        }


        update_hook_config.setOnPreferenceClickListener(this);


        findPreference("view_hook_config").setOnPreferenceClickListener(this);
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        refresh();
    }

    private void refresh() {
        update_hook_config.setSummary("当前配置更新时间为:\n" + spUtil.getString("updateTime"));

        setSummary();
        setTitle();

    }

    private void setSummary() {
        for (int i = 0; i < preferences.length - 1; i++) {
            String summary = spUtil.getString(preference_keys[i]);
            if (summary == null || "".equals(summary.replaceAll("\\s*", ""))) {
                preferences[i].setSummary("（当前配置为空，请设置）");
            } else {
                preferences[i].setSummary(summary);
            }
        }


    }

    private void setTitle() {

        String[] packageNames = new String[]{
                Constants.PACKAGE_NAME_MOBILEQQ,
                Constants.PACKAGE_NAME_WECHAT,
                Constants.PACKAGE_NAME_FLYTEK_INPUTMETHOD,
                Constants.PACKAGE_NAME_CAIJ_SEE,
                Constants.PACKAGE_NAME_GBOARD,
                Constants.PACKAGE_NAME_BILIBILI,
                Constants.PACKAGE_NAME_BILIBILI_IN,
                Constants.PACKAGE_NAME_QUARK,
        };
        String[] labels = new String[]{
                "QQ",
                "微信",
                "讯飞输入法",
                "See",
                "Gboard",
                "哔哩哔哩",
                "bilibili",
                "夸克"
        };

        int versionsCount = 2;
        String getAppVersion;
        for (int i = 0; i < versionsCount; i++) {
            try {
                getAppVersion = AppInfoUtil.getAppVersions(context, packageNames[i]);
                preferences[i].setTitle(String.format(getString(R.string.title_custom),
                        labels[i],
                        getAppVersion == null ?
                                (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ? "（未安装）" : "") :
                                getAppVersion)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = versionsCount; i < labels.length; i++) {
            try {
                getAppVersion = AppInfoUtil.getAppVersion(context, packageNames[i]);
                preferences[i].setTitle(String.format(getString(R.string.title_custom),
                        labels[i],
                        getAppVersion == null ?
                                (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ? "（未安装）" : "") :
                                getAppVersion)
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int lastindex = preferences.length - 1;
        preferences[lastindex].setTitle("自定义品牌：" + spUtil.getString(preference_keys[lastindex], ""));
    }

    @SuppressWarnings("SameReturnValue")
    @Keep
    private boolean hook2ReturnTrue() {
//        如果需要hook，不要注释下一行
        Log.i("hook2ReturnTrue", ": ");

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        singleThreadExecutor.shutdown();

        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String preferenceKey = preference.getKey();
        switch (preferenceKey) {
            case "view_hook_config":
                OpUtil.launchCustomTabsUrl(context, "https://dadaewq.gitee.io/tutorials/config/hook_config.html");
                break;
            case "update_hook_config":
                if (isLock) {
                    return false;
                }
                isLock = true;
                updateHookConfig();
                break;
            case "x_mobileqq_config":
            case "x_wechat_config":
                showDialogTencentHook(preferenceKey);
                break;
            case "x_wechat_hookBrand_config":
                showDialoghookBrand(preferenceKey);
                break;
            case "x_iflytek_input_config":
                showDialogIflytekInputHook(preferenceKey);
                break;
            case "x_caij_see_config":
                showDialogCaijSeeHook(preferenceKey);
                break;
            case "x_gboard_config":
                showDialogGboardHook(preferenceKey);
                break;
            case "x_bili_config":
                showDialogBiliHook(preferenceKey);
                break;
            case "x_bili_in_config":
                showDialogBiliInHook(preferenceKey);
                break;
            case "x_quark_config":
                showDialogQuarkHook(preferenceKey);
                break;
            default:
        }

        return false;
    }

    private void updateHookConfig() {
        successNumber = 0;
        singleThreadExecutor.execute(() -> {
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 9;
            try {
                String configs = readParse("https://gitee.com/dadaewq/Tutorials/raw/master/config/hook_config.json");
                JSONObject jsonObject = new JSONObject(configs);
                spUtil.putString("updateTime", "" + jsonObject.opt("updateTime"));
                Object obconfigs = jsonObject.opt("custom_configs");
                if (obconfigs == null) {
                    msg.arg1 = 6;
                    mHandler.sendMessage(msg);
                    return;
                }
                HashMap<String, String>[] hashMaps = new HashMap[preference_keys.length - 1];

                for (int i = 0; i < hashMaps.length; i++) {
                    hashMaps[i] = new HashMap<>();
                }

                String pref_key = "pref_key";
                String pkgName = "pkgName";
                hashMaps[0].put(pref_key, "x_mobileqq");
                hashMaps[0].put(pkgName, Constants.PACKAGE_NAME_MOBILEQQ);
                hashMaps[1].put(pref_key, "x_wechat");
                hashMaps[1].put(pkgName, Constants.PACKAGE_NAME_WECHAT);
                hashMaps[2].put(pref_key, "x_iflytek_input");
                hashMaps[2].put(pkgName, Constants.PACKAGE_NAME_FLYTEK_INPUTMETHOD);
                hashMaps[3].put(pref_key, "x_caij_see");
                hashMaps[3].put(pkgName, Constants.PACKAGE_NAME_CAIJ_SEE);
                hashMaps[4].put(pref_key, "x_gboard");
                hashMaps[4].put(pkgName, Constants.PACKAGE_NAME_GBOARD);
                hashMaps[5].put(pref_key, "x_bili");
                hashMaps[5].put(pkgName, Constants.PACKAGE_NAME_BILIBILI);
                hashMaps[6].put(pref_key, "x_bili_in");
                hashMaps[6].put(pkgName, Constants.PACKAGE_NAME_BILIBILI_IN);
                hashMaps[7].put(pref_key, "x_quark");
                hashMaps[7].put(pkgName, Constants.PACKAGE_NAME_QUARK);

                int versionsCount = 2;
                String getPrefKey, getPkgName;
                for (int i = 0; i < versionsCount; i++) {
                    getPrefKey = hashMaps[i].get(pref_key);
                    if (spUtil.getBoolean(getPrefKey, true)) {
                        try {
                            getPkgName = hashMaps[i].get(pkgName);
                            String config = ((JSONObject) ((JSONObject) obconfigs).get(getPkgName)).getString(AppInfoUtil.getAppVersions(context, getPkgName));
                            spUtil.putString(getPrefKey + "_config", config);
                            successNumber++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (int i = versionsCount; i < hashMaps.length; i++) {
                    getPrefKey = hashMaps[i].get(pref_key);
                    if (spUtil.getBoolean(getPrefKey, true)) {
                        try {
                            getPkgName = hashMaps[i].get(pkgName);
                            String config = ((JSONObject) ((JSONObject) obconfigs).get(getPkgName)).getString(AppInfoUtil.getAppVersion(context, getPkgName));
                            spUtil.putString(getPrefKey + "_config", config);
                            successNumber++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                try {
                    if (spUtil.getBoolean("x_wechat", true) && AppInfoUtil.getAppVersion(context, "com.tencent.mm").compareTo("7.0.17") >= 0) {
                        JSONObject jsonObject1 = (JSONObject) ((JSONObject) obconfigs).get("com.tencent.mm_hookbrand");
                        if (jsonObject1 != null) {
                            String brand = jsonObject1.getString("brand");
                            int sdk = Build.VERSION.SDK_INT;
                            if (brand != null
                                    && sdk >= jsonObject1.getInt("minsdk")
                                    && sdk <= jsonObject1.getInt("maxsdk")
                            ) {
                                JSONArray limits = jsonObject1.getJSONArray("limits");
                                String myBrnad = Build.BRAND.toLowerCase();
                                String myConfigBrnad = spUtil.getString("x_wechat_hookBrand_config", "");
                                boolean ishookBrand = spUtil.getBoolean("x_wechat_hookBrand", false);
                                boolean ok = false;
                                for (int i = 0; i < limits.length(); i++) {
                                    String limit = limits.get(i).toString();
                                    if ((!ishookBrand && myBrnad.contains(limit)) || (ishookBrand && myConfigBrnad.contains(limit))) {
                                        ok = true;
                                    }
                                }
                                if (ok) {
                                    spUtil.putBoolean("x_wechat_hookBrand", true);
                                    spUtil.putString("x_wechat_hookBrand_config", brand);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                msg.arg1 = 6;
            }

            mHandler.sendMessage(msg);
        });
    }

    private void showDialogTencentHook(String preferenceKey) {
        if ("x_wechat_config".equals(preferenceKey)) {
            creatAndshowDialog(preferenceKey, "类名:方法名,方法名,...或类名;变量名", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("：", ":").replace("，", ",").replace("；", ";"));
        } else {
            creatAndshowDialog(preferenceKey, "类名:方法名,方法名,...", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("：", ":").replace("，", ","));
        }
    }

    private void showDialoghookBrand(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "请输入品牌", () -> valueOfpreferenceKey.getText().toString());
        alertDialog.setTitle("自定义品牌(Brand)");
    }

    private void showDialogIflytekInputHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "类名", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", ""));
    }

    private void showDialogCaijSeeHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "类名:方法名", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("：", ":"));
    }

    private void showDialogGboardHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "类名:方法名", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("：", ":"));

    }

    private void showDialogBiliHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "一大串", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("，", ",").replace("：", ":"));

    }

    private void showDialogBiliInHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "一大串", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("，", ",").replace("：", ":"));

    }

    private void showDialogQuarkHook(String preferenceKey) {
        creatAndshowDialog(preferenceKey, "一大串", () -> valueOfpreferenceKey.getText().toString().replaceAll("\\s*", "").replace("，", ",").replace("：", ":"));
    }


    private void creatAndshowDialog(String preferenceKey, String hint, MyCallback myCallback) {
        valueOfpreferenceKey = new EditText(context);
        valueOfpreferenceKey.setHint(hint);
        valueOfpreferenceKey.setText(spUtil.getString(preferenceKey));
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("自定义HOOK")
                .setView(valueOfpreferenceKey)
                .setNeutralButton("关闭", null)
                .setNegativeButton("清空", null)
                .setPositiveButton("保存", null);
        alertDialog = builder.create();
        OpUtil.showAlertDialog(context, alertDialog);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String value = myCallback.getvalue();

            valueOfpreferenceKey.setText(value);
            opPreferenceValueFromDialog(preferenceKey, value);
        });

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> valueOfpreferenceKey.setText(null));
        alertDialog.setOnDismissListener(dialog -> {
            if (isOpSuccess) {
                isOpSuccess = false;
                refresh();
            }
        });


    }

    private void opPreferenceValueFromDialog(String preferenceKey, String value) {

        isOpSuccess = false;

        try {
            spUtil.putString(preferenceKey, value);
            isOpSuccess = true;
        } catch (Exception ignore) {
            isOpSuccess = false;
        }

    }


    interface MyCallback {
        /**
         * 获取Edittext的有效值
         */
        String getvalue();
    }

    private static class MyHandler extends Handler {

        private final WeakReference<XFeatureFragment> wrFragment;

        MyHandler(XFeatureFragment fragment) {
            this.wrFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (wrFragment.get() == null) {
                return;
            }
            XFeatureFragment xFeatureFragment = wrFragment.get();


            if (msg.arg1 == 9) {
                if (xFeatureFragment.spUtil.getFalseBoolean("x_wechat_hookBrand")) {
                    ((SwitchPreferenceCompat) xFeatureFragment.findPreference("x_wechat_hookBrand")).setChecked(true);
                    (xFeatureFragment.findPreference("x_wechat_hookBrand_config")).setVisible(true);
                }

                OpUtil.showToast0(xFeatureFragment.context, "成功更新" + xFeatureFragment.successNumber + "项自定义配置");
                xFeatureFragment.isLock = false;
                xFeatureFragment.refresh();
            } else if (msg.arg1 == 6) {
                OpUtil.showToast0(xFeatureFragment.context, "更新失败");
            }
        }
    }
}


