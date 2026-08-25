<template>
  <div class="app-logo" :style="{ width: size + 'px', height: size + 'px' }">
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
      <!-- 左耳（摆动动画） -->
      <ellipse class="ear left-ear" cx="24" cy="32" rx="11" ry="18" fill="#c8843a" />
      <!-- 右耳（摆动动画） -->
      <ellipse class="ear right-ear" cx="76" cy="32" rx="11" ry="18" fill="#c8843a" />

      <!-- 脸 -->
      <ellipse cx="50" cy="56" rx="34" ry="29" fill="#e8a33d" />

      <!-- 头顶毛发 -->
      <path
        d="M30 40 Q32 22 50 20 Q68 22 70 40 Q60 32 50 32 Q40 32 30 40Z"
        fill="#f0c068"
      />

      <!-- 眼睛（眨眼动画） -->
      <circle class="eye" cx="38" cy="54" r="5" fill="#2b2419" />
      <circle class="eye" cx="62" cy="54" r="5" fill="#2b2419" />
      <circle cx="40" cy="52" r="1.8" fill="#fff" />
      <circle cx="64" cy="52" r="1.8" fill="#fff" />

      <!-- 鼻子 -->
      <ellipse cx="50" cy="64" rx="6" ry="5" fill="#2b2419" />

      <!-- 嘴 -->
      <path
        d="M43 68 Q50 74 57 68"
        fill="none"
        stroke="#2b2419"
        stroke-width="2.2"
        stroke-linecap="round"
      />

      <!-- 舌头（伸缩动画） -->
      <path class="tongue" d="M45 69 Q50 80 55 69Z" fill="#ff7b8a" />

      <!-- 脸颊腮红 -->
      <ellipse cx="30" cy="63" rx="5" ry="3" fill="#f5c6a0" opacity="0.6" />
      <ellipse cx="70" cy="63" rx="5" ry="3" fill="#f5c6a0" opacity="0.6" />
    </svg>
  </div>
</template>

<script setup>
defineProps({
  size: {
    type: Number,
    default: 32,
  },
})
</script>

<style scoped>
.app-logo {
  display: inline-flex;
  flex-shrink: 0;
}

.app-logo svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* 耳朵摆动 */
.left-ear {
  transform-origin: 24px 32px;
  animation: earLeft 2.4s ease-in-out infinite;
}

.right-ear {
  transform-origin: 76px 32px;
  animation: earRight 2.4s ease-in-out infinite;
}

@keyframes earLeft {
  0%,
  100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(-9deg);
  }
}

@keyframes earRight {
  0%,
  100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(9deg);
  }
}

/* 眼睛眨眼 */
.eye {
  animation: blink 4s ease-in-out infinite;
}

@keyframes blink {
  0%,
  90%,
  100% {
    transform: scaleY(1);
  }
  93% {
    transform: scaleY(0.1);
  }
}

/* 舌头伸缩 */
.tongue {
  transform-origin: 50px 69px;
  animation: tongueWag 1.8s ease-in-out infinite;
}

@keyframes tongueWag {
  0%,
  100% {
    transform: scaleY(0.55);
  }
  50% {
    transform: scaleY(1);
  }
}
</style>
