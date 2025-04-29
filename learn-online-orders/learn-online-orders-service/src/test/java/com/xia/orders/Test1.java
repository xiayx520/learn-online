package com.xia.orders;

import com.xia.base.utils.QRCodeUtil;

import java.io.IOException;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/10/2 10:32
 * @version 1.0
 */
public class Test1 {

    public static void main(String[] args) throws IOException {
      QRCodeUtil qrCodeUtil = new QRCodeUtil();
      System.out.println(qrCodeUtil.createQRCode("http://23f7fb1e.r16.vip.cpolar.cn/orders/alipaytest", 200, 200));
    }

}
