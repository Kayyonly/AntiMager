# AntiMager — Real Device Testing

Branch pengujian: `real-device-ready`

## Install APK

1. Buka tab **Actions** di repository.
2. Pilih workflow **Android Debug APK** yang paling baru dan statusnya hijau.
3. Download artifact **AntiMager-debug**.
4. Ekstrak ZIP lalu install `app-debug.apk` di HP Android.
5. Jika Android menolak update karena signature build lama berbeda, uninstall AntiMager lama sekali lalu install APK baru. Build berikutnya pada branch ini memakai debug key yang sama.

## Izin dasar

Saat pertama dibuka, izinkan notifikasi, mikrofon, dan lokasi presisi.

### App Blocker

1. Buka **Proteksi AntiMager**.
2. Aktifkan **Anti-Distraction Shield**.
3. Tekan **Izinkan** pada Accessibility.
4. Di pengaturan Android, aktifkan service AntiMager.
5. Pilih aplikasi yang ingin diblokir.
6. Buat tugas HIGH / mendesak, lalu buka aplikasi target.

Blocker hanya aktif jika ada tugas prioritas yang belum selesai. Android Settings, permission screen, launcher, System UI, dan AntiMager sendiri tidak diblokir.

### Reminder waktu

1. Tambah tugas melalui **Quick Add**.
2. Pilih preset **5 Menit** untuk pengujian cepat.
3. Keluar dari app.
4. Saat deadline tiba, Android AlarmManager memicu reminder.
5. Tombol **Selesai** menandai tugas selesai dan menghapus notifikasi.
6. Tombol **Tunda 15 Mnt** menjadwalkan ulang reminder 15 menit kemudian.

Reminder dipulihkan kembali setelah reboot dan saat app dibuka lagi.

### Reminder lokasi

Koordinat demo sudah dihapus. Lokasi harus berasal dari GPS HP asli.

1. Datang ke lokasi yang ingin disimpan, misalnya Rumah.
2. Buka **Proteksi AntiMager → Reminder Lokasi**.
3. Pada **Rumah**, tekan **Set di sini**.
4. Lakukan hal yang sama untuk Sekolah/Indomaret/Perpustakaan bila diperlukan.
5. Aktifkan geofencing.
6. Saat membuat tugas, pilih lokasi serta trigger **Masuk lokasi** atau **Keluar lokasi**.

Untuk geofence sistem yang paling stabil saat app benar-benar tertutup, beri izin lokasi **Allow all the time / Izinkan sepanjang waktu** dari pengaturan Android. Jika izin background belum ada, AntiMager memakai foreground location service sebagai fallback.

### Voice Command

1. Tekan ikon mikrofon.
2. Ucapkan contoh: **"Besok jam 8 malam kerjain matematika"**.
3. Periksa hasil parsing.
4. Tekan konfirmasi untuk menyimpan.

Text AI memakai Groq/Llama. Jika GROQ_API_KEY tidak tersedia, parser Bahasa Indonesia lokal tetap dipakai.

### Smart Priority

Buat beberapa tugas dengan deadline dan prioritas berbeda. Urutan Smart Priority memakai algoritma lokal sebagai dasar, sehingga tetap berfungsi tanpa internet. Jika GROQ_API_KEY tersedia, alasan dan ranking diperkuat oleh Groq/Llama.

### Scan jadwal

Scan jadwal memakai Gemini Vision saja. Jika GEMINI_API_KEY belum tersedia, jaringan gagal, atau format hasil tidak valid, app menampilkan error dan tidak menyimpan jadwal palsu.

## Catatan

APK dari branch ini adalah **debug build untuk testing**, bukan release Play Store.
