# MyDocx

تطبيق أندرويد (Kotlin + XML) لنشر المقالات والمشاريع البرمجية، مبني على Supabase.

## ✅ الميزات المُنفّذة

| الميزة | أين تجدها |
|---|---|
| إنشاء حساب (الاسم، اسم المستخدم، كلمة السر) | `ui/auth/RegisterActivity.kt` |
| تسجيل دخول (اسم مستخدم + كلمة سر) | `ui/auth/LoginActivity.kt` |
| **مفتاح المرور الأحمر** (PIN من 4 أرقام كعامل ثانٍ) | `ui/auth/PinSetupActivity.kt`, `PinEntryActivity.kt` |
| نشر مقالات | `ui/publish/PublishArticleActivity.kt` |
| نشر مشاريع برمجية + تحميل هيكلي (zip يحافظ على بنية المجلدات) | `ui/publish/PublishProjectActivity.kt` |
| إعجاب / إعادة نشر / إبلاغ | `FeedAdapter.kt` + `ContentRepository.kt` |
| متابعة / متابَعون | `toggleFollow`, `UserListActivity.kt` |
| اشتراك شهري بالبريد الإلكتروني | `ProfileFragment.kt` + جدول `newsletter_subscriptions` + Edge Function |
| ملف شخصي قابل للتخصيص بالكامل (لمحة، شكر خاص، روابط تواصل) | `ProfileFragment.kt`, `EditProfileActivity.kt` |
| أفاتار افتراضي **مرسوم بالكود من الصفر** لكل حساب جديد | `util/AvatarGenerator.kt` |
| تحميل الأيقونة (تصدير الأفاتار كـ PNG) | زر `btnDownloadAvatar` في `ProfileFragment.kt` |
| زر ترجمة (Google Translate) | `util/TranslateHelper.kt` |
| أيقونة التطبيق مصممة من الصفر (Vector) | `res/drawable/ic_launcher_foreground.xml` |
| تصميم مميز (هوية بصرية: ink navy + أحمر إشاري + ذهبي) | `res/values/colors.xml`, `themes.xml` |

## ⚠️ ملاحظة أمان مهمة قرأتها بعناية

أرسلت لي 3 مفاتيح Supabase. استخدمت **فقط**:
- `SUPABASE_URL`
- `SUPABASE_PUBLISHABLE_KEY` (anon key) — هذا آمن تمامًا لوضعه داخل تطبيق الجوال.

**لم أضع `SUPABASE_SECRET_KEY` في أي مكان داخل كود التطبيق أو المستودع.** هذا المفتاح
(service_role) يملك صلاحيات كاملة على قاعدة البيانات متجاوزًا كل قواعد الأمان (RLS)،
ولو تم تضمينه داخل تطبيق أندرويد يمكن لأي شخص استخراجه بفك حزمة الـ APK والوصول الكامل
لقاعدة بياناتك. استخدمته فقط في تعليقات `supabase/edge-functions/` كمثال على أنه
**يُضبط كـ "Function secret" على خوادم Supabase فقط**، وليس كمتغير بيئة في GitHub Actions
أو داخل `gradle.properties`.

## 🚀 الإعداد

### 1) قاعدة البيانات
1. افتح مشروعك على supabase.com → SQL Editor
2. الصق محتوى `supabase/schema.sql` ونفّذه (ينشئ الجداول + RLS + الـ triggers + الـ storage buckets)
3. تأكد أن Authentication → Providers → Email مفعّل (نستخدمه تحت الغطاء، راجع `AuthRepository.kt`)

> ملاحظة تقنية: التطبيق يستخدم اسم مستخدم + كلمة سر فقط في الواجهة، لكن Supabase Auth
> يتطلب بريدًا إلكترونيًا داخليًا، لذلك نولّد بريدًا وهميًا تلقائيًا بصيغة
> `username@mydocx.local` (لا يصل له أي بريد فعلي، هو فقط معرّف داخلي في auth.users).

### 2) مفاتيح المشروع (لا تضعها في الكود مباشرة)
أنشئ ملف `local.properties` في جذر المشروع (هذا الملف موجود في `.gitignore` ولن يُرفع لـ GitHub):
```properties
SUPABASE_URL=https://ikpdizvfpcpderwwxklx.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_wkx_5jrphDjYDnw8h-3QZw_VdM6CA78
```

### 3) GitHub Actions (بناء APK تلقائيًا)
في المستودع: Settings → Secrets and variables → Actions → أضف:
- `SUPABASE_URL`
- `SUPABASE_PUBLISHABLE_KEY`

كل push على `main` يبني APK ويرفعه كـ Artifact (بدون توقيع)، راجع
`.github/workflows/android-ci.yml`. لتوليد ملف `gradlew` (wrapper) محليًا مرة واحدة:
```bash
gradle wrapper --gradle-version 8.7
git add gradlew gradlew.bat gradle/wrapper && git commit -m "add gradle wrapper"
```

### 4) فتح المشروع في Android Studio
افتحه مباشرة (File → Open) بعد إضافة `local.properties`، ثم Run.

## 📁 هيكل المشروع

```
app/src/main/java/com/mydocx/app/
├── model/       # Profile, Article, ProjectPost, Like, Repost, Report, Follow...
├── network/     # SupabaseApi (Retrofit), RetrofitClient, StorageUploader
├── repo/        # AuthRepository, ContentRepository
├── util/        # SessionManager, PinHasher, AvatarGenerator, TranslateHelper
└── ui/
    ├── splash/  auth/  main/  feed/  publish/  profile/  notifications/
supabase/
├── schema.sql
└── edge-functions/send-monthly-newsletter/index.ts
.github/workflows/android-ci.yml
```

## 🔜 نقاط جاهزة للتوسّع (خارج نطاق هذا التسليم الأولي)
- التحقق من "مفتاح المرور الأحمر" حاليًا يتم على الجهاز؛ للإنتاج انقله إلى دالة
  Postgres/Edge Function (pgcrypto) حتى لا يمكن تجاوزه على جهاز مروَّق (rooted).
- شاشة الإشعارات حاليًا عنصر نائب (placeholder) — تحتاج جدول `notifications` + triggers.
- قسم "إعادة النشر" في الملف الشخصي يحتاج ربطه بدالة `getUserReposts(userId)` في
  `ContentRepository.kt` (مسار جاهز، الاستدعاء الفعلي متروك لتفضيلك في تصميم الاستعلام).
- إرسال البريد الشهري الفعلي يحتاج ربط مزوّد بريد حقيقي (Resend/Postmark/SendGrid)
  داخل `send-monthly-newsletter/index.ts` (المكان محدد بتعليق `TODO`).
- لم يتم اختبار البناء الفعلي (لا يوجد Android SDK في بيئة التوليد)؛ أول build فعلي
  سيكون عبر GitHub Actions أو Android Studio عندك.
