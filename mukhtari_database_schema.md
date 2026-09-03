# 📐 مخطط قاعدة بيانات تطبيق «مختاري»

### SQLite + Room --- Offline 100% --- مختار واحد + جهاز واحد

------------------------------------------------------------------------

## 0. القرارات المعمارية النهائية

هذا المخطط مخصص لتطبيق إلكتروني يعمل على جهاز Android واحد ويستخدمه
مختار واحد فقط لإدارة سجلات منطقته.

### القيود الأساسية

-   التطبيق **Offline 100%** ولا يحتاج إلى الإنترنت في أي وظيفة.
-   لا يوجد جدول `Users`.
-   لا توجد أدوار أو صلاحيات أو حسابات متعددة.
-   لا يوجد `INTERNET` permission في التطبيق.
-   لا توجد خرائط، ولا GPS، ولا latitude/longitude، ولا OpenStreetMap،
    ولا Google Maps، ولا MapLibre.
-   الموقع الجغرافي يوصف نصياً فقط:
    -   المحافظة
    -   القضاء
    -   الناحية
    -   المحلة
    -   الشارع
    -   الزقاق
    -   رقم الدار
    -   العنوان التفصيلي
-   الإحصائيات لا تُخزّن كأعمدة؛ تُحسب عند الطلب.
-   الحذف العادي هو Soft Delete.
-   الحذف النهائي لا يتم إلا من سلة المحذوفات وبعد تأكيد إضافي.
-   جميع عمليات الحذف والاستعادة والدمج والاستيراد والاستعادة من النسخة
    الاحتياطية يجب أن تكون Transactional قدر الإمكان.
-   كل البيانات التشغيلية والمرفقات محلية.
-   النسخة الاحتياطية غير مشفرة حسب القرار النهائي، لكن يجب أن تحتوي على
    Manifest وHash للتحقق من سلامتها.
-   بيانات أمان التطبيق (`PIN`، إجابة سؤال الأمان، وإعدادات المصادقة
    الحساسة) لا تُدرج في النسخة الاحتياطية.
-   Room هو طبقة الوصول إلى SQLite.
-   `INTEGER PRIMARY KEY AUTOINCREMENT` هو المفتاح الداخلي.
-   يمكن استخدام `public_code` للرموز المعروضة للمستخدم، ويجب أن يكون
    فريداً حيث يستخدم.
-   جميع التواريخ التي تمثل **تاريخاً فقط** يجب التعامل معها كتاريخ محلي
    بدون الاعتماد على منطقة زمنية متغيرة. يفضّل تمثيلها في قاعدة البيانات
    كـ `TEXT` بصيغة `YYYY-MM-DD`. أما الطوابع الزمنية الفعلية مثل
    `created_at` فتستخدم `INTEGER` بصيغة Unix epoch millis.
-   كل جداول البيانات الرئيسية تحتوي على:
    -   `is_deleted`
    -   `deleted_at`
    -   `deleted_reason`
    -   `created_at`
    -   `updated_at`

------------------------------------------------------------------------

# 1. الهيكل الإداري للمنطقة

## 1.1 `regions`

يمثل المنطقة الإدارية التي يعمل ضمنها المختار.

  الحقل              النوع          القيود / الملاحظات
  ------------------ -------------- --------------------
  `id`               INTEGER        PK AUTOINCREMENT
  `public_code`      TEXT           UNIQUE
  `governorate`      TEXT           NOT NULL
  `district`         TEXT           NOT NULL
  `sub_district`     TEXT           NOT NULL
  `mahalla`          TEXT           NOT NULL
  `name`             TEXT           NOT NULL
  `description`      TEXT NULL      
  `notes`            TEXT NULL      
  `is_deleted`       INTEGER        NOT NULL DEFAULT 0
  `deleted_at`       INTEGER NULL   
  `deleted_reason`   TEXT NULL      
  `created_at`       INTEGER        NOT NULL
  `updated_at`       INTEGER        NOT NULL

> التطبيق عملياً مخصص لمنطقة المختار، ويمكن أن يحتوي على سجل منطقة فعّال
> واحد. لا توجد حاجة إلى دعم مستخدمين متعددين.

------------------------------------------------------------------------

## 1.2 `streets`

  الحقل              النوع          القيود / الملاحظات
  ------------------ -------------- -----------------------------
  `id`               INTEGER        PK AUTOINCREMENT
  `region_id`        INTEGER        FK → `regions.id`, NOT NULL
  `public_code`      TEXT           UNIQUE
  `name`             TEXT           NOT NULL
  `code`             TEXT NULL      رمز الشارع إن وجد
  `description`      TEXT NULL      
  `notes`            TEXT NULL      
  `is_deleted`       INTEGER        NOT NULL DEFAULT 0
  `deleted_at`       INTEGER NULL   
  `deleted_reason`   TEXT NULL      
  `created_at`       INTEGER        NOT NULL
  `updated_at`       INTEGER        NOT NULL

### Indexes

-   `(region_id)`
-   `(name)`
-   `(is_deleted)`

------------------------------------------------------------------------

## 1.3 `alleys`

  الحقل              النوع          القيود / الملاحظات
  ------------------ -------------- -----------------------------
  `id`               INTEGER        PK AUTOINCREMENT
  `street_id`        INTEGER        FK → `streets.id`, NOT NULL
  `public_code`      TEXT           UNIQUE
  `name`             TEXT           NOT NULL
  `code`             TEXT NULL      
  `description`      TEXT NULL      
  `notes`            TEXT NULL      
  `is_deleted`       INTEGER        NOT NULL DEFAULT 0
  `deleted_at`       INTEGER NULL   
  `deleted_reason`   TEXT NULL      
  `created_at`       INTEGER        NOT NULL
  `updated_at`       INTEGER        NOT NULL

### Indexes

-   `(street_id)`
-   `(name)`
-   `(is_deleted)`

------------------------------------------------------------------------

# 2. الدور

## 2.1 `houses`

يمثل الدار أو العقار المسجل في منطقة المختار.

  الحقل                النوع          القيود / الملاحظات
  -------------------- -------------- ----------------------------
  `id`                 INTEGER        PK AUTOINCREMENT
  `public_code`        TEXT           UNIQUE
  `internal_number`    TEXT           NOT NULL, UNIQUE
  `house_number`       TEXT           NOT NULL
  `alley_id`           INTEGER NULL   FK → `alleys.id`
  `street_id`          INTEGER NULL   FK → `streets.id`
  `mahalla_number`     TEXT NULL      
  `detailed_address`   TEXT NULL      
  `photo_path`         TEXT NULL      مسار الصورة الرئيسية محلياً
  `property_type`      TEXT           NOT NULL
  `status`             TEXT           NOT NULL
  `ownership_type`     TEXT           NOT NULL
  `owner_name`         TEXT NULL      
  `owner_phone`        TEXT NULL      
  `notes`              TEXT NULL      
  `is_deleted`         INTEGER        NOT NULL DEFAULT 0
  `deleted_at`         INTEGER NULL   
  `deleted_reason`     TEXT NULL      
  `created_at`         INTEGER        NOT NULL
  `updated_at`         INTEGER        NOT NULL

### `property_type`

قيم مضبوطة:

-   `residential`
-   `commercial`
-   `mixed`
-   `under_construction`
-   `other`

### `status`

-   `occupied`
-   `vacant`
-   `abandoned`

### `ownership_type`

-   `owned`
-   `rented`
-   `unspecified`

### قواعد العلاقة بين `street_id` و`alley_id`

لا يجوز أن يشير الدار إلى زقاق تابع لشارع مختلف عن `street_id`.

الحالات المسموحة:

1.  `alley_id IS NULL` و`street_id` موجود: دار مرتبطة مباشرة بالشارع.
2.  `alley_id` موجود و`street_id` موجود ويجب أن يكون الشارع هو الشارع
    الأب للزقاق.
3.  `alley_id` موجود و`street_id IS NULL`: لا يُفضّل السماح بها في طبقة
    Repository؛ يجب تحديد الشارع من الزقاق وتعبئته تلقائياً.

### ملاحظة عن رقم الدار

لا نفرض `UNIQUE` على `house_number` وحده، لأن رقم الدار قد يتكرر في أزقة
أو شوارع مختلفة.

------------------------------------------------------------------------

# 3. العوائل

## 3.1 `families`

يمثل عائلة مسجلة رسمياً ضمن سجلات المختار.

  الحقل                 النوع          القيود / الملاحظات
  --------------------- -------------- --------------------
  `id`                  INTEGER        PK AUTOINCREMENT
  `public_code`         TEXT           UNIQUE
  `family_code`         TEXT           UNIQUE, NOT NULL
  `family_name`         TEXT NULL      
  `house_id`            INTEGER NULL   FK → `houses.id`
  `head_of_family_id`   INTEGER NULL   FK → `persons.id`
  `residency_date`      TEXT NULL      `YYYY-MM-DD`
  `residency_status`    TEXT           NOT NULL
  `info_source`         TEXT NULL      
  `notes`               TEXT NULL      
  `is_deleted`          INTEGER        NOT NULL DEFAULT 0
  `deleted_at`          INTEGER NULL   
  `deleted_reason`      TEXT NULL      
  `created_at`          INTEGER        NOT NULL
  `updated_at`          INTEGER        NOT NULL

### `residency_status`

-   `resident`
-   `incoming`
-   `displaced`
-   `returnee`
-   `other`

### قواعد مهمة

-   `head_of_family_id` اختياري.
-   لا يجوز أن يكون رب الأسرة شخصاً محذوفاً.
-   يجب أن يكون رب الأسرة مرتبطاً بالعائلة نفسها.
-   لا يتم إنشاء رب الأسرة في نفس لحظة إنشاء العائلة اعتماداً على FK
    دائري؛ يتم إنشاء العائلة أولاً، ثم الشخص، ثم تحديث
    `head_of_family_id` داخل Transaction واحدة.
-   لا يوجد عمود `member_count`; عدد الأفراد يحسب بـ `COUNT`.

------------------------------------------------------------------------

# 4. الأفراد

## 4.1 `persons`

يمثل الشخص سواء كان عضواً في عائلة أو شخصاً مستقلاً بلا عائلة رسمية.

  الحقل                النوع          القيود / الملاحظات
  -------------------- -------------- --------------------
  `id`                 INTEGER        PK AUTOINCREMENT
  `public_code`        TEXT           UNIQUE
  `full_name`          TEXT           NOT NULL
  `father_name`        TEXT NULL      
  `grandfather_name`   TEXT NULL      
  `surname`            TEXT NULL      
  `gender`             TEXT           NOT NULL
  `birth_date`         TEXT NULL      `YYYY-MM-DD`
  `marital_status`     TEXT           NOT NULL
  `relation_to_head`   TEXT NULL      قيمة مضبوطة
  `family_id`          INTEGER NULL   FK → `families.id`
  `house_id`           INTEGER NULL   FK → `houses.id`
  `work_status`        TEXT           NOT NULL
  `employer`           TEXT NULL      
  `job_title`          TEXT NULL      
  `education_level`    TEXT           NOT NULL
  `phone`              TEXT NULL      
  `phone_alt`          TEXT NULL      
  `notes`              TEXT NULL      
  `is_deleted`         INTEGER        NOT NULL DEFAULT 0
  `deleted_at`         INTEGER NULL   
  `deleted_reason`     TEXT NULL      
  `created_at`         INTEGER        NOT NULL
  `updated_at`         INTEGER        NOT NULL

### `gender`

-   `male`
-   `female`

### `relation_to_head`

قائمة منظمة وليست نصاً حراً:

-   `head`
-   `husband`
-   `wife`
-   `son`
-   `daughter`
-   `father`
-   `mother`
-   `brother`
-   `sister`
-   `grandfather`
-   `grandmother`
-   `grandson`
-   `granddaughter`
-   `relative`
-   `other`
-   `unknown`

### `work_status`

-   `gov_employee`
-   `private_employee`
-   `self_employed`
-   `business_owner`
-   `student`
-   `retired`
-   `unemployed`
-   `housewife`
-   `other`

### `education_level`

-   `illiterate`
-   `primary`
-   `intermediate`
-   `secondary`
-   `diploma`
-   `bachelor`
-   `higher_diploma`
-   `master`
-   `doctorate`
-   `other`
-   `unknown`

### `marital_status`

-   `single`
-   `married`
-   `divorced`
-   `widowed`
-   `unknown`

### قواعد السكن

#### شخص مرتبط بعائلة

إذا:

``` text
family_id IS NOT NULL
```

فإن السكن الحالي للشخص يجب أن يتوافق مع سكن عائلته.

لا يسمح Repository بحفظ:

``` text
person.family_id = Family A
person.house_id = House B
```

إذا كانت العائلة الحالية في House A.

#### شخص بلا عائلة رسمية

يسمح:

``` text
family_id = NULL
house_id = House X
```

وهذا يلبي حالة الأشخاص الذين لا توجد لهم عائلة رسمية مسجلة.

#### شخص بلا عائلة وبلا سكن مسجل

يسمح:

``` text
family_id = NULL
house_id = NULL
```

للحالات غير المكتملة أو التي لم يحدد سكنها بعد.

------------------------------------------------------------------------

# 5. تاريخ السكن

## 5.1 `residencies`

يمثل **فترة سكن فعلية** بدلاً من تسجيل حدث وصول/مغادرة منفصل.

  الحقل                     النوع          القيود / الملاحظات
  ------------------------- -------------- -----------------------------
  `id`                      INTEGER        PK AUTOINCREMENT
  `person_id`               INTEGER        FK → `persons.id`, NOT NULL
  `family_id`               INTEGER NULL   FK → `families.id`
  `house_id`                INTEGER NULL   FK → `houses.id`
  `start_date`              TEXT           NOT NULL, `YYYY-MM-DD`
  `end_date`                TEXT NULL      `YYYY-MM-DD`
  `residency_type`          TEXT           NOT NULL
  `verification_status`     TEXT           NOT NULL
  `reason`                  TEXT NULL      
  `previous_address_text`   TEXT NULL      
  `notes`                   TEXT NULL      
  `created_at`              INTEGER        NOT NULL
  `updated_at`              INTEGER        NOT NULL

### `residency_type`

-   `resident`
-   `incoming`
-   `returnee`
-   `other`

### `verification_status`

-   `pending`
-   `verified`
-   `needs_review`

### قواعد السكن

-   `start_date <= end_date` إذا كان `end_date` موجوداً.
-   وجود `end_date IS NULL` يعني أن هذه هي فترة السكن الحالية.
-   لا يجوز وجود أكثر من فترة سكن حالية واحدة للشخص.
-   عند تسجيل سكن جديد:
    1.  تغلق الفترة الحالية بوضع `end_date`.
    2.  تنشأ الفترة الجديدة.
    3.  يتم تحديث `persons.house_id` إذا كان الشخص مستقلاً.
    4.  إذا كان الشخص ضمن عائلة، يتم التحقق من تطابق الدار مع العائلة.
    5.  كل ذلك داخل Transaction واحدة.
-   العنوان السابق خارج منطقة المختار يخزن في `previous_address_text`.
-   إذا كانت الوجهة أو الدار خارج المنطقة، يمكن استخدام النص عند الحاجة.

### شاشات الوافدين والمغادرين

لا توجد جداول منفصلة للوافدين والمغادرين.

-   الوافدون = فترات سكن ذات `residency_type = incoming` أو حسب تاريخ
    بداية السكن.
-   المغادرون = فترات تحتوي على `end_date` خلال الفترة المطلوبة.

------------------------------------------------------------------------

# 6. المعاملات

## 6.1 `transactions`

  الحقل                       النوع          القيود / الملاحظات
  --------------------------- -------------- ----------------------------
  `id`                        INTEGER        PK AUTOINCREMENT
  `transaction_code`          TEXT           UNIQUE, NOT NULL
  `transaction_type`          TEXT           NOT NULL
  `person_id`                 INTEGER NULL   FK → `persons.id`
  `family_id`                 INTEGER NULL   FK → `families.id`
  `applicant_name_snapshot`   TEXT NULL      اسم مقدم الطلب وقت التسجيل
  `request_date`              TEXT           NOT NULL, `YYYY-MM-DD`
  `subject`                   TEXT           NOT NULL
  `details`                   TEXT NULL      
  `status`                    TEXT           NOT NULL
  `notes`                     TEXT NULL      
  `is_deleted`                INTEGER        NOT NULL DEFAULT 0
  `deleted_at`                INTEGER NULL   
  `deleted_reason`            TEXT NULL      
  `created_at`                INTEGER        NOT NULL
  `updated_at`                INTEGER        NOT NULL

### `status`

-   `new`
-   `in_progress`
-   `pending_info`
-   `completed`
-   `cancelled`
-   `archived`

> لا نخزن اسم الشخص الحالي باعتباره المصدر الوحيد؛ يتم ربط المعاملة بـ
> `person_id` أو `family_id` عند توفرهما، بينما
> `applicant_name_snapshot` يحافظ على الاسم وقت التسجيل.

------------------------------------------------------------------------

# 7. تأييد السكن

## 7.1 `residency_certificates`

  الحقل                النوع       القيود / الملاحظات
  -------------------- ----------- --------------------------------
  `id`                 INTEGER     PK AUTOINCREMENT
  `transaction_id`     INTEGER     FK → `transactions.id`, UNIQUE
  `person_id`          INTEGER     FK → `persons.id`
  `snapshot_name`      TEXT        NOT NULL
  `snapshot_family`    TEXT NULL   
  `snapshot_house`     TEXT NULL   
  `snapshot_address`   TEXT NULL   
  `pdf_path`           TEXT NULL   مسار محلي
  `issued_at`          INTEGER     NOT NULL
  `created_at`         INTEGER     NOT NULL

### قاعدة مهمة

الـ Snapshot لا يتغير بعد إصدار المستند.

إذا تغير:

-   اسم الشخص
-   العائلة
-   الدار
-   العنوان

فالمستند الصادر سابقاً يبقى كما كان وقت الإصدار.

------------------------------------------------------------------------

# 8. الوارد

## 8.1 `incoming_letters`

  الحقل               النوع          القيود / الملاحظات
  ------------------- -------------- ------------------------
  `id`                INTEGER        PK AUTOINCREMENT
  `public_code`       TEXT           UNIQUE
  `letter_number`     TEXT           NOT NULL
  `letter_date`       TEXT           NOT NULL, `YYYY-MM-DD`
  `sender`            TEXT           NOT NULL
  `subject`           TEXT           NOT NULL
  `details`           TEXT NULL      
  `required_action`   TEXT NULL      
  `status`            TEXT           NOT NULL
  `action_date`       TEXT NULL      `YYYY-MM-DD`
  `notes`             TEXT NULL      
  `is_deleted`        INTEGER        NOT NULL DEFAULT 0
  `deleted_at`        INTEGER NULL   
  `deleted_reason`    TEXT NULL      
  `created_at`        INTEGER        NOT NULL
  `updated_at`        INTEGER        NOT NULL

### `status`

-   `new`
-   `in_progress`
-   `done`
-   `postponed`
-   `archived`

> لا يوجد `attachment_paths` داخل الجدول؛ جميع المرفقات تدار من جدول
> `attachments`.

------------------------------------------------------------------------

# 9. الصادر

## 9.1 `outgoing_letters`

  الحقل               النوع          القيود / الملاحظات
  ------------------- -------------- ------------------------
  `id`                INTEGER        PK AUTOINCREMENT
  `public_code`       TEXT           UNIQUE
  `letter_number`     TEXT           NOT NULL
  `letter_date`       TEXT           NOT NULL, `YYYY-MM-DD`
  `recipient`         TEXT           NOT NULL
  `subject`           TEXT           NOT NULL
  `details`           TEXT NULL      
  `recipient_name`    TEXT NULL      
  `delivery_date`     TEXT NULL      `YYYY-MM-DD`
  `delivery_method`   TEXT NULL      
  `notes`             TEXT NULL      
  `is_deleted`        INTEGER        NOT NULL DEFAULT 0
  `deleted_at`        INTEGER NULL   
  `deleted_reason`    TEXT NULL      
  `created_at`        INTEGER        NOT NULL
  `updated_at`        INTEGER        NOT NULL

> لا يوجد `attachment_paths` داخل الجدول؛ المرفقات في `attachments`.

------------------------------------------------------------------------

# 10. سجل المراجعين

## 10.1 `visitors_log`

  الحقل                النوع       القيود / الملاحظات
  -------------------- ----------- ------------------------
  `id`                 INTEGER     PK AUTOINCREMENT
  `visitor_name`       TEXT        NOT NULL
  `phone`              TEXT NULL   
  `visit_reason`       TEXT        NOT NULL
  `transaction_type`   TEXT NULL   
  `visit_date`         TEXT        NOT NULL, `YYYY-MM-DD`
  `visit_time`         TEXT NULL   `HH:mm`
  `result`             TEXT NULL   
  `notes`              TEXT NULL   
  `created_at`         INTEGER     NOT NULL

> لا يوجد `handled_by` لأن التطبيق لمستخدم واحد فقط.

------------------------------------------------------------------------

# 11. المرفقات والملفات

## 11.1 `attachments`

المرفقات لا تخزن داخل SQLite كـ BLOB. SQLite يحتفظ بالبيانات الوصفية
ومسار الملف المحلي.

  الحقل              النوع          القيود / الملاحظات
  ------------------ -------------- --------------------
  `id`               INTEGER        PK AUTOINCREMENT
  `owner_type`       TEXT           NOT NULL
  `owner_id`         INTEGER        NOT NULL
  `file_type`        TEXT           NOT NULL
  `mime_type`        TEXT NULL      
  `file_path`        TEXT           NOT NULL
  `file_name`        TEXT           NOT NULL
  `file_size`        INTEGER NULL   بالبايت
  `notes`            TEXT NULL      
  `is_deleted`       INTEGER        NOT NULL DEFAULT 0
  `deleted_at`       INTEGER NULL   
  `deleted_reason`   TEXT NULL      
  `created_at`       INTEGER        NOT NULL
  `updated_at`       INTEGER        NOT NULL

### `owner_type`

-   `house`
-   `family`
-   `person`
-   `transaction`
-   `incoming_letter`
-   `outgoing_letter`

### `file_type`

-   `image`
-   `pdf`
-   `document`

### Index

``` text
(owner_type, owner_id, is_deleted)
```

### قاعدة مهمة

`owner_id` مع `owner_type` يمثلان علاقة Polymorphic، لذلك لا يستطيع
SQLite فرض FK تقليدي واحد عليهما.

لهذا يجب أن تتولى طبقة Repository:

-   التحقق من وجود المالك.
-   منع إنشاء مرفق لمالك غير موجود.
-   Soft Delete للمرفق عند حذف المالك.
-   Restore للمرفق عند الاستعادة وفق قواعد الاستعادة.
-   حذف الملف الفعلي فقط عندما يصبح من المؤكد أنه لم يعد مطلوباً.

------------------------------------------------------------------------

# 12. الصور الرئيسية للدور

`houses.photo_path` يسمح بتحديد صورة رئيسية للدار.

إذا كان المطلوب لاحقاً أكثر من صورة للدار، تستخدم `attachments` بدلاً من
إضافة أعمدة صور جديدة.

------------------------------------------------------------------------

# 13. سجل النشاط والتدقيق

## 13.1 `activity_log`

  الحقل           النوع          القيود / الملاحظات
  --------------- -------------- --------------------
  `id`            INTEGER        PK AUTOINCREMENT
  `action_type`   TEXT           NOT NULL
  `entity_type`   TEXT           NOT NULL
  `entity_id`     INTEGER NULL   
  `description`   TEXT           NOT NULL
  `old_values`    TEXT NULL      JSON محلي
  `new_values`    TEXT NULL      JSON محلي
  `timestamp`     INTEGER        NOT NULL

### `action_type`

-   `create`
-   `update`
-   `delete`
-   `restore`
-   `merge`
-   `export`
-   `import`
-   `backup`
-   `restore_backup`
-   `settings_change`

### قواعد

-   لا يوجد إدخال يدوي من المستخدم.
-   Repository هو المسؤول عن تسجيل العمليات.
-   لا يستخدم Soft Delete.
-   عمليات الدمج يجب تسجيلها.
-   عمليات الاستيراد والاستعادة من Backup يجب تسجيلها.

------------------------------------------------------------------------

# 14. دمج الأشخاص المكررين

## 14.1 `person_merge_log`

لضمان عدم فقدان أثر الدمج:

  الحقل                النوع
  -------------------- --------------------------
  `id`                 INTEGER PK AUTOINCREMENT
  `source_person_id`   INTEGER FK → persons.id
  `target_person_id`   INTEGER FK → persons.id
  `merged_at`          INTEGER
  `reason`             TEXT NULL
  `details`            TEXT NULL

### عملية الدمج

1.  اختيار الشخص الأساسي.
2.  اختيار الشخص المكرر.
3.  عرض الاختلافات.
4.  تأكيد المستخدم.
5.  نقل الروابط:
    -   `transactions`
    -   `residencies`
    -   `attachments`
    -   العلاقات الأخرى التي تعتمد على الشخص.
6.  معالجة البيانات المتعارضة وفق اختيار المستخدم.
7.  تسجيل العملية في `person_merge_log`.
8.  تسجيلها في `activity_log`.
9.  Soft Delete للشخص المكرر.
10. لا يتم حذف بياناته التاريخية.

------------------------------------------------------------------------

# 15. استيراد Excel / CSV

## 15.1 `import_staging`

جدول مؤقت لمعالجة ملفات الاستيراد قبل إدخالها إلى الجداول الحقيقية.

  الحقل                    النوع
  ------------------------ --------------------------
  `id`                     INTEGER PK AUTOINCREMENT
  `import_session_id`      TEXT
  `row_number`             INTEGER
  `source_file_name`       TEXT
  `raw_data_json`          TEXT
  `normalized_data_json`   TEXT NULL
  `validation_status`      TEXT
  `validation_errors`      TEXT NULL
  `match_status`           TEXT NULL
  `matched_entity_id`      INTEGER NULL
  `created_at`             INTEGER

### مراحل الاستيراد

``` text
Excel / CSV
    ↓
قراءة محلية
    ↓
تحديد الأعمدة
    ↓
import_staging
    ↓
تنظيف وتطبيع النص
    ↓
Validation
    ↓
كشف التكرار
    ↓
مطابقة الشوارع والأزقة
    ↓
معاينة المستخدم
    ↓
تأكيد
    ↓
Transaction
    ↓
الجداول الحقيقية
```

### المطابقة

المطابقة تتم محلياً فقط.

لا يوجد:

-   API
-   خدمة سحابية
-   AI خارجي
-   اتصال إنترنت

ويجب دعم التطبيع العربي عند البحث والمطابقة، مثل إزالة التشكيل والتطويل
وتوحيد أشكال بعض الحروف العربية، مع إبقاء القيمة الأصلية كما أدخلها
المستخدم.

------------------------------------------------------------------------

# 16. سلة المحذوفات

لا نحتاج جدولاً منفصلاً لسلة المحذوفات.

كل سجل رئيسي يستخدم:

``` text
is_deleted
deleted_at
deleted_reason
```

وتعرض Repository موحدة باسم منطقي مثل:

``` text
DeletedItem
```

وتجمع السجلات من:

-   houses
-   families
-   persons
-   transactions
-   incoming_letters
-   outgoing_letters
-   attachments

### الاستعادة

عند الاستعادة:

-   يعاد `is_deleted = 0`.
-   يسجل `restore` في `activity_log`.
-   لا تتم استعادة طفل تم حذفه بشكل مستقل لمجرد استعادة الأب.

### الحذف النهائي

يتطلب:

1.  فتح سلة المحذوفات.
2.  اختيار السجل.
3.  عرض العلاقات المرتبطة.
4.  تحذير واضح.
5.  تأكيد نهائي.
6.  حذف قاعدة البيانات المرتبطة وفق قواعد العلاقات.
7.  حذف الملفات الفعلية التي لم تعد مرتبطة بأي سجل.
8.  تسجيل العملية قبل إتمامها أو ضمن آلية تدقيق مناسبة.

------------------------------------------------------------------------

# 17. قواعد حذف السجلات الأب

## حذف دار

لا تحذف الدار مباشرة.

يجب فحص:

-   العوائل المرتبطة.
-   الأشخاص المستقلين.
-   فترات السكن.
-   المرفقات.
-   أي معاملات مرتبطة بشكل مباشر أو غير مباشر.

ثم تنفيذ Soft Delete داخل Transaction.

## حذف عائلة

يجب عدم حذف الأشخاص تلقائياً كحذف نهائي.

يمكن:

-   إبقاء الأشخاص مع `family_id = NULL` إذا كانت هذه النتيجة مقصودة.
-   أو نقلهم إلى حالة مناسبة.
-   أو إلغاء العملية إذا كانت البيانات لا تسمح بذلك.

لا يجوز اتخاذ قرار تدميري تلقائي بدون تأكيد.

------------------------------------------------------------------------

# 18. المفاتيح والفهارس

## Unique

يجب أن تكون القيم التالية فريدة حيث تنطبق:

-   `regions.public_code`
-   `streets.public_code`
-   `alleys.public_code`
-   `houses.public_code`
-   `houses.internal_number`
-   `families.public_code`
-   `families.family_code`
-   `persons.public_code`
-   `transactions.transaction_code`
-   `incoming_letters.public_code`
-   `outgoing_letters.public_code`

### ملاحظة

`house_number` و`letter_number` لا يفرضان UNIQUE على مستوى الجدول إلا
إذا كانت قاعدة العمل المحلية تفرض ذلك.

------------------------------------------------------------------------

# 19. فهارس البحث

يجب إضافة فهارس على الحقول المستخدمة بكثرة:

### Houses

``` text
status
alley_id
street_id
is_deleted
```

### Families

``` text
house_id
residency_status
is_deleted
```

### Persons

``` text
family_id
house_id
full_name
is_deleted
```

### Residencies

``` text
person_id
family_id
house_id
start_date
end_date
residency_type
```

### Transactions

``` text
person_id
family_id
request_date
status
transaction_type
```

### Letters

``` text
letter_date
status
letter_number
```

### Attachments

``` text
(owner_type, owner_id, is_deleted)
```

------------------------------------------------------------------------

# 20. البحث العربي

لأن أسماء السكان عربية، يجب ألا يعتمد البحث على المطابقة الحرفية فقط.

يُنشأ تطبيع محلي للأسماء عند الحاجة، مثل:

``` text
إزالة التشكيل
إزالة التطويل
توحيد أشكال الألف
توحيد الياء / الألف المقصورة حسب سياسة البحث
توحيد بعض أشكال الهمزة
إزالة المسافات الزائدة
```

لكن:

> لا يتم استبدال الاسم الأصلي.

القيمة الأصلية تبقى كما أدخلها المختار، والتطبيع يستخدم فقط للبحث
والمطابقة.

يمكن لاحقاً استخدام SQLite FTS5 إذا أثبت الاختبار الفعلي أنه مناسب للبحث
العربي المطلوب.

------------------------------------------------------------------------

# 21. الإحصائيات

لا توجد أعمدة مثل:

``` text
member_count
population_count
house_count
```

بل تحسب عند الطلب.

أمثلة:

``` text
عدد الدور
عدد الدور المشغولة
عدد الدور الفارغة
عدد العوائل
عدد الأفراد
عدد الذكور
عدد الإناث
عدد الوافدين
عدد المغادرين
عدد المعاملات
عدد المعاملات المكتملة
عدد الوارد
عدد الصادر
```

ويجب أن تستبعد السجلات المحذوفة من الإحصائيات العادية.

------------------------------------------------------------------------

# 22. إعدادات التطبيق

## 22.1 `app_settings`

  الحقل     النوع
  --------- ---------
  `key`     TEXT PK
  `value`   TEXT

إعدادات غير حساسة مثل:

``` text
theme_mode
auto_lock_duration
```

يمكن تخزينها هنا أو في DataStore.

------------------------------------------------------------------------

# 23. إعدادات الأمان

إعدادات القفل الحساسة لا تعتمد على قاعدة البيانات الرئيسية وحدها.

### المطلوب

-   PIN لا يخزن كنص.
-   استخدام KDF مناسب مثل PBKDF2 أو آلية Android حديثة مناسبة.
-   لا يتم تخزين PIN نفسه.
-   إجابة سؤال الأمان لا تخزن كنص.
-   إعدادات المصادقة الحساسة تبقى محلية على الجهاز.
-   `biometric_enabled` يستخدم فقط إذا كان الجهاز يدعم المصادقة الحيوية.
-   `auto_lock_duration` يحدد مدة القفل التلقائي.

### الاسترداد المحلي

بما أن التطبيق Offline:

``` text
PIN منسي
 ↓
سؤال الأمان
 ↓
التحقق المحلي
 ↓
إعادة ضبط PIN
```

وإذا فشل الاسترداد، يمكن توفير خيار **مسح بيانات التطبيق بالكامل** فقط
بعد تحذيرات متعددة وواضحة.

------------------------------------------------------------------------

# 24. النسخة الاحتياطية

## 24.1 محتوى Backup

يجب أن تشمل النسخة:

``` text
Database
Attachments
Images
PDFs
Documents
Backup Manifest
File Hashes
Schema Version
App Version
Creation Timestamp
```

### لا تشمل

``` text
PIN
Security Answer
Biometric Authentication Secrets
```

### Manifest

يحتوي على معلومات مثل:

``` text
backup_format_version
app_version
database_schema_version
created_at
database_hash
attachment_count
record_counts
```

------------------------------------------------------------------------

# 25. سلامة Backup

عدم تشفير النسخة الاحتياطية **لا يعني عدم التحقق منها**.

يجب حساب Hash للملفات المهمة.

عند الاستعادة:

``` text
اختيار Backup
      ↓
فحص Manifest
      ↓
فحص Schema Version
      ↓
فحص Hash
      ↓
فحص الملفات
      ↓
عرض ملخص المحتوى
      ↓
تأكيد
      ↓
استعادة
```

إذا فشل التحقق:

> لا تتم الاستعادة.

------------------------------------------------------------------------

# 26. الاستعادة من Backup

الاستعادة يجب ألا تكتب فوق قاعدة البيانات الحالية مباشرة.

الآلية:

``` text
Backup
 ↓
Temporary Directory
 ↓
Temporary Database
 ↓
Validation
 ↓
Integrity Check
 ↓
Verify Attachments
 ↓
User Confirmation
 ↓
Atomic Replacement / Transactional Restore
```

إذا حدث خطأ أثناء الاستعادة، لا يجب أن ينتهي التطبيق بقاعدة بيانات نصف
مستعادة.

------------------------------------------------------------------------

# 27. العلاقات النهائية

``` text
regions
   │
   └── streets
          │
          └── alleys
                 │
                 └── houses
```

``` text
houses
   │
   └── families
          │
          └── persons
```

``` text
persons
   │
   └── residencies
          │
          └── houses
```

``` text
persons / families
        │
        └── transactions
                │
                └── residency_certificates
```

``` text
houses
families
persons
transactions
incoming_letters
outgoing_letters
        │
        └── attachments
```

``` text
persons
   │
   └── person_merge_log
```

``` text
كل عمليات التطبيق
        │
        └── activity_log
```

------------------------------------------------------------------------

# 28. Foreign Keys

يجب تفعيل SQLite Foreign Keys.

العلاقات الرئيسية:

``` text
streets.region_id → regions.id
alleys.street_id → streets.id
houses.alley_id → alleys.id
houses.street_id → streets.id
families.house_id → houses.id
families.head_of_family_id → persons.id
persons.family_id → families.id
persons.house_id → houses.id
residencies.person_id → persons.id
residencies.family_id → families.id
residencies.house_id → houses.id
transactions.person_id → persons.id
transactions.family_id → families.id
residency_certificates.transaction_id → transactions.id
residency_certificates.person_id → persons.id
person_merge_log.source_person_id → persons.id
person_merge_log.target_person_id → persons.id
```

### سياسة الحذف

بما أن التطبيق يعتمد Soft Delete، لا ينبغي الاعتماد على
`ON DELETE CASCADE` للحذف العادي.

الحذف النهائي يجب أن يتم من Repository وبعملية صريحة ومدروسة.

------------------------------------------------------------------------

# 29. قواعد Transaction

العمليات المركبة يجب أن تكون Transaction واحدة.

أمثلة:

### إضافة عائلة مع رب الأسرة

``` text
Create Family
Create Person
Update Family.head_of_family_id
Create Residency
Update Person
Commit
```

### نقل شخص

``` text
Close Current Residency
Create New Residency
Update Current House
Commit
```

### حذف دار

``` text
Validate Dependencies
Soft Delete House
Soft Delete / Update Related Records حسب القاعدة
Soft Delete Attachments
Create Activity Log
Commit
```

### دمج شخصين

``` text
Validate
Move Relations
Resolve Conflicts
Create Merge Log
Soft Delete Duplicate
Create Activity Log
Commit
```

------------------------------------------------------------------------

# 30. عدم الاتصال بالإنترنت

التطبيق يجب ألا يحتوي على:

``` text
INTERNET permission
```

ولا يستخدم خدمات تحتاج اتصالاً خارجياً.

ممنوع ضمن التصميم:

-   Google Maps
-   Google Places
-   OpenStreetMap
-   MapLibre
-   Firebase
-   Remote API
-   Cloud Backup
-   Cloud OCR
-   Cloud AI
-   Remote Analytics

كل العمليات:

``` text
Local Android Device
        ↓
Room / SQLite
        ↓
Local Files
```

------------------------------------------------------------------------

# 31. الملفات والتخزين

المرفقات تحفظ في مجلدات التطبيق المحلية المنظمة، مثلاً:

``` text
attachments/
    houses/
    families/
    persons/
    transactions/
    incoming/
    outgoing/
```

ويحتفظ SQLite بالمسار والبيانات الوصفية فقط.

عند تصدير Backup:

``` text
database
+
attachments/
```

يجب الحفاظ على العلاقة بينهما.

------------------------------------------------------------------------

# 32. إصدارات قاعدة البيانات

لا نخزن `db_schema_version` باعتباره المصدر الأساسي للحقيقة.

المصدر الأساسي هو:

``` text
Room Database version
+
Room Migration
```

يمكن وضع نسخة داخل Backup Manifest لأغراض التحقق.

كل تغيير في Schema يجب أن يصاحبه Migration واضحة.

------------------------------------------------------------------------

# 33. قواعد Clean Architecture

التقسيم المقترح:

``` text
UI
 ↓
ViewModel
 ↓
Use Cases
 ↓
Repository
 ↓
Room DAO
 ↓
SQLite
```

والمرفقات:

``` text
Use Case
 ↓
Attachment Repository
 ↓
Local File Storage
```

ولا يسمح للواجهة بالوصول المباشر إلى SQLite أو نظام الملفات.

------------------------------------------------------------------------

# 34. قواعد Repository

Repository هو المسؤول عن:

-   Validation
-   Soft Delete
-   Restore
-   Hard Delete
-   Transactions
-   تحديث العلاقات
-   Activity Log
-   التحقق من المرفقات
-   دمج السجلات
-   الاستيراد
-   التصدير
-   Backup / Restore

ولا توضع هذه القواعد داخل Activity أو Compose UI.

------------------------------------------------------------------------

# 35. حماية البيانات من التناقض

يجب عدم السماح بحفظ:

``` text
Family → House A
Person → Family
Person → House B
```

إذا كانت العائلة في House A.

ولا يسمح:

``` text
Person → Family A
Family A → Head = Person B
```

إذا كان Person B لا ينتمي إلى Family A.

ولا يسمح:

``` text
Residency start > end
```

ولا يسمح بأكثر من:

``` text
current residency
```

لنفس الشخص.

------------------------------------------------------------------------

# 36. الحذف الناعم والاستعادة

كل سجل محذوف يجب أن يحتفظ بـ:

``` text
is_deleted = 1
deleted_at = timestamp
deleted_reason = ...
```

ولا تظهر السجلات المحذوفة في الشاشات العادية.

الاستثناء الوحيد:

``` text
سلة المحذوفات
```

------------------------------------------------------------------------

# 37. قواعد عرض البيانات

الحقول المحسوبة أو المشتقة لا تخزن كقيم مستقلة.

أمثلة:

``` text
عمر الشخص
عدد أفراد العائلة
عدد سكان الدار
عدد الدور المشغولة
عدد الوافدين
```

تحسب من البيانات الأصلية.

أما البيانات التاريخية المجمّدة، مثل Snapshot المستندات الصادرة، فتخزن
لأنها تمثل حالة تاريخية وليست قيمة محسوبة حالياً.

------------------------------------------------------------------------

# 38. حالات خاصة يجب أن يدعمها النظام

يجب أن يدعم:

1.  شخص بدون عائلة.
2.  عائلة بدون رب أسرة مؤقتاً.
3.  شخص ينتقل من دار إلى دار.
4.  شخص يعود إلى دار سابقة.
5.  شخص يغادر المنطقة.
6.  شخص يأتي من خارج المنطقة.
7.  دار شاغرة.
8.  دار مهجورة.
9.  مالك دار ليس من السكان.
10. عائلة تضم عدة أفراد.
11. وجود مرفقات متعددة للسجل.
12. سجل مكرر يحتاج إلى دمج.
13. بيانات مستوردة تحتاج مراجعة.
14. معاملات مرتبطة بشخص أو عائلة.
15. مستند صادر يجب أن يحتفظ ببياناته القديمة.

------------------------------------------------------------------------

# 39. سياسة البيانات المكررة

لا نعتمد على `full_name` وحده لتحديد أن شخصين متطابقان.

المطابقة يمكن أن تعتمد على مجموعة عوامل:

``` text
الاسم بعد التطبيع
اسم الأب
اسم الجد
اللقب
تاريخ الميلاد
رقم الهاتف
العائلة
الدار
```

والنتيجة تكون:

``` text
مطابق بقوة
احتمال تكرار
غير متطابق
```

لكن **الدمج النهائي يحتاج موافقة المختار**.

------------------------------------------------------------------------

# 40. قواعد الاستيراد

لا يدخل أي صف مباشرة إلى البيانات الرسمية.

كل صف يمر أولاً عبر:

``` text
import_staging
```

ويحصل على:

``` text
valid
invalid
warning
duplicate_candidate
```

ولا يتم الإدخال النهائي إلا بعد معاينة المستخدم وتأكيده.

------------------------------------------------------------------------

# 41. متطلبات الأداء

لأن التطبيق Offline وقاعدة البيانات محلية:

-   الاستعلامات يجب أن تستخدم Indexes مناسبة.
-   لا يتم تحميل آلاف السجلات دفعة واحدة إلى الذاكرة.
-   القوائم تستخدم Paging عند الحاجة.
-   الصور الكبيرة لا تحمل كاملة في القوائم.
-   ملفات PDF لا تقرأ كاملة في الذاكرة عند عرض بياناتها الوصفية.
-   عمليات Backup/Export الكبيرة تنفذ بطريقة streaming قدر الإمكان.
-   عمليات الاستيراد تعالج على دفعات.
-   البحث يستخدم فهارس وتطبيعاً مناسباً.

------------------------------------------------------------------------

# 42. متطلبات الذاكرة

يجب تجنب:

``` text
تحميل كل الصور إلى RAM
تحميل كل قاعدة البيانات إلى RAM
تحميل ملف Excel كامل إذا كان ضخماً
```

وتستخدم:

``` text
Paging
Streaming
Batch Processing
Lazy Loading
```

حسب العملية.

------------------------------------------------------------------------

# 43. الخريطة الجغرافية

**غير موجودة نهائياً في النظام.**

لا توجد:

``` text
latitude
longitude
GPS
map_provider
tile
map_url
```

العنوان يعتمد على التسلسل الإداري والنصي:

``` text
المحافظة
→ القضاء
→ الناحية
→ المحلة
→ الشارع
→ الزقاق
→ رقم الدار
→ العنوان التفصيلي
```

------------------------------------------------------------------------

# 44. الخلاصة المعمارية النهائية

الجداول الأساسية:

``` text
regions
streets
alleys
houses
families
persons
residencies
transactions
residency_certificates
incoming_letters
outgoing_letters
visitors_log
attachments
activity_log
person_merge_log
import_staging
app_settings
```

والنظام يعتمد:

``` text
SQLite
+
Room
+
Local File Storage
+
Soft Delete
+
Recycle Bin
+
Audit Log
+
Import Staging
+
Duplicate Detection
+
Person Merge
+
Atomic Backup/Restore
```

مع الالتزام الكامل:

``` text
Offline 100%
مختار واحد
جهاز واحد
بدون Users
بدون Roles
بدون Internet
بدون Maps
بدون GPS
```

------------------------------------------------------------------------

# 45. حالة المخطط

بعد هذه التعديلات، المخطط يعتبر **المرجع الأساسي قبل التنفيذ**.

لا ينبغي إنشاء Room Entities بشكل مباشر قبل الالتزام بهذه القواعد،
خصوصاً:

1.  `residencies` بدلاً من نموذج arrival/departure السابق.
2.  ضبط علاقة `family_id` و`house_id`.
3.  دعم الشخص المستقل.
4.  تنظيم `relation_to_head`.
5.  إزالة JSON attachment paths من الوارد والصادر.
6.  ضبط إدارة `attachments`.
7.  إضافة `person_merge_log`.
8.  إضافة `import_staging`.
9.  فصل بيانات القفل الحساسة عن Backup.
10. إضافة قواعد Transaction والحذف والاستعادة.
11. استخدام Room Migration لإصدارات Schema.
12. منع أي اتصال بالإنترنت أو خدمات الخرائط.

**هذا هو المخطط المرجعي الذي يجب تحويله لاحقاً إلى Room Entities وDAOs
وRepositories وMigrations.**
