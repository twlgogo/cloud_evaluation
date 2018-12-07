const logPanel = document.getElementById("log-panel");

function log (text) {
  logPanel.innerHTML += text + "<br/>";
}


log("2018-11-25 17:05  CPU 温度超阈值")
log("2018-11-25 17:10  磁盘可用存储小于 15%")
log("2018-11-25 17:15  网络带宽占用过高")
log("2018-11-25 17:20  可用内存不足")
log("2018-11-25 17:25  PUE 超阈值")
log("2018-11-25 17:30  Tomcat 状态异常")
log("2018-11-25 17:35  UPS 可用时间小于 30")
log("2018-11-25 17:40  电压超阈值 10%")
log("2018-11-25 17:45  检测到瞬时大量 TCP 连接")
