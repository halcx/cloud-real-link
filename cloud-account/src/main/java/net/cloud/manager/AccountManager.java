package net.cloud.manager;

import net.cloud.model.AccountDO;

import java.util.List;

public interface AccountManager {

    int insert(AccountDO accountDO);

    List<AccountDO> findByPhone(String phone);
}
