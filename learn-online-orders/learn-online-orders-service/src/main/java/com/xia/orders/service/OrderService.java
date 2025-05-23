package com.xia.orders.service;

import com.alipay.api.AlipayApiException;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.messagesdk.model.po.MqMessage;
import com.xia.orders.model.dto.AddOrderDto;
import com.xia.orders.model.dto.OrderQueryDto;
import com.xia.orders.model.dto.PayStatusDto;
import com.xia.orders.model.vo.OrderVO;
import com.xia.orders.model.vo.PayRecordVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

    /**
     * 查询支付结果
     * @param payNo
     * @return
     */
    PayRecordVO queryPayResult(String payNo);


    /**
     * 保存支付宝支付结果
     * @param payStatusDto
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);


    /**
     * 接收支付宝支付结果
     * @param request
     * @param response
     */
    void receivenotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException;


    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);

    /**
     * 查询订单列表
     * @param pageParams
     * @param orderQueryDto
     * @return
     */
    PageResult<OrderVO> list(PageParams pageParams, OrderQueryDto orderQueryDto);
}
