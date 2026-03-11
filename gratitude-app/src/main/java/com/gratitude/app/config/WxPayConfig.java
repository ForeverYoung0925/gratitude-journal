package com.gratitude.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayConfig {

    private String mchId;
    private String mchKey;
    private String notifyUrl;
    private String appId;
    // 微信支付V3 APIv3密钥(AES-256-GCM解密回调用)
    private String apiV3Key;
    // 商户API证书序列号
    private String serialNo;
    // 商户私钥文件路径
    private String privateKeyPath;
}
