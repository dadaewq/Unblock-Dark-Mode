package com.modosa.unblockdarkmode.util;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Keep;

import com.modosa.unblockdarkmode.BuildConfig;
import com.modosa.unblockdarkmode.fragment.XFeatureFragment;
import com.modosa.unblockdarkmode.provider.MyPreferenceProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

/**
 * @author dadaewq
 */
@Keep
@SuppressWarnings("WeakerAccess")
public class XModule implements IXposedHookLoadPackage {

    Context context;
    SharedPreferences sharedPreferences;
    XC_LoadPackage.LoadPackageParam loadPackageParam;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        this.loadPackageParam = loadPackageParam;

        String loadPackageName = loadPackageParam.packageName;

        switch (loadPackageName) {
            case Constants.PACKAGE_NAME_COOLAPK:
                initPreferencesWithCallHook(this::hookCoolapk);
                break;
            case Constants.PACKAGE_NAME_DINGTALK:
            case Constants.PACKAGE_NAME_DINGTALK_GLOBAL:
                initPreferencesWithCallHook(this::hookDingTalk);
                break;
            case Constants.PACKAGE_NAME_FLYTEK_INPUTMETHOD:
                initPreferencesWithCallHook(this::hookCustomIflytekInput);
                break;
            case Constants.PACKAGE_NAME_CAIJ_SEE:
                initPreferencesWithCallHook(this::hookCustomCaijSee);
                break;
            case Constants.PACKAGE_NAME_GBOARD:
                initPreferencesWithCallHook(this::hookCustomGboard);
                break;
            case Constants.PACKAGE_NAME_MOBILEQQ:
                initPreferencesWithCallHook(() -> hookCustomTencent("x_mobileqq"));
                break;
            case Constants.PACKAGE_NAME_WECHAT:
                initPreferencesWithCallHook(() -> hookCustomTencent("x_wechat"));
                break;
            case BuildConfig.APPLICATION_ID:
                hookMyself();
                break;
            default:
        }
    }


    private void initPreferencesWithCallHook(CallHook callHook) {

        try {

            findAndHookMethod(Application.class, "attach",
                    Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            context = (Context) param.args[0];
                            sharedPreferences = MyPreferenceProvider.getRemoteSharedPreference(context);

                            callHook.call();
                        }
                    });
        } catch (Exception e) {
            Log.e("Exception", "initPreferencesWithCallHook : ");
            XposedBridge.log("" + e);
        }

    }


    private void hookSdkInt(int versionSdk) {
        try {
            XposedHelpers.setStaticObjectField(Build.VERSION.class, "SDK_INT", versionSdk);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void hookBrand(String brand) {
        try {
            XposedHelpers.setStaticObjectField(Build.class, "BRAND", brand);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//        try {
//            findAndHookMethod("android.os.SystemProperties", loadPackageParam.classLoader,
//                    "native_get",
//                    String.class,
//                    String.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//                            String key = (String) param.args[0];
//                            Log.d("HookTest", "SystemProperties get " + key);
//                            if ("ro.product.brand".equals(key)) {
//                                param.setResult(brand);
//                            }
//                            param.hasThrowable();
//                        }
//                    }
//            );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void hookCoolapk() {

        if (sharedPreferences != null && sharedPreferences.getBoolean("x_coolapk", true)) {
            try {
                findAndHookMethod("com.coolapk.market.AppSetting", loadPackageParam.classLoader,
                        "shouldDisableXposed",
                        XC_MethodReplacement.returnConstant(false)
                );

                findAndHookMethod("com.coolapk.market.util.XposedUtils", loadPackageParam.classLoader,
                        "disableXposed",
                        XC_MethodReplacement.returnConstant(true)
                );
            } catch (Exception e) {
                XposedBridge.log("" + e);
            }

            try {
                findAndHookMethod("com.coolapk.market.util.NightModeHelper", loadPackageParam.classLoader,
                        "isThisRomSupportSystemTheme",
                        XC_MethodReplacement.returnConstant(true)
                );
            } catch (Exception e) {
                XposedBridge.log("hookCoolapk e：" + e);
            }
        }
    }

    private void hookDingTalk() {

        if (sharedPreferences != null && sharedPreferences.getBoolean("x_dingtalk", true)) {
            try {
                findAndHookMethod("com.alibaba.android.dingtalkui.dark.ThemeHelper", loadPackageParam.classLoader,
                        "d",
                        XC_MethodReplacement.returnConstant(true)
                );

            } catch (Exception e) {
                XposedBridge.log("hookDingTalk e：" + e);
            }
        }
    }


    private void hookCustomTencent(String key) {

        String x_tencent_config;
        if (sharedPreferences != null) {
            if (sharedPreferences.getBoolean(key + "_hookBrand", false)) {
                hookBrand(sharedPreferences.getString(key + "_hookBrand_config", ""));
            }

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_tencent_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_tencent_config", key + "_config——" + x_tencent_config);

                try {
                    if (!"".equals(x_tencent_config)) {
                        assert x_tencent_config != null;
                        x_tencent_config = x_tencent_config.replaceAll("\\s*", "").replace("：", ":").replace("，", ",");
                        int length = x_tencent_config.length();
                        if ("x_wechat".equals(key)) {
                            x_tencent_config = x_tencent_config.replace("；", ";");
                            if (x_tencent_config.endsWith(";")) {
                                x_tencent_config = x_tencent_config.substring(0, length - 1);
                            }
                            if (x_tencent_config.contains(";")) {
                                String[] brandConfig = x_tencent_config.split(";");
                                if (brandConfig.length >= 2) {
                                    hookTencentBrandApi(brandConfig[0], brandConfig[1]);
                                }
                                return;
                            }
                        }


                        if (x_tencent_config.endsWith(",")) {
                            x_tencent_config = x_tencent_config.substring(0, length - 1);
                        }
                        if (length > 2) {
                            String[] splits = x_tencent_config.split(":");
                            if (splits.length >= 2) {
                                String className = splits[0];
                                String[] methodNames = splits[1].split(",");

                                hookReturnBooleanWithmethodNames(loadPackageParam.classLoader, className, methodNames, true);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookTencentBrandApi(String clazzName, String brandVariable) {
        try {
            String brands = Build.BRAND.toLowerCase() + "&8, other&8";
            Log.e("brand_api", brands);
            setStaticObjectField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    brandVariable,
                    brands
            );
        } catch (Exception e) {
            Log.e("Exception", "hook brand_api : ");
        }
    }


    private void hookCustomIflytekInput() {


        String x_iflytek_input_config;
        if (sharedPreferences != null) {
            String key = "x_iflytek_input";

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_iflytek_input_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_iflytek_input_config", key + "_config——" + x_iflytek_input_config);

                try {
                    if (!"".equals(x_iflytek_input_config)) {
                        assert x_iflytek_input_config != null;
                        x_iflytek_input_config = x_iflytek_input_config.replaceAll("\\s*", "");

                        if (x_iflytek_input_config.length() > 2) {

                            hookIflytekInput(x_iflytek_input_config);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookIflytekInput(String clazzName) {

        try {
            findAndHookMethod("com.iflytek.inputmethod.depend.config.settings.Settings", loadPackageParam.classLoader,
                    "isDarkModeAdaptOpen",
                    XC_MethodReplacement.returnConstant(true)
            );

        } catch (Exception e) {
            XposedBridge.log("" + e);
        }

        String[] staticObjectFields = new String[]{"a", "b", "c", "d", "e", "f"};
        int a, b, c;
        Field d;

        try {
            setStaticBooleanField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[4],
                    true
            );
        } catch (Exception e) {
            XposedBridge.log("hookIflytekInput e：" + e);
        }
        try {
            setStaticObjectField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[5],
                    true
            );
        } catch (Exception e) {
            XposedBridge.log("hookIflytekInput e：" + e);
        }

        try {
            Field declaredField = Configuration.class.getDeclaredField("UI_MODE_NIGHT_YES");
            declaredField.setAccessible(true);
            a = declaredField.getInt(null);
            Field declaredField2 = Configuration.class.getDeclaredField("UI_MODE_NIGHT_NO");
            declaredField2.setAccessible(true);
            b = declaredField2.getInt(null);
            Field declaredField3 = Configuration.class.getDeclaredField("UI_MODE_NIGHT_MASK");
            declaredField3.setAccessible(true);
            c = declaredField3.getInt(null);
            d = Configuration.class.getDeclaredField("uiMode");
            d.setAccessible(true);

            setStaticIntField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[0],
                    a
            );
            setStaticIntField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[1],
                    b
            );
            setStaticIntField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[2],
                    c
            );

            setStaticObjectField(
                    XposedHelpers.findClass(clazzName, loadPackageParam.classLoader),
                    staticObjectFields[3],
                    d
            );
        } catch (Exception e) {
            XposedBridge.log("hookIflytekInput e：" + e);
        }

    }


    private void hookCustomCaijSee() {


        String x_caij_see_config;
        if (sharedPreferences != null) {
            String key = "x_caij_see";

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_caij_see_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_caij_see_config", key + "_config——" + x_caij_see_config);

                try {
                    if (!"".equals(x_caij_see_config)) {
                        assert x_caij_see_config != null;
                        x_caij_see_config = x_caij_see_config.replaceAll("\\s*", "").replace("：", ":");

                        if (x_caij_see_config.length() > 15) {
                            String[] splits = x_caij_see_config.split(":");
                            String className, methodName;
                            if (splits.length >= 2) {
                                className = splits[0];
                                methodName = splits[1];

                                hookCaijSee(className, methodName);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookCaijSee(String className, String methodName) {

        try {
            findAndHookMethod(className, loadPackageParam.classLoader, methodName,
                    Context.class,
                    XC_MethodReplacement.returnConstant(true)
            );
        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }

    private boolean isnightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void hookCustomGboard() {

        String x_gboard_config;
        if (sharedPreferences != null) {
            String key = "x_gboard";

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_gboard_config = sharedPreferences.getString(key + "_config", "");

                Log.e("x_gboard_config", key + "_config——" + x_gboard_config);

                try {
                    if (!"".equals(x_gboard_config)) {
                        assert x_gboard_config != null;
                        x_gboard_config = x_gboard_config.replaceAll("\\s*", "").replace("：", ":");

                        if (x_gboard_config.length() > 2) {
                            String[] splits = x_gboard_config.split(":");
                            if (splits.length >= 2) {
                                String className = splits[0];
                                String methodName = splits[1];

                                hookGboard(className, methodName, context);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookGboard(String className, String methodName, Context context) {

        try {
            findAndHookMethod(className,
                    loadPackageParam.classLoader,
                    methodName,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            param.setResult(isnightMode(context));
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }


    private void hookMyself() {
        try {
            findAndHookMethod(XFeatureFragment.class.getName(), loadPackageParam.classLoader,
                    "hook2ReturnTrue",
                    XC_MethodReplacement.returnConstant(true)
            );
        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }

    private void hookReturnBooleanWithmethodNames(ClassLoader classLoader, String className, String[] methodNames, boolean booleanVlaue) {

        Method[] methods = XposedHelpers.findMethodsByExactParameters(XposedHelpers.findClass(className, classLoader), boolean.class);

        for (String methodName : methodNames) {
            try {
                for (Method method : methods) {
                    if (methodName.equals(method.getName())) {
                        XposedBridge.hookMethod(
                                method,
                                XC_MethodReplacement.returnConstant(booleanVlaue)
                        );
                    }
                }
            } catch (Exception e) {
                XposedBridge.log("" + e);
            }
        }
    }

    private void hookReturnBooleanWithmethodName(ClassLoader classLoader, String className, String methodName, boolean booleanVlaue) {
        hookReturnBooleanWithmethodNames(classLoader, className, new String[]{methodName}, booleanVlaue);
    }


    private void findAndHookMethodReturnBoolean(ClassLoader classLoader, String className, String methodName, boolean booleanVlaue) {

        Log.e("hookCaijSee", className + methodName);

        try {
            findAndHookMethod(className, classLoader,
                    methodName,
                    XC_MethodReplacement.returnConstant(booleanVlaue)
            );

        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }

    interface CallHook {
        /**
         * CallHook 接口
         */
        void call();
    }
}
