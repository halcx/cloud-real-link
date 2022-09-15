package net.cloud.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import net.cloud.config.OSSConfig;
import net.cloud.service.FileService;
import net.cloud.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private OSSConfig ossConfig;

    @Override
    public String uploadUserImg(MultipartFile file) {
        String bucketName = ossConfig.getBucketname();
        String endpoint = ossConfig.getEndpoint();
        String accessKeySecret = ossConfig.getAccessKeySecret();
        String accessKeyId = ossConfig.getAccessKeyId();

        //OSS客户端构建
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //获取文件原始名称
        String originalFilename = file.getOriginalFilename();

        //user/2022/12/12/文件名   这样去进行归档
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        //文件一般用的是随机名称
        String folder = dateTimeFormatter.format(ldt);
        String fileName = CommonUtil.generateUUID();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        //在oss上的bucket创建文件夹
        String newFileName = "user/"+folder+"/"+fileName+extension;

        try {
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, newFileName, file.getInputStream());
            //拼装返回路径
            if(putObjectResult!=null){
                String imgUrl = "https://"+bucketName+"."+endpoint+"/"+newFileName;
                return imgUrl;
            }
        } catch (IOException e) {
            log.error("文件上传失败:{}",e.getMessage());
        }finally {
            ossClient.shutdown();
        }


        return null;
    }
}
