# archer

## 目的
* 提供http请求的可靠异步发送服务
* - 高负载的异步发送
* - 定时发送计划
* - 持久化消息（MySQL）
* - 断点恢复
* - 集群化水平扩展

## todo list
* [x] 基于spring的基础版
* [ ] 考虑替换消息体内的读写锁
* [ ] 尝试去除spring
* [ ] 考虑是否不再依赖MySQL和Redis
