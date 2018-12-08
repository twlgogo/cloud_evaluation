const logPanel = document.getElementById("log-panel");

function log (text) {
  logPanel.innerHTML += text + "<br/>";
}

function clearLog () {
  logPanel.innerHTML = "";
}
