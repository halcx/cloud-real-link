package net.cloud.component;

import lombok.extern.slf4j.Slf4j;
import net.cloud.vo.PayInfoVO;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JdPayStrategy implements PayStrategy{
    @Override
    public String unifiedOrder(PayInfoVO payInfoVO) {
        return null;
    }

    @Override
    public String refund(PayInfoVO payInfoVO) {
        return PayStrategy.super.refund(payInfoVO);
    }

    @Override
    public String queryPayState(PayInfoVO payInfoVO) {
        return PayStrategy.super.queryPayState(payInfoVO);
    }

    @Override
    public String closeOrder(PayInfoVO payInfoVO) {
        return PayStrategy.super.closeOrder(payInfoVO);
    }
}
