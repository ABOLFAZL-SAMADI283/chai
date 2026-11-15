// یکتا کردن کارت‌ها با index برای انیمیشن ورود پله‌ای
const fs = require('fs');
const path = require('path');
const file = path.join(__dirname, '..', 'app', 'src', 'main', 'java', 'ir', 'chai', 'focus', 'ui', 'Screens.kt');
let s = fs.readFileSync(file, 'utf8');

const marks = [
  ['item {\n            ChaiCard {\n                Row(\n                    modifier = Modifier.fillMaxWidth(),',
   'item {\n            ChaiCard(index = 1) {\n                Row(\n                    modifier = Modifier.fillMaxWidth(),'],
  ['ChaiCard {\n                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {',
   'ChaiCard(index = 2) {\n                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {'],
  ['ChaiCard {\n                Text("۵ هفتهٔ اخیر"',
   'ChaiCard(index = 3) {\n                Text("۵ هفتهٔ اخیر"'],
  ['ChaiCard {\n                Text("بر اساس هدف"',
   'ChaiCard(index = 4) {\n                Text("بر اساس هدف"'],
  ['ChaiCard {\n                Text("نشان‌ها"',
   'ChaiCard(index = 5) {\n                Text("نشان‌ها"'],
  ['ChaiCard {\n                Text("خلاصهٔ کل"',
   'ChaiCard(index = 6) {\n                Text("خلاصهٔ کل"'],
  ['ChaiCard {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Box(\n                        Modifier\n                            .size(48.dp)',
   'ChaiCard(index = 1) {\n                Row(verticalAlignment = Alignment.CenterVertically) {\n                    Box(\n                        Modifier\n                            .size(48.dp)'],
  ['ChaiCard {\n                Text("جدول هفتگی"',
   'ChaiCard(index = 2) {\n                Text("جدول هفتگی"'],
  ['ChaiCard {\n                Text("⚔️ دوئل"',
   'ChaiCard(index = 3) {\n                Text("⚔️ دوئل"'],
  ['ChaiCard {\n                Text("🎯 چالش روزانه"',
   'ChaiCard(index = 4) {\n                Text("🎯 چالش روزانه"'],
];

let n = 0;
for (const [a, b] of marks) {
  if (s.includes(a)) { s = s.replace(a, b); n++; }
  else console.log('NOT FOUND → ' + a.slice(0, 50).replace(/\n/g, '⏎'));
}
fs.writeFileSync(file, s);
console.log(`replaced ${n}/${marks.length}`);
