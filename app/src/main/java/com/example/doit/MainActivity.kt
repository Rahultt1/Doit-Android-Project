package com.example.doit

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.checkbox.MaterialCheckBox
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MainActivity : AppCompatActivity(), AddTaskDialogFragment.Callback,
    TaskAdapter.OnSwipeListener {

    private val sharedPreferencesKey = "tasks"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.lv_tasks) // Initialize recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addButton = findViewById<MaterialButton>(R.id.btn_add_task)
        val inputField = findViewById<TextInputEditText>(R.id.et_new_task)

        adapter = TaskAdapter(loadTasks(), recyclerView)
        recyclerView.adapter = adapter
        adapter.setOnSwipeListener(this) // Set the swipe listener

        addButton.setOnClickListener {
            val dialog = AddTaskDialogFragment()
            dialog.setCallback(object : AddTaskDialogFragment.Callback {
                override fun onTaskAdded(task: String) {
                    // Save the task
                    saveTask(task)
                    // Update UI
                    adapter.updateTasks(loadTasks())
                }
            })
            dialog.show(supportFragmentManager, "AddTaskDialogFragment")
        }

        //nav bar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation item clicks here
            when (menuItem.itemId) {
                R.id.nav_item1 -> {
                    // Handle item 1 click
                    true
                }
                R.id.nav_item2 -> {
                    // Handle item 2 click
                    true
                }
                // Add more cases for other menu items
                else -> false
            }
        }
    }

    override fun onSwipeLeft(position: Int) {
        // Handle left swipe action
        // For example, delete the task at the swiped position
        val task = adapter.tasks[position]
        deleteTask(task)
        adapter.notifyItemRemoved(position)
    }

    override fun onSwipeRight(position: Int) {
        // Handle right swipe action (if needed)
    }

    private fun deleteTask(task: String) {
        // Implement task deletion logic here
    }

    override fun onTaskAdded(task: String) {
        // Handle the new task here (e.g., save it, update UI)
        Toast.makeText(this, "Task added: $task", Toast.LENGTH_SHORT).show()
    }

    private fun saveTask(task: String) {
        val sharedPreferences = getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tasks = HashSet(sharedPreferences.getStringSet(sharedPreferencesKey, HashSet()))
        tasks.add(task)
        editor.putStringSet(sharedPreferencesKey, tasks)
        editor.apply()
    }

    private fun loadTasks(): ArrayList<String> {
        val sharedPreferences = getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
        val tasks = sharedPreferences.getStringSet(sharedPreferencesKey, HashSet()) ?: HashSet()
        return ArrayList(tasks)
    }
}

class TaskAdapter(var tasks: ArrayList<String>, private val recyclerView: RecyclerView) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val selectedTasks = HashSet<String>()
    private var swipeListener: OnSwipeListener? = null
    private val itemTouchHelper: ItemTouchHelper

    init {
        itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Not used for swipe
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        swipeListener?.onSwipeLeft(position)
                    }

                    ItemTouchHelper.RIGHT -> {
                        swipeListener?.onSwipeRight(position)
                    }
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTextView.text = task
        holder.checkboxTask.isChecked = selectedTasks.contains(task)

        holder.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedTasks.add(task)
            } else {
                selectedTasks.remove(task)
            }
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun updateTasks(newTasks: ArrayList<String>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    fun setOnSwipeListener(listener: OnSwipeListener) {
        swipeListener = listener
    }

    interface OnSwipeListener {
        fun onSwipeLeft(position: Int)
        fun onSwipeRight(position: Int)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
        val checkboxTask: MaterialCheckBox = itemView.findViewById(R.id.checkboxTask)
    }
}