package com.example.comp2003_prototype

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.comp2003_prototype.R

class FoodFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_food, container, false)

        setupAutoCalorie(view.findViewById(R.id.edit_breakfast), view.findViewById(R.id.tv_breakfast_cal), "egg", 80)
        setupAutoCalorie(view.findViewById(R.id.edit_lunch), view.findViewById(R.id.tv_lunch_cal), "chicken salad", 350)
        setupAutoCalorie(view.findViewById(R.id.edit_dinner), view.findViewById(R.id.tv_dinner_cal), "steak", 600)
        setupAutoCalorie(view.findViewById(R.id.edit_snack), view.findViewById(R.id.tv_snack_cal), "apple", 95)

        return view
    }
    // Calorie Maths displaying each calorie seperately
    private fun setupAutoCalorie(input: EditText, display: TextView, target: String, calories: Int) {
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().lowercase() == target) {
                    display.text = "$calories kcal"
                } else {
                    display.text = "0 kcal"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}