

# sso-oauth

sso单点登录案例，springboot 、springcloud 集成oauth2.0





## 2. Spring security Oauth2认证解决方案

本项目采用 Spring security + Oauth2完成用户认证及用户授权，Spring security 是一个强大的和高度可定制的身份验证和访问控制框架，Spring security 框架集成了Oauth2协议。

![image](https://user-images.githubusercontent.com/40937248/128985701-86a60408-0fbd-44d7-ba29-41a966be04d7.png)


1、用户请求认证服务完成认证。 

2、认证服务下发用户身份令牌，拥有身份令牌表示身份合法。 

3、用户携带令牌请求资源服务，请求资源服务必先经过网关/过滤器。 

4、网关/过滤器校验用户身份令牌的合法，不合法表示用户没有登录，如果合法则放行继续访问。 

5、资源服务获取令牌，根据令牌完成授权。 

6、资源服务完成授权则响应资源信息。







##  3. Jwt令牌回顾

 JSON Web Token（JWT）是一个开放的行业标准（RFC 7519），它定义了一种简介的、自包含的协议格式，用于 在通信双方传递json对象，传递的信息经过数字签名可以被验证和信任。JWT可以使用HMAC算法或使用RSA的公 钥/私钥对来签名，防止被篡改。 

官网：https://jwt.io/ 

标准：https://tools.ietf.org/html/rfc7519 

JWT令牌的优点： 

```
1、jwt基于json，非常方便解析。 
2、可以在令牌中自定义丰富的内容，易扩展。 
3、通过非对称加密算法及数字签名技术，JWT防止篡改，安全性高。 
4、资源服务使用JWT可不依赖认证服务即可完成授权。
```

缺点： 

```
１、JWT令牌较长，占存储空间比较大。    
```



### 3.1 令牌结构

通过学习JWT令牌结构为自定义jwt令牌打好基础。

 JWT令牌由三部分组成，每部分中间使用点（.）分隔，比如：xxxxx.yyyyy.zzzzz 

**Header** 

头部包括令牌的类型（即JWT）及使用的哈希算法（如HMAC SHA256或RSA） 

一个例子如下： 

下边是Header部分的内容   

```properties
{
	"alg": "HS256",
	"typ": "JWT"
}
```

 将上边的内容使用Base64Url编码，得到一个字符串就是JWT令牌的第一部分。

**Payload**

第二部分是负载，内容也是一个json对象，它是存放有效信息的地方，它可以存放jwt提供的现成字段，比 如：iss（签发者）,exp（过期时间戳）, sub（面向的用户）等，也可自定义字段。 

此部分不建议存放敏感信息，因为此部分可以解码还原原始内容。 

最后将第二部分负载使用Base64Url编码，得到一个字符串就是JWT令牌的第二部分。    

一个例子：

```properties
{
	"sub": "1234567890",
	"name": "456",
	"admin": true
}
```

**Signature** 

第三部分是签名，此部分用于防止jwt内容被篡改。 

这个部分使用base64url将前两部分进行编码，编码后使用点（.）连接组成字符串，最后使用header中声明 签名算法进行签名。 

一个例子：    

```properties
HMACSHA256(
	base64UrlEncode(header) + "." +
	base64UrlEncode(payload),
	secret)
```

base64UrlEncode(header)：jwt令牌的第一部分。 

base64UrlEncode(payload)：jwt令牌的第二部分。 

secret：签名所使用的密钥。    



### 3.2 生成私钥公钥

JWT令牌生成采用非对称加密算法 

1、生成密钥证书 下边命令生成密钥证书，采用RSA 算法每个证书包含公钥和私钥 

```
keytool -genkeypair -alias sso-oauth -keyalg RSA -keypass sso-oauth -keystore sso-oauth.jks -storepass sso-oauth 
```

Keytool 是一个java提供的证书管理工具 

```properties
-alias：密钥的别名 
-keyalg：使用的hash算法 
-keypass：密钥的访问密码 
-keystore：密钥库文件名，changgou.jks保存了生成的证书 
-storepass：密钥库的访问密码 
```



查询证书信息：

```
keytool -list -keystore sso-oauth.jks
```



2、导出公钥     

openssl是一个加解密工具包，这里使用openssl来导出公钥信息。 

安装 openssl：http://slproweb.com/products/Win32OpenSSL.html 

安装资料目录下的Win64OpenSSL-1_1_1b.exe 

配置openssl的path环境变量，

cmd进入changgou.jks文件所在目录执行如下命令： 

```properties
keytool -list -rfc --keystore sso-oauth.jks | openssl x509 -inform pem -pubkey
```



下面段内容是公钥

```
-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgCvRX1jW5xsXn1fth3aFl/w3dJLmAgAgCODUGf/3JlG4wCfBpx76kk7qtAm+4tYHpJrhDJNaAEPoGWdvjOFKPCtzrN29pqZkd/qS/4mmCpliVAtCDD6rrn2qEaHZB9hMnRtTeQc2E7Hbc7Fo6FaVDE5kTmFS4qMDBE6oBahnaUR1eH1ugPBz5qU5/4Z1tgAInxDHc5XS083Gv8d8UWMPWxmMuZ8GJBFkfG+60CoZ5Qv3JqoCu2rXLcfTt4P14IC1MZnusu/1f01ppwJbla7w+P+pElDL13YDXr9vBSORwJyZ7VS3ucq0TzJqKmDfZ6GnvErwqGFUjJd9FeMsHSt5KQIDAQAB-----END PUBLIC KEY-----

```

将上边的公钥拷贝到文本public.key文件中，合并为一行,可以将它放到需要实现授权认证的工程中。



Security Oauth2.0入门

搭建认证服务器之前，先在用户系统表结构中增加如下表结构：

```sql
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(48) NOT NULL COMMENT '客户端ID，主要用于标识对应的应用',
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL COMMENT '客户端秘钥，BCryptPasswordEncoder加密',
  `scope` varchar(256) DEFAULT NULL COMMENT '对应的范围',
  `authorized_grant_types` varchar(256) DEFAULT NULL COMMENT '认证模式',
  `web_server_redirect_uri` varchar(256) DEFAULT NULL COMMENT '认证后重定向地址',
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL COMMENT '令牌有效期',
  `refresh_token_validity` int(11) DEFAULT NULL COMMENT '令牌刷新周期',
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```





导入2条初始化数据,其中加密字符明文为changgou：

```sql
INSERT INTO `sso-oauth`.`oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES ('jy_sso_oauth', NULL, '$2a$10$wfdHtQCGANLqU0mKl6zdJOnlfrW1sFYJ/9To3zq5ZNK7PKu64LWEC', 'all,read,write', 'authorization_code,password,refresh_token,client_credentials', 'http://localhost', NULL, '48000', '48000', NULL, NULL);
INSERT INTO `sso-oauth`.`oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES ('userweb', NULL, '$2a$10$Yvkp3xzDcri6MAsPIqnzzeGBHez1QZR3A079XDdmNU4R725KrkXi2', 'app', 'password,refresh_token', 'http://localhost', NULL, '48000', '48000', NULL, NULL);

```







### Oauth2授权模式介绍

Oauth2有以下授权模式： 

```
1.授权码模式（Authorization Code）
2.隐式授权模式（Implicit） 
3.密码模式（Resource Owner Password Credentials） 
4.客户端模式（Client Credentials） 
```

其中授权码模式和密码模式应用较多，本小节介绍授权码模式。

#### 4.2.1 授权码模式

##### 4.2.1.1 授权码授权流程 

上边例举的黑马程序员网站使用QQ认证的过程就是授权码模式，流程如下： 

1、客户端请求第三方授权 

2、用户同意给客户端授权 

3、客户端获取到授权码，请求认证服务器申请 令牌 

4、认证服务器向客户端响应令牌 

5、客户端请求资源服务器的资源，资源服务校验令牌合法性，完成授权 

6、资源服务器返回受保护资源   



##### 4.2.1.2 申请授权码

请求认证服务获取授权码：

```properties
Get请求：
http://localhost:9200/oauth/authorize?client_id=jy_sso_oauth&response_type=code&scop=app&redirect_uri=http://localhost
```

参数列表如下： 

```properties
client_id：客户端id，和授权配置类中设置的客户端id一致。 
response_type：授权码模式固定为code 
scop：客户端范围，和授权配置类中设置的scop一致。 
redirect_uri：跳转uri，当授权码申请成功后会跳转到此地址，并在后边带上code参数（授权码）
```

 首先跳转到登录页面：

```
localhost:9200/login
```
![image](https://user-images.githubusercontent.com/40937248/128986084-2059ea01-1716-48f6-b125-c6609672498a.png)



提示输入账号、密码。输入客户端id和客户端秘钥



输入账号和密码，点击Login。 Spring Security接收到请求会调用UserDetailsService接口的loadUserByUsername方法查询用户正确的密码。 当前导入的基础工程中客户端ID为jy_sso_oauth，秘钥也为jy_sso_oauth即可认证通过。 

接下来进入授权页面：

```
http://localhost:9200/oauth/authorize?client_id=jy_sso_oauth&response_type=code&scop=app&redirect_uri=http://localhost
```



点击Authorize,接下来返回授权码： 认证服务携带授权码跳转redirect_uri,code=k45iLY就是返回的授权码, **每一个授权码只能使用一次**

![image](https://user-images.githubusercontent.com/40937248/128985953-115b9028-dd1c-45ca-b921-04cdce2cc7e4.png)


```
http://localhost/?code=BD0464
```
![image](https://user-images.githubusercontent.com/40937248/128986015-f81d5c97-4bf0-4235-a418-4356b42b27e7.png)



##### 4.2.1.3 申请令牌

拿到授权码后，申请令牌。

```properties
Post请求：
http://localhost:9200/oauth/token
```

 参数如下： 

```properties
grant_type：授权类型，填写authorization_code，表示授权码模式 
code：授权码，就是刚刚获取的授权码，注意：授权码只使用一次就无效了，需要重新申请。 
redirect_uri：申请授权码时的跳转url，一定和申请授权码时用的redirect_uri一致。 
```

此链接需要使用 http Basic认证。 

什么是http Basic认证？

​	http协议定义的一种认证方式，将客户端id和客户端密码按照“客户端ID:客户端密码”的格式拼接，并用base64编 码，放在header中请求服务端，一个例子： Authorization：Basic anlfc3NvX29hdXRoOmp5X3Nzb19vYXV0aA== 是客户端id:客户端秘钥的base64编码。 认证失败服务端返回 401 Unauthorized。

以上测试使用接口测试 完成： 

http basic认证：    

basic: 

username: 客户端id

password: 客户端密钥

![image](https://user-images.githubusercontent.com/40937248/128986225-7cfb0654-f4f6-41fb-aad6-eb365f5c56b2.png)



客户端Id和客户端密码会匹配数据库oauth_client_details表中的客户端id及客户端密码。    

点击发送： 申请令牌成功  



```
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwibmFtZSI6Imp5X3Nzb19vYXV0aCIsImlkIjpudWxsLCJleHAiOjE2Mjg3MTA5MjcsImF1dGhvcml0aWVzIjpbImFkbWluIiwidXNlciJdLCJqdGkiOiI0MTJmNWZjNC1iNGNlLTQ4NjctOTIxZi1lYzEwMDgwNGFiZGYiLCJjbGllbnRfaWQiOiJqeV9zc29fb2F1dGgiLCJ1c2VybmFtZSI6Imp5X3Nzb19vYXV0aCJ9.UzX03TzoEa9Dg3qvc4fDxKYoo6BcBiXLn_80iPDDxlsdsF5mstnuqmVHuqQ4TXtJC6WzEEQhC-K8FfyVnM4yaaQIu0N1phnWcNxnS4IW6RHklGLpZ_Lr6GFTDqGtyqQWyAFBtv1c9M-5az3ALX-LAwRoV11P2-uFb9LQJ0VVYLP1N9_0aqZfPLmfWi5e4CN8SGH82uNdtgW8iHEnykNp3k1VGtnQVd4RzMR_W-nuxWOeuxkHwAsQWmL54PKxREnp1pltttYpLNOJfHg2jxDCMuBTp5I5VLDWeWVgnMGbVS_e6YY_Wtnu17qkRgILXlRoNNrx7ItxnV9tEOcV1sXX5w",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwiYXRpIjoiNDEyZjVmYzQtYjRjZS00ODY3LTkyMWYtZWMxMDA4MDRhYmRmIiwibmFtZSI6Imp5X3Nzb19vYXV0aCIsImlkIjpudWxsLCJleHAiOjE2Mjg3MTA5MjcsImF1dGhvcml0aWVzIjpbImFkbWluIiwidXNlciJdLCJqdGkiOiI4MjBmMDkwNi1kYzZlLTRkNjEtOTdmMS1mNjhiMTc5YTc2OWMiLCJjbGllbnRfaWQiOiJqeV9zc29fb2F1dGgiLCJ1c2VybmFtZSI6Imp5X3Nzb19vYXV0aCJ9.RdQosCOKtiwKdyYBJCkH8QpLnCJTL7ff0508gpOyelLcRrMQ0o6VGJFCGTUr_pjR5IRP5-FOZvP9QKZKZn70fjloQaSGmKCkux54AMNdmuJQZB2E2YrZcCgUsCPjdmDsGZlpTV_p8TZR_i8pzGST0tgahK9PsJSmqyN593Gb5103DUorJq_2l-cAgRzSEJe0_4KLcYNb6Ik3hUQ_JvDG1J0Q1qPbi639LagmzrJTWbSV6nsxDP2n8SrCfESKE7ICe8k_6pA5Ytei3YT5KqGCn9eUdAoTSc6MqrINNOTeYoIhtg_tGclI9Y3Ynj-trmkwNJtGsV-Sj6e08IYBt3LeCQ",
  "expires_in": 47999,
  "scope": "all read write",
  "jti": "412f5fc4-b4ce-4867-921f-ec100804abdf"
}
```



返回信如下:

```properties
access_token：访问令牌，携带此令牌访问资源 
token_type：有MAC Token与Bearer Token两种类型，两种的校验算法不同，RFC 6750建议Oauth2采用 Bearer Token（http://www.rfcreader.com/#rfc6750）。 
refresh_token：刷新令牌，使用此令牌可以延长访问令牌的过期时间。 
expires_in：过期时间，单位为秒。 
scope：范围，与定义的客户端范围一致。    
jti：当前token的唯一标识
```

##### 4.2.1.4  令牌校验

Spring Security Oauth2提供校验令牌的端点，如下： 

Get: http://localhost:9200/oauth/check_token?token= [access_token]

参数： 

token：令牌

basic: 

username: 客户端id

password: 客户端密钥

使用接口测试测试如下:

![image](https://user-images.githubusercontent.com/40937248/128986299-4e1043ef-721e-48a0-b032-2b126368a50b.png)


```
{
  "scope": [
    "all",
    "read",
    "write"
  ],
  "exp": 1628710024,
  "authorities": [
    "admin",
    "user"
  ],
  "jti": "37f59666-8478-47bc-85ea-a62a9f04c2da",
  "client_id": "jy_sso_oauth"
}
```

如果令牌校验失败，会出现如下结果：

```
{
  "error": "invalid_token",
  "error_description": "Cannot convert access token to JSON"
}
```

如果令牌过期了，会如下如下结果：

```
{
  "error": "invalid_token",
  "error_description": "Token has expired"
}
```



##### 4.2.1.5 刷新令牌

刷新令牌是当令牌快过期时重新生成一个令牌，它于授权码授权和密码授权生成令牌不同，刷新令牌不需要授权码 也不需要账号和密码，只需要一个刷新令牌、客户端id和客户端密码。 

测试如下： Post：http://localhost:9200/oauth/token

body参数：    

grant_type： 固定为 refresh_token

refresh_token：刷新令牌（注意不是access_token，而是refresh_token）    

basic: 

username: 客户端id

password: 客户端密钥

![image](https://user-images.githubusercontent.com/40937248/128986365-676c1738-747b-4add-bdad-5f3a40b30ef3.png)

![image](https://user-images.githubusercontent.com/40937248/128986406-570dda88-5d21-40d7-9965-e6fd4baf53e4.png)


结果信息：

```
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwiZXhwIjoxNjI4NzEwMDI0LCJhdXRob3JpdGllcyI6WyJhZG1pbiIsInVzZXIiXSwianRpIjoiMzdmNTk2NjYtODQ3OC00N2JjLTg1ZWEtYTYyYTlmMDRjMmRhIiwiY2xpZW50X2lkIjoianlfc3NvX29hdXRoIn0.J9Bvvyqq2mOaa61ywec7-EHbkeut5P3kCpsSrWPzO8rEoGmDZITbvYIS0OnjHYyu5NhjxDJ55wdU4zlBkWlimTCgxKdLxYHD-LE6w4lRzECyhg0P6DpDs4jUWDPR3A-YlvQb0Dy6D3sb8mynqY08ob8Log4cvWWjZsGuKQ--PhcE9l2sPtIU60LsP6n-3EuGNUXHymFjrj0sHQfGhnZ_QAydc_11LqXZHNVsXFZITT8pdASGoaqShfBcm93Y0eftHwXhD67UKLMPUcelNIHFMIZXZ33Xj8rVOBKzV_rhVA39OjHP44yHbSqQifS2_g36bUpfO8q4Ze8pnWi1aH53tg",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwiYXRpIjoiMzdmNTk2NjYtODQ3OC00N2JjLTg1ZWEtYTYyYTlmMDRjMmRhIiwiZXhwIjoxNjI4NzA5NDAyLCJhdXRob3JpdGllcyI6WyJhZG1pbiIsInVzZXIiXSwianRpIjoiMjA0MDgwMzMtZjUwYi00ZDIyLTg3OWUtZjQ2NmNiN2I4NDE2IiwiY2xpZW50X2lkIjoianlfc3NvX29hdXRoIn0.JIwIbX9tDDMi8diz1JTl0BSqFpZZBVlcQoiQxjw29ksC3ffsHhNi_k33SoCeftax251Zl9vhcYZb3GcGitjNBG8XPDP1Xlt579zcu2Sc-A1C4w1jjA77ZVFZSHWGQN9JU-oN0dV3EDkSsTDasWtmPfphOSr37U9bWqaEc4UJL0fBtvFjlWGv1Sz2mHqrEyRyvkuqKfsSxGswNe9EdAg-NM1vHMxpumbLtIP3Lv8gGW0INPzbejgHgn-uoKtqlDHc1OwIfbVVJmvL1EV38Q4Be0kuuKeAfZAkSkAicQFERwYTURss0Lu4aVslKjwK7dQyVOhQvluLY4Gn3QY2UgRWbA",
  "expires_in": 47999,
  "scope": "all read write",
  "jti": "37f59666-8478-47bc-85ea-a62a9f04c2da"
}
```



#### 4.2.2 密码模式

密码模式（Resource Owner Password Credentials）与授权码模式的区别是申请令牌不再使用授权码，而是直接 通过用户名和密码即可申请令牌。 

##### 4.2.2.1 申请令牌

测试如下：

```properties
Post请求：
http://localhost:9200/oauth/token

携带参数： 
grant_type：密码模式授权填写password 
username：账号 
password：密码 
```



并且此链接需要使用 http Basic认证。 



basic: 

username: 客户端id

password: 客户端密钥   



请求：

```
http://localhost:9200/oauth/token?username=admin&password=admin&grant_type=password
```
![image](https://user-images.githubusercontent.com/40937248/128986513-7bd77d00-e024-4c0e-ac36-8a6124d275bc.png)



或者：不在basic设置值

请求：

```
http://localhost:9200/oauth/token?username=admin&password=admin&grant_type=password&client_id=jy_sso_oauth&client_secret=jy_sso_oauth

![image](https://user-images.githubusercontent.com/40937248/128986557-7d6bfc0b-12db-4c36-9092-6c9202edd798.png)


```

测试数据如下：

```
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwibmFtZSI6ImFkbWluIiwiaWQiOm51bGwsImV4cCI6MTYyODcxMDM4MSwiYXV0aG9yaXRpZXMiOlsiYWRtaW4iLCJ1c2VyIl0sImp0aSI6IjVmYWYxYzIzLTBlODktNGE4ZS05NWEwLWExODRmNDgzMmJmYiIsImNsaWVudF9pZCI6Imp5X3Nzb19vYXV0aCIsInVzZXJuYW1lIjoiYWRtaW4ifQ.KrNL0f2RjyynDMlk5Tbh6Z7iwoWC7zjqFPPYfN58lVynKhdjaHCf3ClRWwCCGF9LLOEsvjr9BjWgXtsezkjmjBC1RAbifhYKJWtCPCo8s81aibI5WVdPHi70palM1WNASVcqe99CHJtZoYjkX2AwmUV1EfjEzxO4C7l5K7iK12XW8zrkI4tm4JBLQlrol4yFc3k8OqVAXClGHP7EVJsMKzrVZpC9LmNaFo-YdvfzYjhFaYTpKar5-rYebGqH-2WjN6S3suFbM2R3tjNfzi18iElJ2vFEaMcGzwwAak0PpvO5uEBHqTjxc1KkLBUpuhYNDbVK1bTMlFZD0x0DQKYrnQ",
  "token_type": "bearer",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhbGwiLCJyZWFkIiwid3JpdGUiXSwiYXRpIjoiNWZhZjFjMjMtMGU4OS00YThlLTk1YTAtYTE4NGY0ODMyYmZiIiwibmFtZSI6ImFkbWluIiwiaWQiOm51bGwsImV4cCI6MTYyODcxMDM4MSwiYXV0aG9yaXRpZXMiOlsiYWRtaW4iLCJ1c2VyIl0sImp0aSI6IjkyMWM0NGNkLTE5NTYtNDBiZC1hMzNkLThlMGUxZjk4NmQwNiIsImNsaWVudF9pZCI6Imp5X3Nzb19vYXV0aCIsInVzZXJuYW1lIjoiYWRtaW4ifQ.Fy9y7xMH5WJVAR-JAq0kYzqD07V6QWlrmH6FNTZ1twKMbU0kOYROF1_Mc9X2GBQ494KpgLoe6Tofx15evk9gEwaI6ydkQSetrNGhZcA0sZ82YcpKfnTL8FPDUtGZCcSxtCNunVL4ehMAx7KJT2efYJzu04gc8CSB9V_35pltngbYmXe5EwKE8PIAULX7__slSq4L6i4lffBFXbrKXUlcwmvR1XOoMVFtCcBNG4INewG56QLG_klc2RrjkYTnJ32gM3ZQKJBMxFYWKOwLs8VopF6DJ4JUV3DKBDS603iCIp_SOga8rDrDosy50dkgdsxD5xiMcA1xedXXjb23vaODdw",
  "expires_in": 47999,
  "scope": "all read write",
  "jti": "5faf1c23-0e89-4a8e-95a0-a184f4832bfb"
}
```



##### 4.2.2.2 校验令牌

Spring Security Oauth2提供校验令牌的端点，如下： 

```properties
Get请求 
http://localhost:9200/oauth/check_token?token=

携带参数：
token：令牌 
```

使用接口测试如下:

![image](https://user-images.githubusercontent.com/40937248/128986667-2b118c68-c51b-423c-97e6-c7823af0216e.png)

返回结果：

```properties
{
  "scope": [
    "all",
    "read",
    "write"
  ],
  "name": "admin",
  "id": null,
  "exp": 1628710381,
  "authorities": [
    "admin",
    "user"
  ],
  "jti": "5faf1c23-0e89-4a8e-95a0-a184f4832bfb",
  "client_id": "jy_sso_oauth",
  "username": "admin"
}
```

##### 4.2.2.3 刷新令牌

刷新令牌是当令牌快过期时重新生成一个令牌，它于授权码授权和密码授权生成令牌不同，刷新令牌不需要授权码 也不需要账号和密码，只需要一个刷新令牌、客户端id和客户端密码。 

测试如下： 

```properties
Post请求
http://localhost:9200/oauth/token 

携带参数
grant_type： 固定为 refresh_token 

refresh_token：刷新令牌（注意不是access_token，而是refresh_token）    
```

   使用接口测试如下:



![image](https://user-images.githubusercontent.com/40937248/128986728-5148d073-3985-4194-b1df-ca84b01ef2b7.png)

![image](https://user-images.githubusercontent.com/40937248/128986771-5521c85b-fe45-48c4-ade9-e77e140bb77c.png)


刷新令牌成功，会重新生成新的访问令牌和刷新令牌，令牌的有效期也比旧令牌长。 

刷新令牌通常是在令牌快过期时进行刷新 。

