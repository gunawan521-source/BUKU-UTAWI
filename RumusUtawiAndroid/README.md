# Buku Digital Rumus Utawi — Android

Proyek Android Studio untuk membaca `Salinan Rumus Utawi.pdf` seperti buku digital dengan gaya kitab klasik cokelat-krem.

## Fitur
- PDF asli disertakan di `app/src/main/assets/Salinan Rumus Utawi.pdf`.
- Tampilan menyesuaikan ukuran layar Android.
- Tombol Sebelumnya/Berikutnya.
- Geser halaman ke kiri/kanan.
- Pilih nomor halaman melalui dropdown.
- Efek animasi saat berganti halaman.
- Tidak memerlukan koneksi internet saat aplikasi berjalan.

## Cara membuat APK
1. Instal Android Studio versi terbaru di komputer.
2. Pilih **Open** lalu buka folder `RumusUtawiAndroid` yang berisi file `settings.gradle`.
3. Tunggu proses Gradle Sync selesai. Jika diminta, izinkan Android Studio mengunduh Android SDK dan komponen Gradle yang dibutuhkan.
4. Hubungkan HP Android dengan USB debugging aktif, lalu tekan **Run** untuk menguji aplikasi.
5. Untuk membuat file APK: pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. APK debug biasanya berada di `app/build/outputs/apk/debug/app-debug.apk`.

## Catatan
File PDF sumber disimpan di dalam aplikasi sebagai aset. Proyek ini adalah kode sumber Android Studio; APK belum dikompilasi di lingkungan ini karena Android SDK/Gradle tidak tersedia di lingkungan pembuatan file.

## Opsi membangun APK lewat GitHub Actions (tanpa Android Studio lokal)
1. Buat repository GitHub baru dan unggah seluruh isi folder proyek ini ke repository (termasuk folder `.github/workflows`).
2. Buka tab **Actions** di repository dan pilih workflow **Build Buku Digital Rumus Utawi APK**.
3. Tekan **Run workflow** (atau tunggu build berjalan setelah push ke branch `main`/`master`).
4. Setelah workflow berhasil, buka hasil run dan unduh artifact **RumusUtawi-debug-apk**.
5. Ekstrak ZIP artifact, lalu pindahkan `app-debug.apk` ke HP Android dan instal. Jika diminta, izinkan pemasangan dari sumber tersebut.

APK debug ditujukan untuk instalasi dan pengujian pribadi. Untuk distribusi luas/Play Store, buat APK rilis yang ditandatangani.
