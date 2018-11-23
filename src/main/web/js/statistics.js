const ctx1 = document.getElementById('chart1').getContext('2d');
const ctx2 = document.getElementById('chart2').getContext('2d');
const ctx3 = document.getElementById('chart3').getContext('2d');
const ctx4 = document.getElementById('chart4').getContext('2d');






data






function drawChart (ctx) {
  new Chart(ctx, {
    // The type of chart we want to create
    type: 'line',

    // The data for our dataset
    data: {
      labels: ["January", "February", "March", "April", "May", "June", "July"],
      datasets: [{
        label: "My First dataset",
        borderColor: 'rgb(255, 99, 132)',
        data: [0, 10, 5, 2, 20, 30, 45],
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
      }
    }
  });
}

drawChart(ctx1);
drawChart(ctx2);
drawChart(ctx3);
drawChart(ctx4);
