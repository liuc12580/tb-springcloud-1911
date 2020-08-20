package com.jk.utils;

public class SmsConstant {
    //发送验证码的请求路径URL
    public static final String
            SERVER_URL="https://api.netease.im/sms/sendcode.action";
    //网易云信分配的账号，请替换你在管理后台应用下申请的Appkey
    public static final String
            APP_KEY="f15b6e334314937b4b114cb149b3edc1";
    //网易云信分配的密钥，请替换你在管理后台应用下申请的appSecret
    public static final String APP_SECRET="55ae212a33f5";
    //随机数
    public static final String NONCE="367890";
    //短信模板ID
    public static final String TEMPLATEID="14881425";
    //手机号，接收者号码列表，JSONArray格式，限制接收者号码个数最多为100个
    public static final String MOBILES="['15035487147']";
    //短信参数列表，用于依次填充模板，JSONArray格式，每个变量长度不能超过30字,对于不包含变量的模板，不填此参数表示模板即短信全文内容
    public static final String PARAMS="['xxxx','xxxx']";
    //手机号
    public static final String MOBILE="15035487147";
    //验证码长度，范围4～10，默认为4
    public static final String CODELEN="6";
}
