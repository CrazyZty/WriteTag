package zty.writetag.Control.Main;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import zty.writetag.R;
<<<<<<< HEAD
import zty.writetag.Tool.Function.FileFunction;
import zty.writetag.Tool.Function.LogFunction;
import zty.writetag.Tool.Function.MusicFunction;
import zty.writetag.Tool.Global.Variable;
=======
import zty.writetag.Tool.Common.CommonApplication;
import zty.writetag.Tool.Function.FileFunction;
import zty.writetag.Tool.Function.LogFunction;
import zty.writetag.Tool.Function.MusicFunction;
import zty.writetag.Tool.Gobal.Variable;
>>>>>>> 66c2714dee62ce5e556a29c4b6353bc807449db5

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

    public void writeTag(View v) {
        byte buffer[] = new byte[1024];

        String tempFilepath = Variable.StorageDirectoryPath + "temp.mp3";
        String ID3V1TagTempFilepath = Variable.StorageDirectoryPath + "id3v1temp.mp3";
        String ID3V2TagTempFilepath = Variable.StorageDirectoryPath + "id3v2temp.mp3";
        String ID3V1TagFilepath = Variable.StorageDirectoryPath + "id3v1.mp3";
        String ID3V2TagFilepath = Variable.StorageDirectoryPath + "id3v2.mp3";

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = FileFunction.GetFileOutputStreamFromFile(tempFilepath);

        try {
<<<<<<< HEAD
=======
            fileOutputStream = new FileOutputStream(tempFilepath);

>>>>>>> 66c2714dee62ce5e556a29c4b6353bc807449db5
            inputStream = getResources().openRawResource(R.raw.test);

            if (fileOutputStream != null) {
                while (inputStream.read(buffer) > -1) {
                    fileOutputStream.write(buffer);
                }

                FileFunction.CopyFile(tempFilepath, ID3V1TagTempFilepath);
                FileFunction.CopyFile(tempFilepath, ID3V2TagTempFilepath);
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
<<<<<<< HEAD
=======

        CommonApplication.getInstance().showToast("已写入标签", "MainActivity");
>>>>>>> 66c2714dee62ce5e556a29c4b6353bc807449db5
    }
}
