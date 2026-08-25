// ===== 交互逻辑 (纯原生JS) =====

// 1. 移动端导航栏切换
const menuToggle = document.getElementById('menu-toggle');
const navLinks = document.getElementById('nav-links');
const body = document.body;

// 切换菜单显示/隐藏
function toggleMobileMenu() {
  navLinks.classList.toggle('show');
  const expanded = navLinks.classList.contains('show');
  menuToggle.setAttribute('aria-expanded', expanded);
  // 简单动画扩展汉堡
  const spans = menuToggle.querySelectorAll('span');
  if (expanded) {
    spans[0].style.transform = 'rotate(45deg) translateY(8px)';
    spans[1].style.opacity = '0';
    spans[2].style.transform = 'rotate(-45deg) translateY(-8px)';
  } else {
    spans[0].style.transform = 'none';
    spans[1].style.opacity = '1';
    spans[2].style.transform = 'none';
  }
}

if (menuToggle && navLinks) {
  menuToggle.addEventListener('click', toggleMobileMenu);

  // 点击链接后关闭移动菜单 (仅小屏)
  navLinks.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', (e) => {
      if (window.innerWidth <= 800) {
        toggleMobileMenu(); // 关闭菜单
      }
    });
  });
}

// 2. 平滑滚动 (原生实现，支持 # 锚点)
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function(e) {
    const targetId = this.getAttribute('href');
    if (targetId.length > 1) {
      const targetEl = document.querySelector(targetId);
      if (targetEl) {
        e.preventDefault();
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }
  });
});

// 3. 预订表单的“模拟”提交反馈
const resForm = document.getElementById('res-form');
const formMessage = document.getElementById('form-message');

if (resForm) {
  resForm.addEventListener('submit', function(e) {
    e.preventDefault(); // 阻止真实提交
    
    // 获取姓名、日期等用于消息 (只是趣味展示)
    const nameInput = document.getElementById('name').value.trim() || '美食家';
    const dateInput = document.getElementById('date').value;
    
    // 简单的客户端验证 (使用 HTML5 required 已做,但增强一下)
    const requiredFields = resForm.querySelectorAll('[required]');
    for (let field of requiredFields) {
      if (!field.value) {
        formMessage.style.color = '#ffb4a2';
        formMessage.textContent = '请填写所有必填信息，以便为您保留席位。';
        return;
      }
    }
    
    // 模拟预订成功消息
    let dateText = dateInput ? new Date(dateInput).toLocaleDateString('zh-CN', {year: 'numeric', month: 'long', day: 'numeric'}) : '近期';
    formMessage.style.color = '#9ed8a0';
    formMessage.textContent = `✓ 预订成功 ${nameInput}！我们已收到您的请求，将尽快确认 ${dateText} 的席位。`;
    
    // 可选择重置表单 (不重置以便显示消息)
    // resForm.reset(); 
    // 如果想要清空取消注释，但会清除消息覆盖
  });
}

// 4. 可选: 菜单卡片加载时的懒加载/错误兜底 (picsum 偶尔有问题)
document.querySelectorAll('img').forEach(img => {
  img.addEventListener('error', function() {
    this.src = 'https://picsum.photos/600/400?random=99';
  });
});