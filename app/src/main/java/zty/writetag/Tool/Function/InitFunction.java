package zty.writetag.Tool.Function;

import android.app.Application;

/**
 * Created by 郑童宇 on 2016/05/24.
 */
public class InitFunction {
    public static synchronized void Initialise(Application application) {
        FileFunction.InitStorage(application);

        LogFunction.UpdateErrorOutputStream();
    }
}
