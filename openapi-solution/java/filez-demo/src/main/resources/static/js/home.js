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

function activeSideMenu(text) {
  // drive-list中后代a元素，被选中增加active
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
        text: '本地仓库',
        icon: 'folder',
        href: '/home/local'
      },
      {
        id: 'compare',
        text: '文档比对',
        icon: 'columns',
        href: '/home/compare'
      },
      {
        id: 'aiFormat',
        text: '智能排版',
        icon: 'layout',
        href: '/home/aiFormat'
      },
      {
        id: 'watermark',
        text: '添加水印',
        icon: 'droplet',
        href: '/home/watermark'
      },
      {
        id: 'picWmPoc',
        text: '图片水印poc',
        icon: 'image',
        href: '/home/watermark/picPoc'
      },
      {
        id: 'convert',
        text: '格式转换',
        icon: 'repeat',
        href: '/home/convert'
      },
      {
        id: 'img2pdf',
        text: '图片转PDF',
        icon: 'image',
        href: '/home/imageToPdf'
      },
      {
        id: 'json2excel',
        text: 'JSON转Excel',
        icon: 'grid',
        href: '/home/jsonToExcel'
      },
      {
        id: 'bookmark',
        text: '文档套红',
        icon: 'bookmark',
        href: '/home/bookmark'
      },
      {
        id: 'docSplit',
        text: '文档拆分',
        icon: 'scissors',
        href: '/home/docSplit'
      },
      {
        id: 'taskPool',
        text: '任务池塘',
        icon: 'list',
        href: '/home/tasks'
      },
      {
        id: 'envConfig',
        text: '环境配置',
        icon: 'settings',
        href: '/home/envConfig'
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
 * 监听消息，判断是错误提示还是以iframe打开文档
 * 消息发布处于drive.js文件中
 * 1、openInCurrentTab
 * 2、showToastInParent
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
  // 显示loading-div，隐藏iframe
  const loadingDiv = document.getElementById('loading-div');
  // 去除loading-div的display属性
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
  // iframe加载完成后，隐藏loading-div，显示iframe
  const integrationFrame = document.getElementById('integration-frame');
  integrationFrame.onload = function () {
    const loadingDiv = document.getElementById('loading-div');
    loadingDiv.style.display = 'none';
  }

  // append refresh and close button
  initSideMenu();

  feather.replace();

  // 恢复侧边栏状态
  try {
    if (localStorage.getItem('filez-demo-sidebar-collapsed') === '1') {
      toggleSidebar();
    }
  } catch (e) {}
})

