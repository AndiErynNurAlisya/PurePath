# 🌿 PurePath — Aplikasi Berita & Informasi Lingkungan

✨ **PurePath** adalah aplikasi Android yang menyajikan **informasi kualitas lingkungan** (kualitas udara, indeks UV, cuaca) dan **berita terkini** , lengkap dengan **rekomendasi kesehatan personal** berdasarkan kondisi penyakit pengguna. Aplikasi ini membantu kamu mengambil keputusan harian — kapan aman beraktivitas di luar, kapan perlu masker, dan kapan perlu perlindungan dari sinar UV.

> 🏷️ **Tema proyek:** Berita & Informasi  
> 📱 **Platform:** Android (Java)  
> 🇮🇩 **Bahasa:** Indonesia

---

## 📸 Tampilan Aplikasi

| 🏠 Beranda | 📰 Berita | 🌙 Dark Mode |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/9c63dfe2-1367-4b86-944b-ae6817df68e2" width="200"/> | <img src="https://github.com/user-attachments/assets/6b2fe23c-f2db-4064-af59-1c7080782219" width="200"/> | <img src="https://github.com/user-attachments/assets/7b72c1ed-b2c0-4fbe-a67b-4ba69f085a05" width="200"/> |

| 🧭 Eksplor | 📖 Diary | ⚙️ Pengaturan |
|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/060cec3f-7435-4dad-a0e4-5753862964cf" width="200"/> | <img src="https://github.com/user-attachments/assets/576bf243-4bad-4e6e-b35a-b7f2e369ca10" width="200"/> | <img src="https://github.com/user-attachments/assets/72e97117-524a-47c2-931a-f94d6584690c" width="200"/> |

---

## 🚀 Fitur Utama

- 🏠 **Beranda (Home)** — Menampilkan lokasi pengguna, cuaca real-time, kualitas udara (ISPU), indeks UV, indeks pernapasan, dan rekomendasi kesehatan harian.
- 🌫️ **Kualitas Udara (ISPU)** — Menghitung **Indeks Standar Pencemar Udara (ISPU)** dari konsentrasi PM2.5 sesuai formula resmi **KLHK** (Kementerian Lingkungan Hidup & Kehutanan).
- 💚 **Rekomendasi Kesehatan Personal** — Rekomendasi disesuaikan dengan riwayat penyakit pengguna (Asma, ISPA, Lupus, Eksim, Rosacea, Herpes) terhadap faktor **polusi udara** dan **paparan sinar UV**.
- 📰 **Berita (News)** — Daftar berita terkini dari NewsAPI dengan **filter kategori** (Polusi / Iklim / Cuaca) dan **swipe-to-refresh**. Detail berita dibuka di dalam aplikasi via WebView.
- 🧭 **Eksplor (Explore)** — Daftar lokasi/tempat yang dapat ditandai (bookmark).
- 📖 **Diary** — Riwayat harian kualitas udara & UV pengguna, tersimpan secara lokal (offline).
- ⚙️ **Pengaturan (Settings)** — Profil pengguna, pilihan kondisi kesehatan, dan **mode gelap/terang (Dark/Light Mode)**.
- 🔐 **Autentikasi** — Alur Onboarding, Register, dan Login.

---

## 🛠️ Arsitektur & Komponen Teknis

Aplikasi memenuhi spesifikasi teknis berikut :

| Komponen | Implementasi |
|---|---|
| **Activity (≥2)** | `SplashActivity` (launcher), `MainActivity`, `LoginActivity`, `RegisterActivity`, `OnboardingActivity`, `WebViewActivity` |
| **Intent** | Perpindahan antar Activity (Splash → Login/Main, buka berita di WebViewActivity, dll) |
| **Fragment + Navigation** | 7 Fragment (`Home`, `Explore`, `News`, `Diary`, `Settings`, `DetailLocation`, `DetailPlan`) dengan **Navigation Component** + **BottomNavigationView** |
| **RecyclerView** | `NewsAdapter` (berita), `LocationAdapter` (eksplor), `DiaryAdapter` (riwayat) |
| **Background Thread** | `ExecutorService` (`Executors.newSingleThreadExecutor`) + callback async **Retrofit** untuk pemanggilan jaringan |
| **Networking** | **Retrofit** + penanganan kegagalan: **swipe-to-refresh** (News) & **Snackbar "Coba Lagi"** (Home) saat koneksi terputus. Berita dapat difilter per kategori (Polusi / Iklim / Cuaca) |
| **Penyimpanan Lokal** | **SQLite** (`DatabaseHelper`, `DiaryDao`) untuk Diary + **SharedPreferences** untuk profil, kondisi kesehatan, & preferensi tema |
| **Tampilan Offline** | Data Diary tetap tampil tanpa koneksi internet |
| **Tema Gelap/Terang** | `AppCompatDelegate.setDefaultNightMode` + `res/values-night/themes.xml`, preferensi tersimpan & diterapkan saat startup |

---

## 🌐 Sumber Data (API)

| 📊 Data | 🔌 Penyedia |
|---|---|
| ☁️ Cuaca & Polusi Udara (PM2.5) | **OpenWeatherMap** API |
| ☀️ Indeks UV | **Open-Meteo** API |
| 📰 Berita | **NewsAPI** |

### 🌫️ Tentang ISPU
Nilai kualitas udara dihitung sebagai **ISPU** (bukan AQI internasional) dari konsentrasi PM2.5 menggunakan formula interpolasi linier KLHK:

```
I = ((Ia - Ib) / (Xa - Xb)) * (Xx - Xb) + Ib
```

Kategori:
- 🟢 **Baik (1–50)**
- 🟡 **Sedang (51–100)**
- 🟠 **Tidak Sehat (101–200)**
- 🔴 **Sangat Tidak Sehat (201–300)**
- ⚫ **Berbahaya (301+)**

---

## ⚡ Konfigurasi & Cara Menjalankan

### 📋 Prasyarat
- 🟢 Android Studio (versi terbaru)
- ☕ JDK 11
- 🤖 Android SDK (compileSdk 36)
- 📱 Perangkat/emulator Android (minSdk 24 / Android 7.0+)

### 🔧 Spesifikasi Build
- `applicationId`: `com.example.purepath`
- `compileSdk`: 36 &nbsp;|&nbsp; `minSdk`: 24 &nbsp;|&nbsp; `targetSdk`: 34
- `versionName`: 1.0

### 🪄 Langkah Setup
1. 📥 **Clone** repositori ini:
   ```bash
   git clone <url-repo-anda>
   ```
2. 📂 Buka folder proyek di **Android Studio**.
3. 🔑 **Konfigurasi API Key** — buat/lengkapi file `local.properties` di root proyek (file ini **tidak** di-commit ke Git). Tambahkan:
   ```properties
   sdk.dir=/path/ke/Android/Sdk
   OWM_API_KEY=API_KEY_OPENWEATHERMAP_ANDA
   NEWS_API_KEY=API_KEY_NEWSAPI_ANDA
   ```
   > 💡 Dapatkan API key gratis di [openweathermap.org](https://openweathermap.org/api) dan [newsapi.org](https://newsapi.org). Key dibaca aman lewat `BuildConfig` (tidak ditulis langsung di kode) .
4. 🔄 **Sync Gradle**, lalu **Run** ke perangkat/emulator.

---

## 📖 Cara Pakai

1. 👋 **Onboarding & Daftar/Login** — Pengguna baru melalui onboarding lalu mendaftar; pengguna lama langsung login.
2. 📍 **Beri izin lokasi** — Diperlukan agar data cuaca, udara, & UV sesuai lokasi kamu.
3. 🏠 **Beranda** — Lihat ringkasan kondisi lingkungan & rekomendasi kesehatan hari ini.
4. 💊 **Atur kondisi kesehatan** — Di menu Pengaturan, aktifkan penyakit yang relevan agar rekomendasi lebih personal.
5. 📰 **Baca berita** — Buka tab Berita, pilih kategori, tarik ke bawah untuk refresh .
6. 📖 **Pantau riwayat** — Tab Diary menyimpan catatan kualitas udara & UV harian.
7. 🌙 **Mode gelap** — Aktifkan di Pengaturan; preferensi tersimpan & tetap aktif saat app dibuka kembali.

---

## 🗂️ Struktur Proyek (ringkas)

```
com.example.purepath
├── SplashActivity.java
├── activity/        # Login, Register, Onboarding, Main, WebView
├── fragment/        # Home, Explore, News, Diary, Settings, DetailLocation, DetailPlan
├── adapter/         # NewsAdapter, LocationAdapter, DiaryAdapter (RecyclerView)
├── database/        # DatabaseHelper, DiaryDao (SQLite)
├── network/         # ApiClient + model response (Retrofit)
└── model/           # Model data
```

---

## 🧰 Teknologi yang Digunakan

- 💻 **Bahasa:** Java
- 🎨 **UI:** Material Components, ConstraintLayout, BottomNavigationView
- 🧭 **Navigasi:** AndroidX Navigation Component
- 🌐 **Networking:** Retrofit + Gson
- 📍 **Lokasi:** Google Play Services Location (Fused Location Provider)
- 💾 **Database:** SQLite + SharedPreferences

---

## 👩‍💻 Developer

**Andi Eryn Nur Alisya** 

