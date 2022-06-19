package com.donghua.srb.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.donghua.common.exception.BusinessException;
import com.donghua.common.result.ResponseEnum;
import com.donghua.srb.oss.service.FileService;
import com.donghua.srb.oss.util.OssProperties;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(InputStream inputStream, String module, String fileName) {
        String endpoint = OssProperties.ENDPOINT;
        String accessKeyId = OssProperties.KEY_ID;
        String accessKeySecret = OssProperties.KEY_SECRET;
        String bucketName = OssProperties.BUCKET_NAME;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 判断bucket是否存在
        if (!ossClient.doesBucketExist(bucketName)) {
            ossClient.createBucket(bucketName);
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        }

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。

        // 文件目录结构, 业务模块 + 日期 + uuid + 原始文件格式
        // avatar/2022/06/04/xxxx(uuid).jpg
        String timeFolder = new DateTime().toString("/yyyy/MM/dd/");
        fileName = UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));
        String objectName = module + timeFolder + fileName;

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);
            //阿里云文件绝对路径
            return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/" + objectName;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            log.error("文件上传失败：" + oe.getErrorCode() + ", " + oe.getErrorMessage());
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, oe);
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            log.error("文件上传失败：" + ce.getErrorCode() + ", " + ce.getErrorMessage());
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void remoeFile(String url) {
        String endpoint = OssProperties.ENDPOINT;
        String accessKeyId = OssProperties.KEY_ID;
        String accessKeySecret = OssProperties.KEY_SECRET;
        String bucketName = OssProperties.BUCKET_NAME;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // https://srb-file-donghua.oss-cn-hangzhou.aliyuncs.com/test/2022/06/04/9db0ff6f-de9c-4d95-811d-34fb3e1570bf.jpeg
        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.ENDPOINT + "/";
        String objectName = url.substring(host.length());

        try {
            // 删除文件或目录。如果要删除目录，目录必须为空。
            ossClient.deleteObject(bucketName, objectName);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            log.error("删除失败：" + oe.getErrorCode() + ", " + oe.getErrorMessage());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
