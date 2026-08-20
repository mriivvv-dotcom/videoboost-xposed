package com.custom.videoboost;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.os.Process;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.google.android.GoogleCamera")) {
            return;
        }

        // Определение второго пространства (User 10)
        int userId = Process.myUid() / 100000;
        XposedBridge.log("[VideoBoost] GCam launched. User ID: " + userId);

        if (userId != 10) {
            XposedBridge.log("[VideoBoost] Not User 10, skipping.");
            return;
        }

        XposedBridge.log("[VideoBoost] User 10 confirmed! Injecting hooks...");

        // 1. Принудительно отдаем TRUE на чтение булевых флагов Video Boost
        XposedHelpers.findAndHookMethod(
            "android.app.SharedPreferencesImpl",
            lpparam.classLoader,
            "getBoolean",
            String.class,
            boolean.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key != null && (key.contains("sapphire") || key.contains("video_boost") || key.contains("8k") || key.contains("onyx"))) {
                        param.setResult(true);
                    }
                }
            }
        );

        // 2. Принудительно выставляем 8K разрешение для видео
        XposedHelpers.findAndHookMethod(
            "android.app.SharedPreferencesImpl",
            lpparam.classLoader,
            "getString",
            String.class,
            String.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key != null) {
                        if (key.contains("video_resolution") || key.contains("video_quality") || key.contains("camcorder")) {
                            param.setResult("RES_4320P");
                        }
                    }
                }
            }
        );
    }
}
