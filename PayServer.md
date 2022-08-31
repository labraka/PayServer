# PayServer

简介：该项目集成了支付宝（V2.0版本）和微信（V3版本）的native支付对接，拥有简单的下单、下单回调、关闭订单、退款、退款回调等基础业务对接，修改简单的配置达到开箱即用的目的。

## 一、支付宝（沙箱）

支付宝对java开发很友好，所有的请求都已经封装好，只需要简单的参数即可完成支付相关的api请求。

支付采用策略模式，不同的支付方式有不同的实现，更便于拓展。

### 1.接入准备：

文档地址：https://opendocs.alipay.com/open/204/105297

### 2.对接

- api文档地址：https://opendocs.alipay.com/open/02e7gq?scene=common

- 配置沙箱环境：文档地址：https://opendocs.alipay.com/common/02kkv7，注意支付宝公钥不要填成商户公钥！

- 配置支付宝的公共请求参数：将开放平台的相关参数还有沙箱环境的公钥和私钥配置在项目中，路径：resource/alipay.yaml
- 初始化client bean对象，后面直接使用即可。

### 3.支付

可以用安卓手机下载沙箱版支付宝，在开放平台充值金额（默认有9999元），进行相关消费测试。

## 二、微信支付

微信支付对java开发极不友好，微信支付没有沙箱模式，而且文档写的也不好，里面跳来跳去，容易给新手产生很多误解，我在这也踩了不少坑，所以将其的整个流程做以下总结。

### 1.接入准备

#### 1.1申请成为商户

这一步会劝退个人开发者，因为这个需要营业执照并且要审核通过才会有微信给分配的商户id。地址：https://pay.weixin.qq.com/index.php/core/home/login?return_url=%2F

#### 1.2申请应用

成为商户后需要申请一个应用作为appid，然后在控制台拿到商户id，下载证书，设置V3Key，（若不会操作参照：http://124.222.34.244/aMbRnq）并将其放在指定路径下，当前项目放在：/resource/static

### 2.对接

- api文档地址：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_4_1.shtml
- 微信初始化client需要先验证签名，我都集成在WechatPayInitClient.java类了。
- 微信所有的api请求需要自己组装http请求，所以需要将所有需要用到的请求url放在：WechatUrlConfig.java类中。
- WechatPay.java类封装了所有的api请求。

## 3.定时任务

定时任务的作用是为了扫描还未支付、退款有异常的订单，进行查询并关闭订单，引入了xxl-job（参考文档：https://www.xuxueli.com/xxl-job/）进行分布式任务调度。

需要搭建定时任务服务，将本机的服务进行注册，然后进入xxl-job后台进行配置即可，参照：http://124.222.34.244/aiBBYZ