const TYPE_ELEMENT = 0;
const TYPE_CRITERIA = 1;

const INVALID_PARENT = -1;
const INVALID_SOURCE = -1;


const cy = installCytoscape();

let globalTree;


function url(path) {
  return "http://127.0.0.1:8080" + path;
}

function reciprocal(num) {
  if (parseFloat(num) === 1) {
    return "1";
  } else {
    return (1 / parseFloat(num)).toString();
  }
}

function toPercent(num) {
  num *= 100;
  return `${Math.round(num * 100) / 100}%`
}

function nodeId(id) {
  return `n${id}`
}

function edgeId(sourceId, targetId) {
  return `e${sourceId}-${targetId}`
}

async function pullTree() {
  const response = await fetch(url("/get_tree"));
  const result = await response.json()

  if (result.success) {


    const queue = [];
    queue.push(result.value);

    while (queue.length !== 0) {
      const rNode = queue.shift();

      rNode.value = NaN
      if (rNode.levelPercent == null) {
        rNode.levelPercent = 1;
      }
      if (rNode.globalPercent == null) {
        rNode.globalPercent = 1;
      }

      rNode.children.forEach((child, index) => {
        if (rNode.node.currentValue != null) {
          child.levelPercent = rNode.node.currentValue.vector[index];
          child.globalPercent = child.levelPercent * rNode.globalPercent;
        } else if (rNode.children.length === 1 && child.node.type === TYPE_CRITERIA) {
          child.levelPercent = 1;
          child.globalPercent = rNode.globalPercent;
        } else {
          child.levelPercent = NaN;
          child.globalPercent = NaN;
        }
        queue.push(child)
      })
    }


    return result.value;
  } else {
    throw result.error;
  }
}

async function pushNode(name, type, parent, source) {
  const response = await fetch(url(`/add_node?name=${encodeURIComponent(name)}&type=${encodeURIComponent(type)}&parent=${encodeURIComponent(parent)}&source=${encodeURIComponent(source)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

async function updateNode(id, name, type, source) {
  const response = await fetch(url(`/update_node?id=${encodeURIComponent(id)}&name=${encodeURIComponent(name)}&type=${encodeURIComponent(type)}&source=${encodeURIComponent(source)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

async function removeNode(id) {
  const response = await fetch(url(`/remove_node?id=${encodeURIComponent(id)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

async function pushNodeValue(nodeId, matrix) {
  const response = await fetch(url(`/add_node_value?nodeId=${encodeURIComponent(nodeId)}&matrixStr=${encodeURIComponent(matrix)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

async function removeNodeValue(nodeId, nodeValueId) {
  const response = await fetch(url(`/remove_node_value?nodeId=${encodeURIComponent(nodeId)}&nodeValueId=${encodeURIComponent(nodeValueId)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

async function pullItemValue(itemId, timestamp) {
  const response = await fetch(url(`/get_item_id?itemId=${encodeURIComponent(itemId)}&timestamp=${encodeURIComponent(timestamp)}`));
  const result = await response.json()

  if (result.success) {
    return result.value;
  } else {
    throw result.error;
  }
}

/**
 * Add the node and the edge to it's parent, to the screen.
 */
function addNode(cy, rNode) {
  const node = rNode.node;

  cy.add({
    group: "nodes",
    data: { id: nodeId(node.id), node: node, name: node.name, percent: toPercent(rNode.globalPercent), value: Math.round(rNode.value * 100) / 100 },
  });

  cy.$(`#${nodeId(node.id)}`).on("click", function() {
    showInfoPanel();
    fillInfoPanelWithRNode(rNode)
  });

  if (node.parent !== INVALID_PARENT) {
    cy.add({
      group: "edges",
      data: { id: edgeId(node.parent, node.id), source: nodeId(node.parent), target: nodeId(node.id) },
    });
  }
}

function setTree(cy, tree) {
  cy.elements().remove();

  const queue = []
  queue.push(tree)

  while (queue.length !== 0) {
    const rNode = queue.shift()
    addNode(cy, rNode)

    rNode.children.forEach((child) => {
      queue.push(child)
    })
  }
}

function layout(cy) {
  const layout = cy.elements().layout({
    name: 'dagre'
  });
  layout.run();
  cy.fit(150);
}

function installCytoscape() {
  const cy = cytoscape({
    container: document.getElementById('render-panel'),

    autoungrabify: true,

    layout: {
      name: 'dagre'
    },

    style: [
      {
        selector: 'node',
        style: {
          'background-color': '#11479e'
        }
      },
      {
        selector: 'edge',
        style: {
          'curve-style': 'bezier',
          'width': 4,
          'target-arrow-shape': 'triangle',
          'line-color': '#9dbaea',
          'target-arrow-color': '#9dbaea'
        }
      }
    ]
  });

  cy.nodeHtmlLabel([{
    query: 'node',
    valign: "center",
    halign: "right",
    valignBox: "center",
    halignBox: "right",
    tpl: function(data) {
      return `<span class="node-label">${data.name}</span></br><span class="node-label">${data.percent}</span></br><span class="node-label">${data.value}</span>`;
    }
  }]);

  return cy;
}

async function refreshRenderPanel() {
  let tree = await pullTree()
  if (tree == null) {
    // No root, add a root
    await pushNode("root", TYPE_ELEMENT, INVALID_PARENT, INVALID_SOURCE)
    tree = await pullTree()
  }

  globalTree = tree;

  await setTree(cy, tree)

  layout(cy)
}

async function setupRenderPanel() {
  await refreshRenderPanel();
}

function showInfoPanel() {
  const panel = document.getElementById('info-panel');
  panel.style.display = "block";
}

function hideInfoPanel() {
  const panel = document.getElementById('info-panel');
  panel.style.display = "none";
}

function fillInfoPanelWithRNode(rNode) {
  const node = rNode.node;

  // Info Node

  const nodeNameValue = document.getElementById('info-node-name-value');
  nodeNameValue.value = node.name;

  const nodeTypeValue = document.getElementById('info-node-type-value');
  nodeTypeValue.value = node.type.toString();
  document.getElementById('info-node-type-value-criteria').style.display = rNode.children.length === 0 ? 'block' : 'none';

  const nodeSource = document.getElementById('info-node-source');
  nodeSource.style.display = node.type === TYPE_CRITERIA ? 'block' : 'none';
  const nodeSourceValue = document.getElementById('info-node-source-value');
  if (node.type === TYPE_CRITERIA && node.source != null) {
    nodeSourceValue.value = node.source.toString();
  } else {
    nodeSourceValue.value = "0";
  }

  const nodeChange = document.getElementById('info-node-change');
  nodeChange.onclick = async function () {
    const id = node.id;
    const name = nodeNameValue.value;
    const type = parseInt(nodeTypeValue.value);
    const source = type === TYPE_ELEMENT ? INVALID_SOURCE : parseInt(nodeSourceValue.value);
    // TODO check name, type and source
    await updateNode(id, name, type, source)
    await refreshRenderPanel();
    cy.$(`#${nodeId(node.id)}`).emit("click");
  }

  const canBeDeleted = rNode.children.length === 0 && rNode.node.parent !== INVALID_PARENT;
  const nodeRemove = document.getElementById('info-node-remove');
  nodeRemove.style.display = canBeDeleted ? 'block' : 'none';
  nodeRemove.onclick = async function () {
    if (canBeDeleted) {
      await removeNode(node.id);
      await refreshRenderPanel();
      hideInfoPanel();
    }
  };

  // Add child

  const addChild = document.getElementById('info-add-child');
  addChild.style.display = node.type === TYPE_ELEMENT ? 'block' : 'none';

  const addChildNameValue = document.getElementById('info-add-child-name-value');
  addChildNameValue.value = "New Child";

  const addChildTypeValue = document.getElementById('info-add-child-type-value');
  addChildTypeValue.value = TYPE_ELEMENT.toString();

  const addChildSource = document.getElementById('info-add-child-source');
  addChildSource.style.display = 'none';
  const addChildSourceValue = document.getElementById('info-add-child-source-value');
  addChildSourceValue.value = "0";

  const addChildAdd = document.getElementById('info-add-child-add');
  addChildAdd.onclick = async function () {
    const name = addChildNameValue.value;
    const type = parseInt(addChildTypeValue.value);
    const parent = node.id;
    const source = type === TYPE_ELEMENT ? INVALID_SOURCE : parseInt(addChildSourceValue.value);
    // TODO check name, type and source
    await pushNode(name, type, parent, source);
    await refreshRenderPanel();
    cy.$(`#${nodeId(node.id)}`).emit("click");
  }

  // Matrix
  const matrix = document.getElementById('info-matrix');
  const showMatrix = node.type === TYPE_ELEMENT && rNode.children.length > 1;
  matrix.style.display = showMatrix ? 'block' : 'none';
  if (showMatrix) {
    // Current vector
    const currentVector = (node.currentValue != null && node.currentValue.vector != null) ? node.currentValue.vector : null;
    document.getElementById('info-matrix-current-vector').textContent = createVectorText(currentVector);

    // New matrix
    const newMatrixContainer = document.getElementById('info-matrix-new-matrix-container');
    while (newMatrixContainer.firstChild) {
      newMatrixContainer.removeChild(newMatrixContainer.firstChild);
    }
    const newMatrix = createMatrixDiv(rNode.children.length, null);
    newMatrixContainer.appendChild(newMatrix);
    document.getElementById('info-matrix-new-add').onclick = async function () {
      const matrixText = createMatrixText(rNode.children.length, newMatrix)
      if (matrixText != null) {
        try {
          await pushNodeValue(node.id, matrixText);
          await refreshRenderPanel();
          cy.$(`#${nodeId(node.id)}`).emit("click");
        } catch (e) {
          // TODO pop error
        }
      }
    }

    // Matrix history
    const historyContainer = document.getElementById('info-matrix-history');
    while (historyContainer.firstChild) {
      historyContainer.removeChild(historyContainer.firstChild);
    }
    node.historyValues.reverse().forEach((child) => {
      historyContainer.appendChild(createHistoryEntryDiv(node, child))
    })
  }
}

function createMatrixDiv(n, list) {
  if (n < 2 || (list != null && n * (n - 1) / 2 !== list.length)) {
    return null;
  }

  const div = document.createElement("div");
  div.className = 'matrix-container';
  div.style['grid-template-columns'] = 'auto '.repeat(n).slice(0, -1);

  const texts = {};

  let index = 0;
  for (let i = 0; i < n; i++) {
    texts[`${i}-${i}`] = "1";
    for (let j = i + 1; j < n; j++) {
      if (list != null) {
        texts[`${i}-${j}`] = list[index].toString();
        texts[`${j}-${i}`] = reciprocal(list[index].toString())
        index++;
      } else {
        texts[`${i}-${j}`] = ""
        texts[`${j}-${i}`] = ""
      }
    }
  }

  const cells = []
  for (let i = 0; i < n; i++) {
    for (let j = 0; j < n; j++) {
      let cell;
      if (i >= j || list != null) {
        cell = document.createElement("div");
        cell.className = 'matrix-item';
        cell.textContent = texts[`${i}-${j}`];
      } else {
        cell = document.createElement("input")
        cell.className = 'matrix-item';
        cell.type = "text";
        cell.value = texts[`${i}-${j}`];
        const x = i;
        const y = j;
        cell.onchange = function () {
          cells[y * n + x].textContent = reciprocal(cell.value);
        }
      }
      cells[i * n + j] = cell;
      div.appendChild(cell);
    }
  }

  return div;
}

function createMatrixText(n, div) {
  if (div.childElementCount !== n * n) {
    return null;
  }

  let text = (n * (n - 1) / 2).toString()
  for (let i = 0; i < n; i++) {
    for (let j = 0; j < n; j++) {
      if (i < j) {
        text += ",";
        const num = parseFloat(div.children[i * n + j].value);
        if (!isNaN(num)) {
          text += num;
        } else {
          return null;
        }
      }
    }
  }

  return text;
}

function createVectorDiv(list) {
  const div = document.createElement("div");
  div.textContent = list != null ? list.map(x => x.toString()).join(", ") : "None";
  return div;
}

function createVectorText(list) {
  return list != null ? list.map(x => x.toString()).join(", ") : "None";
}

function createHistoryEntryDiv(node, nodeValue) {
  const div = document.createElement("div");

  const grid = document.createElement("div");
  grid.className = "grid-two";
  div.appendChild(grid);

  const matrix = createMatrixDiv(nodeValue.n, nodeValue.matrix);
  grid.appendChild(matrix);

  const buttons = document.createElement("div");
  grid.appendChild(buttons);

  const remove = document.createElement("input");
  remove.type = "submit";
  remove.value = "Remove"
  remove.onclick = async function() {
    await removeNodeValue(node.id, nodeValue.id)
    await refreshRenderPanel();
    cy.$(`#${nodeId(node.id)}`).emit("click");
  }
  buttons.appendChild(remove);

  const vector = createVectorDiv(nodeValue.vector);
  div.appendChild(vector);

  return div;
}

function setupInfoPanel() {
  window.onclick = function(event) {
    const panel = document.getElementById('info-panel');
    if (event.target === panel) {
      hideInfoPanel();
    }
  }

  // Only show source-selector if the type is 1
  const nodeSource = document.getElementById('info-node-source');
  nodeSource.style.display = 'none';
  document.getElementById('info-node-type-value').onchange = function (event) {
    nodeSource.style.display = event.target.value === TYPE_CRITERIA.toString() ? 'block' : 'none';
  }

  // Only show source-selector if the type is 1
  const addChildSource = document.getElementById('info-add-child-source');
  addChildSource.style.display = 'none';
  document.getElementById('info-add-child-type-value').onchange = function (event) {
    addChildSource.style.display = event.target.value === TYPE_CRITERIA.toString() ? 'block' : 'none';
  }
}

let timeoutID = null;
let time = 0;

async function fillNodeValue (rNode) {
  if (rNode.node.type === TYPE_CRITERIA) {
    rNode.value = await pullItemValue(rNode.node.source, time);
  } else if (rNode.children.length === 0) {
    rNode.value = NaN;
  } else {
    rNode.value = 0;
    for (let i = 0; i < rNode.children.length; i++) {
      const child = rNode.children[i];
      await fillNodeValue(child);
      rNode.value += child.levelPercent * child.value;
    }
  }
}

async function tick() {
  if (globalTree == null) {
    return;
  }

  await fillNodeValue(globalTree);
  await setTree(cy, globalTree)
  layout(cy)

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
  if (timeoutID != null) {
    window.clearTimeout(timeoutID);
  }
  timeoutID = null;
  time = 0;
}

async function main () {
  await setupRenderPanel();
  document.getElementById('button-start').onclick = async function() { await onStart(); };
  document.getElementById('button-stop').onclick = async function() { await onStop(); };
  setupInfoPanel();
}

main().then();
