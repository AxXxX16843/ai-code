// ===== script.js — 个人简介互动：动态问候 =====
// 等待 DOM 完全加载后再绑定事件（遵循最佳实践）
document.addEventListener('DOMContentLoaded', function () {
    // 获取按钮和消息元素
    const greetBtn = document.getElementById('greetBtn');
    const greetMessage = document.getElementById('greetMessage');

    // 确保元素存在（防御性检查）
    if (!greetBtn || !greetMessage) return;

    // 定义问候语数组（简单变化，增加友好感）
    const greetings = [
        '你好呀 👋 很高兴认识你！',
        '嗨，我是林小溪 ✨ 欢迎交流前端~',
        '感谢访问！今天也是元气满满 🌟',
        'Hello! 希望一切顺心 🍀'
    ];

    // 点击按钮时更新消息 (使用随机问候)
    greetBtn.addEventListener('click', function () {
        // 随机选择一条问候语
        const randomIndex = Math.floor(Math.random() * greetings.length);
        const message = greetings[randomIndex];

        // 将问候语显示在卡片中
        greetMessage.textContent = message;

        // 添加一个微小的临时动画（直接用样式改变，不依赖额外库）
        greetMessage.style.transition = 'all 0.15s ease';
        greetMessage.style.transform = 'scale(1.02)';
        setTimeout(() => {
            greetMessage.style.transform = 'scale(1)';
        }, 150);
    });

    // 可选项：点击消息时也清空或变为默认（保留趣味）
    greetMessage.addEventListener('click', function () {
        greetMessage.textContent = '';
    });
});