package com.anynet.admin

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(28, 32, 28, 28)

        val title = TextView(this)
        title.text = "AnyNet Admin Login"
        title.textSize = 28f
        title.gravity = Gravity.CENTER
        root.addView(title)

        val username = EditText(this)
        username.hint = "Username"
        root.addView(username)

        val password = EditText(this)
        password.hint = "Password"
        password.inputType = 129
        root.addView(password)

        val login = Button(this)
        login.text = "LOGIN"
        root.addView(login)

        login.setOnClickListener {
            if (username.text.toString() == "admin" &&
                password.text.toString() == "1234") {

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Wrong Username or Password", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(root)
    }
}
