package net.cloud.service.impl;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.cloud.config.RabbitMQConfig;
import net.cloud.controller.request.AccountLoginRequest;
import net.cloud.controller.request.AccountRegisterRequest;
import net.cloud.enums.AuthTypeEnum;
import net.cloud.enums.BizCodeEnum;
import net.cloud.enums.EventMessageType;
import net.cloud.enums.SendCodeEnum;
import net.cloud.manager.AccountManager;
import net.cloud.model.AccountDO;
import net.cloud.model.EventMessage;
import net.cloud.model.LoginUser;
import net.cloud.service.AccountService;
import net.cloud.service.NotifyService;
import net.cloud.utils.CommonUtil;
import net.cloud.utils.IDUtil;
import net.cloud.utils.JWTUtil;
import net.cloud.utils.JsonData;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    /**
     * 免费流量包id固定为1
     */
    private static final Long FREE_TRAFFIC_PRODUCT_ID = 1L;

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
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
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
        //认证级别
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());

        //accountNo 唯一索引,使用我们自己封装的雪花算法
        accountDO.setAccountNo(Long.parseLong(IDUtil.geneSnowFlakeID().toString()));

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
     * 用户登陆
     * @param request
     * @return
     */
    @Override
    public JsonData login(AccountLoginRequest request) {
        List<AccountDO> accountDOList = accountManager.findByPhone(request.getPhone());
        if(accountDOList!=null && accountDOList.size()==1){
            AccountDO accountDO = accountDOList.get(0);
            String md5Crypt = Md5Crypt.md5Crypt(request.getPwd().getBytes(), accountDO.getSecret());
            if(md5Crypt.equalsIgnoreCase(accountDO.getPwd())){
                //登陆成功，构建用户
                LoginUser loginUser = LoginUser.builder().build();
                BeanUtils.copyProperties(accountDO,loginUser);

                //构建token，并且返回给前端
                String token = JWTUtil.generateJsonWebToken(loginUser);
                return JsonData.buildSuccess(token);
            }else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        }else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }
    }

    /**
     * 用户初始化发放福利：流量包
     * @param accountDO
     */
    private void userRegisterInitTask(AccountDO accountDO) {
        EventMessage eventMessage = EventMessage.builder().messageId(IDUtil.geneSnowFlakeID().toString())
                .accountNo(accountDO.getAccountNo())
                .eventMessageType(EventMessageType.TRAFFIC_FREE_INIT.name())
                .bizId(FREE_TRAFFIC_PRODUCT_ID.toString())
                .build();
        //发放流量包消息
        rabbitTemplate.convertAndSend(rabbitMQConfig.getTrafficEventExchange(),rabbitMQConfig.getTrafficFreeInitRoutingKey(),eventMessage);
    }
}
