<template>
  <div class="app-logo" :class="`app-logo--${variant}`" :style="{ width: `${size}px`, height: `${size}px` }" aria-label="AI Code 小狐狸标志">
    <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" role="img">
      <circle class="logo-disc" cx="50" cy="50" r="46" />
      <path class="logo-tail" d="M24 70c-9-11-7-25 4-32 3 11 10 17 21 20-5 10-13 15-25 12Z" />
      <path class="logo-ear" d="M26 38 23 15c11 3 19 9 23 18Z" />
      <path class="logo-ear" d="m74 38 3-23c-11 3-19 9-23 18Z" />
      <path class="logo-head" d="M25 46c0-16 11-26 25-26s25 10 25 26c0 20-10 31-25 31S25 66 25 46Z" />
      <path class="logo-ear-inner" d="m28 23 3 12 10-10Z" />
      <path class="logo-ear-inner" d="m72 23-3 12-10-10Z" />
      <path class="logo-muzzle" d="M34 57c4-8 10-11 16-11s12 3 16 11c-3 12-9 17-16 17s-13-5-16-17Z" />
      <circle class="logo-eye" cx="39" cy="47" r="4" />
      <circle class="logo-eye" cx="61" cy="47" r="4" />
      <circle class="logo-eye-glint" cx="40" cy="46" r="1.2" />
      <circle class="logo-eye-glint" cx="62" cy="46" r="1.2" />
      <path class="logo-wink" d="M57 47c3-3 7-3 9 0" />
      <path class="logo-nose" d="M46 57c2-2 6-2 8 0-1 4-3 5-4 5s-3-1-4-5Z" />
      <path class="logo-smile" d="M50 62v3m0 0c-3 3-6 3-8 1m8-1c3 3 6 3 8 1" />
      <circle class="logo-blush" cx="32" cy="58" r="3" />
      <circle class="logo-blush" cx="68" cy="58" r="3" />
    </svg>
  </div>
</template>

<script setup>
defineProps({ size: { type: Number, default: 32 }, variant: { type: String, default: 'brand' } })
</script>

<style scoped>
.app-logo { display: inline-flex; flex: 0 0 auto; position: relative; }
.app-logo svg { display: block; width: 100%; height: 100%; overflow: visible; }
.logo-disc { fill: #14212a; stroke: #2dd4bf; stroke-width: 1.5; }
.logo-head { fill: #e8894d; }
.logo-tail { fill: #c9613f; }
.logo-ear { fill: #d87443; }
.logo-ear-inner { fill: #f5c0a0; }
.logo-muzzle { fill: #ffe5ca; }
.logo-eye { fill: #14212a; animation: eyePulse 3.6s ease-in-out infinite; }
.logo-eye-glint { fill: #fff; }
.logo-wink { fill: none; stroke: #14212a; stroke-width: 2.5; stroke-linecap: round; }
.logo-nose { fill: #14212a; }
.logo-smile { fill: none; stroke: #14212a; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
.logo-blush { fill: #eb856e; opacity: .7; }
.app-logo--brand { filter: drop-shadow(0 5px 10px rgba(13, 31, 42, .18)); }
.app-logo--brand::after { content: ''; position: absolute; inset: -4px; border: 1px solid rgba(45, 212, 191, .25); border-radius: 50%; animation: brandRing 5s linear infinite; }
.app-logo--avatar .logo-disc { stroke: #d7a84b; stroke-width: 2; }
.app-logo--avatar { filter: drop-shadow(0 4px 9px rgba(13, 31, 42, .2)); }
.app-logo--avatar .logo-eye { animation: avatarPulse 1.8s ease-in-out infinite; }
.app-logo--loading { animation: loadingFloat 2.8s ease-in-out infinite; filter: drop-shadow(0 12px 22px rgba(45, 212, 191, .24)); }
.app-logo--loading .logo-disc { stroke: #2dd4bf; stroke-width: 2; stroke-dasharray: 12 8; animation: spinRing 8s linear infinite; }
.app-logo--loading .logo-eye { animation: loadingEye 1s ease-in-out infinite; }
@keyframes eyePulse { 0%, 100% { opacity: 1; } 50% { opacity: .55; } }
@keyframes avatarPulse { 0%, 100% { transform: scale(1); transform-origin: 68px 35px; } 50% { transform: scale(1.28); transform-origin: 68px 35px; } }
@keyframes brandRing { to { transform: rotate(360deg); } }
@keyframes spinRing { to { transform: rotate(360deg); transform-origin: 50px 50px; } }
@keyframes loadingFloat { 0%, 100% { transform: translateY(0) rotate(-2deg); } 50% { transform: translateY(-8px) rotate(2deg); } }
@keyframes loadingEye { 0%, 100% { opacity: .45; } 50% { opacity: 1; } }
@media (prefers-reduced-motion: reduce) { .app-logo *, .app-logo::after { animation: none !important; } }
</style>
