import { onBeforeUnmount, onMounted, ref } from 'vue'

const MESSAGE_TYPES = {
  toggle: 'VISUAL_EDITOR_TOGGLE',
  clearSelection: 'VISUAL_EDITOR_CLEAR_SELECTION',
  destroy: 'VISUAL_EDITOR_DESTROY',
  selected: 'VISUAL_EDITOR_ELEMENT_SELECTED',
}

const SCRIPT_ID = 'ai-code-visual-editor-script'
const INJECT_RETRY_DELAY = 120
const MAX_INJECT_RETRIES = 20

export const createVisualEditorScript = (parentOrigin) => String.raw`
  (() => {
    if (window.__AI_CODE_VISUAL_EDITOR__) return;

    const MESSAGE_TYPES = ${JSON.stringify(MESSAGE_TYPES)};
    const PARENT_ORIGIN = ${JSON.stringify(parentOrigin)};
    const STYLE_ID = 'ai-code-visual-editor-styles';
    const HOVER_CLASS = 'ai-code-edit-hover';
    const SELECTED_CLASS = 'ai-code-edit-selected';
    const TIP_ID = 'ai-code-edit-tip';
    const IGNORED_TAGS = new Set(['HTML', 'BODY', 'HEAD', 'SCRIPT', 'STYLE', 'LINK', 'META']);

    let editMode = false;
    let hoveredElement = null;
    let selectedElement = null;
    let tipTimer = null;

    const escapeCss = (value) => {
      if (window.CSS && typeof window.CSS.escape === 'function') return window.CSS.escape(value);
      return String(value).replace(/[^a-zA-Z0-9_-]/g, (character) => '\\' + character);
    };

    const getClassNames = (element) => {
      const className = typeof element.className === 'string'
        ? element.className
        : element.className && typeof element.className.baseVal === 'string'
          ? element.className.baseVal
          : '';
      return className
        .split(/\s+/)
        .filter(Boolean)
        .filter((name) => name !== HOVER_CLASS && name !== SELECTED_CLASS)
        .slice(0, 4);
    };

    const isSelectable = (element) => {
      return element instanceof Element
        && !IGNORED_TAGS.has(element.tagName)
        && !element.closest('#' + TIP_ID);
    };

    const injectStyles = () => {
      if (document.getElementById(STYLE_ID)) return;
      const style = document.createElement('style');
      style.id = STYLE_ID;
      style.textContent = [
        'html.ai-code-editing, html.ai-code-editing * { cursor: crosshair !important; }',
        '.' + HOVER_CLASS + ' { outline: 2px dashed #13a88a !important; outline-offset: 2px !important; background-color: rgba(19, 168, 138, 0.06) !important; }',
        '.' + SELECTED_CLASS + ' { outline: 3px solid #087d68 !important; outline-offset: 2px !important; background-color: rgba(19, 168, 138, 0.1) !important; }',
        '#' + TIP_ID + ' { position: fixed; top: 16px; right: 16px; z-index: 2147483647; padding: 10px 14px; border: 1px solid rgba(255,255,255,.55); border-radius: 6px; color: #fff; background: rgba(8,125,104,.94); box-shadow: 0 8px 24px rgba(8,55,47,.22); font: 13px/1.5 system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif; pointer-events: none; animation: aiCodeEditorTipIn .2s ease-out; }',
        '@keyframes aiCodeEditorTipIn { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }',
      ].join('\n');
      document.head.appendChild(style);
    };

    const showTip = () => {
      document.getElementById(TIP_ID)?.remove();
      if (tipTimer) window.clearTimeout(tipTimer);
      const tip = document.createElement('div');
      tip.id = TIP_ID;
      tip.textContent = '编辑模式已开启 · 悬浮查看，点击选中';
      document.body.appendChild(tip);
      tipTimer = window.setTimeout(() => tip.remove(), 3000);
    };

    const clearHover = () => {
      hoveredElement?.classList.remove(HOVER_CLASS);
      hoveredElement = null;
    };

    const clearSelection = () => {
      document.querySelectorAll('.' + SELECTED_CLASS).forEach((element) => {
        element.classList.remove(SELECTED_CLASS);
      });
      selectedElement = null;
    };

    const generateSelector = (element) => {
      const path = [];
      let current = element;

      while (current && current !== document.body && current instanceof Element) {
        let selector = current.tagName.toLowerCase();
        if (current.id) {
          selector += '#' + escapeCss(current.id);
          path.unshift(selector);
          break;
        }

        const classes = getClassNames(current);
        if (classes.length) selector += '.' + classes.map(escapeCss).join('.');

        const parent = current.parentElement;
        if (parent) {
          const sameTagSiblings = Array.from(parent.children).filter((item) => item.tagName === current.tagName);
          if (sameTagSiblings.length > 1) {
            selector += ':nth-of-type(' + (sameTagSiblings.indexOf(current) + 1) + ')';
          }
        }

        path.unshift(selector);
        current = parent;
      }

      return path.join(' > ');
    };

    const getElementInfo = (element) => {
      const rect = element.getBoundingClientRect();
      const classes = getClassNames(element);
      return {
        tagName: element.tagName.toLowerCase(),
        id: element.id || '',
        className: classes.join(' '),
        textContent: (element.textContent || '').replace(/\s+/g, ' ').trim().slice(0, 160),
        selector: generateSelector(element),
        pagePath: window.location.search + window.location.hash,
        rect: {
          top: Math.round(rect.top),
          left: Math.round(rect.left),
          width: Math.round(rect.width),
          height: Math.round(rect.height),
        },
      };
    };

    const handlePointerOver = (event) => {
      if (!editMode || !isSelectable(event.target) || event.target === selectedElement) return;
      clearHover();
      hoveredElement = event.target;
      hoveredElement.classList.add(HOVER_CLASS);
    };

    const handlePointerOut = (event) => {
      if (!editMode || event.target !== hoveredElement) return;
      if (event.relatedTarget && event.target.contains(event.relatedTarget)) return;
      clearHover();
    };

    const blockInteraction = (event) => {
      if (!editMode) return;
      event.preventDefault();
      event.stopPropagation();
      event.stopImmediatePropagation();
    };

    const handleClick = (event) => {
      if (!editMode) return;
      blockInteraction(event);
      if (!isSelectable(event.target)) return;

      clearSelection();
      clearHover();
      selectedElement = event.target;
      selectedElement.classList.add(SELECTED_CLASS);

      window.parent.postMessage({
        type: MESSAGE_TYPES.selected,
        data: { elementInfo: getElementInfo(selectedElement) },
      }, PARENT_ORIGIN);
    };

    const setEditMode = (enabled) => {
      editMode = enabled;
      document.documentElement.classList.toggle('ai-code-editing', enabled);
      if (enabled) {
        injectStyles();
        showTip();
      } else {
        clearHover();
        clearSelection();
        document.getElementById(TIP_ID)?.remove();
      }
    };

    const handleMessage = (event) => {
      if (event.source !== window.parent || event.origin !== PARENT_ORIGIN || !event.data) return;
      if (event.data.type === MESSAGE_TYPES.toggle) setEditMode(Boolean(event.data.enabled));
      if (event.data.type === MESSAGE_TYPES.clearSelection) clearSelection();
      if (event.data.type === MESSAGE_TYPES.destroy) setEditMode(false);
    };

    document.addEventListener('pointerover', handlePointerOver, true);
    document.addEventListener('pointerout', handlePointerOut, true);
    document.addEventListener('pointerdown', blockInteraction, true);
    document.addEventListener('click', handleClick, true);
    document.addEventListener('submit', blockInteraction, true);
    window.addEventListener('message', handleMessage);

    window.__AI_CODE_VISUAL_EDITOR__ = { setEditMode, clearSelection };
  })();
`

export const buildVisualEditPrompt = (userMessage, elementInfo) => {
  if (!elementInfo) return userMessage
  return `${userMessage}\n\n[可视化编辑目标]\n页面：${elementInfo.pagePath || '/'}\n元素：<${elementInfo.tagName}>\n选择器：${elementInfo.selector}\n元素文本：${elementInfo.textContent || '无'}\n请仅围绕上述页面元素理解并完成本次修改。`
}

export const useVisualEditor = ({ onError } = {}) => {
  const iframeRef = ref(null)
  const editMode = ref(false)
  const selectedElement = ref(null)
  let injectTimer = null

  const sendToIframe = (data) => {
    iframeRef.value?.contentWindow?.postMessage(data, window.location.origin)
  }

  const injectEditor = (retryCount = 0) => {
    if (!editMode.value || !iframeRef.value) return

    try {
      const iframeDocument = iframeRef.value.contentDocument
      if (!iframeDocument?.head || !iframeDocument?.body) {
        if (retryCount < MAX_INJECT_RETRIES) {
          injectTimer = window.setTimeout(() => injectEditor(retryCount + 1), INJECT_RETRY_DELAY)
        }
        return
      }

      if (!iframeDocument.getElementById(SCRIPT_ID)) {
        const script = iframeDocument.createElement('script')
        script.id = SCRIPT_ID
        script.textContent = createVisualEditorScript(window.location.origin)
        iframeDocument.head.appendChild(script)
      }

      sendToIframe({ type: MESSAGE_TYPES.toggle, enabled: true })
    } catch (error) {
      editMode.value = false
      selectedElement.value = null
      onError?.(error)
    }
  }

  const enableEditMode = () => {
    if (!iframeRef.value) return false
    editMode.value = true
    selectedElement.value = null
    injectEditor()
    return true
  }

  const exitEditMode = () => {
    if (injectTimer) window.clearTimeout(injectTimer)
    injectTimer = null
    sendToIframe({ type: MESSAGE_TYPES.toggle, enabled: false })
    editMode.value = false
    selectedElement.value = null
  }

  const toggleEditMode = () => {
    if (editMode.value) {
      exitEditMode()
      return false
    }
    return enableEditMode()
  }

  const clearSelectedElement = () => {
    selectedElement.value = null
    sendToIframe({ type: MESSAGE_TYPES.clearSelection })
  }

  const handleIframeLoad = () => {
    selectedElement.value = null
    if (editMode.value) injectEditor()
  }

  const handleIframeMessage = (event) => {
    if (
      event.origin !== window.location.origin ||
      event.source !== iframeRef.value?.contentWindow ||
      event.data?.type !== MESSAGE_TYPES.selected ||
      !event.data?.data?.elementInfo
    ) {
      return
    }
    selectedElement.value = event.data.data.elementInfo
  }

  onMounted(() => window.addEventListener('message', handleIframeMessage))
  onBeforeUnmount(() => {
    if (injectTimer) window.clearTimeout(injectTimer)
    sendToIframe({ type: MESSAGE_TYPES.destroy })
    window.removeEventListener('message', handleIframeMessage)
  })

  return {
    iframeRef,
    editMode,
    selectedElement,
    toggleEditMode,
    exitEditMode,
    clearSelectedElement,
    handleIframeLoad,
  }
}
