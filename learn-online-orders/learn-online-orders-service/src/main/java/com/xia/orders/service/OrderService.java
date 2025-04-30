package com.xia.orders.service;

import com.alipay.api.AlipayApiException;
import com.xia.orders.model.dto.AddOrderDto;
import com.xia.orders.model.vo.PayRecordVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface OrderService {

    /**
     * 创建订单
     * @param addOrderDto
     * @return
     */
    PayRecordVO createOrder(String userId, AddOrderDto addOrderDto);


    /**
     * 向第三方支付平台发起支付请求
     * @param payNo
     * @param httpResponse
     */
    void requestpay(String payNo, HttpServletResponse httpResponse) throws AlipayApiException, IOException;
}
