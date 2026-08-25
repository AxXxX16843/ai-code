// script.js — 切换额外特性显示
const toggleBtn = document.getElementById('toggleBtn');
const extraInfo = document.getElementById('extraInfo');

// 初始状态：隐藏额外信息
let isExpanded = false;

toggleBtn.addEventListener('click', () => {
  isExpanded = !isExpanded;

  if (isExpanded) {
    extraInfo.classList.remove('hidden');
    toggleBtn.textContent = '收起特性 ▲';
  } else {
    extraInfo.classList.add('hidden');
    toggleBtn.textContent = '查看更多特性';
  }
});