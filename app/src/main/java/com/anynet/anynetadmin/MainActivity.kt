package com.anynet.admin

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {
private val supabaseUrl = "https://taiwpchtowqhwzfwikxk.supabase.co"
private val supabaseKey = "sb_publishable_98ujQazly_wFvActgPUC_A_WWL2HIc9"
    private val prefs by lazy { getSharedPreferences("AnyNetData", MODE_PRIVATE) }
    private val channelId = "anynet_due"

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        createNotificationChannel()
        requestNotificationPermission()
        showLogin()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AnyNet Due Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Customer payment due reminders"
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    private fun makeRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 30, 28, 28)
        }
    }

    private fun title(text: String, size: Float = 26f): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18)
        }
    }

    private fun button(text: String): Button = Button(this).apply { this.text = text }

    private fun field(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        setPadding(12, 8, 12, 8)
    }

    private fun showLogin() {
        val root = makeRoot()
        root.addView(title("AnyNet Admin Login", 28f))
        val username = field("Username")
        val password = field("Password")
        password.inputType = 129
        root.addView(username)
        root.addView(password)
        val login = button("LOGIN")
        root.addView(login)

        login.setOnClickListener {
            if (username.text.toString().trim() == "admin" && password.text.toString() == "1234") {
                showDashboard()
            } else {
                Toast.makeText(this, "Wrong Username or Password", Toast.LENGTH_SHORT).show()
            }
        }
        setContentView(root)
    }

    private fun showDashboard() {
        val root = makeRoot()
        root.addView(title("AnyNet Admin", 30f))

        val customers = getCustomers()
        val payments = getPayments()
     var paid = 0
for (i in 0 until customers.length()) {
    if (customers.getJSONObject(i).optBoolean("paid")) paid++
}
val unpaid = customers.length() - paid
        val summary = TextView(this).apply {
            text = "Customers: ${customers.length()}    Paid: $paid    Unpaid: $unpaid\nPayments: ${payments.length()}"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 18)
        }
        root.addView(summary)

        val customerBtn = button("CUSTOMERS")
        val paymentBtn = button("PAYMENTS")
        val dueBtn = button("DUE TODAY")
        val reminderBtn = button("SEND DUE REMINDER")
        val logoutBtn = button("LOGOUT")
        root.addView(customerBtn)
        root.addView(paymentBtn)
        root.addView(dueBtn)
        root.addView(reminderBtn)
        root.addView(logoutBtn)

        customerBtn.setOnClickListener { showCustomers() }
        paymentBtn.setOnClickListener { showPayments() }
        dueBtn.setOnClickListener { showDueToday() }
        reminderBtn.setOnClickListener { sendDueReminder() }
        logoutBtn.setOnClickListener { showLogin() }
        setContentView(root)
    }

    private fun getCustomers(): JSONArray = JSONArray(prefs.getString("customers", "[]"))
    private fun saveCustomers(data: JSONArray) = prefs.edit().putString("customers", data.toString()).apply()
    private fun getPayments(): JSONArray = JSONArray(prefs.getString("payments", "[]"))
    private fun savePayments(data: JSONArray) = prefs.edit().putString("payments", data.toString()).apply()

    private fun showCustomers() {
        val root = makeRoot()
        root.gravity = Gravity.TOP
        root.addView(title("Customer Management", 26f))

        val name = field("Customer Name *")
        val account = field("Starlink Account *")
        val phone = field("Phone Number")
        val fee = field("Monthly Fee *")
        val due = field("Due Date (YYYY-MM-DD) *")
        root.addView(name)
        root.addView(account)
        root.addView(phone)
        root.addView(fee)
        root.addView(due)

        val save = button("SAVE CUSTOMER")
        val list = TextView(this).apply { textSize = 17f; setPadding(0, 18, 0, 10) }
        val back = button("BACK")
        root.addView(save)
        root.addView(list)
        root.addView(back)

        fun refresh() {
            val data = getCustomers()
            val out = StringBuilder()
            if (data.length() == 0) out.append("No customers yet.")
            for (i in 0 until data.length()) {
                val c = data.getJSONObject(i)
                out.append("${i + 1}. ${c.optString("name")}\n")
                out.append("Account: ${c.optString("account")}\n")
                out.append("Phone: ${c.optString("phone")}\n")
                out.append("Fee: ${c.optString("fee")}\n")
                out.append("Due: ${c.optString("due")}\n")
                out.append("Status: ${if (c.optBoolean("paid")) "PAID" else "UNPAID"}\n")
                out.append("\n")
            }
            list.text = out.toString()
        }

        save.setOnClickListener {
            val n = name.text.toString().trim()
            val a = account.text.toString().trim()
            val f = fee.text.toString().trim()
            val d = due.text.toString().trim()
            if (n.isEmpty() || a.isEmpty() || f.isEmpty() || d.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val data = getCustomers()
            data.put(JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("name", n)
                put("account", a)
                put("phone", phone.text.toString().trim())
                put("fee", f)
                put("due", d)
                put("paid", false)
            })
            saveCustomers(data)
            name.text.clear(); account.text.clear(); phone.text.clear(); fee.text.clear(); due.text.clear()
            refresh()
            Toast.makeText(this, "Customer Saved", Toast.LENGTH_SHORT).show()
        }

        list.setOnClickListener { showCustomerActions() }
        back.setOnClickListener { showDashboard() }
        refresh()
        setContentView(root)
    }

    private fun showCustomerActions() {
        val data = getCustomers()
        if (data.length() == 0) return
        val names = Array(data.length()) { i -> data.getJSONObject(i).optString("name") }
        AlertDialog.Builder(this)
            .setTitle("Select Customer")
            .setItems(names) { _, which -> showCustomerMenu(which) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomerMenu(index: Int) {
        val data = getCustomers()
        if (index >= data.length()) return
        val c = data.getJSONObject(index)
        val paid = c.optBoolean("paid")
        val actions = arrayOf("Mark ${if (paid) "UNPAID" else "PAID"}", "Delete Customer")
        AlertDialog.Builder(this)
            .setTitle(c.optString("name"))
            .setItems(actions) { _, which ->
                if (which == 0) {
                    c.put("paid", !paid)
                    data.put(index, c)
                    saveCustomers(data)
                    if (!paid) recordPayment(c)
                    Toast.makeText(this, if (!paid) "Marked PAID" else "Marked UNPAID", Toast.LENGTH_SHORT).show()
                    showCustomers()
                } else {
                    data.remove(index)
                    saveCustomers(data)
                    Toast.makeText(this, "Customer Deleted", Toast.LENGTH_SHORT).show()
                    showCustomers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun recordPayment(customer: JSONObject) {
        val data = getPayments()
        data.put(JSONObject().apply {
            put("date", today())
            put("customer", customer.optString("name"))
            put("account", customer.optString("account"))
            put("amount", customer.optString("fee"))
        })
        savePayments(data)
    }

    private fun showPayments() {
        val root = makeRoot()
        root.gravity = Gravity.TOP
        root.addView(title("Payment History", 26f))
        val list = TextView(this).apply { textSize = 17f }
        val back = button("BACK")
        root.addView(list)
        root.addView(back)

        val data = getPayments()
        val out = StringBuilder()
        if (data.length() == 0) out.append("No payments yet.")
        for (i in data.length() - 1 downTo 0) {
            val p = data.getJSONObject(i)
            out.append("${i + 1}. ${p.optString("customer")}\n")
            out.append("Account: ${p.optString("account")}\n")
            out.append("Amount: ${p.optString("amount")}\n")
            out.append("Date: ${p.optString("date")}\n\n")
        }
        list.text = out.toString()
        back.setOnClickListener { showDashboard() }
        setContentView(root)
    }

    private fun showDueToday() {
        val root = makeRoot()
        root.gravity = Gravity.TOP
        root.addView(title("Due Today", 26f))
        val list = TextView(this).apply { textSize = 18f }
        val back = button("BACK")
        root.addView(list)
        root.addView(back)

        val today = today()
        val data = getCustomers()
        val out = StringBuilder()
        var count = 0
        for (i in 0 until data.length()) {
            val c = data.getJSONObject(i)
            if (c.optString("due") == today && !c.optBoolean("paid")) {
                count++
                out.append("${c.optString("name")}\n")
                out.append("Phone: ${c.optString("phone")}\n")
                out.append("Fee: ${c.optString("fee")}\n")
                out.append("Account: ${c.optString("account")}\n\n")
            }
        }
        if (count == 0) out.append("No unpaid customers due today.")
        else out.insert(0, "$count customer(s) due today.\n\n")
        list.text = out.toString()
        back.setOnClickListener { showDashboard() }
        setContentView(root)
    }

    private fun sendDueReminder() {
        val data = getCustomers()
        val due = data.filterObjects { it.optString("due") == today() && !it.optBoolean("paid") }
        if (due.isEmpty()) {
            Toast.makeText(this, "No unpaid customers due today", Toast.LENGTH_SHORT).show()
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            val notification = android.app.Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("AnyNet Payment Reminder")
                .setContentText("${due.size} customer(s) have payment due today")
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()
            manager.notify(2001, notification)
            Toast.makeText(this, "Reminder sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please allow notifications", Toast.LENGTH_SHORT).show()
        }
    }

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun JSONArray.filterObjects(predicate: (JSONObject) -> Boolean): List<JSONObject> {
        val result = mutableListOf<JSONObject>()
        for (i in 0 until length()) {
            val obj = getJSONObject(i)
            if (predicate(obj)) result.add(obj)
        }
        return result
    }
}
