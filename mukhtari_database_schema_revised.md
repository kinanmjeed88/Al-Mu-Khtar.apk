# 📐 مخطط قاعدة بيانات تطبيق «مختاري»
### SQLite + Room — Offline 100% — مختار واحد + جهاز واحد

> **هذه النسخة هي المخطط المرجعي بعد اعتماد جميع التعديلات النهائية على المشروع.**
> وهي تلغي أي افتراضات قديمة تخص تعدد المستخدمين أو الخرائط أو الاتصال الخارجي أو نموذج `arrival/departure` القديم.

---

# 0. القرارات المعمارية النهائية

## 0.1 طبيعة التطبيق

- التطبيق مخصص **لمختار واحد فقط وعلى جهاز Android واحد**.
- لا يوجد نظام مستخدمين متعددين.
- لا توجد مزامنة بين الأجهزة.
- لا توجد حسابات دخول للمستخدمين.
- لا توجد Roles أو Permissions أو Admin Accounts.
- لا يوجد `Users` table.
- لا يوجد Authentication خارجي.

## 0.2 Offline 100%

التطبيق يعمل بالكامل محلياً وبدون إنترنت.

يُمنع في التطبيق:

- `INTERNET` permission
- Firebase
- Remote API
- Cloud Database
- Cloud Backup
- Remote Analytics
- Google Maps
- Google Places
- OpenStreetMap
- MapLibre
- GPS
- latitude / longitude
- أي Networking SDK غير ضروري
- أي وظيفة أساسية تعتمد على خادم أو خدمة خارجية

جميع العمليات الأساسية:

```text
Android Device
      ↓
Room / SQLite
      ↓
Local File Storage
      ↓
Local PDF / Import / Export / Backup
```

## 0.3 الموقع

لا يوجد نظام خرائط أو إحداثيات.

الموقع يدار نصياً فقط:

```text
المحافظة
→ القضاء
→ الناحية
→ المحلة
→ الشارع
→ الزقاق
→ رقم الدار
→ العنوان التفصيلي
```

لا توجد حقول:

```text
latitude
longitude
gps
map_provider
tile
map_url
```

## 0.4 قاعدة البيانات

التقنيات المعتمدة:

- SQLite
- Room
- Foreign Keys
- Transactions
- Indexes
- Room Migrations

**Room Database version + Room Migrations هي المصدر الحقيقي لإصدار المخطط.**

لا يتم استخدام `app_settings.db_schema_version` كمصدر للحقيقة.

## 0.5 التاريخ والتوقيت

- التاريخ فقط: `YYYY-MM-DD` عند الحاجة كتاريخ منطقي.
- الطوابع الزمنية: `INTEGER` تمثل Unix epoch milliseconds.
- لا يتم تخزين العمر؛ العمر يحسب من `birth_date`.

## 0.6 الحذف الناعم

في الجداول الرئيسية التي تدعم Soft Delete:

```text
is_deleted = 0  → سجل نشط
is_deleted = 1  → سجل محذوف
```

مع:

```text
deleted_at
deleted_reason
```

القواعد:

- الاستعلامات العادية تعرض `is_deleted = 0`.
- سلة المحذوفات تعرض `is_deleted = 1`.
- الحذف العادي لا ينفذ `DELETE`.
- Hard Delete مسموح فقط من سلة المحذوفات وبعد تأكيد إضافي.
- حذف Parent يجب أن يفحص العلاقات التابعة ويمنع البيانات اليتيمة أو غير المتسقة.
- Restore للـParent لا يعيد تلقائياً Child تم حذفه بشكل مستقل.

---

# 1. مبادئ التصميم

## 1.1 Clean Architecture

المسار الوظيفي:

```text
UI
 ↓
ViewModel
 ↓
Use Case
 ↓
Repository
 ↓
DAO
 ↓
Room / SQLite
```

المرفقات:

```text
UI
 ↓
ViewModel
 ↓
Use Case
 ↓
Attachment Repository
 ↓
Local File Storage
```

الواجهة لا تصل مباشرة إلى SQLite أو نظام الملفات.

## 1.2 مسؤوليات Repository

Repository مسؤول عن:

- Validation
- Business Rules
- Soft Delete
- Restore
- Hard Delete
- Transactions
- تحديث العلاقات
- Activity Log
- Attachment validation
- Duplicate handling
- Person Merge
- Import / Export
- Backup / Restore orchestration

ولا توضع قواعد الأعمال داخل Composable أو Activity.

---

# 2. الجداول الأساسية

الجداول النهائية هي **17 جدولاً**:

1. `regions`
2. `streets`
3. `alleys`
4. `houses`
5. `families`
6. `persons`
7. `residencies`
8. `transactions`
9. `residency_certificates`
10. `incoming_letters`
11. `outgoing_letters`
12. `visitors_log`
13. `attachments`
14. `activity_log`
15. `person_merge_log`
16. `import_staging`
17. `app_settings`

لا يتم إضافة `Users` أو جداول مزامنة أو جداول خرائط.

---

# 3. الهيكل الإداري

## 3.1 `regions`

يمثل المحافظة / القضاء / الناحية / المحلة والمنطقة.

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| governorate | TEXT | NOT NULL |
| district | TEXT | NOT NULL |
| sub_district | TEXT | NOT NULL |
| mahalla | TEXT | NOT NULL |
| name | TEXT | NOT NULL |
| description | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

## 3.2 `streets`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| region_id | INTEGER | FK → regions.id, NOT NULL |
| name | TEXT | NOT NULL |
| code | TEXT | NULL |
| description | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

فهرس على `region_id`.

## 3.3 `alleys`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| street_id | INTEGER | FK → streets.id, NOT NULL |
| name | TEXT | NOT NULL |
| code | TEXT | NULL |
| description | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

فهرس على `street_id`.

---

# 4. الدور

## 4.1 `houses`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| internal_number | TEXT | NOT NULL, UNIQUE |
| house_number | TEXT | NOT NULL |
| alley_id | INTEGER | FK → alleys.id, NULL |
| street_id | INTEGER | FK → streets.id, NULL |
| mahalla_number | TEXT | NULL |
| detailed_address | TEXT | NULL |
| photo_path | TEXT | NULL، مسار محلي فقط |
| property_type | TEXT | NOT NULL: residential / commercial / mixed / under_construction / other |
| status | TEXT | NOT NULL: occupied / vacant / abandoned |
| ownership_type | TEXT | NOT NULL: owned / rented / unspecified |
| owner_name | TEXT | NULL |
| owner_phone | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

الفهارس:

- `status`
- `alley_id`
- `street_id`

قاعدة الاتساق:

- إذا كان `alley_id` غير NULL، يجب أن يكون الزقاق تابعاً لنفس `street_id`.
- لا يجوز ربط دار بزقاق تابع لشارع مختلف.
- إذا كان `street_id` غير NULL بدون زقاق، يسمح بالربط المباشر بالشارع حسب المخطط.

---

# 5. العوائل والأفراد

## 5.1 `families`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| family_code | TEXT | NOT NULL، تسلسلي |
| family_name | TEXT | NOT NULL |
| house_id | INTEGER | FK → houses.id, NOT NULL |
| head_of_family_id | INTEGER | FK → persons.id, NULL |
| residency_date | INTEGER | NOT NULL |
| residency_status | TEXT | NOT NULL: resident / incoming / displaced / returnee / other |
| info_source | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

**عدد أفراد العائلة لا يخزن في قاعدة البيانات.**

يحسب ديناميكياً:

```sql
SELECT COUNT(*)
FROM persons
WHERE family_id = ?
  AND is_deleted = 0;
```

## 5.2 `persons`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| full_name | TEXT | NOT NULL |
| father_name | TEXT | NULL |
| grandfather_name | TEXT | NULL |
| surname | TEXT | NULL |
| gender | TEXT | NOT NULL: male / female |
| birth_date | INTEGER | NULL، epoch milliseconds |
| marital_status | TEXT | NOT NULL |
| relation_to_head | TEXT | NULL، قيمة منظمة |
| family_id | INTEGER | FK → families.id, NULL |
| house_id | INTEGER | FK → houses.id, NULL |
| work_status | TEXT | NOT NULL: gov_employee / private_employee / self_employed / business_owner / student / retired / unemployed / housewife / other |
| employer | TEXT | NULL |
| job_title | TEXT | NULL |
| education_level | TEXT | NOT NULL |
| phone | TEXT | NULL |
| phone_alt | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

الفهارس:

- `family_id`
- `house_id`
- `full_name`

قواعد العمل:

- `family_id = NULL` مسموح لدعم الشخص المستقل.
- إذا كان الشخص مرتبطاً بعائلة، يجب منع عدم الاتساق بين عائلة الشخص ودار إقامته.
- إذا كان `head_of_family_id` مستخدماً، يجب أن يكون الشخص ضمن العائلة نفسها.
- `relation_to_head` قائمة قيم منظمة وليست نصاً حراً.

---

# 6. السكن التاريخي والحالي

## 6.1 `residencies`

هذا هو **النموذج المعتمد النهائي للسكن**.

لا تستخدم نموذج `arrival/departure` القديم كنموذج بيانات مستقل.

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| person_id | INTEGER | FK → persons.id, NOT NULL |
| family_id | INTEGER | FK → families.id, NULL |
| house_id | INTEGER | FK → houses.id, NULL |
| start_date | INTEGER | NOT NULL |
| end_date | INTEGER | NULL |
| residency_status | TEXT | NOT NULL: current / previous / incoming / outgoing / returnee / external |
| previous_external_address | TEXT | NULL |
| verification_status | TEXT | NULL: pending / verified / needs_review |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

قواعد العمل:

- الشخص لا يمتلك أكثر من `current` residency نشطة في الوقت نفسه.
- يطبق ذلك بآلية DB مناسبة مثل Partial Unique Index أو تحقق Transactional داخل Repository بحسب دعم SQLite/Room المستخدم.
- `start_date <= end_date` عند وجود `end_date`.
- الانتقال بين الدور عملية Transaction واحدة.
- النقل يتضمن تحديث residency الحالية وإنشاء residency الجديدة في نفس Transaction.
- تدعم:
  - السكن الحالي
  - السكن السابق
  - الوافد
  - المغادر
  - العائد
  - السكن الخارجي
  - العنوان السابق خارج المنطقة
- لا يجوز حفظ حالة متناقضة بين person/family/house.
- `is_deleted = 1` لا يمثل residency نشطة في الاستعلامات العادية.

---

# 7. المعاملات

## 7.1 `transactions`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| transaction_code | TEXT | NOT NULL، تسلسلي مثل TRX-YYYY-NNNNN |
| transaction_type | TEXT | NOT NULL |
| applicant_name | TEXT | NOT NULL، Snapshot وقت التسجيل |
| person_id | INTEGER | FK → persons.id, NULL |
| family_id | INTEGER | FK → families.id, NULL |
| request_date | INTEGER | NOT NULL |
| subject | TEXT | NOT NULL |
| details | TEXT | NULL |
| status | TEXT | NOT NULL: new / in_progress / pending_info / completed / cancelled / archived |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

`applicant_name` Snapshot ولا يتغير تلقائياً إذا تغير اسم الشخص لاحقاً.

---

# 8. تأييدات السكن

## 8.1 `residency_certificates`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| transaction_id | INTEGER | FK → transactions.id, NOT NULL |
| person_id | INTEGER | FK → persons.id, NOT NULL |
| snapshot_name | TEXT | NOT NULL |
| snapshot_family | TEXT | NULL |
| snapshot_house | TEXT | NULL |
| snapshot_address | TEXT | NULL |
| pdf_path | TEXT | NULL، مسار محلي |
| issued_at | INTEGER | NOT NULL |
| created_at | INTEGER | NOT NULL |

القواعد:

- Snapshot immutable بعد الإصدار.
- تعديل person/family/house لاحقاً لا يغير الشهادة السابقة.
- PDF ينشأ محلياً باستخدام Android native PDF generation أو تنفيذ محلي مكافئ.
- لا توجد خدمة Cloud لإنشاء PDF.

---

# 9. الوارد والصادر

## 9.1 `incoming_letters`

**لا تستخدم `attachment_paths` JSON.**

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| letter_number | TEXT | NOT NULL |
| letter_date | INTEGER | NOT NULL |
| sender | TEXT | NOT NULL |
| subject | TEXT | NOT NULL |
| details | TEXT | NULL |
| required_action | TEXT | NULL |
| status | TEXT | NOT NULL: new / in_progress / done / postponed / archived |
| action_date | INTEGER | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

المرفقات تدار فقط عبر `attachments`.

## 9.2 `outgoing_letters`

**لا تستخدم `attachment_paths` JSON.**

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| letter_number | TEXT | NOT NULL |
| letter_date | INTEGER | NOT NULL |
| recipient | TEXT | NOT NULL |
| subject | TEXT | NOT NULL |
| details | TEXT | NULL |
| recipient_name | TEXT | NULL |
| delivery_date | INTEGER | NULL |
| delivery_method | TEXT | NULL |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |
| updated_at | INTEGER | NOT NULL |

المرفقات تدار فقط عبر `attachments`.

---

# 10. سجل المراجعين

## 10.1 `visitors_log`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| visitor_name | TEXT | NOT NULL |
| phone | TEXT | NULL |
| visit_reason | TEXT | NOT NULL |
| transaction_type | TEXT | NULL |
| visit_date | INTEGER | NOT NULL |
| visit_time | TEXT | NULL |
| handled_by | TEXT | NULL، اسم المختار فقط |
| result | TEXT | NULL |
| notes | TEXT | NULL |
| created_at | INTEGER | NOT NULL |

لا توجد علاقة Users/Account لهذا الحقل.

---

# 11. المرفقات

## 11.1 `attachments`

المرفقات مركزية وتستخدم علاقة Polymorphic:

```text
owner_type + owner_id
```

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| owner_type | TEXT | NOT NULL: house / family / person / transaction / incoming_letter / outgoing_letter |
| owner_id | INTEGER | NOT NULL |
| file_type | TEXT | NOT NULL: image / pdf / document |
| file_path | TEXT | NOT NULL، مسار محلي |
| file_name | TEXT | NOT NULL |
| file_size | INTEGER | NULL، bytes |
| notes | TEXT | NULL |
| is_deleted | INTEGER | NOT NULL DEFAULT 0 |
| deleted_at | INTEGER | NULL |
| deleted_reason | TEXT | NULL |
| created_at | INTEGER | NOT NULL |

فهرس مركب:

```text
(owner_type, owner_id)
```

قواعد التخزين:

- لا تخزن الصور/الملفات الكبيرة كـBLOB داخل SQLite.
- الملفات الفيزيائية تحفظ في Local File Storage.
- SQLite يحفظ metadata والمسار فقط.
- الحذف الفيزيائي لا يتم إذا كان الملف لا يزال مستخدماً من سجل فعال آخر.
- Hard Delete للملف يتم فقط عند انعدام المراجع الفعالة.
- دورة حياة المرفق:

```text
Create
→ View
→ Metadata Update
→ Soft Delete
→ Restore
→ Hard Delete
```

---

# 12. سجل النشاط

## 12.1 `activity_log`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| action_type | TEXT | NOT NULL: create / update / delete / restore / merge / export / import / backup / restore_backup / settings_change |
| entity_type | TEXT | NOT NULL |
| entity_id | INTEGER | NULL |
| description | TEXT | NOT NULL |
| old_values | TEXT | NULL، JSON عند الحاجة |
| new_values | TEXT | NULL، JSON عند الحاجة |
| timestamp | INTEGER | NOT NULL |

القواعد:

- يملأ تلقائياً من Repository.
- لا يسمح للمستخدم بالتعديل اليدوي.
- لا يخزن Secrets.
- عمليات merge تسجل في `activity_log`.
- عمليات import/export/backup/restore تسجل حسب الحاجة.

---

# 13. دمج الأشخاص المكررين

## 13.1 `person_merge_log`

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| source_person_id | INTEGER | FK → persons.id, NOT NULL |
| target_person_id | INTEGER | FK → persons.id, NOT NULL |
| merged_at | INTEGER | NOT NULL |
| reason | TEXT | NULL |
| details | TEXT | NULL |

مسار الدمج:

```text
Select Primary
→ Select Duplicate
→ Show Differences
→ Detect Conflicts
→ Resolve Conflicts
→ User Confirmation
→ Transactional Merge
→ Move Relationships
→ Move Attachments
→ Move Transactions
→ Move Residency History
→ Create person_merge_log
→ Create activity_log
→ Soft Delete Duplicate
→ Commit
```

القواعد:

- لا Auto Merge.
- المستخدم يقرر الدمج.
- العملية Transaction واحدة.
- لا فقدان للبيانات التاريخية.
- يجب منع تعارض current residency.
- يجب الحفاظ على جميع العلاقات والمرفقات والسجلات.

---

# 14. الاستيراد المرحلي

## 14.1 `import_staging`

هذا **جدول Room فعلي** وليس مجرد ذاكرة مؤقتة.

| الحقل | النوع | القيود / الملاحظات |
|---|---|---|
| id | INTEGER | PK AUTOINCREMENT |
| import_session_id | TEXT | NOT NULL |
| row_number | INTEGER | NOT NULL |
| source_file_name | TEXT | NOT NULL |
| raw_data_json | TEXT | NOT NULL |
| normalized_data_json | TEXT | NULL |
| validation_status | TEXT | NOT NULL |
| validation_errors | TEXT | NULL |
| match_status | TEXT | NULL |
| matched_entity_id | INTEGER | NULL |
| created_at | INTEGER | NOT NULL |

المسار الإلزامي:

```text
File
→ Column Detection
→ import_staging
→ Normalization
→ Validation
→ Duplicate Detection
→ Matching
→ Preview
→ User Confirmation
→ Transactional Import
```

القواعد:

- لا تدخل البيانات مباشرة إلى الجداول الرسمية.
- تعرض الأخطاء والتحذيرات قبل الإدخال.
- CSV مدعوم بالكامل.
- Excel مدعوم محلياً بمكتبة Android مناسبة.
- Column Detection لا يفترض الأعمدة بشكل أعمى.
- يجب دعم الملفات الكبيرة باستخدام Streaming/Batch Processing.
- الاستيراد يعمل بدون إنترنت.

---

# 15. الإعدادات

## 15.1 `app_settings`

Key-Value:

| الحقل | النوع |
|---|---|
| key | TEXT PK |
| value | TEXT |

أمثلة:

```text
theme_mode
lock_enabled
security_question
biometric_enabled
auto_lock_duration
```

### أسرار App Lock

إذا تم استخدام `app_settings`، فلا يخزن PIN أو Security Answer كنص صريح.

يمكن استخدام:

- `EncryptedSharedPreferences`
- Android Keystore
- DataStore مشفر

لتخزين الأسرار.

**لا يتم اعتبار `db_schema_version` داخل `app_settings` مصدر الحقيقة للمخطط.**

مصدر الحقيقة:

```text
Room Database version
+
Room Migrations
```

---

# 16. سلة المحذوفات

لا يوجد جدول مستقل باسم Recycle Bin.

السلة هي View / Repository-level aggregation للسجلات:

```text
is_deleted = 1
```

من الجداول التي تدعم Soft Delete.

العمليات:

```text
Normal Delete
→ Soft Delete

Recycle Bin
→ View Deleted

Restore
→ is_deleted = 0

Permanent Delete
→ Explicit Hard Delete
→ Additional Confirmation
```

عند Hard Delete:

- تحقق من العلاقات التابعة.
- تحقق من المرفقات.
- احذف الملف الفيزيائي فقط إذا لم يعد له أي مرجع فعال.
- لا تستخدم Cascade بشكل أعمى لتجاوز قواعد المشروع.

---

# 17. العلاقات الرئيسية

```text
regions
  1 ── *
streets
  1 ── *
alleys
  1 ── *
houses
```

```text
houses
  1 ── *
families
  1 ── *
persons
```

```text
families.head_of_family_id
  → persons.id
```

```text
persons
  1 ── *
residencies
```

```text
persons/families
  1 ── *
transactions
```

```text
transactions
  1 ── 1
residency_certificates
```

```text
{
  house,
  family,
  person,
  transaction,
  incoming_letter,
  outgoing_letter
}
  1 ── *
attachments
```

```text
persons
  1 ── *
person_merge_log
```

---

# 18. قواعد الاتساق

يجب منع الحالات التالية:

## 18.1 تضارب الأسرة والدار

غير مسموح:

```text
Family A → House A
Person → Family A
Person → House B
```

إذا كان النموذج يفرض أن الشخص تابع لسكن العائلة.

## 18.2 رب الأسرة

غير مسموح:

```text
Family A → Head = Person B
```

إذا كان Person B خارج Family A.

## 18.3 السكن

غير مسموح:

```text
start_date > end_date
```

وغير مسموح:

```text
Person
→ Current Residency A
→ Current Residency B
```

في الوقت نفسه.

## 18.4 الشارع والزقاق

غير مسموح:

```text
House.street_id = Street A
House.alley_id = Alley belonging to Street B
```

---

# 19. عمليات Transaction المطلوبة

## 19.1 إنشاء عائلة

```text
Validate
→ Create Family
→ Create/Assign Head
→ Create Person
→ Update Head
→ Create Residency if required
→ Activity Log
→ Commit
```

## 19.2 نقل شخص

```text
Validate
→ Close Current Residency
→ Create New Residency
→ Update current relationship data if required
→ Activity Log
→ Commit
```

## 19.3 حذف دار

```text
Validate Dependencies
→ Soft Delete House
→ Handle Related Records according to business rules
→ Handle Attachments
→ Activity Log
→ Commit
```

## 19.4 دمج شخصين

```text
Validate
→ Resolve Conflicts
→ Move Relations
→ Move Attachments
→ Move Transactions
→ Move Residency History
→ Merge Log
→ Activity Log
→ Soft Delete Duplicate
→ Commit
```

---

# 20. الإحصائيات

لا تخزن Counters ثابتة في قاعدة البيانات.

تحسب ديناميكياً من البيانات الحالية بواسطة Room Queries / COUNT / SUM.

أمثلة:

- عدد الدور
- الدور المشغولة
- الدور الفارغة
- عدد العوائل
- عدد السكان
- الذكور
- الإناث
- الوافدون
- المغادرون
- المعاملات
- الوارد
- الصادر

السجلات:

```text
is_deleted = 1
```

لا تدخل في الإحصائيات العادية.

---

# 21. البحث العربي

التطبيع يستخدم للبحث والمطابقة فقط.

يدعم:

- إزالة التشكيل.
- إزالة التطويل.
- إزالة المسافات الزائدة.
- توحيد أشكال الحروف العربية المناسبة.
- معالجة الاختلافات المناسبة للبحث والمطابقة.

المبدأ:

```text
Original Stored Value
        ≠
Normalized Search Value
```

القيمة الأصلية لا يتم تغييرها.

مثال:

```text
Database:
أحمد  علي

Search:
احمد علي
```

يمكن أن يطابق، لكن قيمة قاعدة البيانات تبقى كما هي.

---

# 22. كشف المكرر

يعتمد على أكثر من عامل:

- الاسم بعد التطبيع
- الأب
- الجد
- اللقب
- تاريخ الميلاد
- الهاتف
- العائلة
- الدار

الـScore يستخدم فقط **لاقتراح المرشحين**.

لا يحدث Merge تلقائياً.

المختار هو صاحب القرار النهائي.

---

# 23. النسخ الاحتياطي

النسخة الاحتياطية غير مشفرة حسب متطلبات المشروع، لكنها محمية بالتحقق من السلامة.

تشمل:

```text
Database
Attachments
Images
PDFs
Documents
Manifest
Schema Version
App Version
Creation Timestamp
Record Counts
Integrity Hashes
```

ولا تشمل:

```text
PIN
Security Answer
Biometric Secrets
```

Manifest يجب أن يحتوي معلومات كافية للتحقق من:

- إصدار تنسيق Backup.
- إصدار التطبيق.
- إصدار Room Schema.
- تاريخ الإنشاء.
- Hashes.
- Counts.

---

# 24. الاستعادة

المسار الإلزامي:

```text
Backup Archive
→ Temporary Staging
→ Archive Validation
→ Hash Validation
→ Schema/Version Validation
→ Database Validation
→ Attachment/File Validation
→ Record Validation
→ User Confirmation
→ Atomic Replacement
```

القواعد:

- لا Partial Restore.
- لا تستبدل البيانات الحالية قبل اكتمال Validation.
- إذا فشلت Validation، تبقى البيانات الحالية سليمة.
- أي ملفات مؤقتة تحذف بعد انتهاء العملية أو فشلها.

---

# 25. المرفقات والتخزين المحلي

المجلدات التنظيمية المقترحة:

```text
attachments/
    houses/
    families/
    persons/
    transactions/
    incoming/
    outgoing/
```

SQLite يحتفظ بالـmetadata والمسارات، وليس بالملفات الكبيرة كـBLOB.

---

# 26. App Lock

يدعم:

- PIN
- Biometric إذا كان الجهاز يدعمها
- Fresh-start lock
- Background resume lock
- Auto-lock
- Security Question
- Local Recovery

القواعد:

- لا تخزين PIN كنص صريح.
- لا تخزين Security Answer كنص صريح.
- لا Secrets في Activity Log.
- لا Secrets في Backup.
- لا اعتماد على الإنترنت للاسترداد.
- لا تجاوز للقفل بسبب فشل initialization.
- التشغيل الصحيح:

```text
Application
→ Security State
→ Lock Decision
→ Lock UI / Main UI
```

---

# 27. الأداء

عند تنفيذ القوائم والملفات:

- Lazy loading / Paging عند الحاجة.
- Streaming للملفات الكبيرة.
- Batch processing للاستيراد والتصدير.
- عدم تحميل جميع الصور إلى RAM.
- عدم تحميل قاعدة البيانات كاملة إلى RAM.
- عدم تحميل Excel كبير بالكامل إلى RAM.
- استخدام Dispatchers مناسبة للعمليات الثقيلة.
- تجنب Blocking I/O على Main Thread.

---

# 28. الاختبارات المطلوبة

يجب اختبار:

### Database
- Schema
- Foreign Keys
- Indexes
- Migrations

### DAO
- CRUD
- Soft Delete
- Restore
- Filters
- Search

### Repository
- Validation
- Business Rules
- Transactions
- Parent/Child behavior

### Residency
- Current uniqueness
- Transfer
- Previous history
- Incoming
- Outgoing
- Returnee
- External address

### Certificates
- Snapshot immutability
- PDF output
- File existence/readability

### Arabic
- normalization
- search
- matching
- original value preservation

### Duplicate/Merge
- scoring
- conflicts
- transactional merge
- relation preservation

### Import
- staging
- validation
- malformed input
- matching
- transactional import

### Backup/Restore
- manifest
- hashes
- corruption
- invalid schema
- atomic restore
- rollback

### Attachments
- lifecycle
- reference counting
- physical deletion rules

### Statistics
- dynamic calculations
- exclusion of deleted rows

### Security
- PIN logic
- recovery logic
- lock state
- secret protection

---

# 29. Offline Audit

عند التدقيق النهائي يجب فحص:

```text
AndroidManifest.xml
Gradle dependencies
Source code
Runtime services
SDKs
Network APIs
```

والبحث عن:

```text
INTERNET
Firebase
Remote API
Cloud
Google Maps
Google Places
OpenStreetMap
MapLibre
GPS
latitude
longitude
Retrofit
OkHttp
Volley
Ktor networking
Remote Analytics
Cloud OCR
Cloud AI
```

الهدف:

```text
Application Runtime = 100% Offline
```

---

# 30. Migration Rules

- كل تغيير في Room schema يجب أن يرافقه Migration.
- Room Database version هو المصدر الحقيقي.
- لا يستخدم `app_settings` كبديل.
- لا تتم إضافة/حذف/إعادة تسمية columns في الإنتاج بدون Migration مناسبة.
- يجب اختبار migrations.

---

# 31. قواعد التنفيذ النهائية

لا يسمح بتطبيق أي ميزة بطريقة:

- Fake Data
- Mock functionality
- Placeholder screen
- TODO بدل التنفيذ
- Hardcoded records
- Hardcoded statistics
- Empty handlers
- Buttons بلا وظيفة
- Screens بلا persistence
- Repository غير مستخدم
- DAO غير مربوط
- UI غير مربوط
- Network dependency

معيار اكتمال الميزة:

```text
Implemented
→ Wired
→ Persisted
→ Tested
→ Runtime Verified
```

إذا تعذر Runtime Verification بسبب عدم توفر Emulator/Device، يجب التصريح بذلك بوضوح وعدم اعتبارها PASS.

---

# 32. الخلاصة المعمارية

```text
مختاري
│
├── Android Native
├── Jetpack Compose + Material 3
├── Arabic RTL
├── Single User
├── Single Device
├── Offline 100%
│
├── SQLite
├── Room
├── Foreign Keys
├── Transactions
├── Indexes
├── Room Migrations
│
├── Local File Storage
├── Soft Delete
├── Recycle Bin
├── Activity Log
├── Import Staging
├── Duplicate Detection
├── Person Merge
├── Atomic Backup / Restore
├── Local PDF
└── App Lock
```

**لا خرائط.  
لا GPS.  
لا latitude/longitude.  
لا Google Maps.  
لا Google Places.  
لا Firebase.  
لا Remote API.  
لا Cloud.  
لا Internet runtime.  
لا Users table.  
لا Multi-user.  
لا Synchronization.**

---

# 33. المرجع النهائي

هذا الملف هو المرجع الأساسي لتصميم قاعدة البيانات والعلاقات وقواعد العمل قبل تنفيذ/مراجعة Room.

أي تعارض حقيقي بين هذا المخطط ومتطلب تقني يجب الإبلاغ عنه قبل تغيير الـschema.

**لا يتم تغيير أي جدول أو حقل أو علاقة لمجرد التبسيط.**

**المخطط المعتمد يجب أن يطابق التنفيذ الفعلي في Room + DAO + Repository + Use Case + UI.**
