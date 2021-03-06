package com.modosa.unblockdarkmode.util;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Keep;

import com.modosa.unblockdarkmode.BuildConfig;
import com.modosa.unblockdarkmode.fragment.XFeatureFragment;
import com.modosa.unblockdarkmode.provider.MyPreferenceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findFieldIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticBooleanField;
import static de.robv.android.xposed.XposedHelpers.setStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

interface CallHook {
    /**
     * CallHook 接口
     */
    void call();
}

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
            case Constants.PACKAGE_NAME_JD:
                initPreferencesWithCallHook(this::hookJD);
                break;
            case Constants.PACKAGE_NAME_WEIBO_IN:
                initPreferencesWithCallHook(this::hookWeiboIn);
                break;
            case Constants.PACKAGE_NAME_QIDIAN:
                initPreferencesWithCallHook(this::hookQiDian);
                break;
            case Constants.PACKAGE_NAME_MOBILEQQ:
                initPreferencesWithCallHook(() -> hookCustomTencent("x_mobileqq"));
                break;
            case Constants.PACKAGE_NAME_WECHAT:
                initPreferencesWithCallHook(() -> hookCustomTencent("x_wechat"));
                break;
            case Constants.PACKAGE_NAME_FLYTEK_INPUTMETHOD:
                initPreferencesWithCallHook(this::hookIflytekInput);
                break;
            case Constants.PACKAGE_NAME_CAIJ_SEE:
                initPreferencesWithCallHook(this::hookCaijSee);
                break;
            case Constants.PACKAGE_NAME_GBOARD:
                initPreferencesWithCallHook(this::hookGboard);
                break;
            case Constants.PACKAGE_NAME_BILIBILI:
            case Constants.PACKAGE_NAME_BILIBILI_IN:
                initPreferencesWithCallHook(() -> hookBili(loadPackageName));
                break;
            case Constants.PACKAGE_NAME_QUARK:
                initPreferencesWithCallHook(this::hookQuark);
                break;
            case Constants.PACKAGE_NAME_CLOUDMUSIC:
                initPreferencesWithCallHook(this::hookCloudMusic);
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


    private void hookJD() {
        if (sharedPreferences != null && sharedPreferences.getBoolean("x_jd", true)) {

            hookJDSharedPreferencesgetBoolean("deep_dark_guide_switch", true);
            hookJDSharedPreferencesgetBoolean("deep_dark_follow_sys_switch", true);
        }
    }

    private void hookJDSharedPreferencesgetBoolean(String key, boolean value) {
        try {
            findAndHookMethod("com.jingdong.jdsdk.utils.JDSharedPreferences", loadPackageParam.classLoader,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Log.e("themejd", param.args.length + " beforeHookedMethod: " + param.args[0]);
                            if (key.equals(param.args[0])) {
                                param.setResult(value);
                            }
                        }
                    }
            );

        } catch (Exception e) {
            XposedBridge.log("" + e);
        }

    }


    /**
     * hook微博国际版的 MainFragmentActivity
     * <p>
     * 微博国际版根据以下代码修改主题
     * if (System.currentTimeMillis() - lastThemeChange >= 3000) {
     * lastThemeChange = System.currentTimeMillis();
     * if (!SkinManager.getInstance().isExternalSkin()) {
     * SkinManager.getInstance().release();
     * LogoActivity.copyNightTheme(new 3());
     * return;
     * }
     * SkinManager.getInstance().release();
     * SkinManager.getInstance().restoreDefaultTheme();
     * EventBus.getDefault().post(new Events.LanguageChangeEvent());
     * }
     */
    private void hookWeiboIn() {
        if (sharedPreferences != null && sharedPreferences.getBoolean("x_weibo_in", true)) {

            Log.e("weibo_in", "\n\n 开始 hookWeibo_In");

            ClassLoader cl = context.getClassLoader();

            String clazzName = "com.weico.international.activity.MainFragmentActivity";
            Class<?> hookclass0 = loadClass(cl, clazzName);


            try {
                findAndHookMethod(
                        hookclass0,
                        "onResume",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e("weibo_in", "onResume 0000000000");

                                weiboChangeTheme(hookclass0, param.thisObject);

                                Log.e("weibo_in", "onResume 11111111111");
                            }
                        }
                );


                findAndHookMethod(
                        hookclass0,
                        "onConfigurationChanged",
                        Configuration.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e("weibo_in", "onConfigurationChanged 0000000000");

                                weiboChangeTheme(hookclass0, param.thisObject);

                                Log.e("weibo_in", "onConfigurationChanged  11111111");
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void weiboChangeTheme(Class<?> MainFragmentActivityClass, Object instance) {
        try {
            boolean isNightMode = isnightMode(context);


            try {
                Field lastThemeChange = findFieldIfExists(MainFragmentActivityClass, "lastThemeChange");
                if (lastThemeChange != null) {
                    lastThemeChange.setLong(null, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //MenuItem
            Object navChangeTheme = getObjectField(
                    instance,
                    "navChangeTheme"
            );
            //SwitchCompat
            Object actionView = callMethod(
                    navChangeTheme,
                    "getActionView"
            );

//            androidx.appcompat.widget.SwitchCompat cannot be cast to androidx.
//            appcompat.widget.SwitchCompat
//                    ((SwitchCompat) actionView).setChecked(isNightMode);

            callMethod(
                    actionView,
                    "setChecked",
                    isNightMode
            );

        } catch (Exception e) {
            Log.e("weibo_in", "Exception onConfigurationChanged  \n" + e);
        }

    }

    /**
     * hook微博国际版的 Application
     */
    private void hookWeiboIn_Application() {

        String x_weibo_in_config;
        if (sharedPreferences != null) {
            String key = "weibo_in";

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_weibo_in_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_weibo_in_config", key + "_config——" + x_weibo_in_config);


                Log.e("weibo_in", "\n\n 开始 hookWeibo_In");

                ClassLoader cl = context.getClassLoader();
                //cl.loadclass("className")找其他类
                //Class.forName("className",true,cl)
                Class<?> hookclass0, hookclass1, hookclass2, hookclass3, hookclass4;

                String clazzName = "com.skin.loader.SkinManager";
                hookclass0 = loadClass(cl, clazzName);

                clazzName = "com.weico.international.activity.LogoActivity";
                hookclass1 = loadClass(cl, clazzName);

                clazzName = "de.greenrobot.event.EventBus";
                hookclass2 = loadClass(cl, clazzName);

                clazzName = "com.weico.international.activity.MainFragmentActivity$3";
                hookclass3 = loadClass(cl, clazzName);

                clazzName = "com.weico.international.flux.Events$LanguageChangeEvent";
                hookclass4 = loadClass(cl, clazzName);


                if (hookclass0 == null || hookclass1 == null || hookclass2 == null || hookclass3 == null || hookclass4 == null) {
                    Log.e("weibo_in", "MutiDex 寻找五个class 失败");
                }

                try {
                    findAndHookMethod(
                            Application.class,
                            "onConfigurationChanged",
                            Configuration.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);

                                    Log.e("weibo_in", "onConfigurationChanged 0000000000");

                                    try {

                                        assert hookclass3 != null;
                                        Constructor<?> constructor = hookclass3.getDeclaredConstructor();
                                        //因为不可直接访问
                                        constructor.setAccessible(true);
                                        Object object0 = constructor.newInstance();

                                        assert hookclass4 != null;
                                        Object object1 = hookclass4.newInstance();
                                        boolean isNightMode = isnightMode(context);


                                        Object instance_SkinManager = callStaticMethod(
                                                hookclass0,
                                                "getInstance"
                                        );

                                        boolean isExternalSkin = (boolean) callMethod(
                                                instance_SkinManager,
                                                "isExternalSkin"
                                        );

                                        callMethod(
                                                instance_SkinManager,
                                                "release"
                                        );

                                        Log.e("weibo_in", " isExternalSkin " + isExternalSkin + " isNightMode " + isNightMode);

                                        if (!isExternalSkin) {
                                            if (isNightMode) {
                                                //开始切换夜间主题
                                                callStaticMethod(
                                                        hookclass1,
                                                        "copyNightTheme",
                                                        object0
                                                );

                                            }

                                        } else {
                                            if (!isNightMode) {
                                                //开始切换默认主题
                                                callMethod(
                                                        instance_SkinManager,
                                                        "restoreDefaultTheme"
                                                );

                                                Object instance_EventBus = callStaticMethod(
                                                        hookclass2,
                                                        "getDefault"
                                                );

                                                callMethod(
                                                        instance_EventBus,
                                                        "post",
                                                        object1
                                                );

                                            }
                                        }


                                    } catch (Exception e) {
                                        Log.e("weibo_in", "Exception onConfigurationChanged  \n" + e);
                                    }

                                    Log.e("weibo_in", "onConfigurationChanged  11111111");

                                }
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private void hookQiDian() {
        if (sharedPreferences != null && sharedPreferences.getBoolean("x_qidian", true)) {

            Log.e("qidian", "\n\n 开始 hookqidian");

            ClassLoader cl = context.getClassLoader();
            Class<?> hookclass0, hookclass1, hookclass2;

            String clazzName = "com.qidian.QDReader.core.config.QDConfig";

            hookclass0 = loadClass(cl, clazzName);

            clazzName = "com.qidian.QDReader.ui.activity.BaseActivity";
            hookclass1 = loadClass(cl, clazzName);

            clazzName = "com.qidian.QDReader.comic.ui.QDComicReadingBaseActivity";
            hookclass2 = loadClass(cl, clazzName);

//            clazzName = "com.qidian.QDReader.comic.ui.QDComicReadingVerticalActivity";
//            hookclass3 = loadClass(cl, clazzName);
//
//            clazzName = "com.qidian.QDReader.comic.ui.QDComicReadingLandActivity";
//            hookclass4 = loadClass(cl, clazzName);

            try {

                Object[] BaseActivity = new Object[]{null};
                findAndHookMethod(
                        hookclass1,
                        "onCreate",
                        Bundle.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e("qidian", "onCreate 0000000000" + param.thisObject);

                                BaseActivity[0] = param.thisObject;
                                qidianChangeTheme(hookclass0, BaseActivity);

                                Log.e("qidian", "onCreate 11111111111" + param.thisObject);
                            }
                        }
                );

                findAndHookMethod(
                        Application.class,
                        "onConfigurationChanged",
                        Configuration.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e("qidian", "onConfigurationChanged 0000000000");

                                qidianChangeTheme(hookclass0, BaseActivity);

                                Log.e("qidian", "onConfigurationChanged  11111111");
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void qidianChangeTheme(Class<?> QDConfig, Object[] BaseActivity) {
        String TAG = "qidianChangeTheme";
        if (BaseActivity[0] == null) {
            return;
        }

        try {
            boolean isNightMode = isnightMode(context);


            Object instance_QDConfig = callStaticMethod(
                    QDConfig,
                    "getInstance"
            );

            String SettingIsNight = (String) callMethod(
                    instance_QDConfig,
                    "GetSetting",
                    "SettingIsNight",
                    "0"
            );


            boolean isNightTheme = "1".equals(SettingIsNight);

            Log.e("qidian", "isNightTheme: " + isNightTheme);
            if (isNightMode != isNightTheme) {
                Log.e(TAG, "qidianChangeTheme: " + BaseActivity[0]);
                if (BaseActivity[0] != null) {
                    callMethod(
                            BaseActivity[0],
                            "setNightDayTheme"
                    );
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "qidianChangeTheme: Exception" + e);
        }


    }

//    private void hookQiDian1() {
//        if (sharedPreferences != null && sharedPreferences.getBoolean("x_qidian", true)) {
//
//            Log.e("qidian", "\n\n 开始 hookqidian");
//
//            ClassLoader cl = context.getClassLoader();
//            Class<?> hookclass0, hookclass1;
//
//            String clazzName = "com.qidian.QDReader.ui.fragment.QDUserAccountFragment";
//            hookclass0 = loadClass(cl, clazzName);
//
//            clazzName = "com.qidian.QDReader.core.config.QDConfig";
//            hookclass1 = loadClass(cl, clazzName);
//
//
//            Object[] frag = new Object[]{null};
//            findAndHookMethod(
//                    hookclass0,
//                    "onCreate",
//                    Bundle.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//
//                            Log.e("qidian", "onResume 0000000000" + param.thisObject);
//
//                            frag[0] = param.thisObject;
//                            qidianChangeTheme(hookclass0, hookclass1, param.thisObject);
//
//                            Log.e("qidian", "onResume 11111111111" + param.thisObject);
//                        }
//                    }
//            );
//
//
//            findAndHookMethod(
//                    hookclass0,
//                    "onViewInject",
//                    View.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//
//                            Log.e("qidian", "onResume 0000000000" + param.thisObject);
//
//                            View view = (View) param.args[0];
//                            Log.e("qidian", "onResume 0000000000" + view);
//
//                            qidianChangeTheme(hookclass0, hookclass1, view, frag[0]);
//
//                            Log.e("qidian", "onResume 11111111111" + param.thisObject);
//                        }
//                    }
//            );
//
//            if (true) return;
//            try {
//                findAndHookMethod(
//                        hookclass0,
//                        "onResume",
//                        new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//
//                                Log.e("qidian", "onResume 0000000000" + param.thisObject);
//
//                                qidianChangeTheme(hookclass0, hookclass1, param.thisObject);
//
//                                Log.e("qidian", "onResume 11111111111" + param.thisObject);
//                            }
//                        }
//                );
//
//
//                findAndHookMethod(
//                        hookclass0,
//                        "onConfigurationChanged",
//                        Configuration.class,
//                        new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//
//                                Log.e("qidian", "onConfigurationChanged 0000000000");
//
//                                qidianChangeTheme(hookclass0, hookclass1, param.thisObject);
//
//                                Log.e("qidian", "onConfigurationChanged  11111111");
//                            }
//                        }
//                );
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

    private void qidianChangeTheme(Class<?> QDUserAccountFragment, Class<?> QDConfig, View view, Object instance) {
        String TAG = "qidian";

        if (instance == null) {
            return;
        }
        Log.e(TAG, "qidianChangeTheme: instance" + instance);
        try {
            boolean isNightMode = isnightMode(context);


            Object instance_QDConfig = callStaticMethod(
                    QDConfig,
                    "getInstance"
            );

            String SettingIsNight = (String) callMethod(
                    instance_QDConfig,
                    "GetSetting",
                    "SettingIsNight",
                    "0"
            );


            boolean isNightTheme = "1".equals(SettingIsNight);

            Log.e("qidian", "isNightTheme: " + isNightTheme);
            if (isNightMode != isNightTheme) {

                Field field = QDUserAccountFragment.getDeclaredField("mLayoutThemeSwitch");

                field.setAccessible(true);

                Log.e("qidian", "qidianChangeTheme: view " + view);


//                View linearLayout = new LinearLayout(context);

//                Log.e("qidian", "qidianChangeTheme:mLayoutThemeSwitch " + mLayoutThemeSwitch);

//                ((LinearLayout) mLayoutThemeSwitch).callOnClick();

//                Activity activity = (Activity) callMethod(
//                        view,
//                        "getActivity"
//                );
//
                Log.e("qidian", "qidianChangeTheme:activity " + context);
                int viewId = context.getResources().getIdentifier("themeSwitch", "id", context.getPackageName());

                Log.e("qidian", "qidianChangeTheme:viewId " + viewId);

                LinearLayout mLayoutThemeSwitch = view.findViewById(viewId);

                View view1 = new View(context);
                view1.setId(viewId);
//                 View view=activity.findViewById(viewId);
                Log.e("qidian", "qidianChangeTheme:linearLayout000 " + mLayoutThemeSwitch);
                callMethod(
                        instance,
                        "onClick",
                        view1
                );

                Log.e("qidian", "qidianChangeTheme:linearLayout 111" + mLayoutThemeSwitch.getId());
            }


//            try {
//                Field lastThemeChange = findFieldIfExists(MainFragmentActivityClass, "lastThemeChange");
//                if (lastThemeChange != null) {
//                    lastThemeChange.setLong(null, 0);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            //MenuItem
//            Object navChangeTheme = getObjectField(
//                    view,
//                    "navChangeTheme"
//            );
//            //SwitchCompat
//            Object actionView = callMethod(
//                    navChangeTheme,
//                    "getActionView"
//            );
//
////            androidx.appcompat.widget.SwitchCompat cannot be cast to androidx.
////            appcompat.widget.SwitchCompat
////                    ((SwitchCompat) actionView).setChecked(isNightMode);
//
//            callMethod(
//                    actionView,
//                    "setChecked",
//                    isNightMode
//            );

        } catch (Exception e) {
            Log.e("qidian", "Exception onConfigurationChanged  \n" + e);
        }

    }

    private void qidianChangeTheme(Class<?> QDUserAccountFragment, Class<?> QDConfig, Object instance) {
        String TAG = "qidian";


        try {
            boolean isNightMode = isnightMode(context);


            Object instance_QDConfig = callStaticMethod(
                    QDConfig,
                    "getInstance"
            );

            String SettingIsNight = (String) callMethod(
                    instance_QDConfig,
                    "GetSetting",
                    "SettingIsNight",
                    "0"
            );


            boolean isNightTheme = "1".equals(SettingIsNight);

            Log.e("qidian", "isNightTheme: " + isNightTheme);
            if (isNightMode != isNightTheme) {

                Field field = QDUserAccountFragment.getDeclaredField("mLayoutThemeSwitch");

                field.setAccessible(true);

                Log.e("qidian", "qidianChangeTheme: instance " + instance);

//                Object mLayoutThemeSwitch = getObjectField(
//                        callMethod(
//                                instance,
//                                "getView"
//                        ),
//                        "mLayoutThemeSwitch"
//                );

                View linearLayout = new LinearLayout(context);

//                Log.e("qidian", "qidianChangeTheme:mLayoutThemeSwitch " + mLayoutThemeSwitch);

//                ((LinearLayout) mLayoutThemeSwitch).callOnClick();

                Activity activity = (Activity) callMethod(
                        instance,
                        "getActivity"
                );
                Log.e("qidian", "qidianChangeTheme:activity " + activity);
                int viewId = activity.getResources().getIdentifier("themeSwitch", "id", context.getPackageName());

                Log.e("qidian", "qidianChangeTheme:viewId " + viewId);

                linearLayout.setId(viewId);
//                 View view=activity.findViewById(viewId);
                Log.e("qidian", "qidianChangeTheme:linearLayout000 " + linearLayout.getId());
                callMethod(
                        instance,
                        "OnClick",
                        linearLayout
                );

                Log.e("qidian", "qidianChangeTheme:linearLayout 111" + linearLayout.getId());
            }


//            try {
//                Field lastThemeChange = findFieldIfExists(MainFragmentActivityClass, "lastThemeChange");
//                if (lastThemeChange != null) {
//                    lastThemeChange.setLong(null, 0);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            //MenuItem
//            Object navChangeTheme = getObjectField(
//                    instance,
//                    "navChangeTheme"
//            );
//            //SwitchCompat
//            Object actionView = callMethod(
//                    navChangeTheme,
//                    "getActionView"
//            );
//
////            androidx.appcompat.widget.SwitchCompat cannot be cast to androidx.
////            appcompat.widget.SwitchCompat
////                    ((SwitchCompat) actionView).setChecked(isNightMode);
//
//            callMethod(
//                    actionView,
//                    "setChecked",
//                    isNightMode
//            );

        } catch (Exception e) {
            Log.e("qidian", "Exception onConfigurationChanged  \n" + e);
        }

    }


    private void hookCloudMusic() {
        if (sharedPreferences != null && sharedPreferences.getBoolean("x_cloudmusic", true)) {

            ClassLoader cl = context.getClassLoader();
            //cl.loadclass("className")找其他类
            //Class.forName("className",true,cl)
            Class<?> hookclass0, hookclass1, hookclass2;

            String clazzName = "com.netease.cloudmusic.ui.MainDrawer";
            hookclass0 = loadClass(cl, clazzName);

            clazzName = "com.netease.cloudmusic.activity.MainActivity";
            hookclass1 = loadClass(cl, clazzName);

            clazzName = "com.netease.cloudmusic.theme.core.ResourceRouter";
            hookclass2 = loadClass(cl, clazzName);


            try {

                final Object[] MainDrawer = {null};

                findAndHookConstructor(hookclass0,
                        hookclass1,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                MainDrawer[0] = param.thisObject;
                            }
                        }
                );

                SwitchCloudMusicTheme switchCloudMusicTheme = new SwitchCloudMusicTheme(MainDrawer, hookclass0, hookclass1, hookclass2);

                switchCloudMusicTheme.findAndHookMethod(
                        hookclass1,
                        "onCreate",
                        Bundle.class
                );

                switchCloudMusicTheme.findAndHookMethod(
                        Application.class,
                        "onConfigurationChanged",
                        Configuration.class
                );


//                findAndHookMethod(Application.class,
//                        "onConfigurationChanged",
//                        Configuration.class,
//                        new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//
//                                if (MainDrawer[0] == null) {
//                                    return;
//                                }
//                                boolean isNightMode = isnightMode(context);
//
//
//                                Object instance_ResourceRouter = callStaticMethod(
//                                        hookclass2,
//                                        "getInstance"
//                                );
//
//                                boolean isNightTheme = (boolean) callMethod(
//                                        instance_ResourceRouter,
//                                        "isNightTheme"
//
//                                );
//
//                                if (isNightTheme != isNightMode) {
//                                    Object drawerThemeMode = getObjectField(
//                                            MainDrawer[0],
//                                            "drawerThemeMode"
//                                    );
//                                    Log.e("x_cloudmusic", "drawerThemeMode " + drawerThemeMode);
//
//                                    ((View) drawerThemeMode).callOnClick();
//                                }
//
//                                Log.e("x_cloudmusic", "isNightMode" + isNightMode);
//
//                                int getCurrentThemeId = (int) callStaticMethod(
//                                        findClass(
//                                                "com.netease.cloudmusic.theme.core.ThemeConfig",
//                                                loadPackageParam.classLoader
//                                        ),
//                                        "getCurrentThemeId"
//                                );
//                                Log.e("x_cloudmusic", "getCurrentThemeId" + getCurrentThemeId);
//
//                                Object ThemeInfo = findClass(
//                                        "com.netease.cloudmusic.theme.core.ThemeInfo",
//                                        loadPackageParam.classLoader
//                                ).getConstructor(int.class)
//                                        .newInstance(getCurrentThemeId);
//
//                                Log.e("x_cloudmusic", "ThemeInfo " + ThemeInfo);
//
//                                boolean isNightheme = (int) callMethod(
//                                        ThemeInfo,
//                                        "getId"
//                                ) == -3;
//
//                                Log.e("x_cloudmusic", "isNightheme " + isNightheme);
//
//
//                                if (isNightheme != isNightMode) {
//                                    Object drawerThemeMode = getObjectField(
//                                            MainDrawer[0],
//                                            "drawerThemeMode"
//                                    );
//                                    Log.e("x_cloudmusic", "drawerThemeMode " + drawerThemeMode);
//
//                                    ((View) drawerThemeMode).callOnClick();
////
////                                      ((View) drawerThemeMode).performClick();
//
//                                    View.OnClickListener clickListener = getOnClickListener((View) drawerThemeMode);
//
//                                    Log.e("x_cloudmusic", "clickListener " + clickListener);
////                                            clickListener.onClick((View) drawerThemeMode);
//
//                                    ClassLoader classLoader = loadPackageParam.classLoader;
//
//                                    Class<?> hookclass0 = classLoader.loadClass("com.netease.cloudmusic.ui.MainDrawer$10");
//                                    Class<?> hookclass1 = classLoader.loadClass("com.netease.cloudmusic.ui.MainDrawer");
//
//                                    Log.e("x_cloudmusic", "hookclass0 " + hookclass0);
//                                    Log.e("x_cloudmusic", "hookclass01 " + hookclass1);
//
//                                    Constructor<?> constructor = hookclass0
//                                            .getDeclaredConstructor(
//                                                    hookclass1
//                                            );
//
//                                    Log.e("x_cloudmusic", "constructor " + constructor);
//
//                                    Log.e("x_cloudmusic", "constructor " + constructor.isAccessible());
//
//                                    constructor.setAccessible(true);
//
//
//                                    Log.e("x_cloudmusic", "constructor " + constructor.isAccessible());
//
//                                    Object mainDrawer$10 = constructor.newInstance(MainDrawer[0]);
//
//                                    getObjectField(
//                                            MainDrawer[0],
//                                            "drawerThemeMode"
//                                    );
//                                    Log.e("x_cloudmusic", "MainDrawer$10 " + mainDrawer$10);
//
//                                    View view = null;
//                                    callMethod(
//                                            mainDrawer$10,
//                                            "onClick",
//                                            view
//
//                                    );
//                                }
//                            }
//                        }
//                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

//    public View.OnClickListener getOnClickListener(View view) {
//        View.OnClickListener listener = null;
//        try {
//            Field listenerInfoField;
//            listenerInfoField = View.class
//                    .getDeclaredField("mListenerInfo");
//            if (listenerInfoField != null) {
//                listenerInfoField.setAccessible(true);
//            }
//            Object myLiObject = null;
//            myLiObject = listenerInfoField.get(view);
//
//            Field listenerField = null;
//            listenerField = Class.forName("android.view.View$ListenerInfo")
//                    .getDeclaredField("mOnClickListener");
//            if (listenerField != null && myLiObject != null) {
//                listener = (View.OnClickListener) listenerField
//                        .get(myLiObject);
//            }
//        } catch (Exception ex) {
//            listener = null;
//        }
//        return listener;
//    }


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

    private void hookSdkInt(int versionSdk) {
        try {
            XposedHelpers.setStaticObjectField(Build.VERSION.class, "SDK_INT", versionSdk);
            Log.e("themejd", " hookSdkInt ||| " + versionSdk);
        } catch (Exception e) {
            e.printStackTrace();
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


    private void hookIflytekInput() {


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

                            hookCustomIflytekInput(x_iflytek_input_config);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookCustomIflytekInput(String clazzName) {

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


    private void hookCaijSee() {


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

                                hookCustomCaijSee(className, methodName);
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookCustomCaijSee(String className, String methodName) {

        try {
            findAndHookMethod(className, loadPackageParam.classLoader, methodName,
                    Context.class,
                    XC_MethodReplacement.returnConstant(true)
            );
        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }


    private void hookGboard() {

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

                                hookCustomGboard(className, methodName, context);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void hookCustomGboard(String className, String methodName, Context context) {

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


    private void hookBili(String packageName) {

        String x_bili_config;
        if (sharedPreferences != null) {
            String key = "x_bili";
            if (Constants.PACKAGE_NAME_BILIBILI_IN.equals(packageName)) {
                key = "x_bili_in";
            }

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_bili_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_bili_config", key + "_config——" + x_bili_config);

                try {
                    if (!"".equals(x_bili_config)) {
                        assert x_bili_config != null;
                        x_bili_config = x_bili_config.replaceAll("\\s*", "").replaceAll("，", ",").replaceAll("：", ":");

                        String[] splits = x_bili_config.split(":");
                        if (splits.length >= 3) {
                            String[] splits0 = splits[0].split(",");
                            String[] splits1 = splits[1].split(",");
                            String[] splits2 = splits[2].split(",");
                            if (splits0.length >= 4 && splits1.length >= 2 && splits2.length >= 3) {
                                hookCustomBili(splits0, splits1, splits2);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

//        if (sharedPreferences != null && sharedPreferences.getBoolean("x_bili", true)) {
//
//            Log.e("bili", "\n\n 开始 hookBili");
//
//
//            ClassLoader cl = context.getClassLoader();
//            //cl.loadclass("className")找其他类
//            //Class.forName("className",true,cl)
//            Class<?> hookclass1, hookclass2, hookclass3, hookclass4;
//            String clazzName1, clazzName2, clazzName3, clazzName4;
//
//
//            String clazzName = "tv.danmaku.bili.ui.theme.g";
//            hookclass1 = loadClass(cl, clazzName);
//
//            clazzName = "com.bilibili.base.BiliGlobalPreferenceHelper";
//            hookclass2 = loadClass(cl, clazzName);
//
//            clazzName = "tv.danmaku.bili.ui.garb.GarbManagerDelegate";
//            hookclass3 = loadClass(cl, clazzName);
//
//            clazzName = "com.bilibili.lib.homepage.ThemeWatcher";
//            hookclass4 = loadClass(cl, clazzName);
//
//            if (hookclass1 == null || hookclass2 == null || hookclass3 == null || hookclass4 == null) {
//                Log.e("bilibili", "MutiDex 寻找四个class 失败");
//            }
//            try {
//
//                findAndHookMethod(Application.class,
//                        "onConfigurationChanged",
//                        Configuration.class,
//                        new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                super.afterHookedMethod(param);
//
//                                Log.e("bilibili", "onConfigurationChanged 0000000000");
//
//                                try {
//                                    boolean isNightMode = isnightMode(context);
//
//                                    Object BiliGlobalPreferenceHelperinstance = callStaticMethod(hookclass2,
//                                            "getInstance",
//                                            context);
//
//                                    //当前皮肤
//                                    int m2 = (int) callStaticMethod(hookclass1,
//                                            "m",
//                                            context);
//
//
//                                    int i2 = 1;
//                                    if (isNightMode) {
//                                        if (m2 == 1) {
//                                            return;
//                                        }
//                                    } else {
//                                        if (m2 != 1) {
//                                            return;
//                                        } else {
//                                            int optInteger = (int) callMethod(BiliGlobalPreferenceHelperinstance, "optInteger", "theme_entries_last_key", 2);
//                                            i2 = optInteger == 1 ? 8 : optInteger;
//                                        }
//                                    }
//
//                                    callMethod(BiliGlobalPreferenceHelperinstance, "setInteger", "theme_entries_last_key", m2);
//                                    callMethod(BiliGlobalPreferenceHelperinstance, "setInteger", "theme_entries_current_key", i2);
//
//                                    callStaticMethod(hookclass3,
//                                            "O",
//                                            i2);
//
//                                    Object ThemeWatcherinstance = callStaticMethod(hookclass4,
//                                            "getInstance");
//
//                                    callMethod(ThemeWatcherinstance, "onChanged");

//
//                                } catch (Exception e) {
//                                    Log.e("bilibili", "Exception onConfigurationChanged  \n" + e);
//                                }
//
//                                Log.e("bilibili", "onConfigurationChanged  11111111");
//
//                            }
//                        }
//                );
//            } catch (Exception e) {
//                Log.e("bilibili", "hook" + "Exception");
//                XposedBridge.log("" + e);
//            }
//        }
    }

    /**
     * 哔哩哔哩根据以下代码切换主题
     * <p>
     * //tv.danmaku.bili.ui.theme.g
     * public static int m(Context context) {
     * //获取当前主题
     * return BiliGlobalPreferenceHelper.getInstance(context).optInteger("theme_entries_current_key", 2);
     * }
     * <p>
     * <p>
     * public static void q(Context context) {
     * //com.bilibili.base.BiliGlobalPreferenceHelper
     * BiliGlobalPreferenceHelper instance = BiliGlobalPreferenceHelper.getInstance(context);
     * <p>
     * //也可替换成  instance.optInteger("theme_entries_current_key", 2);
     * int m2 = m(context);
     * <p>
     * int i2 = 1;
     * if (m2 == 1) {
     * int optInteger = instance.optInteger("theme_entries_last_key", 2);
     * i2 = optInteger == 1 ? 8 : optInteger;
     * }
     * instance.setInteger("theme_entries_last_key", m2);
     * instance.setInteger("theme_entries_current_key", i2);
     * <p>
     * //这行无用
     * //o = i2;
     * <p>
     * //tv.danmaku.bili.ui.garb.GarbManagerDelegate
     * GarbManagerDelegate.O(i2);
     * <p>
     * //com.bilibili.lib.homepage.ThemeWatcher
     * ThemeWatcher.getInstance().onChanged();
     * }
     *
     * @param splits0 com.bilibili.base.BiliGlobalPreferenceHelper, getInstance, optInteger, setInteger:
     * @param splits1 tv.danmaku.bili.ui.garb.GarbManagerDelegate, O:
     * @param splits2 com.bilibili.lib.homepage.ThemeWatcher, getInstance, onChanged
     */
    private void hookCustomBili(String[] splits0, String[] splits1, String[] splits2) {

        Log.e("bilibili", "\n\n 开始 hookBili");

        ClassLoader cl = context.getClassLoader();
        //cl.loadclass("className")找其他类
        //Class.forName("className",true,cl)
        Class<?> hookclass0, hookclass1, hookclass2;

        String clazzName = splits0[0];
        hookclass0 = loadClass(cl, clazzName);

        clazzName = splits1[0];
        hookclass1 = loadClass(cl, clazzName);

        clazzName = splits2[0];
        hookclass2 = loadClass(cl, clazzName);

        if (hookclass0 == null || hookclass1 == null || hookclass2 == null) {
            Log.e("bilibili", "MutiDex 寻找三个class 失败");
        }

        try {
            findAndHookMethod(Application.class,
                    "onConfigurationChanged",
                    Configuration.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            Log.e("bilibili", "onConfigurationChanged 0000000000");

                            try {
                                boolean isNightMode = isnightMode(context);

                                Object instance_BiliGlobalPreferenceHelper = callStaticMethod(
                                        hookclass0,
                                        splits0[1],
                                        context
                                );

                                //当前皮肤
                                int m2 = (int) callMethod(
                                        instance_BiliGlobalPreferenceHelper,
                                        splits0[2],
                                        "theme_entries_current_key",
                                        2
                                );

                                int i2 = 1;

                                if (isNightMode) {
                                    if (m2 == 1) {
                                        return;
                                    }
                                } else {
                                    if (m2 != 1) {
                                        return;
                                    } else {
                                        int optInteger = (int) callMethod(
                                                instance_BiliGlobalPreferenceHelper,
                                                splits0[2],
                                                "theme_entries_last_key",
                                                2
                                        );
                                        i2 = optInteger == 1 ? 8 : optInteger;
                                    }
                                }

                                callMethod(
                                        instance_BiliGlobalPreferenceHelper,
                                        splits0[3],
                                        "theme_entries_last_key",
                                        m2
                                );
                                callMethod(
                                        instance_BiliGlobalPreferenceHelper,
                                        splits0[3],
                                        "theme_entries_current_key",
                                        i2
                                );

                                callStaticMethod(
                                        hookclass1,
                                        splits1[1],
                                        i2
                                );

                                Object instance_ThemeWatcher = null;
                                try {
                                    instance_ThemeWatcher = callStaticMethod(
                                            hookclass2,
                                            splits2[1]
                                    );
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                callMethod(
                                        instance_ThemeWatcher,
                                        splits2[2]
                                );

                            } catch (Exception e) {
                                Log.e("bilibili", "Exception onConfigurationChanged  \n" + e);
                            }

                            Log.e("bilibili", "onConfigurationChanged  11111111");
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("bilibili", "hook" + "Exception");
            XposedBridge.log("" + e);
        }
    }


    private Class<?> loadClass(ClassLoader cl, String clazzName) {
        Class<?> hookclass;

        try {
            hookclass = cl.loadClass(clazzName);
        } catch (Exception e) {
            Log.e("MutiDex ", "MutiDex 寻找" + clazzName + "失败" + e);
            return null;
        }
        Log.e("MutiDex ", "MutiDex 寻找" + clazzName + "成功");
        return hookclass;
    }


    private void hookQuark() {

        String x_quark_config;
        if (sharedPreferences != null) {
            String key = "x_quark";

            if (sharedPreferences.getBoolean(key, true)) {
                //获取自定义
                x_quark_config = sharedPreferences.getString(key + "_config", "");
                Log.e("x_quark_config", key + "_config——" + x_quark_config);

                try {
                    if (!"".equals(x_quark_config)) {
                        assert x_quark_config != null;
                        x_quark_config = x_quark_config.replaceAll("\\s*", "").replaceAll("，", ",").replaceAll("：", ":");

                        String[] splits = x_quark_config.split(":");
                        if (splits.length >= 2) {
                            String[] splits0 = splits[0].split(",");
                            String[] splits1 = splits[1].split(",");
                            if (splits.length >= 3) {
                                String[] splits2 = splits[2].split(",");
                                if (splits0.length >= 3 && splits1.length >= 3 && splits2.length >= 2) {
                                    hookCustomQuark(splits1, splits2);
                                }
                            } else {
                                if (splits0.length >= 3 && splits1.length >= 2) {
                                    hookCustomQuark(splits0, splits1);
                                }
                            }

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 夸克根据以下代码修改暗夜模式
     * com.ucpro.model.a.a$a.aMu().setBoolean("setting_night_mode", com.ucpro.model.a.a$a.aMu().getBoolean("setting_night_mode", false) ^ true);
     * com.ucweb.common.util.l.e.aSS().ox(com.ucweb.common.util.l.f.gtF);
     *
     * @param splits0 com.ucpro.model.a.a$a, aMu, setBoolean:
     * @param splits1 com.ucweb.common.util.l.e, aSS, ox:
     * @param splits2 com.ucweb.common.util.l.f, gtF
     */
    @Deprecated
    private void hookCustomQuark(String[] splits0, String[] splits1, String[] splits2) {

        Log.e("quarkk", "\n\n 开始 hookQuarkk");

        ClassLoader cl = context.getClassLoader();
        //cl.loadclass("className")找其他类
        //Class.forName("className",true,cl)
        Class<?> hookclass0, hookclass1, hookclass2;


        String clazzName = splits0[0];
        hookclass0 = loadClass(cl, clazzName);

        clazzName = splits1[0];
        hookclass1 = loadClass(cl, clazzName);


        clazzName = splits2[0];
        hookclass2 = loadClass(cl, clazzName);

        if (hookclass0 == null || hookclass1 == null || hookclass2 == null) {
            Log.e("quarkk", "MutiDex 寻找三个class 失败");
        }


        try {
            findAndHookMethod(Application.class,
                    "onConfigurationChanged",
                    Configuration.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            Log.e("quarkk", "onConfigurationChanged 0000000000");

                            try {
                                boolean isNightMode = isnightMode(context);

                                Object instance_PreferenceHelper = callStaticMethod(
                                        hookclass0,
                                        splits0[1]
                                );

                                callMethod(
                                        instance_PreferenceHelper,
                                        splits0[2],
                                        "setting_night_mode",
                                        isNightMode
                                );

                                Object instance_switchDark = callStaticMethod(
                                        hookclass1,
                                        splits1[1]
                                );

                                callMethod(
                                        instance_switchDark,
                                        splits1[2],
                                        getStaticIntField(hookclass2, splits2[1])
                                );


                            } catch (Exception e) {
                                Log.e("quarkk", "Exception onConfigurationChanged  \n" + e);
                            }

                            Log.e("quarkk", "onConfigurationChanged  11111111");
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("quarkk", "hook" + "Exception");
            XposedBridge.log("" + e);
        }


//        try {
//
//            Method[] m = hookclass1.getDeclaredMethods();
//            // 打印获取到的所有的类方法的信息
//            for (Method method : m) {
//
//                //XposedBridge.log("HOOKED CLASS-METHOD: "+strClazz+"-"+m[i].toString());
//                if (!Modifier.isAbstract(method.getModifiers())           // 过滤掉指定名称类中声明的抽象方法
//                        && !Modifier.isNative(method.getModifiers())     // 过滤掉指定名称类中声明的Native方法
//                        && !Modifier.isInterface(method.getModifiers())  // 过滤掉指定名称类中声明的接口方法
//                ) {
//
//                    // 对指定名称类中声明的非抽象方法进行java Hook处理
//                    XposedBridge.hookMethod(method, new XC_MethodHook() {
//
//                        // 被java Hook的类方法执行完毕之后，打印log日志
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                            Log.e("quarkk", "HOOKED METHOD: " + strClassName + "-" + param.method.toString() + " | \n");
//                            StringBuilder stringBuilder = new StringBuilder();
//                            for (Object value : param.args) {
//                                stringBuilder.append(value).append(" || ");
//                            }
//
//                            Log.e("quarkk", "HOOKED METHOD: " + strClassName + "-" + param.method.toString() + " | \n\n\n" + stringBuilder);
//
//                            Log.e("quarkk", "HOOKED METHOD\n: ------------");
//                            // 打印被java Hook的类方法的名称和参数类型等信息
////                            XposedBridge.log("HOOKED METHOD: "+strClassName+"-"+param.method.toString());
//                        }
//                    });
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void hookCustomQuark(String[] splits0, String[] splits1) {
        hookCustomQuarkOrUCTurbo(
                "quarkk",
                "com.ucpro.MainActivity",
                splits0,
                splits1
        );
    }

    private void hookCustomQuarkOrUCTurbo(String TAG, String clazzName, String[] splits0, String[] splits1) {

        Log.e(TAG, "\n\n 开始 hook " + TAG);

        ClassLoader cl = context.getClassLoader();
        //cl.loadclass("className")找其他类
        //Class.forName("className",true,cl)
        Class<?> hookclassMain, hookclass0, hookclass1;

        hookclassMain = loadClass(cl, clazzName);

        clazzName = splits0[0];
        hookclass0 = loadClass(cl, clazzName);


        clazzName = splits1[0];
        hookclass1 = loadClass(cl, clazzName);

        if (hookclassMain == null || hookclass0 == null || hookclass1 == null) {
            Log.e(TAG, "MutiDex 寻找三个class 失败");
        }

//        SharedPreferences.Editor editor = context.getSharedPreferences("settings", 0).edit();


        switchQuarkOrUC switchQuarkOrUC = new switchQuarkOrUC(
                context,
                TAG,
                hookclass0, hookclass1,
                splits0, splits1
        );

        switchQuarkOrUC.findAndHookMethod(hookclassMain, "onCreate", Bundle.class);
        switchQuarkOrUC.findAndHookMethod(Application.class, "onConfigurationChanged", Configuration.class);


//        try {
//            findAndHookMethod(
//                    hookclassMain,
//                    methodName,
//                    Bundle.class,
//                    new CallPrepare( TAG,
//                            editor,
//                            hookclass0, hookclass1,
//                            splits0, splits1
//                    )
////                    new XC_MethodHook() {
////                        @Override
////                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                            super.afterHookedMethod(param);
////
////                            Log.e(TAG, "onConfigurationChanged 0000000000");
////
////                            try {
////                                boolean isNightMode = isnightMode(context);
////
////                                editor.putBoolean("setting_night_mode", isNightMode);
////                                editor.apply();
////
////                                Object instance_switchDark = callStaticMethod(
////                                        hookclass0,
////                                        splits0[1]
////                                );
////
////                                callMethod(
////                                        instance_switchDark,
////                                        splits0[2],
////                                        getStaticIntField(hookclass1, splits1[1])
////                                );
////
////
////                            } catch (Exception e) {
////                                Log.e(TAG, "Exception onConfigurationChanged  \n" + e);
////                            }
////
////                            Log.e(TAG, "onConfigurationChanged  11111111");
////                        }
////                    }
//            );
//        } catch (Exception e) {
//            Log.e(TAG, "hook" + "Exception");
//            XposedBridge.log("" + e);
//        }

//        try {
//            findAndHookMethod(Application.class,
//                    "onConfigurationChanged",
//                    new Object[]{"dsf", "sd"
//                    },
//                    Configuration.class,
//                    new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            super.afterHookedMethod(param);
//
//                            Log.e(TAG, "onConfigurationChanged 0000000000");
//
//                            try {
//                                boolean isNightMode = isnightMode(context);
//
//                                editor.putBoolean("setting_night_mode", isNightMode);
//                                editor.apply();
//
//                                Object instance_switchDark = callStaticMethod(
//                                        hookclass0,
//                                        splits0[1]
//                                );
//
//                                callMethod(
//                                        instance_switchDark,
//                                        splits0[2],
//                                        getStaticIntField(hookclass1, splits1[1])
//                                );
//
//
//                            } catch (Exception e) {
//                                Log.e(TAG, "Exception onConfigurationChanged  \n" + e);
//                            }
//
//                            Log.e(TAG, "onConfigurationChanged  11111111");
//                        }
//                    }
//            );
//        } catch (Exception e) {
//            Log.e(TAG, "hook" + "Exception");
//            XposedBridge.log("" + e);
//        }

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

    private boolean isnightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private void hookReturnBooleanWithmethodNames(ClassLoader classLoader, String
            className, String[] methodNames, boolean booleanVlaue) {

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

    private void hookReturnBooleanWithmethodName(ClassLoader classLoader, String
            className, String methodName, boolean booleanVlaue) {
        hookReturnBooleanWithmethodNames(classLoader, className, new String[]{methodName}, booleanVlaue);
    }

    private void findAndHookMethodReturnBoolean(ClassLoader classLoader, String
            className, String methodName, boolean booleanVlaue) {

        try {
            findAndHookMethod(className, classLoader,
                    methodName,
                    XC_MethodReplacement.returnConstant(booleanVlaue)
            );

        } catch (Exception e) {
            XposedBridge.log("" + e);
        }
    }

    class switchQuarkOrUC {
        Context context;
        String TAG;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
        Class<?> hookclass0, hookclass1;
        String[] splits0, splits1;

        public switchQuarkOrUC(Context context, String TAG, Class<?> hookclass0, Class<?> hookclass1, String[] splits0, String[] splits1) {
            this.context = context;
            this.TAG = TAG;
            this.hookclass0 = hookclass0;
            this.hookclass1 = hookclass1;
            this.splits0 = splits0;
            this.splits1 = splits1;
        }


        public void findAndHookMethod(Class<?> clazz, String methodName, Class<?> singclass) {
            try {
                XposedHelpers.findAndHookMethod(clazz,
                        methodName,
                        singclass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e(TAG, methodName + " 0000000000");

                                sharedPreferences = context.getSharedPreferences("settings", 0);
                                editor = sharedPreferences.edit();
                                try {

                                    Log.e(TAG, "isSettingnightMode===: 0get " + sharedPreferences.getBoolean("setting_night_mode", false));
                                    boolean isNightMode = isnightMode(context);
                                    Log.e(TAG, "isnightMode: " + isNightMode);

                                    editor.putBoolean("setting_night_mode", isNightMode).apply();


                                    Log.e(TAG, "isSettingnightMode===: 2get" + sharedPreferences.getBoolean("setting_night_mode", false));


                                    Object instance_switchDark = callStaticMethod(
                                            hookclass0,
                                            splits0[1]
                                    );

                                    try {
                                        callMethod(
                                                instance_switchDark,
                                                splits0[2],
                                                getStaticIntField(hookclass1, splits1[1])
                                        );
                                    } catch (Exception e) {
                                        Log.e(TAG, "callMethod   " + e);
                                    }

                                    Log.e(TAG, "isSettingnightMode===: 3get" + sharedPreferences.getBoolean("setting_night_mode", false));

                                } catch (Exception e) {
                                    Log.e(TAG, "Exception" + methodName + "  \n" + e);
                                }

                                Log.e(TAG, methodName + "  11111111");
                            }
                        }
                );
            } catch (
                    Exception e) {
                Log.e(TAG, "hook" + "Exception");
                XposedBridge.log("" + e);
            }
        }
    }


    class SwitchCloudMusicTheme {
        String TAG = "cloudmusic";
        Object[] MainDrawer;
        Class<?> hookclass0, hookclass1, hookclass2;

        public SwitchCloudMusicTheme(Object[] MainDrawer, Class<?> hookclass0, Class<?> hookclass1, Class<?> hookclass2) {

            Log.e(TAG, "赋值 mainDrawer : " + MainDrawer);
            this.MainDrawer = MainDrawer;
            this.hookclass0 = hookclass0;
            this.hookclass1 = hookclass1;
            this.hookclass2 = hookclass2;
        }


        public void findAndHookMethod(Class<?> tohookclass, String methodName, Class<?> singleArg) {

            Log.e(TAG, "findAndHookMethod: " + MainDrawer[0]);
            try {
                XposedHelpers.findAndHookMethod(
                        tohookclass,
                        methodName,
                        singleArg,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);

                                Log.e(TAG, "开始 afterHookedMethod " + methodName);

                                if (MainDrawer[0] == null) {
                                    return;
                                }

                                Log.e(TAG, "开始 afterHookedMethod==: " + MainDrawer[0]);


                                boolean isNightMode = isnightMode(context);

                                Log.e(TAG, "afterHookedMethod: " + isNightMode);

                                Object instance_ResourceRouter = callStaticMethod(
                                        hookclass2,
                                        "getInstance"
                                );

                                Log.e(TAG, "afterHookedMethod: " + instance_ResourceRouter);

                                boolean isNightTheme = (boolean) callMethod(
                                        instance_ResourceRouter,
                                        "isNightTheme"

                                );

                                Log.e(TAG, "afterHookedMethod: " + isNightTheme);

                                if (isNightTheme != isNightMode) {
                                    View drawerThemeMode = (View) getObjectField(
                                            MainDrawer[0],
                                            "drawerThemeMode"
                                    );
                                    Log.e(TAG, "afterHookedMethod: " + drawerThemeMode);
                                    drawerThemeMode.callOnClick();
                                }

                            }
                        }

                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
