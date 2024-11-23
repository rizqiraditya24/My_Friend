package com.example.myfriend

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import androidx.exifinterface.media.ExifInterface
import com.example.myfriend.databinding.ActivityDetailFriendBinding

@AndroidEntryPoint
class DetailFriendActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailFriendBinding
    private lateinit var viewModel: FriendViewModel
    private var friendId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[FriendViewModel::class.java]

        // Mendapatkan ID teman dari Intent
        friendId = intent.getIntExtra("EXTRA_ID", 0)
        if (friendId == 0) {
            showToast("Invalid Friend ID received.")
            finish()
            return
        }

        // Memuat data teman
        loadFriendData(friendId)

        setupListeners()
        setDrawableIcons()
    }

    // Memuat data teman dari database
    private fun loadFriendData(id: Int) {
        lifecycleScope.launch {
            viewModel.getFriendById(id).collect { friend ->
                if (friend == null) {
                    showToast("No friend found with ID: $id")
                    finish()
                    return@collect
                }

                binding.tvName.text = friend.name
                binding.tvSchool.text = friend.school
                binding.tvBio.text = friend.bio

                // Menampilkan foto jika tersedia
                val photoFile = friend.photoPath?.let { File(it) }
                if (photoFile?.exists() == true) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    binding.profileImage.setImageBitmap(getOrientedBitmap(photoFile.absolutePath, bitmap))
                } else {
                    binding.profileImage.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    // Mengatur ikon drawable pada TextView
    private fun setDrawableIcons() {
        val drawables = listOf(
            R.drawable.ic_person to binding.tvName,
            R.drawable.ic_school to binding.tvSchool,
            R.drawable.ic_info to binding.tvBio
        )

        val size = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._23sdp)

        drawables.forEach { (drawableId, textView) ->
            ContextCompat.getDrawable(this, drawableId)?.apply {
                setBounds(0, 0, size, size)
                textView.setCompoundDrawablesRelative(this, null, null, null)
            }
        }
    }

    // Mendapatkan bitmap yang telah dirotasi jika diperlukan
    private fun getOrientedBitmap(filePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        return if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    // Setup tombol navigasi dan aksi
    private fun setupListeners() {
        binding.editButton.setOnClickListener {
            navigateToEditActivity()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    // Navigasi ke EditFriendActivity
    private fun navigateToEditActivity() {
        val intent = Intent(this, EditFriendActivity::class.java).apply {
            putExtra("EXTRA_ID", friendId)
        }
        startActivity(intent)
    }

    // Menampilkan dialog konfirmasi penghapusan
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove this friend?")
            .setPositiveButton("Remove") { _, _ -> deleteFriend() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Menghapus data teman
    private fun deleteFriend() {
        lifecycleScope.launch {
            viewModel.getFriendById(friendId).collect { friend ->
                if (friend != null) {
                    viewModel.deleteFriend(friend)
                    showToast("Friend deleted.")
                    finish()
                } else {
                    showToast("No friend found with ID: $friendId")
                }
            }
        }
    }

    // Menampilkan Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
