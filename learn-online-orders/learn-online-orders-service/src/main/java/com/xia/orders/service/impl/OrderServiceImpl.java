package com.xia.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xia.base.exception.GlobalException;
import com.xia.base.model.PageParams;
import com.xia.base.model.PageResult;
import com.xia.base.utils.IdWorkerUtils;
import com.xia.base.utils.QRCodeUtil;
import com.xia.messagesdk.model.po.MqMessage;
import com.xia.messagesdk.service.MqMessageService;
import com.xia.orders.config.AlipayConfig;
import com.xia.orders.config.PayNotifyConfig;
import com.xia.orders.mapper.XcOrdersGoodsMapper;
import com.xia.orders.mapper.XcOrdersMapper;
import com.xia.orders.mapper.XcPayRecordMapper;
import com.xia.orders.model.dto.AddOrderDto;
import com.xia.orders.model.dto.OrderQueryDto;
import com.xia.orders.model.dto.PayStatusDto;
import com.xia.orders.model.po.XcOrders;
import com.xia.orders.model.po.XcOrdersGoods;
import com.xia.orders.model.po.XcPayRecord;
import com.xia.orders.model.vo.OrderVO;
import com.xia.orders.model.vo.PayRecordVO;
import com.xia.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    private XcOrdersMapper ordersMapper;

    @Autowired
    private XcPayRecordMapper payRecordMapper;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Value("${pay.notifyurl}")
    String notifyUrl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Autowired
    private OrderService currentProxy;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 生成订单
     *
     * @param addOrderDto
     * @return
     */
    @Override
    @Transactional
    public PayRecordVO createOrder(String userId, AddOrderDto addOrderDto) {

        //创建商品订单
        XcOrders orders = saveXcOrders(userId, addOrderDto);
        if (orders == null) {
            throw new GlobalException("订单创建失败");
        }
        //生成支付记录
        XcPayRecord payRecord = createPayRecord(orders);
        //生成二维码
        String qrCode = null;
        try {
            //url要可以被模拟器访问到，url为下单接口(稍后定义)
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            throw new GlobalException("生成二维码出错");
        }
        PayRecordVO payRecordVO = new PayRecordVO();
        BeanUtils.copyProperties(payRecord, payRecordVO);
        payRecordVO.setQrcode(qrCode);

        return payRecordVO;
    }

    /**
     * 请求支付
     *
     * @param payNo
     * @param httpResponse
     */
    @Override
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws AlipayApiException, IOException {
        //判断订单是否存在
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            throw new GlobalException("请重新点击支付获取二维码");
        }
        if (payRecord.getStatus().equals("601002")) {
            throw new GlobalException("订单已支付，请勿重复支付。");
        }


        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY,
                AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl(notifyUrl);//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                " \"out_trade_no\":\"" + payRecord.getPayNo() + "\"," +
                " \"total_amount\":\"" + payRecord.getTotalPrice() + "\"," +
                " \"subject\":\"" + payRecord.getOrderName() + "\"," +
                " \"product_code\":\"QUICK_WAP_PAY\"" +
                " }");//填充业务参数
        String form = "";
        try {
            //请求支付宝下单接口,发起http请求
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    /**
     * 查询支付结果
     *
     * @param payNo
     * @return
     */
    @Override
    public PayRecordVO queryPayResult(String payNo) {
        if (payNo == null) {
            throw new GlobalException("请传入支付记录号");
        }
        //查询支付记录
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null) {
            throw new GlobalException("支付记录不存在");
        }
        //支付状态
        String status = payRecord.getStatus();
        //如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordVO payRecordVO = new PayRecordVO();
            BeanUtils.copyProperties(payRecord, payRecordVO);
            return payRecordVO;
        }
        //从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);
        //重新查询支付记录
        payRecord = getPayRecordByPayno(payNo);
        PayRecordVO payRecordVO = new PayRecordVO();
        BeanUtils.copyProperties(payRecord, payRecordVO);
        return payRecordVO;
    }

    @Override
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordDB = getPayRecordByPayno(payNo);
        if (payRecordDB == null) {
            throw new GlobalException("支付记录找不到");
        }
        //支付结果
        String trade_status = payStatusDto.getTrade_status();
        log.debug("收到支付结果:{},支付记录:{}}", payStatusDto.toString(), payRecordDB.toString());
        if (trade_status.equals("TRADE_SUCCESS")) {
            //该支付记录对应的金额
            BigDecimal totalPrice = BigDecimal.valueOf(payRecordDB.getTotalPrice());
            //实际支付金额
            BigDecimal total_amount = BigDecimal.valueOf(Float.parseFloat(payStatusDto.getTotal_amount()));
            //判断金额
            if (totalPrice.compareTo(total_amount) != 0) {
                throw new GlobalException("支付金额与实际金额不符");
            }
            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
            XcPayRecord payRecord = new XcPayRecord();
            BeanUtils.copyProperties(payRecordDB, payRecord);

            payRecord.setStatus("601002");//支付成功
            payRecord.setPaySuccessTime(LocalDateTime.now());
            payRecord.setOutPayNo(payStatusDto.getTrade_no());//支付宝交易号
            payRecord.setOutPayChannel("Alipay");

            int updatePayRecord = payRecordMapper.update(payRecord, new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
            if (updatePayRecord > 0) {
                log.info("更新支付记录状态成功:{}", payRecord.toString());
            } else {
                log.info("更新支付记录状态失败:{}", payRecord.toString());
                throw new GlobalException("更新支付记录状态失败");
            }
            //关联的订单号
            Long orderId = payRecord.getOrderId();
            XcOrders orders = ordersMapper.selectById(orderId);
            if (orders == null) {
                log.info("根据支付记录[{}}]找不到订单", payRecord.toString());
                throw new GlobalException("根据支付记录找不到订单");
            }
            XcOrders order = new XcOrders();
            order.setStatus("600002");//支付成功
            int updateOrders = ordersMapper.update(order, new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
            if (updateOrders > 0) {
                log.info("更新订单表状态成功,订单号:{}", orderId);
            } else {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                throw new GlobalException("更新订单表状态失败");
            }
            //保存消息记录,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", orders.getOutBusinessId(), orders.getOrderType(), null);
            //通知消息
            notifyPayResult(mqMessage);
        }
    }

    /**
     * 支付结果通知
     *
     * @param request
     * @param response
     */
    @Override
    public void receivenotify(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException, UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        //验签
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if (verify_result) {//验证成功

            //商户订单号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
            //appid
            String app_id = new String(request.getParameter("app_id").getBytes("ISO-8859-1"), "UTF-8");
            //total_amount
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");

            //交易成功处理
            if (trade_status.equals("TRADE_SUCCESS")) {

                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setApp_id(app_id);
                payStatusDto.setTrade_no(trade_no);
                payStatusDto.setTotal_amount(total_amount);

                //保存支付结果
                currentProxy.saveAliPayStatus(payStatusDto);
            }
        }
    }

    /**
     * 支付结果通知
     *
     * @param message
     */
    @Override
    public void notifyPayResult(MqMessage message) {
        //1、消息体，转json
        String msg = JSON.toJSONString(message);
        //设置消息持久化
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息ID，需要封装到CorrelationData中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加callback
        correlationData.getFuture().addCallback(
                result -> {
                    if (result != null && result.isAck()) {
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

    /**
     * 查询支付结果
     * @param pageParams
     * @param orderQueryDto
     * @return
     */
    @Override
    public PageResult<OrderVO> list(PageParams pageParams, OrderQueryDto orderQueryDto) {
        Page<XcOrders> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //查询订单列表
        Page<XcOrders> pageResult = ordersMapper.selectPage(page, new LambdaQueryWrapper<XcOrders>()
                // 模糊匹配课程名称（仅当 courseName 非空时生效）
                .like(StringUtils.isNotBlank(orderQueryDto.getCourseName()), XcOrders::getOrderName, orderQueryDto.getCourseName())
                // 精确匹配用户ID（仅当 userId 非空时生效）
                .eq(StringUtils.isNotBlank(orderQueryDto.getUserId()), XcOrders::getUserId, orderQueryDto.getUserId())
                // 精确匹配订单状态（仅当 status 非空时生效）
                .eq(StringUtils.isNotBlank(orderQueryDto.getStatus()), XcOrders::getStatus, orderQueryDto.getStatus())
                // 精确匹配订单编号（仅当 orderNo 非空时生效）
                .eq(StringUtils.isNotBlank(orderQueryDto.getOrderNo()), XcOrders::getId, orderQueryDto.getOrderNo())
                // 创建时间大于等于 orderStart（仅当 orderStart 非空时生效）
                .ge(null != orderQueryDto.getOrderStart(), XcOrders::getCreateDate, orderQueryDto.getOrderStart())
                // 创建时间小于等于 orderEnd（仅当 orderEnd 非空时生效）
                .le(null != orderQueryDto.getOrderEnd(), XcOrders::getCreateDate, orderQueryDto.getOrderEnd()));

        List<OrderVO> orderVOList = pageResult.getRecords().stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setCoursePubName(orders.getOrderName());
            orderVO.setPrice(BigDecimal.valueOf(orders.getTotalPrice()));
            orderVO.setOrderNo(orders.getId());
            return orderVO;
        }).collect(Collectors.toList());
        return new PageResult<>(orderVOList, pageResult.getTotal(), pageResult.getPages(), pageResult.getSize());
    }

    /**
     * 从支付宝查询支付结果
     *
     * @param payNo
     * @return
     */
    private PayStatusDto queryPayResultFromAlipay(String payNo) {
        //========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        alipayRequest.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(alipayRequest);
            if (!response.isSuccess()) {
                throw new GlobalException("您没有成功支付");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            throw new GlobalException("请求支付查询查询失败");
        }
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");

        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;
    }

    private XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>()
                .eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }


    /**
     * 生成支付记录
     *
     * @param orders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders) {
        if (orders == null) {
            throw new GlobalException("订单不存在");
        }
        if (orders.getStatus().equals("600002")) {
            throw new GlobalException("订单已支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");//未支付
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;

    }

    /**
     * 生成订单
     *
     * @param userId
     * @param addOrderDto
     * @return
     */
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        //幂等性处理
        XcOrders order = getOrderByBusinessId(userId, addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        order = new XcOrders();
        //生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(order);
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods, xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            ordersGoodsMapper.insert(xcOrdersGoods);
        });
        return order;
    }

    //根据业务id查询订单
    public XcOrders getOrderByBusinessId(String userId, String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, businessId)
                .eq(XcOrders::getUserId, userId));
        return orders;
    }

}
