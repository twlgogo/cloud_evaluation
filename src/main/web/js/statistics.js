const ctx1 = document.getElementById('chart1').getContext('2d');
const ctx2 = document.getElementById('chart2').getContext('2d');
const ctx3 = document.getElementById('chart3').getContext('2d');
const ctx4 = document.getElementById('chart4').getContext('2d');

const data1 = []
const data2 = []
const data3 = []
const data4 = []
const labels = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]

function drawChart (ctx, name, data) {
  new Chart(ctx, {
    // The type of chart we want to create
    type: 'line',

    // The data for our dataset
    data: {
      labels: labels,
      datasets: [{
        label: name,
        borderColor: 'rgb(255, 99, 132)',
        data: data,
      }]
    },

    // Configuration options go here
    options: {
      animation: false,
      responsive: false,
      elements: {
        line: {
          tension: 0, // disables bezier curves
        }
      },
      scales: {
        yAxes: [{
          ticks: {
            min: 0,
            max: 1
          }
        }]
      }
    }
  });
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

clearChart()
