# bboss elasticsearch 数据导入工具demo
使用本demo所带的应用程序运行容器环境，可以快速编写，打包发布可运行的数据导入工具

支持的数据库：
mysql,maridb，postgress,oracle ,sqlserver,db2等

支持的Elasticsearch版本：
1.x,2.x,5.x,6.x,+

支持海量PB级数据同步导入功能

使用参考文档

https://my.oschina.net/bboss/blog/1832212

# 构建部署
## 准备工作
需要通过gradle构建发布版本，因此通过以下链接下面gradle：

https://gradle.org/next-steps/?version=4.10.2&format=all

下载gradle后解压，将gradle bin目录添加到path环境变量，将gradle安装目录设置为GRADLE_HOME环境变量

## 下载源码工程-基于gradle
https://github.com/bbossgroups/db2es-booter

从上面的地址下载源码工程，然后导入idea或者eclipse，根据自己的需求，修改导入程序逻辑

org.frameworkset.elasticsearch.imp.Dbdemo

如果需要测试调试，就在test目录下面编写src/test/java/org/frameworkset/elasticsearch/imp/DbdemoTest.java

修改es和数据库配置-db2es-booter\src\test\resources\application.properties

工程已经内置mysql jdbc驱动，如果有依赖的第三方jdbc包（比如oracle驱动），可以将第三方jdbc依赖包放入db2es-booter\lib目录下

打包构建：进入命令行模式，在源码工程根目录db2es-booter下运行以下gradle指令打包发布版本

gradle clean releaseVersion

## 运行作业
gradle构建成功后，在build/distributions目录下会生成可以运行的zip包，解压运行导入程序

linux：

chmod +x restart.sh

./restart.sh

windows: restart.bat

## 作业jvm配置
修改jvm.options，设置内存大小和其他jvm参数

-Xms1g

-Xmx1g

## 在工程中添加多个表同步作业
默认的作业任务是Dbdemo，同步表td_sm_log的数据到索引dbdemo/dbdemo中

现在我们在工程中添加另外一张表td_cms_document的同步到索引cms_document/cms_document的作业步骤：

1.首先，新建一个带main方法的类org.frameworkset.elasticsearch.imp.CMSDocumentImport,实现同步的逻辑

如果需要测试调试，就在test目录下面编写 src\test\java\org\frameworkset\elasticsearch\imp\CMSDocumentImportTest.java测试类,然后debug即可

2.然后，在runfiles目录下新建CMSDocumentImport作业主程序和作业进程配置文件：runfiles/config-cmsdocmenttable.properties，内容如下：

mainclass=org.frameworkset.elasticsearch.imp.CMSDocumentImport

pidfile=CMSDocumentImport.pid  


3.最后在runfiles目录下新建作业启动sh文件（这里只新建linux/unix指令，windows的类似）：runfiles/restart-cmsdocumenttable.sh

内容与默认的作业任务是Dbdemo内容一样，只是在java命令后面多加了一个参数，用来指定作业配置文件：--conf=config-cmsdocmenttable.properties

nohup java \$RT_JAVA_OPTS -jar ${project}-${bboss_version}.jar restart --conf=config-cmsdocmenttable.properties --shutdownLevel=9 > ${project}.log &

其他stop shell指令也类似建立即可

# 管理提取数据的sql语句
 
db2es工具管理提取数据的sql语句有两种方法：代码中直接编写sql语句，配置文件中采用sql语句  
## 1.代码中写sql

		`//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑，
		// 设置增量变量log_id，增量变量名称#[log_id]可以多次出现在sql语句的不同位置中，例如：
		// select * from td_sm_log where log_id > #[log_id] and parent_id = #[log_id]
		// log_id和数据库对应的字段一致,就不需要设置setNumberLastValueColumn和setNumberLastValueColumn信息，
		// 但是需要设置setLastValueType告诉工具增量字段的类型

		importBuilder.setSql("select * from td_sm_log where log_id > #[log_id]");`
## 2.在配置文件中管理sql
设置sql语句配置文件路径和对应在配置文件中sql name
`importBuilder.setSqlFilepath("sql.xml")
			 .setSqlName("demoexportFull");	`	
 
配置文件sql.xml，编译到classes根目录即可：

<?xml version="1.0" encoding='UTF-8'?>
<properties>
    <description>
        <![CDATA[
	配置数据导入的sql
 ]]>
    </description>
    <property name="demoexport"><![CDATA[select * from td_sm_log where log_id > #[log_id]]]></property>
    <property name="demoexportFull"><![CDATA[select * from td_sm_log ]]></property>

</properties>

在sql配置文件中可以配置多条sql语句



# 数据库数据导入es使用参考文档
https://my.oschina.net/bboss/blog/1832212

## elasticsearch技术交流群:166471282 
     
## elasticsearch微信公众号:bbossgroup   
![GitHub Logo](https://static.oschina.net/uploads/space/2017/0617/094201_QhWs_94045.jpg)


