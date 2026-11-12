# ☕ چای — اپ تمرکز

> «۳۰ دقیقه تمرکز، یک ماگ چای»

اپ اندرویدی که با انتخاب **هدف** و **مدت**، اپ‌های حواس‌پرت‌کن را قفل می‌کند
(تلگرام، اینستاگرام، یوتیوب…) و در پایان **داشبورد و نمودار** تمرکزت را نشان می‌دهد.

---

## ویژگی‌ها

| بخش | توضیح |
|---|---|
| 🎯 هدف‌محور | «درس خواندن / کتاب / کار / ورزش / خواب» → لیست قفل پیشنهادی خودش |
| 🔒 قفل واقعی | `AccessibilityService` — بدون روت، بدون هک |
| ☕ ماگ چای | انیمیشن پر شدن چای به‌اندازهٔ پیشرفت جلسه |
| 📊 داشبورد | نمودار ستونی ۷ روز، heatmap ۸ هفته، streak، تفکیک بر اساس هدف |
| 🏆 رقابت | لیگ هفتگی (برنز/نقره/طلا)، دوئل، چالش روزانه — **رقابت با عدد، نه با کلمه** |
| 🔑 ورود | نام کاربری + کد بازیابی — بدون رمز، بدون پیامک، بدون ایمیل اجباری |
| 🔍 یکتایی نام | بررسی زندهٔ آزاد بودن نام کاربری + **پیشنهاد نام جایگزین** (تبدیل نام فارسی به لاتین) |
| 👥 بازدید و انگیزه | «همین الان ۱۲۴ نفر مثل تو در حال تمرکز هستند» + شمارندهٔ بازدید روزانه |
| ⚙️ تنظیمات | روشن/تیره/سیستم + دربارهٔ سازنده (ابوالفضل صمدی) |
| 📣 تبلیغ | **فعلاً غیرفعال** — SDK تپسل آمادهٔ فعال‌سازی است (۳ قدم در `ads/AdManager.kt`) |
| 📴 آفلاین | کل دادهٔ جلسات در **یک فایل JSON محلی**؛ همگام‌سازی اختیاری |
| 💸 صفر هزینه | بدون سرور اجباری؛ Supabase Free + بیلد در CI رایگان |

---

## معماری

```
اپ اندروید (Kotlin + Compose)
   ├── SessionStore ......... کش محلی (فایل JSON) — کار بدون اینترنت
   ├── FocusBlockerService .. قفل اپ‌ها (AccessibilityService)
   ├── AuthRepository ....... ورود/همگام‌سازی با Supabase REST
   └── SyncManager .......... push سشن‌های محلی به دیتابیس

Supabase (رایگان)
   ├── Postgres + RLS ....... جدول‌ها و امنیت
   ├── Edge Functions ....... register / login
   └── جدول رتبه‌بندی ....... فقط خواندنی
```

### فایل‌ها

```
app/src/main/java/ir/chai/focus/
├── Models.kt                داده‌ها، هدف‌ها، لیست اپ‌های حواس‌پرت‌کن
├── SessionStore.kt          ذخیرهٔ محلی + محاسبات آماری
├── FocusBlockerService.kt   سرویس قفل
├── SyncManager.kt           همگام‌سازی آفلاین → دیتابیس
├── MainActivity.kt          اسکلت اپ + صفحهٔ خانه
├── SessionScreen.kt         تایمر + ماگ چای (انیمیشن پر شدن)
├── BlockActivity.kt         صفحهٔ بازدارنده
├── ui/
│   ├── Theme.kt             پالت رنگ + تایپوگرافی (Vazirmatn)
│   ├── ChaiBottomBar.kt     نوار پایین (۴ تب مثل اینستا)
│   ├── Charts.kt            نمودار ستونی + heatmap + تفکیک هدف (بدون کتابخانه)
│   └── Screens.kt           آمار · لیگ · پروفایل
├── ads/
│   ├── AdManager.kt         تپسل: بنر · interstitial · rewarded
│   └── TopCornerAd.kt       بنر در گوشهٔ بالا
└── auth/
    ├── AuthRepository.kt    ارتباط با سرور
    └── LoginScreen.kt       فرم ورود + نمایش کد بازیابی + چک نام کاربری

app/src/main/res/
├── font/                    Vazirmatn (Regular/Medium/Bold) — ۵۰۰KB
└── drawable-nodpi/          تصاویر AI (WebP، هرکدام ۵-۱۲KB)

design/mockup.html          ماکت رابط کاربری (باز کن با مرورگر)
tools/gen-icons.ps1         تولید تصاویر اپ با Flux (رایگان)
tools/gen-avatars.ps1       تولید آواتارها

db/schema.sql                جدول‌ها + RLS + تابع weekly_league
supabase/functions/
├── register/index.ts        ساخت حساب + صدور کد بازیابی
├── login/index.ts           ورود با کد بازیابی + امضای JWT
├── check-username/index.ts  بررسی آزاد بودن نام + پیشنهاد جایگزین
└── _shared/util.ts          JWT، هش، تولید کد
```

---

## راه‌اندازی

### ۱. دیتابیس (رایگان)
1. در [supabase.com](https://supabase.com) یک پروژهٔ رایگان بساز
2. در SQL Editor فایل `db/schema.sql` را اجرا کن
3. از Settings → API این‌ها را بردار:
   - `Project URL`
   - `anon public key`
   - `service_role key` (برای Edge Functions)
   - `JWT Secret` (از Settings → API → JWT Secret)

### ۲. Edge Functions
```bash
supabase functions deploy register
supabase functions deploy login
```
(بعداً اگر خواستی ایمیل بازیابی اضافه کنی: یک تابع `forgot` + سرویس ایمیل رایگان مثل Resend/Brevo)

### ۳. پیکربندی اپ
کلیدها را در `AuthRepository.saveConfig(url, anonKey)` بگذار
(یا موقتاً در `SharedPreferences` با کلیدهای `supabase_url` و `supabase_anon_key`).

### ۴. ساخت
```bash
# بیلد محلی (نیاز به Android Studio)
./gradlew assembleDebug

# یا بیلد در CI رایگان (GitHub Actions → فایل .github/workflows/android.yml)
```

---

## مسیر درآمد

| لایه | توضیح |
|---|---|
| تبلیغ (تپسل / ادیوری) | فقط بین جلسه‌ها (نه وسط تمرکز!) |
| اشتراک مایکت (IAP) | «حذف تبلیغ» / «روی حرفه‌ای» — ۸۵٪ سهم توسعه‌دهنده |
| گوگل‌پلی | AdMob + Play Billing (نسخهٔ انگلیسی/دیاسپورا) |

---

## سیستم بازدید و شمارندهٔ کاربران فعال

هدف: **انگیزه** — کاربر ببیند دیگران هم دارند درس می‌خوانند.

```
app/src/main/res/...   Visits.kt          ← شمارندهٔ محلی (باز شدن اپ) + کش
db/schema.sql          active_sessions   ← heartbeat هر کاربر
                       app_visits        ← بازدید روزانه
                       log_visit()       ← ثبت بازدید (RPC بدون نیاز به ورود)
                       active_count()    ← چند نفر همین الان فعال هستند
```

- هر باز شدن اپ: `Visits.registerOpen()` (محلی، همیشه کار می‌کند) + `log_visit()` روی سرور (fire & forget)
- اپ هر ۶۰ ثانیه `active_count()` را می‌پرسد و کش می‌کند
- «کاربر فعال» = heartbeat در ۶ دقیقهٔ اخیر
- بدون سرور هم کار می‌کند (کش آخرین مقدار نمایش داده می‌شود)

## نقشهٔ راه

- [x] اسکلت پروژه + Gradle + CI
- [x] مدل‌ها، ذخیرهٔ محلی، آمار
- [x] سرویس قفل اپ‌ها
- [x] دیتابیس + RLS + Edge Functions
- [x] فرم ورود با کد بازیابی
- [ ] UI اصلی: انتخاب هدف + تایمر + ماگ چای
- [ ] صفحهٔ بازدارنده (BlockActivity)
- [ ] داشبورد با نمودار
- [ ] دوشاخه‌سازی هدف/مدت + انیمیشن
- [ ] SDK تبلیغ + IAP مایکت
- [ ] صفحهٔ پروفایل + لیگ + دوئل
- [ ] انتشار در مایکت / بازار / گوگل‌پلی
