	private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String mealType = spMealType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in meal name, calories and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call the Firestore helper instead of SQLite
        addDataToFirestore(TEST_USER_EMAIL, mealType, name, caloriesStr, time, notes);
    }

    private void addDataToFirestore(String user, String type, String name, String calories, String time, String notes) {
        // Reference to "FoodCollection"
        com.google.firebase.firestore.CollectionReference foodRef = db.collection("FoodCollection");

        // Create the POJO object using the FoodLog class
        FoodLog foodLog = new FoodLog(user, type, name, calories, time, notes);

        foodRef.add(foodLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FoodActivity.this, "Meal saved to Cloud", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    renderStoredMeals(); // Refresh the list from Firestore
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FoodActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
	
	private void renderStoredMeals() {
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", TEST_USER_EMAIL) // Matches field name in FoodLog
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder builder = new StringBuilder();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Convert Firestore document back to FoodLog object
                        FoodLog log = document.toObject(FoodLog.class);

                        builder.append("• ")
                                .append(log.getFL_MEAL_TYPE()).append(" - ")
                                .append(log.getFL_MEAL_NAME()).append(" (")
                                .append(log.getFL_CALORIES()).append(" kcal at ")
                                .append(log.getFL_TIME()).append(")\n");
                    }

                    if (builder.length() == 0) {
                        tvMealList.setText("No meals logged yet.");
                    } else {
                        tvMealList.setText(builder.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    tvMealList.setText("Failed to load meals: " + e.getMessage());
                });
    }
