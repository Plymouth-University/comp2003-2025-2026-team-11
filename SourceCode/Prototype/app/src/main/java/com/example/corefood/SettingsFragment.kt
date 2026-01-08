package com.example.corefood

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.corefood.R
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        auth = FirebaseAuth.getInstance()

        val btnReset = view.findViewById<Button>(R.id.btn_reset_password)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        // Show toast for password reset (NOT IMPLEMENTED)
        btnReset.setOnClickListener {
            Toast.makeText(requireContext(), "Reset Password: Not Implemented", Toast.LENGTH_SHORT)
                .show()
        }

        // This takes the user back to LoginActivity / the Main Login Screen
        btnLogout.setOnClickListener {
            //Sign out the account
            auth.signOut()

            val intent = Intent(activity, Login::class.java)
            // This flag clears the activity history so they can't "Go Back" into the app
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}