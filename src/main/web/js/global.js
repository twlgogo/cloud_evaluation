let timeoutID = null;
let time = 0;

async function onTick() {
  await renderTick()
}

async function tick() {
  await onTick()

  time += 1;

  timeoutID = window.setTimeout(async function() {
    await tick();
  }, 1000);
}

async function onStart() {
  if (timeoutID == null) {
    await tick();
  }
}

function onStop() {
  clearChart();
  clearTotalValue();

  if (timeoutID != null) {
    window.clearTimeout(timeoutID);
  }
  timeoutID = null;
  time = 0;
}

function main () {
  document.getElementById('button-start').onclick = async function() { await onStart(); };
  document.getElementById('button-stop').onclick = async function() { await onStop(); };
}

main();
