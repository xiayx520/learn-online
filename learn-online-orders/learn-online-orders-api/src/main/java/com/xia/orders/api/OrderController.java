package com.xia.orders.api;

import com.alipay.api.AlipayApiException;
import com.xia.orders.model.dto.AddOrderDto;
import com.xia.orders.model.vo.PayRecordVO;
import com.xia.orders.service.OrderService;
import com.xia.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@Slf4j
@Api(value = "订单管理接口", tags = "订单管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 生成支付二维码
     * @param addOrderDto
     * @return
     */
    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordVO generatePayCode(@RequestBody AddOrderDto addOrderDto) {

        log.info("生成支付二维码，参数：{}",addOrderDto);

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if (user == null) {
            throw new RuntimeException("请登录后继续选课");
        }
        userId = user.getId();
        return orderService.createOrder(userId, addOrderDto);
    }


    /**
     * 扫码下单接口
     * @param payNo
     * @param httpResponse
     * @throws IOException
     */
    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException, AlipayApiException {

        log.info("扫码下单接口，参数：{}",payNo);

        orderService.requestpay(payNo, httpResponse);
    }

}
