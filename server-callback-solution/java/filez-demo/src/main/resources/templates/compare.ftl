<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Document Comparison</title>
    <style>
        .drop-area {
            border: 2px dashed #ccc;
            border-radius: 10px;
            padding: 20px;
            text-align: center;
            margin: 10px;
            transition: border-color 0.3s;
        }
        
        .drop-area.dragover {
            border-color: #007bff;
            background-color: #f8f9fa;
        }
        
        .drop-area-content {
            padding: 20px;
        }
        
        .compare-container {
            display: flex;
            justify-content: space-between;
        }
        
        .compare-panel {
            width: 48%;
        }
        
        iframe {
            top: 0;
            left: 0;
            width: 100%;
            height: 60vh;
            border: 0;
        }
        
        .file-info {
            margin-top: 10px;
            font-weight: bold;
        }
        
        .error-message {
            color: red;
            font-weight: bold;
            margin-top: 5px;
        }
    </style>
    <link href="/static/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <main role="main" class="col-md-12 col-lg-12">
            <button type="button" class="btn btn-primary mb-3" onclick="compareDocuments()" id="compareBtn" disabled>Compare Documents</button>
            
            <div class="compare-container">
                <!-- Left document area -->
                <div class="compare-panel">
                    <h5>Document A</h5>
                    <div class="drop-area" id="dropAreaA">
                        <div class="drop-area-content">
                            <p>Drag files here or click to select files</p>
                            <input type="file" id="fileInputA" style="display: none;" accept=".doc,.docx">
                            <button type="button" class="btn btn-outline-primary" id="selectBtnA">Select File</button>
                            <div class="file-info" id="fileInfoA">No file selected</div>
                            <div class="error-message" id="errorA"></div>
                        </div>
                    </div>
                </div>
                
                <!-- Right document area -->
                <div class="compare-panel">
                    <h5>Document B</h5>
                    <div class="drop-area" id="dropAreaB">
                        <div class="drop-area-content">
                            <p>Drag files here or click to select files</p>
                            <input type="file" id="fileInputB" style="display: none;" accept=".doc,.docx">
                            <button type="button" class="btn btn-outline-primary" id="selectBtnB">Select File</button>
                            <div class="file-info" id="fileInfoB">No file selected</div>
                            <div class="error-message" id="errorB"></div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="/static/jquery-3.7.0.min.js"></script>
<script>
    let docIdA = null;
    let docIdB = null;
    
    // Initialize drag areas
    function initDropAreas() {
        const dropAreaA = document.getElementById('dropAreaA');
        const dropAreaB = document.getElementById('dropAreaB');
        const fileInputA = document.getElementById('fileInputA');
        const fileInputB = document.getElementById('fileInputB');
        const selectBtnA = document.getElementById('selectBtnA');
        const selectBtnB = document.getElementById('selectBtnB');
        
        // Left drag area events
        setupDropArea(dropAreaA, fileInputA, selectBtnA, 'A');
        
        // Right drag area events
        setupDropArea(dropAreaB, fileInputB, selectBtnB, 'B');
    }
    
    // Setup drag area event listeners
    function setupDropArea(dropArea, fileInput, selectBtn, side) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function(eventName) {
            dropArea.addEventListener(eventName, preventDefaults, false);
        });
        
        ['dragenter', 'dragover'].forEach(function(eventName) {
            dropArea.addEventListener(eventName, highlight, false);
        });
        
        ['dragleave', 'drop'].forEach(function(eventName) {
            dropArea.addEventListener(eventName, unhighlight, false);
        });
        
        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }
        
        function highlight(e) {
            dropArea.classList.add('dragover');
        }
        
        function unhighlight(e) {
            dropArea.classList.remove('dragover');
        }
        
        // Handle drag drop events
        dropArea.addEventListener('drop', function(e) {
            const dt = e.dataTransfer;
            const files = dt.files;
            handleFiles(files, side);
        }, false);
        
        // Handle file selection events
        fileInput.addEventListener('change', function(e) {
            if (e.target.files.length > 0) {
                handleFiles(e.target.files, side);
                // Clear input value to ensure change event triggers for same file selection
                e.target.value = '';
            }
        });
        
        // Add click event for file selection button
        selectBtn.addEventListener('click', function() {
            fileInput.click();
        });
    }
    
    // Check if file type is supported
    function isFileTypeSupported(fileName) {
        const supportedExtensions = ['.doc', '.docx'];
        const lowerCaseFileName = fileName.toLowerCase();
        
        for (let i = 0; i < supportedExtensions.length; i++) {
            if (lowerCaseFileName.endsWith(supportedExtensions[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    // Clear error message
    function clearError(side) {
        if (side === 'A') {
            document.getElementById('errorA').textContent = '';
        } else {
            document.getElementById('errorB').textContent = '';
        }
    }
    
    // Show error message
    function showError(message, side) {
        if (side === 'A') {
            document.getElementById('errorA').textContent = message;
        } else {
            document.getElementById('errorB').textContent = message;
        }
    }
    
    // Handle files
    function handleFiles(files, side) {
        if (files.length === 0) return;
        
        const file = files[0];
        
        // Check file type
        if (!isFileTypeSupported(file.name)) {
            showError('Only doc and docx format documents are supported', side);
            return;
        }
        
        // Clear previous error messages
        clearError(side);
        
        uploadFile(file, side);
    }
    
    // Upload file
    function uploadFile(file, side) {
        const formData = new FormData();
        formData.append('file', file, file.name);
        formData.append('drive', 'local');
        
        fetch('/v2/context/file/upload', {
            method: 'POST',
            body: formData
        })
        .then(function(response) {
            if (!response.ok) {
                // First read response text to get specific error message from server
                return response.text().then(function(errorText) {
                    throw new Error('Upload failed: ' + (errorText || response.statusText));
                });
            }
            return response.json();
        })
        .then(function(data) {
            if (side === 'A') {
                docIdA = data.id;
                document.getElementById('fileInfoA').textContent = 'Selected: ' + data.name;
            } else {
                docIdB = data.id;
                document.getElementById('fileInfoB').textContent = 'Selected: ' + data.name;
            }
            
            updateCompareButton();
        })
        .catch(function(error) {
            alert('File upload failed: ' + error.message);
        });
    }
    
    // Update compare button status
    function updateCompareButton() {
        const compareBtn = document.getElementById('compareBtn');
        if (docIdA && docIdB) {
            compareBtn.disabled = false;
        } else {
            compareBtn.disabled = true;
        }
    }
    
    // Document comparison
    function compareDocuments() {
        if (!docIdA || !docIdB) {
            alert('Please select two documents first');
            return;
        }
        
        // Call backend compareDoc API
        fetch('/v2/context/compareDoc?docAid=' + docIdA + '&docBid=' + docIdB)
        .then(function(response) {
            if (!response.ok) {
                // First read response text to get specific error message from server
                return response.text().then(function(errorText) {
                    throw new Error('Failed to get comparison URL: ' + (errorText || response.statusText));
                });
            }
            return response.text();
        })
        .then(function(url) {
            // Open comparison interface in new tab
            window.open(url, '_blank');
        })
        .catch(function(error) {
            alert('Document comparison failed: ' + error.message);
        });
    }
    
    // Initialize
    document.addEventListener('DOMContentLoaded', function() {
        initDropAreas();
    });
</script>
</body>
</html>