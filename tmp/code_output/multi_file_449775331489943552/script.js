// script.js — 招聘卡片交互：点击申请按钮，显示友好反馈
document.addEventListener('DOMContentLoaded', function () {
  const applyBtn = document.getElementById('applyBtn');
  const originalText = '立即申请';
  const successText = '✓ 简历已发送';

  applyBtn.addEventListener('click', function (e) {
    e.preventDefault(); // 阻止 # 链接跳转

    // 如果已经处于“已申请”状态，则不再重复触发
    if (applyBtn.textContent === successText) return;

    // 临时改变按钮文字，带来肯定感
    applyBtn.textContent = successText;
    // 添加一个短暂的高亮样式
    applyBtn.style.backgroundColor = '#2d6a4f';
    applyBtn.style.boxShadow = '0 8px 20px -8px #2d6a4f';

    // 2.5秒后恢复原始状态 (演示交互)
    setTimeout(() => {
      applyBtn.textContent = originalText;
      applyBtn.style.backgroundColor = ''; // 恢复css中的值
      applyBtn.style.boxShadow = '';
    }, 2500);
  });
});