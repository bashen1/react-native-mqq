# react-native-mqq

[![npm version](https://badge.fury.io/js/react-native-mqq.svg)](http://badge.fury.io/js/react-native-mqq)

此库基于react-native-qq，在此感谢这个库的开发者们的贡献。

React Native的QQ登录插件, react-native版本需要0.40.0及以上

SDK版本：

Android：3.5.13.83

iOS：3.5.17.4

## 如何安装

### 首先安装npm包

```bash
yarn add react-native-mqq
```

或

```bash
npm install react-native-mqq --save
```

然后执行

```bash
react-native link react-native-mqq
```

### 安装iOS工程

在工程plist文件中加入qq白名单：(ios9以上必须)

请以文本方式打开Info.plist，在其中添加

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
    <!-- QQ、Qzone URL Scheme 白名单-->
    <string>mqqapi</string>
    <string>mqq</string>
    <string>mqqOpensdkSSoLogin</string>
    <string>mqqconnect</string>
    <string>mqqopensdkdataline</string>
    <string>mqqopensdkgrouptribeshare</string>
    <string>mqqopensdkfriend</string>
    <string>mqqopensdkapi</string>
    <string>mqqopensdkapiV2</string>
    <string>mqqopensdkapiV3</string>
    <string>mqzoneopensdk</string>
    <string>wtloginmqq</string>
    <string>wtloginmqq2</string>
    <string>mqqwpa</string>
    <string>mqzone</string>
    <string>mqzonev2</string>
    <string>mqzoneshare</string>
    <string>wtloginqzone</string>
    <string>mqzonewx</string>
    <string>mqzoneopensdkapiV2</string>
    <string>mqzoneopensdkapi19</string>
    <string>mqzoneopensdkapi</string>
    <string>mqzoneopensdk</string>
 </array>
```

在`Info->URL Types` 中增加QQ的scheme： `Identifier` 设置为`qq`, `URL Schemes` 设置为你注册的QQ开发者账号中的APPID，需要加前缀`tencent`，例如`tencent1104903684`

在你工程的`AppDelegate.m`文件中添加如下代码：

```oc
#import <React/RCTLinkingManager.h>

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
  return [RCTLinkingManager application:application openURL:url sourceApplication:sourceApplication annotation:annotation];
}

```

### 安装Android工程

在`android/app/build.gradle`里，defaultConfig栏目下添加如下代码：

```oc
manifestPlaceholders = [
    QQ_APPID: "<平台申请的APPID>"
]
```

以后如果需要修改APPID，只需要修改此一处。

另外，确保你的MainActivity.java中有`onActivityResult`的实现：

```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        mReactInstanceManager.onActivityResult(requestCode, resultCode, data);
    }
```

## 如何使用

### 引入包

```js
import * as QQAPI from 'react-native-mqq';

// 初始化，调用其他api时请先调用该api，隐私合规后可调用此API进行初始化操作
QQAPI.init();

// 调用QQ登录，可能会跳转到QQ应用或者打开一个网页浏览器以供用户登录。在本次login返回前，所有接下来的login调用都会直接失败。
// - scopes: 登录所申请的权限，默认为get_simple_userinfo。 需要多个权限时，以逗号分隔。
// 返回一个`Promise`对象。成功时的回调为一个类似这样的对象：
QQAPI.login([scopes]);

{
    "access_token": "CAF0085A2AB8FDE7903C97F4792ECBC3",
    "openid": "0E00BA738F6BB55731A5BBC59746E88D",
    "expires_in": "1458208143094.6",
    "oauth_consumer_key": "12345"
}

// 分享到QQ好友，参数同QQAPI.shareToQzone，返回一个`Promise`对象
QQAPI.shareToQQ(data);

// 分享到QZone，参数为一个object，可以有如下的形式：
QQAPI.shareToQzone(data)

// 分享图文消息
{
    type: 'news',
    title: '分享标题',
    description: '描述',
    webpageUrl: '网页地址',
    imageUrl: '远程图片地址',
}
```

## 常见问题

### Android: 调用QQAPI.login()没有反应

通常出现这个原因是因为Manifest没有配置好，检查Manifest中有关Activity的配置。

### Android: 已经成功激活QQ登录，但回调没有被执行

通常出现这个原因是因为MainActivity.java中缺少onActivityResult的调用。
