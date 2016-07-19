package zty.writetag.Control.Main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import zty.writetag.R;

import com.Tool.Common.CommonApplication;
import com.Tool.Function.FileFunction;
import com.Tool.Function.LogFunction;
import com.Tool.Function.MusicFunction;
import com.Tool.Global.Variable;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    // 目前只支持对无标签音乐写入标签，不支持覆盖标签
    private void writeTag() {
        byte buffer[] = new byte[1024];

        String tempFilepath = Variable.StorageDirectoryPath + "temp.mp3";
        String ID3V1TagTempFilepath = Variable.StorageDirectoryPath + "id3v1temp.mp3";
        String ID3V2TagTempFilepath = Variable.StorageDirectoryPath + "id3v2temp.mp3";
        String ID3V1TagFilepath = Variable.StorageDirectoryPath + "id3v1.mp3";
        String ID3V2TagFilepath = Variable.StorageDirectoryPath + "id3v2.mp3";

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = FileFunction.GetFileOutputStreamFromFile(tempFilepath);

        try {
            inputStream = getResources().openRawResource(R.raw.test);

            if (fileOutputStream != null) {
                while (inputStream.read(buffer) > -1) {
                    fileOutputStream.write(buffer);
                }

                FileFunction.CopyFile(tempFilepath, ID3V1TagTempFilepath);
                FileFunction.CopyFile(tempFilepath, ID3V2TagTempFilepath);
                FileFunction.DeleteFile(tempFilepath);
            }
        } catch (Exception e) {
            LogFunction.error("write file异常", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogFunction.error("close file异常", e);
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LogFunction.error("close file异常", e);
                }
            }

            inputStream = null;
            fileOutputStream = null;
        }

        MusicFunction.StorageMusicFileWithID3V1Tag(new File(ID3V1TagTempFilepath), ID3V1TagFilepath,
                "歌名 songName id3v1", "歌手名 artistName id3v1", "专辑名 albumName id3v1");

        MusicFunction.StorageMusicFileWithID3V2Tag(new File(ID3V2TagTempFilepath), ID3V2TagFilepath,
                "歌名 songName id3v2", "歌手名 artistName id3v2", "专辑名 albumName id3v2");
    }

    public void writeTag(View v) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                writeTag();
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        CommonApplication.getInstance().showToast("写入标签成功", "MainActivity");
                    }

                    @Override
                    public void onError(Throwable e) {
                        CommonApplication.getInstance().showToast("写入标签失败", "MainActivity");
                        LogFunction.error("写入标签异常", e.toString());
                    }

                    @Override
                    public void onNext(String string) {
                    }
                });
    }
}
