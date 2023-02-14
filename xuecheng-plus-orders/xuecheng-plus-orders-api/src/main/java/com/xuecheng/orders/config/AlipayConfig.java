package com.xuecheng.orders.config;
 /**
 * @description 支付宝配置参数
 * @author Mr.M
 * @date 2022/10/20 22:45
 * @version 1.0
 */
 public class AlipayConfig {

  // 商户appid
	public static String APPID = "2021000122612975";
  // 私钥 pkcs8格式的
	public static String RSA_PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCdEZmBKC80sWp4Jf1Dr1pLtglvRVIjha/LYOj9y3mc4birqb8EnZgbWZPDOZCf5U4vsUHgD2ylEB6SV9J9mIbBHgm/5V3cHVROYimzd4OzkE/i1ROvg7zZNMjDFM4lKffZUVeq5WoVyJGdC4/jKXYOwBRfvzr6aU1rmRMxl72PE2yU8x6NOxVI6HzfifQa/tU/XKzuXyXCGhTVNgZ4GAeIZwyfGD1b7q7TXvafTDaK/9EbCxfyZ7aI1yxxaGAwxnBwYobC2UGuS/YawVfS+0LUWgWOrhXlFUKEruT6ksCETbtXkvLCoyYzbsHwkOS6iyWonAiPxSyoUYa1gHOeOUkvAgMBAAECggEBAJj/ztbILQuYEs1MvCMVidCcVx5jmTpu/CoRkYO/lAwkuD3HkQHO/Z8Op+gQh/epZ3r/oNrAS6WSSSVmlOyxCfrN5tNdewtyKnBcIKDoN4tPdPHRe2aJ3gh66WFWP+RluD+hg9M9vBPSFz2ySIwQFCcUJgSKg8nvj3HTX6X8WLMnt99zJOcj4N85Xqy45CYNLa/MRrIRK3+Yt3AmfvU3E6ITCoKC6YZm1lcu4TbKOCZosW3LfMqXc4U/aWBwAaySSbHdHc+Qq3THFI62uavSa1uogEu2HebFU1p9WaLSlT+alIma5yD5wzNfi3e3DJ+kzkXY4WMuJAgrq2gqrw+X8skCgYEA/nHqAkpvjyrv2WdMRaimH8NbBkSNLI2Gpal2Spuu90VdLPq+mIUMF76aCs1yd3zxP05udEyFytLGcetYSBqqv58tgg1x8d3N2B8lIWYiKGJjMnRH4O8KsWendYxRJPZCo+Xx1oJF2lWeZC3zDSDuV3hMP9J/zq/8EjyOnSfqCosCgYEAngdWfNWwfel6bOqEpgJ5zRynnEzHWC5lP/wW3lxlb1xJkk4jvXrpjHdiaWm6Fj9r9gulk0xsq1m5ruIMef6NlYo8wVIC+KU1qVikouE8im0zY6A21124n8yQVc0wsmIviOFnnmlNyeQVLQ16lIMz0lg1GJeLw2hx7aRUVzNt5G0CgYEA6u6FEW5nCPqtEL/W5a5tDIDU57md29gkzYrlZMfQShFqkaBHIQ4fd0EOfBDrsrXTDg+93nXVRstMXVzeW1z78Qfo/d6qKZcRe45uOyNRQJZvXuOMhnxQFDNPpDJVrdoO98PVgqkwFALfx0HB24pfZrX1OSyDw3QsOy5cw3BaITcCgYB6j55YOzMitE2q40f1qL3mgm7MHhANKA6GzXC/DFHvSFgXDFtTvVwdAQpmTwVv88g7j1TfJMqzKmeEZvTeOLp+XDQ6iuybAybXLFSjrqRRh9fvUHa4GYvzA6h9oyI9d0D+nDLM/UbDag2yX13OiONMI/UqGX7gEsUpVeXDq7NzEQKBgQDu9G5NPenr7I9Q6XRc8mRCf2SnV8/zmQ4wE/TyE8mVtgcNwvI4/9VrWzvQx5Kyh5UZ5uiIU1lD9m63JF/hTYO5qmfhEYPtd3p7CFe3vnwGgQ/bns0VWDW3I3phhkRzIYQKt/AJaxrVHEch6zRB2fWpzAtHzzi4pHWbPWguzHpWEA==";
  // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
  public static String notify_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/notify_url.jsp";
  // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
  public static String return_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp";
  // 请求网关地址
  public static String URL = "https://openapi.alipaydev.com/gateway.do"; //沙箱环境
  // public static String URL = "https://openapi.alipay.com/gateway.do"; //真实环境
  // 编码
  public static String CHARSET = "UTF-8";
  // 返回格式
  public static String FORMAT = "json";
  // 支付宝公钥
	public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhSg34c72Zw2gDZm5ycmybH8SSvRWo4+Ew6fnmSkwCNhUu4Pd0o/0u51czoP7HwBju8yIWfHWudby91CXrdilGJWgzs6kQzPeG7JuHCwy3AJowBpoZAubeNbJSZeSOcWt1U8sDKC/LzwNyfUzH+afra12tPMXWXgzkmY9tH52NhNnW9fR+l36/k+7APPh7jNVcBscFVOY6t2Q0aOLKzNPtfZz+U1sWHK262OJ9TtGrnwtzEzt7eTN/hubp0YbG01EsSzTOiNVoa19Ay0MNXMAJHVZeNbwhUBh7V2XtkR6vRtZn95YcegpRY/Je0g7q/DHZ1UjPjcRGUp9rtiL489o/QIDAQAB";
  // 日志记录目录
  public static String log_path = "/log";
  // RSA2
  public static String SIGNTYPE = "RSA2";
 }
