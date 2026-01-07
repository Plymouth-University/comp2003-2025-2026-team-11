package com.example.comp2003_prototype

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.example.comp2003_prototype.R

class CaloriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calories, container, false)

        val editTargeted = view.findViewById<TextInputEditText>(R.id.edit_targeted_calories)
        val editFood = view.findViewById<TextInputEditText>(R.id.edit_food_calories)
        val tvDeficit = view.findViewById<TextView>(R.id.tv_total_deficit)

        // Function to calculate the deficit
        val calculateDeficit = {
            val targeted = editTargeted.text.toString().toIntOrNull() ?: 0
            val food = editFood.text.toString().toIntOrNull() ?: 0
            val staticOther = 350

            val totalDeficit = (targeted + staticOther) - food

            // Displays 0 if the result is negative
            if (totalDeficit > 0) {
                tvDeficit.text = "$totalDeficit kcal"
            } else {
                tvDeficit.text = "0 kcal"
            }
        }

        // Add listeners to both input fields, meaning it will register the numbers inputted
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateDeficit()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editTargeted.addTextChangedListener(watcher)
        editFood.addTextChangedListener(watcher)

        return view
    }
}