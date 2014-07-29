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
import java.lang.String.format


class Adapter(val context: Context, val data: List<Item>) : BaseAdapter() {

    override fun getView(position: Int, convertVw: View?, parent: ViewGroup): View? {

        val cItem = data[position]
        val inflater = LayoutInflater.from(context)
        val convertView = convertVw ?: inflater.inflate(R.layout.list_item, parent, false)

        convertView?.setBackgroundColor(if (cItem.status == "active") Color.GREEN else Color.RED)

        val project = convertView?.findViewById(R.id.project) as TextView

        project.setText(cItem.project)
        val secondsSpent = convertView?.findViewById(R.id.secondsspent) as TextView
        //        secondsSpent.setText(java.lang.Double.toString(cItem.secondsSpent.toDouble() / 1000.0))
        val hours = TimeUnit.MILLISECONDS.toHours(cItem.secondsSpent)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(cItem.secondsSpent) -
                TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(cItem.secondsSpent) -
                TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
        secondsSpent.setText("${format("%02d", hours)}:" +
                "${format("%02d", minutes)}:${format("%02d", seconds)}")
        return convertView
    }

    override fun getCount() = data.size()

    override fun getItem(num: Int) = data.get(num)

    override fun getItemId(arg0: Int) = arg0.toLong()

}