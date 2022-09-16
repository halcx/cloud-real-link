package net.cloud.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.AccountManager;
import net.cloud.mapper.AccountMapper;
import net.cloud.model.AccountDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AccountManagerImpl implements AccountManager {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public int insert(AccountDO accountDO) {
        return accountMapper.insert(accountDO);
    }

    @Override
    public List<AccountDO> findByPhone(String phone) {
        List<AccountDO> accountDOList = accountMapper
                .selectList(new QueryWrapper<AccountDO>().eq("phone", phone));
        return accountDOList;
    }
}
