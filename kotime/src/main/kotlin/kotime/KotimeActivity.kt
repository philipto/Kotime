package com.philipto.kotime.kotime;

import android.os.Bundle
import android.widget.ListView
import android.view.View
import android.app.ListActivity
import java.util.ArrayList
import android.widget.Toast
import android.content.Context
import kotlin.properties.Delegates
import android.widget.Button
import android.widget.EditText
import android.database.Cursor
import android.app.AlertDialog
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import org.apache.commons.lang3.StringEscapeUtils


class Item (val project: String, var status: String, var secondsSpent: Long, var lastactivated: Long)

class KotimeActivity() : ListActivity() {

    private var dh: DBAdapter by Delegates.notNull()
    private var notes: Adapter by Delegates.notNull()
    private val list = ArrayList<Item>()
    private var ctx: Context by Delegates.notNull()

    private var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        ctx = this

        dh = DBAdapter(this)
        dh.open()
        // GET ROWS
        val c = dh.getAll()

        if (c != null ) {
            c.moveToFirst()
            if (!c.isAfterLast()) {
                do {
                    val project = StringEscapeUtils.unescapeJava(c.getString(1))
                    val status = c.getString(2)
                    if (project == null || status == null) {
                        if (project == null) {
                            Toast.makeText(ctx, "Project is null", 10)
                        }
                        if (status == null) {
                            Toast.makeText(ctx, "Status is null", 10)
                        }
                    } else {
                        val secondsSpent = c.getLong(3)
                        val lastActivated = c.getLong(4)
                        list.add(Item(project, status, secondsSpent, lastActivated))
                    }

                } while (c.moveToNext())
            }
        }
        setContentView(R.layout.activity_kotime)

        // Gesture
        val gestureDetector = GestureDetector(MyGestureDetector())
        getListView()?.setOnTouchListener() {
            v, aEvent ->
            gestureDetector.onTouchEvent(aEvent)
        }

        val addButton = findViewById(R.id.button) as Button

        // binding adds project dialog to the button

        addButton.setOnClickListener() {
            // this is a listener for the button
            val alert = AlertDialog.Builder(this)
            val input = EditText(this)

            // Builder

            with (alert) {
                setTitle("Добавление проекта")
                setMessage("Назовите новый проект")
                // Add input field
                getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                setView(input)

                setPositiveButton("ОК") {
                    dialog, whichButton ->
                    val result = input.getText().toString()
                    // next row: one can use named parameters to increase readability or to change default parameters
                    dh.insert(result, "inactive", timespent = 0, lastactivated = 0)
                    list.add(Item(result, "inactive", 0, 0))
                    notes.notifyDataSetChanged()
                }
                setNegativeButton("Отмена") { dialog, whichButton -> }

            }

            // Dialog

            val dialog = alert.create()
            dialog.setOnShowListener {
                dial ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }

            dialog.show()
        }
        notes = Adapter(this, list)
        setListAdapter(notes)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val item = (getListAdapter()?.getItem(position) as Item)
        val project = item.project

        when (item.status) {
            "active" -> {
                item.status = "inactive"
                val spentThisTime = System.currentTimeMillis() - item.lastactivated
                item.secondsSpent = item.secondsSpent + spentThisTime
                list.set(id.toInt(), item)
                dh.updateSpentTimeByProject(project, item.status, item.secondsSpent)
            }
            "inactive" -> {
                item.status = "active"
                item.lastactivated = System.currentTimeMillis()
                list.set(id.toInt(), item)
                dh.updateActivatedByProject(project, item.status, item.lastactivated)
            }
        }
        notes.notifyDataSetChanged()
    }

    class object {
        private val SWIPE_MIN_DISTANCE = 150
        private val SWIPE_MAX_OFF_PATH = 100
        private val SWIPE_THRESHOLD_VELOCITY = 100
    }

    inner class MyGestureDetector() : SimpleOnGestureListener() {
        private var mLastOnDownEvent: MotionEvent? = null

        override fun onDown(e: MotionEvent): Boolean {
            //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
            mLastOnDownEvent = e
            return super.onDown(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if ( e1 == null || e2 == null) return false
            val dX = e2.getX() - e1.getX()
            val dY = e1.getY() - e2.getY()
            val position = getListView()!!.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY()))

            if (Math.abs(dY) < SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dX) >= SWIPE_MIN_DISTANCE) {
                if (dX > 0) {
                    //Swipe Right
                    val projectToDelete = list[position].project

                    val alert = AlertDialog.Builder(ctx)
                    with (alert) {
                        setTitle("Удаление проекта")
                        setMessage("Удаляем $projectToDelete?")
                        // string template
                        setPositiveButton("ОК") {
                            dialog, whichButton ->
                            dh.deleteByProject(projectToDelete)
                            list.remove(position)
                            notes.notifyDataSetChanged()
                        }
                        setNegativeButton("Отмена") { dialog, whichButton -> }
                        create()
                        show()
                    }
                } else {
                    // Swipe Left
                }
                return true
            } else
                if (Math.abs(dX) < SWIPE_MAX_OFF_PATH && Math.abs(velocityY) >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dY) >= SWIPE_MIN_DISTANCE) {
                    if (dY > 0) {
                        // Swipe UP
                    } else {
                        // Swipe DOWN
                    }
                    return true
                }
            return false
        }
    }
}
