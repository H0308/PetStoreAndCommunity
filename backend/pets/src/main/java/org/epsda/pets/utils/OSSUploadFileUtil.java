package org.epsda.pets.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/13
 * Time: 14:54
 *
 * @Author: 憨八嘎
 */
@Component
public class OSSUploadFileUtil {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    // 上传文件
    public String uploadFileToOss(MultipartFile file) {
        // 创建客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 官方没有提供批量上传的接口，并且用户可以上传的文件数量和大小比较小
            // 所以上层可以循环调用
            // 保证文件名唯一，防止同名但是内容不同的文件
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            ossClient.putObject(bucketName, fileName, file.getInputStream()); // 上传文件
            return "https://" + bucketName + "." + endpoint + "/" + fileName; // 返回文件在OSS的地址
        } catch (Exception e) {
            throw new RuntimeException("上传OSS失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    public Integer getMediaType(String filename) {
        if (filename == null) {
            throw new PetException("文件名不能为空");
        }
        // 图片类型
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".bmp") || filename.endsWith(".webp")) {
            return Constants.MEDIA_TYPE_IMAGE;
        }

        // 视频类型
        if (filename.endsWith(".mp4") || filename.endsWith(".avi") ||
                filename.endsWith(".mov") || filename.endsWith(".wmv") ||
                filename.endsWith(".flv") || filename.endsWith(".mkv")) {
            return Constants.MEDIA_TYPE_VIDEO;
        }

        throw new PetException("不支持的文件类型");
    }
}
