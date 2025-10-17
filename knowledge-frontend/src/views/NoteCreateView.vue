<template>
  <div class="note-creator-page">
    <header class="page-header">
      <h1>{{ isEditMode ? '编辑笔记' : '新建笔记' }}</h1>
      <div class="action-buttons">
        <button class="btn btn-secondary" @click="handleSaveDraft" title="保存为草稿，稍后继续编辑">
          保存草稿
        </button>
        <button class="btn btn-primary" @click="handlePublish" :title="isEditMode ? '更新笔记内容' : '发布笔记'">
          {{ isEditMode ? '更新笔记' : '发布笔记' }}
        </button>
        <button class="btn btn-outline" @click="handleCancel" title="返回上一页">
          取消
        </button>
      </div>
    </header>

    <main class="editor-area">
      <!-- 基本信息区域 -->
      <div class="basic-info-section">
        <div class="info-row">
          <!-- 分类选择 -->
          <div class="form-group">
            <label class="form-label">分类 <span class="required">*</span></label>
            <select v-model="categoryId" class="form-select" title="选择笔记所属分类">
              <option value="">请选择分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>
          <!-- 标签选择区域 -->
          <div class="form-group">
            <label class="form-label">标签</label>
            <div class="tag-selection">
              <!-- 已选标签 -->
              <div class="selected-tags" v-if="selectedTagIds.length > 0">
                <el-tag
                    v-for="tag in selectedTags"
                    :key="tag.id"
                    closable
                    @close="removeTag(tag.id)"
                >
                  {{ tag.name }}
                </el-tag>
              </div>

              <!-- 标签选择器 -->
              <el-select
                  v-model="currentTagId"
                  placeholder="选择或搜索标签"
                  filterable
                  clearable
                  @change="addTag"
              >
                <el-option
                    v-for="tag in availableTags"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                />
              </el-select>


              <!-- 新建标签按钮 -->
              <el-button
                  size="small"
                  type="text"
                  @click="showCreateTagDialog = true"
                  class="create-tag-btn"
              >
                <el-icon><Plus /></el-icon> 新建标签
              </el-button>
            </div>
          </div>

          <!-- 新建标签弹窗 -->
          <el-dialog
              v-model="showCreateTagDialog"
              title="新建标签"
              :width="350"
              :close-on-click-modal="false"
          >
            <el-form :model="newTagForm" :rules="newTagRules" ref="newTagFormRef">
              <el-form-item label="标签名称" prop="name">
                <el-input v-model="newTagForm.name" placeholder="请输入标签名称"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showCreateTagDialog = false">取消</el-button>
              <el-button type="primary" @click="createNewTag">确认</el-button>
            </template>
          </el-dialog>

          <!-- 封面图片 -->
          <div class="form-group">
            <label class="form-label">封面图片</label>
            <div class="cover-upload-area">
              <div
                  class="cover-preview"
                  :class="{ 'has-image': coverUrl }"
                  @click="triggerCoverInput"
                  title="点击上传封面图片"
              >
                <img v-if="coverUrl" :src="coverUrl" alt="封面预览" class="cover-image" />
                <div v-else class="cover-placeholder">
                  <div class="cover-icon">🖼️</div>
                  <span>点击上传封面</span>
                </div>
                <input
                    type="file"
                    ref="coverInputRef"
                    @change="handleCoverUpload"
                    accept="image/*"
                    class="cover-input"
                />
              </div>
              <div class="cover-actions" v-if="coverUrl">
                <button @click="triggerCoverInput" class="cover-action-btn" title="重新上传">🔄</button>
                <button @click="removeCover" class="cover-action-btn remove" title="删除封面">🗑️</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 笔记标题 -->
      <div class="title-input-group">
        <input
            v-model="noteTitle"
            type="text"
            class="title-input"
            placeholder="输入笔记标题..."
            maxlength="100"
            title="请输入笔记标题，最多100个字符"
        />
        <span class="char-count">{{ noteTitle.length }}/100</span>
      </div>

      <!-- 富文本编辑器 -->
      <div class="editor-section">
        <label class="section-label">笔记内容</label>
        <div class="quill-editor-container">
          <QuillEditor
              v-model:content="noteContent"
              contentType="html"
              :options="editorOptions"
              @text-change="onEditorChange"
          />
        </div>
      </div>

      <!-- 文件上传区域 -->
      <div class="upload-section">
        <div class="section-header">
          <h3>附件</h3>
          <span class="file-count">已选择 {{ uploadedFiles.length }} 个文件</span>
        </div>

        <p class="section-tip">支持上传图片、PDF、Word、Excel、PPT、TXT 等文档，单个文件不超过 50MB</p>

        <div class="upload-area"
             @click="triggerFileInput"
             @drop="handleDrop"
             @dragover="handleDragOver"
             title="点击或拖拽文件到这里上传">
          <input
              type="file"
              @change="handleFileSelect"
              multiple
              class="file-input"
              ref="fileInputRef"
              accept=".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar"
          />
          <div class="upload-placeholder">
            <div class="upload-icon">📁</div>
            <p>点击选择文件或拖拽文件到这里</p>
            <span class="upload-hint">支持多个文件同时上传</span>
          </div>
        </div>

        <!-- 文件列表 -->
        <div class="file-list" v-if="uploadedFiles.length > 0">
          <div v-for="(file, index) in uploadedFiles" :key="file.id" class="file-item">
            <div class="file-icon" :title="getFileTypeText(file.type)">
              {{ getFileIcon(file.type) }}
            </div>
            <div class="file-info">
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}
                <span v-if="file.uploading">上传中...</span>
                <span v-if="file.uploadError">上传失败</span>
              </span>
            </div>
            <div class="file-actions">
              <button @click="previewFile(file)" class="action-btn" title="预览文件" :disabled="file.uploading">👁️</button>
              <button @click="removeFile(index)" class="action-btn remove" title="删除文件" :disabled="file.uploading">🗑️</button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import {ElTag, ElSelect, ElOption, ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElMessage} from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {ref, computed, onMounted, reactive} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import axios from 'axios'
import request from "@/utils/request";
import { BASE_API } from '@/utils/request';
// 配置axios基础路径
const api = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080',
  // 添加默认请求头
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器添加认证信息
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // 保留这一行，避免拦截器覆盖multipart/form-data
  return config
}, error => {
  return Promise.reject(error)
})

const router = useRouter()
const route = useRoute()

// 响应式数据
const tags = ref([])
const filteredTags = ref([])
const selectedTagIds = ref([])
const currentTagId = ref('')
const showCreateTagDialog = ref(false)
const newTagForm = reactive({
  name: ''
})
const newTagFormRef = ref(null)
const noteTitle = ref('')
const noteContent = ref('')
const categoryId = ref('')
const coverUrl = ref('')
const uploadedFiles = ref([])
const fileInputRef = ref(null)
const coverInputRef = ref(null)
const categories = ref([])
const isLoading = ref(false)
const currentUserId = ref(null)
const noteId = ref(null);

// 计算属性
const isEditMode = computed(() => {
  return !!route.params.id;
})

const selectedTags = computed(() => {
  return filteredTags.value.filter(tag => selectedTagIds.value.includes(tag.id))
})

const availableTags = computed(() => {
  return filteredTags.value.filter(tag => !selectedTagIds.value.includes(tag.id))
})

// 编辑器配置
const editorOptions = {
  modules: {
    toolbar: {
      container: [
        [{'header': [1, 2, 3, 4, 5, 6, false]}],
        [{'size': ['small', false, 'large', 'huge']}],
        ['bold', 'italic', 'underline', 'strike'],
        [{'list': 'ordered'}, {'list': 'bullet'}],
        [{'script': 'sub'}, {'script': 'super'}],
        [{'indent': '-1'}, {'indent': '+1'}],
        [{'direction': 'rtl'}],
        [{'color': []}, {'background': []}],
        [{'font': []}],
        [{'align': []}],
        ['blockquote', 'code-block'],
        ['link', 'image', 'video'],
        ['clean']
      ]
    }
  },
  placeholder: '开始记录你的想法...',
  theme: 'snow'
}

// 文件图标映射
const fileIcons = {
  'image/': '🖼️',
  'application/pdf': '📕',
  'application/msword': '📄',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '📄',
  'application/vnd.ms-excel': '📊',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '📊',
  'application/vnd.ms-powerpoint': '📑',
  'application/zip': '📦',
  'text/': '📝'
}

// 文件类型描述
const fileTypeDescriptions = {
  'image/': '图片文件',
  'application/pdf': 'PDF文档',
  'application/msword': 'Word文档',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word文档',
  'application/vnd.ms-excel': 'Excel表格',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel表格',
  'application/vnd.ms-powerpoint': 'PowerPoint演示文稿',
  'application/zip': '压缩文件',
  'text/': '文本文件'
}

// 方法
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const triggerCoverInput = () => {
  coverInputRef.value?.click()
}

// 封面上传方法
const handleCoverUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    // 验证文件类型和大小
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    const maxSize = 5 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('封面图片不能超过 5MB')
      return
    }

    try {
      // 获取用户信息和token
      const userInfoStr = localStorage.getItem('user')
      const token = localStorage.getItem('token')

      if (!token || !userInfoStr) {
        ElMessage.error('未登录，请先登录')
        router.push('/login')
        return
      }

      const userInfo = JSON.parse(userInfoStr)

      // 创建FormData对象
      const formData = new FormData()
      formData.append('file', file)
      formData.append('userId', userInfo.id)

      // 使用api实例上传
      const response = await api.post('/upload/image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${token}`
        },
        params: {
          userId: userInfo.id
        },
        onUploadProgress: (progressEvent) => {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          console.log(`上传进度: ${percent}%`)
        }
      })

      if (response.data.code === 200) {
        // 确保URL正确指向后端8080端口
        let avatarUrl = response.data.data.url;
        if (!avatarUrl.startsWith('http')) {
          avatarUrl = BASE_API + avatarUrl;
        }
        coverUrl.value = avatarUrl;
        ElMessage.success('封面图片上传成功');
      } else {
        ElMessage.error('封面上传失败: ' + (response.data.msg || '未知错误'))
      }
    } catch (error) {
      console.error('封面上传失败:', error)
      handleUploadError(error)
    }
  }
  event.target.value = ''
}

const removeCover = () => {
  coverUrl.value = ''
}

// 处理文件选择
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  // 验证文件大小 (50MB)
  const maxSize = 50 * 1024 * 1024
  const validFiles = files.filter(file => {
    if (file.size > maxSize) {
      ElMessage.error(`文件 ${file.name} 超过 50MB 限制`)
      return false
    }
    return true
  })

  // 添加到文件列表并上传
  validFiles.forEach(file => {
    const fileItem = {
      id: Date.now() + Math.random(),
      name: file.name,
      size: file.size,
      type: file.type,
      file: file,
      uploading: true,
      uploadError: false,
      url: null
    }
    uploadedFiles.value.push(fileItem)
    uploadFile(fileItem) // 上传文件
  })

  // 清空 input 以便选择相同文件
  event.target.value = ''
}

// 修复文件上传方法 - 确保状态正确更新
const uploadFile = async (fileItem) => {
  try {
    // 获取用户信息和token
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
    const token = localStorage.getItem('token');

    if (!token || !userInfo.id) {
      ElMessage.error('未获取到用户信息，请重新登录')
      router.push('/login')
      // 确保更新状态
      uploadedFiles.value = uploadedFiles.value.map(f => {
        if (f.id === fileItem.id) {
          return {...f, uploading: false, uploadError: true}
        }
        return f
      })
      return
    }

    const formData = new FormData()
    formData.append('file', fileItem.file)
    formData.append('userId', userInfo.id)

    // 使用api实例上传，确保拦截器生效
    const response = await api.post('/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
      params: {
        userId: userInfo.id
      },
      onUploadProgress: (progressEvent) => {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        console.log(`文件 ${fileItem.name} 上传进度: ${progress}%`)
      }
    })

    // 更新文件状态
    if (response.data.code === 200) {
      // 找到当前文件并更新状态
      uploadedFiles.value = uploadedFiles.value.map(f => {
        if (f.id === fileItem.id) {
          let url = response.data.data.url;
          if (!url.startsWith('http')) {
            url = BASE_API + url;
          }
          return {...f, uploading: false, url: url}
        }
        return f
      })
      ElMessage.success(`文件 ${fileItem.name} 上传成功`)
    } else {
      uploadedFiles.value = uploadedFiles.value.map(f => {
        if (f.id === fileItem.id) {
          return {...f, uploading: false, uploadError: true}
        }
        return f
      })
      ElMessage.error(`文件 ${fileItem.name} 上传失败: ${response.data.msg || '未知错误'}`)
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    // 确保在错误情况下更新状态
    uploadedFiles.value = uploadedFiles.value.map(f => {
      if (f.id === fileItem.id) {
        return {...f, uploading: false, uploadError: true}
      }
      return f
    })

    if (error.response && error.response.status === 403) {
      ElMessage.error('上传权限不足，请重新登录')
      router.push('/login')
    } else if (error.response) {
      ElMessage.error(`文件上传失败: ${error.response.data?.msg || '服务器错误'}`)
    } else {
      ElMessage.error(`文件 ${fileItem.name} 上传失败，请检查网络连接`)
    }
  }
}

// 统一错误处理函数
const handleUploadError = (error) => {
  if (error.response) {
    console.error('响应状态:', error.response.status)
    console.error('响应数据:', error.response.data)

    if (error.response.status === 403) {
      ElMessage.error('上传权限不足，请联系管理员')
    } else if (error.response.status === 401) {
      ElMessage.error('身份验证失败，请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else if (error.response.status === 404) {
      ElMessage.error('上传接口不存在，请检查系统配置')
    } else {
      ElMessage.error(`上传失败: ${error.response.data?.msg || `服务器错误 ${error.response.status}`}`)
    }
  } else if (error.request) {
    ElMessage.error('未收到服务器响应，请检查网络连接')
  } else {
    ElMessage.error(`上传失败: ${error.message}`)
  }
}

const handleDrop = (event) => {
  event.preventDefault()
  const files = Array.from(event.dataTransfer.files)
  if (files.length > 0) {
    const fakeEvent = {
      target: {
        files: event.dataTransfer.files
      }
    }
    handleFileSelect(fakeEvent)
  }
}

const handleDragOver = (event) => {
  event.preventDefault()
}

const removeFile = (index) => {
  const file = uploadedFiles.value[index]
  // 如果是已上传的文件，调用删除接口
  if (file.url && !file.uploading) {
    api.delete(`/upload/delete?url=${encodeURIComponent(file.url)}`)
        .catch(err => console.error('删除文件失败:', err))
  }
  uploadedFiles.value.splice(index, 1)
}

const getFileIcon = (fileType) => {
  for (const [key, icon] of Object.entries(fileIcons)) {
    if (fileType.startsWith(key)) {
      return icon
    }
  }
  return '📄'
}

const getFileTypeText = (fileType) => {
  for (const [key, description] of Object.entries(fileTypeDescriptions)) {
    if (fileType.startsWith(key)) {
      return description
    }
  }
  return '未知文件类型'
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const previewFile = (file) => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

const onEditorChange = () => {
  console.log('编辑器内容变化')
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!noteTitle.value.trim()) {
    ElMessage.error('请输入笔记标题')
    return
  }

  if (!categoryId.value) {
    ElMessage.error('请选择分类')
    return
  }

  isLoading.value = true
  try {
    const noteData = {
      id: isEditMode.value ? route.params.id : null,
      title: noteTitle.value,
      content: noteContent.value,
      categoryId: categoryId.value,
      coverUrl: coverUrl.value,
      userId: currentUserId.value,
      isPublic: 0, // 草稿默认私有
      isProcessed: 0 // 标记为未处理（草稿）
    }
    let response;
    let savedNoteId; // 新增变量存储保存后的笔记ID

    if (isEditMode.value) {
      response = await api.put('/note/update', noteData, {
        params: {
          userId: currentUserId.value
        }
      });
      savedNoteId = route.params.id; // 编辑模式使用现有ID
    } else {
      response = await api.post('/note/add', noteData);
      savedNoteId = response.data.data; // 新建模式使用返回的ID
    }

    if (response.data.code === 200) {
      ElMessage.success('草稿保存成功！')

      // 关联附件 - 使用正确的noteId
      const attachments = uploadedFiles.value
          .filter(f => f.url && !f.uploadError)
          .map(f => ({
            fileUrl: f.url,
            fileName: f.name,
            fileSize: f.size,
            fileType: f.type
          }))

      if (attachments.length > 0) {
        await api.post('/note/attachments', {
          noteId: savedNoteId, // 使用保存后的实际noteId
          attachments: attachments
        })
      }

      // 关联标签
      if (selectedTagIds.value.length > 0) {
        await api.post('/note-tag/batch-add',
            selectedTagIds.value,
            {params: {noteId: savedNoteId}} // 使用正确的noteId
        )
      }
    } else {
      ElMessage.error('保存失败: ' + response.data.msg)
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    // 详细错误信息提示
    if (error.response && error.response.status === 404) {
      ElMessage.error(`保存失败: 接口不存在 - ${error.response.config.url}`)
    } else {
      ElMessage.error('保存草稿失败，请重试')
    }
  } finally {
    isLoading.value = false
  }
}

// 发布笔记
const handlePublish = async () => {
  if (!noteTitle.value.trim()) {
    ElMessage.error('请输入笔记标题')
    return
  }

  if (!categoryId.value) {
    ElMessage.error('请选择分类')
    return
  }

  if (!noteContent.value.trim()) {
    ElMessage.error('请输入笔记内容')
    return
  }

  // 检查是否有文件正在上传
  const uploadingFiles = uploadedFiles.value.filter(f => f.uploading)
  if (uploadingFiles.length > 0) {
    ElMessage.warning('有文件正在上传，请等待上传完成后再发布')
    return
  }

  // 检查是否有上传失败的文件
  const errorFiles = uploadedFiles.value.filter(f => f.uploadError)
  if (errorFiles.length > 0) {
    ElMessage.warning('部分文件上传失败，请重新上传后再发布')
    return
  }

  isLoading.value = true
  try {
    const noteData = {
      id: isEditMode.value ? route.params.id : null,
      title: noteTitle.value,
      content: noteContent.value,
      categoryId: categoryId.value,
      coverUrl: coverUrl.value,
      userId: currentUserId.value,
      isPublic: 1, // 发布为公开
      isProcessed: 1 // 标记为已处理
    }

    let response;
    if (isEditMode.value) {
      response = await api.put('/note/update', noteData, {
        params: {
          userId: currentUserId.value
        }
      });
    } else {
      response = await api.post('/note/add', {
        ...noteData,
        userId: currentUserId.value
      });
    }

    if (response.data.code === 200) {
      ElMessage.success('笔记发布成功！')
      console.log('发布的笔记ID:', response.data.data)
      const noteId = isEditMode.value ? route.params.id : response.data.data;

      // 关联附件 - 修复接口URL和请求方式
      if (uploadedFiles.value.length > 0) {
        const attachments = uploadedFiles.value
            .filter(f => f.url && !f.uploadError)
            .map(f => ({
              fileUrl: f.url,        // 对应NoteAttachment的fileUrl字段
              fileName: f.name,      // 对应fileName字段
              fileSize: f.size,      // 对应fileSize字段
              fileType: f.type       // 对应fileType字段
            }))

        if (attachments.length > 0) {
          await api.post('/note/attachments', {
            noteId: noteId,
            attachments: attachments  // 发送符合NoteAttachment结构的对象列表
          })
        }
      }

      // 关联标签
      if (selectedTagIds.value.length > 0) {
        await api.post('/note-tag/batch-add',
            selectedTagIds.value,
            {params: {noteId: noteId}}
        )
      }

      resetForm()
      router.push('/notes')
    } else {
      ElMessage.error('发布失败: ' + response.data.msg)
    }
  } catch (error) {
    console.error('发布笔记失败:', error)
    // 详细错误信息提示
    if (error.response && error.response.status === 404) {
      ElMessage.error(`发布失败: 接口不存在 - ${error.response.config.url}`)
    } else {
      ElMessage.error('发布笔记失败，请重试')
    }
  } finally {
    isLoading.value = false
  }
}

const handleCancel = () => {
  if (noteTitle.value || noteContent.value || uploadedFiles.value.length > 0 || coverUrl.value || categoryId.value) {
    if (confirm('确定要取消吗？未保存的内容将会丢失。')) {
      router.back()
    }
  } else {
    router.back()
  }
}

const resetForm = () => {
  noteTitle.value = ''
  noteContent.value = ''
  categoryId.value = ''
  coverUrl.value = ''
  uploadedFiles.value = []
  selectedTagIds.value = []
  currentTagId.value = ''
}

// 获取笔记的附件列表
const fetchNoteAttachments = async (noteId) => {
  try {
    const token = localStorage.getItem('token');
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');

    console.log(`请求附件列表: /note/attachments/${noteId}?userId=${userInfo.id}`);

    const response = await api.get(`/note/attachments/${noteId}`, {
      params: { userId: userInfo.id },
      headers: { Authorization: `Bearer ${token}` }
    });

    console.log('附件接口返回数据:', response.data);

    // 关键修复：根据实际返回结构判断成功与否
    // 从控制台输出看，接口使用success: true表示成功，而非code: 200
    if (response.data.success) {
      // 确保URL正确
      return response.data.data.map(attachment => {
        let fileUrl = attachment.fileUrl;
        // 如果是相对路径，补全为完整URL
        if (fileUrl && !fileUrl.startsWith('http')) {
          fileUrl = BASE_API + fileUrl;
        }
        console.log(`处理后的文件URL: ${fileUrl}`);
        return {
          id: attachment.id || Date.now() + Math.random(),
          name: attachment.fileName,
          size: attachment.fileSize,
          type: attachment.fileType,
          url: fileUrl,
          uploading: false,
          uploadError: false
        };
      });
    }
    console.log('附件接口返回非成功状态');
    return [];
  } catch (error) {
    console.error('获取笔记附件失败:', error);
    ElMessage.warning('无法加载附件列表');
    return [];
  }
};


// 获取分类列表
const fetchCategories = async () => {
  try {
    const token = localStorage.getItem('token')
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')

    const res = await api.get('/category/tree', {
      params: {userId: userInfo.id},
      headers: {Authorization: `Bearer ${token}`}
    })

    if (res.data.code === 200) {
      // 处理分类数据，添加层级前缀
      const buildHierarchicalCategories = (items, prefix = '') => {
        return items.map(item => {
          const categorizedItem = {
            ...item,
            displayName: prefix + item.name
          }

          if (item.children && item.children.length) {
            categorizedItem.children = buildHierarchicalCategories(
                item.children,
                prefix + '--'
            )
          }
          return categorizedItem
        })
      }

      const hierarchicalCats = buildHierarchicalCategories(res.data.data)

      const flattenCategories = (items) => {
        let result = []
        items.forEach(item => {
          result.push(item)
          if (item.children) {
            result = result.concat(flattenCategories(item.children))
          }
        })
        return result
      }

      categories.value = flattenCategories(hierarchicalCats)
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取笔记详情
const fetchNoteDetail = async (noteId) => {
  try {
    const token = localStorage.getItem('token');
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}');

    if (!token || !userInfo.id) {
      ElMessage.warning('请先登录');
      return;
    }

    const response = await api.get(`/note/get/${noteId}`, {
      params: {
        userId: userInfo.id
      },
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    if (response.data) {
      if (response.data.code === 200) {
        const note = response.data.data;
        noteTitle.value = note.title || '';
        noteContent.value = note.content || '';
        categoryId.value = note.categoryId || '';
        coverUrl.value = note.coverUrl || '';

        // 获取关联的标签
        const tagResponse = await api.get(`/note-tag/tags/${noteId}`);
        if (tagResponse.data.code === 200) {
          selectedTagIds.value = tagResponse.data.data || [];
        } else {
          ElMessage.warning(`获取标签失败: ${tagResponse.data.msg || '未知错误'}`);
        }

        // 关键修复：确保附件加载完成后再更新UI
        const attachments = await fetchNoteAttachments(noteId);
        console.log('加载的附件数据:', attachments); // 用于调试

        // 验证附件URL是否有效
        const validAttachments = attachments.filter(attach => {
          if (!attach.url) {
            console.warn('发现没有URL的附件:', attach);
            return false;
          }
          return true;
        });

        uploadedFiles.value = validAttachments;
        console.log('更新后的附件列表:', uploadedFiles.value); // 用于调试
      } else {
        ElMessage.error(`加载笔记失败: ${response.data.msg || '未知错误'}`);
        console.error('笔记加载失败:', response.data);
      }
    } else {
      ElMessage.error('加载笔记失败: 服务器未返回有效数据');
    }

  }catch (error) {
    console.error('获取笔记详情失败:', error);
    if (error.response) {
      ElMessage.error(`加载笔记失败: ${error.response.data?.msg || `HTTP错误 ${error.response.status}`}`);
    } else if (error.request) {
      ElMessage.error('加载笔记失败: 未收到服务器响应，请检查网络');
    } else {
      ElMessage.error(`加载笔记失败: ${error.message}`);
    }
  }
}

onMounted(() => {
  console.log('笔记页面已加载')
  // 从localStorage获取用户信息
  const userInfo = JSON.parse(localStorage.getItem('user') || '{}');
  if (userInfo.id) {
    currentUserId.value = userInfo.id;
  } else {
    ElMessage.error('未获取到用户信息，请重新登录');
    router.push('/login');
  }
  currentUserId.value = userInfo.id || userInfo.userId || null

  // 编辑模式下初始化noteId
  if (isEditMode.value) {
    noteId.value = route.params.id;
    fetchNoteDetail(route.params.id);
  }

  if (currentUserId.value) {
    fetchCategories()
    fetchTags()
  } else {
    console.error('未获取到用户ID，请先登录')
    ElMessage.warning('请先登录')
    router.push('/login')
  }

  // 为 Quill 编辑器添加自定义提示
  setTimeout(() => {
    addQuillTooltips()
  }, 1000)
})

// 为 Quill 编辑器工具栏添加提示
const addQuillTooltips = () => {
  const tooltips = {
    'ql-bold': '粗体 (Ctrl+B)',
    'ql-italic': '斜体 (Ctrl+I)',
    'ql-underline': '下划线 (Ctrl+U)',
    'ql-strike': '删除线',
    'ql-blockquote': '引用块',
    'ql-code-block': '代码块',
    'ql-header': '标题级别',
    'ql-list[value="ordered"]': '有序列表',
    'ql-list[value="bullet"]': '无序列表',
    'ql-indent[value="-1"]': '减少缩进',
    'ql-indent[value="+1"]': '增加缩进',
    'ql-link': '插入链接',
    'ql-image': '插入图片',
    'ql-video': '插入视频',
    'ql-color': '文字颜色',
    'ql-background': '背景颜色',
    'ql-font': '字体',
    'ql-size': '字体大小',
    'ql-align': '文本对齐',
    'ql-clean': '清除格式'
  }

  const interval = setInterval(() => {
    const toolbar = document.querySelector('.ql-toolbar')
    if (toolbar) {
      Object.entries(tooltips).forEach(([selector, tooltip]) => {
        const element = toolbar.querySelector(`.${selector}`)
        if (element && !element.title) {
          element.title = tooltip
        }
      })
      clearInterval(interval)
    }
  }, 100)
}

// 新建标签表单验证规则
const newTagRules = {
  name: [
    {required: true, message: '请输入标签名称', trigger: 'blur'},
    {max: 20, message: '标签名称不能超过20个字符', trigger: 'blur'}
  ]
}

// 获取所有标签
const fetchTags = async () => {
  try {
    const config = createRequestConfig()
    const res = await request.get('/tag/list', config)

    if (res.data.code === 200) {
      tags.value = res.data.data || []
      filteredTags.value = [...tags.value]
    } else {
      ElMessage.error(res.data.msg || '获取标签失败')
    }
  } catch (error) {
    console.error('获取标签失败:', error)
    ElMessage.error('获取标签失败，请重试')
  }
}

// 创建请求配置
const createRequestConfig = (params = {}) => {
  const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
  const token = localStorage.getItem('token') || ''

  const requestParams = {
    userId: userInfo?.id,
    ...params
  }

  return {
    params: requestParams,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
}

// 添加标签
const addTag = (tagId) => {
  if (tagId && !selectedTagIds.value.includes(tagId)) {
    selectedTagIds.value.push(tagId)
  }
  currentTagId.value = ''
}

// 移除标签
const removeTag = (tagId) => {
  selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId)
}

// 创建新标签
const createNewTag = async () => {
  try {
    const valid = await newTagFormRef.value.validate()
    if (!valid) return

    const config = createRequestConfig()
    const currentTime = new Date().toISOString()

    await request.post('/tag/add', {
      name: newTagForm.name,
      createdAt: currentTime
    }, config)

    ElMessage.success('标签创建成功')
    showCreateTagDialog.value = false
    await fetchTags()
    newTagForm.name = ''
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error(error.response?.data?.msg || '创建标签失败')
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.note-creator-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 80px;
  position: relative;
}

.btn:hover::after {
  content: attr(title);
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
  transform: translateY(-1px);
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background-color: #7f8c8d;
  transform: translateY(-1px);
}

.btn-outline {
  background-color: transparent;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.btn-outline:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

/* 基本信息区域样式 */
.basic-info-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: #f8f9fa;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  align-items: start;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.required {
  color: #e74c3c;
}

.form-select {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.form-select:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

/* 封面图片上传样式 */
.cover-upload-area {
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 12px;
}

.cover-preview {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
}

.cover-preview:hover {
  border-color: #3498db;
  background: #f0f7ff;
}

.cover-preview.has-image {
  border-style: solid;
  border-color: #e1e5e9;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  text-align: center;
  color: #7f8c8d;
}

.cover-icon {
  font-size: 2em;
  margin-bottom: 8px;
}

.cover-input {
  display: none;
}

.cover-actions {
  display: flex;
  gap: 8px;
}

.cover-action-btn {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 12px;
}

.cover-action-btn:hover {
  background: #f5f7fa;
}

.cover-action-btn.remove:hover {
  background: #f8d7da;
  border-color: #f5c6cb;
}

/* 标题输入区域 */
.title-input-group {
  margin-bottom: 30px;
  position: relative;
}

.title-input {
  width: 100%;
  padding: 16px;
  font-size: 1.8em;
  font-weight: 600;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.title-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.char-count {
  position: absolute;
  right: 10px;
  bottom: 10px;
  color: #95a5a6;
  font-size: 0.8em;
}

.editor-section {
  margin-bottom: 30px;
}

.section-label {
  display: block;
  margin-bottom: 10px;
  font-weight: 600;
  color: #2c3e50;
}

.quill-editor-container {
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e1e5e9;
}

/* 深度选择器修改 Quill 内部样式 */
.quill-editor-container :deep(.ql-container) {
  font-family: 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
  font-size: 14px;
  height: 400px;
}

.quill-editor-container :deep(.ql-editor) {
  min-height: 400px;
  line-height: 1.6;
}

.quill-editor-container :deep(.ql-toolbar) {
  border-top: none;
  border-left: none;
  border-right: none;
  border-bottom: 1px solid #e1e5e9;
  background: #f8f9fa;
}

.quill-editor-container :deep(.ql-container) {
  border: none;
}

/* 为 Quill 工具栏按钮添加更好的悬停效果 */
.quill-editor-container :deep(.ql-toolbar .ql-formats) {
  margin-right: 15px;
}

.quill-editor-container :deep(.ql-toolbar button) {
  position: relative;
}

.quill-editor-container :deep(.ql-toolbar button:hover::before) {
  content: attr(title);
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
}

.upload-section {
  margin-top: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
}

.file-count {
  color: #7f8c8d;
  font-size: 0.9em;
}

.section-tip {
  margin-top: 0;
  margin-bottom: 20px;
  color: #7f8c8d;
  font-size: 0.9em;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafbfc;
  margin-bottom: 20px;
  position: relative;
}

.upload-area:hover {
  border-color: #3498db;
  background: #f0f7ff;
}

.file-input {
  display: none;
}

.upload-placeholder {
  color: #7f8c8d;
}

.upload-icon {
  font-size: 2em;
  margin-bottom: 10px;
}

.upload-hint {
  font-size: 0.8em;
  color: #95a5a6;
}

.file-list {
  space-y: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e1e5e9;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #f8f9fa;
  transition: all 0.3s ease;
}

.file-item:hover {
  border-color: #3498db;
  background: #f0f7ff;
}

.file-icon {
  font-size: 1.5em;
  margin-right: 12px;
  cursor: help;
}

.file-info {
  flex: 1;
}

.file-name {
  display: block;
  font-weight: 500;
  color: #2c3e50;
}

.file-size {
  font-size: 0.8em;
  color: #7f8c8d;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
  position: relative;
}

.action-btn:hover::after {
  content: attr(title);
  position: absolute;
  bottom: -30px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
}

.action-btn:hover {
  background-color: #e9ecef;
}

.action-btn.remove:hover {
  background-color: #f8d7da;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-row {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .cover-upload-area {
    align-items: center;
  }

  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: start;
  }

  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
}

/* 标签选择区域样式 */
.tag-selection {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-top: 8px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.create-tag-btn {
  margin-left: 8px;
}

/* 调整表单组间距 */
.form-group {
  margin-bottom: 20px;
}
</style>
