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

//import android.view.GestureDetector

/**
 * Created by filip on 03.05.2014.
 */

public open class Item (var project: String, var status: String, var secondsSpent: Long, var lastactivated: Long)



public class KotimeActivity() : ListActivity() {

    public var dh: DBAdapter by Delegates.notNull()
    var notes: Adapter by Delegates.notNull()
    public var list: ArrayList<Item> = ArrayList<Item>()
    public var ctx: Context by Delegates.notNull()
    var c: Cursor? = null
    public var position: Int = 0

    fun onCreate(savedInstanceState: Bundle) {

        var result: String
        var newitem: Item

        super.onCreate(savedInstanceState)


        ctx = this

        dh = DBAdapter(this)
        dh.open()
        // GET ROWS
        c = dh.getAll()

        if (c != null ) {
            c!!.moveToFirst()
            if (!c!!.isAfterLast()) {
                do {
                    val project: String? = c!!.getString(1)
                    val status: String? = c!!.getString(2)
                    val secondsSpent = c!!.getLong(3)
                    val lastactivated = c!!.getLong(4)
                    if (project == null || status == null) {
                        if (project == null) {
                            Toast.makeText(ctx, "Project is null", 10)
                        }
                        if (status == null) {
                            Toast.makeText(ctx, "Status is null", 10)
                        }
                    } else {
                        newitem = Item(project, status, secondsSpent, lastactivated)
                        list.add(newitem)
                    }

                } while (c!!.moveToNext())
            }
        }
        setContentView(R.layout.activity_kotime)

        // Gesture
        val gestureDetector = GestureDetector(MyGestureDetector())
        getListView()?.setOnTouchListener() {
            v, aEvent ->
             gestureDetector.onTouchEvent(aEvent)
        }
        //

        val addButton = findViewById(R.id.button) as Button
        // binding add project dialog to the button
        addButton.setOnClickListener() {
            // this is a listener for the button
            var alert = AlertDialog.Builder(this)
            var input = EditText(this)

            // Builder

            with (alert) {
                setTitle("Добавление проекта")
                setMessage("Назовите новый проект")
                // Add input field
                getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                setView(input)

                setPositiveButton("ОК") {
                    dialog, whichButton ->
                    result = input.getText().toString()
                    dh.insert(result, "inactive", timespent = 0, lastactivated = 0)
                    newitem = Item(result, "inactive", 0, 0)
                    list.add(newitem)
                    notes.notifyDataSetChanged()
                }
                setNegativeButton("Отмена") { dialog, whichButton -> }

            }

            // Dialog

            val dialog = alert.create()
            dialog.setOnShowListener {
                dial ->
                val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }

            dialog.show()

        }
        notes = Adapter(this, list)
        setListAdapter(notes)

    }


    fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = (getListAdapter()?.getItem(position) as Item)
        var project = item.project
        var status = item.status
        var secondsSpent = item.secondsSpent
        var lastActivated = item.lastactivated

        when (status) {
            "active" -> {

                item.status = "inactive"
                val spentThisTime = System.currentTimeMillis() - lastActivated
                item.secondsSpent = secondsSpent + spentThisTime
                list.set(id.toInt(), item)

                dh.updateSpentTimeByProject(project, item.status, item.secondsSpent)
//            Toast.makeText(this, project + " selected. Now " + (secondsSpent + spentThisTime)/1000.0, Toast.LENGTH_LONG).show();
            }
            "inactive" -> {
                item.status = "active"
                lastActivated = System.currentTimeMillis()
                item.lastactivated = lastActivated
                list.set(id.toInt(), item)
                dh.updateActivatedByProject(project, item.status, item.lastactivated)
            }
        }
        notes.notifyDataSetChanged()
    }

    class object {
        private val SWIPE_MIN_DISTANCE: Int = 150
        private val SWIPE_MAX_OFF_PATH: Int = 100
        private val SWIPE_THRESHOLD_VELOCITY: Int = 100
    }

    inner class MyGestureDetector() : SimpleOnGestureListener() {
        private var mLastOnDownEvent: MotionEvent? = null

        public override fun onDown(e: MotionEvent): Boolean {
            //Android 4.0 bug means e1 in onFling may be NULL due to onLongPress eating it.
            mLastOnDownEvent = e
            return super.onDown(e)
        }

        public fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val dX = e2.getX() - e1.getX()
            val dY = e1.getY() - e2.getY()
            val position = getListView()!!.pointToPosition(Math.round(e1.getX()), Math.round(e1.getY()))

            if (Math.abs(dY) < SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dX) >= SWIPE_MIN_DISTANCE) {
                if (dX > 0) {
                    //Swipe Right
                    val projectToDelete = list.get(position).project

                    var alert = AlertDialog.Builder(ctx)
                    with (alert) {
                        setTitle("Удаление проекта")
                        setMessage("Удаляем " + projectToDelete + "?")

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
