package com.app.mytodo

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.app.mytodo.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it.not()) {
                Toast.makeText(this, "Need permission to start reminder", Toast.LENGTH_SHORT).show()
            }
        }

        if (hasNotificationPermission().not()) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        with(binding) {
            navigation.setOnItemSelectedListener {
                TransitionManager.beginDelayedTransition(window.decorView as ViewGroup, Fade())
                donelistview.isVisible = (it.itemId == R.id.notdone).not()
                listview.isVisible = it.itemId == R.id.notdone
                if (it.itemId == R.id.notdone) {
                    addview.hide()
                } else {
                    addview.show()
                }
                true
            }

            listview.adapter = TodoAdapter(this@MainActivity)
            donelistview.adapter = DoneTodoAdapter(this@MainActivity)

            addview.setOnClickListener {
                EditFragment().show(supportFragmentManager, EditFragment::class.java.name)
            }
        }
    }
}