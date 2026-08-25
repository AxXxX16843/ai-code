/**
 * 代码生成类型枚举（对应后端 CodeGenTypeEnum）
 */
export const CodeGenTypeEnum = {
  HTML: 'html',
  MULTI_FILE: 'multi_file',
}

export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.HTML]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.HTML,
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
  },
}

export const CODE_GEN_TYPE_OPTIONS = Object.values(CODE_GEN_TYPE_CONFIG)
