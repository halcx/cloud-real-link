package net.cloud.controller;


import net.cloud.enums.BizCodeEnum;
import net.cloud.service.FileService;
import net.cloud.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Wxh
 * @since 2022-09-07
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    @Autowired
    private FileService fileService;

    /**
     * 文件上传流程：
     * 先上传文件，返回url地址
     * 在和普通表单一并提交（更加灵活，失败率低）
     * @param file
     * @return
     */
    @PostMapping("/upload")
    //RequestPart注解主要用于接收文件或者其他更为复杂的数据类型
    public JsonData uploadUserImg(@RequestPart("file")MultipartFile file){
        String result = fileService.uploadUserImg(file);
        return result != null ? JsonData.buildSuccess(result) : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);
    }
}

