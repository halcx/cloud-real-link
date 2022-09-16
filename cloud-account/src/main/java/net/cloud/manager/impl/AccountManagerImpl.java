package net.cloud.manager.impl;

import lombok.extern.slf4j.Slf4j;
import net.cloud.manager.AccountManager;
import net.cloud.mapper.AccountMapper;
import net.cloud.model.AccountDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountManagerImpl implements AccountManager {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public int insert(AccountDO accountDO) {
        return accountMapper.insert(accountDO);
    }
}
