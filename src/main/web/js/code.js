const TYPE_ELEMENT = 0;
const TYPE_CRITERIA = 1;

const INVALID_PARENT = -1;
const INVALID_SOURCE = -1;


const cy = installCytoscape()


function url(path) {
  return "http://127.0.0.1:8080" + path;
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
    return result.value;
  } else {
    throw result.error;
  }
}

async function pushNode(name, type, parent) {
  const response = await fetch(url(`/add_node?name=${encodeURIComponent(name)}&type=${encodeURIComponent(type)}&parent=${encodeURIComponent(parent)}`));
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

/**
 * Add the node and the edge to it's parent, to the screen.
 */
function addNode(cy, rNode) {
  const node = rNode.node;

  cy.add({
    group: "nodes",
    data: { id: nodeId(node.id), label: node.name, node: node },
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
}

function installCytoscape() {
  return window.cy = cytoscape({
    container: document.getElementById('render-panel'),

    autoungrabify: true,

    layout: {
      name: 'dagre'
    },

    style: [
      {
        selector: 'node',
        style: {
          'content': 'data(label)',
          'text-opacity': 0.5,
          'text-valign': 'center',
          'text-halign': 'right',
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
}

async function refreshRenderPanel() {
  let tree = await pullTree()
  if (tree == null) {
    // No root, add a root
    await pushNode("root", TYPE_ELEMENT, INVALID_PARENT)
    tree = await pullTree()
  }

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

  const nodeSource = document.getElementById('info-node-source');
  nodeSource.style.display = node.type === TYPE_CRITERIA ? 'block' : 'none';
  const nodeSourceValue = document.getElementById('info-node-source-value');
  if (node.type === 1 && node.source != null) {
    nodeSourceValue.value = node.source.toString();
  }

  const nodeChange = document.getElementById('info-node-change');
  nodeChange.onclick = async function () {
    const id = node.id;
    const name = nodeNameValue.value;
    const type = parseInt(nodeTypeValue.value);
    const source = type === TYPE_ELEMENT ? INVALID_SOURCE : parseInt(nodeSourceValue.value);
    // TODO check name, type and source
    rNode.node = await updateNode(id, name, type, source)
    await refreshRenderPanel();
  }

  // Add child

  const addChild = document.getElementById('info-add-child');
  addChild.style.display = node.type === TYPE_ELEMENT ? 'block' : 'none';
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

async function main () {
  await setupRenderPanel();
  setupInfoPanel();
}

main().then();
