package ca.nanonorth.chatr.helpers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreDbHelper {
    private val rootRef = FirebaseFirestore.getInstance()


    fun updateUser(id: String, user : MutableMap<String, Any>) : String {
        val result = rootRef
                .collection("users")
                .document(id)
                .update(user)
                .addOnCompleteListener{
                    return@addOnCompleteListener
                }

        return if(result.isSuccessful){
            "User update successful!"
        }
        else{
            "Error occurred"
        }

    }
}