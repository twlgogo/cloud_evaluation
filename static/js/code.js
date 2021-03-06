const TYPE_ELEMENT = 0;
const TYPE_CRITERIA = 1;

const INVALID_PARENT = -1;
const INVALID_SOURCE = -1;

const CRITERIAS = new Map()

.set(1, '电能')
.set(2, 'UPS可用时间')
.set(3, '电压')
.set(4, '环境温度')
.set(5, '湿度')
.set(6, '丢包个数')
.set(7, '错误包数量')
.set(8, '网段划分')
.set(9, '关键监控服务通断')
.set(10, 'CPU利用率')
.set(11, 'CPU温度')
.set(12, '内存利用率')
.set(13, '磁盘连续满速时间')
.set(14, 'IO占CPU比率')
.set(15, '虚拟机User CPU利用率')
.set(16, '物理内存利用率')
.set(17, '视频监控设备状态')
.set(18, '视频监控覆盖率')
.set(19, '异常登录用户')
.set(20, 'SYN_RECV状态链接')
.set(21, '备份完整率')
.set(22, '数据库密码强度')
.set(23, '登录密码强度')
.set(24, 'IT设备冗余量')
.set(25, '双路市电接入监控')
.set(26, '7日日志完整性')
.set(27, '标准化设备比率')
.set(28, '标准虚拟机性能')
.set(29, 'PUE值')
.set(30, 'CLF值')
.set(31, 'PLF值')
.set(32, 'RER值')


const cy = installCytoscape();

let globalTree;


function url(path) {
  return path;
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

  if (node.error) {
    cy.$(`#${nodeId(node.id)}`).addClass('error');
  }

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

let firstLayout = true;
let pan = null;
let zoom = null;

function layout(cy) {
  if (firstLayout) {
    firstLayout = false;
  } else {
    pan = cy.pan();
    zoom = cy.zoom();
  }

  const layout = cy.elements().layout({
    name: 'dagre'
  });
  layout.run();

  if (pan == null || zoom == null) {
    cy.fit(-150);
  } else {
    cy.pan(pan);
    cy.zoom(zoom);
  }
}

function installCytoscape() {
  const cy = cytoscape({
    container: document.getElementById('render-panel'),

    autoungrabify: true,
    userPanningEnabled: true,

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
      },
      {
        selector: '.error',
        style: {
          'background-color': '#ff0000'
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
          alert(e)
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
  const bili = 100 / n;
  div.style['grid-template-columns'] = `${bili}% `.repeat(n).slice(0, -1);

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

function padNumber(num, size) {
  const s = "000000000" + num;
  return s.substr(s.length - size);
}

const lastState = new Map()

function findNodeById(list, id) {
  for (let i = 0; i < list.length; i++) {
    if (list[i].node.id === id) {
      return list[i];
    }
  }
  return null;
}

async function fillNodeValue (rNode) {
  if (rNode.node.type === TYPE_CRITERIA) {
    rNode.value = await pullItemValue(rNode.node.source, time + 1541491200);
  } else if (rNode.children.length === 0) {
    rNode.value = NaN;
  } else {
    rNode.value = 0;
    for (let i = 0; i < rNode.children.length; i++) {
      const child = rNode.children[i];
      await fillNodeValue(child);
      if ((child.value <= 0.001 && rNode.node.id !== 1) || rNode.value <= -0.999) {
        rNode.value = -1;
      } else {
        rNode.value += child.levelPercent * child.value;
      }
    }
    if (rNode.value <= -0.999) {
      rNode.value = 0;
    }

    // Clear last error if it's not error anymore
    if (rNode.node.id !== 1 && rNode.node.type === TYPE_ELEMENT) {
      const lastErrorId = lastState.get(rNode.node.id)
      if (lastErrorId != null) {
        if (findNodeById(rNode.children, lastErrorId).value > 0.001) {
          console.log("Clear " + rNode.node.id);
          lastState.set(rNode.node.id, null)
        }
      }
    }

    let currentError = lastState.get(rNode.node.id)

    for (let i = 0; i < rNode.children.length; i++) {
      const child = rNode.children[i];
      // Catch an error
      if ((currentError == null || currentError === child.node.id)&& child.node.type === TYPE_CRITERIA && child.value <= 0.001) {
        currentError = child.node.id
        child.node.error = true
        const date = new Date((time + 1541491200) * 1000);
        const name = CRITERIAS.get(child.node.source);
        log(`${date.getFullYear()}-${padNumber(date.getMonth() + 1, 2)}-${padNumber(date.getDate(), 2)} ${padNumber(date.getHours(), 2)}:${padNumber(date.getMinutes(), 2)}:${padNumber(date.getSeconds(), 2)} : ${name} 异常`);
        cy.$(`#${nodeId(child.node.id)}`).addClass('error');
      } else {
        child.node.error = false
        cy.$(`#${nodeId(child.node.id)}`).removeClass('error');
      }
    }

    lastState.set(rNode.node.id, currentError)
  }
}

async function clearRender() {
  lastState.clear()
  await refreshRenderPanel()
}

async function renderTick() {
  if (globalTree == null) {
    return;
  }

  await fillNodeValue(globalTree);
  await setTree(cy, globalTree)
  layout(cy)

  if (!isNaN(globalTree.value)) {
    updateTotalValue(`${Math.round(globalTree.value * 100) / 100}`);
  }

  if (globalTree.children.length === 4) {
    const value1 = globalTree.children[0].value;
    const value2 = globalTree.children[1].value;
    const value3 = globalTree.children[2].value;
    const value4 = globalTree.children[3].value;

    if (!isNaN(value1) && !isNaN(value2) && !isNaN(value3) && !isNaN(value4)) {
      updateChart(time, value1, value2, value3, value4);
    }
  }
}

async function main () {
  await setupRenderPanel();
  setupInfoPanel();
}

main().then();
