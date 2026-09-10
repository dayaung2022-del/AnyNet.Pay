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

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.anynet_logo)
        logo.adjustViewBounds = true
        root.addView(logo, LinearLayout.LayoutParams(-1, 320))

        val title = TextView(this)
        title.text = "AnyNet Admin"
        title.textSize = 26f
        title.gravity = Gravity.CENTER
        root.addView(title)

        val sub = TextView(this)
        sub.text = "Starlink Payment Management"
        sub.textSize = 17f
        sub.gravity = Gravity.CENTER
        root.addView(sub)

        val b0 = Button(this); b0.text = "Admin Login"; root.addView(b0)
        val b1 = Button(this); b1.text = "Customers"; root.addView(b1)
        val b2 = Button(this); b2.text = "Payments"; root.addView(b2)
        val b3 = Button(this); b3.text = "Due Today"; root.addView(b3)

        setContentView(root)
    }
}
