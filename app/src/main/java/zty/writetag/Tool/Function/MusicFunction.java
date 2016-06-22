package zty.writetag.Tool.Function;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by zhengtongyu on 16/5/23.
 */
public class MusicFunction {
    public static void StorageMusicFileWithID3V1Tag(File sourceFile, String musicFilePath,
                                                    String songName, String artistName,
                                                    String albumName) {
        try {
            sourceFile.renameTo(new File(musicFilePath));

            RandomAccessFile musicRandomAccessFile = new RandomAccessFile(musicFilePath, "rw");
            musicRandomAccessFile.seek(musicRandomAccessFile.length() - 128); // 跳到ID3V1开始的位置

            byte[] tag = new byte[3];
            musicRandomAccessFile.read(tag);

            if (new String(tag).equals("TAG")) {
                return;
            }

            byte[] tagByteArray = new byte[128];

            musicRandomAccessFile.seek(musicRandomAccessFile.length());

            byte[] songNameByteArray = songName.getBytes("GBK");
            byte[] artistNameByteArray = artistName.getBytes("GBK");
            byte[] albumNameByteArray = albumName.getBytes("GBK");

            int songNameByteArrayLength = songNameByteArray.length;
            int artistNameByteArrayLength = artistNameByteArray.length;
            int albumNameByteArrayLength = albumNameByteArray.length;

            songNameByteArrayLength = songNameByteArrayLength > 30 ? 30 : songNameByteArrayLength;
            artistNameByteArrayLength =
                    artistNameByteArrayLength > 30 ? 30 : artistNameByteArrayLength;
            albumNameByteArrayLength =
                    albumNameByteArrayLength > 30 ? 30 : albumNameByteArrayLength;

            System.arraycopy("TAG".getBytes(), 0, tagByteArray, 0, 3);
            System.arraycopy(songNameByteArray, 0, tagByteArray, 3, songNameByteArrayLength);
            System.arraycopy(artistNameByteArray, 0, tagByteArray, 33, artistNameByteArrayLength);
            System.arraycopy(albumNameByteArray, 0, tagByteArray, 63, albumNameByteArrayLength);

            tagByteArray[127] = (byte) 0xFF; // 将流派显示为指定音乐的流派

            musicRandomAccessFile.write(tagByteArray);
        } catch (Exception e) {
            LogFunction.error("写入音乐标签异常", e);
        }
    }

    //  关于mp3 tag的资料很多，大家可以自己去找，大概讲的都是大同小异，但是我觉得他们忘了很重要的一点，也是我忽略的，后来解析文件出错了，才恍然大悟。那就是tag信息的编码。
    //  大家都知道每个frame的头是由10个字节组成的，具体内容我就不仔细写了，在着10个字节后有一个字节，大家可以仔细观察一下，只有四种情况 00 ，01，02，03，这个代表什么呢？
    //  这个就是表示tag的编码方式的。00代表的就是ISO-8859-1 编码，后面直接跟的就是字符串，关于编码方式我就不做解释了，网上很多，01 代表的就是UTF-16编码，我就是被这个搞晕了，
    //  因为在10个字节的表示后有这个三个字节 01 FF FE，我是直接按照ISO-8859-1编码进行的。所以读取的字符串总是不对，FF FE表示的BOM的顺序。
    //  FF FE表示的就是little endian,FE FF 表示的就是big endian。在这两个字节后面才是真正的字符串。02 代表 UTF16BE 我没有碰到过大概的意思就是UTF16的编码顺序为big endian，
    //  但是这种编码没有FF FE这样的标识。03 表示的UTF8编码，在tag信息是不采用这种表明，但是这种编码在tag里不是错的。
    public static void StorageMusicFileWithID3V2Tag(File sourceFile, String musicFilePath,
                                                    String songName, String artistName,
                                                    String albumName) {
        try {
            RandomAccessFile musicRandomAccessFile =
                    new RandomAccessFile(sourceFile.getAbsolutePath(), "rw");
            musicRandomAccessFile.seek(0);

            byte[] tag = new byte[3];
            musicRandomAccessFile.read(tag);

            if (new String(tag).equals("ID3")) {
                sourceFile.renameTo(new File(musicFilePath));
                return;
            }
        } catch (Exception e) {
            LogFunction.error("存储音乐文件异常", e);
        }

        try {
            byte[] encodeByte = {3}; // 03 表示的UTF8编码
            byte[] tagByteArray;
            byte[] tagHeadByteArray;
            byte[] tagFrameHeadByteArray;

            byte[] songNameByteArray = songName.getBytes("UTF-8");
            byte[] artistNameByteArray = artistName.getBytes("UTF-8");
            byte[] albumNameByteArray = albumName.getBytes("UTF-8");

            final int tagHeadLength = 10;
            final int tagFrameHeadLength = 10;
            final int tagFrameEncodeLength = 1;
            final int tagFillByteLength = 20; // 这个填充字节是我看到其他MP3文件ID3标签都会在尾端添加的数据，为了保险起见我也加上了

            int byteArrayOffset = 0;
            int songNameByteArrayLength = songNameByteArray.length;
            int artistNameByteArrayLength = artistNameByteArray.length;
            int albumNameByteArrayLength = albumNameByteArray.length;
            int songNameFrameTotalLength = songNameByteArrayLength + tagFrameEncodeLength;
            int artistNameFrameTotalLength = artistNameByteArrayLength + tagFrameEncodeLength;
            int albumNameFrameTotalLength = albumNameByteArrayLength + tagFrameEncodeLength;

            int tagTotalLength = tagHeadLength + tagFrameHeadLength + songNameByteArrayLength +
                    tagFrameHeadLength + artistNameByteArrayLength +
                    tagFrameHeadLength + albumNameByteArrayLength +
                    tagFillByteLength;
            int tagContentLength = tagTotalLength - tagHeadLength;

            tagByteArray = new byte[tagTotalLength];

            tagHeadByteArray = new byte[tagHeadLength];
            System.arraycopy("ID3".getBytes(), 0, tagHeadByteArray, 0, 3);
            tagHeadByteArray[3] = 3;
            tagHeadByteArray[4] = 0;
            tagHeadByteArray[5] = 0;
            tagHeadByteArray[6] = (byte) ((tagContentLength >> 7 >> 7 >> 7) % 128);
            tagHeadByteArray[7] = (byte) ((tagContentLength >> 7 >> 7) % 128);
            tagHeadByteArray[8] = (byte) ((tagContentLength >> 7) % 128);
            tagHeadByteArray[9] = (byte) (tagContentLength % 128);
            System.arraycopy(tagHeadByteArray, 0, tagByteArray, byteArrayOffset,
                    tagHeadLength);
            byteArrayOffset += tagHeadLength;

            tagFrameHeadByteArray = new byte[tagFrameHeadLength];
            System.arraycopy("TIT2".getBytes(), 0, tagFrameHeadByteArray, 0, 4);
            tagFrameHeadByteArray[4] = (byte) ((songNameFrameTotalLength >> 8 >> 8 >> 8) % 256);
            tagFrameHeadByteArray[5] = (byte) ((songNameFrameTotalLength >> 8 >> 8) % 256);
            tagFrameHeadByteArray[6] = (byte) ((songNameFrameTotalLength >> 8) % 256);
            tagFrameHeadByteArray[7] = (byte) (songNameFrameTotalLength % 256);
            tagFrameHeadByteArray[8] = 0;
            tagFrameHeadByteArray[9] = 0;
            System.arraycopy(tagFrameHeadByteArray, 0, tagByteArray, byteArrayOffset, tagFrameHeadLength);
            byteArrayOffset += tagFrameHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagFrameEncodeLength);
            byteArrayOffset += tagFrameEncodeLength;
            System.arraycopy(songNameByteArray, 0, tagByteArray, byteArrayOffset,
                    songNameByteArrayLength);
            byteArrayOffset += songNameByteArrayLength;

            tagFrameHeadByteArray = new byte[tagFrameHeadLength];
            System.arraycopy("TPE1".getBytes(), 0, tagFrameHeadByteArray, 0, 4);
            tagFrameHeadByteArray[4] = (byte) ((artistNameFrameTotalLength >> 8 >> 8 >> 8) % 256);
            tagFrameHeadByteArray[5] = (byte) ((artistNameFrameTotalLength >> 8 >> 8) % 256);
            tagFrameHeadByteArray[6] = (byte) ((artistNameFrameTotalLength >> 8) % 256);
            tagFrameHeadByteArray[7] = (byte) (artistNameFrameTotalLength % 256);
            tagFrameHeadByteArray[8] = 0;
            tagFrameHeadByteArray[9] = 0;
            System.arraycopy(tagFrameHeadByteArray, 0, tagByteArray, byteArrayOffset, tagFrameHeadLength);
            byteArrayOffset += tagFrameHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagFrameEncodeLength);
            byteArrayOffset += tagFrameEncodeLength;
            System.arraycopy(artistNameByteArray, 0, tagByteArray, byteArrayOffset,
                    artistNameByteArrayLength);
            byteArrayOffset += artistNameByteArrayLength;

            tagFrameHeadByteArray = new byte[tagFrameHeadLength];
            System.arraycopy("TALB".getBytes(), 0, tagFrameHeadByteArray, 0, 4);
            tagFrameHeadByteArray[4] = (byte) ((albumNameFrameTotalLength >> 8 >> 8 >> 8) % 256);
            tagFrameHeadByteArray[5] = (byte) ((albumNameFrameTotalLength >> 8 >> 8) % 256);
            tagFrameHeadByteArray[6] = (byte) ((albumNameFrameTotalLength >> 8) % 256);
            tagFrameHeadByteArray[7] = (byte) (albumNameFrameTotalLength % 256);
            tagFrameHeadByteArray[8] = 0;
            tagFrameHeadByteArray[9] = 0;
            System.arraycopy(tagFrameHeadByteArray, 0, tagByteArray, byteArrayOffset, tagFrameHeadLength);
            byteArrayOffset += tagFrameHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagFrameEncodeLength);
            byteArrayOffset += tagFrameEncodeLength;
            System.arraycopy(albumNameByteArray, 0, tagByteArray, byteArrayOffset,
                    albumNameByteArrayLength);

            byte[] dataByteBuffer = new byte[1024];

            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            FileOutputStream fileOutputStream =
                    FileFunction.GetFileOutputStreamFromFile(musicFilePath);

            fileOutputStream.write(tagByteArray);

            while (fileInputStream.read(dataByteBuffer) > 0) {
                fileOutputStream.write(dataByteBuffer);
            }

            fileOutputStream.close();
            fileInputStream.close();

            FileFunction.DeleteFile(sourceFile.getAbsolutePath());
        } catch (Exception e) {
            LogFunction.error("写入音乐标签异常", e);
        }
    }
}
