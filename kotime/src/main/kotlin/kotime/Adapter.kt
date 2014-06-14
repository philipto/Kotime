package com.philipto.kotime.kotime;

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.ArrayList
import java.util.concurrent.TimeUnit


public class Adapter(var context: Context, arr: ArrayList<Item>) : BaseAdapter() {

    var data: ArrayList<Item> = ArrayList<Item>()

    override fun getView(position: Int, convertVw: View?, parent: ViewGroup): View? {

        val cItem = data.get(position)
        var convertView = convertVw

        val inflater = LayoutInflater.from(context)
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false)
        }
        if (cItem.status == "active") {
            convertView?.setBackgroundColor(Color.GREEN)

        } else {
            convertView?.setBackgroundColor(Color.RED)
        }
        val project = (convertView?.findViewById(R.id.project) as TextView)
        project.setText(cItem.project)
        val secondsspent = convertView?.findViewById(R.id.secondsspent) as TextView
        //        secondsspent.setText(java.lang.Double.toString(cItem.secondsSpent.toDouble() / 1000.0))
        val hours = TimeUnit.MILLISECONDS.toHours(cItem.secondsSpent)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(cItem.secondsSpent) -
        TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(cItem.secondsSpent) -
        TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
        secondsspent.setText(java.lang.String.format("%02d",hours) + ":" +
        java.lang.String.format("%02d",minutes) + ":" +
        java.lang.String.format("%02d",seconds))
        return convertView
    }

    override fun getCount(): Int {
        // TODO Auto-generated method stub
        return data.size()
    }

    override fun getItem(num: Int): Any {
        // TODO Auto-generated method stub
        return data.get(num)
    }

    override fun getItemId(arg0: Int): Long {
        return arg0.toLong()
    }

    {
        data = arr
        this.context = context
    }

}