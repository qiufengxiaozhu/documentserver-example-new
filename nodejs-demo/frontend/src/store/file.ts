import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getFileList, type DocMeta } from '@/api/file';

export const useFileStore = defineStore('file', () => {
  const fileList = ref<DocMeta[]>([]);
  const loading = ref<boolean>(false);
  const selectedFiles = ref<string[]>([]);

  /**
   * 获取文件列表
   */
  async function fetchFileList() {
    loading.value = true;
    try {
      const list = await getFileList();
      fileList.value = list;
      return list;
    } catch (error) {
      console.error('获取文件列表失败:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  /**
   * 刷新文件列表
   */
  async function refreshFileList() {
    return fetchFileList();
  }

  /**
   * 选择文件
   */
  function selectFile(fileId: string) {
    if (!selectedFiles.value.includes(fileId)) {
      selectedFiles.value.push(fileId);
    }
  }

  /**
   * 取消选择文件
   */
  function unselectFile(fileId: string) {
    const index = selectedFiles.value.indexOf(fileId);
    if (index > -1) {
      selectedFiles.value.splice(index, 1);
    }
  }

  /**
   * 切换文件选择状态
   */
  function toggleFileSelection(fileId: string) {
    if (selectedFiles.value.includes(fileId)) {
      unselectFile(fileId);
    } else {
      selectFile(fileId);
    }
  }

  /**
   * 全选
   */
  function selectAll() {
    selectedFiles.value = fileList.value.map(file => file.id);
  }

  /**
   * 取消全选
   */
  function unselectAll() {
    selectedFiles.value = [];
  }

  /**
   * 清空选择
   */
  function clearSelection() {
    selectedFiles.value = [];
  }

  return {
    fileList,
    loading,
    selectedFiles,
    fetchFileList,
    refreshFileList,
    selectFile,
    unselectFile,
    toggleFileSelection,
    selectAll,
    unselectAll,
    clearSelection,
  };
});

