	private void fetchTotalsFromFirestore(Integer target) {
        // We need to fetch from two different collections and sum them up
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", TEST_USER_EMAIL)
                .get()
                .addOnSuccessListener(foodSnapshots -> {
                    int totalConsumed = 0;
                    for (QueryDocumentSnapshot doc : foodSnapshots) {
                        String calStr = doc.getString("fl_CALORIES");
                        if (calStr != null) totalConsumed += Integer.parseInt(calStr);
                    }

                    int finalTotalConsumed = totalConsumed;

                    // Now fetch Exercises
                    db.collection("ExerciseCollection")
                            .whereEqualTo("el_USER", TEST_USER_EMAIL)
                            .get()
                            .addOnSuccessListener(exerciseSnapshots -> {
                                int totalBurned = 0;
                                for (QueryDocumentSnapshot doc : exerciseSnapshots) {
                                    String burnStr = doc.getString("el_CALORIES_BURNED");
                                    if (burnStr != null) totalBurned += Integer.parseInt(burnStr);
                                }
                                renderTotals(finalTotalConsumed, totalBurned, target);
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show());
    }
	
	private void resetDay() {
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", TEST_USER_EMAIL)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    db.collection("ExerciseCollection")
                            .whereEqualTo("el_USER", TEST_USER_EMAIL)
                            .get()
                            .addOnSuccessListener(exerciseSnapshots -> {
                                for (QueryDocumentSnapshot doc : exerciseSnapshots) {
                                    doc.getReference().delete();
                                }

                                etDailyTarget.setText("");
                                renderTotals(0, 0, null);
                                Toast.makeText(CaloriesActivity.this, "Daily logs have been reset.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reset: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }