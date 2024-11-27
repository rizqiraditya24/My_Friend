package com.example.myfriend

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myfriend.data.Friend
import com.example.myfriend.databinding.ActivityAddFriendBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddFriendBinding
    private val viewModel: FriendViewModel by viewModels() // Menggunakan Hilt untuk inject ViewModel

    private lateinit var photoFile: File
    private var oldFriend: Friend? = null
    private var idFriend: Int = 0
    private var isImageChanged = false

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePhoto()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val rotatedImage = rotateImageIfRequired(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(rotatedImage)
                isImageChanged = true
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(
                    it.data?.data ?: return@registerForActivityResult, "r"
                )
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val inputStream = FileInputStream(fileDescriptor)
                val outputStream = FileOutputStream(photoFile)

                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                parcelFileDescriptor?.close()

                val rotatedImage = rotateImageIfRequired(photoFile.absolutePath)
                binding.profileImage.setImageBitmap(rotatedImage)
                isImageChanged = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Cannot create Image File", Toast.LENGTH_SHORT).show()
            return
        }

        idFriend = intent.getIntExtra("id", 0)

        if (idFriend != 0) {
            getFriend()
        }

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.backButton.setOnClickListener {
            val destination = Intent(this, MenuHomeActivity::class.java)
            startActivity(destination)
        }

        binding.cameraButton.setOnClickListener {
            showInsertPhotoDialog()
        }
    }

    private fun showInsertPhotoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_photo, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        val fromCamera = dialogView.findViewById<TextView>(R.id.from_camera)
        val pickGallery = dialogView.findViewById<TextView>(R.id.pick_gallery)

        fromCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            alertDialog.dismiss()
        }

        pickGallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun takePhoto() {
        val photoUri = FileProvider.getUriForFile(this, "com.example.myfriend.fileprovider", photoFile)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }

        try {
            cameraLauncher.launch(cameraIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cannot use Camera", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun getFriend() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getFriendById(idFriend).collect { friend ->
                    oldFriend = friend
                    binding.etName.setText(friend?.name)
                    binding.etSchool.setText(friend?.school)
                    //binding.etBio.setText(friend?.bio)

                    if (!friend?.photoPath.isNullOrEmpty()) {
                        val photo = BitmapFactory.decodeFile(friend?.photoPath)
                        binding.profileImage.setImageBitmap(photo)
                        isImageChanged = false
                    }
                }
            }
        }
    }

    private fun showSaveDialog() {
        if (isFormValid()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Friend")
            builder.setMessage("Are you sure you want to add this friend?")
            builder.setPositiveButton("Save") { _, _ ->
                addData()
                Toast.makeText(this, "Friend saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun isFormValid(): Boolean {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Please fill in the name", Toast.LENGTH_SHORT).show()
                false
            }
            school.isEmpty() -> {
                Toast.makeText(this, "Please fill in the school", Toast.LENGTH_SHORT).show()
                false
            }
            bio.isEmpty() -> {
                Toast.makeText(this, "Please fill in the bio", Toast.LENGTH_SHORT).show()
                false
            }
            !isImageChanged -> {
                Toast.makeText(this, "Please change the image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun addData() {
        val name = binding.etName.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        //val bio = binding.etBio.text.toString().trim()

        if (oldFriend == null) {
            val data = Friend(name, school, photoFile.absolutePath, "0")
            lifecycleScope.launch {
                viewModel.insertFriend(data)
            }
        } else {
            val data = oldFriend!!.copy(
                name = name,
                school = school,
                //bio = bio,
                photoPath = photoFile.absolutePath
            ).apply {
                id = idFriend
            }

            lifecycleScope.launch {
                viewModel.editFriend(data)
            }
        }

        finish()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_", ".jpg", storageDir)
    }

    private fun rotateImageIfRequired(imagePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imagePath)
        val ei = ExifInterface(imagePath)
        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
