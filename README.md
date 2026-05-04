# UTS Distributed System - Multi Client Chat

Project ini adalah aplikasi chat multi-client menggunakan Java Socket Programming. Arsitekturnya client-server: server menerima banyak client secara bersamaan, lalu membroadcast pesan dari satu client ke client lain.

## Identitas

- Nama: Halimatuz Z
- Mata kuliah: Distributed System
- Tugas: UTS Project Chat Application

## Fitur

- Server menerima banyak koneksi client dalam waktu yang sama.
- Setiap client ditangani oleh thread berbeda.
- Pesan dari client dibroadcast ke semua client lain.
- Client memiliki GUI Swing berisi conversation history, text input, dan tombol Send.
- Client dapat mengirim pesan dengan tombol Send atau tombol Enter.
- Server menampilkan client yang terhubung dan log aktivitas chat.

## Struktur Folder

```text
src/com/jgu/chat/ChatServer.java  -> program server
src/com/jgu/chat/ChatClient.java  -> program client GUI
compile.bat                       -> compile source Java
run-server.bat                    -> menjalankan server
run-client.bat                    -> menjalankan client
presentation.md                   -> materi presentasi
```

## Cara Menjalankan

Pastikan JDK 8 atau lebih baru sudah terpasang. File `.bat` akan memakai `java`/`javac` dari PATH. Jika belum ada di PATH, file `.bat` juga mencoba memakai JDK bawaan NetBeans pada `C:\Program Files\Apache NetBeans\jdk\bin`.

1. Compile program:

```bat
compile.bat
```

2. Jalankan server:

```bat
run-server.bat
```

3. Buka terminal baru, lalu jalankan client:

```bat
run-client.bat
```

4. Untuk menguji multi-client, jalankan `run-client.bat` beberapa kali. Setiap window client dapat memakai username berbeda.

## Alur Sistem

1. Server membuka port `5000` menggunakan `ServerSocket`.
2. Client melakukan koneksi ke server memakai `Socket`.
3. Setelah terkoneksi, client mengirim username ke server.
4. Server membuat `ClientHandler` baru untuk setiap client.
5. `ClientHandler` berjalan di thread sendiri sehingga banyak client bisa aktif bersamaan.
6. Ketika client mengirim pesan, server menerima pesan tersebut.
7. Server membroadcast pesan ke semua client lain yang sedang terkoneksi.
8. Client menerima pesan lewat listener thread dan menampilkannya ke conversation history.

## Pembagian Tugas Kelompok

- Anggota 1: Server socket dan accept client.
- Anggota 2: Client GUI Swing.
- Anggota 3: Broadcast message dan multithreading.
- Anggota 4: Testing multi-client dan dokumentasi.
- Anggota 5: PPT, diagram, dan demo presentasi.

## Catatan Demo

Untuk demo presentasi, buka 1 server dan minimal 2 client. Kirim pesan dari client pertama, lalu tunjukkan bahwa pesan muncul di client kedua. Ulangi dari client kedua untuk membuktikan komunikasi dua arah.
