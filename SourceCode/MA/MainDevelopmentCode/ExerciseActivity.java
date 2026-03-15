	private void saveExercise() {
        String type = spExerciseType.getSelectedItem().toString();
        String intensity = spIntensity.getSelectedItem().toString();
        String durationStr = etDuration.getText().toString().trim();
        String time = etExerciseTime.getText().toString().trim();
        String notes = etExerciseNotes.getText().toString().trim();

        if (TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in duration and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        int caloriesBurned = estimateCaloriesBurned(type, intensity, Integer.parseInt(durationStr));

        // Call Firestore instead of SQLite
        addDataToFirestore(TEST_USER_EMAIL, type, intensity, durationStr,
                String.valueOf(caloriesBurned), time, notes);
    }

    private void addDataToFirestore(String user, String type, String intensity, String duration, String calories, String time, String notes) {
        CollectionReference dbExercises = db.collection("ExerciseCollection");

        ExerciseLog exerciseLog = new ExerciseLog(user, type, intensity, duration, calories, time, notes);

        dbExercises.add(exerciseLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ExerciseActivity.this, "Exercise saved to Cloud", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    renderStoredExercises(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExerciseActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderStoredExercises() {
        db.collection("ExerciseCollection")
                .whereEqualTo("el_USER", TEST_USER_EMAIL) // Filter by user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder builder = new StringBuilder();

                    // Iterate through the documents returned
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ExerciseLog log = document.toObject(ExerciseLog.class);

                        builder.append("• ")
                                .append(log.getEL_EXERCISE_TYPE()).append(" (")
                                .append(log.getEl_INTENSITY()).append(") - ")
                                .append(log.getEL_DURATION_MINS()).append(" mins at ")
                                .append(log.getEL_TIME())
                                .append(" | ~").append(log.getEL_CALORIES_BURNED()).append(" kcal\n");
                    }

                    if (builder.length() == 0) {
                        tvExerciseList.setText("No exercises logged yet.");
                    } else {
                        tvExerciseList.setText(builder.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    tvExerciseList.setText("Failed to load data.");
                });
    }