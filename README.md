# MysqlMonitor
重复造轮子不是问题，问题是要造的有价值。此工具主要用于安全工作者代码审计，网上现存的工具看了一圈没有对报错的SQL语句进行记录，这不正是代码审计时最需要的吗。
>mysql 高版本 general_log 不会记录执行错误的SQL语句到日志，需要在配置文件中[mysqld]或者[mysqld_safe]设置log-raw=1

![avatar](https://github.com/J0hnWalker/MysqlMonitor/blob/master/monitor.jpg)
![avatar](https://github.com/J0hnWalker/MysqlMonitor/blob/master/error.jpg)
