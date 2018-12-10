let timeoutID = null;
let time = 0;
let paused = false;

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
    paused = false;
    await tick();
  }
}

async function onResume () {
  if (paused && timeoutID == null) {
    paused = false;
    await tick();
  }
}

async function onPause () {
  if (!paused) {
    if (timeoutID != null) {
      window.clearTimeout(timeoutID);
    }
    timeoutID = null;
    paused = true;
  }
}

async function onStop() {
  await clearRender();
  clearChart();
  clearTotalValue();
  clearLog();

  if (timeoutID != null) {
    window.clearTimeout(timeoutID);
  }
  timeoutID = null;
  time = 0;
  paused = false;
}

function main () {
  document.getElementById('button-start').onclick = async function() { await onStart(); };
  document.getElementById('button-resume').onclick = async function() { await onResume(); };
  document.getElementById('button-pause').onclick = async function() { await onPause(); };
  document.getElementById('button-stop').onclick = async function() { await onStop(); };
}

main();
