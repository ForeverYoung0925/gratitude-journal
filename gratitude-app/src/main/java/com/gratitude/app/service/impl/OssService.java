package com.gratitude.app.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.gratitude.app.config.OssConfig;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * OSS 存储服务
 * 采用客户端直传方案:
 * 1. 前端请求后端获取预签名上传URL
 * 2. 前端直传到OSS，不经过后端中转
 * 3. 前端上传成功后，将OSS返回的URL传给后端业务接口
 */
@Slf4j
@Service
public class OssService {

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssConfig ossConfig;

    /**
     * 获取预签名上传URL
     * 
     * @param dir    子目录 如: diary/ avatar/
     * @param suffix 文件后缀 如: jpg png
     * @return key->上传URL, accessUrl->访问URL
     */
    public Map<String, String> getPresignedUploadUrl(String dir, String suffix) {
        try {
            String objectKey = dir + UUID.randomUUID().toString().replace("-", "") + "." + suffix;
            Date expiration = new Date(System.currentTimeMillis() + ossConfig.getStsExpire() * 1000);

            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(), objectKey,
                    com.aliyun.oss.HttpMethod.PUT);
            request.setExpiration(expiration);
            request.addUserMetadata("x-oss-object-acl", "public-read");

            URL presignedUrl = ossClient.generatePresignedUrl(request);

            Map<String, String> result = new HashMap<>();
            result.put("uploadUrl", presignedUrl.toString());
            result.put("accessUrl", ossConfig.getDomain() + "/" + objectKey);
            result.put("objectKey", objectKey);
            return result;
        } catch (Exception e) {
            log.error("获取OSS预签名URL失败", e);
            throw new BusinessException(ResultCode.OSS_SIGN_FAIL);
        }
    }

    /**
     * 获取防盗链视频播放URL (带时效签名)
     * 用于VIP视频，防止URL被分享后滥用
     * 
     * @param objectKey     OSS对象key
     * @param expireSeconds 链接有效期(秒)
     */
    public String getSignedVideoUrl(String objectKey, long expireSeconds) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(), objectKey,
                    com.aliyun.oss.HttpMethod.GET);
            request.setExpiration(expiration);
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("获取视频防盗链URL失败, objectKey={}", objectKey, e);
            throw new BusinessException(ResultCode.OSS_SIGN_FAIL);
        }
    }
}
