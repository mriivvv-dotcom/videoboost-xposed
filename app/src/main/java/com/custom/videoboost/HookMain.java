package com.custom.videoboost;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.os.Process;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.google.android.GoogleCamera")) {
            return;
        }

        // Проверяем, что запуск во 2-м пространстве (User 10)
        int userId = Process.myUserHandle().hashCode();
        if (userId != 10) {
            return;
        }

        // Принудительно отдаем TRUE на чтение настроек Video Boost / 8K
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
                    if (key != null && (key.contains("sapphire") || key.contains("video_boost") || key.contains("8k"))) {
                        param.setResult(true);
                    }
                }
            }
        );
    }
}
