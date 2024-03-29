package com.readboy.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author 1-PC
 * @date 2016/9/21
 * 消息
 * 音频和视频共享多个字段。
 * @date 2019/2/14
 * 对字段调整，简化内容，可能和数据库字段不对应
 */
public class Conversation implements Parcelable {

    //消息类型
    public int type;
    //发送用户的Id,家庭圈有区别
    public String sendId;
    //实际的发送用户Id
    public String realSendId;
    //接收用户的Id
    public String recId;
    //发件人用户名
    public String senderName;
    //消息的Id
    public String conversationId;
    //是否是家庭圈的消息标识(是否显示用户名)
    public int isHomeGroup;
    //消息的未读状态
    public int isUnread;
    //发送表情的本地id
    public int emojiId;
    //接收到的表情编码
    public String emojiCode;
    /**
     * 发送图片的本地路径
     *
     * @deprecated use {@link #content}
     */
    public String imageLocalPath;
    /**
     * 接收图片的缩略图url
     *
     * @deprecated use {@link #preview}
     */
    public String thumbImageUrl;
    /**
     * 接收图片的原图url
     *
     * @deprecated use {@link #extra}
     */
    public String imageUrl;
    /**
     * 接收的文本内容
     *
     * @deprecated use {@link #content}
     */
    public String textContent;
    /**
     * 发送语音保存的本地路径
     *
     * @deprecated use {@link #content}
     */
    public String voiceLocalPath;
    /**
     * 接收语音文件的url
     *
     * @deprecated use {@link #extra}
     */
    public String voiceUrl;
    /**
     * 语音时长
     *
     * @deprecated use {@link #preview}
     */
    public int lastTime;
    //语音播放标识(是否显示小红点)
    public int isUnPlay;
    //语音,图片消息发送标识(判断是否需要重发)
    public int shouldResend;
    //正在发送,是否需要显示进度
    public int isSending;
    /**
     * 接收：服务器的时间
     * 发送：本地时间
     */
    public String time;
    //是否正在播放语音(语音项特有)
    public int isPlaying;

    // 2.0版本新字段，不同类型数据共享变量

    /**
     * 数据内容，要显示的内容，文件则为本地路径path
     */
    public String content;
    /**
     * 预览图之类的
     */
    public String preview;
    /**
     * 网络链接之类的
     */
    public String extra;
    /**
     * 更多信息
     */
    public String extra2;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(sendId);
        dest.writeString(recId);
        dest.writeString(senderName);
        dest.writeString(conversationId);
        dest.writeInt(isHomeGroup);
        dest.writeInt(isUnread);
        dest.writeInt(emojiId);
        dest.writeString(emojiCode);
        dest.writeString(imageLocalPath);
        dest.writeString(thumbImageUrl);
        dest.writeString(imageUrl);
        dest.writeString(textContent);
        dest.writeString(voiceLocalPath);
        dest.writeString(voiceUrl);
        dest.writeInt(lastTime);
        dest.writeInt(isUnPlay);
        dest.writeInt(shouldResend);
        dest.writeInt(isSending);
        dest.writeString(time);
        dest.writeString(realSendId);
        dest.writeInt(isPlaying);
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel source) {
            Conversation conversation = new Conversation();
            conversation.type = source.readInt();
            conversation.sendId = source.readString();
            conversation.recId = source.readString();
            conversation.senderName = source.readString();
            conversation.conversationId = source.readString();
            conversation.isHomeGroup = source.readInt();
            conversation.isUnread = source.readInt();
            conversation.emojiId = source.readInt();
            conversation.emojiCode = source.readString();
            conversation.imageLocalPath = source.readString();
            conversation.thumbImageUrl = source.readString();
            conversation.imageUrl = source.readString();
            conversation.textContent = source.readString();
            conversation.voiceLocalPath = source.readString();
            conversation.voiceUrl = source.readString();
            conversation.lastTime = source.readInt();
            conversation.isUnPlay = source.readInt();
            conversation.shouldResend = source.readInt();
            conversation.isSending = source.readInt();
            conversation.time = source.readString();
            conversation.realSendId = source.readString();
            conversation.isPlaying = source.readInt();
            return conversation;
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    @Override
    public String toString() {
        return "Conversation{" +
                "type=" + type +
                ", sendId='" + sendId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", imageLocalPath='" + imageLocalPath + '\'' +
                ", thumbImageUrl='" + thumbImageUrl + '\'' +
                ", textContent='" + textContent + '\'' +
                ", voiceLocalPath='" + voiceLocalPath + '\'' +
                ", voiceUrl='" + voiceUrl + '\'' +
                ", lastTime=" + lastTime +
                ", time='" + time + '\'' +
                '}';
    }
}
