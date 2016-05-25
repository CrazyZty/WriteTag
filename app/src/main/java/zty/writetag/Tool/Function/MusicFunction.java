package zty.writetag.Tool.Function;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by zhengtongyu on 16/5/23.
 */
public class MusicFunction {
    public static void StorageMusicFileWithID3V1Tag(File tempFile, String musicFilePath,
                                                    String songName, String artistName,
                                                    String albumName) {
        try {
            tempFile.renameTo(new File(musicFilePath));

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

            tagByteArray[127] = (byte) 0xFF; // 在这里0xFF代表流派未知

            musicRandomAccessFile.write(tagByteArray);
        } catch (Exception e) {
            LogFunction.error("写入音乐标签异常", e);
        }
    }

    //    关于mp3 tag的资料很多，大家可以自己去找，大概讲的都是大同小异，但是我觉得他们忘了很重要的一点，也是我忽略的，后来解析文件出错了，才恍然大悟。那就是tag信息的编码。
    //
    //    大家都知道每个frame的头是由10个字节组成的，具体内容我就不仔细写了，在着10个字节后有一个字节，大家可以仔细观察一下，只有四种情况 00 ，01，02，03，这个代表什么呢？
    //    这个就是表示tag的编码方式的。00代表的就是ISO-8859-1 编码，后面直接跟的就是字符串，关于编码方式我就不做解释了，网上很多，01 代表的就是UTF-16编码，我就是被这个搞晕了，
    //    因为在10个字节的表示后有这个三个字节 01 FF FE，我是直接按照ISO-8859-1编码进行的。所以读取的字符串总是不对，FF FE表示的BOM的顺序。
    //    FF FE表示的就是little endian,FE FF 表示的就是big endian。在这两个字节后面才是真正的字符串。02 代表 UTF16BE 我没有碰到过大概的意思就是UTF16的编码顺序为big endian，
    //    但是这种编码没有FF FE这样的标识。03 表示的UTF8编码，在tag信息是不采用这种表明，但是这种编码在tag里不是错的。
    public static void StorageMusicFileWithID3V2Tag(File tempFile, String musicFilePath,
                                                    String songName, String artistName,
                                                    String albumName) {
        try {
            RandomAccessFile musicRandomAccessFile =
                    new RandomAccessFile(tempFile.getAbsolutePath(), "rw");
            musicRandomAccessFile.seek(0);

            byte[] tag = new byte[3];
            musicRandomAccessFile.read(tag);

            if (new String(tag).equals("ID3")) {
                tempFile.renameTo(new File(musicFilePath));
                return;
            }
        } catch (Exception e) {
            LogFunction.error("存储音乐文件异常", e);
        }

        try {
            byte[] encodeByte = {3};
            byte[] tagByteArray;
            byte[] tagHeadByteArray;
            byte[] dataByteBuffer = new byte[1024];

            byte[] songNameByteArray = songName.getBytes("UTF-8");
            byte[] artistNameByteArray = artistName.getBytes("UTF-8");
            byte[] albumNameByteArray = albumName.getBytes("UTF-8");

            final int tagEncodeLength = 1;
            final int tagHeadLength = 10;
            final int totalTagHeadLength = 10;

            int byteArrayOffset = 0;
            int songNameByteArrayLength = songNameByteArray.length;
            int artistNameByteArrayLength = artistNameByteArray.length;
            int albumNameByteArrayLength = albumNameByteArray.length;
            int songNameByteArrayTotalLength = songNameByteArrayLength + tagEncodeLength;
            int artistNameByteArrayTotalLength = artistNameByteArrayLength + tagEncodeLength;
            int albumNameByteArrayTotalLength = albumNameByteArrayLength + tagEncodeLength;

            int totalByteLength =
                    20 + totalTagHeadLength + songNameByteArrayLength + tagHeadLength +
                            artistNameByteArrayLength +
                            tagHeadLength +
                            albumNameByteArrayLength + tagHeadLength;
            int tagSize = totalByteLength - totalTagHeadLength;

            tagByteArray = new byte[totalByteLength];

            tagHeadByteArray = new byte[tagHeadLength];
            System.arraycopy("ID3".getBytes(), 0, tagHeadByteArray, 0, 3);
            tagHeadByteArray[3] = 3;
            tagHeadByteArray[4] = 0;
            tagHeadByteArray[5] = 0;
            tagHeadByteArray[6] = (byte) ((tagSize >> 7 >> 7 >> 7) % 128);
            tagHeadByteArray[7] = (byte) ((tagSize >> 7 >> 7) % 128);
            tagHeadByteArray[8] = (byte) ((tagSize >> 7) % 128);
            tagHeadByteArray[9] = (byte) (tagSize % 128);
            System.arraycopy(tagHeadByteArray, 0, tagByteArray, byteArrayOffset,
                    totalTagHeadLength);
            byteArrayOffset += totalTagHeadLength;

            tagHeadByteArray = new byte[tagHeadLength];
            System.arraycopy("TIT2".getBytes(), 0, tagHeadByteArray, 0, 4);
            tagHeadByteArray[4] = (byte) ((songNameByteArrayTotalLength >> 8 >> 8 >> 8) % 256);
            tagHeadByteArray[5] = (byte) ((songNameByteArrayTotalLength >> 8 >> 8) % 256);
            tagHeadByteArray[6] = (byte) ((songNameByteArrayTotalLength >> 8) % 256);
            tagHeadByteArray[7] = (byte) (songNameByteArrayTotalLength % 256);
            tagHeadByteArray[8] = 0;
            tagHeadByteArray[9] = 0;
            System.arraycopy(tagHeadByteArray, 0, tagByteArray, byteArrayOffset, tagHeadLength);
            byteArrayOffset += tagHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagEncodeLength);
            byteArrayOffset += tagEncodeLength;
            System.arraycopy(songNameByteArray, 0, tagByteArray, byteArrayOffset,
                    songNameByteArrayLength);
            byteArrayOffset += songNameByteArrayLength;

            tagHeadByteArray = new byte[tagHeadLength];
            System.arraycopy("TPE1".getBytes(), 0, tagHeadByteArray, 0, 4);
            tagHeadByteArray[4] = (byte) ((artistNameByteArrayTotalLength >> 8 >> 8 >> 8) % 256);
            tagHeadByteArray[5] = (byte) ((artistNameByteArrayTotalLength >> 8 >> 8) % 256);
            tagHeadByteArray[6] = (byte) ((artistNameByteArrayTotalLength >> 8) % 256);
            tagHeadByteArray[7] = (byte) (artistNameByteArrayTotalLength % 256);
            tagHeadByteArray[8] = 0;
            tagHeadByteArray[9] = 0;
            System.arraycopy(tagHeadByteArray, 0, tagByteArray, byteArrayOffset, tagHeadLength);
            byteArrayOffset += tagHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagEncodeLength);
            byteArrayOffset += tagEncodeLength;
            System.arraycopy(artistNameByteArray, 0, tagByteArray, byteArrayOffset,
                    artistNameByteArrayLength);
            byteArrayOffset += artistNameByteArrayLength;

            tagHeadByteArray = new byte[tagHeadLength];
            System.arraycopy("TALB".getBytes(), 0, tagHeadByteArray, 0, 4);
            tagHeadByteArray[4] = (byte) ((albumNameByteArrayTotalLength >> 8 >> 8 >> 8) % 256);
            tagHeadByteArray[5] = (byte) ((albumNameByteArrayTotalLength >> 8 >> 8) % 256);
            tagHeadByteArray[6] = (byte) ((albumNameByteArrayTotalLength >> 8) % 256);
            tagHeadByteArray[7] = (byte) (albumNameByteArrayTotalLength % 256);
            tagHeadByteArray[8] = 0;
            tagHeadByteArray[9] = 0;
            System.arraycopy(tagHeadByteArray, 0, tagByteArray, byteArrayOffset, tagHeadLength);
            byteArrayOffset += tagHeadLength;
            System.arraycopy(encodeByte, 0, tagByteArray, byteArrayOffset, tagEncodeLength);
            byteArrayOffset += tagEncodeLength;
            System.arraycopy(albumNameByteArray, 0, tagByteArray, byteArrayOffset,
                    albumNameByteArrayLength);

            FileInputStream fileInputStream = new FileInputStream(tempFile);
            FileOutputStream fileOutputStream =
                    FileFunction.GetFileOutputStreamFromFile(musicFilePath);

            fileOutputStream.write(tagByteArray);

            while (fileInputStream.read(dataByteBuffer) > 0) {
                fileOutputStream.write(dataByteBuffer);
            }

            fileOutputStream.close();
            fileInputStream.close();

            FileFunction.DeleteFile(tempFile.getAbsolutePath());
        } catch (Exception e) {
            LogFunction.error("写入音乐标签异常", e);
        }
    }
}
