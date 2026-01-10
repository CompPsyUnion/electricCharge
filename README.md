# electricCharge

宁波诺丁汉大学生活区查询寝室电费

使用的jdk21

## 说明

楼层映射[文件](https://github.com/CompPsyUnion/electricCharge/blob/main/src/main/java/cn/yiming1234/electriccharge/service/ElectricService.java)

由于没有常规渠道获取Cookie，只能定期抓包更新到数据库中，周期未知

如果Cookie过期，电费数据能够正常获取，但是付款链接点击后会显示下单账户和付款账户不匹配

当项目在本地运行时要配合微信公众号进行响应式交互，需要进行内网穿透，并在“设置与开发-服务器配置”中进行修改

## 部署方法

首先克隆仓库

```shell
git clone https://github.com/CompPsyUnion/electricCharge
```

然后补充需要自行填充的[环境变量](https://github.com/CompPsyUnion/electricCharge/tree/main/src/main/docker/.env.template)

运行下面的命令直接运行

```shell
docker-compose -f src/main/docker/docker-compose.yml up -d
```

或者填充[application-template.yml](https://github.com/CompPsyUnion/electricCharge/tree/main/src/main/resources/application-template.yml)在本地运行
