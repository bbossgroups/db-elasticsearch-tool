# bboss elasticsearch 数据导入工具demo

# 构建部署
## 准备工作
通过gradle构建发布版本：
https://gradle.org/next-steps/?version=4.10.2&format=all
下载后解压，gradle bin目录配置到环境变量path，将安装目录设置GRADLE_HOME环境变量

## 下载源码工程-基于gradle
https://github.com/bbossgroups/db2es-booter

下载下来后，导入idea或者eclipse，根据自己的需求，修改导入程序逻辑

org.frameworkset.elasticsearch.imp.Dbdemo

修改es和数据库配置-db2es-booter\src\test\resources\application.properties

在源码根目录下运行以下gradle指令打包发布版本
gradle clean releaseVersion

## 运行作业
gradle构建成功后，在build/distributions目录下会生成可以运行的zip包，解压运行导入程序

linux：

chmod +x startup.sh

./startup.sh

windows: startup.bat

# 数据库数据导入es使用参考文档
https://my.oschina.net/bboss/blog/1832212

## elasticsearch技术交流群:166471282 
     
## elasticsearch微信公众号:bbossgroup   
![GitHub Logo](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)


