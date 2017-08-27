package br.com.wespa.wchat.helpers;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by dsouza on 26/08/17.
 */

public abstract class ServiceHelper {

    public static boolean isRunning(Context context, Class<?> serviceClass) {
        String name = serviceClass.getName();

        ActivityManager manager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> services =
                manager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo service : services)
            if (name.equals(service.service.getClassName()))
                return true;

        return false;
    }

}
