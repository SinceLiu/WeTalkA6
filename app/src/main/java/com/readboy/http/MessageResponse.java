package com.readboy.http;

/**
 * "h":表示消息头，SENDERID|GROUP|TYPE|TIME
 * "a":表示消息属性，由attr简化而来，格式不变
 * "m":表示消息内容，格式不变
 * @author oubin
 * @date 2019/1/14
 */
public class MessageResponse {

    /**
     * 文本信息
     * {
     *     "data": [{
     *         "a": {"sendmsgId": "UA593B49B4A276921547514883699"},
     *         "h": "UA593B49B4A27692|UA593B49B4A27692|text|1547514879511",
     *         "m": "chvyvh"
     *     }],
     * }
     *
     * 表情信息
     * {
     *     "data": [{
     *         "a": {"sendmsgId": "UA593B49B4A276921547515000049"},
     *         "h": "UA593B49B4A27692|UA593B49B4A27692|text|1547514995871",
     *         "m": "/emoji_13"
     *     }],
     * }
     */
    public static class TextResponse{

    }

    /**
     * {
     *     "data": [{
     *         "a": {"length": 3},
     *         "h": "UA593B49B4A27692|UA593B49B4A27692|audio|1547515000592",
     *         "m": "http://img.readboy.com/wear/a/7m2b59s3lb9.amr"
     *     }],
     * }
     */
    public static class AudioResponse{

    }

    /**
     * {
     *     "data": [
     *         {
     *             "a": {
     *                 "src": "http://img.readboy.com/wear/p/201901150943385c3d3acab539d.jpg",
     *                 "w": 1200,
     *                 "h": 1200
     *             },
     *             "h": "D05C2C75FA00413B|D05C2C75FA00413B|image|1547516639036",
     *             "m": "http://img.readboy.com/wear/p/201901150943385c3d3acab539d.jpg@!t"
     *         },
     *         {
     *             "h": "D05C2C75FA00413B|D05C2C75FA00413B|text|1547516641477",
     *             "m": "/emoji_17"
     *         }
     *     ],
     * }
     */
    public static class ImageResponse{
    }

    /**
     * {
     *     "r": "mget",
     *     "t": "19011500000018",
     *     "data": [{
     *         "h": "D05C2C75FA00413B|D05C2C75FA00413B|video|1547516645015",
     *         "m": "http://img.readboy.com/wear/v/201901150944045c3d3ae4e3271.mp4"
     *     }],
     *     "o": "13"
     * }
     */
    public static class VideoResponse{

    }

}
