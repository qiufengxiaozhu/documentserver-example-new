let frameEle;

function appendBtn(ulId, text, clickFun, spanIcon, href, id) {
  const rootEle = document.getElementById(ulId);
  // create span element and attribute data-feather is spanIcon
  let span = document.createElement('span');
  // default spanIcon is chevron-right
  spanIcon = spanIcon ? spanIcon : 'chevron-right';
  span.setAttribute('data-feather', spanIcon);

  // create a element and attribute onclick is clickFun and class is nav-link
  let a = document.createElement('a');
  let onMenuClick = clickFun;
  if (!onMenuClick) {
    onMenuClick = () => {
      activeSideMenu(text);
      document.getElementById('zOfficeDoc').innerHTML = '';
      setIframeSrc(href);
    };
  }
  a.onclick = onMenuClick;
  // add data-href attribute
  a.className = 'nav-link';
  if (id) a.id = id;

  // create li element and attribute class is nav-item
  let li = document.createElement('li');
  li.className = 'nav-item';

  // append span and text to a element
  a.append(span);
  a.append(text);

  // append a element to li element
  li.append(a);

  // append li element to ul element
  rootEle.append(li);

  // call feather.replace() to render icon
  feather.replace();
}

// funtion to get frameEle
function getFrameEle() {
  if (!frameEle) frameEle = document.getElementById("integration-frame");
  return frameEle;
}

function batchOp() {
  setIframeSrc('/home/local/batch')
}

function activeSideMenu(text) {
  // Add active class to selected descendant a elements in drive-list
  const fun = (id) => {
    const driveList = document.getElementById(id);
    const aList = driveList.getElementsByTagName('a');
    for (let i = 0; i < aList.length; i++) {
      aList[i].classList.remove('active');
      if (aList[i].innerText === text) {
        aList[i].classList.add('active');
      }
    }
  }
  fun('drive-list')
}

const sideMenu = [
  {
    menuId: 'drive-list',
    subMenu: [
      {
        id: 'local',
        text: 'Local Repository',
        icon: 'folder',
        href: '/home/local'
      },
      {
        id: 'compare',
        text: 'Document Comparison',
        icon: 'folder',
        href: '/home/compare'
      }
    ]
  }
]

function initSideMenu() {
  sideMenu.forEach(menu => {
    const menuId = menu.menuId;
    menu.subMenu.forEach(subMenu => {
      appendBtn(menuId, subMenu.text, subMenu.clickFun, subMenu.icon, subMenu.href, subMenu.id);
    })
  })
}

/**
 * Listen to messages to determine whether it's an error prompt or opening a document in iframe
 * Message publishing is in the drive.js file
 * 1. showToastInParent
 */
window.addEventListener('message', function (event) {
  const data = event.data;
  if (data.type === 'showToast') {
    showToast(data.msg);
  } else if (data.type === 'updateFrameSrc') {
    setIframeSrc(data.msg.url);
  }
});

// function to set the iframe src
const setIframeSrc = (url) => {
  // Show loading-div, hide iframe
  const loadingDiv = document.getElementById('loading-div');
  // Remove display property of loading-div
  loadingDiv.style.display = '';

  const frameEle = getFrameEle();
  frameEle.src = url;
}

// function to show toast
const showToast = (msg) => {
  var el = document.createElement("div");
  el.setAttribute("style", "position:absolute;top:8%;left:45%");
  el.setAttribute("role", "alert");
  // set el class
  el.className = 'alert alert-dark';

  el.innerHTML = msg;
  setTimeout(function () {
    el.parentNode.removeChild(el);
  }, 4000);
  document.body.appendChild(el);
}

$(() => {
  // After iframe loading is complete, hide loading-div and show iframe
  const integrationFrame = document.getElementById('integration-frame');
  integrationFrame.onload = function () {
    const loadingDiv = document.getElementById('loading-div');
    loadingDiv.style.display = 'none';
  }

  // append refresh and close button
  initSideMenu();

  feather.replace();
})

