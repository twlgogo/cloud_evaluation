const ctx1 = document.getElementById('chart1');
const ctx2 = document.getElementById('chart2');
const ctx3 = document.getElementById('chart3');
const ctx4 = document.getElementById('chart4');

const data1 = []
const data2 = []
const data3 = []
const data4 = []
const labels = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]

// 11月6日下午4点
// 1541491200

function drawChart (ctx, name, data) {
  const myChart = echarts.init(ctx);

  const option = {
    title: {
      text: name,
      x:'center',
      y:'top',
    },
    xAxis: {
      type: 'category',
      data: labels
    },
    yAxis: {
      type: 'value',
      min:0.0,
      max:1.0
    },
    series: [{
      data: data,
      type: 'line'
    }]
  }

  myChart.setOption(option, true);
}

function clearArray(array) {
  while(array.length > 0) {
    array.pop();
  }
}

function updateChart(time, value1, value2, value3, value4) {
  if (data1.length === 0) {
    clearArray(labels);
    for (let i = 0; i < 10; i++) {
      labels.push((time + i).toString());
    }
    clearArray(data1);
    data1.push(value1);
    clearArray(data2);
    data2.push(value2);
    clearArray(data3);
    data3.push(value3);
    clearArray(data4);
    data4.push(value4);

  } else if (data1.length < 10) {
    data1.push(value1);
    data2.push(value2);
    data3.push(value3);
    data4.push(value4);

  } else {
    labels.shift();
    labels.push(time.toString());
    data1.shift();
    data1.push(value1);
    data2.shift();
    data2.push(value2);
    data3.shift();
    data3.push(value3);
    data4.shift();
    data4.push(value4);
  }

  drawChart(ctx1, "业务延续性", data1);
  drawChart(ctx2, "能耗", data2);
  drawChart(ctx3, "资源池状态", data3);
  drawChart(ctx4, "动环指标", data4);
}

function clearChart() {
  clearArray(labels);
  for (let i = 0; i < 10; i++) {
    labels.push(i.toString());
  }
  clearArray(data1);
  clearArray(data2);
  clearArray(data3);
  clearArray(data4);

  drawChart(ctx1, "业务延续性", data1);
  drawChart(ctx2, "能耗", data2);
  drawChart(ctx3, "资源池状态", data3);
  drawChart(ctx4, "动环指标", data4);
}

function updateTotalValue(value) {
  document.getElementById('total-score').innerHTML = value.toString();
}

function clearTotalValue() {
  document.getElementById('total-score').innerHTML = 'NaN';
}

clearChart();
clearTotalValue();
