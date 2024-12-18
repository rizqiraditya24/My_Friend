package com.example.myfriend.data

// Kelas data untuk merepresentasikan informasi teman
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Anotasi @Entity digunakan untuk menandai bahwa kelas ini adalah entitas database Room
@Entity(tableName = "friend")
data class Friend(
    var name: String, // Nama teman
    var school: String, // Nama sekolah teman
    var photoPath: String?, // Path dari file gambar (opsional, dapat bernilai null)
    @ColumnInfo(defaultValue = "")
    var phone: String
) {
    // Anotasi @PrimaryKey menandai bahwa properti ini adalah primary key untuk tabel
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0 // ID teman, auto-generate untuk setiap entri baru
}
