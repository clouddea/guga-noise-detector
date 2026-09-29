/* =====================================================================
   build-c.js — 把真实角色贴纸（base64）注入 C-kanahei-pastel 模板
   用法：node build-c.js
   输入：C-kanahei-pastel.src.html（含占位符 __GG_IMG_JSON__）
         assets/gg-images.json（key → data:image/png;base64,...）
   输出：C-kanahei-pastel.html（自包含，双击即开）
   ===================================================================== */
const fs = require('fs');
const path = require('path');

const dir = __dirname;
const tplPath = path.join(dir, 'C-kanahei-pastel.src.html');
const jsonPath = path.join(dir, 'assets', 'gg-images.json');
const outPath = path.join(dir, 'C-kanahei-pastel.html');
const PLACEHOLDER = '__GG_IMG_JSON__';

const json = fs.readFileSync(jsonPath, 'utf8').trim();
const tpl = fs.readFileSync(tplPath, 'utf8');

if (!tpl.includes(PLACEHOLDER)) {
  throw new Error('模板里找不到占位符 ' + PLACEHOLDER);
}

// 用 split/join 而非 replace，避免 base64 中的 $ 被当成替换模式
const out = tpl.split(PLACEHOLDER).join(json);

// 简单自检：注入后应包含 5 张 base64 PNG
const imgCount = (out.match(/data:image\/png;base64,/g) || []).length;
if (imgCount < 5) {
  throw new Error('注入后 base64 PNG 数量异常：' + imgCount);
}

fs.writeFileSync(outPath, out, 'utf8');
console.log('OK  written: ' + outPath);
console.log('    尺寸: ' + (Buffer.byteLength(out) / 1024).toFixed(1) + ' KB');
console.log('    内嵌 base64 PNG: ' + imgCount + ' 张');
