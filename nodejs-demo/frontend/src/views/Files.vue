<template>
  <div class="files-container">
    <div class="files-header">
      <h2>文件列表</h2>
      <div class="header-actions">
        <el-upload
          ref="uploadRef"
          :action="uploadAction"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :show-file-list="false"
          :multiple="true"
          :before-upload="beforeUpload"
        >
          <el-button type="primary" :icon="Upload">上传文件</el-button>
        </el-upload>
        
        <el-dropdown @command="handleNewFile" style="margin-left: 10px;">
          <el-button type="success" :icon="Plus">
            新建文档
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="docx">Word 文档</el-dropdown-item>
              <el-dropdown-item command="xlsx">Excel 表格</el-dropdown-item>
              <el-dropdown-item command="pptx">PowerPoint 演示</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button
          v-if="fileStore.selectedFiles.length > 0"
          type="danger"
          :icon="Delete"
          @click="handleBatchDelete"
          style="margin-left: 10px;"
        >
          批量删除 ({{ fileStore.selectedFiles.length }})
        </el-button>

        <el-button
          :icon="Refresh"
          @click="refreshList"
          style="margin-left: 10px;"
        >
          刷新
        </el-button>
      </div>
    </div>

    <el-table
      v-loading="fileStore.loading"
      :data="fileStore.fileList"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      
      <el-table-column prop="name" label="文件名" min-width="200">
        <template #default="{ row }">
          <div class="file-name">
            <el-icon :size="18" style="margin-right: 8px;">
              <Document />
            </el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="size" label="大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.size) }}
        </template>
      </el-table-column>

      <el-table-column prop="createdBy" label="创建者" width="150">
        <template #default="{ row }">
          {{ row.createdBy?.nickname || row.createdBy?.username || '-' }}
        </template>
      </el-table-column>

      <el-table-column prop="modifiedAt" label="修改时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.modifiedAt || row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <!--              预览-->
            <el-button
              type="primary"
              size="small"
              :icon="View"
              @click="handleView(row)"
            >
            </el-button>
            <!--              编辑-->
            <el-button
              type="warning"
              size="small"
              :icon="Edit"
              @click="handleEdit(row)"
            >
            </el-button>
            <!--              下载-->
            <el-button
              type="success"
              size="small"
              :icon="Download"
              @click="handleDownload(row)"
            >
            </el-button>
            <!--              删除-->
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            >
            </el-button>
            <!--                更多-->
            &nbsp;
            <el-dropdown @command="handleMoreCommand($event, row)" trigger="click">
              <el-button size="small" :icon="More">
              </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="viewInFrame">
                  <el-icon><View /></el-icon>
                  当前窗口预览
                </el-dropdown-item>
                <el-dropdown-item command="editInFrame">
                  <el-icon><Edit /></el-icon>
                  当前窗口编辑
                </el-dropdown-item>
                <el-dropdown-item command="editMeta" divided>
                  <el-icon><Setting /></el-icon>
                  编辑文档元数据
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty
      v-if="!fileStore.loading && fileStore.fileList.length === 0"
      description="暂无文件，请上传文件"
    />

    <!-- iframe 预览/编辑弹窗 -->
    <el-dialog
      v-model="iframeDialogVisible"
      :title="iframeTitle"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
      :before-close="closeIframeDialog"
      class="iframe-dialog"
    >
      <div v-loading="iframeLoading" class="iframe-container">
        <iframe
          v-if="iframeUrl"
          :src="iframeUrl"
          frameborder="0"
          class="doc-iframe"
        ></iframe>
      </div>
    </el-dialog>

    <!-- 编辑元数据对话框 -->
    <el-dialog
      v-model="metaDialogVisible"
      title="修改元数据"
      width="700px"
      :close-on-click-modal="false"
      class="meta-dialog"
    >
      <div v-if="currentEditDoc" v-loading="metaFormLoading" class="meta-content">
        <!-- 第一部分：文档相关配置 -->
        <h3 class="section-title">文档相关配置，全局生效</h3>
        <el-form :model="metaForm" label-width="140px" label-position="left">
          <el-form-item label="文档ID">
            <el-input :model-value="currentEditDoc.id" disabled />
          </el-form-item>
          
          <el-form-item label="修改时间">
            <el-input :model-value="formatDate(currentEditDoc.modifiedAt || currentEditDoc.updatedAt)" disabled />
          </el-form-item>
          
          <el-form-item label="name">
            <el-input v-model="metaForm.name" placeholder="文件名" />
          </el-form-item>

          <el-form-item label="createdBy.id">
            <el-input :model-value="currentEditDoc.createdBy?.id || ''" disabled />
          </el-form-item>
          
          <el-form-item label="createdBy.name">
            <el-input :model-value="currentEditDoc.createdBy?.username || ''" disabled />
          </el-form-item>
          
          <el-form-item label="createdBy.email">
            <el-input :model-value="currentEditDoc.createdBy?.email || ''" disabled />
          </el-form-item>

          <el-form-item label="owner.id">
            <el-input :model-value="currentEditDoc.owner?.id || ''" disabled />
          </el-form-item>
          
          <el-form-item label="owner.name">
            <el-input :model-value="currentEditDoc.owner?.username || ''" disabled />
          </el-form-item>
          
          <el-form-item label="owner.email">
            <el-input :model-value="currentEditDoc.owner?.email || ''" disabled />
          </el-form-item>
        </el-form>

        <div class="update-btn-wrapper">
          <el-button type="primary" @click="submitMetaForm">更新</el-button>
        </div>

        <!-- 第二部分：其他配置，和用户及文档ID绑定 -->
        <h3 class="section-title">其他配置，和用户及文档ID绑定</h3>
        <el-form :model="controlForm" label-width="180px" label-position="left">
          <el-form-item label="write">
            <el-input v-model="controlForm.canEdit" placeholder="true/false" />
          </el-form-item>
          
          <el-form-item label="read">
            <el-input model-value="true" disabled />
          </el-form-item>
          
          <el-form-item label="download">
            <el-input v-model="controlForm.canDownload" placeholder="true/false" />
          </el-form-item>
          
          <el-form-item label="打印">
            <el-input v-model="controlForm.canPrint" placeholder="true/false" />
          </el-form-item>
          
          <el-form-item label="role">
            <el-input v-model="controlForm.role" placeholder="角色" />
          </el-form-item>

          <el-form-item label="line1">
            <el-input v-model="controlForm.watermarkLine1" placeholder="水印文字第1行" />
          </el-form-item>
          
          <el-form-item label="line2">
            <el-input v-model="controlForm.watermarkLine2" placeholder="水印文字第2行" />
          </el-form-item>
          
          <el-form-item label="line3">
            <el-input v-model="controlForm.watermarkLine3" placeholder="水印文字第3行" />
          </el-form-item>
          
          <el-form-item label="line4">
            <el-input v-model="controlForm.watermarkLine4" placeholder="水印文字第4行" />
          </el-form-item>
          
          <el-form-item label="withDate">
            <el-input v-model="controlForm.watermarkWithDate" placeholder="true/false" />
          </el-form-item>
          
          <el-form-item label="fontcolor">
            <el-input v-model="controlForm.watermarkFontColor" placeholder="字体颜色" />
          </el-form-item>
          
          <el-form-item label="transparent">
            <el-input v-model="controlForm.watermarkTransparent" placeholder="透明度 0-1" />
          </el-form-item>
          
          <el-form-item label="rotation">
            <el-input v-model="controlForm.watermarkRotation" placeholder="旋转角度" />
          </el-form-item>
          
          <el-form-item label="fontsize">
            <el-input v-model="controlForm.watermarkFontSize" placeholder="字体大小" />
          </el-form-item>
          
          <el-form-item label="font">
            <el-input v-model="controlForm.watermarkFont" placeholder="字体" />
          </el-form-item>

          <el-form-item label="previewWithTrackChange">
            <el-input v-model="controlForm.previewWithTrackChange" placeholder="true/false" />
          </el-form-item>
          
          <el-form-item label="trackChangeForceOn">
            <el-input v-model="controlForm.trackChangeForceOn" placeholder="true/false" />
          </el-form-item>
        </el-form>

        <div class="update-btn-wrapper">
          <el-button type="primary" @click="submitControlForm">更新</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, reactive } from 'vue';
import { useFileStore } from '@/store/file';
import { batchDeleteFiles, deleteFile, type DocMeta, downloadFile, newFile, updateDocMeta, getDocControl, updateDocControl, getZOfficeUrl } from '@/api/file';
import { config } from '@/config';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown, Delete, Document, Download, Edit, More, Plus, Refresh, Setting, Upload, View } from '@element-plus/icons-vue';

// 元数据编辑对话框
const metaDialogVisible = ref(false);
const metaFormLoading = ref(false);
const currentEditDoc = ref<DocMeta | null>(null);

// iframe 预览/编辑弹窗
const iframeDialogVisible = ref(false);
const iframeUrl = ref('');
const iframeTitle = ref('');
const iframeLoading = ref(false);

// 文档元数据表单
const metaForm = reactive({
  name: '',
});

// 文档控制配置表单
const controlForm = reactive({
  canEdit: 'true',
  canDownload: 'true',
  canPrint: 'true',
  role: '',
  watermarkLine1: '',
  watermarkLine2: '',
  watermarkLine3: '',
  watermarkLine4: '',
  watermarkWithDate: 'false',
  watermarkFontColor: '',
  watermarkTransparent: '0',
  watermarkRotation: '0',
  watermarkFontSize: '',
  watermarkFont: 'SimHei',
  previewWithTrackChange: 'false',
  trackChangeForceOn: 'false',
});

const fileStore = useFileStore();

const uploadAction = computed(() => {
  return `${config.apiBaseURL}/api/file/upload`;
});

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return {
    Authorization: `Bearer ${token}`,
  };
});

onMounted(() => {
  refreshList();
});

const refreshList = async () => {
  try {
    await fileStore.fetchFileList();
  } catch (error) {
    ElMessage.error('获取文件列表失败');
  }
};

const beforeUpload = (file: File) => {
  const maxSize = 500 * 1024 * 1024; // 500MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 500MB');
    return false;
  }
  return true;
};

const handleUploadSuccess = () => {
  ElMessage.success('上传成功');
  refreshList();
};

const handleUploadError = () => {
  ElMessage.error('上传失败');
};

const handleNewFile = async (docType: string) => {
  try {
    await newFile(docType);
    ElMessage.success('新建文档成功');
    await refreshList();
  } catch (error) {
    ElMessage.error('新建文档失败');
  }
};

const handleSelectionChange = (selection: DocMeta[]) => {
  fileStore.selectedFiles = selection.map(item => item.id);
};

const handleView = async (row: DocMeta) => {
  try {
    const url = await getZOfficeUrl(row.id, 'view', false);
    if (url) {
      window.open(url, '_blank');
    } else {
      ElMessage.error('获取预览地址失败');
    }
  } catch (error) {
    ElMessage.error('获取预览地址失败');
  }
};

const handleEdit = async (row: DocMeta) => {
  try {
    const url = await getZOfficeUrl(row.id, 'edit', false);
    if (url) {
      window.open(url, '_blank');
    } else {
      ElMessage.error('获取编辑地址失败');
    }
  } catch (error) {
    ElMessage.error('获取编辑地址失败');
  }
};

const handleMoreCommand = (command: string, row: DocMeta) => {
  switch (command) {
    case 'viewInFrame':
      handleViewInFrame(row);
      break;
    case 'editInFrame':
      handleEditInFrame(row);
      break;
    case 'editMeta':
      handleEditMeta(row);
      break;
  }
};

const handleViewInFrame = async (row: DocMeta) => {
  iframeLoading.value = true;
  iframeTitle.value = `预览 - ${row.name}`;
  iframeDialogVisible.value = true;
  
  try {
    const url = await getZOfficeUrl(row.id, 'view', true);
    if (url) {
      iframeUrl.value = url;
    } else {
      ElMessage.error('获取预览地址失败');
      iframeDialogVisible.value = false;
    }
  } catch (error) {
    ElMessage.error('获取预览地址失败');
    iframeDialogVisible.value = false;
  } finally {
    iframeLoading.value = false;
  }
};

const handleEditInFrame = async (row: DocMeta) => {
  iframeLoading.value = true;
  iframeTitle.value = `编辑 - ${row.name}`;
  iframeDialogVisible.value = true;
  
  try {
    const url = await getZOfficeUrl(row.id, 'edit', true);
    if (url) {
      iframeUrl.value = url;
    } else {
      ElMessage.error('获取编辑地址失败');
      iframeDialogVisible.value = false;
    }
  } catch (error) {
    ElMessage.error('获取编辑地址失败');
    iframeDialogVisible.value = false;
  } finally {
    iframeLoading.value = false;
  }
};

const closeIframeDialog = () => {
  iframeDialogVisible.value = false;
  iframeUrl.value = '';
  iframeTitle.value = '';
  // 关闭后刷新列表，以获取可能的更新
  refreshList();
};

const handleEditMeta = async (row: DocMeta) => {
  currentEditDoc.value = row;
  metaForm.name = row.name;
  metaDialogVisible.value = true;
  
  // 加载文档控制配置
  metaFormLoading.value = true;
  try {
    const control = await getDocControl(row.id);
    if (control) {
      controlForm.canEdit = String(control.canEdit ?? true);
      controlForm.canDownload = String(control.canDownload ?? true);
      controlForm.canPrint = String(control.canPrint ?? true);
      
      // 解析 extensions JSON
      if (control.extensions) {
        try {
          const ext = JSON.parse(control.extensions);
          controlForm.role = ext.role || '';
          controlForm.watermarkLine1 = ext.watermark?.line1 || '';
          controlForm.watermarkLine2 = ext.watermark?.line2 || '';
          controlForm.watermarkLine3 = ext.watermark?.line3 || '';
          controlForm.watermarkLine4 = ext.watermark?.line4 || '';
          controlForm.watermarkWithDate = String(ext.watermark?.withDate ?? false);
          controlForm.watermarkFontColor = ext.watermark?.fontcolor || '';
          controlForm.watermarkTransparent = String(ext.watermark?.transparent ?? 0);
          controlForm.watermarkRotation = String(ext.watermark?.rotation ?? 0);
          controlForm.watermarkFontSize = ext.watermark?.fontsize || '';
          controlForm.watermarkFont = ext.watermark?.font || 'SimHei';
          controlForm.previewWithTrackChange = String(ext.previewWithTrackChange ?? false);
          controlForm.trackChangeForceOn = String(ext.trackChangeForceOn ?? false);
        } catch {
          // 解析失败，使用默认值
        }
      }
    }
  } catch (error) {
    console.error('获取文档控制配置失败:', error);
  } finally {
    metaFormLoading.value = false;
  }
};

const submitMetaForm = async () => {
  if (!currentEditDoc.value) return;
  
  metaFormLoading.value = true;
  try {
    await updateDocMeta(currentEditDoc.value.id, {
      name: metaForm.name,
    });
    ElMessage.success('文档元数据更新成功');
    await refreshList();
  } catch (error) {
    ElMessage.error('更新失败');
  } finally {
    metaFormLoading.value = false;
  }
};

const submitControlForm = async () => {
  if (!currentEditDoc.value) return;
  
  metaFormLoading.value = true;
  try {
    // 构建 extensions JSON
    const extensions = {
      role: controlForm.role,
      watermark: {
        line1: controlForm.watermarkLine1,
        line2: controlForm.watermarkLine2,
        line3: controlForm.watermarkLine3,
        line4: controlForm.watermarkLine4,
        withDate: controlForm.watermarkWithDate === 'true',
        fontcolor: controlForm.watermarkFontColor,
        transparent: parseFloat(controlForm.watermarkTransparent) || 0,
        rotation: parseFloat(controlForm.watermarkRotation) || 0,
        fontsize: controlForm.watermarkFontSize,
        font: controlForm.watermarkFont,
      },
      previewWithTrackChange: controlForm.previewWithTrackChange === 'true',
      trackChangeForceOn: controlForm.trackChangeForceOn === 'true',
    };

    await updateDocControl(currentEditDoc.value.id, {
      canEdit: controlForm.canEdit === 'true',
      canDownload: controlForm.canDownload === 'true',
      canPrint: controlForm.canPrint === 'true',
      extensions: JSON.stringify(extensions),
    });
    ElMessage.success('文档控制配置更新成功');
  } catch (error) {
    ElMessage.error('更新失败');
  } finally {
    metaFormLoading.value = false;
  }
};

const handleDownload = async (row: DocMeta) => {
  try {
    const response = await downloadFile(row.id);
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', row.name);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('下载成功');
  } catch (error) {
    ElMessage.error('下载失败');
  }
};

const handleDelete = async (row: DocMeta) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${row.name}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await deleteFile(row.id);
    ElMessage.success('删除成功');
    await refreshList();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const handleBatchDelete = async () => {
  if (fileStore.selectedFiles.length === 0) {
    ElMessage.warning('请选择要删除的文件');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${fileStore.selectedFiles.length} 个文件吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await batchDeleteFiles(fileStore.selectedFiles);
    ElMessage.success('批量删除成功');
    fileStore.clearSelection();
    await refreshList();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败');
    }
  }
};

const formatFileSize = (size: number): string => {
  if (size === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(size) / Math.log(k));
  return (size / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i];
};

const formatDate = (date: string): string => {
  if (!date) return '-';
  const d = new Date(date);
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};
</script>

<style scoped>
.files-container {
  background: white;
  border-radius: 4px;
  padding: 20px;
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.files-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}

.file-name {
  display: flex;
  align-items: center;
}

/* 操作按钮容器 - 防止换行 */
.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 4px;
}

/* 元数据对话框样式 */
.meta-dialog .meta-content {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 10px;
}

.meta-dialog .section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 20px 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.meta-dialog .section-title:first-child {
  margin-top: 0;
}

.meta-dialog .update-btn-wrapper {
  display: flex;
  justify-content: center;
  margin: 20px 0 30px 0;
}

.meta-dialog .el-form-item {
  margin-bottom: 12px;
}

.meta-dialog .el-input {
  width: 100%;
}

/* iframe 弹窗样式 */
.iframe-dialog .iframe-container {
  width: 100%;
  height: 80vh;
  min-height: 500px;
}

.iframe-dialog .doc-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>

