# PPT UTS Distributed System - Multi Client Chat

## Slide 1 - Judul

Multi-Client Chat Application Using Java Socket Programming

Nama kelompok:

- Halimatuz Z
- Anggota 2
- Anggota 3
- Anggota 4
- Anggota 5

Mata kuliah: Distributed System

## Slide 2 - Tujuan Project

- Membuat aplikasi chat dengan arsitektur client-server.
- Server dapat menangani banyak client secara bersamaan.
- Pesan dari satu client dapat diterima oleh client lain secara real-time.
- Client memiliki tampilan GUI sederhana dan mudah digunakan.

## Slide 3 - Teknologi

- Java JDK 8 atau lebih baru.
- `java.net.ServerSocket` untuk server.
- `java.net.Socket` untuk client.
- `java.io.BufferedReader` dan `BufferedWriter` untuk komunikasi pesan.
- `javax.swing` untuk user interface client.
- Multithreading untuk menangani banyak client.

## Slide 4 - Arsitektur Sistem

```text
Client 1 GUI ----\
Client 2 GUI ----- ServerSocket + ClientHandler Threads ---- Broadcast Message
Client 3 GUI ----/
```

Penjelasan:

- Server menjadi pusat komunikasi.
- Setiap client membuat koneksi socket ke server.
- Server membuat thread khusus untuk setiap client.
- Pesan dari satu client dikirim ke server, lalu server meneruskan ke client lain.

## Slide 5 - Alur Program Server

1. Server berjalan pada port `5000`.
2. Server menunggu koneksi client dengan `accept()`.
3. Saat client masuk, server membuat objek `ClientHandler`.
4. `ClientHandler` dijalankan sebagai thread baru.
5. Server membaca pesan dari client.
6. Server membroadcast pesan ke semua client lain.

## Slide 6 - Alur Program Client

1. Client mengisi host, port, dan username.
2. Client membuat koneksi ke server.
3. Client mengirim username sebagai identitas.
4. User mengetik pesan pada text field.
5. Pesan dikirim ke server saat tombol Send atau Enter ditekan.
6. Listener thread menerima pesan dari server dan menampilkannya di text area.

## Slide 7 - Struktur Kode

- `ChatServer.java`: menjalankan server, menyimpan daftar client, broadcast pesan.
- `ClientHandler`: inner class untuk menangani setiap client di thread terpisah.
- `ChatClient.java`: membuat GUI Swing, mengirim pesan, dan menerima pesan.
- `compile.bat`: compile semua file Java ke folder `out`.
- `run-server.bat`: menjalankan server.
- `run-client.bat`: menjalankan client.

## Slide 8 - Fitur User Interface

- Text area untuk history percakapan.
- Text field untuk mengetik pesan.
- Tombol Send untuk mengirim pesan.
- Auto-scroll ke pesan terbaru.
- Status koneksi pada bagian atas window.

## Slide 9 - Demo

Skenario demo:

1. Jalankan `compile.bat`.
2. Jalankan `run-server.bat`.
3. Jalankan `run-client.bat` sebanyak dua atau tiga kali.
4. Masukkan username berbeda.
5. Kirim pesan dari satu client.
6. Tunjukkan bahwa pesan muncul di client lain dan tercatat di server.

## Slide 10 - Kesimpulan

- Aplikasi berhasil menerapkan konsep distributed system sederhana.
- Komunikasi client-server berjalan menggunakan socket.
- Multithreading membuat server mampu menangani banyak client.
- GUI Swing membuat client lebih mudah digunakan.
