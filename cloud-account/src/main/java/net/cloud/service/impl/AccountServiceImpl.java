package net.cloud.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.controller.request.AccountRegisterRequest;
import net.cloud.enums.AuthTypeEnum;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.SendCodeEnum;
import net.cloud.manager.AccountManager;
import net.cloud.model.AccountDO;
import net.cloud.service.AccountService;
import net.cloud.service.NotifyService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.JsonData;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private AccountManager accountManager;

    /**
     * 用户注册流程：
     * 1、手机验证码验证
     * 2、密码加密 TODO
     * 3、账号唯一性检查 TODO
     * 4、插入数据库
     * 5、新注册用户福利发放 TODO
     * @param registerRequest
     * @return
     */
    @Override
    public JsonData register(AccountRegisterRequest registerRequest) {
        boolean checkCode = false;
        //判断验证码
        if(StringUtils.isNotBlank(registerRequest.getPhone())){
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getPhone(), registerRequest.getCode());
        }
        //验证码错误
        if(!checkCode){
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }

        //创建DO
        AccountDO accountDO = new AccountDO();
        BeanUtils.copyProperties(registerRequest,accountDO);
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());

        //设置密码 密钥 盐
        accountDO.setSecret("$1$"+CommonUtil.getStringNumRandom(8));
        //生成md5加密过后的密码
        String cryptPwd = Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(),accountDO.getSecret());
        accountDO.setPwd(cryptPwd);

        int rows = accountManager.insert(accountDO);
        log.info("rows:{},注册成功:{}",rows,accountDO);

        //用户注册成功，发放福利
        userRegisterInitTask(accountDO);

        return JsonData.buildSuccess();
    }

    /**
     * 用户初始化发放福利 TODO
     * @param accountDO
     */
    private void userRegisterInitTask(AccountDO accountDO) {
    }
}
