const drive = "local";

/**
 * Open file
 * @param docId
 * @param action Open mode: view or edit
 */
function openDoc(docId, action) {
    window.open(`/v2/context/openDoc?docId=${docId}&action=${action}`);
}

/**
 * Download file in current page popup
 * @param docId
 */
function downloadDoc(docId) {
    if (!docId) return;
    window.open(`/v2/context/${docId}/content?download=true`);
}

/**
 * Upload file
 */
function uploadDoc() {
    $('#fileUpload').click();
}

/**
 * Delete file
 */
function deleteDoc(docId) {
    if (!docId) return;
    const url = `/v2/context/file/delete/${docId}?driveId=${drive}`;
    $.get(url, function (data, status) {
        console.log('delete doc success');
        // Remove file, find the element with data-id as docId in the element with id file-list, and delete the corresponding tr element
        document.getElementById("file-list").querySelector(`td[data-id="${docId}"]`).parentElement.remove();
        // Send message to parent page
        showToastInParent(`${data.name} deleted successfully`);
     }).fail(function (xhr, status, error) {
        handleError('Delete', xhr, status, error);
    });
}

/**
 * Upload file
 * @param file
 */
function uploadFile(file) {
    if (!file) return;
    const url = `/v2/context/file/upload`;
    const formData = new FormData();
    formData.append('file', file, file.name);
    // Get the active drive from nav-link in drive-list
    formData.append('drive', drive);
    $.ajax({
        url,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (data) {
            if (!data) return;
            console.log('data: ', data);
            if (data.id) {
                console.log('upload doc success');
                addDocToList(data);
                eraseFileInput($('#fileUpload'));
                showToastInParent(`${data.name} uploaded successfully`)
            }
        },
        error: function (xhr, status, error) {
            eraseFileInput($('#fileUpload'));
            handleError('File upload', xhr, status, error);
        }
    });
}

/**
 * Clear selected files in file selection box
 * @param jqFileInput
 */
function eraseFileInput(jqFileInput) {
    jqFileInput.wrap('<form>').closest('form').get(0).reset();
    jqFileInput.unwrap();
}

/**
 * Add mouse click events for files
 * @param doc
 */
function addDocToList(doc) {

    // Edit button
    const editBtnDiv = document.createElement('div');
    editBtnDiv.className = 'op-btn';
    editBtnDiv.addEventListener('click', function () { openDoc(doc.id, 'edit'); });
    const editBtnSpan = document.createElement('span');
    editBtnSpan.setAttribute('data-feather', 'edit');
    editBtnDiv.append(editBtnSpan);
    // Download button
    const downloadBtnDiv = document.createElement('div');
    downloadBtnDiv.className = 'op-btn';
    downloadBtnDiv.addEventListener('click', function () { downloadDoc(doc.id); });
    const downloadBtnSpan = document.createElement('span');
    downloadBtnSpan.setAttribute('data-feather', 'download');
    downloadBtnDiv.append(downloadBtnSpan);
    // Delete button
    const deleteBtnDiv = document.createElement('div');
    deleteBtnDiv.className = 'op-btn';
    deleteBtnDiv.addEventListener('click', function () { deleteDoc(doc.id); });
    const deleteBtnSpan = document.createElement('span');
    deleteBtnSpan.setAttribute('data-feather', 'trash-2');
    deleteBtnDiv.append(deleteBtnSpan);
    // More button
    const opMoreBtnDiv = document.createElement('div');
    opMoreBtnDiv.className = 'op-btn';
    opMoreBtnDiv.addEventListener('click', function () { onFileOpClick(doc.id); });
    const opMoreBtnSpan = document.createElement('span');
    opMoreBtnSpan.setAttribute('data-feather', 'more-horizontal');
    opMoreBtnDiv.append(opMoreBtnSpan);

    // Button group
    const opBtnDiv = document.createElement('div');
    opBtnDiv.className = 'multi-btn';
    opBtnDiv.append(editBtnDiv);
    opBtnDiv.append(downloadBtnDiv);
    opBtnDiv.append(deleteBtnDiv);
    opBtnDiv.append(opMoreBtnDiv);

    const opBtnTd = document.createElement('td');
    opBtnTd.setAttribute('data-id', doc.id);
    opBtnTd.append(opBtnDiv);
    const lastTd = document.createElement('td');

    // Create new span element, set data-feather attribute to file-text, class to mr-2
    const span = document.createElement('span');
    span.setAttribute('data-feather', 'file-text');
    span.className = 'mr-2';

    // Create new td element, set scope attribute to row, data-id attribute to doc.id, class to file-list-item
    const filenameTh = document.createElement('td');
    filenameTh.setAttribute('scope', 'row');
    filenameTh.setAttribute('data-id', doc.id);
    filenameTh.className = 'file-list-item';
    filenameTh.onclick = onenFileClick; // Add key event
    filenameTh.append(span);
    filenameTh.append(doc.name);

    // Create new tr element, set class attribute to file-row
    const tr = document.createElement('tr');
    tr.className = 'file-row';
    tr.append(filenameTh);
    tr.append(opBtnTd);
    tr.append(lastTd);

    // Add tr element to the element with id file-list
    const fileListEle = document.getElementById('file-list');
    fileListEle.insertBefore(tr, fileListEle.firstChild);

    // Re-render feather
    feather.replace();
}

/**
 * Click event for each element with class list-group-item, click to open in new window
 * @param event
 */
function onenFileClick(event) {
    const docId = event.target.attributes['data-id'].value;
    if (!docId) return;
    openDoc(docId, 'view');
}

/**
 * Open modal dialog
 * @param title
 * @param src
 */
function openFileOpModal(title, src) {
    const modalTitle = document.getElementById('fileOpModalLabel');
    const modalIframe = document.getElementById('fileOpIframe');
    modalTitle.innerText = title;
    modalIframe.src = src;
    // bootstarap modal show
    $('#fileOpModal').modal('show').modal('handleUpdate')
}

/**
 * Close modal dialog
 */
function closeFileOpModal() {
    $('#fileOpModal').modal('hide');
}

/**
 * Publish notification message
 */
function showToastInParent(msg) {
    window.parent.postMessage({
        type: 'showToast',
        msg
    }, '*');
}

/**
 * Unified error handling function
 */
function handleError(operation, xhr, status, error) {
    const errorMsg = xhr.responseText || `${status}: ${error}` || 'Unknown error';
    showToastInParent(`${operation} failed: ${errorMsg}`);
}

/**
 * Maintain a singleton ContextMenu
 */
let menu = null;
function getMenu(actions) {
    if (!menu) {
        menu = new ContextMenu(actions);
    }
    return menu;
}
const menuActions = [
    {
        name: 'Custom document metadata',
        id: 'meta',
        onClick: function () {
            const docId = this.docId;
            if (!docId) return;
            openFileOpModal('Modify metadata', `/home/meta/${docId}`)
        }
    }
];

/**
 * More menu
 */
function onFileOpClick(docId) {
    if (!docId) return;

    const menu = getMenu(menuActions);
    menu.docId = docId;
    // Menu displays to the right of current element, Y-axis position matches mouse position, need to add scrollbar height
    menu.menu.style.left = event.clientX + 'px';
    menu.menu.style.top = event.clientY + document.documentElement.scrollTop + 'px';
    // Show menu
    menu.show();
    // Hide when mouse leaves menu
    menu.menu.addEventListener('mouseleave', function () {
        menu.hide();
    });
    // jQuery find tr element with data-id as docId
    const fileRow = $(`tr[data-id="${docId}"]`)[0];
    // Activate CSS hover effect when mouse is on menu
    menu.menu.addEventListener('mouseenter', function () {
        // force fileRow state :hover
        if (fileRow.classList) {
            fileRow.classList.add('file-row-hover');
        }
        fileRow.classList.add('file-row-hover');
        // Cancel CSS hover effect for other tr elements
        const fileRows = document.getElementsByClassName('file-row');
        for (let i = 0; i < fileRows.length; i++) {
            if (fileRows[i] !== fileRow) {
                fileRows[i].classList.remove('file-row-hover');
            }
        }
    });
    // Cancel CSS hover effect when mouse leaves menu
    menu.menu.addEventListener('mouseleave', function () {
        fileRow.classList.remove('file-row-hover');
    });
}

/**
 * Define ContextMenu, menu always displays at right-click mouse position
 */
class ContextMenu {
    constructor(actions) {
        this.menu = document.createElement('div');
        this.menu.className = 'context-menu';
        this.menu.style.left = event.clientX + 'px';
        this.menu.style.top = event.clientY + 'px';
        this.menu.style.display = 'absolute';

        this.menuItems = [];
        for (let i = 0; i < actions.length; i++) {
            const menuItem = document.createElement('div');
            menuItem.className = 'context-menu-item';
            menuItem.innerText = actions[i].name;
            menuItem.addEventListener('click', actions[i].onClick.bind(this));
            this.menuItems.push(menuItem);
            this.menu.appendChild(menuItem);
            // Highlight when mouse enters
            menuItem.addEventListener('mouseenter', function () {
                menuItem.style.backgroundColor = '#ccc';
            });
            // Cancel highlight when mouse leaves
            menuItem.addEventListener('mouseleave', function () {
                menuItem.style.backgroundColor = '#fff';
            });
            menuItem.addEventListener('click', function () {
                if (this.menu) this.menu.hide();
            });
        }
        document.body.appendChild(this.menu);
    }

    show() { this.menu.style.display = 'block'; }

    hide() { this.menu.style.display = 'none'; }
}

/**
 * When clicking filename th, open a search box, after entering content, search for files whose names contain that content
 */
function searchFile() {
    const searchText = document.getElementById('search-input').value;
    const listItems = document.getElementsByClassName('file-list-item');
    for (let i = 0; i < listItems.length; i++) {
        const listItem = listItems[i];
        if (!searchText || listItem.innerText.indexOf(searchText) > -1) {
            listItem.parentElement.style.display = 'table-row';
        } else {
            listItem.parentElement.style.display = 'none';
        }
    }
}

/**
 * Listen for file drag and drop
 */
function onFileDrop() {
    const dropArea = document.getElementById("dropArea");

    window.addEventListener("dragenter", function (e) {
        dropArea.style.display = 'block';
    });

    // Prevent browser default behavior
    dropArea.addEventListener("dragover", function (e) {
        e.preventDefault();
        dropArea.classList.add("active");
    });

    // Restore default style
    dropArea.addEventListener("dragleave", function () {
        dropArea.classList.remove("active"); // Remove class name
        dropArea.style.display = 'none';
    });

    // Triggered when file is dropped
    dropArea.addEventListener("drop", function (e) {
        e.preventDefault();
        dropArea.classList.remove("active"); // Remove class name
        dropArea.style.display = 'none';

        // Get dragged files
        const files = e.dataTransfer.files;
        uploadFile(files[0]);
    });
}

/**
 * Triggered when file is selected
 * @param event
 */
function onFileInputChange(event) {
    console.log('file input change');
    uploadFile(event.target.files[0]);
}

$(function(){
    $('#fileUpload').change(onFileInputChange);

    // Enable file drag and drop
    onFileDrop();

    // Load open source library for drawing right-click menu
    document.addEventListener('contextmenu', function (e) {
        e.preventDefault();
    });

    feather.replace();

    // Add click event for each element with class list-group-item
    const listItems = document.getElementsByClassName('file-list-item');
    for (let i = 0; i < listItems.length; i++) {
        listItems[i].addEventListener('click', onenFileClick);
        // If filename contains enc, add title attribute as encrypted document password: luoshu
        if (listItems[i].innerText.indexOf('enc') > -1) {
            listItems[i].setAttribute('title', 'Encrypted document password: luoshu');
        }
    }

    // search-input enter event
    document.getElementById('search-input').addEventListener('keyup', function (event) {
        if (event.keyCode === 13) searchFile();
    });
})
